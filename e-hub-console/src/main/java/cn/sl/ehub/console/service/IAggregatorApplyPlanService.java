package cn.sl.ehub.console.service;


import cn.sl.ehub.service.req.AddPlanReq;
import cn.sl.ehub.service.req.QueryPlanListReq;
import cn.sl.ehub.service.req.ReferDatePowerReq;
import cn.sl.ehub.service.resp.PlanDetailResp;
import cn.sl.ehub.service.resp.QueryPlanListResp;
import cn.sl.ehub.service.resp.ReferDatePowerResp;
import cn.sl.ehub.service.vo.AggregatorApplyPlan;

/**
 * 聚合商计划管理
 *
 * @author sl
 * @classes cn.sl.ehub.upstream.service.IAggregatorApplyPlanService
 * @date 2026-05-28
 */
public interface IAggregatorApplyPlanService {

    /**
     * @description 获取参考日功率
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    ReferDatePowerResp getReferDatePower(ReferDatePowerReq referDatePowerReq);


    /**
     * @description 查询计划列表
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    QueryPlanListResp  getPlanList(QueryPlanListReq queryPlanListReq);


    /**
     * @description 查询计划详情
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    PlanDetailResp getPlanDetailById(String planId);

    /**
     * @description 新增或者更新计划
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    Boolean addOrUpdatePlan(AddPlanReq addPlanReq);

    /**
     * @description 查询计划
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    AggregatorApplyPlan getPlan(String aggregatorId, String sourceId, String date);




}
