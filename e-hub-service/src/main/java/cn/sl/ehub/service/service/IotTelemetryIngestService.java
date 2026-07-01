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
import cn.sl.ehub.service.mapper.IotDeviceExternalRefMapper;
import cn.sl.ehub.service.mapper.IotDeviceMapper;
import cn.sl.ehub.service.mapper.IotDevicePointMapper;
import cn.sl.ehub.service.mapper.IotPointExternalRefMapper;
import cn.sl.ehub.service.mapper.IotProjectExternalRefMapper;
import cn.sl.ehub.service.mapper.IotTelemetryMinuteMapper;
import cn.sl.ehub.service.mapper.IotUnmatchedTelemetryLogMapper;
import cn.sl.ehub.service.vo.IotAccessApp;
import cn.sl.ehub.service.vo.IotDevice;
import cn.sl.ehub.service.vo.IotDeviceExternalRef;
import cn.sl.ehub.service.vo.IotDevicePoint;
import cn.sl.ehub.service.vo.IotPointExternalRef;
import cn.sl.ehub.service.vo.IotProjectExternalRef;
import cn.sl.ehub.service.vo.IotTelemetryMinute;
import cn.sl.ehub.service.vo.IotUnmatchedTelemetryLog;
import com.alibaba.fastjson.JSON;
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

    private final IotAccessAppMapper iotAccessAppMapper;
    private final IotProjectExternalRefMapper iotProjectExternalRefMapper;
    private final IotDeviceExternalRefMapper iotDeviceExternalRefMapper;
    private final IotPointExternalRefMapper iotPointExternalRefMapper;
    private final IotDeviceMapper iotDeviceMapper;
    private final IotDevicePointMapper iotDevicePointMapper;
    private final IotTelemetryMinuteMapper iotTelemetryMinuteMapper;
    private final IotUnmatchedTelemetryLogMapper iotUnmatchedTelemetryLogMapper;

    public IotTelemetryIngestService(IotAccessAppMapper iotAccessAppMapper,
                                     IotProjectExternalRefMapper iotProjectExternalRefMapper,
                                     IotDeviceExternalRefMapper iotDeviceExternalRefMapper,
                                     IotPointExternalRefMapper iotPointExternalRefMapper,
                                     IotDeviceMapper iotDeviceMapper,
                                     IotDevicePointMapper iotDevicePointMapper,
                                     IotTelemetryMinuteMapper iotTelemetryMinuteMapper,
                                     IotUnmatchedTelemetryLogMapper iotUnmatchedTelemetryLogMapper) {
        this.iotAccessAppMapper = iotAccessAppMapper;
        this.iotProjectExternalRefMapper = iotProjectExternalRefMapper;
        this.iotDeviceExternalRefMapper = iotDeviceExternalRefMapper;
        this.iotPointExternalRefMapper = iotPointExternalRefMapper;
        this.iotDeviceMapper = iotDeviceMapper;
        this.iotDevicePointMapper = iotDevicePointMapper;
        this.iotTelemetryMinuteMapper = iotTelemetryMinuteMapper;
        this.iotUnmatchedTelemetryLogMapper = iotUnmatchedTelemetryLogMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public IotDataReceiveResp ingestOriginData(String accessKey, IotOriginDataReceiveReq req) {
        IotAccessApp accessApp = requireAccessApp(accessKey, req == null ? null : req.getUserKey(),
                req == null ? null : req.getEntId(), true);
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
        IotAccessApp accessApp = requireAccessApp(accessKey, req == null ? null : req.getUserKey(),
                req == null ? null : req.getEntId(), false);
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
        String rawPayload = JSON.toJSONString(item);
        Date dataTime;
        try {
            dataTime = parseDataTime(item == null ? null : item.getDataTime());
        } catch (BaseException e) {
            failOrigin(resp, accessApp, item, "TIME_INVALID", rawPayload, null);
            return;
        }
        if (item == null || StringUtils.isBlank(item.getDeviceId()) || StringUtils.isBlank(item.getMetric())) {
            failOrigin(resp, accessApp, item, "PARAM_INVALID", rawPayload, dataTime);
            return;
        }
        IngestContext context = resolveContext(accessApp, item.getProjectId(), item.getDeviceId(), item.getMetric());
        if (context.reason != null) {
            failOrigin(resp, accessApp, item, context.reason, rawPayload, dataTime);
            return;
        }
        Double value = parseValue(item.getValue());
        if (value == null) {
            failOrigin(resp, accessApp, item, "VALUE_INVALID", rawPayload, dataTime);
            return;
        }
        upsertTelemetry(accessApp, context, dataTime, item.getValue(), value);
        resp.addSuccess();
    }

    private void processCimItem(IotDataReceiveResp resp, IotAccessApp accessApp, IotCimDataItem item) {
        String rawPayload = JSON.toJSONString(item);
        Date dataTime;
        try {
            dataTime = parseDataTime(item == null ? null : item.getDataTime());
        } catch (BaseException e) {
            failCim(resp, accessApp, item, "TIME_INVALID", rawPayload, null);
            return;
        }
        if (item == null || StringUtils.isBlank(item.getDeviceId()) || StringUtils.isBlank(item.getMetric())) {
            failCim(resp, accessApp, item, "PARAM_INVALID", rawPayload, dataTime);
            return;
        }
        IngestContext context = resolveContext(accessApp, null, item.getDeviceId(), item.getMetric());
        if (context.reason != null) {
            failCim(resp, accessApp, item, context.reason, rawPayload, dataTime);
            return;
        }
        Double value = parseValue(item.getValue());
        if (value == null) {
            failCim(resp, accessApp, item, "VALUE_INVALID", rawPayload, dataTime);
            return;
        }
        upsertTelemetry(accessApp, context, dataTime, item.getValue(), value);
        resp.addSuccess();
    }

    private IngestContext resolveContext(IotAccessApp accessApp, String externalProjectId, String externalDeviceId, String externalMetric) {
        IngestContext context = new IngestContext();
        context.projectId = accessApp.getProjectId();
        if (StringUtils.isNotBlank(externalProjectId)) {
            IotProjectExternalRef projectRef = findProjectExternalRef(accessApp.getSourceCode(), accessApp.getEntId(), externalProjectId);
            if (projectRef != null && projectRef.getProjectId() != null) {
                context.projectId = projectRef.getProjectId();
            }
        }
        IotDeviceExternalRef deviceRef = findDeviceExternalRef(accessApp.getSourceCode(), accessApp.getEntId(), externalDeviceId);
        if (deviceRef == null) {
            context.reason = "DEVICE_NOT_MATCHED";
            return context;
        }
        IotDevice device = iotDeviceMapper.selectByPrimaryKey(deviceRef.getDeviceId());
        if (device == null || Integer.valueOf(1).equals(device.getDeleted())) {
            context.reason = "DEVICE_INVALID";
            return context;
        }
        IotPointExternalRef pointRef = findPointExternalRef(accessApp.getSourceCode(), deviceRef.getDeviceId(), externalMetric);
        if (pointRef == null) {
            context.reason = "POINT_NOT_MATCHED";
            return context;
        }
        IotDevicePoint point = iotDevicePointMapper.selectByPrimaryKey(pointRef.getPointId());
        if (point == null || Integer.valueOf(1).equals(point.getDeleted()) || Integer.valueOf(0).equals(point.getStatus())) {
            context.reason = "POINT_INVALID";
            return context;
        }
        context.device = device;
        context.deviceRef = deviceRef;
        context.point = point;
        context.pointRef = pointRef;
        if (context.projectId == null) {
            context.projectId = device.getProjectId();
        }
        return context;
    }

    private void upsertTelemetry(IotAccessApp accessApp, IngestContext context, Date dataTime, String rawValue, Double value) {
        Date minuteTime = toMinute(dataTime);
        Double standardValue = value * defaultDouble(context.pointRef.getRatio(), 1D)
                + defaultDouble(context.pointRef.getOffsetValue(), 0D);
        String pointCode = context.point.getPropertyCode();
        IotTelemetryMinute existed = findTelemetry(context.device.getId(), pointCode, minuteTime);
        Date now = new Date();
        if (existed == null) {
            IotTelemetryMinute telemetry = new IotTelemetryMinute();
            telemetry.setAggregatorId(accessApp.getAggregatorId());
            telemetry.setEntId(accessApp.getEntId());
            telemetry.setProjectId(context.projectId);
            telemetry.setDeviceId(context.device.getId());
            telemetry.setDeviceCode(context.device.getDeviceCode());
            telemetry.setPointCode(pointCode);
            telemetry.setDataTime(dataTime);
            telemetry.setMinuteTime(minuteTime);
            telemetry.setPointValue(standardValue);
            telemetry.setUnit(context.point.getUnit());
            telemetry.setQuality(QUALITY_NORMAL);
            telemetry.setSourceCode(accessApp.getSourceCode());
            telemetry.setReceiveTime(now);
            telemetry.setRawValue(rawValue);
            iotTelemetryMinuteMapper.insertSelective(telemetry);
        } else {
            existed.setDataTime(dataTime);
            existed.setPointValue(standardValue);
            existed.setUnit(context.point.getUnit());
            existed.setQuality(QUALITY_NORMAL);
            existed.setSourceCode(accessApp.getSourceCode());
            existed.setReceiveTime(now);
            existed.setRawValue(rawValue);
            iotTelemetryMinuteMapper.updateByPrimaryKeySelective(existed);
        }
        updateDeviceOnline(context.device, dataTime);
    }

    private void updateDeviceOnline(IotDevice device, Date dataTime) {
        device.setOnlineStatus(1);
        device.setLastDataTime(dataTime);
        device.setUpdateTime(new Date());
        iotDeviceMapper.updateByPrimaryKeySelective(device);
    }

    private IotAccessApp requireAccessApp(String accessKey, String userKey, String entId, boolean requireUserKey) {
        if (StringUtils.isBlank(accessKey)) {
            throwAuth("缺少请求头：" + HEADER_ACCESS_KEY);
        }
        if (requireUserKey && StringUtils.isBlank(userKey)) {
            throwAuth("userKey不能为空");
        }
        Example example = new Example(IotAccessApp.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("accessKey", StringUtils.trim(accessKey));
        criteria.andEqualTo("enabled", 1);
        if (StringUtils.isNotBlank(userKey)) {
            criteria.andEqualTo("userKey", StringUtils.trim(userKey));
        }
        if (StringUtils.isNotBlank(entId)) {
            criteria.andEqualTo("entId", StringUtils.trim(entId));
        }
        List<IotAccessApp> apps = iotAccessAppMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(apps)) {
            throwAuth("认证失败");
        }
        if (apps.size() > 1 && StringUtils.isBlank(entId)) {
            throwAuth("接入应用绑定多个企业，请传入entId");
        }
        return apps.get(0);
    }

    private IotProjectExternalRef findProjectExternalRef(String sourceCode, String entId, String externalProjectId) {
        Example example = new Example(IotProjectExternalRef.class);
        example.createCriteria()
                .andEqualTo("sourceCode", sourceCode)
                .andEqualTo("entId", entId)
                .andEqualTo("externalProjectId", externalProjectId)
                .andEqualTo("status", 1);
        List<IotProjectExternalRef> refs = iotProjectExternalRefMapper.selectByExample(example);
        return refs.isEmpty() ? null : refs.get(0);
    }

    private IotDeviceExternalRef findDeviceExternalRef(String sourceCode, String entId, String externalDeviceId) {
        Example example = new Example(IotDeviceExternalRef.class);
        example.createCriteria()
                .andEqualTo("sourceCode", sourceCode)
                .andEqualTo("entId", entId)
                .andEqualTo("externalDeviceId", externalDeviceId)
                .andEqualTo("status", 1);
        List<IotDeviceExternalRef> refs = iotDeviceExternalRefMapper.selectByExample(example);
        return refs.isEmpty() ? null : refs.get(0);
    }

    private IotPointExternalRef findPointExternalRef(String sourceCode, Long deviceId, String externalMetric) {
        Example example = new Example(IotPointExternalRef.class);
        example.createCriteria()
                .andEqualTo("sourceCode", sourceCode)
                .andEqualTo("deviceId", deviceId)
                .andEqualTo("externalMetric", externalMetric)
                .andEqualTo("status", 1);
        List<IotPointExternalRef> refs = iotPointExternalRefMapper.selectByExample(example);
        return refs.isEmpty() ? null : refs.get(0);
    }

    private IotTelemetryMinute findTelemetry(Long deviceId, String pointCode, Date minuteTime) {
        Example example = new Example(IotTelemetryMinute.class);
        example.createCriteria()
                .andEqualTo("deviceId", deviceId)
                .andEqualTo("pointCode", pointCode)
                .andEqualTo("minuteTime", minuteTime);
        List<IotTelemetryMinute> list = iotTelemetryMinuteMapper.selectByExample(example);
        return list.isEmpty() ? null : list.get(0);
    }

    private void failOrigin(IotDataReceiveResp resp, IotAccessApp accessApp, IotOriginDataItem item,
                            String reason, String rawPayload, Date dataTime) {
        IotDataReceiveFailItem failItem = new IotDataReceiveFailItem();
        if (item != null) {
            failItem.setProjectId(item.getProjectId());
            failItem.setProjectName(item.getProjectName());
            failItem.setDeviceId(item.getDeviceId());
            failItem.setDeviceName(item.getDeviceName());
            failItem.setMetric(item.getMetric());
            failItem.setMetricName(item.getMetricName());
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
        log.setExternalProjectId(failItem.getProjectId());
        log.setExternalDeviceId(failItem.getDeviceId());
        log.setExternalDeviceName(failItem.getDeviceName());
        log.setExternalMetric(failItem.getMetric());
        log.setExternalMetricName(failItem.getMetricName());
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

    private Double defaultDouble(Double value, Double defaultValue) {
        return value == null ? defaultValue : value;
    }

    private void throwAuth(String message) {
        throw new BaseException(401, message);
    }

    private void throwParam(String message) {
        throw new BaseException(StatusCode.C.getCode(), message);
    }

    private static class IngestContext {
        private String projectId;
        private IotDevice device;
        private IotDeviceExternalRef deviceRef;
        private IotDevicePoint point;
        private IotPointExternalRef pointRef;
        private String reason;
    }
}
