package cn.sl.ehub.console.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 电网上送质量核查服务。查询以标准分钟量测为基础，优先使用历史快照确定参与范围。
 */
@Service
public class GridDeliveryQualityService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final JdbcTemplate jdbcTemplate;

    @Value("${grid.delivery.reconciliation.exact-tolerance-kw:0.0001}")
    private BigDecimal reconciliationExactToleranceKw;

    @Value("${grid.delivery.reconciliation.precision-base-kw:0.05}")
    private BigDecimal reconciliationPrecisionBaseKw;

    @Value("${grid.delivery.reconciliation.precision-per-single-kw:0.005}")
    private BigDecimal reconciliationPrecisionPerSingleKw;

    public GridDeliveryQualityService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> summary(String aggregatorId, String startDate, String endDate,
                                       String channelNo) {
        return summary(aggregatorId, startDate, endDate, channelNo, null);
    }

    public Map<String, Object> summary(String aggregatorId, String startDate, String endDate,
                                       String channelNo, String resourceTypeId) {
        channelNo = StringUtils.defaultString(channelNo);
        DateRange range = DateRange.of(startDate, endDate);
        List<Map<String, Object>> totalRows = totalRows(aggregatorId, range, channelNo, resourceTypeId);
        List<Map<String, Object>> singleRows = new ArrayList<>();
        List<Participant> participants = Collections.emptyList();
        int singleExpected = 0;
        LocalDateTime dayCursor = range.start;
        while (dayCursor.isBefore(range.end)) {
            DateRange dayRange = new DateRange(dayCursor, dayCursor.plusDays(1));
            participants = participants(aggregatorId, dayCursor, resourceTypeId);
            singleRows.addAll(singleRows(aggregatorId, dayRange, channelNo, resourceTypeId, participants));
            singleExpected += 96 * participants.size();
            dayCursor = dayCursor.plusDays(1);
        }
        List<Map<String, Object>> reconciliation = reconcile(range, totalRows, singleRows);

        int totalExpected = (int) (range.days() * 1440L);
        int totalActual = countPrepared(totalRows);
        int singleActual = countPrepared(singleRows);
        int reconcileExpected = (int) (range.days() * 96L);
        int reconcileMatched = 0;
        for (Map<String, Object> row : reconciliation) {
            if ("MATCH".equals(row.get("status"))) {
                reconcileMatched++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("aggregatorId", aggregatorId);
        result.put("resourceTypeId", resourceTypeId);
        result.put("startDate", range.start.toLocalDate().format(DATE));
        result.put("endDate", range.end.minusSeconds(1).toLocalDate().format(DATE));
        result.put("days", range.days());
        result.put("participantCount", participants.size());
        result.put("totalExpected", totalExpected);
        result.put("totalActual", totalActual);
        result.put("totalMissing", totalExpected - totalActual);
        result.put("totalRate", rate(totalActual, totalExpected));
        result.put("totalSendFailed", count(totalRows, "SEND_FAILED"));
        result.put("singleExpected", singleExpected);
        result.put("singleActual", singleActual);
        result.put("singleMissing", singleExpected - singleActual);
        result.put("singleRate", rate(singleActual, singleExpected));
        result.put("singleSendFailed", count(singleRows, "SEND_FAILED"));
        result.put("reconcileExpected", reconcileExpected);
        result.put("reconcileMatched", reconcileMatched);
        result.put("reconcileMismatch", reconcileExpected - reconcileMatched);
        result.put("reconcileRate", rate(reconcileMatched, reconcileExpected));
        result.put("participants", participantsToMap(participants));
        return result;
    }

    public List<Map<String, Object>> daily(String aggregatorId, String date, String type,
                                           String channelNo) {
        return daily(aggregatorId, date, type, channelNo, null);
    }

    public List<Map<String, Object>> daily(String aggregatorId, String date, String type,
                                           String channelNo, String resourceTypeId) {
        channelNo = StringUtils.defaultString(channelNo);
        DateRange range = DateRange.of(date, date);
        List<Participant> participants = participants(aggregatorId, range.start, resourceTypeId);
        if ("SINGLE".equalsIgnoreCase(type)) {
            return singleRows(aggregatorId, range, channelNo, resourceTypeId, participants);
        }
        return totalRows(aggregatorId, range, channelNo, resourceTypeId);
    }

    public List<Map<String, Object>> dailyAllSingles(String aggregatorId, String date, String channelNo,
                                                     String resourceTypeId) {
        DateRange range = DateRange.of(date, date);
        return singleRows(aggregatorId, range, StringUtils.defaultString(channelNo), resourceTypeId,
                allSingleScopes(aggregatorId, range.start, resourceTypeId));
    }

    public List<Map<String, Object>> participationScope(String aggregatorId, String date, String resourceTypeId) {
        DateRange range = DateRange.of(date, date);
        return participantsToMap(allSingleScopes(aggregatorId, range.start, resourceTypeId));
    }

    public Map<String, Object> dailyPage(String aggregatorId, String date, String type, String channelNo,
                                         String status, String singleCode, int pageIndex, int pageSize) {
        return dailyPage(aggregatorId, date, type, channelNo, null, status, singleCode, pageIndex, pageSize);
    }

    public Map<String, Object> dailyPage(String aggregatorId, String date, String type, String channelNo,
                                         String resourceTypeId, String status, String singleCode, int pageIndex, int pageSize) {
        channelNo = StringUtils.defaultString(channelNo);
        DateRange range = DateRange.of(date, date);
        List<Map<String, Object>> rows;
        if ("SINGLE".equalsIgnoreCase(type)) {
            List<Participant> participants = participants(aggregatorId, range.start, resourceTypeId);
            if (StringUtils.isNotBlank(singleCode)) {
                List<Participant> matched = new ArrayList<>();
                for (Participant participant : participants) {
                    if (StringUtils.containsIgnoreCase(participant.singleCode, singleCode.trim())) {
                        matched.add(participant);
                    }
                }
                participants = matched;
            }
            rows = singleRows(aggregatorId, range, channelNo, resourceTypeId, participants);
        } else {
            rows = totalRows(aggregatorId, range, channelNo, resourceTypeId);
        }
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            if (StringUtils.isNotBlank(status) && !status.equals(row.get("status"))) {
                continue;
            }
            if (StringUtils.isNotBlank(singleCode)
                    && !StringUtils.containsIgnoreCase(string(row.get("singleCode")), singleCode.trim())) {
                continue;
            }
            filtered.add(row);
        }
        int safePage = Math.max(1, pageIndex);
        int safeSize = Math.max(20, Math.min(500, pageSize));
        int from = Math.min((safePage - 1) * safeSize, filtered.size());
        int to = Math.min(from + safeSize, filtered.size());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", new ArrayList<>(filtered.subList(from, to)));
        result.put("total", filtered.size());
        result.put("pageIndex", safePage);
        result.put("pageSize", safeSize);
        return result;
    }

    public List<Map<String, Object>> reconciliation(String aggregatorId, String date, String channelNo) {
        return reconciliation(aggregatorId, date, channelNo, null);
    }

    public List<Map<String, Object>> reconciliation(String aggregatorId, String date, String channelNo,
                                                    String resourceTypeId) {
        channelNo = StringUtils.defaultString(channelNo);
        DateRange range = DateRange.of(date, date);
        List<Participant> participants = participants(aggregatorId, range.start, resourceTypeId);
        List<Map<String, Object>> result = reconcile(range, totalRows(aggregatorId, range, channelNo, resourceTypeId),
                singleRows(aggregatorId, range, channelNo, resourceTypeId, participants));
        persistReconciliation(aggregatorId, range, channelNo, resourceTypeId, result);
        return result;
    }

    public List<Map<String, Object>> participation(String aggregatorId, String date) {
        return participation(aggregatorId, date, null);
    }

    public List<Map<String, Object>> participation(String aggregatorId, String date, String resourceTypeId) {
        DateRange range = DateRange.of(date, date);
        return participantsToMap(participants(aggregatorId, range.start, resourceTypeId));
    }

    public Map<String, Object> dailyOverview(String aggregatorId, String date, String channelNo,
                                             String resourceTypeId) {
        DateRange range = DateRange.of(date, date);
        LocalDate dataDate = range.start.toLocalDate();
        LocalDateTime cutoff = dataDate.isBefore(LocalDate.now()) ? range.end
                : dataDate.isAfter(LocalDate.now()) ? range.start
                : LocalDateTime.now().withSecond(0).withNano(0);
        List<Participant> allSingles = allSingleScopes(aggregatorId, range.start, resourceTypeId);
        List<Participant> participants = new ArrayList<>();
        for (Participant item : allSingles) {
            if (item.participating) participants.add(item);
        }
        List<Map<String, Object>> totals = before(totalRows(aggregatorId, range,
                StringUtils.defaultString(channelNo), resourceTypeId), cutoff);
        List<Map<String, Object>> singles = before(singleRows(aggregatorId, range,
                StringUtils.defaultString(channelNo), resourceTypeId, allSingles), cutoff);
        List<Map<String, Object>> participantRows = new ArrayList<>();
        for (Map<String, Object> row : singles) {
            if (Boolean.TRUE.equals(row.get("participating"))) participantRows.add(row);
        }
        List<Map<String, Object>> reconciliation = before(reconcile(range, totals, participantRows), cutoff);

        Map<String, SingleStats> stats = new LinkedHashMap<>();
        for (Participant item : allSingles) stats.put(item.singleCode, new SingleStats(item));
        for (Map<String, Object> row : singles) {
            SingleStats item = stats.get(string(row.get("singleCode")));
            if (item == null) continue;
            item.expected++;
            if (hasPreparedValue(row.get("status"))) item.actual++;
            if (row.get("value") != null) item.lastTime = string(row.get("time"));
        }
        List<Map<String, Object>> participantDetails = new ArrayList<>();
        List<Map<String, Object>> nonParticipantDetails = new ArrayList<>();
        int completeParticipants = 0;
        for (SingleStats item : stats.values()) {
            Map<String, Object> detail = item.toMap();
            if (item.participant.participating) {
                participantDetails.add(detail);
                if (item.expected > 0 && item.actual == item.expected) completeParticipants++;
            } else {
                nonParticipantDetails.add(detail);
            }
        }
        int totalActual = countPrepared(totals);
        int singleActual = countPrepared(participantRows);
        int totalSendFailed = count(totals, "SEND_FAILED");
        int singleSendFailed = count(participantRows, "SEND_FAILED");
        int matched = count(reconciliation, "MATCH");
        int precision = count(reconciliation, "PRECISION_DIFFERENCE");
        int mismatch = count(reconciliation, "MISMATCH");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date", date);
        result.put("cutoff", cutoff.format(DATE_TIME));
        result.put("totalExpected", totals.size());
        result.put("totalActual", totalActual);
        result.put("totalMissing", totals.size() - totalActual);
        result.put("totalSendFailed", totalSendFailed);
        result.put("totalDeliverySuccess", count(totals, "NORMAL"));
        result.put("totalRate", rate(totalActual, totals.size()));
        result.put("singleExpected", participantRows.size());
        result.put("singleActual", singleActual);
        result.put("singleMissing", participantRows.size() - singleActual);
        result.put("singleSendFailed", singleSendFailed);
        result.put("singleDeliverySuccess", count(participantRows, "NORMAL"));
        result.put("singleRate", rate(singleActual, participantRows.size()));
        result.put("reconcileExpected", reconciliation.size());
        result.put("reconcileMatched", matched);
        result.put("reconcilePrecision", precision);
        result.put("reconcileMismatch", mismatch);
        result.put("reconcileUnavailable", reconciliation.size() - matched - precision - mismatch);
        result.put("reconcileRate", rate(matched + precision, reconciliation.size()));
        result.put("participantCount", participantDetails.size());
        result.put("nonParticipantCount", nonParticipantDetails.size());
        result.put("completeParticipantCount", completeParticipants);
        result.put("incompleteParticipantCount", participantDetails.size() - completeParticipants);
        result.put("participants", participantDetails);
        result.put("nonParticipants", nonParticipantDetails);
        return result;
    }

    private List<Map<String, Object>> before(List<Map<String, Object>> rows, LocalDateTime cutoff) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            if (toLocalDateTime(row.get("time")).isBefore(cutoff)) result.add(row);
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createSnapshot(String aggregatorId, String effectiveStart, String snapshotName) {
        LocalDateTime effectiveTime = LocalDateTime.parse(effectiveStart.replace('T', ' '), DATE_TIME);
        Integer futureOverlap = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM la_model_snapshot "
                        + "WHERE aggregator_id = ? AND status IN ('EFFECTIVE','EXPIRED') AND effective_start > ? "
                        + "AND (effective_end IS NULL OR effective_end > ?)", Integer.class, aggregatorId,
                Timestamp.valueOf(effectiveTime), Timestamp.valueOf(effectiveTime));
        if (futureOverlap != null && futureOverlap > 0) {
            throw new IllegalArgumentException("生效时间之后已有未结束的参与范围快照，请先维护快照有效期");
        }
        String snapshotCode = "GRID-" + effectiveTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update("UPDATE la_model_snapshot SET effective_end = ?, status = 'EXPIRED' "
                        + "WHERE aggregator_id = ? AND effective_end IS NULL AND effective_start < ?",
                Timestamp.valueOf(effectiveTime), aggregatorId, Timestamp.valueOf(effectiveTime));
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement statement = connection.prepareStatement("INSERT INTO la_model_snapshot "
                            + "(aggregator_id, snapshot_code, snapshot_name, effective_start, status) "
                            + "VALUES (?, ?, ?, ?, 'EFFECTIVE')", java.sql.Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, aggregatorId);
            statement.setString(2, snapshotCode);
            statement.setString(3, StringUtils.defaultIfBlank(snapshotName, "电网上送参与范围"));
            statement.setTimestamp(4, Timestamp.valueOf(effectiveTime));
            return statement;
        }, holder);
        Number key = holder.getKey();
        if (key == null) {
            throw new IllegalStateException("创建参与范围快照失败");
        }
        Long snapshotId = key.longValue();
        List<Map<String, Object>> devices = jdbcTemplate.queryForList("SELECT d.id db_device_id, d.ent_id, d.device_id, "
                + "d.energy_station_code, d.energy_station, d.resource_type_id, "
                + "CASE WHEN m.controll = '1' THEN 1 ELSE 0 END participation_status "
                + "FROM aggregator_ent_device d LEFT JOIN aggregator_single_model_data m ON m.aggregator_id = d.aggregator_id "
                + "AND m.resource_type_id = d.resource_type_id AND m.energy_station_code = d.energy_station_code "
                + "WHERE d.aggregator_id = ? AND d.status = 1 AND d.del_flag = 1 AND d.model_flag = 1", aggregatorId);
        for (Map<String, Object> device : devices) {
            jdbcTemplate.update("INSERT INTO la_model_snapshot_device (snapshot_id, aggregator_id, ent_id, "
                            + "device_id, device_code, single_code, single_name, resource_type_id, participation_status) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", snapshotId, aggregatorId, device.get("ent_id"),
                    device.get("db_device_id"), device.get("device_id"), device.get("energy_station_code"),
                    device.get("energy_station"), device.get("resource_type_id"), device.get("participation_status"));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("snapshotId", snapshotId);
        result.put("snapshotCode", snapshotCode);
        result.put("effectiveStart", effectiveStart);
        result.put("deviceCount", devices.size());
        return result;
    }

    private List<Map<String, Object>> totalRows(String aggregatorId, DateRange range, String channelNo,
                                                String resourceTypeId) {
        Map<LocalDateTime, Map<String, Object>> values = new LinkedHashMap<>();
        String auditSql = "SELECT t.minute_time, t.total_value, t.has_value, t.send_status "
                + "FROM la_grid_total_minute t JOIN ("
                + "SELECT aggregator_id, resource_type_id, grid_channel_no, minute_time, MAX(id) id "
                + "FROM la_grid_total_minute WHERE aggregator_id = ? AND minute_time >= ? AND minute_time < ?"
                + (StringUtils.isBlank(resourceTypeId) ? "" : " AND resource_type_id = ?")
                + (StringUtils.isBlank(channelNo) ? "" : " AND grid_channel_no = ?")
                + " GROUP BY aggregator_id, resource_type_id, grid_channel_no, minute_time) latest ON latest.id = t.id "
                + "ORDER BY t.id";
        try {
            List<Object> auditArgs = new ArrayList<>();
            auditArgs.add(aggregatorId);
            auditArgs.add(Timestamp.valueOf(range.start));
            auditArgs.add(Timestamp.valueOf(range.end));
            if (StringUtils.isNotBlank(resourceTypeId)) {
                auditArgs.add(resourceTypeId);
            }
            if (StringUtils.isNotBlank(channelNo)) {
                auditArgs.add(channelNo);
            }
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(auditSql, auditArgs.toArray());
            for (Map<String, Object> row : rows) {
                LocalDateTime time = toLocalDateTime(row.get("minute_time"));
                Map<String, Object> value = values.computeIfAbsent(time, ignored -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("time", time.format(DATE_TIME));
                    item.put("value", null);
                    item.put("status", "NORMAL");
                    item.put("channelNo", channelNo);
                    item.put("source", "UPSTREAM_AUDIT");
                    return item;
                });
                if (row.get("total_value") != null) {
                    BigDecimal current = value.get("value") == null ? BigDecimal.ZERO : decimal(value.get("value"));
                    value.put("value", current.add(decimal(row.get("total_value"))));
                }
                if (number(row.get("has_value")) != 1) {
                    value.put("status", "MISSING");
                } else if (!"SUCCESS".equals(String.valueOf(row.get("send_status")))) {
                    value.put("status", "SEND_FAILED");
                }
            }
        } catch (DataAccessException ignored) {
            // 新审计表未部署时使用分钟量测复算。
        }
        String sql = "SELECT minute_time, SUM(point_value) total_value, "
                + "SUM(CASE WHEN quality = 'normal' THEN 1 ELSE 0 END) valid_count "
                + "FROM iot_telemetry_minute WHERE aggregator_id = ? "
                + "AND minute_time >= ? AND minute_time < ? AND point_code IN ('P','active_power') "
                + "GROUP BY minute_time ORDER BY minute_time";
        try {
            if (!values.isEmpty()) {
                return fillTotalTimeline(range, channelNo, values);
            }
            if (StringUtils.isNotBlank(resourceTypeId)) {
                // 历史分钟量测没有能源字段，不能在能源核查中把不同能源混合复算。
                return fillTotalTimeline(range, channelNo, values);
            }
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, aggregatorId,
                    Timestamp.valueOf(range.start), Timestamp.valueOf(range.end));
            for (Map<String, Object> row : rows) {
                LocalDateTime time = toLocalDateTime(row.get("minute_time"));
                Map<String, Object> value = new LinkedHashMap<>();
                value.put("time", time.format(DATE_TIME));
                value.put("value", decimal(row.get("total_value")));
                value.put("status", number(row.get("valid_count")) > 0 ? "NORMAL" : "INVALID");
                value.put("channelNo", channelNo);
                value.put("source", "TELEMETRY_REBUILD");
                values.put(time, value);
            }
        } catch (DataAccessException ignored) {
            // 数据库未初始化时页面返回完整的缺点时间轴，便于部署检查。
        }
        return fillTotalTimeline(range, channelNo, values);
    }

    private List<Map<String, Object>> fillTotalTimeline(DateRange range, String channelNo,
                                                        Map<LocalDateTime, Map<String, Object>> values) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDateTime cursor = range.start;
        while (cursor.isBefore(range.end)) {
            Map<String, Object> value = values.get(cursor);
            if (value == null) {
                value = new LinkedHashMap<>();
                value.put("time", cursor.format(DATE_TIME));
                value.put("value", null);
                value.put("status", "MISSING");
                value.put("channelNo", channelNo);
                value.put("source", "NO_DATA");
            }
            result.add(value);
            cursor = cursor.plusMinutes(1);
        }
        return result;
    }

    private List<Map<String, Object>> singleRows(String aggregatorId, DateRange range, String channelNo,
                                                 String resourceTypeId,
                                                 List<Participant> participants) {
        Map<String, Map<LocalDateTime, BigDecimal>> values = new LinkedHashMap<>();
        Map<String, Map<LocalDateTime, String>> statuses = new LinkedHashMap<>();
        Map<String, Map<LocalDateTime, Set<String>>> presentDevices = new LinkedHashMap<>();
        if (participants.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> deviceCodes = new LinkedHashSet<>();
        Set<String> singleCodes = new LinkedHashSet<>();
        Map<String, Participant> participantByDevice = new LinkedHashMap<>();
        for (Participant participant : participants) {
            singleCodes.add(participant.singleCode);
            deviceCodes.addAll(participant.deviceCodes);
            for (String deviceCode : participant.deviceCodes) {
                participantByDevice.put(deviceCode, participant);
            }
        }
        if (deviceCodes.isEmpty()) {
            return Collections.emptyList();
        }
        String singlePlaceholders = String.join(",", Collections.nCopies(singleCodes.size(), "?"));
        String auditSql = "SELECT s.quarter_time, s.single_code, s.active_power, s.has_value, s.send_status "
                + "FROM la_grid_single_quarter s JOIN ("
                + "SELECT aggregator_id, resource_type_id, grid_channel_no, single_code, quarter_time, MAX(id) id "
                + "FROM la_grid_single_quarter WHERE aggregator_id = ? AND quarter_time >= ? AND quarter_time < ?"
                + (StringUtils.isBlank(resourceTypeId) ? "" : " AND resource_type_id = ?")
                + (StringUtils.isBlank(channelNo) ? "" : " AND grid_channel_no = ?")
                + " AND single_code IN (" + singlePlaceholders + ")"
                + " GROUP BY aggregator_id, resource_type_id, grid_channel_no, single_code, quarter_time) latest ON latest.id = s.id "
                + "ORDER BY s.id";
        try {
            List<Object> auditArgs = new ArrayList<>();
            auditArgs.add(aggregatorId);
            auditArgs.add(Timestamp.valueOf(range.start));
            auditArgs.add(Timestamp.valueOf(range.end));
            if (StringUtils.isNotBlank(resourceTypeId)) {
                auditArgs.add(resourceTypeId);
            }
            if (StringUtils.isNotBlank(channelNo)) {
                auditArgs.add(channelNo);
            }
            auditArgs.addAll(singleCodes);
            for (Map<String, Object> row : jdbcTemplate.queryForList(auditSql, auditArgs.toArray())) {
                String singleCode = string(row.get("single_code"));
                LocalDateTime time = toLocalDateTime(row.get("quarter_time"));
                if (row.get("active_power") != null) {
                    values.computeIfAbsent(singleCode, ignored -> new LinkedHashMap<>())
                            .put(time, decimal(row.get("active_power")));
                }
                String status = number(row.get("has_value")) != 1 ? "MISSING"
                        : "SUCCESS".equals(String.valueOf(row.get("send_status"))) ? "NORMAL" : "SEND_FAILED";
                statuses.computeIfAbsent(singleCode, ignored -> new LinkedHashMap<>()).put(time, status);
            }
        } catch (DataAccessException ignored) {
            // 新审计表未部署时使用分钟量测复算。
        }
        String placeholders = String.join(",", Collections.nCopies(deviceCodes.size(), "?"));
        List<Object> args = new ArrayList<>();
        args.add(aggregatorId);
        args.add(Timestamp.valueOf(range.start));
        args.add(Timestamp.valueOf(range.end));
        args.addAll(deviceCodes);
        String sql = "SELECT minute_time, device_code, SUM(point_value) value "
                + "FROM iot_telemetry_minute WHERE aggregator_id = ? AND minute_time >= ? AND minute_time < ? "
                + "AND point_code IN ('P','active_power') AND device_code IN (" + placeholders + ") "
                + "AND quality = 'normal' AND MINUTE(minute_time) IN (0, 15, 30, 45) "
                + "GROUP BY minute_time, device_code";
        try {
            if (!statuses.isEmpty()) {
                return buildSingleTimeline(range, channelNo, participants, values, statuses, presentDevices);
            }
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, args.toArray());
            for (Map<String, Object> row : rows) {
                String deviceCode = String.valueOf(row.get("device_code"));
                Participant participant = participantByDevice.get(deviceCode);
                if (participant == null) {
                    continue;
                }
                LocalDateTime time = toLocalDateTime(row.get("minute_time"));
                values.computeIfAbsent(participant.singleCode, ignored -> new LinkedHashMap<>())
                        .merge(time, decimal(row.get("value")), BigDecimal::add);
                presentDevices.computeIfAbsent(participant.singleCode, ignored -> new LinkedHashMap<>())
                        .computeIfAbsent(time, ignored -> new LinkedHashSet<>()).add(deviceCode);
            }
        } catch (DataAccessException ignored) {
            // 返回缺点时间轴。
        }
        return buildSingleTimeline(range, channelNo, participants, values, statuses, presentDevices);
    }

    private List<Map<String, Object>> buildSingleTimeline(DateRange range, String channelNo,
                                                          List<Participant> participants,
                                                          Map<String, Map<LocalDateTime, BigDecimal>> values,
                                                          Map<String, Map<LocalDateTime, String>> statuses,
                                                          Map<String, Map<LocalDateTime, Set<String>>> presentDevices) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDateTime cursor = range.start;
        while (cursor.isBefore(range.end)) {
            if (cursor.getMinute() % 15 == 0) {
                for (Participant participant : participants) {
                    BigDecimal value = values.getOrDefault(participant.singleCode, Collections.emptyMap()).get(cursor);
                    String status = statuses.getOrDefault(participant.singleCode, Collections.emptyMap()).get(cursor);
                    if (status == null && value != null) {
                        int present = presentDevices.getOrDefault(participant.singleCode, Collections.emptyMap())
                                .getOrDefault(cursor, Collections.emptySet()).size();
                        status = present == participant.deviceCodes.size() ? "NORMAL" : "INCOMPLETE";
                    }
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("time", cursor.format(DATE_TIME));
                    item.put("singleCode", participant.singleCode);
                    item.put("singleName", participant.singleName);
                    item.put("entId", participant.entId);
                    item.put("deviceCode", String.join(",", participant.deviceCodes));
                    item.put("value", value);
                    item.put("status", status == null ? "MISSING" : status);
                    item.put("channelNo", channelNo);
                    item.put("source", statuses.isEmpty() ? "TELEMETRY_REBUILD" : "UPSTREAM_AUDIT");
                    item.put("participating", participant.participating);
                    result.add(item);
                }
            }
            cursor = cursor.plusMinutes(1);
        }
        return result;
    }

    private List<Map<String, Object>> reconcile(DateRange range, List<Map<String, Object>> totalRows,
                                                List<Map<String, Object>> singleRows) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (Map<String, Object> row : totalRows) {
            if (hasPreparedValue(row.get("status"))) {
                totals.put(String.valueOf(row.get("time")), decimal(row.get("value")));
            }
        }
        Map<String, BigDecimal> sums = new LinkedHashMap<>();
        Map<String, Integer> missing = new LinkedHashMap<>();
        for (Map<String, Object> row : singleRows) {
            String time = String.valueOf(row.get("time"));
            if (hasPreparedValue(row.get("status"))) {
                sums.merge(time, decimal(row.get("value")), BigDecimal::add);
            } else {
                missing.merge(time, 1, Integer::sum);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDateTime cursor = range.start;
        while (cursor.isBefore(range.end)) {
            if (cursor.getMinute() % 15 == 0) {
                String time = cursor.format(DATE_TIME);
                BigDecimal total = totals.get(time);
                BigDecimal sum = sums.get(time);
                BigDecimal totalKw = total == null ? null : total.multiply(BigDecimal.valueOf(1000));
                BigDecimal difference = totalKw == null || sum == null ? null : totalKw.subtract(sum).abs();
                int participantCount = 0;
                for (Map<String, Object> singleRow : singleRows) {
                    if (time.equals(String.valueOf(singleRow.get("time")))) participantCount++;
                }
                BigDecimal precisionTolerance = reconciliationPrecisionBaseKw.add(
                        reconciliationPrecisionPerSingleKw.multiply(BigDecimal.valueOf(participantCount)));
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("time", time);
                row.put("totalValue", total);
                row.put("totalValueKw", totalKw);
                row.put("singleSum", sum);
                row.put("difference", difference);
                row.put("missingSingleCount", missing.getOrDefault(time, 0));
                row.put("tolerance", precisionTolerance);
                String status = "UNAVAILABLE";
                if (difference != null && missing.getOrDefault(time, 0) == 0) {
                    status = difference.compareTo(reconciliationExactToleranceKw) <= 0 ? "MATCH"
                            : difference.compareTo(precisionTolerance) <= 0 ? "PRECISION_DIFFERENCE" : "MISMATCH";
                }
                row.put("status", status);
                result.add(row);
            }
            cursor = cursor.plusMinutes(1);
        }
        return result;
    }

    private List<Participant> participants(String aggregatorId, LocalDateTime effectiveTime,
                                           String resourceTypeId) {
        List<Participant> all = allSingleScopes(aggregatorId, effectiveTime, resourceTypeId);
        List<Participant> result = new ArrayList<>();
        for (Participant item : all) if (item.participating) result.add(item);
        return result;
    }

    private List<Participant> allSingleScopes(String aggregatorId, LocalDateTime effectiveTime,
                                              String resourceTypeId) {
        String snapshotSql = "SELECT d.single_code, d.single_name, d.ent_id, d.device_code, d.resource_type_id, "
                + "d.participation_status, s.id snapshot_id "
                + "FROM la_model_snapshot s JOIN la_model_snapshot_device d ON d.snapshot_id = s.id "
                + "WHERE s.aggregator_id = ? AND s.status IN ('EFFECTIVE','EXPIRED') AND s.effective_start <= ? "
                + "AND (s.effective_end IS NULL OR s.effective_end > ?)"
                + (StringUtils.isBlank(resourceTypeId) ? "" : " AND d.resource_type_id = ?")
                + " ORDER BY d.single_code, d.device_code";
        try {
            List<Object> args = new ArrayList<>();
            args.add(aggregatorId);
            args.add(Timestamp.valueOf(effectiveTime));
            args.add(Timestamp.valueOf(effectiveTime));
            if (StringUtils.isNotBlank(resourceTypeId)) {
                args.add(resourceTypeId);
            }
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(snapshotSql, args.toArray());
            if (!rows.isEmpty()) {
                return participantsFromRows(rows);
            }
        } catch (DataAccessException ignored) {
            // 首次部署尚未执行快照迁移时兼容旧设备表。
        }
        String fallback = "SELECT d.energy_station_code single_code, d.energy_station single_name, d.ent_id, "
                + "d.device_id device_code, d.resource_type_id, CASE WHEN m.controll = '1' THEN 1 ELSE 0 END participation_status, "
                + "NULL snapshot_id FROM aggregator_ent_device d LEFT JOIN aggregator_single_model_data m "
                + "ON m.aggregator_id = d.aggregator_id AND m.resource_type_id = d.resource_type_id "
                + "AND m.energy_station_code = d.energy_station_code WHERE d.aggregator_id = ? "
                + "AND d.status = 1 AND d.del_flag = 1 AND d.model_flag = 1 "
                + (StringUtils.isBlank(resourceTypeId) ? "" : "AND d.resource_type_id = ? ")
                + "ORDER BY d.energy_station_code, d.device_id";
        try {
            return participantsFromRows(StringUtils.isBlank(resourceTypeId)
                    ? jdbcTemplate.queryForList(fallback, aggregatorId)
                    : jdbcTemplate.queryForList(fallback, aggregatorId, resourceTypeId));
        } catch (DataAccessException ignored) {
            return Collections.emptyList();
        }
    }

    private List<Participant> participantsFromRows(List<Map<String, Object>> rows) {
        Map<String, Participant> grouped = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String singleCode = string(row.get("single_code"));
            String deviceCode = string(row.get("device_code"));
            if (StringUtils.isBlank(singleCode) || StringUtils.isBlank(deviceCode)) {
                continue;
            }
            Participant participant = grouped.get(singleCode);
            if (participant == null) {
                participant = new Participant();
                participant.singleCode = singleCode;
                participant.singleName = string(row.get("single_name"));
                participant.entId = string(row.get("ent_id"));
                participant.resourceTypeId = string(row.get("resource_type_id"));
                participant.snapshotId = row.get("snapshot_id");
                participant.participating = number(row.get("participation_status")) == 1;
                grouped.put(singleCode, participant);
            }
            if (!participant.deviceCodes.contains(deviceCode)) {
                participant.deviceCodes.add(deviceCode);
            }
        }
        return new ArrayList<>(grouped.values());
    }

    private List<Map<String, Object>> participantsToMap(List<Participant> participants) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Participant participant : participants) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("singleCode", participant.singleCode);
            row.put("singleName", participant.singleName);
            row.put("entId", participant.entId);
            row.put("deviceCode", String.join(",", participant.deviceCodes));
            row.put("snapshotId", participant.snapshotId);
            row.put("resourceTypeId", participant.resourceTypeId);
            row.put("participating", participant.participating);
            result.add(row);
        }
        return result;
    }

    private void persistReconciliation(String aggregatorId, DateRange range, String channelNo,
                                       String resourceTypeId,
                                       List<Map<String, Object>> rows) {
        try {
            for (Map<String, Object> row : rows) {
                LocalDateTime time = LocalDateTime.parse(String.valueOf(row.get("time")), DATE_TIME);
                BigDecimal total = row.get("totalValue") == null ? null : decimal(row.get("totalValue"));
                BigDecimal sum = row.get("singleSum") == null ? null : decimal(row.get("singleSum"));
                BigDecimal diff = row.get("difference") == null ? null : decimal(row.get("difference"));
                BigDecimal totalKw = total == null ? null : total.multiply(BigDecimal.valueOf(1000));
                BigDecimal relative = totalKw == null || totalKw.compareTo(BigDecimal.ZERO) == 0 || diff == null
                        ? null : diff.divide(totalKw.abs(), 8, RoundingMode.HALF_UP);
                jdbcTemplate.update("INSERT INTO la_grid_reconciliation (aggregator_id, data_date, resource_type_id, grid_channel_no, "
                                + "quarter_time, total_value, single_sum, difference, relative_difference, "
                                + "tolerance, reconciliation_status, missing_single_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                                + "ON DUPLICATE KEY UPDATE total_value = VALUES(total_value), single_sum = VALUES(single_sum), "
                                + "difference = VALUES(difference), relative_difference = VALUES(relative_difference), "
                                + "tolerance = VALUES(tolerance), reconciliation_status = VALUES(reconciliation_status), "
                                + "missing_single_count = VALUES(missing_single_count)",
                        aggregatorId, java.sql.Date.valueOf(time.toLocalDate()), resourceTypeId, channelNo, Timestamp.valueOf(time),
                        total, sum, diff, relative, row.get("tolerance"), row.get("status"), row.get("missingSingleCount"));
            }
        } catch (DataAccessException ignored) {
            // 允许只部署查询表的环境继续使用实时核查。
        }
    }

    private int count(List<Map<String, Object>> rows, String status) {
        int count = 0;
        for (Map<String, Object> row : rows) {
            if (status.equals(row.get("status"))) {
                count++;
            }
        }
        return count;
    }

    private int countPrepared(List<Map<String, Object>> rows) {
        int result = 0;
        for (Map<String, Object> row : rows) if (hasPreparedValue(row.get("status"))) result++;
        return result;
    }

    private boolean hasPreparedValue(Object status) {
        return "NORMAL".equals(status) || "SEND_FAILED".equals(status);
    }

    private double rate(int actual, int expected) {
        return expected == 0 ? 100D : BigDecimal.valueOf(actual * 100D / expected)
                .setScale(4, RoundingMode.HALF_UP).doubleValue();
    }

    private BigDecimal decimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(String.valueOf(value));
    }

    private int number(Object value) {
        return value == null ? 0 : Integer.parseInt(String.valueOf(value));
    }

    private String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        }
        if (value instanceof Date) {
            return ((Date) value).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        }
        return LocalDateTime.parse(String.valueOf(value).replace('T', ' '), DATE_TIME);
    }

    private static final class Participant {
        private String singleCode;
        private String singleName;
        private String entId;
        private String resourceTypeId;
        private final List<String> deviceCodes = new ArrayList<>();
        private Object snapshotId;
        private boolean participating = true;
    }

    private static final class SingleStats {
        private final Participant participant;
        private int expected;
        private int actual;
        private String lastTime;

        private SingleStats(Participant participant) { this.participant = participant; }

        private Map<String, Object> toMap() {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("singleCode", participant.singleCode);
            row.put("singleName", participant.singleName);
            row.put("entId", participant.entId);
            row.put("deviceCount", participant.deviceCodes.size());
            row.put("deviceCode", String.join(",", participant.deviceCodes));
            row.put("participating", participant.participating);
            row.put("expected", expected);
            row.put("actual", actual);
            row.put("missing", Math.max(0, expected - actual));
            row.put("lastTime", lastTime);
            row.put("status", expected > 0 && actual == expected ? "COMPLETE" : "INCOMPLETE");
            row.put("snapshotId", participant.snapshotId);
            return row;
        }
    }

    private static final class DateRange {
        private final LocalDateTime start;
        private final LocalDateTime end;

        private DateRange(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }

        private static DateRange of(String startDate, String endDate) {
            LocalDate start = LocalDate.parse(StringUtils.defaultIfBlank(startDate, LocalDate.now().format(DATE)), DATE);
            LocalDate end = LocalDate.parse(StringUtils.defaultIfBlank(endDate, start.format(DATE)), DATE);
            if (end.isBefore(start)) {
                throw new IllegalArgumentException("结束日期不能早于开始日期");
            }
            if (end.isAfter(start.plusDays(30))) {
                throw new IllegalArgumentException("单次核查最多查询31天");
            }
            return new DateRange(start.atTime(LocalTime.MIN), end.plusDays(1).atTime(LocalTime.MIN));
        }

        private long days() {
            return java.time.Duration.between(start, end).toDays();
        }
    }
}
