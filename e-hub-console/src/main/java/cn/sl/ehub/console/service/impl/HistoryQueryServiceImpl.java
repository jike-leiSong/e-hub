package cn.sl.ehub.console.service.impl;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.math.BigDecimal;
import java.text.Collator;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;

import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.enn.bigdata.resp.BigDataHistoryAndCalculationResp;
import cn.enn.bigdata.resp.BigDataHistoryResp;
import cn.enn.bigdata.service.IBigDataHandlerService;
import cn.sl.ehub.console.enums.AggregatorProfitTimeEnum;
import cn.sl.ehub.console.enums.MetricEnum;
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
import cn.sl.ehub.console.model.resp.PriceExcelResp;
import cn.sl.ehub.console.model.vo.HistoryQueryGraphVO;
import cn.sl.ehub.console.model.vo.HistoryQueryTableVO;
import cn.sl.ehub.console.model.vo.LineDataGraphVO;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.model.vo.ProfitStatisticsDailyVO;
import cn.sl.ehub.console.model.vo.ProfitStatisticsVO;
import cn.sl.ehub.console.model.vo.UserProfitStatisticsDetailsVO;
import cn.sl.ehub.console.model.vo.UserProfitStatisticsVO;
import cn.sl.ehub.service.req.AdjustSituationExcelRep;
import cn.sl.ehub.service.req.IndexOverviewBaseTableResp;
import cn.sl.ehub.service.req.IndexOverviewTableResp;
import cn.sl.ehub.service.resp.AggregatorDeviceDateProfitResp;
import cn.sl.ehub.service.resp.AggregatorEntDateAdjustResp;
import cn.sl.ehub.service.resp.HistoryQueryDeviceMetricResp;
import cn.sl.ehub.service.resp.IndexOverviewResp;
import cn.sl.ehub.service.resp.IndexOverviewTimeColorResp;
import cn.sl.ehub.console.service.IAggregatorAvgRtChartService;
import cn.sl.ehub.console.service.IAggregatorBaseLineLoadChartService;
import cn.sl.ehub.console.service.IAggregatorCrChartService;
import cn.sl.ehub.console.service.IAggregatorDapChartService;
import cn.sl.ehub.console.service.IAggregatorDateIssueChartService;
import cn.sl.ehub.console.service.IAggregatorDateProfitService;
import cn.sl.ehub.console.service.IAggregatorDeviceDateIssueChartService;
import cn.sl.ehub.console.service.IAggregatorDeviceDateProfitService;
import cn.sl.ehub.console.service.IAggregatorDevicePointService;
import cn.sl.ehub.console.service.IAggregatorEntBaseLineLoadChartService;
import cn.sl.ehub.console.service.IAggregatorEntDateAdjustService;
import cn.sl.ehub.console.service.IAggregatorEntDateProfitService;
import cn.sl.ehub.console.service.IAggregatorEntDeviceService;
import cn.sl.ehub.console.service.IAggregatorEntProfitTimeService;
import cn.sl.ehub.console.service.IAggregatorEntService;
import cn.sl.ehub.console.service.IAggregatorInfoService;
import cn.sl.ehub.console.service.IAggregatorResourceDateDeliveryOfferService;
import cn.sl.ehub.console.service.IAggregatorResourceDateIssueOfferService;
import cn.sl.ehub.console.service.IAggregatorResourceTypeService;
import cn.sl.ehub.console.service.IHistoryQueryService;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.common.utils.RandomColorUtil;
import cn.sl.ehub.service.vo.AggregatorAvgRtChart;
import cn.sl.ehub.service.vo.AggregatorBaseLineLoadChart;
import cn.sl.ehub.service.vo.AggregatorCrChart;
import cn.sl.ehub.service.vo.AggregatorDapChart;
import cn.sl.ehub.service.vo.AggregatorDateIssueChart;
import cn.sl.ehub.service.vo.AggregatorDateProfit;
import cn.sl.ehub.service.vo.AggregatorDeviceDateIssueChart;
import cn.sl.ehub.service.vo.AggregatorDeviceDateProfit;
import cn.sl.ehub.service.vo.AggregatorEnt;
import cn.sl.ehub.service.vo.AggregatorEntBaseLineLoadChart;
import cn.sl.ehub.service.vo.AggregatorEntDateAdjust;
import cn.sl.ehub.service.vo.AggregatorEntDateProfit;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorEntProfitTime;
import cn.sl.ehub.service.vo.AggregatorInfo;
import cn.sl.ehub.service.vo.AggregatorResourceDateDeliveryOffer;
import cn.sl.ehub.service.vo.AggregatorResourceDateIssueOffer;
import cn.sl.ehub.service.vo.AggregatorResourceType;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.common.vo.DeviceGroupPointInfo;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class HistoryQueryServiceImpl implements IHistoryQueryService {

    private final IAggregatorDateIssueChartService aggregatorDateIssueChartService;
    private final IAggregatorCrChartService aggregatorCrChartService;
    private final IAggregatorDapChartService aggregatorDapChartService;
    private final IAggregatorEntDeviceService aggregatorEntDeviceService;
    private final IAggregatorDeviceDateIssueChartService aggregatorDeviceDateIssueChartService;
    private final IBigDataHandlerService bigDataHistoryService;
    private final IAggregatorDeviceDateProfitService aggregatorDeviceDateProfitService;
    private final IAggregatorDateProfitService aggregatorDateProfitService;
    private final IAggregatorEntDateProfitService aggregatorEntDateProfitService;
    private final IAggregatorEntService aggregatorEntService;
    private final IAggregatorResourceDateDeliveryOfferService aggregatorResourceDateDeliveryOfferService;
    private final IAggregatorResourceDateIssueOfferService aggregatorResourceDateIssueOfferService;
    private final IAggregatorDevicePointService aggregatorDevicePointService;
    private final IAggregatorEntProfitTimeService aggregatorEntProfitTimeService;
    @Resource(name = "threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor executor;
    private final IAggregatorBaseLineLoadChartService aggregatorBaseLineLoadChartService;
    private final IAggregatorEntBaseLineLoadChartService aggregatorEntBaseLineLoadChartService;
    private final IAggregatorAvgRtChartService aggregatorAvgRtChartService;
    private final IAggregatorEntDateAdjustService aggregatorEntDateAdjustService;
    private final IAggregatorInfoService aggregatorInfoService;
    private final IAggregatorResourceTypeService aggregatorResourceTypeService;

    @Override
    public HistoryQueryGraphVO userAdjustmentGraph(UserAdjustmentGraphReq userAdjustmentGraphReq, String simulate) {
        HistoryQueryGraphVO resp = new HistoryQueryGraphVO();
        List<String> dateList = DateUtils.getDayList(userAdjustmentGraphReq.getStartTime(), userAdjustmentGraphReq.getEndTime());
        userAdjustmentGraphReq.setStartTime(userAdjustmentGraphReq.getStartTime() + " 00:01:00");
        userAdjustmentGraphReq.setEndTime(DateUtils.getAddDate(userAdjustmentGraphReq.getEndTime(), 1) + " 00:00:00");
        List<String> minuteList = DateUtils.getMinuteList(userAdjustmentGraphReq.getStartTime(), userAdjustmentGraphReq.getEndTime());
        //查询实际功率
        getPowerChart(userAdjustmentGraphReq, minuteList, simulate, resp);
        //查询分解后设备功率/设备有效负荷
        getIssueChart(userAdjustmentGraphReq, dateList, minuteList, resp);
        //查询曲线颜色
        List<String> minuteListWithColor = AggregatorProfitTimeEnum.getMinuteList();
        resp.setFillColor(getColorList(resp.getResolvedPower(), resp.getActualPower(), minuteListWithColor));
        return resp;
    }

    @Override
    public HistoryQueryGraphVO userAdjustmentGraphNew(NewUserAdjustmentGraphReq userAdjustmentGraphReq, String simulate) {
        HistoryQueryGraphVO resp = new HistoryQueryGraphVO();

        //查询实际功率
        getEntPowerChartNew(userAdjustmentGraphReq, resp);
        //查询分解后设备功率/设备有效负荷
        //getIssueChart(userAdjustmentGraphReq, dateList, minuteList, resp);

        //do  有效调节负荷
        getEntAdjust(userAdjustmentGraphReq,resp);

        //查询企业基线
        getEntBaseLine(userAdjustmentGraphReq.getSubEntId(),userAdjustmentGraphReq.getResourceTypeId(),userAdjustmentGraphReq.getStartTime(),userAdjustmentGraphReq.getEndTime(),resp);

        //查询曲线颜色
        List<String> minuteListWithColor = AggregatorProfitTimeEnum.getMinuteList();
        resp.setFillColor(getColorList(resp.getResolvedPower(), resp.getActualPower(), minuteListWithColor));
        return resp;
    }
    /**
     * @description 查询多个企业有效调节负荷
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private Map<String, List<DataResp>> getMoreEntAdjust(List<String> entIdList,String startDate,String endDate,String sourceId){
        Map<String, List<DataResp>> resMap = new HashMap<>();
        Map<String, List<AggregatorEntDateAdjust>> entAdjustMap = aggregatorEntDateAdjustService.getMoreEntAdjust(entIdList, startDate, endDate, sourceId);
        if(CollectionUtil.isNotEmpty(entAdjustMap)){
            entAdjustMap.forEach((key,value)->{
                List<DataResp> respList = value.stream().flatMap(a -> {
                    String profitDetail = a.getProfitDetail();
                    List<AggregatorEntDateAdjustResp> adjustRespList = JSONObject.parseArray(profitDetail, AggregatorEntDateAdjustResp.class);
                    List<DataResp> list = adjustRespList.stream().map(c -> {
                        DataResp dataResp = new DataResp();
                        dataResp.setTime(c.getEndTime());
                        dataResp.setValue(c.getCountPower());
                        return dataResp;
                    }).collect(toList());
                    return list.stream();
                }).collect(toList());
                resMap.put(key,respList);
            });
        }
        return  resMap;
    }

    /**
     * @description 企业有效调节
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private void getEntAdjust(NewUserAdjustmentGraphReq userAdjustmentGraphReq,HistoryQueryGraphVO resp){
        List<AggregatorEntDateAdjust> entAdjustList = aggregatorEntDateAdjustService.getEntAdjust(userAdjustmentGraphReq.getSubEntId(), userAdjustmentGraphReq.getStartTime(), userAdjustmentGraphReq.getEndTime(), userAdjustmentGraphReq.getResourceTypeId());
        List<DataResp> adjustPowerList = new ArrayList<>();
        if(CollectionUtil.isNotEmpty(entAdjustList)){
            adjustPowerList = entAdjustList.stream().filter(a -> StringUtil.isNotEmpty(a.getProfitDetail())).flatMap(b -> {
                String profitDetail = b.getProfitDetail();
                List<AggregatorEntDateAdjustResp> adjustRespList = JSONObject.parseArray(profitDetail, AggregatorEntDateAdjustResp.class);
                List<DataResp> list = adjustRespList.stream().map(c -> {
                    DataResp dataResp = new DataResp();
                    dataResp.setTime(c.getEndTime());
                    dataResp.setValue(c.getCountPower());
                    return dataResp;
                }).collect(toList());
                return list.stream();
            }).collect(toList());
        }
        resp.setAdjustPower(adjustPowerList);
    }

    /**
     * @description 计算平均功率 15分钟功率之和求平均值
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private  Map<String, Map<String, Double>> getRealTimeAvgPower(List<AggregatorEntDevice> deviceList, String startTime, String endTime, List<String> minuteList) {
       // minuteList =  minuteList.stream().sorted().collect(Collectors.toList());

        Map<String, Map<String, Double>> deviceBaseIdTimeValueMap = new HashMap<>();
        List<BigDataHistoryResp> bigDataHistoryRespList = bigDataHistoryService.getBigData(deviceList, Arrays.asList(MetricEnum.YES_POWER.getCode()), startTime, endTime, "0");

        // 【优化】改为 debug 级别日志，只打印数量，避免大数据量时输出巨大日志
        log.debug("bigDataHistoryRespList size={}, deviceList size={}",
            bigDataHistoryRespList != null ? bigDataHistoryRespList.size() : 0,
            deviceList != null ? deviceList.size() : 0);
        if (CollectionUtils.isNotEmpty(bigDataHistoryRespList)) {
            Map<String, List<DataResp>> deviceIdStationIdMap = new HashMap<>();
            for (BigDataHistoryResp history : bigDataHistoryRespList) {
                if (history != null && history.getDataResp() != null && !history.getDataResp().isEmpty()) {
                    String key = history.getEquipMK() + "_" + history.getEquipID() + "," + history.getStaId();
                    deviceIdStationIdMap.put(key, history.getDataResp());
                }
            }
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
                        }else {
                            timeMap.put(minuteList.get(i + 14), null);
                        }
                    }
                }
                deviceBaseIdTimeValueMap.put(device.getDeviceBaseId(), timeMap);
            });
        }
        return deviceBaseIdTimeValueMap;
    }


    @Override
    public PageResultVO<HistoryQueryTableVO> userAdjustmentTable(UserAdjustmentTableReq userAdjustmentTableReq) {
        PageResultVO<HistoryQueryTableVO> pageResultVO = new PageResultVO<>();
        List<String> dateList = DateUtils.getDayList(userAdjustmentTableReq.getStartTime(), userAdjustmentTableReq.getEndTime());
        List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList = aggregatorDeviceDateProfitService.getAggregatorDeviceDateProfitList(userAdjustmentTableReq.getDeviceBaseId(), dateList);
        if (CollectionUtils.isEmpty(aggregatorDeviceDateProfitList)) {
            return getPageResultVO(pageResultVO, userAdjustmentTableReq.getPageSize());
        }
        List<AggregatorDeviceDateProfitResp> totalAggregatorDeviceDateProfitRespList = Lists.newArrayList();
        aggregatorDeviceDateProfitList.stream().filter(profit -> null != profit && StringUtils.isNotEmpty(profit.getProfitDetail())).forEach(aggregatorDeviceDateProfit -> {
            String profitDetail = aggregatorDeviceDateProfit.getProfitDetail();
            List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = JSONArray.parseArray(profitDetail, AggregatorDeviceDateProfitResp.class);
            if (null != aggregatorDeviceDateProfitRespList && aggregatorDeviceDateProfitRespList.size() > 0) {
                totalAggregatorDeviceDateProfitRespList.addAll(aggregatorDeviceDateProfitRespList);
            }
        });
        if (CollectionUtils.isEmpty(totalAggregatorDeviceDateProfitRespList)) {
            return getPageResultVO(pageResultVO, userAdjustmentTableReq.getPageSize());
        }
        pageResultVO.setPageSize(userAdjustmentTableReq.getPageSize());
        pageResultVO.setTotal(totalAggregatorDeviceDateProfitRespList.size());

        Integer pageIndex = userAdjustmentTableReq.getPageNo();
        Integer pageIndexMax = (int) Math.ceil(pageResultVO.getTotal() / Double.parseDouble(pageResultVO.getPageSize().toString()));
        if (pageIndex > pageIndexMax) {
            pageIndex = pageIndexMax;
        }
        pageResultVO.setPageIndex(pageIndex);

        Integer pageStart = (pageResultVO.getPageIndex() - 1) * pageResultVO.getPageSize();
        Integer pageEnd = pageStart + pageResultVO.getPageSize();
        if (pageStart >= pageResultVO.getTotal()) {
            pageStart = pageResultVO.getTotal() - 1;
        }
        if (pageEnd > pageResultVO.getTotal()) {
            pageEnd = pageResultVO.getTotal();
        }
        AggregatorEntDevice aggregatorEntDevice = aggregatorEntDeviceService.getAggregatorEntDevice(userAdjustmentTableReq.getDeviceBaseId());
        List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = totalAggregatorDeviceDateProfitRespList.subList(pageStart, pageEnd);
        List<HistoryQueryTableVO> historyQueryTableVOList = Lists.newArrayList();
        aggregatorDeviceDateProfitRespList.forEach(aggregatorDeviceDateProfitResp -> {
            HistoryQueryTableVO historyQueryTableVO = new HistoryQueryTableVO();
            historyQueryTableVO.setTime(DateUtils.format(aggregatorDeviceDateProfitResp.getStartTime(), "yyyy-MM-dd HH:mm") + "~" + DateUtils.format(aggregatorDeviceDateProfitResp.getEndTime(), "HH:mm"));
            historyQueryTableVO.setDeviceName(null == aggregatorEntDevice ? null : aggregatorEntDevice.getDeviceName());
            historyQueryTableVO.setIssuePower(aggregatorDeviceDateProfitResp.getIssuePower());
            historyQueryTableVO.setActualPower(MathUtils.doublePointNotRounding(aggregatorDeviceDateProfitResp.getReallyPower(), 2));
            historyQueryTableVO.setUsePower(MathUtils.mulDoubleNull(historyQueryTableVO.getIssuePower(), 0.7, 2));
            historyQueryTableVOList.add(historyQueryTableVO);
        });
        pageResultVO.setList(historyQueryTableVOList);
        return pageResultVO;
    }

    @Override
    public List<LineDataGraphResp> deviceRunStatusChart(DeviceRunStatusReq deviceRunStatusReq, String simulate) {
        List<LineDataGraphResp> respList = Lists.newArrayList();
        if (null == deviceRunStatusReq
                || null == deviceRunStatusReq.getDeviceBaseIdList()
                || deviceRunStatusReq.getDeviceBaseIdList().size() <= 0
                || null == deviceRunStatusReq.getMetricList()
                || deviceRunStatusReq.getMetricList().size() <= 0) {
            return respList;
        }
        deviceRunStatusReq.setStartTime(deviceRunStatusReq.getStartTime() + " 00:01:00");
        deviceRunStatusReq.setEndTime(DateUtils.getAddDate(deviceRunStatusReq.getEndTime(), 1) + " 00:00:00");
        List<String> minuteList = DateUtils.getMinuteList(deviceRunStatusReq.getStartTime(), deviceRunStatusReq.getEndTime());
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(deviceRunStatusReq.getDeviceBaseIdList());
        Map<String, AggregatorEntDevice> deviceMap = deviceList.stream().collect(Collectors.toMap(AggregatorEntDevice::getDeviceBaseId, Function.identity(), (v1, v2) -> v1));
        if (deviceRunStatusReq.getMetricList().contains(MetricEnum.USE_ELECTRIC.getCode())) {
            deviceRunStatusReq.getMetricList().remove(MetricEnum.USE_ELECTRIC.getCode());
            deviceRunStatusReq.getMetricList().addAll(Arrays.asList(MetricEnum.IA.getCode(), MetricEnum.IB.getCode(), MetricEnum.IC.getCode()));
        }
        Map<String, Map<String, List<DataResp>>> deviceIdMap = new HashMap<>();
        Map<String, List<DataResp>> metricMap = new HashMap<>();
        List<DeviceGroupPointInfo> deviceGroupPointInfoList = aggregatorDevicePointService.getDeviceGroupPointInfoList(deviceList, deviceRunStatusReq.getMetricList());
        List<BigDataHistoryAndCalculationResp> bigDataHistoryAndCalculationRespList = bigDataHistoryService.queryBigData(deviceList, deviceGroupPointInfoList, "1minute", deviceRunStatusReq.getStartTime(), deviceRunStatusReq.getEndTime(), simulate);
        if (null != bigDataHistoryAndCalculationRespList && bigDataHistoryAndCalculationRespList.size() > 0) {
            deviceIdMap.putAll(bigDataHistoryAndCalculationRespList.stream().collect(Collectors.groupingBy(BigDataHistoryAndCalculationResp::getDeviceId, Collectors.toMap(bigDataHistoryResp -> bigDataHistoryResp.getMetric().split("\\.")[1], BigDataHistoryAndCalculationResp::getDataResp, (v1, v2) -> v1))));
            metricMap.putAll(bigDataHistoryAndCalculationRespList.stream().collect(Collectors.toMap(bigDataHistoryResp -> bigDataHistoryResp.getMetric().split("\\.")[1], BigDataHistoryAndCalculationResp::getDataResp, (v1, v2) -> v1)));
        }
        if (deviceRunStatusReq.getStatus().equals("1")) {
            //多设备单指标
            LineDataGraphResp lineDataGraphResp = new LineDataGraphResp();
            List<LineDataGraphVO> resultList = Lists.newArrayList();
            deviceRunStatusReq.getDeviceBaseIdList().forEach(deviceBaseId -> {
                AggregatorEntDevice aggregatorEntDevice = deviceMap.get(deviceBaseId);
                Map<String, List<DataResp>> metricDataRespListMap = deviceIdMap.get(aggregatorEntDevice.getDeviceId().split("_")[1]);
                if (null == metricDataRespListMap) {
                    metricDataRespListMap = new HashMap<>();
                }
                Map<String, List<DataResp>> finalMetricDataRespListMap = metricDataRespListMap;
                deviceRunStatusReq.getMetricList().forEach(metric -> {
                    String lineName = aggregatorEntDevice.getDeviceName();
                    MetricEnum metricEnum = MetricEnum.getMetricEnum(metric);
                    if (MetricEnum.IA.getCode().equals(metric)) {
                        lineName += MetricEnum.IA.getDesc();
                    }
                    if (MetricEnum.IB.getCode().equals(metric)) {
                        lineName += MetricEnum.IB.getDesc();
                    }
                    if (MetricEnum.IC.getCode().equals(metric)) {
                        lineName += MetricEnum.IC.getDesc();
                    }
                    resultList.add(getLineDataGraphVO(lineName, metricEnum.getGroupName(), metricEnum, finalMetricDataRespListMap.get(metric), minuteList, deviceRunStatusReq.getResourceTypeId()));
                });
            });
            if (null != resultList && resultList.size() > 0) {
                lineDataGraphResp.setUnit(resultList.get(0).getLineUnit());
                lineDataGraphResp.setChartName(resultList.get(0).getChartName());
            }
            lineDataGraphResp.setLineDataGraphVOList(resultList);
            respList.add(lineDataGraphResp);
        } else {
            //单设备多指标
            String deviceBaseId = deviceRunStatusReq.getDeviceBaseIdList().get(0);
            AggregatorEntDevice aggregatorEntDevice = deviceMap.get(deviceBaseId);
            String deviceName = "";
            if (null != aggregatorEntDevice) {
                deviceName = aggregatorEntDevice.getDeviceName();
            }
            List<LineDataGraphVO> resultList = Lists.newArrayList();
            String finalDeviceName = deviceName;
            deviceRunStatusReq.getMetricList().forEach(metric -> {
                MetricEnum metricEnum = MetricEnum.getMetricEnum(metric);
                resultList.add(getLineDataGraphVO(metricEnum.getDesc(), finalDeviceName, metricEnum, metricMap.get(metric), minuteList, deviceRunStatusReq.getResourceTypeId()));
            });
            if (null != resultList && resultList.size() > 0) {
                Map<String, List<LineDataGraphVO>> groupCodeMap = resultList.stream().collect(Collectors.groupingBy(LineDataGraphVO::getGroupCode));
                groupCodeMap.entrySet().forEach(groupCodeMapEntry -> {
                    LineDataGraphResp lineDataGraphResp = new LineDataGraphResp();
                    List<LineDataGraphVO> lineDataGraphVOList = groupCodeMapEntry.getValue();
                    if (null != lineDataGraphVOList && lineDataGraphVOList.size() > 0) {
                        lineDataGraphResp.setUnit(lineDataGraphVOList.get(0).getLineUnit());
                        lineDataGraphResp.setChartName(lineDataGraphVOList.get(0).getChartName());
                    }
                    lineDataGraphResp.setLineDataGraphVOList(lineDataGraphVOList);
                    respList.add(lineDataGraphResp);
                });
            }
        }
        return respList;
    }

    /**
     * 处理数据
     *
     * @param lineName
     * @param chartName
     * @param metricEnum
     * @param dataRespList
     * @param minuteList
     * @param resourceTypeId
     * @return
     */
    private LineDataGraphVO getLineDataGraphVO(String lineName, String chartName, MetricEnum metricEnum, List<DataResp> dataRespList, List<String> minuteList, String resourceTypeId) {
        LineDataGraphVO result = new LineDataGraphVO();
        result.setLineName(lineName);
        result.setChartName(chartName);
        if (null != metricEnum) {
            result.setLineUnit(metricEnum.getUnit());
            result.setGroupCode(metricEnum.getGroupCode());
            result.setGroupName(metricEnum.getGroupName());
        }
        Map<String, Double> timeValueMap = new HashMap<>();
        if (null != dataRespList && dataRespList.size() > 0) {
            timeValueMap.putAll(dataRespList.stream().collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1)));
        }
        List<DataResp> dataRespArrayList = Lists.newArrayList();
        minuteList.forEach(minute -> {
            DataResp dataResp = new DataResp();
            dataResp.setTime(minute);
            dataResp.setValue(null == timeValueMap.get(minute) ? null : MathUtils.doublePoint(timeValueMap.get(minute), 2));
            if (StringUtils.isNotEmpty(resourceTypeId) && resourceTypeId.equals("27")) {
                dataResp.setValue(null == dataResp.getValue() ? null : 0 - dataResp.getValue());
            }
            dataRespArrayList.add(dataResp);
        });
        result.setDataRespList(dataRespArrayList);
        return result;
    }

    @Override
    public ProfitStatisticsVO profitStatistics(ProfitStatisticsReq profitStatisticsReq) {
        ProfitStatisticsVO profitStatisticsVO = new ProfitStatisticsVO();
        List<String> dateList = DateUtils.getDayList(profitStatisticsReq.getStartTime(), profitStatisticsReq.getEndTime());
        profitStatisticsVO.setDateList(dateList);
        ProfitStatisticsDailyVO profitStatisticsDailyVO = new ProfitStatisticsDailyVO();
        profitStatisticsDailyVO.setIssueAmount(0D);
        profitStatisticsDailyVO.setAggregatorProfits(0D);
        profitStatisticsDailyVO.setUserProfits(0D);
        List<ProfitStatisticsDailyVO> profitStatisticsDailyVOList = Lists.newArrayList();
        List<AggregatorDateProfit> aggregatorDateProfitList = aggregatorDateProfitService.getAggregatorDateProfitList(profitStatisticsReq.getAggregatorId(), dateList);
        if (null != aggregatorDateProfitList && aggregatorDateProfitList.size() > 0) {
            Double issueProfit = aggregatorDateProfitList.stream().filter(profit -> null != profit && null != profit.getIssueProfit()).collect(Collectors.summingDouble(AggregatorDateProfit::getIssueProfit));
            profitStatisticsDailyVO.setIssueAmount(null == issueProfit ? 0 : MathUtils.doublePoint(issueProfit, 2));
            Double aggregatorProfit = aggregatorDateProfitList.stream().filter(profit -> null != profit && null != profit.getAggregatorProfit()).collect(Collectors.summingDouble(AggregatorDateProfit::getAggregatorProfit));
            profitStatisticsDailyVO.setAggregatorProfits(null == aggregatorProfit ? 0 : MathUtils.doublePoint(aggregatorProfit, 2));
            Double entProfit = aggregatorDateProfitList.stream().filter(profit -> null != profit && null != profit.getEntProfit()).collect(Collectors.summingDouble(AggregatorDateProfit::getEntProfit));
            profitStatisticsDailyVO.setUserProfits(null == entProfit ? 0 : MathUtils.doublePoint(entProfit, 2));
            Map<String, AggregatorDateProfit> dateMap = aggregatorDateProfitList.stream().collect(Collectors.toMap(AggregatorDateProfit::getDate, Function.identity(), (v1, v2) -> v1));
            dateList.forEach(date -> {
                ProfitStatisticsDailyVO profitVO = new ProfitStatisticsDailyVO();
                AggregatorDateProfit aggregatorDateProfit = dateMap.get(date);
                if (null != aggregatorDateProfit) {
                    profitVO.setIssueAmount(MathUtils.doublePointNotRounding(aggregatorDateProfit.getIssueProfit(), 2));
                    profitVO.setAggregatorProfits(MathUtils.doublePointNotRounding(aggregatorDateProfit.getAggregatorProfit(), 2));
                    profitVO.setUserProfits(MathUtils.doublePointNotRounding(aggregatorDateProfit.getEntProfit(), 2));
                } else {
                    profitVO.setIssueAmount(0D);
                    profitVO.setAggregatorProfits(0D);
                    profitVO.setUserProfits(0D);
                }
                profitStatisticsDailyVOList.add(profitVO);
            });
        } else {
            dateList.forEach(date -> {
                ProfitStatisticsDailyVO profitVO = new ProfitStatisticsDailyVO();
                profitVO.setIssueAmount(0D);
                profitVO.setAggregatorProfits(0D);
                profitVO.setUserProfits(0D);
                profitStatisticsDailyVOList.add(profitVO);
            });
        }
        profitStatisticsVO.setProfitStatisticsDailyList(profitStatisticsDailyVOList);
        profitStatisticsVO.setProfitStatisticsAmount(profitStatisticsDailyVO);
        return profitStatisticsVO;
    }

    @Override
    public UserProfitStatisticsVO userProfitStatistics(ProfitStatisticsReq profitStatisticsReq) {
        UserProfitStatisticsVO userProfitStatisticsVO = new UserProfitStatisticsVO();
        List<String> dateList = DateUtils.getDayList(profitStatisticsReq.getStartTime(), profitStatisticsReq.getEndTime());
        List<UserProfitStatisticsDetailsVO> userProfitStatisticsDetailsVOList = Lists.newArrayList();
        List<AggregatorEntDateProfit> aggregatorEntDateProfitList = aggregatorEntDateProfitService.getAggregatorEntDateProfitList(profitStatisticsReq.getAggregatorId(), dateList);
        double totalProfit = 0;
        if (null != aggregatorEntDateProfitList && aggregatorEntDateProfitList.size() > 0) {
            totalProfit += aggregatorEntDateProfitList.stream().filter(profit -> null != profit && null != profit.getEntProfit()).mapToDouble(AggregatorEntDateProfit::getEntProfit).sum();
            userProfitStatisticsVO.setUserAmount(totalProfit);
            Map<String, Double> entProfitMap = aggregatorEntDateProfitList.stream().filter(profit -> null != profit && null != profit.getEntProfit()).collect(Collectors.groupingBy(AggregatorEntDateProfit::getEntId, Collectors.summingDouble(AggregatorEntDateProfit::getEntProfit)));
            if (null != entProfitMap && entProfitMap.size() > 0) {
                List<String> entIdList = aggregatorEntDateProfitList.stream().map(AggregatorEntDateProfit::getEntId).distinct().collect(Collectors.toList());
                List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList(entIdList);
                Map<String, String> entIdNameMap = new HashMap<>();
                if (null != aggregatorEntList && aggregatorEntList.size() > 0) {
                    entIdNameMap = aggregatorEntList.stream().collect(Collectors.toMap(AggregatorEnt::getEntId, AggregatorEnt::getEntName, (v1, v2) -> v1));
                }
                if (null == entIdNameMap || entIdNameMap.size() <= 0) {
                    entIdNameMap = new HashMap<>();
                }

                List<String> colorSet = RandomColorUtil.getColorSet(entProfitMap.size());
                AtomicReference<Integer> num = new AtomicReference<>(0);
                Map<String, String> finalEntIdNameMap = entIdNameMap;
                double finalTotalProfit = totalProfit;
                entProfitMap.entrySet().forEach(entProfitMapEntry -> {
                    UserProfitStatisticsDetailsVO userProfitStatisticsDetailsVO = new UserProfitStatisticsDetailsVO();
                    userProfitStatisticsDetailsVO.setColor(colorSet.get(num.get()));
                    userProfitStatisticsDetailsVO.setEntName(finalEntIdNameMap.get(entProfitMapEntry.getKey()));
                    Double entProfit = MathUtils.doublePoint(entProfitMapEntry.getValue(), 2);
                    userProfitStatisticsDetailsVO.setEntProfitSort(null == entProfit ? 0 : entProfit);
                    userProfitStatisticsDetailsVO.setEntProfit(null == entProfit ? "元" : entProfit + "元");
                    Double profitPercent = MathUtils.divideNull(entProfit, finalTotalProfit, 2);
                    int percent = MathUtils.mulDoubleZero(profitPercent, 100D, 0).intValue();
                    userProfitStatisticsDetailsVO.setProfitPercent(percent + "%");
                    userProfitStatisticsDetailsVOList.add(userProfitStatisticsDetailsVO);
                    num.getAndSet(num.get() + 1);
                });
            }
        }
        if (null != userProfitStatisticsDetailsVOList && userProfitStatisticsDetailsVOList.size() > 0) {
            List<UserProfitStatisticsDetailsVO> sortList = userProfitStatisticsDetailsVOList.stream().sorted(Comparator.comparing(UserProfitStatisticsDetailsVO::getEntProfitSort).reversed()).collect(Collectors.toList());
            userProfitStatisticsVO.setUserProfitStatisticsList(sortList);
        } else {
            userProfitStatisticsVO.setUserProfitStatisticsList(userProfitStatisticsDetailsVOList);
        }
        userProfitStatisticsVO.setUserAmount(MathUtils.doublePoint(totalProfit, 2));
        return userProfitStatisticsVO;
    }

    @Override
    public IndexOverviewResp getTotalPowerChart(String simulate, String aggregatorId, String resourceTypeId, String startDate, String endDate) {
        IndexOverviewResp resp = new IndexOverviewResp();

        // 处理日期参数 - 创建最终副本以供 lambda 使用
        String finalStartDate = StringUtils.isEmpty(startDate) && StringUtils.isEmpty(endDate)
                ? DateUtils.getAddDate(DateUtils.getDay(), -2)
                : startDate;
        String finalEndDate = StringUtils.isEmpty(startDate) && StringUtils.isEmpty(endDate)
                ? finalStartDate
                : endDate;

        List<String> dateList = DateUtils.getDayList(finalStartDate, finalEndDate);
        String startTime = finalStartDate + " 00:01:00";
        String endTime = DateUtils.getAddDate(finalEndDate) + " 00:00:00";

        // 【优化】使用 CompletableFuture 并行执行 6 个独立查询任务
        CompletableFuture<Void> issueFuture = CompletableFuture.runAsync(() ->
            getIssueChartNew(aggregatorId, resourceTypeId, dateList, DateUtils.getMinuteList(startTime, endTime), resp), executor);

        CompletableFuture<Void> crFuture = CompletableFuture.runAsync(() ->
            getCrChartNew(aggregatorId, resourceTypeId, dateList, DateUtils.getMinuteList(startTime, endTime), resp), executor);

        CompletableFuture<Void> dapFuture = CompletableFuture.runAsync(() ->
            getDapChartNew(aggregatorId, resourceTypeId, dateList, DateUtils.getMinuteList(startTime, endTime), resp), executor);

        CompletableFuture<Void> powerFuture = CompletableFuture.runAsync(() ->
            getAggregatorPowerChartNew(aggregatorId, resourceTypeId, finalStartDate, finalEndDate, resp), executor);

        CompletableFuture<Void> baseLineFuture = CompletableFuture.runAsync(() ->
            getAggregatorBaseLine(aggregatorId, resourceTypeId, finalStartDate, finalEndDate, resp), executor);

        CompletableFuture<Void> issuePriceFuture = CompletableFuture.runAsync(() ->
            getIssuePriceChartNew(aggregatorId, resourceTypeId, dateList, DateUtils.getMinuteList(startTime, endTime), resp), executor);

        // 等待所有任务完成
        CompletableFuture.allOf(issueFuture, crFuture, dapFuture, powerFuture, baseLineFuture, issuePriceFuture).join();

        // 生成时间轴
        List<String> timeList = new ArrayList<>();
        String startTimeIndex = finalStartDate + " 00:00:00";
        String endTimeIndex = DateUtils.getAddDate(finalEndDate) + " 00:01:00";
        LocalDateTime start = LocalDateTimeUtil.parse(startTimeIndex, DatePattern.NORM_DATETIME_PATTERN);
        LocalDateTime end = LocalDateTimeUtil.parse(endTimeIndex, DatePattern.NORM_DATETIME_PATTERN);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime current = start;
        while (current.isBefore(end)) {
            timeList.add(current.format(formatter));
            current = current.plusMinutes(15);
        }
        resp.setTimeList(timeList);

        // 【优化】使用通用方法填充所有图表数据
        fillTimeSeriesData(resp.getPowerChart(), timeList, resp::setPowerChart, resp);
        fillTimeSeriesData(resp.getBaseLineChart(), timeList, resp::setBaseLineChart, resp);
        fillTimeSeriesData(resp.getIssueChart(), timeList, resp::setIssueChart, resp);
        fillTimeSeriesData(resp.getCrChart(), timeList, resp::setCrChart, resp);
        fillTimeSeriesData(resp.getDapChart(), timeList, resp::setDapChart, resp);
        fillTimeSeriesData(resp.getIssuePrice(), timeList, resp::setIssuePrice, resp);

        return resp;
    }

    /**
     * 【优化】通用方法：将图表数据按时间轴补全
     * 如果图表数据时间点缺失，用 null 填充时间轴
     */
    private void fillTimeSeriesData(List<DataResp> chartData, List<String> timeList, Consumer<List<DataResp>> setter, IndexOverviewResp resp) {
        if (CollectionUtils.isEmpty(chartData)) {
            setter.accept(new ArrayList<>());
            return;
        }
        Map<String, Double> dataMap = chartData.stream()
                .filter(d -> d != null && d.getTime() != null)
                .collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1));
        List<DataResp> filledData = timeList.stream()
                .map(time -> new DataResp(time, dataMap.get(time)))
                .collect(Collectors.toList());
        setter.accept(filledData);
    }

    @Override
    public IndexOverviewResp getPrice(ProfitStatisticsReq profitStatisticsReq) {
        IndexOverviewResp resp = new IndexOverviewResp();
        String nextDate = DateUtils.getAddDate(profitStatisticsReq.getEndTime(), 1);
        String startTime = profitStatisticsReq.getStartTime() + " 00:15:00";
        String endTime = nextDate + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime, 15);
        List<String> dateList = DateUtils.getDayList(profitStatisticsReq.getStartTime(), profitStatisticsReq.getEndTime());
        Map<String, Double> deliveryMap = getDeliveryMap(profitStatisticsReq, dateList);
        Map<String, Double> issueMap = getIssueMap(profitStatisticsReq, dateList);
        //查询火电平均负荷率曲线
        Map<String, Double> avgRtChartMap = getAvgRtChart(profitStatisticsReq, dateList);
        List<DataResp> deliveryList = Lists.newArrayList();
        List<DataResp> issueList = Lists.newArrayList();
       // List<DataResp> avgRtChartList = Lists.newArrayList();
        minuteList.forEach(minute -> {
            DataResp delivery = new DataResp();
            delivery.setTime(minute);
            delivery.setValue(deliveryMap.get(minute));
            deliveryList.add(delivery);
            DataResp issue = new DataResp();
            issue.setTime(minute);
            issue.setValue(issueMap.get(minute));
            issueList.add(issue);

//            DataResp avgRtChart = new DataResp();
//            avgRtChart.setTime(minute);
//            avgRtChart.setValue(avgRtChartMap.get(minute));
//            avgRtChartList.add(avgRtChart);
        });
        resp.setDeliveryPrice(deliveryList);
        resp.setIssuePrice(issueList);
      //  resp.setAvgRtChart(avgRtChartList);
        return resp;
    }

    @Override
    public IndexOverviewTableResp getPriceTable(ProfitStatisticsReq profitStatisticsReq) {
        IndexOverviewTableResp indexOverviewBaseTableResp = new IndexOverviewTableResp();
        List<IndexOverviewBaseTableResp> list = new ArrayList<>();
        String nextDate = DateUtils.getAddDate(profitStatisticsReq.getEndTime(), 1);
        String startTime = profitStatisticsReq.getStartTime() + " 00:15:00";
        String endTime = nextDate + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime, 15);
        List<String> dateList = DateUtils.getDayList(profitStatisticsReq.getStartTime(), profitStatisticsReq.getEndTime());
        Map<String, Double> deliveryMap = getDeliveryMap(profitStatisticsReq, dateList);
        Map<String, Double> issueMap = getIssueMap(profitStatisticsReq, dateList);
        AggregatorResourceType aggregatorResourceType = aggregatorResourceTypeService.getTypeById(profitStatisticsReq.getResourceTypeId());
        String resourceTypeName =null;
        if(null != aggregatorResourceType){
             resourceTypeName = aggregatorResourceType.getName();
        }

        //查询火电平均负荷率曲线
        Map<String, Double> avgRtChartMap = getAvgRtChart(profitStatisticsReq, dateList);
