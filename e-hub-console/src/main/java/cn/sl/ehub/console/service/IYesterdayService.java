package cn.sl.ehub.console.service;

import cn.sl.ehub.service.req.AggregatorEntDateInviteDetailReq;
import cn.sl.ehub.service.resp.AggregatorResourceTypeResp;
import cn.sl.ehub.service.resp.EntUserDeviceYesterdayChartResp;
import cn.sl.ehub.service.resp.EntUserOverviewResp;
import cn.sl.ehub.service.resp.IndexOverviewResp;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorResourceType;

import java.util.List;

/**
 * 曲线图Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IYesterdayService {

    /**
     * 查询上次收益
     *
     * @param aggregatorId
     * @return
     */
    IndexOverviewResp getLastProfit(String aggregatorId);

    /**
     * 昨日总览
     *
     * @param simulate
     * @param aggregatorId
     * @param resourceTypeId
     * @param dayType
     * @return
     */
    IndexOverviewResp getOverview(String simulate, String aggregatorId, String resourceTypeId, String dayType);

    /**
     * 查询设备列表
     *
     * @param aggregatorId
     * @param entId
     * @param stationId
     * @param resourceTypeId
     * @return
     */
    List<AggregatorEntDevice> getDeviceList(String aggregatorId, String entId, String stationId, String resourceTypeId);

    /**
     * 查询昨日设备曲线
     *
     * @param simulate
     * @param deviceBaseId
     * @param deviceList
     * @param date
     * @return
     */
    EntUserDeviceYesterdayChartResp getEntUserDeviceChartResp(String simulate, String deviceBaseId, List<AggregatorEntDevice> deviceList, String date);
    /**
     * 查询多设备昨日设备曲线
     *
     * @param simulate
     * @param deviceList
     * @param date
     * @return
     */
    EntUserDeviceYesterdayChartResp getEntUserDeviceListChartResp(String simulate,List<AggregatorEntDevice> deviceList, String date);

    EntUserDeviceYesterdayChartResp getEntUserChartResp(String simulate,List<AggregatorEntDevice> deviceList, String date,String entId);

    /**
     * 查询昨日设备曲线
     *
     * @param simulate
     * @param deviceBaseId
     * @param date
     * @return
     */
    EntUserDeviceYesterdayChartResp getPowerDetail(String simulate, String deviceBaseId, String date);

    /**
     * 用户情况
     *
     * @param aggregatorId
     * @param dayType
     * @return
     */
    List<EntUserOverviewResp> getEntUserOverviewResp(String aggregatorId, String dayType);

    /**
     * 企业用户邀约
     *
     * @param req
     * @return
     */
    String entInvite(AggregatorEntDateInviteDetailReq req);

}