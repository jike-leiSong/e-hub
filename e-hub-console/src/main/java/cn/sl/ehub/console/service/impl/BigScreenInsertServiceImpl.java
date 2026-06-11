package cn.sl.ehub.console.service.impl;

import cn.enn.bigdata.resp.BigDataHistoryAndCalculationResp;
import cn.enn.bigdata.service.IBigDataHandlerService;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.console.enums.AggregatorEntSocialResponsibilityEnum;
import cn.sl.ehub.console.enums.MetricEnum;
import cn.sl.ehub.service.mapper.*;
import cn.sl.ehub.service.resp.AggregatorDeviceDateProfitResp;
import cn.sl.ehub.service.resp.AggregatorDeviceQuantityResp;
import cn.sl.ehub.console.service.*;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.service.vo.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Comparators;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.summingDouble;

/**
 * 大屏接口ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class BigScreenInsertServiceImpl implements IBigScreenInsertService {

    private final IAggregatorDateProfitService aggregatorDateProfitService;
    private final AggregatorEntSocialResponsibilityMapper aggregatorEntSocialResponsibilityMapper;
    private final BigScreenGeneralSituationMapper bigScreenGeneralSituationMapper;
    private final IAggregatorEntDateProfitService aggregatorEntDateProfitService;
    private final IAggregatorEntService aggregatorEntService;
    private final BigScreenEntProfitMapper bigScreenEntProfitMapper;
    private final IAggregatorDeviceDateDeliveryChartService aggregatorDeviceDateDeliveryChartService;
    private final IAggregatorEntDeviceService aggregatorEntDeviceService;
    private final IAggregatorDevicePointService aggregatorDevicePointService;
    private final IBigDataHandlerService bigDataHistoryService;
    private final IAggregatorDeviceDateBaseLineLoadChartService aggregatorDeviceDateBaseLineLoadChartService;
    private final IAggregatorEntProfitTimeService aggregatorEntProfitTimeService;
    private final BigScreenEntTodayRateMapper bigScreenEntTodayRateMapper;
    private final IAggregatorResourceDateIssueOfferService aggregatorResourceDateIssueOfferService;
    private final BigScreenWeekDayAverageChartMapper bigScreenWeekDayAverageChartMapper;
    private final IAggregatorDeviceDateProfitService aggregatorDeviceDateProfitService;
    //    private final BigScreenDayLogMapper bigScreenDayLogMapper;
    @Resource(name = "threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor executor;

    @Override
    public Boolean insertAllAndEntProfit() {
        executor.execute(() -> insertAll());
        executor.execute(() -> insertBigScreenEntProfit());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertAll() {
        List<AggregatorDateProfit> aggregatorDateProfitList = aggregatorDateProfitService.getAggregatorDateProfitList();
        BigScreenGeneralSituation bigScreenGeneralSituation = bigScreenGeneralSituationMapper.getBigScreenGeneralSituation();
        String now = DateUtils.getTime();
        boolean flag = true;
        if (null == bigScreenGeneralSituation) {
            bigScreenGeneralSituation = new BigScreenGeneralSituation();
            flag = false;
        }
        Long days = aggregatorDateProfitList.stream().filter(profit -> null != profit && null != profit.getIssueProfit() && 0 != profit.getIssueProfit()).collect(Collectors.counting());
        Double issueProfit = aggregatorDateProfitList.stream().filter(profit -> null != profit && null != profit.getIssueProfit()).collect(Collectors.summingDouble(AggregatorDateProfit::getIssueProfit));
        Double electricQuantity = aggregatorDateProfitList.stream().filter(profit -> null != profit && null != profit.getElectricQuantity()).collect(Collectors.summingDouble(AggregatorDateProfit::getElectricQuantity));
        bigScreenGeneralSituation.setTransactionDay(days.intValue());
        bigScreenGeneralSituation.setProfit(null == issueProfit ? 0 : MathUtils.doublePoint(issueProfit, 2));
        bigScreenGeneralSituation.setQuantity(null == electricQuantity ? 0 : MathUtils.doublePoint(electricQuantity, 2));
        List<AggregatorEntSocialResponsibility> aggregatorEntSocialResponsibilityList = aggregatorEntSocialResponsibilityMapper.selectAll();
        BigScreenGeneralSituation finalBigScreenGeneralSituation = bigScreenGeneralSituation;
        aggregatorEntSocialResponsibilityList.forEach(config -> {
            if (config.getCode().equals(AggregatorEntSocialResponsibilityEnum.CO2V2.getCode())) {
                finalBigScreenGeneralSituation.setCo2(MathUtils.mulDoubleZero(finalBigScreenGeneralSituation.getQuantity(), config.getValue(), config.getPoint()));
            } else if (config.getCode().equals(AggregatorEntSocialResponsibilityEnum.TREEV2.getCode())) {
                finalBigScreenGeneralSituation.setTree(MathUtils.mulDoubleZero(finalBigScreenGeneralSituation.getQuantity(), config.getValue(), config.getPoint()).intValue());
            } else if (config.getCode().equals(AggregatorEntSocialResponsibilityEnum.AREAV2.getCode())) {
                finalBigScreenGeneralSituation.setArea(MathUtils.mulDoubleZero(finalBigScreenGeneralSituation.getQuantity(), config.getValue(), config.getPoint()));
            } else if (config.getCode().equals(AggregatorEntSocialResponsibilityEnum.COALV2.getCode())) {
                finalBigScreenGeneralSituation.setCoal(MathUtils.mulDoubleZero(finalBigScreenGeneralSituation.getQuantity(), config.getValue(), config.getPoint()));
            } else if (config.getCode().equals(AggregatorEntSocialResponsibilityEnum.SO2V2.getCode())) {
                finalBigScreenGeneralSituation.setSo2(MathUtils.mulDoubleZero(finalBigScreenGeneralSituation.getQuantity(), config.getValue(), config.getPoint()));
            } else if (config.getCode().equals(AggregatorEntSocialResponsibilityEnum.NOXV2.getCode())) {
                finalBigScreenGeneralSituation.setNox(MathUtils.mulDoubleZero(finalBigScreenGeneralSituation.getQuantity(), config.getValue(), config.getPoint()));
            }
        });
        bigScreenGeneralSituation.setUpdateTime(now);
        if (flag) {
            bigScreenGeneralSituationMapper.updateByPrimaryKeySelective(bigScreenGeneralSituation);
        } else {
            bigScreenGeneralSituation.setCreateTime(now);
            bigScreenGeneralSituationMapper.insertSelective(bigScreenGeneralSituation);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertBigScreenEntProfit() {
        String now = DateUtils.getTime();
        Map<String, BigScreenEntProfit> bigScreenEntProfitMap = new HashMap<>();
        List<BigScreenEntProfit> bigScreenEntProfitList = bigScreenEntProfitMapper.selectAll();
        if (CollectionUtils.isNotEmpty(bigScreenEntProfitList)) {
            bigScreenEntProfitMap.putAll(bigScreenEntProfitList.stream().filter(profit -> null != profit).collect(Collectors.toMap(BigScreenEntProfit::getEntId, Function.identity(), (v1, v2) -> v1)));
        }
        List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList();
        List<AggregatorEntDateProfit> aggregatorEntDateProfitList = aggregatorEntDateProfitService.getAggregatorEntDateProfitList();
        Map<String, Double> entIdProfit = aggregatorEntDateProfitList.stream().filter(profit -> null != profit && null != profit.getEntProfit()).collect(Collectors.toMap(AggregatorEntDateProfit::getEntId, AggregatorEntDateProfit::getEntProfit, (v1, v2) -> v1 + v2));
        Map<String, Double> entIdElectricQuantity = aggregatorEntDateProfitList.stream().filter(profit -> null != profit && null != profit.getElectricQuantity()).collect(Collectors.toMap(AggregatorEntDateProfit::getEntId, AggregatorEntDateProfit::getElectricQuantity, (v1, v2) -> v1 + v2));
        aggregatorEntList.forEach(ent -> {
            boolean flag = true;
            BigScreenEntProfit bigScreenEntProfit = bigScreenEntProfitMap.get(ent.getEntId());
            if (null == bigScreenEntProfit) {
                bigScreenEntProfit = new BigScreenEntProfit();
                flag = false;
            }
            bigScreenEntProfit.setEntId(ent.getEntId());
            bigScreenEntProfit.setEntName(ent.getEntName());
            Double electricQuantity = entIdElectricQuantity.get(ent.getEntId());
            bigScreenEntProfit.setQuantity(null == electricQuantity ? 0 : MathUtils.doublePoint(electricQuantity, 2));
            Double profit = entIdProfit.get(ent.getEntId());
            bigScreenEntProfit.setProfit(null == profit ? 0 : MathUtils.doublePoint(profit, 2));
            bigScreenEntProfit.setUpdateTime(now);
            if (flag) {
                bigScreenEntProfitMapper.updateByPrimaryKeySelective(bigScreenEntProfit);
            } else {
                bigScreenEntProfit.setCreateTime(now);
                bigScreenEntProfitMapper.insertSelective(bigScreenEntProfit);
            }
        });
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertBigScreenTodayRate(String date) {
        Map<String, List<AggregatorEntProfitTime>> entTimeMap = aggregatorEntProfitTimeService.getEntMap();
        List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList = aggregatorDeviceDateProfitService.getAggregatorDeviceDateProfitList(date);
        List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = Lists.newArrayList();
        aggregatorDeviceDateProfitList.forEach(profit -> {
            aggregatorDeviceDateProfitRespList.addAll(JSONArray.parseArray(profit.getProfitDetail(), AggregatorDeviceDateProfitResp.class));
        });
        List<AggregatorDeviceDateProfitResp> respList = getEntIdTotalQuantityList(aggregatorDeviceDateProfitRespList, entTimeMap);
        Map<String, Double> entTotalQuantityMap = respList.stream().filter(resp -> null != resp && null != resp.getEstimateElectricQuantity()).collect(toMap(AggregatorDeviceDateProfitResp::getEntId, AggregatorDeviceDateProfitResp::getEstimateElectricQuantity, (v1, v2) -> v1 + v2));
        Map<String, Map<String, Double>> entTimeValueMap = respList.stream().collect(groupingBy(AggregatorDeviceDateProfitResp::getEntId, toMap(AggregatorDeviceDateProfitResp::getEndTime, AggregatorDeviceDateProfitResp::getElectricQuantity, (v1, v2) -> v1 + v2)));
        List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList();
        List<AggregatorEntSocialResponsibility> aggregatorEntSocialResponsibilityList = aggregatorEntSocialResponsibilityMapper.selectAll();
        AggregatorEntSocialResponsibility socialConfig = aggregatorEntSocialResponsibilityList.stream().filter(config -> null != config && config.getCode().equals(AggregatorEntSocialResponsibilityEnum.AREAV2.getCode())).findFirst().get();
        Map<String, String> entMap = aggregatorEntList.stream().collect(toMap(AggregatorEnt::getEntId, AggregatorEnt::getEntName, (v1, v2) -> v1));
        List<BigScreenEntTodayRate> bigScreenEntTodayRateList = Lists.newArrayList();
        entTimeValueMap.entrySet().forEach(entTimeValueMapEntry -> {
            Map<String, Double> timeValueMap = entTimeValueMapEntry.getValue();
            if (null != timeValueMap && timeValueMap.size() > 0) {
                timeValueMap.entrySet().forEach(timeValueMapEntry -> {
                    BigScreenEntTodayRate bigScreenEntTodayRate = new BigScreenEntTodayRate();
                    bigScreenEntTodayRate.setEntId(entTimeValueMapEntry.getKey());
                    bigScreenEntTodayRate.setEntName(entMap.get(bigScreenEntTodayRate.getEntId()));
                    bigScreenEntTodayRate.setTotalQuantity(entTotalQuantityMap.get(bigScreenEntTodayRate.getEntId()));
                    bigScreenEntTodayRate.setTime(timeValueMapEntry.getKey());
                    bigScreenEntTodayRate.setFinishQuantity(timeValueMapEntry.getValue());
                    bigScreenEntTodayRate.setFinishRate(MathUtils.mulDoubleZero(MathUtils.divideZero(bigScreenEntTodayRate.getFinishQuantity(), bigScreenEntTodayRate.getTotalQuantity(), 2), 100D, 2));
                    bigScreenEntTodayRate.setArea(MathUtils.mulDoubleZero(bigScreenEntTodayRate.getFinishQuantity(), socialConfig.getValue(), socialConfig.getPoint()));
                    bigScreenEntTodayRateList.add(bigScreenEntTodayRate);
                });
            }
        });
        if (CollectionUtils.isNotEmpty(bigScreenEntTodayRateList)) {
            List<BigScreenEntTodayRate> resultList = Lists.newArrayList();
            Map<String, List<BigScreenEntTodayRate>> entRateListMap = bigScreenEntTodayRateList.stream().collect(groupingBy(BigScreenEntTodayRate::getEntId));
            entRateListMap.entrySet().forEach(entRateListMapEntry -> {
                List<BigScreenEntTodayRate> rateList = entRateListMapEntry.getValue();
                List<BigScreenEntTodayRate> sortList = rateList.stream().sorted(Comparator.comparing(BigScreenEntTodayRate::getTime)).collect(toList());
                for (int i = 0; i < sortList.size() - 1; i++) {
                    BigScreenEntTodayRate bigScreenEntTodayRate0 = sortList.get(i);
                    BigScreenEntTodayRate bigScreenEntTodayRate1 = sortList.get(i + 1);
                    bigScreenEntTodayRate1.setFinishQuantity(bigScreenEntTodayRate0.getFinishQuantity() + bigScreenEntTodayRate1.getFinishQuantity());
                    bigScreenEntTodayRate1.setFinishRate(MathUtils.mulDoubleZero(MathUtils.divideZero(bigScreenEntTodayRate1.getFinishQuantity(), bigScreenEntTodayRate1.getTotalQuantity(), 2), 100D, 2));
                    bigScreenEntTodayRate1.setArea(MathUtils.mulDoubleZero(bigScreenEntTodayRate1.getFinishQuantity(), socialConfig.getValue(), socialConfig.getPoint()));
                }
                resultList.addAll(sortList);
            });
            bigScreenEntTodayRateMapper.batchInsert(resultList);
        }
        return true;
    }

    @Override
    public Boolean insertBigScreenWeekDayAverageChart(String startDate, String endDate) {
        if (StringUtils.isEmpty(startDate)) {
            startDate = "2021-03-06";
        }
        if (StringUtils.isEmpty(endDate)) {
            endDate = "2021-03-12";
        }
        List<String> dayList = DateUtils.getDayList(startDate, endDate);
        List<AggregatorResourceDateIssueOffer> aggregatorResourceDateIssueOfferList = aggregatorResourceDateIssueOfferService.getAggregatorResourceDateIssueOfferList(dayList);
        List<DataResp> dataRespList = Lists.newArrayList();
        aggregatorResourceDateIssueOfferList.forEach(offer -> {
            String priceChart = offer.getPriceChart();
            List<DataResp> dataList = JSONArray.parseArray(priceChart, DataResp.class);
            dataRespList.addAll(dataList);
        });
        List<DataResp> offerList = Lists.newArrayList();
        Map<String, List<DataResp>> timeMap = dataRespList.stream().collect(groupingBy(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm")));
        timeMap.entrySet().forEach(timeMapEntry -> {
            List<DataResp> dataResps = timeMapEntry.getValue().stream().filter(resp -> null != resp).collect(toList());
            Double totalValue = dataResps.stream().collect(summingDouble(DataResp::getValue));
            Double calValue = MathUtils.divideNull(totalValue, (double) dayList.size(), 2);
            DataResp dataResp = new DataResp();
            dataResp.setTime(timeMapEntry.getKey());
            dataResp.setValue(MathUtils.doublePoint(calValue, 2));
            offerList.add(dataResp);
        });

        List<DataResp> powerList = Lists.newArrayList();
        List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList = aggregatorDeviceDateProfitService.getAggregatorDeviceDateProfitList(dayList);
        List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = Lists.newArrayList();
        aggregatorDeviceDateProfitList.forEach(profit -> aggregatorDeviceDateProfitRespList.addAll(JSONArray.parseArray(profit.getProfitDetail(), AggregatorDeviceDateProfitResp.class)));
        Map<String, List<AggregatorDeviceDateProfitResp>> endTimeMap = aggregatorDeviceDateProfitRespList.stream().collect(groupingBy(resp -> DateUtils.format(resp.getEndTime(), "HH:mm")));
        endTimeMap.entrySet().forEach(endTimeMapEntry -> {
            List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitResps = endTimeMapEntry.getValue().stream().filter(resp -> null != resp).collect(toList());
            Double totalValue = aggregatorDeviceDateProfitResps.stream().collect(summingDouble(AggregatorDeviceDateProfitResp::getCountPower));
            Double calValue = MathUtils.divideNull(totalValue, (double) dayList.size(), 0);
            DataResp dataResp = new DataResp();
            dataResp.setTime(endTimeMapEntry.getKey());
            dataResp.setValue(calValue);
            powerList.add(dataResp);
        });

        BigScreenWeekDayAverageChart bigScreenWeekDayAverageChart = new BigScreenWeekDayAverageChart();
        bigScreenWeekDayAverageChart.setOffer(JSONObject.toJSONString(offerList.stream().sorted(Comparator.comparing(DataResp::getTime)).collect(toList())));
        bigScreenWeekDayAverageChart.setPower(JSONObject.toJSONString(powerList.stream().sorted(Comparator.comparing(DataResp::getTime)).collect(toList())));
        bigScreenWeekDayAverageChartMapper.insertSelective(bigScreenWeekDayAverageChart);
        return true;
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
     * @param minuteList
     * @return
     */
    private Map<String, Map<String, Double>> getRealTimeAvgPower(List<AggregatorEntDevice> deviceList, String startTime, String endTime, List<String> minuteList) {
        Map<String, Map<String, Double>> deviceBaseIdTimeValueMap = new HashMap<>();
        List<BigDataHistoryAndCalculationResp> bigDataResultList = Lists.newArrayList();
        List<DeviceGroupPointInfo> deviceGroupPointInfoList = aggregatorDevicePointService.getDeviceGroupPointInfoList(deviceList, MetricEnum.YES_POWER.getCode());
        List<BigDataHistoryAndCalculationResp> bigDataHistoryAndCalculationRespList = bigDataHistoryService.queryBigData(deviceList, deviceGroupPointInfoList, "1minute", startTime, endTime, "0");
        if (null != bigDataHistoryAndCalculationRespList && bigDataHistoryAndCalculationRespList.size() > 0) {
            bigDataResultList.addAll(bigDataHistoryAndCalculationRespList);
        }
        if (null != bigDataResultList && bigDataResultList.size() > 0) {
            Map<String, List<DataResp>> deviceIdStationIdMap = bigDataResultList.stream().collect(toMap(bigDataHistoryResp -> bigDataHistoryResp.getEquipMK() + "_" + bigDataHistoryResp.getDeviceId() + "," + bigDataHistoryResp.getSystemCode(), bigDataHistoryResp -> bigDataHistoryResp.getDataResp(), (v1, v2) -> v1));
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
                            Double avgPower = MathUtils.divideNull(totalValue.get(), Double.valueOf(String.valueOf(num)), 2);
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
     * 查询基线负荷
     *
     * @param deviceBaseIdList
     * @param date
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
     * 处理有效用电量
     *
     * @param aggregatorDeviceDateProfitRespList
     * @param entTimeMap
     * @return
     */
    private List<AggregatorDeviceDateProfitResp> getEntIdTotalQuantityList(List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList, Map<String, List<AggregatorEntProfitTime>> entTimeMap) {
        List<AggregatorDeviceDateProfitResp> respList = Lists.newArrayList();
        if (null == entTimeMap) {
            return aggregatorDeviceDateProfitRespList;
        } else {
            aggregatorDeviceDateProfitRespList.stream().filter(profit -> null != profit).forEach(profit -> {
                AtomicBoolean flag = new AtomicBoolean(false);
                List<AggregatorEntProfitTime> aggregatorEntProfitTimeList = entTimeMap.get(profit.getEntId());
                if (CollectionUtils.isNotEmpty(aggregatorEntProfitTimeList)) {
                    aggregatorEntProfitTimeList.forEach(time -> {
                        if (DateUtils.format(profit.getEndTime(), "HH:mm").compareTo(time.getStartTime()) >= 0 && DateUtils.format(profit.getEndTime(), "HH:mm").compareTo(time.getEndTime()) <= 0) {
                            flag.set(true);
                        }
                    });
                }
                if (!flag.get()) {
                    profit.setElectricQuantity(0D);
                    profit.setEstimateElectricQuantity(0D);
                }
                respList.add(profit);
            });
            return respList;
        }
    }
}
