package cn.sl.ehub.console.controller.iot;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.auth.LoadAggregationScopeService;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.service.IAggregatorEntService;
import cn.sl.ehub.console.service.IAggregatorResourceTypeService;
import cn.sl.ehub.service.dto.iot.IotDeviceExternalRefSaveReq;
import cn.sl.ehub.service.dto.iot.IotDevicePointSaveReq;
import cn.sl.ehub.service.dto.iot.IotDeviceQuery;
import cn.sl.ehub.service.dto.iot.IotDeviceSaveReq;
import cn.sl.ehub.service.dto.iot.IotPointExternalRefSaveReq;
import cn.sl.ehub.service.dto.iot.IotTelemetryMinuteQuery;
import cn.sl.ehub.service.service.AggregatorSingleModelDataService;
import cn.sl.ehub.service.service.IotDeviceService;
import cn.sl.ehub.service.vo.AggregatorEnt;
import cn.sl.ehub.service.vo.AggregatorResourceType;
import cn.sl.ehub.service.vo.AggregatorSingleModelData;
import cn.sl.ehub.service.vo.IotDevice;
import cn.sl.ehub.service.vo.IotDeviceExternalRef;
import cn.sl.ehub.service.vo.IotDevicePoint;
import cn.sl.ehub.service.vo.IotPointExternalRef;
import cn.sl.ehub.service.vo.IotTelemetryMinute;
import cn.sl.ehub.service.vo.IotUnmatchedTelemetryLog;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@RequestMapping("/iot")
@Api(tags = "IoT设备管理")
public class IotDeviceController {

    private final IotDeviceService iotDeviceService;
    private final LoadAggregationScopeService loadScopeService;
    private final IAggregatorEntService aggregatorEntService;
    private final IAggregatorResourceTypeService aggregatorResourceTypeService;
    private final AggregatorSingleModelDataService aggregatorSingleModelDataService;

    public IotDeviceController(IotDeviceService iotDeviceService,
                                LoadAggregationScopeService loadScopeService,
                                IAggregatorEntService aggregatorEntService,
                                IAggregatorResourceTypeService aggregatorResourceTypeService,
                                AggregatorSingleModelDataService aggregatorSingleModelDataService) {
        this.iotDeviceService = iotDeviceService;
        this.loadScopeService = loadScopeService;
        this.aggregatorEntService = aggregatorEntService;
        this.aggregatorResourceTypeService = aggregatorResourceTypeService;
        this.aggregatorSingleModelDataService = aggregatorSingleModelDataService;
    }

    @GetMapping("/devices")
    @ApiOperation("设备列表")
    public ResultVO<PageResultVO<IotDevice>> listDevices(@RequestParam(value = "aggregatorId", required = false) String aggregatorId,
                                                         @RequestParam(value = "entId", required = false) String entId,
                                                         @RequestParam(value = "projectId", required = false) String projectId,
                                                         @RequestParam(value = "energyStation", required = false) String energyStation,
                                                         @RequestParam(value = "deviceCode", required = false) String deviceCode,
                                                         @RequestParam(value = "deviceName", required = false) String deviceName,
                                                         @RequestParam(value = "deviceTypeCode", required = false) String deviceTypeCode,
                                                         @RequestParam(value = "assetStatus", required = false) Integer assetStatus,
                                                         @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
                                                         @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                                         @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        IotDeviceQuery query = new IotDeviceQuery();
        query.setAggregatorId(aggregatorId);
        query.setEntId(entId);
        query.setProjectId(projectId);
        query.setEnergyStation(energyStation);
        query.setDeviceCode(deviceCode);
        query.setDeviceName(deviceName);
        query.setDeviceTypeCode(deviceTypeCode);
        query.setAssetStatus(assetStatus);
        query.setOnlineStatus(onlineStatus);
        applyDataScope(query);
        if (StringUtils.isNotBlank(energyStation)) {
            List<AggregatorSingleModelData> models = aggregatorSingleModelDataService.list(
                    null, null, null, energyStation, null);
            List<String> stationCodes = models.stream()
                    .map(AggregatorSingleModelData::getEnergyStationCode)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (stationCodes.isEmpty()) {
                return ResultVO.success(emptyPage(pageIndex, pageSize));
            }
            query.setProjectId(null);
            query.setEnergyStation(null);
            PageHelper.startPage(pageIndex, pageSize);
            List<IotDevice> list = iotDeviceService.listDevices(query);
            list = list.stream()
                    .filter(d -> stationCodes.contains(d.getProjectId()))
                    .collect(Collectors.toList());
            bindDeviceExtraInfo(list);
            return ResultVO.success(toPage(list, pageIndex, pageSize));
        }
        query.setEnergyStation(null);
        PageHelper.startPage(pageIndex, pageSize);
        List<IotDevice> list = iotDeviceService.listDevices(query);
        bindDeviceExtraInfo(list);
        return ResultVO.success(toPage(list, pageIndex, pageSize));
    }

