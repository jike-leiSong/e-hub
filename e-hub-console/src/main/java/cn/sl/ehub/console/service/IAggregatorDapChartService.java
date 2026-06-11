package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorDapChart;
import cn.sl.ehub.service.vo.AggregatorDateIssueChart;

import java.util.List;

public interface IAggregatorDapChartService {
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
     * @param aggregatorDapCharts
     * @return
     */
    int batchInsert(List<AggregatorDapChart> aggregatorDapCharts);

    /**
     * @description
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    List<AggregatorDapChart> getDapChart(String aggregatorId, String resourceTypeId, List<String> dateList);


    AggregatorDapChart getAggregatorDateDapChart(String aggregatorId, String resourceTypeId, String date);

    List<AggregatorDapChart> getAggregatorDateDapChart(String aggregatorId, String resourceTypeId, List<String> dateList);

    List<AggregatorDapChart> getAggregatorDateDapChartListNew(String aggregatorId, String resourceTypeId, List<String> dateList);

    List<AggregatorDapChart> getAggregatorDapChart(String aggregatorId, String resourceTypeId, String startDate, String endDate);
}
