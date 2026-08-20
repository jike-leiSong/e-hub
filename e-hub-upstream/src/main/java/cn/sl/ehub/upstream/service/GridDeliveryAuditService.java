package cn.sl.ehub.upstream.service;

import cn.sl.ehub.common.req.SingleMeasDeliveryReq;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 电网发送审计。审计写入失败不能影响实时上送，因此调用方会捕获异常。
 */
@Service
public class GridDeliveryAuditService {

    private final JdbcTemplate jdbcTemplate;

    public GridDeliveryAuditService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void recordTotal(String aggregatorId, String resourceTypeId, String channelNo, Map<String, String> payload,
                            String response, boolean dataComplete) {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        Long batchId = createBatch(aggregatorId, resourceTypeId, now, "TOTAL", channelNo, 1, JSON.toJSONString(payload), response);
        String value = payload.get(channelNo + "-5");
        jdbcTemplate.update("INSERT INTO la_grid_total_minute (batch_id, aggregator_id, resource_type_id, grid_channel_no, "
                        + "minute_time, total_value, has_value, quality, source_type, send_status, receive_time) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                batchId, aggregatorId, resourceTypeId, channelNo, Timestamp.valueOf(now), decimal(value),
                StringUtils.isBlank(value) || !dataComplete ? 0 : 1,
                StringUtils.isBlank(value) || !dataComplete ? "missing" : "normal",
                "UPSTREAM_PAYLOAD", sendStatus(response), Timestamp.valueOf(LocalDateTime.now()));
    }

    @SuppressWarnings("unchecked")
    public void recordSingle(String aggregatorId, String resourceTypeId, String channelNo, SingleMeasDeliveryReq request,
                             String response) {
        recordSingleAt(aggregatorId, resourceTypeId, channelNo, request, response,
                LocalDateTime.now().withSecond(0).withNano(0));
    }

    @SuppressWarnings("unchecked")
    public void recordSingleAt(String aggregatorId, String resourceTypeId, String channelNo,
                               SingleMeasDeliveryReq request, String response, LocalDateTime businessTime) {
        LocalDateTime now = businessTime.withSecond(0).withNano(0);
        List<Object> detailList = request.getSingleMeasData();
        Long batchId = createBatch(aggregatorId, resourceTypeId, now, "SINGLE_MEAS", channelNo,
                detailList == null ? 0 : detailList.size(), JSON.toJSONString(request), response);
        if (detailList == null) {
            return;
        }
        for (Object detail : detailList) {
            Map<String, Object> row = JSON.parseObject(JSON.toJSONString(detail), Map.class);
            String activePower = value(row, "userActivePower");
            boolean hasValue = StringUtils.isNotBlank(activePower)
                    && !"missing".equalsIgnoreCase(value(row, "dataQuality"));
            jdbcTemplate.update("INSERT INTO la_grid_single_quarter (batch_id, aggregator_id, resource_type_id, grid_channel_no, "
                            + "single_code, quarter_time, active_power, reactive_power, current_value, electric_quantity, "
                            + "has_value, quality, send_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    batchId, aggregatorId, resourceTypeId, channelNo, value(row, "innerStationId"), Timestamp.valueOf(now),
                    decimal(activePower), decimal(value(row, "userReactivePower")), decimal(value(row, "userElecCurrent")),
                    decimal(value(row, "todayZeroElecQuantity")), hasValue ? 1 : 0,
                    hasValue ? "normal" : "missing", sendStatus(response));
        }
    }

    private Long createBatch(String aggregatorId, String resourceTypeId, LocalDateTime time, String type, String channelNo,
                             int preparedCount, String requestPayload, String response) {
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
                    PreparedStatement statement = connection.prepareStatement("INSERT INTO la_grid_delivery_batch "
                            + "(aggregator_id, data_date, delivery_type, resource_type_id, grid_channel_no, expected_count, prepared_count, "
                            + "sent_time, response_time, send_status, response_message, request_payload, response_payload) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, aggregatorId);
            statement.setDate(2, java.sql.Date.valueOf(time.toLocalDate()));
            statement.setString(3, type);
            statement.setString(4, resourceTypeId);
            statement.setString(5, channelNo);
            statement.setInt(6, preparedCount);
            statement.setInt(7, preparedCount);
            statement.setTimestamp(8, Timestamp.valueOf(time));
            statement.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(10, sendStatus(response));
            statement.setString(11, response);
            statement.setString(12, requestPayload);
            statement.setString(13, response);
            return statement;
        }, holder);
        Number key = holder.getKey();
        if (key == null) {
            throw new IllegalStateException("未获得电网上送审计批次ID");
        }
        return key.longValue();
    }

    private String value(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private BigDecimal decimal(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String sendStatus(String response) {
        return StringUtils.isBlank(response) || StringUtils.containsIgnoreCase(response, "失败")
                ? "FAILED" : "SUCCESS";
    }
}
