package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.console.enums.ApplyStatusEnum;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.service.mapper.AggregatorDateDeliveryChartMapper;
import cn.sl.ehub.service.mapper.AggregatorDeviceDateDeliveryChartMapper;
import cn.sl.ehub.console.model.resp.ChartDataResp;
import cn.sl.ehub.service.req.*;
import cn.sl.ehub.service.resp.*;
import cn.sl.ehub.console.service.*;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.service.vo.*;
import cn.enn.uac.resp.UacAdminUserInfoResp;
import cn.enn.uac.service.UacAdminService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.Column;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

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
public class TomorrowServiceImpl implements ITomorrowService {

    private final IAggregatorDateHolidayService aggregatorDateHolidayService;
    private final IAggregatorResourceTypeService aggregatorResourceTypeService;
    private final IAggregatorEntService aggregatorEntService;
    private final IAggregatorDeviceDateDeliveryChartService aggregatorDeviceDateDeliveryChartService;
    private final IAggregatorDeviceDateIssueChartService aggregatorDeviceDateIssueChartService;
    private final IAggregatorDateApplyDetailService aggregatorDateApplyDetailService;
    private final IAggregatorEntDateApplyDetailService aggregatorEntDateApplyDetailService;
    private final IAggregatorResourceDateDeliveryOfferService aggregatorResourceDateDeliveryOfferService;
    private final IAggregatorDateDeliveryChartService aggregatorDateDeliveryChartService;
    private final UacAdminService uacAdminService;
    private final IAggregatorInfoService aggregatorInfoService;
    private final LoadAggregatorDeliveryService loadAggregatorDeliveryService;
    private final IAggregatorApplyPlanService aggregatorApplyPlanService;
    @Resource(name = "threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor executor;
    @Autowired
    private AggregatorDeviceDateDeliveryChartMapper aggregatorDeviceDateDeliveryChartMapper;
    @Autowired
    private AggregatorDateDeliveryChartMapper aggregatorDateDeliveryChartMapper;

    @Override
    public EntUserDeviceTomorrowChartResp getEntUserDeviceTomorrowChartResp(String deviceBaseId) {
        EntUserDeviceTomorrowChartResp resp = new EntUserDeviceTomorrowChartResp();
        String dateStr = DateUtils.getNextDay();
        String startTime = dateStr + " 00:00:00";
        String endTime = dateStr + " 23:59:59";
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime, 15);
        //用户申报功率
        getDeviceDeliveryChart(deviceBaseId, dateStr, minuteList, resp);
        //分解后设备功率
        getDeviceIssueChart(deviceBaseId, dateStr, minuteList, resp);
        return resp;
    }

    @Override
    public AggregatorApplyResp getAggregatorApply(String aggregatorId, String date) {
        //查询聚合商
        AggregatorInfo aggregatorInfo = aggregatorInfoService.getAggregatorInfo(aggregatorId);
        if (null == aggregatorInfo) {
            throw new BaseException(StatusCode.ERROR.getCode(), "未查询到聚合商信息");
        }
        AggregatorApplyResp resp = new AggregatorApplyResp();
        //查询用户申报情况
        if (StringUtils.isEmpty(date)) {
            date = DateUtils.getNextDay();
        } else {
            date = DateUtils.getAddDate(date, 1);
        }
//        getEntNum(aggregatorId, date, resp);
        // modify by sl 2024-04-09 计划参与用户
        getPlanApplyEntNum(aggregatorId, date, resp);
        // modify by sl 2024-04-28 申报资源类型
        getApplyResourceType(aggregatorId, date, resp);
        //查询报价
        resp.setApplyPriceStatus("0");
        int count = aggregatorResourceDateDeliveryOfferService.getCount(aggregatorId, date, "1");
        if (count > 0) {
            resp.setApplyPriceStatus("1");
        }
        //计划日
        List<String> applyDateList = aggregatorDateHolidayService.getApplyDateList(DateUtils.getAddDate(date, -1), false);
        String planDateCheck = applyDateList.get(0);
        String planDate = applyDateList.get(0);
        if (applyDateList.size() != 1) {
            planDate = DateUtils.getDay(applyDateList.get(0)) + "至" + DateUtils.getDay(applyDateList.get(applyDateList.size() - 1));
        }
        resp.setPlanDate(planDate);

        String time = DateUtils.format(DateUtils.getTime(), "HH:mm:ss");
        //查询聚合商申报
        if (planDateCheck.equals(date)) {
            AggregatorDateApplyDetail aggregatorDateApplyDetail = aggregatorDateApplyDetailService.getAggregatorDateApplyDetail(aggregatorId, date);
            if (null == aggregatorDateApplyDetail || aggregatorDateApplyDetail.getApplyStatus().equals("0")) {
                resp.setApplyStatus(ApplyStatusEnum.APPLY_NO.getCode());
            } else {
                resp.setApplyStatus(aggregatorDateApplyDetail.getApplyStatus());
                resp.setWinStatus(aggregatorDateApplyDetail.getWinStatus());
                resp.setApplyType(aggregatorDateApplyDetail.getApplyType());
                resp.setApplyTime(DateUtils.format(aggregatorDateApplyDetail.getApplyTime(), "yyyy-MM-dd HH:mm"));
                resp.setApplyBy(aggregatorDateApplyDetail.getApplyBy());
            }
            if (resp.getApplyStatus().equals(ApplyStatusEnum.APPLY_NO.getCode())) {
                if (time.compareTo(aggregatorInfo.getApplyStartTime()) < 0) {
                    resp.setApplyStatus(ApplyStatusEnum.NO_ALLOW_APPLY.getCode());
                    resp.setApplyContext("将于" + DateUtils.format(date + " " + aggregatorInfo.getApplyStartTime(), "HH:mm") + "开始，请耐心等待");
                } else if (time.compareTo(aggregatorInfo.getApplyEndTime()) < 0) {
                    resp.setApplyContext("将于" + DateUtils.format(date + " " + aggregatorInfo.getApplyEndTime(), "HH:mm") + "结束，请尽快申报 ");
                } else {
                    resp.setApplyStatus(ApplyStatusEnum.APPLY_YES.getCode());
                    resp.setApplyContext("今日未申报");
                }
            } else {
                if (StringUtils.isEmpty(aggregatorDateApplyDetail.getWinStatus())) {
                    resp.setApplyContext(resp.getApplyBy() + "已于" + resp.getApplyTime() + "完成申报");
                } else if (aggregatorDateApplyDetail.getWinStatus().equals("1")) {
                    resp.setApplyContext("系统已为您自动下发功率曲线");
                } else {
                    resp.setApplyContext(applyDateList.get(applyDateList.size() - 1) + " " + DateUtils.format(date + " " + aggregatorInfo.getApplyStartTime(), "HH:mm") + "开始新一轮申报");
                }
            }
        } else {
            AggregatorDateApplyDetail aggregatorDateApplyDetail = aggregatorDateApplyDetailService.getAggregatorDateApplyDetail(aggregatorId, date);
            if (null == aggregatorDateApplyDetail) {
                resp.setApplyStatus(ApplyStatusEnum.APPLY_YES.getCode());
                resp.setApplyContext(applyDateList.get(applyDateList.size() - 1) + " " + DateUtils.format(date + " " + aggregatorInfo.getApplyStartTime(), "HH:mm") + "开始新一轮申报");
            } else {
                resp.setApplyStatus(aggregatorDateApplyDetail.getApplyStatus());
                resp.setWinStatus(aggregatorDateApplyDetail.getWinStatus());
                resp.setApplyType(aggregatorDateApplyDetail.getApplyType());
                resp.setApplyTime(DateUtils.format(aggregatorDateApplyDetail.getApplyTime(), "yyyy-MM-dd HH:mm"));
                resp.setApplyBy(aggregatorDateApplyDetail.getApplyBy());
                if (StringUtils.isEmpty(aggregatorDateApplyDetail.getWinStatus())) {
                    resp.setApplyContext(resp.getApplyBy() + "已于" + resp.getApplyTime() + "完成申报");
                } else if (aggregatorDateApplyDetail.getWinStatus().equals("1")) {
                    resp.setApplyContext("系统已为您自动下发功率曲线");
                } else {
                    resp.setApplyContext(applyDateList.get(applyDateList.size() - 1) + " " + DateUtils.format(date + " " + aggregatorInfo.getApplyStartTime(), "HH:mm") + "开始新一轮申报");
                }
            }
        }
        return resp;
    }