    @GetMapping("/devices/{id}")
    @ApiOperation("设备详情")
    public ResultVO<IotDevice> getDevice(@PathVariable("id") Long id) {
        return ResultVO.success(validateDeviceScope(id));
    }

    @PostMapping("/devices")
    @ApiOperation("新增设备")
    public ResultVO<IotDevice> createDevice(@RequestBody IotDeviceSaveReq req) {
        fillDeviceReqScope(req);
        return ResultVO.success(iotDeviceService.createDevice(req));
    }

    @PutMapping("/devices/{id}")
    @ApiOperation("更新设备")
    public ResultVO<IotDevice> updateDevice(@PathVariable("id") Long id, @RequestBody IotDeviceSaveReq req) {
        validateDeviceScope(id);
        fillDeviceReqScope(req);
        return ResultVO.success(iotDeviceService.updateDevice(id, req));
    }

    @DeleteMapping("/devices/{id}")
    @ApiOperation("删除设备")
    public ResultVO<Boolean> deleteDevice(@PathVariable("id") Long id) {
        validateDeviceScope(id);
        iotDeviceService.deleteDevice(id);
        return ResultVO.success(true);
    }

    @GetMapping("/devices/{deviceId}/points")
    @ApiOperation("设备测点列表")
    public ResultVO<List<IotDevicePoint>> listPoints(@PathVariable("deviceId") Long deviceId) {
        validateDeviceScope(deviceId);
        return ResultVO.success(iotDeviceService.listPoints(deviceId));
    }

    @PostMapping("/devices/{deviceId}/points")
    @ApiOperation("新增设备测点")
    public ResultVO<IotDevicePoint> createPoint(@PathVariable("deviceId") Long deviceId,
                                                @RequestBody IotDevicePointSaveReq req) {
        validateDeviceScope(deviceId);
        return ResultVO.success(iotDeviceService.createPoint(deviceId, req));
    }

    @PutMapping("/points/{id}")
    @ApiOperation("更新设备测点")
    public ResultVO<IotDevicePoint> updatePoint(@PathVariable("id") Long id,
                                                @RequestBody IotDevicePointSaveReq req) {
        validatePointScope(id);
        return ResultVO.success(iotDeviceService.updatePoint(id, req));
    }

    @DeleteMapping("/points/{id}")
    @ApiOperation("删除设备测点")
    public ResultVO<Boolean> deletePoint(@PathVariable("id") Long id) {
        validatePointScope(id);
        iotDeviceService.deletePoint(id);
        return ResultVO.success(true);
    }

    @GetMapping("/devices/{deviceId}/external-refs")
    @ApiOperation("设备三方绑定列表")
    public ResultVO<List<IotDeviceExternalRef>> listDeviceExternalRefs(@PathVariable("deviceId") Long deviceId) {
        validateDeviceScope(deviceId);
        return ResultVO.success(iotDeviceService.listDeviceExternalRefs(deviceId));
    }

    @PostMapping("/devices/{deviceId}/external-refs")
    @ApiOperation("新增设备三方绑定")
    public ResultVO<IotDeviceExternalRef> createDeviceExternalRef(@PathVariable("deviceId") Long deviceId,
                                                                  @RequestBody IotDeviceExternalRefSaveReq req) {
        IotDevice device = validateDeviceScope(deviceId);
        if (req != null) {
            req.setEntId(device.getEntId());
        }
        return ResultVO.success(iotDeviceService.createDeviceExternalRef(deviceId, req));
    }

