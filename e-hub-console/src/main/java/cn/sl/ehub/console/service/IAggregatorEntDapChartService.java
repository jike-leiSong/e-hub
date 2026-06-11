package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorEntDapChart;

import java.util.List;
import java.util.Map;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
public interface IAggregatorEntDapChartService {

    /**
     * 批量删除数据
     *
     * @param entIdList
     * @param date
     * @return
     */
    void batchDelete(List<String> entIdList, String date);

    /**
     * 批量新增数据
     *
     * @param
     * @return
     */
    int batchInsert(List<AggregatorEntDapChart> AggregatorEntDapChartList);


    /**
     * @description 查询企业前计划
     * @param
     * @return
     */
    List<AggregatorEntDapChart> getEntDapLine(String entId, String resourceTypeId, String startDate, String endDate);

    /**
     * @description 查询多个企业日前计划
     * @param
     * @return
     */
    Map<String,List<AggregatorEntDapChart>> getMoreEntDapLine(List<String> entIdList, String resourceTypeId, String startDate, String endDate);


    /**
     * @description 查询企业日前计划
     * @param
     * @return
     */
    List<AggregatorEntDapChart> getEntDapLineByDate(String entId, String resourceTypeId, String date);

    /**
     * @description 查询聚合商日前计划
     * @param
     * @return
     */
    List<AggregatorEntDapChart> getAggregatorDapLineByDate(String aggregatorId, String resourceTypeId, String date);

    List<AggregatorEntDapChart> getBatchDapLineByEntId(List<String> entIds,String date);
}
