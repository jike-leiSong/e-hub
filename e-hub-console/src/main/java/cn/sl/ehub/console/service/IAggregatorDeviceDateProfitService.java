package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorDeviceDateProfit;

import java.util.List;

/**
 * 设备收益Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorDeviceDateProfitService {

    /**
     * 添加数据
     *
     * @param aggregatorDeviceDateProfitList
     * @return
     */
    int batchInsert(List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param date
     * @return
     */
    List<AggregatorDeviceDateProfit> getAggregatorDeviceDateProfitList(String aggregatorId, String date);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param date
     * @return
     */
    List<AggregatorDeviceDateProfit> getAggregatorDeviceDateProfitList(String aggregatorId, String resourceTypeId, String date);

    /**
     * 更新数据
     *
     * @param aggregatorDeviceDateProfitList
     * @return
     */
    int updateListById(List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList);

    /**
     * 删除数据
     *
     * @param deviceBaseId
     * @param date
     * @return
     */
    int delete(String deviceBaseId, String date);

    /**
     * 删除数据
     *
     * @param aggregatorId
     * @param date
     * @return
     */
    int deleteByAggregatorId(String aggregatorId, String date);

    /**
     * 删除数据
     *
     * @param aggregatorId
     * @param date
     * @param resourceTypeId
     * @return
     */
    int deleteByAggregatorId(String aggregatorId, String date, String resourceTypeId);

    /**
     * 删除数据
     *
     * @param deviceBaseIdList
     * @param date
     * @return
     */
    int delete(List<String> deviceBaseIdList, String date);

    /**
     * 保存数据
     *
     * @param deviceBaseId
     * @param date
     * @param aggregatorDeviceDateProfitList
     * @return
     */
    int save(String deviceBaseId, String date, List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList);

    /**
     * 保存数据
     *
     * @param aggregatorId
     * @param date
     * @param aggregatorDeviceDateProfitList
     * @return
     */
    int saveByAggregatorId(String aggregatorId, String date, List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList);

    /**
     * 保存数据
     *
     * @param aggregatorId
     * @param date
     * @param resourceTypeId
     * @param aggregatorDeviceDateProfitList
     * @return
     */
    int saveByAggregatorId(String aggregatorId, String date, String resourceTypeId, List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList);

    /**
     * 保存数据
     *
     * @param deviceBaseIdList
     * @param date
     * @param aggregatorDeviceDateProfitList
     * @return
     */
    int save(List<String> deviceBaseIdList, String date, List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList);

    /**
     * 查询数据
     *
     * @param deviceBaseId
     * @param dateList
     * @return
     */
    List<AggregatorDeviceDateProfit> getAggregatorDeviceDateProfitList(String deviceBaseId, List<String> dateList);

    /**
     * 查询数据
     *
     * @param date
     * @return
     */
    List<AggregatorDeviceDateProfit> getAggregatorDeviceDateProfitList(String date);

    /**
     * 查询数据
     *
     * @param dateList
     * @return
     */
    List<AggregatorDeviceDateProfit> getAggregatorDeviceDateProfitList(List<String> dateList);

    /**
     * 查询数据
     *
     * @param deviceBaseId
     * @param date
     * @return
     */
    List<AggregatorDeviceDateProfit> getAggregatorDeviceDateProfitListByDeviceBaseIdAndDate(String deviceBaseId, String date);

    /**
     * 查询数据
     *
     * @param entId
     * @param dateList
     * @return
     */
    List<AggregatorDeviceDateProfit> getAggregatorDeviceDateProfitListByEntId(String entId, List<String> dateList);

    /**
     * 修改聚合商ID
     *
     * @param oldAggregatorId
     * @param newAggregatorId
     * @param startId
     * @param endId
     */
    void updateAggregatorId(String oldAggregatorId, String newAggregatorId, Long startId, Long endId);
}
