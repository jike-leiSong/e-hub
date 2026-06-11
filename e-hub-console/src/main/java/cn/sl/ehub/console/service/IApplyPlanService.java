package cn.sl.ehub.console.service;

import cn.sl.ehub.console.model.req.AggregatorEntDeviceApplyPlanDateReq;
import cn.sl.ehub.console.model.req.AggregatorEntDeviceApplyPlanReq;
import cn.sl.ehub.service.resp.AppApplyIndexResp;
import cn.sl.ehub.service.vo.AggregatorDeviceDateDeliveryChart;
import cn.sl.ehub.service.vo.AggregatorEntDeviceApplyPlan;

import java.util.List;

/**
 * 申报计划Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IApplyPlanService {

    /**
     * 查询申报首页
     *
     * @param entId
     * @param time
     * @return
     */
    AppApplyIndexResp getApplyIndexResp(String entId, String time);

    /**
     * 保存设备申报计划
     *
     * @param req
     * @return
     */
    Boolean saveAggregatorEntDeviceApplyPlanList(AggregatorEntDeviceApplyPlanReq req);

    /**
     * 确认申报
     *
     * @param req
     * @return
     */
    Boolean apply(AggregatorEntDeviceApplyPlanReq req);

    /**
     * 保存设备申报曲线、企业申报状态、聚合商申报曲线
     *
     * @param now
     * @param aggregatorId
     * @param req
     */
    void saveDeliveryChart(String now, String aggregatorId, AggregatorEntDeviceApplyPlanReq req);

    /**
     * 保存聚合商上报曲线
     *
     * @param aggregatorId
     * @param aggregatorDeviceDateDeliveryChartList
     */
    void saveAggregatorDateDeliveryChart(String aggregatorId, List<AggregatorDeviceDateDeliveryChart> aggregatorDeviceDateDeliveryChartList);
}
