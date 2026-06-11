package cn.sl.ehub.console.service;

import cn.sl.ehub.service.resp.ProfitExportResp;
import cn.sl.ehub.service.resp.AggregatorDeviceDateProfitResp;
import cn.sl.ehub.service.resp.AggregatorEntProfitResp;
import cn.sl.ehub.service.resp.AggregatorProfitResp;
import cn.sl.ehub.service.vo.AggregatorDateIssueChart;
import cn.sl.ehub.service.vo.AggregatorEntProfitTime;
import cn.sl.ehub.service.vo.AggregatorResourceDateIssueOffer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据处理Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IDataService {

    /**
     * 数据处理
     *
     * @param issue
     */
    void dealData(String issue);

    /**
     * 保存中标结果
     *
     * @param dataList
     * @param aggregatorId
     * @param resourceTypeId
     */
    void saveWinStatus(List<Map<String, String>> dataList, String aggregatorId, String resourceTypeId);

    /**
     * 保存下发出清价格
     *
     * @param dataList
     * @param aggregatorId
     * @param resourceTypeId
     */
    Map<String, Double> saveAggregatorResourceDateIssueOfferList(List<Map<String, String>> dataList, String aggregatorId, String resourceTypeId);

    /**
     * 保存下发功率
     *
     * @param dataList
     * @param aggregatorId
     * @param resourceTypeId
     */
    void saveAggregatorDateIssueChartList(List<Map<String, String>> dataList, String aggregatorId, String resourceTypeId);

    /**
     * 保存下发收益
     *
     * @param dataList
     * @param aggregatorId
     * @param resourceTypeId
     * @param offerMap
     */
    String saveAggregatorIssueProfit(List<Map<String, String>> dataList, String aggregatorId, String resourceTypeId, Map<String, Double> offerMap);

    /**
     * 计划聚合商和企业收益
     *
     * @param aggregatorId
     * @param date
     */
    void dealAggregatorAndEntProfit(String aggregatorId, String date);

    void dealAggregatorEntDateProfitOffer(String aggregatorId, String date);

    /**
     * 保存下发设备功率
     *
     * @param date
     * @param resourceTypeId
     * @param aggregatorDateIssueChartList
     */
    void saveAggregatorDeviceDateIssueChartList(String date, String resourceTypeId, List<AggregatorDateIssueChart> aggregatorDateIssueChartList);

    /**
     * 查询聚合商收收益
     *
     * @param startDate
     * @param endDate
     * @return
     */
    AggregatorProfitResp getAggregatorProfitResp(String startDate, String endDate);

    /**
     * 查询企业收益
     *
     * @param startDate
     * @param endDate
     * @return
     */
    Map<String, AggregatorEntProfitResp> getEntProfitRespMap(String startDate, String endDate);

    /**
     * 查询用电量
     *
     * @param startTime
     * @param endTime
     * @return
     */
    Map<String, Double> getElectricQuantity(String startTime, String endTime);

    /**
     * 查询用电量
     *
     * @param aggregatorId
     * @param date
     * @return
     */
    Map<String, LinkedHashMap<String, Double>> getElectricQuantityV1(String aggregatorId, String date);

    /**
     * 查询用电量
     *
     * @param aggregatorId
     * @param startDate
     * @param endDate
     * @return
     */
    Map<String, LinkedHashMap<String, Double>> getElectricQuantityV2(String aggregatorId, String startDate, String endDate);

    /**
     * 定时计算企业用电量
     *
     * @param date
     * @return
     */
    Boolean dealEntElectricQuantity(String date);

    /**
     * 实时计算用电量
     *
     * @param date
     * @return
     */
    List<AggregatorDeviceDateProfitResp> dealDevicePowerAndQuantity(String date);

    /**
     * 处理有效用电量
     *
     * @param aggregatorDeviceDateProfitRespList
     * @param entTimeMap
     * @return
     */
    Map<String, Double> getEntIdElectricQuantityMap(List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList, Map<String, List<AggregatorEntProfitTime>> entTimeMap);

    /**
     * 实时计算用电量
     *
     * @param date
     * @return
     */
    List<AggregatorDeviceDateProfitResp> dealDevicePowerAndQuantity(String entId, String date);

    /**
     * 补设备分解功率
     *
     * @param deviceBaseId
     * @param date
     * @return
     */
    Boolean dealDeviceIssuePower(String deviceBaseId, String date);

    /**
     * 补设备预计调节功率和调节电量
     *
     * @param deviceBaseId
     * @param date
     * @return
     */
    Boolean dealDeviceEstimatePower(String deviceBaseId, String date);

    /**
     * 处理报价问题
     *
     * @param date
     * @return
     */
    Boolean dealOffer(String date);

    /**
     * 查询数据
     *
     * @param startDate
     * @param endDate
     * @return
     */
    List<ProfitExportResp> profitImport(String startDate, String endDate);

    /**
     * 补设备收益表中出清价格
     *
     * @param deviceBaseId
     * @param date
     * @return
     */
    Boolean dealDeviceIssueOffer(String deviceBaseId, String date);

    /**
     * 修改聚合商ID
     *
     * @param oldAggregatorId
     * @param newAggregatorId
     * @param startId
     * @param endId
     * @return
     */
    Boolean updateAggregatorDeviceDeliveryPowerPercentWithAggregatorId(String oldAggregatorId, String newAggregatorId, Long startId, Long endId);

    /**
     * 修改聚合商ID
     *
     * @param oldAggregatorId
     * @param newAggregatorId
     * @param startId
     * @param endId
     * @return
     */
    Boolean updateAggregatorDeviceDateProfitWithAggregatorId(String oldAggregatorId, String newAggregatorId, Long startId, Long endId);
}
