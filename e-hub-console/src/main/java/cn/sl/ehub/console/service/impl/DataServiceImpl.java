package cn.sl.ehub.console.service.impl;

import cn.enn.bigdata.resp.BigDataHistoryAndCalculationResp;
import cn.enn.bigdata.resp.BigDataHistoryResp;
import cn.enn.bigdata.service.IBigDataHandlerService;
import cn.sl.ehub.console.enums.AggregatorRefreshEnum;
import cn.sl.ehub.console.enums.MetricEnum;
import cn.sl.ehub.service.resp.ProfitExportResp;
import cn.sl.ehub.service.resp.*;
import cn.sl.ehub.console.service.*;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.common.utils.GZIPUtil;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.service.vo.*;
import cn.enn.sms.req.SendMessageReq;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.summingDouble;

/**
 * 数据处理ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DataServiceImpl implements IDataService {

    private final IAggregatorInfoService aggregatorInfoService;
    private final IAggregatorResourceTypeService aggregatorResourceTypeService;
    private final IAggregatorResourceDateIssueOfferService aggregatorResourceDateIssueOfferService;
    private final IAggregatorDateIssueChartService aggregatorDateIssueChartService;
    private final IAggregatorDeviceDateProfitService aggregatorDeviceDateProfitService;
    private final IAggregatorEntService aggregatorEntService;
    private final IAggregatorEntDateProfitService aggregatorEntDateProfitService;
    private final IAggregatorDateProfitService aggregatorDateProfitService;
    private final IAggregatorDateApplyDetailService aggregatorDateApplyDetailService;
    private final IAggregatorEntDateApplyDetailService aggregatorEntDateApplyDetailService;
    private final IAggregatorDeviceDateIssueChartService aggregatorDeviceDateIssueChartService;
    private final IAggregatorDeviceDeliveryPowerPercentService aggregatorDeviceDeliveryPowerPercentService;
    private final IAggregatorDeviceDateDeliveryChartService aggregatorDeviceDateDeliveryChartService;
    private final IAggregatorEntProfitTimeService aggregatorEntProfitTimeService;
    private final IAggregatorEntDeviceService aggregatorEntDeviceService;
    private final IAggregatorDevicePointService aggregatorDevicePointService;
    private final IBigDataHandlerService bigDataHistoryService;
    private final IAggregatorDeviceDateBaseLineLoadChartService aggregatorDeviceDateBaseLineLoadChartService;
    @Autowired
    private ISmsService pushService;
    @Resource(name = "threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor executor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dealData(String issue) {
        if (StringUtils.isNotEmpty(issue)) {
            List<AggregatorInfo> aggregatorInfoList = aggregatorInfoService.getAggregatorInfoList();
            AggregatorInfo aggregatorInfo = aggregatorInfoList.get(0);
            List<AggregatorResourceType> aggregatorResourceTypeList = aggregatorResourceTypeService.getAggregatorResourceTypeList();
            JSONObject remoteResp = JSONObject.parseObject(issue);
            List<Map<String, String>> dataList = (List<Map<String, String>>) remoteResp.get("data");
            executor.execute(() -> {
                aggregatorResourceTypeList.forEach(aggregatorResourceType -> {
                    saveWinStatus(dataList, aggregatorInfo.getAggregatorId(), aggregatorResourceType.getId());
                    Map<String, Double> offerMap = saveAggregatorResourceDateIssueOfferList(dataList, aggregatorInfo.getAggregatorId(), aggregatorResourceType.getId());
                    saveAggregatorDateIssueChartList(dataList, aggregatorInfo.getAggregatorId(), aggregatorResourceType.getId());
                    String date = saveAggregatorIssueProfit(dataList, aggregatorInfo.getAggregatorId(), aggregatorResourceType.getId(), offerMap);
                    if (StringUtils.isNotEmpty(date)) {
                        dealAggregatorAndEntProfit(aggregatorInfo.getAggregatorId(), date);
                    }
                });
                List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList(aggregatorInfo.getAggregatorId());
                //推送刷新
                try {
                    //企业用户推送
                    List<String> codeListByApp = AggregatorRefreshEnum.getCodeByType("app");
                    codeListByApp.forEach(code -> {
                        aggregatorEntList.forEach(ent -> {
                            SendMessageReq req = new SendMessageReq();
                            req.setContent(code);
                            req.setEntId(ent.getEntId());
                            log.info("企业用户出清下发推送消息,{}", JSONObject.toJSONString(req));
                            pushService.sendSocket(req);
                        });
                    });
                } catch (Exception e) {
                    log.info("企业用户出清下发推送消息失败");
                }
                try {
                    //聚合商推送
                    List<String> codeListByPc = AggregatorRefreshEnum.getCodeByType("pc");
                    codeListByPc.forEach(code -> {
                        SendMessageReq req = new SendMessageReq();
                        req.setContent(code);
                        req.setEntId(aggregatorInfo.getAggregatorId());
                        log.info("聚合商出清下发推送消息,{}", JSONObject.toJSONString(req));
                        pushService.sendSocket(req);
                    });
                } catch (Exception e) {
                    log.info("聚合商出清下发推送消息失败");
                }
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWinStatus(List<Map<String, String>> dataList, String aggregatorId, String resourceTypeId) {
        dataList.forEach(data -> {
            String offerAndTime = data.get("IFCE-" + resourceTypeId + "-1");
            if (StringUtils.isNotEmpty(offerAndTime)) {
                Boolean flag = false;
                String now = DateUtils.getTime();
                String[] offerAndTimes = offerAndTime.split(":");
                String date = DateUtils.format(DateUtils.stampToDate(offerAndTimes[1]), "yyyy-MM-dd");
                if (MathUtils.stringToDouble(offerAndTimes[0]).compareTo(1D) == 0) {
                    flag = true;
                }
                AggregatorEntDateApplyDetail aggregatorEntDateApplyDetail = new AggregatorEntDateApplyDetail();
                aggregatorEntDateApplyDetail.setWinStatus(flag);
                aggregatorEntDateApplyDetailService.update(aggregatorEntDateApplyDetail, aggregatorId, Arrays.asList(date));
                AggregatorDateApplyDetail aggregatorDateApplyDetail = new AggregatorDateApplyDetail();
                aggregatorDateApplyDetail.setWinStatus(flag ? "1" : "0");
                aggregatorDateApplyDetail.setWinTime(now);
                aggregatorDateApplyDetail.setUpdateTime(now);
                aggregatorDateApplyDetailService.updateAggregatorDateApplyDetail(aggregatorDateApplyDetail, aggregatorId, date);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Double> saveAggregatorResourceDateIssueOfferList(List<Map<String, String>> dataList, String aggregatorId, String resourceTypeId) {
        Map<String, Double> offerMap = new HashMap<>();
        dataList.forEach(data -> {
            List<AggregatorResourceDateIssueOffer> aggregatorResourceDateIssueOfferList = Lists.newArrayList();
            String date = DateUtils.getNextDay();
            String offerAndTime = data.get("CP-" + resourceTypeId + "-1");
            if (StringUtils.isNotEmpty(offerAndTime)) {
                List<DataResp> dataRespList = Lists.newArrayList();
                data.values().forEach(offerTime -> {
                    String[] offerAndTimes = offerTime.split(":");
                    DataResp dataResp = new DataResp();
                    dataResp.setTime(DateUtils.stampToDate(offerAndTimes[1]));
                    dataResp.setValue(MathUtils.stringToDouble(offerAndTimes[0]));
                    if (null != dataResp && null != dataResp.getValue()) {
                        dataResp.setValue(MathUtils.mulDoubleNull(dataResp.getValue(), 0.001, 8));
                    }
                    dataRespList.add(dataResp);
                });
                String priceChart = JSONObject.toJSONString(dataRespList);
                Double offer = 0D;
                if (CollectionUtils.isNotEmpty(dataRespList)) {
                    List<DataResp> dataRespListSort = dataRespList.stream().sorted(Comparator.comparing(DataResp::getTime)).collect(Collectors.toList());
                    priceChart = JSONObject.toJSONString(dataRespListSort);
                    date = DateUtils.format(dataRespListSort.get(0).getTime(), "yyyy-MM-dd");
                    offer = dataRespListSort.get(0).getValue();
                    Map<String, Double> timeValueMap = dataRespList.stream().filter(resp -> null != resp && StringUtils.isNotEmpty(resp.getTime())).collect(toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1));
                    if (null != timeValueMap && timeValueMap.size() > 0) {
                        offerMap.putAll(timeValueMap);
                    }
                }
                AggregatorResourceDateIssueOffer aggregatorResourceDateIssueOffer = new AggregatorResourceDateIssueOffer();
                aggregatorResourceDateIssueOffer.setAggregatorId(aggregatorId);
                aggregatorResourceDateIssueOffer.setResourceTypeId(resourceTypeId);
                aggregatorResourceDateIssueOffer.setPriceChart(priceChart);
                aggregatorResourceDateIssueOffer.setDate(date);
                aggregatorResourceDateIssueOffer.setOffer(offer);
                aggregatorResourceDateIssueOfferList.add(aggregatorResourceDateIssueOffer);
            }
            if (CollectionUtils.isNotEmpty(aggregatorResourceDateIssueOfferList)) {
                aggregatorResourceDateIssueOfferService.deleteAggregatorResourceDateDeliveryOffer(aggregatorId, date, resourceTypeId);
                aggregatorResourceDateIssueOfferService.batchInsertAggregatorResourceDateDeliveryOffer(aggregatorResourceDateIssueOfferList);
            }
        });
        return offerMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAggregatorDateIssueChartList(List<Map<String, String>> dataList, String aggregatorId, String resourceTypeId) {
        dataList.forEach(data -> {
            String date = DateUtils.getNextDay();
            List<AggregatorDateIssueChart> aggregatorDateIssueChartList = Lists.newArrayList();
            String offerAndTime = data.get("DAP-" + resourceTypeId + "-1");
            if (StringUtils.isNotEmpty(offerAndTime)) {
                List<DataResp> dataRespList = Lists.newArrayList();
                data.values().forEach(offerTime -> {
                    String[] offerAndTimes = offerTime.split(":");
                    DataResp dataResp = new DataResp();
                    dataResp.setTime(DateUtils.stampToDate(offerAndTimes[1]));
                    dataResp.setValue(MathUtils.stringToDouble(offerAndTimes[0]));
                    //数据转换兆瓦
                    if (null != dataResp.getValue()) {
                        dataResp.setValue(MathUtils.mulDoubleNull(dataResp.getValue(), 1000D, 8));
                    }
                    dataRespList.add(dataResp);
                });
                String issueChart = JSONObject.toJSONString(dataRespList);
                if (CollectionUtils.isNotEmpty(dataRespList)) {
                    List<DataResp> dataRespListSort = dataRespList.stream().sorted(Comparator.comparing(DataResp::getTime)).collect(Collectors.toList());
                    issueChart = JSONObject.toJSONString(dataRespListSort);
                    date = DateUtils.format(dataRespListSort.get(0).getTime(), "yyyy-MM-dd");
                }
                AggregatorDateIssueChart aggregatorDateIssueChart = new AggregatorDateIssueChart();
                aggregatorDateIssueChart.setAggregatorId(aggregatorId);
                aggregatorDateIssueChart.setResourceTypeId(resourceTypeId);
                aggregatorDateIssueChart.setDate(date);
                aggregatorDateIssueChart.setIssueChart(issueChart);
                aggregatorDateIssueChartList.add(aggregatorDateIssueChart);
                aggregatorDateIssueChartService.delete(aggregatorId, date, resourceTypeId);
                if (CollectionUtils.isNotEmpty(aggregatorDateIssueChartList)) {
                    aggregatorDateIssueChartService.batchInsert(aggregatorDateIssueChartList);
                    //写入设备下发功率
                    String finalDate = date;
                    executor.execute(() -> {
                        saveAggregatorDeviceDateIssueChartList(finalDate, resourceTypeId, aggregatorDateIssueChartList);
                    });
                }
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveAggregatorIssueProfit(List<Map<String, String>> dataList, String aggregatorId, String resourceTypeId, Map<String, Double> offerMap) {
        AtomicReference<String> date = new AtomicReference<>("");
        dataList.forEach(data -> {
            String offerAndTime = data.get("FEE-" + resourceTypeId + "-1");
            if (StringUtils.isNotEmpty(offerAndTime)) {
                List<DataResp> dataRespList = Lists.newArrayList();
                data.values().forEach(offerTime -> {
                    String[] offerAndTimes = offerTime.split(":");
                    DataResp dataResp = new DataResp();
                    dataResp.setTime(DateUtils.stampToDate(offerAndTimes[1]));
                    dataResp.setValue(MathUtils.stringToDouble(offerAndTimes[0]));
                    dataRespList.add(dataResp);
                });
//                Map<String, Double> electricQuantityMap = new HashMap<>();
//                dataRespList.forEach(dataResp -> {
//                    Double profit = dataResp.getValue();
//                    Double offer = offerMap.get(dataResp.getTime());
//                    Double electricQuantity = MathUtils.divideNull(profit, offer, 8);
//                    electricQuantityMap.put(dataResp.getTime(), electricQuantity);
//                });
                List<DataResp> profitList = dataRespList.stream().sorted(Comparator.comparing(DataResp::getTime)).collect(Collectors.toList());
                date.set(DateUtils.format(profitList.get(0).getTime(), "yyyy-MM-dd"));
                Map<String, Double> timeProfitMap = profitList.stream().collect(toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1));
                List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList = aggregatorDeviceDateProfitService.getAggregatorDeviceDateProfitList(aggregatorId, resourceTypeId, date.get());
                if (null != aggregatorDeviceDateProfitList && aggregatorDeviceDateProfitList.size() > 0) {
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
                        Double powerPercent = MathUtils.divideNullNotRounding(deviceProfit.getCountPower(), totalPower, 8);
                        deviceProfit.setPowerPercent(powerPercent);
                        Double profit = MathUtils.mulDoubleNullNotRounding(totalProfit, powerPercent, 8);
                        deviceProfit.setProfit(profit);
                        Double countPrice = offerMap.get(deviceProfit.getEndTime());
                        deviceProfit.setCountPrice(countPrice);
//                        Double totalElectricQuantity = electricQuantityMap.get(deviceProfit.getEndTime());
//                        Double electricQuantity = MathUtils.mulDoubleNullNotRounding(totalElectricQuantity, powerPercent, 8);
                        Double electricQuantity = MathUtils.divideNullNotRounding(deviceProfit.getProfit(), deviceProfit.getCountPrice(), 8);
                        deviceProfit.setElectricQuantity(electricQuantity);
                    });
                    Map<String, List<AggregatorDeviceDateProfitResp>> deviceMap = aggregatorDeviceDateProfitRespList.stream().collect(groupingBy(AggregatorDeviceDateProfitResp::getDeviceBaseId));
                    aggregatorDeviceDateProfitList.forEach(profit -> {
                        List<AggregatorDeviceDateProfitResp> deviceDateProfitRespList = deviceMap.get(profit.getDeviceBaseId());
                        String profitDetail = JSONObject.toJSONString(deviceDateProfitRespList);
                        profit.setProfitDetail(profitDetail);
                        profit.setProfitDetailByte(GZIPUtil.compressString(profit.getProfitDetail()));
                    });
                    aggregatorDeviceDateProfitService.saveByAggregatorId(aggregatorId, date.get(), resourceTypeId, aggregatorDeviceDateProfitList);
//                    dealAggregatorAndEntProfit(aggregatorId, date);
                }
            }
        });
        return date.get();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dealAggregatorAndEntProfit(String aggregatorId, String date) {
        //查询企业有效时间配置
        Map<String, List<AggregatorEntProfitTime>> entTimeMap = aggregatorEntProfitTimeService.getEntMap(aggregatorId);
        //写入企业收益
        List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList = aggregatorDeviceDateProfitService.getAggregatorDeviceDateProfitList(aggregatorId, date);
        List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = Lists.newArrayList();
        aggregatorDeviceDateProfitList.stream().filter(profit -> null != profit && StringUtils.isNotEmpty(profit.getProfitDetail())).forEach(profit -> {
            List<AggregatorDeviceDateProfitResp> profitRespList = JSONArray.parseArray(profit.getProfitDetail(), AggregatorDeviceDateProfitResp.class);
            aggregatorDeviceDateProfitRespList.addAll(profitRespList);
        });
        List<AggregatorEntDateProfit> aggregatorEntDateProfitList = Lists.newArrayList();
        List<String> entIdList = aggregatorDeviceDateProfitList.stream().map(AggregatorDeviceDateProfit::getEntId).distinct().collect(toList());
        List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList(entIdList);
        Map<String, Double> entIdProfitPercentMap = aggregatorEntList.stream().collect(toMap(AggregatorEnt::getEntId, AggregatorEnt::getPercent));
        Map<String, Double> entIdCountPriceMap = getEntIdCountPriceMap(aggregatorDeviceDateProfitRespList);
        Map<String, Double> entIdElectricQuantityMap = getEntIdElectricQuantityMap(aggregatorDeviceDateProfitRespList, entTimeMap);
        Map<String, Double> entIdDateProfitMap = aggregatorDeviceDateProfitRespList.stream().filter(profit -> null != profit.getProfit()).collect(groupingBy(AggregatorDeviceDateProfitResp::getEntId, summingDouble(AggregatorDeviceDateProfitResp::getProfit)));
        entIdDateProfitMap.entrySet().forEach(entIdDateProfitMapEntry -> {
            AggregatorEntDateProfit aggregatorEntDateProfit = new AggregatorEntDateProfit();
            aggregatorEntDateProfit.setAggregatorId(aggregatorId);
            aggregatorEntDateProfit.setEntId(entIdDateProfitMapEntry.getKey());
            aggregatorEntDateProfit.setDate(date);
            Double entProfitPercent = entIdProfitPercentMap.get(entIdDateProfitMapEntry.getKey());
            Double entProfit = MathUtils.mulDoubleNull(entIdDateProfitMapEntry.getValue(), entProfitPercent, 8);
            aggregatorEntDateProfit.setEntProfit(null == entProfit ? 0 : MathUtils.doublePoint(entProfit, 8));
            aggregatorEntDateProfit.setElectricQuantity(null == entIdElectricQuantityMap.get(entIdDateProfitMapEntry.getKey()) ? 0 : MathUtils.doublePoint(entIdElectricQuantityMap.get(entIdDateProfitMapEntry.getKey()), 8));
            aggregatorEntDateProfit.setAveragePrice(entIdCountPriceMap.get(entIdDateProfitMapEntry.getKey()));
            aggregatorEntDateProfit.setCountProfit(entIdDateProfitMapEntry.getValue());
            aggregatorEntDateProfit.setCountPrice(MathUtils.divideZero(aggregatorEntDateProfit.getCountProfit(), aggregatorEntDateProfit.getElectricQuantity(), 8));
            aggregatorEntDateProfitList.add(aggregatorEntDateProfit);
        });
        aggregatorEntDateProfitService.saveByAggregatorId(aggregatorId, date, aggregatorEntDateProfitList);
        //写入聚合商收益
        Double totalIssueProfit = aggregatorEntDateProfitList.stream().filter(profit -> null != profit && null != profit.getCountProfit()).collect(summingDouble(AggregatorEntDateProfit::getCountProfit));
        Double totalEntProfit = aggregatorEntDateProfitList.stream().filter(profit -> null != profit && null != profit.getEntProfit()).collect(summingDouble(AggregatorEntDateProfit::getEntProfit));
        Double totalAggregatorProfit = MathUtils.subDouble(totalIssueProfit, totalEntProfit);
        Double totalElectricQuantity = aggregatorEntDateProfitList.stream().filter(profit -> null != profit && null != profit.getElectricQuantity()).collect(summingDouble(AggregatorEntDateProfit::getElectricQuantity));
        AggregatorDateProfit aggregatorDateProfit = new AggregatorDateProfit();
        aggregatorDateProfit.setAggregatorId(aggregatorId);
        aggregatorDateProfit.setDate(date);
        aggregatorDateProfit.setIssueProfit(null == totalIssueProfit ? 0 : MathUtils.doublePoint(totalIssueProfit, 8));
        aggregatorDateProfit.setAggregatorProfit(null == totalAggregatorProfit ? 0 : MathUtils.doublePoint(totalAggregatorProfit, 8));
        aggregatorDateProfit.setEntProfit(null == totalEntProfit ? 0 : MathUtils.doublePoint(totalEntProfit, 8));
        aggregatorDateProfit.setElectricQuantity(null == totalElectricQuantity ? 0 : MathUtils.doublePoint(totalElectricQuantity, 8));
        aggregatorDateProfitService.save(aggregatorId, date, Arrays.asList(aggregatorDateProfit));
    }

    @Override
    public void dealAggregatorEntDateProfitOffer(String aggregatorId, String date) {
        List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList = aggregatorDeviceDateProfitService.getAggregatorDeviceDateProfitList(aggregatorId, date);
        List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = Lists.newArrayList();
        aggregatorDeviceDateProfitList.stream().filter(profit -> null != profit && StringUtils.isNotEmpty(profit.getProfitDetail())).forEach(profit -> {
            List<AggregatorDeviceDateProfitResp> profitRespList = JSONArray.parseArray(profit.getProfitDetail(), AggregatorDeviceDateProfitResp.class);
            aggregatorDeviceDateProfitRespList.addAll(profitRespList);
        });
        Map<String, Double> entIdCountPriceMap = getEntIdCountPriceMap(aggregatorDeviceDateProfitRespList);
        entIdCountPriceMap.entrySet().forEach(entIdCountPriceMapEntry -> {
            AggregatorEntDateProfit aggregatorEntDateProfit = new AggregatorEntDateProfit();
            aggregatorEntDateProfit.setAveragePrice(entIdCountPriceMap.get(entIdCountPriceMapEntry.getKey()));
            aggregatorEntDateProfitService.update(aggregatorEntDateProfit, entIdCountPriceMapEntry.getKey(), date);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAggregatorDeviceDateIssueChartList(String date, String resourceTypeId, List<AggregatorDateIssueChart> aggregatorDateIssueChartList) {
//        List<AggregatorDeviceDeliveryPowerPercent> aggregatorDeviceDeliveryPowerPercentList = aggregatorDeviceDeliveryPowerPercentService.getAggregatorDeviceDeliveryPowerPercentList(resourceTypeId, date);
//        if (CollectionUtils.isNotEmpty(aggregatorDeviceDeliveryPowerPercentList)) {
//            List<AggregatorDeviceDeliveryPowerPercentDetail> aggregatorDeviceDeliveryPowerPercentDetailList = Lists.newArrayList();
//            aggregatorDeviceDeliveryPowerPercentList.stream().filter(aggregatorDeviceDeliveryPowerPercent -> null != aggregatorDeviceDeliveryPowerPercent && StringUtils.isNotEmpty(aggregatorDeviceDeliveryPowerPercent.getDetail())).forEach(aggregatorDeviceDeliveryPowerPercent -> {
//                List<AggregatorDeviceDeliveryPowerPercentDetail> detailList = JSONArray.parseArray(aggregatorDeviceDeliveryPowerPercent.getDetail(), AggregatorDeviceDeliveryPowerPercentDetail.class);
//                if (CollectionUtils.isNotEmpty(detailList)) {
//                    aggregatorDeviceDeliveryPowerPercentDetailList.addAll(detailList);
//                }
//            });
//            if (CollectionUtils.isNotEmpty(aggregatorDeviceDeliveryPowerPercentDetailList)) {
//                Map<String, AggregatorDeviceDeliveryPowerPercentDetail> deviceBaseIdInfoMap = aggregatorDeviceDeliveryPowerPercentDetailList.stream().collect(toMap(AggregatorDeviceDeliveryPowerPercentDetail::getDeviceBaseId, Function.identity(), (v1, v2) -> v1));
//                if (null == deviceBaseIdInfoMap || deviceBaseIdInfoMap.size() <= 0) {
//                    deviceBaseIdInfoMap = new HashMap<>();
//                }
//                Map<String, Map<String, Map<String, Double>>> resourceTypeMap = aggregatorDeviceDeliveryPowerPercentDetailList
//                        .stream().collect(groupingBy(AggregatorDeviceDeliveryPowerPercentDetail::getResourceTypeId,
//                                groupingBy(aggregatorDeviceDeliveryPowerPercentDetail -> DateUtils.format(aggregatorDeviceDeliveryPowerPercentDetail.getTime(), "HH:mm"),
//                                        toMap(AggregatorDeviceDeliveryPowerPercentDetail::getDeviceBaseId, AggregatorDeviceDeliveryPowerPercentDetail::getPercent, (v1, v2) -> v1))));
//                if (null == resourceTypeMap || resourceTypeMap.size() <= 0) {
//                    resourceTypeMap = new HashMap<>();
//                }
//                Map<String, Map<String, Map<String, Double>>> finalResourceTypeMap = resourceTypeMap;
//                Map<String, List<DataResp>> deviceIssueChartMap = new HashMap<>();
//                aggregatorDateIssueChartList.stream().filter(aggregatorDateIssueChart -> null != aggregatorDateIssueChart && StringUtils.isNotEmpty(aggregatorDateIssueChart.getIssueChart())).forEach(aggregatorDateIssueChart -> {
//                    Map<String, Map<String, Double>> timeMap = finalResourceTypeMap.get(aggregatorDateIssueChart.getResourceTypeId());
//                    if (null == timeMap || timeMap.size() <= 0) {
//                        timeMap = new HashMap<>();
//                    }
//                    Map<String, Map<String, Double>> finalTimeMap = timeMap;
//                    List<DataResp> dataRespList = JSONArray.parseArray(aggregatorDateIssueChart.getIssueChart(), DataResp.class);
//                    dataRespList.forEach(dataResp -> {
//                        Map<String, Double> deviceBaseIdMap = finalTimeMap.get(DateUtils.format(dataResp.getTime(), "HH:mm"));
//                        if (null == deviceBaseIdMap || deviceBaseIdMap.size() <= 0) {
//                            deviceBaseIdMap = new HashMap<>();
//                        }
//                        deviceBaseIdMap.entrySet().forEach(deviceBaseIdMapEntry -> {
//                            List<DataResp> timeValueList = deviceIssueChartMap.get(deviceBaseIdMapEntry.getKey());
//                            if (null == timeValueList) {
//                                timeValueList = Lists.newArrayList();
//                            }
//                            DataResp timeValue = new DataResp();
//                            timeValue.setTime(dataResp.getTime());
//                            timeValue.setValue(MathUtils.mulDoubleNull(dataResp.getValue(), deviceBaseIdMapEntry.getValue(), 2));
//                            timeValueList.add(timeValue);
//                            deviceIssueChartMap.put(deviceBaseIdMapEntry.getKey(), timeValueList);
//                        });
//                    });
//                });
//                List<AggregatorDeviceDateIssueChart> aggregatorDeviceDateIssueChartList = Lists.newArrayList();
//                if (null != deviceIssueChartMap && deviceIssueChartMap.size() > 0) {
//                    Map<String, AggregatorDeviceDeliveryPowerPercentDetail> finalDeviceBaseIdInfoMap = deviceBaseIdInfoMap;
//                    deviceIssueChartMap.entrySet().forEach(deviceIssueChartMapEntry -> {
//                        AggregatorDeviceDeliveryPowerPercentDetail aggregatorDeviceDeliveryPowerPercentDetail = finalDeviceBaseIdInfoMap.get(deviceIssueChartMapEntry.getKey());
//                        if (null != aggregatorDeviceDeliveryPowerPercentDetail) {
//                            AggregatorDeviceDateIssueChart aggregatorDeviceDateIssueChart = new AggregatorDeviceDateIssueChart();
//                            aggregatorDeviceDateIssueChart.setAggregatorId(aggregatorDeviceDeliveryPowerPercentDetail.getAggregatorId());
//                            aggregatorDeviceDateIssueChart.setEntId(aggregatorDeviceDeliveryPowerPercentDetail.getEntId());
//                            aggregatorDeviceDateIssueChart.setStationId(aggregatorDeviceDeliveryPowerPercentDetail.getStationId());
//                            aggregatorDeviceDateIssueChart.setResourceTypeId(aggregatorDeviceDeliveryPowerPercentDetail.getResourceTypeId());
//                            aggregatorDeviceDateIssueChart.setDeviceBaseId(aggregatorDeviceDeliveryPowerPercentDetail.getDeviceBaseId());
//                            aggregatorDeviceDateIssueChart.setDate(aggregatorDeviceDeliveryPowerPercentDetail.getDate());
//                            if (CollectionUtils.isNotEmpty(deviceIssueChartMapEntry.getValue())) {
//                                List<DataResp> sortList = deviceIssueChartMapEntry.getValue().stream().sorted(Comparator.comparing(DataResp::getTime)).collect(toList());
//                                aggregatorDeviceDateIssueChart.setIssueChart(JSONObject.toJSONString(sortList));
//                            }
//                            aggregatorDeviceDateIssueChartList.add(aggregatorDeviceDateIssueChart);
//                        }
//                    });
//                }
//                if (CollectionUtils.isNotEmpty(aggregatorDeviceDateIssueChartList)) {
//                    aggregatorDeviceDateIssueChartService.batchInsert(aggregatorDeviceDateIssueChartList);
//                }
//            }
//        }
        List<AggregatorDeviceDateIssueChart> aggregatorDeviceDateIssueChartList = Lists.newArrayList();
        List<AggregatorDeviceDateDeliveryChart> aggregatorDeviceDateDeliveryChartList = aggregatorDeviceDateDeliveryChartService.getAggregatorDeviceDateDeliveryChartListByResourceTypeId(resourceTypeId, date);
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDateDeliveryChartList)) {
            aggregatorDeviceDateDeliveryChartList.forEach(aggregatorDeviceDateDeliveryChart -> {
                AggregatorDeviceDateIssueChart aggregatorDeviceDateIssueChart = new AggregatorDeviceDateIssueChart();
                BeanUtils.copyProperties(aggregatorDeviceDateDeliveryChart, aggregatorDeviceDateIssueChart);
                aggregatorDeviceDateIssueChart.setIssueChart(aggregatorDeviceDateDeliveryChart.getDeliveryChart());
                aggregatorDeviceDateIssueChartList.add(aggregatorDeviceDateIssueChart);
            });
        }
        aggregatorDeviceDateIssueChartService.deleteByResourceTypeId(resourceTypeId, date);
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDateIssueChartList)) {
            aggregatorDeviceDateIssueChartService.batchInsert(aggregatorDeviceDateIssueChartList);
        }
    }

    @Override
    public AggregatorProfitResp getAggregatorProfitResp(String startDate, String endDate) {
        List<AggregatorInfo> aggregatorInfoList = aggregatorInfoService.getAggregatorInfoList();
        if (CollectionUtils.isNotEmpty(aggregatorInfoList)) {
            return aggregatorDateProfitService.getAggregatorProfitRespTotal(aggregatorInfoList.get(0).getAggregatorId(), startDate, endDate);
        }
        return new AggregatorProfitResp();
    }

    @Override
    public Map<String, AggregatorEntProfitResp> getEntProfitRespMap(String startDate, String endDate) {
        List<AggregatorInfo> aggregatorInfoList = aggregatorInfoService.getAggregatorInfoList();
        if (CollectionUtils.isNotEmpty(aggregatorInfoList)) {
            List<AggregatorEntDateProfit> aggregatorEntDateProfitList = aggregatorEntDateProfitService.getAggregatorEntDateProfitList(aggregatorInfoList.get(0).getAggregatorId(), startDate, endDate);
            if (CollectionUtils.isNotEmpty(aggregatorEntDateProfitList)) {
                List<AggregatorEntProfitResp> aggregatorEntProfitRespList = Lists.newArrayList();
                aggregatorEntDateProfitList.stream().filter(aggregatorEntDateProfit -> null != aggregatorEntDateProfit).forEach(aggregatorEntDateProfit -> {
                    AggregatorEntProfitResp aggregatorEntProfitResp = new AggregatorEntProfitResp();
                    aggregatorEntProfitResp.setEntId(aggregatorEntDateProfit.getEntId());
                    aggregatorEntProfitResp.setElectricQuantity(aggregatorEntDateProfit.getElectricQuantity());
                    aggregatorEntProfitResp.setEntProfit(aggregatorEntDateProfit.getEntProfit());
                    aggregatorEntProfitRespList.add(aggregatorEntProfitResp);
                });
                return aggregatorEntProfitRespList.stream().collect(toMap(AggregatorEntProfitResp::getEntId, Function.identity(), (v1, v2) -> {
                    v1.setElectricQuantity(MathUtils.addDouble(v1.getElectricQuantity(), v2.getElectricQuantity(), 2));
                    if (null == v1.getElectricQuantity()) {
                        v1.setElectricQuantity(0D);
                    }
                    v1.setEntProfit(MathUtils.addDouble(v1.getEntProfit(), v2.getEntProfit(), 2));
                    if (null == v1.getEntProfit()) {
                        v1.setEntProfit(0D);
                    }
                    return v1;
                }));
            }
        }
        return new HashMap<>();
    }

    @Override
    public Map<String, Double> getElectricQuantity(String startTime, String endTime) {
        String date = DateUtils.getDay(startTime);
        List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList = aggregatorDeviceDateProfitService.getAggregatorDeviceDateProfitList(date);
        if (null != aggregatorDeviceDateProfitList && aggregatorDeviceDateProfitList.size() > 0) {
            List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = Lists.newArrayList();
            aggregatorDeviceDateProfitList.forEach(aggregatorDeviceDateProfit -> {
                if (StringUtils.isNotEmpty(aggregatorDeviceDateProfit.getProfitDetail())) {
                    List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitResps = JSONArray.parseArray(aggregatorDeviceDateProfit.getProfitDetail(), AggregatorDeviceDateProfitResp.class);
                    if (null != aggregatorDeviceDateProfitResps && aggregatorDeviceDateProfitResps.size() > 0) {
                        aggregatorDeviceDateProfitRespList.addAll(aggregatorDeviceDateProfitResps);
                    }
                }
            });
            Map<String, Double> entIdElectricQuantityMap = aggregatorDeviceDateProfitRespList
                    .stream()
                    .filter(profit -> null != profit.getElectricQuantity() && profit.getEndTime().compareTo(startTime) >= 0 && profit.getEndTime().compareTo(endTime) <= 0)
                    .collect(groupingBy(AggregatorDeviceDateProfitResp::getEntId, summingDouble(AggregatorDeviceDateProfitResp::getElectricQuantity)));
            AtomicDouble result = new AtomicDouble(0);
            entIdElectricQuantityMap.entrySet().forEach(entIdElectricQuantityMapEntry -> {
                result.set(MathUtils.addDouble(result.get(), entIdElectricQuantityMapEntry.getValue(), 2));
            });
            entIdElectricQuantityMap.put("total", result.get());
            return entIdElectricQuantityMap;
        }
        return null;
    }

    @Override
    public Map<String, LinkedHashMap<String, Double>> getElectricQuantityV1(String aggregatorId, String date) {
        Map<String, String> entMap = new HashMap<>();
        List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList(aggregatorId);
        if (CollectionUtils.isNotEmpty(aggregatorEntList)) {
            entMap.putAll(aggregatorEntList.stream().collect(toMap(AggregatorEnt::getEntId, AggregatorEnt::getEntName)));
        }
        List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = Lists.newArrayList();
        Map<String, List<AggregatorEntProfitTime>> entTimeMap = aggregatorEntProfitTimeService.getEntMap(aggregatorId);
        List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespListDeal = Lists.newArrayList();
        //计算实时数据
        List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespListByToday = dealDevicePowerAndQuantity(date);
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDateProfitRespListByToday)) {
            aggregatorDeviceDateProfitRespList.addAll(aggregatorDeviceDateProfitRespListByToday);
        }
        aggregatorDeviceDateProfitRespList.forEach(aggregatorDeviceDateProfitResp -> {
            List<AggregatorEntProfitTime> aggregatorEntProfitTimeList = entTimeMap.get(aggregatorDeviceDateProfitResp.getEntId());
            if (CollectionUtils.isNotEmpty(aggregatorEntProfitTimeList)) {
                aggregatorEntProfitTimeList.forEach(time -> {
                    if (DateUtils.format(aggregatorDeviceDateProfitResp.getEndTime(), "HH:mm").compareTo(time.getStartTime()) >= 0 && DateUtils.format(aggregatorDeviceDateProfitResp.getEndTime(), "HH:mm").compareTo(time.getEndTime()) <= 0) {
                        aggregatorDeviceDateProfitRespListDeal.add(aggregatorDeviceDateProfitResp);
                    }
                });
            } else {
                aggregatorDeviceDateProfitRespListDeal.add(aggregatorDeviceDateProfitResp);
            }
        });
        Map<String, LinkedHashMap<String, Double>> entIdElectricQuantityMap = aggregatorDeviceDateProfitRespListDeal
                .stream().collect(
                        groupingBy(AggregatorDeviceDateProfitResp::getEntId,
                                groupingBy(AggregatorDeviceDateProfitResp::getDate, LinkedHashMap::new,
                                        summingDouble(profitResp -> MathUtils.doublePoint(profitResp.getElectricQuantity(), 2)))));
        AtomicDouble result = new AtomicDouble(0);
        Map<String, LinkedHashMap<String, Double>> resultMap = new HashMap<>();
        entIdElectricQuantityMap.entrySet().forEach(entIdElectricQuantityMapEntry -> {
            String entName = entMap.get(entIdElectricQuantityMapEntry.getKey());
            entIdElectricQuantityMapEntry.getValue().entrySet().forEach(entIdElectricQuantityDateMapEntry -> {
                entIdElectricQuantityDateMapEntry.setValue(MathUtils.doublePoint(entIdElectricQuantityDateMapEntry.getValue(), 2));
                result.set(MathUtils.addDouble(result.get(), entIdElectricQuantityDateMapEntry.getValue(), 2));
            });
            if (StringUtils.isEmpty(entName)) {
                entName = entIdElectricQuantityMapEntry.getKey();
            }
            resultMap.put(entName, entIdElectricQuantityMapEntry.getValue());
        });
        LinkedHashMap<String, Double> total = new LinkedHashMap<>();
        total.put("total", result.get());
        resultMap.put("total", total);
        return resultMap;
    }

    @Override
    public Map<String, LinkedHashMap<String, Double>> getElectricQuantityV2(String aggregatorId, String startDate, String endDate) {
        List<String> dayList = DateUtils.getDayList(startDate, endDate);
        Map<String, String> entMap = new HashMap<>();
        List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList(aggregatorId);
        if (CollectionUtils.isNotEmpty(aggregatorEntList)) {
            entMap.putAll(aggregatorEntList.stream().collect(toMap(AggregatorEnt::getEntId, AggregatorEnt::getEntName)));
        }
        List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = Lists.newArrayList();
        List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList = aggregatorDeviceDateProfitService.getAggregatorDeviceDateProfitList(dayList);
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDateProfitList)) {
            aggregatorDeviceDateProfitList.forEach(aggregatorDeviceDateProfit -> {
                if (StringUtils.isNotEmpty(aggregatorDeviceDateProfit.getProfitDetail())) {
                    List<AggregatorDeviceDateProfitResp> profitDetailList = JSONArray.parseArray(aggregatorDeviceDateProfit.getProfitDetail(), AggregatorDeviceDateProfitResp.class);
                    if (CollectionUtils.isNotEmpty(profitDetailList)) {
                        aggregatorDeviceDateProfitRespList.addAll(profitDetailList);
                    }
                }
            });
        }
        Map<String, List<AggregatorEntProfitTime>> entTimeMap = aggregatorEntProfitTimeService.getEntMap(aggregatorId);
        List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespListDeal = Lists.newArrayList();
        //计算当天数据
        if (DateUtils.getDay().equals(endDate)) {
            List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespListByToday = dealDevicePowerAndQuantity(endDate);
            if (CollectionUtils.isNotEmpty(aggregatorDeviceDateProfitRespListByToday)) {
                aggregatorDeviceDateProfitRespList.addAll(aggregatorDeviceDateProfitRespListByToday);
            }
        }
        //查询已计算数据
        aggregatorDeviceDateProfitRespList.forEach(aggregatorDeviceDateProfitResp -> {
            List<AggregatorEntProfitTime> aggregatorEntProfitTimeList = entTimeMap.get(aggregatorDeviceDateProfitResp.getEntId());
            if (CollectionUtils.isNotEmpty(aggregatorEntProfitTimeList)) {
                aggregatorEntProfitTimeList.forEach(time -> {
                    if (DateUtils.format(aggregatorDeviceDateProfitResp.getEndTime(), "HH:mm").compareTo(time.getStartTime()) >= 0 && DateUtils.format(aggregatorDeviceDateProfitResp.getEndTime(), "HH:mm").compareTo(time.getEndTime()) <= 0) {
                        aggregatorDeviceDateProfitRespListDeal.add(aggregatorDeviceDateProfitResp);
                    }
                });
            } else {
                aggregatorDeviceDateProfitRespListDeal.add(aggregatorDeviceDateProfitResp);
            }
        });
        Map<String, LinkedHashMap<String, Double>> entIdElectricQuantityMap = aggregatorDeviceDateProfitRespListDeal
                .stream().collect(
                        groupingBy(AggregatorDeviceDateProfitResp::getEntId,
                                groupingBy(AggregatorDeviceDateProfitResp::getDate, LinkedHashMap::new,
                                        summingDouble(AggregatorDeviceDateProfitResp::getElectricQuantity))));
        AtomicDouble result = new AtomicDouble(0);
        Map<String, LinkedHashMap<String, Double>> resultMap = new HashMap<>();
        entIdElectricQuantityMap.entrySet().forEach(entIdElectricQuantityMapEntry -> {
            String entName = entMap.get(entIdElectricQuantityMapEntry.getKey());
            entIdElectricQuantityMapEntry.getValue().entrySet().forEach(entIdElectricQuantityDateMapEntry -> {
                entIdElectricQuantityDateMapEntry.setValue(MathUtils.doublePoint(entIdElectricQuantityDateMapEntry.getValue(), 2));
                result.set(MathUtils.addDouble(result.get(), entIdElectricQuantityDateMapEntry.getValue(), 2));
            });
            if (StringUtils.isEmpty(entName)) {
                entName = entIdElectricQuantityMapEntry.getKey();
            }
            resultMap.put(entName, entIdElectricQuantityMapEntry.getValue());
        });
        LinkedHashMap<String, Double> total = new LinkedHashMap<>();
        total.put("total", result.get());
        resultMap.put("total", total);
        return resultMap;
    }

    @Override
    public Boolean dealEntElectricQuantity(String date) {
        if (StringUtils.isEmpty(date)) {
            date = DateUtils.getDay();
        }
        List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = Lists.newArrayList();
        List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList = aggregatorDeviceDateProfitService.getAggregatorDeviceDateProfitList(date);
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDateProfitList)) {
            aggregatorDeviceDateProfitList.forEach(aggregatorDeviceDateProfit -> {
                if (StringUtils.isNotEmpty(aggregatorDeviceDateProfit.getProfitDetail())) {
                    List<AggregatorDeviceDateProfitResp> profitDetailList = JSONArray.parseArray(aggregatorDeviceDateProfit.getProfitDetail(), AggregatorDeviceDateProfitResp.class);
                    if (CollectionUtils.isNotEmpty(profitDetailList)) {
                        aggregatorDeviceDateProfitRespList.addAll(profitDetailList);
                    }
                }
            });
        }
        //查询企业有效时间配置
        Map<String, List<AggregatorEntProfitTime>> entTimeMap = aggregatorEntProfitTimeService.getEntMap();
        Map<String, Double> entIdElectricQuantityMap = getEntIdElectricQuantityMap(aggregatorDeviceDateProfitRespList, entTimeMap);
        //写入企业用电量
        List<AggregatorEntDateProfit> aggregatorEntDateProfitList = Lists.newArrayList();
        List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList();
        String finalDate = date;
        aggregatorEntList.forEach(ent -> {
            AggregatorEntDateProfit aggregatorEntDateProfit = new AggregatorEntDateProfit();
            aggregatorEntDateProfit.setAggregatorId(ent.getAggregatorId());
            aggregatorEntDateProfit.setEntId(ent.getEntId());
            aggregatorEntDateProfit.setDate(finalDate);
            aggregatorEntDateProfit.setElectricQuantity(null == entIdElectricQuantityMap.get(ent.getEntId()) ? 0 : MathUtils.doublePoint(entIdElectricQuantityMap.get(ent.getEntId()), 8));
            aggregatorEntDateProfitList.add(aggregatorEntDateProfit);
        });
        if (CollectionUtils.isNotEmpty(aggregatorEntDateProfitList)) {
            aggregatorEntDateProfitService.save(aggregatorEntDateProfitList);
        }
        return true;
    }

    /**
     * 实时计算用电量
     *
     * @param date
     * @return
     */
    @Override
    public List<AggregatorDeviceDateProfitResp> dealDevicePowerAndQuantity(String date) {
        List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = Lists.newArrayList();
        String startTime = date + " 00:00:00";
        String endTime = DateUtils.getAddDate(date) + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime);
        List<String> minuteListWith15 = DateUtils.getMinuteList(startTime, endTime, 15);
        //查询设备信息
        List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList();
        List<String> deviceBaseIdList = aggregatorEntDeviceList.stream().map(AggregatorEntDevice::getDeviceBaseId).collect(toList());
        //查询实时功率
        Map<String, Map<String, Double>> realTimeAvgPowerMap = getRealTimeAvgPower(aggregatorEntDeviceList, startTime, endTime, minuteList);
        //查询基线负荷
        Map<String, Map<String, Double>> baseLinePowerMap = getBaseLinePowerMap(deviceBaseIdList, date);
        //设备功率计算
        String finalDate = date;
        aggregatorEntDeviceList.forEach(device -> {
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
                Map<String, Double> realTimeMap = realTimeAvgPowerMap.get(device.getDeviceBaseId());
                if (null != realTimeMap && realTimeMap.size() > 0) {
                    aggregatorDeviceDateProfitResp.setReallyPower(realTimeMap.get(minuteNext));
                }
                aggregatorDeviceDateProfitResp.setMinPower(aggregatorDeviceDateProfitResp.getReallyPower());
                Map<String, Double> baseLineTimeMap = baseLinePowerMap.get(device.getDeviceBaseId());
                if (null != baseLineTimeMap && baseLineTimeMap.size() > 0) {
                    aggregatorDeviceDateProfitResp.setBaseLinePower(baseLineTimeMap.get(DateUtils.format(minuteNext, "HH:mm")));
                }
                Double subDouble = null;
                if (device.getResourceTypeId().equals("27")) {
                    if (null != aggregatorDeviceDateProfitResp.getMinPower() && aggregatorDeviceDateProfitResp.getMinPower() < 0) {
                        if (null != aggregatorDeviceDateProfitResp.getBaseLinePower() && aggregatorDeviceDateProfitResp.getBaseLinePower() < 0) {
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
                } else {
                    subDouble = MathUtils.subDoubleABS(aggregatorDeviceDateProfitResp.getMinPower(), aggregatorDeviceDateProfitResp.getBaseLinePower());
                    if (null == subDouble || subDouble.compareTo(0D) < 0) {
                        subDouble = 0D;
                    }
                }
                //1分钟功率 乘以 15 分钟 除以 60 分钟
                aggregatorDeviceDateProfitResp.setElectricQuantity(MathUtils.mulDoubleNull(subDouble, 0.25D, 8));
                aggregatorDeviceDateProfitRespList.add(aggregatorDeviceDateProfitResp);
            }
        });
        return aggregatorDeviceDateProfitRespList;
    }

    /**
     * 处理有效用电量
     *
     * @param aggregatorDeviceDateProfitRespList
     * @param entTimeMap
     * @return
     */
    @Override
    public Map<String, Double> getEntIdElectricQuantityMap(List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList, Map<String, List<AggregatorEntProfitTime>> entTimeMap) {
        if (null == entTimeMap) {
            return aggregatorDeviceDateProfitRespList.stream().collect(groupingBy(AggregatorDeviceDateProfitResp::getEntId, summingDouble(deviceProfitResp -> null == deviceProfitResp.getElectricQuantity() ? 0 : deviceProfitResp.getElectricQuantity())));
        } else {
            List<AggregatorDeviceDateProfitResp> resultList = Lists.newArrayList();
            aggregatorDeviceDateProfitRespList.stream().forEach(profit -> {
                if (null != profit) {
                    List<AggregatorEntProfitTime> aggregatorEntProfitTimeList = entTimeMap.get(profit.getEntId());
                    if (CollectionUtils.isNotEmpty(aggregatorEntProfitTimeList)) {
                        aggregatorEntProfitTimeList.forEach(time -> {
                            if (DateUtils.format(profit.getEndTime(), "HH:mm").compareTo(time.getStartTime()) >= 0 && DateUtils.format(profit.getEndTime(), "HH:mm").compareTo(time.getEndTime()) <= 0) {
                                resultList.add(profit);
                            }
                        });
                    } else {
                        resultList.add(profit);
                    }
                }
            });
            return resultList.stream().collect(groupingBy(AggregatorDeviceDateProfitResp::getEntId, summingDouble(deviceProfitResp -> null == deviceProfitResp.getElectricQuantity() ? 0 : deviceProfitResp.getElectricQuantity())));
        }
    }

    @Override
    public List<AggregatorDeviceDateProfitResp> dealDevicePowerAndQuantity(String entId, String date) {
        List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = Lists.newArrayList();
        String startTime = date + " 00:00:00";
        String endTime = DateUtils.getAddDate(date) + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime);
        List<String> minuteListWith15 = DateUtils.getMinuteList(startTime, endTime, 15);
        //查询设备信息
        List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(entId);
        if (CollectionUtils.isEmpty(aggregatorEntDeviceList)) {
            return aggregatorDeviceDateProfitRespList;
        }
        List<String> deviceBaseIdList = aggregatorEntDeviceList.stream().map(AggregatorEntDevice::getDeviceBaseId).collect(toList());
        //查询实时功率
        Map<String, Map<String, Double>> realTimeAvgPowerMap = getRealTimeAvgPower(aggregatorEntDeviceList, startTime, endTime, minuteList);
        //查询基线负荷
        Map<String, Map<String, Double>> baseLinePowerMap = getBaseLinePowerMap(deviceBaseIdList, date);
        //设备功率计算
        String finalDate = date;
        aggregatorEntDeviceList.forEach(device -> {
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
                Map<String, Double> realTimeMap = realTimeAvgPowerMap.get(device.getDeviceBaseId());
                if (null != realTimeMap && realTimeMap.size() > 0) {
                    aggregatorDeviceDateProfitResp.setReallyPower(realTimeMap.get(minuteNext));
                }
                aggregatorDeviceDateProfitResp.setMinPower(aggregatorDeviceDateProfitResp.getReallyPower());
                Map<String, Double> baseLineTimeMap = baseLinePowerMap.get(device.getDeviceBaseId());
                if (null != baseLineTimeMap && baseLineTimeMap.size() > 0) {
                    aggregatorDeviceDateProfitResp.setBaseLinePower(baseLineTimeMap.get(DateUtils.format(minuteNext, "HH:mm")));
                }
                Double subDouble = null;
                if (device.getResourceTypeId().equals("27")) {
                    if (null != aggregatorDeviceDateProfitResp.getMinPower() && aggregatorDeviceDateProfitResp.getMinPower() < 0) {
                        if (null != aggregatorDeviceDateProfitResp.getBaseLinePower() && aggregatorDeviceDateProfitResp.getBaseLinePower() < 0) {
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
                } else {
                    subDouble = MathUtils.subDoubleABS(aggregatorDeviceDateProfitResp.getMinPower(), aggregatorDeviceDateProfitResp.getBaseLinePower());
                    if (null == subDouble || subDouble.compareTo(0D) < 0) {
                        subDouble = 0D;
                    }
                }
                //1分钟功率 乘以 15 分钟 除以 60 分钟
                aggregatorDeviceDateProfitResp.setElectricQuantity(MathUtils.mulDoubleNull(subDouble, 0.25D, 8));
                aggregatorDeviceDateProfitRespList.add(aggregatorDeviceDateProfitResp);
            }
        });
        return aggregatorDeviceDateProfitRespList;
    }

    @Override
    public Boolean dealDeviceIssuePower(String deviceBaseId, String date) {
        List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList = aggregatorDeviceDateProfitService.getAggregatorDeviceDateProfitListByDeviceBaseIdAndDate(deviceBaseId, date);
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDateProfitList)) {
            List<String> deviceBaseIdList = aggregatorDeviceDateProfitList.stream().map(AggregatorDeviceDateProfit::getDeviceBaseId).collect(toList());
            List<String> dateList = aggregatorDeviceDateProfitList.stream().map(AggregatorDeviceDateProfit::getDate).collect(toList());
            List<AggregatorDeviceDateIssueChart> aggregatorDeviceDateIssueChartList = aggregatorDeviceDateIssueChartService.getAggregatorDeviceDateIssueChartList(deviceBaseIdList, dateList);
            if (CollectionUtils.isNotEmpty(aggregatorDeviceDateIssueChartList)) {
                Map<String, Double> issuePowerMap = getIssuePowerMap(aggregatorDeviceDateIssueChartList);
                List<AggregatorDeviceDateProfit> updateList = Lists.newArrayList();
                aggregatorDeviceDateProfitList.stream().filter(profit -> null != profit && StringUtils.isNotEmpty(profit.getProfitDetail())).forEach(profit -> {
                    List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = JSONArray.parseArray(profit.getProfitDetail(), AggregatorDeviceDateProfitResp.class);
                    aggregatorDeviceDateProfitRespList.forEach(profitResp -> {
                        String issuePowerMapKey = profitResp.getDeviceBaseId() + "&" + profitResp.getEndTime();
                        profitResp.setIssuePower(issuePowerMap.get(issuePowerMapKey));
                    });
                    String profitDetail = JSONObject.toJSONString(aggregatorDeviceDateProfitRespList);
                    profit.setProfitDetail(profitDetail);
                    updateList.add(profit);
                });
                if (CollectionUtils.isNotEmpty(updateList)) {
                    aggregatorDeviceDateProfitService.updateListById(updateList);
                }
            }
        }
        return true;
    }

    @Override
    public Boolean dealDeviceEstimatePower(String deviceBaseId, String date) {
        List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList = aggregatorDeviceDateProfitService.getAggregatorDeviceDateProfitListByDeviceBaseIdAndDate(deviceBaseId, date);
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDateProfitList)) {
            List<AggregatorDeviceDateProfit> updateList = Lists.newArrayList();
            aggregatorDeviceDateProfitList.stream().filter(profit -> null != profit && StringUtils.isNotEmpty(profit.getProfitDetail())).forEach(profit -> {
                List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = JSONArray.parseArray(profit.getProfitDetail(), AggregatorDeviceDateProfitResp.class);
                aggregatorDeviceDateProfitRespList.forEach(profitResp -> {
                    Double estimateSubDouble = null;
                    if (profitResp.getResourceTypeId().equals("27")) {
                        if (null != profitResp.getDeliveryPower() && profitResp.getDeliveryPower() < 0) {
                            if (null != profitResp.getBaseLinePower() && profitResp.getBaseLinePower() < 0) {
                                estimateSubDouble = MathUtils.subDoubleABS(profitResp.getDeliveryPower(), profitResp.getBaseLinePower());
                                if (null == estimateSubDouble || estimateSubDouble.compareTo(0D) < 0) {
                                    estimateSubDouble = 0D;
                                }
                            } else {
                                estimateSubDouble = MathUtils.subDoubleABS(profitResp.getDeliveryPower(), 0D);
                            }
                        } else {
                            estimateSubDouble = 0D;
                        }
                    } else {
                        estimateSubDouble = MathUtils.subDoubleABS(profitResp.getDeliveryPower(), profitResp.getBaseLinePower());
                        if (null == estimateSubDouble || estimateSubDouble.compareTo(0D) < 0) {
                            estimateSubDouble = 0D;
                        }
                    }
                    profitResp.setEstimatePower(MathUtils.doublePoint(estimateSubDouble, 8));
                    profitResp.setEstimateElectricQuantity(MathUtils.mulDoubleNull(profitResp.getEstimatePower(), 0.25D, 8));
                });
                String profitDetail = JSONObject.toJSONString(aggregatorDeviceDateProfitRespList);
                profit.setProfitDetail(profitDetail);
                updateList.add(profit);
            });
            if (CollectionUtils.isNotEmpty(updateList)) {
                aggregatorDeviceDateProfitService.updateListById(updateList);
            }
        }
        return true;
    }

    @Override
    public Boolean dealOffer(String date) {
        List<AggregatorResourceDateIssueOffer> aggregatorResourceDateIssueOfferList = aggregatorResourceDateIssueOfferService.getAggregatorResourceDateIssueOfferList(date);
        aggregatorResourceDateIssueOfferList.forEach(offer -> {
            String priceChart = offer.getPriceChart();
            List<DataResp> dataRespList = JSONArray.parseArray(priceChart, DataResp.class);
            dataRespList.forEach(dataResp -> {
                dataResp.setValue(MathUtils.mulDoubleZero(dataResp.getValue(), 0.001, 8));
            });
            offer.setPriceChart(JSONObject.toJSONString(dataRespList));
            aggregatorResourceDateIssueOfferService.updateById(offer);
        });
        return true;
    }

    @Override
    public List<ProfitExportResp> profitImport(String startDate, String endDate) {
        List<ProfitExportResp> profitExportRespList = Lists.newArrayList();
        List<String> dayList = DateUtils.getDayList(startDate, endDate);
        List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList();
        List<AggregatorResourceDateIssueOffer> aggregatorResourceDateIssueOfferList = aggregatorResourceDateIssueOfferService.getAggregatorResourceDateIssueOfferList(dayList);
        Map<String, Double> offerMap = new HashMap<>();
        aggregatorResourceDateIssueOfferList.forEach(offer -> {
            List<DataResp> dataRespList = JSONArray.parseArray(offer.getPriceChart(), DataResp.class);
            offerMap.putAll(dataRespList.stream().collect(toMap(dataResp -> offer.getResourceTypeId() + "~" + dataResp.getTime(), DataResp::getValue)));
        });
        Map<String, String> entMap = aggregatorEntList.stream().collect(toMap(AggregatorEnt::getEntId, AggregatorEnt::getEntName));
        List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList = aggregatorDeviceDateProfitService.getAggregatorDeviceDateProfitList(dayList);
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDateProfitList)) {
            List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = Lists.newArrayList();
            aggregatorDeviceDateProfitList.stream().filter(profit -> null != profit && StringUtils.isNotEmpty(profit.getProfitDetail())).forEach(profit -> {
                List<AggregatorDeviceDateProfitResp> respList = JSONArray.parseArray(profit.getProfitDetail(), AggregatorDeviceDateProfitResp.class);
                if (CollectionUtils.isNotEmpty(respList)) {
                    aggregatorDeviceDateProfitRespList.addAll(respList);
                }
            });
            if (CollectionUtils.isNotEmpty(aggregatorDeviceDateProfitRespList)) {
                Map<String, Map<String, List<AggregatorDeviceDateProfitResp>>> entTimeMap = aggregatorDeviceDateProfitRespList.stream().filter(resp -> null != resp).collect(groupingBy(AggregatorDeviceDateProfitResp::getEntId, groupingBy(time -> time.getResourceTypeId() + "~" + time.getStartTime() + "~" + time.getEndTime())));
                entTimeMap.entrySet().forEach(entTimeMapEntry -> {
                    ProfitExportResp profitExportResp = new ProfitExportResp();
                    profitExportResp.setEntName(entMap.get(entTimeMapEntry.getKey()));
                    profitExportResp.setCustomerName(entMap.get(entTimeMapEntry.getKey()));
                    profitExportResp.setProjectName(entMap.get(entTimeMapEntry.getKey()));
                    profitExportResp.setStartDate(startDate);
                    profitExportResp.setEndDate(endDate);
                    List<AggregatorDeviceDateProfitResp> resultList = Lists.newArrayList();
                    Map<String, List<AggregatorDeviceDateProfitResp>> timeMap = entTimeMapEntry.getValue();
                    timeMap.entrySet().forEach(timeMapEntry -> {
                        List<AggregatorDeviceDateProfitResp> respList = timeMapEntry.getValue();
                        AggregatorDeviceDateProfitResp aggregatorDeviceDateProfitResp = new AggregatorDeviceDateProfitResp();
                        aggregatorDeviceDateProfitResp.setResourceTypeId(timeMapEntry.getKey().split("~")[0]);
                        aggregatorDeviceDateProfitResp.setDate(DateUtils.format(timeMapEntry.getKey().split("~")[1], "yyyy-MM-dd"));
                        aggregatorDeviceDateProfitResp.setStartTime(timeMapEntry.getKey().split("~")[1]);
                        aggregatorDeviceDateProfitResp.setEndTime(timeMapEntry.getKey().split("~")[2]);
                        aggregatorDeviceDateProfitResp.setElectricQuantity(respList.stream().filter(resp -> null != resp && null != resp.getElectricQuantity()).collect(summingDouble(AggregatorDeviceDateProfitResp::getElectricQuantity)));
                        aggregatorDeviceDateProfitResp.setProfit(respList.stream().filter(resp -> null != resp && null != resp.getProfit()).collect(summingDouble(AggregatorDeviceDateProfitResp::getProfit)));
                        resultList.add(aggregatorDeviceDateProfitResp);
                    });
                    List<AggregatorDeviceDateProfitResp> sortList = resultList.stream().sorted(Comparator.comparing(AggregatorDeviceDateProfitResp::getEndTime)).collect(toList());
                    List<String> timeList = Lists.newArrayList();
                    List<Double> offerList = Lists.newArrayList();
                    List<Double> electricQuantityList = Lists.newArrayList();
                    List<Double> profitList = Lists.newArrayList();
                    List<String> dateList = Lists.newArrayList();
                    sortList.forEach(resp -> {
                        timeList.add(DateUtils.format(resp.getStartTime(), "HH:mm") + "-" + DateUtils.format(resp.getEndTime(), "HH:mm"));
                        offerList.add(MathUtils.aDoubletwo(offerMap.get(resp.getResourceTypeId() + "~" + resp.getEndTime()), 2));
                        electricQuantityList.add(MathUtils.aDoubletwo(resp.getElectricQuantity(), 2));
                        profitList.add(MathUtils.aDoubletwo(resp.getProfit(), 2));
                        dateList.add(DateUtils.format(resp.getStartTime(), "MM月dd日"));
                    });
                    profitExportResp.setTimeList(timeList);
                    profitExportResp.setOfferList(offerList);
                    profitExportResp.setElectricQuantityList(electricQuantityList);
                    profitExportResp.setProfitList(profitList);
                    profitExportResp.setDateList(dateList.stream().distinct().sorted().collect(toList()));
                    Map<String, Double> dateProfitMap = sortList.stream().collect(toMap(AggregatorDeviceDateProfitResp::getDate, AggregatorDeviceDateProfitResp::getProfit, (v1, v2) -> v1 + v2));
                    List<DataResp> dataRespList = Lists.newArrayList();
                    dateProfitMap.entrySet().forEach(dateProfitMapEntry -> {
                        DataResp dataResp = new DataResp();
                        dataResp.setTime(dateProfitMapEntry.getKey());
                        dataResp.setValue(dateProfitMapEntry.getValue());
                        dataRespList.add(dataResp);
                    });
                    profitExportResp.setDayProfitList(dataRespList.stream().sorted(Comparator.comparing(DataResp::getTime)).collect(toList()).stream().map(dateResp -> MathUtils.aDoubletwo(dateResp.getValue(), 2)).collect(toList()));
                    profitExportResp.setProfit(MathUtils.aDoubletwo(profitExportResp.getDayProfitList().stream().reduce(Double::sum).orElse(0.00), 2));
                    profitExportRespList.add(profitExportResp);
                });
            }
        }
        return profitExportRespList;
    }

    @Override
    public Boolean dealDeviceIssueOffer(String deviceBaseId, String date) {
        if (StringUtils.isEmpty(date)) {
            date = DateUtils.getAddDate(DateUtils.getDay(), -2);
        }
        List<String> deviceBaseIdList = Lists.newArrayList();
        if (StringUtils.isEmpty(deviceBaseId)) {
            deviceBaseIdList.addAll(aggregatorEntDeviceService.getAggregatorEntDeviceList().stream().map(AggregatorEntDevice::getDeviceBaseId).collect(toList()));
        }
        //查询出清价格
        Map<String, Double> offerMap = new HashMap<>();
        List<AggregatorResourceDateIssueOffer> aggregatorResourceDateIssueOfferList = aggregatorResourceDateIssueOfferService.getAggregatorResourceDateIssueOfferList(date);
        if (CollectionUtils.isNotEmpty(aggregatorResourceDateIssueOfferList)) {
            aggregatorResourceDateIssueOfferList.stream().filter(offer -> null != offer && StringUtils.isNotEmpty(offer.getPriceChart())).forEach(offer -> {
                List<DataResp> dataRespList = JSONArray.parseArray(offer.getPriceChart(), DataResp.class);
                if (CollectionUtils.isNotEmpty(dataRespList)) {
                    offerMap.putAll(dataRespList.stream().collect(toMap(dataResp -> offer.getResourceTypeId() + "&" + dataResp.getTime(), DataResp::getValue, (v1, v2) -> v1)));
                }
            });
        }
        String finalDate = date;
        deviceBaseIdList.forEach(baseId -> {
            //查询设备收益
            List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList = aggregatorDeviceDateProfitService.getAggregatorDeviceDateProfitListByDeviceBaseIdAndDate(baseId, finalDate);
            if (CollectionUtils.isNotEmpty(aggregatorDeviceDateProfitList)) {
                List<AggregatorDeviceDateProfit> updateList = Lists.newArrayList();
                aggregatorDeviceDateProfitList.stream().filter(profit -> null != profit && StringUtils.isNotEmpty(profit.getProfitDetail())).forEach(profit -> {
                    List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = JSONArray.parseArray(profit.getProfitDetail(), AggregatorDeviceDateProfitResp.class);
                    aggregatorDeviceDateProfitRespList.forEach(profitResp -> {
                        Double offer = offerMap.get(profitResp.getResourceTypeId() + "&" + profitResp.getEndTime());
                        profitResp.setCountPrice(null == offer ? 0 : offer);
                    });
                    String profitDetail = JSONObject.toJSONString(aggregatorDeviceDateProfitRespList);
                    profit.setProfitDetail(profitDetail);
                    updateList.add(profit);
                });
                if (CollectionUtils.isNotEmpty(updateList)) {
                    aggregatorDeviceDateProfitService.updateListById(updateList);
                }
            }
        });
        return true;
    }

    @Override
    public Boolean updateAggregatorDeviceDeliveryPowerPercentWithAggregatorId(String oldAggregatorId, String newAggregatorId, Long startId, Long endId) {
        executor.execute(() -> {
            aggregatorDeviceDeliveryPowerPercentService.updateDetailAggregatorId(oldAggregatorId, newAggregatorId, startId, endId);
        });
        return true;
    }

    @Override
    public Boolean updateAggregatorDeviceDateProfitWithAggregatorId(String oldAggregatorId, String newAggregatorId, Long startId, Long endId) {
        executor.execute(() -> {
            aggregatorDeviceDateProfitService.updateAggregatorId(oldAggregatorId, newAggregatorId, startId, endId);
        });
        return true;
    }

    /**
     * 查询下发功率
     *
     * @param aggregatorDeviceDateIssueChartList
     * @return
     */
    private Map<String, Double> getIssuePowerMap(List<AggregatorDeviceDateIssueChart> aggregatorDeviceDateIssueChartList) {
        Map<String, Double> issuePowerMap = new HashMap<>();
        if (null == aggregatorDeviceDateIssueChartList || aggregatorDeviceDateIssueChartList.size() <= 0) {
            return issuePowerMap;
        }
        aggregatorDeviceDateIssueChartList.forEach(chart -> {
            if (null != chart && StringUtils.isNotEmpty(chart.getIssueChart())) {
                List<DataResp> dataRespList = JSONArray.parseArray(chart.getIssueChart(), DataResp.class);
                if (null != dataRespList && dataRespList.size() > 0) {
                    Map<String, Double> timeMap = dataRespList.stream().collect(toMap(dataResp -> chart.getDeviceBaseId() + "&" + dataResp.getTime(), DataResp::getValue));
                    if (null != timeMap && timeMap.size() > 0) {
                        issuePowerMap.putAll(timeMap);
                    }
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
     * @param minuteList
     * @return
     */
    private Map<String, Map<String, Double>> getRealTimeAvgPower(List<AggregatorEntDevice> deviceList, String startTime, String endTime, List<String> minuteList) {
        Map<String, Map<String, Double>> deviceBaseIdTimeValueMap = new HashMap<>();
//        List<BigDataHistoryAndCalculationResp> bigDataResultList = Lists.newArrayList();
//        List<DeviceGroupPointInfo> deviceGroupPointInfoList = aggregatorDevicePointService.getDeviceGroupPointInfoList(deviceList, MetricEnum.YES_POWER.getCode());
//        List<BigDataHistoryAndCalculationResp> bigDataHistoryAndCalculationRespList = bigDataHistoryService.queryBigData(deviceList, deviceGroupPointInfoList, "1minute", startTime, endTime, "0");
//        if (null != bigDataHistoryAndCalculationRespList && bigDataHistoryAndCalculationRespList.size() > 0) {
//            bigDataResultList.addAll(bigDataHistoryAndCalculationRespList);
//        }
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
        List<BigDataHistoryResp> bigDataHistoryRespList = bigDataHistoryService.getBigData(deviceList, Arrays.asList(MetricEnum.YES_POWER.getCode()), startTime, endTime, "0");
        if (CollectionUtils.isNotEmpty(bigDataHistoryRespList)) {
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
     * 查询企业平均价格
     *
     * @param aggregatorDeviceDateProfitRespList
     * @return
     */
    private Map<String, Double> getEntIdCountPriceMap(List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList) {
        Map<String, Double> resultMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDateProfitRespList)) {
            Map<String, List<AggregatorDeviceDateProfitResp>> entMap = aggregatorDeviceDateProfitRespList.stream().collect(groupingBy(AggregatorDeviceDateProfitResp::getEntId));
            entMap.entrySet().forEach(entMapEntry -> {
                Double value = 0D;
                List<AggregatorDeviceDateProfitResp> resultList = entMapEntry.getValue().stream().filter(resp -> null != resp && null != resp.getCountPrice() && 0 != resp.getCountPrice()).collect(toList());
                if (CollectionUtils.isNotEmpty(resultList)) {
                    DoubleSummaryStatistics doubleSummaryStatistics = resultList.stream().collect(summarizingDouble(AggregatorDeviceDateProfitResp::getCountPrice));
                    if (null != doubleSummaryStatistics) {
                        value = MathUtils.doublePoint(doubleSummaryStatistics.getAverage(), 8);
                    }
                }
                resultMap.put(entMapEntry.getKey(), value);
            });
        }
        return resultMap;
    }

}
