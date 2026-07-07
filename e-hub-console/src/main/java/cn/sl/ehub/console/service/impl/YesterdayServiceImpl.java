package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.console.enums.DayTypeEnum;
import cn.sl.ehub.console.service.IAggregatorCrChartService;
import cn.sl.ehub.console.service.IAggregatorDateDeliveryChartService;
import cn.sl.ehub.console.service.IAggregatorDateHolidayService;
import cn.sl.ehub.console.service.IAggregatorDateIssueChartService;
import cn.sl.ehub.console.service.IAggregatorDateProfitService;
import cn.sl.ehub.console.service.IAggregatorDapChartService;
import cn.sl.ehub.console.service.IAggregatorDeviceDateBaseLineLoadChartService;
import cn.sl.ehub.console.service.IAggregatorDeviceDateIssueChartService;
import cn.sl.ehub.console.service.IAggregatorEntBaseLineLoadChartService;
import cn.sl.ehub.console.service.IAggregatorEntDateApplyDetailService;
import cn.sl.ehub.console.service.IAggregatorEntDateProfitService;
import cn.sl.ehub.console.service.IAggregatorEntDeviceService;
import cn.sl.ehub.console.service.IAggregatorEntService;
import cn.sl.ehub.console.service.IYesterdayService;
import cn.sl.ehub.service.mapper.IotTelemetryMinuteMapper;
import cn.sl.ehub.service.req.AggregatorEntDateInviteDetailReq;
import cn.sl.ehub.service.resp.EntUserDeviceYesterdayChartResp;
import cn.sl.ehub.service.resp.EntUserOverviewResp;
import cn.sl.ehub.service.resp.IndexOverviewResp;
import cn.sl.ehub.service.vo.AggregatorCrChart;
import cn.sl.ehub.service.vo.AggregatorDapChart;
import cn.sl.ehub.service.vo.AggregatorDateDeliveryChart;
import cn.sl.ehub.service.vo.AggregatorDateIssueChart;
import cn.sl.ehub.service.vo.AggregatorDateProfit;
import cn.sl.ehub.service.vo.AggregatorDeviceDateBaseLineLoadChart;
import cn.sl.ehub.service.vo.AggregatorDeviceDateIssueChart;
import cn.sl.ehub.service.vo.AggregatorEnt;
import cn.sl.ehub.service.vo.AggregatorEntBaseLineLoadChart;
import cn.sl.ehub.service.vo.AggregatorEntDateApplyDetail;
import cn.sl.ehub.service.vo.AggregatorEntDateProfit;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YesterdayServiceImpl implements IYesterdayService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final IAggregatorDateProfitService aggregatorDateProfitService;
    private final IAggregatorDateIssueChartService aggregatorDateIssueChartService;
    private final IAggregatorCrChartService aggregatorCrChartService;
    private final IAggregatorDapChartService aggregatorDapChartService;
    private final IAggregatorEntDeviceService aggregatorEntDeviceService;
    private final IAggregatorDateDeliveryChartService aggregatorDateDeliveryChartService;
    private final IAggregatorDeviceDateBaseLineLoadChartService aggregatorDeviceDateBaseLineLoadChartService;
    private final IAggregatorDeviceDateIssueChartService aggregatorDeviceDateIssueChartService;
    private final IAggregatorEntBaseLineLoadChartService aggregatorEntBaseLineLoadChartService;
    private final IAggregatorEntService aggregatorEntService;
    private final IAggregatorEntDateApplyDetailService aggregatorEntDateApplyDetailService;
    private final IAggregatorEntDateProfitService aggregatorEntDateProfitService;
    private final IAggregatorDateHolidayService aggregatorDateHolidayService;
    private final IotTelemetryMinuteMapper iotTelemetryMinuteMapper;

    @Override
    public IndexOverviewResp getLastProfit(String aggregatorId) {
        IndexOverviewResp resp = new IndexOverviewResp();
        AggregatorDateProfit profit = aggregatorDateProfitService.getAggregatorDateProfit(aggregatorId, null);
        if (profit != null) {
            resp.setTotalProfit(profit.getIssueProfit() == null ? 0D : profit.getIssueProfit());
            resp.setTotalProfitTime(profit.getDate());
        }
        return resp;
    }

    @Override
    public IndexOverviewResp getOverview(String aggregatorId, String resourceTypeId, String dayType) {
        if (DayTypeEnum.TOMORROW.getCode().equals(dayType)) {
            return buildTomorrowOverview(aggregatorId, resourceTypeId);
        }
        LocalDate targetDate = resolveTargetDate(dayType);
        String date = DATE_FORMATTER.format(targetDate);
        List<String> minuteAxis = LoadAggregationChartSupport.buildDayMinuteDateTimeAxis(targetDate);

        IndexOverviewResp resp = new IndexOverviewResp();
        resp.setTimeList(LoadAggregationChartSupport.toShortTimeAxis(minuteAxis));
        if (DayTypeEnum.YESTERDAY.getCode().equals(dayType)) {
            AggregatorDateProfit profit = aggregatorDateProfitService.getAggregatorDateProfit(aggregatorId, date);
            if (profit != null) {
                resp.setTotalProfit(profit.getIssueProfit() == null ? 0D : profit.getIssueProfit());
                resp.setTotalProfitTime(profit.getDate());
            }
        }

        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getDeviceListByAggregatorId(aggregatorId, resourceTypeId);
        resp.setPowerChart(buildActualPowerChart(aggregatorId, deviceList, targetDate, minuteAxis, resourceTypeId));
        resp.setIssueChart(buildAggregatorMinuteChart(
                minuteAxis,
                aggregatorDateIssueChartService.getAggregatorDateIssueChart(aggregatorId, resourceTypeId, date),
                date,
                AggregatorDateIssueChart::getIssueChart));
        resp.setDapChart(buildAggregatorMinuteChart(
                minuteAxis,
                aggregatorDapChartService.getAggregatorDateDapChart(aggregatorId, resourceTypeId, date),
                date,
                AggregatorDapChart::getDapChart));
        resp.setCrChart(buildAggregatorMinuteChart(
                minuteAxis,
                aggregatorCrChartService.getAggregatorDateCrChart(aggregatorId, resourceTypeId, date),
                date,
                AggregatorCrChart::getCrLoadChart));
        resp.setTimeColorRespList(LoadAggregationChartSupport.buildColorRanges(resp.getIssueChart(), resp.getPowerChart()));
        return resp;
    }

    @Override
    public List<AggregatorEntDevice> getDeviceList(String aggregatorId, String entId, String stationId, String resourceTypeId) {
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(aggregatorId, entId, stationId, resourceTypeId);
        return deviceList == null ? Collections.emptyList() : deviceList;
    }

    @Override
    public EntUserDeviceYesterdayChartResp getEntUserDeviceChartResp(String deviceBaseId, List<AggregatorEntDevice> deviceList, String date) {
        LocalDate targetDate = parseDateOrDefault(date, LocalDate.now().minusDays(1));
        List<AggregatorEntDevice> actualDeviceList = CollectionUtils.isNotEmpty(deviceList)
                ? deviceList
                : aggregatorEntDeviceService.getAggregatorEntDeviceList(Collections.singletonList(deviceBaseId));
        if (CollectionUtils.isEmpty(actualDeviceList)) {
            return emptyDeviceChart();
        }
        List<String> minuteAxis = LoadAggregationChartSupport.buildDayMinuteDateTimeAxis(targetDate);
        String resourceTypeId = actualDeviceList.get(0).getResourceTypeId();

        EntUserDeviceYesterdayChartResp resp = new EntUserDeviceYesterdayChartResp();
        resp.setBaseLineChart(buildDeviceBaseLineChart(deviceBaseId, targetDate, minuteAxis));
        resp.setPowerChart(buildActualPowerChart(actualDeviceList.get(0).getAggregatorId(), actualDeviceList, targetDate, minuteAxis, resourceTypeId));
        resp.setIssueChart(buildDeviceIssueChart(Collections.singletonList(deviceBaseId), targetDate, minuteAxis));
        resp.setTimeColorRespList(LoadAggregationChartSupport.buildColorRanges(resp.getIssueChart(), resp.getPowerChart()));
        return resp;
    }

    @Override
    public EntUserDeviceYesterdayChartResp getEntUserDeviceListChartResp(List<AggregatorEntDevice> deviceList, String date) {
        LocalDate targetDate = parseDateOrDefault(date, LocalDate.now().minusDays(1));
        if (CollectionUtils.isEmpty(deviceList)) {
            return emptyDeviceChart();
        }
        List<String> minuteAxis = LoadAggregationChartSupport.buildDayMinuteDateTimeAxis(targetDate);
        String resourceTypeId = deviceList.get(0).getResourceTypeId();
        List<String> deviceBaseIdList = deviceList.stream()
                .map(AggregatorEntDevice::getDeviceBaseId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        EntUserDeviceYesterdayChartResp resp = new EntUserDeviceYesterdayChartResp();
        resp.setBaseLineChart(buildMultiDeviceBaseLineChart(deviceBaseIdList, targetDate, minuteAxis));
        resp.setPowerChart(buildActualPowerChart(deviceList.get(0).getAggregatorId(), deviceList, targetDate, minuteAxis, resourceTypeId));
        resp.setIssueChart(buildDeviceIssueChart(deviceBaseIdList, targetDate, minuteAxis));
        resp.setTimeColorRespList(LoadAggregationChartSupport.buildColorRanges(resp.getIssueChart(), resp.getPowerChart()));
        return resp;
    }

    @Override
    public EntUserDeviceYesterdayChartResp getEntUserChartResp(List<AggregatorEntDevice> deviceList, String date, String entId) {
        LocalDate targetDate = parseDateOrDefault(date, LocalDate.now().minusDays(1));
        if (CollectionUtils.isEmpty(deviceList)) {
            return emptyDeviceChart();
        }
        List<String> minuteAxis = LoadAggregationChartSupport.buildDayMinuteDateTimeAxis(targetDate);
        String resourceTypeId = deviceList.get(0).getResourceTypeId();
        List<String> deviceBaseIdList = deviceList.stream()
                .map(AggregatorEntDevice::getDeviceBaseId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        String stationId = StringUtils.defaultIfBlank(entId, deviceList.get(0).getStationId());

        EntUserDeviceYesterdayChartResp resp = new EntUserDeviceYesterdayChartResp();
        resp.setBaseLineChart(buildEntBaseLineChart(stationId, targetDate, minuteAxis, deviceBaseIdList));
        resp.setPowerChart(buildActualPowerChart(deviceList.get(0).getAggregatorId(), deviceList, targetDate, minuteAxis, resourceTypeId));
        resp.setIssueChart(buildDeviceIssueChart(deviceBaseIdList, targetDate, minuteAxis));
        resp.setTimeColorRespList(LoadAggregationChartSupport.buildColorRanges(resp.getIssueChart(), resp.getPowerChart()));
        return resp;
    }

    @Override
    public EntUserDeviceYesterdayChartResp getPowerDetail(String deviceBaseId, String date) {
        return getEntUserDeviceChartResp(deviceBaseId, null, date);
    }

    @Override
    public List<EntUserOverviewResp> getEntUserOverviewResp(String aggregatorId, String dayType) {
        List<AggregatorEnt> entList = aggregatorEntService.getAggregatorEntList(aggregatorId);
        if (CollectionUtils.isEmpty(entList)) {
            return Collections.emptyList();
        }
        LocalDate targetDate = resolveTargetDate(dayType);
        String date = DATE_FORMATTER.format(targetDate);
        List<String> entIdList = entList.stream().map(AggregatorEnt::getEntId).collect(Collectors.toList());
        Map<String, AggregatorEntDateApplyDetail> applyDetailMap = aggregatorEntDateApplyDetailService
                .getAggregatorEntDateApplyDetailList(entIdList, date)
                .stream()
                .collect(Collectors.toMap(AggregatorEntDateApplyDetail::getEntId, Function.identity(), (v1, v2) -> v1, LinkedHashMap::new));
        Map<String, Double> profitMap = Collections.emptyMap();
        if (DayTypeEnum.YESTERDAY.getCode().equals(dayType)) {
            profitMap = aggregatorEntDateProfitService.getAggregatorEntDateProfitList(entIdList, date)
                    .stream()
                    .collect(Collectors.toMap(AggregatorEntDateProfit::getEntId, AggregatorEntDateProfit::getEntProfit, (v1, v2) -> v1, LinkedHashMap::new));
        }

        List<EntUserOverviewResp> result = new ArrayList<>(entList.size());
        for (AggregatorEnt ent : entList) {
            EntUserOverviewResp resp = new EntUserOverviewResp();
            resp.setAggregatorId(ent.getAggregatorId());
            resp.setEntId(ent.getEntId());
            resp.setStationId(ent.getStationId());
            resp.setEntName(ent.getEntName());
            AggregatorEntDateApplyDetail applyDetail = applyDetailMap.get(ent.getEntId());
            resp.setApplyStatus(applyDetail == null ? "0" : "2");
            resp.setApplyTime(applyDetail == null ? null : applyDetail.getApplyTime());
            resp.setTotalProfit(profitMap.get(ent.getEntId()));
            result.add(resp);
        }
        Collections.sort(result);
        return result;
    }

    @Override
    public String entInvite(AggregatorEntDateInviteDetailReq req) {
        return "success";
    }

    private IndexOverviewResp buildTomorrowOverview(String aggregatorId, String resourceTypeId) {
        List<String> dateList = aggregatorDateHolidayService.getApplyDateList(LocalDate.now().format(DATE_FORMATTER), false);
        List<String> quarterAxis = LoadAggregationChartSupport.buildQuarterDateTimeAxis(dateList);
        IndexOverviewResp resp = new IndexOverviewResp();
        resp.setTimeList(quarterAxis);
        resp.setDeliveryChart(buildQuarterChart(
                quarterAxis,
                aggregatorDateDeliveryChartService.getAggregatorDateDeliveryChartList(aggregatorId, resourceTypeId, dateList),
                AggregatorDateDeliveryChart::getDate,
                AggregatorDateDeliveryChart::getDeliveryChart));
        resp.setDapChart(buildQuarterChart(
                quarterAxis,
                aggregatorDapChartService.getAggregatorDateDapChartListNew(aggregatorId, resourceTypeId, dateList),
                item -> DATE_FORMATTER.format(item.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()),
                AggregatorDapChart::getDapChart));
        return resp;
    }

    private List<DataResp> buildActualPowerChart(String aggregatorId,
                                                 List<AggregatorEntDevice> deviceList,
                                                 LocalDate targetDate,
                                                 List<String> minuteAxis,
                                                 String resourceTypeId) {
        if (CollectionUtils.isEmpty(deviceList)) {
            return emptyAxisChart(minuteAxis);
        }
        Set<Long> deviceIds = LoadAggregationChartSupport.newDeviceIdSet();
        Set<String> deviceCodes = LoadAggregationChartSupport.newDeviceCodeSet();
        LoadAggregationChartSupport.collectTelemetryScope(
                deviceList.stream()
                        .filter(item -> item != null && Integer.valueOf(1).equals(item.getModelFlag()) && Integer.valueOf(1).equals(item.getStatus()))
                        .collect(Collectors.toList()),
                deviceIds,
                deviceCodes);
        if (deviceIds.isEmpty() && deviceCodes.isEmpty()) {
            return emptyAxisChart(minuteAxis);
        }
        List<DataResp> rawList = iotTelemetryMinuteMapper.sumPointValueByMinute(
                aggregatorId,
                new ArrayList<>(deviceIds),
                new ArrayList<>(deviceCodes),
                LoadAggregationChartSupport.POWER_POINT_CODE,
                LoadAggregationChartSupport.startOfDay(targetDate, 1),
                LoadAggregationChartSupport.endOfDayInclusive(targetDate)
        );
        return LoadAggregationChartSupport.alignMinuteAxis(
                minuteAxis,
                LoadAggregationChartSupport.toValueMap(rawList),
                resourceTypeId
        );
    }

    private <T> List<DataResp> buildAggregatorMinuteChart(List<String> minuteAxis,
                                                          T record,
                                                          String recordDate,
                                                          Function<T, String> chartGetter) {
        if (record == null) {
            return emptyAxisChart(minuteAxis);
        }
        Map<String, Double> quarterMap = LoadAggregationChartSupport.parseCurveJson(recordDate, chartGetter.apply(record));
        return LoadAggregationChartSupport.expandQuarterMapToMinuteAxis(minuteAxis, quarterMap, null);
    }

    private <T> List<DataResp> buildQuarterChart(List<String> axis,
                                                 List<T> recordList,
                                                 Function<T, String> dateGetter,
                                                 Function<T, String> chartGetter) {
        if (CollectionUtils.isEmpty(recordList)) {
            return LoadAggregationChartSupport.alignQuarterAxis(axis, Collections.emptyMap());
        }
        List<Map<String, Double>> mapList = new ArrayList<>(recordList.size());
        for (T item : recordList) {
            if (item == null) {
                continue;
            }
            mapList.add(LoadAggregationChartSupport.parseCurveJson(dateGetter.apply(item), chartGetter.apply(item)));
        }
        return LoadAggregationChartSupport.alignQuarterAxis(axis, LoadAggregationChartSupport.mergeCurveMaps(mapList));
    }

    private List<DataResp> buildDeviceBaseLineChart(String deviceBaseId, LocalDate targetDate, List<String> minuteAxis) {
        AggregatorDeviceDateBaseLineLoadChart chart = aggregatorDeviceDateBaseLineLoadChartService
                .getAggregatorDeviceDateBaseLineLoadChart(deviceBaseId, DATE_FORMATTER.format(targetDate));
        if (chart == null) {
            return emptyAxisChart(minuteAxis);
        }
        Map<String, Double> quarterMap = LoadAggregationChartSupport.parseCurveJson(
                DATE_FORMATTER.format(targetDate),
                chart.getBaseLineLoadChart());
        return LoadAggregationChartSupport.expandQuarterMapToMinuteAxis(minuteAxis, quarterMap, null);
    }

    private List<DataResp> buildMultiDeviceBaseLineChart(List<String> deviceBaseIdList,
                                                         LocalDate targetDate,
                                                         List<String> minuteAxis) {
        if (CollectionUtils.isEmpty(deviceBaseIdList)) {
            return emptyAxisChart(minuteAxis);
        }
        List<AggregatorDeviceDateBaseLineLoadChart> chartList = aggregatorDeviceDateBaseLineLoadChartService
                .getAggregatorDeviceDateBaseLineLoadChartList(deviceBaseIdList, DATE_FORMATTER.format(targetDate));
        if (CollectionUtils.isEmpty(chartList)) {
            return emptyAxisChart(minuteAxis);
        }
        List<Map<String, Double>> mapList = new ArrayList<>(chartList.size());
        for (AggregatorDeviceDateBaseLineLoadChart chart : chartList) {
            mapList.add(LoadAggregationChartSupport.parseCurveJson(DATE_FORMATTER.format(targetDate), chart.getBaseLineLoadChart()));
        }
        return LoadAggregationChartSupport.expandQuarterMapToMinuteAxis(
                minuteAxis,
                LoadAggregationChartSupport.mergeCurveMaps(mapList),
                null
        );
    }

    private List<DataResp> buildEntBaseLineChart(String stationId,
                                                 LocalDate targetDate,
                                                 List<String> minuteAxis,
                                                 List<String> deviceBaseIdList) {
        List<AggregatorEntBaseLineLoadChart> chartList = aggregatorEntBaseLineLoadChartService
                .getEntBaseLineBySystemCode(stationId, DATE_FORMATTER.format(targetDate));
        if (CollectionUtils.isNotEmpty(chartList)) {
            List<Map<String, Double>> mapList = new ArrayList<>(chartList.size());
            for (AggregatorEntBaseLineLoadChart chart : chartList) {
                mapList.add(LoadAggregationChartSupport.parseCurveJson(DATE_FORMATTER.format(targetDate), chart.getBaseLineLoadChart()));
            }
            return LoadAggregationChartSupport.expandQuarterMapToMinuteAxis(
                    minuteAxis,
                    LoadAggregationChartSupport.mergeCurveMaps(mapList),
                    null
            );
        }
        return buildMultiDeviceBaseLineChart(deviceBaseIdList, targetDate, minuteAxis);
    }

    private List<DataResp> buildDeviceIssueChart(List<String> deviceBaseIdList, LocalDate targetDate, List<String> minuteAxis) {
        if (CollectionUtils.isEmpty(deviceBaseIdList)) {
            return emptyAxisChart(minuteAxis);
        }
        List<AggregatorDeviceDateIssueChart> chartList = aggregatorDeviceDateIssueChartService
                .getAggregatorDeviceDateIssueChartList(deviceBaseIdList, Collections.singletonList(DATE_FORMATTER.format(targetDate)));
        if (CollectionUtils.isEmpty(chartList)) {
            return emptyAxisChart(minuteAxis);
        }
        List<Map<String, Double>> mapList = new ArrayList<>(chartList.size());
        for (AggregatorDeviceDateIssueChart chart : chartList) {
            mapList.add(LoadAggregationChartSupport.parseCurveJson(DATE_FORMATTER.format(targetDate), chart.getIssueChart()));
        }
        return LoadAggregationChartSupport.expandQuarterMapToMinuteAxis(
                minuteAxis,
                LoadAggregationChartSupport.mergeCurveMaps(mapList),
                null
        );
    }

    private List<DataResp> emptyAxisChart(List<String> minuteAxis) {
        return LoadAggregationChartSupport.alignMinuteAxis(minuteAxis, Collections.emptyMap(), null);
    }

    private EntUserDeviceYesterdayChartResp emptyDeviceChart() {
        EntUserDeviceYesterdayChartResp resp = new EntUserDeviceYesterdayChartResp();
        resp.setBaseLineChart(Collections.emptyList());
        resp.setIssueChart(Collections.emptyList());
        resp.setPowerChart(Collections.emptyList());
        resp.setTimeColorRespList(Collections.emptyList());
        return resp;
    }

    private LocalDate resolveTargetDate(String dayType) {
        if (DayTypeEnum.YESTERDAY.getCode().equals(dayType)) {
            return LocalDate.now().minusDays(1);
        }
        if (DayTypeEnum.TOMORROW.getCode().equals(dayType)) {
            return LocalDate.now().plusDays(1);
        }
        return LocalDate.now();
    }

    private LocalDate parseDateOrDefault(String value, LocalDate defaultDate) {
        if (StringUtils.isBlank(value)) {
            return defaultDate;
        }
        return LocalDate.parse(StringUtils.trim(value), DATE_FORMATTER);
    }
}
