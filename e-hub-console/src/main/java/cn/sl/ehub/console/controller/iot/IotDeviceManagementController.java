package cn.sl.ehub.console.controller.iot;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.auth.LoadAggregationScopeService;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.service.dto.iot.IotDeviceGroupSaveReq;
import cn.sl.ehub.service.dto.iot.IotDevicePointSaveReq;
import cn.sl.ehub.service.dto.iot.IotDeviceSaveReq;
import cn.sl.ehub.service.dto.iot.IotPointDefinitionSaveReq;
import cn.sl.ehub.service.service.IotDeviceManagementService;
import cn.sl.ehub.service.vo.IotDevice;
import cn.sl.ehub.service.vo.IotDeviceGroup;
import cn.sl.ehub.service.vo.IotDeviceGroupParam;
import cn.sl.ehub.service.vo.IotDeviceGroupPoint;
import cn.sl.ehub.service.vo.IotDeviceGroupPointDefinition;
import cn.sl.ehub.service.vo.IotDevicePoint;
import cn.sl.ehub.service.vo.IotDevicePointDefinition;
import cn.sl.ehub.service.vo.IotDeviceTypeParamMetadata;
import cn.sl.ehub.service.vo.IotDeviceTypePointMetadata;
import cn.sl.ehub.service.vo.IotGateway;
import cn.sl.ehub.service.vo.IotOptionVO;
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

@RestController
@RequestMapping("/iot/manage")
@RequiredArgsConstructor
@Api(tags = "IoT设备管理新域")
public class IotDeviceManagementController {

    private final IotDeviceManagementService iotDeviceManagementService;
    private final LoadAggregationScopeService loadScopeService;

    @GetMapping("/options/device-types")
    @ApiOperation("设备类型选项")
    public ResultVO<List<IotOptionVO>> deviceTypes() {
        return ResultVO.success(iotDeviceManagementService.listDeviceTypes());
    }

    @GetMapping("/options/communication-methods")
    @ApiOperation("通讯方式选项")
    public ResultVO<List<IotOptionVO>> communicationMethods() {
        return ResultVO.success(iotDeviceManagementService.listCommunicationMethods());
    }

    @GetMapping("/options/device-group-types")
    @ApiOperation("设备组类型选项")
    public ResultVO<List<IotOptionVO>> deviceGroupTypes() {
        return ResultVO.success(iotDeviceManagementService.listDeviceGroupTypes());
    }

    @GetMapping("/options/energy-types")
    @ApiOperation("能源类型选项")
    public ResultVO<List<IotOptionVO>> energyTypes() {
        return ResultVO.success(iotDeviceManagementService.listEnergyTypes());
    }

    @GetMapping("/options/carriers")
    @ApiOperation("运营商选项")
    public ResultVO<List<IotOptionVO>> carriers() {
        return ResultVO.success(iotDeviceManagementService.listCarriers());
    }

    @GetMapping("/options/third-party-apis")
    @ApiOperation("第三方API选项")
    public ResultVO<List<IotOptionVO>> thirdPartyApis() {
        return ResultVO.success(iotDeviceManagementService.listThirdPartyApis());
    }

    @GetMapping("/gateways")
    @ApiOperation("网关列表")
    public ResultVO<List<IotGateway>> gateways(@RequestParam(value = "aggregatorId", required = false) String aggregatorId,
                                               @RequestParam(value = "entId", required = false) String entId) {
        LoadAggregationScopeService.Scope scope = requireEntScope(aggregatorId, entId);
        return ResultVO.success(iotDeviceManagementService.listGateways(scope.getAggregatorId(), scope.getEntId()));
    }

    @GetMapping("/device-groups")
    @ApiOperation("设备组列表")
    public ResultVO<List<IotDeviceGroup>> deviceGroups(@RequestParam(value = "aggregatorId", required = false) String aggregatorId,
                                                       @RequestParam(value = "entId", required = false) String entId) {
        LoadAggregationScopeService.Scope scope = requireEntScope(aggregatorId, entId);
        return ResultVO.success(iotDeviceManagementService.listDeviceGroups(scope.getAggregatorId(), scope.getEntId()));
    }