    private void getPlanApplyEntNum(String aggregatorId, String date, AggregatorApplyResp resp) {
        List<String> ents = aggregatorDateDeliveryChartMapper.getPlanApplyEntNum(aggregatorId, date);
        resp.setApplyYesNum(CollectionUtils.isNotEmpty(ents) ? ents.size() : 0);
    }

    /**
     *
     * <申报资源类型><功能具体实现>
     *
     * @create：2024/4/28 14:32
     * @author sl
     * @param aggregatorId
     * @param date
     * @param resp
     * @return void
     */
    private void getApplyResourceType(String aggregatorId, String date, AggregatorApplyResp resp) {
        List<String> resourceTypes = aggregatorDeviceDateDeliveryChartMapper.getApplyResourceType(aggregatorId, date);
        if (CollectionUtils.isNotEmpty(resourceTypes)) {
            resp.setApplyResourceType(String.join("、", resourceTypes));
        } else {
            resp.setApplyResourceType("无");
        }
    }

    @Override
    public Boolean updateAggregatorApply(AggregatorApplyReq req) {
        String day = DateUtils.getDay();
        String now = DateUtils.getTime();
        Boolean applyDateCheck = aggregatorDateHolidayService.getApplyDateCheck(DateUtils.getAddDate(day, 0));
        if (applyDateCheck) {
            log.info("非工作日跳过自动申报");
            return true;
        }
        List<String> dateList = aggregatorDateHolidayService.getApplyDateList(day, false);
        AggregatorDateApplyDetail aggregatorDateApplyDetail = aggregatorDateApplyDetailService.getAggregatorDateApplyDetail(req.getAggregatorId(), dateList.get(0));
        if (null != aggregatorDateApplyDetail && aggregatorDateApplyDetail.getApplyStatus().equals(ApplyStatusEnum.APPLY_YES.getCode())) {
            throw new BaseException(StatusCode.SUCCESS.getCode(), "已手动申报");
        }
        //查询报价
        if (StringUtils.isNotEmpty(req.getApplyType()) && req.getApplyType().equals("1")) {
            int offerCount = aggregatorResourceDateDeliveryOfferService.getCount(req.getAggregatorId(), dateList);
            if (offerCount <= 0) {
                throw new BaseException(StatusCode.ERROR.getCode(), "请提交报价后再申报");
            }
        } else {
            List<AggregatorResourceDateDeliveryOffer> aggregatorResourceDateDeliveryOfferListQuery = aggregatorResourceDateDeliveryOfferService.getAggregatorResourceDateDeliveryOfferList(req.getAggregatorId(), dateList);
            if (CollectionUtils.isEmpty(aggregatorResourceDateDeliveryOfferListQuery)) {
               // List<AggregatorResourceType> aggregatorResourceTypeList = aggregatorResourceTypeService.getAggregatorResourceTypeList();
                List<AggregatorResourceType> aggregatorResourceTypeList = aggregatorResourceTypeService.getAggregatorDisplayResourceTypeList(req.getAggregatorId());
                List<AggregatorResourceDateDeliveryOffer> aggregatorResourceDateDeliveryOfferList = Lists.newArrayList();
                dateList.forEach(date -> {
                    aggregatorResourceTypeList.forEach(aggregatorResourceType -> {
                        AggregatorResourceDateDeliveryOffer aggregatorResourceDateDeliveryOffer = new AggregatorResourceDateDeliveryOffer();
                        aggregatorResourceDateDeliveryOffer.setAggregatorId(req.getAggregatorId());
                        aggregatorResourceDateDeliveryOffer.setResourceTypeId(aggregatorResourceType.getId());
                        aggregatorResourceDateDeliveryOffer.setDate(date);
                        aggregatorResourceDateDeliveryOffer.setOffer(0D);
                        aggregatorResourceDateDeliveryOffer.setStatus("1");
                        List<AggregatorResourceDateDeliveryOfferResp> aggregatorResourceDateDeliveryOfferRespList = Lists.newArrayList();
                        AggregatorResourceDateDeliveryOfferResp aggregatorResourceDateDeliveryOfferResp = new AggregatorResourceDateDeliveryOfferResp();
                        aggregatorResourceDateDeliveryOfferResp.setStartTime("00:00");
                        aggregatorResourceDateDeliveryOfferResp.setEndTime("24:00");
                        aggregatorResourceDateDeliveryOfferResp.setOffer(0D);
                        aggregatorResourceDateDeliveryOfferRespList.add(aggregatorResourceDateDeliveryOfferResp);
                        String priceDetail = JSONObject.toJSONString(aggregatorResourceDateDeliveryOfferRespList);
                        aggregatorResourceDateDeliveryOffer.setPriceDetail(priceDetail);
                        aggregatorResourceDateDeliveryOffer.setPriceStatus(true);
                        List<DataResp> priceChartList = Lists.newArrayList();
                        List<String> minuteList = DateUtils.getMinuteList(date + " 00:15:00", DateUtils.getAddDate(date) + " 00:00:00", 15);
                        minuteList.forEach(minute -> {
                            DataResp dataResp = new DataResp();
                            dataResp.setTime(minute);
                            dataResp.setValue(0D);
                            priceChartList.add(dataResp);
                        });
                        String priceChart = JSONObject.toJSONString(priceChartList);
                        aggregatorResourceDateDeliveryOffer.setPriceChart(priceChart);
                        aggregatorResourceDateDeliveryOfferList.add(aggregatorResourceDateDeliveryOffer);
                    });
                });
                if (CollectionUtils.isNotEmpty(aggregatorResourceDateDeliveryOfferList)) {
                    aggregatorResourceDateDeliveryOfferService.batchInsert(aggregatorResourceDateDeliveryOfferList);
                }
            }
        }
        List<AggregatorDateApplyDetail> detailList = Lists.newArrayList();
        dateList.forEach(date -> {
            AggregatorDateApplyDetail detail = new AggregatorDateApplyDetail();
            detail.setAggregatorId(req.getAggregatorId());
            detail.setDate(date);
            detail.setApplyType(req.getApplyType());
            //申报结束
            detail.setApplyStatus(ApplyStatusEnum.APPLY_YES.getCode());
            detail.setApplyTime(now);
            detail.setCreateTime(now);
            detail.setUpdateTime(now);
            //申报人
            detail.setApplyBy(req.getApplyBy());
            if (StringUtils.isNotEmpty(detail.getApplyType()) && detail.getApplyType().equals("1")) {
                ResultVO<String> userByTicketResult = uacAdminService.getUserByTicket(req.getApplyBy());
                if (null != userByTicketResult && userByTicketResult.getCode().equals(StatusCode.SUCCESS.getCode()) && StringUtils.isNotEmpty(userByTicketResult.getData())) {
                    UacAdminUserInfoResp uacAdminUserInfoResp = JSONObject.parseObject(userByTicketResult.getData(), UacAdminUserInfoResp.class);
                    if (null != uacAdminUserInfoResp && StringUtils.isNotEmpty(uacAdminUserInfoResp.getRealName())) {
                        detail.setApplyBy(uacAdminUserInfoResp.getRealName());
                    }
                }
            }
            detailList.add(detail);
        });
        aggregatorDateApplyDetailService.delete(req.getAggregatorId(), dateList);
        if (null != detailList && detailList.size() > 0) {
            aggregatorDateApplyDetailService.batchInsert(detailList);
        }
        //调用申报接口
        executor.execute(() -> {
            deliveryChart(req.getAggregatorId(), dateList);
        });
        return true;
    }

