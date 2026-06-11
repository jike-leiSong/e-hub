package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorDateIssueChart;

import java.util.List;

/**
 * 聚合商下发曲线Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorDateIssueChartService {

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param date
     * @return
     */
    AggregatorDateIssueChart getAggregatorDateIssueChart(String aggregatorId, String resourceTypeId, String date);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param dateList
     * @return
     */
    List<AggregatorDateIssueChart> getAggregatorDateIssueChartList(String aggregatorId, String resourceTypeId, List<String> dateList);



    List<AggregatorDateIssueChart> getAggregatorDateIssueChartListNew(String aggregatorId, String resourceTypeId, List<String> dateList);






    /**
     * 删除数据
     *
     * @param aggregatorId
     * @param date
     * @return
     */
    int delete(String aggregatorId, String date);

    /**
     * 删除数据
     *
     * @param aggregatorId
     * @param date
     * @param resourceTypeId
     * @return
     */
    int delete(String aggregatorId, String date, String resourceTypeId);

    /**
     * 添加数据
     *
     * @param aggregatorDateIssueChartList
     * @return
     */
    int batchInsert(List<AggregatorDateIssueChart> aggregatorDateIssueChartList);

    /**
     * @description 数据删除
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    int delete(String aggregatorId, List<String> dateList, String resourceTypeId);

}