    @GetMapping("/device-groups/{id}")
    @ApiOperation("设备组详情")
    public ResultVO<IotDeviceGroup> deviceGroupDetail(@PathVariable("id") Long id) {
        IotDeviceGroup group = requireDeviceGroup(id);
        loadScopeService.validateScope(group.getAggregatorId(), group.getEntId());
        return ResultVO.success(group);
    }

    @PostMapping("/device-groups")
    @ApiOperation("新增设备组")
    public ResultVO<IotDeviceGroup> createDeviceGroup(@RequestBody IotDeviceGroupSaveReq req) {
        LoadAggregationScopeService.Scope scope = requireEntScope(req == null ? null : req.getAggregatorId(),
                req == null ? null : req.getEntId());
        req.setAggregatorId(scope.getAggregatorId());
        req.setEntId(scope.getEntId());
        return ResultVO.success(iotDeviceManagementService.saveDeviceGroup(req));
    }

    @PutMapping("/device-groups/{id}")
    @ApiOperation("更新设备组")
    public ResultVO<IotDeviceGroup> updateDeviceGroup(@PathVariable("id") Long id,
                                                      @RequestBody IotDeviceGroupSaveReq req) {
        IotDeviceGroup group = requireDeviceGroup(id);
        loadScopeService.validateScope(group.getAggregatorId(), group.getEntId());
        if (req == null) {
            throw new BaseException(StatusCode.C.getCode(), "设备组数据不能为空");
        }
        req.setAggregatorId(group.getAggregatorId());
        req.setEntId(group.getEntId());
        return ResultVO.success(iotDeviceManagementService.updateDeviceGroup(id, req));
    }

    @DeleteMapping("/device-groups/{id}")
    @ApiOperation("删除设备组")
    public ResultVO<Boolean> deleteDeviceGroup(@PathVariable("id") Long id) {
        IotDeviceGroup group = requireDeviceGroup(id);
        loadScopeService.validateScope(group.getAggregatorId(), group.getEntId());
        iotDeviceManagementService.deleteDeviceGroup(id);
        return ResultVO.success(true);
    }

    @GetMapping("/devices")
    @ApiOperation("设备列表分页")
    public ResultVO<PageResultVO<IotDevice>> devices(@RequestParam(value = "aggregatorId", required = false) String aggregatorId,
                                                     @RequestParam(value = "entId", required = false) String entId,
                                                     @RequestParam(value = "deviceGroupId", required = false) Long deviceGroupId,
                                                     @RequestParam(value = "deviceTypeCode", required = false) String deviceTypeCode,
                                                     @RequestParam(value = "deviceName", required = false) String deviceName,
                                                     @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                     @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        LoadAggregationScopeService.Scope scope = requireEntScope(aggregatorId, entId);
        PageHelper.startPage(pageNum, pageSize);
        List<IotDevice> list = iotDeviceManagementService.listDevices(
                scope.getAggregatorId(), scope.getEntId(), deviceGroupId, deviceTypeCode, deviceName);
        return ResultVO.success(toPage(list, pageNum, pageSize));
    }

    @GetMapping("/devices/{id}")
    @ApiOperation("设备详情")
    public ResultVO<IotDevice> deviceDetail(@PathVariable("id") Long id) {
        IotDevice device = requireDevice(id);
        loadScopeService.validateScope(device.getAggregatorId(), device.getEntId());
        return ResultVO.success(device);
    }

    @PostMapping("/devices")
    @ApiOperation("新增设备")
    public ResultVO<IotDevice> createDevice(@RequestBody IotDeviceSaveReq req) {
        LoadAggregationScopeService.Scope scope = requireEntScope(req == null ? null : req.getAggregatorId(),
                req == null ? null : req.getEntId());
        req.setAggregatorId(scope.getAggregatorId());
        req.setEntId(scope.getEntId());
        return ResultVO.success(iotDeviceManagementService.saveManagedDevice(req));
    }

    @PutMapping("/devices/{id}")
    @ApiOperation("更新设备")
    public ResultVO<IotDevice> updateDevice(@PathVariable("id") Long id,
                                            @RequestBody IotDeviceSaveReq req) {
        IotDevice device = requireDevice(id);
        loadScopeService.validateScope(device.getAggregatorId(), device.getEntId());
        if (req == null) {
            throw new BaseException(StatusCode.C.getCode(), "设备数据不能为空");
        }
        req.setAggregatorId(device.getAggregatorId());
        req.setEntId(device.getEntId());
        return ResultVO.success(iotDeviceManagementService.updateManagedDevice(id, req));
    }

