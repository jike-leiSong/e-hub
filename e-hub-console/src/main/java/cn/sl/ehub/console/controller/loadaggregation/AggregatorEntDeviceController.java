package cn.sl.ehub.console.controller.loadaggregation;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.auth.LoadAggregationScopeService;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.service.IAggregatorEntDeviceService;
import cn.sl.ehub.console.service.IAggregatorEntService;
import cn.sl.ehub.console.service.IAggregatorResourceTypeService;
import cn.sl.ehub.service.service.AggregatorSingleModelDataService;
import cn.sl.ehub.service.service.IotDeviceManagementService;
import cn.sl.ehub.service.vo.AggregatorEnt;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorResourceType;
import cn.sl.ehub.service.vo.AggregatorSingleModelData;
import cn.sl.ehub.service.vo.IotDevice;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ent-device")
@RequiredArgsConstructor
@Api(tags = "企业设备管理")
public class AggregatorEntDeviceController {

    private final IAggregatorEntDeviceService aggregatorEntDeviceService;
    private final IAggregatorEntService aggregatorEntService;
    private final IAggregatorResourceTypeService aggregatorResourceTypeService;
    private final AggregatorSingleModelDataService aggregatorSingleModelDataService;
    private final IotDeviceManagementService iotDeviceManagementService;
    private final LoadAggregationScopeService loadScopeService;

    @GetMapping("/page")
    @ApiOperation("分页查询设备")
    public ResultVO<PageResultVO<AggregatorEntDevice>> page(
            @RequestParam(value = "aggregatorId", required = false) String aggregatorId,
            @RequestParam(value = "entId", required = false) String entId,
            @RequestParam(value = "deviceName", required = false) String deviceName,
            @RequestParam(value = "energyStation", required = false) String energyStation,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        LoadAggregationScopeService.Scope scope = loadScopeService.resolveQueryScope(aggregatorId, entId);
        List<String> stationCodes = null;
        if (StringUtils.isNotBlank(energyStation)) {
            List<AggregatorSingleModelData> models = aggregatorSingleModelDataService.list(
                    scope.getAggregatorId(), scope.getEntId(), null, null, energyStation, null);
            stationCodes = models.stream()
                    .map(AggregatorSingleModelData::getEnergyStationCode)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (stationCodes.isEmpty()) {
                return ResultVO.success(emptyPage(pageIndex, pageSize));
            }
        }
        PageHelper.startPage(pageIndex, pageSize);
        List<AggregatorEntDevice> list = aggregatorEntDeviceService.queryDeviceList(
                scope.getAggregatorId(), scope.getEntId(), deviceName, status, stationCodes);
        bindExtraInfo(list);
        return ResultVO.success(toPage(list, pageIndex, pageSize));
    }

    @GetMapping("/{id}")
    @ApiOperation("设备详情")
    public ResultVO<AggregatorEntDevice> detail(@PathVariable("id") Integer id) {
        AggregatorEntDevice device = requireDevice(id);
        loadScopeService.validateScope(device.getAggregatorId(), device.getEntId());
        bindExtraInfo(Collections.singletonList(device));
        return ResultVO.success(device);
    }

    @GetMapping("/iot-device-options")
    @ApiOperation("企业物联设备选项")
    public ResultVO<List<IotDevice>> iotDeviceOptions(@RequestParam(value = "aggregatorId", required = false) String aggregatorId,
                                                      @RequestParam("entId") String entId) {
        AggregatorEnt ent = requireScopedEnt(aggregatorId, entId);
        return ResultVO.success(iotDeviceManagementService.listDeviceOptions(ent.getAggregatorId(), ent.getEntId()));
    }

    @PostMapping
    @ApiOperation("新增设备")
    public ResultVO<AggregatorEntDevice> create(@RequestBody AggregatorEntDevice device) {
        validateDeviceReq(device);
        prepareDeviceForSave(device, null);
        aggregatorEntDeviceService.createDevice(device);
        bindExtraInfo(Collections.singletonList(device));
        return ResultVO.success(device);
    }

