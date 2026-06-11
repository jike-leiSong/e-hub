package cn.sl.ehub.console.service;

import cn.sl.ehub.service.resp.AggregatorDeviceDeliveryPowerPercentDetail;
import cn.sl.ehub.service.vo.AggregatorDeviceDeliveryPowerPercent;
import cn.sl.ehub.service.vo.AggregatorResourceType;

import java.util.List;

/**
 * 设备申报功率比例Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorDeviceDeliveryPowerPercentService {

    /**
     * 添加数据
     *
     * @param aggregatorDeviceDeliveryPowerPercentList
     * @return
     */
    Integer batchInsert(List<AggregatorDeviceDeliveryPowerPercent> aggregatorDeviceDeliveryPowerPercentList);

    /**
     * 查询数据
     *
     * @param resourceTypeId
     * @param time
     * @return
     */
    List<AggregatorDeviceDeliveryPowerPercentDetail> getAggregatorDeviceDeliveryPowerPercentDetailList(String resourceTypeId, String time);

    /**
     * 查询数据
     *
     * @param resourceTypeId
     * @param date
     * @return
     */
    List<AggregatorDeviceDeliveryPowerPercent> getAggregatorDeviceDeliveryPowerPercentList(String resourceTypeId, String date);

    /**
     * 删除数据
     *
     * @param dateList
     * @return
     */
    Integer delete(List<String> dateList);

    /**
     * 更新聚合商ID
     *
     * @param oldAggregatorId
     * @param newAggregatorId
     * @param startId
     * @param endId
     */
    void updateDetailAggregatorId(String oldAggregatorId, String newAggregatorId, Long startId, Long endId);
}
