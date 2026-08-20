package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.console.model.req.AggregatorIssueProfitReq;
import cn.sl.ehub.console.model.req.SaveDevicePercentBaseLoadSqlReq;
import cn.sl.ehub.console.service.IAggregatorEntApplyPlanService;
import cn.sl.ehub.console.service.IAggregatorEntDateApplyDetailService;
import cn.sl.ehub.console.service.IAggregatorEntService;
import cn.sl.ehub.console.service.IAggregatorDateHolidayService;
import cn.sl.ehub.console.service.IDataSupportService;
import cn.sl.ehub.service.req.AggregatorEntApplyPlanReq;
import cn.sl.ehub.service.resp.AggregatorEntApplyPlanResp;
import cn.sl.ehub.service.vo.AggregatorEnt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 迁移期的数据支持服务。
 *
 * 收益计算仍保持停用，自动申报只负责把已提交的默认计划物化为日申报数据。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DataSupportServiceImpl implements IDataSupportService {

    private final IAggregatorEntService aggregatorEntService;
    private final IAggregatorEntDateApplyDetailService aggregatorEntDateApplyDetailService;
    private final IAggregatorEntApplyPlanService aggregatorEntApplyPlanService;
    private final IAggregatorDateHolidayService aggregatorDateHolidayService;

    @Override
    public Boolean addDeviceIotLog() {
        return false;
    }

    @Override
    public Boolean addDeviceBaselineLoadChart(String deviceBaseId, String startDate, String endDate, String date) {
        return false;
    }

    @Override
    public Boolean handAggregatorIssueChart(String aggregatorId, String winStatus, String date) {
        return false;
    }

    @Override
    public Boolean dealDevicePowerAndQuantity(String deviceBaseId, String date) {
        return false;
    }

    /**
     * 收益规则待确认，禁止在迁移期沿用旧计算模型。
     */
    @Override
    public Boolean handAggregatorIssueProfit(AggregatorIssueProfitReq req) {
        log.warn("收益计算尚未启用，忽略手工收益计算请求");
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAutoApplyPlan(String date) {
        String requestedDate = StringUtils.defaultIfBlank(date, DateUtils.getNextDay());
        if (DateUtils.parseDate(requestedDate) == null) {
            throw new BaseException(StatusCode.C.getCode(), "自动申报日期格式应为yyyy-MM-dd");
        }
        String referenceDate = DateUtils.getAddDate(requestedDate, -1);
        if (Boolean.TRUE.equals(aggregatorDateHolidayService.getApplyDateCheck(referenceDate))) {
            log.info("非工作日跳过企业默认计划自动申报，date={}", requestedDate);
            return true;
        }
        List<String> dateList = aggregatorDateHolidayService.getApplyDateList(referenceDate, false);
        if (dateList == null || dateList.isEmpty()) {
            dateList = Collections.singletonList(requestedDate);
        }
        List<AggregatorEnt> entList = aggregatorEntService.getAggregatorEntList();
        Map<String, AggregatorEnt> changedAggregatorEnt = new LinkedHashMap<>();
        String now = DateUtils.getTime();

        for (String targetDate : dateList) {
            for (AggregatorEnt ent : entList) {
                if (ent == null || StringUtils.isBlank(ent.getEntId())
                        || !isServiceDate(ent, targetDate)
                        || aggregatorEntDateApplyDetailService.getAggregatorEntDateApplyDetail(ent.getEntId(), targetDate) != null) {
                    continue;
                }
                AggregatorEntApplyPlanResp defaultPlan = aggregatorEntApplyPlanService
                        .getApplyPlanV1(ent.getEntId(), false, targetDate, true);
                if (defaultPlan == null || defaultPlan.getAppApplyIndexDeviceDetailRespList() == null
                        || defaultPlan.getAppApplyIndexDeviceDetailRespList().isEmpty()) {
                    continue;
                }
                AggregatorEntApplyPlanReq request = toDefaultPlanRequest(defaultPlan, ent, targetDate);
                List<String> singleDate = Collections.singletonList(targetDate);
                aggregatorEntApplyPlanService.saveAggregatorEntDateApplyDetail(request, singleDate, now);
                aggregatorEntApplyPlanService.saveAggregatorDeviceDateDeliveryChart(request, singleDate, now);
                aggregatorEntApplyPlanService.saveDevicePlan(request, singleDate);
                changedAggregatorEnt.putIfAbsent(ent.getAggregatorId(), ent);
            }
        }

        // Each aggregate curve is recomputed from all current device declaration curves once per aggregator.
        for (AggregatorEnt ent : changedAggregatorEnt.values()) {
            AggregatorEntApplyPlanReq request = new AggregatorEntApplyPlanReq();
            request.setEntId(ent.getEntId());
            request.setAggregatorId(ent.getAggregatorId());
            aggregatorEntApplyPlanService.saveAggregatorDateDeliveryChart(request, dateList);
        }
        log.info("企业默认计划自动申报完成，date={}, affectedAggregators={}",
                dateList, changedAggregatorEnt.size());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAggregatorEntDateApplyDetail(List<AggregatorEnt> aggregatorEntList,
                                                  List<String> dateList, String now) {
        if (aggregatorEntList == null || dateList == null) {
            return;
        }
        for (AggregatorEnt ent : aggregatorEntList) {
            if (ent == null || StringUtils.isBlank(ent.getEntId())) {
                continue;
            }
            for (String date : dateList) {
                if (aggregatorEntDateApplyDetailService.getAggregatorEntDateApplyDetail(ent.getEntId(), date) == null) {
                    saveDefaultPlanForDate(ent, date, now);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAggregatorEntDateApplyDetail(AggregatorEnt ent, String date, String now, String applyStatus) {
        if (ent == null || StringUtils.isBlank(ent.getEntId())
                || aggregatorEntDateApplyDetailService.getAggregatorEntDateApplyDetail(ent.getEntId(), date) != null) {
            return;
        }
        AggregatorEntApplyPlanResp plan = aggregatorEntApplyPlanService
                .getApplyPlanV1(ent.getEntId(), false, date, true);
        if (plan == null) {
            return;
        }
        aggregatorEntApplyPlanService.saveAggregatorEntDateApplyDetail(
                toDefaultPlanRequest(plan, ent, date), Collections.singletonList(date), now);
    }

    @Override
    public void saveAggregatorDeviceDateDeliveryChart(AggregatorEntApplyPlanResp applyPlan,
                                                       String date, AggregatorEnt ent) {
        if (applyPlan == null || ent == null) {
            return;
        }
        aggregatorEntApplyPlanService.saveAggregatorDeviceDateDeliveryChart(
                toDefaultPlanRequest(applyPlan, ent, date), Collections.singletonList(date), DateUtils.getTime());
    }

    @Override
    public void saveAggregatorDateDeliveryChart(List<String> dateList) {
        if (dateList == null || dateList.isEmpty()) {
            return;
        }
        Map<String, AggregatorEnt> representativeEnt = new LinkedHashMap<>();
        for (AggregatorEnt ent : aggregatorEntService.getAggregatorEntList()) {
            if (ent != null && StringUtils.isNotBlank(ent.getAggregatorId()) && StringUtils.isNotBlank(ent.getEntId())) {
                representativeEnt.putIfAbsent(ent.getAggregatorId(), ent);
            }
        }
        for (AggregatorEnt ent : representativeEnt.values()) {
            AggregatorEntApplyPlanReq request = new AggregatorEntApplyPlanReq();
            request.setAggregatorId(ent.getAggregatorId());
            request.setEntId(ent.getEntId());
            aggregatorEntApplyPlanService.saveAggregatorDateDeliveryChart(request, dateList);
        }
    }

    @Override
    public void saveDevicePlan(AggregatorEntApplyPlanResp applyPlan, String date) {
        if (applyPlan == null || StringUtils.isBlank(applyPlan.getEntId())) {
            return;
        }
        AggregatorEnt ent = aggregatorEntService.getAggregatorEnt(applyPlan.getEntId());
        if (ent != null) {
            aggregatorEntApplyPlanService.saveDevicePlan(
                    toDefaultPlanRequest(applyPlan, ent, date), Collections.singletonList(date));
        }
    }

    /**
     * 与收益分摊绑定的快照，待新结算规则定义后再实现。
     */
    @Override
    public void saveAggregatorDeviceDeliveryPowerPercent(List<String> dateList) {
        log.debug("跳过申报功率比例快照，收益规则尚未启用");
    }

    @Override
    public void excelImportWithApply(MultipartFile file) {
        throw new BaseException(StatusCode.C.getCode(), "申报导入尚未迁移");
    }

    @Override
    public void addApplyPlan(String entId, Boolean planStatus, Boolean saveStatus,
                             Double rate, Double dealPower, Boolean holidayFlag) {
        throw new BaseException(StatusCode.C.getCode(), "自动生成申报计划尚未迁移");
    }

    @Override
    public void saveDevicePercentBaseLoadSql(SaveDevicePercentBaseLoadSqlReq req) {
        throw new BaseException(StatusCode.C.getCode(), "基线比例导入尚未迁移");
    }

    @Override
    public Boolean addAggregatorAutoApplyPlan() {
        return false;
    }

    @Override
    public Boolean autoApplyPlan(String date) {
        return false;
    }

    private void saveDefaultPlanForDate(AggregatorEnt ent, String date, String now) {
        AggregatorEntApplyPlanResp defaultPlan = aggregatorEntApplyPlanService
                .getApplyPlanV1(ent.getEntId(), false, date, true);
        if (defaultPlan == null || defaultPlan.getAppApplyIndexDeviceDetailRespList() == null
                || defaultPlan.getAppApplyIndexDeviceDetailRespList().isEmpty()) {
            return;
        }
        AggregatorEntApplyPlanReq request = toDefaultPlanRequest(defaultPlan, ent, date);
        List<String> dateList = Collections.singletonList(date);
        aggregatorEntApplyPlanService.saveAggregatorEntDateApplyDetail(request, dateList, now);
        aggregatorEntApplyPlanService.saveAggregatorDeviceDateDeliveryChart(request, dateList, now);
        aggregatorEntApplyPlanService.saveDevicePlan(request, dateList);
    }

    private AggregatorEntApplyPlanReq toDefaultPlanRequest(AggregatorEntApplyPlanResp plan,
                                                            AggregatorEnt ent, String date) {
        AggregatorEntApplyPlanReq request = new AggregatorEntApplyPlanReq();
        request.setAggregatorId(ent.getAggregatorId());
        request.setEntId(ent.getEntId());
        request.setStartDate(date);
        request.setEndDate(date);
        request.setPlanStatus(false);
        request.setSaveStatus(true);
        request.setStatus(plan.getStatus());
        request.setAppApplyIndexDeviceDetailRespList(new ArrayList<>(plan.getAppApplyIndexDeviceDetailRespList()));
        return request;
    }

    private boolean isServiceDate(AggregatorEnt ent, String date) {
        return (StringUtils.isBlank(ent.getServiceStartDate()) || date.compareTo(ent.getServiceStartDate()) >= 0)
                && (StringUtils.isBlank(ent.getServiceEndDate()) || date.compareTo(ent.getServiceEndDate()) <= 0);
    }
}