    @PutMapping("/{id}")
    @ApiOperation("修改设备")
    public ResultVO<AggregatorEntDevice> update(@PathVariable("id") Integer id,
                                                @RequestBody AggregatorEntDevice device) {
        AggregatorEntDevice existing = requireDevice(id);
        loadScopeService.validateScope(existing.getAggregatorId(), existing.getEntId());
        if (device == null) {
            throw new BaseException(StatusCode.C.getCode(), "设备数据不能为空");
        }
        device.setId(id);
        prepareDeviceForSave(device, existing);
        aggregatorEntDeviceService.updateDevice(device);
        AggregatorEntDevice updated = requireDevice(id);
        bindExtraInfo(Collections.singletonList(updated));
        return ResultVO.success(updated);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除设备")
    public ResultVO<Boolean> delete(@PathVariable("id") Integer id) {
        AggregatorEntDevice existing = requireDevice(id);
        loadScopeService.validateScope(existing.getAggregatorId(), existing.getEntId());
        aggregatorEntDeviceService.deleteDevice(id);
        return ResultVO.success(true);
    }

    private AggregatorEntDevice requireDevice(Integer id) {
        AggregatorEntDevice device = aggregatorEntDeviceService.getDeviceById(id);
        if (device == null) {
            throw new BaseException(StatusCode.C.getCode(), "设备不存在");
        }
        return device;
    }

    private void validateDeviceReq(AggregatorEntDevice device) {
        if (device == null) {
            throw new BaseException(StatusCode.C.getCode(), "设备数据不能为空");
        }
        if (StringUtils.isBlank(device.getEntId())) {
            throw new BaseException(StatusCode.C.getCode(), "企业不能为空");
        }
        if (StringUtils.isBlank(device.getEnergyStationCode())) {
            throw new BaseException(StatusCode.C.getCode(), "项目不能为空");
        }
        if (StringUtils.isBlank(device.getIotDeviceBaseId())) {
            throw new BaseException(StatusCode.C.getCode(), "物联设备不能为空");
        }
    }

    private void prepareDeviceForSave(AggregatorEntDevice device, AggregatorEntDevice existing) {
        if (existing != null) {
            if (StringUtils.isBlank(device.getEntId())) {
                device.setEntId(existing.getEntId());
            }
            if (StringUtils.isBlank(device.getAggregatorId())) {
                device.setAggregatorId(existing.getAggregatorId());
            }
            if (StringUtils.isBlank(device.getEnergyStationCode())) {
                device.setEnergyStationCode(existing.getEnergyStationCode());
            }
            if (StringUtils.isBlank(device.getIotDeviceBaseId())) {
                device.setIotDeviceBaseId(existing.getIotDeviceBaseId());
            }
        }
        validateDeviceReq(device);

        AggregatorEnt ent = requireScopedEnt(device.getAggregatorId(), device.getEntId());
        AggregatorSingleModelData model = requireModelForDevice(ent.getAggregatorId(), ent.getEntId(), device.getEnergyStationCode());
        IotDevice iotDevice = requireIotDeviceForResource(ent.getAggregatorId(), ent.getEntId(), device.getIotDeviceBaseId());

        device.setAggregatorId(ent.getAggregatorId());
        device.setEntId(ent.getEntId());
        device.setStationId(ent.getStationId());
        device.setEnergyStation(model.getEnergyStation());
        device.setEnergyStationCode(model.getEnergyStationCode());
        device.setResourceTypeId(StringUtils.defaultIfBlank(model.getResourceTypeId(), device.getResourceTypeId()));
        if (StringUtils.isBlank(device.getResourceTypeId())) {
            throw new BaseException(StatusCode.C.getCode(), "资源类型不能为空");
        }
        device.setIotDeviceBaseId(String.valueOf(iotDevice.getId()));
        device.setDeviceBaseId(iotDevice.getDeviceCode());
        device.setDeviceId(iotDevice.getDeviceCode());
        device.setDeviceName(iotDevice.getDeviceName());
        device.setDeviceType(iotDevice.getDeviceTypeCode());
        if (StringUtils.isBlank(device.getDataSource())) {
            device.setDataSource("IOT");
        }
        if (device.getStatus() == null) {
            device.setStatus(1);
        }
        if (device.getModelFlag() == null) {
            device.setModelFlag(1);
        }
        if (device.getDelFlag() == null) {
            device.setDelFlag(1);
        }
    }

    private AggregatorEnt requireScopedEnt(String aggregatorId, String entId) {
        if (StringUtils.isBlank(entId)) {
            throw new BaseException(StatusCode.C.getCode(), "企业不能为空");
        }
        AggregatorEnt ent = aggregatorEntService.getAggregatorEnt(entId);
        if (ent == null) {
            throw new BaseException(StatusCode.C.getCode(), "企业不存在");
        }
        if (StringUtils.isNotBlank(aggregatorId) && !StringUtils.equals(aggregatorId, ent.getAggregatorId())) {
            throw new BaseException(StatusCode.C.getCode(), "企业不属于当前聚合商");
        }
        loadScopeService.validateScope(ent.getAggregatorId(), ent.getEntId());
        return ent;
    }

    private AggregatorSingleModelData requireModelForDevice(String aggregatorId, String entId, String energyStationCode) {
        if (StringUtils.isBlank(energyStationCode)) {
            throw new BaseException(StatusCode.C.getCode(), "项目不能为空");
        }
        String stationCode = StringUtils.trim(energyStationCode);
        List<AggregatorSingleModelData> models = aggregatorSingleModelDataService.list(
                aggregatorId, entId, null, stationCode, null, null);
        AggregatorSingleModelData model = models.stream()
                .filter(item -> StringUtils.equals(item.getEnergyStationCode(), stationCode))
                .filter(item -> StringUtils.equals(item.getAggregatorId(), aggregatorId))
                .filter(item -> StringUtils.equals(item.getEntId(), entId))
                .findFirst()
                .orElse(null);
        if (model == null) {
            throw new BaseException(StatusCode.C.getCode(), "项目不存在或不属于当前企业");
        }
        return model;
    }

    private IotDevice requireIotDeviceForResource(String aggregatorId, String entId, String iotDeviceBaseId) {
        IotDevice iotDevice = iotDeviceManagementService.getDeviceByIdOrCode(aggregatorId, entId, iotDeviceBaseId);
        if (iotDevice == null) {
            throw new BaseException(StatusCode.C.getCode(), "物联设备不存在或不属于当前企业");
        }
        if (StringUtils.isBlank(iotDevice.getDeviceCode())) {
            throw new BaseException(StatusCode.C.getCode(), "物联设备编码不能为空");
        }
        return iotDevice;
    }

    private void bindExtraInfo(List<AggregatorEntDevice> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> entIds = list.stream()
                .map(AggregatorEntDevice::getEntId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> entNameMap = Collections.emptyMap();
        if (!entIds.isEmpty()) {
            List<AggregatorEnt> ents = aggregatorEntService.getAggregatorEntList(entIds);
            entNameMap = ents.stream().collect(Collectors.toMap(
                    AggregatorEnt::getEntId, AggregatorEnt::getEntName, (a, b) -> a));
        }

        List<String> stationCodes = list.stream()
                .map(AggregatorEntDevice::getEnergyStationCode)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> stationNameMap = Collections.emptyMap();
        Map<String, String> stationResourceTypeMap = Collections.emptyMap();
        if (!stationCodes.isEmpty()) {
            List<AggregatorSingleModelData> models = aggregatorSingleModelDataService.getByEnergyStationCodes(stationCodes);
            stationNameMap = models.stream().collect(Collectors.toMap(
                    AggregatorSingleModelData::getEnergyStationCode, AggregatorSingleModelData::getEnergyStation, (a, b) -> a));
            stationResourceTypeMap = models.stream().collect(Collectors.toMap(
                    AggregatorSingleModelData::getEnergyStationCode, AggregatorSingleModelData::getResourceTypeId, (a, b) -> a));
        }

        List<String> resourceTypeIds = list.stream()
                .map(AggregatorEntDevice::getResourceTypeId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (resourceTypeIds.isEmpty()) {
            resourceTypeIds = stationResourceTypeMap.values().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        }
        Map<String, String> resourceTypeNameMap = Collections.emptyMap();
        if (!resourceTypeIds.isEmpty()) {
            List<AggregatorResourceType> types = aggregatorResourceTypeService.getAggregatorResourceTypeList();
            resourceTypeNameMap = types.stream().collect(Collectors.toMap(
                    AggregatorResourceType::getId, AggregatorResourceType::getName, (a, b) -> a));
        }

        for (AggregatorEntDevice device : list) {
            device.setUsername(entNameMap.getOrDefault(device.getEntId(), device.getUsername()));
            if (device.getEnergyStation() == null && stationNameMap.containsKey(device.getEnergyStationCode())) {
                device.setEnergyStation(stationNameMap.get(device.getEnergyStationCode()));
            }
            String rtId = device.getResourceTypeId() != null ? device.getResourceTypeId() : stationResourceTypeMap.get(device.getEnergyStationCode());
            if (rtId != null) {
                device.setResourceTypeId(rtId);
                device.setResourceTypeName(resourceTypeNameMap.get(rtId));
            }
        }
    }

    private <T> PageResultVO<T> toPage(List<T> list, Integer pageIndex, Integer pageSize) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        PageResultVO<T> page = new PageResultVO<>();
        page.setList(list);
        page.setTotal((int) pageInfo.getTotal());
        page.setPageIndex(pageIndex);
        page.setPageSize(pageSize);
        return page;
    }

    private <T> PageResultVO<T> emptyPage(Integer pageIndex, Integer pageSize) {
        PageResultVO<T> page = new PageResultVO<>();
        page.setList(Collections.emptyList());
        page.setTotal(0);
        page.setPageIndex(pageIndex);
        page.setPageSize(pageSize);
        return page;
    }
}
