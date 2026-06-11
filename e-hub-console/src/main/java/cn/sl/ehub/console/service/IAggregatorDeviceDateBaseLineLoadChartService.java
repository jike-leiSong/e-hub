package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorDeviceDateBaseLineLoadChart;

import java.util.List;

/**
 * 设备基线负荷Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorDeviceDateBaseLineLoadChartService {

    /**
     * 查询数据
     *
     * @param deviceBaseIdList
     * @param date
     * @return
     */
    List<AggregatorDeviceDateBaseLineLoadChart> getAggregatorDeviceDateBaseLineLoadChartList(List<String> deviceBaseIdList, String date);

    /**
     * 查询数据
     *
     * @param deviceBaseId
     * @param date
     * @return
     */
    AggregatorDeviceDateBaseLineLoadChart getAggregatorDeviceDateBaseLineLoadChart(String deviceBaseId, String date);

    /**
     * 查询数据
     *
     * @param deviceBaseIdList
     * @param dateList
     * @return
     */
    List<AggregatorDeviceDateBaseLineLoadChart> getAggregatorDeviceDateBaseLineLoadChartList(List<String> deviceBaseIdList, List<String> dateList);

    /**
     * 添加数据
     *
     * @return
     */
    int insert(AggregatorDeviceDateBaseLineLoadChart aggregatorDeviceDateBaseLineLoadChart);

    /**
     * 删除数据
     *
     * @param deviceBaseId
     * @param date
     * @return
     */
    int delete(String deviceBaseId, String date);

    /**
     * 批量删除数据
     *
     * @param deviceBaseIdList
     * @param date
     * @return
     */
    void batchDelete(List<String> deviceBaseIdList, String date);

    /**
     * 批量新增数据
     *
     * @param aggregatorDeviceDateBaseLineLoadChartList
     * @return
     */
    int batchInsert(List<AggregatorDeviceDateBaseLineLoadChart> aggregatorDeviceDateBaseLineLoadChartList);
}
