package cn.sl.ehub.console.controller.loadaggregation;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.service.IAggregatorEntApplyPlanService;
import cn.sl.ehub.console.service.IAggregatorEntDateProfitService;
import cn.sl.ehub.service.req.AggregatorEntApplyPlanReq;
import cn.sl.ehub.service.resp.AggregatorEntApplyPlanResp;
import cn.sl.ehub.service.resp.AggregatorEntApplyPlanStatusResp;
import cn.sl.ehub.service.resp.AggregatorEntDateDeviceStartStopPlanResp;
import cn.sl.ehub.service.resp.AggregatorEntDateProfitResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企业用户申报计划管理
 *
 * @Author 迁移自load-aggregator
 * @Date 2026-06-23
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/entPlan")
@Api(tags = "企业用户申报计划管理")
public class AggregatorEntApplyPlanController {

    private final IAggregatorEntApplyPlanService aggregatorEntApplyPlanService;
    private final IAggregatorEntDateProfitService aggregatorEntDateProfitService;

    @ApiOperation(value = "查询申报计划列表（分页）")
    @GetMapping("/getAggregatorEntApplyPlanRespList")
    public ResultVO<PageResultVO<AggregatorEntApplyPlanResp>> getAggregatorEntApplyPlanRespList(
            @RequestParam("entId") String entId,
            @RequestParam(value = "saveStatus", required = false) Boolean saveStatus,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        log.info("查询企业申报计划列表: entId={}, saveStatus={}, pageNo={}, pageSize={}",
                entId, saveStatus, pageNo, pageSize);
        return ResultVO.success(aggregatorEntApplyPlanService.getAggregatorEntApplyPlanRespList(
                entId, saveStatus, pageNo, pageSize));
    }

    @ApiOperation(value = "根据ID查询申报计划详情")
    @GetMapping("/getAggregatorEntApplyPlanResp")
    public ResultVO<AggregatorEntApplyPlanResp> getAggregatorEntApplyPlanResp(@RequestParam("id") String id) {
        log.info("查询申报计划详情: id={}", id);
        return ResultVO.success(aggregatorEntApplyPlanService.getAggregatorEntApplyPlanResp(id));
    }

    @ApiOperation(value = "查询明日计划")
    @GetMapping("/getTomorrowPlan")
    public ResultVO<AggregatorEntApplyPlanResp> getTomorrowPlan(
            @RequestParam("entId") String entId,
            @RequestParam(value = "date", required = false) String date) {
        log.info("查询明日计划: entId={}, date={}", entId, date);
        return ResultVO.success(aggregatorEntApplyPlanService.getAggregatorEntApplyPlanResp(entId, date));
    }

    @ApiOperation(value = "创建申报计划")
    @PostMapping("/addApplyPlan")
    public ResultVO<Boolean> addApplyPlan(@RequestBody AggregatorEntApplyPlanReq aggregatorEntApplyPlanReq) {
        log.info("创建申报计划: entId={}, startDate={}, endDate={}",
                aggregatorEntApplyPlanReq.getEntId(),
                aggregatorEntApplyPlanReq.getStartDate(),
                aggregatorEntApplyPlanReq.getEndDate());
        return ResultVO.success(aggregatorEntApplyPlanService.addApplyPlan(aggregatorEntApplyPlanReq));
    }

    @ApiOperation(value = "创建计划回显")
    @GetMapping("/getApplyPlan")
    public ResultVO<AggregatorEntApplyPlanResp> getApplyPlan(
            @RequestParam("entId") String entId,
            @RequestParam("planStatus") Boolean planStatus,
            @RequestParam(value = "date", required = false) String date) {
        log.info("创建计划回显: entId={}, planStatus={}, date={}", entId, planStatus, date);
        return ResultVO.success(aggregatorEntApplyPlanService.getApplyPlan(entId, planStatus, date, null));
    }

    @ApiOperation(value = "查询企业收益")
    @GetMapping("/getProfit")
    public ResultVO<AggregatorEntDateProfitResp> getProfit(@RequestParam("entId") String entId) {
        log.info("查询企业收益: entId={}", entId);
        return ResultVO.success(aggregatorEntDateProfitService.getProfit(entId));
    }

    @ApiOperation(value = "查询申报状态")
    @GetMapping("/getApplyStatus")
    public ResultVO<AggregatorEntApplyPlanStatusResp> getApplyStatus(
            @RequestParam("entId") String entId,
            @RequestParam(value = "date", required = false) String date) {
        log.info("查询申报状态: entId={}, date={}", entId, date);
        return ResultVO.success(aggregatorEntApplyPlanService.getApplyStatus(entId, date));
    }

    @ApiOperation(value = "查询设备启停计划")
    @GetMapping("/getDevicePlan")
    public ResultVO<List<AggregatorEntDateDeviceStartStopPlanResp>> getDevicePlan(
            @RequestParam("entId") String entId,
            @RequestParam(value = "date", required = false) String date) {
        log.info("查询设备启停计划: entId={}, date={}", entId, date);
        return ResultVO.success(aggregatorEntApplyPlanService.getDevicePlan(entId, date));
    }

    @ApiOperation(value = "查看默认计划")
    @GetMapping("/getDefaultPlanResp")
    public ResultVO<AggregatorEntApplyPlanResp> getDefaultPlanResp(@RequestParam("entId") String entId) {
        log.info("查看默认计划: entId={}", entId);
        return ResultVO.success(aggregatorEntApplyPlanService.getDefaultPlanResp(entId));
    }
}
