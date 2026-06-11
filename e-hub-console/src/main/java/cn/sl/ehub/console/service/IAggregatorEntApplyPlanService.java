package cn.sl.ehub.console.service;

import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.service.req.AggregatorEntApplyPlanReq;
import cn.sl.ehub.service.resp.AggregatorEntApplyDateResp;
import cn.sl.ehub.service.resp.AggregatorEntApplyPlanResp;
import cn.sl.ehub.service.resp.AggregatorEntApplyPlanStatusResp;
import cn.sl.ehub.service.resp.AggregatorEntDateDeviceStartStopPlanResp;
import cn.sl.ehub.service.vo.AggregatorEntApplyPlan;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 企业用户申报计划Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorEntApplyPlanService {

    /**
     * 查询数据
     *
     * @param entId
     * @param saveStatus
     * @param pageNo
     * @param pageSize
     * @return
     */
    PageResultVO<AggregatorEntApplyPlanResp> getAggregatorEntApplyPlanRespList(String entId, Boolean saveStatus, Integer pageNo, Integer pageSize);

    /**
     * 查询数据
     *
     * @param id
     * @return
     */
    AggregatorEntApplyPlanResp getAggregatorEntApplyPlanResp(String id);

    /**
     * 查询数据
     *
     * @param entId
     * @param date
     * @return
     */
    AggregatorEntApplyPlanResp getAggregatorEntApplyPlanResp(String entId, String date);

    /**
     * 添加数据
     *
     * @param aggregatorEntApplyPlanReq
     * @return
     */
    Boolean addApplyPlan(AggregatorEntApplyPlanReq aggregatorEntApplyPlanReq);

    /**
     * 写入企业申报详情，设备申报功率，聚合商申报功率==聚合商查询申报功率实时变化
     *
     * @param aggregatorEntApplyPlanReq
     * @param dateList
     * @param now
     */
    void addData(AggregatorEntApplyPlanReq aggregatorEntApplyPlanReq, List<String> dateList, String now);

    /**
     * 写入企业申报详情
     *
     * @param aggregatorEntApplyPlanReq
     * @param dateList
     * @param now
     */
    void saveAggregatorEntDateApplyDetail(AggregatorEntApplyPlanReq aggregatorEntApplyPlanReq, List<String> dateList, String now);

    /**
     * 设备申报功率
     *
     * @param aggregatorEntApplyPlanReq
     * @param dateList
     * @param now
     */
    void saveAggregatorDeviceDateDeliveryChart(AggregatorEntApplyPlanReq aggregatorEntApplyPlanReq, List<String> dateList, String now);

    /**
     * 聚合商申报功率
     *
     * @param aggregatorEntApplyPlanReq
     * @param dateList
     */
    void saveAggregatorDateDeliveryChart(AggregatorEntApplyPlanReq aggregatorEntApplyPlanReq, List<String> dateList);

    /**
     * 保存设备启停计划
     *
     * @param aggregatorEntApplyPlanReq
     * @param dateList
     */
    void saveDevicePlan(AggregatorEntApplyPlanReq aggregatorEntApplyPlanReq, List<String> dateList);

    /**
     * 查询数据
     *
     * @param entId
     * @param planStatus
     * @param date
     * @param saveStatus
     * @return
     */
    AggregatorEntApplyPlanResp getApplyPlan(String entId, Boolean planStatus, String date, Boolean saveStatus);

    /**
     * 查询数据
     *
     * @param entId
     * @param planStatus
     * @param saveStatus
     * @return
     */
    AggregatorEntApplyPlan getApplyPlan(String entId, Boolean planStatus, Boolean saveStatus);

    /**
     * 查询数据
     *
     * @param entId
     * @param planStatus
     * @param date
     * @param saveStatus
     * @return
     */
    AggregatorEntApplyPlanResp getApplyPlanV1(String entId, Boolean planStatus, String date, Boolean saveStatus);

    /**
     * 查询申报状态
     *
     * @param entId
     * @param date
     * @return
     */
    AggregatorEntApplyPlanStatusResp getApplyStatus(String entId, String date);

    /**
     * 查询设备启停计划
     *
     * @param entId
     * @param date
     * @return
     */
    List<AggregatorEntDateDeviceStartStopPlanResp> getDevicePlan(String entId, String date);

    /**
     * 查询数据
     *
     * @param entId
     * @return
     */
    AggregatorEntApplyPlanResp getDefaultPlanResp(String entId);

    /**
     * 查询数据
     *
     * @param entId
     * @param planStatus
     * @param date
     * @param saveStatus
     * @return
     */
    AggregatorEntApplyPlanResp getApplyPlanResp(String entId, Boolean planStatus, String date, Boolean saveStatus);

    /**
     * 查询数据
     *
     * @param entId
     * @param type
     * @param planType
     * @param date
     * @param pageNo
     * @param pageSize
     * @return
     */
    PageResultVO<AggregatorEntApplyPlanResp> getAggregatorEntApplyPlanRespList(String entId, String type, String planType, String date, Integer pageNo, Integer pageSize);

    /**
     * 添加数据
     *
     * @param aggregatorEntApplyPlanReq
     * @return
     */
    Boolean addApplyPlanV1(AggregatorEntApplyPlanReq aggregatorEntApplyPlanReq);

    /**
     * 查询数据
     *
     * @param entId
     * @param date
     * @return
     */
    AggregatorEntApplyDateResp getDate(String entId, String date);
}
