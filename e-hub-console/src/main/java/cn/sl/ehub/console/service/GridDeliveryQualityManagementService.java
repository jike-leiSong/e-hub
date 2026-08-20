package cn.sl.ehub.console.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 电网上送质量的自动核查、问题闭环和快照版本管理。
 */
@Service
public class GridDeliveryQualityManagementService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<String> ISSUE_STATUSES = Arrays.asList("OPEN", "PROCESSING", "RESOLVED", "IGNORED");

    private final JdbcTemplate jdbcTemplate;
    private final GridDeliveryQualityService qualityService;

    @Value("${grid.delivery.quality.delay-minutes:7}")
    private int delayMinutes;

    @Value("${grid.delivery.quality.standard-rate:99}")
    private BigDecimal standardRate;

    public GridDeliveryQualityManagementService(JdbcTemplate jdbcTemplate,
                                                GridDeliveryQualityService qualityService) {
        this.jdbcTemplate = jdbcTemplate;
        this.qualityService = qualityService;
    }

    public Map<String, Object> marketStatus(String aggregatorId, String resourceTypeId, String date) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("aggregatorId", aggregatorId);
        result.put("resourceTypeId", resourceTypeId);
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT market_enabled marketEnabled, remark, operator_name operatorName, update_time updateTime "
                            + "FROM la_grid_market_status WHERE aggregator_id = ? AND resource_type_id = ? LIMIT 1",
                    aggregatorId, resourceTypeId);
            if (!rows.isEmpty()) {
                result.putAll(rows.get(0));
            }
        } catch (Exception ignored) {
            // 未执行标记迁移时保持默认展示：当前参与市场。
        }
        boolean enabled = !Integer.valueOf(0).equals(numberValue(result.get("marketEnabled")));
        result.put("marketEnabled", enabled);
        result.put("status", enabled ? "ACTIVE" : "INACTIVE");
        result.put("statusText", enabled ? "当前参与电网市场" : "当前未参与市场");
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateMarketStatus(String aggregatorId, String resourceTypeId, boolean enabled,
                                                   String remark, String operatorId, String operatorName) {
        if (StringUtils.isBlank(aggregatorId) || StringUtils.isBlank(resourceTypeId)) {
            throw new IllegalArgumentException("聚合商和能源类型不能为空");
        }
        jdbcTemplate.update("INSERT INTO la_grid_market_status (aggregator_id, resource_type_id, market_enabled, "
                        + "remark, operator_id, operator_name) VALUES (?, ?, ?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE market_enabled = VALUES(market_enabled), remark = VALUES(remark), "
                        + "operator_id = VALUES(operator_id), operator_name = VALUES(operator_name), update_time = NOW()",
                aggregatorId, resourceTypeId, enabled ? 1 : 0, remark, operatorId, operatorName);
        return marketStatus(aggregatorId, resourceTypeId, null);
    }

    private Integer numberValue(Object value) {
        if (value instanceof Number) return ((Number) value).intValue();
        try { return value == null ? null : Integer.valueOf(String.valueOf(value)); }
        catch (NumberFormatException ignored) { return null; }
    }

    public Map<String, Object> recalculate(String aggregatorId, String startDate, String endDate,
                                           String channelNo) {
        return recalculate(aggregatorId, startDate, endDate, channelNo, null, null);
    }

    public Map<String, Object> recalculate(String aggregatorId, String startDate, String endDate,
                                           String channelNo, Long periodId) {
        return recalculate(aggregatorId, startDate, endDate, channelNo, periodId, null);
    }

    public Map<String, Object> recalculate(String aggregatorId, String startDate, String endDate,
                                           String channelNo, Long periodId, String resourceTypeId) {
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(StringUtils.defaultIfBlank(endDate, startDate));
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        if (days < 1 || days > 31) {
            throw new IllegalArgumentException("单次重算日期范围必须为 1 至 31 天");
        }
        if (periodId != null) {
            Map<String, Object> period = period(aggregatorId, periodId);
            LocalDate periodStart = sqlDate(period.get("startDate"));
            LocalDate periodEnd = sqlDate(period.get("endDate"));
            if (start.isBefore(periodStart) || end.isAfter(periodEnd)) {
                throw new IllegalArgumentException("重算日期必须位于所选电网上送周期内");
            }
            channelNo = string(period.get("channelNo"));
            resourceTypeId = periodResourceType(period, resourceTypeId);
        }
        List<Map<String, Object>> daily = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            daily.add(inspectDay(aggregatorId, cursor, channelNo, periodId, resourceTypeId));
            cursor = cursor.plusDays(1);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("startDate", start.format(DATE));
        result.put("endDate", end.format(DATE));
        result.put("days", daily.size());
        result.put("daily", daily);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> inspectDay(String aggregatorId, LocalDate date, String channelNo) {
        Long periodId = findPeriodId(aggregatorId, date, channelNo, null);
        return inspectDay(aggregatorId, date, channelNo, periodId,
                periodId == null ? null : string(period(aggregatorId, periodId).get("resourceTypeId")));
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> inspectDay(String aggregatorId, LocalDate date, String channelNo, Long periodId) {
        return inspectDay(aggregatorId, date, channelNo, periodId,
                periodId == null ? null : string(period(aggregatorId, periodId).get("resourceTypeId")));
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> inspectDay(String aggregatorId, LocalDate date, String channelNo, Long periodId,
                                          String resourceTypeId) {
        String channel = StringUtils.defaultString(channelNo);
        String resource = StringUtils.defaultString(resourceTypeId);
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        LocalDateTime watermark = LocalDateTime.now().minusMinutes(Math.max(0, delayMinutes));
        LocalDateTime cutoff = watermark.isBefore(dayEnd) ? watermark : dayEnd;
        if (cutoff.isBefore(dayStart)) {
            cutoff = dayStart;
        }

        String day = date.format(DATE);
        List<Map<String, Object>> totalRows = qualityService.daily(aggregatorId, day, "TOTAL", channel, resourceTypeId);
        List<Map<String, Object>> singleRows = qualityService.daily(aggregatorId, day, "SINGLE", channel, resourceTypeId);
        List<Map<String, Object>> reconciliation = qualityService.reconciliation(aggregatorId, day, channel, resourceTypeId);
        List<Map<String, Object>> participants = qualityService.participation(aggregatorId, day, resourceTypeId);

        jdbcTemplate.update("UPDATE la_grid_quality_issue SET current_detected = 0 "
                        + "WHERE aggregator_id = ? AND data_date = ? AND IFNULL(grid_channel_no, '') = ? "
                        + "AND IFNULL(period_id, 0) = ? AND IFNULL(resource_type_id, '') = ?",
                aggregatorId, java.sql.Date.valueOf(date), channel, periodId == null ? 0L : periodId, resource);

        int totalExpected = 0;
        int totalActual = 0;
        for (Map<String, Object> row : totalRows) {
            LocalDateTime time = time(row.get("time"));
            if (!time.isBefore(cutoff)) {
                continue;
            }
            totalExpected++;
            String status = string(row.get("status"));
            if ("NORMAL".equals(status) || "SEND_FAILED".equals(status)) {
                totalActual++;
            }
            if (!"NORMAL".equals(status)) {
                upsertIssue(aggregatorId, periodId, resourceTypeId, date, channel, issueType("TOTAL", status), time,
                        null, null, reason("总加", status, row));
            }
        }

        int singleExpected = 0;
        int singleActual = 0;
        for (Map<String, Object> row : singleRows) {
            LocalDateTime time = time(row.get("time"));
            if (!time.isBefore(cutoff)) {
                continue;
            }
            singleExpected++;
            String status = string(row.get("status"));
            if ("NORMAL".equals(status) || "SEND_FAILED".equals(status)) {
                singleActual++;
            }
            if (!"NORMAL".equals(status)) {
                upsertIssue(aggregatorId, periodId, resourceTypeId, date, channel, issueType("SINGLE", status), time,
                        string(row.get("singleCode")), null, reason("单体", status, row));
            }
        }

        int reconciliationExpected = 0;
        int reconciliationMatched = 0;
        for (Map<String, Object> row : reconciliation) {
            LocalDateTime time = time(row.get("time"));
            if (!time.isBefore(cutoff)) {
                continue;
            }
            reconciliationExpected++;
            if ("MATCH".equals(row.get("status")) || "PRECISION_DIFFERENCE".equals(row.get("status"))) {
                reconciliationMatched++;
            } else if ("MISMATCH".equals(row.get("status"))) {
                String detail = "总加=" + display(row.get("totalValue")) + "，单体加和="
                        + display(row.get("singleSum")) + "，差值=" + display(row.get("difference"))
                        + "，缺失单体=" + display(row.get("missingSingleCount"));
                upsertIssue(aggregatorId, periodId, resourceTypeId, date, channel, "RECONCILIATION_MISMATCH", time,
                        null, null, detail);
            }
        }
        if (participants.isEmpty() && cutoff.isAfter(dayStart)) {
            upsertIssue(aggregatorId, periodId, resourceTypeId, date, channel, "PARTICIPATION_MISSING", dayStart,
                    null, null, "该业务日没有有效的参与范围快照");
        }

        jdbcTemplate.update("UPDATE la_grid_quality_issue SET status = 'RESOLVED', resolved_time = NOW() "
                        + "WHERE aggregator_id = ? AND data_date = ? AND IFNULL(grid_channel_no, '') = ? "
                        + "AND IFNULL(period_id, 0) = ? AND IFNULL(resource_type_id, '') = ? "
                        + "AND current_detected = 0 AND status IN ('OPEN','PROCESSING')",
                aggregatorId, java.sql.Date.valueOf(date), channel, periodId == null ? 0L : periodId, resource);

        int issueCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM la_grid_quality_issue "
                        + "WHERE aggregator_id = ? AND data_date = ? AND IFNULL(grid_channel_no, '') = ? "
                        + "AND IFNULL(period_id, 0) = ? AND IFNULL(resource_type_id, '') = ? "
                        + "AND current_detected = 1 AND status NOT IN ('RESOLVED','IGNORED')",
                Integer.class, aggregatorId, java.sql.Date.valueOf(date), channel, periodId == null ? 0L : periodId, resource);
        BigDecimal totalRate = rate(totalActual, totalExpected);
        BigDecimal singleRate = rate(singleActual, singleExpected);
        BigDecimal reconciliationRate = rate(reconciliationMatched, reconciliationExpected);
        BigDecimal threshold = periodId == null ? standardRate : decimal(period(aggregatorId, periodId).get("standardRate"));
        boolean completed = !date.equals(LocalDate.now()) && date.isBefore(LocalDate.now());
        boolean reached = completed && totalRate.compareTo(threshold) >= 0
                && singleRate.compareTo(threshold) >= 0
                && reconciliationRate.compareTo(threshold) >= 0;
        Object snapshotId = participants.isEmpty() ? null : participants.get(0).get("snapshotId");
        jdbcTemplate.update("INSERT INTO la_grid_quality_daily (aggregator_id, data_date, period_id, resource_type_id, grid_channel_no, snapshot_id, "
                        + "participant_count, total_expected, total_actual, total_rate, single_expected, single_actual, "
                        + "single_rate, reconciliation_expected, reconciliation_matched, reconciliation_rate, issue_count, "
                        + "reached_standard, calculated_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW()) "
                        + "ON DUPLICATE KEY UPDATE period_id = VALUES(period_id), snapshot_id = VALUES(snapshot_id), participant_count = VALUES(participant_count), "
                        + "total_expected = VALUES(total_expected), total_actual = VALUES(total_actual), total_rate = VALUES(total_rate), "
                        + "single_expected = VALUES(single_expected), single_actual = VALUES(single_actual), single_rate = VALUES(single_rate), "
                        + "reconciliation_expected = VALUES(reconciliation_expected), reconciliation_matched = VALUES(reconciliation_matched), "
                        + "reconciliation_rate = VALUES(reconciliation_rate), issue_count = VALUES(issue_count), "
                        + "reached_standard = VALUES(reached_standard), calculated_time = NOW()",
                aggregatorId, java.sql.Date.valueOf(date), periodId, resourceTypeId, channel, snapshotId, participants.size(), totalExpected,
                totalActual, totalRate, singleExpected, singleActual, singleRate, reconciliationExpected,
                reconciliationMatched, reconciliationRate, issueCount, reached ? 1 : 0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date", day);
        result.put("periodId", periodId);
        result.put("resourceTypeId", resourceTypeId);
        result.put("standardRate", threshold);
        result.put("cutoff", cutoff.format(DATE_TIME));
        result.put("participantCount", participants.size());
        result.put("totalRate", totalRate);
        result.put("singleRate", singleRate);
        result.put("reconciliationRate", reconciliationRate);
        result.put("issueCount", issueCount);
        result.put("reachedStandard", reached);
        return result;
    }

    public Map<String, Object> trend(String aggregatorId, String startDate, String endDate, String channelNo) {
        return trend(aggregatorId, startDate, endDate, channelNo, null);
    }

    public Map<String, Object> trend(String aggregatorId, String startDate, String endDate, String channelNo,
                                     Long periodId) {
        return trend(aggregatorId, startDate, endDate, channelNo, periodId, null);
    }

    public Map<String, Object> trend(String aggregatorId, String startDate, String endDate, String channelNo,
                                     Long periodId, String resourceTypeId) {
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);
        if (end.isBefore(start) || ChronoUnit.DAYS.between(start, end) > 30) {
            throw new IllegalArgumentException("质量趋势最多查询31天");
        }
        String channelFilter = StringUtils.isBlank(channelNo) ? "" : " AND grid_channel_no = ?";
        String resourceFilter = StringUtils.isBlank(resourceTypeId) ? "" : " AND resource_type_id = ?";
        List<Object> args = new ArrayList<>();
        args.add(aggregatorId);
        args.add(java.sql.Date.valueOf(start));
        args.add(java.sql.Date.valueOf(end));
        if (StringUtils.isNotBlank(channelNo)) args.add(channelNo);
        if (StringUtils.isNotBlank(resourceTypeId)) args.add(resourceTypeId);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT data_date date, "
                        + "total_expected totalExpected, total_actual totalActual, total_rate totalRate, "
                        + "single_expected singleExpected, single_actual singleActual, single_rate singleRate, "
                        + "reconciliation_expected reconciliationExpected, reconciliation_matched reconciliationMatched, "
                        + "reconciliation_rate reconciliationRate, issue_count issueCount, calculated_time calculatedTime "
                        + "FROM la_grid_quality_daily WHERE aggregator_id = ? AND data_date BETWEEN ? AND ?"
                        + channelFilter + resourceFilter + " ORDER BY data_date", args.toArray());
        if (!LocalDate.now().isBefore(start) && !LocalDate.now().isAfter(end)) {
            Map<String, Object> today = new LinkedHashMap<>(qualityService.dailyOverview(aggregatorId,
                    LocalDate.now().format(DATE), channelNo, resourceTypeId));
            today.put("date", LocalDate.now().format(DATE));
            today.put("reconciliationRate", today.get("reconcileRate"));
            rows.removeIf(row -> LocalDate.now().equals(sqlDate(row.get("date"))));
            rows.add(today);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", rows);
        return result;
    }

    public List<Map<String, Object>> automaticScopes() {
        return automaticScopes(LocalDate.now());
    }

    public List<Map<String, Object>> automaticScopes(LocalDate date) {
        return jdbcTemplate.queryForList("SELECT aggregator_id aggregatorId, resource_type_id resourceTypeId "
                + "FROM aggregator_ent_device WHERE status = 1 AND del_flag = 1 AND model_flag = 1 "
                + "AND resource_type_id IS NOT NULL GROUP BY aggregator_id, resource_type_id");
    }

    public Map<String, Object> issues(String aggregatorId, String startDate, String endDate, String issueType,
                                      String status, String channelNo, int pageIndex, int pageSize) {
        return issues(aggregatorId, startDate, endDate, issueType, status, channelNo, null, pageIndex, pageSize);
    }

    public Map<String, Object> issues(String aggregatorId, String startDate, String endDate, String issueType,
                                      String status, String channelNo, Long periodId, int pageIndex, int pageSize) {
        return issues(aggregatorId, startDate, endDate, issueType, status, channelNo, periodId,
                null, pageIndex, pageSize);
    }

    public Map<String, Object> issues(String aggregatorId, String startDate, String endDate, String issueType,
                                      String status, String channelNo, Long periodId, String resourceTypeId,
                                      int pageIndex, int pageSize) {
        int safePage = Math.max(1, pageIndex);
        int safeSize = Math.max(1, Math.min(200, pageSize));
        StringBuilder where = new StringBuilder(" FROM la_grid_quality_issue WHERE aggregator_id = ? AND data_date BETWEEN ? AND ?");
        List<Object> args = new ArrayList<>();
        args.add(aggregatorId);
        args.add(java.sql.Date.valueOf(parseDate(startDate)));
        args.add(java.sql.Date.valueOf(parseDate(endDate)));
        if (StringUtils.isNotBlank(issueType)) {
            where.append(" AND issue_type = ?");
            args.add(issueType);
        }
        if (StringUtils.isNotBlank(status)) {
            where.append(" AND status = ?");
            args.add(status);
        }
        if (StringUtils.isNotBlank(channelNo)) {
            where.append(" AND IFNULL(grid_channel_no, '') = ?");
            args.add(channelNo);
        }
        if (periodId != null) {
            where.append(" AND period_id = ?");
            args.add(periodId);
        }
        if (StringUtils.isNotBlank(resourceTypeId)) {
            where.append(" AND resource_type_id = ?");
            args.add(resourceTypeId);
        }
        Integer total = jdbcTemplate.queryForObject("SELECT COUNT(1)" + where, args.toArray(), Integer.class);
        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add((safePage - 1) * safeSize);
        pageArgs.add(safeSize);
        List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT id, period_id periodId, resource_type_id resourceTypeId, data_date dataDate, issue_type issueType, "
                        + "issue_time issueTime, grid_channel_no channelNo, single_code singleCode, device_id deviceId, "
                        + "reason, status, remark, handler_id handlerId, handler_name handlerName, "
                        + "first_detected_time firstDetectedTime, last_detected_time lastDetectedTime, "
                        + "resolved_time resolvedTime, current_detected currentDetected" + where
                        + " ORDER BY issue_time DESC, id DESC LIMIT ?, ?", pageArgs.toArray());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total == null ? 0 : total);
        result.put("pageIndex", safePage);
        result.put("pageSize", safeSize);
        result.put("list", list);
        return result;
    }

    public Map<String, Object> issueDetail(String aggregatorId, long issueId) {
        List<Map<String, Object>> issues = jdbcTemplate.queryForList("SELECT * FROM la_grid_quality_issue "
                + "WHERE id = ? AND aggregator_id = ?", issueId, aggregatorId);
        if (issues.isEmpty()) {
            throw new IllegalArgumentException("问题不存在或不属于当前聚合商");
        }
        Map<String, Object> issue = issues.get(0);
        LocalDateTime issueTime = time(issue.get("issue_time"));
        List<Map<String, Object>> devices = expectedDeviceMeasurements(aggregatorId, issueTime,
                string(issue.get("single_code")), string(issue.get("resource_type_id")));
        List<Map<String, Object>> reconciliation = jdbcTemplate.queryForList("SELECT quarter_time time, total_value totalValue, "
                        + "single_sum singleSum, difference, tolerance, reconciliation_status status, "
                        + "missing_single_count missingSingleCount FROM la_grid_reconciliation WHERE aggregator_id = ? "
                        + "AND quarter_time = ? AND IFNULL(grid_channel_no, '') = ? "
                        + "AND IFNULL(resource_type_id, '') = ?",
                aggregatorId, Timestamp.valueOf(issueTime), StringUtils.defaultString(string(issue.get("grid_channel_no"))),
                StringUtils.defaultString(string(issue.get("resource_type_id"))));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("issue", issue);
        result.put("devices", devices);
        result.put("reconciliation", reconciliation.isEmpty() ? null : reconciliation.get(0));
        return result;
    }

    public void updateIssue(String aggregatorId, long issueId, String status, String remark,
                            String handlerId, String handlerName) {
        String normalized = StringUtils.upperCase(StringUtils.trim(status));
        if (!ISSUE_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("问题状态仅支持 OPEN/PROCESSING/RESOLVED/IGNORED");
        }
        int affected = jdbcTemplate.update("UPDATE la_grid_quality_issue SET status = ?, remark = ?, handler_id = ?, "
                        + "handler_name = ?, resolved_time = CASE WHEN ? IN ('RESOLVED','IGNORED') THEN NOW() ELSE NULL END "
                        + "WHERE id = ? AND aggregator_id = ?", normalized, remark, handlerId, handlerName,
                normalized, issueId, aggregatorId);
        if (affected == 0) {
            throw new IllegalArgumentException("问题不存在或不属于当前聚合商");
        }
    }

    public List<Map<String, Object>> snapshots(String aggregatorId) {
        return jdbcTemplate.queryForList("SELECT s.id, s.snapshot_code snapshotCode, s.snapshot_name snapshotName, "
                        + "s.effective_start effectiveStart, s.effective_end effectiveEnd, s.status, "
                        + "COUNT(d.id) deviceCount, COUNT(DISTINCT d.single_code) singleCount, s.create_time createTime "
                        + "FROM la_model_snapshot s LEFT JOIN la_model_snapshot_device d ON d.snapshot_id = s.id "
                        + "WHERE s.aggregator_id = ? GROUP BY s.id, s.snapshot_code, s.snapshot_name, s.effective_start, "
                        + "s.effective_end, s.status, s.create_time ORDER BY s.effective_start DESC, s.id DESC", aggregatorId);
    }

    public Map<String, Object> snapshotDetail(String aggregatorId, long snapshotId) {
        List<Map<String, Object>> snapshots = jdbcTemplate.queryForList("SELECT id, snapshot_code snapshotCode, "
                + "snapshot_name snapshotName, effective_start effectiveStart, effective_end effectiveEnd, status "
                + "FROM la_model_snapshot WHERE id = ? AND aggregator_id = ?", snapshotId, aggregatorId);
        if (snapshots.isEmpty()) {
            throw new IllegalArgumentException("快照不存在或不属于当前聚合商");
        }
        Map<String, Object> result = new LinkedHashMap<>(snapshots.get(0));
        result.put("devices", jdbcTemplate.queryForList("SELECT ent_id entId, device_code deviceCode, point_code pointCode, "
                + "single_code singleCode, single_name singleName, resource_type_id resourceTypeId "
                + "FROM la_model_snapshot_device WHERE snapshot_id = ? ORDER BY single_code, device_code", snapshotId));
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSnapshot(String aggregatorId, long snapshotId, String snapshotName,
                               String effectiveStart, String effectiveEnd, String status) {
        List<Map<String, Object>> existing = jdbcTemplate.queryForList("SELECT id FROM la_model_snapshot "
                + "WHERE id = ? AND aggregator_id = ?", snapshotId, aggregatorId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("快照不存在或不属于当前聚合商");
        }
        LocalDateTime start = parseDateTime(effectiveStart);
        LocalDateTime end = StringUtils.isBlank(effectiveEnd) ? null : parseDateTime(effectiveEnd);
        if (end != null && !end.isAfter(start)) {
            throw new IllegalArgumentException("失效时间必须晚于生效时间");
        }
        String normalized = StringUtils.upperCase(StringUtils.defaultIfBlank(status, "EFFECTIVE"));
        if (!Arrays.asList("EFFECTIVE", "EXPIRED", "DISABLED").contains(normalized)) {
            throw new IllegalArgumentException("快照状态不合法");
        }
        if ("EFFECTIVE".equals(normalized)) {
            Integer overlaps = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM la_model_snapshot WHERE aggregator_id = ? "
                            + "AND id <> ? AND status IN ('EFFECTIVE','EXPIRED') AND effective_start < COALESCE(?, '9999-12-31') "
                            + "AND (effective_end IS NULL OR effective_end > ?)", Integer.class, aggregatorId, snapshotId,
                    end == null ? null : Timestamp.valueOf(end), Timestamp.valueOf(start));
            if (overlaps != null && overlaps > 0) {
                throw new IllegalArgumentException("该有效期与已有参与范围快照重叠");
            }
        }
        jdbcTemplate.update("UPDATE la_model_snapshot SET snapshot_name = ?, effective_start = ?, effective_end = ?, "
                        + "status = ? WHERE id = ? AND aggregator_id = ?", snapshotName, Timestamp.valueOf(start),
                end == null ? null : Timestamp.valueOf(end), normalized, snapshotId, aggregatorId);
    }

    public List<Map<String, Object>> periods(String aggregatorId) {
        return jdbcTemplate.queryForList("SELECT id, period_code periodCode, period_name periodName, start_date startDate, "
                + "end_date endDate, resource_type_id resourceTypeId, grid_channel_no channelNo, status, standard_rate standardRate, "
                + "required_days requiredDays, remark, create_time createTime FROM la_grid_delivery_period "
                + "WHERE aggregator_id = ? ORDER BY start_date DESC, id DESC", aggregatorId);
    }

    public Map<String, Object> period(String aggregatorId, long periodId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT id, period_code periodCode, period_name periodName, "
                + "start_date startDate, end_date endDate, resource_type_id resourceTypeId, grid_channel_no channelNo, status, standard_rate standardRate, "
                + "required_days requiredDays, remark FROM la_grid_delivery_period WHERE id = ? AND aggregator_id = ?",
                periodId, aggregatorId);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("电网上送周期不存在或不属于当前聚合商");
        }
        return rows.get(0);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPeriod(String aggregatorId, String periodName, String startDate,
                                            String endDate, String channelNo, BigDecimal periodStandardRate,
                                            Integer requiredDays, String remark) {
        return createPeriod(aggregatorId, periodName, startDate, endDate, channelNo, "ACTIVE",
                periodStandardRate, requiredDays, remark, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPeriod(String aggregatorId, String periodName, String startDate,
                                            String endDate, String channelNo, String status,
                                            BigDecimal periodStandardRate, Integer requiredDays, String remark) {
        return createPeriod(aggregatorId, periodName, startDate, endDate, channelNo, status,
                periodStandardRate, requiredDays, remark, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPeriod(String aggregatorId, String periodName, String startDate,
                                            String endDate, String channelNo, String status,
                                            BigDecimal periodStandardRate, Integer requiredDays, String remark,
                                            String resourceTypeId) {
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);
        String normalized = StringUtils.upperCase(StringUtils.defaultIfBlank(status, "ACTIVE"));
        if (!Arrays.asList("PLANNED", "ACTIVE", "ENDED", "DISABLED").contains(normalized)) {
            throw new IllegalArgumentException("周期状态仅支持 PLANNED/ACTIVE/ENDED/DISABLED");
        }
        validatePeriodRange(aggregatorId, null, start, end, channelNo, resourceTypeId);
        String code = "GRID-PERIOD-" + start.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-"
                + UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update("INSERT INTO la_grid_delivery_period (aggregator_id, resource_type_id, period_code, period_name, start_date, "
                        + "end_date, grid_channel_no, status, standard_rate, required_days, remark) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", aggregatorId, resourceTypeId, code,
                StringUtils.defaultIfBlank(periodName, startDate + " 至 " + endDate + " 电网上送"),
                java.sql.Date.valueOf(start), java.sql.Date.valueOf(end), StringUtils.defaultString(channelNo),
                normalized, periodStandardRate == null ? standardRate : periodStandardRate,
                requiredDays == null ? 7 : Math.max(1, requiredDays), remark);
        Long id = jdbcTemplate.queryForObject("SELECT id FROM la_grid_delivery_period WHERE aggregator_id = ? "
                + "AND period_code = ?", Long.class, aggregatorId, code);
        return period(aggregatorId, id == null ? 0L : id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePeriod(String aggregatorId, long periodId, String periodName, String startDate,
                             String endDate, String channelNo, String status, BigDecimal periodStandardRate,
                             Integer requiredDays, String remark) {
        updatePeriod(aggregatorId, periodId, periodName, startDate, endDate, channelNo, status,
                periodStandardRate, requiredDays, remark, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePeriod(String aggregatorId, long periodId, String periodName, String startDate,
                             String endDate, String channelNo, String status, BigDecimal periodStandardRate,
                             Integer requiredDays, String remark, String resourceTypeId) {
        Map<String, Object> existing = period(aggregatorId, periodId);
        resourceTypeId = StringUtils.defaultIfBlank(resourceTypeId, string(existing.get("resourceTypeId")));
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);
        String normalized = StringUtils.upperCase(StringUtils.defaultIfBlank(status, "ACTIVE"));
        if (!Arrays.asList("PLANNED", "ACTIVE", "ENDED", "DISABLED").contains(normalized)) {
            throw new IllegalArgumentException("周期状态仅支持 PLANNED/ACTIVE/ENDED/DISABLED");
        }
        validatePeriodRange(aggregatorId, periodId, start, end, channelNo, resourceTypeId);
        jdbcTemplate.update("UPDATE la_grid_delivery_period SET resource_type_id = ?, period_name = ?, start_date = ?, end_date = ?, "
                        + "grid_channel_no = ?, status = ?, standard_rate = ?, required_days = ?, remark = ? "
                        + "WHERE id = ? AND aggregator_id = ?", resourceTypeId, periodName, java.sql.Date.valueOf(start),
                java.sql.Date.valueOf(end), StringUtils.defaultString(channelNo), normalized,
                periodStandardRate == null ? standardRate : periodStandardRate,
                requiredDays == null ? 7 : Math.max(1, requiredDays), remark, periodId, aggregatorId);
    }

    public List<Map<String, Object>> automaticPeriods(LocalDate date, boolean includeEnded) {
        String statuses = includeEnded ? "('ACTIVE','ENDED')" : "('ACTIVE')";
        return jdbcTemplate.queryForList("SELECT id, aggregator_id aggregatorId, resource_type_id resourceTypeId, grid_channel_no channelNo "
                + "FROM la_grid_delivery_period WHERE status IN " + statuses + " AND resource_type_id IS NOT NULL "
                + "AND start_date <= ? AND end_date >= ?",
                java.sql.Date.valueOf(date), java.sql.Date.valueOf(date));
    }

    private void validatePeriodRange(String aggregatorId, Long periodId, LocalDate start, LocalDate end,
                                     String channelNo, String resourceTypeId) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("周期结束日期不能早于开始日期");
        }
        if (ChronoUnit.DAYS.between(start, end) + 1 > 366) {
            throw new IllegalArgumentException("单个电网上送周期不能超过 366 天");
        }
        Integer overlaps = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM la_grid_delivery_period "
                + "WHERE aggregator_id = ? AND IFNULL(resource_type_id, '') = ? AND IFNULL(grid_channel_no, '') = ? AND status <> 'DISABLED' "
                        + "AND (? IS NULL OR id <> ?) AND start_date <= ? AND end_date >= ?", Integer.class,
                aggregatorId, StringUtils.defaultString(resourceTypeId), StringUtils.defaultString(channelNo), periodId, periodId,
                java.sql.Date.valueOf(end), java.sql.Date.valueOf(start));
        if (overlaps != null && overlaps > 0) {
            throw new IllegalArgumentException("该日期范围与同一电网通道的已有周期重叠");
        }
    }

    private Long findPeriodId(String aggregatorId, LocalDate date, String channelNo, String resourceTypeId) {
        List<Long> ids = jdbcTemplate.queryForList("SELECT id FROM la_grid_delivery_period WHERE aggregator_id = ? "
                        + "AND start_date <= ? AND end_date >= ? AND status <> 'DISABLED' "
                        + "AND (? = '' OR resource_type_id = ?) AND (? = '' OR grid_channel_no = ?) ORDER BY start_date DESC LIMIT 1", Long.class,
                aggregatorId, java.sql.Date.valueOf(date), java.sql.Date.valueOf(date),
                StringUtils.defaultString(resourceTypeId), StringUtils.defaultString(resourceTypeId),
                StringUtils.defaultString(channelNo), StringUtils.defaultString(channelNo));
        return ids.isEmpty() ? null : ids.get(0);
    }

    private List<Map<String, Object>> expectedDeviceMeasurements(String aggregatorId, LocalDateTime issueTime,
                                                                  String singleCode, String resourceTypeId) {
        String sql = "SELECT d.snapshot_id snapshotId, d.ent_id entId, d.single_code singleCode, d.single_name singleName, "
                + "d.device_code deviceCode FROM la_model_snapshot s JOIN la_model_snapshot_device d ON d.snapshot_id = s.id "
                + "WHERE s.aggregator_id = ? AND s.status IN ('EFFECTIVE','EXPIRED') AND s.effective_start <= ? "
                + "AND (s.effective_end IS NULL OR s.effective_end > ?)"
                + (StringUtils.isBlank(singleCode) ? "" : " AND d.single_code = ?")
                + (StringUtils.isBlank(resourceTypeId) ? "" : " AND d.resource_type_id = ?")
                + " ORDER BY d.single_code, d.device_code";
        List<Object> args = new ArrayList<>();
        args.add(aggregatorId);
        args.add(Timestamp.valueOf(issueTime));
        args.add(Timestamp.valueOf(issueTime));
        if (StringUtils.isNotBlank(singleCode)) {
            args.add(singleCode);
        }
        if (StringUtils.isNotBlank(resourceTypeId)) {
            args.add(resourceTypeId);
        }
        List<Map<String, Object>> devices = jdbcTemplate.queryForList(sql, args.toArray());
        for (Map<String, Object> device : devices) {
            List<Map<String, Object>> values = jdbcTemplate.queryForList("SELECT point_value value, quality, receive_time receiveTime "
                            + "FROM iot_telemetry_minute WHERE aggregator_id = ? AND device_code = ? "
                            + "AND minute_time = ? AND point_code IN ('P','active_power') ORDER BY id DESC LIMIT 1",
                    aggregatorId, device.get("deviceCode"), Timestamp.valueOf(issueTime));
            if (values.isEmpty()) {
                device.put("value", null);
                device.put("quality", "missing");
                device.put("receiveTime", null);
            } else {
                device.putAll(values.get(0));
            }
        }
        return devices;
    }

    private void upsertIssue(String aggregatorId, Long periodId, String resourceTypeId, LocalDate date, String channel, String issueType,
                             LocalDateTime issueTime, String singleCode, Long deviceId, String reason) {
        String key = (periodId == null ? "0" : periodId) + "|" + StringUtils.defaultString(resourceTypeId) + "|" + issueType + "|"
                + issueTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "|" + channel + "|" + StringUtils.defaultString(singleCode) + "|"
                + (deviceId == null ? "" : deviceId);
        jdbcTemplate.update("INSERT INTO la_grid_quality_issue (aggregator_id, data_date, period_id, resource_type_id, issue_key, issue_type, "
                        + "issue_time, grid_channel_no, single_code, device_id, reason, status, current_detected, "
                        + "first_detected_time, last_detected_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'OPEN', 1, NOW(), NOW()) "
                        + "ON DUPLICATE KEY UPDATE reason = VALUES(reason), current_detected = 1, last_detected_time = NOW(), "
                        + "resolved_time = CASE WHEN status = 'RESOLVED' THEN NULL ELSE resolved_time END, "
                        + "status = CASE WHEN status = 'RESOLVED' THEN 'OPEN' ELSE status END",
                aggregatorId, java.sql.Date.valueOf(date), periodId, resourceTypeId, key, issueType, Timestamp.valueOf(issueTime), channel,
                singleCode, deviceId, reason);
    }

    private String issueType(String prefix, String status) {
        if ("SEND_FAILED".equals(status)) {
            return prefix + "_SEND_FAILED";
        }
        if ("INCOMPLETE".equals(status)) {
            return prefix + "_INCOMPLETE";
        }
        if ("INVALID".equals(status)) {
            return prefix + "_INVALID";
        }
        return prefix + "_MISSING";
    }

    private String reason(String label, String status, Map<String, Object> row) {
        return label + "点状态=" + status + "，来源=" + display(row.get("source"))
                + (row.get("deviceCode") == null ? "" : "，参与设备=" + row.get("deviceCode"));
    }

    private BigDecimal rate(int actual, int expected) {
        if (expected == 0) {
            return BigDecimal.valueOf(100).setScale(4);
        }
        return BigDecimal.valueOf(actual).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(expected), 4, RoundingMode.HALF_UP);
    }

    private LocalDate parseDate(String value) {
        return LocalDate.parse(value, DATE);
    }

    private LocalDate sqlDate(Object value) {
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        return parseDate(String.valueOf(value).substring(0, 10));
    }

    private LocalDateTime parseDateTime(String value) {
        return LocalDateTime.parse(value.replace('T', ' '), DATE_TIME);
    }

    private LocalDateTime time(Object value) {
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        }
        return parseDateTime(String.valueOf(value));
    }

    private String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private int number(Object value) {
        return value == null ? 0 : Integer.parseInt(String.valueOf(value));
    }

    private BigDecimal decimal(Object value) {
        return value == null ? standardRate : new BigDecimal(String.valueOf(value));
    }

    private String display(Object value) {
        return value == null ? "缺失" : String.valueOf(value);
    }

    private String periodResourceType(Map<String, Object> period, String requested) {
        String configured = string(period.get("resourceTypeId"));
        if (StringUtils.isNotBlank(requested) && StringUtils.isNotBlank(configured)
                && !StringUtils.equals(requested, configured)) {
            throw new IllegalArgumentException("所选能源与电网上送周期不一致");
        }
        return StringUtils.defaultIfBlank(requested, configured);
    }
}
