package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorEntDateDeviceStartStopPlan;

import java.util.List;

/**
 * 设备启停计划Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorEntDateDeviceStartStopPlanService {

    /**
     * 批量添加数据
     *
     * @param aggregatorEntDateDeviceStartStopPlanList
     * @return
     */
    int batchInsert(List<AggregatorEntDateDeviceStartStopPlan> aggregatorEntDateDeviceStartStopPlanList);

    /**
     * 清除数据
     *
     * @param entId
     * @param dateList
     * @return
     */
    int delete(String entId, List<String> dateList);

    /**
     * 查询数据
     *
     * @param entId
     * @param date
     * @return
     */
    AggregatorEntDateDeviceStartStopPlan getAggregatorEntDateDeviceStartStopPlan(String entId, String date);
}
