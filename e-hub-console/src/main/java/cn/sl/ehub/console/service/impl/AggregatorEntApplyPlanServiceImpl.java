package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.console.enums.AggregatorEntPlanTypeEnum;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.service.IAggregatorDateDeliveryChartService;
import cn.sl.ehub.console.service.IAggregatorDateHolidayService;
import cn.sl.ehub.console.service.IAggregatorDeviceDateDeliveryChartService;
import cn.sl.ehub.console.service.IAggregatorEntApplyPlanService;
import cn.sl.ehub.console.service.IAggregatorEntDateApplyDetailService;
import cn.sl.ehub.console.service.IAggregatorEntDateDeviceStartStopPlanService;
import cn.sl.ehub.console.service.IAggregatorEntDeviceService;
import cn.sl.ehub.console.service.IAggregatorEntService;
import cn.sl.ehub.service.mapper.AggregatorEntApplyPlanMapper;
import cn.sl.ehub.service.req.AggregatorEntApplyPlanReq;
import cn.sl.ehub.service.resp.AggregatorEntApplyDateResp;
import cn.sl.ehub.service.resp.AggregatorEntApplyPlanResp;
import cn.sl.ehub.service.resp.AggregatorEntApplyPlanStatusResp;
import cn.sl.ehub.service.resp.AggregatorEntDateDeviceStartStopPlanDetailResp;
import cn.sl.ehub.service.resp.AggregatorEntDateDeviceStartStopPlanResp;
import cn.sl.ehub.service.resp.AppApplyIndexDeviceDetailResp;
import cn.sl.ehub.service.resp.AppApplyIndexDeviceTimeDetailResp;
import cn.sl.ehub.service.vo.AggregatorDateDeliveryChart;
import cn.sl.ehub.service.vo.AggregatorDeviceDateDeliveryChart;
import cn.sl.ehub.service.vo.AggregatorEnt;
import cn.sl.ehub.service.vo.AggregatorEntApplyPlan;
import cn.sl.ehub.service.vo.AggregatorEntDateApplyDetail;
import cn.sl.ehub.service.vo.AggregatorEntDateDeviceStartStopPlan;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 企业用户申报计划服务。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AggregatorEntApplyPlanServiceImpl implements IAggregatorEntApplyPlanService {

    private static final String STORAGE_RESOURCE_TYPE = "27";
    private static final String HEAT_STORAGE_RESOURCE_TYPE = "26";

    private final AggregatorEntApplyPlanMapper aggregatorEntApplyPlanMapper;
    private final IAggregatorEntDeviceService aggregatorEntDeviceService;
    private final IAggregatorDateHolidayService aggregatorDateHolidayService;
    private final IAggregatorEntService aggregatorEntService;
    private final IAggregatorEntDateApplyDetailService aggregatorEntDateApplyDetailService;
    private final IAggregatorDeviceDateDeliveryChartService aggregatorDeviceDateDeliveryChartService;
    private final IAggregatorDateDeliveryChartService aggregatorDateDeliveryChartService;
    private final IAggregatorEntDateDeviceStartStopPlanService aggregatorEntDateDeviceStartStopPlanService;

    @Override
    public PageResultVO<AggregatorEntApplyPlanResp> getAggregatorEntApplyPlanRespList(
            String entId, Boolean saveStatus, Integer pageNo, Integer pageSize) {
        int safePageNo = normalizePageNo(pageNo);
        int safePageSize = normalizePageSize(pageSize);
        PageHelper.startPage(safePageNo, safePageSize);
        List<AggregatorEntApplyPlanResp> list = aggregatorEntApplyPlanMapper
                .getAggregatorEntApplyPlanRespList(requireEnt(entId).getEntId(), saveStatus == null || saveStatus);
        PageInfo<AggregatorEntApplyPlanResp> pageInfo = new PageInfo<>(list);
        list.forEach(this::fillShowDate);
        return pageResult(list, pageInfo.getTotal(), safePageNo, safePageSize);
    }

    @Override
    public AggregatorEntApplyPlanResp getAggregatorEntApplyPlanResp(String id) {
        if (StringUtils.isBlank(id)) {
            throw parameterError("申报计划ID不能为空");
        }
        AggregatorEntApplyPlan plan;
        try {
            plan = aggregatorEntApplyPlanMapper.selectByPrimaryKey(Integer.valueOf(id));
        } catch (NumberFormatException e) {
            throw parameterError("申报计划ID格式不正确");
        }
        if (plan == null) {
            throw new BaseException(StatusCode.E_J.getCode(), "申报计划不存在");
        }
        return toResp(plan, true);
    }

    @Override
    public AggregatorEntApplyPlanResp getAggregatorEntApplyPlanResp(String entId, String date) {
        requireEnt(entId);
        String queryDate = StringUtils.defaultIfBlank(date, DateUtils.getNextDay());
        List<AggregatorEntApplyPlan> plans = getAggregatorEntApplyPlanList(entId, queryDate, true);
        return plans.isEmpty() ? emptyPlanResp() : toResp(plans.get(0), true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addApplyPlan(AggregatorEntApplyPlanReq req) {
        validateAndNormalizeRequest(req);
        if (Boolean.TRUE.equals(req.getPlanStatus())) {
            List<String> dates = DateUtils.getDayList(req.getStartDate(), req.getEndDate());
            if (Boolean.TRUE.equals(req.getSaveStatus())
                    && aggregatorEntDateApplyDetailService.checkDate(req.getEntId(), dates)) {
                throw businessError("已经提交过临时计划");
            }
        } else if (Boolean.TRUE.equals(req.getSaveStatus()) && countPlan(req.getEntId(), false, true) > 0) {
            throw businessError("已经提交过默认计划");
        }
        return persistPlan(req);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addData(AggregatorEntApplyPlanReq req, List<String> dateList, String now) {
        if (!Boolean.TRUE.equals(req.getPlanStatus()) || CollectionUtils.isEmpty(dateList)) {
            return;
        }
        saveAggregatorEntDateApplyDetail(req, dateList, now);
        saveAggregatorDeviceDateDeliveryChart(req, dateList, now);
        saveAggregatorDateDeliveryChart(req, dateList);
        saveDevicePlan(req, dateList);
    }

    @Override
    public void saveAggregatorEntDateApplyDetail(AggregatorEntApplyPlanReq req, List<String> dateList, String now) {
        AggregatorEnt ent = requireEnt(req.getEntId());
        List<AggregatorEntDateApplyDetail> rows = new ArrayList<>();
        for (String date : dateList) {
            AggregatorEntDateApplyDetail row = new AggregatorEntDateApplyDetail();
            row.setAggregatorId(ent.getAggregatorId());
            row.setEntId(ent.getEntId());
            row.setStationId(ent.getStationId());
            row.setDate(date);
            row.setApplyDate(DateUtils.getDay(now));
            row.setApplyTime(now);
            row.setPlanStatus(req.getPlanStatus());
            row.setApplyStatus("1");
            rows.add(row);
        }
        aggregatorEntDateApplyDetailService.delete(ent.getEntId(), dateList);
        if (!rows.isEmpty()) {
            aggregatorEntDateApplyDetailService.batchInsert(rows);
        }
    }

    @Override
    public void saveAggregatorDeviceDateDeliveryChart(AggregatorEntApplyPlanReq req,
                                                       List<String> dateList, String now) {
        AggregatorEnt ent = requireEnt(req.getEntId());
        List<AggregatorDeviceDateDeliveryChart> rows = new ArrayList<>();
        List<AppApplyIndexDeviceDetailResp> devices = safeDevices(req);
        for (String date : dateList) {
            for (AppApplyIndexDeviceDetailResp device : devices) {
                AggregatorDeviceDateDeliveryChart row = new AggregatorDeviceDateDeliveryChart();
                row.setAggregatorId(ent.getAggregatorId());
                row.setEntId(ent.getEntId());
                row.setStationId(ent.getStationId());
                row.setResourceTypeId(device.getResourceTypeId());
                row.setDeviceBaseId(device.getDeviceBaseId());
                row.setDate(date);
                row.setDeliveryChart(JSONObject.toJSONString(buildDeviceDeliveryChart(date, device)));
                rows.add(row);
            }
        }
        aggregatorDeviceDateDeliveryChartService.delete(ent.getEntId(), dateList);
        if (!rows.isEmpty()) {
            aggregatorDeviceDateDeliveryChartService.batchInsert(rows);
        }
    }

    @Override
    public void saveAggregatorDateDeliveryChart(AggregatorEntApplyPlanReq req, List<String> dateList) {
        AggregatorEnt ent = requireEnt(req.getEntId());
        String aggregatorId = ent.getAggregatorId();
        List<AggregatorDeviceDateDeliveryChart> deviceCharts = aggregatorDeviceDateDeliveryChartService
                .getAggregatorDeviceDateDeliveryChartList(aggregatorId, dateList);
        Map<String, Map<String, List<DataResp>>> grouped = new LinkedHashMap<>();
        for (AggregatorDeviceDateDeliveryChart deviceChart : deviceCharts) {
            if (deviceChart == null || StringUtils.isBlank(deviceChart.getDeliveryChart())) {
                continue;
            }
            List<DataResp> points = JSONArray.parseArray(deviceChart.getDeliveryChart(), DataResp.class);
            grouped.computeIfAbsent(deviceChart.getDate(), key -> new LinkedHashMap<>())
                    .computeIfAbsent(deviceChart.getResourceTypeId(), key -> new ArrayList<>())
                    .addAll(points == null ? Collections.emptyList() : points);
        }

        List<AggregatorDateDeliveryChart> rows = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<DataResp>>> dateEntry : grouped.entrySet()) {
            for (Map.Entry<String, List<DataResp>> resourceEntry : dateEntry.getValue().entrySet()) {
                Map<String, Double> totals = resourceEntry.getValue().stream()
                        .filter(point -> point != null && StringUtils.isNotBlank(point.getTime()))
                        .collect(Collectors.toMap(DataResp::getTime,
                                point -> point.getValue() == null ? 0D : point.getValue(), Double::sum));
                List<DataResp> chart = totals.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(entry -> new DataResp(entry.getKey(), MathUtils.doublePoint(entry.getValue(), 8)))
                        .collect(Collectors.toList());
                AggregatorDateDeliveryChart row = new AggregatorDateDeliveryChart();
                row.setAggregatorId(aggregatorId);
                row.setResourceTypeId(resourceEntry.getKey());
                row.setDate(dateEntry.getKey());
                row.setDeliveryChart(JSONObject.toJSONString(chart));
                rows.add(row);
            }
        }
        aggregatorDateDeliveryChartService.delete(aggregatorId, dateList);
        if (!rows.isEmpty()) {
            aggregatorDateDeliveryChartService.batchInsert(rows);
        }
    }

    @Override
    public void saveDevicePlan(AggregatorEntApplyPlanReq req, List<String> dateList) {
        AggregatorEnt ent = requireEnt(req.getEntId());
        List<AggregatorEntDateDeviceStartStopPlan> rows = new ArrayList<>();
        for (String date : dateList) {
            List<AggregatorEntDateDeviceStartStopPlanDetailResp> details = new ArrayList<>();
            for (AppApplyIndexDeviceDetailResp device : safeDevices(req)) {
                for (AppApplyIndexDeviceTimeDetailResp time : safeTimes(device)) {
                    AggregatorEntDateDeviceStartStopPlanDetailResp detail = new AggregatorEntDateDeviceStartStopPlanDetailResp();
                    detail.setResourceTypeId(device.getResourceTypeId());
                    detail.setDeviceBaseId(device.getDeviceBaseId());
                    detail.setTime(time.getStartTime());
                    detail.setDetail(describeDevicePlan(device.getResourceTypeId(), time));
                    details.add(detail);
                }
            }
            if (!details.isEmpty()) {
                AggregatorEntDateDeviceStartStopPlan row = new AggregatorEntDateDeviceStartStopPlan();
                row.setAggregatorId(ent.getAggregatorId());
                row.setEntId(ent.getEntId());
                row.setDate(date);
                row.setDetail(JSONObject.toJSONString(details));
                rows.add(row);
            }
        }
        aggregatorEntDateDeviceStartStopPlanService.delete(ent.getEntId(), dateList);
        if (!rows.isEmpty()) {
            aggregatorEntDateDeviceStartStopPlanService.batchInsert(rows);
        }
    }

    @Override
    public AggregatorEntApplyPlanResp getApplyPlan(String entId, Boolean planStatus,
                                                   String date, Boolean saveStatus) {
        AggregatorEnt ent = requireEnt(entId);
        Boolean safePlanStatus = Boolean.TRUE.equals(planStatus);
        String today = DateUtils.getDay();
        Weekend<AggregatorEntApplyPlan> weekend = Weekend.of(AggregatorEntApplyPlan.class);
        WeekendCriteria<AggregatorEntApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntApplyPlan::getEntId, entId);
        criteria.andEqualTo(AggregatorEntApplyPlan::getPlanStatus, safePlanStatus);
        if (saveStatus != null) {
            criteria.andEqualTo(AggregatorEntApplyPlan::getSaveStatus, saveStatus);
        }
        criteria.andGreaterThanOrEqualTo(AggregatorEntApplyPlan::getApplyTime, today + " 00:00:00");
        criteria.andLessThanOrEqualTo(AggregatorEntApplyPlan::getApplyTime, today + " 23:59:59");
        weekend.orderBy("applyTime").desc();
        List<AggregatorEntApplyPlan> plans = aggregatorEntApplyPlanMapper.selectByExample(weekend);
        if (!plans.isEmpty()) {
            AggregatorEntApplyPlanResp resp = toResp(plans.get(0), true);
            if (safePlanStatus && StringUtils.isNotBlank(date)) {
                resp.setStartDate(date);
            }
            return resp;
        }
        AggregatorEntApplyPlanResp resp = emptyPlanResp();
        resp.setAggregatorId(ent.getAggregatorId());
        resp.setEntId(ent.getEntId());
        resp.setPlanStatus(safePlanStatus);
        resp.setStatus(true);
        if (safePlanStatus) {
            resp.setStartDate(DateUtils.getNextDay());
            resp.setEndDate(DateUtils.getNextDay());
        } else {
            resp.setStartDate(ent.getServiceStartDate());
            resp.setEndDate(ent.getServiceEndDate());
        }
        return resp;
    }

    @Override
    public AggregatorEntApplyPlan getApplyPlan(String entId, Boolean planStatus, Boolean saveStatus) {
        Weekend<AggregatorEntApplyPlan> weekend = Weekend.of(AggregatorEntApplyPlan.class);
        WeekendCriteria<AggregatorEntApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntApplyPlan::getEntId, entId);
        criteria.andEqualTo(AggregatorEntApplyPlan::getPlanStatus, planStatus);
        criteria.andEqualTo(AggregatorEntApplyPlan::getSaveStatus, saveStatus);
        weekend.orderBy("applyTime").desc();
        List<AggregatorEntApplyPlan> plans = aggregatorEntApplyPlanMapper.selectByExample(weekend);
        return plans.isEmpty() ? null : plans.get(0);
    }

    @Override
    public AggregatorEntApplyPlanResp getApplyPlanV1(String entId, Boolean planStatus,
                                                     String date, Boolean saveStatus) {
        return findPlanResp(entId, planStatus, date, saveStatus, true);
    }

    @Override
    public AggregatorEntApplyPlanResp getApplyPlanResp(String entId, Boolean planStatus,
                                                       String date, Boolean saveStatus) {
        AggregatorEntApplyPlanResp resp = findPlanResp(entId, planStatus, date, saveStatus, false);
        return resp == null ? emptyPlanResp() : resp;
    }

    @Override
    public AggregatorEntApplyPlanStatusResp getApplyStatus(String entId, String date) {
        AggregatorEnt ent = requireEnt(entId);
        String queryDate = StringUtils.defaultIfBlank(date, DateUtils.getNextDay());
        AggregatorEntApplyPlanStatusResp resp = new AggregatorEntApplyPlanStatusResp();
        if (countPlan(entId, false, true) == 0) {
            resp.setApplyStatus(0);
            resp.setShowTime(ent.getServiceStartDate() + " 00:00");
            return resp;
        }
        if (StringUtils.isNotBlank(ent.getServiceStartDate()) && StringUtils.isNotBlank(ent.getServiceEndDate())
                && (queryDate.compareTo(ent.getServiceStartDate()) < 0
                || queryDate.compareTo(ent.getServiceEndDate()) > 0)) {
            resp.setApplyStatus(2);
            resp.setShowTime(DateUtils.getAddDate(ent.getServiceStartDate(), -1) + " 00:00");
            return resp;
        }

        String now = DateUtils.getTime();
        String today = DateUtils.getDay();
        String nowTime = DateUtils.format(now, "HH:mm:ss");
        String allowTime = normalizeSecondTime(ent.getAllowApplyTime());
        AggregatorEntDateApplyDetail detail = aggregatorEntDateApplyDetailService
                .getAggregatorEntDateApplyDetail(entId, queryDate);
        if (detail == null) {
            resp.setApplyStatus(nowTime.compareTo(allowTime) < 0 ? 1 : 2);
            resp.setShowTime(nowTime.compareTo(allowTime) < 0 ? ent.getAllowApplyTime() : DateUtils.getNextDay() + " 00:00");
            return resp;
        }
        if (detail.getWinStatus() != null) {
            resp.setApplyStatus(detail.getWinStatus() ? 5 : 6);
            return resp;
        }
        if (Boolean.TRUE.equals(detail.getPlanStatus())) {
            resp.setApplyStatus(today.equals(detail.getApplyDate()) ? 4 : 3);
            resp.setShowTime(ent.getAllowApplyTime());
            resp.setWinTime(ent.getWinTime());
            return resp;
        }
        if (nowTime.compareTo(allowTime) < 0) {
            resp.setApplyStatus(1);
            resp.setShowTime(ent.getAllowApplyTime());
        } else if (today.equals(detail.getApplyDate())) {
            resp.setApplyStatus(2);
            resp.setShowTime(DateUtils.getNextDay() + " 00:00");
        } else {
            resp.setApplyStatus(3);
            resp.setShowTime(ent.getAllowApplyTime());
            resp.setWinTime(ent.getWinTime());
        }
        return resp;
    }

    @Override
    public List<AggregatorEntDateDeviceStartStopPlanResp> getDevicePlan(String entId, String date) {
        requireEnt(entId);
        String queryDate = StringUtils.defaultIfBlank(date, DateUtils.getDay());
        if (aggregatorEntDateApplyDetailService.getAggregatorEntDateApplyDetail(entId, queryDate) == null) {
            return Collections.emptyList();
        }
        AggregatorEntDateDeviceStartStopPlan plan = aggregatorEntDateDeviceStartStopPlanService
                .getAggregatorEntDateDeviceStartStopPlan(entId, queryDate);
        if (plan == null || StringUtils.isBlank(plan.getDetail())) {
            return Collections.emptyList();
        }
        List<AggregatorEntDateDeviceStartStopPlanDetailResp> details = JSONArray.parseArray(
                plan.getDetail(), AggregatorEntDateDeviceStartStopPlanDetailResp.class);
        Map<String, String> deviceNames = deviceNameMap(details.stream()
                .map(AggregatorEntDateDeviceStartStopPlanDetailResp::getDeviceBaseId)
                .collect(Collectors.toList()));
        List<AggregatorEntDateDeviceStartStopPlanResp> result = details.stream()
                .collect(Collectors.groupingBy(AggregatorEntDateDeviceStartStopPlanDetailResp::getTime))
                .entrySet().stream()
                .map(entry -> {
                    AggregatorEntDateDeviceStartStopPlanResp item = new AggregatorEntDateDeviceStartStopPlanResp();
                    item.setTime(entry.getKey());
                    item.setContentList(entry.getValue().stream()
                            .map(detail -> StringUtils.defaultString(deviceNames.get(detail.getDeviceBaseId()))
                                    + StringUtils.defaultString(detail.getDetail()))
                            .collect(Collectors.toList()));
                    return item;
                }).sorted(Comparator.comparing(AggregatorEntDateDeviceStartStopPlanResp::getTime))
                .collect(Collectors.toList());
        addBoundaryTime(result, "00:00", true);
        addBoundaryTime(result, "24:00", false);
        return result;
    }

    @Override
    public AggregatorEntApplyPlanResp getDefaultPlanResp(String entId) {
        requireEnt(entId);
        Weekend<AggregatorEntApplyPlan> weekend = Weekend.of(AggregatorEntApplyPlan.class);
        WeekendCriteria<AggregatorEntApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntApplyPlan::getEntId, entId);
        criteria.andEqualTo(AggregatorEntApplyPlan::getPlanStatus, false);
        criteria.andEqualTo(AggregatorEntApplyPlan::getSaveStatus, true);
        weekend.orderBy("applyTime").desc();
        List<AggregatorEntApplyPlan> plans = aggregatorEntApplyPlanMapper.selectByExample(weekend);
        return plans.isEmpty() ? emptyPlanResp() : toResp(plans.get(0), true);
    }

    @Override
    public PageResultVO<AggregatorEntApplyPlanResp> getAggregatorEntApplyPlanRespList(
            String entId, String type, String planType, String date, Integer pageNo, Integer pageSize) {
        requireEnt(entId);
        int safePageNo = normalizePageNo(pageNo);
        int safePageSize = normalizePageSize(pageSize);
        String today = DateUtils.getDay();
        List<AggregatorEntApplyPlanResp> source;
        if (StringUtils.isNotBlank(planType)) {
            if (AggregatorEntPlanTypeEnum.ALL.getCode().equals(planType)) {
                source = aggregatorEntApplyPlanMapper.getAggregatorEntApplyPlanRespListByPlanStatus(entId, true);
            } else if (AggregatorEntPlanTypeEnum.DEFAULT.getCode().equals(planType)) {
                source = aggregatorEntApplyPlanMapper.getAggregatorEntApplyPlanRespListByPlanStatus(entId, false);
            } else if (AggregatorEntPlanTypeEnum.FINISH.getCode().equals(planType)) {
                source = aggregatorEntApplyPlanMapper.getAggregatorEntApplyPlanRespListByFinish(entId, true, today);
            } else if (AggregatorEntPlanTypeEnum.NOW.getCode().equals(planType)) {
                source = aggregatorEntApplyPlanMapper.getAggregatorEntApplyPlanRespListByNowByLimitOne(entId, true, today);
            } else if (AggregatorEntPlanTypeEnum.NOSTART.getCode().equals(planType)) {
                source = aggregatorEntApplyPlanMapper.getAggregatorEntApplyPlanRespListByNoStart(entId, true, today);
            } else {
                source = Collections.emptyList();
            }
        } else if (StringUtils.isNotBlank(date)) {
            source = aggregatorEntApplyPlanMapper.getAggregatorEntApplyPlanRespListByNowByLimitOne(entId, true, date);
        } else {
            source = aggregatorEntApplyPlanMapper.getAggregatorEntApplyPlanRespListByEntId(entId);
        }
        boolean currentAssigned = false;
        for (AggregatorEntApplyPlanResp resp : source) {
            fillShowDate(resp);
            resp.setShowPlanSort("1");
            if (!Boolean.TRUE.equals(resp.getPlanStatus())) {
                resp.setShowPlanStatus(AggregatorEntPlanTypeEnum.DEFAULT.getCode());
            } else if (today.compareTo(resp.getEndDate()) > 0) {
                resp.setShowPlanStatus(AggregatorEntPlanTypeEnum.FINISH.getCode());
            } else if (today.compareTo(resp.getStartDate()) < 0
                    || AggregatorEntPlanTypeEnum.NOSTART.getCode().equals(planType)) {
                resp.setShowPlanStatus(AggregatorEntPlanTypeEnum.NOSTART.getCode());
            } else if (!currentAssigned) {
                resp.setShowPlanStatus(AggregatorEntPlanTypeEnum.NOW.getCode());
                resp.setShowPlanSort("0");
                currentAssigned = true;
            } else {
                resp.setShowPlanStatus(AggregatorEntPlanTypeEnum.NOSTART.getCode());
            }
        }
        source.sort(Comparator.comparing(AggregatorEntApplyPlanResp::getShowPlanSort)
                .thenComparing(AggregatorEntApplyPlanResp::getApplyTime,
                        Comparator.nullsLast(Comparator.reverseOrder())));
        int total = source.size();
        int from = Math.min((safePageNo - 1) * safePageSize, total);
        int to = Math.min(from + safePageSize, total);
        return pageResult(new ArrayList<>(source.subList(from, to)), total, safePageNo, safePageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addApplyPlanV1(AggregatorEntApplyPlanReq req) {
        // V1 derives the plan type from whether a submitted default plan exists.
        if (req != null && req.getPlanStatus() == null) {
            req.setPlanStatus(false);
        }
        validateAndNormalizeRequest(req);
        if (req.getStartDate().compareTo(req.getEndDate()) > 0) {
            throw parameterError("结束时间必须大于或等于开始时间");
        }
        AggregatorEntApplyDateResp allowed = getDate(req.getEntId(), DateUtils.getDay());
        if (StringUtils.isNotBlank(allowed.getStartDate())
                && allowed.getStartDate().compareTo(req.getStartDate()) > 0) {
            throw businessError("开始时间必须大于或等于：" + allowed.getStartDate());
        }
        req.setPlanStatus(countPlan(req.getEntId(), false, true) > 0);
        if (Boolean.TRUE.equals(req.getPlanStatus()) && Boolean.TRUE.equals(req.getSaveStatus())
                && aggregatorEntDateApplyDetailService.checkDate(req.getEntId(),
                DateUtils.getDayList(req.getStartDate(), req.getEndDate()))) {
            throw businessError("所选日期已经提交过计划");
        }
        return persistPlan(req);
    }

    @Override
    public AggregatorEntApplyDateResp getDate(String entId, String date) {
        AggregatorEnt ent = requireEnt(entId);
        String now = DateUtils.getTime();
        String queryDate = StringUtils.defaultIfBlank(date, DateUtils.getDay());
        if (DateUtils.format(now, "HH:mm:ss").compareTo(normalizeSecondTime(ent.getAllowApplyTime())) >= 0) {
            queryDate = DateUtils.getAddDate(queryDate, 1);
        }
        if (Boolean.TRUE.equals(aggregatorDateHolidayService.getApplyDateCheck(queryDate))) {
            List<String> dates = aggregatorDateHolidayService.getApplyDateList(queryDate, false);
            if (!dates.isEmpty()) {
                queryDate = DateUtils.getAddDate(dates.get(dates.size() - 1), 1);
            }
        } else {
            queryDate = DateUtils.getAddDate(queryDate, 1);
        }
        AggregatorEntApplyDateResp resp = new AggregatorEntApplyDateResp();
        resp.setStartDate(queryDate);
        resp.setEndDate(queryDate);
        return resp;
    }

    private Boolean persistPlan(AggregatorEntApplyPlanReq req) {
        String now = DateUtils.getTime();
        AggregatorEntApplyPlan draft = getApplyPlan(req.getEntId(), req.getPlanStatus(), false);
        if (draft != null && draft.getId() != null) {
            aggregatorEntApplyPlanMapper.deleteByPrimaryKey(draft.getId());
        }
        AggregatorEntApplyPlan plan = new AggregatorEntApplyPlan();
        BeanUtils.copyProperties(req, plan);
        plan.setApplyTime(now);
        plan.setDetail(JSONObject.toJSONString(safeDevices(req)));
        aggregatorEntApplyPlanMapper.insertSelective(plan);
        if (Boolean.TRUE.equals(req.getSaveStatus())) {
            addData(req, DateUtils.getDayList(req.getStartDate(), req.getEndDate()), now);
        }
        return true;
    }

    private void validateAndNormalizeRequest(AggregatorEntApplyPlanReq req) {
        if (req == null) {
            throw parameterError("申报计划不能为空");
        }
        AggregatorEnt ent = requireEnt(req.getEntId());
        if (StringUtils.isBlank(req.getStartDate()) || StringUtils.isBlank(req.getEndDate())) {
            throw parameterError("申报开始日期和结束日期不能为空");
        }
        if (DateUtils.parseDate(req.getStartDate()) == null || DateUtils.parseDate(req.getEndDate()) == null) {
            throw parameterError("申报日期格式应为yyyy-MM-dd");
        }
        if (req.getStartDate().compareTo(req.getEndDate()) > 0) {
            throw parameterError("结束时间必须大于或等于开始时间");
        }
        if (req.getPlanStatus() == null) {
            throw parameterError("计划类型不能为空");
        }
        if (req.getSaveStatus() == null) {
            req.setSaveStatus(false);
        }
        req.setAggregatorId(ent.getAggregatorId());
        validateDevices(ent, safeDevices(req));
    }

    private void validateDevices(AggregatorEnt ent, List<AppApplyIndexDeviceDetailResp> requested) {
        if (requested.isEmpty()) {
            throw parameterError("申报设备不能为空");
        }
        Set<String> ids = new HashSet<>();
        for (AppApplyIndexDeviceDetailResp device : requested) {
            if (device == null || StringUtils.isBlank(device.getDeviceBaseId())) {
                throw parameterError("申报设备ID不能为空");
            }
            if (!ids.add(device.getDeviceBaseId())) {
                throw parameterError("申报设备不能重复：" + device.getDeviceBaseId());
            }
            if (safeTimes(device).isEmpty()) {
                throw parameterError("设备申报时段不能为空：" + device.getDeviceBaseId());
            }
        }
        List<AggregatorEntDevice> stored = aggregatorEntDeviceService.getAggregatorEntDeviceList(new ArrayList<>(ids));
        Map<String, AggregatorEntDevice> storedMap = stored.stream().collect(Collectors.toMap(
                AggregatorEntDevice::getDeviceBaseId, Function.identity(), (left, right) -> left));
        for (AppApplyIndexDeviceDetailResp device : requested) {
            AggregatorEntDevice actual = storedMap.get(device.getDeviceBaseId());
            if (actual == null || !StringUtils.equals(ent.getEntId(), actual.getEntId())
                    || !StringUtils.equals(ent.getAggregatorId(), actual.getAggregatorId())) {
                throw new BaseException(StatusCode.U.getCode(), "设备不属于当前企业：" + device.getDeviceBaseId());
            }
            device.setResourceTypeId(actual.getResourceTypeId());
            List<AppApplyIndexDeviceTimeDetailResp> sortedTimes = new ArrayList<>(safeTimes(device));
            sortedTimes.sort(Comparator.comparingInt(time -> time == null
                    ? -1 : parseQuarterMinute(time.getStartTime())));
            device.setTimeList(sortedTimes);
            validateTimeSegments(device);
        }
    }

    private void validateTimeSegments(AppApplyIndexDeviceDetailResp device) {
        int previousEnd = -1;
        for (AppApplyIndexDeviceTimeDetailResp time : safeTimes(device)) {
            if (time == null || StringUtils.isBlank(time.getStartTime()) || StringUtils.isBlank(time.getEndTime())) {
                throw parameterError("设备申报时段不完整：" + device.getDeviceBaseId());
            }
            int startMinute = parseQuarterMinute(time.getStartTime());
            int endMinute = parseQuarterMinute(time.getEndTime());
            if (startMinute < 0 || endMinute < 0 || startMinute >= endMinute
                    || startMinute < previousEnd || startMinute % 15 != 0 || endMinute % 15 != 0) {
                throw parameterError("设备申报时段不合法：" + device.getDeviceBaseId());
            }
            previousEnd = endMinute;
        }
    }

    private int parseQuarterMinute(String value) {
        if ("24:00".equals(value)) {
            return 24 * 60;
        }
        if (StringUtils.isBlank(value) || !value.matches("\\d{2}:\\d{2}")) {
            return -1;
        }
        try {
            LocalTime time = LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm"));
            return time.getHour() * 60 + time.getMinute();
        } catch (DateTimeParseException ex) {
            return -1;
        }
    }

    private List<DataResp> buildDeviceDeliveryChart(String date, AppApplyIndexDeviceDetailResp device) {
        Map<String, Double> pointMap = new LinkedHashMap<>();
        for (AppApplyIndexDeviceTimeDetailResp segment : safeTimes(device)) {
            String start = date + " " + segment.getStartTime() + ":00";
            String end = "24:00".equals(segment.getEndTime())
                    ? DateUtils.getAddDate(date) + " 00:00:00"
                    : date + " " + segment.getEndTime() + ":00";
            List<String> minutes = DateUtils.getMinuteList(start, end, 15);
            double value = segment.getPower() == null ? 0D : segment.getPower();
            if (STORAGE_RESOURCE_TYPE.equals(device.getResourceTypeId())
                    && Integer.valueOf(-1).equals(segment.getUseStatus())) {
                value = -value;
            }
            for (int index = 1; index < minutes.size(); index++) {
                pointMap.put(minutes.get(index), value);
            }
        }
        return pointMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DataResp(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private String describeDevicePlan(String resourceTypeId, AppApplyIndexDeviceTimeDetailResp time) {
        Double power = time.getPower();
        if (STORAGE_RESOURCE_TYPE.equals(resourceTypeId) && time.getUseStatus() != null) {
            if (time.getUseStatus() == 1) {
                return "放电功率" + power + "kW";
            }
            if (time.getUseStatus() == -1) {
                return "充电功率" + power + "kW";
            }
            return "不充不放";
        }
        if (HEAT_STORAGE_RESOURCE_TYPE.equals(resourceTypeId)) {
            return power != null && power == 0D ? "在非蓄热状态" : "用电功率" + power + "kW";
        }
        return power != null && power != 0D ? "启动" : "停止";
    }

    private AggregatorEntApplyPlanResp findPlanResp(String entId, Boolean planStatus, String date,
                                                     Boolean saveStatus, boolean nullWhenMissing) {
        requireEnt(entId);
        Weekend<AggregatorEntApplyPlan> weekend = Weekend.of(AggregatorEntApplyPlan.class);
        WeekendCriteria<AggregatorEntApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntApplyPlan::getEntId, entId);
        criteria.andEqualTo(AggregatorEntApplyPlan::getPlanStatus, planStatus);
        criteria.andEqualTo(AggregatorEntApplyPlan::getSaveStatus, saveStatus);
        if (StringUtils.isNotBlank(date)) {
            criteria.andLessThanOrEqualTo(AggregatorEntApplyPlan::getStartDate, date);
            criteria.andGreaterThanOrEqualTo(AggregatorEntApplyPlan::getEndDate, date);
        }
        weekend.orderBy("applyTime").desc();
        List<AggregatorEntApplyPlan> plans = aggregatorEntApplyPlanMapper.selectByExample(weekend);
        if (plans.isEmpty()) {
            return nullWhenMissing ? null : emptyPlanResp();
        }
        return toResp(plans.get(0), true);
    }

    private List<AggregatorEntApplyPlan> getAggregatorEntApplyPlanList(String entId, String date, Boolean saveStatus) {
        Weekend<AggregatorEntApplyPlan> weekend = Weekend.of(AggregatorEntApplyPlan.class);
        WeekendCriteria<AggregatorEntApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntApplyPlan::getEntId, entId);
        criteria.andEqualTo(AggregatorEntApplyPlan::getSaveStatus, saveStatus);
        criteria.andLessThanOrEqualTo(AggregatorEntApplyPlan::getStartDate, date);
        criteria.andGreaterThanOrEqualTo(AggregatorEntApplyPlan::getEndDate, date);
        weekend.orderBy("applyTime").desc();
        return aggregatorEntApplyPlanMapper.selectByExample(weekend);
    }

    private int countPlan(String entId, Boolean planStatus, Boolean saveStatus) {
        Weekend<AggregatorEntApplyPlan> weekend = Weekend.of(AggregatorEntApplyPlan.class);
        WeekendCriteria<AggregatorEntApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntApplyPlan::getEntId, entId);
        criteria.andEqualTo(AggregatorEntApplyPlan::getPlanStatus, planStatus);
        criteria.andEqualTo(AggregatorEntApplyPlan::getSaveStatus, saveStatus);
        return aggregatorEntApplyPlanMapper.selectCountByExample(weekend);
    }

    private AggregatorEntApplyPlanResp toResp(AggregatorEntApplyPlan plan, boolean fillDeviceNames) {
        AggregatorEntApplyPlanResp resp = new AggregatorEntApplyPlanResp();
        BeanUtils.copyProperties(plan, resp);
        fillShowDate(resp);
        List<AppApplyIndexDeviceDetailResp> devices = StringUtils.isBlank(plan.getDetail())
                ? new ArrayList<>()
                : JSONArray.parseArray(plan.getDetail(), AppApplyIndexDeviceDetailResp.class);
        if (devices == null) {
            devices = new ArrayList<>();
        }
        if (fillDeviceNames && !devices.isEmpty()) {
            Map<String, String> names = deviceNameMap(devices.stream()
                    .map(AppApplyIndexDeviceDetailResp::getDeviceBaseId).collect(Collectors.toList()));
            devices.forEach(device -> device.setDeviceName(names.get(device.getDeviceBaseId())));
        }
        resp.setAppApplyIndexDeviceDetailRespList(devices);
        return resp;
    }

    private Map<String, String> deviceNameMap(List<String> deviceIds) {
        if (CollectionUtils.isEmpty(deviceIds)) {
            return Collections.emptyMap();
        }
        return aggregatorEntDeviceService.getAggregatorEntDeviceList(deviceIds).stream()
                .collect(Collectors.toMap(AggregatorEntDevice::getDeviceBaseId,
                        AggregatorEntDevice::getDeviceName, (left, right) -> left));
    }

    private void fillShowDate(AggregatorEntApplyPlanResp resp) {
        String start = formatChineseDate(resp.getStartDate());
        String end = formatChineseDate(resp.getEndDate());
        if (StringUtils.isBlank(start)) {
            resp.setShowDate(end);
        } else if (StringUtils.isBlank(end) || start.equals(end)) {
            resp.setShowDate(start);
        } else {
            resp.setShowDate(start + "~" + end);
        }
    }

    private String formatChineseDate(String date) {
        return StringUtils.isBlank(date) ? null : DateUtils.format(date + " 00:00:00", "yyyy年MM月dd日");
    }

    private AggregatorEnt requireEnt(String entId) {
        if (StringUtils.isBlank(entId)) {
            throw parameterError("企业ID不能为空");
        }
        AggregatorEnt ent = aggregatorEntService.getAggregatorEnt(entId);
        if (ent == null) {
            throw new BaseException(StatusCode.E_D.getCode(), "未查询到企业信息");
        }
        return ent;
    }

    private List<AppApplyIndexDeviceDetailResp> safeDevices(AggregatorEntApplyPlanReq req) {
        return req.getAppApplyIndexDeviceDetailRespList() == null
                ? Collections.emptyList() : req.getAppApplyIndexDeviceDetailRespList();
    }

    private List<AppApplyIndexDeviceTimeDetailResp> safeTimes(AppApplyIndexDeviceDetailResp device) {
        return device == null || device.getTimeList() == null ? Collections.emptyList() : device.getTimeList();
    }

    private AggregatorEntApplyPlanResp emptyPlanResp() {
        AggregatorEntApplyPlanResp resp = new AggregatorEntApplyPlanResp();
        resp.setAppApplyIndexDeviceDetailRespList(new ArrayList<>());
        return resp;
    }

    private void addBoundaryTime(List<AggregatorEntDateDeviceStartStopPlanResp> result,
                                 String time, boolean first) {
        if (result.stream().noneMatch(item -> time.equals(item.getTime()))) {
            AggregatorEntDateDeviceStartStopPlanResp boundary = new AggregatorEntDateDeviceStartStopPlanResp();
            boundary.setTime(time);
            boundary.setContentList(new ArrayList<>());
            if (first) {
                result.add(0, boundary);
            } else {
                result.add(boundary);
            }
        }
    }

    private String normalizeSecondTime(String time) {
        if (StringUtils.isBlank(time)) {
            return "23:59:59";
        }
        return time.length() == 5 ? time + ":00" : time;
    }

    private int normalizePageNo(Integer pageNo) {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 200);
    }

    private <T> PageResultVO<T> pageResult(List<T> list, long total, int pageNo, int pageSize) {
        PageResultVO<T> result = new PageResultVO<>();
        result.setList(list == null ? new ArrayList<>() : list);
        result.setTotal((int) Math.min(total, Integer.MAX_VALUE));
        result.setPageIndex(pageNo);
        result.setPageSize(pageSize);
        return result;
    }

    private BaseException parameterError(String message) {
        return new BaseException(StatusCode.C.getCode(), message);
    }

    private BaseException businessError(String message) {
        return new BaseException(StatusCode.ERROR.getCode(), message);
    }
}
