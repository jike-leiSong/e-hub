package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorDateDeliveryChart;

import java.util.List;

/**
 * 聚合商申报曲线Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorDateDeliveryChartService {

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param dateList
     * @return
     */
    List<AggregatorDateDeliveryChart> getAggregatorDateDeliveryChartList(String aggregatorId, List<String> dateList);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param date
     * @return
     */
    List<AggregatorDateDeliveryChart> getAggregatorDateDeliveryChartList(String aggregatorId, String date);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param date
     * @return
     */
    List<AggregatorDateDeliveryChart> getAggregatorDateDeliveryChartList(String aggregatorId, String resourceTypeId, String date);

    /**
     * 删除数据
     *
     * @param aggregatorId
     * @param dateList
     * @return
     */
    int delete(String aggregatorId, List<String> dateList);

    /**
     * 添加数据
     *
     * @param aggregatorDateDeliveryChartList
     * @return
     */
    int batchInsert(List<AggregatorDateDeliveryChart> aggregatorDateDeliveryChartList);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param dateList
     * @return
     */
    List<AggregatorDateDeliveryChart> getAggregatorDateDeliveryChartList(String aggregatorId, String resourceTypeId, List<String> dateList);

    /**
     * @description 删除数据
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    int delete(String aggregatorId, List<String> dateList,String sourceTypeId);

}
