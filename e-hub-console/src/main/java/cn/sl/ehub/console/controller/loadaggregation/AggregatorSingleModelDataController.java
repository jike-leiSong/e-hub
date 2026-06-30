package cn.sl.ehub.console.controller.loadaggregation;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.auth.LoadAggregationScopeService;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.service.IAggregatorEntDeviceService;
import cn.sl.ehub.console.service.IAggregatorEntService;
import cn.sl.ehub.service.service.AggregatorSingleModelDataService;
import cn.sl.ehub.service.vo.AggregatorEnt;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorSingleModelData;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/model")
@RequiredArgsConstructor
@Api(tags = "模型数据管理")
public class AggregatorSingleModelDataController {

    private final AggregatorSingleModelDataService aggregatorSingleModelDataService;
    private final IAggregatorEntDeviceService aggregatorEntDeviceService;
    private final IAggregatorEntService aggregatorEntService;
    private final LoadAggregationScopeService loadScopeService;

    @GetMapping("/listByEnt")
    @ApiOperation("查询企业下的项目列表")
    public ResultVO<List<AggregatorSingleModelData>> listByEnt(@RequestParam("entId") String entId) {
        if (StringUtils.isBlank(entId)) {
            return ResultVO.success(Collections.emptyList());
        }
        List<String> stationCodes = aggregatorEntDeviceService.getEnergyStationCodesByEntId(entId);
        if (CollectionUtils.isEmpty(stationCodes)) {
            return ResultVO.success(Collections.emptyList());
        }
        List<AggregatorSingleModelData> models = aggregatorSingleModelDataService.list(
                null, null, null, null, stationCodes);
        return ResultVO.success(models);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询模型数据")
    public ResultVO<PageResultVO<AggregatorSingleModelData>> page(@RequestParam(value = "aggregatorId", required = false) String aggregatorId,
                                                                  @RequestParam(value = "entId", required = false) String entId,
                                                                  @RequestParam(value = "resourceTypeId", required = false) String resourceTypeId,
                                                                  @RequestParam(value = "energyStationCode", required = false) String energyStationCode,
                                                                  @RequestParam(value = "energyStation", required = false) String energyStation,
                                                                  @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                                                  @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        LoadAggregationScopeService.Scope scope = loadScopeService.resolveQueryScope(aggregatorId, entId);
        List<String> stationCodes = resolveStationCodes(scope, entId);
        if (stationCodes != null && stationCodes.isEmpty()) {
            return ResultVO.success(emptyPage(pageIndex, pageSize));
        }
        PageHelper.startPage(pageIndex, pageSize);
        List<AggregatorSingleModelData> list = aggregatorSingleModelDataService.list(
                scope.getAggregatorId(), resourceTypeId, energyStationCode, energyStation, stationCodes);
        bindEnterpriseInfo(list);
        return ResultVO.success(toPage(list, pageIndex, pageSize));
    }

    @GetMapping("/{id}")
    @ApiOperation("模型数据详情")
    public ResultVO<AggregatorSingleModelData> detail(@PathVariable("id") Integer id) {
        AggregatorSingleModelData data = requireModel(id);
        validateModelScope(data);
        bindEnterpriseInfo(Collections.singletonList(data));
        return ResultVO.success(data);
    }

    @PostMapping
    @ApiOperation("新增模型数据")
    public ResultVO<AggregatorSingleModelData> create(@RequestBody AggregatorSingleModelData data) {
        validateSaveReq(data, false);
        AggregatorEnt ent = requireEnt(data.getEntId());
        loadScopeService.validateScope(ent.getAggregatorId(), ent.getEntId());
        data.setAggregatorId(ent.getAggregatorId());
        data.setOwner(ent.getEntName());
        AggregatorSingleModelData saved = aggregatorSingleModelDataService.create(data);
        bindEnterpriseInfo(Collections.singletonList(saved));
        return ResultVO.success(saved);
    }

    @PutMapping("/{id}")
    @ApiOperation("修改模型数据")
    public ResultVO<AggregatorSingleModelData> update(@PathVariable("id") Integer id,
                                                      @RequestBody AggregatorSingleModelData data) {
        AggregatorSingleModelData existing = requireModel(id);
        validateModelScope(existing);
        validateSaveReq(data, true);
        AggregatorEnt ent = requireEnt(data.getEntId());
        loadScopeService.validateScope(ent.getAggregatorId(), ent.getEntId());
        if (StringUtils.isNotBlank(data.getEnergyStationCode())
                && aggregatorSingleModelDataService.existsByEnergyStationCode(data.getEnergyStationCode(), id)) {
            throw new BaseException(StatusCode.C.getCode(), "项目编码已存在");
        }
        data.setAggregatorId(ent.getAggregatorId());
        data.setOwner(ent.getEntName());
        AggregatorSingleModelData saved = aggregatorSingleModelDataService.update(id, data);
        bindEnterpriseInfo(Collections.singletonList(saved));
        return ResultVO.success(saved);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除模型数据")
    public ResultVO<Boolean> delete(@PathVariable("id") Integer id) {
        AggregatorSingleModelData existing = requireModel(id);
        validateModelScope(existing);
        aggregatorSingleModelDataService.delete(id);
        return ResultVO.success(true);
    }

    private void validateSaveReq(AggregatorSingleModelData data, boolean update) {
        if (data == null) {
            throw new BaseException(StatusCode.C.getCode(), "模型数据不能为空");
        }
        if (StringUtils.isBlank(data.getEntId())) {
            throw new BaseException(StatusCode.C.getCode(), "企业不能为空");
        }
        if (StringUtils.isBlank(data.getEnergyStation())) {
            throw new BaseException(StatusCode.C.getCode(), "项目名称不能为空");
        }
        if (StringUtils.isBlank(data.getResourceTypeId())) {
            throw new BaseException(StatusCode.C.getCode(), "资源类型不能为空");
        }
        if (!update && StringUtils.isNotBlank(data.getEnergyStationCode())
                && aggregatorSingleModelDataService.existsByEnergyStationCode(data.getEnergyStationCode(), null)) {
            throw new BaseException(StatusCode.C.getCode(), "项目编码已存在");
        }
    }

    private AggregatorSingleModelData requireModel(Integer id) {
        AggregatorSingleModelData data = aggregatorSingleModelDataService.getById(id);
        if (data == null) {
            throw new BaseException(StatusCode.C.getCode(), "模型数据不存在");
        }
        return data;
    }

    private AggregatorEnt requireEnt(String entId) {
        AggregatorEnt ent = aggregatorEntService.getAggregatorEnt(entId);
        if (ent == null) {
            throw new BaseException(StatusCode.C.getCode(), "企业不存在");
        }
        return ent;
    }

    private void validateModelScope(AggregatorSingleModelData data) {
        String entId = null;
        AggregatorEntDevice binding = findBinding(data.getEnergyStationCode());
        if (binding != null) {
            entId = binding.getEntId();
        }
        loadScopeService.validateScope(data.getAggregatorId(), entId);
    }

    private AggregatorEntDevice requireBinding(String energyStationCode) {
        AggregatorEntDevice binding = findBinding(energyStationCode);
        if (binding == null) {
            throw new BaseException(StatusCode.C.getCode(), "项目编码未绑定企业设备");
        }
        return binding;
    }

    private AggregatorEntDevice findBinding(String energyStationCode) {
        if (StringUtils.isBlank(energyStationCode)) {
            return null;
        }
        List<AggregatorEntDevice> bindings = aggregatorEntDeviceService.getDevicesByEnergyStationCodes(
                Collections.singletonList(energyStationCode));
        if (CollectionUtils.isEmpty(bindings)) {
            return null;
        }
        return bindings.stream().filter(item -> StringUtils.isNotBlank(item.getEntId())).findFirst().orElse(bindings.get(0));
    }

    private List<String> resolveStationCodes(LoadAggregationScopeService.Scope scope, String entId) {
        if (StringUtils.isNotBlank(scope.getEntId())) {
            return aggregatorEntDeviceService.getEnergyStationCodesByEntId(scope.getEntId());
        }
        if (StringUtils.isNotBlank(entId)) {
            return aggregatorEntDeviceService.getEnergyStationCodesByEntId(entId);
        }
        return null;
    }

    private void bindEnterpriseInfo(List<AggregatorSingleModelData> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> codes = list.stream()
                .map(AggregatorSingleModelData::getEnergyStationCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(codes)) {
            return;
        }
        List<AggregatorEntDevice> bindings = aggregatorEntDeviceService.getDevicesByEnergyStationCodes(codes);
        if (CollectionUtils.isEmpty(bindings)) {
            return;
        }
        Map<String, AggregatorEntDevice> deviceMap = new LinkedHashMap<>();
        for (AggregatorEntDevice item : bindings) {
            if (item == null || StringUtils.isBlank(item.getEnergyStationCode())) {
                continue;
            }
            deviceMap.putIfAbsent(item.getEnergyStationCode(), item);
        }
        List<String> entIds = deviceMap.values().stream()
                .map(AggregatorEntDevice::getEntId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, AggregatorEnt> entMap = aggregatorEntService.getAggregatorEntList(entIds).stream()
                .collect(Collectors.toMap(AggregatorEnt::getEntId, item -> item, (a, b) -> a));
        for (AggregatorSingleModelData item : list) {
            AggregatorEntDevice binding = deviceMap.get(item.getEnergyStationCode());
            if (binding == null) {
                continue;
            }
            item.setEntId(binding.getEntId());
            AggregatorEnt ent = entMap.get(binding.getEntId());
            item.setEntName(ent != null ? ent.getEntName() : null);
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
