package cn.sl.ehub.console.service.impl;

import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.console.auth.LoadAggregationScopeService;
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
import cn.sl.ehub.console.model.vo.LineDataGraphVO;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.model.vo.ProfitStatisticsVO;
import cn.sl.ehub.console.model.vo.UserProfitStatisticsVO;
import cn.sl.ehub.console.service.IAggregatorBaseLineLoadChartService;
import cn.sl.ehub.console.service.IAggregatorCrChartService;
import cn.sl.ehub.console.service.IAggregatorDapChartService;
import cn.sl.ehub.console.service.IAggregatorDateIssueChartService;
import cn.sl.ehub.console.service.IAggregatorDeviceDateProfitService;
import cn.sl.ehub.console.service.IAggregatorEntService;
import cn.sl.ehub.console.service.IAggregatorEntBaseLineLoadChartService;
import cn.sl.ehub.console.service.IAggregatorEntDateAdjustService;
import cn.sl.ehub.console.service.IAggregatorEntDeviceService;
import cn.sl.ehub.console.service.IAggregatorInfoService;
import cn.sl.ehub.console.service.IAggregatorResourceDateIssueOfferService;
import cn.sl.ehub.console.service.IHistoryQueryService;
import cn.sl.ehub.service.mapper.IotTelemetryMinuteMapper;
import cn.sl.ehub.service.mapper.IotTelemetryQueryMapper;
import cn.sl.ehub.console.enums.MetricEnum;
import cn.sl.ehub.service.dto.iot.IotTelemetryDataResp;
import cn.sl.ehub.service.req.AdjustSituationExcelRep;
import cn.sl.ehub.service.req.IndexOverviewTableResp;
import cn.sl.ehub.service.resp.HistoryQueryDeviceMetricResp;
import cn.sl.ehub.service.resp.IndexOverviewResp;
import cn.sl.ehub.service.resp.AggregatorEntDateAdjustResp;
import cn.sl.ehub.service.resp.AggregatorDeviceDateProfitResp;
import cn.sl.ehub.service.vo.AggregatorBaseLineLoadChart;
import cn.sl.ehub.service.vo.AggregatorCrChart;
import cn.sl.ehub.service.vo.AggregatorDapChart;
import cn.sl.ehub.service.vo.AggregatorDateIssueChart;
import cn.sl.ehub.service.vo.AggregatorEnt;
import cn.sl.ehub.service.vo.AggregatorEntBaseLineLoadChart;
import cn.sl.ehub.service.vo.AggregatorEntDateAdjust;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorInfo;
import cn.sl.ehub.service.vo.AggregatorResourceDateIssueOffer;
import cn.sl.ehub.service.vo.AggregatorDeviceDateProfit;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
 * 历史查询服务实现。
 *
 * 调节明细和设备运行曲线已迁移，收益结算相关接口等待新规则后实现。
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
    private final IAggregatorEntDateAdjustService aggregatorEntDateAdjustService;
    private final IAggregatorEntBaseLineLoadChartService aggregatorEntBaseLineLoadChartService;
    private final IAggregatorDateIssueChartService aggregatorDateIssueChartService;
    private final IAggregatorDapChartService aggregatorDapChartService;
    private final IAggregatorBaseLineLoadChartService aggregatorBaseLineLoadChartService;
    private final IAggregatorCrChartService aggregatorCrChartService;
    private final IAggregatorResourceDateIssueOfferService aggregatorResourceDateIssueOfferService;
    private final IAggregatorEntService aggregatorEntService;
    private final IAggregatorInfoService aggregatorInfoService;
    private final IotTelemetryMinuteMapper iotTelemetryMinuteMapper;
    private final IotTelemetryQueryMapper iotTelemetryQueryMapper;
    private final IAggregatorDeviceDateProfitService aggregatorDeviceDateProfitService;
    private final LoadAggregationScopeService loadScopeService;

    @Override
    public HistoryQueryGraphVO userAdjustmentGraph(UserAdjustmentGraphReq userAdjustmentGraphReq) {
        NewUserAdjustmentGraphReq req = new NewUserAdjustmentGraphReq(
                userAdjustmentGraphReq.getStartTime(),
                userAdjustmentGraphReq.getEndTime(),
                userAdjustmentGraphReq.getSubEntId(),
                userAdjustmentGraphReq.getResourceTypeId()
        );
        HistoryQueryGraphVO resp = userAdjustmentGraphNew(req);
        resp.setResolvedPower(resp.getAdjustPower());
        resp.setEffectivePower(resp.getAdjustPower());
        resp.setFillColor(resp.getTimeColorRespList());
        return resp;
    }

    @Override
    public HistoryQueryGraphVO userAdjustmentGraphNew(NewUserAdjustmentGraphReq userAdjustmentGraphReq) {
        DateRange dateRange = resolveAdjustmentDateRange(userAdjustmentGraphReq.getStartTime(), userAdjustmentGraphReq.getEndTime());
        List<String> quarterTimeList = buildQuarterTimeList(dateRange.getStartDate(), dateRange.getEndDate());
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService
                .getDeviceListByEntId(userAdjustmentGraphReq.getSubEntId(), userAdjustmentGraphReq.getResourceTypeId());

        HistoryQueryGraphVO resp = new HistoryQueryGraphVO();
        List<DataResp> powerChart = buildEntQuarterAveragePowerChart(deviceList, dateRange, quarterTimeList);
        List<DataResp> adjustPower = buildEntAdjustChart(userAdjustmentGraphReq, quarterTimeList);
        List<DataResp> baseLineChart = buildEntBaseLineChart(userAdjustmentGraphReq, quarterTimeList);

        resp.setActualPower(powerChart);
        resp.setResolvedPower(adjustPower);
        resp.setPowerChart(powerChart);
        resp.setAdjustPower(adjustPower);
        resp.setBaseLineChart(baseLineChart);
        resp.setFillColor(Collections.emptyList());
        resp.setTimeColorRespList(Collections.emptyList());
        resp.setProfit("0");
        resp.setProfitUnit("元");
        return resp;
    }

    @Override
    public PageResultVO<HistoryQueryTableVO> userAdjustmentTable(UserAdjustmentTableReq userAdjustmentTableReq) {
        if (userAdjustmentTableReq == null || StringUtils.isBlank(userAdjustmentTableReq.getDeviceBaseId())) {
            throw new BaseException(StatusCode.C.getCode(), "设备ID不能为空");
        }
        AggregatorEntDevice device = aggregatorEntDeviceService
                .getAggregatorEntDevice(userAdjustmentTableReq.getDeviceBaseId());
        if (device == null) {
            throw new BaseException(StatusCode.E_J.getCode(), "设备不存在");
        }
        loadScopeService.validateScope(device.getAggregatorId(), device.getEntId());
        validateAdjustmentDeviceScope(userAdjustmentTableReq, device);
        DateRange range = resolveAdjustmentDateRange(
                userAdjustmentTableReq.getStartTime(), userAdjustmentTableReq.getEndTime());
        List<AggregatorDeviceDateProfit> profits = aggregatorDeviceDateProfitService
                .getAggregatorDeviceDateProfitList(device.getDeviceBaseId(),
                        buildDateList(range.getStartDate(), range.getEndDate()));
        List<AggregatorDeviceDateProfitResp> details = new ArrayList<>();
        for (AggregatorDeviceDateProfit profit : profits) {
            if (profit == null || StringUtils.isBlank(profit.getProfitDetail())) {
                continue;
            }
            List<AggregatorDeviceDateProfitResp> parsed = JSONArray.parseArray(
                    profit.getProfitDetail(), AggregatorDeviceDateProfitResp.class);
            if (parsed != null) {
                details.addAll(parsed.stream()
                        .filter(item -> matchesProfitType(item, userAdjustmentTableReq.getProfitType()))
                        .collect(Collectors.toList()));
            }
        }
        details.sort(Comparator.comparing(AggregatorDeviceDateProfitResp::getStartTime,
                Comparator.nullsLast(Comparator.reverseOrder())));

        int pageNo = userAdjustmentTableReq.getPageNo() == null || userAdjustmentTableReq.getPageNo() < 1
                ? 1 : userAdjustmentTableReq.getPageNo();
        int pageSize = userAdjustmentTableReq.getPageSize() == null || userAdjustmentTableReq.getPageSize() < 1
                ? 10 : Math.min(userAdjustmentTableReq.getPageSize(), 200);
        int from = Math.min((pageNo - 1) * pageSize, details.size());
        int to = Math.min(from + pageSize, details.size());
        List<HistoryQueryTableVO> rows = details.subList(from, to).stream()
                .map(item -> toAdjustmentTableRow(item, device.getDeviceName()))
                .collect(Collectors.toList());
        PageResultVO<HistoryQueryTableVO> result = new PageResultVO<>();
        result.setPageIndex(pageNo);
        result.setPageSize(pageSize);
        result.setTotal(details.size());
        result.setList(rows);
        return result;
    }

    @Override
    public List<LineDataGraphResp> deviceRunStatusChart(DeviceRunStatusReq deviceRunStatusReq) {
        if (deviceRunStatusReq == null || CollectionUtils.isEmpty(deviceRunStatusReq.getDeviceBaseIdList())
                || CollectionUtils.isEmpty(deviceRunStatusReq.getMetricList())) {
            return Collections.emptyList();
        }
        DateRange range = resolveAdjustmentDateRange(
                deviceRunStatusReq.getStartTime(), deviceRunStatusReq.getEndTime());
        List<AggregatorEntDevice> devices = aggregatorEntDeviceService
                .getAggregatorEntDeviceList(deviceRunStatusReq.getDeviceBaseIdList());
        Map<String, AggregatorEntDevice> deviceMap = devices.stream().collect(Collectors.toMap(
                AggregatorEntDevice::getDeviceBaseId, Function.identity(), (left, right) -> left));
        if (deviceMap.size() != new LinkedHashSet<>(deviceRunStatusReq.getDeviceBaseIdList()).size()) {
            throw new BaseException(StatusCode.E_J.getCode(), "部分设备不存在");
        }
        validateRunStatusScope(deviceRunStatusReq, devices);

        List<MetricEnum> metrics = expandMetrics(deviceRunStatusReq.getMetricList());
        if (metrics.isEmpty()) {
            return Collections.emptyList();
        }
        List<IotTelemetryDataResp> telemetry = iotTelemetryQueryMapper.selectRunStatusData(
                devices.get(0).getAggregatorId(),
                StringUtils.trimToNull(deviceRunStatusReq.getSubEntId()),
                telemetryDeviceIds(devices),
                telemetryDeviceCodes(devices),
                metrics.stream().map(MetricEnum::getCode).distinct().collect(Collectors.toList()),
                range.getStartTime(), range.getEndTime());
        Map<String, Map<String, Map<String, Double>>> dataMap = groupRunStatusData(telemetry, devices);
        List<String> minuteAxis = buildMinuteTimeList(range.getStartDate(), range.getEndDate());

        if ("1".equals(deviceRunStatusReq.getStatus())) {
            List<LineDataGraphVO> lines = new ArrayList<>();
            for (String deviceBaseId : deviceRunStatusReq.getDeviceBaseIdList()) {
                AggregatorEntDevice device = deviceMap.get(deviceBaseId);
                for (MetricEnum metric : metrics) {
                    String lineName = device.getDeviceName();
                    if (MetricEnum.IA == metric || MetricEnum.IB == metric || MetricEnum.IC == metric) {
                        lineName = StringUtils.defaultString(lineName) + metric.getDesc();
                    }
                    lines.add(buildRunStatusLine(lineName, metric.getGroupName(), metric,
                            runStatusPoints(dataMap, deviceBaseId, metric.getCode()), minuteAxis,
                            device.getResourceTypeId()));
                }
            }
            return Collections.singletonList(toLineGraphResp(lines));
        }

        String deviceBaseId = deviceRunStatusReq.getDeviceBaseIdList().get(0);
        AggregatorEntDevice device = deviceMap.get(deviceBaseId);
        Map<String, List<LineDataGraphVO>> groups = new LinkedHashMap<>();
        for (MetricEnum metric : metrics) {
            LineDataGraphVO line = buildRunStatusLine(metric.getDesc(), device.getDeviceName(), metric,
                    runStatusPoints(dataMap, deviceBaseId, metric.getCode()), minuteAxis,
                    device.getResourceTypeId());
            groups.computeIfAbsent(metric.getGroupCode(), key -> new ArrayList<>()).add(line);
        }
        return groups.values().stream().map(this::toLineGraphResp).collect(Collectors.toList());
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
        return MetricEnum.getMetricEnumByFlag(true).stream().map(metric -> {
            HistoryQueryDeviceMetricResp resp = new HistoryQueryDeviceMetricResp();
            resp.setMetricCode(metric.getCode());
            resp.setMetricName(metric.getDesc());
            return resp;
        }).collect(Collectors.toList());
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
        DateRange dateRange = resolveAdjustmentDateRange(adjustSituationExcelRep.getStartDate(), adjustSituationExcelRep.getEndDate());
        List<String> dateList = buildDateList(dateRange.getStartDate(), dateRange.getEndDate());
        List<String> quarterAxis = buildQuarterTimeList(dateRange.getStartDate(), dateRange.getEndDate());
        String aggregatorId = adjustSituationExcelRep.getAggregatorId();
        String resourceTypeId = adjustSituationExcelRep.getSourceId();
        String aggregatorName = resolveAggregatorName(aggregatorId);

        HistoryAdjustExcelResp resp = new HistoryAdjustExcelResp();
        resp.setEntityList(buildAdjustExportTitle());

        List<Map<String, String>> rowDataList = new ArrayList<>();
        if (StringUtils.isBlank(adjustSituationExcelRep.getEntId())) {
            rowDataList.addAll(buildAggregatorAdjustExportRows(
                    aggregatorId,
                    resourceTypeId,
                    aggregatorName,
                    dateRange,
                    dateList,
                    quarterAxis
            ));
        }

        List<AggregatorEnt> entList = resolveExportEntList(aggregatorId, adjustSituationExcelRep.getEntId());
        for (AggregatorEnt ent : entList) {
            rowDataList.addAll(buildEntAdjustExportRows(
                    ent,
                    aggregatorName,
                    resourceTypeId,
                    dateRange,
                    dateList,
                    quarterAxis
            ));
        }
        resp.setAllExcelDataList(rowDataList);
        return resp;
    }

    @Override
    public HistoryAdjustExcelResp exportBuZhaoUploadData(AdjustSituationExcelRep req) {
        DateRange dateRange = resolveDateRange(req.getStartDate(), req.getEndDate());
        List<DataResp> powerChart = buildPowerChart(req.getAggregatorId(), req.getSourceId(), dateRange);

        HistoryAdjustExcelResp resp = new HistoryAdjustExcelResp();
        resp.setEntityList(buildBuzhaoExportTitle());
        resp.setAllExcelDataList(buildBuzhaoExportRows(powerChart, dateRange));
        return resp;
    }

    private void validateAdjustmentDeviceScope(UserAdjustmentTableReq req, AggregatorEntDevice device) {
        if ((StringUtils.isNotBlank(req.getAggregatorId())
                && !StringUtils.equals(req.getAggregatorId(), device.getAggregatorId()))
                || (StringUtils.isNotBlank(req.getSubEntId())
                && !StringUtils.equals(req.getSubEntId(), device.getEntId()))
                || (StringUtils.isNotBlank(req.getResourceTypeId())
                && !StringUtils.equals(req.getResourceTypeId(), device.getResourceTypeId()))
                || (StringUtils.isNotBlank(req.getDeviceId())
                && !StringUtils.equals(req.getDeviceId(), device.getDeviceId()))) {
            throw new BaseException(StatusCode.U.getCode(), StatusCode.U.getMsg());
        }
    }

    private boolean matchesProfitType(AggregatorDeviceDateProfitResp item, Integer profitType) {
        if (item == null || profitType == null) {
            return item != null;
        }
        return profitType == 1
                ? "1".equals(item.getProfitStatus())
                : profitType != 0 || !"1".equals(item.getProfitStatus());
    }

    private HistoryQueryTableVO toAdjustmentTableRow(AggregatorDeviceDateProfitResp item, String deviceName) {
        HistoryQueryTableVO row = new HistoryQueryTableVO();
        String start = normalizeFullDateTime(item.getStartTime());
        String end = normalizeFullDateTime(item.getEndTime());
        String endLabel = end == null || end.length() < 5 ? item.getEndTime() : end.substring(end.length() - 5);
        row.setTime(StringUtils.defaultString(start, item.getStartTime()) + "~" + StringUtils.defaultString(endLabel));
        row.setDeviceName(deviceName);
        row.setApplyPower(item.getDeliveryPower() == null ? null : String.valueOf(item.getDeliveryPower()));
        row.setIssuePower(item.getIssuePower());
        row.setActualPower(MathUtils.doublePointNotRounding(item.getReallyPower(), 2));
        row.setUsePower(MathUtils.mulDoubleNull(item.getIssuePower(), 0.7D, 2));
        row.setProfit(item.getProfit() == null ? null : String.valueOf(MathUtils.doublePoint(item.getProfit(), 2)));
        row.setPowerUnit("kW");
        row.setProfitUnit("元");
        return row;
    }

    private void validateRunStatusScope(DeviceRunStatusReq req, List<AggregatorEntDevice> devices) {
        Set<String> aggregatorIds = devices.stream().map(AggregatorEntDevice::getAggregatorId)
                .filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        if (aggregatorIds.size() != 1) {
            throw new BaseException(StatusCode.C.getCode(), "所选设备必须属于同一聚合商");
        }
        for (AggregatorEntDevice device : devices) {
            loadScopeService.validateScope(device.getAggregatorId(), device.getEntId());
            if ((StringUtils.isNotBlank(req.getAggregatorId())
                    && !StringUtils.equals(req.getAggregatorId(), device.getAggregatorId()))
                    || (StringUtils.isNotBlank(req.getSubEntId())
                    && !StringUtils.equals(req.getSubEntId(), device.getEntId()))
                    || (StringUtils.isNotBlank(req.getResourceTypeId())
                    && !StringUtils.equals(req.getResourceTypeId(), device.getResourceTypeId()))) {
                throw new BaseException(StatusCode.U.getCode(), StatusCode.U.getMsg());
            }
        }
    }

    private List<MetricEnum> expandMetrics(List<String> requestedMetrics) {
        LinkedHashSet<MetricEnum> metrics = new LinkedHashSet<>();
        for (String metricCode : requestedMetrics) {
            if (MetricEnum.USE_ELECTRIC.getCode().equals(metricCode)) {
                metrics.add(MetricEnum.IA);
                metrics.add(MetricEnum.IB);
                metrics.add(MetricEnum.IC);
                continue;
            }
            MetricEnum metric = MetricEnum.getMetricEnum(metricCode);
            if (metric != null) {
                metrics.add(metric);
            }
        }
        return new ArrayList<>(metrics);
    }

    private List<Long> telemetryDeviceIds(List<AggregatorEntDevice> devices) {
        Set<Long> result = new LinkedHashSet<>();
        for (AggregatorEntDevice device : devices) {
            addTelemetryDeviceId(result, device.getIotDeviceBaseId());
        }
        return new ArrayList<>(result);
    }

    private List<String> telemetryDeviceCodes(List<AggregatorEntDevice> devices) {
        Set<String> result = new LinkedHashSet<>();
        for (AggregatorEntDevice device : devices) {
            if (StringUtils.isNotBlank(device.getDeviceId())) {
                result.add(device.getDeviceId());
                result.add(normalizeTelemetryDeviceCode(device.getDeviceId()));
            }
        }
        result.remove(null);
        return new ArrayList<>(result);
    }

    private Map<String, Map<String, Map<String, Double>>> groupRunStatusData(
            List<IotTelemetryDataResp> telemetry, List<AggregatorEntDevice> devices) {
        Map<Long, String> idToBaseId = new HashMap<>();
        Map<String, String> codeToBaseId = new HashMap<>();
        for (AggregatorEntDevice device : devices) {
            Set<Long> ids = new LinkedHashSet<>();
            addTelemetryDeviceId(ids, device.getIotDeviceBaseId());
            ids.forEach(id -> idToBaseId.put(id, device.getDeviceBaseId()));
            if (StringUtils.isNotBlank(device.getDeviceId())) {
                codeToBaseId.put(device.getDeviceId(), device.getDeviceBaseId());
                codeToBaseId.put(normalizeTelemetryDeviceCode(device.getDeviceId()), device.getDeviceBaseId());
            }
        }
        Map<String, Map<String, Map<String, Double>>> result = new LinkedHashMap<>();
        if (telemetry == null) {
            return result;
        }
        for (IotTelemetryDataResp item : telemetry) {
            if (item == null || item.getDataTime() == null || StringUtils.isBlank(item.getPointCode())) {
                continue;
            }
            String deviceBaseId = idToBaseId.get(item.getDeviceId());
            if (deviceBaseId == null) {
                deviceBaseId = codeToBaseId.get(item.getDeviceCode());
            }
            if (deviceBaseId == null) {
                deviceBaseId = codeToBaseId.get(normalizeTelemetryDeviceCode(item.getDeviceCode()));
            }
            if (deviceBaseId == null) {
                continue;
            }
            String minute = DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(item.getDataTime().getTime())
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
            result.computeIfAbsent(deviceBaseId, key -> new LinkedHashMap<>())
                    .computeIfAbsent(item.getPointCode(), key -> new LinkedHashMap<>())
                    .put(minute, item.getValue());
        }
        return result;
    }

    private Map<String, Double> runStatusPoints(
            Map<String, Map<String, Map<String, Double>>> dataMap, String deviceBaseId, String metricCode) {
        Map<String, Map<String, Double>> metricMap = dataMap.get(deviceBaseId);
        return metricMap == null ? Collections.emptyMap()
                : metricMap.getOrDefault(metricCode, Collections.emptyMap());
    }

    private List<String> buildMinuteTimeList(LocalDate startDate, LocalDate endDate) {
        List<String> result = new ArrayList<>();
        LocalDateTime current = startDate.atStartOfDay().plusMinutes(1);
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        while (!current.isAfter(end)) {
            result.add(DATE_TIME_FORMATTER.format(current));
            current = current.plusMinutes(1);
        }
        return result;
    }

    private LineDataGraphVO buildRunStatusLine(String lineName, String chartName, MetricEnum metric,
                                                Map<String, Double> values, List<String> minuteAxis,
                                                String resourceTypeId) {
        LineDataGraphVO line = new LineDataGraphVO();
        line.setLineName(lineName);
        line.setChartName(chartName);
        line.setLineUnit(metric.getUnit());
        line.setGroupCode(metric.getGroupCode());
        line.setGroupName(metric.getGroupName());
        line.setDataRespList(minuteAxis.stream().map(time -> {
            Double value = values.get(time);
            if (value != null) {
                value = MathUtils.doublePoint(value, 2);
                if ("27".equals(resourceTypeId)) {
                    value = -value;
                }
            }
            return new DataResp(time, value);
        }).collect(Collectors.toList()));
        return line;
    }

    private LineDataGraphResp toLineGraphResp(List<LineDataGraphVO> lines) {
        LineDataGraphResp resp = new LineDataGraphResp();
        resp.setLineDataGraphVOList(lines);
        if (!lines.isEmpty()) {
            resp.setUnit(lines.get(0).getLineUnit());
            resp.setChartName(lines.get(0).getChartName());
        }
        return resp;
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

    private List<DataResp> buildEntQuarterAveragePowerChart(List<AggregatorEntDevice> deviceList,
                                                            DateRange dateRange,
                                                            List<String> quarterTimeList) {
        if (deviceList == null || deviceList.isEmpty() || quarterTimeList.isEmpty()) {
            return Collections.emptyList();
        }
        List<AggregatorEntDevice> onlineDeviceList = deviceList.stream()
                .filter(device -> Integer.valueOf(1).equals(device.getModelFlag()))
                .filter(device -> Integer.valueOf(1).equals(device.getStatus()))
                .collect(Collectors.toList());
        if (onlineDeviceList.isEmpty()) {
            return quarterTimeList.stream().map(time -> new DataResp(time, null)).collect(Collectors.toList());
        }

        List<DataResp> minutePowerChart = buildPowerChart(onlineDeviceList.get(0).getAggregatorId(), onlineDeviceList, dateRange);
        Map<String, Double> minuteValueMap = minutePowerChart.stream()
                .filter(item -> StringUtils.isNotBlank(item.getTime()))
                .collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1, LinkedHashMap::new));

        List<DataResp> result = new ArrayList<>(quarterTimeList.size());
        for (String quarterTime : quarterTimeList) {
            LocalDateTime endTime = LocalDateTime.parse(quarterTime, DATE_TIME_FORMATTER);
            double total = 0D;
            int count = 0;
            for (int i = 14; i >= 0; i--) {
                String minuteKey = DATE_TIME_FORMATTER.format(endTime.minusMinutes(i));
                Double value = minuteValueMap.get(minuteKey);
                if (value != null) {
                    total += value;
                    count++;
                }
            }
            result.add(new DataResp(quarterTime, count == 0 ? null : MathUtils.doublePoint(total / count, 2)));
        }
        return result;
    }

    private List<DataResp> buildPowerChart(String aggregatorId, List<AggregatorEntDevice> deviceList, DateRange dateRange) {
        if (deviceList == null || deviceList.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> deviceIds = new LinkedHashSet<>();
        Set<String> deviceCodes = deviceList.stream()
                .peek(device -> addTelemetryDeviceId(deviceIds, device.getIotDeviceBaseId()))
                .map(AggregatorEntDevice::getDeviceId)
                .filter(StringUtils::isNotBlank)
                .map(this::normalizeTelemetryDeviceCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (deviceIds.isEmpty() && deviceCodes.isEmpty()) {
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

    private List<DataResp> buildEntAdjustChart(NewUserAdjustmentGraphReq req, List<String> quarterTimeList) {
        List<AggregatorEntDateAdjust> adjustList = aggregatorEntDateAdjustService.getEntAdjust(
                req.getSubEntId(),
                req.getStartTime(),
                req.getEndTime(),
                req.getResourceTypeId()
        );
        if (adjustList == null || adjustList.isEmpty()) {
            return quarterTimeList.stream().map(time -> new DataResp(time, null)).collect(Collectors.toList());
        }

        Map<String, Double> valueMap = new LinkedHashMap<>();
        for (AggregatorEntDateAdjust item : adjustList) {
            if (StringUtils.isBlank(item.getProfitDetail())) {
                continue;
            }
            List<AggregatorEntDateAdjustResp> detailList = JSONArray.parseArray(item.getProfitDetail(), AggregatorEntDateAdjustResp.class);
            if (detailList == null || detailList.isEmpty()) {
                continue;
            }
            for (AggregatorEntDateAdjustResp detail : detailList) {
                if (detail == null || StringUtils.isBlank(detail.getEndTime())) {
                    continue;
                }
                String normalizedTime = normalizeCurveTime(item.getDate(), detail.getEndTime());
                if (normalizedTime == null) {
                    continue;
                }
                valueMap.put(normalizedTime, detail.getCountPower());
            }
        }
        return quarterTimeList.stream()
                .map(time -> new DataResp(time, valueMap.containsKey(time) ? MathUtils.doublePoint(valueMap.get(time), 2) : null))
                .collect(Collectors.toList());
    }

    private List<DataResp> buildEntBaseLineChart(NewUserAdjustmentGraphReq req, List<String> quarterTimeList) {
        List<AggregatorEntBaseLineLoadChart> baseLineList = aggregatorEntBaseLineLoadChartService.getEntBaseLine(
                req.getSubEntId(),
                req.getResourceTypeId(),
                req.getStartTime(),
                req.getEndTime()
        );
        if (baseLineList == null || baseLineList.isEmpty()) {
            return quarterTimeList.stream().map(time -> new DataResp(time, null)).collect(Collectors.toList());
        }
        Map<String, Double> valueMap = new LinkedHashMap<>();
        for (AggregatorEntBaseLineLoadChart item : baseLineList) {
            valueMap.putAll(parseCurveJson(item.getBaseDate(), item.getBaseLineLoadChart()));
        }
        return quarterTimeList.stream()
                .map(time -> new DataResp(time, valueMap.containsKey(time) ? valueMap.get(time) : null))
                .collect(Collectors.toList());
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
        if (StringUtils.isNotBlank(recordDate) && ("24:00".equals(value) || "24:00:00".equals(value))) {
            try {
                LocalDate date = LocalDate.parse(recordDate, DATE_FORMATTER).plusDays(1);
                return DATE_TIME_FORMATTER.format(LocalDateTime.of(date, LocalTime.MIN));
            } catch (DateTimeParseException ex) {
                log.warn("解析24点曲线时间失败, recordDate={}", recordDate, ex);
                return null;
            }
        }
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

    private DateRange resolveAdjustmentDateRange(String startDate, String endDate) {
        LocalDate start = parseDateOrDefault(startDate, LocalDate.now().minusDays(1));
        LocalDate end = parseDateOrDefault(endDate, start);
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
        ZoneId zoneId = ZoneId.systemDefault();
        Date startTime = Date.from(start.atStartOfDay(zoneId).toInstant());
        Date endTime = Date.from(end.plusDays(1).atStartOfDay(zoneId).toInstant());
        return new DateRange(start, end, startTime, endTime);
    }

    private List<String> buildQuarterTimeList(LocalDate startDate, LocalDate endDate) {
        List<String> result = new ArrayList<>();
        LocalDateTime current = startDate.atStartOfDay().plusMinutes(15);
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        while (!current.isAfter(end)) {
            result.add(DATE_TIME_FORMATTER.format(current));
            current = current.plusMinutes(15);
        }
        return result;
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

    private List<ExcelExportEntity> buildAdjustExportTitle() {
        List<ExcelExportEntity> entityList = new ArrayList<>();
        entityList.add(buildExportEntity("日期", "date", 15D));
        entityList.add(buildExportEntity("聚合商名称", "aggregatorName", 22D));
        entityList.add(buildExportEntity("用户名称", "entName", 22D));
        entityList.add(buildExportEntity("功率(kw)", "power", 18D));
        for (String label : buildQuarterLabels()) {
            entityList.add(buildExportEntity(label, label, 12D));
        }
        return entityList;
    }

    private List<ExcelExportEntity> buildBuzhaoExportTitle() {
        List<ExcelExportEntity> entityList = new ArrayList<>();
        entityList.add(buildExportEntity("时间", "-1", 20D));
        for (int i = 0; i < 60; i++) {
            entityList.add(buildExportEntity(String.valueOf(i), String.valueOf(i), 8D));
        }
        return entityList;
    }

    private ExcelExportEntity buildExportEntity(String name, String key, Double width) {
        ExcelExportEntity entity = new ExcelExportEntity();
        entity.setName(name);
        entity.setKey(key);
        entity.setOrderNum(1);
        if (width != null) {
            entity.setWidth(width);
        }
        return entity;
    }

    private List<String> buildQuarterLabels() {
        List<String> labels = new ArrayList<>(96);
        LocalTime current = LocalTime.of(0, 15);
        for (int i = 0; i < 96; i++) {
            if (i == 95) {
                labels.add("24:00");
            } else {
                labels.add(current.format(TIME_FORMATTER));
            }
            current = current.plusMinutes(15);
        }
        return labels;
    }

    private List<AggregatorEnt> resolveExportEntList(String aggregatorId, String entId) {
        if (StringUtils.isNotBlank(entId)) {
            AggregatorEnt ent = aggregatorEntService.getAggregatorEnt(entId);
            return ent == null ? Collections.emptyList() : Collections.singletonList(ent);
        }
        List<AggregatorEnt> entList = aggregatorEntService.getAggregatorEntList(aggregatorId);
        if (entList == null || entList.isEmpty()) {
            return Collections.emptyList();
        }
        Collator collator = Collator.getInstance(Locale.CHINA);
        return entList.stream()
                .filter(java.util.Objects::nonNull)
                .sorted((left, right) -> collator.compare(
                        StringUtils.defaultString(left.getEntName()),
                        StringUtils.defaultString(right.getEntName())))
                .collect(Collectors.toList());
    }

    private String resolveAggregatorName(String aggregatorId) {
        AggregatorInfo aggregatorInfo = aggregatorInfoService.getAggregatorInfo(aggregatorId);
        if (aggregatorInfo != null && StringUtils.isNotBlank(aggregatorInfo.getAggregatorName())) {
            return aggregatorInfo.getAggregatorName();
        }
        return aggregatorId;
    }

    private List<Map<String, String>> buildAggregatorAdjustExportRows(String aggregatorId,
                                                                      String resourceTypeId,
                                                                      String aggregatorName,
                                                                      DateRange dateRange,
                                                                      List<String> dateList,
                                                                      List<String> quarterAxis) {
        List<DataResp> quarterPowerChart = buildQuarterAverageChart(buildPowerChart(aggregatorId, resourceTypeId, dateRange), quarterAxis);
        Map<String, Double> powerMap = toDateTimeValueMap(quarterPowerChart);
        Map<String, Double> issueMap = buildIssueChartMap(aggregatorId, resourceTypeId, dateList);
        String startDate = DATE_FORMATTER.format(dateRange.getStartDate());
        String endDate = DATE_FORMATTER.format(dateRange.getEndDate());
        Map<String, Double> dapMap = buildDapChartMap(aggregatorId, resourceTypeId, startDate, endDate);
        Map<String, Double> baseLineMap = buildBaseLineChartMap(aggregatorId, resourceTypeId, startDate, endDate);
        Map<String, Double> crMap = buildCrChartMap(aggregatorId, resourceTypeId, startDate, endDate);
        Map<String, Double> issuePriceMap = buildIssuePriceMap(aggregatorId, resourceTypeId, startDate, endDate);

        List<Map<String, String>> rowList = new ArrayList<>();
        for (String date : dateList) {
            rowList.add(buildAdjustExportRow(date, aggregatorName, "", "聚合申报功率", issueMap, false));
            rowList.add(buildAdjustExportRow(date, aggregatorName, "", "实际汇总功率", powerMap, false));
            rowList.add(buildAdjustExportRow(date, aggregatorName, "", "基线", baseLineMap, false));
            rowList.add(buildAdjustExportRow(date, aggregatorName, "", "碳排因子", crMap, false));
            rowList.add(buildAdjustExportRow(date, aggregatorName, "", "调度下发功率", dapMap, false));
            rowList.add(buildAdjustExportRow(date, aggregatorName, "", "出清价格", issuePriceMap, false));
        }
        return rowList;
    }

    private List<Map<String, String>> buildEntAdjustExportRows(AggregatorEnt ent,
                                                               String aggregatorName,
                                                               String resourceTypeId,
                                                               DateRange dateRange,
                                                               List<String> dateList,
                                                               List<String> quarterAxis) {
        if (ent == null || StringUtils.isBlank(ent.getEntId())) {
            return Collections.emptyList();
        }
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getDeviceListByEntId(ent.getEntId(), resourceTypeId);
        NewUserAdjustmentGraphReq req = new NewUserAdjustmentGraphReq(
                DATE_FORMATTER.format(dateRange.getStartDate()),
                DATE_FORMATTER.format(dateRange.getEndDate()),
                ent.getEntId(),
                resourceTypeId
        );
        Map<String, Double> powerMap = toDateTimeValueMap(buildEntQuarterAveragePowerChart(deviceList, dateRange, quarterAxis));
        Map<String, Double> adjustMap = toDateTimeValueMap(buildEntAdjustChart(req, quarterAxis));
        Map<String, Double> baseLineMap = toDateTimeValueMap(buildEntBaseLineChart(req, quarterAxis));

        List<Map<String, String>> rowList = new ArrayList<>();
        for (String date : dateList) {
            rowList.add(buildAdjustExportRow(date, aggregatorName, ent.getEntName(), "有效调节负荷", adjustMap, false));
            rowList.add(buildAdjustExportRow(date, aggregatorName, ent.getEntName(), "实际调节功率", powerMap, false));
            rowList.add(buildAdjustExportRow(date, aggregatorName, ent.getEntName(), "基线", baseLineMap, false));
        }
        return rowList;
    }

    private Map<String, String> buildAdjustExportRow(String date,
                                                     String aggregatorName,
                                                     String entName,
                                                     String metricName,
                                                     Map<String, Double> valueMap,
                                                     boolean zeroWhenNull) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("date", date);
        row.put("aggregatorName", StringUtils.defaultString(aggregatorName));
        row.put("entName", StringUtils.defaultString(entName));
        row.put("power", metricName);
        for (String label : buildQuarterLabels()) {
            row.put(label, formatExportValue(valueMap.get(resolveQuarterDateTime(date, label)), zeroWhenNull));
        }
        return row;
    }

    private String resolveQuarterDateTime(String date, String quarterLabel) {
        if ("24:00".equals(quarterLabel)) {
            return DATE_TIME_FORMATTER.format(LocalDate.parse(date, DATE_FORMATTER).plusDays(1).atStartOfDay());
        }
        LocalTime time = LocalTime.parse(quarterLabel, TIME_FORMATTER);
        return DATE_TIME_FORMATTER.format(LocalDate.parse(date, DATE_FORMATTER).atTime(time));
    }

    private Map<String, Double> toDateTimeValueMap(List<DataResp> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Double> valueMap = new LinkedHashMap<>();
        for (DataResp item : dataList) {
            if (item == null || StringUtils.isBlank(item.getTime())) {
                continue;
            }
            String normalized = normalizeCurveTime(null, item.getTime());
            if (normalized != null) {
                valueMap.put(normalized, item.getValue());
            }
        }
        return valueMap;
    }

    private List<DataResp> buildQuarterAverageChart(List<DataResp> minuteChart, List<String> quarterAxis) {
        if (minuteChart == null || minuteChart.isEmpty() || quarterAxis == null || quarterAxis.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Double> minuteValueMap = new LinkedHashMap<>();
        for (DataResp item : minuteChart) {
            if (item == null || StringUtils.isBlank(item.getTime())) {
                continue;
            }
            String normalized = normalizeCurveTime(null, item.getTime());
            if (normalized != null) {
                minuteValueMap.putIfAbsent(normalized, item.getValue());
            }
        }

        List<DataResp> result = new ArrayList<>(quarterAxis.size());
        for (String quarterTime : quarterAxis) {
            LocalDateTime endTime = LocalDateTime.parse(quarterTime, DATE_TIME_FORMATTER);
            double total = 0D;
            int count = 0;
            for (int i = 14; i >= 0; i--) {
                Double value = minuteValueMap.get(DATE_TIME_FORMATTER.format(endTime.minusMinutes(i)));
                if (value != null) {
                    total += value;
                    count++;
                }
            }
            result.add(new DataResp(quarterTime, count == 0 ? null : MathUtils.doublePoint(total / count, 2)));
        }
        return result;
    }

    private List<Map<String, String>> buildBuzhaoExportRows(List<DataResp> powerChart, DateRange dateRange) {
        if (powerChart == null || powerChart.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Double> minuteValueMap = new LinkedHashMap<>();
        for (DataResp item : powerChart) {
            if (item == null || StringUtils.isBlank(item.getTime())) {
                continue;
            }
            String normalized = normalizeCurveTime(null, item.getTime());
            if (normalized != null) {
                minuteValueMap.putIfAbsent(normalized, item.getValue());
            }
        }

        List<Map<String, String>> rowList = new ArrayList<>();
        LocalDateTime current = dateRange.getStartDate().atStartOfDay();
        LocalDateTime end = dateRange.getEndDate().plusDays(1).atStartOfDay();
        while (current.isBefore(end)) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("-1", DATE_TIME_SECOND_FORMATTER.format(current));
            for (int minute = 0; minute < 60; minute++) {
                String fullTime = DATE_TIME_FORMATTER.format(current.plusMinutes(minute));
                row.put(String.valueOf(minute), formatExportValue(minuteValueMap.get(fullTime), true));
            }
            rowList.add(row);
            current = current.plusHours(1);
        }
        return rowList;
    }

    private String formatExportValue(Double value, boolean zeroWhenNull) {
        if (value == null) {
            return zeroWhenNull ? "0" : "";
        }
        return String.valueOf(MathUtils.doublePoint(value, 2));
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
