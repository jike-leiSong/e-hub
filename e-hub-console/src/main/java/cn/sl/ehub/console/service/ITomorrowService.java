package cn.sl.ehub.console.service;

import cn.sl.ehub.service.req.AggregatorApplyOfferReq;
import cn.sl.ehub.service.req.AggregatorApplyReq;
import cn.sl.ehub.service.resp.*;

import java.util.List;

/**
 * 曲线图Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface ITomorrowService {

    /**
     * 查询设备曲线
     *
     * @param deviceBaseId
     * @return
     */
    EntUserDeviceTomorrowChartResp getEntUserDeviceTomorrowChartResp(String deviceBaseId);

    /**
     * 查询申报
     *
     * @param aggregatorId
     * @param date
     * @return
     */
    AggregatorApplyResp getAggregatorApply(String aggregatorId, String date);

    /**
     * 立即申报
     *
     * @param req
     * @return
     */
    Boolean updateAggregatorApply(AggregatorApplyReq req);


    /**
     * @description 自动申报
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    Boolean autoAggregatorApply(AggregatorApplyReq req);


    /**
     * 查询报价
     *
     * @param aggregatorId
     * @param date
     * @return
     */
    AggregatorApplyOfferResp getAggregatorApplyOfferResp(String aggregatorId, String date);

    /**
     * 暂存报价
     *
     * @param req
     * @param status
     * @return
     */
    Boolean saveAggregatorApplyOffer(AggregatorApplyOfferReq req, String status);

    /**
     * 用户申报汇总曲线
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param date
     * @return
     */
    IndexOverviewResp getAggregatorDeliveryChart(String aggregatorId, String resourceTypeId, String date);

    /**
     * 按资源类型查询报价
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param date
     * @return
     */
    List<AggregatorApplyOfferResourceDateResp> getPriceByResourceTypeId(String aggregatorId, String resourceTypeId, String date);
}
