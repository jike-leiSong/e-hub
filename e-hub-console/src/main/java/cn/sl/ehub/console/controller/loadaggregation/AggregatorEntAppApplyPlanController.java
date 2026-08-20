package cn.sl.ehub.console.controller.loadaggregation;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.auth.LoadAggregationScopeService;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.service.IAggregatorEntAppApplyPlanService;
import cn.sl.ehub.console.service.IAggregatorEntApplyPlanService;
import cn.sl.ehub.service.req.AggregatorEntApplyPlanReq;
import cn.sl.ehub.service.resp.AggregatorEntApplyDateResp;
import cn.sl.ehub.service.resp.AggregatorEntApplyPlanDateResp;
import cn.sl.ehub.service.resp.AggregatorEntApplyPlanResp;
import cn.sl.ehub.service.resp.AggregatorEntSocialResponsibilityResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企业用户APP申报计划管理（APP1.1.1版本）
 *
 * @Author 迁移自load-aggregator
 * @Date 2026-06-23
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/entAppPlan")
@Api(tags = "企业用户APP申报计划管理")
public class AggregatorEntAppApplyPlanController {

    private final IAggregatorEntApplyPlanService aggregatorEntApplyPlanService;
    private final IAggregatorEntAppApplyPlanService aggregatorEntAppApplyPlanService;
    private final LoadAggregationScopeService loadScopeService;

    @ApiOperation(value = "查询申报计划列表（分页，支持多种过滤）")
    @GetMapping("/getAggregatorEntApplyPlanRespList")
    public ResultVO<PageResultVO<AggregatorEntApplyPlanResp>> getAggregatorEntApplyPlanRespList(
            @ApiParam("企业ID") @RequestParam("entId") String entId,
            @ApiParam("类型：0=计算类型，1=日期") @RequestParam(value = "type", defaultValue = "0", required = false) String type,
            @ApiParam("计划类型：0=全部计划不包含默认，1=默认计划，2=已完成计划，3=当前进行中计划，4=未开始计划")
            @RequestParam(value = "planType", required = false) String planType,
            @ApiParam("日期：yyyy-MM-dd") @RequestParam(value = "date", required = false) String date,
            @ApiParam("页码") @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @ApiParam("数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        log.info("查询APP申报计划列表: entId={}, type={}, planType={}, date={}, pageNum={}, pageSize={}",
                entId, type, planType, date, pageNum, pageSize);
        loadScopeService.validateScope(null, entId);
        return ResultVO.success(aggregatorEntApplyPlanService.getAggregatorEntApplyPlanRespList(
                entId, type, planType, date, pageNum, pageSize));
    }

    @ApiOperation(value = "创建申报计划（V1.1版本）")
    @PostMapping("/addApplyPlan")
    public ResultVO<Boolean> addApplyPlan(@RequestBody AggregatorEntApplyPlanReq aggregatorEntApplyPlanReq) {
        log.info("创建APP申报计划: entId={}, startDate={}, endDate={}",
                aggregatorEntApplyPlanReq.getEntId(),
                aggregatorEntApplyPlanReq.getStartDate(),
                aggregatorEntApplyPlanReq.getEndDate());
        loadScopeService.validateScope(aggregatorEntApplyPlanReq.getAggregatorId(), aggregatorEntApplyPlanReq.getEntId());
        return ResultVO.success(aggregatorEntApplyPlanService.addApplyPlanV1(aggregatorEntApplyPlanReq));
    }

    @ApiOperation(value = "查询企业社会责任数据")
    @GetMapping("/getSocialResponsibility")
    public ResultVO<AggregatorEntSocialResponsibilityResp> getSocialResponsibility(
            @ApiParam("企业ID") @RequestParam("entId") String entId,
            @ApiParam("开始日期") @RequestParam(value = "date", defaultValue = "2021-03-05", required = false) String date) {
        log.info("查询社会责任: entId={}, date={}", entId, date);
        loadScopeService.validateScope(null, entId);
        return ResultVO.success(aggregatorEntAppApplyPlanService.getSocialResponsibility(entId, date));
    }

    @ApiOperation(value = "查询企业默认申报计划")
    @GetMapping("/getAggregatorEntDefaultApplyPlanResp")
    public ResultVO<AggregatorEntApplyPlanResp> getAggregatorEntDefaultApplyPlanResp(
            @ApiParam("企业ID") @RequestParam("entId") String entId) {
        log.info("查询默认计划: entId={}", entId);
        loadScopeService.validateScope(null, entId);
        return ResultVO.success(aggregatorEntAppApplyPlanService.getAggregatorEntDefaultApplyPlanResp(entId));
    }

    @ApiOperation(value = "查询调峰日历")
    @GetMapping("/getAggregatorEntApplyPlanDateResp")
    public ResultVO<List<AggregatorEntApplyPlanDateResp>> getAggregatorEntApplyPlanDateResp(
            @ApiParam("企业ID") @RequestParam("entId") String entId,
            @ApiParam("开始日期") @RequestParam("startDate") String startDate,
            @ApiParam("结束日期") @RequestParam("endDate") String endDate) {
        log.info("查询调峰日历: entId={}, startDate={}, endDate={}", entId, startDate, endDate);
        loadScopeService.validateScope(null, entId);
        return ResultVO.success(aggregatorEntAppApplyPlanService.getAggregatorEntApplyPlanDateResp(
                entId, startDate, endDate));
    }

    @ApiOperation(value = "查询创建计划可选日期")
    @GetMapping("/getDate")
    public ResultVO<AggregatorEntApplyDateResp> getDate(
            @ApiParam("企业ID") @RequestParam("entId") String entId,
            @ApiParam("日期") @RequestParam(value = "date", required = false) String date) {
        log.info("查询创建计划日期: entId={}, date={}", entId, date);
        loadScopeService.validateScope(null, entId);
        return ResultVO.success(aggregatorEntApplyPlanService.getDate(entId, date));
    }
}
