package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.console.service.IAggregatorApplyPlanService;
import cn.sl.ehub.service.req.AddPlanReq;
import cn.sl.ehub.service.req.QueryPlanListReq;
import cn.sl.ehub.service.req.ReferDatePowerReq;
import cn.sl.ehub.service.resp.PlanDetailResp;
import cn.sl.ehub.service.resp.QueryPlanListResp;
import cn.sl.ehub.service.resp.ReferDatePowerResp;
import cn.sl.ehub.service.vo.AggregatorApplyPlan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 聚合商计划管理服务实现（空实现）
 *
 * @author sl
 * @date 2026-06-15
 */
@Slf4j
@Service
public class AggregatorApplyPlanServiceImpl implements IAggregatorApplyPlanService {

    @Override
    public ReferDatePowerResp getReferDatePower(ReferDatePowerReq referDatePowerReq) {
        log.warn("getReferDatePower called - empty implementation");
        return new ReferDatePowerResp();
    }

    @Override
    public QueryPlanListResp getPlanList(QueryPlanListReq queryPlanListReq) {
        log.warn("getPlanList called - empty implementation");
        return new QueryPlanListResp();
    }

    @Override
    public PlanDetailResp getPlanDetailById(String planId) {
        log.warn("getPlanDetailById called with planId: {} - empty implementation", planId);
        return new PlanDetailResp();
    }

    @Override
    public Boolean addOrUpdatePlan(AddPlanReq addPlanReq) {
        log.warn("addOrUpdatePlan called - empty implementation");
        return false;
    }

    @Override
    public AggregatorApplyPlan getPlan(String aggregatorId, String sourceId, String date) {
        log.warn("getPlan called with aggregatorId: {}, sourceId: {}, date: {} - empty implementation",
                aggregatorId, sourceId, date);
        return null;
    }
}
