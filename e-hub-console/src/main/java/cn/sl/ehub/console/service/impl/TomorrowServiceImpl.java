package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.console.service.ITomorrowService;
import cn.sl.ehub.service.req.AggregatorApplyOfferReq;
import cn.sl.ehub.service.req.AggregatorApplyReq;
import cn.sl.ehub.service.resp.AggregatorApplyOfferResourceDateResp;
import cn.sl.ehub.service.resp.AggregatorApplyOfferResp;
import cn.sl.ehub.service.resp.AggregatorApplyResp;
import cn.sl.ehub.service.resp.EntUserDeviceTomorrowChartResp;
import cn.sl.ehub.service.resp.IndexOverviewResp;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class TomorrowServiceImpl implements ITomorrowService {

    @Override
    public EntUserDeviceTomorrowChartResp getEntUserDeviceTomorrowChartResp(String deviceBaseId) {
        EntUserDeviceTomorrowChartResp resp = new EntUserDeviceTomorrowChartResp();
        resp.setDeliveryChart(Collections.emptyList());
        resp.setIssueChart(Collections.emptyList());
        return resp;
    }

    @Override
    public AggregatorApplyResp getAggregatorApply(String aggregatorId, String date) {
        AggregatorApplyResp resp = new AggregatorApplyResp();
        resp.setApplyStatus("0");
        resp.setApplyPriceStatus("0");
        resp.setEntNum(0);
        resp.setApplyNoNum(0);
        resp.setApplyYesNum(0);
        return resp;
    }

    @Override
    public Boolean updateAggregatorApply(AggregatorApplyReq req) {
        return false;
    }

    @Override
    public Boolean autoAggregatorApply(AggregatorApplyReq req) {
        return false;
    }

    @Override
    public AggregatorApplyOfferResp getAggregatorApplyOfferResp(String aggregatorId, String date) {
        AggregatorApplyOfferResp resp = new AggregatorApplyOfferResp();
        resp.setStatus("0");
        resp.setResourceList(Collections.emptyList());
        return resp;
    }

    @Override
    public Boolean saveAggregatorApplyOffer(AggregatorApplyOfferReq req, String status) {
        return false;
    }

    @Override
    public IndexOverviewResp getAggregatorDeliveryChart(String aggregatorId, String resourceTypeId, String date) {
        return new IndexOverviewResp();
    }

    @Override
    public List<AggregatorApplyOfferResourceDateResp> getPriceByResourceTypeId(String aggregatorId, String resourceTypeId, String date) {
        return Collections.emptyList();
    }
}