    @DeleteMapping("/devices/{id}")
    @ApiOperation("删除设备")
    public ResultVO<Boolean> deleteDevice(@PathVariable("id") Long id) {
        IotDevice device = requireDevice(id);
        loadScopeService.validateScope(device.getAggregatorId(), device.getEntId());
        iotDeviceManagementService.deleteManagedDevice(id);
        return ResultVO.success(true);
    }

    @GetMapping("/device-type-param-metadata")
    @ApiOperation("设备类型参数元数据")
    public ResultVO<List<IotDeviceTypeParamMetadata>> deviceTypeParamMetadata(@RequestParam(value = "deviceTypeCode", required = false) String deviceTypeCode) {
        return ResultVO.success(iotDeviceManagementService.listDeviceParamMetadata(deviceTypeCode));
    }

    @GetMapping("/device-type-point-metadata")
    @ApiOperation("设备类型测点元数据")
    public ResultVO<List<IotDeviceTypePointMetadata>> deviceTypePointMetadata(@RequestParam(value = "deviceTypeCode", required = false) String deviceTypeCode) {
        return ResultVO.success(iotDeviceManagementService.listDevicePointMetadata(deviceTypeCode));
    }

    @GetMapping("/device-group-param-metadata")
    @ApiOperation("设备组参数元数据")
    public ResultVO<List<IotDeviceGroupParam>> deviceGroupParamMetadata() {
        return ResultVO.success(iotDeviceManagementService.listDeviceGroupParamMetadata());
    }

    @GetMapping("/devices/{deviceId}/points/page")
    @ApiOperation("设备测点分页")
    public ResultVO<PageResultVO<IotDevicePoint>> devicePoints(@PathVariable("deviceId") Long deviceId,
                                                               @RequestParam(value = "pointQuery", required = false) String pointQuery,
                                                               @RequestParam(value = "dataType", required = false) String dataType,
                                                               @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                               @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        IotDevice device = requireDevice(deviceId);
        loadScopeService.validateScope(device.getAggregatorId(), device.getEntId());
        PageHelper.startPage(pageNum, pageSize);
        List<IotDevicePoint> list = iotDeviceManagementService.listDevicePoints(deviceId, pointQuery, dataType);
        bindPointAliases(list);
        return ResultVO.success(toPage(list, pageNum, pageSize));
    }

    @GetMapping("/devices/{deviceId}/available-points")
    @ApiOperation("设备可添加测点")
    public ResultVO<List<IotDeviceTypePointMetadata>> availablePoints(@PathVariable("deviceId") Long deviceId) {
        IotDevice device = requireDevice(deviceId);
        loadScopeService.validateScope(device.getAggregatorId(), device.getEntId());
        return ResultVO.success(iotDeviceManagementService.listAvailablePoints(deviceId));
    }

    @PostMapping("/devices/{deviceId}/points/batch")
    @ApiOperation("批量新增设备测点")
    public ResultVO<Boolean> batchAddPoints(@PathVariable("deviceId") Long deviceId,
                                            @RequestBody List<IotDevicePointSaveReq> reqList) {
        IotDevice device = requireDevice(deviceId);
        loadScopeService.validateScope(device.getAggregatorId(), device.getEntId());
        iotDeviceManagementService.batchAddDevicePoints(deviceId, reqList);
        return ResultVO.success(true);
    }

    @DeleteMapping("/points/{id}")
    @ApiOperation("删除设备测点")
    public ResultVO<Boolean> deletePoint(@PathVariable("id") Long id) {
        IotDevicePoint point = requirePoint(id);
        IotDevice device = requireDevice(point.getDeviceId());
        loadScopeService.validateScope(device.getAggregatorId(), device.getEntId());
        iotDeviceManagementService.deleteDevicePoint(id);
        return ResultVO.success(true);
    }

