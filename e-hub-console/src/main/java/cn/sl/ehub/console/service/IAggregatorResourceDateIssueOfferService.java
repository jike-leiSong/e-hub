package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorResourceDateDeliveryOffer;
import cn.sl.ehub.service.vo.AggregatorResourceDateIssueOffer;

import java.util.List;

/**
 * 下发报价Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorResourceDateIssueOfferService {

    /**
     * 查询数据
     *
     * @param dateList
     * @return
     */
    List<AggregatorResourceDateIssueOffer> getAggregatorResourceDateIssueOfferList(List<String> dateList);

    /**
     * 保存数据
     *
     * @param aggregatorId
     * @param date
     * @param aggregatorResourceDateIssueOfferList
     * @return
     */
    Integer saveAggregatorResourceDateDeliveryOffer(String aggregatorId, String date, List<AggregatorResourceDateIssueOffer> aggregatorResourceDateIssueOfferList);

    /**
     * 删除下发报价
     *
     * @param aggregatorId
     * @param date
     * @return
     */
    Integer deleteAggregatorResourceDateDeliveryOffer(String aggregatorId, String date);

    /**
     * 删除下发报价
     *
     * @param aggregatorId
     * @param date
     * @param resourceTypeId
     * @return
     */
    Integer deleteAggregatorResourceDateDeliveryOffer(String aggregatorId, String date, String resourceTypeId);

    /**
     * 批量添加数据
     *
     * @param aggregatorResourceDateIssueOfferList
     * @return
     */
    Integer batchInsertAggregatorResourceDateDeliveryOffer(List<AggregatorResourceDateIssueOffer> aggregatorResourceDateIssueOfferList);

    /**
     * 查询数据
     *
     * @param date
     * @return
     */
    List<AggregatorResourceDateIssueOffer> getAggregatorResourceDateIssueOfferList(String date);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param dateList
     * @return
     */
    List<AggregatorResourceDateIssueOffer> getAggregatorResourceDateIssueOfferList(String aggregatorId, List<String> dateList);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param dateList
     * @return
     */
    List<AggregatorResourceDateIssueOffer> getAggregatorResourceDateIssueOfferList(String aggregatorId, String resourceTypeId, List<String> dateList);

    /**
     * 更新数据
     *
     * @param aggregatorResourceDateIssueOffer
     * @return
     */
    int updateById(AggregatorResourceDateIssueOffer aggregatorResourceDateIssueOffer);

    List<AggregatorResourceDateIssueOffer> getAggregatorIssuePriceChart(String aggregatorId, String resourceTypeId, String startDate, String endDate);

}
