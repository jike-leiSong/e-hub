package cn.sl.ehub.service.service;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.service.dto.iot.IotCimDataItem;
import cn.sl.ehub.service.dto.iot.IotCimDataReceiveReq;
import cn.sl.ehub.service.dto.iot.IotDataReceiveFailItem;
import cn.sl.ehub.service.dto.iot.IotDataReceiveResp;
import cn.sl.ehub.service.dto.iot.IotOriginDataItem;
import cn.sl.ehub.service.dto.iot.IotOriginDataReceiveReq;
import cn.sl.ehub.service.mapper.IotAccessAppMapper;
import cn.sl.ehub.service.mapper.IotDeviceMapper;
import cn.sl.ehub.service.mapper.IotDevicePointMapper;
import cn.sl.ehub.service.mapper.IotTelemetryMinuteMapper;
import cn.sl.ehub.service.mapper.IotTelemetryRawMapper;
import cn.sl.ehub.service.mapper.IotUnmatchedTelemetryLogMapper;
import cn.sl.ehub.service.vo.IotAccessApp;
import cn.sl.ehub.service.vo.IotDevice;
import cn.sl.ehub.service.vo.IotDevicePoint;
import cn.sl.ehub.service.vo.IotTelemetryMinute;
import cn.sl.ehub.service.vo.IotTelemetryRaw;
import cn.sl.ehub.service.vo.IotUnmatchedTelemetryLog;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class IotTelemetryIngestService {

    private static final String HEADER_ACCESS_KEY = "X-GW-AccessKey";
    private static final String INTERFACE_ORIGIN = "originData";
    private static final String INTERFACE_CIM = "cimData";
    private static final String QUALITY_NORMAL = "normal";
    private static final String MATCH_STATUS_MATCHED = "matched";
    private static final String MATCH_STATUS_DEVICE_NOT_FOUND = "device_not_found";
    private static final String MATCH_STATUS_DEVICE_INVALID = "device_invalid";
    private static final String MATCH_STATUS_POINT_NOT_FOUND = "point_not_found";
    private static final String MATCH_STATUS_POINT_INVALID = "point_invalid";
    private static final String MATCH_STATUS_PARAM_INVALID = "param_invalid";
    private static final String MATCH_STATUS_TIME_INVALID = "time_invalid";
    private static final String MATCH_STATUS_VALUE_INVALID = "value_invalid";

    private final IotAccessAppMapper iotAccessAppMapper;
    private final IotDeviceMapper iotDeviceMapper;
    private final IotDevicePointMapper iotDevicePointMapper;
    private final IotTelemetryMinuteMapper iotTelemetryMinuteMapper;
    private final IotTelemetryRawMapper iotTelemetryRawMapper;
    private final IotUnmatchedTelemetryLogMapper iotUnmatchedTelemetryLogMapper;

    public IotTelemetryIngestService(IotAccessAppMapper iotAccessAppMapper,
                                     IotDeviceMapper iotDeviceMapper,
                                     IotDevicePointMapper iotDevicePointMapper,
                                     IotTelemetryMinuteMapper iotTelemetryMinuteMapper,
                                     IotTelemetryRawMapper iotTelemetryRawMapper,
                                     IotUnmatchedTelemetryLogMapper iotUnmatchedTelemetryLogMapper) {
        this.iotAccessAppMapper = iotAccessAppMapper;
        this.iotDeviceMapper = iotDeviceMapper;
        this.iotDevicePointMapper = iotDevicePointMapper;
        this.iotTelemetryMinuteMapper = iotTelemetryMinuteMapper;
        this.iotTelemetryRawMapper = iotTelemetryRawMapper;
        this.iotUnmatchedTelemetryLogMapper = iotUnmatchedTelemetryLogMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public IotDataReceiveResp ingestOriginData(String accessKey, IotOriginDataReceiveReq req) {
        IotAccessApp accessApp = requireAccessApp(accessKey);
        IotDataReceiveResp resp = new IotDataReceiveResp();
        if (req == null || CollectionUtils.isEmpty(req.getDataList())) {
            return resp;
        }
        for (IotOriginDataItem item : req.getDataList()) {
            processOriginItem(resp, accessApp, item);
        }
        return resp;
    }

    @Transactional(rollbackFor = Exception.class)
    public IotDataReceiveResp ingestCimData(String accessKey, IotCimDataReceiveReq req) {
        IotAccessApp accessApp = requireAccessApp(accessKey);
        IotDataReceiveResp resp = new IotDataReceiveResp();
        if (req == null || CollectionUtils.isEmpty(req.getData())) {
            return resp;
        }
        for (IotCimDataItem item : req.getData()) {
            processCimItem(resp, accessApp, item);
        }
        return resp;
    }

    private void processOriginItem(IotDataReceiveResp resp, IotAccessApp accessApp, IotOriginDataItem item) {
        String rawPayload = buildOriginRawPayload(item);
        String externalDeviceId = item == null ? null : item.getDeviceId();
        String externalMetric = item == null ? null : item.getMetric();
        String rawValue = item == null ? null : item.getValue();
        Date dataTime;
        try {
            dataTime = parseDataTime(item == null ? null : item.getDataTime());
        } catch (BaseException e) {
            saveRawRecord(accessApp, INTERFACE_ORIGIN, null, null, null, externalDeviceId, externalMetric, null, rawValue, rawPayload, MATCH_STATUS_TIME_INVALID, "TIME_INVALID");
            failOrigin(resp, accessApp, item, "TIME_INVALID", rawPayload, null);
            return;
        }
        if (item == null || StringUtils.isBlank(item.getDeviceId()) || StringUtils.isBlank(item.getMetric())) {
            saveRawRecord(accessApp, INTERFACE_ORIGIN, null, null, null, externalDeviceId, externalMetric, dataTime, rawValue, rawPayload, MATCH_STATUS_PARAM_INVALID, "PARAM_INVALID");
            failOrigin(resp, accessApp, item, "PARAM_INVALID", rawPayload, dataTime);
            return;
        }
        MatchResult result = resolveMatch(accessApp, item.getDeviceId(), item.getMetric());
        if (!result.matched()) {
            saveRawRecord(accessApp, INTERFACE_ORIGIN, result.device == null ? null : result.device.getProjectId(), result.device, result.point, externalDeviceId, externalMetric, dataTime, rawValue, rawPayload, result.matchStatus, result.reason);
            failOrigin(resp, accessApp, item, result.reason, rawPayload, dataTime);
            return;
        }
        Double value = parseValue(item.getValue());
        if (value == null) {
            saveRawRecord(accessApp, INTERFACE_ORIGIN, result.device.getProjectId(), result.device, result.point, externalDeviceId, externalMetric, dataTime, rawValue, rawPayload, MATCH_STATUS_VALUE_INVALID, "VALUE_INVALID");
            failOrigin(resp, accessApp, item, "VALUE_INVALID", rawPayload, dataTime);
            return;
        }
        saveRawRecord(accessApp, INTERFACE_ORIGIN, result.device.getProjectId(), result.device, result.point, externalDeviceId, externalMetric, dataTime, rawValue, rawPayload, MATCH_STATUS_MATCHED, null);
        saveMinuteRecord(accessApp, result.device.getProjectId(), result.device, result.point, dataTime, value, item.getValue());
        updateDeviceOnline(result.device, dataTime);
        resp.addSuccess();
    }

    private void processCimItem(IotDataReceiveResp resp, IotAccessApp accessApp, IotCimDataItem item) {
        String rawPayload = buildCimRawPayload(item);
        String externalDeviceId = item == null ? null : item.getDeviceId();
        String externalMetric = item == null ? null : item.getMetric();
        String rawValue = item == null ? null : item.getValue();
        Date dataTime;
        try {
            dataTime = parseDataTime(item == null ? null : item.getDataTime());
        } catch (BaseException e) {
            saveRawRecord(accessApp, INTERFACE_CIM, null, null, null, externalDeviceId, externalMetric, null, rawValue, rawPayload, MATCH_STATUS_TIME_INVALID, "TIME_INVALID");
            failCim(resp, accessApp, item, "TIME_INVALID", rawPayload, null);
            return;
        }
        if (item == null || StringUtils.isBlank(item.getDeviceId()) || StringUtils.isBlank(item.getMetric())) {
            saveRawRecord(accessApp, INTERFACE_CIM, null, null, null, externalDeviceId, externalMetric, dataTime, rawValue, rawPayload, MATCH_STATUS_PARAM_INVALID, "PARAM_INVALID");
            failCim(resp, accessApp, item, "PARAM_INVALID", rawPayload, dataTime);
            return;
        }
        MatchResult result = resolveMatch(accessApp, item.getDeviceId(), item.getMetric());
        if (!result.matched()) {
            saveRawRecord(accessApp, INTERFACE_CIM, result.device == null ? null : result.device.getProjectId(), result.device, result.point, externalDeviceId, externalMetric, dataTime, rawValue, rawPayload, result.matchStatus, result.reason);
            failCim(resp, accessApp, item, result.reason, rawPayload, dataTime);
            return;
        }
        Double value = parseValue(item.getValue());
        if (value == null) {
            saveRawRecord(accessApp, INTERFACE_CIM, result.device.getProjectId(), result.device, result.point, externalDeviceId, externalMetric, dataTime, rawValue, rawPayload, MATCH_STATUS_VALUE_INVALID, "VALUE_INVALID");
            failCim(resp, accessApp, item, "VALUE_INVALID", rawPayload, dataTime);
            return;
        }
        saveRawRecord(accessApp, INTERFACE_CIM, result.device.getProjectId(), result.device, result.point, externalDeviceId, externalMetric, dataTime, rawValue, rawPayload, MATCH_STATUS_MATCHED, null);
        saveMinuteRecord(accessApp, result.device.getProjectId(), result.device, result.point, dataTime, value, item.getValue());
        updateDeviceOnline(result.device, dataTime);
        resp.addSuccess();
    }

    private MatchResult resolveMatch(IotAccessApp accessApp, String externalDeviceId, String externalMetric) {
        MatchResult result = new MatchResult();
        IotDevice device = findDeviceByExternalId(accessApp.getSourceCode(), externalDeviceId);
        if (device == null) {
            result.reason = "DEVICE_NOT_MATCHED";
            result.matchStatus = MATCH_STATUS_DEVICE_NOT_FOUND;
            return result;
        }
        result.device = device;
        if (Integer.valueOf(1).equals(device.getDeleted())
                || Integer.valueOf(0).equals(device.getStatus())
                || Integer.valueOf(0).equals(device.getAssetStatus())) {
            result.reason = "DEVICE_INVALID";
            result.matchStatus = MATCH_STATUS_DEVICE_INVALID;
            return result;
        }
        IotDevicePoint point = findPointByExternalMetric(device.getId(), externalMetric);
        if (point == null) {
            result.reason = "POINT_NOT_MATCHED";
            result.matchStatus = MATCH_STATUS_POINT_NOT_FOUND;
            return result;
        }
        result.point = point;
        if (Integer.valueOf(1).equals(point.getDeleted()) || Integer.valueOf(0).equals(point.getStatus())) {
            result.reason = "POINT_INVALID";
            result.matchStatus = MATCH_STATUS_POINT_INVALID;
            return result;
        }
        result.matchStatus = MATCH_STATUS_MATCHED;
        return result;
    }

    private IotDevice findDeviceByExternalId(String sourceCode, String externalDeviceId) {
        Example example = new Example(IotDevice.class);
        example.createCriteria()
                .andEqualTo("thirdPartyApi", StringUtils.trim(sourceCode))
                .andEqualTo("thirdPartyCode", StringUtils.trim(externalDeviceId))
                .andEqualTo("deleted", 0);
        List<IotDevice> devices = iotDeviceMapper.selectByExample(example);
        if (devices.isEmpty()) {
            return null;
        }
        if (devices.size() > 1) {
            throw new BaseException(400, "三方设备标识匹配到多个设备，请检查设备映射配置");
        }
        return devices.get(0);
    }

    private IotDevicePoint findPointByExternalMetric(Long deviceId, String externalMetric) {
        Example example = new Example(IotDevicePoint.class);
        example.createCriteria()
                .andEqualTo("deviceId", deviceId)
                .andEqualTo("thirdPartyCode", StringUtils.trim(externalMetric))
                .andEqualTo("deleted", 0);
        List<IotDevicePoint> points = iotDevicePointMapper.selectByExample(example);
        return points.isEmpty() ? null : points.get(0);
    }

    private void saveRawRecord(IotAccessApp accessApp, String interfaceType, String projectId,
                               IotDevice device, IotDevicePoint point, String externalDeviceId,
                               String externalMetric, Date dataTime, String rawValue,
                               String rawPayload, String matchStatus, String matchReason) {
        IotTelemetryRaw raw = new IotTelemetryRaw();
        raw.setInterfaceType(interfaceType);
        raw.setSourceCode(accessApp.getSourceCode());
        raw.setEntId(device == null ? null : device.getEntId());
        raw.setProjectId(projectId);
        raw.setDeviceId(device == null ? null : device.getId());
        raw.setDeviceCode(device == null ? null : device.getDeviceCode());
        raw.setPointCode(point == null ? null : point.getPropertyCode());
        raw.setExternalDeviceId(externalDeviceId);
        raw.setExternalMetric(externalMetric);
        raw.setDataTime(dataTime);
        raw.setRawValue(rawValue);
        raw.setReceiveTime(new Date());
        raw.setRawPayload(rawPayload);
        raw.setMatchStatus(matchStatus);
        raw.setMatchReason(matchReason);
        raw.setCreateTime(new Date());
        iotTelemetryRawMapper.insertRaw(raw);
    }

    private void saveMinuteRecord(IotAccessApp accessApp, String projectId, IotDevice device, IotDevicePoint point,
                                  Date dataTime, Double standardValue, String rawValue) {
        Date minuteTime = toMinute(dataTime);
        Date now = new Date();
        IotTelemetryMinute record = new IotTelemetryMinute();
        record.setAggregatorId(accessApp.getAggregatorId());
        record.setEntId(device.getEntId());
        record.setProjectId(projectId);
        record.setDeviceId(device.getId());
        record.setDeviceCode(device.getDeviceCode());
        record.setPointCode(point.getPropertyCode());
        record.setDataTime(dataTime);
        record.setMinuteTime(minuteTime);
        record.setPointValue(standardValue);
        record.setUnit(point.getUnit());
        record.setQuality(QUALITY_NORMAL);
        record.setSourceCode(accessApp.getSourceCode());
        record.setReceiveTime(now);
        record.setRawValue(rawValue);
        iotTelemetryMinuteMapper.upsertMinute(record);
    }

    private void updateDeviceOnline(IotDevice device, Date dataTime) {
        device.setOnlineStatus(1);
        if (dataTime != null && (device.getLastDataTime() == null || !device.getLastDataTime().after(dataTime))) {
            device.setLastDataTime(dataTime);
        }
        device.setUpdateTime(new Date());
        iotDeviceMapper.updateByPrimaryKeySelective(device);
    }

    private IotAccessApp requireAccessApp(String accessKey) {
        if (StringUtils.isBlank(accessKey)) {
            throwAuth("缺少请求头：" + HEADER_ACCESS_KEY);
        }
        Example example = new Example(IotAccessApp.class);
        example.createCriteria()
                .andEqualTo("accessKey", StringUtils.trim(accessKey))
                .andEqualTo("enabled", 1);
        List<IotAccessApp> apps = iotAccessAppMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(apps)) {
            throwAuth("认证失败");
        }
        return apps.get(0);
    }

    private void failOrigin(IotDataReceiveResp resp, IotAccessApp accessApp, IotOriginDataItem item,
                            String reason, String rawPayload, Date dataTime) {
        IotDataReceiveFailItem failItem = new IotDataReceiveFailItem();
        if (item != null) {
            failItem.setDeviceId(item.getDeviceId());
            failItem.setMetric(item.getMetric());
        }
        failItem.setReason(reason);
        resp.addFail(failItem);
        saveUnmatched(accessApp, INTERFACE_ORIGIN, failItem, dataTime, item == null ? null : item.getValue(), reason, rawPayload);
    }

    private void failCim(IotDataReceiveResp resp, IotAccessApp accessApp, IotCimDataItem item,
                         String reason, String rawPayload, Date dataTime) {
        IotDataReceiveFailItem failItem = new IotDataReceiveFailItem();
        if (item != null) {
            failItem.setDeviceId(item.getDeviceId());
            failItem.setMetric(item.getMetric());
        }
        failItem.setReason(reason);
        resp.addFail(failItem);
        saveUnmatched(accessApp, INTERFACE_CIM, failItem, dataTime, item == null ? null : item.getValue(), reason, rawPayload);
    }

    private void saveUnmatched(IotAccessApp accessApp, String interfaceType, IotDataReceiveFailItem failItem,
                               Date dataTime, String value, String reason, String rawPayload) {
        IotUnmatchedTelemetryLog log = new IotUnmatchedTelemetryLog();
        log.setSourceCode(accessApp == null ? null : accessApp.getSourceCode());
        log.setInterfaceType(interfaceType);
        log.setExternalDeviceId(failItem.getDeviceId());
        log.setExternalMetric(failItem.getMetric());
        log.setDataTime(dataTime);
        log.setValue(value);
        log.setReason(reason);
        log.setRawPayload(rawPayload);
        log.setHandled(0);
        Date now = new Date();
        log.setCreateTime(now);
        log.setUpdateTime(now);
        iotUnmatchedTelemetryLogMapper.insertSelective(log);
    }

    private String buildOriginRawPayload(IotOriginDataItem item) {
        JSONObject payload = new JSONObject();
        payload.put("data", item);
        return payload.toJSONString();
    }

    private String buildCimRawPayload(IotCimDataItem item) {
        JSONObject payload = new JSONObject();
        payload.put("data", item);
        return payload.toJSONString();
    }

    private Date parseDataTime(String value) {
        if (StringUtils.isBlank(value)) {
            throwParam("数据时间不能为空");
        }
        String text = StringUtils.trim(value);
        String[] patterns = new String[]{
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-M-d H:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy-M-d H:mm"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(pattern);
                format.setLenient(false);
                return format.parse(text);
            } catch (ParseException ignored) {
                // try next pattern
            }
        }
        throwParam("数据时间格式错误");
        return null;
    }

    private Date toMinute(Date dataTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dataTime);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Double parseValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Double.valueOf(StringUtils.trim(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void throwAuth(String message) {
        throw new BaseException(401, message);
    }

    private void throwParam(String message) {
        throw new BaseException(StatusCode.C.getCode(), message);
    }

    private static class MatchResult {
        private IotDevice device;
        private IotDevicePoint point;
        private String reason;
        private String matchStatus;

        private boolean matched() {
            return reason == null && device != null && point != null;
        }
    }
}