    @GetMapping("/points/{id}/definitions")
    @ApiOperation("设备测点状态值维护列表")
    public ResultVO<List<IotDevicePointDefinition>> pointDefinitions(@PathVariable("id") Long id) {
        IotDevicePoint point = requirePoint(id);
        IotDevice device = requireDevice(point.getDeviceId());
        loadScopeService.validateScope(device.getAggregatorId(), device.getEntId());
        return ResultVO.success(iotDeviceManagementService.listDevicePointDefinitions(id));
    }

    @PostMapping("/points/{id}/definitions")
    @ApiOperation("新增设备测点状态值")
    public ResultVO<IotDevicePointDefinition> savePointDefinition(@PathVariable("id") Long id,
                                                                  @RequestBody IotPointDefinitionSaveReq req) {
        IotDevicePoint point = requirePoint(id);
        IotDevice device = requireDevice(point.getDeviceId());
        loadScopeService.validateScope(device.getAggregatorId(), device.getEntId());
        return ResultVO.success(iotDeviceManagementService.saveDevicePointDefinition(id, req));
    }

    @PutMapping("/point-definitions/{id}")
    @ApiOperation("更新设备测点状态值")
    public ResultVO<IotDevicePointDefinition> updatePointDefinition(@PathVariable("id") Long id,
                                                                    @RequestBody IotPointDefinitionSaveReq req) {
        IotDevicePointDefinition definition = iotDeviceManagementService.getDevicePointDefinition(id);
        if (definition == null) {
            throw new BaseException(StatusCode.C.getCode(), "状态值定义不存在");
        }
        IotDevicePoint point = requirePoint(definition.getDevicePointId());
        IotDevice device = requireDevice(point.getDeviceId());
        loadScopeService.validateScope(device.getAggregatorId(), device.getEntId());
        return ResultVO.success(iotDeviceManagementService.updateDevicePointDefinition(id, req));
    }

    @DeleteMapping("/point-definitions/{id}")
    @ApiOperation("删除设备测点状态值")
    public ResultVO<Boolean> deletePointDefinition(@PathVariable("id") Long id) {
        IotDevicePointDefinition definition = iotDeviceManagementService.getDevicePointDefinition(id);
        if (definition == null) {
            throw new BaseException(StatusCode.C.getCode(), "状态值定义不存在");
        }
        IotDevicePoint point = requirePoint(definition.getDevicePointId());
        IotDevice device = requireDevice(point.getDeviceId());
        loadScopeService.validateScope(device.getAggregatorId(), device.getEntId());
        iotDeviceManagementService.deleteDevicePointDefinition(id);
        return ResultVO.success(true);
    }

    @GetMapping("/device-groups/{id}/points")
    @ApiOperation("设备组测点列表")
    public ResultVO<List<IotDeviceGroupPoint>> deviceGroupPoints(@PathVariable("id") Long id) {
        IotDeviceGroup group = requireDeviceGroup(id);
        loadScopeService.validateScope(group.getAggregatorId(), group.getEntId());
        return ResultVO.success(iotDeviceManagementService.listDeviceGroupPoints(id));
    }

    @GetMapping("/group-points/{id}/definitions")
    @ApiOperation("设备组测点状态值维护列表")
    public ResultVO<List<IotDeviceGroupPointDefinition>> groupPointDefinitions(@PathVariable("id") Long id) {
        IotDeviceGroupPoint point = requireGroupPoint(id);
        IotDeviceGroup group = requireDeviceGroup(point.getDeviceGroupId());
        loadScopeService.validateScope(group.getAggregatorId(), group.getEntId());
        return ResultVO.success(iotDeviceManagementService.listDeviceGroupPointDefinitions(id));
    }

    @PostMapping("/group-points/{id}/definitions")
    @ApiOperation("新增设备组测点状态值")
    public ResultVO<IotDeviceGroupPointDefinition> saveGroupPointDefinition(@PathVariable("id") Long id,
                                                                            @RequestBody IotPointDefinitionSaveReq req) {
        IotDeviceGroupPoint point = requireGroupPoint(id);
        IotDeviceGroup group = requireDeviceGroup(point.getDeviceGroupId());
        loadScopeService.validateScope(group.getAggregatorId(), group.getEntId());
        return ResultVO.success(iotDeviceManagementService.saveDeviceGroupPointDefinition(id, req));
    }