    @PutMapping("/device-external-refs/{id}")
    @ApiOperation("更新设备三方绑定")
    public ResultVO<IotDeviceExternalRef> updateDeviceExternalRef(@PathVariable("id") Long id,
                                                                  @RequestBody IotDeviceExternalRefSaveReq req) {
        IotDeviceExternalRef ref = validateDeviceExternalRefScope(id);
        if (req != null) {
            req.setEntId(ref.getEntId());
            req.setDeviceId(ref.getDeviceId());
        }
        return ResultVO.success(iotDeviceService.updateDeviceExternalRef(id, req));
    }

    @DeleteMapping("/device-external-refs/{id}")
    @ApiOperation("停用设备三方绑定")
    public ResultVO<Boolean> disableDeviceExternalRef(@PathVariable("id") Long id) {
        validateDeviceExternalRefScope(id);
        iotDeviceService.disableDeviceExternalRef(id);
        return ResultVO.success(true);
    }

    @GetMapping("/points/{pointId}/external-refs")
    @ApiOperation("测点三方绑定列表")
    public ResultVO<List<IotPointExternalRef>> listPointExternalRefs(@PathVariable("pointId") Long pointId) {
        validatePointScope(pointId);
        return ResultVO.success(iotDeviceService.listPointExternalRefs(pointId));
    }

    @PostMapping("/points/{pointId}/external-refs")
    @ApiOperation("新增测点三方绑定")
    public ResultVO<IotPointExternalRef> createPointExternalRef(@PathVariable("pointId") Long pointId,
                                                                @RequestBody IotPointExternalRefSaveReq req) {
        validatePointScope(pointId);
        return ResultVO.success(iotDeviceService.createPointExternalRef(pointId, req));
    }

    @PutMapping("/point-external-refs/{id}")
    @ApiOperation("更新测点三方绑定")
    public ResultVO<IotPointExternalRef> updatePointExternalRef(@PathVariable("id") Long id,
                                                                @RequestBody IotPointExternalRefSaveReq req) {
        IotPointExternalRef ref = validatePointExternalRefScope(id);
        if (req != null) {
            req.setDeviceId(ref.getDeviceId());
            req.setPointId(ref.getPointId());
        }
        return ResultVO.success(iotDeviceService.updatePointExternalRef(id, req));
    }

    @DeleteMapping("/point-external-refs/{id}")
    @ApiOperation("停用测点三方绑定")
    public ResultVO<Boolean> disablePointExternalRef(@PathVariable("id") Long id) {
        validatePointExternalRefScope(id);
        iotDeviceService.disablePointExternalRef(id);
        return ResultVO.success(true);
    }

