package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.console.service.IAggregatorEntDeviceService;
import cn.sl.ehub.console.service.IYesterdayService;
import cn.sl.ehub.service.req.AggregatorEntDateInviteDetailReq;
import cn.sl.ehub.service.resp.EntUserDeviceYesterdayChartResp;
import cn.sl.ehub.service.resp.EntUserOverviewResp;
import cn.sl.ehub.service.resp.IndexOverviewResp;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class YesterdayServiceImpl implements IYesterdayService {

    private final IAggregatorEntDeviceService aggregatorEntDeviceService;

    @Override
    public IndexOverviewResp getLastProfit(String aggregatorId) {
        return new IndexOverviewResp();
    }

    @Override
    public IndexOverviewResp getOverview(String aggregatorId, String resourceTypeId, String dayType) {
        return new IndexOverviewResp();
    }

    @Override
    public List<AggregatorEntDevice> getDeviceList(String aggregatorId, String entId, String stationId, String resourceTypeId) {
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(aggregatorId, entId, stationId, resourceTypeId);
        return deviceList == null ? Collections.emptyList() : deviceList;
    }

    @Override
    public EntUserDeviceYesterdayChartResp getEntUserDeviceChartResp(String deviceBaseId, List<AggregatorEntDevice> deviceList, String date) {
        return emptyChart();
    }

    @Override
    public EntUserDeviceYesterdayChartResp getEntUserDeviceListChartResp(List<AggregatorEntDevice> deviceList, String date) {
        return emptyChart();
    }

    @Override
    public EntUserDeviceYesterdayChartResp getEntUserChartResp(List<AggregatorEntDevice> deviceList, String date, String entId) {
        return emptyChart();
    }

    @Override
    public EntUserDeviceYesterdayChartResp getPowerDetail(String deviceBaseId, String date) {
        return emptyChart();
    }

    @Override
    public List<EntUserOverviewResp> getEntUserOverviewResp(String aggregatorId, String dayType) {
        return Collections.emptyList();
    }

    @Override
    public String entInvite(AggregatorEntDateInviteDetailReq req) {
        return "success";
    }

    private EntUserDeviceYesterdayChartResp emptyChart() {
        EntUserDeviceYesterdayChartResp resp = new EntUserDeviceYesterdayChartResp();
        resp.setBaseLineChart(Collections.emptyList());
        resp.setIssueChart(Collections.emptyList());
        resp.setPowerChart(Collections.emptyList());
        resp.setTimeColorRespList(Collections.emptyList());
        return resp;
    }
}
