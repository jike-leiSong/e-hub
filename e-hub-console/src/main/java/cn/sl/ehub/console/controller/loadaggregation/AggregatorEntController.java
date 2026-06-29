package cn.sl.ehub.console.controller.loadaggregation;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.auth.LoadAggregationScopeService;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.service.IAggregatorEntService;
import cn.sl.ehub.service.vo.AggregatorEnt;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 聚合商企业信息管理
 *
 * @Author 迁移自load-aggregator
 * @Date 2026-06-23
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/ent")
@Api(tags = "聚合商企业信息管理")
public class AggregatorEntController {

    private final IAggregatorEntService aggregatorEntService;
    private final LoadAggregationScopeService loadScopeService;

    @ApiOperation(value = "分页查询企业维护列表")
    @GetMapping("/page")
    public ResultVO<PageResultVO<AggregatorEnt>> pageAggregatorEnt(@RequestParam(value = "aggregatorId", required = false) String aggregatorId,
                                                                   @RequestParam(value = "entId", required = false) String entId,
                                                                   @RequestParam(value = "entName", required = false) String entName,
                                                                   @RequestParam(value = "status", required = false) Integer status,
                                                                   @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                                                   @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        LoadAggregationScopeService.Scope scopeQuery = loadScopeService.resolveQueryScope(aggregatorId, entId);
        PageHelper.startPage(pageIndex, pageSize);
        List<AggregatorEnt> list = aggregatorEntService.pageAggregatorEntList(
                scopeQuery.getAggregatorId(), scopeQuery.getEntId(), entName, status);
        return ResultVO.success(toPage(list, pageIndex, pageSize));
    }

    @ApiOperation(value = "根据企业ID查询企业信息")
    @GetMapping("/getAggregatorEnt")
    public ResultVO<AggregatorEnt> getAggregatorEnt(@RequestParam("entId") String entId) {
        log.info("查询企业信息: entId={}", entId);
        AggregatorEnt ent = requireEnt(entId);
        loadScopeService.validateScope(ent.getAggregatorId(), ent.getEntId());
        return ResultVO.success(ent);
    }

    @ApiOperation(value = "根据聚合商ID查询企业列表")
    @GetMapping("/list")
    public ResultVO<List<AggregatorEnt>> getAggregatorEntList(@RequestParam("aggregatorId") String aggregatorId) {
        log.info("查询聚合商企业列表: aggregatorId={}", aggregatorId);
        LoadAggregationScopeService.Scope scopeQuery = loadScopeService.resolveQueryScope(aggregatorId, null);
        return ResultVO.success(listEntByScope(scopeQuery));
    }

    @ApiOperation(value = "查询响应计划的企业列表")
    @GetMapping("/planRunList")
    public ResultVO<List<AggregatorEnt>> getAggregatorPlanRunEntList(@RequestParam("aggregatorId") String aggregatorId) {
        log.info("查询响应计划的企业列表: aggregatorId={}", aggregatorId);
        LoadAggregationScopeService.Scope scopeQuery = loadScopeService.resolveQueryScope(aggregatorId, null);
        if (StringUtils.isNotBlank(scopeQuery.getEntId())) {
            AggregatorEnt ent = requireEnt(scopeQuery.getEntId());
            loadScopeService.validateScope(ent.getAggregatorId(), ent.getEntId());
            return ResultVO.success(Integer.valueOf(1).equals(ent.getPlanRunStatus())
                    ? Collections.singletonList(ent)
                    : Collections.emptyList());
        }
        return ResultVO.success(aggregatorEntService.getAggregatorPlanRunEntList(scopeQuery.getAggregatorId()));
    }

    @ApiOperation(value = "根据企业ID查询聚合商ID")
    @GetMapping("/getAggregatorId")
    public ResultVO<String> getAggregatorIdByEntId(@RequestParam("entId") String entId) {
        log.info("查询企业所属聚合商: entId={}", entId);
        AggregatorEnt ent = requireEnt(entId);
        loadScopeService.validateScope(ent.getAggregatorId(), ent.getEntId());
        return ResultVO.success(ent.getAggregatorId());
    }

