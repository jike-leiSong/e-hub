package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.console.model.req.DeviceRunStatusReq;
import cn.sl.ehub.console.model.req.NewUserAdjustmentGraphReq;
import cn.sl.ehub.console.model.req.ProfitStatisticsReq;
import cn.sl.ehub.console.model.req.UserAdjustmentGraphReq;
import cn.sl.ehub.console.model.req.UserAdjustmentTableReq;
import cn.sl.ehub.console.model.resp.HistoryAdjustExcelResp;
import cn.sl.ehub.console.model.resp.HistoryProfitCalculationExcelResp;
import cn.sl.ehub.console.model.resp.HistoryProfitCalculationTimeExcelResp;
import cn.sl.ehub.console.model.resp.LineDataGraphResp;
import cn.sl.ehub.console.model.resp.PriceExcelDateResp;
import cn.sl.ehub.console.model.vo.HistoryQueryGraphVO;
import cn.sl.ehub.console.model.vo.HistoryQueryTableVO;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.model.vo.ProfitStatisticsVO;
import cn.sl.ehub.console.model.vo.UserProfitStatisticsVO;
import cn.sl.ehub.console.service.IAggregatorBaseLineLoadChartService;
import cn.sl.ehub.console.service.IAggregatorCrChartService;
import cn.sl.ehub.console.service.IAggregatorDapChartService;
import cn.sl.ehub.console.service.IAggregatorDateIssueChartService;
import cn.sl.ehub.console.service.IAggregatorEntDeviceService;
import cn.sl.ehub.console.service.IAggregatorResourceDateIssueOfferService;
import cn.sl.ehub.console.service.IHistoryQueryService;
import cn.sl.ehub.service.mapper.IotTelemetryMinuteMapper;
import cn.sl.ehub.service.req.AdjustSituationExcelRep;
import cn.sl.ehub.service.req.IndexOverviewTableResp;
import cn.sl.ehub.service.resp.HistoryQueryDeviceMetricResp;
import cn.sl.ehub.service.resp.IndexOverviewResp;
import cn.sl.ehub.service.vo.AggregatorBaseLineLoadChart;
import cn.sl.ehub.service.vo.AggregatorCrChart;
import cn.sl.ehub.service.vo.AggregatorDapChart;
import cn.sl.ehub.service.vo.AggregatorDateIssueChart;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorResourceDateIssueOffer;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 历史查询服务实现 (空实现)
 *
 * @Author sl
 * @Date 2026-06-15
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class HistoryQueryServiceImpl implements IHistoryQueryService {

    private static final String POWER_POINT_CODE = "P";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_TIME_SECOND_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SLASH_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    private static final DateTimeFormatter SLASH_DATE_TIME_SECOND_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static final DateTimeFormatter SHORT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-M-d H:mm");
    private static final DateTimeFormatter SHORT_DATE_TIME_SECOND_FORMATTER = DateTimeFormatter.ofPattern("yyyy-M-d H:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TIME_SECOND_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter SHORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    private static final DateTimeFormatter SHORT_TIME_SECOND_FORMATTER = DateTimeFormatter.ofPattern("H:mm:ss");

    private final IAggregatorEntDeviceService aggregatorEntDeviceService;
    private final IAggregatorDateIssueChartService aggregatorDateIssueChartService;
    private final IAggregatorDapChartService aggregatorDapChartService;
    private final IAggregatorBaseLineLoadChartService aggregatorBaseLineLoadChartService;
    private final IAggregatorCrChartService aggregatorCrChartService;
    private final IAggregatorResourceDateIssueOfferService aggregatorResourceDateIssueOfferService;
    private final IotTelemetryMinuteMapper iotTelemetryMinuteMapper;

    @Override
    public HistoryQueryGraphVO userAdjustmentGraph(UserAdjustmentGraphReq userAdjustmentGraphReq) {
        log.warn("userAdjustmentGraph called - empty implementation");
        return new HistoryQueryGraphVO();
    }

    @Override
    public HistoryQueryGraphVO userAdjustmentGraphNew(NewUserAdjustmentGraphReq userAdjustmentGraphReq) {
        log.warn("userAdjustmentGraphNew called - empty implementation");
        return new HistoryQueryGraphVO();
    }

    @Override
    public PageResultVO<HistoryQueryTableVO> userAdjustmentTable(UserAdjustmentTableReq userAdjustmentTableReq) {
        log.warn("userAdjustmentTable called - empty implementation");
        return new PageResultVO<>();
    }

    @Override
    public List<LineDataGraphResp> deviceRunStatusChart(DeviceRunStatusReq deviceRunStatusReq) {
        log.warn("deviceRunStatusChart called - empty implementation");
        return new ArrayList<>();
    }

    @Override
    public ProfitStatisticsVO profitStatistics(ProfitStatisticsReq profitStatisticsReq) {
        log.warn("profitStatistics called - empty implementation");
        return new ProfitStatisticsVO();
    }

    @Override
    public UserProfitStatisticsVO userProfitStatistics(ProfitStatisticsReq profitStatisticsReq) {
        log.warn("userProfitStatistics called - empty implementation");
        return new UserProfitStatisticsVO();
    }

    @Override
    public IndexOverviewResp getTotalPowerChart(String aggregatorId, String resourceTypeId, String startDate, String endDate) {
        DateRange dateRange = resolveDateRange(startDate, endDate);
        String queryStartDate = DATE_FORMATTER.format(dateRange.getStartDate());
        String queryEndDate = DATE_FORMATTER.format(dateRange.getEndDate());
        List<String> dateList = buildDateList(dateRange.getStartDate(), dateRange.getEndDate());
        List<DataResp> powerChart = buildPowerChart(aggregatorId, resourceTypeId, dateRange);
        List<String> timeList = powerChart.stream().map(DataResp::getTime).collect(Collectors.toList());

        Map<String, Double> issueChartMap = buildIssueChartMap(aggregatorId, resourceTypeId, dateList);
        Map<String, Double> dapChartMap = buildDapChartMap(aggregatorId, resourceTypeId, queryStartDate, queryEndDate);
        Map<String, Double> baseLineChartMap = buildBaseLineChartMap(aggregatorId, resourceTypeId, queryStartDate, queryEndDate);
        Map<String, Double> crChartMap = buildCrChartMap(aggregatorId, resourceTypeId, queryStartDate, queryEndDate);
        Map<String, Double> issuePriceMap = buildIssuePriceMap(aggregatorId, resourceTypeId, queryStartDate, queryEndDate);

        IndexOverviewResp resp = new IndexOverviewResp();
        resp.setTimeList(timeList);
        resp.setPowerChart(powerChart);
        resp.setIssueChart(buildAlignedChart(timeList, issueChartMap));
        resp.setDapChart(buildAlignedChart(timeList, dapChartMap));
        resp.setBaseLineChart(buildAlignedChart(timeList, baseLineChartMap));
        resp.setCrChart(buildAlignedChart(timeList, crChartMap));
        resp.setIssuePrice(buildAlignedChart(timeList, issuePriceMap));
        resp.setTimeColorRespList(Collections.emptyList());
        return resp;
    }

    @Override
    public IndexOverviewResp getPrice(ProfitStatisticsReq profitStatisticsReq) {
        log.warn("getPrice called - empty implementation");
        return new IndexOverviewResp();
    }

    @Override
    public IndexOverviewTableResp getPriceTable(ProfitStatisticsReq profitStatisticsReq) {
        log.warn("getPriceTable called - empty implementation");
        return new IndexOverviewTableResp();
    }

    @Override
    public List<PriceExcelDateResp> getPriceExcel(ProfitStatisticsReq profitStatisticsReq) {
        log.warn("getPriceExcel called - empty implementation");
        return new ArrayList<>();
    }

    @Override
    public List<HistoryQueryDeviceMetricResp> getMetricList() {
        log.warn("getMetricList called - empty implementation");
        return new ArrayList<>();
    }

    @Override
    public List<HistoryProfitCalculationExcelResp> getProfitCalculation(String entId, String startDate, String endDate) {
        log.warn("getProfitCalculation called - empty implementation");
        return new ArrayList<>();
    }

    @Override
    public LinkedHashMap<String, List<HistoryProfitCalculationTimeExcelResp>> getProfitCalculationMap(String entId, String startDate, String endDate) {
        log.warn("getProfitCalculationMap called - empty implementation");
        return new LinkedHashMap<>();
    }

    @Override
    public HistoryAdjustExcelResp exportAdjustSituationExcel(AdjustSituationExcelRep adjustSituationExcelRep) {
        log.warn("exportAdjustSituationExcel called - empty implementation");
        return new HistoryAdjustExcelResp();
    }

    @Override
    public HistoryAdjustExcelResp exportBuZhaoUploadData(AdjustSituationExcelRep req) {
        log.warn("exportBuZhaoUploadData called - empty implementation");
        return new HistoryAdjustExcelResp();
    }

    private List<DataResp> buildPowerChart(String aggregatorId, String resourceTypeId, DateRange dateRange) {
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getDeviceListByAggregatorId(aggregatorId, resourceTypeId);
        if (deviceList == null || deviceList.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> deviceIds = new LinkedHashSet<>();
        Set<String> deviceCodes = deviceList.stream()
                .filter(device -> Integer.valueOf(1).equals(device.getStatus()))
                .filter(device -> Integer.valueOf(1).equals(device.getModelFlag()))
                .peek(device -> addTelemetryDeviceId(deviceIds, device.getIotDeviceBaseId()))
                .map(AggregatorEntDevice::getDeviceId)
                .filter(StringUtils::isNotBlank)
                .map(this::normalizeTelemetryDeviceCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (deviceCodes.isEmpty() && deviceIds.isEmpty()) {
            return Collections.emptyList();
        }
        return iotTelemetryMinuteMapper.sumPointValueByMinute(
                aggregatorId,
                new ArrayList<>(deviceIds),
                new ArrayList<>(deviceCodes),
                POWER_POINT_CODE,
                dateRange.getStartTime(),
                dateRange.getEndTime()
        );
    }

    private Map<String, Double> buildIssueChartMap(String aggregatorId, String resourceTypeId, List<String> dateList) {
        List<AggregatorDateIssueChart> chartList = aggregatorDateIssueChartService
                .getAggregatorDateIssueChartListNew(aggregatorId, resourceTypeId, dateList);
        return buildCurveValueMap(chartList,
                AggregatorDateIssueChart::getDate,
                AggregatorDateIssueChart::getIssueChart);
    }

    private Map<String, Double> buildDapChartMap(String aggregatorId, String resourceTypeId, String startDate, String endDate) {
        List<AggregatorDapChart> chartList = aggregatorDapChartService
                .getAggregatorDapChart(aggregatorId, resourceTypeId, startDate, endDate);
        return buildCurveValueMap(chartList,
                item -> formatDate(item.getDate()),
                AggregatorDapChart::getDapChart);
    }

    private Map<String, Double> buildBaseLineChartMap(String aggregatorId, String resourceTypeId, String startDate, String endDate) {
        List<AggregatorBaseLineLoadChart> chartList = aggregatorBaseLineLoadChartService
                .getAggregatorBaseLine(aggregatorId, resourceTypeId, startDate, endDate);
        return buildCurveValueMap(chartList,
                AggregatorBaseLineLoadChart::getBaseDate,
                AggregatorBaseLineLoadChart::getBaseLineLoadChart);
    }

    private Map<String, Double> buildCrChartMap(String aggregatorId, String resourceTypeId, String startDate, String endDate) {
        List<AggregatorCrChart> chartList = aggregatorCrChartService
                .getAggregatorCrLine(aggregatorId, resourceTypeId, startDate, endDate);
        return buildCurveValueMap(chartList,
                AggregatorCrChart::getCrDate,
                AggregatorCrChart::getCrLoadChart);
    }

    private Map<String, Double> buildIssuePriceMap(String aggregatorId, String resourceTypeId, String startDate, String endDate) {
        List<AggregatorResourceDateIssueOffer> chartList = aggregatorResourceDateIssueOfferService
                .getAggregatorIssuePriceChart(aggregatorId, resourceTypeId, startDate, endDate);
        return buildCurveValueMap(chartList,
                AggregatorResourceDateIssueOffer::getDate,
                AggregatorResourceDateIssueOffer::getPriceChart);
    }

    private <T> Map<String, Double> buildCurveValueMap(List<T> recordList,
                                                       Function<T, String> dateGetter,
                                                       Function<T, String> chartGetter) {
        if (recordList == null || recordList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Double> valueMap = new LinkedHashMap<>();
        for (T item : recordList) {
            String recordDate = dateGetter.apply(item);
            valueMap.putAll(parseCurveJson(recordDate, chartGetter.apply(item)));
        }
        return valueMap;
    }

    private Map<String, Double> parseCurveJson(String recordDate, String chartJson) {
        if (StringUtils.isBlank(chartJson)) {
            return Collections.emptyMap();
        }
        JSONArray array;
        try {
            array = JSONArray.parseArray(chartJson);
        } catch (Exception ex) {
            log.warn("解析业务曲线JSON失败, recordDate={}, chart={}", recordDate, chartJson, ex);
            return Collections.emptyMap();
        }
        if (array == null || array.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Double> valueMap = new LinkedHashMap<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject item = array.getJSONObject(i);
            if (item == null) {
                continue;
            }
            String normalizedTime = normalizeCurveTime(recordDate, extractTime(item));
            Double value = extractValue(item);
            if (StringUtils.isBlank(normalizedTime) || value == null) {
                continue;
            }
            valueMap.put(normalizedTime, MathUtils.doublePoint(value, 2));
        }
        return valueMap;
    }

    private List<DataResp> buildAlignedChart(List<String> timeList, Map<String, Double> valueMap) {
        if (timeList == null || timeList.isEmpty()) {
            return Collections.emptyList();
        }
        List<DataResp> result = new ArrayList<>(timeList.size());
        for (String time : timeList) {
            result.add(new DataResp(time, valueMap.getOrDefault(time, 0D)));
        }
        return result;
    }

    private String extractTime(JSONObject item) {
        String[] fields = new String[]{"time", "date", "dateTime", "readTime"};
        for (String field : fields) {
            String value = item.getString(field);
            if (StringUtils.isNotBlank(value)) {
                return StringUtils.trim(value);
            }
        }
        return null;
    }

    private Double extractValue(JSONObject item) {
        String[] fields = new String[]{"value", "quantity", "dateValue", "useQuantity"};
        for (String field : fields) {
            Object rawValue = item.get(field);
            if (rawValue == null) {
                continue;
            }
            if (rawValue instanceof Number) {
                return ((Number) rawValue).doubleValue();
            }
            String text = StringUtils.trimToNull(String.valueOf(rawValue));
            if (text == null) {
                continue;
            }
            try {
                return Double.valueOf(text);
            } catch (NumberFormatException ignored) {
                // ignore invalid field and try next one
            }
        }
        return null;
    }

    private String normalizeCurveTime(String recordDate, String rawTime) {
        if (StringUtils.isBlank(rawTime)) {
            return null;
        }
        String value = StringUtils.trim(rawTime);
        String normalized = normalizeFullDateTime(value);
        if (normalized != null) {
            return normalized;
        }
        LocalTime localTime = parseLocalTime(value);
        if (localTime != null && StringUtils.isNotBlank(recordDate)) {
            try {
                LocalDate date = LocalDate.parse(recordDate, DATE_FORMATTER);
                return DATE_TIME_FORMATTER.format(LocalDateTime.of(date, localTime.withSecond(0).withNano(0)));
            } catch (DateTimeParseException ex) {
                log.warn("解析曲线日期失败, recordDate={}", recordDate, ex);
            }
        }
        return null;
    }

    private void addTelemetryDeviceId(Set<Long> deviceIds, String rawDeviceId) {
        if (StringUtils.isBlank(rawDeviceId)) {
            return;
        }
        try {
            deviceIds.add(Long.valueOf(StringUtils.trim(rawDeviceId)));
        } catch (NumberFormatException ex) {
            log.warn("iotDeviceBaseId 不是有效数字, value={}", rawDeviceId);
        }
    }

    private String normalizeTelemetryDeviceCode(String rawDeviceCode) {
        String deviceCode = StringUtils.trimToNull(rawDeviceCode);
        if (deviceCode == null) {
            return null;
        }
        int separatorIndex = deviceCode.indexOf('_');
        if (separatorIndex < 0 || separatorIndex == deviceCode.length() - 1) {
            return deviceCode;
        }
        return deviceCode.substring(separatorIndex + 1);
    }

    private String normalizeFullDateTime(String value) {
        DateTimeFormatter[] formatters = new DateTimeFormatter[]{
                DATE_TIME_FORMATTER,
                DATE_TIME_SECOND_FORMATTER,
                SLASH_DATE_TIME_FORMATTER,
                SLASH_DATE_TIME_SECOND_FORMATTER,
                SHORT_DATE_TIME_FORMATTER,
                SHORT_DATE_TIME_SECOND_FORMATTER,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(value, formatter);
                return DATE_TIME_FORMATTER.format(dateTime.withSecond(0).withNano(0));
            } catch (DateTimeParseException ignored) {
                // try next formatter
            }
        }
        return null;
    }

    private LocalTime parseLocalTime(String value) {
        DateTimeFormatter[] formatters = new DateTimeFormatter[]{
                TIME_FORMATTER,
                TIME_SECOND_FORMATTER,
                SHORT_TIME_FORMATTER,
                SHORT_TIME_SECOND_FORMATTER
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // try next formatter
            }
        }
        return null;
    }

    private DateRange resolveDateRange(String startDate, String endDate) {
        LocalDate defaultDate = LocalDate.now().minusDays(1);
        LocalDate start = StringUtils.isBlank(startDate) ? null : parseDateOrDefault(startDate, defaultDate);
        LocalDate end = StringUtils.isBlank(endDate) ? null : parseDateOrDefault(endDate, defaultDate);
        if (start == null && end == null) {
            start = defaultDate;
            end = defaultDate;
        } else if (start == null) {
            start = end;
        } else if (end == null) {
            end = start;
        }
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
        ZoneId zoneId = ZoneId.systemDefault();
        Date startTime = Date.from(start.atStartOfDay(zoneId).toInstant());
        Date endTime = Date.from(end.atTime(23, 59, 59).atZone(zoneId).toInstant());
        return new DateRange(start, end, startTime, endTime);
    }

    private LocalDate parseDateOrDefault(String value, LocalDate defaultDate) {
        if (StringUtils.isBlank(value)) {
            return defaultDate;
        }
        try {
            return LocalDate.parse(StringUtils.trim(value), DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new BaseException(StatusCode.C.getCode(), "日期格式错误: " + value, ex);
        }
    }

    private List<String> buildDateList(LocalDate startDate, LocalDate endDate) {
        List<String> dateList = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dateList.add(DATE_FORMATTER.format(current));
            current = current.plusDays(1);
        }
        return dateList;
    }

    private String formatDate(Date value) {
        if (value == null) {
            return null;
        }
        return DATE_FORMATTER.format(Instant.ofEpochMilli(value.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate());
    }

    private static class DateRange {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final Date startTime;
        private final Date endTime;

        private DateRange(LocalDate startDate, LocalDate endDate, Date startTime, Date endTime) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public Date getStartTime() {
            return startTime;
        }

        public Date getEndTime() {
            return endTime;
        }
    }
}
