package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.console.model.req.AggregatorIssueProfitReq;
import cn.sl.ehub.console.req.SaveDevicePercentBaseLoadSqlReq;
import cn.sl.ehub.console.service.IDataSupportService;
import cn.sl.ehub.service.resp.AggregatorEntApplyPlanResp;
import cn.sl.ehub.service.vo.AggregatorEnt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@Service
public class DataSupportServiceImpl implements IDataSupportService {

    @Override
    public Boolean addDeviceIotLog() {
        return false;
    }

    @Override
    public Boolean addDeviceBaselineLoadChart(String deviceBaseId, String startDate, String endDate, String date, String simulate) {
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

    @Override
    public Boolean handAggregatorIssueProfit(AggregatorIssueProfitReq req) {
        return false;
    }

    @Override
    public Boolean addAutoApplyPlan(String date) {
        return false;
    }

    @Override
    public void saveAggregatorEntDateApplyDetail(List<AggregatorEnt> aggregatorEntList, List<String> dateList, String now) {
    }

    @Override
    public void saveAggregatorEntDateApplyDetail(AggregatorEnt aggregatorEnt, String date, String now, String applyStatus) {
    }

    @Override
    public void saveAggregatorDeviceDateDeliveryChart(AggregatorEntApplyPlanResp applyPlan, String date, AggregatorEnt ent) {
    }

    @Override
    public void saveAggregatorDateDeliveryChart(List<String> dateList) {
    }

    @Override
    public void saveDevicePlan(AggregatorEntApplyPlanResp applyPlan, String date) {
    }

    @Override
    public void saveAggregatorDeviceDeliveryPowerPercent(List<String> dateList) {
    }

    @Override
    public void excelImportWithApply(MultipartFile file) {
    }

    @Override
    public void addApplyPlan(String entId, Boolean planStatus, Boolean saveStatus, Double rate, Double dealPower, Boolean holidayFlag) {
    }

    @Override
    public void saveDevicePercentBaseLoadSql(SaveDevicePercentBaseLoadSqlReq req) {
    }

    @Override
    public Boolean addAggregatorAutoApplyPlan() {
        return false;
    }

    @Override
    public Boolean autoApplyPlan(String s) {
        return false;
    }
}