//        List<DataResp> deliveryList = Lists.newArrayList();
//        List<DataResp> issueList = Lists.newArrayList();
//        List<DataResp> avgRtChartList = Lists.newArrayList();
//        minuteList.forEach(minute -> {
//            DataResp delivery = new DataResp();
//            delivery.setTime(minute);
//            delivery.setValue(deliveryMap.get(minute));
//            deliveryList.add(delivery);
//            DataResp issue = new DataResp();
//            issue.setTime(minute);
//            issue.setValue(issueMap.get(minute));
//            issueList.add(issue);
//
//            DataResp avgRtChart = new DataResp();
//            avgRtChart.setTime(minute);
//            avgRtChart.setValue(avgRtChartMap.get(minute));
//            avgRtChartList.add(avgRtChart);
//        });

        String finalResourceTypeName = resourceTypeName;


//        @ApiModelProperty("申报价格")
//        private List<DataResp> deliveryPrice;
//        @ApiModelProperty("出清价格")
//        private List<DataResp> issuePrice;


        dateList.stream().forEach(date->{
            IndexOverviewBaseTableResp deliveryPriceBaseData = new IndexOverviewBaseTableResp();
            deliveryPriceBaseData.setDate(date);
            deliveryPriceBaseData.setSourceTypeName(finalResourceTypeName);
            deliveryPriceBaseData.setType("出清价格");
            deliveryPriceBaseData.setValueMap(getTimeData(date,issueMap));



            IndexOverviewBaseTableResp issueBaseData = new IndexOverviewBaseTableResp();
            issueBaseData.setDate(date);
            issueBaseData.setSourceTypeName(finalResourceTypeName);
            issueBaseData.setType("申报价格");
            issueBaseData.setValueMap(getTimeData(date,deliveryMap));


            IndexOverviewBaseTableResp avgRtBaseData = new IndexOverviewBaseTableResp();
            avgRtBaseData.setDate(date);
            avgRtBaseData.setSourceTypeName(finalResourceTypeName);
            avgRtBaseData.setType("火电平均负荷率");
            avgRtBaseData.setValueMap(getTimeData(date,avgRtChartMap));

            list.add(deliveryPriceBaseData);
            list.add(issueBaseData);
            list.add(avgRtBaseData);
        });
        indexOverviewBaseTableResp.setRowDataList(list);

        return indexOverviewBaseTableResp;
    }

    private Map<String, Double> getTimeData(String date,Map<String, Double> dataMap){
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String time = date + " 00:15:00";
        LocalDateTime localDateTime = LocalDateTime.parse(time,formatter);

        Map<String, Double> resMap = new HashMap<>();
        for(int i=0;i<96;i++){
            LocalDateTime   currentTime = localDateTime.plusMinutes(15*i);
            Double value = dataMap.get(currentTime.format(formatter));
            String key = currentTime.toLocalTime().format(timeFormatter);
            resMap.put(key,value);
        }
        return  resMap;
    }



    @Override
    public List<PriceExcelDateResp> getPriceExcel(ProfitStatisticsReq profitStatisticsReq) {
        String nextDate = DateUtils.getAddDate(profitStatisticsReq.getEndTime(), 1);
        String startTime = profitStatisticsReq.getStartTime() + " 00:00:00";
        String endTime = nextDate + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime, 15);
        List<String> dateList = DateUtils.getDayList(profitStatisticsReq.getStartTime(), profitStatisticsReq.getEndTime());
        Map<String, Double> deliveryMap = getDeliveryMap(profitStatisticsReq, dateList);
        Map<String, Double> issueMap = getIssueMap(profitStatisticsReq, dateList);
        Map<String, Double> avgRtChartMap = getAvgRtChart(profitStatisticsReq, dateList);
        Map<String, List<PriceExcelResp>> dateMap = new HashMap<>();
        minuteList.forEach(minute -> {
            PriceExcelResp priceExcelResp = new PriceExcelResp();
            priceExcelResp.setTime(minute);
            priceExcelResp.setDeliveryPrice(null == deliveryMap.get(minute) ? "" : String.valueOf(deliveryMap.get(minute)));
            priceExcelResp.setIssuePrice(null == issueMap.get(minute) ? "" : String.valueOf(issueMap.get(minute)));
            priceExcelResp.setAvgPower(null == avgRtChartMap.get(minute) ? "" : String.valueOf(avgRtChartMap.get(minute)));
            String date = DateUtils.getDay(minute);
            if (DateUtils.format(minute, "HH:mm:ss").equals("00:00:00")) {
                date = DateUtils.getAddDate(date, -1);
            }
            List<PriceExcelResp> priceList = dateMap.get(date);
            if (null == priceList) {
                priceList = Lists.newArrayList();
            }
            priceList.add(priceExcelResp);
            dateMap.put(date, priceList);
        });
        List<PriceExcelDateResp> resultList = Lists.newArrayList();
        dateMap.entrySet().forEach(dateMapEntry -> {
            PriceExcelDateResp priceExcelDateResp = new PriceExcelDateResp();
            priceExcelDateResp.setDate(dateMapEntry.getKey());
            priceExcelDateResp.setPriceExcelRespList(dateMapEntry.getValue());
            if (null != dateMapEntry.getValue() && dateMapEntry.getValue().size() > 1) {
                resultList.add(priceExcelDateResp);
            }
        });
        if (null != resultList && resultList.size() > 0) {
            return resultList.stream().sorted(Comparator.comparing(PriceExcelDateResp::getDate)).collect(Collectors.toList());
        }
        return resultList;
    }

    @Override
    public List<HistoryQueryDeviceMetricResp> getMetricList() {
        List<HistoryQueryDeviceMetricResp> respList = Lists.newArrayList();
        List<MetricEnum> metricEnumList = MetricEnum.getMetricEnumByFlag(true);
        if (null != metricEnumList && metricEnumList.size() > 0) {
            metricEnumList.forEach(metricEnum -> {
                HistoryQueryDeviceMetricResp resp = new HistoryQueryDeviceMetricResp();
                resp.setMetricCode(metricEnum.getCode());
                resp.setMetricName(metricEnum.getDesc());
                respList.add(resp);
            });
        }
        return respList;
    }

    @Override
    public List<HistoryProfitCalculationExcelResp> getProfitCalculation(String entId, String startDate, String endDate) {
        List<HistoryProfitCalculationExcelResp> respList = Lists.newArrayList();
        List<String> dayList = DateUtils.getDayList(startDate, endDate);
        List<AggregatorEntDateProfit> aggregatorEntDateProfitList = aggregatorEntDateProfitService.getAggregatorEntDateProfitListByEntId(entId, dayList);
        Map<String, AggregatorEntDateProfit> dateMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(aggregatorEntDateProfitList)) {
            dateMap.putAll(aggregatorEntDateProfitList.stream().filter(profit -> null != profit).collect(Collectors.toMap(AggregatorEntDateProfit::getDate, Function.identity(), (v1, v2) -> v1)));
        }
        AtomicDouble totalElectricQuantity = new AtomicDouble();
        AtomicDouble issueProfit = new AtomicDouble();
        AtomicDouble entProfit = new AtomicDouble();
        AtomicDouble offerTotal = new AtomicDouble(0);
        AtomicInteger offerNum = new AtomicInteger(0);
        dayList.forEach(date -> {
            HistoryProfitCalculationExcelResp resp = new HistoryProfitCalculationExcelResp();
            resp.setDate(date);
            AggregatorEntDateProfit profit = dateMap.get(date);
            if (null != profit) {
                totalElectricQuantity.set(MathUtils.addDouble(totalElectricQuantity.get(), profit.getElectricQuantity(), 8));
                issueProfit.set(MathUtils.addDouble(issueProfit.get(), profit.getCountProfit(), 8));
                entProfit.set(MathUtils.addDouble(entProfit.get(), profit.getEntProfit(), 8));
                if (null != profit.getAveragePrice() && 0D != profit.getAveragePrice()) {
                    offerTotal.addAndGet(profit.getAveragePrice());
                    offerNum.getAndIncrement();
                }
                resp.setElectricQuantity(MathUtils.doublePointNotRounding(profit.getElectricQuantity(), 4));
                resp.setIssueProfit(MathUtils.doublePointNotRounding(profit.getCountProfit(), 4));
                resp.setEntProfit(MathUtils.doublePointNotRounding(profit.getEntProfit(), 4));
                resp.setOffer(MathUtils.doublePointNotRounding(profit.getAveragePrice(), 4));
                resp.setElectricOffer(MathUtils.doublePointNotRounding(profit.getCountPrice(), 4));
            }
            respList.add(resp);
        });
        HistoryProfitCalculationExcelResp resp = new HistoryProfitCalculationExcelResp();
        resp.setDate("汇总");
        resp.setElectricQuantity(totalElectricQuantity.get());
        resp.setIssueProfit(issueProfit.get());
        resp.setEntProfit(entProfit.get());
        if (offerNum.get() == 0) {
            resp.setOffer(MathUtils.doublePoint(offerTotal.get(), 8));
        } else {
            resp.setOffer(MathUtils.divideZero(offerTotal.get(), Double.valueOf(offerNum.get()), 8));
        }
        resp.setElectricOffer(MathUtils.divideZero(resp.getIssueProfit(), resp.getElectricQuantity(), 8));
        //处理小数位
        resp.setElectricQuantity(MathUtils.doublePointNotRounding(resp.getElectricQuantity(), 4));
        resp.setIssueProfit(MathUtils.doublePointNotRounding(resp.getIssueProfit(), 4));
        resp.setEntProfit(MathUtils.doublePointNotRounding(resp.getEntProfit(), 4));
        resp.setOffer(MathUtils.doublePointNotRounding(resp.getOffer(), 4));
        resp.setElectricOffer(MathUtils.doublePointNotRounding(resp.getElectricOffer(), 4));
        respList.add(0, resp);
        return respList;
    }

    @Override
    public LinkedHashMap<String, List<HistoryProfitCalculationTimeExcelResp>> getProfitCalculationMap(String entId, String startDate, String endDate) {
        List<String> dayList = DateUtils.getDayList(startDate, endDate);
        List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList = aggregatorDeviceDateProfitService.getAggregatorDeviceDateProfitListByEntId(entId, dayList);
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDateProfitList)) {
            AggregatorEnt aggregatorEnt = aggregatorEntService.getAggregatorEnt(entId);
            String resourceTypeId = aggregatorDeviceDateProfitList.get(0).getResourceTypeId();
            String aggregatorId = aggregatorDeviceDateProfitList.get(0).getAggregatorId();
            List<AggregatorResourceDateIssueOffer> aggregatorResourceDateIssueOfferList = aggregatorResourceDateIssueOfferService.getAggregatorResourceDateIssueOfferList(aggregatorId, resourceTypeId, dayList);
            List<HistoryProfitCalculationTimeExcelResp> historyProfitCalculationTimeExcelRespList = Lists.newArrayList();
            List<AggregatorDeviceDateProfitResp> profitRespList = Lists.newArrayList();
            List<DataResp> profitOfferRespList = Lists.newArrayList();
            aggregatorDeviceDateProfitList.stream().filter(profit -> null != profit && StringUtils.isNotEmpty(profit.getProfitDetail())).forEach(profit -> {
                profitRespList.addAll(JSONArray.parseArray(profit.getProfitDetail(), AggregatorDeviceDateProfitResp.class));
            });
            aggregatorResourceDateIssueOfferList.stream().filter(profit -> null != profit && StringUtils.isNotEmpty(profit.getPriceChart())).forEach(profit -> {
                profitOfferRespList.addAll(JSONArray.parseArray(profit.getPriceChart(), DataResp.class));
            });
            Map<String, Double> offerMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(profitOfferRespList)) {
                offerMap.putAll(profitOfferRespList.stream().collect(toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1)));
            }
            profitRespList.forEach(profit -> profit.setCountPrice(offerMap.get(profit.getEndTime())));
            List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = getAggregatorDeviceDateProfitRespList(profitRespList, aggregatorEntProfitTimeService.getEntMapByEntId(entId));
            Map<String, AggregatorDeviceDateProfitResp> timeMap = aggregatorDeviceDateProfitRespList
                    .stream()
                    .collect(Collectors.toMap(AggregatorDeviceDateProfitResp::getEndTime, Function.identity(), (v1, v2) -> {
                        v1.setElectricQuantity(MathUtils.addDouble(v1.getElectricQuantity(), v2.getElectricQuantity(), 2));
                        v1.setProfit(MathUtils.addDouble(v1.getProfit(), v2.getProfit(), 2));
                        return v1;
                    }));
            timeMap.entrySet().forEach(timeMapEntry -> {
                AggregatorDeviceDateProfitResp profit = timeMapEntry.getValue();
                HistoryProfitCalculationTimeExcelResp historyProfitCalculationTimeExcelResp = new HistoryProfitCalculationTimeExcelResp();
                historyProfitCalculationTimeExcelResp.setDate(DateUtils.format(profit.getEndTime(), "yyyy-MM-dd"));
                historyProfitCalculationTimeExcelResp.setTime(DateUtils.format(profit.getEndTime(), "HH:mm"));
                if (historyProfitCalculationTimeExcelResp.getTime().equals("00:00")) {
                    historyProfitCalculationTimeExcelResp.setDate(DateUtils.getAddDate(historyProfitCalculationTimeExcelResp.getDate(), -1));
                    historyProfitCalculationTimeExcelResp.setTime("24:00");
                }
                historyProfitCalculationTimeExcelResp.setElectricQuantity(profit.getElectricQuantity());
                historyProfitCalculationTimeExcelResp.setOffer(profit.getCountPrice());
                historyProfitCalculationTimeExcelResp.setIssueProfit(profit.getProfit());
                historyProfitCalculationTimeExcelResp.setEntProfit(MathUtils.mulDoubleNull(profit.getProfit(), aggregatorEnt.getPercent(), 2));
                historyProfitCalculationTimeExcelRespList.add(historyProfitCalculationTimeExcelResp);
            });
            List<HistoryProfitCalculationTimeExcelResp> respList = historyProfitCalculationTimeExcelRespList.stream().sorted(Comparator.comparing(HistoryProfitCalculationTimeExcelResp::getDate).thenComparing(HistoryProfitCalculationTimeExcelResp::getTime)).collect(Collectors.toList());
            return respList.stream().collect(Collectors.groupingBy(HistoryProfitCalculationTimeExcelResp::getDate, LinkedHashMap::new, Collectors.toList()));
        }
        return new LinkedHashMap<>();
    }

    /**
     * @description 调节效果excel导出
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    @Override
    public HistoryAdjustExcelResp exportAdjustSituationExcel(AdjustSituationExcelRep adjustSituationExcelRep) {
        //生成表头信息
        List<ExcelExportEntity> excelExportEntityList = createTitle();
        List<String> keyList = excelExportEntityList.stream().map(a->a.getKey().toString()).collect(toList());

        String sourceId = adjustSituationExcelRep.getSourceId();
         String startDate = adjustSituationExcelRep.getStartDate();
        String endDate = adjustSituationExcelRep.getEndDate();
         String aggregatorId = adjustSituationExcelRep.getAggregatorId();
        List<String> dateList = DateUtils.getDayList(startDate, endDate);
        String entId = adjustSituationExcelRep.getEntId();

        // 【修复】在方法开始时声明变量，避免作用域问题
        List<AggregatorEnt> aggregatorEntList = new ArrayList<>();
        Map<String, List<Map<String, String>>> aggregatorExcelData = null;
        Map<String, List<Map<String, String>>> entExcelData = null;
        List<String> entIdList;

        //查询聚合商信息
        AggregatorInfo aggregatorInfo = aggregatorInfoService.getAggregatorInfo(aggregatorId);
        String aggregatorName = aggregatorInfo.getAggregatorName();

        //查询所有企业+聚合商数据
        if (StringUtil.isEmpty(entId)) {
            // 先查询企业列表和ID列表
             aggregatorEntList = aggregatorEntService.getAggregatorEntList(aggregatorId);
            Comparator<AggregatorEnt> comparator = (a, b) -> {
                Collator collator = Collator.getInstance(Locale.CHINA);
                return collator.compare(a.getEntName(), b.getEntName());
            };
            Collections.sort(aggregatorEntList, comparator);
             entIdList = aggregatorEntList.stream().map(AggregatorEnt::getEntId).collect(toList());

            // 【优化】使用 CompletableFuture 并行执行独立查询
            CompletableFuture<List<DataResp>> issueChartFuture = CompletableFuture.supplyAsync(
                () -> getIssueChartNew(aggregatorId, sourceId, dateList), executor);

            // 【优化】一次请求同时获取聚合商功率和企业功率，减少一次大数据调用
            CompletableFuture<PowerDataResult> powerDataFuture = CompletableFuture.supplyAsync(
                () -> getAggregatorAndEntPower(entIdList, startDate, endDate, sourceId), executor);

            CompletableFuture<List<DataResp>> baseLineFuture = CompletableFuture.supplyAsync(
                () -> getAggregatorBaseLine(aggregatorId, sourceId, startDate, endDate), executor);

            CompletableFuture<List<DataResp>> crChartFuture = CompletableFuture.supplyAsync(
                () -> getCrChartNew(aggregatorId, sourceId, startDate, endDate), executor);

            CompletableFuture<List<DataResp>> dapChartFuture = CompletableFuture.supplyAsync(
                () -> getDapChartNew(aggregatorId, sourceId, startDate, endDate), executor);

            CompletableFuture<List<DataResp>> issuePriceFuture = CompletableFuture.supplyAsync(
                () -> getIssuePriceChartNew(aggregatorId, sourceId, startDate, endDate), executor);

            CompletableFuture<Map<String, List<DataResp>>> moreEntAdjustFuture = CompletableFuture.supplyAsync(
                () -> getMoreEntAdjust(entIdList, startDate, endDate, sourceId), executor);

            CompletableFuture<Map<String, List<DataResp>>> moreEntBaseLineFuture = CompletableFuture.supplyAsync(
                () -> getMoreEntBaseLine(entIdList, sourceId, startDate, endDate), executor);

            // 等待所有并行任务完成
            CompletableFuture.allOf(
                issueChartFuture, powerDataFuture, baseLineFuture, crChartFuture,
                dapChartFuture, issuePriceFuture, moreEntAdjustFuture, moreEntBaseLineFuture
            ).join();

            // 获取并行执行结果
            List<DataResp> issueChartList = issueChartFuture.join();
            PowerDataResult powerDataResult = powerDataFuture.join();
            List<DataResp> powerChartList = powerDataResult.getAggregatorPowerList();
            Map<String, List<DataResp>> moreEntPowerMap = powerDataResult.getEntPowerMap();
            List<DataResp> aggregatorBaseLineList = baseLineFuture.join();
            List<DataResp> aggregatorCrCharList = crChartFuture.join();
            List<DataResp> dapChartNew = dapChartFuture.join();
            List<DataResp> issuePriceChartNew = issuePriceFuture.join();
            Map<String, List<DataResp>> moreEntAdjustMap = moreEntAdjustFuture.join();
            Map<String, List<DataResp>> moreEntBaseLineMap = moreEntBaseLineFuture.join();

            //获取聚合商的数据
            aggregatorExcelData = getAggregatorExcelData(keyList, dateList, aggregatorName,
                issueChartList, powerChartList, aggregatorBaseLineList,
                aggregatorCrCharList, dapChartNew, issuePriceChartNew);

            //获取用户的数据
            entExcelData = getEntExcelData(keyList, dateList, aggregatorEntList, aggregatorName,
                entIdList, moreEntAdjustMap, moreEntPowerMap, moreEntBaseLineMap);

        } else {
            // 单个企业查询场景
            AggregatorEnt aggregatorEnt = aggregatorEntService.getAggregatorEnt(entId);
            aggregatorEntList.add(aggregatorEnt);
             entIdList = new ArrayList<>();
            entIdList.add(entId);

            // 【优化】使用 CompletableFuture 并行执行
            CompletableFuture<Map<String, List<DataResp>>> moreEntAdjustFuture = CompletableFuture.supplyAsync(
                () -> getMoreEntAdjust(entIdList, startDate, endDate, sourceId), executor);

            CompletableFuture<Map<String, List<DataResp>>> moreEntPowerFuture = CompletableFuture.supplyAsync(
                () -> getEntPowerChartNew(entIdList, startDate, DateUtils.getAddDate(endDate), sourceId), executor);

            CompletableFuture<Map<String, List<DataResp>>> moreEntBaseLineFuture = CompletableFuture.supplyAsync(
                () -> getMoreEntBaseLine(entIdList, sourceId, startDate, endDate), executor);

            // 等待所有并行任务完成
            CompletableFuture.allOf(moreEntAdjustFuture, moreEntPowerFuture, moreEntBaseLineFuture).join();

            Map<String, List<DataResp>> moreEntAdjustMap = moreEntAdjustFuture.join();
            Map<String, List<DataResp>> moreEntPowerMap = moreEntPowerFuture.join();
            Map<String, List<DataResp>> moreEntBaseLineMap = moreEntBaseLineFuture.join();

            //获取用户的数据
            entExcelData = getEntExcelData(keyList, dateList, aggregatorEntList, aggregatorName,
                entIdList, moreEntAdjustMap, moreEntPowerMap, moreEntBaseLineMap);
        }

        HistoryAdjustExcelResp historyAdjustExcelResp = composeAllExcelData(
            excelExportEntityList, aggregatorExcelData, entExcelData, dateList, entIdList);
        return historyAdjustExcelResp;
    }

    /**
     *
     * <补招数据><功能具体实现>
     *
     * @create：2024/4/29 14:31
     * @author sl
     * @param req
     * @return cn.sl.ehub.upstream.model.resp.HistoryAdjustExcelResp
     */
    @Override
    public HistoryAdjustExcelResp exportBuZhaoUploadData(AdjustSituationExcelRep req) {
        HistoryAdjustExcelResp result = new HistoryAdjustExcelResp();

        //生成表头信息
        List<ExcelExportEntity> excelExportEntityList = createBuZhaoExcelTitle();
        result.setEntityList(excelExportEntityList);

        // 获取聚合商数据
        List<DataResp> aggregatorExcelData = getAggregatorExcelData(req.getAggregatorId(), req.getSourceId(), req.getStartDate(), req.getEndDate());
        if (CollectionUtils.isEmpty(aggregatorExcelData)) {
            result.setAllExcelDataList(new ArrayList<>());
            return result;
        }
        // 处理数据 -按每小时分割数据
        Map<String, List<DataResp>> hourlyDataMap = splitDataByHour(aggregatorExcelData);
        // execl data
        List<Map<String, String>> allExcelDataList = pkgExcelData(hourlyDataMap);
        result.setAllExcelDataList(allExcelDataList);

        return result;
    }

    /**
     *
     * <数据excel><功能具体实现>
     *
     * @create：2024/4/29 14:11
     * @author sl
     * @param hourlyDataMap
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.String>>
     */
    private List<Map<String, String>> pkgExcelData(Map<String, List<DataResp>> hourlyDataMap) {
        if (hourlyDataMap == null || hourlyDataMap.isEmpty()) {
            return new ArrayList<>();
        }

        // 【优化】简化 Comparator，使用 Integer::compareTo 比较
        Comparator<String> numericComparator = Comparator.comparingInt(key -> {
            try {
                if ("-1".equals(key)) {
                    return -1;
                }
                return Integer.parseInt(key);
            } catch (NumberFormatException e) {
                return Integer.MAX_VALUE;
            }
        });

        List<Map<String, String>> dataLists = new ArrayList<>(hourlyDataMap.size());
        for (Map.Entry<String, List<DataResp>> entry : hourlyDataMap.entrySet()) {
            Map<String, String> sortedMap = new TreeMap<>(numericComparator);
            sortedMap.put("-1", entry.getKey());

            for (DataResp v : entry.getValue()) {
                sortedMap.put(v.getTime(),
                    v.getValue() != null ? new BigDecimal(v.getValue()).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() : "0");
            }

            dataLists.add(sortedMap);
        }

        return dataLists;
    }

    /**
     *
     * <excel 数据><功能具体实现>
     *
     * @create：2024/4/29 14:10
     * @author sl
     * @param aggregatorId
     * @param sourceId
     * @param startDate
     * @param endDate
     * @return java.util.List<cn.sl.ehub.upstream.vo.DataResp>
     */
    private List<DataResp> getAggregatorExcelData(String aggregatorId, String sourceId, String startDate, String endDate) {
        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        // 【优化】先获取设备列表，检查是否为空
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getDeviceList(aggregatorId, sourceId);
        if (CollectionUtils.isEmpty(deviceList)) {
            return null;
        }

        // 获取聚合商功率数据
        List<BigDataHistoryResp> pDatas = bigDataHistoryService.getBigData(deviceList,
            Arrays.asList(MetricEnum.YES_POWER.getCode()), startTime, endTime, "0");

        if (CollectionUtils.isEmpty(pDatas)) {
            return null;
        }

        // 【优化】使用 Stream 直接提取和合并数据，避免中间列表
        List<List<DataResp>> dataPointLists = pDatas.stream()
            .map(BigDataHistoryResp::getDataResp)
            .filter(CollectionUtils::isNotEmpty)
            .collect(Collectors.toList());

        return mergeDataPoints(dataPointLists);
    }

    /**
     *
     * <合并多个数据点列表><功能具体实现>
     *
     * @create：2024/4/29 14:10
     * @author sl
     * @param dataPointLists
     * @return java.util.List<cn.sl.ehub.upstream.vo.DataResp>
     */
    private List<DataResp> mergeDataPoints(List<List<DataResp>> dataPointLists) {
        if (CollectionUtils.isEmpty(dataPointLists)) {
            return new ArrayList<>();
        }
        // 使用Stream将所有数据点列表合并为一个Map，其中键为时间戳，值为对应的值的和
        Map<String, Double> mergedMap = dataPointLists.stream()
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(List::stream)
                .filter(data -> data != null && data.getTime() != null)
                .collect(Collectors.groupingBy(DataResp::getTime, Collectors.summingDouble(v -> v.getValue() == null ? 0d : v.getValue())));

        // 根据Map构建新的数据点列表
        return mergedMap.entrySet().stream()
                .map(entry -> new DataResp(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     *
     * <将数据按每小时分割><功能具体实现>
     *
     * @create：2024/4/29 14:09
     * @author sl
     * @param dataPoints
     * @return java.util.Map<java.lang.String,java.util.List<cn.sl.ehub.upstream.vo.DataResp>>
     */
    private Map<String, List<DataResp>> splitDataByHour(List<DataResp> dataPoints) {
        if (CollectionUtils.isEmpty(dataPoints)) {
            return new TreeMap<>();
        }

        // 【优化】一次遍历完成分组、时间转换和排序
        Map<String, List<DataResp>> hourlyDataMap = new TreeMap<>();
        Map<String, List<DataResp>> tempMap = new HashMap<>();

        for (DataResp dataPoint : dataPoints) {
            String hourKey = dataPoint.getTime().substring(0, 13) + ":00:00";
            List<DataResp> hourData = tempMap.computeIfAbsent(hourKey, k -> new ArrayList<>());
            hourData.add(dataPoint);
        }

        // 转换时间格式并排序（一次遍历完成）
        for (List<DataResp> hourData : tempMap.values()) {
            // 转换分钟格式
            for (DataResp time : hourData) {
                time.setTime(DateUtils.format(time.getTime(), "mm"));
            }
            // 排序
            hourData.sort(Comparator.comparingInt(v -> {
                try {
                    return Integer.parseInt(v.getTime());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }));
        }

        return tempMap;
    }

    /**
     *
     * <创建补招数据表头><功能具体实现>
     *
     * @create：2024/4/25 14:30
     * @author sl
     * @param
     * @return java.util.List<cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity>
     */
    private List<ExcelExportEntity> createBuZhaoExcelTitle() {
        String[] titleArray={"时间"};
        List<String> titleList = Arrays.asList(titleArray);
        List<ExcelExportEntity> excelExportEntityList = titleList.stream().map(title -> {
            ExcelExportEntity excelExportEntity = new ExcelExportEntity();
            excelExportEntity.setOrderNum(1);
            excelExportEntity.setName(title);
            excelExportEntity.setKey("-1");
            excelExportEntity.setWidth(20d);

            return excelExportEntity;
        }).collect(Collectors.toList());

        for(int i = 0; i < 60; i++){
            ExcelExportEntity timeExcelExportEntity = new ExcelExportEntity();
            timeExcelExportEntity.setName(i + "");
            timeExcelExportEntity.setOrderNum(1);
            timeExcelExportEntity.setKey(i + "");
            excelExportEntityList.add(timeExcelExportEntity);
        }
        return excelExportEntityList;
    }

    private HistoryAdjustExcelResp composeAllExcelData(List<ExcelExportEntity> entityList, Map<String, List<Map<String, String>>> aggregatorExcelDataMap,
                                     Map<String, List<Map<String, String>>> entExcelDataMap,List<String> dateList,List<String> entIdList){
        HistoryAdjustExcelResp historyAdjustExcelResp = new HistoryAdjustExcelResp();
        historyAdjustExcelResp.setEntityList(entityList);
        List<Map<String, String>> allExcelDataList = new ArrayList<>();
        List<Map<String, String>> finalAllExcelDataList = allExcelDataList;
        dateList.stream().forEach(date->{
            if(CollectionUtil.isNotEmpty(aggregatorExcelDataMap)){
                List<Map<String, String>> aggregatorMap = aggregatorExcelDataMap.get(date);
                finalAllExcelDataList.addAll(aggregatorMap);
            }
            entIdList.stream().forEach(entId->{
                List<Map<String, String>> entMap = entExcelDataMap.get(entId+date);
                finalAllExcelDataList.addAll(entMap);
            });
        });
        if(CollectionUtil.isNotEmpty(finalAllExcelDataList)){
            allExcelDataList =   finalAllExcelDataList.stream().filter(a->CollectionUtil.isNotEmpty(a)).collect(Collectors.toList());
        }
        historyAdjustExcelResp.setAllExcelDataList(allExcelDataList);
        return historyAdjustExcelResp;

    }

    /**
     * @description 获取聚合商excel
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private Map<String, List<Map<String, String>>> getAggregatorExcelData(List<String> keyList, List<String> dateList, String aggregatorName, List<DataResp> issueChartList, List<DataResp> powerChartList, List<DataResp> aggregatorBaseLineList,List<DataResp> aggregatorCrChartNew, List<DataResp>  dapChartNew, List<DataResp> issuePriceChartNew) {
        //组装聚合商数据
        //1.调度下发功率
        Map<String, Map<String, String>> aggregatorIssueDataMap = dealAggregatorData(keyList, dateList, aggregatorName, issueChartList,"聚合申报功率");

        //2.实际汇总功率
        Map<String, Map<String, String>> aggregatorPowerChartMap = dealAggregatorData(keyList, dateList, aggregatorName, powerChartList,"实际汇总功率");

        //3.基线
        Map<String, Map<String, String>> aggregatorBaseLineMap = dealAggregatorData(keyList, dateList, aggregatorName, aggregatorBaseLineList,"基线");

        //4.Cr
        Map<String, Map<String, String>> aggregatorCrLineMap = dealAggregatorData(keyList, dateList, aggregatorName, aggregatorCrChartNew,"碳排因子");

        //5. dap曲线
        Map<String, Map<String, String>> aggregatorDapLineMap = dealAggregatorData(keyList, dateList, aggregatorName, dapChartNew,"调度下发功率");

        // 6.出清价格
        Map<String, Map<String, String>> aggregatorIssuePriceLineMap = dealAggregatorData(keyList, dateList, aggregatorName, issuePriceChartNew,"出清价格");


        Map<String, List<Map<String, String>>> aggregatorRowDataMap = composeAggregatorData(aggregatorIssueDataMap, aggregatorPowerChartMap, aggregatorBaseLineMap,aggregatorCrLineMap,aggregatorDapLineMap,aggregatorIssuePriceLineMap,dateList);
        return aggregatorRowDataMap;
    }

    /**
     * @description 获取企业用户数据
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private Map<String, List<Map<String, String>>> getEntExcelData(List<String> keyList, List<String> dateList, List<AggregatorEnt> aggregatorEntList, String aggregatorName, List<String> entIdList, Map<String, List<DataResp>> moreEntAdjustMap, Map<String, List<DataResp>> moreEntPowerMap, Map<String, List<DataResp>> moreEntBaseLineMap) {
        //有效调节负荷
        Map<String, Map<String, String>> allEntAdjustMap = dealEntData(keyList, dateList, aggregatorEntList, aggregatorName, CollectionUtil.isNotEmpty(moreEntAdjustMap)?moreEntAdjustMap:new HashMap<>(), "有效调节负荷");

        //实际调节负荷
        Map<String, Map<String, String>> allEntPowerMap = dealEntData(keyList, dateList, aggregatorEntList, aggregatorName, CollectionUtil.isNotEmpty(moreEntPowerMap)?moreEntPowerMap:new HashMap<>() , "实际调节负荷");

        //用户基线
        Map<String, Map<String, String>> allEntBaseLineMap = dealEntData(keyList, dateList, aggregatorEntList, aggregatorName, CollectionUtil.isNotEmpty(moreEntBaseLineMap)?moreEntBaseLineMap:new HashMap<>() , "基线");

        Map<String, List<Map<String, String>>> entDataMap = composeEntData(allEntAdjustMap, allEntPowerMap, allEntBaseLineMap,dateList,entIdList);
        return entDataMap;
    }

    /**
     * @description
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private Map<String, List<Map<String, String>>> composeEntData(Map<String, Map<String, String>> allEntAdjustMap,
                                                                  Map<String, Map<String, String>> allEntPowerMap,
                                                                  Map<String, Map<String, String>> allEntBaseLineMap,
                                                                  List<String> dateList,
                                                                  List<String> entIdList){
        Map<String, List<Map<String, String>>> map = new HashMap<>();
        entIdList.stream().forEach(entId->
                dateList.stream().forEach(date->{
                    List<Map<String, String>> list = new ArrayList<>();
                    String key = entId + date;
                    Map<String, String> adjustMap = allEntAdjustMap.get(key);
            Map<String, String> powerMap = allEntPowerMap.get(key);
            Map<String, String> baseLineMap = allEntBaseLineMap.get(key);
            list.add(adjustMap);
            list.add(powerMap);
            list.add(baseLineMap);
            map.put(key,list);
        }
        ));


        return map;
    }

    /**
     * @description 组合聚合商 调度下发数据/实际汇总功率/基线 数据
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private Map<String, List<Map<String, String>>> composeAggregatorData(Map<String, Map<String, String>> aggregatorIssueDataMap,Map<String, Map<String, String>> aggregatorPowerChartMap,Map<String, Map<String, String>> aggregatorBaseLineMap,Map<String,
            Map<String, String>> aggregatorCrLineMap,Map<String, Map<String, String>> aggregatorDapLineMap,Map<String, Map<String, String>>  aggregatorIssuePriceLineMap,
                                                                         List<String> dateList){
        Map<String, List<Map<String, String>>> map = new HashMap<>();
        dateList.stream().forEach(date->{
            List<Map<String, String>> list = new ArrayList<>();
            Map<String, String> issueDataMap = aggregatorIssueDataMap.get(date);
            Map<String, String> powerMap = aggregatorPowerChartMap.get(date);
            Map<String, String> baseLineMap = aggregatorBaseLineMap.get(date);
            Map<String, String> crLineMap = aggregatorCrLineMap.get(date);
            Map<String, String> dapLineMap = aggregatorDapLineMap.get(date);
            Map<String, String> issuePriceLineMap = aggregatorIssuePriceLineMap.get(date);

            list.add(issueDataMap);
            list.add(powerMap);
            list.add(baseLineMap);
            list.add(crLineMap);
            list.add(dapLineMap);
            list.add(issuePriceLineMap);
           map.put(date,list);
        });
        return map;
    }

    private Map<String, String> parseTime(Map<String, String> map){
        Map<String, String> resMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        if(CollectionUtil.isNotEmpty(map)){
            map.forEach((key,value)->{
                LocalDateTime localDateTime = LocalDateTime.parse(key, formatter);
                String newKey = localDateTime.toLocalTime().format(timeFormatter);
                resMap.put(newKey,value);
            });
            return resMap;
        }
        return map;
    }



   /**
    * @description 处理用户数据
    * @param
    * @return
    * @author sl
    * @date 2026-05-28
    */
    private Map<String, Map<String, String>> dealEntData(List<String> keyList, List<String> dateList, List<AggregatorEnt> entList,String aggregatorName ,Map<String, List<DataResp>> dataMap,String powerName) {
        Map<String, Map<String, String>> moreRowDataMap = new HashMap<>();

        entList.stream().forEach(entInfo->{
            List<DataResp> dataResps = dataMap.get(entInfo.getEntId());
            if(CollectionUtil.isNotEmpty(dataResps)){


           // Map<String, String> map = dataResps.stream().collect(Collectors.toMap(a -> a.getTime(), b -> (null != b.getValue() ? b.getValue().toString():null)));
            Map<String, String> map = new HashMap<>();
                dataResps.stream().forEach(a->{
                    String value= null != a.getValue() ? a.getValue().toString(): null;
                    map.put(a.getTime(),value);
                });


            dateList.stream().forEach(date->{
                Map<String, String> rowDataMap = new HashMap<>();

                keyList.stream().forEach(key->{
                    if("date".equals(key)){
                        rowDataMap.put(key,date);
                    }else if("aggregatorName".equals(key)){
                        rowDataMap.put(key,aggregatorName);
                    }else if("entName".equals(key)){
                        rowDataMap.put(key,entInfo.getEntName());
                    }else if("power".equals(key)){
                        rowDataMap.put(key,powerName);
                    }else {
                        String time = date + " " + key+":00";
                        String value = map.get(time);
                        rowDataMap.put(key,value);
                    }
                });
                moreRowDataMap.put(entInfo.getEntId()+date,rowDataMap);
        });


            }else {

                dateList.stream().forEach(date->{
                    Map<String, String> rowDataMap = new HashMap<>();

                    keyList.stream().forEach(key->{
                        if("date".equals(key)){
                            rowDataMap.put(key,date);
                        }else if("aggregatorName".equals(key)){
                            rowDataMap.put(key,aggregatorName);
                        }else if("entName".equals(key)){
                            rowDataMap.put(key,entInfo.getEntName());
                        }else if("power".equals(key)){
                            rowDataMap.put(key,powerName);
                        }else {
                            rowDataMap.put(key,null);
                        }
                    });
                    moreRowDataMap.put(entInfo.getEntId()+date,rowDataMap);
                });
            }
        });
        return moreRowDataMap;
    }


    /**
     * @description 获取聚合商--调度下发数据/实际汇总功率/基线
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private Map<String, Map<String, String>> dealAggregatorData(List<String> keyList, List<String> dateList, String aggregatorName, List<DataResp> issueChartList,String powerName) {
        Map<String, String> map = issueChartList.stream().collect(toMap(a -> a.getTime(), b -> b.getValue().toString(),(a,b)->a));
        Map<String, Map<String, String>> moreRowDataMap = new HashMap<>();
        dateList.stream().forEach(date->{
            Map<String, String> rowDataMap = new HashMap<>();

            //需要聚合商的数据
            Map<String, String> finalRowDataMap = rowDataMap;
            keyList.stream().forEach(key->{
                if("date".equals(key)){
                    finalRowDataMap.put(key,date);
                }else if("aggregatorName".equals(key)){
                    finalRowDataMap.put(key,aggregatorName);
                }else if("entName".equals(key)){
                    finalRowDataMap.put(key,null);
                }else if("power".equals(key)){
                    finalRowDataMap.put(key,powerName);
                }else {
                    String time = date + " " + key+":00";
                    String value = map.get(time);
                    finalRowDataMap.put(key,value);
                }
            });



            List<String> sortKeys = new ArrayList<>(finalRowDataMap.keySet());
            //进行排序
            Collections.sort(sortKeys);
              for(String key:finalRowDataMap.keySet()){
                  rowDataMap.put(key,finalRowDataMap.get(key));
              }

            moreRowDataMap.put(date,rowDataMap);

        });
        return moreRowDataMap;
    }


    /**
     * @description 创建excel表头
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private List<ExcelExportEntity> createTitle(){
        String[] titleArray={"日期","聚合商名称","用户名称","功率(kw)"};
        List<String> titleList = Arrays.asList(titleArray);
        List<ExcelExportEntity> excelExportEntityList = titleList.stream().map(a -> {
            ExcelExportEntity excelExportEntity = new ExcelExportEntity();
            excelExportEntity.setOrderNum(1);
            excelExportEntity.setName(a);
            excelExportEntity.setKey(getKeyByName(a));
            if(a.equals("日期")||a.equals("功率(kw)")){
                excelExportEntity.setWidth(15d);
            }else if(a.equals("聚合商名称")||a.equals("用户名称")){
                excelExportEntity.setWidth(20d);
            }

            return excelExportEntity;
        }).collect(Collectors.toList());


        for(int i=0;i<96;i++){
            LocalTime time = LocalTime.of(0, 15);
            time =  time.plusMinutes(15*i);
            String formatTime = time.format(DateTimeFormatter.ofPattern("HH:mm"));
            ExcelExportEntity timeExcelExportEntity = new ExcelExportEntity();
            timeExcelExportEntity.setName(formatTime);
            timeExcelExportEntity.setOrderNum(1);
            timeExcelExportEntity.setKey(formatTime);
            excelExportEntityList.add(timeExcelExportEntity);
        }
        return excelExportEntityList;

    }

    private String getKeyByName(String name){
        switch (name){
            case "日期":
                return "date";
            case "聚合商名称":
                return "aggregatorName";
            case "用户名称":
                return "entName";
            case "功率(kw)":
                return "power";
        }
        return null;
    }


    /**
     * 查询申报价格
     *
     * @param profitStatisticsReq
     * @param dateList
     * @return
     */
    private Map<String, Double> getDeliveryMap(ProfitStatisticsReq profitStatisticsReq, List<String> dateList) {
        Map<String, Double> deliveryMap = new HashMap<>();
        List<AggregatorResourceDateDeliveryOffer> aggregatorResourceDateDeliveryOfferList = aggregatorResourceDateDeliveryOfferService.getAggregatorResourceDateDeliveryOfferList(profitStatisticsReq.getAggregatorId(), profitStatisticsReq.getResourceTypeId(), dateList, "1");
        if (null != aggregatorResourceDateDeliveryOfferList && aggregatorResourceDateDeliveryOfferList.size() > 0) {
//            aggregatorResourceDateDeliveryOfferList.stream().filter(offer -> null != offer && StringUtils.isNotEmpty(offer.getPriceDetail())).forEach(aggregatorResourceDateDeliveryOffer -> {
//                String priceDetail = aggregatorResourceDateDeliveryOffer.getPriceDetail();
//                List<AggregatorResourceDateDeliveryOfferResp> aggregatorResourceDateDeliveryOfferRespList = JSONArray.parseArray(priceDetail, AggregatorResourceDateDeliveryOfferResp.class);
//                if (null != aggregatorResourceDateDeliveryOfferRespList && aggregatorResourceDateDeliveryOfferRespList.size() > 0) {
//                    aggregatorResourceDateDeliveryOfferRespList.forEach(aggregatorResourceDateDeliveryOfferResp -> {
//                        String startTimeWithResp = aggregatorResourceDateDeliveryOffer.getDate() + " " + aggregatorResourceDateDeliveryOfferResp.getStartTime() + ":00";
//                        String endTimeWithResp = aggregatorResourceDateDeliveryOffer.getDate() + " " + aggregatorResourceDateDeliveryOfferResp.getEndTime() + ":00";
//                        List<String> minuteListWithResp = DateUtils.getMinuteList(startTimeWithResp, endTimeWithResp, 15);
//                        minuteListWithResp.forEach(minute -> {
//                            deliveryMap.put(minute, aggregatorResourceDateDeliveryOfferResp.getOffer());
//                        });
//                    });
//                }
//            });
            aggregatorResourceDateDeliveryOfferList.stream().filter(offer -> null != offer && StringUtils.isNotEmpty(offer.getPriceChart())).forEach(aggregatorResourceDateDeliveryOffer -> {
                String priceChart = aggregatorResourceDateDeliveryOffer.getPriceChart();
                List<DataResp> dataRespList = JSONArray.parseArray(priceChart, DataResp.class);
                if (null != dataRespList && dataRespList.size() > 0) {
                    deliveryMap.putAll(dataRespList.stream().collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1)));
                }
            });
        }
        return deliveryMap;
    }

    /**
     * @description 查询火电负荷率曲线
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private Map<String, Double> getAvgRtChart(ProfitStatisticsReq profitStatisticsReq, List<String> dateList){
        Map<String, Double> avgChartMap = new HashMap<>();

        List<AggregatorAvgRtChart> avgChartList = aggregatorAvgRtChartService.getAvgRtChart(profitStatisticsReq.getAggregatorId(), profitStatisticsReq.getResourceTypeId(), dateList);
        if(CollectionUtil.isNotEmpty(avgChartList)){
            avgChartList.stream().filter(chart -> null != chart && StringUtils.isNotEmpty(chart.getAvgRtChart())).forEach(a -> {
                String avgRtChart = a.getAvgRtChart();
                List<DataResp> dataRespList = JSONArray.parseArray(avgRtChart, DataResp.class);
                if (null != dataRespList && dataRespList.size() > 0) {
                    avgChartMap.putAll(dataRespList.stream().collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1)));
                }
            });
        }
        return  avgChartMap;
    }

    /**
     * 查询出清价格
     *
     * @param profitStatisticsReq
     * @param dateList
     * @return
     */
    private Map<String, Double> getIssueMap(ProfitStatisticsReq profitStatisticsReq, List<String> dateList) {
        Map<String, Double> issueMap = new HashMap<>();
        List<AggregatorResourceDateIssueOffer> aggregatorResourceDateIssueOfferList = aggregatorResourceDateIssueOfferService.getAggregatorResourceDateIssueOfferList(profitStatisticsReq.getAggregatorId(), profitStatisticsReq.getResourceTypeId(), dateList);
        if (null != aggregatorResourceDateIssueOfferList && aggregatorResourceDateIssueOfferList.size() > 0) {
//            aggregatorResourceDateIssueOfferList.stream().filter(offer -> null != offer && StringUtils.isNotEmpty(offer.getPriceDetail())).forEach(aggregatorResourceDateIssueOffer -> {
//                String priceDetail = aggregatorResourceDateIssueOffer.getPriceDetail();
//                List<AggregatorResourceDateDeliveryOfferResp> aggregatorResourceDateOfferRespList = JSONArray.parseArray(priceDetail, AggregatorResourceDateDeliveryOfferResp.class);
//                if (null != aggregatorResourceDateOfferRespList && aggregatorResourceDateOfferRespList.size() > 0) {
//                    aggregatorResourceDateOfferRespList.forEach(aggregatorResourceDateOfferResp -> {
//                        String startTimeWithResp = aggregatorResourceDateIssueOffer.getDate() + " " + aggregatorResourceDateOfferResp.getStartTime() + ":00";
//                        String endTimeWithResp = aggregatorResourceDateIssueOffer.getDate() + " " + aggregatorResourceDateOfferResp.getEndTime() + ":00";
//                        List<String> minuteListWithResp = DateUtils.getMinuteList(startTimeWithResp, endTimeWithResp, 15);
//                        minuteListWithResp.forEach(minute -> {
//                            issueMap.put(minute, aggregatorResourceDateOfferResp.getOffer());
//                        });
//                    });
//                }
//            });
            aggregatorResourceDateIssueOfferList.stream().filter(offer -> null != offer && StringUtils.isNotEmpty(offer.getPriceChart())).forEach(aggregatorResourceDateIssueOffer -> {
                String priceChart = aggregatorResourceDateIssueOffer.getPriceChart();
                List<DataResp> dataRespList = JSONArray.parseArray(priceChart, DataResp.class);
                if (null != dataRespList && dataRespList.size() > 0) {
                    issueMap.putAll(dataRespList.stream().collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1)));
                }
            });
        }
        return issueMap;
    }

    /**
     * 查询调度下发功率曲线
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param dateList
     * @param minuteList
     * @param resp
     */
    private void getIssueChart(String aggregatorId, String resourceTypeId, List<String> dateList, List<String> minuteList, IndexOverviewResp resp) {
        List<DataResp> issueChartList = Lists.newArrayList();
        Map<String, Double> dataRespMap = new HashMap<>();
        List<AggregatorDateIssueChart> aggregatorDateIssueChartList = aggregatorDateIssueChartService.getAggregatorDateIssueChartList(aggregatorId, resourceTypeId, dateList);
        if (null != aggregatorDateIssueChartList && aggregatorDateIssueChartList.size() > 0) {
            List<DataResp> totalDataRespList = Lists.newArrayList();
            aggregatorDateIssueChartList.stream().filter(chart -> null != chart && StringUtils.isNotEmpty(chart.getIssueChart())).forEach(aggregatorDateIssueChart -> {
                String issueChart = aggregatorDateIssueChart.getIssueChart();
                List<DataResp> dataRespList = JSONArray.parseArray(issueChart, DataResp.class);
                if (null != dataRespList && dataRespList.size() > 0) {
                    totalDataRespList.addAll(dataRespList);
                }
            });
            if (null != totalDataRespList && totalDataRespList.size() > 0) {
                dataRespMap = totalDataRespList.stream().collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1 + v2));
            }
        }
        if (null == dataRespMap || dataRespMap.size() <= 0) {
            dataRespMap = new HashMap<>();
        }
        for (int i = minuteList.size() - 1; i > 0; i -= 15) {
            for (int j = i; j >= i - 14; j--) {
                DataResp dataResp = new DataResp();
                dataResp.setTime(minuteList.get(j));
                dataResp.setValue(dataRespMap.get(minuteList.get(i)));
                issueChartList.add(0, dataResp);
            }
        }
        resp.setIssueChart(issueChartList);
    }
    /**
     * @description 调度下发功率
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private List<DataResp> getIssueChartNew(String aggregatorId, String resourceTypeId, List<String> dateList) {
        Map<String, Double> dataRespMap = new HashMap<>();
        List<DataResp> issueChartList = new ArrayList<>();

        List<AggregatorDateIssueChart> aggregatorDateIssueChartList = aggregatorDateIssueChartService.getAggregatorDateIssueChartListNew(aggregatorId, resourceTypeId, dateList);
        if (null != aggregatorDateIssueChartList && aggregatorDateIssueChartList.size() > 0) {
            issueChartList = aggregatorDateIssueChartList.stream().flatMap(a -> {
                String issueChart = a.getIssueChart();
                List<DataResp> list = JSONObject.parseArray(issueChart, DataResp.class);
                return list.stream();
            }).collect(toList());
        }

        return issueChartList;
    }


    /**
     * @description 调度下发功率
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private void getIssueChartNew(String aggregatorId, String resourceTypeId, List<String> dateList, List<String> minuteList, IndexOverviewResp resp) {
        Map<String, Double> dataRespMap = new HashMap<>();
        List<DataResp> issueChartList = new ArrayList<>();
        List<AggregatorDateIssueChart> aggregatorDateIssueChartList = aggregatorDateIssueChartService.getAggregatorDateIssueChartListNew(aggregatorId, resourceTypeId, dateList);
        if (null != aggregatorDateIssueChartList && aggregatorDateIssueChartList.size() > 0) {
             issueChartList = aggregatorDateIssueChartList.stream().flatMap(a -> {
                String issueChart = a.getIssueChart();
                List<DataResp> list = JSONObject.parseArray(issueChart, DataResp.class);
                return list.stream();
            }).collect(toList());
        }

        resp.setIssueChart(issueChartList);
    }

    /**
     * @description Cr曲线查询
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private void getCrChartNew(String aggregatorId, String resourceTypeId, List<String> dateList, List<String> minuteList, IndexOverviewResp resp) {
        Map<String, Double> dataRespMap = new HashMap<>();
        List<DataResp> issueChartList = new ArrayList<>();
        List<AggregatorCrChart> aggregatorCrChartList = aggregatorCrChartService.getAggregatorDateCrChartListNew(aggregatorId, resourceTypeId, dateList);
        if (null != aggregatorCrChartList && aggregatorCrChartList.size() > 0) {
            issueChartList = aggregatorCrChartList.stream().flatMap(a -> {
                String crChart = a.getCrLoadChart();
                List<DataResp> list = JSONObject.parseArray(crChart, DataResp.class);
                return list.stream();
            }).sorted(Comparator.comparing(DataResp::getTime)).collect(toList());
        }

        resp.setCrChart(issueChartList);
    }

    // 聚合商 dap曲线查询
    private void getDapChartNew(String aggregatorId, String resourceTypeId, List<String> dateList, List<String> minuteList, IndexOverviewResp resp) {
        Map<String, Double> dataRespMap = new HashMap<>();
        List<DataResp> issueChartList = new ArrayList<>();
        List<AggregatorDapChart> aggregatorDapChartList = aggregatorDapChartService.getAggregatorDateDapChartListNew(aggregatorId, resourceTypeId, dateList);
        if (null != aggregatorDapChartList && aggregatorDapChartList.size() > 0) {
            issueChartList = aggregatorDapChartList.stream().flatMap(a -> {
                String dapChart = a.getDapChart();
                List<DataResp> list = JSONObject.parseArray(dapChart, DataResp.class);
                return list.stream();
            }).sorted(Comparator.comparing(DataResp::getTime)).collect(toList());
        }

        resp.setDapChart(issueChartList);
    }


    // 出清价格曲线
    private void getIssuePriceChartNew(String aggregatorId, String resourceTypeId, List<String> dateList, List<String> minuteList, IndexOverviewResp resp) {
        Map<String, Double> dataRespMap = new HashMap<>();
        List<DataResp> issueChartList = new ArrayList<>();
        List<AggregatorResourceDateIssueOffer> aggregatorResourceDateIssueOfferList = aggregatorResourceDateIssueOfferService.
                getAggregatorResourceDateIssueOfferList(aggregatorId, resourceTypeId, dateList);
        if (null != aggregatorResourceDateIssueOfferList && aggregatorResourceDateIssueOfferList.size() > 0) {
            issueChartList = aggregatorResourceDateIssueOfferList.stream().flatMap(a -> {
                String issuePriceChart = a.getPriceChart();
                List<DataResp> list = JSONObject.parseArray(issuePriceChart, DataResp.class);
                return list.stream();
            }).sorted(Comparator.comparing(DataResp::getTime)).collect(toList());
        }

        if (issueChartList == null) issueChartList = Lists.newArrayList();

        resp.setIssuePrice(issueChartList);
    }




    /**
     * @description 聚合商Cr曲线查询
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private List<DataResp> getCrChartNew(String aggregatorId, String resourceTypeId, String startDate, String endDate) {
        List<DataResp> crChartList = new ArrayList<>();
        List<AggregatorCrChart> aggregatorCrChartList = aggregatorCrChartService.getAggregatorCrLine(aggregatorId, resourceTypeId, startDate,endDate);

        if (CollectionUtil.isNotEmpty(aggregatorCrChartList)) {
            crChartList = aggregatorCrChartList.stream().flatMap(a -> {
                String crChart = a.getCrLoadChart();
                List<DataResp> list = JSONObject.parseArray(crChart, DataResp.class);
                return list.stream();
            }).sorted(Comparator.comparing(DataResp::getTime)).collect(toList());
        }
        return crChartList;
    }

    private List<DataResp> getDapChartNew(String aggregatorId, String resourceTypeId, String startDate, String endDate) {
        List<DataResp> dapChartList = new ArrayList<>();
        List<AggregatorDapChart> aggregatorCrChartList = aggregatorDapChartService.getAggregatorDapChart(aggregatorId, resourceTypeId, startDate,endDate);

        if (CollectionUtil.isNotEmpty(aggregatorCrChartList)) {
            dapChartList = aggregatorCrChartList.stream().flatMap(a -> {
                String dapChart = a.getDapChart();
                List<DataResp> list = JSONObject.parseArray(dapChart, DataResp.class);
                return list.stream();
            }).sorted(Comparator.comparing(DataResp::getTime)).collect(toList());
        }
        return dapChartList;
    }


    private List<DataResp> getIssuePriceChartNew(String aggregatorId, String resourceTypeId, String startDate, String endDate) {
        List<DataResp> dapChartList = new ArrayList<>();
        List<AggregatorResourceDateIssueOffer> aggregatorCrChartList = aggregatorResourceDateIssueOfferService.getAggregatorIssuePriceChart(aggregatorId, resourceTypeId, startDate,endDate);
        if (CollectionUtil.isNotEmpty(aggregatorCrChartList)) {
            dapChartList = aggregatorCrChartList.stream().flatMap(a -> {
                String issuePriceChart = a.getPriceChart();
                List<DataResp> list = JSONObject.parseArray(issuePriceChart, DataResp.class);
                return list.stream();
            }).sorted(Comparator.comparing(DataResp::getTime)).collect(toList());
        }
        return dapChartList;
    }

    /**
     * @description 查询聚合商基线数据
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private List<DataResp>  getAggregatorBaseLine(String aggregatorId, String resourceTypeId, String startDate, String endDate){
        List<DataResp> baseLineDataList = new ArrayList<>();
        List<AggregatorBaseLineLoadChart> baseLineList = aggregatorBaseLineLoadChartService.getAggregatorBaseLine(aggregatorId, resourceTypeId, startDate, endDate);
        if(CollectionUtil.isNotEmpty(baseLineList)){
             baseLineDataList = baseLineList.stream().flatMap(a -> {
                String baseLineLoadChart = a.getBaseLineLoadChart();
                List<DataResp> list = JSONObject.parseArray(baseLineLoadChart, DataResp.class);
                return list.stream();

            }).sorted(Comparator.comparing(DataResp::getTime)).collect(toList());
        }
        return  baseLineDataList;

    }

    /**
     * @description 查询聚合商基线数据
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private void getAggregatorBaseLine(String aggregatorId, String resourceTypeId, String startDate, String endDate, IndexOverviewResp resp){
        List<AggregatorBaseLineLoadChart> baseLineList = aggregatorBaseLineLoadChartService.getAggregatorBaseLine(aggregatorId, resourceTypeId, startDate, endDate);
        if(CollectionUtil.isNotEmpty(baseLineList)){
            List<DataResp> baseLineDataList = baseLineList.stream().flatMap(a -> {
                String baseLineLoadChart = a.getBaseLineLoadChart();
                List<DataResp> list = JSONObject.parseArray(baseLineLoadChart, DataResp.class);
                return list.stream();

            }).sorted(Comparator.comparing(DataResp::getTime)).collect(toList());

            resp.setBaseLineChart(baseLineDataList);
        }

    }

    /**
     * @description 查询企业基线
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private Map<String, List<DataResp>> getMoreEntBaseLine(List<String> entIdList,String resourceTypeId, String startDate, String endDate){
        Map<String, List<AggregatorEntBaseLineLoadChart>> map = aggregatorEntBaseLineLoadChartService.getMoreEntBaseLine(entIdList, resourceTypeId, startDate, endDate);
        Map<String, List<DataResp>> resMap =  new HashMap<>();
        if(CollectionUtil.isNotEmpty(map)){
            map.forEach((key,value)->{
                List<DataResp> dataRespList = value.stream().flatMap(a -> {
                    String baseLineLoadChart = a.getBaseLineLoadChart();
                    List<DataResp> list = JSONObject.parseArray(baseLineLoadChart, DataResp.class);
                    return list.stream();
                }).collect(toList());
                resMap.put(key,dataRespList);
            });
        }
        return resMap;

    }

    /**
     * @description 查询企业基线
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private void getEntBaseLine(String entId, String resourceTypeId, String startDate, String endDate, HistoryQueryGraphVO resp){
        List<AggregatorEntBaseLineLoadChart> baseLineList = aggregatorEntBaseLineLoadChartService.getEntBaseLine(entId, resourceTypeId, startDate, endDate);
        if(CollectionUtil.isNotEmpty(baseLineList)){
            List<DataResp> baseLineDataList = baseLineList.stream().flatMap(a -> {
                String baseLineLoadChart = a.getBaseLineLoadChart();
                List<DataResp> list = JSONObject.parseArray(baseLineLoadChart, DataResp.class);
                return list.stream();

            }).collect(toList());
            resp.setBaseLineChart(baseLineDataList);
        }

    }

    List<DataResp>  getAggregatorPowerChartNew( String aggregatorId, String resourceTypeId, String startDate,String endDate){
        String startTime = startDate + " 00:00:00";
        String endTime = DateUtils.getAddDate(endDate) + " 00:00:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateTime = LocalDateTime.parse(startTime, formatter).plusMinutes(15);
        LocalDateTime endDateTime = LocalDateTime.parse(endTime, formatter);
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getDeviceList(aggregatorId, resourceTypeId);
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime);
        Map<String, Map<String, Double>> map = getRealTimeAvgPower(deviceList, startTime, endTime, minuteList);
        List<DataResp> dataRespList = new ArrayList<>();
        while (startDateTime.isBefore(endDateTime) || startDateTime.isEqual(endDateTime)){
            String time = startDateTime.format(formatter);
            AtomicReference<Double> totalValue=new AtomicReference<Double>();
            totalValue.set(0d);
            map.forEach((key,value)->{
                totalValue.updateAndGet(v->v+ (null != value.get(time) ? value.get(time) : Double.valueOf(0d)));
            });
            DataResp dataResp = new DataResp();
            dataResp.setTime(time);
            dataResp.setValue(new BigDecimal(totalValue.get()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
            dataRespList.add(dataResp);
            startDateTime = startDateTime.plusMinutes(15);
        }
       return dataRespList;
    }

    private void getAggregatorPowerChartNew(String aggregatorId, String resourceTypeId, String startDate, String endDate, IndexOverviewResp resp) {
        String startTime = startDate + " 00:00:00";
        String endTime = DateUtils.getAddDate(endDate) + " 00:00:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateTime = LocalDateTime.parse(startTime, formatter).plusMinutes(15);
        LocalDateTime endDateTime = LocalDateTime.parse(endTime, formatter);
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getDeviceList(aggregatorId, resourceTypeId);
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime);
        Map<String, Map<String, Double>> map = getRealTimeAvgPower(deviceList, startTime, endTime, minuteList);
        List<DataResp> dataRespList = new ArrayList<>();
        while (startDateTime.isBefore(endDateTime) || startDateTime.isEqual(endDateTime)){
            String time = startDateTime.format(formatter);
            AtomicReference<Double> totalValue=new AtomicReference<Double>();
            totalValue.set(0d);
            map.forEach((key,value)->{
                totalValue.updateAndGet(v->v+ (null != value.get(time) ? value.get(time) : 0d));
            });
            DataResp dataResp = new DataResp();
            dataResp.setTime(time);
            dataResp.setValue(BigDecimal.valueOf(totalValue.get()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
            dataRespList.add(dataResp);
            startDateTime = startDateTime.plusMinutes(15);
        }
        resp.setPowerChart(dataRespList);

    }



        /**
         * 查询实时功率曲线
         *
         * @param simulate
         * @param aggregatorId
         * @param resourceTypeId
         * @param dateList
         * @param minuteList
         * @param resp
         */
    private void getPowerChart(String simulate, String aggregatorId, String resourceTypeId, List<String> dateList, List<String> minuteList, IndexOverviewResp resp) {
        List<DataResp> powerList = Lists.newArrayList();
        Map<String, Double> valueMap = new HashMap<>();
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(aggregatorId, null, null, resourceTypeId);
        List<DeviceGroupPointInfo> deviceGroupPointInfoList = aggregatorDevicePointService.getDeviceGroupPointInfoList(deviceList, MetricEnum.YES_POWER.getCode());
        List<BigDataHistoryAndCalculationResp> bigDataHistoryAndCalculationRespList = bigDataHistoryService.queryBigData(deviceList, deviceGroupPointInfoList, "1minute", minuteList.get(0), minuteList.get(minuteList.size() - 1), simulate);
        if (null != bigDataHistoryAndCalculationRespList && bigDataHistoryAndCalculationRespList.size() > 0) {
            List<DataResp> dataRespList = Lists.newArrayList();
            bigDataHistoryAndCalculationRespList.stream().filter(history -> null != history && null != history.getDataResp() && history.getDataResp().size() > 0).forEach(history -> {
                dataRespList.addAll(history.getDataResp());
            });
            if (null != dataRespList && dataRespList.size() > 0) {
                valueMap = dataRespList.stream().collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1 + v2));
            }
        }
        if (null == valueMap || valueMap.size() <= 0) {
            valueMap = new HashMap<>();
        }
        Map<String, Double> finalValueMap = valueMap;
        minuteList.forEach(minute -> {
            DataResp dataResp = new DataResp();
            dataResp.setTime(minute);
            dataResp.setValue(null == finalValueMap.get(minute) ? null : MathUtils.doublePoint(finalValueMap.get(minute), 2));
            if (StringUtils.isNotEmpty(resourceTypeId) && resourceTypeId.equals("27")) {
                dataResp.setValue(null == dataResp.getValue() ? null : 0 - dataResp.getValue());
            }
            powerList.add(dataResp);
        });
        resp.setPowerChart(powerList);
    }

    /**
     * 查询曲线颜色
     *
     * @param issueChartList
     * @param powerChartList
     * @return
     */
    private List<List<IndexOverviewTimeColorResp>> getColorList(List<DataResp> issueChartList, List<DataResp> powerChartList, List<String> minuteList) {
        if (null == minuteList) {
            minuteList = Lists.newArrayList();
        }
        List<List<IndexOverviewTimeColorResp>> timeColorRespList = Lists.newArrayList();
        if (null != issueChartList && issueChartList.size() > 0 && null != powerChartList && powerChartList.size() > 0 ) {
            for (int i = 0; i < powerChartList.size(); i++) {
                DataResp dataRespOne = issueChartList.get(i);
                if (null != minuteList && minuteList.size() > 0) {
                    if (minuteList.contains(DateUtils.format(dataRespOne.getTime(), "HH:mm:ss"))) {
                        Double issueDoubleMul = MathUtils.mulDoubleZero(dataRespOne.getValue(), 0.7, 2);
                        Double powerTotal = 0D;
                        for (int j = 0; j < 15; j++) {
                            powerTotal += (null == powerChartList.get(i + j).getValue() ? 0 : powerChartList.get(i + j).getValue());
                        }
                        i += 14;
                        Double powerTotalAvg = MathUtils.doublePoint(powerTotal / 15, 2);
                        if (powerTotalAvg < issueDoubleMul) {
                            List<IndexOverviewTimeColorResp> respList = Lists.newArrayList();
                            IndexOverviewTimeColorResp respStart = new IndexOverviewTimeColorResp();
                            respStart.setxAxis(dataRespOne.getTime());
                            respList.add(respStart);
                            IndexOverviewTimeColorResp respEnd = new IndexOverviewTimeColorResp();
                            respEnd.setxAxis(issueChartList.get(i).getTime());
                            respList.add(respEnd);
                            if (minuteList.contains(DateUtils.format(issueChartList.get(i).getTime(), "HH:mm:ss"))) {
                                timeColorRespList.add(respList);
                            }
                        }
                    } else {
                        i += 14;
                    }
                } else {
                    Double issueDoubleMul = MathUtils.mulDoubleZero(dataRespOne.getValue(), 0.7, 2);
                    Double powerTotal = 0D;
                    for (int j = 0; j < 15; j++) {
                        powerTotal += (null == powerChartList.get(i + j).getValue() ? 0 : powerChartList.get(i + j).getValue());
                    }
                    i += 14;
                    Double powerTotalAvg = MathUtils.doublePoint(powerTotal / 15, 2);
                    if (powerTotalAvg < issueDoubleMul) {
                        List<IndexOverviewTimeColorResp> respList = Lists.newArrayList();
                        IndexOverviewTimeColorResp respStart = new IndexOverviewTimeColorResp();
                        respStart.setxAxis(dataRespOne.getTime());
                        respList.add(respStart);
                        IndexOverviewTimeColorResp respEnd = new IndexOverviewTimeColorResp();
                        respEnd.setxAxis(issueChartList.get(i).getTime());
                        respList.add(respEnd);
                        timeColorRespList.add(respList);
                    }
                }
            }
        }
        return timeColorRespList;
    }

    /**
     * @description 查询企业实际功率
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private Map<String,List<DataResp>> getEntPowerChartNew(List<String> entIdList, String startDate, String endDate, String resourceId) {
        if (CollectionUtils.isEmpty(entIdList)) {
            return new HashMap<>();
        }

        String startDateTime = startDate + " 00:00:00";
        String endDateTime = DateUtils.getAddDate(endDate) + " 00:00:00";
            List<String> minuteList = DateUtils.getMinuteList(startDateTime, endDateTime);

        // 【优化】一次性查询所有企业的设备列表，避免 N 次数据库查询
        List<AggregatorEntDevice> allDevices = new ArrayList<>();
        Map<String, List<AggregatorEntDevice>> entDeviceMap = new HashMap<>();

        for (String entId : entIdList) {
            List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getDeviceListByEntId(entId, resourceId);
            if (CollectionUtils.isNotEmpty(deviceList)) {
                entDeviceMap.put(entId, deviceList);
                allDevices.addAll(deviceList);
            }
        }

        // 【优化】一次性调用大数据获取所有设备数据，减少 N 次调用为 1 次
        Map<String, List<DataResp>> entPowerMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(allDevices)) {
            Map<String, Map<String, Double>> allDevicePowerMap = getRealTimeAvgPower(allDevices, startDateTime, endDateTime, minuteList);

            // 按企业汇总功率数据
            for (Map.Entry<String, List<AggregatorEntDevice>> entry : entDeviceMap.entrySet()) {
                String entId = entry.getKey();
                List<AggregatorEntDevice> devices = entry.getValue();

                List<DataResp> dataRespList = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime startTime = LocalDateTime.parse(startDateTime, formatter).plusMinutes(15);
            LocalDateTime endTime = LocalDateTime.parse(endDateTime, formatter);

                while (startTime.isBefore(endTime) || startTime.isEqual(endTime)) {
                String time = startTime.format(formatter);
                    AtomicReference<Double> totalValue = new AtomicReference<>(0d);
                    for (AggregatorEntDevice device : devices) {
                        Map<String, Double> devicePowerMap = allDevicePowerMap.get(device.getDeviceBaseId());
                        if (devicePowerMap != null && devicePowerMap.get(time) != null) {
                            totalValue.updateAndGet(v -> v + devicePowerMap.get(time));
                        }
                    }
                DataResp dataResp = new DataResp();
                dataResp.setTime(time);
                    dataResp.setValue(new BigDecimal(totalValue.get()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                dataRespList.add(dataResp);
                startTime = startTime.plusMinutes(15);
            }
                entPowerMap.put(entId, dataRespList);
            }
        }

        return entPowerMap;
    }

    /**
     * 【优化】同时获取聚合商功率和企业功率（一次大数据请求）
     * 聚合商功率 = 所有企业设备功率之和
     *
     * @param entIdList 企业ID列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param resourceId 资源类型ID
     * @return PowerDataResult 包含聚合商功率和企业功率Map
     */
    private PowerDataResult getAggregatorAndEntPower(List<String> entIdList, String startDate, String endDate, String resourceId) {
        PowerDataResult result = new PowerDataResult();

        if (CollectionUtils.isEmpty(entIdList)) {
            result.setAggregatorPowerList(new ArrayList<>());
            result.setEntPowerMap(new HashMap<>());
            return result;
        }

        String startDateTime = startDate + " 00:00:00";
        String endDateTime = DateUtils.getAddDate(endDate) + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(startDateTime, endDateTime);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 1. 查询所有企业的设备列表
        Map<String, List<AggregatorEntDevice>> entDeviceMap = new HashMap<>();
        List<AggregatorEntDevice> allDevices = new ArrayList<>();

        for (String entId : entIdList) {
            List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getDeviceListByEntId(entId, resourceId);
            if (CollectionUtils.isNotEmpty(deviceList)) {
                entDeviceMap.put(entId, deviceList);
                allDevices.addAll(deviceList);
            }
        }

        // 2. 一次性调用大数据获取所有设备数据
        Map<String, Map<String, Double>> allDevicePowerMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(allDevices)) {
            allDevicePowerMap = getRealTimeAvgPower(allDevices, startDateTime, endDateTime, minuteList);
        }

        // 3. 分别计算聚合商功率和企业功率
        List<DataResp> aggregatorPowerList = new ArrayList<>();
        Map<String, List<DataResp>> entPowerMap = new HashMap<>();
        LocalDateTime startTime = LocalDateTime.parse(startDateTime, formatter).plusMinutes(15);
        LocalDateTime endTime = LocalDateTime.parse(endDateTime, formatter);

        for (Map.Entry<String, List<AggregatorEntDevice>> entry : entDeviceMap.entrySet()) {
            String entId = entry.getKey();
            List<AggregatorEntDevice> devices = entry.getValue();
            List<DataResp> dataRespList = new ArrayList<>();
            LocalDateTime currentTime = startTime;

            while (currentTime.isBefore(endTime) || currentTime.isEqual(endTime)) {
                String time = currentTime.format(formatter);
                AtomicReference<Double> entTotal = new AtomicReference<>(0d);

                for (AggregatorEntDevice device : devices) {
                    Map<String, Double> devicePowerMap = allDevicePowerMap.get(device.getDeviceBaseId());
                    if (devicePowerMap != null && devicePowerMap.get(time) != null) {
                        entTotal.updateAndGet(v -> v + devicePowerMap.get(time));
                    }
                }

                DataResp dataResp = new DataResp();
                dataResp.setTime(time);
                dataResp.setValue(new BigDecimal(entTotal.get()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                dataRespList.add(dataResp);
                currentTime = currentTime.plusMinutes(15);
            }

            entPowerMap.put(entId, dataRespList);
        }

        // 4. 计算聚合商功率（所有企业功率之和）
        LocalDateTime aggCurrentTime = startTime;
        while (aggCurrentTime.isBefore(endTime) || aggCurrentTime.isEqual(endTime)) {
            String time = aggCurrentTime.format(formatter);
            AtomicReference<Double> aggTotal = new AtomicReference<>(0d);

            for (List<DataResp> entPowerList : entPowerMap.values()) {
                for (DataResp dataResp : entPowerList) {
                    if (time.equals(dataResp.getTime()) && dataResp.getValue() != null) {
                        aggTotal.updateAndGet(v -> v + dataResp.getValue());
                    }
                }
            }

            DataResp aggDataResp = new DataResp();
            aggDataResp.setTime(time);
            aggDataResp.setValue(new BigDecimal(aggTotal.get()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            aggregatorPowerList.add(aggDataResp);
            aggCurrentTime = aggCurrentTime.plusMinutes(15);
        }

        result.setAggregatorPowerList(aggregatorPowerList);
        result.setEntPowerMap(entPowerMap);
        return result;
    }

    /**
     * 功率数据结果封装类
     */
    private static class PowerDataResult {
        private List<DataResp> aggregatorPowerList;
        private Map<String, List<DataResp>> entPowerMap;

        public List<DataResp> getAggregatorPowerList() {
            return aggregatorPowerList;
        }

        public void setAggregatorPowerList(List<DataResp> aggregatorPowerList) {
            this.aggregatorPowerList = aggregatorPowerList;
        }

        public Map<String, List<DataResp>> getEntPowerMap() {
            return entPowerMap;
        }

        public void setEntPowerMap(Map<String, List<DataResp>> entPowerMap) {
            this.entPowerMap = entPowerMap;
        }
    }


    /**
     * 查询实际功率 用户级别
     *
     * @param
     * @param
     * @param
     * @param resp
     */
    private void getEntPowerChartNew(NewUserAdjustmentGraphReq userAdjustmentGraphReq, HistoryQueryGraphVO resp) {
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getDeviceListByEntId(userAdjustmentGraphReq.getSubEntId(), userAdjustmentGraphReq.getResourceTypeId());
        String startDate = userAdjustmentGraphReq.getStartTime()+" 00:00:00";
        String endDate = DateUtils.getAddDate(userAdjustmentGraphReq.getEndTime())+" 00:00:00";

        List<String> minuteList = DateUtils.getMinuteList(startDate, endDate);

        Map<String, Map<String, Double>> map = getRealTimeAvgPower(deviceList, startDate, endDate, minuteList);
        List<DataResp> dataRespList = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateTime = LocalDateTime.parse(startDate, formatter).plusMinutes(15);
        LocalDateTime endDateTime = LocalDateTime.parse(endDate, formatter);
        while (startDateTime.isBefore(endDateTime) || startDateTime.isEqual(endDateTime)){
            String time = startDateTime.format(formatter);
            AtomicReference<Double> totalValue=new AtomicReference<Double>();
            totalValue.set(0d);
            map.forEach((key,value)-> totalValue.updateAndGet(v->v+ (null != value.get(time) ? value.get(time):0D)));
            DataResp dataResp = new DataResp();
            dataResp.setTime(time);
            dataResp.setValue(null != totalValue.get() ? BigDecimal.valueOf(totalValue.get()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue() :null );
            dataRespList.add(dataResp);
            startDateTime = startDateTime.plusMinutes(15);
        }
        resp.setPowerChart(dataRespList);
    }

    /**
     * 查询实际功率
     *
     * @param userAdjustmentGraphReq
     * @param minuteList
     * @param simulate
     * @param resp
     */
    private void getPowerChart(UserAdjustmentGraphReq userAdjustmentGraphReq, List<String> minuteList, String simulate, HistoryQueryGraphVO resp) {
        AggregatorEntDevice aggregatorEntDevice = aggregatorEntDeviceService.getAggregatorEntDevice(userAdjustmentGraphReq.getDeviceBaseId());
        Map<String, Double> realPowerMap = new HashMap<>();
        List<DeviceGroupPointInfo> deviceGroupPointInfoList = aggregatorDevicePointService.getDeviceGroupPointInfoList(Arrays.asList(aggregatorEntDevice), MetricEnum.YES_POWER.getCode());
        List<BigDataHistoryAndCalculationResp> bigDataHistoryAndCalculationRespList = bigDataHistoryService.queryBigData(Arrays.asList(aggregatorEntDevice), deviceGroupPointInfoList, "1minute", userAdjustmentGraphReq.getStartTime(), userAdjustmentGraphReq.getEndTime(), simulate);
        if (null != bigDataHistoryAndCalculationRespList && bigDataHistoryAndCalculationRespList.size() > 0) {
            BigDataHistoryAndCalculationResp bigDataHistoryResp = bigDataHistoryAndCalculationRespList.get(0);
            if (null != bigDataHistoryResp) {
                List<DataResp> dataRespList = bigDataHistoryResp.getDataResp();
                if (null != dataRespList && dataRespList.size() > 0) {
                    realPowerMap = dataRespList.stream().collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1));
                }
            }
        }
        if (null == realPowerMap) {
            realPowerMap = new HashMap<>();
        }
        List<DataResp> powerList = Lists.newArrayList();
        Map<String, Double> finalRealPowerMap = realPowerMap;
        minuteList.forEach(minute -> {
            DataResp dataResp = new DataResp();
            dataResp.setTime(minute);
            dataResp.setValue(null == finalRealPowerMap.get(minute) ? null : MathUtils.doublePoint(finalRealPowerMap.get(minute), 2));
            if (StringUtils.isNotEmpty(userAdjustmentGraphReq.getResourceTypeId()) && userAdjustmentGraphReq.getResourceTypeId().equals("27")) {
                dataResp.setValue(null == dataResp.getValue() ? null : 0 - dataResp.getValue());
            }
            powerList.add(dataResp);
        });
        resp.setActualPower(powerList);
    }

    /**
     * 查询分解后设备功率/设备有效负荷
     *
     * @param userAdjustmentGraphReq
     * @param dateList
     * @param minuteList
     * @param resp
     */
    private void getIssueChart(UserAdjustmentGraphReq userAdjustmentGraphReq, List<String> dateList, List<String> minuteList, HistoryQueryGraphVO resp) {
        List<DataResp> issueChartList = Lists.newArrayList();
        List<DataResp> issueChartUseList = Lists.newArrayList();
        Map<String, Double> dataRespMap = new HashMap<>();
        List<AggregatorDeviceDateIssueChart> aggregatorDeviceDateIssueChartList = aggregatorDeviceDateIssueChartService.getAggregatorDeviceDateIssueChartList(userAdjustmentGraphReq.getDeviceBaseId(), dateList);
        if (null != aggregatorDeviceDateIssueChartList && aggregatorDeviceDateIssueChartList.size() > 0) {
            List<DataResp> totalDataRespList = Lists.newArrayList();
            aggregatorDeviceDateIssueChartList.stream().filter(chart -> null != chart && StringUtils.isNotEmpty(chart.getIssueChart())).forEach(aggregatorDateIssueChart -> {
                String issueChart = aggregatorDateIssueChart.getIssueChart();
                List<DataResp> dataRespList = JSONArray.parseArray(issueChart, DataResp.class);
                if (null != dataRespList && dataRespList.size() > 0) {
                    totalDataRespList.addAll(dataRespList);
                }
            });
            if (null != totalDataRespList && totalDataRespList.size() > 0) {
                dataRespMap = totalDataRespList.stream().collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1 + v2));
            }
        }
        if (null == dataRespMap || dataRespMap.size() <= 0) {
            dataRespMap = new HashMap<>();
        }
        for (int i = minuteList.size() - 1; i > 0; i -= 15) {
            for (int j = i; j >= i - 14; j--) {
                //下发功率
                DataResp dataResp = new DataResp();
                dataResp.setTime(minuteList.get(j));
                dataResp.setValue(dataRespMap.get(minuteList.get(i)));
                issueChartList.add(0, dataResp);
                //有效功率
                DataResp dataRespUse = new DataResp();
                dataRespUse.setTime(minuteList.get(j));
                dataRespUse.setValue(MathUtils.mulDoubleNull(dataResp.getValue(), 0.7, 2));
                issueChartUseList.add(0, dataRespUse);
            }
        }
        resp.setResolvedPower(issueChartList);
        resp.setEffectivePower(issueChartUseList);
    }

    /**
     * 返回结果
     *
     * @param pageResultVO
     * @param pageSize
     * @return
     */
    private PageResultVO<HistoryQueryTableVO> getPageResultVO(PageResultVO<HistoryQueryTableVO> pageResultVO, Integer pageSize) {
        pageResultVO.setPageIndex(1);
        pageResultVO.setPageSize(pageSize);
        pageResultVO.setTotal(0);
        pageResultVO.setList(Lists.newArrayList());
        return pageResultVO;
    }


    /**
     * 处理数据
     *
     * @param aggregatorDeviceDateProfitRespList
     * @param entTimeMap
     * @return
     */
    private List<AggregatorDeviceDateProfitResp> getAggregatorDeviceDateProfitRespList(List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList, Map<String, List<AggregatorEntProfitTime>> entTimeMap) {
        if (null == entTimeMap) {
            return aggregatorDeviceDateProfitRespList;
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
            return resultList;
        }
    }
}
