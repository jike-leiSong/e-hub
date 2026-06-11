package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorDeviceDateIssueChart;

import java.util.List;

/**
 * 设备下发功率Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorDeviceDateIssueChartService {

    /**
     * 查询数据
     *
     * @param deviceBaseId
     * @param date
     * @return
     */
    AggregatorDeviceDateIssueChart getAggregatorDeviceDateIssueChart(String deviceBaseId, String date);

    /**
     * 查询数据
     *
     * @param deviceBaseId
     * @param dateList
     * @return
     */
    List<AggregatorDeviceDateIssueChart> getAggregatorDeviceDateIssueChartList(String deviceBaseId, List<String> dateList);

    /**
     * 查询数据
     *
     * @param aggregatorIdList
     * @param date
     * @return
     */
    List<AggregatorDeviceDateIssueChart> getAggregatorDeviceDateIssueChartList(List<String> aggregatorIdList, String date);

    /**
     * 查询上一次下发功率
     *
     * @param deviceBaseId
     * @return
     */
    AggregatorDeviceDateIssueChart getAggregatorDeviceDateIssueChart(String deviceBaseId);

    /**
     * 删除数据
     *
     * @param aggregatorId
     * @param date
     * @return
     */
    int delete(String aggregatorId, String date);

    /**
     * 添加数据
     *
     * @param aggregatorDeviceDateIssueChartList
     * @return
     */
    int batchInsert(List<AggregatorDeviceDateIssueChart> aggregatorDeviceDateIssueChartList);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param dateList
     * @return
     */
    List<AggregatorDeviceDateIssueChart> getAggregatorDeviceDateIssueChartList(String aggregatorId, String resourceTypeId, List<String> dateList);

    /**
     * 查询数据
     *
     * @param deviceBaseIdList
     * @param dateList
     * @return
     */
    List<AggregatorDeviceDateIssueChart> getAggregatorDeviceDateIssueChartList(List<String> deviceBaseIdList, List<String> dateList);

    /**
     * 删除数据
     *
     * @param resourceTypeId
     * @param date
     * @return
     */
    int deleteByResourceTypeId(String resourceTypeId, String date);

    /**
     * 查询数据
     *
     * @param resourceTypeId
     * @param date
     * @return
     */
    List<AggregatorDeviceDateIssueChart> getAggregatorDeviceDateIssueChartListByResourceTypeId(String resourceTypeId, String date);
}
