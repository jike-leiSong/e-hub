package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorEntDeviceApplyPlan;

import java.util.List;

/**
 * 设备申报计划Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorEntDeviceApplyPlanService {

    /**
     * 查询数据
     *
     * @param entId
     * @param date
     * @param deviceBaseId
     * @return
     */
    List<AggregatorEntDeviceApplyPlan> getAggregatorEntDeviceApplyPlanList(String entId, String date, String deviceBaseId);

    /**
     * 删除数据
     *
     * @param entId
     * @param dateList
     * @return
     */
    int delete(String entId, List<String> dateList);

    /**
     * 添加数据
     *
     * @param planList
     * @return
     */
    int batchInsert(List<AggregatorEntDeviceApplyPlan> planList);

    /**
     * 查询数据
     *
     * @param entId
     * @param startDate
     * @param endDate
     * @return
     */
    List<AggregatorEntDeviceApplyPlan> getAggregatorEntDeviceApplyPlanListByDate(String entId, String startDate, String endDate);
}