    @Override
    public Boolean autoAggregatorApply(AggregatorApplyReq req) {
        String day = DateUtils.getDay();
        // modify by sl 2024-11-21 支持时间补招
        if (StringUtils.isNotBlank(req.getDate())) {
            day = req.getDate();
        }
        String now = DateUtils.getTime();
        Boolean applyDateCheck = aggregatorDateHolidayService.getApplyDateCheck(DateUtils.getAddDate(day, 0));
        if (applyDateCheck) {
            log.info("非工作日跳过自动申报");
            return true;
        }
        String aggregatorId = req.getAggregatorId();
        List<String> dateList = aggregatorDateHolidayService.getApplyDateList(day, false);
        log.info("申报日期：{}", dateList);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss");

        List<AggregatorResourceDateDeliveryOffer> aggregatorResourceDateDeliveryOfferListQuery = aggregatorResourceDateDeliveryOfferService.getAggregatorResourceDateDeliveryOfferList(req.getAggregatorId(), dateList);
        if (CollectionUtils.isEmpty(aggregatorResourceDateDeliveryOfferListQuery)) {
            // List<AggregatorResourceType> aggregatorResourceTypeList = aggregatorResourceTypeService.getAggregatorResourceTypeList();
            List<AggregatorResourceType> aggregatorResourceTypeList = aggregatorResourceTypeService.getAggregatorDisplayResourceTypeList(req.getAggregatorId());
            List<AggregatorResourceDateDeliveryOffer> aggregatorResourceDateDeliveryOfferList = Lists.newArrayList();
            dateList.forEach(date -> {
                aggregatorResourceTypeList.forEach(aggregatorResourceType -> {

                    AggregatorApplyPlan aggregatorApplyPlan = aggregatorApplyPlanService.getPlan(aggregatorId, aggregatorResourceType.getId(), date);
                    log.info("日期：{},申报计划:{}", date, aggregatorApplyPlan);
                    if(null == aggregatorApplyPlan){
                        return;
                    }
                    String applyPrice = aggregatorApplyPlan.getApplyPrice();
                    List<ChartDataResp> chartDataRespList = JSONObject.parseArray(applyPrice, ChartDataResp.class);

                    List<DataResp> dataRespList = chartDataRespList.stream().map(a -> {
                        DataResp dataResp = new DataResp();
                        if(a.getTime().equals("24:00")){
                            dataResp.setTime(LocalDateTime.of( LocalDate.parse(date,dateTimeFormatter).plusDays(1), LocalTime.MIN).format(formatter));;
                        }else {
                            dataResp.setTime(date + " " + a.getTime()+":00");
                        }
                        dataResp.setValue(Double.valueOf(a.getValue()));
                        return dataResp;
                    }).collect(Collectors.toList());
                    AggregatorResourceDateDeliveryOffer aggregatorResourceDateDeliveryOffer = new AggregatorResourceDateDeliveryOffer();
                    aggregatorResourceDateDeliveryOffer.setAggregatorId(req.getAggregatorId());
                    aggregatorResourceDateDeliveryOffer.setResourceTypeId(aggregatorResourceType.getId());
                    aggregatorResourceDateDeliveryOffer.setDate(date);
                    aggregatorResourceDateDeliveryOffer.setOffer(0D);
                    aggregatorResourceDateDeliveryOffer.setStatus("1");
                    aggregatorResourceDateDeliveryOffer.setPriceChart(JSONObject.toJSONString(dataRespList));
                    aggregatorResourceDateDeliveryOfferList.add(aggregatorResourceDateDeliveryOffer);
                });
            });
            if (CollectionUtils.isNotEmpty(aggregatorResourceDateDeliveryOfferList)) {
                aggregatorResourceDateDeliveryOfferService.batchInsert(aggregatorResourceDateDeliveryOfferList);
            }
        }
        List<AggregatorDateApplyDetail> detailList = Lists.newArrayList();
        dateList.forEach(date -> {
            AggregatorDateApplyDetail detail = new AggregatorDateApplyDetail();
            detail.setAggregatorId(req.getAggregatorId());
            detail.setDate(date);
            detail.setApplyType(req.getApplyType());
            //申报结束
            detail.setApplyStatus(ApplyStatusEnum.APPLY_YES.getCode());
            detail.setApplyTime(now);
            detail.setCreateTime(now);
            detail.setUpdateTime(now);
            //申报人
            detail.setApplyBy(req.getApplyBy());
            if (StringUtils.isNotEmpty(detail.getApplyType()) && detail.getApplyType().equals("1")) {
                ResultVO<String> userByTicketResult = uacAdminService.getUserByTicket(req.getApplyBy());
                if (null != userByTicketResult && userByTicketResult.getCode().equals(StatusCode.SUCCESS.getCode()) && StringUtils.isNotEmpty(userByTicketResult.getData())) {
                    UacAdminUserInfoResp uacAdminUserInfoResp = JSONObject.parseObject(userByTicketResult.getData(), UacAdminUserInfoResp.class);
                    if (null != uacAdminUserInfoResp && StringUtils.isNotEmpty(uacAdminUserInfoResp.getRealName())) {
                        detail.setApplyBy(uacAdminUserInfoResp.getRealName());
                    }
                }
            }
            detailList.add(detail);
        });
        aggregatorDateApplyDetailService.delete(req.getAggregatorId(), dateList);
        if (null != detailList && detailList.size() > 0) {
            aggregatorDateApplyDetailService.batchInsert(detailList);
        }
        //调用申报接口
        executor.execute(() -> {
            deliveryChart(req.getAggregatorId(), dateList);
        });
        return true;

    }

