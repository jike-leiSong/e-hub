package cn.sl.ehub.console.service;

import cn.sl.ehub.service.resp.AggregatorEntApplyDateResp;
import cn.sl.ehub.service.resp.AggregatorEntApplyPlanDateResp;
import cn.sl.ehub.service.resp.AggregatorEntApplyPlanResp;
import cn.sl.ehub.service.resp.AggregatorEntSocialResponsibilityResp;

import java.util.List;

/**
 * 企业APP申报计划Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorEntAppApplyPlanService {

    /**
     * 查询数据
     *
     * @param entId
     * @param date
     * @return
     */
    AggregatorEntSocialResponsibilityResp getSocialResponsibility(String entId, String date);

    /**
     * 查询默认计划
     *
     * @param entId
     * @return
     */
    AggregatorEntApplyPlanResp getAggregatorEntDefaultApplyPlanResp(String entId);

    /**
     * 查询调峰日历
     *
     * @param entId
     * @param startDate
     * @param endDate
     * @return
     */
    List<AggregatorEntApplyPlanDateResp> getAggregatorEntApplyPlanDateResp(String entId, String startDate, String endDate);
}
