package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorDeviceDateDeliveryChart;

import java.util.List;

/**
 * 设备申报曲线Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorDeviceDateDeliveryChartService {

    /**
     * 查询数据
     *
     * @param date
     * @return
     */
    List<AggregatorDeviceDateDeliveryChart> getAggregatorDeviceDateDeliveryChartList(String date);

    /**
     * 查询数据
     *
     * @param deviceBaseId
     * @param date
     * @return
     */
    AggregatorDeviceDateDeliveryChart getAggregatorDeviceDateDeliveryChart(String deviceBaseId, String date);

    /**
     * 查询数据
     *
     * @param aggregatorIdList
     * @param date
     * @return
     */
    List<AggregatorDeviceDateDeliveryChart> getAggregatorDeviceDateDeliveryChartList(List<String> aggregatorIdList, String date);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param date
     * @return
     */
    List<AggregatorDeviceDateDeliveryChart> getAggregatorDeviceDateDeliveryChartList(String aggregatorId, String date);

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
     * @param aggregatorDeviceDateDeliveryChartList
     * @return
     */
    int batchInsert(List<AggregatorDeviceDateDeliveryChart> aggregatorDeviceDateDeliveryChartList);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param dateList
     * @return
     */
    List<AggregatorDeviceDateDeliveryChart> getAggregatorDeviceDateDeliveryChartList(String aggregatorId, List<String> dateList);

    /**
     * 查询数据
     *
     * @param resourceTypeId
     * @param date
     * @return
     */
    List<AggregatorDeviceDateDeliveryChart> getAggregatorDeviceDateDeliveryChartListByResourceTypeId(String resourceTypeId, String date);

    /**
     * 查询数据
     *
     * @param deviceBaseIdList
     * @param dateList
     * @return
     */
    List<AggregatorDeviceDateDeliveryChart> getAggregatorDeviceDateDeliveryChartList(List<String> deviceBaseIdList, List<String> dateList);

    /**
     * 查询数据
     *
     * @param deviceBaseId
     * @param dateList
     * @return
     */
    List<AggregatorDeviceDateDeliveryChart> getAggregatorDeviceDateDeliveryChartListByDeviceBaseId(String deviceBaseId, List<String> dateList);
}