    @Override
    public AggregatorApplyOfferResp getAggregatorApplyOfferResp(String aggregatorId, String date) {
        AggregatorApplyOfferResp resp = new AggregatorApplyOfferResp();
        if (StringUtils.isEmpty(date)) {
            date = DateUtils.getDay();
        }
        List<String> dateList = aggregatorDateHolidayService.getApplyDateList(date, false);
        List<AggregatorResourceDateDeliveryOffer> aggregatorResourceDateDeliveryOfferList = aggregatorResourceDateDeliveryOfferService.getAggregatorResourceDateDeliveryOfferList(aggregatorId, dateList);
        Map<String, Map<String, List<AggregatorResourceDateDeliveryOffer>>> resourceDateMap = new HashMap<>();
        Map<String, Boolean> resourceIdPriceStatusMap = new HashMap<>();
        resp.setStatus("0");
        if (null != aggregatorResourceDateDeliveryOfferList && aggregatorResourceDateDeliveryOfferList.size() > 0) {
            resp.setStatus(aggregatorResourceDateDeliveryOfferList.get(0).getStatus());
            resourceDateMap = aggregatorResourceDateDeliveryOfferList.stream().collect(groupingBy(AggregatorResourceDateDeliveryOffer::getResourceTypeId, groupingBy(AggregatorResourceDateDeliveryOffer::getDate)));
            resourceIdPriceStatusMap = aggregatorResourceDateDeliveryOfferList.stream().collect(toMap(AggregatorResourceDateDeliveryOffer::getResourceTypeId, AggregatorResourceDateDeliveryOffer::getPriceStatus, (v1, v2) -> v1));
        }
       // List<AggregatorResourceType> aggregatorResourceTypeList = aggregatorResourceTypeService.getAggregatorResourceTypeList();
        List<AggregatorResourceType> aggregatorResourceTypeList = aggregatorResourceTypeService.getAggregatorResourceTypeListByAggregatorId(aggregatorId);
        List<AggregatorApplyOfferResourceResp> aggregatorApplyOfferResourceRespList = Lists.newArrayList();
        for (AggregatorResourceType aggregatorResourceType : aggregatorResourceTypeList) {
            AggregatorApplyOfferResourceResp aggregatorApplyOfferResourceResp = new AggregatorApplyOfferResourceResp();
            aggregatorApplyOfferResourceResp.setResourceTypeId(aggregatorResourceType.getId());
            aggregatorApplyOfferResourceResp.setResourceTypeName(aggregatorResourceType.getName());
            Map<String, List<AggregatorResourceDateDeliveryOffer>> dateMap = resourceDateMap.get(aggregatorResourceType.getId());
            Boolean resourceStatus = false;
            if (null != resourceIdPriceStatusMap.get(aggregatorResourceType.getId())) {
                resourceStatus = resourceIdPriceStatusMap.get(aggregatorResourceType.getId());
            }
            aggregatorApplyOfferResourceResp.setStatus(resourceStatus);
            List<AggregatorApplyOfferResourceDateResp> aggregatorApplyOfferResourceDateRespList = Lists.newArrayList();
            for (String dateStr : dateList) {
                AggregatorApplyOfferResourceDateResp aggregatorApplyOfferResourceDateResp = new AggregatorApplyOfferResourceDateResp();
                aggregatorApplyOfferResourceDateResp.setDate(dateStr);
                Boolean dateStatus = true;
                if (null != dateMap && dateMap.size() > 0 && null != dateMap.get(dateStr) && dateMap.get(dateStr).size() > 0) {
                    List<AggregatorResourceDateDeliveryOffer> offerList = dateMap.get(dateStr);
                    if (null != offerList.get(0).getPriceStatus()) {
                        dateStatus = offerList.get(0).getPriceStatus();
                    }
                    AggregatorResourceDateDeliveryOffer aggregatorResourceDateDeliveryOffer = offerList.get(0);
                    if (null != aggregatorResourceDateDeliveryOffer && StringUtils.isNotEmpty(aggregatorResourceDateDeliveryOffer.getPriceDetail())) {
                        String priceDetail = aggregatorResourceDateDeliveryOffer.getPriceDetail();
                        List<AggregatorResourceDateOfferResp> aggregatorResourceDateOfferRespList = JSONArray.parseArray(priceDetail, AggregatorResourceDateOfferResp.class);
                        if (null == aggregatorResourceDateOfferRespList || aggregatorResourceDateOfferRespList.size() <= 0) {
                            AggregatorResourceDateOfferResp aggregatorResourceDateOfferResp = new AggregatorResourceDateOfferResp();
                            aggregatorResourceDateOfferResp.setAggregatorId(aggregatorId);
                            aggregatorResourceDateOfferResp.setResourceTypeId(aggregatorResourceType.getId());
                            aggregatorResourceDateOfferResp.setDate(dateStr);
                            aggregatorResourceDateOfferResp.setStartTime("00:00");
                            aggregatorResourceDateOfferResp.setEndTime("24:00");
                            aggregatorResourceDateOfferResp.setOffer(0D);
                            aggregatorResourceDateOfferRespList.add(aggregatorResourceDateOfferResp);
                        }
                        aggregatorApplyOfferResourceDateResp.setOfferList(aggregatorResourceDateOfferRespList);
                    }
                } else {
                    AggregatorResourceDateOfferResp aggregatorResourceDateOfferResp = new AggregatorResourceDateOfferResp();
                    aggregatorResourceDateOfferResp.setAggregatorId(aggregatorId);
                    aggregatorResourceDateOfferResp.setResourceTypeId(aggregatorResourceType.getId());
                    aggregatorResourceDateOfferResp.setDate(dateStr);
                    aggregatorResourceDateOfferResp.setStartTime("00:00");
                    aggregatorResourceDateOfferResp.setEndTime("24:00");
                    aggregatorResourceDateOfferResp.setOffer(0D);
                    aggregatorApplyOfferResourceDateResp.setOfferList(Arrays.asList(aggregatorResourceDateOfferResp));
                }
                aggregatorApplyOfferResourceDateResp.setStatus(dateStatus);
                aggregatorApplyOfferResourceDateRespList.add(aggregatorApplyOfferResourceDateResp);
            }
            aggregatorApplyOfferResourceResp.setDateList(aggregatorApplyOfferResourceDateRespList);
            aggregatorApplyOfferResourceRespList.add(aggregatorApplyOfferResourceResp);
        }
        resp.setResourceList(aggregatorApplyOfferResourceRespList);
        return resp;
    }

