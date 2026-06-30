package cn.sl.ehub.service.service;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.service.dto.iot.IotDeviceExternalRefSaveReq;
import cn.sl.ehub.service.dto.iot.IotDevicePointSaveReq;
import cn.sl.ehub.service.dto.iot.IotDeviceQuery;
import cn.sl.ehub.service.dto.iot.IotDeviceSaveReq;
import cn.sl.ehub.service.dto.iot.IotPointExternalRefSaveReq;
import cn.sl.ehub.service.dto.iot.IotTelemetryMinuteQuery;
import cn.sl.ehub.service.mapper.IotDeviceExternalRefMapper;
import cn.sl.ehub.service.mapper.IotDeviceMapper;
import cn.sl.ehub.service.mapper.IotDevicePointMapper;
import cn.sl.ehub.service.mapper.IotPointExternalRefMapper;
import cn.sl.ehub.service.mapper.IotTelemetryMinuteMapper;
import cn.sl.ehub.service.mapper.IotUnmatchedTelemetryLogMapper;
import cn.sl.ehub.service.vo.IotDevice;
import cn.sl.ehub.service.vo.IotDeviceExternalRef;
import cn.sl.ehub.service.vo.IotDevicePoint;
import cn.sl.ehub.service.vo.IotPointExternalRef;
import cn.sl.ehub.service.vo.IotTelemetryMinute;
import cn.sl.ehub.service.vo.IotUnmatchedTelemetryLog;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class IotDeviceService {

    private static final String DEFAULT_POINT_CODE = "active_power";
    private static final String DEFAULT_POINT_NAME = "有功功率";
    private static final String DEFAULT_VALUE_TYPE = "double";
    private static final String DEFAULT_UNIT = "kW";
    private static final String READ_ONLY = "readOnly";

    private final IotDeviceMapper iotDeviceMapper;
    private final IotDevicePointMapper iotDevicePointMapper;
    private final IotDeviceExternalRefMapper iotDeviceExternalRefMapper;
    private final IotPointExternalRefMapper iotPointExternalRefMapper;
    private final IotTelemetryMinuteMapper iotTelemetryMinuteMapper;
    private final IotUnmatchedTelemetryLogMapper iotUnmatchedTelemetryLogMapper;

    public IotDeviceService(IotDeviceMapper iotDeviceMapper,
                            IotDevicePointMapper iotDevicePointMapper,
                            IotDeviceExternalRefMapper iotDeviceExternalRefMapper,
                            IotPointExternalRefMapper iotPointExternalRefMapper,
                            IotTelemetryMinuteMapper iotTelemetryMinuteMapper,
                            IotUnmatchedTelemetryLogMapper iotUnmatchedTelemetryLogMapper) {
        this.iotDeviceMapper = iotDeviceMapper;
        this.iotDevicePointMapper = iotDevicePointMapper;
        this.iotDeviceExternalRefMapper = iotDeviceExternalRefMapper;
        this.iotPointExternalRefMapper = iotPointExternalRefMapper;
        this.iotTelemetryMinuteMapper = iotTelemetryMinuteMapper;
        this.iotUnmatchedTelemetryLogMapper = iotUnmatchedTelemetryLogMapper;
    }

    public List<IotDevice> listDevices(IotDeviceQuery query) {
        Example example = new Example(IotDevice.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("deleted", 0);
        if (query != null) {
            if (StringUtils.isNotBlank(query.getAggregatorId())) {
                criteria.andEqualTo("aggregatorId", query.getAggregatorId());
            }
            if (StringUtils.isNotBlank(query.getEntId())) {
                criteria.andEqualTo("entId", query.getEntId());
            }
            if (StringUtils.isNotBlank(query.getProjectId())) {
                criteria.andEqualTo("projectId", StringUtils.trim(query.getProjectId()));
            }
            if (StringUtils.isNotBlank(query.getDeviceCode())) {
                criteria.andLike("deviceCode", "%" + StringUtils.trim(query.getDeviceCode()) + "%");
            }
            if (StringUtils.isNotBlank(query.getDeviceName())) {
                criteria.andLike("deviceName", "%" + StringUtils.trim(query.getDeviceName()) + "%");
            }
            if (StringUtils.isNotBlank(query.getDeviceTypeCode())) {
                criteria.andEqualTo("deviceTypeCode", query.getDeviceTypeCode());
            }
            if (query.getAssetStatus() != null) {
                criteria.andEqualTo("assetStatus", query.getAssetStatus());
            }
            if (query.getOnlineStatus() != null) {
                criteria.andEqualTo("onlineStatus", query.getOnlineStatus());
            }
        }
        example.orderBy("createTime").desc();
        return iotDeviceMapper.selectByExample(example);
    }

    public IotDevice getDevice(Long id) {
        if (id == null) {
            return null;
        }
        IotDevice device = iotDeviceMapper.selectByPrimaryKey(id);
        if (device == null || Integer.valueOf(1).equals(device.getDeleted())) {
            return null;
        }
        return device;
    }

    @Transactional(rollbackFor = Exception.class)
    public IotDevice createDevice(IotDeviceSaveReq req) {
        validateDeviceReq(req);
        IotDevice device = new IotDevice();
        device.setAggregatorId(StringUtils.trimToNull(req.getAggregatorId()));
        device.setEntId(StringUtils.trim(req.getEntId()));
        device.setProjectId(StringUtils.trimToNull(req.getProjectId()));
        device.setDeviceTypeCode(StringUtils.trimToNull(req.getDeviceTypeCode()));
        device.setDeviceTypeName(StringUtils.trimToNull(req.getDeviceTypeName()));
        device.setDeviceCode(StringUtils.trimToNull(req.getDeviceCode()));
        if (StringUtils.isBlank(device.getDeviceCode())) {
            device.setDeviceCode(generateDeviceCode(device.getEntId(), device.getDeviceTypeCode()));
        }
        if (existsDeviceCode(device.getEntId(), device.getDeviceCode(), null)) {
            throwParam("设备编码在当前企业下已存在");
        }
        device.setDeviceName(StringUtils.trim(req.getDeviceName()));
        device.setManufacturer(StringUtils.trimToNull(req.getManufacturer()));
        device.setModel(StringUtils.trimToNull(req.getModel()));
        device.setAssetStatus(req.getAssetStatus() == null ? 1 : req.getAssetStatus());
        device.setOnlineStatus(req.getOnlineStatus() == null ? 0 : req.getOnlineStatus());
        device.setDeleted(0);
        device.setRemark(StringUtils.trimToNull(req.getRemark()));
        Date now = new Date();
        device.setCreateTime(now);
        device.setUpdateTime(now);
        iotDeviceMapper.insertSelective(device);
        if (device.getId() == null) {
            device = getDeviceByEntAndCode(device.getEntId(), device.getDeviceCode());
        }
        if (device != null && !Boolean.FALSE.equals(req.getCreateDefaultPowerPoint())) {
            createDefaultPowerPoint(device.getId());
        }
        return device;
    }

    @Transactional(rollbackFor = Exception.class)
    public IotDevice updateDevice(Long id, IotDeviceSaveReq req) {
        IotDevice device = requireDevice(id);
        if (req == null) {
            throwParam("请求参数不能为空");
        }
        String newEntId = StringUtils.defaultIfBlank(req.getEntId(), device.getEntId());
        String newDeviceCode = StringUtils.defaultIfBlank(req.getDeviceCode(), device.getDeviceCode());
        if (!StringUtils.equals(device.getEntId(), newEntId)
                || !StringUtils.equals(device.getDeviceCode(), newDeviceCode)) {
            if (existsDeviceCode(newEntId, newDeviceCode, id)) {
                throwParam("设备编码在当前企业下已存在");
            }
        }
        device.setAggregatorId(StringUtils.defaultIfBlank(req.getAggregatorId(), device.getAggregatorId()));
        device.setEntId(newEntId);
        device.setProjectId(StringUtils.isNotBlank(req.getProjectId()) ? StringUtils.trim(req.getProjectId()) : device.getProjectId());
        device.setDeviceCode(newDeviceCode);
        device.setDeviceName(StringUtils.defaultIfBlank(req.getDeviceName(), device.getDeviceName()));
        device.setDeviceTypeCode(StringUtils.defaultIfBlank(req.getDeviceTypeCode(), device.getDeviceTypeCode()));
        device.setDeviceTypeName(StringUtils.defaultIfBlank(req.getDeviceTypeName(), device.getDeviceTypeName()));
        device.setManufacturer(req.getManufacturer() == null ? device.getManufacturer() : StringUtils.trimToNull(req.getManufacturer()));
        device.setModel(req.getModel() == null ? device.getModel() : StringUtils.trimToNull(req.getModel()));
        device.setAssetStatus(req.getAssetStatus() == null ? device.getAssetStatus() : req.getAssetStatus());
        device.setOnlineStatus(req.getOnlineStatus() == null ? device.getOnlineStatus() : req.getOnlineStatus());
        device.setRemark(req.getRemark() == null ? device.getRemark() : StringUtils.trimToNull(req.getRemark()));
        device.setUpdateTime(new Date());
        iotDeviceMapper.updateByPrimaryKeySelective(device);
        return iotDeviceMapper.selectByPrimaryKey(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDevice(Long id) {
        IotDevice device = requireDevice(id);
        device.setDeleted(1);
        device.setUpdateTime(new Date());
        iotDeviceMapper.updateByPrimaryKeySelective(device);
    }

    public List<IotDevicePoint> listPoints(Long deviceId) {
        Example example = new Example(IotDevicePoint.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("deviceId", deviceId);
        criteria.andEqualTo("deleted", 0);
        example.orderBy("sort").asc();
        return iotDevicePointMapper.selectByExample(example);
    }

    public IotDevicePoint getPoint(Long id) {
        if (id == null) {
            return null;
        }
        IotDevicePoint point = iotDevicePointMapper.selectByPrimaryKey(id);
        if (point == null || Integer.valueOf(1).equals(point.getDeleted())) {
            return null;
        }
        return point;
    }

    @Transactional(rollbackFor = Exception.class)
    public IotDevicePoint createPoint(Long deviceId, IotDevicePointSaveReq req) {
        requireDevice(deviceId);
        if (req == null) {
            throwParam("请求参数不能为空");
        }
        req.setDeviceId(deviceId);
        validatePointReq(req);
        if (existsPointCode(deviceId, req.getPointCode(), null)) {
            throwParam("测点编码在当前设备下已存在");
        }
        IotDevicePoint point = buildPoint(req);
        Date now = new Date();
        point.setCreateTime(now);
        point.setUpdateTime(now);
        iotDevicePointMapper.insertSelective(point);
        return point;
    }

    @Transactional(rollbackFor = Exception.class)
    public IotDevicePoint updatePoint(Long id, IotDevicePointSaveReq req) {
        IotDevicePoint point = requirePoint(id);
        if (req == null) {
            throwParam("请求参数不能为空");
        }
        String newPointCode = StringUtils.defaultIfBlank(req.getPointCode(), point.getPointCode());
        if (!StringUtils.equals(point.getPointCode(), newPointCode)
                && existsPointCode(point.getDeviceId(), newPointCode, id)) {
            throwParam("测点编码在当前设备下已存在");
        }
        point.setPointCode(newPointCode);
        point.setPointName(StringUtils.defaultIfBlank(req.getPointName(), point.getPointName()));
        point.setValueType(StringUtils.defaultIfBlank(req.getValueType(), point.getValueType()));
        point.setUnit(req.getUnit() == null ? point.getUnit() : StringUtils.trimToNull(req.getUnit()));
        point.setDataFrequency(req.getDataFrequency() == null ? point.getDataFrequency() : req.getDataFrequency());
        point.setRequiredFlag(req.getRequiredFlag() == null ? point.getRequiredFlag() : req.getRequiredFlag());
        point.setReadWriteRole(StringUtils.defaultIfBlank(req.getReadWriteRole(), point.getReadWriteRole()));
        point.setStatus(req.getStatus() == null ? point.getStatus() : req.getStatus());
        point.setSort(req.getSort() == null ? point.getSort() : req.getSort());
        point.setRemark(req.getRemark() == null ? point.getRemark() : StringUtils.trimToNull(req.getRemark()));
        point.setUpdateTime(new Date());
        iotDevicePointMapper.updateByPrimaryKeySelective(point);
        return iotDevicePointMapper.selectByPrimaryKey(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePoint(Long id) {
        IotDevicePoint point = requirePoint(id);
        point.setDeleted(1);
        point.setUpdateTime(new Date());
        iotDevicePointMapper.updateByPrimaryKeySelective(point);
    }

    public List<IotDeviceExternalRef> listDeviceExternalRefs(Long deviceId) {
        Example example = new Example(IotDeviceExternalRef.class);
        example.createCriteria().andEqualTo("deviceId", deviceId);
        example.orderBy("createTime").desc();
        return iotDeviceExternalRefMapper.selectByExample(example);
    }

    public IotDeviceExternalRef getDeviceExternalRef(Long id) {
        if (id == null) {
            return null;
        }
        return iotDeviceExternalRefMapper.selectByPrimaryKey(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public IotDeviceExternalRef createDeviceExternalRef(Long deviceId, IotDeviceExternalRefSaveReq req) {
        IotDevice device = requireDevice(deviceId);
        if (req == null) {
            throwParam("请求参数不能为空");
        }
        req.setDeviceId(deviceId);
        if (StringUtils.isBlank(req.getEntId())) {
            req.setEntId(device.getEntId());
        }
        if (StringUtils.isBlank(req.getProjectId())) {
            req.setProjectId(device.getProjectId());
        }
        validateDeviceExternalRefReq(req);
        if (existsDeviceExternalRef(req.getSourceCode(), req.getEntId(), req.getExternalDeviceId(), null)) {
            throwParam("三方设备标识已绑定");
        }
        IotDeviceExternalRef ref = buildDeviceExternalRef(req);
        Date now = new Date();
        ref.setCreateTime(now);
        ref.setUpdateTime(now);
        iotDeviceExternalRefMapper.insertSelective(ref);
        return ref;
    }

    @Transactional(rollbackFor = Exception.class)
    public IotDeviceExternalRef updateDeviceExternalRef(Long id, IotDeviceExternalRefSaveReq req) {
        IotDeviceExternalRef ref = requireDeviceExternalRef(id);
        if (req == null) {
            throwParam("请求参数不能为空");
        }
        String sourceCode = StringUtils.defaultIfBlank(req.getSourceCode(), ref.getSourceCode());
        String entId = StringUtils.defaultIfBlank(req.getEntId(), ref.getEntId());
        String externalDeviceId = StringUtils.defaultIfBlank(req.getExternalDeviceId(), ref.getExternalDeviceId());
        if ((!StringUtils.equals(sourceCode, ref.getSourceCode())
                || !StringUtils.equals(entId, ref.getEntId())
                || !StringUtils.equals(externalDeviceId, ref.getExternalDeviceId()))
                && existsDeviceExternalRef(sourceCode, entId, externalDeviceId, id)) {
            throwParam("三方设备标识已绑定");
        }
        ref.setSourceCode(sourceCode);
        ref.setEntId(entId);
        ref.setProjectId(StringUtils.isNotBlank(req.getProjectId()) ? req.getProjectId() : ref.getProjectId());
        ref.setExternalDeviceId(externalDeviceId);
        ref.setExternalDeviceCode(req.getExternalDeviceCode() == null ? ref.getExternalDeviceCode() : StringUtils.trimToNull(req.getExternalDeviceCode()));
        ref.setExternalDeviceName(req.getExternalDeviceName() == null ? ref.getExternalDeviceName() : StringUtils.trimToNull(req.getExternalDeviceName()));
        ref.setGatewayCode(req.getGatewayCode() == null ? ref.getGatewayCode() : StringUtils.trimToNull(req.getGatewayCode()));
        ref.setStatus(req.getStatus() == null ? ref.getStatus() : req.getStatus());
        ref.setUpdateTime(new Date());
        iotDeviceExternalRefMapper.updateByPrimaryKeySelective(ref);
        return iotDeviceExternalRefMapper.selectByPrimaryKey(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void disableDeviceExternalRef(Long id) {
        IotDeviceExternalRef ref = requireDeviceExternalRef(id);
        ref.setStatus(0);
        ref.setUpdateTime(new Date());
        iotDeviceExternalRefMapper.updateByPrimaryKeySelective(ref);
    }

    public List<IotPointExternalRef> listPointExternalRefs(Long pointId) {
        Example example = new Example(IotPointExternalRef.class);
        example.createCriteria().andEqualTo("pointId", pointId);
        example.orderBy("createTime").desc();
        return iotPointExternalRefMapper.selectByExample(example);
    }

    public IotPointExternalRef getPointExternalRef(Long id) {
        if (id == null) {
            return null;
        }
        return iotPointExternalRefMapper.selectByPrimaryKey(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public IotPointExternalRef createPointExternalRef(Long pointId, IotPointExternalRefSaveReq req) {
        IotDevicePoint point = requirePoint(pointId);
        if (req == null) {
            throwParam("请求参数不能为空");
        }
        req.setPointId(pointId);
        req.setDeviceId(point.getDeviceId());
        validatePointExternalRefReq(req);
        if (existsPointExternalRef(req.getSourceCode(), req.getDeviceId(), req.getExternalMetric(), null)) {
            throwParam("三方测点标识已绑定");
        }
        IotPointExternalRef ref = buildPointExternalRef(req);
        Date now = new Date();
        ref.setCreateTime(now);
        ref.setUpdateTime(now);
        iotPointExternalRefMapper.insertSelective(ref);
        return ref;
    }

    @Transactional(rollbackFor = Exception.class)
    public IotPointExternalRef updatePointExternalRef(Long id, IotPointExternalRefSaveReq req) {
        IotPointExternalRef ref = requirePointExternalRef(id);
        if (req == null) {
            throwParam("请求参数不能为空");
        }
        String sourceCode = StringUtils.defaultIfBlank(req.getSourceCode(), ref.getSourceCode());
        String externalMetric = StringUtils.defaultIfBlank(req.getExternalMetric(), ref.getExternalMetric());
        Long deviceId = req.getDeviceId() == null ? ref.getDeviceId() : req.getDeviceId();
        if ((!StringUtils.equals(sourceCode, ref.getSourceCode())
                || !StringUtils.equals(externalMetric, ref.getExternalMetric())
                || !deviceId.equals(ref.getDeviceId()))
                && existsPointExternalRef(sourceCode, deviceId, externalMetric, id)) {
            throwParam("三方测点标识已绑定");
        }
        ref.setSourceCode(sourceCode);
        ref.setDeviceId(deviceId);
        ref.setExternalMetric(externalMetric);
        ref.setExternalMetricName(req.getExternalMetricName() == null ? ref.getExternalMetricName() : StringUtils.trimToNull(req.getExternalMetricName()));
        ref.setRatio(req.getRatio() == null ? ref.getRatio() : req.getRatio());
        ref.setOffsetValue(req.getOffsetValue() == null ? ref.getOffsetValue() : req.getOffsetValue());
        ref.setStatus(req.getStatus() == null ? ref.getStatus() : req.getStatus());
        ref.setUpdateTime(new Date());
        iotPointExternalRefMapper.updateByPrimaryKeySelective(ref);
        return iotPointExternalRefMapper.selectByPrimaryKey(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void disablePointExternalRef(Long id) {
        IotPointExternalRef ref = requirePointExternalRef(id);
        ref.setStatus(0);
        ref.setUpdateTime(new Date());
        iotPointExternalRefMapper.updateByPrimaryKeySelective(ref);
    }

    public List<IotTelemetryMinute> listTelemetryMinute(IotTelemetryMinuteQuery query) {
        Example example = new Example(IotTelemetryMinute.class);
        Example.Criteria criteria = example.createCriteria();
        if (query != null) {
            if (StringUtils.isNotBlank(query.getAggregatorId())) {
                criteria.andEqualTo("aggregatorId", query.getAggregatorId());
            }
            if (StringUtils.isNotBlank(query.getEntId())) {
                criteria.andEqualTo("entId", query.getEntId());
            }
            if (StringUtils.isNotBlank(query.getProjectId())) {
                criteria.andEqualTo("projectId", StringUtils.trim(query.getProjectId()));
            }
            if (query.getDeviceId() != null) {
                criteria.andEqualTo("deviceId", query.getDeviceId());
            }
            if (StringUtils.isNotBlank(query.getDeviceCode())) {
                criteria.andEqualTo("deviceCode", query.getDeviceCode());
            }
            if (StringUtils.isNotBlank(query.getPointCode())) {
                criteria.andEqualTo("pointCode", query.getPointCode());
            }
            Date start = parseDateTime(query.getStartTime());
            Date end = parseDateTime(query.getEndTime());
            if (start != null && end != null) {
                criteria.andBetween("minuteTime", start, end);
            } else if (start != null) {
                criteria.andGreaterThanOrEqualTo("minuteTime", start);
            } else if (end != null) {
                criteria.andLessThanOrEqualTo("minuteTime", end);
            }
        }
        example.orderBy("minuteTime").asc();
        return iotTelemetryMinuteMapper.selectByExample(example);
    }

    public List<IotUnmatchedTelemetryLog> listUnmatchedTelemetry(String sourceCode, Integer handled) {
        Example example = new Example(IotUnmatchedTelemetryLog.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(sourceCode)) {
            criteria.andEqualTo("sourceCode", sourceCode);
        }
        if (handled != null) {
            criteria.andEqualTo("handled", handled);
        }
        example.orderBy("createTime").desc();
        return iotUnmatchedTelemetryLogMapper.selectByExample(example);
    }

    private void createDefaultPowerPoint(Long deviceId) {
        if (existsPointCode(deviceId, DEFAULT_POINT_CODE, null)) {
            return;
        }
        IotDevicePointSaveReq req = new IotDevicePointSaveReq();
        req.setDeviceId(deviceId);
        req.setPointCode(DEFAULT_POINT_CODE);
        req.setPointName(DEFAULT_POINT_NAME);
        req.setValueType(DEFAULT_VALUE_TYPE);
        req.setUnit(DEFAULT_UNIT);
        req.setDataFrequency(60);
        req.setRequiredFlag(1);
        req.setReadWriteRole(READ_ONLY);
        req.setStatus(1);
        req.setSort(1);
        IotDevicePoint point = buildPoint(req);
        Date now = new Date();
        point.setCreateTime(now);
        point.setUpdateTime(now);
        iotDevicePointMapper.insertSelective(point);
    }

    private IotDevicePoint buildPoint(IotDevicePointSaveReq req) {
        IotDevicePoint point = new IotDevicePoint();
        point.setDeviceId(req.getDeviceId());
        point.setPointCode(StringUtils.trim(req.getPointCode()));
        point.setPointName(StringUtils.trim(req.getPointName()));
        point.setValueType(StringUtils.defaultIfBlank(req.getValueType(), DEFAULT_VALUE_TYPE));
        point.setUnit(StringUtils.trimToNull(req.getUnit()));
        point.setDataFrequency(req.getDataFrequency());
        point.setRequiredFlag(req.getRequiredFlag() == null ? 0 : req.getRequiredFlag());
        point.setReadWriteRole(StringUtils.defaultIfBlank(req.getReadWriteRole(), READ_ONLY));
        point.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        point.setDeleted(0);
        point.setSort(req.getSort() == null ? 0 : req.getSort());
        point.setRemark(StringUtils.trimToNull(req.getRemark()));
        return point;
    }

    private IotDeviceExternalRef buildDeviceExternalRef(IotDeviceExternalRefSaveReq req) {
        IotDeviceExternalRef ref = new IotDeviceExternalRef();
        ref.setSourceCode(StringUtils.trim(req.getSourceCode()));
        ref.setEntId(StringUtils.trim(req.getEntId()));
        ref.setProjectId(StringUtils.trimToNull(req.getProjectId()));
        ref.setDeviceId(req.getDeviceId());
        ref.setExternalDeviceId(StringUtils.trim(req.getExternalDeviceId()));
        ref.setExternalDeviceCode(StringUtils.trimToNull(req.getExternalDeviceCode()));
        ref.setExternalDeviceName(StringUtils.trimToNull(req.getExternalDeviceName()));
        ref.setGatewayCode(StringUtils.trimToNull(req.getGatewayCode()));
        ref.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        return ref;
    }

    private IotPointExternalRef buildPointExternalRef(IotPointExternalRefSaveReq req) {
        IotPointExternalRef ref = new IotPointExternalRef();
        ref.setSourceCode(StringUtils.trim(req.getSourceCode()));
        ref.setDeviceId(req.getDeviceId());
        ref.setPointId(req.getPointId());
        ref.setExternalMetric(StringUtils.trim(req.getExternalMetric()));
        ref.setExternalMetricName(StringUtils.trimToNull(req.getExternalMetricName()));
        ref.setRatio(req.getRatio() == null ? 1D : req.getRatio());
        ref.setOffsetValue(req.getOffsetValue() == null ? 0D : req.getOffsetValue());
        ref.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        return ref;
    }

    private String generateDeviceCode(String entId, String deviceTypeCode) {
        String prefix = normalizeDevicePrefix(deviceTypeCode);
        int index = 1;
        String code;
        do {
            code = prefix + String.format(Locale.ROOT, "%03d", index++);
        } while (existsDeviceCode(entId, code, null));
        return code;
    }

    private String normalizeDevicePrefix(String deviceTypeCode) {
        String prefix = StringUtils.defaultIfBlank(deviceTypeCode, "DEV").toUpperCase(Locale.ROOT);
        prefix = prefix.replaceAll("[^A-Z0-9]", "");
        if (StringUtils.isBlank(prefix)) {
            prefix = "DEV";
        }
        return prefix;
    }

    private boolean existsDeviceCode(String entId, String deviceCode, Long excludeId) {
        Example example = new Example(IotDevice.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("entId", entId);
        criteria.andEqualTo("deviceCode", deviceCode);
        criteria.andEqualTo("deleted", 0);
        List<IotDevice> list = iotDeviceMapper.selectByExample(example);
        for (IotDevice device : list) {
            if (excludeId == null || !excludeId.equals(device.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean existsPointCode(Long deviceId, String pointCode, Long excludeId) {
        Example example = new Example(IotDevicePoint.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("deviceId", deviceId);
        criteria.andEqualTo("pointCode", pointCode);
        criteria.andEqualTo("deleted", 0);
        List<IotDevicePoint> list = iotDevicePointMapper.selectByExample(example);
        for (IotDevicePoint point : list) {
            if (excludeId == null || !excludeId.equals(point.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean existsDeviceExternalRef(String sourceCode, String entId, String externalDeviceId, Long excludeId) {
        Example example = new Example(IotDeviceExternalRef.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("sourceCode", sourceCode);
        criteria.andEqualTo("entId", entId);
        criteria.andEqualTo("externalDeviceId", externalDeviceId);
        List<IotDeviceExternalRef> list = iotDeviceExternalRefMapper.selectByExample(example);
        for (IotDeviceExternalRef ref : list) {
            if (excludeId == null || !excludeId.equals(ref.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean existsPointExternalRef(String sourceCode, Long deviceId, String externalMetric, Long excludeId) {
        Example example = new Example(IotPointExternalRef.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("sourceCode", sourceCode);
        criteria.andEqualTo("deviceId", deviceId);
        criteria.andEqualTo("externalMetric", externalMetric);
        List<IotPointExternalRef> list = iotPointExternalRefMapper.selectByExample(example);
        for (IotPointExternalRef ref : list) {
            if (excludeId == null || !excludeId.equals(ref.getId())) {
                return true;
            }
        }
        return false;
    }

    private IotDevice getDeviceByEntAndCode(String entId, String deviceCode) {
        Example example = new Example(IotDevice.class);
        example.createCriteria()
                .andEqualTo("entId", entId)
                .andEqualTo("deviceCode", deviceCode)
                .andEqualTo("deleted", 0);
        List<IotDevice> list = iotDeviceMapper.selectByExample(example);
        return list.isEmpty() ? null : list.get(0);
    }

    private IotDevice requireDevice(Long id) {
        IotDevice device = getDevice(id);
        if (device == null) {
            throwParam("设备不存在");
        }
        return device;
    }

    private IotDevicePoint requirePoint(Long id) {
        IotDevicePoint point = iotDevicePointMapper.selectByPrimaryKey(id);
        if (point == null || Integer.valueOf(1).equals(point.getDeleted())) {
            throwParam("测点不存在");
        }
        return point;
    }

    private IotDeviceExternalRef requireDeviceExternalRef(Long id) {
        IotDeviceExternalRef ref = iotDeviceExternalRefMapper.selectByPrimaryKey(id);
        if (ref == null) {
            throwParam("三方设备绑定不存在");
        }
        return ref;
    }

    private IotPointExternalRef requirePointExternalRef(Long id) {
        IotPointExternalRef ref = iotPointExternalRefMapper.selectByPrimaryKey(id);
        if (ref == null) {
            throwParam("三方测点绑定不存在");
        }
        return ref;
    }

    private void validateDeviceReq(IotDeviceSaveReq req) {
        if (req == null) {
            throwParam("请求参数不能为空");
        }
        requireNotBlank(req.getEntId(), "企业ID不能为空");
        requireNotBlank(req.getDeviceName(), "设备名称不能为空");
    }

    private void validatePointReq(IotDevicePointSaveReq req) {
        requireNotBlank(req.getPointCode(), "测点编码不能为空");
        requireNotBlank(req.getPointName(), "测点名称不能为空");
    }

    private void validateDeviceExternalRefReq(IotDeviceExternalRefSaveReq req) {
        requireNotBlank(req.getSourceCode(), "三方来源编码不能为空");
        requireNotBlank(req.getEntId(), "企业ID不能为空");
        requireNotBlank(req.getExternalDeviceId(), "三方设备唯一标识不能为空");
        if (req.getDeviceId() == null) {
            throwParam("设备ID不能为空");
        }
    }

    private void validatePointExternalRefReq(IotPointExternalRefSaveReq req) {
        requireNotBlank(req.getSourceCode(), "三方来源编码不能为空");
        requireNotBlank(req.getExternalMetric(), "三方测点编码不能为空");
        if (req.getDeviceId() == null) {
            throwParam("设备ID不能为空");
        }
        if (req.getPointId() == null) {
            throwParam("测点ID不能为空");
        }
    }

    private void requireNotBlank(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throwParam(message);
        }
    }

    private void throwParam(String message) {
        throw new BaseException(StatusCode.C.getCode(), message);
    }

    private Date parseDateTime(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
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
        throwParam("时间格式错误：" + value);
        return null;
    }
}
