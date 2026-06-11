package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorBaseLineLoadChart;
import cn.sl.ehub.service.vo.AggregatorDateIssueChart;

import java.util.List;

/**
 * 聚合商收益下发
 * @Author sl
 * @Date 2026-05-28
 **/
public interface IAggregatorBaseLineLoadChartService {
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
     * @param aggregatorBaseLineLoadChartList
     * @return
     */
    int batchInsert(List<AggregatorBaseLineLoadChart> aggregatorBaseLineLoadChartList);


    /**
     * @description 根据聚合商、时间区间、时间区间查询基线
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    List<AggregatorBaseLineLoadChart> getAggregatorBaseLine(String aggregatorId, String resourceTypeId,String startDate, String endDate);
    

}