    @Override
    public Boolean saveAggregatorApplyOffer(AggregatorApplyOfferReq req, String status) {
        List<AggregatorResourceDateDeliveryOffer> saveList = Lists.newArrayList();
        if (null == req || StringUtils.isEmpty(req.getAggregatorId())) {
            throw new BaseException(StatusCode.ERROR.getCode(), "聚合商ID为空");
        }
        List<AggregatorApplyOfferResourceReq> resourceList = req.getResourceList();
        if (null == resourceList || resourceList.size() <= 0) {
            throw new BaseException(StatusCode.ERROR.getCode(), "资源类型为空");
        }
        List<String> dateList = aggregatorDateHolidayService.getApplyDateList(DateUtils.getDay(), false);
        int count = aggregatorResourceDateDeliveryOfferService.getCount(req.getAggregatorId(), dateList, "1");
        if (count > 0) {
            throw new BaseException(StatusCode.ERROR.getCode(), "已提交报价");
        }
        //清除历史数据
        aggregatorResourceDateDeliveryOfferService.delete(req.getAggregatorId(), dateList);
        resourceList.forEach(resource -> {
            List<AggregatorApplyOfferResourceDateReq> dateReqList = resource.getDateList();
            if (null != dateReqList && dateReqList.size() > 0) {
                dateReqList.forEach(date -> {
                    List<AggregatorResourceDateDeliveryOfferReq> offerList = date.getOfferList();
                    if (null != offerList && offerList.size() > 0) {
                        AggregatorResourceDateDeliveryOffer save = new AggregatorResourceDateDeliveryOffer();
                        save.setAggregatorId(req.getAggregatorId());
                        save.setResourceTypeId(resource.getResourceTypeId());
                        save.setDate(date.getDate());
                        save.setOffer(offerList.get(0).getOffer());
                        save.setStatus(status);
                        save.setPriceStatus(resource.getStatus());
                        List<AggregatorResourceDateDeliveryOfferResp> aggregatorResourceDateDeliveryOfferRespList = Lists.newArrayList();
                        offerList.forEach(offer -> {
                            AggregatorResourceDateDeliveryOfferResp aggregatorResourceDateDeliveryOfferResp = new AggregatorResourceDateDeliveryOfferResp();
                            aggregatorResourceDateDeliveryOfferResp.setStartTime(offer.getStartTime());
                            aggregatorResourceDateDeliveryOfferResp.setEndTime(offer.getEndTime());
                            aggregatorResourceDateDeliveryOfferResp.setOffer(offer.getOffer());
                            aggregatorResourceDateDeliveryOfferRespList.add(aggregatorResourceDateDeliveryOfferResp);
                        });
                        String priceDetail = JSONObject.toJSONString(aggregatorResourceDateDeliveryOfferRespList);
                        save.setPriceDetail(priceDetail);
                        Map<String, Double> deliveryMap = new HashMap<>();
                        aggregatorResourceDateDeliveryOfferRespList.forEach(aggregatorResourceDateDeliveryOfferResp -> {
                            String startTimeWithResp = date.getDate() + " " + aggregatorResourceDateDeliveryOfferResp.getStartTime() + ":00";
                            String endTimeWithResp = date.getDate() + " " + aggregatorResourceDateDeliveryOfferResp.getEndTime() + ":00";
                            if (aggregatorResourceDateDeliveryOfferResp.getEndTime().equals("24:00")) {
                                endTimeWithResp = DateUtils.getAddDate(date.getDate(), +1) + " 00:00:00";
                            }
                            List<String> minuteListWithResp = DateUtils.getMinuteList(startTimeWithResp, endTimeWithResp, 15);
                            minuteListWithResp.forEach(minute -> {
                                deliveryMap.put(minute, aggregatorResourceDateDeliveryOfferResp.getOffer());
                            });
                        });
                        List<DataResp> priceChartList = Lists.newArrayList();
                        List<String> minuteList = DateUtils.getMinuteList(date.getDate() + " 00:15:00", DateUtils.getAddDate(date.getDate(), +1) + " 00:00:00", 15);
                        minuteList.forEach(minute -> {
                            DataResp delivery = new DataResp();
                            delivery.setTime(minute);
                            delivery.setValue(deliveryMap.get(minute));
                            priceChartList.add(delivery);
                        });
                        String priceChart = JSONObject.toJSONString(priceChartList);
                        save.setPriceChart(priceChart);
                        saveList.add(save);
                    }
                });
            }
        });
        if (null != saveList && saveList.size() > 0) {
            aggregatorResourceDateDeliveryOfferService.batchInsert(saveList);
        }
        return true;
    }

