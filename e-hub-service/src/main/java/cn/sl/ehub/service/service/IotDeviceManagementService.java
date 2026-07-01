package cn.sl.ehub.service.service;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.service.dto.iot.IotDeviceGroupParamSaveReq;
import cn.sl.ehub.service.dto.iot.IotDeviceGroupPointSaveReq;
import cn.sl.ehub.service.dto.iot.IotDeviceGroupSaveReq;
import cn.sl.ehub.service.dto.iot.IotDeviceParamSaveReq;
import cn.sl.ehub.service.dto.iot.IotDevicePointSaveReq;
import cn.sl.ehub.service.dto.iot.IotDeviceSaveReq;
import cn.sl.ehub.service.dto.iot.IotPointDefinitionSaveReq;
import cn.sl.ehub.service.mapper.IotDeviceExternalRefMapper;
import cn.sl.ehub.service.mapper.IotDeviceGroupMapper;
import cn.sl.ehub.service.mapper.IotDeviceGroupParamMapper;
import cn.sl.ehub.service.mapper.IotDeviceGroupParamMetadataMapper;
import cn.sl.ehub.service.mapper.IotDeviceGroupPointDefinitionMapper;
import cn.sl.ehub.service.mapper.IotDeviceGroupPointMapper;
import cn.sl.ehub.service.mapper.IotDeviceGroupPointMetadataMapper;
import cn.sl.ehub.service.mapper.IotDeviceMapper;
import cn.sl.ehub.service.mapper.IotDeviceParamMapper;
import cn.sl.ehub.service.mapper.IotDevicePointDefinitionMapper;
import cn.sl.ehub.service.mapper.IotDevicePointMapper;
import cn.sl.ehub.service.mapper.IotDeviceTypeParamMetadataMapper;
import cn.sl.ehub.service.mapper.IotDeviceTypePointMetadataMapper;
import cn.sl.ehub.service.mapper.IotGatewayMapper;
import cn.sl.ehub.service.vo.IotDevice;
import cn.sl.ehub.service.vo.IotDeviceExternalRef;
import cn.sl.ehub.service.vo.IotDeviceGroup;
import cn.sl.ehub.service.vo.IotDeviceGroupParam;
import cn.sl.ehub.service.vo.IotDeviceGroupParamMetadata;
import cn.sl.ehub.service.vo.IotDeviceGroupPoint;
import cn.sl.ehub.service.vo.IotDeviceGroupPointDefinition;
import cn.sl.ehub.service.vo.IotDeviceGroupPointMetadata;
import cn.sl.ehub.service.vo.IotDeviceParam;
import cn.sl.ehub.service.vo.IotDevicePoint;
import cn.sl.ehub.service.vo.IotDevicePointDefinition;
import cn.sl.ehub.service.vo.IotDeviceTypeParamMetadata;
import cn.sl.ehub.service.vo.IotDeviceTypePointMetadata;
import cn.sl.ehub.service.vo.IotGateway;
import cn.sl.ehub.service.vo.IotOptionVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class IotDeviceManagementService {

    public static final String DEFAULT_DEVICE_GROUP_TYPE = "DEFAULT_GROUP";
    public static final String DEFAULT_DEVICE_GROUP_TYPE_NAME = "默认设备组";
    public static final String DEFAULT_DEVICE_GROUP_NAME = "默认设备组";
    public static final String DEFAULT_GATEWAY_CODE_PREFIX = "GW";
    public static final String DEFAULT_GATEWAY_NAME = "默认网关";
    public static final String DEFAULT_DEVICE_SOURCE_CODE = "DEFAULT_API";
    public static final String READ_ONLY = "readOnly";

    private static final List<IotOptionVO> DEVICE_TYPES = Collections.unmodifiableList(Arrays.asList(
            new IotOptionVO("ENERGY_STORAGE", "储能设备"),
            new IotOptionVO("AIR_CONDITIONER", "空调设备"),
            new IotOptionVO("CHILLER", "冷机设备"),
            new IotOptionVO("METER", "电表设备")
    ));

    private static final List<IotOptionVO> COMMUNICATION_METHODS = Collections.unmodifiableList(Arrays.asList(
            new IotOptionVO("third_party", "第三方接入"),
            new IotOptionVO("4g_gateway", "4G网关"),
            new IotOptionVO("4g_direct", "4G直连")
    ));

    private static final List<IotOptionVO> DEVICE_GROUP_TYPES = Collections.unmodifiableList(Arrays.asList(
            new IotOptionVO("ENERGY_STORAGE", "储能设备组"),
            new IotOptionVO("AIR_CONDITIONER", "空调设备组"),
            new IotOptionVO("CHILLER", "冷机设备组")
    ));

    private static final List<IotOptionVO> ENERGY_TYPES = Collections.unmodifiableList(Arrays.asList(
            new IotOptionVO("electric", "电"),
            new IotOptionVO("heat", "热"),
            new IotOptionVO("cold", "冷")
    ));

    private static final List<IotOptionVO> THIRD_PARTY_APIS = Collections.unmodifiableList(Arrays.asList(
            new IotOptionVO(DEFAULT_DEVICE_SOURCE_CODE, "默认第三方API")
    ));

    private static final List<IotOptionVO> CARRIERS = Collections.unmodifiableList(Arrays.asList(
            new IotOptionVO("mobile", "中国移动"),
            new IotOptionVO("unicom", "中国联通"),
            new IotOptionVO("telecom", "中国电信")
    ));

    private final IotDeviceGroupMapper iotDeviceGroupMapper;
    private final IotDeviceGroupParamMapper iotDeviceGroupParamMapper;
    private final IotDeviceGroupParamMetadataMapper iotDeviceGroupParamMetadataMapper;
    private final IotDeviceGroupPointMapper iotDeviceGroupPointMapper;
    private final IotDeviceGroupPointDefinitionMapper iotDeviceGroupPointDefinitionMapper;
    private final IotDeviceGroupPointMetadataMapper iotDeviceGroupPointMetadataMapper;
    private final IotGatewayMapper iotGatewayMapper;
    private final IotDeviceMapper iotDeviceMapper;
    private final IotDeviceParamMapper iotDeviceParamMapper;
    private final IotDevicePointMapper iotDevicePointMapper;
    private final IotDevicePointDefinitionMapper iotDevicePointDefinitionMapper;
    private final IotDeviceTypeParamMetadataMapper iotDeviceTypeParamMetadataMapper;
    private final IotDeviceTypePointMetadataMapper iotDeviceTypePointMetadataMapper;
    private final IotDeviceExternalRefMapper iotDeviceExternalRefMapper;

    public IotDeviceManagementService(IotDeviceGroupMapper iotDeviceGroupMapper,
                                      IotDeviceGroupParamMapper iotDeviceGroupParamMapper,
                                      IotDeviceGroupParamMetadataMapper iotDeviceGroupParamMetadataMapper,
                                      IotDeviceGroupPointMapper iotDeviceGroupPointMapper,
                                      IotDeviceGroupPointDefinitionMapper iotDeviceGroupPointDefinitionMapper,
                                      IotDeviceGroupPointMetadataMapper iotDeviceGroupPointMetadataMapper,
                                      IotGatewayMapper iotGatewayMapper,
                                      IotDeviceMapper iotDeviceMapper,
                                      IotDeviceParamMapper iotDeviceParamMapper,
                                      IotDevicePointMapper iotDevicePointMapper,
                                      IotDevicePointDefinitionMapper iotDevicePointDefinitionMapper,
                                      IotDeviceTypeParamMetadataMapper iotDeviceTypeParamMetadataMapper,
                                      IotDeviceTypePointMetadataMapper iotDeviceTypePointMetadataMapper,
                                      IotDeviceExternalRefMapper iotDeviceExternalRefMapper) {
        this.iotDeviceGroupMapper = iotDeviceGroupMapper;
        this.iotDeviceGroupParamMapper = iotDeviceGroupParamMapper;
        this.iotDeviceGroupParamMetadataMapper = iotDeviceGroupParamMetadataMapper;
        this.iotDeviceGroupPointMapper = iotDeviceGroupPointMapper;
        this.iotDeviceGroupPointDefinitionMapper = iotDeviceGroupPointDefinitionMapper;
        this.iotDeviceGroupPointMetadataMapper = iotDeviceGroupPointMetadataMapper;
        this.iotGatewayMapper = iotGatewayMapper;
        this.iotDeviceMapper = iotDeviceMapper;
        this.iotDeviceParamMapper = iotDeviceParamMapper;
        this.iotDevicePointMapper = iotDevicePointMapper;
        this.iotDevicePointDefinitionMapper = iotDevicePointDefinitionMapper;
        this.iotDeviceTypeParamMetadataMapper = iotDeviceTypeParamMetadataMapper;
        this.iotDeviceTypePointMetadataMapper = iotDeviceTypePointMetadataMapper;
        this.iotDeviceExternalRefMapper = iotDeviceExternalRefMapper;
    }

    public List<IotOptionVO> listDeviceTypes() {
        return DEVICE_TYPES;
    }

    public List<IotOptionVO> listCommunicationMethods() {
        return COMMUNICATION_METHODS;
    }

    public List<IotOptionVO> listDeviceGroupTypes() {
        return DEVICE_GROUP_TYPES;
    }

    public List<IotOptionVO> listEnergyTypes() {
        return ENERGY_TYPES;
    }

    public List<IotOptionVO> listCarriers() {
        return CARRIERS;
    }

    public List<IotOptionVO> listThirdPartyApis() {
        return THIRD_PARTY_APIS;
    }

    public List<IotGateway> listGateways(String aggregatorId, String entId) {
        ensureDefaultGateway(aggregatorId, entId);
        Example example = new Example(IotGateway.class);
        example.createCriteria()
                .andEqualTo("entId", entId)
                .andEqualTo("deleted", 0)
                .andEqualTo("status", 1);
        example.orderBy("defaultFlag").desc();
        example.orderBy("id").asc();
        return iotGatewayMapper.selectByExample(example);
    }

    public IotGateway getDefaultGateway(String aggregatorId, String entId) {
        ensureDefaultGateway(aggregatorId, entId);
        Example example = new Example(IotGateway.class);
        example.createCriteria()
                .andEqualTo("entId", entId)
                .andEqualTo("deleted", 0)
                .andEqualTo("defaultFlag", 1);
        List<IotGateway> list = iotGatewayMapper.selectByExample(example);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<IotDeviceGroup> listDeviceGroups(String aggregatorId, String entId) {
        IotDeviceGroup defaultGroup = ensureDefaultDeviceGroup(aggregatorId, entId);
        Example example = new Example(IotDeviceGroup.class);
        example.createCriteria()
                .andEqualTo("entId", entId)
                .andEqualTo("deleted", 0);
        example.orderBy("virtualFlag").desc();
        example.orderBy("id").asc();
        List<IotDeviceGroup> groups = iotDeviceGroupMapper.selectByExample(example);
        fillDeviceGroups(groups);
        if (groups.stream().noneMatch(item -> Objects.equals(item.getId(), defaultGroup.getId()))) {
            groups.add(0, defaultGroup);
        }
        return groups;
    }

    public IotDeviceGroup getDeviceGroup(Long id) {
        IotDeviceGroup group = iotDeviceGroupMapper.selectByPrimaryKey(id);
        if (group == null || Integer.valueOf(1).equals(group.getDeleted())) {
            return null;
        }
        fillDeviceGroup(group);
        return group;
    }

    @Transactional(rollbackFor = Exception.class)
    public IotDeviceGroup saveDeviceGroup(IotDeviceGroupSaveReq req) {
        validateDeviceGroupReq(req);
        IotGateway gateway = ensureGatewayRequired(req.getAggregatorId(), req.getEntId(), req.getGatewayId());
        IotDeviceGroup group = new IotDeviceGroup();
        group.setTenantId(parseTenantId(req.getEntId()));
        group.setAggregatorId(StringUtils.trimToNull(req.getAggregatorId()));
        group.setEntId(StringUtils.trim(req.getEntId()));
        group.setDeviceGroupCode(generateDeviceGroupCode(group.getEntId(), req.getDeviceGroupType()));
        group.setDeviceGroupName(StringUtils.trim(req.getDeviceGroupName()));
        group.setDeviceGroupType(StringUtils.trimToNull(req.getDeviceGroupType()));
        group.setDeviceGroupTypeName(resolveDeviceGroupTypeName(req.getDeviceGroupType(), req.getDeviceGroupTypeName()));
        group.setEnergyType(StringUtils.trimToNull(req.getEnergyType()));
        group.setGatewayId(gateway.getId());
        group.setVirtualFlag(0);
        group.setStatus(1);
        group.setDeleted(0);
        group.setRemark(StringUtils.trimToNull(req.getRemark()));
        Date now = new Date();
        group.setCreateTime(now);
        group.setUpdateTime(now);
        iotDeviceGroupMapper.insertSelective(group);
        saveDeviceGroupParams(group.getId(), group.getTenantId(), req.getParamList());
        saveDeviceGroupPoints(group.getId(), group.getTenantId(), req.getPointList());
        return getDeviceGroup(group.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public IotDeviceGroup updateDeviceGroup(Long id, IotDeviceGroupSaveReq req) {
        IotDeviceGroup group = requireDeviceGroup(id);
        validateDeviceGroupReq(req);
        IotGateway gateway = ensureGatewayRequired(
                StringUtils.defaultIfBlank(req.getAggregatorId(), group.getAggregatorId()),
                group.getEntId(),
                req.getGatewayId());
        group.setDeviceGroupName(StringUtils.trim(req.getDeviceGroupName()));
        group.setDeviceGroupType(StringUtils.defaultIfBlank(req.getDeviceGroupType(), group.getDeviceGroupType()));
        group.setDeviceGroupTypeName(resolveDeviceGroupTypeName(group.getDeviceGroupType(), req.getDeviceGroupTypeName()));
        group.setEnergyType(req.getEnergyType() == null ? group.getEnergyType() : StringUtils.trimToNull(req.getEnergyType()));
        group.setGatewayId(gateway.getId());
        group.setRemark(req.getRemark() == null ? group.getRemark() : StringUtils.trimToNull(req.getRemark()));
        group.setUpdateTime(new Date());
        iotDeviceGroupMapper.updateByPrimaryKeySelective(group);
        replaceDeviceGroupParams(group.getId(), group.getTenantId(), req.getParamList());
        replaceDeviceGroupPoints(group.getId(), group.getTenantId(), req.getPointList());
        return getDeviceGroup(group.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDeviceGroup(Long id) {
        IotDeviceGroup group = requireDeviceGroup(id);
        if (Integer.valueOf(1).equals(group.getVirtualFlag())) {
            throwParam("默认虚拟设备组不允许删除");
        }
        Example deviceExample = new Example(IotDevice.class);
        deviceExample.createCriteria()
                .andEqualTo("deviceGroupId", id)
                .andEqualTo("deleted", 0);
        if (iotDeviceMapper.selectCountByExample(deviceExample) > 0) {
            throwParam("设备组下存在设备，不允许删除");
        }
        group.setDeleted(1);
        group.setUpdateTime(new Date());
        iotDeviceGroupMapper.updateByPrimaryKeySelective(group);
    }

    public List<IotDevice> listDevices(String aggregatorId, String entId, Long deviceGroupId,
                                       String deviceTypeCode, String deviceName) {
        ensureDefaultDeviceGroup(aggregatorId, entId);
        Example example = new Example(IotDevice.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("entId", entId);
        criteria.andEqualTo("deleted", 0);
        if (deviceGroupId != null) {
            criteria.andEqualTo("deviceGroupId", deviceGroupId);
        }
        if (StringUtils.isNotBlank(deviceTypeCode)) {
            criteria.andEqualTo("deviceTypeCode", StringUtils.trim(deviceTypeCode));
        }
        if (StringUtils.isNotBlank(deviceName)) {
            criteria.andLike("deviceName", "%" + StringUtils.trim(deviceName) + "%");
        }
        example.orderBy("id").desc();
        List<IotDevice> list = iotDeviceMapper.selectByExample(example);
        fillDevices(list);
        return list;
    }

    public IotDevice getManagedDevice(Long id) {
        IotDevice device = iotDeviceMapper.selectByPrimaryKey(id);
        if (device == null || Integer.valueOf(1).equals(device.getDeleted())) {
            return null;
        }
        fillDevice(device);
        return device;
    }

    public IotDevicePoint getDevicePoint(Long id) {
        IotDevicePoint point = iotDevicePointMapper.selectByPrimaryKey(id);
        if (point == null || Integer.valueOf(1).equals(point.getDeleted())) {
            return null;
        }
        return point;
    }

    public IotDeviceGroupPoint getDeviceGroupPoint(Long id) {
        IotDeviceGroupPoint point = iotDeviceGroupPointMapper.selectByPrimaryKey(id);
        if (point == null || Integer.valueOf(1).equals(point.getDeleted())) {
            return null;
        }
        return point;
    }

    public IotDevicePointDefinition getDevicePointDefinition(Long id) {
        return iotDevicePointDefinitionMapper.selectByPrimaryKey(id);
    }

    public IotDeviceGroupPointDefinition getDeviceGroupPointDefinition(Long id) {
        return iotDeviceGroupPointDefinitionMapper.selectByPrimaryKey(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public IotDevice saveManagedDevice(IotDeviceSaveReq req) {
        validateManagedDeviceReq(req);
        IotDeviceGroup group = ensureDeviceGroupRequired(req.getAggregatorId(), req.getEntId(), req.getDeviceGroupId());
        IotGateway gateway = resolveGatewayForDevice(req, group);
        IotDevice device = new IotDevice();
        device.setTenantId(parseTenantId(req.getEntId()));
        device.setAggregatorId(StringUtils.trimToNull(req.getAggregatorId()));
        device.setEntId(StringUtils.trim(req.getEntId()));
        device.setProjectId(StringUtils.trimToNull(req.getProjectId()));
        device.setDeviceGroupId(group.getId());
        device.setGatewayId(gateway.getId());
        device.setDeviceCode(resolveDeviceCode(req.getEntId(), req.getDeviceTypeCode(), req.getDeviceCode(), null));
        device.setDeviceName(StringUtils.trim(req.getDeviceName()));
        device.setDeviceTypeCode(StringUtils.trimToNull(req.getDeviceTypeCode()));
        device.setDeviceTypeName(resolveDeviceTypeName(req.getDeviceTypeCode(), req.getDeviceTypeName()));
        device.setCommunicationMethod(StringUtils.trimToNull(req.getCommunicationMethod()));
        device.setManufacturer(StringUtils.trimToNull(req.getManufacturer()));
        device.setModel(StringUtils.trimToNull(req.getModel()));
        device.setThirdPartyApi(StringUtils.trimToNull(req.getThirdPartyApi()));
        device.setThirdPartyCode(StringUtils.trimToNull(req.getThirdPartyCode()));
        device.setStatus(1);
        device.setAssetStatus(req.getAssetStatus() == null ? 1 : req.getAssetStatus());
        device.setOnlineStatus(req.getOnlineStatus() == null ? 0 : req.getOnlineStatus());
        device.setRemark(StringUtils.trimToNull(req.getRemark()));
        device.setDeleted(0);
        Date now = new Date();
        device.setCreateTime(now);
        device.setUpdateTime(now);
        iotDeviceMapper.insertSelective(device);
        replaceDeviceParams(device.getId(), device.getTenantId(), req.getParamList());
        if (req.getPointList() != null) {
            replaceDevicePoints(device.getId(), device.getTenantId(), req.getPointList(), group.getId());
        } else {
            copyDevicePointsFromGroup(device.getId(), device.getTenantId(), group.getId());
        }
        bindGatewayAndThirdParty(device.getId(), gateway, req);
        return getManagedDevice(device.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public IotDevice updateManagedDevice(Long id, IotDeviceSaveReq req) {
        IotDevice device = requireManagedDevice(id);
        validateManagedDeviceReq(req);
        IotDeviceGroup group = ensureDeviceGroupRequired(
                StringUtils.defaultIfBlank(req.getAggregatorId(), device.getAggregatorId()),
                device.getEntId(),
                req.getDeviceGroupId() == null ? device.getDeviceGroupId() : req.getDeviceGroupId());
        IotGateway gateway = resolveGatewayForDevice(req, group);
        device.setProjectId(req.getProjectId() == null ? device.getProjectId() : StringUtils.trimToNull(req.getProjectId()));
        device.setDeviceGroupId(group.getId());
        device.setGatewayId(gateway.getId());
        device.setDeviceCode(resolveDeviceCode(device.getEntId(),
                StringUtils.defaultIfBlank(req.getDeviceTypeCode(), device.getDeviceTypeCode()),
                StringUtils.defaultIfBlank(req.getDeviceCode(), device.getDeviceCode()), id));
        device.setDeviceName(StringUtils.defaultIfBlank(req.getDeviceName(), device.getDeviceName()));
        device.setDeviceTypeCode(StringUtils.defaultIfBlank(req.getDeviceTypeCode(), device.getDeviceTypeCode()));
        device.setDeviceTypeName(resolveDeviceTypeName(device.getDeviceTypeCode(), req.getDeviceTypeName()));
        device.setCommunicationMethod(req.getCommunicationMethod() == null ? device.getCommunicationMethod() : StringUtils.trimToNull(req.getCommunicationMethod()));
        device.setManufacturer(req.getManufacturer() == null ? device.getManufacturer() : StringUtils.trimToNull(req.getManufacturer()));
        device.setModel(req.getModel() == null ? device.getModel() : StringUtils.trimToNull(req.getModel()));
        device.setThirdPartyApi(req.getThirdPartyApi() == null ? device.getThirdPartyApi() : StringUtils.trimToNull(req.getThirdPartyApi()));
        device.setThirdPartyCode(req.getThirdPartyCode() == null ? device.getThirdPartyCode() : StringUtils.trimToNull(req.getThirdPartyCode()));
        device.setAssetStatus(req.getAssetStatus() == null ? device.getAssetStatus() : req.getAssetStatus());
        device.setOnlineStatus(req.getOnlineStatus() == null ? device.getOnlineStatus() : req.getOnlineStatus());
        device.setRemark(req.getRemark() == null ? device.getRemark() : StringUtils.trimToNull(req.getRemark()));
        device.setUpdateTime(new Date());
        iotDeviceMapper.updateByPrimaryKeySelective(device);
        if (req.getParamList() != null) {
            replaceDeviceParams(device.getId(), device.getTenantId(), req.getParamList());
        }
        bindGatewayAndThirdParty(device.getId(), gateway, req);
        return getManagedDevice(device.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteManagedDevice(Long id) {
        IotDevice device = requireManagedDevice(id);
        device.setDeleted(1);
        device.setUpdateTime(new Date());
        iotDeviceMapper.updateByPrimaryKeySelective(device);
    }

    public List<IotDevicePoint> listDevicePoints(Long deviceId, String pointQuery, String dataType) {
        requireManagedDevice(deviceId);
        Example example = new Example(IotDevicePoint.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("deviceId", deviceId);
        criteria.andEqualTo("deleted", 0);
        if (StringUtils.isNotBlank(pointQuery)) {
            Example.Criteria codeCriteria = example.or();
            codeCriteria.andEqualTo("deviceId", deviceId);
            codeCriteria.andEqualTo("deleted", 0);
            codeCriteria.andLike("propertyCode", "%" + StringUtils.trim(pointQuery) + "%");
            criteria.andLike("propertyName", "%" + StringUtils.trim(pointQuery) + "%");
        }
        if (StringUtils.isNotBlank(dataType)) {
            criteria.andEqualTo("dataType", StringUtils.trim(dataType));
        }
        example.orderBy("sort").asc();
        example.orderBy("id").asc();
        return iotDevicePointMapper.selectByExample(example);
    }

    public List<IotDeviceTypePointMetadata> listAvailablePoints(Long deviceId) {
        IotDevice device = requireManagedDevice(deviceId);
        String deviceTypeCode = device.getDeviceTypeCode();
        if (StringUtils.isBlank(deviceTypeCode)) {
            return Collections.emptyList();
        }
        List<IotDeviceTypePointMetadata> metas = listDevicePointMetadata(deviceTypeCode);
        if (metas.isEmpty()) {
            return Collections.emptyList();
        }
        List<IotDevicePoint> currentPoints = listDevicePoints(deviceId, null, null);
        List<String> existingCodes = currentPoints.stream()
                .map(IotDevicePoint::getPropertyCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        return metas.stream()
                .filter(item -> !existingCodes.contains(item.getPropertyCode()))
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchAddDevicePoints(Long deviceId, List<IotDevicePointSaveReq> reqList) {
        IotDevice device = requireManagedDevice(deviceId);
        if (CollectionUtils.isEmpty(reqList)) {
            return;
        }
        for (IotDevicePointSaveReq req : reqList) {
            if (req == null || StringUtils.isBlank(req.getPropertyCode())) {
                continue;
            }
            if (existsPointCode(deviceId, req.getPropertyCode(), null)) {
                continue;
            }
            IotDevicePoint point = buildPoint(deviceId, device.getTenantId(), req);
            Date now = new Date();
            point.setCreateTime(now);
            point.setUpdateTime(now);
            iotDevicePointMapper.insertSelective(point);
        }
        syncThirdPartyCodesFromDevice(device);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDevicePoint(Long id) {
        IotDevicePoint point = requireDevicePoint(id);
        point.setDeleted(1);
        point.setUpdateTime(new Date());
        iotDevicePointMapper.updateByPrimaryKeySelective(point);
    }

    public List<IotDevicePointDefinition> listDevicePointDefinitions(Long devicePointId) {
        requireDevicePoint(devicePointId);
        Example example = new Example(IotDevicePointDefinition.class);
        example.createCriteria().andEqualTo("devicePointId", devicePointId);
        example.orderBy("sort").asc();
        example.orderBy("id").asc();
        return iotDevicePointDefinitionMapper.selectByExample(example);
    }

    @Transactional(rollbackFor = Exception.class)
    public IotDevicePointDefinition saveDevicePointDefinition(Long devicePointId, IotPointDefinitionSaveReq req) {
        requireDevicePoint(devicePointId);
        validatePointDefinitionReq(req);
        IotDevicePointDefinition definition = new IotDevicePointDefinition();
        definition.setDevicePointId(devicePointId);
        definition.setValue(StringUtils.trim(req.getValue()));
        definition.setDescription(StringUtils.trimToNull(req.getDescription()));
        definition.setTags(StringUtils.trimToNull(req.getTags()));
        definition.setSort(req.getSort() == null ? 0 : req.getSort());
        definition.setRemark(StringUtils.trimToNull(req.getRemark()));
        Date now = new Date();
        definition.setCreateTime(now);
        definition.setUpdateTime(now);
        iotDevicePointDefinitionMapper.insertSelective(definition);
        return definition;
    }

    @Transactional(rollbackFor = Exception.class)
    public IotDevicePointDefinition updateDevicePointDefinition(Long id, IotPointDefinitionSaveReq req) {
        IotDevicePointDefinition definition = requireDevicePointDefinition(id);
        validatePointDefinitionReq(req);
        definition.setValue(StringUtils.trim(req.getValue()));
        definition.setDescription(StringUtils.trimToNull(req.getDescription()));
        definition.setTags(StringUtils.trimToNull(req.getTags()));
        definition.setSort(req.getSort() == null ? definition.getSort() : req.getSort());
        definition.setRemark(req.getRemark() == null ? definition.getRemark() : StringUtils.trimToNull(req.getRemark()));
        definition.setUpdateTime(new Date());
        iotDevicePointDefinitionMapper.updateByPrimaryKeySelective(definition);
        return iotDevicePointDefinitionMapper.selectByPrimaryKey(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDevicePointDefinition(Long id) {
        requireDevicePointDefinition(id);
        iotDevicePointDefinitionMapper.deleteByPrimaryKey(id);
    }

    public List<IotDeviceGroupPoint> listDeviceGroupPoints(Long deviceGroupId) {
        requireDeviceGroup(deviceGroupId);
        Example example = new Example(IotDeviceGroupPoint.class);
        example.createCriteria()
                .andEqualTo("deviceGroupId", deviceGroupId)
                .andEqualTo("deleted", 0);
        example.orderBy("sort").asc();
        example.orderBy("id").asc();
        return iotDeviceGroupPointMapper.selectByExample(example);
    }

    public List<IotDeviceGroupPointDefinition> listDeviceGroupPointDefinitions(Long deviceGroupPointId) {
        requireDeviceGroupPoint(deviceGroupPointId);
        Example example = new Example(IotDeviceGroupPointDefinition.class);
        example.createCriteria().andEqualTo("deviceGroupPointId", deviceGroupPointId);
        example.orderBy("sort").asc();
        example.orderBy("id").asc();
        return iotDeviceGroupPointDefinitionMapper.selectByExample(example);
    }

    @Transactional(rollbackFor = Exception.class)
    public IotDeviceGroupPointDefinition saveDeviceGroupPointDefinition(Long deviceGroupPointId, IotPointDefinitionSaveReq req) {
        requireDeviceGroupPoint(deviceGroupPointId);
        validatePointDefinitionReq(req);
        IotDeviceGroupPointDefinition definition = new IotDeviceGroupPointDefinition();
        definition.setDeviceGroupPointId(deviceGroupPointId);
        definition.setValue(StringUtils.trim(req.getValue()));
        definition.setDescription(StringUtils.trimToNull(req.getDescription()));
        definition.setTags(StringUtils.trimToNull(req.getTags()));
        definition.setSort(req.getSort() == null ? 0 : req.getSort());
        definition.setRemark(StringUtils.trimToNull(req.getRemark()));
        Date now = new Date();
        definition.setCreateTime(now);
        definition.setUpdateTime(now);
        iotDeviceGroupPointDefinitionMapper.insertSelective(definition);
        return definition;
    }

    @Transactional(rollbackFor = Exception.class)
    public IotDeviceGroupPointDefinition updateDeviceGroupPointDefinition(Long id, IotPointDefinitionSaveReq req) {
        IotDeviceGroupPointDefinition definition = requireDeviceGroupPointDefinition(id);
        validatePointDefinitionReq(req);
        definition.setValue(StringUtils.trim(req.getValue()));
        definition.setDescription(StringUtils.trimToNull(req.getDescription()));
        definition.setTags(StringUtils.trimToNull(req.getTags()));
        definition.setSort(req.getSort() == null ? definition.getSort() : req.getSort());
        definition.setRemark(req.getRemark() == null ? definition.getRemark() : StringUtils.trimToNull(req.getRemark()));
        definition.setUpdateTime(new Date());
        iotDeviceGroupPointDefinitionMapper.updateByPrimaryKeySelective(definition);
        return iotDeviceGroupPointDefinitionMapper.selectByPrimaryKey(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDeviceGroupPointDefinition(Long id) {
        requireDeviceGroupPointDefinition(id);
        iotDeviceGroupPointDefinitionMapper.deleteByPrimaryKey(id);
    }

    public List<IotDeviceTypeParamMetadata> listDeviceParamMetadata(String deviceTypeCode) {
        if (StringUtils.isBlank(deviceTypeCode)) {
            return defaultDeviceParamMetadata();
        }
        Example example = new Example(IotDeviceTypeParamMetadata.class);
        example.createCriteria().andEqualTo("deviceTypeCode", StringUtils.trim(deviceTypeCode));
        example.orderBy("sort").asc();
        List<IotDeviceTypeParamMetadata> list = iotDeviceTypeParamMetadataMapper.selectByExample(example);
        return list.isEmpty() ? defaultDeviceParamMetadata() : list;
    }

    public List<IotDeviceTypePointMetadata> listDevicePointMetadata(String deviceTypeCode) {
        if (StringUtils.isBlank(deviceTypeCode)) {
            return Collections.emptyList();
        }
        Example example = new Example(IotDeviceTypePointMetadata.class);
        example.createCriteria().andEqualTo("deviceTypeCode", StringUtils.trim(deviceTypeCode));
        example.orderBy("sort").asc();
        List<IotDeviceTypePointMetadata> list = iotDeviceTypePointMetadataMapper.selectByExample(example);
        return list.isEmpty() ? defaultDevicePointMetadata(deviceTypeCode) : list;
    }

    public List<IotDeviceTypePointMetadata> listDeviceGroupPointMetadata(String deviceGroupType) {
        if (StringUtils.isBlank(deviceGroupType)) {
            return defaultDevicePointMetadata();
        }
        Example example = new Example(IotDeviceGroupPointMetadata.class);
        example.createCriteria()
                .andEqualTo("deviceGroupType", StringUtils.trim(deviceGroupType))
                .andEqualTo("deleted", 0);
        example.orderBy("sort").asc();
        List<IotDeviceGroupPointMetadata> list = iotDeviceGroupPointMetadataMapper.selectByExample(example);
        if (list.isEmpty()) {
            return defaultDevicePointMetadata(deviceGroupType);
        }
        return list.stream()
                .map(this::toDeviceTypePointMetadata)
                .collect(Collectors.toList());
    }

    public List<IotDeviceGroupParam> listDeviceGroupParamMetadata() {
        Example example = new Example(IotDeviceGroupParamMetadata.class);
        example.createCriteria().andEqualTo("deleted", 0);
        example.orderBy("sort").asc();
        List<IotDeviceGroupParamMetadata> list = iotDeviceGroupParamMetadataMapper.selectByExample(example);
        if (list.isEmpty()) {
            return defaultDeviceGroupParamMetadata();
        }
        return list.stream()
                .map(this::toDeviceGroupParam)
                .collect(Collectors.toList());
    }

    private void fillDeviceGroups(List<IotDeviceGroup> groups) {
        if (CollectionUtils.isEmpty(groups)) {
            return;
        }
        Map<Long, String> gatewayMap = listGatewaysByIds(groups.stream()
                .map(IotDeviceGroup::getGatewayId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        Map<Long, Integer> deviceCountMap = countDevicesByGroupIds(groups.stream()
                .map(IotDeviceGroup::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        for (IotDeviceGroup group : groups) {
            group.setGatewayName(gatewayMap.get(group.getGatewayId()));
            group.setDeviceCount(deviceCountMap.getOrDefault(group.getId(), 0));
        }
    }

    private void fillDeviceGroup(IotDeviceGroup group) {
        if (group == null) {
            return;
        }
        fillDeviceGroups(Collections.singletonList(group));
        group.setParamList(listGroupParams(group.getId()));
        group.setPointList(listDeviceGroupPoints(group.getId()));
    }

    private void fillDevices(List<IotDevice> devices) {
        if (CollectionUtils.isEmpty(devices)) {
            return;
        }
        List<Long> deviceIds = devices.stream().map(IotDevice::getId).filter(Objects::nonNull).collect(Collectors.toList());
        List<Long> groupIds = devices.stream().map(IotDevice::getDeviceGroupId).filter(Objects::nonNull).collect(Collectors.toList());
        Map<Long, String> groupNameMap = listDeviceGroupsByIds(groupIds).stream()
                .collect(Collectors.toMap(IotDeviceGroup::getId, IotDeviceGroup::getDeviceGroupName, (a, b) -> a));
        Map<Long, List<IotDeviceParam>> paramMap = listDeviceParams(deviceIds);
        Map<Long, String> gatewayNameMap = resolveGatewayNameByDeviceIds(deviceIds);
        for (IotDevice device : devices) {
            device.setDeviceGroupName(groupNameMap.get(device.getDeviceGroupId()));
            device.setParamList(paramMap.getOrDefault(device.getId(), Collections.emptyList()));
            device.setGatewayName(gatewayNameMap.get(device.getId()));
        }
    }

    private void fillDevice(IotDevice device) {
        if (device == null) {
            return;
        }
        fillDevices(Collections.singletonList(device));
        device.setPointList(listDevicePoints(device.getId(), null, null));
        if (device.getGatewayId() == null) {
            Map<Long, Long> gatewayIdMap = resolveGatewayIdByDeviceIds(Collections.singletonList(device.getId()));
            device.setGatewayId(gatewayIdMap.get(device.getId()));
        }
    }

    private Map<Long, String> listGatewaysByIds(List<Long> gatewayIds) {
        if (CollectionUtils.isEmpty(gatewayIds)) {
            return Collections.emptyMap();
        }
        Example example = new Example(IotGateway.class);
        example.createCriteria().andIn("id", gatewayIds);
        return iotGatewayMapper.selectByExample(example).stream()
                .collect(Collectors.toMap(IotGateway::getId, IotGateway::getGatewayName, (a, b) -> a));
    }

    private Map<Long, Integer> countDevicesByGroupIds(List<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyMap();
        }
        Example example = new Example(IotDevice.class);
        example.createCriteria()
                .andIn("deviceGroupId", groupIds)
                .andEqualTo("deleted", 0);
        List<IotDevice> list = iotDeviceMapper.selectByExample(example);
        Map<Long, Integer> result = new LinkedHashMap<>();
        for (IotDevice item : list) {
            if (item.getDeviceGroupId() == null) {
                continue;
            }
            result.put(item.getDeviceGroupId(), result.getOrDefault(item.getDeviceGroupId(), 0) + 1);
        }
        return result;
    }

    private List<IotDeviceGroupParam> listGroupParams(Long groupId) {
        Example example = new Example(IotDeviceGroupParam.class);
        example.createCriteria().andEqualTo("deviceGroupId", groupId);
        example.orderBy("sort").asc();
        example.orderBy("id").asc();
        return iotDeviceGroupParamMapper.selectByExample(example);
    }

    private List<IotDeviceGroup> listDeviceGroupsByIds(List<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyList();
        }
        Example example = new Example(IotDeviceGroup.class);
        example.createCriteria()
                .andIn("id", groupIds)
                .andEqualTo("deleted", 0);
        return iotDeviceGroupMapper.selectByExample(example);
    }

    private Map<Long, List<IotDeviceParam>> listDeviceParams(List<Long> deviceIds) {
        if (CollectionUtils.isEmpty(deviceIds)) {
            return Collections.emptyMap();
        }
        Example example = new Example(IotDeviceParam.class);
        example.createCriteria().andIn("deviceId", deviceIds);
        example.orderBy("sort").asc();
        example.orderBy("id").asc();
        List<IotDeviceParam> params = iotDeviceParamMapper.selectByExample(example);
        return params.stream().collect(Collectors.groupingBy(IotDeviceParam::getDeviceId, LinkedHashMap::new, Collectors.toList()));
    }

    private Map<Long, String> resolveGatewayNameByDeviceIds(List<Long> deviceIds) {
        Map<Long, Long> gatewayIdMap = resolveGatewayIdByDeviceIds(deviceIds);
        if (gatewayIdMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, String> gatewayNameMap = listGatewaysByIds(new ArrayList<>(gatewayIdMap.values()));
        Map<Long, String> result = new LinkedHashMap<>();
        for (Map.Entry<Long, Long> entry : gatewayIdMap.entrySet()) {
            result.put(entry.getKey(), gatewayNameMap.get(entry.getValue()));
        }
        return result;
    }

    private Map<Long, Long> resolveGatewayIdByDeviceIds(List<Long> deviceIds) {
        if (CollectionUtils.isEmpty(deviceIds)) {
            return Collections.emptyMap();
        }
        Example deviceExample = new Example(IotDevice.class);
        deviceExample.createCriteria()
                .andIn("id", deviceIds)
                .andEqualTo("deleted", 0);
        List<IotDevice> devices = iotDeviceMapper.selectByExample(deviceExample);
        Map<Long, Long> result = new LinkedHashMap<>();
        for (IotDevice device : devices) {
            if (device.getGatewayId() != null) {
                result.put(device.getId(), device.getGatewayId());
            }
        }
        List<Long> missingDeviceIds = deviceIds.stream()
                .filter(id -> !result.containsKey(id))
                .collect(Collectors.toList());
        if (missingDeviceIds.isEmpty()) {
            return result;
        }
        Example example = new Example(IotDeviceExternalRef.class);
        example.createCriteria().andIn("deviceId", missingDeviceIds);
        List<IotDeviceExternalRef> refs = iotDeviceExternalRefMapper.selectByExample(example);
        if (refs.isEmpty()) {
            return result;
        }
        List<String> gatewayCodes = refs.stream()
                .map(IotDeviceExternalRef::getGatewayCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (gatewayCodes.isEmpty()) {
            return result;
        }
        Example gatewayExample = new Example(IotGateway.class);
        gatewayExample.createCriteria().andIn("gatewayCode", gatewayCodes);
        Map<String, Long> codeIdMap = iotGatewayMapper.selectByExample(gatewayExample).stream()
                .collect(Collectors.toMap(IotGateway::getGatewayCode, IotGateway::getId, (a, b) -> a));
        for (IotDeviceExternalRef ref : refs) {
            Long gatewayId = codeIdMap.get(ref.getGatewayCode());
            if (gatewayId != null) {
                result.putIfAbsent(ref.getDeviceId(), gatewayId);
            }
        }
        return result;
    }

    private IotDeviceGroup ensureDefaultDeviceGroup(String aggregatorId, String entId) {
        Example example = new Example(IotDeviceGroup.class);
        example.createCriteria()
                .andEqualTo("entId", entId)
                .andEqualTo("virtualFlag", 1)
                .andEqualTo("deleted", 0);
        List<IotDeviceGroup> groups = iotDeviceGroupMapper.selectByExample(example);
        if (!groups.isEmpty()) {
            return groups.get(0);
        }
        IotGateway gateway = ensureDefaultGateway(aggregatorId, entId);
        IotDeviceGroup group = new IotDeviceGroup();
        group.setTenantId(parseTenantId(entId));
        group.setAggregatorId(StringUtils.trimToNull(aggregatorId));
        group.setEntId(StringUtils.trim(entId));
        group.setDeviceGroupCode(generateDeviceGroupCode(entId, DEFAULT_DEVICE_GROUP_TYPE));
        group.setDeviceGroupName(DEFAULT_DEVICE_GROUP_NAME);
        group.setDeviceGroupType(DEFAULT_DEVICE_GROUP_TYPE);
        group.setDeviceGroupTypeName(DEFAULT_DEVICE_GROUP_TYPE_NAME);
        group.setGatewayId(gateway.getId());
        group.setVirtualFlag(1);
        group.setStatus(1);
        group.setDeleted(0);
        Date now = new Date();
        group.setCreateTime(now);
        group.setUpdateTime(now);
        iotDeviceGroupMapper.insertSelective(group);
        saveDeviceGroupParams(group.getId(), group.getTenantId(), defaultDeviceGroupParamReqs());
        saveDeviceGroupPoints(group.getId(), group.getTenantId(), defaultDeviceGroupPointReqs());
        return group;
    }

    private IotGateway ensureDefaultGateway(String aggregatorId, String entId) {
        Example example = new Example(IotGateway.class);
        example.createCriteria()
                .andEqualTo("entId", entId)
                .andEqualTo("deleted", 0)
                .andEqualTo("defaultFlag", 1);
        List<IotGateway> gateways = iotGatewayMapper.selectByExample(example);
        if (!gateways.isEmpty()) {
            return gateways.get(0);
        }
        IotGateway gateway = new IotGateway();
        gateway.setTenantId(parseTenantId(entId));
        gateway.setAggregatorId(StringUtils.trimToNull(aggregatorId));
        gateway.setEntId(StringUtils.trim(entId));
        gateway.setGatewayCode(generateGatewayCode(entId));
        gateway.setGatewayName(DEFAULT_GATEWAY_NAME);
        gateway.setStatus(1);
        gateway.setDefaultFlag(1);
        gateway.setDeleted(0);
        Date now = new Date();
        gateway.setCreateTime(now);
        gateway.setUpdateTime(now);
        iotGatewayMapper.insertSelective(gateway);
        return gateway;
    }

    private IotDeviceGroup ensureDeviceGroupRequired(String aggregatorId, String entId, Long deviceGroupId) {
        if (deviceGroupId != null) {
            IotDeviceGroup group = requireDeviceGroup(deviceGroupId);
            if (!StringUtils.equals(group.getEntId(), entId)) {
                throwParam("设备组不属于当前企业");
            }
            return group;
        }
        return ensureDefaultDeviceGroup(aggregatorId, entId);
    }

    private IotGateway ensureGatewayRequired(String aggregatorId, String entId, Long gatewayId) {
        if (gatewayId == null) {
            return ensureDefaultGateway(aggregatorId, entId);
        }
        IotGateway gateway = iotGatewayMapper.selectByPrimaryKey(gatewayId);
        if (gateway == null || Integer.valueOf(1).equals(gateway.getDeleted())) {
            throwParam("网关不存在");
        }
        if (!StringUtils.equals(entId, gateway.getEntId())) {
            throwParam("网关不属于当前企业");
        }
        return gateway;
    }

    private IotGateway resolveGatewayForDevice(IotDeviceSaveReq req, IotDeviceGroup group) {
        Long gatewayId = req.getGatewayId();
        if (gatewayId == null) {
            gatewayId = group.getGatewayId();
        }
        return ensureGatewayRequired(StringUtils.defaultIfBlank(req.getAggregatorId(), group.getAggregatorId()), group.getEntId(), gatewayId);
    }

    private IotDeviceGroup requireDeviceGroup(Long id) {
        IotDeviceGroup group = iotDeviceGroupMapper.selectByPrimaryKey(id);
        if (group == null || Integer.valueOf(1).equals(group.getDeleted())) {
            throwParam("设备组不存在");
        }
        return group;
    }

    private IotDevice requireManagedDevice(Long id) {
        IotDevice device = iotDeviceMapper.selectByPrimaryKey(id);
        if (device == null || Integer.valueOf(1).equals(device.getDeleted())) {
            throwParam("设备不存在");
        }
        return device;
    }

    private IotDevicePoint requireDevicePoint(Long id) {
        IotDevicePoint point = iotDevicePointMapper.selectByPrimaryKey(id);
        if (point == null || Integer.valueOf(1).equals(point.getDeleted())) {
            throwParam("测点不存在");
        }
        return point;
    }

    private IotDevicePointDefinition requireDevicePointDefinition(Long id) {
        IotDevicePointDefinition definition = iotDevicePointDefinitionMapper.selectByPrimaryKey(id);
        if (definition == null) {
            throwParam("状态值定义不存在");
        }
        return definition;
    }

    private IotDeviceGroupPoint requireDeviceGroupPoint(Long id) {
        IotDeviceGroupPoint point = iotDeviceGroupPointMapper.selectByPrimaryKey(id);
        if (point == null || Integer.valueOf(1).equals(point.getDeleted())) {
            throwParam("设备组测点不存在");
        }
        return point;
    }

    private IotDeviceGroupPointDefinition requireDeviceGroupPointDefinition(Long id) {
        IotDeviceGroupPointDefinition definition = iotDeviceGroupPointDefinitionMapper.selectByPrimaryKey(id);
        if (definition == null) {
            throwParam("设备组状态值定义不存在");
        }
        return definition;
    }

    private void replaceDeviceGroupParams(Long groupId, Long tenantId, List<IotDeviceGroupParamSaveReq> reqList) {
        Example example = new Example(IotDeviceGroupParam.class);
        example.createCriteria().andEqualTo("deviceGroupId", groupId);
        iotDeviceGroupParamMapper.deleteByExample(example);
        saveDeviceGroupParams(groupId, tenantId, reqList);
    }

    private void saveDeviceGroupParams(Long groupId, Long tenantId, List<IotDeviceGroupParamSaveReq> reqList) {
        if (CollectionUtils.isEmpty(reqList)) {
            return;
        }
        Date now = new Date();
        for (IotDeviceGroupParamSaveReq req : reqList) {
            if (req == null || StringUtils.isBlank(req.getAttrCode()) || StringUtils.isBlank(req.getAttrName())) {
                continue;
            }
            IotDeviceGroupParam param = new IotDeviceGroupParam();
            param.setTenantId(tenantId);
            param.setDeviceGroupId(groupId);
            param.setAttrCode(StringUtils.trim(req.getAttrCode()));
            param.setAttrName(StringUtils.trim(req.getAttrName()));
            param.setAliasName(StringUtils.trimToNull(req.getAliasName()));
            param.setAttrValue(StringUtils.trimToNull(req.getAttrValue()));
            param.setAttrUnit(StringUtils.trimToNull(req.getAttrUnit()));
            param.setAttrType(StringUtils.trimToNull(req.getAttrType()));
            param.setSort(req.getSort() == null ? 0 : req.getSort());
            param.setDeleted(0);
            param.setRemark(StringUtils.trimToNull(req.getRemark()));
            param.setCreateTime(now);
            param.setUpdateTime(now);
            iotDeviceGroupParamMapper.insertSelective(param);
        }
    }

    private void replaceDeviceGroupPoints(Long groupId, Long tenantId, List<IotDeviceGroupPointSaveReq> reqList) {
        Example example = new Example(IotDeviceGroupPoint.class);
        example.createCriteria().andEqualTo("deviceGroupId", groupId);
        List<IotDeviceGroupPoint> oldPoints = iotDeviceGroupPointMapper.selectByExample(example);
        if (!oldPoints.isEmpty()) {
            List<Long> pointIds = oldPoints.stream().map(IotDeviceGroupPoint::getId).filter(Objects::nonNull).collect(Collectors.toList());
            if (!pointIds.isEmpty()) {
                Example definitionExample = new Example(IotDeviceGroupPointDefinition.class);
                definitionExample.createCriteria().andIn("deviceGroupPointId", pointIds);
                iotDeviceGroupPointDefinitionMapper.deleteByExample(definitionExample);
            }
        }
        iotDeviceGroupPointMapper.deleteByExample(example);
        saveDeviceGroupPoints(groupId, tenantId, reqList);
    }

    private void saveDeviceGroupPoints(Long groupId, Long tenantId, List<IotDeviceGroupPointSaveReq> reqList) {
        if (CollectionUtils.isEmpty(reqList)) {
            return;
        }
        List<IotDeviceGroupPointSaveReq> source = reqList;
        Date now = new Date();
        for (IotDeviceGroupPointSaveReq req : source) {
            if (req == null || StringUtils.isBlank(req.getPropertyCode()) || StringUtils.isBlank(req.getPropertyName())) {
                continue;
            }
            IotDeviceGroupPoint point = new IotDeviceGroupPoint();
            point.setTenantId(tenantId);
            point.setDeviceGroupId(groupId);
            point.setPropertyCode(StringUtils.trim(req.getPropertyCode()));
            point.setPropertyName(StringUtils.trim(req.getPropertyName()));
            point.setDataType(StringUtils.trimToNull(req.getDataType()));
            point.setDataTypeName(StringUtils.trimToNull(req.getDataTypeName()));
            point.setValueType(StringUtils.defaultIfBlank(req.getValueType(), "double"));
            point.setUnit(StringUtils.trimToNull(req.getUnit()));
            point.setReadWriteRole(StringUtils.defaultIfBlank(req.getReadWriteRole(), READ_ONLY));
            point.setValueLowerLimit(StringUtils.trimToNull(req.getValueLowerLimit()));
            point.setValueHighLimit(StringUtils.trimToNull(req.getValueHighLimit()));
            point.setDeadZoneType(req.getDeadZoneType());
            point.setType(req.getType());
            point.setSort(req.getSort() == null ? 0 : req.getSort());
            point.setStatus(req.getStatus() == null ? 1 : req.getStatus());
            point.setDeleted(0);
            point.setRemark(StringUtils.trimToNull(req.getRemark()));
            point.setCreateTime(now);
            point.setUpdateTime(now);
            iotDeviceGroupPointMapper.insertSelective(point);
        }
    }

    private void replaceDeviceParams(Long deviceId, Long tenantId, List<IotDeviceParamSaveReq> reqList) {
        Example example = new Example(IotDeviceParam.class);
        example.createCriteria().andEqualTo("deviceId", deviceId);
        iotDeviceParamMapper.deleteByExample(example);
        if (CollectionUtils.isEmpty(reqList)) {
            return;
        }
        Date now = new Date();
        for (IotDeviceParamSaveReq req : reqList) {
            if (req == null || StringUtils.isBlank(req.getAttrCode())) {
                continue;
            }
            IotDeviceParam param = new IotDeviceParam();
            param.setTenantId(req.getTenantId() == null ? tenantId : req.getTenantId());
            param.setDeviceId(deviceId);
            param.setAttrCode(StringUtils.trim(req.getAttrCode()));
            param.setAttrName(StringUtils.defaultIfBlank(StringUtils.trimToNull(req.getAttrName()), param.getAttrCode()));
            param.setAliasName(StringUtils.trimToNull(req.getAliasName()));
            param.setAttrValue(StringUtils.trimToNull(req.getAttrValue()));
            param.setAttrUnit(StringUtils.trimToNull(req.getAttrUnit()));
            param.setAttrType(StringUtils.trimToNull(req.getAttrType()));
            param.setSort(req.getSort() == null ? 0 : req.getSort());
            param.setDeleted(0);
            param.setRemark(StringUtils.trimToNull(req.getRemark()));
            param.setCreateTime(now);
            param.setUpdateTime(now);
            iotDeviceParamMapper.insertSelective(param);
        }
    }

    private void replaceDevicePoints(Long deviceId, Long tenantId, List<IotDevicePointSaveReq> reqList, Long groupId) {
        Example example = new Example(IotDevicePoint.class);
        example.createCriteria().andEqualTo("deviceId", deviceId);
        List<IotDevicePoint> oldPoints = iotDevicePointMapper.selectByExample(example);
        if (!oldPoints.isEmpty()) {
            List<Long> pointIds = oldPoints.stream().map(IotDevicePoint::getId).filter(Objects::nonNull).collect(Collectors.toList());
            if (!pointIds.isEmpty()) {
                Example definitionExample = new Example(IotDevicePointDefinition.class);
                definitionExample.createCriteria().andIn("devicePointId", pointIds);
                iotDevicePointDefinitionMapper.deleteByExample(definitionExample);
            }
        }
        iotDevicePointMapper.deleteByExample(example);
        if (CollectionUtils.isEmpty(reqList)) {
            return;
        }
        Date now = new Date();
        for (IotDevicePointSaveReq req : reqList) {
            if (req == null || StringUtils.isBlank(req.getPropertyCode()) || StringUtils.isBlank(req.getPropertyName())) {
                continue;
            }
            IotDevicePoint point = buildPoint(deviceId, tenantId, req);
            point.setCreateTime(now);
            point.setUpdateTime(now);
            iotDevicePointMapper.insertSelective(point);
        }
    }

    private void copyDevicePointsFromGroup(Long deviceId, Long tenantId, Long groupId) {
        if (groupId == null) {
            return;
        }
        List<IotDeviceGroupPoint> groupPoints = listDeviceGroupPoints(groupId);
        if (groupPoints.isEmpty()) {
            groupPoints = defaultDeviceGroupPoints();
        }
        Date now = new Date();
        for (IotDeviceGroupPoint groupPoint : groupPoints) {
            if (existsPointCode(deviceId, groupPoint.getPropertyCode(), null)) {
                continue;
            }
            IotDevicePoint point = new IotDevicePoint();
            point.setTenantId(tenantId);
            point.setDeviceId(deviceId);
            point.setPropertyCode(groupPoint.getPropertyCode());
            point.setPropertyName(groupPoint.getPropertyName());
            point.setDataType(groupPoint.getDataType());
            point.setDataTypeName(groupPoint.getDataTypeName());
            point.setValueType(StringUtils.defaultIfBlank(groupPoint.getValueType(), "double"));
            point.setUnit(groupPoint.getUnit());
            point.setDataFrequency(60);
            point.setRequiredFlag(0);
            point.setReadWriteRole(StringUtils.defaultIfBlank(groupPoint.getReadWriteRole(), READ_ONLY));
            point.setValueLowerLimit(groupPoint.getValueLowerLimit());
            point.setValueHighLimit(groupPoint.getValueHighLimit());
            point.setDeadZoneType(groupPoint.getDeadZoneType());
            point.setType(groupPoint.getType());
            point.setStatus(groupPoint.getStatus() == null ? 1 : groupPoint.getStatus());
            point.setDeleted(0);
            point.setSort(groupPoint.getSort() == null ? 0 : groupPoint.getSort());
            point.setRemark(groupPoint.getRemark());
            point.setCreateTime(now);
            point.setUpdateTime(now);
            iotDevicePointMapper.insertSelective(point);
        }
    }

    private IotDevicePoint buildPoint(Long deviceId, Long tenantId, IotDevicePointSaveReq req) {
        IotDevicePoint point = new IotDevicePoint();
        point.setTenantId(req.getTenantId() == null ? tenantId : req.getTenantId());
        point.setDeviceId(deviceId);
        point.setPropertyCode(StringUtils.trim(req.getPropertyCode()));
        point.setPropertyName(StringUtils.trim(req.getPropertyName()));
        point.setThirdPartyCode(StringUtils.trimToNull(req.getThirdPartyCode()));
        point.setDataType(StringUtils.trimToNull(req.getDataType()));
        point.setDataTypeName(StringUtils.trimToNull(req.getDataTypeName()));
        point.setValueType(StringUtils.defaultIfBlank(req.getValueType(), "double"));
        point.setUnit(StringUtils.trimToNull(req.getUnit()));
        point.setDataFrequency(req.getDataFrequency() == null ? 60 : req.getDataFrequency());
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

    private void bindGatewayAndThirdParty(Long deviceId, IotGateway gateway, IotDeviceSaveReq req) {
        Example example = new Example(IotDeviceExternalRef.class);
        example.createCriteria().andEqualTo("deviceId", deviceId);
        List<IotDeviceExternalRef> refs = iotDeviceExternalRefMapper.selectByExample(example);
        IotDeviceExternalRef ref = refs.isEmpty() ? new IotDeviceExternalRef() : refs.get(0);
        if (refs.isEmpty()) {
            ref.setDeviceId(deviceId);
            ref.setSourceCode(StringUtils.defaultIfBlank(req.getThirdPartyApi(), DEFAULT_DEVICE_SOURCE_CODE));
            ref.setEntId(StringUtils.trim(req.getEntId()));
            ref.setProjectId(StringUtils.trimToNull(req.getProjectId()));
            ref.setExternalDeviceId(StringUtils.defaultIfBlank(StringUtils.trimToNull(req.getThirdPartyCode()), "DEVICE_" + deviceId));
            ref.setExternalDeviceCode(StringUtils.trimToNull(req.getThirdPartyCode()));
            ref.setExternalDeviceName(StringUtils.trimToNull(req.getDeviceName()));
            ref.setStatus(1);
            ref.setCreateTime(new Date());
        } else {
            ref.setSourceCode(StringUtils.defaultIfBlank(StringUtils.trimToNull(req.getThirdPartyApi()), ref.getSourceCode()));
            ref.setExternalDeviceId(StringUtils.defaultIfBlank(StringUtils.trimToNull(req.getThirdPartyCode()), ref.getExternalDeviceId()));
            ref.setExternalDeviceCode(req.getThirdPartyCode() == null ? ref.getExternalDeviceCode() : StringUtils.trimToNull(req.getThirdPartyCode()));
            ref.setExternalDeviceName(StringUtils.defaultIfBlank(StringUtils.trimToNull(req.getDeviceName()), ref.getExternalDeviceName()));
            ref.setProjectId(req.getProjectId() == null ? ref.getProjectId() : StringUtils.trimToNull(req.getProjectId()));
        }
        ref.setGatewayCode(gateway.getGatewayCode());
        ref.setUpdateTime(new Date());
        if (refs.isEmpty()) {
            iotDeviceExternalRefMapper.insertSelective(ref);
        } else {
            iotDeviceExternalRefMapper.updateByPrimaryKeySelective(ref);
        }
    }

    private void syncThirdPartyCodesFromDevice(IotDevice device) {
        if (device == null || device.getId() == null) {
            return;
        }
        Example example = new Example(IotDeviceExternalRef.class);
        example.createCriteria().andEqualTo("deviceId", device.getId());
        List<IotDeviceExternalRef> refs = iotDeviceExternalRefMapper.selectByExample(example);
        if (refs.isEmpty()) {
            return;
        }
        IotDeviceExternalRef ref = refs.get(0);
        if (StringUtils.isBlank(ref.getExternalDeviceName())) {
            ref.setExternalDeviceName(device.getDeviceName());
            ref.setUpdateTime(new Date());
            iotDeviceExternalRefMapper.updateByPrimaryKeySelective(ref);
        }
    }

    private String resolveDeviceCode(String entId, String deviceTypeCode, String inputCode, Long excludeId) {
        String code = StringUtils.trimToNull(inputCode);
        if (StringUtils.isBlank(code)) {
            code = generateDeviceCode(entId, deviceTypeCode);
        }
        if (existsDeviceCode(entId, code, excludeId)) {
            throwParam("设备编码在当前企业下已存在");
        }
        return code;
    }

    private boolean existsDeviceCode(String entId, String deviceCode, Long excludeId) {
        Example example = new Example(IotDevice.class);
        example.createCriteria()
                .andEqualTo("entId", entId)
                .andEqualTo("deviceCode", deviceCode)
                .andEqualTo("deleted", 0);
        List<IotDevice> list = iotDeviceMapper.selectByExample(example);
        for (IotDevice item : list) {
            if (excludeId == null || !excludeId.equals(item.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean existsPointCode(Long deviceId, String pointCode, Long excludeId) {
        Example example = new Example(IotDevicePoint.class);
        example.createCriteria()
                .andEqualTo("deviceId", deviceId)
                .andEqualTo("propertyCode", pointCode)
                .andEqualTo("deleted", 0);
        List<IotDevicePoint> list = iotDevicePointMapper.selectByExample(example);
        for (IotDevicePoint item : list) {
            if (excludeId == null || !excludeId.equals(item.getId())) {
                return true;
            }
        }
        return false;
    }

    private String generateDeviceGroupCode(String entId, String deviceGroupType) {
        String prefix = normalizeCodePrefix(StringUtils.defaultIfBlank(deviceGroupType, DEFAULT_DEVICE_GROUP_TYPE));
        int index = 1;
        String code;
        do {
            code = prefix + String.format(Locale.ROOT, "%03d", index++);
        } while (existsDeviceGroupCode(entId, code));
        return code;
    }

    private boolean existsDeviceGroupCode(String entId, String code) {
        Example example = new Example(IotDeviceGroup.class);
        example.createCriteria()
                .andEqualTo("entId", entId)
                .andEqualTo("deviceGroupCode", code)
                .andEqualTo("deleted", 0);
        return iotDeviceGroupMapper.selectCountByExample(example) > 0;
    }

    private String generateGatewayCode(String entId) {
        String prefix = DEFAULT_GATEWAY_CODE_PREFIX + normalizeCodePrefix(entId);
        int index = 1;
        String code;
        do {
            code = prefix + String.format(Locale.ROOT, "%03d", index++);
        } while (existsGatewayCode(entId, code));
        return code;
    }

    private boolean existsGatewayCode(String entId, String code) {
        Example example = new Example(IotGateway.class);
        example.createCriteria()
                .andEqualTo("entId", entId)
                .andEqualTo("gatewayCode", code)
                .andEqualTo("deleted", 0);
        return iotGatewayMapper.selectCountByExample(example) > 0;
    }

    private String generateDeviceCode(String entId, String deviceTypeCode) {
        String prefix = normalizeCodePrefix(StringUtils.defaultIfBlank(deviceTypeCode, "DEV"));
        int index = 1;
        String code;
        do {
            code = prefix + String.format(Locale.ROOT, "%03d", index++);
        } while (existsDeviceCode(entId, code, null));
        return code;
    }

    private String normalizeCodePrefix(String value) {
        String prefix = StringUtils.defaultIfBlank(value, "DEF").toUpperCase(Locale.ROOT);
        prefix = prefix.replaceAll("[^A-Z0-9]", "");
        if (StringUtils.isBlank(prefix)) {
            return "DEF";
        }
        return prefix;
    }

    private String resolveDeviceTypeName(String deviceTypeCode, String currentName) {
        if (StringUtils.isNotBlank(currentName)) {
            return StringUtils.trim(currentName);
        }
        return DEVICE_TYPES.stream()
                .filter(item -> StringUtils.equals(item.getValue(), deviceTypeCode))
                .map(IotOptionVO::getLabel)
                .findFirst()
                .orElse(StringUtils.trimToNull(deviceTypeCode));
    }

    private String resolveDeviceGroupTypeName(String typeCode, String currentName) {
        if (StringUtils.isNotBlank(currentName)) {
            return StringUtils.trim(currentName);
        }
        return DEVICE_GROUP_TYPES.stream()
                .filter(item -> StringUtils.equals(item.getValue(), typeCode))
                .map(IotOptionVO::getLabel)
                .findFirst()
                .orElse(DEFAULT_DEVICE_GROUP_TYPE.equals(typeCode) ? DEFAULT_DEVICE_GROUP_TYPE_NAME : StringUtils.trimToNull(typeCode));
    }

    private void validateDeviceGroupReq(IotDeviceGroupSaveReq req) {
        if (req == null) {
            throwParam("设备组数据不能为空");
        }
        requireNotBlank(req.getEntId(), "企业不能为空");
        requireNotBlank(req.getDeviceGroupName(), "设备组名称不能为空");
    }

    private void validateManagedDeviceReq(IotDeviceSaveReq req) {
        if (req == null) {
            throwParam("设备数据不能为空");
        }
        requireNotBlank(req.getEntId(), "企业不能为空");
        requireNotBlank(req.getDeviceName(), "设备名称不能为空");
    }

    private void validatePointDefinitionReq(IotPointDefinitionSaveReq req) {
        if (req == null) {
            throwParam("状态值维护数据不能为空");
        }
        requireNotBlank(req.getValue(), "状态值不能为空");
    }

    private void requireNotBlank(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throwParam(message);
        }
    }

    private void throwParam(String message) {
        throw new BaseException(StatusCode.C.getCode(), message);
    }

    private Long parseTenantId(String entId) {
        String value = StringUtils.trimToNull(entId);
        if (value == null || !StringUtils.isNumeric(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private List<IotDeviceTypeParamMetadata> defaultDeviceParamMetadata() {
        List<IotDeviceTypeParamMetadata> list = new ArrayList<>();
        list.add(paramMeta("brand", "品牌", 1, 1, null));
        list.add(paramMeta("model", "型号", 1, 2, null));
        list.add(paramMeta("contact_method", "通讯方式", 1, 3, null));
        list.add(paramMeta("carrier", "运营商", 0, 4, null));
        list.add(paramMeta("iot_card_number", "物联卡号", 0, 5, null));
        return list;
    }

    private IotDeviceTypeParamMetadata paramMeta(String code, String name, int required, int sort, String sample) {
        IotDeviceTypeParamMetadata meta = new IotDeviceTypeParamMetadata();
        meta.setAttrCode(code);
        meta.setAttrName(name);
        meta.setRequiredFlag(required);
        meta.setSort(sort);
        meta.setSampleValue(sample);
        return meta;
    }

    private List<IotDeviceTypePointMetadata> defaultDevicePointMetadata() {
        List<IotDeviceTypePointMetadata> list = new ArrayList<>();
        list.add(pointMeta("active_power", "有功功率", "double", "数值", "double", "kW", 1));
        list.add(pointMeta("reactive_power", "无功功率", "double", "数值", "double", "kVar", 2));
        list.add(pointMeta("soc", "荷电状态", "double", "数值", "double", "%", 3));
        list.add(pointMeta("status", "运行状态", "enum", "枚举", "string", null, 4));
        return list;
    }

    private List<IotDeviceTypePointMetadata> defaultDevicePointMetadata(String deviceTypeCode) {
        List<IotDeviceTypePointMetadata> list = defaultDevicePointMetadata();
        String trimmedDeviceTypeCode = StringUtils.trimToNull(deviceTypeCode);
        for (IotDeviceTypePointMetadata meta : list) {
            meta.setDeviceTypeCode(trimmedDeviceTypeCode);
        }
        return list;
    }

    private IotDeviceTypePointMetadata pointMeta(String code, String name, String dataType,
                                                 String dataTypeName, String valueType, String unit, int sort) {
        IotDeviceTypePointMetadata meta = new IotDeviceTypePointMetadata();
        meta.setPropertyCode(code);
        meta.setPropertyName(name);
        meta.setDataType(dataType);
        meta.setDataTypeName(dataTypeName);
        meta.setValueType(valueType);
        meta.setUnit(unit);
        meta.setReadWriteRole(READ_ONLY);
        meta.setSort(sort);
        return meta;
    }

    private IotDeviceTypePointMetadata toDeviceTypePointMetadata(IotDeviceGroupPointMetadata source) {
        IotDeviceTypePointMetadata target = new IotDeviceTypePointMetadata();
        target.setDeviceTypeCode(source.getDeviceGroupType());
        target.setDeviceTypeName(source.getDeviceGroupTypeName());
        target.setPropertyCode(source.getPropertyCode());
        target.setPropertyName(source.getPropertyName());
        target.setDataType(source.getDataType());
        target.setDataTypeName(source.getDataTypeName());
        target.setValueType(source.getValueType());
        target.setUnit(source.getUnit());
        target.setReadWriteRole(source.getReadWriteRole());
        target.setSort(source.getSort());
        target.setRemark(source.getRemark());
        target.setCreateTime(source.getCreateTime());
        target.setUpdateTime(source.getUpdateTime());
        target.setDeleted(source.getDeleted());
        return target;
    }

    private IotDeviceGroupParam toDeviceGroupParam(IotDeviceGroupParamMetadata source) {
        IotDeviceGroupParam target = new IotDeviceGroupParam();
        target.setAttrCode(source.getAttrCode());
        target.setAttrName(source.getAttrName());
        target.setAttrType(source.getAttrType());
        target.setAttrUnit(source.getAttrUnit());
        target.setSort(source.getSort());
        target.setRemark(source.getRemark());
        target.setDeleted(source.getDeleted());
        target.setCreateTime(source.getCreateTime());
        target.setUpdateTime(source.getUpdateTime());
        return target;
    }

    private List<IotDeviceGroupParam> defaultDeviceGroupParamMetadata() {
        IotDeviceGroupParam energyType = new IotDeviceGroupParam();
        energyType.setAttrCode("Heatcold");
        energyType.setAttrName("供能类型");
        energyType.setSort(1);
        IotDeviceGroupParam password = new IotDeviceGroupParam();
        password.setAttrCode("Password");
        password.setAttrName("一体机密码");
        password.setSort(2);
        return Arrays.asList(energyType, password);
    }

    private List<IotDeviceGroupParamSaveReq> defaultDeviceGroupParamReqs() {
        IotDeviceGroupParamSaveReq energyType = new IotDeviceGroupParamSaveReq();
        energyType.setAttrCode("Heatcold");
        energyType.setAttrName("供能类型");
        energyType.setSort(1);
        IotDeviceGroupParamSaveReq password = new IotDeviceGroupParamSaveReq();
        password.setAttrCode("Password");
        password.setAttrName("一体机密码");
        password.setSort(2);
        return Arrays.asList(energyType, password);
    }

    private List<IotDeviceGroupPointSaveReq> defaultDeviceGroupPointReqs() {
        List<IotDeviceGroupPointSaveReq> list = new ArrayList<>();
        list.add(groupPointReq("active_power", "有功功率", "double", "数值", "double", "kW", 1));
        list.add(groupPointReq("reactive_power", "无功功率", "double", "数值", "double", "kVar", 2));
        list.add(groupPointReq("status", "运行状态", "enum", "枚举", "string", null, 3));
        return list;
    }

    private List<IotDeviceGroupPoint> defaultDeviceGroupPoints() {
        return defaultDeviceGroupPointReqs().stream().map(req -> {
            IotDeviceGroupPoint point = new IotDeviceGroupPoint();
            point.setPropertyCode(req.getPropertyCode());
            point.setPropertyName(req.getPropertyName());
            point.setDataType(req.getDataType());
            point.setDataTypeName(req.getDataTypeName());
            point.setValueType(req.getValueType());
            point.setUnit(req.getUnit());
            point.setReadWriteRole(req.getReadWriteRole());
            point.setSort(req.getSort());
            point.setStatus(req.getStatus());
            return point;
        }).collect(Collectors.toList());
    }

    private IotDeviceGroupPointSaveReq groupPointReq(String code, String name, String dataType,
                                                     String dataTypeName, String valueType, String unit, int sort) {
        IotDeviceGroupPointSaveReq req = new IotDeviceGroupPointSaveReq();
        req.setPropertyCode(code);
        req.setPropertyName(name);
        req.setDataType(dataType);
        req.setDataTypeName(dataTypeName);
        req.setValueType(valueType);
        req.setUnit(unit);
        req.setReadWriteRole(READ_ONLY);
        req.setSort(sort);
        req.setStatus(1);
        return req;
    }
}
