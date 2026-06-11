package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorResourceDateDeliveryOffer;

import java.util.List;

/**
 * 申报价格Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorResourceDateDeliveryOfferService {

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param date
     * @return
     */
    List<AggregatorResourceDateDeliveryOffer> getAggregatorResourceDateDeliveryOfferList(String aggregatorId, String date);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param dateList
     * @return
     */
    List<AggregatorResourceDateDeliveryOffer> getAggregatorResourceDateDeliveryOfferList(String aggregatorId, List<String> dateList);

    /**
     * 添加数据
     *
     * @param aggregatorResourceDateDeliveryOfferList
     * @return
     */
    int batchInsert(List<AggregatorResourceDateDeliveryOffer> aggregatorResourceDateDeliveryOfferList);

    /**
     * 删除数据
     *
     * @param aggregatorId
     * @param dateList
     * @return
     */
    int delete(String aggregatorId, List<String> dateList);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param date
     * @param status
     * @return
     */
    int getCount(String aggregatorId, String date, String status);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param dateList
     * @return
     */
    int getCount(String aggregatorId, List<String> dateList);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param dateList
     * @param status
     * @return
     */
    int getCount(String aggregatorId, List<String> dateList, String status);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param dateList
     * @param status
     * @return
     */
    List<AggregatorResourceDateDeliveryOffer> getAggregatorResourceDateDeliveryOfferList(String aggregatorId, String resourceTypeId, List<String> dateList, String status);
}