    @Override
    public IndexOverviewResp getAggregatorDeliveryChart(String aggregatorId, String resourceTypeId, String date) {
        IndexOverviewResp resp = new IndexOverviewResp();
        if (StringUtils.isEmpty(date)) {
            date = DateUtils.getDay();
        }
        List<String> dateList = aggregatorDateHolidayService.getApplyDateList(date, false);
        String startTime = dateList.get(0) + " 00:15:00";
        String endTime = DateUtils.getAddDate(dateList.get(dateList.size() - 1)) + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime, 15);
        getDeliveryChart(aggregatorId, resourceTypeId, dateList, minuteList, resp);
        return resp;
    }

    @Override
    public List<AggregatorApplyOfferResourceDateResp> getPriceByResourceTypeId(String aggregatorId, String resourceTypeId, String date) {
        if (StringUtils.isEmpty(date)) {
            date = DateUtils.getDay();
        }
        List<String> dateList = aggregatorDateHolidayService.getApplyDateList(date, false);
        List<AggregatorResourceDateDeliveryOffer> aggregatorResourceDateDeliveryOfferList = aggregatorResourceDateDeliveryOfferService.getAggregatorResourceDateDeliveryOfferList(aggregatorId, resourceTypeId, dateList, "1");
        Map<String, Map<String, List<AggregatorResourceDateDeliveryOffer>>> resourceDateMap = new HashMap<>();
        if (null != aggregatorResourceDateDeliveryOfferList && aggregatorResourceDateDeliveryOfferList.size() > 0) {
            resourceDateMap = aggregatorResourceDateDeliveryOfferList.stream().collect(groupingBy(AggregatorResourceDateDeliveryOffer::getResourceTypeId, groupingBy(AggregatorResourceDateDeliveryOffer::getDate)));
        }
        if (null == resourceDateMap || resourceDateMap.size() <= 0) {
            resourceDateMap = new HashMap<>();
        }
        Map<String, List<AggregatorResourceDateDeliveryOffer>> dateMap = resourceDateMap.get(resourceTypeId);
        if (null == dateMap || dateMap.size() <= 0) {
            dateMap = new HashMap<>();
        }
        List<AggregatorApplyOfferResourceDateResp> aggregatorApplyOfferResourceDateRespList = Lists.newArrayList();
        for (String dateStr : dateList) {
            AggregatorApplyOfferResourceDateResp aggregatorApplyOfferResourceDateResp = new AggregatorApplyOfferResourceDateResp();
            aggregatorApplyOfferResourceDateResp.setDate(dateStr);
            List<AggregatorResourceDateDeliveryOffer> offerList = dateMap.get(dateStr);
            if (null != offerList && offerList.size() > 0) {
                AggregatorResourceDateDeliveryOffer aggregatorResourceDateDeliveryOffer = offerList.get(0);
                if (null != aggregatorResourceDateDeliveryOffer && aggregatorResourceDateDeliveryOffer.getPriceStatus() && StringUtils.isNotEmpty(aggregatorResourceDateDeliveryOffer.getPriceDetail())) {
                    String priceDetail = aggregatorResourceDateDeliveryOffer.getPriceDetail();
                    List<AggregatorResourceDateOfferResp> aggregatorResourceDateOfferRespList = JSONArray.parseArray(priceDetail, AggregatorResourceDateOfferResp.class);
//                    if (null == aggregatorResourceDateOfferRespList || aggregatorResourceDateOfferRespList.size() <= 0) {
//                        AggregatorResourceDateOfferResp aggregatorResourceDateOfferResp = new AggregatorResourceDateOfferResp();
//                        aggregatorResourceDateOfferResp.setAggregatorId(aggregatorId);
//                        aggregatorResourceDateOfferResp.setResourceTypeId(resourceTypeId);
//                        aggregatorResourceDateOfferResp.setDate(dateStr);
//                        aggregatorResourceDateOfferResp.setStartTime("00:00");
//                        aggregatorResourceDateOfferResp.setEndTime("24:00");
//                        aggregatorResourceDateOfferRespList.add(aggregatorResourceDateOfferResp);
//                    }
                    aggregatorApplyOfferResourceDateResp.setOfferList(aggregatorResourceDateOfferRespList);
                }
            }
            if (null == aggregatorApplyOfferResourceDateResp.getOfferList()) {
                aggregatorApplyOfferResourceDateResp.setOfferList(Lists.newArrayList());
            }
            aggregatorApplyOfferResourceDateRespList.add(aggregatorApplyOfferResourceDateResp);
        }
        return aggregatorApplyOfferResourceDateRespList;
    }

    /**
     * 查询用户申报功率曲线
     *
     * @param deviceBaseId
     * @param date
     * @param minuteList
     * @param resp
     * @return
     */
    private EntUserDeviceTomorrowChartResp getDeviceDeliveryChart(String deviceBaseId, String date, List<String> minuteList, EntUserDeviceTomorrowChartResp resp) {
        List<DataResp> deliveryChartList = Lists.newArrayList();
        AggregatorDeviceDateDeliveryChart aggregatorDeviceDateDeliveryChart = aggregatorDeviceDateDeliveryChartService.getAggregatorDeviceDateDeliveryChart(deviceBaseId, date);
        if (null != aggregatorDeviceDateDeliveryChart && StringUtils.isNotEmpty(aggregatorDeviceDateDeliveryChart.getDeliveryChart())) {
            String deliveryChart = aggregatorDeviceDateDeliveryChart.getDeliveryChart();
            List<DataResp> dataRespList = JSONArray.parseArray(deliveryChart, DataResp.class);
            if (null != dataRespList && dataRespList.size() > 0) {
                Map<String, Double> timeValueMap = dataRespList.stream().collect(Collectors.toMap(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm"), DataResp::getValue, (v1, v2) -> v1));
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
        resp.setDeliveryChart(deliveryChartList);
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
    private EntUserDeviceTomorrowChartResp getDeviceIssueChart(String deviceBaseId, String date, List<String> minuteList, EntUserDeviceTomorrowChartResp resp) {
        List<DataResp> issueChartList = Lists.newArrayList();
        AggregatorDeviceDateIssueChart aggregatorDeviceDateIssueChart = aggregatorDeviceDateIssueChartService.getAggregatorDeviceDateIssueChart(deviceBaseId, date);
        if (null != aggregatorDeviceDateIssueChart && StringUtils.isNotEmpty(aggregatorDeviceDateIssueChart.getIssueChart())) {
            String issueChart = aggregatorDeviceDateIssueChart.getIssueChart();
            List<DataResp> dataRespList = JSONArray.parseArray(issueChart, DataResp.class);
            if (null != dataRespList && dataRespList.size() > 0) {
                Map<String, Double> timeValueMap = dataRespList.stream().collect(Collectors.toMap(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm"), DataResp::getValue, (v1, v2) -> v1));
                if (null != minuteList && minuteList.size() > 0) {
                    minuteList.forEach(minute -> {
                        DataResp dataResp = new DataResp();
                        dataResp.setTime(DateUtils.format(minute, "HH:mm"));
                        dataResp.setValue(null == timeValueMap.get(dataResp.getTime()) ? null : MathUtils.doublePoint(timeValueMap.get(dataResp.getTime()), 2));
                        issueChartList.add(dataResp);
                    });
                }
            }
        }
        resp.setIssueChart(issueChartList);
        return resp;
    }

    /**
     * 查询用户申报情况
     *
     * @param aggregatorId
     * @param date
     * @param resp
     * @return
     */
    private AggregatorApplyResp getEntNum(String aggregatorId, String date, AggregatorApplyResp resp) {
        //查询用户总量
        int entNum = aggregatorEntService.getCount(aggregatorId);
        if (entNum == 0) {
            resp.setEntNum(0);
            resp.setApplyYesNum(0);
            resp.setApplyNoNum(0);
            return resp;
        }
        resp.setEntNum(entNum);
        //查询已申报企业
        resp.setApplyYesNum(aggregatorEntDateApplyDetailService.getCount(aggregatorId, date));
        //查询未申报企业
        resp.setApplyNoNum(resp.getEntNum() - resp.getApplyYesNum());
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
        List<AggregatorDateDeliveryChart> aggregatorDateDeliveryChartList = aggregatorDateDeliveryChartService.getAggregatorDateDeliveryChartList(aggregatorId, resourceTypeId, dateList);
        Map<String, Double> timeValueMap = new HashMap<>();
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
        if (null != minuteList && minuteList.size() > 0) {
            minuteList.forEach(minute -> {
                DataResp dataResp = new DataResp();
                dataResp.setTime(minute);
                dataResp.setValue(null == timeValueMap || null == timeValueMap.get(dataResp.getTime()) ? null : MathUtils.doublePoint(timeValueMap.get(dataResp.getTime()), 2));
                deliveryChartList.add(dataResp);
            });
        }
        resp.setDeliveryChart(deliveryChartList);
        return resp;
    }

    /**
     * 聚合商申报
     *
     * @param aggregatorId
     * @param dateList
     */
    private void deliveryChart(String aggregatorId, List<String> dateList) {
        log.info("聚合商申报功开始");
        try {
            List<AggregatorDateDeliveryChart> aggregatorDateDeliveryChartList = aggregatorDateDeliveryChartService.getAggregatorDateDeliveryChartList(aggregatorId, dateList);
            log.info("聚合商申报曲线:{}", JSON.toJSONString(aggregatorDateDeliveryChartList));
            if (CollectionUtils.isEmpty(aggregatorDateDeliveryChartList)) {
                return;
            }
            List<AggregatorResourceDateDeliveryOffer> aggregatorResourceDateDeliveryOfferList = aggregatorResourceDateDeliveryOfferService.getAggregatorResourceDateDeliveryOfferList(aggregatorId, dateList);
            log.info("聚合商资源类型日期申报价格:{}", JSON.toJSONString(aggregatorResourceDateDeliveryOfferList));
            if (CollectionUtils.isEmpty(aggregatorResourceDateDeliveryOfferList)) {
                return;
            }
            List<HashMap<String, String>> deliveryList = Lists.newArrayList();
            aggregatorDateDeliveryChartList.stream()
                    .filter(aggregatorDateDeliveryChart -> null != aggregatorDateDeliveryChart && StringUtils.isNotEmpty(aggregatorDateDeliveryChart.getDeliveryChart()))
                    .forEach(aggregatorDateDeliveryChart -> {
                        HashMap<String, String> deliveryChartMap = new LinkedHashMap<>();
                        String deliveryChart = aggregatorDateDeliveryChart.getDeliveryChart();
                        List<DataResp> deliveryChartList = JSONArray.parseArray(deliveryChart, DataResp.class);
                        if (CollectionUtils.isNotEmpty(deliveryChartList)) {
                            List<DataResp> dataRespList = deliveryChartList.stream().sorted(Comparator.comparing(DataResp::getTime)).collect(Collectors.toList());
                            for (int i = 1; i <= dataRespList.size(); i++) {
                                String key = "FLD" + "-" + aggregatorDateDeliveryChart.getResourceTypeId() + "-" + i;
                                //数据转换兆瓦
                                Double valueMW = MathUtils.mulDoubleZero(dataRespList.get(i - 1).getValue(), 0.001, 2);
                                String value = valueMW + ":" + DateUtils.stringToLong(dataRespList.get(i - 1).getTime());
                                deliveryChartMap.put(key, value);
                            }
                        }
                        deliveryList.add(deliveryChartMap);
                    });
            aggregatorResourceDateDeliveryOfferList.stream()
                    .filter(aggregatorResourceDateDeliveryOffer -> null != aggregatorResourceDateDeliveryOffer && StringUtils.isNotEmpty(aggregatorResourceDateDeliveryOffer.getPriceChart()))
                    .forEach(aggregatorResourceDateDeliveryOffer -> {
                        HashMap<String, String> priceChartMap = new LinkedHashMap<>();
                        String priceChart = aggregatorResourceDateDeliveryOffer.getPriceChart();
                        List<DataResp> priceChartList = JSONArray.parseArray(priceChart, DataResp.class);
                        if (CollectionUtils.isNotEmpty(priceChartList)) {
                            List<DataResp> dataRespList = priceChartList.stream().sorted(Comparator.comparing(DataResp::getTime)).collect(Collectors.toList());
                            for (int i = 1; i <= dataRespList.size(); i++) {
                                String key = "BP" + "-" + aggregatorResourceDateDeliveryOffer.getResourceTypeId() + "-" + i;
                                String value = dataRespList.get(i - 1).getValue() + ":" + DateUtils.stringToLong(dataRespList.get(i - 1).getTime());
                                priceChartMap.put(key, value);
                            }
                        }
                        deliveryList.add(priceChartMap);
                    });
            log.info("申报参数：{}", JSONObject.toJSONString(deliveryList));
            loadAggregatorDeliveryService.declare(deliveryList);
        } catch (Exception e) {
            log.info("申报成功:{}", e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Double aDouble = MathUtils.mulDoubleZero(Double.valueOf("1"), 0.001, 2);
        System.out.println(aDouble);
    }
}