    @ApiOperation(value = "统计聚合商下的企业数量")
    @GetMapping("/count")
    public ResultVO<Integer> getCount(@RequestParam("aggregatorId") String aggregatorId) {
        log.info("统计企业数量: aggregatorId={}", aggregatorId);
        LoadAggregationScopeService.Scope scopeQuery = loadScopeService.resolveQueryScope(aggregatorId, null);
        if (StringUtils.isNotBlank(scopeQuery.getEntId())) {
            requireEnt(scopeQuery.getEntId());
            return ResultVO.success(1);
        }
        return ResultVO.success(aggregatorEntService.getCount(scopeQuery.getAggregatorId()));
    }

    @ApiOperation(value = "查询所有企业列表")
    @GetMapping("/all")
    public ResultVO<List<AggregatorEnt>> getAllAggregatorEntList() {
        log.info("查询所有企业列表");
        LoadAggregationScopeService.Scope scopeQuery = loadScopeService.resolveQueryScope(null, null);
        return ResultVO.success(scopeQuery.isAdmin()
                ? aggregatorEntService.getAggregatorEntList()
                : listEntByScope(scopeQuery));
    }

    @ApiOperation(value = "根据企业ID列表批量查询")
    @PostMapping("/listByIds")
    public ResultVO<List<AggregatorEnt>> getAggregatorEntListByIds(@RequestBody List<String> entIdList) {
        log.info("批量查询企业信息: entIdList.size={}", entIdList != null ? entIdList.size() : 0);
        if (entIdList == null || entIdList.isEmpty()) {
            return ResultVO.success(Collections.emptyList());
        }
        List<AggregatorEnt> list = aggregatorEntService.getAggregatorEntList(entIdList);
        for (AggregatorEnt ent : list) {
            if (ent != null) {
                loadScopeService.validateScope(ent.getAggregatorId(), ent.getEntId());
            }
        }
        return ResultVO.success(list);
    }

    @ApiOperation(value = "批量添加企业")
    @PostMapping("/addBatch")
    public ResultVO<Integer> addAggregatorEntList(@RequestBody List<AggregatorEnt> aggregatorEntList) {
        log.info("批量添加企业: size={}", aggregatorEntList != null ? aggregatorEntList.size() : 0);
        if (aggregatorEntList != null) {
            for (AggregatorEnt ent : aggregatorEntList) {
                loadScopeService.fillAggregatorEntSaveScope(ent, false);
            }
        }
        int count = aggregatorEntService.addAggregatorEntList(aggregatorEntList);
        return ResultVO.success(count);
    }

    @ApiOperation(value = "新增企业")
    @PostMapping
    public ResultVO<AggregatorEnt> createAggregatorEnt(@RequestBody AggregatorEnt aggregatorEnt) {
        loadScopeService.fillAggregatorEntSaveScope(aggregatorEnt, false);
        return ResultVO.success(aggregatorEntService.createAggregatorEnt(aggregatorEnt));
    }

    @ApiOperation(value = "更新企业")
    @PutMapping("/{entId}")
    public ResultVO<AggregatorEnt> updateAggregatorEnt(@PathVariable("entId") String entId,
                                                       @RequestBody AggregatorEnt aggregatorEnt) {
        AggregatorEnt existing = requireEnt(entId);
        loadScopeService.validateScope(existing.getAggregatorId(), existing.getEntId());
        loadScopeService.fillAggregatorEntSaveScope(aggregatorEnt, true);
        return ResultVO.success(aggregatorEntService.updateAggregatorEnt(entId, aggregatorEnt));
    }

    @ApiOperation(value = "停用企业")
    @DeleteMapping("/{entId}")
    public ResultVO<Boolean> disableAggregatorEnt(@PathVariable("entId") String entId) {
        AggregatorEnt existing = requireEnt(entId);
        loadScopeService.validateScope(existing.getAggregatorId(), existing.getEntId());
        aggregatorEntService.updateAggregatorEntStatus(entId, 0);
        return ResultVO.success(true);
    }

    private AggregatorEnt requireEnt(String entId) {
        AggregatorEnt ent = aggregatorEntService.getAggregatorEnt(entId);
        if (ent == null) {
            throw new BaseException(StatusCode.C.getCode(), "企业不存在");
        }
        return ent;
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

    private List<AggregatorEnt> listEntByScope(LoadAggregationScopeService.Scope scopeQuery) {
        if (StringUtils.isNotBlank(scopeQuery.getEntId())) {
            return aggregatorEntService.getAggregatorEntList(Collections.singletonList(scopeQuery.getEntId()));
        }
        return aggregatorEntService.getAggregatorEntList(scopeQuery.getAggregatorId());
    }
}
