package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.console.config.RedisLock;
import cn.sl.ehub.console.enums.ApplyPlanStatusContentEnum;
import cn.sl.ehub.console.enums.Constants;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.service.mapper.*;
import cn.sl.ehub.console.model.req.AggregatorEntDeviceApplyPlanDateReq;
import cn.sl.ehub.console.model.req.AggregatorEntDeviceApplyPlanDetailReq;
import cn.sl.ehub.console.model.req.AggregatorEntDeviceApplyPlanDeviceReq;
import cn.sl.ehub.console.model.req.AggregatorEntDeviceApplyPlanReq;
import cn.sl.ehub.service.resp.*;
import cn.sl.ehub.console.service.*;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.common.utils.RedisUtil;
import cn.sl.ehub.service.vo.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * 申报计划ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class ApplyPlanServiceImpl implements IApplyPlanService {

    private final IAggregatorEntDeviceApplyPlanService aggregatorEntDeviceApplyPlanService;
    private final IAggregatorDeviceDateDeliveryChartService aggregatorDeviceDateDeliveryChartService;
    private final IAggregatorDateDeliveryChartService aggregatorDateDeliveryChartService;
    private final IAggregatorEntDeviceService aggregatorEntDeviceService;
    private final IAggregatorDateHolidayService aggregatorDateHolidayService;
    private final IAggregatorEntDateApplyDetailService aggregatorEntDateApplyDetailService;
    private final IAggregatorEntService aggregatorEntService;
    private final RedisUtil redis;
    @Resource(name = "threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor executor;
    @Resource(name = "redisTemplate")
    protected RedisTemplate redisTemplate;

    @Override
    public AppApplyIndexResp getApplyIndexResp(String entId, String time) {
        //用户未申报，时间未到9点，status 为 0，今日是工作日，展示时间列表配置
        //用户未申报，时间未到9点，status 为 3，今日是非工作日，展示计划未提交
        //用户未申报，时间已过9点，status 为 2，今日是工作日，展示申报已结束
        //用户未申报，时间已过9点，status 为 3，今日是非工作日，展示计划未提交
        //用户已申报, 时间是当天，status 为 1, 完成申报
        //用户已申报，时间非当天，status 为 1, 计划已提交
        AggregatorEnt aggregatorEnt = aggregatorEntService.getAggregatorEnt(entId);
        if (null == aggregatorEnt) {
            throw new BaseException(StatusCode.ERROR.getCode(), "未查到企业信息");
        }
        AppApplyIndexResp resp = new AppApplyIndexResp();
        if (StringUtils.isEmpty(time)) {
            time = DateUtils.getTime();
        }
        String date = DateUtils.format(time, "yyyy-MM-dd");
        String hour = DateUtils.format(time, "HH:mm:ss");
        List<String> applyDateList = aggregatorDateHolidayService.getApplyDateList(date, true);
        String startDate = applyDateList.get(1);
        String endDate = applyDateList.get(applyDateList.size() - 1);
        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 00:00:00";
        //查询用户申报
        List<AggregatorEntDateApplyDetail> aggregatorEntDateApplyDetailList = aggregatorEntDateApplyDetailService.getAggregatorEntDateApplyDetailList(entId, DateUtils.getAddDate(date));
        boolean flag = true;
        if (null != aggregatorEntDateApplyDetailList && aggregatorEntDateApplyDetailList.size() > 0) {
            //已申报
            AggregatorEntDateApplyDetail aggregatorEntDateApplyDetail = aggregatorEntDateApplyDetailList.get(0);
            if (date.equals(aggregatorEntDateApplyDetail.getApplyDate())) {
                //用户已申报, 时间是当天，status 为 1, 完成申报
                resp.setStatus(1);
                resp.setTitle(String.format(ApplyPlanStatusContentEnum.FINAL_APPLY_TODAY.getTitle(), DateUtils.format(aggregatorEntDateApplyDetail.getApplyTime(), "HH:mm")));
                resp.setApplyPlan(String.format(ApplyPlanStatusContentEnum.FINAL_APPLY_TODAY.getApplyPlan(), startTime.equals(endTime) ? DateUtils.format(startTime, "MM月dd日") : DateUtils.format(startTime, "MM月dd日") + "~" + DateUtils.format(endTime, "MM月dd日")));
                resp.setTimeColor(DateUtils.format(endTime, "MM月dd日HH:mm"));
            } else {
                //用户已申报，时间非当天，status 为 1, 计划已提交
                resp.setStatus(1);
                resp.setTitle(ApplyPlanStatusContentEnum.FINAL_APPLY_NO_TODAY.getTitle());
                resp.setContent(String.format(ApplyPlanStatusContentEnum.FINAL_APPLY_NO_TODAY.getContent(), DateUtils.format(aggregatorEntDateApplyDetail.getApplyTime(), "yyyy-MM-dd HH:mm")));
                resp.setApplyPlan(String.format(ApplyPlanStatusContentEnum.FINAL_APPLY_NO_TODAY.getApplyPlan(), startTime.equals(endTime) ? DateUtils.format(startTime, "MM月dd日") : DateUtils.format(startTime, "MM月dd日") + "~" + DateUtils.format(endTime, "MM月dd日")));
                resp.setTimeColor(DateUtils.format(endTime, "MM月dd日HH:mm"));
            }
        } else {
            //未申报
            if (hour.compareTo(aggregatorEnt.getAllowApplyTime()) >= 0) {
                if (applyDateList.get(0).equals(date) || endDate.equals(date)) {
                    //用户未申报，时间已过9点，status 为 2，今日是工作日，展示申报已结束
                    resp.setStatus(2);
                    resp.setTitle(ApplyPlanStatusContentEnum.NO_FINAL_APPLY_TODAY.getTitle());
                    resp.setApplyPlan(String.format(ApplyPlanStatusContentEnum.NO_FINAL_APPLY_TODAY.getApplyPlan(), startTime.equals(endTime) ? DateUtils.format(startTime, "MM月dd日") : DateUtils.format(startTime, "MM月dd日") + "~" + DateUtils.format(endTime, "MM月dd日")));
                    resp.setTimeColor(DateUtils.format(endTime, "MM月dd日HH:mm"));
                } else {
                    //用户未申报，时间已过9点，status 为 3，今日是非工作日，展示计划未提交
                    resp.setStatus(3);
                    resp.setTitle(ApplyPlanStatusContentEnum.NO_FINAL_APPLY_NO_TODAY.getTitle());
                    resp.setContent(String.format(ApplyPlanStatusContentEnum.NO_FINAL_APPLY_NO_TODAY.getContent(), DateUtils.format(applyDateList.get(0) + " 09:00:00", "yyyy-MM-dd HH:mm")));
                    resp.setApplyPlan(String.format(ApplyPlanStatusContentEnum.NO_FINAL_APPLY_NO_TODAY.getApplyPlan(), startTime.equals(endTime) ? DateUtils.format(startTime, "MM月dd日") : DateUtils.format(startTime, "MM月dd日") + "~" + DateUtils.format(endTime, "MM月dd日")));
                    resp.setTimeColor(DateUtils.format(endTime, "MM月dd日HH:mm"));
                }
                flag = false;
            } else {
                if (applyDateList.get(0).equals(date) || endDate.equals(date)) {
                    //用户未申报，时间未到9点，status 为 0，今日是工作日，展示时间列表配置
                    resp.setStatus(0);
                } else {
                    //用户未申报，时间未到9点，status 为 3，今日是非工作日，展示计划未提交
                    resp.setStatus(3);
                    resp.setTitle(ApplyPlanStatusContentEnum.NO_FINAL_APPLY_NO_TODAY.getTitle());
                    resp.setContent(String.format(ApplyPlanStatusContentEnum.NO_FINAL_APPLY_NO_TODAY.getContent(), DateUtils.format(applyDateList.get(0) + " 09:00:00", "yyyy-MM-dd HH:mm")));
                    resp.setApplyPlan(String.format(ApplyPlanStatusContentEnum.NO_FINAL_APPLY_NO_TODAY.getApplyPlan(), startTime.equals(endTime) ? DateUtils.format(startTime, "MM月dd日") : DateUtils.format(startTime, "MM月dd日") + "~" + DateUtils.format(endTime, "MM月dd日")));
                    resp.setTimeColor(DateUtils.format(endTime, "MM月dd日HH:mm"));
                    flag = false;
                }
            }
        }
        if (flag) {
            resp.setPlanList(getAppApplyIndexDetailRespList(entId, startDate, endDate, applyDateList));
        }
        return resp;
    }

    /**
     * 查询申报计划
     *
     * @param entId
     * @param startDate
     * @param endDate
     * @param applyDateList
     * @return
     */
    private List<AppApplyIndexDetailResp> getAppApplyIndexDetailRespList(String entId, String startDate, String endDate, List<String> applyDateList) {
        List<AppApplyIndexDetailResp> planList = Lists.newArrayList();
        List<AggregatorEntDeviceApplyPlan> aggregatorEntDeviceApplyPlanList = aggregatorEntDeviceApplyPlanService.getAggregatorEntDeviceApplyPlanListByDate(entId, startDate, endDate);
        if (null != aggregatorEntDeviceApplyPlanList && aggregatorEntDeviceApplyPlanList.size() > 0) {
            List<String> deviceBaseIdList = aggregatorEntDeviceApplyPlanList.stream().map(AggregatorEntDeviceApplyPlan::getDeviceBaseId).collect(toList());
            List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(deviceBaseIdList);
            Map<String, AggregatorEntDevice> deviceBaseIdNameMap = deviceList.stream().collect(toMap(AggregatorEntDevice::getDeviceBaseId, Function.identity(), (v1, v2) -> v1));
            Map<String, Map<String, List<AggregatorEntDeviceApplyPlan>>> dateDeviceBaseIdMap = aggregatorEntDeviceApplyPlanList.stream().collect(groupingBy(AggregatorEntDeviceApplyPlan::getDate, groupingBy(AggregatorEntDeviceApplyPlan::getDeviceBaseId)));
            applyDateList.subList(1, applyDateList.size()).forEach(applyDate -> {
                AppApplyIndexDetailResp appApplyIndexDetailResp = new AppApplyIndexDetailResp();
                appApplyIndexDetailResp.setDate(applyDate);
                Map<String, List<AggregatorEntDeviceApplyPlan>> deviceBaseIdMap = dateDeviceBaseIdMap.get(applyDate);
                if (null == deviceBaseIdMap || deviceBaseIdMap.size() <= 0) {
                    deviceBaseIdMap = new HashMap<>();
                }
                List<AppApplyIndexDeviceDetailResp> appApplyIndexDeviceDetailRespList = Lists.newArrayList();
                Map<String, List<AggregatorEntDeviceApplyPlan>> finalDeviceBaseIdMap = deviceBaseIdMap;
                aggregatorEntDeviceApplyPlanList.forEach(plan -> {
                    AppApplyIndexDeviceDetailResp appApplyIndexDeviceDetailResp = new AppApplyIndexDeviceDetailResp();
                    AggregatorEntDevice aggregatorEntDevice = deviceBaseIdNameMap.get(plan.getDeviceBaseId());
                    if (null != aggregatorEntDevice) {
                        appApplyIndexDeviceDetailResp.setResourceTypeId(aggregatorEntDevice.getResourceTypeId());
                        appApplyIndexDeviceDetailResp.setDeviceBaseId(plan.getDeviceBaseId());
                        appApplyIndexDeviceDetailResp.setDeviceName(aggregatorEntDevice.getDeviceName());
                        appApplyIndexDeviceDetailResp.setDevicePower(aggregatorEntDevice.getPower());
                    }
                    List<AggregatorEntDeviceApplyPlan> applyPlanList = finalDeviceBaseIdMap.get(plan.getDeviceBaseId());
                    if (null != applyPlanList && applyPlanList.size() > 0) {
                        List<AppApplyIndexDeviceTimeDetailResp> appApplyIndexDeviceTimeDetailRespList = Lists.newArrayList();
                        applyPlanList.forEach(applyPlan -> {
                            AppApplyIndexDeviceTimeDetailResp appApplyIndexDeviceTimeDetailResp = new AppApplyIndexDeviceTimeDetailResp();
                            appApplyIndexDeviceTimeDetailResp.setStartTime(applyPlan.getStartTime());
                            appApplyIndexDeviceTimeDetailResp.setEndTime(applyPlan.getEndTime());
                            appApplyIndexDeviceTimeDetailResp.setPower(applyPlan.getPlanPower());
                            appApplyIndexDeviceTimeDetailRespList.add(appApplyIndexDeviceTimeDetailResp);
                        });
                        appApplyIndexDeviceDetailResp.setTimeList(appApplyIndexDeviceTimeDetailRespList);
                    }
                    appApplyIndexDeviceDetailRespList.add(appApplyIndexDeviceDetailResp);
                });
                appApplyIndexDetailResp.setDeviceList(appApplyIndexDeviceDetailRespList);
                planList.add(appApplyIndexDetailResp);
            });
        } else {
            applyDateList.subList(1, applyDateList.size()).forEach(applyDate -> {
                AppApplyIndexDetailResp appApplyIndexDetailResp = new AppApplyIndexDetailResp();
                appApplyIndexDetailResp.setDate(applyDate);
                appApplyIndexDetailResp.setDeviceList(Lists.newArrayList());
                planList.add(appApplyIndexDetailResp);
            });
        }
        return planList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveAggregatorEntDeviceApplyPlanList(AggregatorEntDeviceApplyPlanReq req) {
        if (null == req || StringUtils.isEmpty(req.getEntId()) || StringUtils.isEmpty(req.getStationId())) {
            throw new BaseException(StatusCode.ERROR.getCode(), "企业ID为空");
        }
        List<AggregatorEntDeviceApplyPlanDateReq> aggregatorEntDeviceApplyPlanDateReqList = req.getDetailList();
        if (null == aggregatorEntDeviceApplyPlanDateReqList || aggregatorEntDeviceApplyPlanDateReqList.size() <= 0) {
            throw new BaseException(StatusCode.ERROR.getCode(), "请配置设备申报时间");
        }
        //根据企业ID查询聚合商ID
        String aggregatorId = aggregatorEntService.getAggregatorIdByEntId(req.getEntId());
        if (StringUtils.isEmpty(aggregatorId)) {
            throw new BaseException(StatusCode.ERROR.getCode(), "本企业未绑定聚合商");
        }
        try {
            List<String> dateList = aggregatorEntDeviceApplyPlanDateReqList.stream().map(AggregatorEntDeviceApplyPlanDateReq::getDate).collect(Collectors.toList());
            aggregatorEntDeviceApplyPlanService.delete(req.getEntId(), dateList);
            List<AggregatorEntDeviceApplyPlan> planList = Lists.newArrayList();
            aggregatorEntDeviceApplyPlanDateReqList.forEach(date -> {
                List<AggregatorEntDeviceApplyPlanDeviceReq> deviceList = date.getDeviceList();
                if (null != deviceList && deviceList.size() > 0) {
                    deviceList.forEach(device -> {
                        List<AggregatorEntDeviceApplyPlanDetailReq> detailList = device.getTimeList();
                        if (null != detailList && detailList.size() > 0) {
                            detailList.forEach(detail -> {
                                AggregatorEntDeviceApplyPlan plan = new AggregatorEntDeviceApplyPlan();
                                plan.setAggregatorId(aggregatorId);
                                plan.setEntId(req.getEntId());
                                plan.setResourceTypeId(device.getResourceTypeId());
                                plan.setDeviceBaseId(device.getDeviceBaseId());
                                plan.setDate(date.getDate());
                                plan.setStartTime(detail.getStartTime());
                                plan.setEndTime(detail.getEndTime());
                                plan.setPlanPower(detail.getPower());
                                planList.add(plan);
                            });
                        }
                    });
                }
            });
            if (null != planList && planList.size() > 0) {
                aggregatorEntDeviceApplyPlanService.batchInsert(planList);
            }
        } catch (Exception e) {
            throw new BaseException(StatusCode.ERROR.getCode(), "保存申报计划失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean apply(AggregatorEntDeviceApplyPlanReq req) {
        if (null == req || StringUtils.isEmpty(req.getEntId()) || StringUtils.isEmpty(req.getStationId())) {
            throw new BaseException(StatusCode.ERROR.getCode(), "企业ID为空");
        }
        if (null == req.getDetailList() || req.getDetailList().size() <= 0) {
            throw new BaseException(StatusCode.ERROR.getCode(), "请配置设备申报时间");
        }
        //保存设备申报计划
        saveAggregatorEntDeviceApplyPlanList(req);
        //根据企业ID查询聚合商ID
        String aggregatorId = aggregatorEntService.getAggregatorIdByEntId(req.getEntId());
        if (StringUtils.isEmpty(aggregatorId)) {
            throw new BaseException(StatusCode.ERROR.getCode(), "本企业未绑定聚合商");
        }
        String now = DateUtils.getTime();
        //保存设备申报曲线、企业申报状态、聚合商申报曲线
        saveDeliveryChart(now, aggregatorId, req);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDeliveryChart(String now, String aggregatorId, AggregatorEntDeviceApplyPlanReq req) {
        List<AggregatorDeviceDateDeliveryChart> aggregatorDeviceDateDeliveryChartList = Lists.newArrayList();
        List<AggregatorEntDateApplyDetail> aggregatorEntDateApplyDetailList = Lists.newArrayList();
        List<String> dateList = req.getDetailList().stream().map(AggregatorEntDeviceApplyPlanDateReq::getDate).distinct().collect(toList());
        req.getDetailList().forEach(date -> {
            List<AggregatorEntDeviceApplyPlanDeviceReq> deviceList = date.getDeviceList();
            if (null != deviceList && deviceList.size() > 0) {
                deviceList.forEach(device -> {
                    List<AggregatorEntDeviceApplyPlanDetailReq> detailList = device.getTimeList();
                    if (null != detailList && detailList.size() > 0) {
                        AggregatorDeviceDateDeliveryChart aggregatorDeviceDateDeliveryChart = new AggregatorDeviceDateDeliveryChart();
                        aggregatorDeviceDateDeliveryChart.setAggregatorId(aggregatorId);
                        aggregatorDeviceDateDeliveryChart.setEntId(req.getEntId());
                        aggregatorDeviceDateDeliveryChart.setStationId(req.getStationId());
                        aggregatorDeviceDateDeliveryChart.setResourceTypeId(device.getResourceTypeId());
                        aggregatorDeviceDateDeliveryChart.setDeviceBaseId(device.getDeviceBaseId());
                        aggregatorDeviceDateDeliveryChart.setDate(date.getDate());
                        List<DataResp> dataRespList = Lists.newArrayList();
                        detailList.forEach(detail -> {
                            String startTime = date.getDate() + " " + detail.getStartTime() + ":00";
                            String endTime = date.getDate() + " " + detail.getEndTime() + ":00";
                            if (detail.getEndTime().equals("24:00")) {
                                endTime = DateUtils.getAddDate(date.getDate()) + " 00:00:00";
                            }
                            List<String> minuteList = DateUtils.getMinuteList(startTime, endTime, 15);
                            for (int i = 0; i < minuteList.size() - 1; i++) {
                                String minute = minuteList.get(i + 1);
                                DataResp dataResp = new DataResp();
                                dataResp.setTime(minute);
                                dataResp.setValue(detail.getPower());
                                dataRespList.add(dataResp);
                            }
                        });
                        String deliveryChart = JSONArray.toJSONString(dataRespList);
                        aggregatorDeviceDateDeliveryChart.setDeliveryChart(deliveryChart);
                        aggregatorDeviceDateDeliveryChartList.add(aggregatorDeviceDateDeliveryChart);
                    }
                });
            }
            //组合企业申报数据
            AggregatorEntDateApplyDetail aggregatorEntDateApplyDetail = new AggregatorEntDateApplyDetail();
            aggregatorEntDateApplyDetail.setAggregatorId(aggregatorId);
            aggregatorEntDateApplyDetail.setDate(date.getDate());
            aggregatorEntDateApplyDetail.setEntId(req.getEntId());
            aggregatorEntDateApplyDetail.setStationId(req.getStationId());
            aggregatorEntDateApplyDetail.setApplyTime(now);
            aggregatorEntDateApplyDetail.setApplyDate(DateUtils.format(now, "yyyy-MM-dd"));
            aggregatorEntDateApplyDetailList.add(aggregatorEntDateApplyDetail);
        });
        //保存设备申报曲线
        if (aggregatorDeviceDateDeliveryChartList.size() > 0) {
            //删除上次设备申报
            aggregatorDeviceDateDeliveryChartService.delete(req.getEntId(), dateList);
            aggregatorDeviceDateDeliveryChartService.batchInsert(aggregatorDeviceDateDeliveryChartList);
            //保存聚合商申报曲线
            saveAggregatorDateDeliveryChart(aggregatorId, aggregatorDeviceDateDeliveryChartList);
        }
        //保存企业申请
        if (aggregatorEntDateApplyDetailList.size() > 0) {
            //删除企业申报详情
            aggregatorEntDateApplyDetailService.delete(req.getEntId(), dateList);
            aggregatorEntDateApplyDetailService.batchInsert(aggregatorEntDateApplyDetailList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAggregatorDateDeliveryChart(String aggregatorId, List<AggregatorDeviceDateDeliveryChart> aggregatorDeviceDateDeliveryChartList) {
        List<String> dateList = aggregatorDeviceDateDeliveryChartList.stream().map(AggregatorDeviceDateDeliveryChart::getDate).distinct().collect(toList());
        List<AggregatorDateDeliveryChartResp> respList = Lists.newArrayList();
        //处理设备上报数据
        getAggregatorDeviceDateDeliveryChartRespList(aggregatorDeviceDateDeliveryChartList, respList);
        //取锁
        RedisLock redisLock = new RedisLock(redisTemplate, Constants.AGGREGATOR_DELIVERY_CHART_LOCK + aggregatorId, System.currentTimeMillis() + 2000);
        try {
            if (!redisLock.lockSync()) {
                throw new BaseException(StatusCode.ERROR.getCode(), "保存聚合商总功率曲线取锁失败");
            }
            List<AggregatorDateDeliveryChart> aggregatorDateDeliveryChartList = aggregatorDateDeliveryChartService.getAggregatorDateDeliveryChartList(aggregatorId, dateList);
            if (null != aggregatorDateDeliveryChartList && aggregatorDateDeliveryChartList.size() > 0) {
                Map<String, Map<String, List<AggregatorDateDeliveryChart>>> resourceDateAggMap = aggregatorDateDeliveryChartList.stream().collect(groupingBy(AggregatorDateDeliveryChart::getResourceTypeId, groupingBy(AggregatorDateDeliveryChart::getDate)));
                resourceDateAggMap.entrySet().forEach(resourceDateAggMapEntry -> {
                    AggregatorDateDeliveryChartResp resp = new AggregatorDateDeliveryChartResp();
                    resp.setResourceTypeId(resourceDateAggMapEntry.getKey());
                    Map<String, List<AggregatorDateDeliveryChart>> dateMap = resourceDateAggMapEntry.getValue();
                    dateMap.entrySet().forEach(dateMapEntry -> {
                        resp.setDate(dateMapEntry.getKey());
                        List<AggregatorDateDeliveryChart> chartList = dateMapEntry.getValue();
                        if (null != chartList && chartList.size() > 0) {
                            List<DataResp> dataRespList = Lists.newArrayList();
                            chartList.forEach(chart -> {
                                String deliveryChart = chart.getDeliveryChart();
                                if (StringUtils.isNotEmpty(deliveryChart)) {
                                    List<DataResp> deliveryChartList = JSONArray.parseArray(deliveryChart, DataResp.class);
                                    if (null != deliveryChartList && deliveryChartList.size() > 0) {
                                        dataRespList.addAll(deliveryChartList);
                                    }
                                }
                            });
                            resp.setDataRespList(dataRespList);
                            respList.add(resp);
                        }
                    });
                });
                //删除上次聚合商汇总
                aggregatorDateDeliveryChartService.delete(aggregatorId, dateList);
            }
            aggregatorDateDeliveryChartList = getAggregatorDateDeliveryChartList(aggregatorId, respList);
            aggregatorDateDeliveryChartService.batchInsert(aggregatorDateDeliveryChartList);
            //有设备申报修改缓存状态
            redis.set(Constants.aggregatorDelivery + aggregatorId, "1");
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(StatusCode.ERROR.getCode(), "保存聚合商总功率曲线失败");
        } finally {
            redisLock.unlock();
        }
    }

    /**
     * 处理设备上报数据
     *
     * @param aggregatorDeviceDateDeliveryChartList
     * @param respList
     * @return
     */
    private List<AggregatorDateDeliveryChartResp> getAggregatorDeviceDateDeliveryChartRespList(List<AggregatorDeviceDateDeliveryChart> aggregatorDeviceDateDeliveryChartList, List<AggregatorDateDeliveryChartResp> respList) {
        Map<String, Map<String, List<AggregatorDeviceDateDeliveryChart>>> resourceDateMap = aggregatorDeviceDateDeliveryChartList.stream().collect(groupingBy(AggregatorDeviceDateDeliveryChart::getResourceTypeId, groupingBy(AggregatorDeviceDateDeliveryChart::getDate)));
        resourceDateMap.entrySet().forEach(resourceDateMapEntry -> {
            AggregatorDateDeliveryChartResp resp = new AggregatorDateDeliveryChartResp();
            resp.setResourceTypeId(resourceDateMapEntry.getKey());
            Map<String, List<AggregatorDeviceDateDeliveryChart>> dateMap = resourceDateMapEntry.getValue();
            dateMap.entrySet().forEach(dateMapEntry -> {
                resp.setDate(dateMapEntry.getKey());
                List<AggregatorDeviceDateDeliveryChart> chartList = dateMapEntry.getValue();
                if (null != chartList && chartList.size() > 0) {
                    List<DataResp> dataRespList = Lists.newArrayList();
                    chartList.forEach(chart -> {
                        String deliveryChart = chart.getDeliveryChart();
                        if (StringUtils.isNotEmpty(deliveryChart)) {
                            List<DataResp> deliveryChartList = JSONArray.parseArray(deliveryChart, DataResp.class);
                            if (null != deliveryChartList && deliveryChartList.size() > 0) {
                                dataRespList.addAll(deliveryChartList);
                            }
                        }
                    });
                    resp.setDataRespList(dataRespList);
                    respList.add(resp);
                }
            });
        });
        return respList;
    }

    /**
     * 处理聚合商上报曲线
     *
     * @param aggregatorId
     * @param aggregatorDateDeliveryChartRespList
     * @return
     */
    private List<AggregatorDateDeliveryChart> getAggregatorDateDeliveryChartList(String aggregatorId, List<AggregatorDateDeliveryChartResp> aggregatorDateDeliveryChartRespList) {
        List<AggregatorDateDeliveryChart> chartList = Lists.newArrayList();
        if (null != aggregatorDateDeliveryChartRespList && aggregatorDateDeliveryChartRespList.size() > 0) {
            Map<String, Map<String, List<AggregatorDateDeliveryChartResp>>> resourceDateMap = aggregatorDateDeliveryChartRespList.stream().collect(groupingBy(AggregatorDateDeliveryChartResp::getResourceTypeId, groupingBy(AggregatorDateDeliveryChartResp::getDate)));
            resourceDateMap.entrySet().forEach(resourceDateMapEntry -> {
                Map<String, List<AggregatorDateDeliveryChartResp>> dateMap = resourceDateMapEntry.getValue();
                dateMap.entrySet().forEach(dateMapEntry -> {
                    List<AggregatorDateDeliveryChartResp> respList = dateMapEntry.getValue();
                    List<DataResp> dataRespList = Lists.newArrayList();
                    respList.forEach(resp -> {
                        dataRespList.addAll(resp.getDataRespList());
                    });
                    Map<String, Double> timeValueMap = dataRespList.stream().collect(toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1 + v2));
                    List<DataResp> resultList = Lists.newArrayList();
                    timeValueMap.entrySet().forEach(timeValueMapEntry -> {
                        DataResp dataResp = new DataResp();
                        dataResp.setTime(timeValueMapEntry.getKey());
                        dataResp.setValue(null == timeValueMapEntry.getValue() ? null : MathUtils.doublePoint(timeValueMapEntry.getValue(), 2));
                        resultList.add(dataResp);
                    });
                    List<DataResp> sortList = resultList.stream().sorted(Comparator.comparing(DataResp::getTime)).collect(toList());
                    String deliveryChartStr = JSONObject.toJSONString(sortList);
                    AggregatorDateDeliveryChart chart = new AggregatorDateDeliveryChart();
                    chart.setAggregatorId(aggregatorId);
                    chart.setResourceTypeId(resourceDateMapEntry.getKey());
                    chart.setDate(dateMapEntry.getKey());
                    chart.setDeliveryChart(deliveryChartStr);
                    chartList.add(chart);
                });
            });
        }
        return chartList;
    }
}
