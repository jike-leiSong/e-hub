package cn.sl.ehub.upstream.service;

import cn.sl.ehub.upstream.dto.*;
import cn.sl.ehub.common.vo.DataResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * BigData服务（临时替代类）
 * 原：cn.enn.bigdata.service.BigDataHandlerService
 *
 * 兼容旧 upstream DTO，底层读取标准化的 iot_telemetry_minute。
 */
@Service
@Slf4j
public class BigDataHandlerService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取实时数据
     */
    public BigDataRealTimeResp getRealTimeData(RealTimeReq req) {
        List<BigDataRealTimeResp> result = getRealTime(req, "0");
        return result.isEmpty() ? new BigDataRealTimeResp() : result.get(0);
    }

    /**
     * 获取实时数据（带标志参数）
     */
    public List<BigDataRealTimeResp> getRealTime(RealTimeReq req, String flag) {
        if (req == null || req.getListQueries() == null || req.getListQueries().isEmpty()) {
            return Collections.emptyList();
        }
        List<BigDataRealTimeResp> result = new ArrayList<>();
        for (OpentsdbReq query : req.getListQueries()) {
            BigDataRealTimeResp item = queryLatest(query);
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 获取历史数据
     */
    public List<BigDataHistoryResp> getHistory(HistoryReq req, String flag) {
        if (req == null || req.getListQueries() == null || req.getListQueries().isEmpty()) {
            return Collections.emptyList();
        }
        List<BigDataHistoryResp> result = new ArrayList<>();
        for (OpentsdbReq query : req.getListQueries()) {
            BigDataHistoryResp item = queryHistory(query, req.getStartTime(), req.getEndTime());
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 查询OpenTSDB数据
     */
    public BigDataRealTimeResp queryOpentsdb(OpentsdbReq req) {
        BigDataRealTimeResp result = queryLatest(req);
        return result == null ? new BigDataRealTimeResp() : result;
    }

    private BigDataRealTimeResp queryLatest(OpentsdbReq query) {
        if (query == null || query.getTags() == null) {
            return null;
        }
        String pointCode = pointCode(query.getMetric());
        String deviceCode = query.getTags().getEquipID();
        if (StringUtils.isBlank(pointCode) || StringUtils.isBlank(deviceCode)) {
            return null;
        }
        String sql = "SELECT minute_time, point_value FROM iot_telemetry_minute "
                + "WHERE device_code IN (?, ?) AND point_code IN (?, ?) AND minute_time <= NOW() "
                + "ORDER BY minute_time DESC LIMIT 1";
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, deviceCode, fullDeviceCode(query),
                    pointCode, aliasPointCode(pointCode));
            if (rows.isEmpty()) {
                return null;
            }
            Map<String, Object> row = rows.get(0);
            BigDataRealTimeResp result = baseRealTime(query);
            result.setDataResp(Collections.singletonList(new DataResp(
                    formatTime(row.get("minute_time")), number(row.get("point_value")))));
            return result;
        } catch (DataAccessException ex) {
            log.warn("读取标准分钟量测失败，metric={}, deviceCode={}, message={}", query.getMetric(), deviceCode, ex.getMessage());
            return null;
        }
    }

    private BigDataHistoryResp queryHistory(OpentsdbReq query, String startTime, String endTime) {
        if (query == null || query.getTags() == null) {
            return null;
        }
        String pointCode = pointCode(query.getMetric());
        String deviceCode = query.getTags().getEquipID();
        if (StringUtils.isBlank(pointCode) || StringUtils.isBlank(deviceCode)) {
            return null;
        }
        String sql = "SELECT minute_time, point_value FROM iot_telemetry_minute "
                + "WHERE device_code IN (?, ?) AND point_code IN (?, ?) AND minute_time >= ? AND minute_time < ? "
                + "ORDER BY minute_time";
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, deviceCode, fullDeviceCode(query),
                    pointCode, aliasPointCode(pointCode),
                    timestamp(startTime, LocalDateTime.now().minusMinutes(15)),
                    timestamp(endTime, LocalDateTime.now()));
            BigDataHistoryResp result = new BigDataHistoryResp();
            result.setMetric(query.getMetric());
            result.setStaId(query.getTags().getStaId());
            result.setEquipMK(query.getTags().getEquipMK());
            result.setEquipID(query.getTags().getEquipID());
            List<DataResp> values = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                values.add(new DataResp(formatTime(row.get("minute_time")), number(row.get("point_value"))));
            }
            result.setDataResp(values);
            return result;
        } catch (DataAccessException ex) {
            log.warn("读取标准分钟历史量测失败，metric={}, deviceCode={}, message={}", query.getMetric(), deviceCode, ex.getMessage());
            return null;
        }
    }

    private BigDataRealTimeResp baseRealTime(OpentsdbReq query) {
        BigDataRealTimeResp result = new BigDataRealTimeResp();
        result.setMetric(query.getMetric());
        result.setStaId(query.getTags().getStaId());
        result.setEquipMK(query.getTags().getEquipMK());
        result.setEquipID(query.getTags().getEquipID());
        return result;
    }

    private String pointCode(String metric) {
        if (StringUtils.isBlank(metric)) {
            return null;
        }
        String value = metric.trim();
        if (value.startsWith("EMS.")) {
            return value.substring(4);
        }
        return value;
    }

    private Timestamp timestamp(String value, LocalDateTime fallback) {
        if (StringUtils.isBlank(value)) {
            return Timestamp.valueOf(fallback);
        }
        try {
            return Timestamp.valueOf(value.length() == 16 ? value + ":00" : value);
        } catch (IllegalArgumentException ex) {
            return Timestamp.valueOf(fallback);
        }
    }

    private String aliasPointCode(String pointCode) {
        return "P".equalsIgnoreCase(pointCode) ? "active_power" : pointCode;
    }

    private String fullDeviceCode(OpentsdbReq query) {
        String equipMK = query.getTags().getEquipMK();
        String equipID = query.getTags().getEquipID();
        return StringUtils.isBlank(equipMK) ? equipID : equipMK + "_" + equipID;
    }

    private String formatTime(Object value) {
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime().format(DATE_TIME);
        }
        return String.valueOf(value);
    }

    private Double number(Object value) {
        return value == null ? null : Double.valueOf(String.valueOf(value));
    }
}
