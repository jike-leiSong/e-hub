package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorAvgRtChart;
import cn.sl.ehub.service.vo.AggregatorBaseLineLoadChart;
import cn.sl.ehub.service.vo.AggregatorCrChart;
import cn.sl.ehub.service.vo.AggregatorDateIssueChart;

import java.util.List;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
public interface IAggregatorCrChartService {

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
     * @param aggregatorCrChartList
     * @return
     */
    int batchInsert(List<AggregatorCrChart> aggregatorCrChartList);

    /**
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param dateList
     * @return
     */
    List<AggregatorCrChart> getCrChart(String aggregatorId,String resourceTypeId,List<String> dateList);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param date
     * @return
     */
    AggregatorCrChart getAggregatorDateCrChart(String aggregatorId, String resourceTypeId, String date);

    List<AggregatorCrChart> getAggregatorDateCrChartListNew(String aggregatorId, String resourceTypeId, List<String> dateList);

    /**
     * @description 根据聚合商、时间区间、时间区间查询Cr曲线
     * @param
     * @return
     */
    List<AggregatorCrChart> getAggregatorCrLine(String aggregatorId, String resourceTypeId, String startDate, String endDate);
}
