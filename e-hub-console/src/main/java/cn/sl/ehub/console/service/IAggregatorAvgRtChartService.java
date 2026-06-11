package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorAvgRtChart;
import cn.sl.ehub.service.vo.AggregatorBaseLineLoadChart;

import java.util.List;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
public interface IAggregatorAvgRtChartService {
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
     * @param aggregatorAvgRtChartList
     * @return
     */
    int batchInsert(List<AggregatorAvgRtChart> aggregatorAvgRtChartList);

    /**
     * @description
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    List<AggregatorAvgRtChart> getAvgRtChart(String aggregatorId,String resourceTypeId,List<String> dateList);

}
