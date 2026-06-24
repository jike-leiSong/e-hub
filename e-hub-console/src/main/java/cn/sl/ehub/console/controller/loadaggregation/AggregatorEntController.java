package cn.sl.ehub.console.controller.loadaggregation;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.auth.AuthContext;
import cn.sl.ehub.console.auth.AuthUser;
import cn.sl.ehub.console.auth.ConsoleProductService;
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

    @ApiOperation(value = "分页查询企业维护列表")
    @GetMapping("/page")
    public ResultVO<PageResultVO<AggregatorEnt>> pageAggregatorEnt(@RequestParam(value = "aggregatorId", required = false) String aggregatorId,
                                                                   @RequestParam(value = "entId", required = false) String entId,
                                                                   @RequestParam(value = "entName", required = false) String entName,
                                                                   @RequestParam(value = "status", required = false) Integer status,
                                                                   @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                                                   @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        ScopeQuery scopeQuery = applyQueryScope(aggregatorId, entId);
        PageHelper.startPage(pageIndex, pageSize);
        List<AggregatorEnt> list = aggregatorEntService.pageAggregatorEntList(
                scopeQuery.aggregatorId, scopeQuery.entId, entName, status);
        return ResultVO.success(toPage(list, pageIndex, pageSize));
    }

    @ApiOperation(value = "根据企业ID查询企业信息")
    @GetMapping("/getAggregatorEnt")
    public ResultVO<AggregatorEnt> getAggregatorEnt(@RequestParam("entId") String entId) {
        log.info("查询企业信息: entId={}", entId);
        return ResultVO.success(aggregatorEntService.getAggregatorEnt(entId));
    }

    @ApiOperation(value = "根据聚合商ID查询企业列表")
    @GetMapping("/list")
    public ResultVO<List<AggregatorEnt>> getAggregatorEntList(@RequestParam("aggregatorId") String aggregatorId) {
        log.info("查询聚合商企业列表: aggregatorId={}", aggregatorId);
        ScopeQuery scopeQuery = applyQueryScope(aggregatorId, null);
        if (StringUtils.isNotBlank(scopeQuery.entId)) {
            return ResultVO.success(aggregatorEntService.getAggregatorEntList(Collections.singletonList(scopeQuery.entId)));
        }
        return ResultVO.success(aggregatorEntService.getAggregatorEntList(scopeQuery.aggregatorId));
    }

    @ApiOperation(value = "查询响应计划的企业列表")
    @GetMapping("/planRunList")
    public ResultVO<List<AggregatorEnt>> getAggregatorPlanRunEntList(@RequestParam("aggregatorId") String aggregatorId) {
        log.info("查询响应计划的企业列表: aggregatorId={}", aggregatorId);
        return ResultVO.success(aggregatorEntService.getAggregatorPlanRunEntList(aggregatorId));
    }

    @ApiOperation(value = "根据企业ID查询聚合商ID")
    @GetMapping("/getAggregatorId")
    public ResultVO<String> getAggregatorIdByEntId(@RequestParam("entId") String entId) {
        log.info("查询企业所属聚合商: entId={}", entId);
        return ResultVO.success(aggregatorEntService.getAggregatorIdByEntId(entId));
    }

    @ApiOperation(value = "统计聚合商下的企业数量")
    @GetMapping("/count")
    public ResultVO<Integer> getCount(@RequestParam("aggregatorId") String aggregatorId) {
        log.info("统计企业数量: aggregatorId={}", aggregatorId);
        return ResultVO.success(aggregatorEntService.getCount(aggregatorId));
    }

    @ApiOperation(value = "查询所有企业列表")
    @GetMapping("/all")
    public ResultVO<List<AggregatorEnt>> getAllAggregatorEntList() {
        log.info("查询所有企业列表");
        return ResultVO.success(aggregatorEntService.getAggregatorEntList());
    }

    @ApiOperation(value = "根据企业ID列表批量查询")
    @PostMapping("/listByIds")
    public ResultVO<List<AggregatorEnt>> getAggregatorEntListByIds(@RequestBody List<String> entIdList) {
        log.info("批量查询企业信息: entIdList.size={}", entIdList != null ? entIdList.size() : 0);
        return ResultVO.success(aggregatorEntService.getAggregatorEntList(entIdList));
    }

    @ApiOperation(value = "批量添加企业")
    @PostMapping("/addBatch")
    public ResultVO<Integer> addAggregatorEntList(@RequestBody List<AggregatorEnt> aggregatorEntList) {
        log.info("批量添加企业: size={}", aggregatorEntList != null ? aggregatorEntList.size() : 0);
        int count = aggregatorEntService.addAggregatorEntList(aggregatorEntList);
        return ResultVO.success(count);
    }

    @ApiOperation(value = "新增企业")
    @PostMapping
    public ResultVO<AggregatorEnt> createAggregatorEnt(@RequestBody AggregatorEnt aggregatorEnt) {
        fillSaveScope(aggregatorEnt, false);
        return ResultVO.success(aggregatorEntService.createAggregatorEnt(aggregatorEnt));
    }

    @ApiOperation(value = "更新企业")
    @PutMapping("/{entId}")
    public ResultVO<AggregatorEnt> updateAggregatorEnt(@PathVariable("entId") String entId,
                                                       @RequestBody AggregatorEnt aggregatorEnt) {
        AggregatorEnt existing = requireEnt(entId);
        validateScope(existing.getAggregatorId(), existing.getEntId());
        fillSaveScope(aggregatorEnt, true);
        return ResultVO.success(aggregatorEntService.updateAggregatorEnt(entId, aggregatorEnt));
    }

    @ApiOperation(value = "停用企业")
    @DeleteMapping("/{entId}")
    public ResultVO<Boolean> disableAggregatorEnt(@PathVariable("entId") String entId) {
        AggregatorEnt existing = requireEnt(entId);
        validateScope(existing.getAggregatorId(), existing.getEntId());
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

    private ScopeQuery applyQueryScope(String aggregatorId, String entId) {
        AuthUser user = AuthContext.get();
        if (user == null || isAdmin(user)) {
            return new ScopeQuery(aggregatorId, entId);
        }
        if (isEntCustomer(user)) {
            return new ScopeQuery(user.getAggregatorId(), user.getEntId());
        }
        if (isAggregatorCustomer(user)) {
            if (StringUtils.isNotBlank(aggregatorId) && !StringUtils.equals(user.getAggregatorId(), aggregatorId)) {
                throwNoPermission();
            }
            return new ScopeQuery(user.getAggregatorId(), entId);
        }
        throwNoPermission();
        return new ScopeQuery(aggregatorId, entId);
    }

    private void fillSaveScope(AggregatorEnt aggregatorEnt, boolean update) {
        if (aggregatorEnt == null) {
            throw new BaseException(StatusCode.C.getCode(), "企业信息不能为空");
        }
        AuthUser user = AuthContext.get();
        if (user == null || isAdmin(user)) {
            return;
        }
        if (isEntCustomer(user)) {
            if (!update) {
                throwNoPermission();
            }
            aggregatorEnt.setAggregatorId(user.getAggregatorId());
            aggregatorEnt.setEntId(user.getEntId());
            return;
        }
        if (isAggregatorCustomer(user)) {
            aggregatorEnt.setAggregatorId(user.getAggregatorId());
            if (StringUtils.isNotBlank(aggregatorEnt.getEntId())) {
                validateScope(user.getAggregatorId(), aggregatorEnt.getEntId());
            }
            return;
        }
        throwNoPermission();
    }

    private void validateScope(String aggregatorId, String entId) {
        AuthUser user = AuthContext.get();
        if (user == null || isAdmin(user)) {
            return;
        }
        if (isEntCustomer(user)) {
            if (!StringUtils.equals(user.getAggregatorId(), aggregatorId)
                    || !StringUtils.equals(user.getEntId(), entId)) {
                throwNoPermission();
            }
            return;
        }
        if (isAggregatorCustomer(user)) {
            if (!StringUtils.equals(user.getAggregatorId(), aggregatorId)) {
                throwNoPermission();
            }
            return;
        }
        throwNoPermission();
    }

    private boolean isAdmin(AuthUser user) {
        return StringUtils.equalsIgnoreCase(ConsoleProductService.USER_TYPE_ADMIN, user.getUserType())
                || StringUtils.equalsIgnoreCase(ConsoleProductService.USER_TYPE_PLATFORM, user.getUserType());
    }

    private boolean isEntCustomer(AuthUser user) {
        return isCustomer(user) && StringUtils.isNotBlank(user.getEntId());
    }

    private boolean isAggregatorCustomer(AuthUser user) {
        return isCustomer(user) && StringUtils.isNotBlank(user.getAggregatorId()) && StringUtils.isBlank(user.getEntId());
    }

    private boolean isCustomer(AuthUser user) {
        return StringUtils.equalsIgnoreCase(ConsoleProductService.USER_TYPE_CUSTOMER, user.getUserType())
                || StringUtils.equalsIgnoreCase(ConsoleProductService.USER_TYPE_AGGREGATOR, user.getUserType())
                || StringUtils.equalsIgnoreCase(ConsoleProductService.USER_TYPE_ENT, user.getUserType());
    }

    private void throwNoPermission() {
        throw new BaseException(StatusCode.U.getCode(), StatusCode.U.getMsg());
    }

    private static class ScopeQuery {
        private final String aggregatorId;
        private final String entId;

        private ScopeQuery(String aggregatorId, String entId) {
            this.aggregatorId = aggregatorId;
            this.entId = entId;
        }
    }
}
