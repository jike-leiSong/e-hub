package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.console.exception.BaseException;
import cn.sl.ehub.console.model.resp.ChartDataResp;
import cn.sl.ehub.console.service.IAggregatorApplyPlanService;
import cn.sl.ehub.console.service.IAggregatorDateApplyDetailService;
import cn.sl.ehub.console.service.IAggregatorDateDeliveryChartService;
import cn.sl.ehub.console.service.IAggregatorDateHolidayService;
import cn.sl.ehub.console.service.IAggregatorDateIssueChartService;
import cn.sl.ehub.console.service.IAggregatorEntDeviceService;
import cn.sl.ehub.service.mapper.AggregatorApplyPlanMapper;
import cn.sl.ehub.service.req.AddPlanDataReq;
import cn.sl.ehub.service.req.AddPlanReq;
import cn.sl.ehub.service.req.QueryPlanListReq;
import cn.sl.ehub.service.req.ReferDatePowerReq;
import cn.sl.ehub.service.service.IotTelemetryQueryService;
import cn.sl.ehub.service.resp.PlanDetailDataResp;
import cn.sl.ehub.service.resp.PlanDetailResp;
import cn.sl.ehub.service.resp.QueryPlanListDataResp;
import cn.sl.ehub.service.resp.QueryPlanListResp;
import cn.sl.ehub.service.resp.QueryPlanSourceListResp;
import cn.sl.ehub.service.resp.ReferDatePowerDataResp;
import cn.sl.ehub.service.resp.ReferDatePowerResp;
import cn.sl.ehub.service.vo.AggregatorApplyPlan;
import cn.sl.ehub.service.vo.AggregatorDateDeliveryChart;
import cn.sl.ehub.service.vo.AggregatorDateIssueChart;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 聚合商申报计划管理。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AggregatorApplyPlanServiceImpl implements IAggregatorApplyPlanService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_TIME_SECOND_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final IAggregatorEntDeviceService aggregatorEntDeviceService;
    private final IotTelemetryQueryService iotTelemetryQueryService;
    private final AggregatorApplyPlanMapper aggregatorApplyPlanMapper;
    private final IAggregatorDateDeliveryChartService aggregatorDateDeliveryChartService;
    private final IAggregatorDateIssueChartService aggregatorDateIssueChartService;
    private final IAggregatorDateApplyDetailService aggregatorDateApplyDetailService;
    private final IAggregatorDateHolidayService aggregatorDateHolidayService;

    @Override
    public ReferDatePowerResp getReferDatePower(ReferDatePowerReq req) {
        String aggregatorId = req.getAggregatorId();
        String resourceTypeId = req.getSourceId();
        LocalDate referDate = LocalDate.parse(req.getReferDate(), DATE_FORMATTER);

        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getDeviceList(aggregatorId, resourceTypeId);
        if (deviceList == null || deviceList.isEmpty()) {
            throw new BaseException(StatusCode.ERROR.getCode(), "设备信息不存在");
        }

        Set<Long> deviceIds = LoadAggregationChartSupport.newDeviceIdSet();
        Set<String> deviceCodes = LoadAggregationChartSupport.newDeviceCodeSet();
        LoadAggregationChartSupport.collectTelemetryScope(
                deviceList.stream()
                        .filter(item -> item != null
                                && Integer.valueOf(1).equals(item.getModelFlag())
                                && Integer.valueOf(1).equals(item.getStatus()))
                        .collect(Collectors.toList()),
                deviceIds,
                deviceCodes);
        if (deviceIds.isEmpty() && deviceCodes.isEmpty()) {
            throw new BaseException(StatusCode.ERROR.getCode(), "设备信息不存在");
        }

        List<DataResp> minutePowerList = iotTelemetryQueryService.sumPointValueByMinute(
                aggregatorId,
                new ArrayList<>(deviceIds),
                new ArrayList<>(deviceCodes),
                LoadAggregationChartSupport.POWER_POINT_CODE,
                LoadAggregationChartSupport.startOfDay(referDate, 15),
                LoadAggregationChartSupport.endOfDayInclusive(referDate));
        Map<String, Double> minuteValueMap = LoadAggregationChartSupport.toValueMap(minutePowerList);

        List<ReferDatePowerDataResp> dataList = new ArrayList<>(96);
        LocalDateTime firstPoint = referDate.atStartOfDay().plusMinutes(15);
        for (int i = 0; i < 96; i++) {
            LocalDateTime pointTime = firstPoint.plusMinutes(15L * i);
            String key = DATE_TIME_FORMATTER.format(pointTime);
            String displayTime = pointTime.toLocalTime().equals(LocalTime.MIN)
                    ? "24:00"
                    : TIME_FORMATTER.format(pointTime);

            ReferDatePowerDataResp data = new ReferDatePowerDataResp();
            data.setDate(displayTime);
            data.setValue(formatPowerValue(minuteValueMap.get(key), resourceTypeId));
            dataList.add(data);
        }

        ReferDatePowerResp resp = new ReferDatePowerResp();
        resp.setList(dataList);
        return resp;
    }

    @Override
    public QueryPlanListResp getPlanList(QueryPlanListReq req) {
        Weekend<AggregatorApplyPlan> weekend = Weekend.of(AggregatorApplyPlan.class);
        WeekendCriteria<AggregatorApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorApplyPlan::getAggregatorId, req.getAggregatorId());
        List<AggregatorApplyPlan> planList = aggregatorApplyPlanMapper.selectByExample(weekend);

        QueryPlanListResp resp = new QueryPlanListResp();
        if (planList == null || planList.isEmpty()) {
            resp.setList(Collections.emptyList());
            return resp;
        }

        LocalDate today = LocalDate.now();
        Map<String, List<AggregatorApplyPlan>> sourcePlanMap = planList.stream()
                .filter(item -> StringUtils.isNotBlank(item.getSourceId()))
                .collect(Collectors.groupingBy(AggregatorApplyPlan::getSourceId, LinkedHashMap::new, Collectors.toList()));

        List<QueryPlanSourceListResp> sourceList = new ArrayList<>();
        sourcePlanMap.forEach((sourceId, plans) -> {
            List<QueryPlanListDataResp> planDataList = plans.stream()
                    .sorted(Comparator.comparing(
                            (AggregatorApplyPlan item) -> StringUtils.defaultString(item.getUpdateTime())).reversed())
                    .map(item -> {
                        QueryPlanListDataResp data = new QueryPlanListDataResp();
                        BeanUtils.copyProperties(item, data);
                        data.setPlanStatus(getPlanStatus(
                                today,
                                LocalDate.parse(item.getStartDate(), DATE_FORMATTER),
                                LocalDate.parse(item.getEndDate(), DATE_FORMATTER)));
                        return data;
                    })
                    .filter(item -> !"0".equals(item.getPlanStatus()))
                    .collect(Collectors.toList());

            QueryPlanSourceListResp source = new QueryPlanSourceListResp();
            source.setSourceId(sourceId);
            source.setPlanDataList(planDataList);
            sourceList.add(source);
        });
        resp.setList(sourceList);
        return resp;
    }

    @Override
    public PlanDetailResp getPlanDetailById(String planId) {
        AggregatorApplyPlan plan = aggregatorApplyPlanMapper.selectByPrimaryKey(planId);
        if (plan == null) {
            throw new BaseException(StatusCode.ERROR.getCode(), "计划不存在");
        }

        Map<String, String> referDatePowerMap = parseChartMap(plan.getReferDatePower());
        Map<String, String> adjustFactorMap = parseChartMap(plan.getAdjustFactor());
        Map<String, String> adjustValueMap = parseChartMap(plan.getAdjustValue());
        Map<String, String> applyPowerMap = parseChartMap(plan.getApplyPower());
        Map<String, String> applyPriceMap = parseChartMap(plan.getApplyPrice());

        List<PlanDetailDataResp> dataList = new ArrayList<>(96);
        LocalTime firstPoint = LocalTime.MIN.plusMinutes(15);
        for (int i = 0; i < 96; i++) {
            String time = firstPoint.plusMinutes(15L * i).format(TIME_FORMATTER);
            if ("00:00".equals(time)) {
                time = "24:00";
            }

            PlanDetailDataResp data = new PlanDetailDataResp();
            data.setDateTime(time);
            data.setReferDatePower(referDatePowerMap.get(time));
            data.setAdjustFactor(adjustFactorMap.get(time));
            data.setAdjustValue(adjustValueMap.get(time));
            data.setApplyPower(applyPowerMap.get(time));
            data.setApplyPrice(applyPriceMap.get(time));
            dataList.add(data);
        }

        PlanDetailResp resp = new PlanDetailResp();
        BeanUtils.copyProperties(plan, resp);
        resp.setDataList(dataList);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addOrUpdatePlan(AddPlanReq req) {
        List<AddPlanDataReq> reqDataList = req.getDataList();
        if (reqDataList == null || reqDataList.size() != 96) {
            throw new BaseException(StatusCode.ERROR.getCode(), "数据集合必须包括96个时间点值");
        }

        AggregatorApplyPlan beforePlan = null;
        if (StringUtils.isNotBlank(req.getId())) {
            beforePlan = aggregatorApplyPlanMapper.selectByPrimaryKey(req.getId());
            if (beforePlan == null) {
                throw new BaseException(StatusCode.ERROR.getCode(), "计划不存在");
            }
        }

        String aggregatorId = req.getAggregatorId();
        String sourceId = req.getSourceId();
        LocalDate reqStartDate = LocalDate.parse(req.getStartDate(), DATE_FORMATTER);
        LocalDate reqEndDate = LocalDate.parse(req.getEndDate(), DATE_FORMATTER);
        checkDuplicatePlan(req, aggregatorId, sourceId, reqStartDate, reqEndDate);

        List<ChartDataResp> referDatePowerList = new ArrayList<>(96);
        List<ChartDataResp> adjustFactorList = new ArrayList<>(96);
        List<ChartDataResp> adjustValueList = new ArrayList<>(96);
        List<ChartDataResp> applyPowerList = new ArrayList<>(96);
        List<ChartDataResp> applyPriceList = new ArrayList<>(96);
        for (AddPlanDataReq dataReq : reqDataList) {
            String time = dataReq.getDateTime();
            referDatePowerList.add(buildChartData(time, dataReq.getReferDatePower()));
            adjustFactorList.add(buildChartData(time, dataReq.getAdjustFactor()));
            adjustValueList.add(buildChartData(time, dataReq.getAdjustValue()));
            applyPowerList.add(buildChartData(time, dataReq.getApplyPower()));
            applyPriceList.add(buildChartData(time, dataReq.getApplyPrice()));
        }

        AggregatorApplyPlan plan = new AggregatorApplyPlan();
        BeanUtils.copyProperties(req, plan);
        plan.setReferDatePower(JSONObject.toJSONString(referDatePowerList));
        plan.setAdjustFactor(JSONObject.toJSONString(adjustFactorList));
        plan.setAdjustValue(JSONObject.toJSONString(adjustValueList));
        plan.setApplyPower(JSONObject.toJSONString(applyPowerList));
        plan.setApplyPrice(JSONObject.toJSONString(applyPriceList));

        if (StringUtils.isNotBlank(req.getId())) {
            plan.setId(Integer.valueOf(req.getId()));
            aggregatorApplyPlanMapper.updateByPrimaryKeySelective(plan);
        } else {
            aggregatorApplyPlanMapper.insertSelective(plan);
        }

        refreshDeliveryAndIssueChart(req, beforePlan, reqStartDate, reqEndDate, applyPowerList);
        return true;
    }

    @Override
    public AggregatorApplyPlan getPlan(String aggregatorId, String sourceId, String date) {
        Weekend<AggregatorApplyPlan> weekend = Weekend.of(AggregatorApplyPlan.class);
        WeekendCriteria<AggregatorApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorApplyPlan::getAggregatorId, aggregatorId)
                .andEqualTo(AggregatorApplyPlan::getSourceId, sourceId);
        List<AggregatorApplyPlan> planList = aggregatorApplyPlanMapper.selectByExample(weekend);
        if (planList == null || planList.isEmpty()) {
            return null;
        }

        LocalDate targetDate = LocalDate.parse(date, DATE_FORMATTER);
        return planList.stream()
                .filter(item -> containsDate(item, targetDate))
                .findFirst()
                .orElse(null);
    }

    private void checkDuplicatePlan(AddPlanReq req,
                                    String aggregatorId,
                                    String sourceId,
                                    LocalDate reqStartDate,
                                    LocalDate reqEndDate) {
        Weekend<AggregatorApplyPlan> weekend = Weekend.of(AggregatorApplyPlan.class);
        WeekendCriteria<AggregatorApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorApplyPlan::getAggregatorId, aggregatorId)
                .andEqualTo(AggregatorApplyPlan::getSourceId, sourceId);
        if (StringUtils.isNotBlank(req.getId())) {
            criteria.andNotEqualTo(AggregatorApplyPlan::getId, req.getId());
        }
        List<AggregatorApplyPlan> planList = aggregatorApplyPlanMapper.selectByExample(weekend);
        if (planList == null || planList.isEmpty()) {
            return;
        }

        boolean repeat = planList.stream().anyMatch(item -> checkRepeatPlan(
                reqStartDate,
                reqEndDate,
                LocalDate.parse(item.getStartDate(), DATE_FORMATTER),
                LocalDate.parse(item.getEndDate(), DATE_FORMATTER)));
        if (repeat) {
            throw new BaseException(StatusCode.ERROR.getCode(), "计划周期重复，请重新选择周期");
        }
    }

    private void refreshDeliveryAndIssueChart(AddPlanReq req,
                                              AggregatorApplyPlan beforePlan,
                                              LocalDate reqStartDate,
                                              LocalDate reqEndDate,
                                              List<ChartDataResp> applyPowerList) {
        String aggregatorId = req.getAggregatorId();
        String sourceId = req.getSourceId();

        List<String> dateList = buildDateList(reqStartDate, reqEndDate);
        List<AggregatorDateDeliveryChart> deliveryChartList = new ArrayList<>();
        for (String date : dateList) {
            AggregatorDateDeliveryChart deliveryChart = new AggregatorDateDeliveryChart();
            deliveryChart.setAggregatorId(aggregatorId);
            deliveryChart.setResourceTypeId(sourceId);
            deliveryChart.setDate(date);
            deliveryChart.setDeliveryChart(JSONObject.toJSONString(buildDeliveryChartData(date, applyPowerList)));
            deliveryChartList.add(deliveryChart);
        }

        if (StringUtils.isNotBlank(req.getId()) && beforePlan != null) {
            dateList = resolveUpdateDeleteDateList(aggregatorId, beforePlan, reqStartDate, reqEndDate);
        }

        final List<String> activeDateList = dateList;
        log.info("删除调度申报功率曲线参数dateList={}", JSONObject.toJSONString(activeDateList));
        if (!activeDateList.isEmpty()) {
            aggregatorDateDeliveryChartService.delete(aggregatorId, activeDateList, sourceId);
        }

        deliveryChartList = deliveryChartList.stream()
                .filter(item -> activeDateList.contains(item.getDate()))
                .collect(Collectors.toList());
        log.info("新增调度申报功率曲线参数aggregatorDateDeliveryChartList={}", JSONObject.toJSONString(deliveryChartList));
        if (!deliveryChartList.isEmpty()) {
            aggregatorDateDeliveryChartService.batchInsert(deliveryChartList);
        }

        List<AggregatorDateIssueChart> issueChartList = deliveryChartList.stream().map(item -> {
            AggregatorDateIssueChart issueChart = new AggregatorDateIssueChart();
            BeanUtils.copyProperties(item, issueChart);
            issueChart.setIssueChart(item.getDeliveryChart());
            return issueChart;
        }).collect(Collectors.toList());

        log.info("删除调度下发功率曲线参数dateList={}", JSONObject.toJSONString(activeDateList));
        if (!activeDateList.isEmpty()) {
            aggregatorDateIssueChartService.delete(aggregatorId, activeDateList, sourceId);
        }
        log.info("新增调度下发功率曲线参数issueChartList={}", JSONObject.toJSONString(issueChartList));
        if (!issueChartList.isEmpty()) {
            aggregatorDateIssueChartService.batchInsert(issueChartList);
        }
    }

    private List<String> resolveUpdateDeleteDateList(String aggregatorId,
                                                     AggregatorApplyPlan beforePlan,
                                                     LocalDate reqStartDate,
                                                     LocalDate reqEndDate) {
        LocalDate deleteStartDate = reqStartDate;
        LocalDate beforeEndDate = LocalDate.parse(beforePlan.getEndDate(), DATE_FORMATTER);
        LocalDate deleteEndDate = beforeEndDate.isAfter(reqEndDate) || beforeEndDate.isEqual(reqEndDate)
                ? beforeEndDate
                : reqEndDate;

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        boolean tomorrowApplied = aggregatorDateApplyDetailService.checkDateAutoApply(
                aggregatorId,
                tomorrow.format(DATE_FORMATTER));
        if (tomorrowApplied) {
            List<String> holidayDateList = aggregatorDateHolidayService.getApplyDateList(tomorrow.format(DATE_FORMATTER), false);
            if (holidayDateList != null && !holidayDateList.isEmpty()) {
                LocalDate lastHolidayApplyDate = LocalDate.parse(holidayDateList.get(holidayDateList.size() - 1), DATE_FORMATTER);
                deleteStartDate = lastHolidayApplyDate.isAfter(tomorrow.plusDays(1))
                        ? lastHolidayApplyDate.plusDays(1)
                        : tomorrow.plusDays(1);
            } else {
                deleteStartDate = tomorrow.plusDays(1);
            }
        }
        return buildDateList(deleteStartDate, deleteEndDate);
    }

    private List<DataResp> buildDeliveryChartData(String date, List<ChartDataResp> applyPowerList) {
        LocalDate chartDate = LocalDate.parse(date, DATE_FORMATTER);
        return applyPowerList.stream().map(item -> {
            DataResp data = new DataResp();
            if ("24:00".equals(item.getTime())) {
                data.setTime(LocalDateTime.of(chartDate.plusDays(1), LocalTime.MIN).format(DATE_TIME_SECOND_FORMATTER));
            } else {
                data.setTime(date + " " + item.getTime() + ":00");
            }
            data.setValue(parseDoubleOrZero(item.getValue()));
            return data;
        }).collect(Collectors.toList());
    }

    private List<String> buildDateList(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            return Collections.emptyList();
        }
        long days = Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toDays();
        List<String> dateList = new ArrayList<>((int) days + 1);
        for (int i = 0; i <= days; i++) {
            dateList.add(startDate.plusDays(i).format(DATE_FORMATTER));
        }
        return dateList;
    }

    private Map<String, String> parseChartMap(String json) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyMap();
        }
        List<ChartDataResp> chartList;
        try {
            chartList = JSONObject.parseArray(json, ChartDataResp.class);
        } catch (Exception ex) {
            log.warn("计划曲线JSON解析失败: {}", json, ex);
            return Collections.emptyMap();
        }
        if (chartList == null || chartList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (ChartDataResp item : chartList) {
            if (item == null || StringUtils.isBlank(item.getTime())) {
                continue;
            }
            result.put(item.getTime(), item.getValue());
        }
        return result;
    }

    private ChartDataResp buildChartData(String time, String value) {
        ChartDataResp data = new ChartDataResp();
        data.setTime(time);
        data.setValue(value);
        return data;
    }

    private String formatPowerValue(Double value, String resourceTypeId) {
        if (value == null) {
            value = 0D;
        }
        if (LoadAggregationChartSupport.STORAGE_RESOURCE_TYPE_ID.equals(resourceTypeId)) {
            value = 0D - value;
        }
        return String.valueOf(MathUtils.doublePoint(value, 2));
    }

    private Double parseDoubleOrZero(String value) {
        if (StringUtils.isBlank(value)) {
            return 0D;
        }
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException ex) {
            return 0D;
        }
    }

    private boolean containsDate(AggregatorApplyPlan plan, LocalDate targetDate) {
        if (StringUtils.isBlank(plan.getStartDate()) || StringUtils.isBlank(plan.getEndDate())) {
            return false;
        }
        LocalDate startDate = LocalDate.parse(plan.getStartDate(), DATE_FORMATTER);
        LocalDate endDate = LocalDate.parse(plan.getEndDate(), DATE_FORMATTER);
        return !targetDate.isBefore(startDate) && !targetDate.isAfter(endDate);
    }

    private boolean checkRepeatPlan(LocalDate reqBeginDate, LocalDate reqEndDate, LocalDate beginDate, LocalDate endDate) {
        return !reqEndDate.isBefore(beginDate) && !reqBeginDate.isAfter(endDate);
    }

    /**
     * 0:已过期 1:待开始 2:执行中
     */
    private String getPlanStatus(LocalDate todayDate, LocalDate beginDate, LocalDate endDate) {
        if (todayDate.isBefore(beginDate)) {
            return "1";
        }
        if (!todayDate.isAfter(endDate)) {
            return "2";
        }
        return "0";
    }
}
