package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.console.enums.AggregatorEntPlanTypeEnum;
import cn.sl.ehub.console.enums.AggregatorRefreshEnum;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.service.mapper.AggregatorEntApplyPlanMapper;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.service.req.AggregatorEntApplyPlanReq;
import cn.sl.ehub.service.resp.*;
import cn.sl.ehub.console.service.*;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.common.utils.RedisUtil;
import cn.sl.ehub.service.vo.*;
import cn.enn.sms.req.SendMessageReq;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 企业用户申报计划ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AggregatorEntApplyPlanServiceImpl implements IAggregatorEntApplyPlanService {

    private final AggregatorEntApplyPlanMapper aggregatorEntApplyPlanMapper;
    private final IAggregatorEntDeviceService aggregatorEntDeviceService;
    private final IAggregatorEntApplyDateCheckService aggregatorEntApplyDateCheckService;
    private final IAggregatorDateHolidayService aggregatorDateHolidayService;
    private final IAggregatorEntService aggregatorEntService;
    private final IAggregatorEntDateApplyDetailService aggregatorEntDateApplyDetailService;
    private final IAggregatorDeviceDateDeliveryChartService aggregatorDeviceDateDeliveryChartService;
    private final IAggregatorDateDeliveryChartService aggregatorDateDeliveryChartService;
    private final IAggregatorEntDateDeviceStartStopPlanService aggregatorEntDateDeviceStartStopPlanService;
    @Resource(name = "threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor executor;
    private final RedisUtil redis;
    private final ISmsService pushService;

    @Override
    public PageResultVO<AggregatorEntApplyPlanResp> getAggregatorEntApplyPlanRespList(String entId, Boolean saveStatus, Integer pageNo, Integer pageSize) {
        PageResultVO<AggregatorEntApplyPlanResp> pageResultVO = new PageResultVO<>();
        pageResultVO.setPageIndex(pageNo);
        pageResultVO.setPageSize(pageSize);
        pageResultVO.setTotal(0);
        PageHelper.startPage(pageNo, pageSize);
        if (null == saveStatus) {
            saveStatus = true;
        }
        List<AggregatorEntApplyPlanResp> aggregatorEntApplyPlanRespList = aggregatorEntApplyPlanMapper.getAggregatorEntApplyPlanRespList(entId, saveStatus);
        if (CollectionUtils.isEmpty(aggregatorEntApplyPlanRespList)) {
            pageResultVO.setList(new ArrayList<>());
            return pageResultVO;
        }
        pageResultVO.setTotal((int) ((Page<AggregatorEntApplyPlanResp>) aggregatorEntApplyPlanRespList).getTotal());
        aggregatorEntApplyPlanRespList.forEach(resp -> {
            String startDate = DateUtils.format(resp.getStartDate() + " 00:00:00", "yyyy年MM月dd日");
            String endDate = DateUtils.format(resp.getEndDate() + " 00:00:00", "yyyy年MM月dd日");
            if (StringUtils.isEmpty(startDate)) {
                resp.setShowDate(endDate);
            } else if (StringUtils.isEmpty(endDate)) {
                resp.setShowDate(startDate);
            } else if (startDate.equals(endDate)) {
                resp.setShowDate(startDate);
            } else {
                resp.setShowDate(startDate + "~" + endDate);
            }
        });
        pageResultVO.setList(aggregatorEntApplyPlanRespList);
        return pageResultVO;
    }

    @Override
    public AggregatorEntApplyPlanResp getAggregatorEntApplyPlanResp(String id) {
        AggregatorEntApplyPlanResp aggregatorEntApplyPlanResp = new AggregatorEntApplyPlanResp();
        aggregatorEntApplyPlanResp.setAppApplyIndexDeviceDetailRespList(Lists.newArrayList());
        AggregatorEntApplyPlan aggregatorEntApplyPlan = aggregatorEntApplyPlanMapper.selectByPrimaryKey(id);
        if (null != aggregatorEntApplyPlan) {
            BeanUtils.copyProperties(aggregatorEntApplyPlan, aggregatorEntApplyPlanResp);
            String startDate = DateUtils.format(aggregatorEntApplyPlanResp.getStartDate() + " 00:00:00", "yyyy年MM月dd日");
            String endDate = DateUtils.format(aggregatorEntApplyPlanResp.getEndDate() + " 00:00:00", "yyyy年MM月dd日");
            if (StringUtils.isEmpty(startDate)) {
                aggregatorEntApplyPlanResp.setShowDate(endDate);
            } else if (StringUtils.isEmpty(endDate)) {
                aggregatorEntApplyPlanResp.setShowDate(startDate);
            } else if (startDate.equals(endDate)) {
                aggregatorEntApplyPlanResp.setShowDate(startDate);
            } else {
                aggregatorEntApplyPlanResp.setShowDate(startDate + "~" + endDate);
            }
            String detail = aggregatorEntApplyPlan.getDetail();
            if (StringUtils.isNotEmpty(detail)) {
                List<AppApplyIndexDeviceDetailResp> appApplyIndexDeviceDetailRespList = JSONArray.parseArray(detail, AppApplyIndexDeviceDetailResp.class);
                if (CollectionUtils.isNotEmpty(appApplyIndexDeviceDetailRespList)) {
                    Map<String, String> deviceMap = new HashMap<>();
                    List<String> deviceBaseIdList = appApplyIndexDeviceDetailRespList.stream().map(AppApplyIndexDeviceDetailResp::getDeviceBaseId).collect(Collectors.toList());
                    List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(deviceBaseIdList);
                    if (CollectionUtils.isNotEmpty(deviceList)) {
                        Map<String, String> deviceInfoMap = deviceList.stream().collect(Collectors.toMap(AggregatorEntDevice::getDeviceBaseId, AggregatorEntDevice::getDeviceName, (v1, v2) -> v1));
                        deviceMap.putAll(deviceInfoMap);
                    }
                    appApplyIndexDeviceDetailRespList.forEach(resp -> resp.setDeviceName(deviceMap.get(resp.getDeviceBaseId())));
                }
                aggregatorEntApplyPlanResp.setAppApplyIndexDeviceDetailRespList(appApplyIndexDeviceDetailRespList);
            }
        }
        return aggregatorEntApplyPlanResp;
    }

    @Override
    public AggregatorEntApplyPlanResp getAggregatorEntApplyPlanResp(String entId, String date) {
        AggregatorEntApplyPlanResp aggregatorEntApplyPlanResp = new AggregatorEntApplyPlanResp();
        if (StringUtils.isEmpty(date)) {
            date = DateUtils.getNextDay();
        }
        List<AggregatorEntApplyPlan> aggregatorEntApplyPlanList = getAggregatorEntApplyPlanList(entId, date, true);
        if (null != aggregatorEntApplyPlanList && aggregatorEntApplyPlanList.size() > 0) {
            AggregatorEntApplyPlan aggregatorEntApplyPlan = aggregatorEntApplyPlanList.get(0);
            if (null != aggregatorEntApplyPlan) {
                aggregatorEntApplyPlanResp.setApplyTime(aggregatorEntApplyPlan.getApplyTime());
                String detail = aggregatorEntApplyPlan.getDetail();
                if (StringUtils.isNotEmpty(detail)) {
                    List<AppApplyIndexDeviceDetailResp> appApplyIndexDeviceDetailRespList = JSONArray.parseArray(detail, AppApplyIndexDeviceDetailResp.class);
                    if (CollectionUtils.isNotEmpty(appApplyIndexDeviceDetailRespList)) {
                        Map<String, String> deviceMap = new HashMap<>();
                        List<String> deviceBaseIdList = appApplyIndexDeviceDetailRespList.stream().map(AppApplyIndexDeviceDetailResp::getDeviceBaseId).collect(Collectors.toList());
                        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(deviceBaseIdList);
                        if (CollectionUtils.isNotEmpty(deviceList)) {
                            Map<String, String> deviceInfoMap = deviceList.stream().collect(Collectors.toMap(AggregatorEntDevice::getDeviceBaseId, AggregatorEntDevice::getDeviceName, (v1, v2) -> v1));
                            deviceMap.putAll(deviceInfoMap);
                        }
                        appApplyIndexDeviceDetailRespList.forEach(resp -> resp.setDeviceName(deviceMap.get(resp.getDeviceBaseId())));
                    }
                    aggregatorEntApplyPlanResp.setAppApplyIndexDeviceDetailRespList(appApplyIndexDeviceDetailRespList);
                }
            }
        }
        return aggregatorEntApplyPlanResp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addApplyPlan(AggregatorEntApplyPlanReq aggregatorEntApplyPlanReq) {
        List<String> dateList = DateUtils.getDayList(aggregatorEntApplyPlanReq.getStartDate(), aggregatorEntApplyPlanReq.getEndDate());
        Boolean flag = aggregatorEntDateApplyDetailService.checkDate(aggregatorEntApplyPlanReq.getEntId(), dateList);
        if (flag) {
            throw new BaseException(StatusCode.ERROR.getCode(), "已经提交过临时计划");
        }
        if (!aggregatorEntApplyPlanReq.getPlanStatus()) {
            Weekend<AggregatorEntApplyPlan> weekend = Weekend.of(AggregatorEntApplyPlan.class);
            WeekendCriteria<AggregatorEntApplyPlan, Object> criteria = weekend.weekendCriteria();
            criteria.andEqualTo(AggregatorEntApplyPlan::getEntId, aggregatorEntApplyPlanReq.getEntId());
            criteria.andEqualTo(AggregatorEntApplyPlan::getPlanStatus, aggregatorEntApplyPlanReq.getPlanStatus());
            criteria.andEqualTo(AggregatorEntApplyPlan::getSaveStatus, true);
            int count = aggregatorEntApplyPlanMapper.selectCountByExample(weekend);
            if (count > 0) {
                throw new BaseException(StatusCode.ERROR.getCode(), "已经提交过默认计划");
            }
        }
        String now = DateUtils.getTime();
        //保存申报计划
        AggregatorEntApplyPlan aggregatorEntApplyPlan = new AggregatorEntApplyPlan();
        BeanUtils.copyProperties(aggregatorEntApplyPlanReq, aggregatorEntApplyPlan);
        aggregatorEntApplyPlan.setApplyTime(now);
        List<AppApplyIndexDeviceDetailResp> appApplyIndexDeviceDetailRespList = aggregatorEntApplyPlanReq.getAppApplyIndexDeviceDetailRespList();
        if (null != appApplyIndexDeviceDetailRespList && appApplyIndexDeviceDetailRespList.size() > 0) {
            aggregatorEntApplyPlan.setDetail(JSONObject.toJSONString(appApplyIndexDeviceDetailRespList));
        }
        AggregatorEntApplyPlan aggregatorEntApplyPlanBySaveStatus = getApplyPlan(aggregatorEntApplyPlanReq.getEntId(), aggregatorEntApplyPlanReq.getPlanStatus(), false);
        if (null != aggregatorEntApplyPlanBySaveStatus && null != aggregatorEntApplyPlanBySaveStatus.getId()) {
            aggregatorEntApplyPlanMapper.deleteByPrimaryKey(aggregatorEntApplyPlanBySaveStatus.getId());
        }
        aggregatorEntApplyPlanMapper.insertSelective(aggregatorEntApplyPlan);
        if (aggregatorEntApplyPlanReq.getSaveStatus()) {
            //写入企业申报详情，设备申报功率，聚合商申报功率==聚合商查询申报功率实时变化
            executor.execute(() -> addData(aggregatorEntApplyPlanReq, dateList, now));
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addData(AggregatorEntApplyPlanReq aggregatorEntApplyPlanReq, List<String> dateList, String now) {
        if (aggregatorEntApplyPlanReq.getPlanStatus()) {
            //写入企业申报详情
            saveAggregatorEntDateApplyDetail(aggregatorEntApplyPlanReq, dateList, now);
            //设备申报功率
            saveAggregatorDeviceDateDeliveryChart(aggregatorEntApplyPlanReq, dateList, now);
            //聚合商申报功率
            saveAggregatorDateDeliveryChart(aggregatorEntApplyPlanReq, dateList);
            //设备启动计划
            saveDevicePlan(aggregatorEntApplyPlanReq, dateList);
        }
        //推送刷新
        try {
            //企业用户推送
            List<String> codeListByApp = AggregatorRefreshEnum.getCodeByType("app");
            codeListByApp.forEach(code -> {
                SendMessageReq req = new SendMessageReq();
                req.setContent(code);
                req.setEntId(aggregatorEntApplyPlanReq.getEntId());
                log.info("企业用户创建计划推送消息,{}", JSONObject.toJSONString(req));
                pushService.sendSocket(req);
            });
        } catch (Exception e) {
            log.info("企业用户创建计划推送消息失败");
        }
        try {
            //聚合商推送
            List<String> codeListByPc = AggregatorRefreshEnum.getCodeByType("pc");
            codeListByPc.forEach(code -> {
                SendMessageReq req = new SendMessageReq();
                req.setContent(code);
                req.setEntId(aggregatorEntApplyPlanReq.getAggregatorId());
                log.info("聚合商创建计划推送消息,{}", JSONObject.toJSONString(req));
                pushService.sendSocket(req);
            });
        } catch (Exception e) {
            log.info("聚合商创建计划推送消息失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAggregatorEntDateApplyDetail(AggregatorEntApplyPlanReq aggregatorEntApplyPlanReq, List<String> dateList, String now) {
        List<AggregatorEntDateApplyDetail> aggregatorEntDateApplyDetailList = Lists.newArrayList();
        AggregatorEnt aggregatorEnt = aggregatorEntService.getAggregatorEnt(aggregatorEntApplyPlanReq.getEntId());
        dateList.forEach(date -> {
            AggregatorEntDateApplyDetail aggregatorEntDateApplyDetail = new AggregatorEntDateApplyDetail();
            aggregatorEntDateApplyDetail.setAggregatorId(aggregatorEntApplyPlanReq.getAggregatorId());
            aggregatorEntDateApplyDetail.setDate(date);
            aggregatorEntDateApplyDetail.setEntId(aggregatorEntApplyPlanReq.getEntId());
            aggregatorEntDateApplyDetail.setStationId(aggregatorEnt.getStationId());
            aggregatorEntDateApplyDetail.setApplyDate(DateUtils.getDay(now));
            aggregatorEntDateApplyDetail.setApplyTime(now);
            aggregatorEntDateApplyDetail.setPlanStatus(aggregatorEntApplyPlanReq.getPlanStatus());
            aggregatorEntDateApplyDetail.setApplyStatus("1");
            aggregatorEntDateApplyDetailList.add(aggregatorEntDateApplyDetail);
        });
        aggregatorEntDateApplyDetailService.delete(aggregatorEntApplyPlanReq.getEntId(), dateList);
        if (CollectionUtils.isNotEmpty(aggregatorEntDateApplyDetailList)) {
            aggregatorEntDateApplyDetailService.batchInsert(aggregatorEntDateApplyDetailList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAggregatorDeviceDateDeliveryChart(AggregatorEntApplyPlanReq aggregatorEntApplyPlanReq, List<String> dateList, String now) {
        List<AggregatorDeviceDateDeliveryChart> aggregatorDeviceDateDeliveryChartList = Lists.newArrayList();
        AtomicReference<String> stationId = new AtomicReference<>();
        AggregatorEnt aggregatorEnt = aggregatorEntService.getAggregatorEnt(aggregatorEntApplyPlanReq.getEntId());
        if (null != aggregatorEnt) {
            stationId.set(aggregatorEnt.getStationId());
        }
        dateList.forEach(date -> {
            List<AppApplyIndexDeviceDetailResp> appApplyIndexDeviceDetailRespList = aggregatorEntApplyPlanReq.getAppApplyIndexDeviceDetailRespList();
            if (CollectionUtils.isNotEmpty(appApplyIndexDeviceDetailRespList)) {
                appApplyIndexDeviceDetailRespList.forEach(appApplyIndexDeviceDetailResp -> {
                    AggregatorDeviceDateDeliveryChart aggregatorDeviceDateDeliveryChart = new AggregatorDeviceDateDeliveryChart();
                    aggregatorDeviceDateDeliveryChart.setAggregatorId(aggregatorEntApplyPlanReq.getAggregatorId());
                    aggregatorDeviceDateDeliveryChart.setEntId(aggregatorEntApplyPlanReq.getEntId());
                    aggregatorDeviceDateDeliveryChart.setStationId(stationId.get());
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
                                dataResp.setValue(detail.getPower());
                                if (null != dataResp.getValue() && appApplyIndexDeviceDetailResp.getResourceTypeId().equals("27") && detail.getUseStatus() == -1) {
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
            }
        });
        aggregatorDeviceDateDeliveryChartService.delete(aggregatorEntApplyPlanReq.getEntId(), dateList);
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDateDeliveryChartList)) {
            aggregatorDeviceDateDeliveryChartService.batchInsert(aggregatorDeviceDateDeliveryChartList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAggregatorDateDeliveryChart(AggregatorEntApplyPlanReq aggregatorEntApplyPlanReq, List<String> dateList) {
        List<AggregatorDateDeliveryChart> aggregatorDateDeliveryChartList = Lists.newArrayList();
        List<AggregatorDeviceDateDeliveryChart> deviceDateDeliveryChartList = aggregatorDeviceDateDeliveryChartService.getAggregatorDeviceDateDeliveryChartList(aggregatorEntApplyPlanReq.getAggregatorId(), dateList);
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
                                    dataResp.setValue(timeValueMapEntry.getValue());
                                    chartList.add(dataResp);
                                }
                                AggregatorDateDeliveryChart aggregatorDateDeliveryChart = new AggregatorDateDeliveryChart();
                                aggregatorDateDeliveryChart.setAggregatorId(aggregatorEntApplyPlanReq.getAggregatorId());
                                aggregatorDateDeliveryChart.setResourceTypeId(resourceMapEntry.getKey());
                                aggregatorDateDeliveryChart.setDate(dateMapEntry.getKey());
                                if (CollectionUtils.isNotEmpty(chartList)) {
                                    chartList = chartList.stream().sorted(Comparator.comparing(DataResp::getTime)).collect(Collectors.toList());
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
        aggregatorDateDeliveryChartService.delete(aggregatorEntApplyPlanReq.getAggregatorId(), dateList);
        if (CollectionUtils.isNotEmpty(aggregatorDateDeliveryChartList)) {
            aggregatorDateDeliveryChartService.batchInsert(aggregatorDateDeliveryChartList);
        }
    }

    @Override
    public void saveDevicePlan(AggregatorEntApplyPlanReq aggregatorEntApplyPlanReq, List<String> dateList) {
        List<AggregatorEntDateDeviceStartStopPlan> aggregatorEntDateDeviceStartStopPlanList = Lists.newArrayList();
        dateList.forEach(date -> {
            List<AppApplyIndexDeviceDetailResp> appApplyIndexDeviceDetailRespList = aggregatorEntApplyPlanReq.getAppApplyIndexDeviceDetailRespList();
            if (CollectionUtils.isNotEmpty(appApplyIndexDeviceDetailRespList)) {
                AggregatorEntDateDeviceStartStopPlan aggregatorEntDateDeviceStartStopPlan = new AggregatorEntDateDeviceStartStopPlan();
                aggregatorEntDateDeviceStartStopPlan.setAggregatorId(aggregatorEntApplyPlanReq.getAggregatorId());
                aggregatorEntDateDeviceStartStopPlan.setEntId(aggregatorEntApplyPlanReq.getEntId());
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
        });
        aggregatorEntDateDeviceStartStopPlanService.delete(aggregatorEntApplyPlanReq.getEntId(), dateList);
        if (CollectionUtils.isNotEmpty(aggregatorEntDateDeviceStartStopPlanList)) {
            aggregatorEntDateDeviceStartStopPlanService.batchInsert(aggregatorEntDateDeviceStartStopPlanList);
        }
    }

    @Override
    public AggregatorEntApplyPlanResp getApplyPlan(String entId, Boolean planStatus, String date, Boolean saveStatus) {
        AggregatorEntApplyPlanResp aggregatorEntApplyPlanResp = new AggregatorEntApplyPlanResp();
        AggregatorEnt aggregatorEnt = aggregatorEntService.getAggregatorEnt(entId);
        String nowDate = DateUtils.getDay();
        Weekend<AggregatorEntApplyPlan> weekend = Weekend.of(AggregatorEntApplyPlan.class);
        WeekendCriteria<AggregatorEntApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntApplyPlan::getEntId, entId);
        criteria.andEqualTo(AggregatorEntApplyPlan::getPlanStatus, planStatus);
        criteria.andGreaterThanOrEqualTo(AggregatorEntApplyPlan::getApplyTime, nowDate + " 00:00:00");
        criteria.andLessThanOrEqualTo(AggregatorEntApplyPlan::getApplyTime, nowDate + " 23:59:59");
        weekend.orderBy("applyTime").desc();
        List<AggregatorEntApplyPlan> aggregatorEntApplyPlanList = aggregatorEntApplyPlanMapper.selectByExample(weekend);
        if (null != aggregatorEntApplyPlanList && aggregatorEntApplyPlanList.size() > 0) {
            AggregatorEntApplyPlan aggregatorEntApplyPlan = aggregatorEntApplyPlanList.get(0);
            if (null != aggregatorEntApplyPlan) {
                BeanUtils.copyProperties(aggregatorEntApplyPlan, aggregatorEntApplyPlanResp);
                if (aggregatorEntApplyPlanResp.getPlanStatus()) {
                    if (StringUtils.isEmpty(date)) {
                        date = DateUtils.getDay();
                    }
                    aggregatorEntApplyPlanResp.setStartDate(date);
                }
                String detail = aggregatorEntApplyPlan.getDetail();
                if (StringUtils.isNotEmpty(detail)) {
                    List<AppApplyIndexDeviceDetailResp> appApplyIndexDeviceDetailRespList = JSONArray.parseArray(detail, AppApplyIndexDeviceDetailResp.class);
                    if (CollectionUtils.isNotEmpty(appApplyIndexDeviceDetailRespList)) {
                        Map<String, String> deviceMap = new HashMap<>();
                        List<String> deviceBaseIdList = appApplyIndexDeviceDetailRespList.stream().map(AppApplyIndexDeviceDetailResp::getDeviceBaseId).collect(Collectors.toList());
                        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(deviceBaseIdList);
                        if (CollectionUtils.isNotEmpty(deviceList)) {
                            Map<String, String> deviceInfoMap = deviceList.stream().collect(Collectors.toMap(AggregatorEntDevice::getDeviceBaseId, AggregatorEntDevice::getDeviceName, (v1, v2) -> v1));
                            deviceMap.putAll(deviceInfoMap);
                        }
                        appApplyIndexDeviceDetailRespList.forEach(resp -> resp.setDeviceName(deviceMap.get(resp.getDeviceBaseId())));
                    }
                    aggregatorEntApplyPlanResp.setAppApplyIndexDeviceDetailRespList(appApplyIndexDeviceDetailRespList);
                }
            }
        } else {
            aggregatorEntApplyPlanResp.setAggregatorId(aggregatorEnt.getAggregatorId());
            aggregatorEntApplyPlanResp.setEntId(aggregatorEnt.getEntId());
            aggregatorEntApplyPlanResp.setPlanStatus(planStatus);
            if (planStatus) {
                aggregatorEntApplyPlanResp.setStartDate(DateUtils.getNextDay());
                aggregatorEntApplyPlanResp.setEndDate(DateUtils.getNextDay());
            } else {
                aggregatorEntApplyPlanResp.setStartDate(aggregatorEnt.getServiceStartDate());
                aggregatorEntApplyPlanResp.setEndDate(aggregatorEnt.getServiceEndDate());
            }
            aggregatorEntApplyPlanResp.setStatus(true);
        }
        return aggregatorEntApplyPlanResp;
    }

    @Override
    public AggregatorEntApplyPlan getApplyPlan(String entId, Boolean planStatus, Boolean saveStatus) {
        AggregatorEntApplyPlan aggregatorEntApplyPlan = new AggregatorEntApplyPlan();
        Weekend<AggregatorEntApplyPlan> weekend = Weekend.of(AggregatorEntApplyPlan.class);
        WeekendCriteria<AggregatorEntApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntApplyPlan::getEntId, entId);
        criteria.andEqualTo(AggregatorEntApplyPlan::getPlanStatus, planStatus);
        criteria.andEqualTo(AggregatorEntApplyPlan::getSaveStatus, saveStatus);
        weekend.orderBy("applyTime").desc();
        List<AggregatorEntApplyPlan> aggregatorEntApplyPlanList = aggregatorEntApplyPlanMapper.selectByExample(weekend);
        if (CollectionUtils.isNotEmpty(aggregatorEntApplyPlanList)) {
            return aggregatorEntApplyPlanList.get(0);
        }
        return aggregatorEntApplyPlan;
    }

    @Override
    public AggregatorEntApplyPlanResp getApplyPlanV1(String entId, Boolean planStatus, String date, Boolean saveStatus) {
        Weekend<AggregatorEntApplyPlan> weekend = Weekend.of(AggregatorEntApplyPlan.class);
        WeekendCriteria<AggregatorEntApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntApplyPlan::getEntId, entId);
        criteria.andEqualTo(AggregatorEntApplyPlan::getPlanStatus, planStatus);
        criteria.andLessThanOrEqualTo(AggregatorEntApplyPlan::getStartDate, date);
        criteria.andGreaterThanOrEqualTo(AggregatorEntApplyPlan::getEndDate, date);
        criteria.andEqualTo(AggregatorEntApplyPlan::getSaveStatus, saveStatus);
        weekend.orderBy("applyTime").desc();
        List<AggregatorEntApplyPlan> aggregatorEntApplyPlanList = aggregatorEntApplyPlanMapper.selectByExample(weekend);
        if (null != aggregatorEntApplyPlanList && aggregatorEntApplyPlanList.size() > 0) {
            AggregatorEntApplyPlan aggregatorEntApplyPlan = aggregatorEntApplyPlanList.get(0);
            if (null != aggregatorEntApplyPlan) {
                AggregatorEntApplyPlanResp aggregatorEntApplyPlanResp = new AggregatorEntApplyPlanResp();
                BeanUtils.copyProperties(aggregatorEntApplyPlan, aggregatorEntApplyPlanResp);
                String detail = aggregatorEntApplyPlan.getDetail();
                if (StringUtils.isNotEmpty(detail)) {
                    List<AppApplyIndexDeviceDetailResp> appApplyIndexDeviceDetailRespList = JSONArray.parseArray(detail, AppApplyIndexDeviceDetailResp.class);
                    if (CollectionUtils.isNotEmpty(appApplyIndexDeviceDetailRespList)) {
                        Map<String, String> deviceMap = new HashMap<>();
                        List<String> deviceBaseIdList = appApplyIndexDeviceDetailRespList.stream().map(AppApplyIndexDeviceDetailResp::getDeviceBaseId).collect(Collectors.toList());
                        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(deviceBaseIdList);
                        if (CollectionUtils.isNotEmpty(deviceList)) {
                            Map<String, String> deviceInfoMap = deviceList.stream().collect(Collectors.toMap(AggregatorEntDevice::getDeviceBaseId, AggregatorEntDevice::getDeviceName, (v1, v2) -> v1));
                            deviceMap.putAll(deviceInfoMap);
                        }
                        appApplyIndexDeviceDetailRespList.forEach(resp -> resp.setDeviceName(deviceMap.get(resp.getDeviceBaseId())));
                    }
                    aggregatorEntApplyPlanResp.setAppApplyIndexDeviceDetailRespList(appApplyIndexDeviceDetailRespList);
                }
                return aggregatorEntApplyPlanResp;
            }
        }
        return null;
    }

    @Override
    public AggregatorEntApplyPlanStatusResp getApplyStatus(String entId, String date) {
        AggregatorEntApplyPlanStatusResp aggregatorEntApplyPlanStatusResp = new AggregatorEntApplyPlanStatusResp();
        if (StringUtils.isEmpty(date)) {
            date = DateUtils.getNextDay();
        }
        String now = DateUtils.getTime();
        String nowDate = DateUtils.getDay();
        String nowTime = DateUtils.format(now, "HH:mm:ss");
        List<String> dateList = aggregatorDateHolidayService.getApplyDateList(date, false);
        AggregatorEnt aggregatorEnt = aggregatorEntService.getAggregatorEnt(entId);
        int countDefault = getAggregatorEntApplyPlanListCount(entId, false, true);
        if (countDefault == 0) {
            aggregatorEntApplyPlanStatusResp.setApplyStatus(0);
            aggregatorEntApplyPlanStatusResp.setShowTime(aggregatorEnt.getServiceStartDate() + " 00:00");
            return aggregatorEntApplyPlanStatusResp;
        }
        //未在服务时间，显示 不可提交新计划
        if (date.compareTo(aggregatorEnt.getServiceStartDate()) < 0 || date.compareTo(aggregatorEnt.getServiceEndDate()) > 0) {
            aggregatorEntApplyPlanStatusResp.setApplyStatus(2);
            aggregatorEntApplyPlanStatusResp.setShowTime(DateUtils.getAddDate(aggregatorEnt.getServiceStartDate(), -1) + " 00:00");
            return aggregatorEntApplyPlanStatusResp;
        }
        AggregatorEntDateApplyDetail aggregatorEntDateApplyDetail = aggregatorEntDateApplyDetailService.getAggregatorEntDateApplyDetail(entId, date);
        //已提交默认计划，当前时间在提交时间之前，可以提交临时计划
        if (null == aggregatorEntDateApplyDetail && nowTime.compareTo(aggregatorEnt.getAllowApplyTime()) < 0) {
            aggregatorEntApplyPlanStatusResp.setApplyStatus(1);
            aggregatorEntApplyPlanStatusResp.setShowTime(aggregatorEnt.getAllowApplyTime());
            return aggregatorEntApplyPlanStatusResp;
        }
        //已提交默认计划，当前时间在提交时间之后，不可以提交临时计划
        if (null == aggregatorEntDateApplyDetail && nowTime.compareTo(aggregatorEnt.getAllowApplyTime()) >= 0) {
            aggregatorEntApplyPlanStatusResp.setApplyStatus(2);
            aggregatorEntApplyPlanStatusResp.setShowTime(DateUtils.getNextDay() + " 00:00");
            return aggregatorEntApplyPlanStatusResp;
        }
        //中标
        if (null != aggregatorEntDateApplyDetail.getWinStatus() && aggregatorEntDateApplyDetail.getWinStatus()) {
            aggregatorEntApplyPlanStatusResp.setApplyStatus(5);
            return aggregatorEntApplyPlanStatusResp;
        }
        //未中标
        if (null != aggregatorEntDateApplyDetail.getWinStatus() && !aggregatorEntDateApplyDetail.getWinStatus()) {
            aggregatorEntApplyPlanStatusResp.setApplyStatus(6);
            return aggregatorEntApplyPlanStatusResp;
        }
        //已提交临时计划，提交时间为当天，已提交
        if (aggregatorEntDateApplyDetail.getPlanStatus() && aggregatorEntDateApplyDetail.getApplyDate().equals(nowDate)) {
            aggregatorEntApplyPlanStatusResp.setApplyStatus(4);
            aggregatorEntApplyPlanStatusResp.setWinTime(aggregatorEnt.getWinTime());
            return aggregatorEntApplyPlanStatusResp;
        }
        //已提交临时计划，提交时间非当天，自动提交
        if (aggregatorEntDateApplyDetail.getPlanStatus() && !aggregatorEntDateApplyDetail.getApplyDate().equals(nowDate)) {
            aggregatorEntApplyPlanStatusResp.setApplyStatus(3);
            aggregatorEntApplyPlanStatusResp.setShowTime(aggregatorEnt.getAllowApplyTime());
            aggregatorEntApplyPlanStatusResp.setWinTime(aggregatorEnt.getWinTime());
            return aggregatorEntApplyPlanStatusResp;
        }
        //已提交默认计划，当前时间在提交时间之后，自动提交
        if (!aggregatorEntDateApplyDetail.getPlanStatus() && nowTime.compareTo(aggregatorEnt.getAllowApplyTime()) > 0) {
            aggregatorEntApplyPlanStatusResp.setApplyStatus(3);
            aggregatorEntApplyPlanStatusResp.setShowTime(aggregatorEnt.getAllowApplyTime());
            aggregatorEntApplyPlanStatusResp.setWinTime(aggregatorEnt.getWinTime());
            return aggregatorEntApplyPlanStatusResp;
        }
        //当天提交默认计划，当前时间在提交时间之后，不可提交新计划
        if (!aggregatorEntDateApplyDetail.getPlanStatus() && nowTime.compareTo(aggregatorEnt.getAllowApplyTime()) > 0 && aggregatorEntDateApplyDetail.getApplyDate().equals(nowDate)) {
            aggregatorEntApplyPlanStatusResp.setApplyStatus(2);
            aggregatorEntApplyPlanStatusResp.setShowTime(DateUtils.getNextDay() + " 00:00");
            return aggregatorEntApplyPlanStatusResp;
        }
        //已提交默认计划，当前时间在提交时间之前，可以提交临时计划
        if (null != aggregatorEntDateApplyDetail && !aggregatorEntDateApplyDetail.getPlanStatus() && nowTime.compareTo(aggregatorEnt.getAllowApplyTime()) < 0) {
            aggregatorEntApplyPlanStatusResp.setApplyStatus(1);
            aggregatorEntApplyPlanStatusResp.setShowTime(aggregatorEnt.getAllowApplyTime());
            return aggregatorEntApplyPlanStatusResp;
        }
        return aggregatorEntApplyPlanStatusResp;
    }

    @Override
    public List<AggregatorEntDateDeviceStartStopPlanResp> getDevicePlan(String entId, String date) {
        List<AggregatorEntDateDeviceStartStopPlanResp> aggregatorEntDateDeviceStartStopPlanRespList = Lists.newArrayList();
        if (StringUtils.isEmpty(date)) {
            date = DateUtils.getDay();
        }
        AggregatorEntDateApplyDetail aggregatorEntDateApplyDetail = aggregatorEntDateApplyDetailService.getAggregatorEntDateApplyDetail(entId, date);
//        if (null != aggregatorEntDateApplyDetail && null != aggregatorEntDateApplyDetail.getWinStatus() && aggregatorEntDateApplyDetail.getWinStatus()) {
        if (null != aggregatorEntDateApplyDetail) {
            AggregatorEntDateDeviceStartStopPlan aggregatorEntDateDeviceStartStopPlan = aggregatorEntDateDeviceStartStopPlanService.getAggregatorEntDateDeviceStartStopPlan(entId, date);
            if (null != aggregatorEntDateDeviceStartStopPlan && StringUtils.isNotEmpty(aggregatorEntDateDeviceStartStopPlan.getDetail())) {
                String detail = aggregatorEntDateDeviceStartStopPlan.getDetail();
                List<AggregatorEntDateDeviceStartStopPlanDetailResp> aggregatorEntDateDeviceStartStopPlanDetailRespList = JSONArray.parseArray(detail, AggregatorEntDateDeviceStartStopPlanDetailResp.class);
                List<String> deviceBaseIdList = aggregatorEntDateDeviceStartStopPlanDetailRespList.stream().map(AggregatorEntDateDeviceStartStopPlanDetailResp::getDeviceBaseId).distinct().collect(Collectors.toList());
                Map<String, String> deviceMap = new HashMap<>();
                if (CollectionUtils.isNotEmpty(deviceBaseIdList)) {
                    List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(deviceBaseIdList);
                    if (CollectionUtils.isNotEmpty(aggregatorEntDeviceList)) {
                        Map<String, String> deviceNameMap = aggregatorEntDeviceList.stream().collect(Collectors.toMap(AggregatorEntDevice::getDeviceBaseId, AggregatorEntDevice::getDeviceName, (v1, v2) -> v1));
                        deviceMap.putAll(deviceNameMap);
                    }
                }
                Map<String, List<AggregatorEntDateDeviceStartStopPlanDetailResp>> timeMap = aggregatorEntDateDeviceStartStopPlanDetailRespList.stream().collect(Collectors.groupingBy(AggregatorEntDateDeviceStartStopPlanDetailResp::getTime));
                timeMap.entrySet().forEach(timeValueMap -> {
                    AggregatorEntDateDeviceStartStopPlanResp aggregatorEntDateDeviceStartStopPlanResp = new AggregatorEntDateDeviceStartStopPlanResp();
                    aggregatorEntDateDeviceStartStopPlanResp.setTime(timeValueMap.getKey());
                    List<AggregatorEntDateDeviceStartStopPlanDetailResp> detailRespList = timeValueMap.getValue();
                    if (CollectionUtils.isNotEmpty(detailRespList)) {
                        List<String> contentList = Lists.newArrayList();
                        detailRespList.forEach(detailResp -> contentList.add(deviceMap.get(detailResp.getDeviceBaseId()) + detailResp.getDetail()));
                        aggregatorEntDateDeviceStartStopPlanResp.setContentList(contentList);
                    }
                    aggregatorEntDateDeviceStartStopPlanRespList.add(aggregatorEntDateDeviceStartStopPlanResp);
                });
            }
            if (CollectionUtils.isNotEmpty(aggregatorEntDateDeviceStartStopPlanRespList)) {
                List<AggregatorEntDateDeviceStartStopPlanResp> sortList = aggregatorEntDateDeviceStartStopPlanRespList.stream().sorted(Comparator.comparing(AggregatorEntDateDeviceStartStopPlanResp::getTime)).collect(Collectors.toList());
                AggregatorEntDateDeviceStartStopPlanResp aggregatorEntDateDeviceStartStopPlanResp = sortList.get(0);
                if (null != aggregatorEntDateDeviceStartStopPlanResp && !aggregatorEntDateDeviceStartStopPlanResp.getTime().equals("00:00")) {
                    AggregatorEntDateDeviceStartStopPlanResp insert = new AggregatorEntDateDeviceStartStopPlanResp();
                    insert.setTime("00:00");
                    insert.setContentList(Lists.newArrayList());
                    sortList.add(0, insert);
                }
                AggregatorEntDateDeviceStartStopPlanResp aggregatorEntDateDeviceStartStopPlanRespLast = sortList.get(sortList.size() - 1);
                if (null != aggregatorEntDateDeviceStartStopPlanRespLast && !aggregatorEntDateDeviceStartStopPlanRespLast.getTime().equals("24:00")) {
                    AggregatorEntDateDeviceStartStopPlanResp last = new AggregatorEntDateDeviceStartStopPlanResp();
                    last.setTime("24:00");
                    last.setContentList(Lists.newArrayList());
                    sortList.add(last);
                }
                return sortList.stream().sorted(Comparator.comparing(AggregatorEntDateDeviceStartStopPlanResp::getTime)).collect(Collectors.toList());
            }
        }
        return aggregatorEntDateDeviceStartStopPlanRespList;
    }

    @Override
    public AggregatorEntApplyPlanResp getDefaultPlanResp(String entId) {
        AggregatorEntApplyPlanResp aggregatorEntApplyPlanResp = new AggregatorEntApplyPlanResp();
        aggregatorEntApplyPlanResp.setAppApplyIndexDeviceDetailRespList(Lists.newArrayList());
        Weekend<AggregatorEntApplyPlan> weekend = Weekend.of(AggregatorEntApplyPlan.class);
        WeekendCriteria<AggregatorEntApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntApplyPlan::getEntId, entId);
        criteria.andEqualTo(AggregatorEntApplyPlan::getPlanStatus, false);
        List<AggregatorEntApplyPlan> aggregatorEntApplyPlanList = aggregatorEntApplyPlanMapper.selectByExample(weekend);
        if (CollectionUtils.isEmpty(aggregatorEntApplyPlanList)) {
            return aggregatorEntApplyPlanResp;
        }
        AggregatorEntApplyPlan aggregatorEntApplyPlan = aggregatorEntApplyPlanList.get(0);
        if (null != aggregatorEntApplyPlan) {
            BeanUtils.copyProperties(aggregatorEntApplyPlan, aggregatorEntApplyPlanResp);
            String startDate = DateUtils.format(aggregatorEntApplyPlanResp.getStartDate() + " 00:00:00", "yyyy年MM月dd日");
            String endDate = DateUtils.format(aggregatorEntApplyPlanResp.getEndDate() + " 00:00:00", "yyyy年MM月dd日");
            if (StringUtils.isEmpty(startDate)) {
                aggregatorEntApplyPlanResp.setShowDate(endDate);
            } else if (StringUtils.isEmpty(endDate)) {
                aggregatorEntApplyPlanResp.setShowDate(startDate);
            } else if (startDate.equals(endDate)) {
                aggregatorEntApplyPlanResp.setShowDate(startDate);
            } else {
                aggregatorEntApplyPlanResp.setShowDate(startDate + "~" + endDate);
            }
            String detail = aggregatorEntApplyPlan.getDetail();
            if (StringUtils.isNotEmpty(detail)) {
                List<AppApplyIndexDeviceDetailResp> appApplyIndexDeviceDetailRespList = JSONArray.parseArray(detail, AppApplyIndexDeviceDetailResp.class);
                if (CollectionUtils.isNotEmpty(appApplyIndexDeviceDetailRespList)) {
                    Map<String, String> deviceMap = new HashMap<>();
                    List<String> deviceBaseIdList = appApplyIndexDeviceDetailRespList.stream().map(AppApplyIndexDeviceDetailResp::getDeviceBaseId).collect(Collectors.toList());
                    List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(deviceBaseIdList);
                    if (CollectionUtils.isNotEmpty(deviceList)) {
                        Map<String, String> deviceInfoMap = deviceList.stream().collect(Collectors.toMap(AggregatorEntDevice::getDeviceBaseId, AggregatorEntDevice::getDeviceName, (v1, v2) -> v1));
                        deviceMap.putAll(deviceInfoMap);
                    }
                    appApplyIndexDeviceDetailRespList.forEach(resp -> resp.setDeviceName(deviceMap.get(resp.getDeviceBaseId())));
                }
                aggregatorEntApplyPlanResp.setAppApplyIndexDeviceDetailRespList(appApplyIndexDeviceDetailRespList);
            }
        }
        return aggregatorEntApplyPlanResp;
    }

    @Override
    public AggregatorEntApplyPlanResp getApplyPlanResp(String entId, Boolean planStatus, String date, Boolean saveStatus) {
        AggregatorEntApplyPlanResp aggregatorEntApplyPlanResp = new AggregatorEntApplyPlanResp();
        Weekend<AggregatorEntApplyPlan> weekend = Weekend.of(AggregatorEntApplyPlan.class);
        WeekendCriteria<AggregatorEntApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntApplyPlan::getEntId, entId);
        criteria.andLessThanOrEqualTo(AggregatorEntApplyPlan::getStartDate, date);
        criteria.andGreaterThanOrEqualTo(AggregatorEntApplyPlan::getEndDate, date);
        criteria.andEqualTo(AggregatorEntApplyPlan::getSaveStatus, saveStatus);
        criteria.andEqualTo(AggregatorEntApplyPlan::getPlanStatus, planStatus);
        weekend.orderBy("applyTime").desc();
        List<AggregatorEntApplyPlan> aggregatorEntApplyPlanList = aggregatorEntApplyPlanMapper.selectByExample(weekend);
        if (null != aggregatorEntApplyPlanList && aggregatorEntApplyPlanList.size() > 0) {
            AggregatorEntApplyPlan aggregatorEntApplyPlan = aggregatorEntApplyPlanList.get(0);
            if (null != aggregatorEntApplyPlan) {
                BeanUtils.copyProperties(aggregatorEntApplyPlan, aggregatorEntApplyPlanResp);
                String detail = aggregatorEntApplyPlan.getDetail();
                if (StringUtils.isNotEmpty(detail)) {
                    List<AppApplyIndexDeviceDetailResp> appApplyIndexDeviceDetailRespList = JSONArray.parseArray(detail, AppApplyIndexDeviceDetailResp.class);
                    aggregatorEntApplyPlanResp.setAppApplyIndexDeviceDetailRespList(appApplyIndexDeviceDetailRespList);
                }
            }
        }
        return aggregatorEntApplyPlanResp;
    }

    @Override
    public PageResultVO<AggregatorEntApplyPlanResp> getAggregatorEntApplyPlanRespList(String entId, String type, String planType, String date, Integer pageNo, Integer pageSize) {
        String now = DateUtils.getDay();
        PageResultVO<AggregatorEntApplyPlanResp> pageResultVO = new PageResultVO<>();
        pageResultVO.setPageIndex(pageNo);
        pageResultVO.setPageSize(pageSize);
        pageResultVO.setTotal(0);
//        PageHelper.startPage(pageNo, pageSize);
        List<AggregatorEntApplyPlanResp> aggregatorEntApplyPlanRespList = Lists.newArrayList();
        if (StringUtils.isNotEmpty(planType)) {
            if (planType.equals(AggregatorEntPlanTypeEnum.ALL.getCode())) {
                aggregatorEntApplyPlanRespList = aggregatorEntApplyPlanMapper.getAggregatorEntApplyPlanRespListByPlanStatus(entId, true);
            } else if (planType.equals(AggregatorEntPlanTypeEnum.DEFAULT.getCode())) {
                aggregatorEntApplyPlanRespList = aggregatorEntApplyPlanMapper.getAggregatorEntApplyPlanRespListByPlanStatus(entId, false);
            } else if (planType.equals(AggregatorEntPlanTypeEnum.FINISH.getCode())) {
                aggregatorEntApplyPlanRespList = aggregatorEntApplyPlanMapper.getAggregatorEntApplyPlanRespListByFinish(entId, true, now);
            } else if (planType.equals(AggregatorEntPlanTypeEnum.NOW.getCode())) {
//                pageSize = 1;
//                PageHelper.startPage(pageNo, pageSize);
                aggregatorEntApplyPlanRespList = aggregatorEntApplyPlanMapper.getAggregatorEntApplyPlanRespListByNowByLimitOne(entId, true, now);
            } else if (planType.equals(AggregatorEntPlanTypeEnum.NOSTART.getCode())) {
                aggregatorEntApplyPlanRespList = aggregatorEntApplyPlanMapper.getAggregatorEntApplyPlanRespListByNoStart(entId, true, now);
            }
        } else if (StringUtils.isNotEmpty(date)) {
//            pageSize = 1;
//            PageHelper.startPage(pageNo, pageSize);
            aggregatorEntApplyPlanRespList = aggregatorEntApplyPlanMapper.getAggregatorEntApplyPlanRespListByNowByLimitOne(entId, true, date);
        } else {
            aggregatorEntApplyPlanRespList = aggregatorEntApplyPlanMapper.getAggregatorEntApplyPlanRespListByEntId(entId);
        }
        if (CollectionUtils.isEmpty(aggregatorEntApplyPlanRespList)) {
            pageResultVO.setList(new ArrayList<>());
            return pageResultVO;
        }
        pageResultVO.setTotal(aggregatorEntApplyPlanRespList.size());
        AtomicReference<Boolean> nowPlanFlag = new AtomicReference<>(true);
        List<AggregatorEntApplyPlanResp> resultList = Lists.newArrayList();
        aggregatorEntApplyPlanRespList.forEach(resp -> {
            resp.setShowPlanSort("1");
            String startDate = DateUtils.format(resp.getStartDate() + " 00:00:00", "yyyy年MM月dd日");
            String endDate = DateUtils.format(resp.getEndDate() + " 00:00:00", "yyyy年MM月dd日");
            if (StringUtils.isEmpty(startDate)) {
                resp.setShowDate(endDate);
            } else if (StringUtils.isEmpty(endDate)) {
                resp.setShowDate(startDate);
            } else if (startDate.equals(endDate)) {
                resp.setShowDate(startDate);
            } else {
                resp.setShowDate(startDate + "~" + endDate);
            }
            if (StringUtils.isNotEmpty(resp.getStartDate()) && StringUtils.isNotEmpty(resp.getEndDate())) {
                if (!resp.getPlanStatus()) {
                    resp.setShowPlanStatus(AggregatorEntPlanTypeEnum.DEFAULT.getCode());
                } else if (now.compareTo(resp.getEndDate()) > 0) {
                    resp.setShowPlanStatus(AggregatorEntPlanTypeEnum.FINISH.getCode());
                } else if (now.compareTo(resp.getStartDate()) >= 0) {
                    if (planType.equals(AggregatorEntPlanTypeEnum.NOSTART.getCode())) {
                        resp.setShowPlanStatus(AggregatorEntPlanTypeEnum.NOSTART.getCode());
                    } else if (nowPlanFlag.get()) {
                        resp.setShowPlanStatus(AggregatorEntPlanTypeEnum.NOW.getCode());
                        nowPlanFlag.set(false);
                        resp.setShowPlanSort("0");
                    } else {
                        resp.setShowPlanStatus(AggregatorEntPlanTypeEnum.NOSTART.getCode());
                    }
                } else if (now.compareTo(resp.getStartDate()) < 0) {
                    resp.setShowPlanStatus(AggregatorEntPlanTypeEnum.NOSTART.getCode());
                }
            }
            resultList.add(resp);
        });
        List<AggregatorEntApplyPlanResp> sortList = resultList.stream().sorted(Comparator.comparing(AggregatorEntApplyPlanResp::getShowPlanSort).thenComparing(AggregatorEntApplyPlanResp::getApplyTime, Comparator.reverseOrder())).collect(Collectors.toList());
        pageResultVO.setList(sortList);
        if (CollectionUtils.isNotEmpty(sortList)) {
            int fromIndex = (pageNo - 1) * pageSize;
            if (fromIndex > sortList.size() - 1) {
                pageResultVO.setPageIndex((sortList.size() - 1) / pageSize + 1);
                fromIndex = (pageResultVO.getPageIndex() - 1) * pageSize;
            }
            int toIndex = pageNo * pageSize;
            if (toIndex > sortList.size()) {
                toIndex = sortList.size();
            }
            pageResultVO.setList(sortList.subList(fromIndex, toIndex));
        }
        return pageResultVO;
    }

    /**
     * 查询数据
     *
     * @param entId
     * @param date
     * @param saveStatus
     * @return
     */
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

    /**
     * 查询数据
     *
     * @param entId
     * @param date
     * @return
     */
    private int getAggregatorEntApplyPlanListCount(String entId, String date) {
        Weekend<AggregatorEntApplyPlan> weekend = Weekend.of(AggregatorEntApplyPlan.class);
        WeekendCriteria<AggregatorEntApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntApplyPlan::getEntId, entId);
        criteria.andLessThanOrEqualTo(AggregatorEntApplyPlan::getStartDate, date);
        criteria.andGreaterThanOrEqualTo(AggregatorEntApplyPlan::getEndDate, date);
        weekend.orderBy("applyTime").desc();
        return aggregatorEntApplyPlanMapper.selectCountByExample(weekend);
    }

    /**
     * 查询数据
     *
     * @param entId
     * @param planStatus
     * @param saveStatus
     * @return
     */
    private int getAggregatorEntApplyPlanListCount(String entId, Boolean planStatus, Boolean saveStatus) {
        Weekend<AggregatorEntApplyPlan> weekend = Weekend.of(AggregatorEntApplyPlan.class);
        WeekendCriteria<AggregatorEntApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntApplyPlan::getEntId, entId);
        criteria.andEqualTo(AggregatorEntApplyPlan::getPlanStatus, planStatus);
        criteria.andEqualTo(AggregatorEntApplyPlan::getSaveStatus, saveStatus);
        weekend.orderBy("applyTime").desc();
        return aggregatorEntApplyPlanMapper.selectCountByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addApplyPlanV1(AggregatorEntApplyPlanReq aggregatorEntApplyPlanReq) {
        String now = DateUtils.getTime();
        //参数校验
        if (aggregatorEntApplyPlanReq.getStartDate().compareTo(aggregatorEntApplyPlanReq.getEndDate()) > 0) {
            throw new BaseException(StatusCode.ERROR.getCode(), "结束时间必须大于或等于开始时间");
        }
        AggregatorEntApplyDateResp aggregatorEntApplyDateResp = getDate(aggregatorEntApplyPlanReq.getEntId(), DateUtils.getDay());
        if (null != aggregatorEntApplyDateResp && StringUtils.isNotEmpty(aggregatorEntApplyDateResp.getStartDate()) && StringUtils.isNotEmpty(aggregatorEntApplyDateResp.getEndDate())) {
            if (aggregatorEntApplyDateResp.getStartDate().compareTo(aggregatorEntApplyPlanReq.getStartDate()) > 0) {
                throw new BaseException(StatusCode.ERROR.getCode(), "开始时间必须大于或等于：" + aggregatorEntApplyDateResp.getStartDate());
            }
        } else {
            AggregatorEnt aggregatorEnt = aggregatorEntService.getAggregatorEnt(aggregatorEntApplyPlanReq.getEntId());
            if (DateUtils.format(now, "HH:mm").compareTo(aggregatorEnt.getAllowApplyTime()) >= 0) {
                //当前时间大于等于申报时间
                if (aggregatorEntApplyPlanReq.getStartDate().compareTo(DateUtils.getNextDay()) <= 0) {
                    //申报开始时间小于等于明天
                    throw new BaseException(StatusCode.ERROR.getCode(), "已超过企业申报时间：" + aggregatorEnt.getAllowApplyTime() + "，开始时间必须大于或等于：" + DateUtils.getAddDate(DateUtils.getNextDay()));
                }
            } else {
                //当前时间小于可申报时间
                if (aggregatorEntApplyPlanReq.getStartDate().compareTo(DateUtils.getDay()) <= 0) {
                    //申报开始时间小于等于当前时间
                    throw new BaseException(StatusCode.ERROR.getCode(), "开始时间必须大于或等于：" + DateUtils.getNextDay());
                }
            }
        }

        List<String> dateList = DateUtils.getDayList(aggregatorEntApplyPlanReq.getStartDate(), aggregatorEntApplyPlanReq.getEndDate());
        Weekend<AggregatorEntApplyPlan> weekend = Weekend.of(AggregatorEntApplyPlan.class);
        WeekendCriteria<AggregatorEntApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntApplyPlan::getEntId, aggregatorEntApplyPlanReq.getEntId());
        criteria.andEqualTo(AggregatorEntApplyPlan::getPlanStatus, false);
        criteria.andEqualTo(AggregatorEntApplyPlan::getSaveStatus, true);
        int count = aggregatorEntApplyPlanMapper.selectCountByExample(weekend);
        if (count > 0) {
            aggregatorEntApplyPlanReq.setPlanStatus(true);
        } else {
            aggregatorEntApplyPlanReq.setPlanStatus(false);
        }
        //保存申报计划
        AggregatorEntApplyPlan aggregatorEntApplyPlan = new AggregatorEntApplyPlan();
        BeanUtils.copyProperties(aggregatorEntApplyPlanReq, aggregatorEntApplyPlan);
        aggregatorEntApplyPlan.setApplyTime(now);
        List<AppApplyIndexDeviceDetailResp> appApplyIndexDeviceDetailRespList = aggregatorEntApplyPlanReq.getAppApplyIndexDeviceDetailRespList();
        if (null != appApplyIndexDeviceDetailRespList && appApplyIndexDeviceDetailRespList.size() > 0) {
            aggregatorEntApplyPlan.setDetail(JSONObject.toJSONString(appApplyIndexDeviceDetailRespList));
        }
        AggregatorEntApplyPlan aggregatorEntApplyPlanBySaveStatus = getApplyPlan(aggregatorEntApplyPlanReq.getEntId(), aggregatorEntApplyPlanReq.getPlanStatus(), false);
        if (null != aggregatorEntApplyPlanBySaveStatus && null != aggregatorEntApplyPlanBySaveStatus.getId()) {
            aggregatorEntApplyPlanMapper.deleteByPrimaryKey(aggregatorEntApplyPlanBySaveStatus.getId());
        }
        aggregatorEntApplyPlanMapper.insertSelective(aggregatorEntApplyPlan);
        if (aggregatorEntApplyPlanReq.getSaveStatus()) {
            //写入企业申报详情，设备申报功率，聚合商申报功率==聚合商查询申报功率实时变化
            executor.execute(() -> addData(aggregatorEntApplyPlanReq, dateList, now));
        }
        return true;
    }

    @Override
    public AggregatorEntApplyDateResp getDate(String entId, String date) {
        AggregatorEntApplyDateResp aggregatorEntApplyDateResp = new AggregatorEntApplyDateResp();
        AggregatorEnt aggregatorEnt = aggregatorEntService.getAggregatorEnt(entId);
        if (null == aggregatorEnt) {
            throw new BaseException(StatusCode.ERROR.getCode(), "未查询到企业信息");
        }
        String dateTime = DateUtils.getTime();
        if (StringUtils.isEmpty(date)) {
            date = DateUtils.format(dateTime, "yyyy-MM-dd");
        }
        String time = DateUtils.format(dateTime, "HH:mm:ss");
        if (time.compareTo(aggregatorEnt.getAllowApplyTime()) >= 0) {
            date = DateUtils.getAddDate(date, 1);
        }
        Boolean applyDateCheck = aggregatorDateHolidayService.getApplyDateCheck(date);
        if (applyDateCheck) {
            List<String> applyDateList = aggregatorDateHolidayService.getApplyDateList(date, false);
            date = DateUtils.getAddDate(applyDateList.get(applyDateList.size() - 1), 1);
        } else {
            date = DateUtils.getAddDate(date, 1);
        }
        aggregatorEntApplyDateResp.setStartDate(date);
        aggregatorEntApplyDateResp.setEndDate(date);
        return aggregatorEntApplyDateResp;
    }
}
