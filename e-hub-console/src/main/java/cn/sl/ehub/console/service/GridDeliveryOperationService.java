package cn.sl.ehub.console.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GridDeliveryOperationService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate;

    @Value("${grid.delivery.upstream-base-url:http://127.0.0.1:8088}")
    private String upstreamBaseUrl;

    public GridDeliveryOperationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(130000);
        this.restTemplate = new RestTemplate(factory);
    }

    public Map<String, Object> preview(String aggregatorId, String resourceTypeId) {
        List<Map<String, Object>> types = jdbcTemplate.queryForList(
                "SELECT name FROM aggregator_resource_type WHERE aggregator_id = ? AND id = ? LIMIT 1",
                aggregatorId, resourceTypeId);
        if (types.isEmpty()) throw new IllegalArgumentException("所选资源类型不存在");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("resourceTypeId", resourceTypeId);
        result.put("resourceTypeName", types.get(0).get("name"));
        List<Map<String, Object>> counts = jdbcTemplate.queryForList("SELECT COUNT(1) totalCount, "
                        + "SUM(CASE WHEN controll = '1' THEN 1 ELSE 0 END) participantCount, "
                        + "SUM(CASE WHEN controll <> '1' OR controll IS NULL THEN 1 ELSE 0 END) nonParticipantCount "
                        + "FROM aggregator_single_model_data WHERE aggregator_id = ? AND resource_type_id = ?",
                aggregatorId, resourceTypeId);
        if (!counts.isEmpty()) result.putAll(counts.get(0));
        try {
            List<Map<String, Object>> latest = jdbcTemplate.queryForList("SELECT operation_status status, "
                            + "response_message responseMessage, operator_name operatorName, create_time createTime "
                            + "FROM la_grid_manual_operation WHERE aggregator_id = ? AND resource_type_id = ? "
                            + "AND operation_type = 'SEND_MODEL' ORDER BY id DESC LIMIT 1", aggregatorId, resourceTypeId);
            result.put("lastOperation", latest.isEmpty() ? null : latest.get(0));
        } catch (Exception ignored) {
            result.put("lastOperation", null);
        }
        return result;
    }

    public Map<String, Object> sendModel(String aggregatorId, String resourceTypeId,
                                         String operatorId, String operatorName) {
        Map<String, Object> preview = preview(aggregatorId, resourceTypeId);
        URI uri = UriComponentsBuilder.fromHttpUrl(StringUtils.removeEnd(upstreamBaseUrl, "/"))
                .path("/delivery/singleModelDataDelivery")
                .queryParam("aggregatorId", aggregatorId)
                .queryParam("energyType", preview.get("resourceTypeName"))
                .build().encode().toUri();
        return execute(uri, preview, aggregatorId, resourceTypeId, "SEND_MODEL", null, operatorId, operatorName);
    }

    public Map<String, Object> retrySingle(String aggregatorId, String resourceTypeId, long epochSecond,
                                           String operatorId, String operatorName) {
        URI uri = UriComponentsBuilder.fromHttpUrl(StringUtils.removeEnd(upstreamBaseUrl, "/"))
                .path("/delivery/singleMeasRetry")
                .queryParam("aggregatorId", aggregatorId)
                .queryParam("resourceTypeId", resourceTypeId)
                .queryParam("time", epochSecond)
                .build().encode().toUri();
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("resourceTypeId", resourceTypeId);
        context.put("time", epochSecond);
        LocalDateTime businessTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ZoneId.systemDefault());
        return execute(uri, context, aggregatorId, resourceTypeId, "RETRY_SINGLE", businessTime,
                operatorId, operatorName);
    }

    public Map<String, Object> retrySingleRange(String aggregatorId, String resourceTypeId, String startTime,
                                                String endTime, String operatorId, String operatorName) {
        LocalDateTime start = parseQuarter(startTime);
        LocalDateTime end = parseQuarter(endTime);
        if (end.isBefore(start)) throw new IllegalArgumentException("补送结束时间不能早于开始时间");
        long batches = java.time.Duration.between(start, end).toMinutes() / 15 + 1;
        if (batches > 16) throw new IllegalArgumentException("单次最多补送16个连续15分钟批次");
        List<Map<String, Object>> results = new ArrayList<>();
        int success = 0;
        LocalDateTime cursor = start;
        while (!cursor.isAfter(end)) {
            long epoch = cursor.atZone(ZoneId.systemDefault()).toEpochSecond();
            try {
                results.add(retrySingle(aggregatorId, resourceTypeId, epoch, operatorId, operatorName));
                success++;
            } catch (RuntimeException ex) {
                Map<String, Object> failed = new LinkedHashMap<>();
                failed.put("time", cursor.toString());
                failed.put("status", "FAILED");
                failed.put("message", ex.getMessage());
                results.add(failed);
            }
            cursor = cursor.plusMinutes(15);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("startTime", start.toString());
        result.put("endTime", end.toString());
        result.put("total", results.size());
        result.put("success", success);
        result.put("failed", results.size() - success);
        result.put("items", results);
        return result;
    }

    private LocalDateTime parseQuarter(String value) {
        if (StringUtils.isBlank(value)) throw new IllegalArgumentException("补送起止时间不能为空");
        LocalDateTime parsed = LocalDateTime.parse(value.replace('T', ' '),
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (parsed.getSecond() != 0 || parsed.getNano() != 0 || parsed.getMinute() % 15 != 0) {
            throw new IllegalArgumentException("补送时间必须为每小时00、15、30、45分钟");
        }
        return parsed;
    }

    public Map<String, Object> connectionOverview(String aggregatorId, String resourceTypeId, String dataDate) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("model", preview(aggregatorId, resourceTypeId));
        result.put("measurement", measurementStatus(aggregatorId, resourceTypeId));
        return result;
    }

    public Map<String, Object> peakPlanStatus(String aggregatorId, String resourceTypeId, String dataDate) {
        LocalDate date = LocalDate.parse(dataDate, DATE);
        Timestamp start = Timestamp.valueOf(date.atStartOfDay());
        Timestamp end = Timestamp.valueOf(date.plusDays(1).atStartOfDay());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dataDate", dataDate);
        result.put("bepfCount", count("mos_peak_bepf_data", aggregatorId, resourceTypeId, start, end, true));
        result.put("mpscCount", count("mos_peak_mpsc_data", aggregatorId, resourceTypeId, start, end, true));
        result.put("dailyCount", count("mos_peak_third_party_bid_data", aggregatorId, resourceTypeId, start, end, false));
        result.put("latest96Point", latestPeakLog(aggregatorId, resourceTypeId, dataDate, "96POINT"));
        result.put("latestDaily", latestPeakLog(aggregatorId, resourceTypeId, dataDate, "DAILY"));
        return result;
    }

    public Map<String, Object> operationRecords(String aggregatorId, String resourceTypeId, String operationType,
                                                 int pageIndex, int pageSize) {
        int safePage = Math.max(1, pageIndex);
        int safeSize = Math.min(100, Math.max(10, pageSize));
        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE aggregator_id = ? AND resource_type_id = ?");
        args.add(aggregatorId);
        args.add(resourceTypeId);
        if (StringUtils.isNotBlank(operationType)) {
            where.append(" AND operation_type = ?");
            args.add(operationType);
        }
        try {
            Long total = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM la_grid_manual_operation" + where,
                    args.toArray(), Long.class);
            List<Object> pageArgs = new ArrayList<>(args);
            pageArgs.add(safeSize);
            pageArgs.add((safePage - 1) * safeSize);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT id, operation_type operationType, business_time businessTime, "
                            + "operation_status status, response_message responseMessage, operator_id operatorId, "
                            + "operator_name operatorName, create_time createTime FROM la_grid_manual_operation"
                            + where + " ORDER BY id DESC LIMIT ? OFFSET ?", pageArgs.toArray());
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("list", rows);
            result.put("total", total == null ? 0 : total);
            result.put("pageIndex", safePage);
            result.put("pageSize", safeSize);
            return result;
        } catch (Exception ignored) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("list", Collections.emptyList());
            result.put("total", 0);
            result.put("pageIndex", safePage);
            result.put("pageSize", safeSize);
            return result;
        }
    }

    private Map<String, Object> measurementStatus(String aggregatorId, String resourceTypeId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("latestTotalTime", scalar("SELECT MAX(minute_time) FROM la_grid_total_minute "
                + "WHERE aggregator_id = ? AND resource_type_id = ? AND has_value = 1", aggregatorId, resourceTypeId));
        result.put("latestSingleTime", scalar("SELECT MAX(quarter_time) FROM la_grid_single_quarter "
                + "WHERE aggregator_id = ? AND resource_type_id = ? AND has_value = 1", aggregatorId, resourceTypeId));
        result.put("latestFailedBatch", scalar("SELECT MAX(quarter_time) FROM la_grid_single_quarter "
                + "WHERE aggregator_id = ? AND resource_type_id = ? AND (has_value = 0 OR UPPER(send_status) = 'FAILED')",
                aggregatorId, resourceTypeId));
        return result;
    }

    private Object scalar(String sql, Object... args) {
        try {
            return jdbcTemplate.queryForObject(sql, args, Object.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private int count(String table, String aggregatorId, String resourceTypeId, Timestamp start, Timestamp end,
                      boolean excludeStart) {
        String comparator = excludeStart ? ">" : ">=";
        Integer value = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM " + table
                        + " WHERE aggregator_id = ? AND source_id = ? AND data_time " + comparator
                        + " ? AND data_time <= ?", Integer.class, aggregatorId, resourceTypeId, start, end);
        return value == null ? 0 : value;
    }

    private Map<String, Object> latestPeakLog(String aggregatorId, String resourceTypeId, String dataDate,
                                               String dataType) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT delivery_status status, create_time createTime, remark FROM peak_plan_delivery_log "
                            + "WHERE aggregator_id = ? AND source_id = ? AND data_type = ? AND DATE(data_date) = ? "
                            + "ORDER BY id DESC LIMIT 1", aggregatorId, resourceTypeId, dataType, dataDate);
            return rows.isEmpty() ? null : rows.get(0);
        } catch (Exception ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> execute(URI uri, Map<String, Object> context, String aggregatorId,
                                        String resourceTypeId, String operationType, LocalDateTime businessTime,
                                        String operatorId, String operatorName) {
        try {
            Map<String, Object> response = restTemplate.exchange(uri, HttpMethod.POST, HttpEntity.EMPTY, Map.class).getBody();
            if (response == null) {
                throw new IllegalStateException("upstream 未返回上送结果");
            }
            Map<String, Object> result = new LinkedHashMap<>(context);
            result.putAll(response);
            Object code = result.get("code");
            String message = String.valueOf(result.get("data"));
            if (code == null || !"200".equals(String.valueOf(code))
                    || StringUtils.containsIgnoreCase(message, "失败")
                    || StringUtils.containsIgnoreCase(message, "error")
                    || StringUtils.containsIgnoreCase(message, "not found")
                    || StringUtils.containsIgnoreCase(message, "no config")) {
                throw new IllegalStateException(StringUtils.defaultIfBlank(String.valueOf(result.get("msg")), message));
            }
            record(aggregatorId, resourceTypeId, operationType, businessTime, "SUCCESS", message,
                    operatorId, operatorName);
            return result;
        } catch (RuntimeException ex) {
            record(aggregatorId, resourceTypeId, operationType, businessTime, "FAILED", ex.getMessage(),
                    operatorId, operatorName);
            throw ex;
        }
    }

    private void record(String aggregatorId, String resourceTypeId, String type, LocalDateTime businessTime,
                        String status, String response, String operatorId, String operatorName) {
        try {
            jdbcTemplate.update("INSERT INTO la_grid_manual_operation (aggregator_id, resource_type_id, operation_type, "
                            + "business_time, operation_status, response_message, operator_id, operator_name) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", aggregatorId, resourceTypeId, type,
                    businessTime == null ? null : Timestamp.valueOf(businessTime), status,
                    StringUtils.abbreviate(response, 2048), operatorId, operatorName);
        } catch (Exception ignored) {
            // 操作结果落库失败不能覆盖真实电网上送结果。
        }
    }
}
