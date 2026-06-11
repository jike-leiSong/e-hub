package cn.sl.ehub.console.service.impl;

import cn.enn.bigdata.resp.BigDataHistoryAndCalculationResp;
import cn.enn.bigdata.resp.BigDataHistoryResp;
import cn.enn.bigdata.service.IBigDataHandlerService;
import cn.sl.ehub.common.enums.*;
import cn.sl.ehub.service.req.AggregatorApplyReq;
import cn.sl.ehub.service.req.AggregatorEntApplyPlanReq;
import cn.sl.ehub.console.req.SaveDevicePercentBaseLoadSqlReq;
import cn.sl.ehub.common.utils.GZIPUtil;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.common.vo.DeviceGroupPointInfo;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.console.model.req.AggregatorIssueProfitReq;
import cn.sl.ehub.service.resp.*;
import cn.sl.ehub.console.service.*;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.common.utils.RedisUtil;
import cn.sl.ehub.service.vo.*;
import cn.enn.sms.req.SendMessageReq;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * 数据支持ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DataSupportServiceImpl implements IDataSupportService {

    private final RedisUtil redis;
    private final IAggregatorResourceDateIssueOfferService aggregatorResourceDateIssueOfferService;
    private final IAggregatorResourceDateDeliveryOfferService aggregatorResourceDateDeliveryOfferService;
    private final IAggregatorEntDeviceService aggregatorEntDeviceService;
    private final IAggregatorDateApplyDetailService aggregatorDateApplyDetailService;
    private final IAggregatorEntSimulateService aggregatorEntSimulateService;
    private final IAggregatorDeviceDateDeliveryChartService aggregatorDeviceDateDeliveryChartService;
    private final IBigDataHandlerService bigDataHistoryService;
    private final IAggregatorDeviceDateIssueChartService aggregatorDeviceDateIssueChartService;
    private final IAggregatorDeviceDateBaseLineLoadChartService aggregatorDeviceDateBaseLineLoadChartService;
    private final IAggregatorDateDeliveryChartService aggregatorDateDeliveryChartService;
    private final IAggregatorDateIssueChartService aggregatorDateIssueChartService;
    private final IAggregatorDateProfitService aggregatorDateProfitService;
    private final IAggregatorEntService aggregatorEntService;
    private final IAggregatorEntDateProfitService aggregatorEntDateProfitService;
    private final IAggregatorDeviceDateProfitService aggregatorDeviceDateProfitService;
    private final IAggregatorEntDeviceIotLogService aggregatorEntDeviceIotLogService;
    private final IAggregatorEntDateApplyDetailService aggregatorEntDateApplyDetailService;
    private final IAggregatorDateHolidayService aggregatorDateHolidayService;
    private final IAggregatorEntApplyPlanService aggregatorEntApplyPlanService;
    private final IAggregatorInfoService aggregatorInfoService;
    private final IAggregatorEntDateDeviceStartStopPlanService aggregatorEntDateDeviceStartStopPlanService;
    private final IAggregatorDevicePointService aggregatorDevicePointService;
    private final ISmsService pushService;
    private final IAggregatorDeviceDeliveryPowerPercentService aggregatorDeviceDeliveryPowerPercentService;
    private final IAggregatorResourceTypeService aggregatorResourceTypeService;
    private final ITomorrowService detailService;
    @Resource(name = "threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor executor;

    @Override
    public Boolean addDeviceIotLog() {
        String now = DateUtils.getTime();
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList();
        if (null != deviceList && deviceList.size() > 0) {
            List<AggregatorEntDeviceIotLog> logList = Lists.newArrayList();
            deviceList.forEach(device -> {
                AggregatorEntDeviceIotLog aggregatorEntDeviceIotLog = new AggregatorEntDeviceIotLog();
                aggregatorEntDeviceIotLog.setAggregatorId(device.getAggregatorId());
                aggregatorEntDeviceIotLog.setEntId(device.getEntId());
                aggregatorEntDeviceIotLog.setStationId(device.getStationId());
                aggregatorEntDeviceIotLog.setDeviceBaseId(device.getDeviceBaseId());
                aggregatorEntDeviceIotLog.setDeviceId(device.getDeviceId());
                aggregatorEntDeviceIotLog.setDeviceName(device.getDeviceName());
                aggregatorEntDeviceIotLog.setDeviceType(device.getDeviceType());
                aggregatorEntDeviceIotLog.setSendTime(now);
                aggregatorEntDeviceIotLog.setResultMsg("执行成功");
                logList.add(aggregatorEntDeviceIotLog);
            });
            if (null != logList && logList.size() > 0) {
                aggregatorEntDeviceIotLogService.batchInsert(logList);
            }
        }
        return null;
    }

    @Override
    public Boolean addDeviceBaselineLoadChart(String deviceBaseId, String startDate, String endDate, String date, String simulate) {
//        List<DataResp> dataRespListByMinuteAvg = Lists.newArrayList();
        List<DataResp> dataRespListByDayAvg = Lists.newArrayList();
        if (StringUtils.isEmpty(date)) {
            date = DateUtils.getDay();
        }
        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:45:00";
        List<String> minuteList = DateUtils.getMinuteList(date + " 00:00:00", date + " 23:45:00", 15);
        AggregatorEntDevice device = aggregatorEntDeviceService.getAggregatorEntDevice(deviceBaseId);
//        List<DeviceGroupPointInfo> deviceGroupPointInfoList = aggregatorDevicePointService.getDeviceGroupPointInfoList(Arrays.asList(device), MetricEnum.YES_POWER.getCode());
//        List<BigDataHistoryAndCalculationResp> bigDataHistoryAndCalculationRespList = bigDataHistoryService.queryBigData(Arrays.asList(device), deviceGroupPointInfoList, "1minute", startTime, endTime, simulate);
//        if (null != bigDataHistoryAndCalculationRespList && bigDataHistoryAndCalculationRespList.size() > 0) {
//            BigDataHistoryAndCalculationResp bigDataHistoryAndCalculationResp = bigDataHistoryAndCalculationRespList.get(0);
//            if (null != bigDataHistoryAndCalculationResp) {
//                List<DataResp> dataRespList = bigDataHistoryAndCalculationResp.getDataResp();
//                if (null != dataRespList && dataRespList.size() > 0) {
//                    Map<String, List<DataResp>> timeListMap = dataRespList.stream().filter(dataResp -> null != dataResp && StringUtils.isNotEmpty(dataResp.getTime())).collect(groupingBy(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm")));
//                    String finalDate = date;
//                    minuteList.forEach(minute -> {
//                        DataResp dataResp = new DataResp();
//                        dataResp.setTime(finalDate + " " + DateUtils.format(minute, "HH:mm") + ":00");
//                        List<DataResp> dataRespListByValue = timeListMap.get(DateUtils.format(minute, "HH:mm"));
//                        if (CollectionUtils.isNotEmpty(dataRespListByValue)) {
//                            List<DataResp> timeListMapEntryDataRespList = dataRespListByValue.stream().filter(timeListMapEntryDataResp -> null != timeListMapEntryDataResp && null != timeListMapEntryDataResp.getValue()).collect(toList());
//                            if (CollectionUtils.isNotEmpty(timeListMapEntryDataRespList)) {
//                                Double power = MathUtils.doublePoint(timeListMapEntryDataRespList.stream().mapToDouble(DataResp::getValue).sum() / timeListMapEntryDataRespList.size(), 2);
//                                if (device.getResourceTypeId().equals(EnergyModelEnum.DISTRIBUTED_ENERGY.getChannelNo())) {
//                                    dataResp.setValue(MathUtils.subDouble(0D, power));
//                                } else {
//                                    dataResp.setValue(power);
//                                }
//                            }
//                        }
//                        dataRespListByDayAvg.add(dataResp);
//                    });
////                    if (null != dataRespListByDayAvg && dataRespListByDayAvg.size() > 0) {
////                        for (int i = 0; i < dataRespListByDayAvg.size(); i += 15) {
////                            DataResp dataResp = new DataResp();
////                            dataResp.setTime(dataRespListByDayAvg.get(i).getTime());
////                            List<DataResp> dataRespListByMinute = dataRespListByDayAvg.subList(i, i + 15).stream().filter(dataRespByMinute -> null != dataRespByMinute && null != dataRespByMinute.getValue()).collect(toList());
////                            if (CollectionUtils.isNotEmpty(dataRespListByMinute)) {
////                                dataResp.setValue(MathUtils.doublePoint(dataRespListByMinute.stream().mapToDouble(DataResp::getValue).sum() / dataRespListByMinute.size(), 2));
////                            }
////                            dataRespListByMinuteAvg.add(dataResp);
////                        }
////                    }
//                }
//            }
//        }
        List<BigDataHistoryResp> bigDataHistoryRespList = bigDataHistoryService.getBigData(Arrays.asList(device), Arrays.asList(MetricEnum.YES_POWER.getCode()), startTime, endTime, simulate);
        if (null != bigDataHistoryRespList && bigDataHistoryRespList.size() > 0) {
            BigDataHistoryResp bigDataHistoryResp = bigDataHistoryRespList.get(0);
            if (null != bigDataHistoryResp) {
                List<DataResp> dataRespList = bigDataHistoryResp.getDataResp();
                if (null != dataRespList && dataRespList.size() > 0) {
                    Map<String, List<DataResp>> timeListMap = dataRespList.stream().filter(dataResp -> null != dataResp && StringUtils.isNotEmpty(dataResp.getTime())).collect(groupingBy(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm")));
                    String finalDate = date;
                    minuteList.forEach(minute -> {
                        DataResp dataResp = new DataResp();
                        dataResp.setTime(finalDate + " " + DateUtils.format(minute, "HH:mm") + ":00");
                        List<DataResp> dataRespListByValue = timeListMap.get(DateUtils.format(minute, "HH:mm"));
                        if (CollectionUtils.isNotEmpty(dataRespListByValue)) {
                            List<DataResp> timeListMapEntryDataRespList = dataRespListByValue.stream().filter(timeListMapEntryDataResp -> null != timeListMapEntryDataResp && null != timeListMapEntryDataResp.getValue()).collect(toList());
                            if (CollectionUtils.isNotEmpty(timeListMapEntryDataRespList)) {
                                Double power = MathUtils.doublePoint(timeListMapEntryDataRespList.stream().mapToDouble(DataResp::getValue).sum() / timeListMapEntryDataRespList.size(), 8);
                                if (device.getResourceTypeId().equals(EnergyModelEnum.DISTRIBUTED_ENERGY.getChannelNo())) {
                                    dataResp.setValue(MathUtils.subDouble(0D, power));
                                } else {
                                    dataResp.setValue(power);
                                }
                            }
                        }
                        dataRespListByDayAvg.add(dataResp);
                    });
//                    if (null != dataRespListByDayAvg && dataRespListByDayAvg.size() > 0) {
//                        for (int i = 0; i < dataRespListByDayAvg.size(); i += 15) {
//                            DataResp dataResp = new DataResp();
//                            dataResp.setTime(dataRespListByDayAvg.get(i).getTime());
//                            List<DataResp> dataRespListByMinute = dataRespListByDayAvg.subList(i, i + 15).stream().filter(dataRespByMinute -> null != dataRespByMinute && null != dataRespByMinute.getValue()).collect(toList());
//                            if (CollectionUtils.isNotEmpty(dataRespListByMinute)) {
//                                dataResp.setValue(MathUtils.doublePoint(dataRespListByMinute.stream().mapToDouble(DataResp::getValue).sum() / dataRespListByMinute.size(), 2));
//                            }
//                            dataRespListByMinuteAvg.add(dataResp);
//                        }
//                    }
                }
            }
        }
        AggregatorDeviceDateBaseLineLoadChart aggregatorDeviceDateBaseLineLoadChart = new AggregatorDeviceDateBaseLineLoadChart();
        aggregatorDeviceDateBaseLineLoadChart.setAggregatorId(device.getAggregatorId());
        aggregatorDeviceDateBaseLineLoadChart.setEntId(device.getEntId());
        aggregatorDeviceDateBaseLineLoadChart.setStationId(device.getStationId());
        aggregatorDeviceDateBaseLineLoadChart.setDeviceBaseId(deviceBaseId);
        if (CollectionUtils.isNotEmpty(dataRespListByDayAvg)) {
            List<DataResp> sortList = dataRespListByDayAvg.stream().sorted(Comparator.comparing(DataResp::getTime)).collect(toList());
            aggregatorDeviceDateBaseLineLoadChart.setBaseLineLoadChart(JSONObject.toJSONString(sortList));
        }
        aggregatorDeviceDateBaseLineLoadChart.setStartDate(date);
        aggregatorDeviceDateBaseLineLoadChart.setEndDate(date);
        aggregatorDeviceDateBaseLineLoadChartService.delete(deviceBaseId, date);
        aggregatorDeviceDateBaseLineLoadChartService.insert(aggregatorDeviceDateBaseLineLoadChart);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handAggregatorIssueChart(String aggregatorId, String winStatus, String date) {
        String now = DateUtils.getTime();
        if (StringUtils.isEmpty(date)) {
            date = DateUtils.getNextDay();
        }
        //修改聚合中标状态
        saveAggregatorWinStatus(now, aggregatorId, winStatus, date);
        //保存企业中标状态
        saveAggregatorEntDateApplyDetail(aggregatorId, winStatus, Arrays.asList(date));
        //中标
        if (winStatus.equals("1")) {
            //保存聚合商下发曲线
            saveAggregatorDateChart(aggregatorId, date);
            //保存设备下发曲线
            saveAggregatorDeviceDateIssueChart(aggregatorId, date);
            //保存下发报价
            saveAggregatorResourceDateDeliveryOffer(aggregatorId, date);
        }
//        try {
//            for (AggregatorRefreshEnum aggregatorRefreshEnum : AggregatorRefreshEnum.values()) {
//                SendMessageReq req = new SendMessageReq();
//                req.setContent(aggregatorRefreshEnum.getCode());
//                req.setEntId(aggregatorId);
//                pushService.sendSocket(req);
//            }
//        } catch (Exception e) {
////            log.info("推送消息失败");
////        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean dealDevicePowerAndQuantity(String deviceBaseId, String date) {
        if (StringUtils.isEmpty(date)) {
            date = DateUtils.getLastDay();
        }
        String startTime = date + " 00:00:00";
        String endTime = DateUtils.getAddDate(date) + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime);
        List<String> minuteListWith15 = DateUtils.getMinuteList(startTime, endTime, 15);
        Map<String, Map<String, Double>> deliveryPowerMap = new HashMap<>();
        Map<String, Map<String, Double>> issuePowerMap = new HashMap<>();
        List<AggregatorEntDevice> aggregatorEntDeviceList = Lists.newArrayList();
        if (StringUtils.isEmpty(deviceBaseId)) {
            //查询中标数据
//            List<AggregatorDateApplyDetail> aggregatorDateApplyDetailList = aggregatorDateApplyDetailService.getAggregatorDateApplyDetailList(null, date, "1");
//            if (null == aggregatorDateApplyDetailList || aggregatorDateApplyDetailList.size() <= 0) {
//                return true;
//            }
//            List<String> aggregatorIdList = aggregatorDateApplyDetailList.stream().map(AggregatorDateApplyDetail::getAggregatorId).collect(toList());
            List<String> aggregatorIdList = aggregatorInfoService.getAggregatorInfoList().stream().map(AggregatorInfo::getAggregatorId).collect(toList());
            //查询设备申报数据
            List<AggregatorDeviceDateDeliveryChart> aggregatorDeviceDateDeliveryChartList = aggregatorDeviceDateDeliveryChartService.getAggregatorDeviceDateDeliveryChartList(aggregatorIdList, date);
            Map<String, Map<String, Double>> getDeliveryPowerMap = getDeliveryPowerMap(aggregatorDeviceDateDeliveryChartList);
            if (null != getDeliveryPowerMap && getDeliveryPowerMap.size() > 0) {
                deliveryPowerMap.putAll(getDeliveryPowerMap);
            }
            //查询设备下发数据
            List<AggregatorDeviceDateIssueChart> aggregatorDeviceDateIssueChartList = aggregatorDeviceDateIssueChartService.getAggregatorDeviceDateIssueChartList(aggregatorIdList, date);
            Map<String, Map<String, Double>> getIssuePowerMap = getIssuePowerMap(aggregatorDeviceDateIssueChartList);
            if (null != getIssuePowerMap && getIssuePowerMap.size() > 0) {
                issuePowerMap.putAll(getIssuePowerMap);
            }
            //查询设备信息
            aggregatorEntDeviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList();
        } else {
            //查询设备申报数据
            AggregatorDeviceDateDeliveryChart aggregatorDeviceDateDeliveryChart = aggregatorDeviceDateDeliveryChartService.getAggregatorDeviceDateDeliveryChart(deviceBaseId, date);
            if (null != aggregatorDeviceDateDeliveryChart && StringUtils.isNotEmpty(aggregatorDeviceDateDeliveryChart.getDeliveryChart())) {
                Map<String, Map<String, Double>> getDeliveryPowerMap = getDeliveryPowerMap(Arrays.asList(aggregatorDeviceDateDeliveryChart));
                if (null != getDeliveryPowerMap && getDeliveryPowerMap.size() > 0) {
                    deliveryPowerMap.putAll(getDeliveryPowerMap);
                }
            }
            //查询设备下发数据
            AggregatorDeviceDateIssueChart aggregatorDeviceDateIssueChart = aggregatorDeviceDateIssueChartService.getAggregatorDeviceDateIssueChart(deviceBaseId, date);
            if (null != aggregatorDeviceDateIssueChart && StringUtils.isNotEmpty(aggregatorDeviceDateIssueChart.getIssueChart())) {
                Map<String, Map<String, Double>> getIssuePowerMap = getIssuePowerMap(Arrays.asList(aggregatorDeviceDateIssueChart));
                if (null != getIssuePowerMap && getIssuePowerMap.size() > 0) {
                    issuePowerMap.putAll(getIssuePowerMap);
                }
            }
            //查询设备信息
            AggregatorEntDevice aggregatorEntDevice = aggregatorEntDeviceService.getAggregatorEntDevice(deviceBaseId);
            if (null != aggregatorEntDevice) {
                aggregatorEntDeviceList.add(aggregatorEntDevice);
            }
        }
        if (CollectionUtils.isEmpty(aggregatorEntDeviceList)) {
            return true;
        }
        //查询仿真
        List<String> entSimulateList = Lists.newArrayList();
//        List<AggregatorEntSimulate> aggregatorEntSimulateList = aggregatorEntSimulateService.getAggregatorEntSimulateList();
//        if (null != aggregatorEntSimulateList && aggregatorEntSimulateList.size() > 0) {
//            entSimulateList = aggregatorEntSimulateList
//                    .stream()
//                    .filter(aggregatorEntSimulate -> aggregatorEntSimulate.getSimulate().equals("1"))
//                    .map(AggregatorEntSimulate::getEntId)
//                    .collect(toList());
//        }
        //查询出清价格
        Map<String, Double> aggregatorPriceMap = getAggregatorPriceMap(date);
        //查询实时功率
        Map<String, Map<String, Double>> realTimeAvgPowerMap = getRealTimeAvgPower(aggregatorEntDeviceList, startTime, endTime, entSimulateList, minuteList);
        List<String> deviceBaseIdList = aggregatorEntDeviceList.stream().map(AggregatorEntDevice::getDeviceBaseId).collect(toList());
        //查询基线负荷
        Map<String, Map<String, Double>> baseLinePowerMap = getBaseLinePowerMap(deviceBaseIdList, date);
        //保存设备功率计算
        List<AggregatorDeviceDateProfit> profitList = Lists.newArrayList();
        Map<String, Map<String, Double>> finalDeliveryPowerMap = deliveryPowerMap;
        Map<String, Map<String, Double>> finalIssuePowerMap = issuePowerMap;
        String finalDate = date;
        aggregatorEntDeviceList.forEach(device -> {
            AggregatorDeviceDateProfit profit = new AggregatorDeviceDateProfit();
            profit.setAggregatorId(device.getAggregatorId());
            profit.setEntId(device.getEntId());
            profit.setResourceTypeId(device.getResourceTypeId());
            profit.setDeviceBaseId(device.getDeviceBaseId());
            profit.setDate(finalDate);
            List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = Lists.newArrayList();
            for (int i = 0; i < minuteListWith15.size() - 1; i++) {
                String minute = minuteListWith15.get(i);
                String minuteNext = minuteListWith15.get(i + 1);
                AggregatorDeviceDateProfitResp aggregatorDeviceDateProfitResp = new AggregatorDeviceDateProfitResp();
                aggregatorDeviceDateProfitResp.setAggregatorId(device.getAggregatorId());
                aggregatorDeviceDateProfitResp.setEntId(device.getEntId());
                aggregatorDeviceDateProfitResp.setResourceTypeId(device.getResourceTypeId());
                aggregatorDeviceDateProfitResp.setDeviceBaseId(device.getDeviceBaseId());
                aggregatorDeviceDateProfitResp.setDate(finalDate);
                aggregatorDeviceDateProfitResp.setStartTime(minute);
                aggregatorDeviceDateProfitResp.setEndTime(minuteNext);
                Map<String, Double> deliveryTimeMap = finalDeliveryPowerMap.get(device.getDeviceBaseId());
                if (null != deliveryTimeMap && deliveryTimeMap.size() > 0) {
                    aggregatorDeviceDateProfitResp.setDeliveryPower(deliveryTimeMap.get(minuteNext));
                }
                Map<String, Double> realTimeMap = realTimeAvgPowerMap.get(device.getDeviceBaseId());
                if (null != realTimeMap && realTimeMap.size() > 0) {
                    aggregatorDeviceDateProfitResp.setReallyPower(realTimeMap.get(minuteNext));
                }
                Map<String, Double> issueTimeMap = finalIssuePowerMap.get(device.getDeviceBaseId());
                if (null != issueTimeMap && issueTimeMap.size() > 0) {
                    aggregatorDeviceDateProfitResp.setIssuePower(issueTimeMap.get(minuteNext));
                }
//                if (null != aggregatorDeviceDateProfitResp.getDeliveryPower() && null != aggregatorDeviceDateProfitResp.getReallyPower() && null != aggregatorDeviceDateProfitResp.getIssuePower()) {
//                    aggregatorDeviceDateProfitResp.setMinPower(aggregatorDeviceDateProfitResp.getDeliveryPower());
                aggregatorDeviceDateProfitResp.setMinPower(MathUtils.compareReturnMinABS(aggregatorDeviceDateProfitResp.getReallyPower(), aggregatorDeviceDateProfitResp.getMinPower()));
//                    aggregatorDeviceDateProfitResp.setMinPower(MathUtils.compareReturnMinABS(aggregatorDeviceDateProfitResp.getIssuePower(), aggregatorDeviceDateProfitResp.getMinPower()));
//                }
                Map<String, Double> baseLineTimeMap = baseLinePowerMap.get(device.getDeviceBaseId());
                if (null != baseLineTimeMap && baseLineTimeMap.size() > 0) {
                    aggregatorDeviceDateProfitResp.setBaseLinePower(baseLineTimeMap.get(DateUtils.format(minuteNext, "HH:mm")));
                }
                Double subDouble = null;
                Double estimateSubDouble = null;
                if (device.getResourceTypeId().equals("27")) {
                    if (null != aggregatorDeviceDateProfitResp.getMinPower() && aggregatorDeviceDateProfitResp.getMinPower() < 0) {
                        if (null == aggregatorDeviceDateProfitResp.getBaseLinePower() || aggregatorDeviceDateProfitResp.getBaseLinePower() < 0) {
                            subDouble = MathUtils.subDoubleABS(aggregatorDeviceDateProfitResp.getMinPower(), aggregatorDeviceDateProfitResp.getBaseLinePower());
                            if (null == subDouble || subDouble.compareTo(0D) < 0) {
                                subDouble = 0D;
                            }
                        } else {
                            subDouble = MathUtils.subDoubleABS(aggregatorDeviceDateProfitResp.getMinPower(), 0D);
                        }
                    } else {
                        subDouble = 0D;
                    }
                    if (null != aggregatorDeviceDateProfitResp.getDeliveryPower() && aggregatorDeviceDateProfitResp.getDeliveryPower() < 0) {
                        if (null == aggregatorDeviceDateProfitResp.getBaseLinePower() && aggregatorDeviceDateProfitResp.getBaseLinePower() < 0) {
                            estimateSubDouble = MathUtils.subDoubleABS(aggregatorDeviceDateProfitResp.getDeliveryPower(), aggregatorDeviceDateProfitResp.getBaseLinePower());
                            if (null == estimateSubDouble || estimateSubDouble.compareTo(0D) < 0) {
                                estimateSubDouble = 0D;
                            }
                        } else {
                            estimateSubDouble = MathUtils.subDoubleABS(aggregatorDeviceDateProfitResp.getDeliveryPower(), 0D);
                        }
                    } else {
                        estimateSubDouble = 0D;
                    }
                } else {
                    subDouble = MathUtils.subDoubleABS(aggregatorDeviceDateProfitResp.getMinPower(), aggregatorDeviceDateProfitResp.getBaseLinePower());
                    if (null == subDouble || subDouble.compareTo(0D) < 0) {
                        subDouble = 0D;
                    }
                    estimateSubDouble = MathUtils.subDoubleABS(aggregatorDeviceDateProfitResp.getDeliveryPower(), aggregatorDeviceDateProfitResp.getBaseLinePower());
                    if (null == estimateSubDouble || estimateSubDouble.compareTo(0D) < 0) {
                        estimateSubDouble = 0D;
                    }
                }
                aggregatorDeviceDateProfitResp.setCountPower(MathUtils.doublePoint(subDouble, 8));
                aggregatorDeviceDateProfitResp.setEstimatePower(MathUtils.doublePoint(estimateSubDouble, 8));
                //1分钟功率 乘以 15 分钟 除以 60 分钟
                aggregatorDeviceDateProfitResp.setCountElectricQuantity(MathUtils.mulDoubleNull(aggregatorDeviceDateProfitResp.getCountPower(), 0.25D, 8));
                aggregatorDeviceDateProfitResp.setEstimateElectricQuantity(MathUtils.mulDoubleNull(aggregatorDeviceDateProfitResp.getEstimatePower(), 0.25D, 8));
                aggregatorDeviceDateProfitResp.setCountPrice(aggregatorPriceMap.get(device.getAggregatorId()));
                aggregatorDeviceDateProfitRespList.add(aggregatorDeviceDateProfitResp);
            }
            String profitDetail = JSONObject.toJSONString(aggregatorDeviceDateProfitRespList);
            profit.setProfitDetail(profitDetail);
            profit.setProfitDetailByte(GZIPUtil.compressString(profit.getProfitDetail()));
            profitList.add(profit);
        });
        if (null != profitList && profitList.size() > 0) {
            aggregatorDeviceDateProfitService.save(deviceBaseIdList, date, profitList);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handAggregatorIssueProfit(AggregatorIssueProfitReq req) {
        if (StringUtils.isEmpty(req.getDate())) {
            req.setDate(DateUtils.getLastDay());
        }
        List<DataResp> profitList = req.getProfitList();
        Map<String, Double> timeProfitMap = profitList.stream().collect(toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1));
        List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList = aggregatorDeviceDateProfitService.getAggregatorDeviceDateProfitList(req.getAggregatorId(), req.getDate());
        if (null == aggregatorDeviceDateProfitList || aggregatorDeviceDateProfitList.size() <= 0) {
            return true;
        }
        //每个时间段总功率
        List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = Lists.newArrayList();
        aggregatorDeviceDateProfitList.forEach(aggregatorDeviceDateProfit -> {
            if (StringUtils.isNotEmpty(aggregatorDeviceDateProfit.getProfitDetail())) {
                List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitResps = JSONArray.parseArray(aggregatorDeviceDateProfit.getProfitDetail(), AggregatorDeviceDateProfitResp.class);
                if (null != aggregatorDeviceDateProfitResps && aggregatorDeviceDateProfitResps.size() > 0) {
                    aggregatorDeviceDateProfitRespList.addAll(aggregatorDeviceDateProfitResps);
                }
            }
        });
        Map<String, Double> totalTimePower = aggregatorDeviceDateProfitRespList.stream().collect(toMap(AggregatorDeviceDateProfitResp::getEndTime, AggregatorDeviceDateProfitResp::getCountPower, (v1, v2) -> v1 + v2));
        //写入设备收益
        aggregatorDeviceDateProfitRespList.forEach(deviceProfit -> {
            Double totalPower = totalTimePower.get(deviceProfit.getEndTime());
            Double totalProfit = timeProfitMap.get(deviceProfit.getEndTime());
            Double powerPercent = MathUtils.divideNull(deviceProfit.getCountPower(), totalPower, 8);
            deviceProfit.setPowerPercent(powerPercent);
            Double profit = MathUtils.mulDoubleNull(totalProfit, powerPercent, 8);
            deviceProfit.setProfit(profit);
        });
        Map<String, List<AggregatorDeviceDateProfitResp>> deviceMap = aggregatorDeviceDateProfitRespList.stream().collect(groupingBy(AggregatorDeviceDateProfitResp::getDeviceBaseId));
        aggregatorDeviceDateProfitList.forEach(profit -> {
            List<AggregatorDeviceDateProfitResp> deviceDateProfitRespList = deviceMap.get(profit.getDeviceBaseId());
            String profitDetail = JSONObject.toJSONString(deviceDateProfitRespList);
            profit.setProfitDetail(profitDetail);
            profit.setProfitDetailByte(GZIPUtil.compressString(profit.getProfitDetail()));
        });
        aggregatorDeviceDateProfitService.saveByAggregatorId(req.getAggregatorId(), req.getDate(), aggregatorDeviceDateProfitList);
        //写入企业收益
        List<AggregatorEntDateProfit> aggregatorEntDateProfitList = Lists.newArrayList();
        List<String> entIdList = aggregatorDeviceDateProfitList.stream().map(AggregatorDeviceDateProfit::getEntId).collect(toList());
        List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList(entIdList);
        Map<String, Double> entIdProfitPercentMap = aggregatorEntList.stream().collect(toMap(AggregatorEnt::getEntId, AggregatorEnt::getPercent));
        Map<String, Double> entIdCountPriceMap = aggregatorDeviceDateProfitRespList.stream().filter(profit -> null != profit.getElectricQuantity()).collect(toMap(AggregatorDeviceDateProfitResp::getEntId, AggregatorDeviceDateProfitResp::getCountPrice, (v1, v2) -> v1));
        Map<String, Double> entIdElectricQuantityMap = aggregatorDeviceDateProfitRespList.stream().filter(profit -> null != profit.getElectricQuantity()).collect(groupingBy(AggregatorDeviceDateProfitResp::getEntId, summingDouble(AggregatorDeviceDateProfitResp::getElectricQuantity)));
        Map<String, Double> entIdDateProfitMap = aggregatorDeviceDateProfitRespList.stream().filter(profit -> null != profit.getProfit()).collect(groupingBy(AggregatorDeviceDateProfitResp::getEntId, summingDouble(AggregatorDeviceDateProfitResp::getProfit)));
        entIdDateProfitMap.entrySet().forEach(entIdDateProfitMapEntry -> {
            AggregatorEntDateProfit aggregatorEntDateProfit = new AggregatorEntDateProfit();
            aggregatorEntDateProfit.setAggregatorId(req.getAggregatorId());
            aggregatorEntDateProfit.setEntId(entIdDateProfitMapEntry.getKey());
            aggregatorEntDateProfit.setDate(req.getDate());
            Double entProfitPercent = entIdProfitPercentMap.get(entIdDateProfitMapEntry.getKey());
            Double entProfit = MathUtils.mulDoubleNull(entIdDateProfitMapEntry.getValue(), entProfitPercent, 8);
            aggregatorEntDateProfit.setEntProfit(entProfit);
            aggregatorEntDateProfit.setElectricQuantity(entIdElectricQuantityMap.get(entIdDateProfitMapEntry.getKey()));
            aggregatorEntDateProfit.setCountPrice(entIdCountPriceMap.get(entIdDateProfitMapEntry.getKey()));
            aggregatorEntDateProfit.setCountProfit(entIdDateProfitMapEntry.getValue());
            aggregatorEntDateProfitList.add(aggregatorEntDateProfit);
        });
        aggregatorEntDateProfitService.saveByAggregatorId(req.getAggregatorId(), req.getDate(), aggregatorEntDateProfitList);
        //写入聚合商收益
        Double totalIssueProfit = profitList.stream().collect(summingDouble(DataResp::getValue));
        Double totalEntProfit = aggregatorEntDateProfitList.stream().collect(summingDouble(AggregatorEntDateProfit::getEntProfit));
        AggregatorDateProfit aggregatorDateProfit = new AggregatorDateProfit();
        aggregatorDateProfit.setAggregatorId(req.getAggregatorId());
        aggregatorDateProfit.setDate(req.getDate());
        aggregatorDateProfit.setIssueProfit(totalIssueProfit);
        aggregatorDateProfit.setAggregatorProfit(MathUtils.subDouble(totalIssueProfit, totalEntProfit));
        aggregatorDateProfit.setEntProfit(totalEntProfit);
        aggregatorDateProfitService.save(req.getAggregatorId(), req.getDate(), Arrays.asList(aggregatorDateProfit));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAutoApplyPlan(String day) {
        if (StringUtils.isEmpty(day)) {
            day = DateUtils.getNextDay();
        }
        Boolean applyDateCheck = aggregatorDateHolidayService.getApplyDateCheck(DateUtils.getAddDate(day, -1));
        if (applyDateCheck) {
            log.info("非工作日跳过企业自动申报");
            return true;
        }
        String now = DateUtils.getTime();
        String finalDay = day;
        executor.execute(() -> {
            List<String> dateList = aggregatorDateHolidayService.getApplyDateList(DateUtils.getAddDate(finalDay, -1), false);
            List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList();
            dateList.forEach(date -> {
                aggregatorEntList.forEach(ent -> {
                    AggregatorEntDateApplyDetail aggregatorEntDateApplyDetail = aggregatorEntDateApplyDetailService.getAggregatorEntDateApplyDetail(ent.getEntId(), date);
                    if (null == aggregatorEntDateApplyDetail) {
                        AggregatorEntApplyPlanResp applyPlan = aggregatorEntApplyPlanService.getApplyPlanV1(ent.getEntId(), false, date, true);
                        if (null != applyPlan) {
                            //修改企业申报详情
                            saveAggregatorEntDateApplyDetail(ent, date, now, "1");
                            //设备申报功率
                            saveAggregatorDeviceDateDeliveryChart(applyPlan, date, ent);
                            //保存设备启停计划
                            saveDevicePlan(applyPlan, date);
                        }
                    }
                });
            });
            //聚合商申报功率
            saveAggregatorDateDeliveryChart(dateList);
            List<String> aggregatorIdList = aggregatorEntList.stream().map(AggregatorEnt::getAggregatorId).distinct().collect(toList());
            //推送刷新
            try {
                //企业用户推送
                List<String> codeListByApp = AggregatorRefreshEnum.getCodeByType("app");
                codeListByApp.forEach(code -> {
                    aggregatorEntList.forEach(ent -> {
                        SendMessageReq req = new SendMessageReq();
                        req.setContent(code);
                        req.setEntId(ent.getEntId());
                        log.info("企业用户自动提交计划推送消息,{}", JSONObject.toJSONString(req));
                        pushService.sendSocket(req);
                    });
                });
            } catch (Exception e) {
                log.info("企业用户自动提交推送消息失败");
            }
            try {
                //聚合商推送
                List<String> codeListByPc = AggregatorRefreshEnum.getCodeByType("pc");
                codeListByPc.forEach(code -> {
                    aggregatorIdList.forEach(aggregatorId -> {
                        SendMessageReq req = new SendMessageReq();
                        req.setContent(code);
                        req.setEntId(aggregatorId);
                        log.info("聚合商自动提交计划推送消息,{}", JSONObject.toJSONString(req));
                        pushService.sendSocket(req);
                    });
                });
            } catch (Exception e) {
                log.info("聚合商自动提交计划推送消息失败");
            }
            //申报功率比例
            saveAggregatorDeviceDeliveryPowerPercent(dateList);
        });
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAggregatorEntDateApplyDetail(List<AggregatorEnt> aggregatorEntList, List<String> dateList, String now) {
        List<String> entIdList = aggregatorEntList.stream().map(AggregatorEnt::getEntId).collect(toList());
        List<AggregatorEntDateApplyDetail> aggregatorEntDateApplyDetailList = aggregatorEntDateApplyDetailService.getAggregatorEntDateApplyDetailList(entIdList, dateList);
        Map<String, Map<String, List<AggregatorEntDateApplyDetail>>> dateEntMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(aggregatorEntDateApplyDetailList)) {
            Map<String, Map<String, List<AggregatorEntDateApplyDetail>>> dateEntMapDeal = aggregatorEntDateApplyDetailList.stream().collect(groupingBy(AggregatorEntDateApplyDetail::getDate, groupingBy(AggregatorEntDateApplyDetail::getEntId)));
            if (null != dateEntMapDeal) {
                dateEntMap.putAll(dateEntMapDeal);
            }
        }
        List<AggregatorEntDateApplyDetail> addAggregatorEntDateApplyDetailList = Lists.newArrayList();
        dateList.forEach(date -> {
            Map<String, List<AggregatorEntDateApplyDetail>> entMap = new HashMap<>();
            Map<String, List<AggregatorEntDateApplyDetail>> entMapDeal = dateEntMap.get(date);
            if (null != entMapDeal) {
                entMap.putAll(entMapDeal);
            }
            aggregatorEntList.forEach(ent -> {
                List<AggregatorEntDateApplyDetail> applyDetailList = entMap.get(ent.getEntId());
                if (CollectionUtils.isEmpty(applyDetailList)) {
                    AggregatorEntDateApplyDetail aggregatorEntDateApplyDetail = new AggregatorEntDateApplyDetail();
                    aggregatorEntDateApplyDetail.setAggregatorId(ent.getAggregatorId());
                    aggregatorEntDateApplyDetail.setDate(date);
                    aggregatorEntDateApplyDetail.setEntId(ent.getEntId());
                    aggregatorEntDateApplyDetail.setApplyDate(DateUtils.getDay(now));
                    aggregatorEntDateApplyDetail.setApplyTime(now);
                    aggregatorEntDateApplyDetail.setPlanStatus(false);
                    addAggregatorEntDateApplyDetailList.add(aggregatorEntDateApplyDetail);
                }
            });
        });
        if (CollectionUtils.isNotEmpty(addAggregatorEntDateApplyDetailList)) {
            aggregatorEntDateApplyDetailService.batchInsert(addAggregatorEntDateApplyDetailList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAggregatorEntDateApplyDetail(AggregatorEnt aggregatorEnt, String date, String now, String applyStatus) {
        AggregatorEntDateApplyDetail aggregatorEntDateApplyDetail = aggregatorEntDateApplyDetailService.getAggregatorEntDateApplyDetail(aggregatorEnt.getEntId(), date);
        if (null == aggregatorEntDateApplyDetail) {
            AggregatorEntDateApplyDetail addAggregatorEntDateApplyDetail = new AggregatorEntDateApplyDetail();
            addAggregatorEntDateApplyDetail.setAggregatorId(aggregatorEnt.getAggregatorId());
            addAggregatorEntDateApplyDetail.setDate(date);
            addAggregatorEntDateApplyDetail.setEntId(aggregatorEnt.getEntId());
            addAggregatorEntDateApplyDetail.setStationId(aggregatorEnt.getStationId());
            addAggregatorEntDateApplyDetail.setApplyDate(DateUtils.getDay(now));
            addAggregatorEntDateApplyDetail.setApplyTime(now);
            addAggregatorEntDateApplyDetail.setPlanStatus(false);
            addAggregatorEntDateApplyDetail.setApplyStatus(applyStatus);
            aggregatorEntDateApplyDetailService.batchInsert(Arrays.asList(addAggregatorEntDateApplyDetail));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAggregatorDeviceDateDeliveryChart(AggregatorEntApplyPlanResp applyPlan, String date, AggregatorEnt ent) {
        List<AppApplyIndexDeviceDetailResp> appApplyIndexDeviceDetailRespList = applyPlan.getAppApplyIndexDeviceDetailRespList();
        if (CollectionUtils.isNotEmpty(appApplyIndexDeviceDetailRespList)) {
            List<AggregatorDeviceDateDeliveryChart> aggregatorDeviceDateDeliveryChartList = Lists.newArrayList();
            appApplyIndexDeviceDetailRespList.forEach(appApplyIndexDeviceDetailResp -> {
                AggregatorDeviceDateDeliveryChart aggregatorDeviceDateDeliveryChart = new AggregatorDeviceDateDeliveryChart();
                aggregatorDeviceDateDeliveryChart.setAggregatorId(applyPlan.getAggregatorId());
                aggregatorDeviceDateDeliveryChart.setEntId(applyPlan.getEntId());
                aggregatorDeviceDateDeliveryChart.setStationId(ent.getStationId());
                aggregatorDeviceDateDeliveryChart.setResourceTypeId(appApplyIndexDeviceDetailResp.getResourceTypeId());
                aggregatorDeviceDateDeliveryChart.setDeviceBaseId(appApplyIndexDeviceDetailResp.getDeviceBaseId());
                aggregatorDeviceDateDeliveryChart.setDate(date);
                List<AppApplyIndexDeviceTimeDetailResp> timeList = appApplyIndexDeviceDetailResp.getTimeList();
                if (CollectionUtils.isNotEmpty(timeList)) {
                    List<DataResp> deliveryChartList = Lists.newArrayList();
                    timeList.forEach(detail -> {
                        String startTime = date + " " + detail.getStartTime() + ":00";
                        String endTime = date + " " + detail.getEndTime() + ":00";
                        if (detail.getEndTime().equals("24:00")) {
                            endTime = DateUtils.getAddDate(date) + " 00:00:00";
                        }
                        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime, 15);
                        for (int i = 0; i < minuteList.size() - 1; i++) {
                            String minute = minuteList.get(i + 1);
                            DataResp dataResp = new DataResp();
                            dataResp.setTime(minute);
                            dataResp.setValue(null == detail.getPower() ? 0D : detail.getPower());
                            if (appApplyIndexDeviceDetailResp.getResourceTypeId().equals("27") && null != detail.getUseStatus() && detail.getUseStatus() == -1) {
                                dataResp.setValue(0 - dataResp.getValue());
                            }
                            deliveryChartList.add(dataResp);
                        }
                    });
                    String deliveryChart = JSONArray.toJSONString(deliveryChartList);
                    aggregatorDeviceDateDeliveryChart.setDeliveryChart(deliveryChart);
                }
                aggregatorDeviceDateDeliveryChartList.add(aggregatorDeviceDateDeliveryChart);
            });
            aggregatorDeviceDateDeliveryChartService.delete(applyPlan.getEntId(), Arrays.asList(date));
            if (CollectionUtils.isNotEmpty(aggregatorDeviceDateDeliveryChartList)) {
                aggregatorDeviceDateDeliveryChartService.batchInsert(aggregatorDeviceDateDeliveryChartList);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAggregatorDateDeliveryChart(List<String> dateList) {
        List<AggregatorInfo> aggregatorList = aggregatorInfoService.getAggregatorInfoList();
        aggregatorList.forEach(aggregatorInfo -> {
            List<AggregatorDateDeliveryChart> aggregatorDateDeliveryChartList = Lists.newArrayList();
            List<AggregatorDeviceDateDeliveryChart> deviceDateDeliveryChartList = aggregatorDeviceDateDeliveryChartService.getAggregatorDeviceDateDeliveryChartList(aggregatorInfo.getAggregatorId(), dateList);
            if (CollectionUtils.isNotEmpty(deviceDateDeliveryChartList)) {
                Map<String, Map<String, List<AggregatorDeviceDateDeliveryChart>>> dateMap = deviceDateDeliveryChartList.stream().collect(Collectors.groupingBy(AggregatorDeviceDateDeliveryChart::getDate, Collectors.groupingBy(AggregatorDeviceDateDeliveryChart::getResourceTypeId)));
                for (Map.Entry<String, Map<String, List<AggregatorDeviceDateDeliveryChart>>> dateMapEntry : dateMap.entrySet()) {
                    Map<String, List<AggregatorDeviceDateDeliveryChart>> resourceMap = dateMapEntry.getValue();
                    if (null != resourceMap && resourceMap.size() > 0) {
                        for (Map.Entry<String, List<AggregatorDeviceDateDeliveryChart>> resourceMapEntry : resourceMap.entrySet()) {
                            List<AggregatorDeviceDateDeliveryChart> deliveryChartList = resourceMapEntry.getValue();
                            if (CollectionUtils.isNotEmpty(deliveryChartList)) {
                                List<DataResp> dataRespList = Lists.newArrayList();
                                deliveryChartList.stream().filter(aggregatorDeviceDateDeliveryChart -> null != aggregatorDeviceDateDeliveryChart && StringUtils.isNotEmpty(aggregatorDeviceDateDeliveryChart.getDeliveryChart())).forEach(aggregatorDeviceDateDeliveryChart -> {
                                    String deliveryChart = aggregatorDeviceDateDeliveryChart.getDeliveryChart();
                                    List<DataResp> dataList = JSONArray.parseArray(deliveryChart, DataResp.class);
                                    if (CollectionUtils.isNotEmpty(dataList)) {
                                        dataRespList.addAll(dataList);
                                    }
                                });
                                if (CollectionUtils.isNotEmpty(dataRespList)) {
                                    Map<String, Double> timeValueMap = dataRespList.stream().collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1 + v2));
                                    List<DataResp> chartList = Lists.newArrayList();
                                    for (Map.Entry<String, Double> timeValueMapEntry : timeValueMap.entrySet()) {
                                        DataResp dataResp = new DataResp();
                                        dataResp.setTime(timeValueMapEntry.getKey());
                                        dataResp.setValue(null == timeValueMapEntry.getValue() ? null : MathUtils.doublePoint(timeValueMapEntry.getValue(), 8));
                                        chartList.add(dataResp);
                                    }
                                    AggregatorDateDeliveryChart aggregatorDateDeliveryChart = new AggregatorDateDeliveryChart();
                                    aggregatorDateDeliveryChart.setAggregatorId(aggregatorInfo.getAggregatorId());
                                    aggregatorDateDeliveryChart.setResourceTypeId(resourceMapEntry.getKey());
                                    aggregatorDateDeliveryChart.setDate(dateMapEntry.getKey());
                                    if (CollectionUtils.isNotEmpty(chartList)) {
                                        chartList = chartList.stream().sorted(Comparator.comparing(DataResp::getTime)).collect(toList());
                                    }
                                    String chart = JSONObject.toJSONString(chartList);
                                    aggregatorDateDeliveryChart.setDeliveryChart(chart);
                                    aggregatorDateDeliveryChartList.add(aggregatorDateDeliveryChart);
                                }
                            }
                        }
                    }
                }
            }
            aggregatorDateDeliveryChartService.delete(aggregatorInfo.getAggregatorId(), dateList);
            if (CollectionUtils.isNotEmpty(aggregatorDateDeliveryChartList)) {
                aggregatorDateDeliveryChartService.batchInsert(aggregatorDateDeliveryChartList);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDevicePlan(AggregatorEntApplyPlanResp applyPlan, String date) {
        List<AggregatorEntDateDeviceStartStopPlan> aggregatorEntDateDeviceStartStopPlanList = Lists.newArrayList();
        List<AppApplyIndexDeviceDetailResp> appApplyIndexDeviceDetailRespList = applyPlan.getAppApplyIndexDeviceDetailRespList();
        if (CollectionUtils.isNotEmpty(appApplyIndexDeviceDetailRespList)) {
            AggregatorEntDateDeviceStartStopPlan aggregatorEntDateDeviceStartStopPlan = new AggregatorEntDateDeviceStartStopPlan();
            aggregatorEntDateDeviceStartStopPlan.setAggregatorId(applyPlan.getAggregatorId());
            aggregatorEntDateDeviceStartStopPlan.setEntId(applyPlan.getEntId());
            aggregatorEntDateDeviceStartStopPlan.setDate(date);
            List<AggregatorEntDateDeviceStartStopPlanDetailResp> aggregatorEntDateDeviceStartStopPlanDetailRespList = Lists.newArrayList();
            appApplyIndexDeviceDetailRespList.forEach(appApplyIndexDeviceDetailResp -> {
                List<AppApplyIndexDeviceTimeDetailResp> timeList = appApplyIndexDeviceDetailResp.getTimeList();
                if (CollectionUtils.isNotEmpty(timeList)) {
                    timeList.forEach(detail -> {
                        AggregatorEntDateDeviceStartStopPlanDetailResp aggregatorEntDateDeviceStartStopPlanDetailResp = new AggregatorEntDateDeviceStartStopPlanDetailResp();
                        aggregatorEntDateDeviceStartStopPlanDetailResp.setResourceTypeId(appApplyIndexDeviceDetailResp.getResourceTypeId());
                        aggregatorEntDateDeviceStartStopPlanDetailResp.setDeviceBaseId(appApplyIndexDeviceDetailResp.getDeviceBaseId());
                        aggregatorEntDateDeviceStartStopPlanDetailResp.setTime(detail.getStartTime());
                        Double power = detail.getPower();
                        if (null != detail.getUseStatus() && appApplyIndexDeviceDetailResp.getResourceTypeId().equals("27")) {
                            if (detail.getUseStatus() == 1) {
                                aggregatorEntDateDeviceStartStopPlanDetailResp.setDetail("放电功率" + power + "kW");
                            } else if (detail.getUseStatus() == 0) {
                                aggregatorEntDateDeviceStartStopPlanDetailResp.setDetail("不充不放");
                            } else if (detail.getUseStatus() == -1) {
                                aggregatorEntDateDeviceStartStopPlanDetailResp.setDetail("充电功率" + power + "kW");
                            }
                        } else if (appApplyIndexDeviceDetailResp.getResourceTypeId().equals("26")) {
                            if (null != power && power == 0) {
                                aggregatorEntDateDeviceStartStopPlanDetailResp.setDetail("在非蓄热状态");
                            } else {
                                aggregatorEntDateDeviceStartStopPlanDetailResp.setDetail("用电功率" + power + "kW");
                            }
                        } else {
                            if (null != power && power != 0) {
                                aggregatorEntDateDeviceStartStopPlanDetailResp.setDetail("启动");
                            } else {
                                aggregatorEntDateDeviceStartStopPlanDetailResp.setDetail("停止");
                            }
                        }
                        aggregatorEntDateDeviceStartStopPlanDetailRespList.add(aggregatorEntDateDeviceStartStopPlanDetailResp);
                    });
                }
            });
            if (CollectionUtils.isNotEmpty(aggregatorEntDateDeviceStartStopPlanDetailRespList)) {
                String detail = JSONObject.toJSONString(aggregatorEntDateDeviceStartStopPlanDetailRespList);
                aggregatorEntDateDeviceStartStopPlan.setDetail(detail);
            }
            aggregatorEntDateDeviceStartStopPlanList.add(aggregatorEntDateDeviceStartStopPlan);
        }
        aggregatorEntDateDeviceStartStopPlanService.delete(applyPlan.getEntId(), Arrays.asList(date));
        if (CollectionUtils.isNotEmpty(aggregatorEntDateDeviceStartStopPlanList)) {
            aggregatorEntDateDeviceStartStopPlanService.batchInsert(aggregatorEntDateDeviceStartStopPlanList);
        }
    }

    @Override
    public void saveAggregatorDeviceDeliveryPowerPercent(List<String> dateList) {
        List<AggregatorDeviceDeliveryPowerPercent> aggregatorDeviceDeliveryPowerPercentList = Lists.newArrayList();
        List<AggregatorDeviceDeliveryPowerPercentDetail> aggregatorDeviceDeliveryPowerPercentDetailList = Lists.newArrayList();
        List<AggregatorInfo> aggregatorInfoList = aggregatorInfoService.getAggregatorInfoList();
        aggregatorInfoList.forEach(aggregatorInfo -> {
            List<AggregatorDeviceDateDeliveryChart> aggregatorDeviceDateDeliveryChartList = aggregatorDeviceDateDeliveryChartService.getAggregatorDeviceDateDeliveryChartList(aggregatorInfo.getAggregatorId(), dateList);
            if (CollectionUtils.isNotEmpty(aggregatorDeviceDateDeliveryChartList)) {
                List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(aggregatorDeviceDateDeliveryChartList.stream().map(AggregatorDeviceDateDeliveryChart::getDeviceBaseId).collect(toList()));
                if (CollectionUtils.isNotEmpty(aggregatorEntDeviceList)) {
                    Map<String, AggregatorEntDevice> deviceBaseIdMap = aggregatorEntDeviceList.stream().collect(toMap(AggregatorEntDevice::getDeviceBaseId, Function.identity()));
                    Map<String, Map<String, List<AggregatorDeviceDateDeliveryChart>>> dateMap = aggregatorDeviceDateDeliveryChartList.stream().collect(Collectors.groupingBy(AggregatorDeviceDateDeliveryChart::getDate, Collectors.groupingBy(AggregatorDeviceDateDeliveryChart::getResourceTypeId)));
                    for (Map.Entry<String, Map<String, List<AggregatorDeviceDateDeliveryChart>>> dateMapEntry : dateMap.entrySet()) {
                        Map<String, List<AggregatorDeviceDateDeliveryChart>> resourceMap = dateMapEntry.getValue();
                        if (null != resourceMap && resourceMap.size() > 0) {
                            for (Map.Entry<String, List<AggregatorDeviceDateDeliveryChart>> resourceMapEntry : resourceMap.entrySet()) {
                                List<AggregatorDeviceDateDeliveryChart> deliveryChartList = resourceMapEntry.getValue();
                                if (CollectionUtils.isNotEmpty(deliveryChartList)) {
                                    deliveryChartList.stream().filter(aggregatorDeviceDateDeliveryChart -> null != aggregatorDeviceDateDeliveryChart && StringUtils.isNotEmpty(aggregatorDeviceDateDeliveryChart.getDeliveryChart())).forEach(aggregatorDeviceDateDeliveryChart -> {
                                        AggregatorEntDevice aggregatorEntDevice = deviceBaseIdMap.get(aggregatorDeviceDateDeliveryChart.getDeviceBaseId());
                                        if (null != aggregatorEntDevice) {
                                            String deliveryChart = aggregatorDeviceDateDeliveryChart.getDeliveryChart();
                                            List<DataResp> dataList = JSONArray.parseArray(deliveryChart, DataResp.class);
                                            if (CollectionUtils.isNotEmpty(dataList)) {
                                                dataList.forEach(dataResp -> {
                                                    AggregatorDeviceDeliveryPowerPercentDetail aggregatorDeviceDeliveryPowerPercentDetail = new AggregatorDeviceDeliveryPowerPercentDetail();
                                                    aggregatorDeviceDeliveryPowerPercentDetail.setAggregatorId(aggregatorEntDevice.getAggregatorId());
                                                    aggregatorDeviceDeliveryPowerPercentDetail.setResourceTypeId(aggregatorEntDevice.getResourceTypeId());
                                                    aggregatorDeviceDeliveryPowerPercentDetail.setEntId(aggregatorEntDevice.getEntId());
                                                    aggregatorDeviceDeliveryPowerPercentDetail.setStationId(aggregatorEntDevice.getStationId());
                                                    aggregatorDeviceDeliveryPowerPercentDetail.setDeviceBaseId(aggregatorEntDevice.getDeviceBaseId());
                                                    aggregatorDeviceDeliveryPowerPercentDetail.setDeviceId(aggregatorEntDevice.getDeviceId());
                                                    aggregatorDeviceDeliveryPowerPercentDetail.setDate(dateMapEntry.getKey());
                                                    aggregatorDeviceDeliveryPowerPercentDetail.setTime(dataResp.getTime());
                                                    aggregatorDeviceDeliveryPowerPercentDetail.setPower(aggregatorEntDevice.getPower());
                                                    aggregatorDeviceDeliveryPowerPercentDetail.setDeliveryPower(dataResp.getValue());
                                                    aggregatorDeviceDeliveryPowerPercentDetail.setIotDeviceBaseId(aggregatorEntDevice.getIotDeviceBaseId());
                                                    aggregatorDeviceDeliveryPowerPercentDetailList.add(aggregatorDeviceDeliveryPowerPercentDetail);
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }
        });
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDeliveryPowerPercentDetailList)) {
            Map<String, Map<String, List<AggregatorDeviceDeliveryPowerPercentDetail>>> resourceTypeMap = aggregatorDeviceDeliveryPowerPercentDetailList
                    .stream()
                    .collect(groupingBy(AggregatorDeviceDeliveryPowerPercentDetail::getResourceTypeId, groupingBy(AggregatorDeviceDeliveryPowerPercentDetail::getTime)));
            resourceTypeMap.entrySet().forEach(resourceTypeMapEntry -> {
                Map<String, List<AggregatorDeviceDeliveryPowerPercentDetail>> timeMap = resourceTypeMapEntry.getValue();
                if (null != timeMap && timeMap.size() > 0) {
                    timeMap.entrySet().stream().filter(timeMapEntry -> null != timeMapEntry && null != timeMapEntry.getValue() && timeMapEntry.getValue().size() > 0).forEach(timeMapEntry -> {
                        AggregatorDeviceDeliveryPowerPercent aggregatorDeviceDeliveryPowerPercent = new AggregatorDeviceDeliveryPowerPercent();
                        aggregatorDeviceDeliveryPowerPercent.setResourceTypeId(resourceTypeMapEntry.getKey());
                        aggregatorDeviceDeliveryPowerPercent.setDate(DateUtils.format(timeMapEntry.getKey(), "yyyy-MM-dd"));
                        if (DateUtils.format(timeMapEntry.getKey(), "HH:mm").equals("00:00")) {
                            aggregatorDeviceDeliveryPowerPercent.setDate(DateUtils.getAddDate(aggregatorDeviceDeliveryPowerPercent.getDate(), -1));
                        }
                        aggregatorDeviceDeliveryPowerPercent.setTime(timeMapEntry.getKey());
                        List<AggregatorDeviceDeliveryPowerPercentDetail> valueList = timeMapEntry.getValue();
                        if (CollectionUtils.isNotEmpty(valueList)) {
                            double totalDeliveryPower = valueList.stream().filter(detail -> null != detail && null != detail.getDeliveryPower()).mapToDouble(AggregatorDeviceDeliveryPowerPercentDetail::getDeliveryPower).sum();
                            valueList.stream().filter(detail -> null != detail && null != detail.getDeliveryPower()).forEach(detail -> {
                                detail.setPercent(MathUtils.divideNull(detail.getDeliveryPower(), totalDeliveryPower, 8));
                            });
                        }
                        aggregatorDeviceDeliveryPowerPercent.setDetail(JSONObject.toJSONString(valueList));
                        aggregatorDeviceDeliveryPowerPercent.setDetailByte(GZIPUtil.compressString(aggregatorDeviceDeliveryPowerPercent.getDetail()));
                        aggregatorDeviceDeliveryPowerPercentList.add(aggregatorDeviceDeliveryPowerPercent);
                    });
                }
            });
            aggregatorDeviceDeliveryPowerPercentService.delete(dateList);
            if (CollectionUtils.isNotEmpty(aggregatorDeviceDeliveryPowerPercentList)) {
                aggregatorDeviceDeliveryPowerPercentService.batchInsert(aggregatorDeviceDeliveryPowerPercentList);
            }
        }
    }

    @Override
    public void excelImportWithApply(MultipartFile file) {
//        executor.execute(() -> {
//            try {
//                ImportParams importParamsBySheet1 = new ImportParams();
//                importParamsBySheet1.setSheetNum(1);
//                List<DataRespExcelImportReq> dataRespListBySheet1 = ExcelImportUtil.importExcel(file.getInputStream(), DataRespExcelImportReq.class, importParamsBySheet1);
//                log.info("盒马1#储能侧电表导入申报数据：{}", JSONObject.toJSONString(dataRespListBySheet1));
//
//                ImportParams importParamsBySheet2 = new ImportParams();
//                importParamsBySheet2.setSheetNum(1);
//                List<DataRespExcelImportReq> dataRespListBySheet2 = ExcelImportUtil.importExcel(file.getInputStream(), DataRespExcelImportReq.class, importParamsBySheet2);
//                log.info("盒马2#储能侧电表导入申报数据：{}", JSONObject.toJSONString(dataRespListBySheet2));
//
//                ImportParams importParamsBySheet3 = new ImportParams();
//                importParamsBySheet3.setSheetNum(2);
//                List<DataRespExcelImportReq> dataRespListBySheet3 = ExcelImportUtil.importExcel(file.getInputStream(), DataRespExcelImportReq.class, importParamsBySheet3);
//                log.info("北京供暖1#锅炉电表导入申报数据：{}", JSONObject.toJSONString(dataRespListBySheet3));
//
//                ImportParams importParamsBySheet4 = new ImportParams();
//                importParamsBySheet4.setSheetNum(2);
//                List<DataRespExcelImportReq> dataRespListBySheet4 = ExcelImportUtil.importExcel(file.getInputStream(), DataRespExcelImportReq.class, importParamsBySheet4);
//                log.info("北京供暖2#锅炉电表导入申报数据：{}", JSONObject.toJSONString(dataRespListBySheet4));
//
//                String nextDay = DateUtils.getNextDay();
//                String aggregatorId = "1455435872428793857";
//                List<String> entIdList = Arrays.asList("1341556517236285442", "1344182026064609281");
//                List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getAggregatorEntDeviceListByEntIdList(entIdList);
//                AggregatorEntApplyPlanReq aggregatorEntApplyPlanReq = new AggregatorEntApplyPlanReq();
//                aggregatorEntApplyPlanReq.setAggregatorId(aggregatorId);
//                aggregatorEntApplyPlanReq.setEntId();
//                aggregatorEntApplyPlanReq.setStartDate(nextDay);
//                aggregatorEntApplyPlanReq.setEndDate(nextDay);
//                aggregatorEntApplyPlanReq.setPlanStatus(true);
//                aggregatorEntApplyPlanReq.setStatus(true);
//                aggregatorEntApplyPlanReq.setSaveStatus(true);
//                List<AppApplyIndexDeviceDetailResp> appApplyIndexDeviceDetailRespList = Lists.newArrayList();
//                aggregatorEntDeviceList.forEach(device -> {
//
//                });
//                aggregatorEntApplyPlanService.addApplyPlan(aggregatorEntApplyPlanReq);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
    }

    @Override
    public void addApplyPlan(String entId, Boolean planStatus, Boolean saveStatus, Double rate, Double dealPower, Boolean holidayFlag) {
        String nextDay = DateUtils.getNextDay();
        Boolean applyDateCheck = aggregatorDateHolidayService.getApplyDateCheck(DateUtils.getAddDate(nextDay, -1));
        if (applyDateCheck) {
            return;
        }
        AggregatorEnt aggregatorEnt = aggregatorEntService.getAggregatorEnt(entId);
        AggregatorEntApplyPlanReq aggregatorEntApplyPlanReq = new AggregatorEntApplyPlanReq();
        aggregatorEntApplyPlanReq.setAggregatorId(aggregatorEnt.getAggregatorId());
        aggregatorEntApplyPlanReq.setEntId(entId);
        aggregatorEntApplyPlanReq.setStartDate(nextDay);
        aggregatorEntApplyPlanReq.setEndDate(nextDay);
        aggregatorEntApplyPlanReq.setPlanStatus(planStatus);
        if (!planStatus) {
            aggregatorEntApplyPlanReq.setStartDate(DateUtils.getNextDay());
            aggregatorEntApplyPlanReq.setEndDate(aggregatorEnt.getServiceEndDate());
        }
        aggregatorEntApplyPlanReq.setStatus(true);
        aggregatorEntApplyPlanReq.setSaveStatus(saveStatus);
        List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(entId);
        Map<String, List<AppApplyIndexDeviceTimeDetailResp>> deviceBaseIdMap = getAppApplyIndexDeviceTimeDetailRespList(aggregatorEntDeviceList, nextDay, rate, dealPower);
        if (null == deviceBaseIdMap || deviceBaseIdMap.size() <= 0) {
            deviceBaseIdMap = new HashMap<>();
        }
        List<AppApplyIndexDeviceDetailResp> appApplyIndexDeviceDetailRespList = Lists.newArrayList();
        Map<String, List<AppApplyIndexDeviceTimeDetailResp>> finalDeviceBaseIdMap = deviceBaseIdMap;
        aggregatorEntDeviceList.forEach(device -> {
            AppApplyIndexDeviceDetailResp appApplyIndexDeviceDetailResp = new AppApplyIndexDeviceDetailResp();
            appApplyIndexDeviceDetailResp.setResourceTypeId(device.getResourceTypeId());
            appApplyIndexDeviceDetailResp.setDeviceBaseId(device.getDeviceBaseId());
            appApplyIndexDeviceDetailResp.setDeviceName(device.getDeviceName());
            appApplyIndexDeviceDetailResp.setDevicePower(device.getPower());
            List<AppApplyIndexDeviceTimeDetailResp> timeList = finalDeviceBaseIdMap.get(device.getDeviceBaseId());
            appApplyIndexDeviceDetailResp.setTimeList(timeList);
            appApplyIndexDeviceDetailRespList.add(appApplyIndexDeviceDetailResp);
        });
        aggregatorEntApplyPlanReq.setAppApplyIndexDeviceDetailRespList(appApplyIndexDeviceDetailRespList);
        List<String> applyDateList = aggregatorDateHolidayService.getApplyDateList(DateUtils.getAddDate(nextDay, -1), false);
        if (null != applyDateList && applyDateList.size() > 1) {
            aggregatorEntApplyPlanReq.setStartDate(applyDateList.get(0));
            aggregatorEntApplyPlanReq.setEndDate(applyDateList.get(applyDateList.size() - 1));
        }
        aggregatorEntApplyPlanService.addApplyPlan(aggregatorEntApplyPlanReq);
    }

    @Override
    public void saveDevicePercentBaseLoadSql(SaveDevicePercentBaseLoadSqlReq req) {
        if (null != req && CollectionUtils.isNotEmpty(req.getPowerList()) && CollectionUtils.isNotEmpty(req.getDevicePercentReqList())) {
            Map<String, Double> checkSumMap = new HashMap<>();
            for (int j = 0; j < req.getDevicePercentReqList().size(); j++) {
                List<DataResp> dataRespList = Lists.newArrayList();
                List<String> minuteList = DateUtils.getMinuteList(DateUtils.getDay() + " 00:15:00", DateUtils.getAddDate(DateUtils.getDay()) + " 00:00:00", 15);
                for (int i = 0; i < minuteList.size(); i++) {
                    DataResp dataResp = new DataResp();
                    dataResp.setTime(minuteList.get(i));
                    if (j != req.getDevicePercentReqList().size() - 1) {
                        dataResp.setValue(MathUtils.mulDoubleNullNotRounding(req.getPowerList().get(i), req.getDevicePercentReqList().get(j).getPercent(), 8));
                        Double checkSumMapValue = checkSumMap.get(dataResp.getTime());
                        if (null == checkSumMapValue) {
                            checkSumMapValue = 0D;
                        }
                        checkSumMapValue += dataResp.getValue();
                        checkSumMap.put(dataResp.getTime(), checkSumMapValue);
                    } else {
                        Double checkSumMapValue = checkSumMap.get(dataResp.getTime());
                        if (null == checkSumMapValue) {
                            checkSumMapValue = 0D;
                        }
                        dataResp.setValue(MathUtils.subDouble(req.getPowerList().get(i), checkSumMapValue, 8));
                    }
                    dataRespList.add(dataResp);
                }
                log.info("INSERT INTO aggregator_device_date_base_line_load_chart " +
                                "(id, aggregator_id, ent_id, station_id, device_base_id, base_line_load_chart, start_date, end_date) " +
                                "VALUES (1,'{}','{}','{}','{}','{}','{}','{}');",
                        req.getDevicePercentReqList().get(j).getAggregatorId(),
                        req.getDevicePercentReqList().get(j).getEntId(),
                        req.getDevicePercentReqList().get(j).getStationId(),
                        req.getDevicePercentReqList().get(j).getDeviceBaseId(),
                        JSONObject.toJSONString(dataRespList),
                        req.getDevicePercentReqList().get(j).getStartDate(),
                        req.getDevicePercentReqList().get(j).getEndDate());
            }
        }
    }

    @Override
    public Boolean addAggregatorAutoApplyPlan() {
        List<AggregatorInfo> aggregatorInfoList = aggregatorInfoService.getAggregatorInfoList();
        aggregatorInfoList.forEach(aggregator -> {
            AggregatorApplyReq req = new AggregatorApplyReq();
            req.setAggregatorId(aggregator.getAggregatorId());
            req.setApplyBy("系统");
            req.setApplyType("0");
            detailService.updateAggregatorApply(req);
        });
        return true;
    }

    @Override
    public Boolean autoApplyPlan(String s) {
        List<AggregatorInfo> aggregatorInfoList = aggregatorInfoService.getAggregatorInfoList();
        aggregatorInfoList.forEach(aggregator -> {
            AggregatorApplyReq req = new AggregatorApplyReq();
            req.setAggregatorId(aggregator.getAggregatorId());
            req.setApplyBy("系统");
            req.setApplyType("0");
            // modify by sl 2024-11-21 申报支持指定日期补招
            req.setDate(s);
            detailService.autoAggregatorApply(req);
        });
        return true;
    }

    /**
     * 查询设备功率
     *
     * @param deviceList
     * @param nextDay
     * @param rate
     * @return
     */
    private Map<String, List<AppApplyIndexDeviceTimeDetailResp>> getAppApplyIndexDeviceTimeDetailRespList(List<AggregatorEntDevice> deviceList, String nextDay, Double rate, Double dealPower) {
        Map<String, List<AppApplyIndexDeviceTimeDetailResp>> deviceBaseIdMap = new HashMap<>();
        Map<String, AggregatorEntDevice> deviceMap = deviceList.stream().collect(toMap(device -> device.getStationId() + "_" + device.getDeviceId().split("_")[1], Function.identity(), (v1, v2) -> v1));
        String startTime = DateUtils.getAddDate(nextDay, -4) + " 00:15:00";
        String endTime = DateUtils.getAddDate(nextDay, -1) + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(nextDay + " 00:15:00", DateUtils.getAddDate(nextDay, +1) + " 00:00:00", 15);
        List<BigDataHistoryResp> bigDataHistoryRespList = bigDataHistoryService.getBigData(deviceList, Arrays.asList(MetricEnum.YES_POWER.getCode()), startTime, endTime, "0");
        if (CollectionUtils.isNotEmpty(bigDataHistoryRespList)) {
            bigDataHistoryRespList.stream().filter(bigDataHistoryResp -> null != bigDataHistoryResp).forEach(bigDataHistoryResp -> {
                List<DataResp> dataRespList = bigDataHistoryResp.getDataResp();
                if (CollectionUtils.isNotEmpty(dataRespList)) {
                    AggregatorEntDevice aggregatorEntDevice = deviceMap.get(bigDataHistoryResp.getStaId() + "_" + bigDataHistoryResp.getEquipID());
                    if (null != aggregatorEntDevice) {
                        List<AppApplyIndexDeviceTimeDetailResp> appApplyIndexDeviceTimeDetailRespList = Lists.newArrayList();
                        Map<String, List<DataResp>> timeListMap = dataRespList.stream().filter(dataResp -> null != dataResp && StringUtils.isNotEmpty(dataResp.getTime())).collect(groupingBy(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm")));
                        minuteList.forEach(minute -> {
                            List<DataResp> dataRespListByValue = timeListMap.get(DateUtils.format(minute, "HH:mm"));
                            if (CollectionUtils.isNotEmpty(dataRespListByValue)) {
                                List<DataResp> timeListMapEntryDataRespList = dataRespListByValue.stream().filter(timeListMapEntryDataResp -> null != timeListMapEntryDataResp && null != timeListMapEntryDataResp.getValue()).collect(toList());
                                if (CollectionUtils.isNotEmpty(timeListMapEntryDataRespList)) {
                                    Double power = MathUtils.doublePoint(timeListMapEntryDataRespList.stream().mapToDouble(DataResp::getValue).sum() / timeListMapEntryDataRespList.size(), 8);
                                    AppApplyIndexDeviceTimeDetailResp appApplyIndexDeviceTimeDetailResp = new AppApplyIndexDeviceTimeDetailResp();
                                    appApplyIndexDeviceTimeDetailResp.setStartTime(DateUtils.format(DateUtils.getAddMinute(timeListMapEntryDataRespList.get(0).getTime(), -15), "HH:mm"));
                                    appApplyIndexDeviceTimeDetailResp.setEndTime(DateUtils.format(timeListMapEntryDataRespList.get(0).getTime(), "HH:mm"));
                                    if (appApplyIndexDeviceTimeDetailResp.getEndTime().equals("00:00")) {
                                        appApplyIndexDeviceTimeDetailResp.setEndTime("24:00");
                                    }
                                    appApplyIndexDeviceTimeDetailResp.setPower(MathUtils.mulDoubleZero(Math.abs(power), rate, 8));
                                    if (appApplyIndexDeviceTimeDetailResp.getPower().compareTo(dealPower) < 0) {
                                        appApplyIndexDeviceTimeDetailResp.setPower(0D);
                                    }
                                    appApplyIndexDeviceTimeDetailResp.setUseStatus(0);
                                    if (aggregatorEntDevice.getResourceTypeId().equals(EnergyModelEnum.DISTRIBUTED_ENERGY.getChannelNo())) {
                                        if (appApplyIndexDeviceTimeDetailResp.getPower() != 0D) {
                                            if (power > 0) {
                                                appApplyIndexDeviceTimeDetailResp.setUseStatus(-1);
                                            } else if (power < 0) {
                                                appApplyIndexDeviceTimeDetailResp.setUseStatus(1);
                                            }
                                        }
                                    }
                                    appApplyIndexDeviceTimeDetailRespList.add(appApplyIndexDeviceTimeDetailResp);
                                }
                            }
                        });
                        deviceBaseIdMap.put(aggregatorEntDevice.getDeviceBaseId(), appApplyIndexDeviceTimeDetailRespList);
                    }
                }
            });
        }
        return deviceBaseIdMap;
    }

    /**
     * 查询出清价格
     *
     * @param date
     * @return
     */
    private Map<String, Double> getAggregatorPriceMap(String date) {
        List<AggregatorResourceDateIssueOffer> aggregatorResourceDateIssueOfferList = aggregatorResourceDateIssueOfferService.getAggregatorResourceDateIssueOfferList(date);
        if (null != aggregatorResourceDateIssueOfferList && aggregatorResourceDateIssueOfferList.size() > 0) {
            return aggregatorResourceDateIssueOfferList.stream().collect(toMap(AggregatorResourceDateIssueOffer::getAggregatorId, AggregatorResourceDateIssueOffer::getOffer, (v1, v2) -> v1));
        }
        return new HashMap<>();
    }

    /**
     * 查询基线负荷
     *
     * @param deviceBaseIdList
     * @return
     */
    private Map<String, Map<String, Double>> getBaseLinePowerMap(List<String> deviceBaseIdList, String date) {
        Map<String, Map<String, Double>> baseLinePowerMap = new HashMap<>();
        List<AggregatorDeviceDateBaseLineLoadChart> aggregatorDeviceDateBaseLineLoadChartList = aggregatorDeviceDateBaseLineLoadChartService.getAggregatorDeviceDateBaseLineLoadChartList(deviceBaseIdList, date);
        if (null == aggregatorDeviceDateBaseLineLoadChartList || aggregatorDeviceDateBaseLineLoadChartList.size() <= 0) {
            return baseLinePowerMap;
        }
        aggregatorDeviceDateBaseLineLoadChartList.forEach(chart -> {
            if (null != chart && StringUtils.isNotEmpty(chart.getBaseLineLoadChart())) {
                List<DataResp> dataRespList = JSONArray.parseArray(chart.getBaseLineLoadChart(), DataResp.class);
                if (null != dataRespList && dataRespList.size() > 0) {
                    Map<String, Double> timeMap = dataRespList.stream().collect(toMap(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm"), DataResp::getValue));
                    baseLinePowerMap.put(chart.getDeviceBaseId(), timeMap);
                }
            }
        });
        return baseLinePowerMap;
    }

    /**
     * 查询下发功率
     *
     * @param aggregatorDeviceDateIssueChartList
     * @return
     */
    private Map<String, Map<String, Double>> getIssuePowerMap(List<AggregatorDeviceDateIssueChart> aggregatorDeviceDateIssueChartList) {
        Map<String, Map<String, Double>> issuePowerMap = new HashMap<>();
        if (null == aggregatorDeviceDateIssueChartList || aggregatorDeviceDateIssueChartList.size() <= 0) {
            return issuePowerMap;
        }
        aggregatorDeviceDateIssueChartList.forEach(chart -> {
            if (null != chart && StringUtils.isNotEmpty(chart.getIssueChart())) {
                List<DataResp> dataRespList = JSONArray.parseArray(chart.getIssueChart(), DataResp.class);
                if (null != dataRespList && dataRespList.size() > 0) {
                    Map<String, Double> timeMap = dataRespList.stream().collect(toMap(DataResp::getTime, DataResp::getValue));
                    issuePowerMap.put(chart.getDeviceBaseId(), timeMap);
                }
            }
        });
        return issuePowerMap;
    }

    /**
     * 查询申报功率
     *
     * @param aggregatorDeviceDateDeliveryChartList
     * @return
     */
    private Map<String, Map<String, Double>> getDeliveryPowerMap(List<AggregatorDeviceDateDeliveryChart> aggregatorDeviceDateDeliveryChartList) {
        Map<String, Map<String, Double>> deliveryPowerMap = new HashMap<>();
        if (null == aggregatorDeviceDateDeliveryChartList || aggregatorDeviceDateDeliveryChartList.size() <= 0) {
            return deliveryPowerMap;
        }
        aggregatorDeviceDateDeliveryChartList.forEach(chart -> {
            if (null != chart && StringUtils.isNotEmpty(chart.getDeliveryChart())) {
                List<DataResp> dataRespList = JSONArray.parseArray(chart.getDeliveryChart(), DataResp.class);
                if (null != dataRespList && dataRespList.size() > 0) {
                    Map<String, Double> timeMap = dataRespList.stream().collect(toMap(DataResp::getTime, DataResp::getValue));
                    deliveryPowerMap.put(chart.getDeviceBaseId(), timeMap);
                }
            }
        });
        return deliveryPowerMap;
    }

    /**
     * 查询大数据
     *
     * @param deviceList
     * @param startTime
     * @param endTime
     * @param entSimulateList
     * @param minuteList
     * @return
     */
    private Map<String, Map<String, Double>> getRealTimeAvgPower(List<AggregatorEntDevice> deviceList, String startTime, String endTime, List<String> entSimulateList, List<String> minuteList) {
        Map<String, Map<String, Double>> deviceBaseIdTimeValueMap = new HashMap<>();
//        List<BigDataHistoryAndCalculationResp> bigDataResultList = Lists.newArrayList();
        List<BigDataHistoryResp> bigDataHistoryRespList = Lists.newArrayList();
        if (null != entSimulateList && entSimulateList.size() > 0) {
            List<AggregatorEntDevice> deviceListWithOne = deviceList.stream().filter(device -> entSimulateList.contains(device.getEntId())).collect(toList());
            if (null != deviceListWithOne && deviceListWithOne.size() > 0) {
//                List<DeviceGroupPointInfo> deviceGroupPointInfoList = aggregatorDevicePointService.getDeviceGroupPointInfoList(deviceListWithOne, MetricEnum.YES_POWER.getCode());
//                List<BigDataHistoryAndCalculationResp> bigDataHistoryAndCalculationRespList = bigDataHistoryService.queryBigData(deviceListWithOne, deviceGroupPointInfoList, "1minute", startTime, endTime, "1");
//                if (null != bigDataHistoryAndCalculationRespList && bigDataHistoryAndCalculationRespList.size() > 0) {
//                    bigDataResultList.addAll(bigDataHistoryAndCalculationRespList);
//                }
                List<BigDataHistoryResp> bigDataHistoryRespListByOne = bigDataHistoryService.getBigData(deviceListWithOne, Arrays.asList(MetricEnum.YES_POWER.getCode()), startTime, endTime, "1");
                if (null != bigDataHistoryRespListByOne && bigDataHistoryRespListByOne.size() > 0) {
                    bigDataHistoryRespList.addAll(bigDataHistoryRespListByOne);
                }
            }
            List<AggregatorEntDevice> deviceListWithZero = deviceList.stream().filter(device -> !entSimulateList.contains(device.getEntId())).collect(toList());
            if (null != deviceListWithZero && deviceListWithZero.size() > 0) {
//                List<DeviceGroupPointInfo> deviceGroupPointInfoList = aggregatorDevicePointService.getDeviceGroupPointInfoList(deviceListWithZero, MetricEnum.YES_POWER.getCode());
//                List<BigDataHistoryAndCalculationResp> bigDataHistoryAndCalculationRespList = bigDataHistoryService.queryBigData(deviceListWithZero, deviceGroupPointInfoList, "1minute", startTime, endTime, "0");
//                if (null != bigDataHistoryAndCalculationRespList && bigDataHistoryAndCalculationRespList.size() > 0) {
//                    bigDataResultList.addAll(bigDataHistoryAndCalculationRespList);
//                }
                List<BigDataHistoryResp> bigDataHistoryRespListByZero = bigDataHistoryService.getBigData(deviceListWithZero, Arrays.asList(MetricEnum.YES_POWER.getCode()), startTime, endTime, "0");
                if (null != bigDataHistoryRespListByZero && bigDataHistoryRespListByZero.size() > 0) {
                    bigDataHistoryRespList.addAll(bigDataHistoryRespListByZero);
                }
            }
        } else {
//            List<DeviceGroupPointInfo> deviceGroupPointInfoList = aggregatorDevicePointService.getDeviceGroupPointInfoList(deviceList, MetricEnum.YES_POWER.getCode());
//            List<BigDataHistoryAndCalculationResp> bigDataHistoryAndCalculationRespList = bigDataHistoryService.queryBigData(deviceList, deviceGroupPointInfoList, "1minute", startTime, endTime, "0");
//            if (null != bigDataHistoryAndCalculationRespList && bigDataHistoryAndCalculationRespList.size() > 0) {
//                bigDataResultList.addAll(bigDataHistoryAndCalculationRespList);
//            }
            List<BigDataHistoryResp> bigDataHistoryRespListByDevice = bigDataHistoryService.getBigData(deviceList, Arrays.asList(MetricEnum.YES_POWER.getCode()), startTime, endTime, "0");
            if (null != bigDataHistoryRespListByDevice && bigDataHistoryRespListByDevice.size() > 0) {
                bigDataHistoryRespList.addAll(bigDataHistoryRespListByDevice);
            }
        }
//        if (null != bigDataResultList && bigDataResultList.size() > 0) {
//            Map<String, List<DataResp>> deviceIdStationIdMap = bigDataResultList.stream().collect(toMap(bigDataHistoryResp -> bigDataHistoryResp.getEquipMK() + "_" + bigDataHistoryResp.getDeviceId() + "," + bigDataHistoryResp.getSystemCode(), bigDataHistoryResp -> bigDataHistoryResp.getDataResp(), (v1, v2) -> v1));
//            deviceList.forEach(device -> {
//                Map<String, Double> timeMap = new HashMap<>();
//                String key = device.getDeviceId() + "," + device.getStationId();
//                List<DataResp> dataRespList = deviceIdStationIdMap.get(key);
//                if (null != dataRespList && dataRespList.size() > 0) {
//                    Map<String, Double> dateRespMap = dataRespList.stream().filter(dataResp -> null != dataResp && null != dataResp.getValue()).collect(toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1));
//                    for (int i = 1; i < minuteList.size() - 1; i += 15) {
//                        AtomicReference<Double> totalValue = new AtomicReference<>();
//                        AtomicReference<Integer> num = new AtomicReference<>(0);
//                        minuteList.subList(i, i + 15).forEach(minute -> {
//                            if (null != dateRespMap.get(minute)) {
//                                num.getAndSet(num.get() + 1);
//                                if (null == totalValue.get()) {
//                                    totalValue.set(dateRespMap.get(minute));
//                                } else {
//                                    totalValue.updateAndGet(v -> v + dateRespMap.get(minute));
//                                }
//                            }
//                        });
//                        if (null != totalValue.get() && !num.get().equals(0)) {
//                            Double avgPower = MathUtils.divideNull(totalValue.get(), Double.valueOf(String.valueOf(num)), 8);
//                            timeMap.put(minuteList.get(i + 14), avgPower);
//                            if (StringUtils.isNotEmpty(device.getResourceTypeId()) && device.getResourceTypeId().equals("27")) {
//                                timeMap.put(minuteList.get(i + 14), null == avgPower ? null : 0 - avgPower);
//                            }
//                        }
//                    }
//                }
//                deviceBaseIdTimeValueMap.put(device.getDeviceBaseId(), timeMap);
//            });
//        }
        if (null != bigDataHistoryRespList && bigDataHistoryRespList.size() > 0) {
            Map<String, List<DataResp>> deviceIdStationIdMap = bigDataHistoryRespList.stream().collect(toMap(bigDataHistoryResp -> bigDataHistoryResp.getEquipMK() + "_" + bigDataHistoryResp.getEquipID() + "," + bigDataHistoryResp.getStaId(), bigDataHistoryResp -> bigDataHistoryResp.getDataResp(), (v1, v2) -> v1));
            deviceList.forEach(device -> {
                Map<String, Double> timeMap = new HashMap<>();
                String key = device.getDeviceId() + "," + device.getStationId();
                List<DataResp> dataRespList = deviceIdStationIdMap.get(key);
                if (null != dataRespList && dataRespList.size() > 0) {
                    Map<String, Double> dateRespMap = dataRespList.stream().filter(dataResp -> null != dataResp && null != dataResp.getValue()).collect(toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1));
                    for (int i = 1; i < minuteList.size() - 1; i += 15) {
                        AtomicReference<Double> totalValue = new AtomicReference<>();
                        AtomicReference<Integer> num = new AtomicReference<>(0);
                        minuteList.subList(i, i + 15).forEach(minute -> {
                            if (null != dateRespMap.get(minute)) {
                                num.getAndSet(num.get() + 1);
                                if (null == totalValue.get()) {
                                    totalValue.set(dateRespMap.get(minute));
                                } else {
                                    totalValue.updateAndGet(v -> v + dateRespMap.get(minute));
                                }
                            }
                        });
                        if (null != totalValue.get() && !num.get().equals(0)) {
                            Double avgPower = MathUtils.divideNull(totalValue.get(), Double.valueOf(String.valueOf(num)), 8);
                            timeMap.put(minuteList.get(i + 14), avgPower);
                            if (StringUtils.isNotEmpty(device.getResourceTypeId()) && device.getResourceTypeId().equals("27")) {
                                timeMap.put(minuteList.get(i + 14), null == avgPower ? null : 0 - avgPower);
                            }
                        }
                    }
                }
                deviceBaseIdTimeValueMap.put(device.getDeviceBaseId(), timeMap);
            });
        }
        return deviceBaseIdTimeValueMap;
    }

    /**
     * 保存下发报价
     *
     * @param aggregatorId
     * @param date
     */
    private void saveAggregatorResourceDateDeliveryOffer(String aggregatorId, String date) {
        List<AggregatorResourceDateDeliveryOffer> aggregatorResourceDateDeliveryOfferList = aggregatorResourceDateDeliveryOfferService.getAggregatorResourceDateDeliveryOfferList(aggregatorId, date);
        if (null == aggregatorResourceDateDeliveryOfferList || aggregatorResourceDateDeliveryOfferList.size() <= 0) {
            throw new BaseException(StatusCode.ERROR.getCode(), "聚合商未申报价格");
        }
        List<AggregatorResourceDateIssueOffer> issueOfferList = Lists.newArrayList();
        aggregatorResourceDateDeliveryOfferList.forEach(offer -> {
            AggregatorResourceDateIssueOffer aggregatorResourceDateIssueOffer = new AggregatorResourceDateIssueOffer();
            aggregatorResourceDateIssueOffer.setAggregatorId(offer.getAggregatorId());
            aggregatorResourceDateIssueOffer.setResourceTypeId(offer.getResourceTypeId());
            aggregatorResourceDateIssueOffer.setDate(offer.getDate());
            aggregatorResourceDateIssueOffer.setOffer(offer.getOffer());
            aggregatorResourceDateIssueOffer.setPriceDetail(offer.getPriceDetail());
            issueOfferList.add(aggregatorResourceDateIssueOffer);
        });
        aggregatorResourceDateIssueOfferService.saveAggregatorResourceDateDeliveryOffer(aggregatorId, date, issueOfferList);
    }

    /**
     * 保存设备下发曲线
     *
     * @param aggregatorId
     * @param date
     */
    private void saveAggregatorDeviceDateIssueChart(String aggregatorId, String date) {
        List<AggregatorDeviceDateDeliveryChart> aggregatorDeviceDateDeliveryChartList = aggregatorDeviceDateDeliveryChartService.getAggregatorDeviceDateDeliveryChartList(aggregatorId, date);
        if (null == aggregatorDeviceDateDeliveryChartList || aggregatorDeviceDateDeliveryChartList.size() <= 0) {
            throw new BaseException(StatusCode.ERROR.getCode(), "聚合商企业设备未申报功率曲线");
        }
        aggregatorDeviceDateIssueChartService.delete(aggregatorId, date);
        List<AggregatorDeviceDateIssueChart> issueChartList = Lists.newArrayList();
        aggregatorDeviceDateDeliveryChartList.forEach(chart -> {
            AggregatorDeviceDateIssueChart aggregatorDeviceDateIssueChart = new AggregatorDeviceDateIssueChart();
            aggregatorDeviceDateIssueChart.setAggregatorId(chart.getAggregatorId());
            aggregatorDeviceDateIssueChart.setEntId(chart.getEntId());
            aggregatorDeviceDateIssueChart.setStationId(chart.getStationId());
            aggregatorDeviceDateIssueChart.setResourceTypeId(chart.getResourceTypeId());
            aggregatorDeviceDateIssueChart.setDeviceBaseId(chart.getDeviceBaseId());
            aggregatorDeviceDateIssueChart.setDate(chart.getDate());
            aggregatorDeviceDateIssueChart.setIssueChart(chart.getDeliveryChart());
            issueChartList.add(aggregatorDeviceDateIssueChart);
        });
        aggregatorDeviceDateIssueChartService.batchInsert(issueChartList);
    }

    /**
     * 保存聚合商下发曲线
     *
     * @param aggregatorId
     * @param date
     */
    private void saveAggregatorDateChart(String aggregatorId, String date) {
        List<AggregatorDateDeliveryChart> aggregatorDateDeliveryChartList = aggregatorDateDeliveryChartService.getAggregatorDateDeliveryChartList(aggregatorId, date);
        if (null == aggregatorDateDeliveryChartList || aggregatorDateDeliveryChartList.size() <= 0) {
            throw new BaseException(StatusCode.ERROR.getCode(), "聚合商未申报功率曲线");
        }
        aggregatorDateIssueChartService.delete(aggregatorId, date);
        List<AggregatorDateIssueChart> issueChartList = Lists.newArrayList();
        aggregatorDateDeliveryChartList.forEach(chart -> {
            AggregatorDateIssueChart aggregatorDateIssueChart = new AggregatorDateIssueChart();
            aggregatorDateIssueChart.setAggregatorId(aggregatorId);
            aggregatorDateIssueChart.setResourceTypeId(chart.getResourceTypeId());
            aggregatorDateIssueChart.setDate(chart.getDate());
            aggregatorDateIssueChart.setIssueChart(chart.getDeliveryChart());
            issueChartList.add(aggregatorDateIssueChart);
        });
        aggregatorDateIssueChartService.batchInsert(issueChartList);
    }

    /**
     * 修改聚合中标状态
     *
     * @param aggregatorId
     * @param winStatus
     * @param date
     */
    private void saveAggregatorWinStatus(String now, String aggregatorId, String winStatus, String date) {
        List<AggregatorDateApplyDetail> aggregatorDateApplyDetailList = aggregatorDateApplyDetailService.getAggregatorDateApplyDetailList(aggregatorId, date, null);
        if (null == aggregatorDateApplyDetailList || aggregatorDateApplyDetailList.size() <= 0) {
            throw new BaseException(StatusCode.ERROR.getCode(), "聚合商未申报");
        }
        AggregatorDateApplyDetail update = new AggregatorDateApplyDetail();
        update.setWinStatus(winStatus);
        update.setWinTime(now);
        update.setUpdateTime(now);
        aggregatorDateApplyDetailService.updateAggregatorDateApplyDetail(update, aggregatorId, date);
    }

    /**
     * 保存企业中标状态
     *
     * @param aggregatorId
     * @param winStatus
     * @param dateList
     */
    private void saveAggregatorEntDateApplyDetail(String aggregatorId, String winStatus, List<String> dateList) {
        AggregatorEntDateApplyDetail aggregatorEntDateApplyDetail = new AggregatorEntDateApplyDetail();
        aggregatorEntDateApplyDetail.setWinStatus(StringUtils.isEmpty(winStatus) || winStatus.equals("0") ? false : true);
        aggregatorEntDateApplyDetailService.update(aggregatorEntDateApplyDetail, aggregatorId, dateList);
        List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList(aggregatorId);
        aggregatorEntList.forEach(ent -> redis.set(Constants.APP_INDEX_ENT_REFRESH_STATUS + ent.getEntId(), "1"));
    }

    /**
     * 处理曲线数据
     *
     * @return
     */
    private String getChart(List<String> minuteList) {
        if (null != minuteList && minuteList.size() > 0) {
            List<DataResp> dataRespList = Lists.newArrayList();
            minuteList.forEach(minute -> {
                DataResp dataResp = new DataResp();
                dataResp.setTime(minute);
                dataResp.setValue(MathUtils.randomDoubleValue(30, 50, 8));
                dataRespList.add(dataResp);
            });
            return JSONObject.toJSONString(dataRespList);
        }
        return null;
    }

    /**
     * 曲线汇总
     *
     * @param deviceDateChartList
     * @param minuteList
     * @return
     */
    private List<DataResp> getDeliveryChartList(List<String> deviceDateChartList, List<String> minuteList) {
        if (null != deviceDateChartList && deviceDateChartList.size() > 0) {
            //各设备曲线字符串转成集合放入总集里
            List<DataResp> chartList = Lists.newArrayList();
            deviceDateChartList.stream().filter(deliveryChart -> StringUtils.isNotEmpty(deliveryChart)).forEach(deliveryChart -> {
                List<DataResp> dataRespList = JSONArray.parseArray(deliveryChart, DataResp.class);
                if (null != dataRespList && dataRespList.size() > 0) {
                    chartList.addAll(dataRespList);
                }
            });
            //各设备曲线总集按时间汇总
            Map<String, Double> timeValueMap = new HashMap<>();
            if (null != chartList && chartList.size() > 0) {
                timeValueMap = chartList.stream().collect(groupingBy(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm"), summingDouble(DataResp::getValue)));
            }
            if (null == timeValueMap || timeValueMap.size() <= 0) {
                timeValueMap = new HashMap<>();
            }
            //数据汇总Map转成15分钟96个点
            if (null != minuteList && minuteList.size() > 0) {
                List<DataResp> deliveryChartList = Lists.newArrayList();
                Map<String, Double> finalTimeValueMap = timeValueMap;
                minuteList.forEach(minute -> {
                    DataResp dataResp = new DataResp();
                    dataResp.setTime(minute);
                    dataResp.setValue(null == finalTimeValueMap.get(DateUtils.format(dataResp.getTime(), "HH:mm")) ? null : MathUtils.doublePoint(finalTimeValueMap.get(DateUtils.format(dataResp.getTime(), "HH:mm")), 8));
                    deliveryChartList.add(dataResp);
                });
                return deliveryChartList;
            }
        }
        return Lists.newArrayList();
    }
}