    @GetMapping("/telemetry/minute")
    @ApiOperation("分钟测点数据")
    public ResultVO<PageResultVO<IotTelemetryMinute>> listTelemetryMinute(@RequestParam(value = "aggregatorId", required = false) String aggregatorId,
                                                                          @RequestParam(value = "entId", required = false) String entId,
                                                                          @RequestParam(value = "projectId", required = false) String projectId,
                                                                          @RequestParam(value = "deviceId", required = false) Long deviceId,
                                                                          @RequestParam(value = "deviceCode", required = false) String deviceCode,
                                                                          @RequestParam(value = "pointCode", required = false) String pointCode,
                                                                          @RequestParam(value = "startTime", required = false) String startTime,
                                                                          @RequestParam(value = "endTime", required = false) String endTime,
                                                                          @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                                                          @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {
        IotTelemetryMinuteQuery query = new IotTelemetryMinuteQuery();
        query.setAggregatorId(aggregatorId);
        query.setEntId(entId);
        query.setProjectId(projectId);
        query.setDeviceId(deviceId);
        query.setDeviceCode(deviceCode);
        query.setPointCode(pointCode);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        applyTelemetryScope(query);
        PageHelper.startPage(pageIndex, pageSize);
        List<IotTelemetryMinute> list = iotDeviceService.listTelemetryMinute(query);
        return ResultVO.success(toPage(list, pageIndex, pageSize));
    }

    @GetMapping("/unmatched-telemetry")
    @ApiOperation("未匹配测点数据")
    public ResultVO<PageResultVO<IotUnmatchedTelemetryLog>> listUnmatchedTelemetry(@RequestParam(value = "sourceCode", required = false) String sourceCode,
                                                                                   @RequestParam(value = "handled", required = false) Integer handled,
                                                                                   @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                                                                   @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        PageHelper.startPage(pageIndex, pageSize);
        List<IotUnmatchedTelemetryLog> list = iotDeviceService.listUnmatchedTelemetry(sourceCode, handled);
        return ResultVO.success(toPage(list, pageIndex, pageSize));
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

    private void applyDataScope(IotDeviceQuery query) {
        LoadAggregationScopeService.Scope scope = loadScopeService.resolveQueryScope(
                query.getAggregatorId(), query.getEntId());
        query.setAggregatorId(scope.getAggregatorId());
        query.setEntId(scope.getEntId());
    }

    private void applyTelemetryScope(IotTelemetryMinuteQuery query) {
        LoadAggregationScopeService.Scope scope = loadScopeService.resolveQueryScope(
                query.getAggregatorId(), query.getEntId());
        query.setAggregatorId(scope.getAggregatorId());
        query.setEntId(scope.getEntId());
    }

    private void fillDeviceReqScope(IotDeviceSaveReq req) {
        if (req == null) {
            return;
        }
        LoadAggregationScopeService.Scope scope = loadScopeService.resolveQueryScope(
                req.getAggregatorId(), req.getEntId());
        req.setAggregatorId(scope.getAggregatorId());
        req.setEntId(scope.getEntId());
    }

    private IotDevice validateDeviceScope(Long deviceId) {
        IotDevice device = iotDeviceService.getDevice(deviceId);
        if (device == null) {
            throw new BaseException(StatusCode.C.getCode(), "设备不存在");
        }
        loadScopeService.validateScope(device.getAggregatorId(), device.getEntId());
        return device;
    }

    private IotDevicePoint validatePointScope(Long pointId) {
        IotDevicePoint point = iotDeviceService.getPoint(pointId);
        if (point == null) {
            throw new BaseException(StatusCode.C.getCode(), "测点不存在");
        }
        validateDeviceScope(point.getDeviceId());
        return point;
    }

    private IotDeviceExternalRef validateDeviceExternalRefScope(Long id) {
        IotDeviceExternalRef ref = iotDeviceService.getDeviceExternalRef(id);
        if (ref == null) {
            throw new BaseException(StatusCode.C.getCode(), "三方设备绑定不存在");
        }
        loadScopeService.validateScope(null, ref.getEntId());
        return ref;
    }

    private IotPointExternalRef validatePointExternalRefScope(Long id) {
        IotPointExternalRef ref = iotDeviceService.getPointExternalRef(id);
        if (ref == null) {
            throw new BaseException(StatusCode.C.getCode(), "三方测点绑定不存在");
        }
        validateDeviceScope(ref.getDeviceId());
        return ref;
    }

    private void bindDeviceExtraInfo(List<IotDevice> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<String> entIds = list.stream()
                .map(IotDevice::getEntId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> entNameMap = Collections.emptyMap();
        if (!entIds.isEmpty()) {
            List<AggregatorEnt> ents = aggregatorEntService.getAggregatorEntList(entIds);
            entNameMap = ents.stream().collect(Collectors.toMap(
                    AggregatorEnt::getEntId, AggregatorEnt::getEntName, (a, b) -> a));
        }

        List<String> projectIds = list.stream()
                .map(IotDevice::getProjectId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<String, AggregatorSingleModelData> modelMap = Collections.emptyMap();
        if (!projectIds.isEmpty()) {
            List<AggregatorSingleModelData> models = aggregatorSingleModelDataService.getByEnergyStationCodes(projectIds);
            modelMap = models.stream().collect(Collectors.toMap(
                    AggregatorSingleModelData::getEnergyStationCode, m -> m, (a, b) -> a));
        }

        List<String> resourceTypeIds = modelMap.values().stream()
                .map(AggregatorSingleModelData::getResourceTypeId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> resourceTypeNameMap = Collections.emptyMap();
        if (!resourceTypeIds.isEmpty()) {
            List<AggregatorResourceType> types = aggregatorResourceTypeService.getAggregatorResourceTypeList();
            resourceTypeNameMap = types.stream().collect(Collectors.toMap(
                    AggregatorResourceType::getId, AggregatorResourceType::getName, (a, b) -> a));
        }

        for (IotDevice device : list) {
            device.setEntName(entNameMap.get(device.getEntId()));
            AggregatorSingleModelData model = modelMap.get(device.getProjectId());
            if (model != null) {
                device.setEnergyStation(model.getEnergyStation());
                device.setResourceTypeName(resourceTypeNameMap.get(model.getResourceTypeId()));
            }
        }
    }

}
