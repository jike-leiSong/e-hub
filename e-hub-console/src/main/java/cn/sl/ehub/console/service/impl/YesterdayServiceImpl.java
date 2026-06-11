package cn.sl.ehub.console.service.impl;

import cn.enn.bigdata.resp.BigDataHistoryResp;
import cn.enn.bigdata.service.IBigDataHandlerService;
import cn.sl.ehub.common.enums.*;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.service.req.AggregatorEntDateInviteDetailReq;
import cn.sl.ehub.service.resp.*;
import cn.sl.ehub.console.service.*;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.service.vo.*;
import cn.sl.ehub.common.vo.DataResp;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * 曲线图ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class YesterdayServiceImpl implements IYesterdayService {

    private final IAggregatorDateProfitService aggregatorDateProfitService;
    private final IAggregatorDateIssueChartService aggregatorDateIssueChartService;
    private final IAggregatorCrChartService aggregatorCrChartService;
    private final IAggregatorDapChartService aggregatorDapChartService;
    private final IAggregatorEntDeviceService aggregatorEntDeviceService;
    private final IBigDataHandlerService bigDataHistoryService;
    private final IAggregatorDateDeliveryChartService aggregatorDateDeliveryChartService;
    private final IAggregatorDeviceDateBaseLineLoadChartService aggregatorDeviceDateBaseLineLoadChartService;
    private final IAggregatorDeviceDateIssueChartService aggregatorDeviceDateIssueChartService;
    private final IAggregatorEntService aggregatorEntService;
    private final IAggregatorEntDateApplyDetailService aggregatorEntDateApplyDetailService;
    private final IAggregatorEntDateProfitService aggregatorEntDateProfitService;
    private final IAggregatorEntDateInviteDetailService aggregatorEntDateInviteDetailService;
    private final IAggregatorDateHolidayService aggregatorDateHolidayService;
    private final IAggregatorDevicePointService aggregatorDevicePointService;
    private final IAggregatorEntBaseLineLoadChartService aggregatorEntBaseLineLoadChartService;
    @Resource(name = "threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor executor;

    @Override
    public IndexOverviewResp getLastProfit(String aggregatorId) {
        return getProfit(aggregatorId, null, new IndexOverviewResp());
    }

    @Override
    public IndexOverviewResp getOverview(String simulate, String aggregatorId, String resourceTypeId, String dayType) {
        IndexOverviewResp resp = new IndexOverviewResp();
        String dateStr = DateUtils.getDay();
        String startTime, endTime;
        List<String> minuteList;

        if (dayType.equals(DayTypeEnum.YESTERDAY.getCode())) {
            dateStr = DateUtils.getAddDate(dateStr, -1);
            startTime = dateStr + " 00:00:00";
            endTime = dateStr + " 23:59:59";
            //查询调度下发总收益
            getProfit(aggregatorId, dateStr, resp);
            minuteList = DateUtils.getMinuteList(startTime, endTime);
            //查询调度下发功率曲线
            getIssueChart(aggregatorId, resourceTypeId, dateStr, minuteList, resp);
            //查询实时功率曲线
            getPowerChart(simulate, resourceTypeId, aggregatorId, startTime, endTime, minuteList, resp);
            // 查询电网下发dap曲线
            getDapChart(aggregatorId, resourceTypeId, dateStr, minuteList, resp);

            //查询曲线颜色
            resp.setTimeColorRespList(getColorList(resp.getIssueChart(), resp.getPowerChart()));

            //生成时间轴
            List<String> timeList = new ArrayList<>();
            LocalDateTime start =LocalDateTimeUtil.parse(startTime, DatePattern.NORM_DATETIME_PATTERN);
            LocalDateTime end = LocalDateTimeUtil.parse(endTime, DatePattern.NORM_DATETIME_PATTERN);
            LocalDateTime current = start; // 初始化当前时间为起始时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 设置日期时间格式
            while (current.isBefore(end)) { // 循环直到当前时间超过结束时间
                String minuteTime = current.format(formatter); // 将当前时间转换为指定格式的日期时间字符串
                timeList.add(minuteTime);
                current = current.plusMinutes(1); // 更新当前时间为下一个时间间隔
            }
            resp.setTimeList(timeList);

        } else if (dayType.equals(DayTypeEnum.TODAY.getCode())) {
            startTime = dateStr + " 00:00:00";
            endTime = DateUtils.getAddDate(dateStr) + " 00:00:00";
            minuteList = DateUtils.getMinuteList(startTime, endTime);
            CountDownLatch countDownLatch = new CountDownLatch(4);
            //查询调度下发功率曲线
            String finalDateStr = dateStr;
            executor.execute(() -> {
                try {
                    getIssueChart(aggregatorId, resourceTypeId, finalDateStr, minuteList, resp);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }
            });
            //查询Cr曲线
            executor.execute(() -> {
                try {
                    getCrChart(aggregatorId, resourceTypeId, finalDateStr, minuteList, resp);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }
            });
            //查询实时功率曲线
            executor.execute(() -> {
                try {
                    getPowerChart(simulate, aggregatorId, resourceTypeId, startTime, endTime, minuteList.subList(1, minuteList.size()), resp);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }
            });
            // 查询dap曲线
            executor.execute(() -> {
                try {
                    getDapChart(aggregatorId, resourceTypeId, finalDateStr, minuteList, resp);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }
            });

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new BaseException(StatusCode.ERROR.getCode(), "功率请求失败！");
            }
            //查询曲线颜色
            resp.setTimeColorRespList(getColorList(resp.getIssueChart(), resp.getPowerChart()));
            //生成时间轴
            List<String> timeList = new ArrayList<>();

            LocalDateTime start =LocalDateTimeUtil.parse(startTime, DatePattern.NORM_DATETIME_PATTERN);
            LocalDateTime end = LocalDateTimeUtil.parse(endTime, DatePattern.NORM_DATETIME_PATTERN);
            LocalDateTime current = start; // 初始化当前时间为起始时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm"); // 设置日期时间格式
            while (current.isBefore(end)) { // 循环直到当前时间超过结束时间
                String minuteTime = current.format(formatter); // 将当前时间转换为指定格式的日期时间字符串
                timeList.add(minuteTime);
                current = current.plusMinutes(1); // 更新当前时间为下一个时间间隔
            }
            resp.setTimeList(timeList);
        } else if (dayType.equals(DayTypeEnum.TOMORROW.getCode())) {
            List<String> dateList = aggregatorDateHolidayService.getApplyDateList(dateStr, false);
            startTime = dateList.get(0) + " 00:15:00";
            endTime = DateUtils.getAddDate(dateList.get(dateList.size() - 1)) + " 00:00:00";
            minuteList = DateUtils.getMinuteList(startTime, endTime, 15);
            //查询调度下发功率曲线
            getIssueChart(aggregatorId, resourceTypeId, dateList, minuteList, resp);
            //查询用户申报功率曲线
            getDeliveryChart(aggregatorId, resourceTypeId, dateList, minuteList, resp);
            // 电网下发dap曲线
            getDapChart(aggregatorId, resourceTypeId, dateList, minuteList, resp);

            //生成时间轴
            List<String> timeList = new ArrayList<>();
            LocalDateTime start =LocalDateTimeUtil.parse(startTime, DatePattern.NORM_DATETIME_PATTERN);
            LocalDateTime end = LocalDateTimeUtil.parse(endTime, DatePattern.NORM_DATETIME_PATTERN);
            LocalDateTime current = start; // 初始化当前时间为起始时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 设置日期时间格式
            while (current.isBefore(end)) { // 循环直到当前时间超过结束时间
                String minuteTime = current.format(formatter); // 将当前时间转换为指定格式的日期时间字符串
                timeList.add(minuteTime);
                current = current.plusMinutes(1); // 更新当前时间为下一个时间间隔
            }
            resp.setTimeList(timeList);

        } else {
            return resp;
        }
        return resp;
    }

    /**
     * 查询曲线颜色
     *
     * @param issueChartList
     * @param powerChartList
     * @return
     */
    private List<List<IndexOverviewTimeColorResp>> getColorList(List<DataResp> issueChartList, List<DataResp> powerChartList) {
        List<List<IndexOverviewTimeColorResp>> timeColorRespList = Lists.newArrayList();
        if (null != issueChartList && issueChartList.size() > 0 && null != powerChartList && powerChartList.size() > 0) {
            for (int i = 0; i < powerChartList.size(); i++) {
                DataResp dataRespOne = issueChartList.get(i);
                Double issueDoubleMul = MathUtils.mulDoubleNull(dataRespOne.getValue(), 0.7, 2);
                Double powerTotal = null;
                for (int j = 0; j < 15; j++) {
                    powerTotal = MathUtils.addDouble(powerTotal, powerChartList.get(i + j).getValue(), 2);
                }
                i += 14;
                Double powerTotalAvg = null;
                if (null != powerTotal) {
                    powerTotalAvg = MathUtils.doublePoint(powerTotal / 15, 2);
                }
                if (null != powerTotalAvg && null != issueDoubleMul && powerTotalAvg < issueDoubleMul) {
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
        return timeColorRespList;
    }

    @Override
    public List<AggregatorEntDevice> getDeviceList(String aggregatorId, String entId, String stationId, String resourceTypeId) {
        return aggregatorEntDeviceService.getAggregatorEntDeviceList(aggregatorId, entId, stationId, resourceTypeId);
    }

    @Override
    public EntUserDeviceYesterdayChartResp getEntUserDeviceChartResp(String simulate, String deviceBaseId, List<AggregatorEntDevice> deviceList, String date) {
        EntUserDeviceYesterdayChartResp resp = new EntUserDeviceYesterdayChartResp();
        Date now = new Date();
        if (StringUtils.isEmpty(date)) {
            date = DateUtils.getDay(now, -1);
        }
        String startTime = date + " 00:01:00";
        String endTime = DateUtils.getAddDate(date) + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime);
        //基线负荷
        getBaseLineChart(deviceBaseId, minuteList, resp, date);
        //设备实际功率
        getDevicePowerChart(simulate, deviceBaseId, deviceList, startTime, endTime, minuteList, resp);
        //分解后设备功率
        getDeviceIssueChart(deviceBaseId, date, minuteList, resp);
        //查询曲线颜色
        resp.setTimeColorRespList(getColorList(resp.getIssueChart(), resp.getPowerChart()));
        return resp;
    }

    /**
     * 获取多设备累计功率、下发功率、基线曲线
     * @param simulate
     * @param deviceList
     * @param date
     * @return
     */
    @Override
    public EntUserDeviceYesterdayChartResp getEntUserDeviceListChartResp(String simulate, List<AggregatorEntDevice> deviceList, String date) {
        EntUserDeviceYesterdayChartResp resp = new EntUserDeviceYesterdayChartResp();
        Date now = new Date();
        if (StringUtils.isEmpty(date)) {
            date = DateUtils.getDay(now, -1);
        }
        String startTime = date + " 00:01:00";
        String endTime = DateUtils.getAddDate(date) + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime);

        List<String> deviceBaseIdList = deviceList.stream().map(e -> e.getDeviceBaseId()).collect(Collectors.toList());
        //基线负荷
        getDevicesBaseLineChart(deviceBaseIdList, minuteList, resp, date);
        //设备实际功率
        getDeviceListPowerChart(simulate, deviceList, startTime, endTime, minuteList, resp);
        //分解后设备功率
        getDevicesIssuePowerChart(deviceBaseIdList, date, minuteList, resp);
        //查询曲线颜色
        resp.setTimeColorRespList(getColorList(resp.getIssueChart(), resp.getPowerChart()));
        return resp;
    }

    @Override
    public EntUserDeviceYesterdayChartResp getEntUserChartResp(String simulate, List<AggregatorEntDevice> deviceList, String date, String stationId) {
        EntUserDeviceYesterdayChartResp resp = new EntUserDeviceYesterdayChartResp();
        Date now = new Date();
        if (StringUtils.isEmpty(date)) {
            date = DateUtils.getDay(now, -1);
        }
        String startTime = date + " 00:01:00";
        String endTime = DateUtils.getAddDate(date) + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime);

        List<String> deviceBaseIdList = deviceList.stream().map(e -> e.getDeviceBaseId()).collect(Collectors.toList());
        //基线负荷
        //getDevicesBaseLineChart(deviceBaseIdList, minuteList, resp, date);
        try {
            getUserBaseLineChart(stationId,resp,date);
        } catch (Exception e) {
            log.info("用户基线未读取");
        }
        //设备实际功率
        getDeviceListPowerChart(simulate, deviceList, startTime, endTime, minuteList, resp);
        //分解后设备功率
        getDevicesIssuePowerChart(deviceBaseIdList, date, minuteList, resp);
        //查询曲线颜色
        resp.setTimeColorRespList(getColorList(resp.getIssueChart(), resp.getPowerChart()));
        return resp;
    }

    @Override
    public EntUserDeviceYesterdayChartResp getPowerDetail(String simulate, String deviceBaseId, String date) {
        EntUserDeviceYesterdayChartResp resp = new EntUserDeviceYesterdayChartResp();
        //上一个中标申报日
        AggregatorDeviceDateIssueChart aggregatorDeviceDateIssueChart = aggregatorDeviceDateIssueChartService.getAggregatorDeviceDateIssueChart(deviceBaseId);
        if (null != aggregatorDeviceDateIssueChart) {
            String startTime = aggregatorDeviceDateIssueChart.getDate() + " 00:01:00";
            String endTime = DateUtils.getAddDate(aggregatorDeviceDateIssueChart.getDate()) + " 00:00:00";
            List<String> minuteList = DateUtils.getMinuteList(startTime, endTime);
            //基线负荷
            getBaseLineChart(deviceBaseId, minuteList, resp, date);
            //设备实际功率
            getDevicePowerChart(simulate, deviceBaseId, null, startTime, endTime, minuteList, resp);
        }
        return resp;
    }

    @Override
    public List<EntUserOverviewResp> getEntUserOverviewResp(String aggregatorId, String dayType) {
        List<EntUserOverviewResp> respList = Lists.newArrayList();
        //查询企业用户
        List<AggregatorEnt> entList = aggregatorEntService.getAggregatorEntList(aggregatorId);
        if (null != entList && entList.size() > 0) {
            List<String> entIdList = entList.stream().map(AggregatorEnt::getEntId).collect(Collectors.toList());
            Date date = new Date();
            String dateStr = "";
            Map<String, Double> entIdProfitMap = new HashMap<>();
            Map<String, AggregatorEntDateInviteDetail> entIdInviteMap = new HashMap<>();
            if (dayType.equals(DayTypeEnum.YESTERDAY.getCode())) {
                dateStr = DateUtils.getDay(date, -1);
                //查询企业用户收益
                entIdProfitMap = getEntIdProfitMap(entIdList, dateStr);
            } else if (dayType.equals(DayTypeEnum.TODAY.getCode())) {
                dateStr = DateUtils.getDay();
            } else if (dayType.equals(DayTypeEnum.TOMORROW.getCode())) {
                dateStr = DateUtils.getNextDay();
                //查询企业用户邀约
                entIdInviteMap = getInviteMap(entIdList, dateStr);
            } else {
                return respList;
            }
            //查询企业用户申报状态
            Map<String, AggregatorEntDateApplyDetail> entIdApplyDetailMap = getEntIdApplyDetailMap(entIdList, dateStr);
            Map<String, Double> finalEntIdProfitMap = entIdProfitMap;
            Map<String, AggregatorEntDateInviteDetail> finalEntIdInviteMap = entIdInviteMap;
            entList.forEach(ent -> {
                EntUserOverviewResp resp = new EntUserOverviewResp();
                resp.setAggregatorId(ent.getAggregatorId());
                resp.setEntId(ent.getEntId());
                resp.setStationId(ent.getStationId());
                resp.setEntName(ent.getEntName());
                resp.setApplyStatus(ApplyStatusEnum.APPLY_NO.getCode());
                AggregatorEntDateInviteDetail inviteDetail = finalEntIdInviteMap.get(ent.getEntId());
                if (dayType.equals(DayTypeEnum.TOMORROW.getCode())) {
                    if (null == inviteDetail) {
//                        resp.setApplyStatus(ApplyStatusEnum.INVITE.getCode());
                    } else {
                        resp.setInviteBy(inviteDetail.getInviteBy());
                        resp.setInviteTime(StringUtils.isEmpty(inviteDetail.getInviteTime()) ? inviteDetail.getInviteTime() : DateUtils.format(inviteDetail.getInviteTime(), "HH:mm:ss"));
                    }
                }
                AggregatorEntDateApplyDetail applyDetail = entIdApplyDetailMap.get(ent.getEntId());
                if (null != applyDetail && applyDetail.getApplyStatus().equals("1")) {
                    resp.setApplyStatus(ApplyStatusEnum.APPLY_YES.getCode());
                    resp.setApplyTime(applyDetail.getApplyTime());
                }
                resp.setTotalProfit(finalEntIdProfitMap.get(ent.getEntId()));
                respList.add(resp);
            });
        }
        if (null != respList && respList.size() > 0) {
            Collections.sort(respList);
        }
        return respList;
    }

    @Override
    public String entInvite(AggregatorEntDateInviteDetailReq req) {
        AggregatorEntDateInviteDetail detail = new AggregatorEntDateInviteDetail();
        detail.setAggregatorId(req.getAggregatorId());
        detail.setEntId(req.getEntId());
        detail.setStationId(req.getStationId());
        detail.setDate(DateUtils.getNextDay());
        detail.setInviteBy(req.getInviteBy());
        detail.setInviteTime(DateUtils.getTime());
        aggregatorEntDateInviteDetailService.insert(detail);
        return "邀约成功";
    }

    /**
     * 查询企业用户邀约
     *
     * @param entIdList
     * @param date
     * @return
     */
    private Map<String, AggregatorEntDateInviteDetail> getInviteMap(List<String> entIdList, String date) {
        Map<String, AggregatorEntDateInviteDetail> entIdInviteDetailMap = new HashMap<>();
        List<AggregatorEntDateInviteDetail> detailList = aggregatorEntDateInviteDetailService.getAggregatorEntDateInviteDetailList(entIdList, date);
        if (null != detailList && detailList.size() > 0) {
            entIdInviteDetailMap = detailList.stream().collect(Collectors.toMap(AggregatorEntDateInviteDetail::getEntId, Function.identity(), (v1, v2) -> v1));
            if (null == entIdInviteDetailMap || entIdInviteDetailMap.size() <= 0) {
                entIdInviteDetailMap = new HashMap<>();
            }
        }
        return entIdInviteDetailMap;
    }

    /**
     * 查询企业用户收益
     *
     * @param entIdList
     * @param date
     * @return
     */
    private Map<String, Double> getEntIdProfitMap(List<String> entIdList, String date) {
        Map<String, Double> getEntIdProfitMap = new HashMap<>();
        List<AggregatorEntDateProfit> profitList = aggregatorEntDateProfitService.getAggregatorEntDateProfitList(entIdList, date);
        if (null != profitList && profitList.size() > 0) {
            getEntIdProfitMap = profitList.stream().collect(Collectors.toMap(AggregatorEntDateProfit::getEntId, AggregatorEntDateProfit::getEntProfit, (v1, v2) -> v1));
            if (null == getEntIdProfitMap || getEntIdProfitMap.size() <= 0) {
                getEntIdProfitMap = new HashMap<>();
            }
        }
        return getEntIdProfitMap;
    }

    /**
     * 查询企业用户申报状态
     *
     * @param entIdList
     * @return
     */
    private Map<String, AggregatorEntDateApplyDetail> getEntIdApplyDetailMap(List<String> entIdList, String date) {
        Map<String, AggregatorEntDateApplyDetail> entIdApplyDetailMap = new HashMap<>();
        List<AggregatorEntDateApplyDetail> aggregatorEntDateApplyDetailList = aggregatorEntDateApplyDetailService.getAggregatorEntDateApplyDetailList(entIdList, date);
        if (null != aggregatorEntDateApplyDetailList && aggregatorEntDateApplyDetailList.size() > 0) {
            entIdApplyDetailMap = aggregatorEntDateApplyDetailList.stream().collect(Collectors.toMap(AggregatorEntDateApplyDetail::getEntId, Function.identity(), (v1, v2) -> v1));
            if (null == entIdApplyDetailMap || entIdApplyDetailMap.size() <= 0) {
                entIdApplyDetailMap = new HashMap<>();
            }
        }
        return entIdApplyDetailMap;
    }

    /**
     * 基线负荷
     *
     * @param deviceBaseId
     * @param resp
     * @return
     */
    private EntUserDeviceYesterdayChartResp getBaseLineChart(String deviceBaseId, List<String> minuteList, EntUserDeviceYesterdayChartResp resp, String date) {
        List<DataResp> baseLineChartList = Lists.newArrayList();
        Map<String, Double> timeValueMap = new HashMap<>();
        AggregatorDeviceDateBaseLineLoadChart aggregatorDeviceDateBaseLineLoadChart = aggregatorDeviceDateBaseLineLoadChartService.getAggregatorDeviceDateBaseLineLoadChart(deviceBaseId, date);
        if (null != aggregatorDeviceDateBaseLineLoadChart && StringUtils.isNotEmpty(aggregatorDeviceDateBaseLineLoadChart.getBaseLineLoadChart())) {
            String baseLineLoadChart = aggregatorDeviceDateBaseLineLoadChart.getBaseLineLoadChart();
            List<DataResp> dataRespList = JSONArray.parseArray(baseLineLoadChart, DataResp.class);
            if (null != dataRespList && dataRespList.size() > 0) {
                Map<String, Double> dataRespMap = dataRespList.stream().collect(Collectors.toMap(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm"), DataResp::getValue, (v1, v2) -> v1));
                timeValueMap.putAll(dataRespMap);
            }
        }
        for (int i = minuteList.size() - 1; i > 0; i -= 15) {
            for (int j = i; j >= i - 14; j--) {
                DataResp dataResp = new DataResp();
                dataResp.setTime(DateUtils.format(minuteList.get(j), "HH:mm"));
                dataResp.setValue(timeValueMap.get(DateUtils.format(minuteList.get(i), "HH:mm")));
                baseLineChartList.add(0, dataResp);
            }
        }
        resp.setBaseLineChart(baseLineChartList);
        return resp;
    }

    /**
     * 获取用户基线
     * @param stationId
     * @param resp
     * @param date
     * @return
     */
    private EntUserDeviceYesterdayChartResp getUserBaseLineChart(String stationId, EntUserDeviceYesterdayChartResp resp, String date){
        List<AggregatorEntBaseLineLoadChart> baseLineList = aggregatorEntBaseLineLoadChartService.getEntBaseLineBySystemCode(stationId,date);
        if(CollectionUtil.isNotEmpty(baseLineList)){
            List<DataResp> baseLineDataList = baseLineList.stream().flatMap(a -> {
                String baseLineLoadChart = a.getBaseLineLoadChart();
                List<DataResp> list = JSONObject.parseArray(baseLineLoadChart, DataResp.class);
                return list.stream();
            }).collect(toList());
            if(CollectionUtils.isEmpty(baseLineDataList)){
                resp.setBaseLineChart(baseLineDataList);
                return resp;
            }
            List<DataResp> result= new ArrayList<>();
            List<String> minuteList = DateUtils.getMinuteList(date + " 00:01:00", DateUtils.getAddDate(date, 1) + " 00:00:00", 1);
            List<String> minuteListFit = DateUtils.getMinuteList(date + " 00:15:00", DateUtils.getAddDate(date, 1) + " 00:00:00", 15);
            Map<String, Double> minuteListFitMap = baseLineDataList.stream().filter(e->!Objects.isNull(e.getValue())).collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1));
            for (int i = 0; i < minuteListFit.size(); i++) {
                String minuteFitNew = minuteListFit.get(i);
                Double aDouble = minuteListFitMap.get(minuteFitNew);
                for (int i1 = 0; i1 < 15; i1++) {
                    String minuteNew = minuteList.get(i*15+i1);
                    result.add(new DataResp(minuteNew, aDouble));
                }
            }
            resp.setBaseLineChart(result);
        }
        return resp;
    }
    /**
     * 多设备累加基线负荷
     *
     * @param deviceBaseIdList
     * @param resp
     * @return
     */
    private EntUserDeviceYesterdayChartResp getDevicesBaseLineChart(List<String> deviceBaseIdList, List<String> minuteList, EntUserDeviceYesterdayChartResp resp, String date) {
        List<DataResp> baseLineChartList = Lists.newArrayList();
        List<Map<String, Double>> timeValueMapList= new ArrayList<>();
        List<AggregatorDeviceDateBaseLineLoadChart> aggregatorDeviceDateBaseLineLoadCharts = aggregatorDeviceDateBaseLineLoadChartService.getAggregatorDeviceDateBaseLineLoadChartList(deviceBaseIdList, date);
        for (AggregatorDeviceDateBaseLineLoadChart aggregatorDeviceDateBaseLineLoadChart : aggregatorDeviceDateBaseLineLoadCharts) {
            Map<String, Double> timeValueMap = new HashMap<>();
            if (null != aggregatorDeviceDateBaseLineLoadChart && StringUtils.isNotEmpty(aggregatorDeviceDateBaseLineLoadChart.getBaseLineLoadChart())) {
                String baseLineLoadChart = aggregatorDeviceDateBaseLineLoadChart.getBaseLineLoadChart();
                List<DataResp> dataRespList = JSONArray.parseArray(baseLineLoadChart, DataResp.class);
                if (null != dataRespList && dataRespList.size() > 0) {
                    Map<String, Double> dataRespMap = dataRespList.stream().collect(Collectors.toMap(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm"), DataResp::getValue, (v1, v2) -> v1));
                    timeValueMap.putAll(dataRespMap);
                }
            }
            timeValueMapList.add(timeValueMap);
        }

        for (int i = minuteList.size() - 1; i > 0; i -= 15) {
            for (int j = i; j >= i - 14; j--) {
                DataResp dataResp = new DataResp();
                dataResp.setTime(DateUtils.format(minuteList.get(j), "HH:mm"));
                Double sum = 0.0;
                for (Map<String, Double> stringDoubleMap : timeValueMapList) {
                    Double aDouble = stringDoubleMap.get(DateUtils.format(minuteList.get(i), "HH:mm"));
                    if(Objects.isNull(aDouble)){
                        continue;
                    }
                    sum=sum+aDouble;
                    dataResp.setValue(sum);
                }
                baseLineChartList.add(0, dataResp);
            }
        }

        resp.setBaseLineChart(baseLineChartList);
        return resp;
    }
    /**
     * 查询实时功率曲线
     *
     * @param simulate
     * @param deviceBaseId
     * @param deviceList
     * @param startTime
     * @param endTime
     * @param minuteList
     * @param resp
     * @return
     */
    private EntUserDeviceYesterdayChartResp getDevicePowerChart(String simulate, String deviceBaseId, List<AggregatorEntDevice> deviceList, String startTime, String endTime, List<String> minuteList, EntUserDeviceYesterdayChartResp resp) {
        List<DataResp> powerChartList = Lists.newArrayList();
        if (null == deviceList || deviceList.size() <= 0) {
            deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(Arrays.asList(deviceBaseId));
        }
        List<BigDataHistoryResp> bigDataHistoryRespList = bigDataHistoryService.getBigData(deviceList, Arrays.asList(MetricEnum.YES_POWER.getCode()), startTime, endTime, simulate);
        if (null != bigDataHistoryRespList && bigDataHistoryRespList.size() > 0) {
            BigDataHistoryResp history = bigDataHistoryRespList.get(0);
            if (null != history) {
                List<DataResp> dataRespList = history.getDataResp();
                Map<String, Double> timeValueMap = new HashMap<>();
                if (null != dataRespList && dataRespList.size() > 0) {
                    timeValueMap = dataRespList.stream().filter(dataResp -> null != dataResp && StringUtils.isNotEmpty(dataResp.getTime()))
                            .collect(Collectors.toMap(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm"), dataResp -> null == dataResp.getValue() ? null : MathUtils.doublePoint(dataResp.getValue(), 2)));
                    if (null == timeValueMap || timeValueMap.size() <= 0) {
                        timeValueMap = new HashMap<>();
                    }
                    AggregatorEntDevice aggregatorEntDevice = deviceList.get(0);
                    if (null == aggregatorEntDevice) {
                        aggregatorEntDevice = new AggregatorEntDevice();
                    }
                    if (null != minuteList && minuteList.size() > 0) {
                        Map<String, Double> finalTimeValueMap = timeValueMap;
                        AggregatorEntDevice finalAggregatorEntDevice = aggregatorEntDevice;
                        minuteList.forEach(minute -> {
                            DataResp dataResp = new DataResp();
                            dataResp.setTime(DateUtils.format(minute, "HH:mm"));
                            dataResp.setValue(finalTimeValueMap.get(dataResp.getTime()));
                            if (StringUtils.isNotEmpty(finalAggregatorEntDevice.getResourceTypeId()) && finalAggregatorEntDevice.getResourceTypeId().equals("27")) {
                                dataResp.setValue(null == dataResp.getValue() ? null : 0 - dataResp.getValue());
                            }
                            powerChartList.add(dataResp);
                        });
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(powerChartList) && CollectionUtils.isNotEmpty(minuteList)) {
            minuteList.forEach(minute -> {
                DataResp dataResp = new DataResp();
                dataResp.setTime(DateUtils.format(minute, "HH:mm"));
                powerChartList.add(dataResp);
            });
        }
        resp.setPowerChart(powerChartList);
        return resp;
    }
    /**
     * 查询多设备实时功率曲线
     *
     * @param simulate
     * @param deviceList
     * @param startTime
     * @param endTime
     * @param minuteList
     * @param resp
     * @return
     */
    private EntUserDeviceYesterdayChartResp getDeviceListPowerChart(String simulate, List<AggregatorEntDevice> deviceList, String startTime, String endTime, List<String> minuteList, EntUserDeviceYesterdayChartResp resp) {
        List<DataResp> powerChartList = Lists.newArrayList();
        List<Map<String, Double>> timeValueMapList= new ArrayList<>();
        List<BigDataHistoryResp> bigDataHistoryRespList = bigDataHistoryService.getBigData(deviceList, Arrays.asList(MetricEnum.YES_POWER.getCode()), startTime, endTime, simulate);
        if (null != bigDataHistoryRespList && bigDataHistoryRespList.size() > 0) {
//            BigDataHistoryResp history = bigDataHistoryRespList.get(0);
            for (BigDataHistoryResp history : bigDataHistoryRespList) {
                if (null != history) {
                    List<DataResp> dataRespList = history.getDataResp();
                    Map<String, Double> timeValueMap = new HashMap<>();
                    if (null != dataRespList && dataRespList.size() > 0) {
                        timeValueMap = dataRespList.stream().filter(dataResp -> null != dataResp && StringUtils.isNotEmpty(dataResp.getTime()))
                                .collect(Collectors.toMap(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm"), dataResp -> null == dataResp.getValue() ? null : MathUtils.doublePoint(dataResp.getValue(), 2)));
                        if (null == timeValueMap || timeValueMap.size() <= 0) {
                            timeValueMap = new HashMap<>();
                        }

                        timeValueMapList.add(timeValueMap);
                    }
                }
            }
            if (null != minuteList && minuteList.size() > 0) {
                AggregatorEntDevice aggregatorEntDevice = deviceList.get(0);
                if (null == aggregatorEntDevice) {
                    aggregatorEntDevice = new AggregatorEntDevice();
                }
                AggregatorEntDevice finalAggregatorEntDevice = aggregatorEntDevice;
                minuteList.forEach(minute -> {
                    DataResp dataResp = new DataResp();
                    dataResp.setTime(DateUtils.format(minute, "HH:mm"));
                    Double sum = 0.00;
                    log.info("minute"+minute);
                    for (Map<String, Double> stringDoubleMap : timeValueMapList) {
                        Double aDouble = stringDoubleMap.get(DateUtils.format(minute, "HH:mm"));
                        if(Objects.isNull(aDouble)){
                            continue;
                        }
                        if (StringUtils.isNotEmpty(finalAggregatorEntDevice.getResourceTypeId()) && StrUtil.equals(EnergyModelEnum.getByCode(finalAggregatorEntDevice.getResourceTypeId()).getName(),"储能")) {
                            aDouble = 0-aDouble;
                        }
                        sum=sum+aDouble;
                        String resultTemp = new BigDecimal(String.valueOf(sum)).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                        dataResp.setValue(Double.valueOf(resultTemp));
                    }
                    powerChartList.add(dataResp);
                });
            }
        }

        if (CollectionUtils.isEmpty(powerChartList) && CollectionUtils.isNotEmpty(minuteList)) {
            minuteList.forEach(minute -> {
                DataResp dataResp = new DataResp();
                dataResp.setTime(DateUtils.format(minute, "HH:mm"));
                powerChartList.add(dataResp);
            });
        }
        resp.setPowerChart(powerChartList);
        return resp;
    }

    /**
     * 查询调度下发功率曲线
     *
     * @param deviceBaseId
     * @param date
     * @param minuteList
     * @param resp
     * @return
     */
    private EntUserDeviceYesterdayChartResp getDeviceIssueChart(String deviceBaseId, String date, List<String> minuteList, EntUserDeviceYesterdayChartResp resp) {
        List<DataResp> issueChartList = Lists.newArrayList();
        Map<String, Double> timeValueMap = new HashMap<>();
        AggregatorDeviceDateIssueChart aggregatorDeviceDateIssueChart = aggregatorDeviceDateIssueChartService.getAggregatorDeviceDateIssueChart(deviceBaseId, date);

        if (null != aggregatorDeviceDateIssueChart && StringUtils.isNotEmpty(aggregatorDeviceDateIssueChart.getIssueChart())) {
            String issueChart = aggregatorDeviceDateIssueChart.getIssueChart();
            List<DataResp> dataRespList = JSONArray.parseArray(issueChart, DataResp.class);
            if (null != dataRespList && dataRespList.size() > 0) {
                Map<String, Double> dataRespMap = dataRespList.stream().collect(Collectors.toMap(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm"), DataResp::getValue, (v1, v2) -> v1));
                timeValueMap.putAll(dataRespMap);
            }
        }
        for (int i = minuteList.size() - 1; i > 0; i -= 15) {
            for (int j = i; j >= i - 14; j--) {
                DataResp dataResp = new DataResp();
                dataResp.setTime(DateUtils.format(minuteList.get(j), "HH:mm"));
                dataResp.setValue(timeValueMap.get(DateUtils.format(minuteList.get(i), "HH:mm")));
                issueChartList.add(0, dataResp);
            }
        }
        resp.setIssueChart(issueChartList);
        return resp;
    }

    /**
     * 多设备累加下发功率
     *
     * @param deviceBaseIdList
     * @param resp
     * @return
     */
    private EntUserDeviceYesterdayChartResp getDevicesIssuePowerChart(List<String> deviceBaseIdList, String date,List<String> minuteList, EntUserDeviceYesterdayChartResp resp) {
        List<DataResp> issueChartList = Lists.newArrayList();
        List<Map<String, Double>> timeValueMapList= new ArrayList<>();
        List<String> dateList = Arrays.asList(date);
        List<AggregatorDeviceDateIssueChart> aggregatorDeviceDateIssueChartList = aggregatorDeviceDateIssueChartService.getAggregatorDeviceDateIssueChartList(deviceBaseIdList, dateList);
        for (AggregatorDeviceDateIssueChart aggregatorDeviceDateIssueChart : aggregatorDeviceDateIssueChartList) {
            Map<String, Double> timeValueMap = new HashMap<>();
            if (null != aggregatorDeviceDateIssueChart && StringUtils.isNotEmpty(aggregatorDeviceDateIssueChart.getIssueChart())) {
                String issueChart = aggregatorDeviceDateIssueChart.getIssueChart();
                List<DataResp> dataRespList = JSONArray.parseArray(issueChart, DataResp.class);
                if (null != dataRespList && dataRespList.size() > 0) {
                    Map<String, Double> dataRespMap = dataRespList.stream().collect(Collectors.toMap(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm"), DataResp::getValue, (v1, v2) -> v1));
                    timeValueMap.putAll(dataRespMap);
                }
            }
            timeValueMapList.add(timeValueMap);
        }
        for (int i = minuteList.size() - 1; i > 0; i -= 15) {
            for (int j = i; j >= i - 14; j--) {
                DataResp dataResp = new DataResp();
                dataResp.setTime(DateUtils.format(minuteList.get(j), "HH:mm"));
                Double sum = 0.0;
                for (Map<String, Double> stringDoubleMap : timeValueMapList) {
                    Double aDouble = stringDoubleMap.get(DateUtils.format(minuteList.get(i), "HH:mm"));
                    if(Objects.isNull(aDouble)){
                        continue;
                    }
                    sum=sum+aDouble;
                    dataResp.setValue(sum);
                }
                issueChartList.add(0, dataResp);
            }
        }
        resp.setIssueChart(issueChartList);
        return resp;
    }

    /**
     * 查询调度下发总收益
     *
     * @param aggregatorId
     * @param date
     * @param resp
     * @return
     */
    private IndexOverviewResp getProfit(String aggregatorId, String date, IndexOverviewResp resp) {
        AggregatorDateProfit aggregatorDateProfit = aggregatorDateProfitService.getAggregatorDateProfit(aggregatorId, date);
        if (null != aggregatorDateProfit) {
            resp.setTotalProfit(null == aggregatorDateProfit.getIssueProfit() ? 0 : MathUtils.doublePointNotRounding(aggregatorDateProfit.getIssueProfit(), 2));
            resp.setTotalProfitTime(DateUtils.format(aggregatorDateProfit.getDate() + " 00:00:00", "MM-dd"));
        }
        return resp;
    }

    /**
     * 查询调度下发功率曲线
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param date
     * @param minuteList
     * @param resp
     * @return
     */
    private IndexOverviewResp getIssueChart(String aggregatorId, String resourceTypeId, String date, List<String> minuteList, IndexOverviewResp resp) {
        List<DataResp> issueChartList = Lists.newArrayList();
        Map<String, Double> dataRespMap = new HashMap<>();
        AggregatorDateIssueChart aggregatorDateIssueChart = aggregatorDateIssueChartService.getAggregatorDateIssueChart(aggregatorId, resourceTypeId, date);
        if (null != aggregatorDateIssueChart && StringUtils.isNotEmpty(aggregatorDateIssueChart.getIssueChart())) {
            String issueChart = aggregatorDateIssueChart.getIssueChart();
            List<DataResp> dataRespList = JSONArray.parseArray(issueChart, DataResp.class);
            if (null != dataRespList && dataRespList.size() > 0) {
                dataRespMap = dataRespList.stream().collect(Collectors.toMap(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm"), DataResp::getValue, (v1, v2) -> v1));
            }
        }
        if (null == dataRespMap || dataRespMap.size() <= 0) {
            dataRespMap = new HashMap<>();
        }
        for (int i = minuteList.size() - 1; i > 0; i -= 15) {
            for (int j = i; j >= i - 14; j--) {
                DataResp dataResp = new DataResp();
                dataResp.setTime(DateUtils.format(minuteList.get(j), "HH:mm"));
                dataResp.setValue(dataRespMap.get(DateUtils.format(minuteList.get(i), "HH:mm")));
                issueChartList.add(0, dataResp);
            }
        }
        resp.setIssueChart(issueChartList);
        return resp;
    }

    /**
     * 查询碳因子CR曲线
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param date
     * @param minuteList
     * @param resp
     * @return
     */
    private IndexOverviewResp getCrChart(String aggregatorId, String resourceTypeId, String date, List<String> minuteList, IndexOverviewResp resp) {
        List<DataResp> crChartList = Lists.newArrayList();
        Map<String, Double> dataRespMap = new HashMap<>();
        AggregatorCrChart aggregatorCrChart = aggregatorCrChartService.getAggregatorDateCrChart(aggregatorId, resourceTypeId, date);
        if (null != aggregatorCrChart && StringUtils.isNotEmpty(aggregatorCrChart.getCrLoadChart())) {
            String issueChart = aggregatorCrChart.getCrLoadChart();
            List<DataResp> dataRespList = JSONArray.parseArray(issueChart, DataResp.class);
            if (null != dataRespList && dataRespList.size() > 0) {
                dataRespMap = dataRespList.stream().collect(Collectors.toMap(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm"), DataResp::getValue, (v1, v2) -> v1));
            }
        }
        if (null == dataRespMap || dataRespMap.size() <= 0) {
            dataRespMap = new HashMap<>();
        }
        for (int i = minuteList.size() - 1; i > 0; i -= 15) {
            for (int j = i; j >= i - 14; j--) {
                DataResp dataResp = new DataResp();
                dataResp.setTime(DateUtils.format(minuteList.get(j), "HH:mm"));
                dataResp.setValue(dataRespMap.get(DateUtils.format(minuteList.get(i), "HH:mm")));
                crChartList.add(0, dataResp);
            }
        }
        resp.setCrChart(crChartList);
        return resp;
    }

    /**
     * 查询电网下发的DAP数据
     * @param aggregatorId
     * @param resourceTypeId
     * @param date
     * @param minuteList
     * @param resp
     * @return
     */
    // todo
    private IndexOverviewResp getDapChart(String aggregatorId, String resourceTypeId, String date, List<String> minuteList, IndexOverviewResp resp) {
        List<DataResp> dapChartList = Lists.newArrayList();
        Map<String, Double> dataRespMap = new HashMap<>();
        AggregatorDapChart aggregatorDateDapChart = aggregatorDapChartService.getAggregatorDateDapChart(aggregatorId, resourceTypeId, date);
        if (null != aggregatorDateDapChart && StringUtils.isNotEmpty(aggregatorDateDapChart.getDapChart())) {
            String issueChart = aggregatorDateDapChart.getDapChart();
            List<DataResp> dataRespList = JSONArray.parseArray(issueChart, DataResp.class);
            if (null != dataRespList && dataRespList.size() > 0) {
                dataRespMap = dataRespList.stream().collect(Collectors.toMap(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm"), DataResp::getValue, (v1, v2) -> v1));
            }
        }
        if (null == dataRespMap || dataRespMap.size() <= 0) {
            dataRespMap = new HashMap<>();
        }
        for (int i = minuteList.size() - 1; i > 0; i -= 15) {
            for (int j = i; j >= i - 14; j--) {
                DataResp dataResp = new DataResp();
                dataResp.setTime(DateUtils.format(minuteList.get(j), "HH:mm"));
                dataResp.setValue(dataRespMap.get(DateUtils.format(minuteList.get(i), "HH:mm")));
                dapChartList.add(0, dataResp);
            }
        }
        resp.setDapChart(dapChartList);
        return resp;
    }

    private IndexOverviewResp getDapChart(String aggregatorId, String resourceTypeId, List<String> dateList, List<String> minuteList, IndexOverviewResp resp) {
        List<DataResp> dapChartList = Lists.newArrayList();
        Map<String, Double> dataRespMap = new HashMap<>();
        List<AggregatorDapChart> aggregatorDapChartList = aggregatorDapChartService.getAggregatorDateDapChart(aggregatorId, resourceTypeId, dateList);
        if (null != aggregatorDapChartList && aggregatorDapChartList.size() > 0) {
            List<DataResp> totalDataRespList = Lists.newArrayList();
            aggregatorDapChartList.stream().filter(chart -> null != chart && StringUtils.isNotEmpty(chart.getDapChart())).forEach(aggregatorDateIssueChart -> {
                String dapChart = aggregatorDateIssueChart.getDapChart();
                List<DataResp> dataRespList = JSONArray.parseArray(dapChart, DataResp.class);
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
        Map<String, Double> finalDataRespMap = dataRespMap;
        minuteList.forEach(minute -> {
            DataResp dataResp = new DataResp();
            dataResp.setTime(minute);
            dataResp.setValue(finalDataRespMap.get(minute));
            dapChartList.add(dataResp);
        });
        resp.setDapChart(dapChartList);
        return resp;
    }


    /**
     * 查询调度下发功率曲线
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param dateList
     * @param minuteList
     * @param resp
     * @return
     */
    private IndexOverviewResp getIssueChart(String aggregatorId, String resourceTypeId, List<String> dateList, List<String> minuteList, IndexOverviewResp resp) {
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
        Map<String, Double> finalDataRespMap = dataRespMap;
        minuteList.forEach(minute -> {
            DataResp dataResp = new DataResp();
            dataResp.setTime(minute);
            dataResp.setValue(finalDataRespMap.get(minute));
            issueChartList.add(dataResp);
        });
        resp.setIssueChart(issueChartList);
        return resp;
    }

    /**
     * 查询实时功率曲线
     *
     * @param simulate
     * @param aggregatorId
     * @param resourceTypeId
     * @param startTime
     * @param endTime
     * @param minuteList
     * @param resp
     * @return
     */
    private IndexOverviewResp getPowerChart(String simulate, String aggregatorId, String resourceTypeId, String startTime, String endTime, List<String> minuteList, IndexOverviewResp resp) {
        List<DataResp> powerList = Lists.newArrayList();
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(aggregatorId, null, null, resourceTypeId);
        List<DataResp> dataRespList = Lists.newArrayList();
        List<BigDataHistoryResp> bigDataHistoryRespList = bigDataHistoryService.getBigData(deviceList, Arrays.asList(MetricEnum.YES_POWER.getCode()), startTime, endTime, simulate);
        if (CollectionUtils.isNotEmpty(bigDataHistoryRespList)) {
            bigDataHistoryRespList.stream().filter(history -> null != history && null != history.getDataResp() && history.getDataResp().size() > 0).forEach(history -> {
                dataRespList.addAll(history.getDataResp());
            });
        }
        if (null != dataRespList && dataRespList.size() > 0) {
            Map<String, Double> valueMap = dataRespList.stream().collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1 + v2));
            if (null == valueMap || valueMap.size() <= 0) {
                valueMap = new HashMap<>();
            }
            if (null != minuteList && minuteList.size() > 0) {
                Map<String, Double> finalValueMap = valueMap;
                minuteList.forEach(minute -> {
                    DataResp dataResp = new DataResp();
                    dataResp.setTime(DateUtils.format(minute, "HH:mm"));
                    dataResp.setValue(null == finalValueMap.get(minute) ? null : MathUtils.doublePoint(finalValueMap.get(minute), 2));
                    if (StringUtils.isNotEmpty(resourceTypeId) && resourceTypeId.equals("27")) {
                        dataResp.setValue(null == dataResp.getValue() ? null : 0 - dataResp.getValue());
                    }
                    powerList.add(dataResp);
                });
            }
        }
        resp.setPowerChart(powerList);
        return resp;
    }

    /**
     * 查询用户申报功率曲线
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param date
     * @param minuteList
     * @param resp
     * @return
     */
    private IndexOverviewResp getDeliveryChart(String aggregatorId, String resourceTypeId, String date, List<String> minuteList, IndexOverviewResp resp) {
        List<DataResp> deliveryChartList = Lists.newArrayList();
        List<AggregatorDateDeliveryChart> aggregatorDateDeliveryChartList = aggregatorDateDeliveryChartService.getAggregatorDateDeliveryChartList(aggregatorId, resourceTypeId, date);
        if (null != aggregatorDateDeliveryChartList && aggregatorDateDeliveryChartList.size() > 0) {
            AggregatorDateDeliveryChart aggregatorDateDeliveryChart = aggregatorDateDeliveryChartList.get(0);
            if (null != aggregatorDateDeliveryChart && StringUtils.isNotEmpty(aggregatorDateDeliveryChart.getDeliveryChart())) {
                String deliveryChart = aggregatorDateDeliveryChart.getDeliveryChart();
                List<DataResp> dataRespList = JSONArray.parseArray(deliveryChart, DataResp.class);
                if (null != dataRespList && dataRespList.size() > 0) {
                    Map<String, Double> timeValueMap = dataRespList.stream()
                            .filter(dataResp -> null != dataResp && StringUtils.isNotEmpty(dataResp.getTime()))
                            .collect(Collectors.toMap(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm"), DataResp::getValue));
                    if (null != minuteList && minuteList.size() > 0) {
                        minuteList.forEach(minute -> {
                            DataResp dataResp = new DataResp();
                            dataResp.setTime(DateUtils.format(minute, "HH:mm"));
                            dataResp.setValue(null == timeValueMap.get(dataResp.getTime()) ? null : MathUtils.doublePoint(timeValueMap.get(dataResp.getTime()), 2));
                            deliveryChartList.add(dataResp);
                        });
                    }
                }
            }
        }
        resp.setDeliveryChart(deliveryChartList);
        return resp;
    }

    /**
     * 查询用户申报功率曲线
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param dateList
     * @param minuteList
     * @param resp
     * @return
     */
    private IndexOverviewResp getDeliveryChart(String aggregatorId, String resourceTypeId, List<String> dateList, List<String> minuteList, IndexOverviewResp resp) {
        List<DataResp> deliveryChartList = Lists.newArrayList();
        Map<String, Double> timeValueMap = new HashMap<>();
        List<AggregatorDateDeliveryChart> aggregatorDateDeliveryChartList = aggregatorDateDeliveryChartService.getAggregatorDateDeliveryChartList(aggregatorId, resourceTypeId, dateList);
        if (null != aggregatorDateDeliveryChartList && aggregatorDateDeliveryChartList.size() > 0) {
            aggregatorDateDeliveryChartList.forEach(aggregatorDateDeliveryChart -> {
                List<DataResp> resultList = Lists.newArrayList();
                if (null != aggregatorDateDeliveryChart && StringUtils.isNotEmpty(aggregatorDateDeliveryChart.getDeliveryChart())) {
                    String deliveryChart = aggregatorDateDeliveryChart.getDeliveryChart();
                    List<DataResp> dataRespList = JSONArray.parseArray(deliveryChart, DataResp.class);
                    if (null != dataRespList && dataRespList.size() > 0) {
                        resultList.addAll(dataRespList);
                    }
                }
                if (null != resultList && resultList.size() > 0) {
                    timeValueMap.putAll(resultList.stream().filter(dataResp -> null != dataResp && StringUtils.isNotEmpty(dataResp.getTime())).collect(Collectors.toMap(DataResp::getTime, DataResp::getValue)));
                }
            });
        }
        minuteList.forEach(minute -> {
            DataResp dataResp = new DataResp();
            dataResp.setTime(minute);
            dataResp.setValue(null == timeValueMap || null == timeValueMap.get(dataResp.getTime()) ? null : MathUtils.doublePoint(timeValueMap.get(dataResp.getTime()), 2));
            deliveryChartList.add(dataResp);
        });
        resp.setDeliveryChart(deliveryChartList);
        return resp;
    }
}
