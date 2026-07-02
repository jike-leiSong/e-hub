package cn.sl.ehub.service.service;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.service.dto.iot.IotDevicePointSaveReq;
import cn.sl.ehub.service.dto.iot.IotDeviceQuery;
import cn.sl.ehub.service.dto.iot.IotDeviceSaveReq;
import cn.sl.ehub.service.dto.iot.IotTelemetryMinuteQuery;
import cn.sl.ehub.service.mapper.IotDeviceMapper;
import cn.sl.ehub.service.mapper.IotDevicePointMapper;
import cn.sl.ehub.service.mapper.IotTelemetryMinuteMapper;
import cn.sl.ehub.service.mapper.IotUnmatchedTelemetryLogMapper;
import cn.sl.ehub.service.vo.IotDevice;
import cn.sl.ehub.service.vo.IotDevicePoint;
import cn.sl.ehub.service.vo.IotTelemetryMinute;
import cn.sl.ehub.service.vo.IotUnmatchedTelemetryLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class IotDeviceService {

    private static final String DEFAULT_POINT_CODE = "active_power";
    private static final String DEFAULT_POINT_NAME = "有功功率";
    private static final String DEFAULT_VALUE_TYPE = "double";
    private static final String DEFAULT_UNIT = "kW";
    private static final String READ_ONLY = "readOnly";

    private final IotDeviceMapper iotDeviceMapper;
    private final IotDevicePointMapper iotDevicePointMapper;
    private final IotTelemetryMinuteMapper iotTelemetryMinuteMapper;
    private final IotUnmatchedTelemetryLogMapper iotUnmatchedTelemetryLogMapper;

    public IotDeviceService(IotDeviceMapper iotDeviceMapper,
                            IotDevicePointMapper iotDevicePointMapper,
                            IotTelemetryMinuteMapper iotTelemetryMinuteMapper,
                            IotUnmatchedTelemetryLogMapper iotUnmatchedTelemetryLogMapper) {
        this.iotDeviceMapper = iotDeviceMapper;
        this.iotDevicePointMapper = iotDevicePointMapper;
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
        device.setThirdPartyApi(StringUtils.trimToNull(req.getThirdPartyApi()));
        device.setThirdPartyCode(StringUtils.trimToNull(req.getThirdPartyCode()));
        validateThirdPartyDeviceUnique(device.getEntId(), device.getThirdPartyApi(), device.getThirdPartyCode(), null);
        device.setDeviceName(StringUtils.trim(req.getDeviceName()));
        device.setManufacturer(StringUtils.trimToNull(req.getManufacturer()));
        device.setModel(StringUtils.trimToNull(req.getModel()));
        device.setStatus(1);
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
        String thirdPartyApi = req.getThirdPartyApi() == null ? device.getThirdPartyApi() : StringUtils.trimToNull(req.getThirdPartyApi());
        String thirdPartyCode = req.getThirdPartyCode() == null ? device.getThirdPartyCode() : StringUtils.trimToNull(req.getThirdPartyCode());
        validateThirdPartyDeviceUnique(newEntId, thirdPartyApi, thirdPartyCode, id);
        device.setThirdPartyApi(thirdPartyApi);
        device.setThirdPartyCode(thirdPartyCode);
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
        if (existsPointCode(deviceId, req.getPropertyCode(), null)) {
            throwParam("测点编码在当前设备下已存在");
        }
        validateThirdPartyPointUnique(deviceId, req.getThirdPartyCode(), null);
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
        String newPointCode = StringUtils.defaultIfBlank(req.getPropertyCode(), point.getPropertyCode());
        if (!StringUtils.equals(point.getPropertyCode(), newPointCode)
                && existsPointCode(point.getDeviceId(), newPointCode, id)) {
            throwParam("测点编码在当前设备下已存在");
        }
        String thirdPartyCode = req.getThirdPartyCode() == null ? point.getThirdPartyCode() : StringUtils.trimToNull(req.getThirdPartyCode());
        validateThirdPartyPointUnique(point.getDeviceId(), thirdPartyCode, id);
        point.setPropertyCode(newPointCode);
        point.setPropertyName(StringUtils.defaultIfBlank(req.getPropertyName(), point.getPropertyName()));
        point.setThirdPartyCode(thirdPartyCode);
        point.setDataType(req.getDataType() == null ? point.getDataType() : StringUtils.trimToNull(req.getDataType()));
        point.setDataTypeName(req.getDataTypeName() == null ? point.getDataTypeName() : StringUtils.trimToNull(req.getDataTypeName()));
        point.setValueType(StringUtils.defaultIfBlank(req.getValueType(), point.getValueType()));
        point.setUnit(req.getUnit() == null ? point.getUnit() : StringUtils.trimToNull(req.getUnit()));
        point.setDataFrequency(req.getDataFrequency() == null ? point.getDataFrequency() : req.getDataFrequency());
        point.setRequiredFlag(req.getRequiredFlag() == null ? point.getRequiredFlag() : req.getRequiredFlag());
        point.setReadWriteRole(StringUtils.defaultIfBlank(req.getReadWriteRole(), point.getReadWriteRole()));
        point.setUpWay(req.getUpWay() == null ? point.getUpWay() : StringUtils.trimToNull(req.getUpWay()));
        point.setUpWayName(req.getUpWayName() == null ? point.getUpWayName() : StringUtils.trimToNull(req.getUpWayName()));
        point.setUpPeriod(req.getUpPeriod() == null ? point.getUpPeriod() : StringUtils.trimToNull(req.getUpPeriod()));
        point.setUpPeriodName(req.getUpPeriodName() == null ? point.getUpPeriodName() : StringUtils.trimToNull(req.getUpPeriodName()));
        point.setValueLowerLimit(req.getValueLowerLimit() == null ? point.getValueLowerLimit() : StringUtils.trimToNull(req.getValueLowerLimit()));
        point.setValueHighLimit(req.getValueHighLimit() == null ? point.getValueHighLimit() : StringUtils.trimToNull(req.getValueHighLimit()));
        point.setDeadZoneType(req.getDeadZoneType() == null ? point.getDeadZoneType() : req.getDeadZoneType());
        point.setType(req.getType() == null ? point.getType() : req.getType());
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
        req.setPropertyCode(DEFAULT_POINT_CODE);
        req.setPropertyName(DEFAULT_POINT_NAME);
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
        point.setPropertyCode(StringUtils.trim(req.getPropertyCode()));
        point.setPropertyName(StringUtils.trim(req.getPropertyName()));
        point.setThirdPartyCode(StringUtils.trimToNull(req.getThirdPartyCode()));
        point.setDataType(StringUtils.trimToNull(req.getDataType()));
        point.setDataTypeName(StringUtils.trimToNull(req.getDataTypeName()));
        point.setValueType(StringUtils.defaultIfBlank(req.getValueType(), DEFAULT_VALUE_TYPE));
        point.setUnit(StringUtils.trimToNull(req.getUnit()));
        point.setDataFrequency(req.getDataFrequency());
        point.setRequiredFlag(req.getRequiredFlag() == null ? 0 : req.getRequiredFlag());
        point.setReadWriteRole(StringUtils.defaultIfBlank(req.getReadWriteRole(), READ_ONLY));
        point.setUpWay(StringUtils.trimToNull(req.getUpWay()));
        point.setUpWayName(StringUtils.trimToNull(req.getUpWayName()));
        point.setUpPeriod(StringUtils.trimToNull(req.getUpPeriod()));
        point.setUpPeriodName(StringUtils.trimToNull(req.getUpPeriodName()));
        point.setValueLowerLimit(StringUtils.trimToNull(req.getValueLowerLimit()));
        point.setValueHighLimit(StringUtils.trimToNull(req.getValueHighLimit()));
        point.setDeadZoneType(req.getDeadZoneType());
        point.setType(req.getType());
        point.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        point.setDeleted(0);
        point.setSort(req.getSort() == null ? 0 : req.getSort());
        point.setRemark(StringUtils.trimToNull(req.getRemark()));
        return point;
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

    private void validateThirdPartyDeviceUnique(String entId, String thirdPartyApi, String thirdPartyCode, Long excludeId) {
        if (StringUtils.isBlank(thirdPartyApi) || StringUtils.isBlank(thirdPartyCode)) {
            return;
        }
        Example example = new Example(IotDevice.class);
        example.createCriteria()
                .andEqualTo("entId", entId)
                .andEqualTo("thirdPartyApi", thirdPartyApi)
                .andEqualTo("thirdPartyCode", thirdPartyCode)
                .andEqualTo("deleted", 0);
        List<IotDevice> list = iotDeviceMapper.selectByExample(example);
        for (IotDevice device : list) {
            if (excludeId == null || !excludeId.equals(device.getId())) {
                throwParam("设备第三方标识在当前企业和第三方API下已存在");
            }
        }
    }

    private boolean existsPointCode(Long deviceId, String pointCode, Long excludeId) {
        Example example = new Example(IotDevicePoint.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("deviceId", deviceId);
        criteria.andEqualTo("propertyCode", pointCode);
        criteria.andEqualTo("deleted", 0);
        List<IotDevicePoint> list = iotDevicePointMapper.selectByExample(example);
        for (IotDevicePoint point : list) {
            if (excludeId == null || !excludeId.equals(point.getId())) {
                return true;
            }
        }
        return false;
    }

    private void validateThirdPartyPointUnique(Long deviceId, String thirdPartyCode, Long excludeId) {
        if (StringUtils.isBlank(thirdPartyCode)) {
            return;
        }
        Example example = new Example(IotDevicePoint.class);
        example.createCriteria()
                .andEqualTo("deviceId", deviceId)
                .andEqualTo("thirdPartyCode", StringUtils.trim(thirdPartyCode))
                .andEqualTo("deleted", 0);
        List<IotDevicePoint> list = iotDevicePointMapper.selectByExample(example);
        for (IotDevicePoint point : list) {
            if (excludeId == null || !excludeId.equals(point.getId())) {
                throwParam("测点第三方标识在当前设备下已存在");
            }
        }
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

    private void validateDeviceReq(IotDeviceSaveReq req) {
        if (req == null) {
            throwParam("请求参数不能为空");
        }
        requireNotBlank(req.getEntId(), "企业ID不能为空");
        requireNotBlank(req.getDeviceName(), "设备名称不能为空");
    }

    private void validatePointReq(IotDevicePointSaveReq req) {
        requireNotBlank(req.getPropertyCode(), "测点编码不能为空");
        requireNotBlank(req.getPropertyName(), "测点名称不能为空");
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

    private String generateDeviceCode(String entId, String deviceTypeCode) {
        String prefix = StringUtils.isNotBlank(deviceTypeCode) ? deviceTypeCode.toUpperCase() : "DEV";
        return prefix + "_" + entId + "_" + System.currentTimeMillis();
    }
}