    @PutMapping("/group-point-definitions/{id}")
    @ApiOperation("更新设备组测点状态值")
    public ResultVO<IotDeviceGroupPointDefinition> updateGroupPointDefinition(@PathVariable("id") Long id,
                                                                              @RequestBody IotPointDefinitionSaveReq req) {
        IotDeviceGroupPointDefinition definition = iotDeviceManagementService.getDeviceGroupPointDefinition(id);
        if (definition == null) {
            throw new BaseException(StatusCode.C.getCode(), "设备组状态值定义不存在");
        }
        IotDeviceGroupPoint point = requireGroupPoint(definition.getDeviceGroupPointId());
        IotDeviceGroup group = requireDeviceGroup(point.getDeviceGroupId());
        loadScopeService.validateScope(group.getAggregatorId(), group.getEntId());
        return ResultVO.success(iotDeviceManagementService.updateDeviceGroupPointDefinition(id, req));
    }

    @DeleteMapping("/group-point-definitions/{id}")
    @ApiOperation("删除设备组测点状态值")
    public ResultVO<Boolean> deleteGroupPointDefinition(@PathVariable("id") Long id) {
        IotDeviceGroupPointDefinition definition = iotDeviceManagementService.getDeviceGroupPointDefinition(id);
        if (definition == null) {
            throw new BaseException(StatusCode.C.getCode(), "设备组状态值定义不存在");
        }
        IotDeviceGroupPoint point = requireGroupPoint(definition.getDeviceGroupPointId());
        IotDeviceGroup group = requireDeviceGroup(point.getDeviceGroupId());
        loadScopeService.validateScope(group.getAggregatorId(), group.getEntId());
        iotDeviceManagementService.deleteDeviceGroupPointDefinition(id);
        return ResultVO.success(true);
    }

    private LoadAggregationScopeService.Scope requireEntScope(String aggregatorId, String entId) {
        LoadAggregationScopeService.Scope scope = loadScopeService.resolveQueryScope(aggregatorId, entId);
        if (StringUtils.isBlank(scope.getEntId())) {
            throw new BaseException(StatusCode.C.getCode(), "请选择企业");
        }
        return scope;
    }

    private IotDeviceGroup requireDeviceGroup(Long id) {
        IotDeviceGroup group = iotDeviceManagementService.getDeviceGroup(id);
        if (group == null) {
            throw new BaseException(StatusCode.C.getCode(), "设备组不存在");
        }
        return group;
    }

    private IotDevice requireDevice(Long id) {
        IotDevice device = iotDeviceManagementService.getManagedDevice(id);
        if (device == null) {
            throw new BaseException(StatusCode.C.getCode(), "设备不存在");
        }
        return device;
    }

    private IotDevicePoint requirePoint(Long id) {
        IotDevicePoint point = iotDeviceManagementService.getDevicePoint(id);
        if (point == null) {
            throw new BaseException(StatusCode.C.getCode(), "测点不存在");
        }
        return point;
    }

    private IotDeviceGroupPoint requireGroupPoint(Long id) {
        IotDeviceGroupPoint point = iotDeviceManagementService.getDeviceGroupPoint(id);
        if (point == null) {
            throw new BaseException(StatusCode.C.getCode(), "设备组测点不存在");
        }
        return point;
    }

    private void bindPointAliases(List<IotDevicePoint> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (IotDevicePoint point : list) {
            point.setPropertyCode(point.getPointCode());
            point.setPropertyName(point.getPointName());
            point.setReadWriteRoleName(StringUtils.equalsIgnoreCase("readWrite", point.getReadWriteRole()) ? "读写" : "只读");
            point.setUpWayName("周期上报");
            point.setUpPeriodName(point.getDataFrequency() == null ? "--" : point.getDataFrequency() + "s");
        }
    }

    private <T> PageResultVO<T> toPage(List<T> list, Integer pageNum, Integer pageSize) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        PageResultVO<T> page = new PageResultVO<>();
        page.setList(list);
        page.setTotal((int) pageInfo.getTotal());
        page.setPageIndex(pageNum);
        page.setPageSize(pageSize);
        return page;
    }
}
