package cn.sl.ehub.console.controller.loadaggregation;


import cn.sl.ehub.service.req.AddPlanReq;
import cn.sl.ehub.service.req.QueryPlanListReq;
import cn.sl.ehub.service.req.ReferDatePowerReq;
import cn.sl.ehub.service.resp.PlanDetailResp;
import cn.sl.ehub.service.resp.QueryPlanListResp;
import cn.sl.ehub.service.resp.ReferDatePowerResp;
import cn.sl.ehub.console.service.IAggregatorApplyPlanService;
import cn.sl.ehub.console.service.IAggregatorDateHolidayService;
import cn.sl.ehub.console.service.IAggregatorEntDapChartService;
import cn.sl.ehub.service.vo.AggregatorEntDapChart;
import cn.sl.ehub.common.vo.ResultVO;
import cn.hutool.core.collection.CollectionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


/**
 * @description 聚合商计划
 * @param
 * @return
 * @author sl
 * @date 2026-05-28
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/aggregatorPlan")
@Api(tags = "聚合商计划管理")
public class AggregatorApplyPlanController {

    @Autowired
    private IAggregatorApplyPlanService aggregatorApplyPlanService;

    @Autowired
    private IAggregatorDateHolidayService aggregatorDateHolidayService;
    @Autowired
    IAggregatorEntDapChartService aggregatorEntDapChartService;

    @ApiOperation(value = "查询参考日功率")
    @PostMapping("/getReferDatePower")
    public ResultVO<ReferDatePowerResp> getReferDatePower(@RequestBody @Valid ReferDatePowerReq referDatePowerReq){
        ReferDatePowerResp referDatePower = aggregatorApplyPlanService.getReferDatePower(referDatePowerReq);
        return ResultVO.success(referDatePower);
    }


    @ApiOperation(value = "查询计划列表")
    @PostMapping("/getPlanList")
    public ResultVO<QueryPlanListResp> getPlanList(@RequestBody @Valid QueryPlanListReq queryPlanListReq){

        return ResultVO.success(aggregatorApplyPlanService.getPlanList(queryPlanListReq));
    }


    @ApiOperation(value = "查询计划详情")
    @GetMapping("/getPlanDetail")
    public ResultVO<PlanDetailResp> getPlanList(@RequestParam("planId") String planId){

        return ResultVO.success(aggregatorApplyPlanService.getPlanDetailById(planId));
    }


    @ApiOperation(value = "新增计划")
    @PostMapping("/addOrUpdatePlan")
    public ResultVO<Boolean> addPlan(@RequestBody @Valid   AddPlanReq addPlanReq){

        return ResultVO.success(aggregatorApplyPlanService.addOrUpdatePlan(addPlanReq));
    }


    @ApiOperation(value = "初始化2024年假期")
    @PostMapping("/initHoliday")
    public ResultVO<Boolean> initHoliday(){

        return ResultVO.success(aggregatorDateHolidayService.initHoliday());
    }

    @ApiOperation(value = "查询是否有日前计划下发")
    @GetMapping("/getRunPlan")
    public ResultVO<Boolean> getRunPlanList(@RequestParam("aggregatorId") String aggregatorId,@RequestParam("resourceTypeId") String resourceTypeId,@RequestParam("date") String date){
        List<AggregatorEntDapChart> aggregatorDapLineByDate = aggregatorEntDapChartService.getAggregatorDapLineByDate(aggregatorId, resourceTypeId, date);
        if(CollectionUtil.isNotEmpty(aggregatorDapLineByDate)){
            return ResultVO.success(true);
        }
        return ResultVO.success(false);
    }

    @ApiOperation(value = "获取申报日期列表")
    @GetMapping("/getApplyDateList")
    public ResultVO<List<String>> getApplyDateList(@RequestParam("aggregatorId") String aggregatorId){
        return ResultVO.success(aggregatorDateHolidayService.getApplyDateList(null, true));
    }

}
