package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.console.model.vo.EnergyStationInfoAndDevice;
import cn.sl.ehub.console.model.vo.UserInfoAndDevice;
import cn.sl.ehub.console.service.IAggregatorEntDapChartService;
import cn.sl.ehub.console.service.IAggregatorEntDateApplyDetailService;
import cn.sl.ehub.console.service.IAggregatorEntDeviceService;
import cn.sl.ehub.console.service.ITodayService;
import cn.sl.ehub.console.service.IYesterdayService;
import cn.sl.ehub.service.dto.iot.IotTelemetryDataResp;
import cn.sl.ehub.service.dto.iot.IotTelemetryQueryReq;
import cn.sl.ehub.service.mapper.AggregatorEntDeviceIotLogMapper;
import cn.sl.ehub.service.resp.AggregatorEntDeviceIotLogResp;
import cn.sl.ehub.service.resp.EntUserDeviceTodayChartResp;
import cn.sl.ehub.service.resp.EntUserDeviceTodayElectricCurrentChartResp;
import cn.sl.ehub.service.resp.EntUserDeviceYesterdayChartResp;
import cn.sl.ehub.service.service.IotTelemetryQueryService;
import cn.sl.ehub.service.vo.AggregatorEntDateApplyDetail;
import cn.sl.ehub.service.vo.AggregatorEntDapChart;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorEntDeviceIotLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodayServiceImpl implements ITodayService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_TIME_SECOND_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final IYesterdayService yesterdayService;
    private final IAggregatorEntDeviceService aggregatorEntDeviceService;
    private final AggregatorEntDeviceIotLogMapper aggregatorEntDeviceIotLogMapper;
    private final IAggregatorEntDateApplyDetailService aggregatorEntDateApplyDetailService;
    private final IAggregatorEntDapChartService aggregatorEntDapChartService;
    private final IotTelemetryQueryService iotTelemetryQueryService;

    @Override
    public EntUserDeviceTodayChartResp getEntUserDeviceTodayChartResp(String deviceBaseId) {
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(Collections.singletonList(deviceBaseId));
        if (CollectionUtils.isEmpty(deviceList)) {
            return emptyTodayChart();
        }
        return buildSingleDeviceTodayChart(deviceList.get(0), deviceList);
    }

    @Override
    public EntUserDeviceTodayChartResp getDeviceTreeTodayChartResp(String deviceBaseId, String energyStationcode, String systemCode) {
        if (StringUtils.isNotBlank(deviceBaseId)) {
            return getEntUserDeviceTodayChartResp(deviceBaseId);
        }
        if (StringUtils.isNotBlank(energyStationcode)) {
            List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getDeviceByStationCode(energyStationcode);
            if (CollectionUtils.isEmpty(deviceList)) {
                return emptyTodayChart();
            }
            return buildGroupedTodayChart(deviceList, false);
        }
        if (StringUtils.isNotBlank(systemCode)) {
            List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getDeviceBySystemCode(systemCode);
            if (CollectionUtils.isEmpty(deviceList)) {
                return emptyTodayChart();
            }
            return buildGroupedTodayChart(deviceList, true);
        }
        return emptyTodayChart();
    }

    @Override
    public List<AggregatorEntDeviceIotLogResp> getIotLog(String entId, String stationId, String resourceTypeId, String deviceBaseId) {
        String date = LocalDate.now().format(DATE_FORMATTER);
        Weekend<AggregatorEntDeviceIotLog> weekend = Weekend.of(AggregatorEntDeviceIotLog.class);
        WeekendCriteria<AggregatorEntDeviceIotLog, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDeviceIotLog::getEntId, entId);
        criteria.andEqualTo(AggregatorEntDeviceIotLog::getStationId, stationId);
        criteria.andEqualTo(AggregatorEntDeviceIotLog::getResourceTypeId, resourceTypeId);
        criteria.andGreaterThanOrEqualTo(AggregatorEntDeviceIotLog::getSendTime, date + " 00:00:00");
        criteria.andLessThanOrEqualTo(AggregatorEntDeviceIotLog::getSendTime, date + " 23:59:59");
        if (StringUtils.isNotBlank(deviceBaseId)) {
            criteria.andEqualTo(AggregatorEntDeviceIotLog::getDeviceBaseId, deviceBaseId);
        }
        List<AggregatorEntDeviceIotLog> logList = aggregatorEntDeviceIotLogMapper.selectByExample(weekend);
        if (CollectionUtils.isEmpty(logList)) {
            return Collections.emptyList();
        }
        List<AggregatorEntDeviceIotLogResp> result = new ArrayList<>(logList.size());
        for (AggregatorEntDeviceIotLog item : logList) {
            AggregatorEntDeviceIotLogResp resp = new AggregatorEntDeviceIotLogResp();
            resp.setDeviceName(item.getDeviceName());
            resp.setResultMsg(item.getResultMsg());
            resp.setSendTime(toShortTime(item.getSendTime()));
            result.add(resp);
        }
        Collections.sort(result);
        return result;
    }

    @Override
    public List<UserInfoAndDevice> getDevices(String aggregatorId, String resourceType) {
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getDeviceListByAggregatorId(aggregatorId, resourceType);
        if (CollectionUtils.isEmpty(deviceList)) {
            return Collections.emptyList();
        }
        String date = LocalDate.now().format(DATE_FORMATTER);
        List<String> entIdList = deviceList.stream()
                .map(AggregatorEntDevice::getEntId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, AggregatorEntDateApplyDetail> applyDetailMap = aggregatorEntDateApplyDetailService
                .getAggregatorEntDateApplyDetailList(entIdList, date)
                .stream()
                .collect(Collectors.toMap(AggregatorEntDateApplyDetail::getEntId, Function.identity(), (v1, v2) -> v1, LinkedHashMap::new));
        Map<String, AggregatorEntDapChart> winMap = aggregatorEntDapChartService.getBatchDapLineByEntId(entIdList, date)
                .stream()
                .filter(item -> StringUtils.isNotBlank(item.getDapChart()))
                .collect(Collectors.toMap(AggregatorEntDapChart::getEntId, Function.identity(), (v1, v2) -> v1, LinkedHashMap::new));

        Map<String, List<AggregatorEntDevice>> stationDeviceMap = deviceList.stream()
                .collect(Collectors.groupingBy(AggregatorEntDevice::getStationId, LinkedHashMap::new, Collectors.toList()));

        List<UserInfoAndDevice> result = new ArrayList<>(stationDeviceMap.size());
        for (Map.Entry<String, List<AggregatorEntDevice>> entry : stationDeviceMap.entrySet()) {
            List<AggregatorEntDevice> stationDevices = entry.getValue();
            AggregatorEntDevice first = stationDevices.get(0);
            UserInfoAndDevice user = new UserInfoAndDevice();
            user.setEntId(first.getEntId());
            user.setDeviceBaseId(entry.getKey());
            user.setDeviceName(first.getUsername());
            user.setDeviceType("3");
            user.setApplyStatus(applyDetailMap.containsKey(first.getEntId()) ? "1" : "0");
            user.setWinStatu(winMap.containsKey(first.getEntId()));
            user.setChildren(buildStationChildren(stationDevices));
            result.add(user);
        }
        result.sort(Comparator.comparing(UserInfoAndDevice::getWinStatu, Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    private List<EnergyStationInfoAndDevice> buildStationChildren(List<AggregatorEntDevice> stationDevices) {
        List<EnergyStationInfoAndDevice> result = new ArrayList<>();
        List<AggregatorEntDevice> directDevices = stationDevices.stream()
                .filter(device -> StringUtils.isBlank(device.getEnergyStation()))
                .collect(Collectors.toList());
        for (AggregatorEntDevice device : directDevices) {
            EnergyStationInfoAndDevice node = new EnergyStationInfoAndDevice();
            node.setDeviceBaseId(device.getDeviceBaseId());
            node.setDeviceName(device.getDeviceName());
            node.setResourceTypeId(device.getResourceTypeId());
            node.setDeviceType("1");
            node.setChildren(Collections.singletonList(device));
            result.add(node);
        }
        Map<String, List<AggregatorEntDevice>> energyStationMap = stationDevices.stream()
                .filter(device -> StringUtils.isNotBlank(device.getEnergyStationCode()))
                .collect(Collectors.groupingBy(AggregatorEntDevice::getEnergyStationCode, LinkedHashMap::new, Collectors.toList()));
        for (Map.Entry<String, List<AggregatorEntDevice>> entry : energyStationMap.entrySet()) {
            List<AggregatorEntDevice> devices = entry.getValue();
            AggregatorEntDevice first = devices.get(0);
            devices.forEach(device -> device.setDeviceType("1"));
            EnergyStationInfoAndDevice node = new EnergyStationInfoAndDevice();
            node.setDeviceBaseId(entry.getKey());
            node.setDeviceName(first.getEnergyStation());
            node.setDeviceType("0");
            node.setChildren(devices);
            result.add(node);
        }
        return result;
    }

    private EntUserDeviceTodayChartResp buildSingleDeviceTodayChart(AggregatorEntDevice device,
                                                                    List<AggregatorEntDevice> deviceList) {
        LocalDate today = LocalDate.now();
        String date = today.format(DATE_FORMATTER);
        EntUserDeviceTodayChartResp resp = new EntUserDeviceTodayChartResp();
        resp.setEntUserDeviceYesterdayChartResp(yesterdayService.getEntUserDeviceChartResp(device.getDeviceBaseId(), deviceList, date));

        List<String> fullAxis = LoadAggregationChartSupport.buildDayMinuteDateTimeAxis(today);
        Map<String, List<IotTelemetryDataResp>> pointDataMap = queryDevicePoints(device, today,
                LoadAggregationChartSupport.NO_POWER_POINT_CODE,
                LoadAggregationChartSupport.CURRENT_A_POINT_CODE,
                LoadAggregationChartSupport.CURRENT_B_POINT_CODE,
                LoadAggregationChartSupport.CURRENT_C_POINT_CODE,
                LoadAggregationChartSupport.ZERO_POWER_POINT_CODE);

        resp.setNoPowerChart(alignPointSeries(fullAxis, pointDataMap.get(LoadAggregationChartSupport.NO_POWER_POINT_CODE), device.getResourceTypeId()));

        EntUserDeviceTodayElectricCurrentChartResp currentResp = new EntUserDeviceTodayElectricCurrentChartResp();
        currentResp.setIaList(alignPointSeries(fullAxis, pointDataMap.get(LoadAggregationChartSupport.CURRENT_A_POINT_CODE), null));
        currentResp.setIbList(alignPointSeries(fullAxis, pointDataMap.get(LoadAggregationChartSupport.CURRENT_B_POINT_CODE), null));
        currentResp.setIcList(alignPointSeries(fullAxis, pointDataMap.get(LoadAggregationChartSupport.CURRENT_C_POINT_CODE), null));
        resp.setEntUserDeviceTodayElectricCurrentChartResp(currentResp);

        resp.setZeroPointElectricityQuantityChart(buildZeroPointSeries(today, fullAxis, pointDataMap.get(LoadAggregationChartSupport.ZERO_POWER_POINT_CODE)));
        return resp;
    }

    private EntUserDeviceTodayChartResp buildGroupedTodayChart(List<AggregatorEntDevice> deviceList, boolean entLevel) {
        LocalDate today = LocalDate.now();
        String date = today.format(DATE_FORMATTER);
        EntUserDeviceTodayChartResp resp = emptyTodayChart();
        EntUserDeviceYesterdayChartResp powerResp = entLevel
                ? yesterdayService.getEntUserChartResp(deviceList, date, deviceList.get(0).getStationId())
                : yesterdayService.getEntUserDeviceListChartResp(deviceList, date);
        resp.setEntUserDeviceYesterdayChartResp(powerResp);
        return resp;
    }

    private Map<String, List<IotTelemetryDataResp>> queryDevicePoints(AggregatorEntDevice device,
                                                                      LocalDate today,
                                                                      String... pointCodes) {
        Long deviceId = parseLong(device.getIotDeviceBaseId());
        if (deviceId == null || pointCodes == null || pointCodes.length == 0) {
            return Collections.emptyMap();
        }
        IotTelemetryQueryReq req = new IotTelemetryQueryReq();
        req.setDeviceIds(Collections.singletonList(deviceId));
        req.setPointCodes(java.util.Arrays.asList(pointCodes));
        req.setStartTime(today.format(DATE_FORMATTER) + " 00:00:00");
        req.setEndTime(today.plusDays(1).format(DATE_FORMATTER) + " 00:00:00");
        req.setLimit(10000);
        List<IotTelemetryDataResp> list = iotTelemetryQueryService.queryData(req).getList();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().collect(Collectors.groupingBy(IotTelemetryDataResp::getPointCode, LinkedHashMap::new, Collectors.toList()));
    }

    private List<DataResp> alignPointSeries(List<String> fullAxis,
                                            List<IotTelemetryDataResp> dataList,
                                            String resourceTypeId) {
        if (fullAxis == null || fullAxis.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Double> valueMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(dataList)) {
            valueMap = dataList.stream()
                    .filter(item -> item.getDataTime() != null)
                    .collect(Collectors.toMap(
                            item -> DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(item.getDataTime().getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime()),
                            IotTelemetryDataResp::getValue,
                            (v1, v2) -> v1,
                            LinkedHashMap::new));
        }
        return LoadAggregationChartSupport.alignMinuteAxis(fullAxis, valueMap, resourceTypeId);
    }

    private List<DataResp> buildZeroPointSeries(LocalDate today,
                                                List<String> fullAxis,
                                                List<IotTelemetryDataResp> dataList) {
        if (fullAxis == null || fullAxis.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Double> valueMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(dataList)) {
            valueMap = dataList.stream()
                    .filter(item -> item.getDataTime() != null)
                    .collect(Collectors.toMap(
                            item -> DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(item.getDataTime().getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime()),
                            IotTelemetryDataResp::getValue,
                            (v1, v2) -> v1,
                            LinkedHashMap::new));
        }
        Double zeroValue = LoadAggregationChartSupport.firstValue(valueMap, today.format(DATE_FORMATTER) + " 00:00");
        List<DataResp> result = new ArrayList<>(fullAxis.size());
        for (String axis : fullAxis) {
            Double value = valueMap.get(axis);
            Double diff = value == null || zeroValue == null ? null : value - zeroValue;
            result.add(new DataResp(LoadAggregationChartSupport.toShortTime(axis), diff == null ? null : cn.sl.ehub.common.utils.MathUtils.doublePoint(diff, 2)));
        }
        return result;
    }

    private EntUserDeviceTodayChartResp emptyTodayChart() {
        EntUserDeviceTodayChartResp resp = new EntUserDeviceTodayChartResp();
        EntUserDeviceYesterdayChartResp powerResp = new EntUserDeviceYesterdayChartResp();
        powerResp.setBaseLineChart(Collections.emptyList());
        powerResp.setIssueChart(Collections.emptyList());
        powerResp.setPowerChart(Collections.emptyList());
        powerResp.setTimeColorRespList(Collections.emptyList());
        resp.setEntUserDeviceYesterdayChartResp(powerResp);
        resp.setNoPowerChart(Collections.emptyList());
        resp.setZeroPointElectricityQuantityChart(Collections.emptyList());

        EntUserDeviceTodayElectricCurrentChartResp currentResp = new EntUserDeviceTodayElectricCurrentChartResp();
        currentResp.setIaList(Collections.emptyList());
        currentResp.setIbList(Collections.emptyList());
        currentResp.setIcList(Collections.emptyList());
        resp.setEntUserDeviceTodayElectricCurrentChartResp(currentResp);
        return resp;
    }

    private Long parseLong(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Long.valueOf(StringUtils.trim(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String toShortTime(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        try {
            return DateTimeFormatter.ofPattern("HH:mm").format(LocalDate.parse(value.substring(0, 10)).atTime(
                    Integer.parseInt(value.substring(11, 13)),
                    Integer.parseInt(value.substring(14, 16))));
        } catch (Exception ex) {
            try {
                return DateTimeFormatter.ofPattern("HH:mm").format(
                        java.time.LocalDateTime.parse(value, DATE_TIME_SECOND_FORMATTER));
            } catch (Exception ignore) {
                return value;
            }
        }
    }
}
