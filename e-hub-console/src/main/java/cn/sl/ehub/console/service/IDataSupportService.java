package cn.sl.ehub.console.service;

import cn.sl.ehub.console.model.req.AggregatorIssueProfitReq;
import cn.sl.ehub.service.req.AggregatorEntApplyPlanReq;
import cn.sl.ehub.console.model.req.SaveDevicePercentBaseLoadSqlReq;
import cn.sl.ehub.service.resp.AggregatorEntApplyPlanResp;
import cn.sl.ehub.service.vo.AggregatorEnt;
import cn.sl.ehub.common.vo.DataResp;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 数据支持Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IDataSupportService {

    /**
     * 写入设备下发记录
     *
     * @return
     */
    Boolean addDeviceIotLog();

    /**
     * 写入基线负荷
     *
     * @param deviceBaseId
     * @param startDate
     * @param endDate
     * @param date
     * @param simulate
     * @return
     */
    Boolean addDeviceBaselineLoadChart(String deviceBaseId, String startDate, String endDate, String date, String simulate);

    /**
     * 华北网下发功率曲线-手动触发
     *
     * @param aggregatorId
     * @param winStatus
     * @param date
     * @return
     */
    Boolean handAggregatorIssueChart(String aggregatorId, String winStatus, String date);

    /**
     * 计算设备功率及用电量
     *
     * @param deviceBaseId
     * @param date
     * @return
     */
    Boolean dealDevicePowerAndQuantity(String deviceBaseId, String date);

    /**
     * 华北网下发收益
     *
     * @param req
     * @return
     */
    Boolean handAggregatorIssueProfit(AggregatorIssueProfitReq req);

    /**
     * 自动提交计划
     *
     * @param date
     * @return
     */
    Boolean addAutoApplyPlan(String date);

    /**
     * 写入企业申报详情
     *
     * @param aggregatorEntList
     * @param dateList
     * @param now
     */
    void saveAggregatorEntDateApplyDetail(List<AggregatorEnt> aggregatorEntList, List<String> dateList, String now);

    /**
     * 写入企业申报详情
     *
     * @param aggregatorEnt
     * @param date
     * @param now
     * @param applyStatus
     */
    void saveAggregatorEntDateApplyDetail(AggregatorEnt aggregatorEnt, String date, String now, String applyStatus);

    /**
     * 设备申报功率
     *
     * @param applyPlan
     * @param date
     * @param ent
     */
    void saveAggregatorDeviceDateDeliveryChart(AggregatorEntApplyPlanResp applyPlan, String date, AggregatorEnt ent);

    /**
     * 聚合商申报功率
     *
     * @param dateList
     */
    void saveAggregatorDateDeliveryChart(List<String> dateList);

    /**
     * 保存设备启停计划
     *
     * @param applyPlan
     * @param date
     */
    void saveDevicePlan(AggregatorEntApplyPlanResp applyPlan, String date);

    /**
     * 保存申报功率比例
     *
     * @param dateList
     */
    void saveAggregatorDeviceDeliveryPowerPercent(List<String> dateList);

    /**
     * 导入数据
     *
     * @param file
     * @return
     */
    void excelImportWithApply(MultipartFile file);

    /**
     * 添加计划
     *
     * @param entId
     * @param planStatus
     * @param saveStatus
     * @param rate
     * @param dealPower
     * @param holidayFlag
     */
    void addApplyPlan(String entId, Boolean planStatus, Boolean saveStatus, Double rate, Double dealPower, Boolean holidayFlag);

    /**
     * 生成设备比例基线负荷Sql
     *
     * @param req
     */
    void saveDevicePercentBaseLoadSql(SaveDevicePercentBaseLoadSqlReq req);

    /**
     * 聚合商自动提交申报计划
     *
     * @return
     */
    Boolean addAggregatorAutoApplyPlan();

    /**
     * @description 聚合商自动提交申报计划
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    Boolean autoApplyPlan(String s);
}
