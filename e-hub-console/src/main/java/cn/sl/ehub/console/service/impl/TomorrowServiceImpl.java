package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.console.enums.ApplyStatusEnum;
import cn.sl.ehub.console.service.IAggregatorDateApplyDetailService;
import cn.sl.ehub.console.service.IAggregatorDateDeliveryChartService;
import cn.sl.ehub.console.service.IAggregatorDateHolidayService;
import cn.sl.ehub.console.service.IAggregatorDeviceDateDeliveryChartService;
import cn.sl.ehub.console.service.IAggregatorDeviceDateIssueChartService;
import cn.sl.ehub.console.service.IAggregatorEntDateApplyDetailService;
import cn.sl.ehub.console.service.IAggregatorEntService;
import cn.sl.ehub.console.service.IAggregatorInfoService;
import cn.sl.ehub.console.service.IAggregatorResourceDateDeliveryOfferService;
import cn.sl.ehub.console.service.IAggregatorResourceTypeService;
import cn.sl.ehub.console.service.ITomorrowService;
import cn.sl.ehub.service.mapper.AggregatorDateDeliveryChartMapper;
import cn.sl.ehub.service.mapper.AggregatorDeviceDateDeliveryChartMapper;
import cn.sl.ehub.service.req.AggregatorApplyOfferReq;
import cn.sl.ehub.service.req.AggregatorApplyOfferResourceDateReq;
import cn.sl.ehub.service.req.AggregatorApplyOfferResourceReq;
import cn.sl.ehub.service.req.AggregatorApplyReq;
import cn.sl.ehub.service.req.AggregatorResourceDateDeliveryOfferReq;
import cn.sl.ehub.service.resp.AggregatorApplyOfferResourceDateResp;
import cn.sl.ehub.service.resp.AggregatorApplyOfferResourceResp;
import cn.sl.ehub.service.resp.AggregatorApplyOfferResp;
import cn.sl.ehub.service.resp.AggregatorApplyResp;
import cn.sl.ehub.service.resp.AggregatorResourceDateDeliveryOfferResp;
import cn.sl.ehub.service.resp.AggregatorResourceDateOfferResp;
import cn.sl.ehub.service.resp.EntUserDeviceTomorrowChartResp;
import cn.sl.ehub.service.resp.IndexOverviewResp;
import cn.sl.ehub.service.vo.AggregatorDateApplyDetail;
import cn.sl.ehub.service.vo.AggregatorDateDeliveryChart;
import cn.sl.ehub.service.vo.AggregatorDeviceDateDeliveryChart;
import cn.sl.ehub.service.vo.AggregatorDeviceDateIssueChart;
import cn.sl.ehub.service.vo.AggregatorInfo;
import cn.sl.ehub.service.vo.AggregatorResourceDateDeliveryOffer;
import cn.sl.ehub.service.vo.AggregatorResourceType;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TomorrowServiceImpl implements ITomorrowService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final IAggregatorDateHolidayService aggregatorDateHolidayService;
    private final IAggregatorResourceTypeService aggregatorResourceTypeService;
    private final IAggregatorEntService aggregatorEntService;
    private final IAggregatorDeviceDateDeliveryChartService aggregatorDeviceDateDeliveryChartService;
    private final IAggregatorDeviceDateIssueChartService aggregatorDeviceDateIssueChartService;
    private final IAggregatorDateApplyDetailService aggregatorDateApplyDetailService;
    private final IAggregatorEntDateApplyDetailService aggregatorEntDateApplyDetailService;
    private final IAggregatorResourceDateDeliveryOfferService aggregatorResourceDateDeliveryOfferService;
    private final IAggregatorDateDeliveryChartService aggregatorDateDeliveryChartService;
    private final IAggregatorInfoService aggregatorInfoService;
    private final AggregatorDeviceDateDeliveryChartMapper aggregatorDeviceDateDeliveryChartMapper;
    private final AggregatorDateDeliveryChartMapper aggregatorDateDeliveryChartMapper;

    @Override
    public EntUserDeviceTomorrowChartResp getEntUserDeviceTomorrowChartResp(String deviceBaseId) {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<String> quarterAxis = buildQuarterAxis(tomorrow);
        EntUserDeviceTomorrowChartResp resp = new EntUserDeviceTomorrowChartResp();
        resp.setDeliveryChart(buildSingleDeviceQuarterChart(
                quarterAxis,
                aggregatorDeviceDateDeliveryChartService.getAggregatorDeviceDateDeliveryChart(deviceBaseId, tomorrow.format(DATE_FORMATTER)),
                AggregatorDeviceDateDeliveryChart::getDeliveryChart));
        resp.setIssueChart(buildSingleDeviceQuarterChart(
                quarterAxis,
                aggregatorDeviceDateIssueChartService.getAggregatorDeviceDateIssueChart(deviceBaseId, tomorrow.format(DATE_FORMATTER)),
                AggregatorDeviceDateIssueChart::getIssueChart));
        return resp;
    }

    @Override
    public AggregatorApplyResp getAggregatorApply(String aggregatorId, String date) {
        AggregatorInfo aggregatorInfo = aggregatorInfoService.getAggregatorInfo(aggregatorId);
        if (aggregatorInfo == null) {
            throw new BaseException(StatusCode.ERROR.getCode(), "未查询到聚合商信息");
        }
        String baseDate = StringUtils.isBlank(date) ? LocalDate.now().format(DATE_FORMATTER) : date;
        String targetDate = LocalDate.parse(baseDate, DATE_FORMATTER).plusDays(1).format(DATE_FORMATTER);
        List<String> applyDateList = aggregatorDateHolidayService.getApplyDateList(baseDate, false);

        AggregatorApplyResp resp = new AggregatorApplyResp();
        int entNum = aggregatorEntService.getCount(aggregatorId);
        int applyYesNum = resolveApplyEntCount(aggregatorId, targetDate);
        resp.setEntNum(entNum);
        resp.setApplyYesNum(applyYesNum);
        resp.setApplyNoNum(Math.max(entNum - applyYesNum, 0));
        resp.setApplyResourceType(resolveApplyResourceType(aggregatorId, targetDate));
        resp.setApplyPriceStatus(aggregatorResourceDateDeliveryOfferService.getCount(aggregatorId, targetDate, "1") > 0 ? "1" : "0");
        resp.setPlanDate(formatPlanDate(applyDateList));

        String planDateCheck = CollectionUtils.isEmpty(applyDateList) ? targetDate : applyDateList.get(0);
        AggregatorDateApplyDetail detail = aggregatorDateApplyDetailService.getAggregatorDateApplyDetail(aggregatorId, targetDate);
        String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        if (planDateCheck.equals(targetDate)) {
            if (detail == null || ApplyStatusEnum.APPLY_NO.getCode().equals(detail.getApplyStatus())) {
                resp.setApplyStatus(ApplyStatusEnum.APPLY_NO.getCode());
                String applyStartTime = StringUtils.defaultString(aggregatorInfo.getApplyStartTime(), "09:00:00");
                String applyEndTime = StringUtils.defaultString(aggregatorInfo.getApplyEndTime(), "16:00:00");
                if (nowTime.compareTo(applyStartTime) < 0) {
                    resp.setApplyStatus(ApplyStatusEnum.NO_ALLOW_APPLY.getCode());
                    resp.setApplyContext("将于" + formatClock(applyStartTime) + "开始，请耐心等待");
                } else if (nowTime.compareTo(applyEndTime) < 0) {
                    resp.setApplyContext("将于" + formatClock(applyEndTime) + "结束，请尽快申报");
                } else {
                    resp.setApplyStatus(ApplyStatusEnum.APPLY_YES.getCode());
                    resp.setApplyContext("今日未申报");
                }
            } else {
                fillApplyDetail(resp, detail);
                fillApplyDetailContext(resp, detail, applyDateList, aggregatorInfo);
            }
        } else {
            if (detail == null) {
                resp.setApplyStatus(ApplyStatusEnum.APPLY_YES.getCode());
                resp.setApplyContext(buildNextRoundApplyContext(applyDateList, aggregatorInfo));
            } else {
                fillApplyDetail(resp, detail);
                fillApplyDetailContext(resp, detail, applyDateList, aggregatorInfo);
            }
        }
        return resp;
    }

    @Override
    public Boolean updateAggregatorApply(AggregatorApplyReq req) {
        if (req == null || StringUtils.isBlank(req.getAggregatorId())) {
            throw new BaseException(StatusCode.ERROR.getCode(), "聚合商ID为空");
        }
        String day = StringUtils.isBlank(req.getDate()) ? LocalDate.now().format(DATE_FORMATTER) : req.getDate();
        if (Boolean.TRUE.equals(aggregatorDateHolidayService.getApplyDateCheck(day))) {
            return true;
        }
        List<String> dateList = aggregatorDateHolidayService.getApplyDateList(day, false);
        if ("1".equals(req.getApplyType())
                && aggregatorResourceDateDeliveryOfferService.getCount(req.getAggregatorId(), dateList, "1") <= 0) {
            throw new BaseException(StatusCode.ERROR.getCode(), "请提交报价后再申报");
        }
        if (!"1".equals(req.getApplyType())
                && aggregatorResourceDateDeliveryOfferService.getCount(req.getAggregatorId(), dateList) <= 0) {
            initZeroOffer(req.getAggregatorId(), dateList);
        }

        aggregatorDateApplyDetailService.delete(req.getAggregatorId(), dateList);
        List<AggregatorDateApplyDetail> saveList = new ArrayList<>(dateList.size());
        String applyTime = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        for (String targetDate : dateList) {
            AggregatorDateApplyDetail detail = new AggregatorDateApplyDetail();
            detail.setAggregatorId(req.getAggregatorId());
            detail.setDate(targetDate);
            detail.setApplyType(StringUtils.defaultIfBlank(req.getApplyType(), "1"));
            detail.setApplyStatus("1");
            detail.setApplyTime(applyTime);
            detail.setCreateTime(applyTime);
            detail.setUpdateTime(applyTime);
            detail.setApplyBy(StringUtils.defaultIfBlank(req.getApplyBy(), "系统"));
            saveList.add(detail);
        }
        if (!saveList.isEmpty()) {
            aggregatorDateApplyDetailService.batchInsert(saveList);
        }
        return true;
    }

    @Override
    public Boolean autoAggregatorApply(AggregatorApplyReq req) {
        return updateAggregatorApply(req);
    }

    @Override
    public AggregatorApplyOfferResp getAggregatorApplyOfferResp(String aggregatorId, String date) {
        String baseDate = StringUtils.isBlank(date) ? LocalDate.now().format(DATE_FORMATTER) : date;
        List<String> dateList = aggregatorDateHolidayService.getApplyDateList(baseDate, false);
        List<AggregatorResourceDateDeliveryOffer> offerList = aggregatorResourceDateDeliveryOfferService
                .getAggregatorResourceDateDeliveryOfferList(aggregatorId, dateList);

        Map<String, Map<String, AggregatorResourceDateDeliveryOffer>> offerMap = offerList.stream()
                .collect(Collectors.groupingBy(
                        AggregatorResourceDateDeliveryOffer::getResourceTypeId,
                        LinkedHashMap::new,
                        Collectors.toMap(AggregatorResourceDateDeliveryOffer::getDate, Function.identity(), (v1, v2) -> v1, LinkedHashMap::new)));

        AggregatorApplyOfferResp resp = new AggregatorApplyOfferResp();
        resp.setStatus(offerList.isEmpty() ? "0" : StringUtils.defaultIfBlank(offerList.get(0).getStatus(), "0"));
        List<AggregatorApplyOfferResourceResp> resourceRespList = new ArrayList<>();
        for (AggregatorResourceType resourceType : aggregatorResourceTypeService.getAggregatorResourceTypeListByAggregatorId(aggregatorId)) {
            AggregatorApplyOfferResourceResp resourceResp = new AggregatorApplyOfferResourceResp();
            resourceResp.setResourceTypeId(resourceType.getId());
            resourceResp.setResourceTypeName(resourceType.getName());
            Map<String, AggregatorResourceDateDeliveryOffer> dateOfferMap = offerMap.getOrDefault(resourceType.getId(), Collections.emptyMap());
            resourceResp.setStatus(dateOfferMap.values().stream()
                    .map(AggregatorResourceDateDeliveryOffer::getPriceStatus)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse(Boolean.FALSE));

            List<AggregatorApplyOfferResourceDateResp> dateRespList = new ArrayList<>();
            for (String targetDate : dateList) {
                AggregatorResourceDateDeliveryOffer offer = dateOfferMap.get(targetDate);
                AggregatorApplyOfferResourceDateResp dateResp = new AggregatorApplyOfferResourceDateResp();
                dateResp.setDate(targetDate);
                dateResp.setStatus(offer == null ? Boolean.TRUE : offer.getPriceStatus());
                dateResp.setOfferList(resolveOfferDetailList(aggregatorId, resourceType.getId(), targetDate, offer));
                dateRespList.add(dateResp);
            }
            resourceResp.setDateList(dateRespList);
            resourceRespList.add(resourceResp);
        }
        resp.setResourceList(resourceRespList);
        return resp;
    }

    @Override
    public Boolean saveAggregatorApplyOffer(AggregatorApplyOfferReq req, String status) {
        if (req == null || StringUtils.isBlank(req.getAggregatorId())) {
            throw new BaseException(StatusCode.ERROR.getCode(), "聚合商ID为空");
        }
        if (CollectionUtils.isEmpty(req.getResourceList())) {
            throw new BaseException(StatusCode.ERROR.getCode(), "资源类型为空");
        }
        List<String> dateList = aggregatorDateHolidayService.getApplyDateList(LocalDate.now().format(DATE_FORMATTER), false);
        if (aggregatorResourceDateDeliveryOfferService.getCount(req.getAggregatorId(), dateList, "1") > 0) {
            throw new BaseException(StatusCode.ERROR.getCode(), "已提交报价");
        }
        aggregatorResourceDateDeliveryOfferService.delete(req.getAggregatorId(), dateList);

        List<AggregatorResourceDateDeliveryOffer> saveList = new ArrayList<>();
        for (AggregatorApplyOfferResourceReq resource : req.getResourceList()) {
            if (CollectionUtils.isEmpty(resource.getDateList())) {
                continue;
            }
            for (AggregatorApplyOfferResourceDateReq dateReq : resource.getDateList()) {
                List<AggregatorResourceDateDeliveryOfferReq> offerReqList = dateReq.getOfferList();
                if (CollectionUtils.isEmpty(offerReqList)) {
                    continue;
                }
                AggregatorResourceDateDeliveryOffer save = new AggregatorResourceDateDeliveryOffer();
                save.setAggregatorId(req.getAggregatorId());
                save.setResourceTypeId(resource.getResourceTypeId());
                save.setDate(dateReq.getDate());
                save.setOffer(offerReqList.get(0).getOffer());
                save.setStatus(status);
                save.setPriceStatus(resource.getStatus());
                save.setPriceDetail(JSONObject.toJSONString(buildOfferRespList(offerReqList)));
                save.setPriceChart(JSONObject.toJSONString(buildPriceChart(dateReq.getDate(), offerReqList)));
                saveList.add(save);
            }
        }
        if (!saveList.isEmpty()) {
            aggregatorResourceDateDeliveryOfferService.batchInsert(saveList);
        }
        return true;
    }

    @Override
    public IndexOverviewResp getAggregatorDeliveryChart(String aggregatorId, String resourceTypeId, String date) {
        String baseDate = StringUtils.isBlank(date) ? LocalDate.now().format(DATE_FORMATTER) : date;
        List<String> dateList = aggregatorDateHolidayService.getApplyDateList(baseDate, false);
        List<String> axis = LoadAggregationChartSupport.buildQuarterDateTimeAxis(dateList);
        IndexOverviewResp resp = new IndexOverviewResp();
        resp.setTimeList(axis);
        resp.setDeliveryChart(buildQuarterChart(
                axis,
                aggregatorDateDeliveryChartService.getAggregatorDateDeliveryChartList(aggregatorId, resourceTypeId, dateList),
                AggregatorDateDeliveryChart::getDate,
                AggregatorDateDeliveryChart::getDeliveryChart));
        return resp;
    }

    @Override
    public List<AggregatorApplyOfferResourceDateResp> getPriceByResourceTypeId(String aggregatorId, String resourceTypeId, String date) {
        String baseDate = StringUtils.isBlank(date) ? LocalDate.now().format(DATE_FORMATTER) : date;
        List<String> dateList = aggregatorDateHolidayService.getApplyDateList(baseDate, false);
        List<AggregatorResourceDateDeliveryOffer> offerList = aggregatorResourceDateDeliveryOfferService
                .getAggregatorResourceDateDeliveryOfferList(aggregatorId, resourceTypeId, dateList, "1");
        Map<String, AggregatorResourceDateDeliveryOffer> dateOfferMap = offerList.stream()
                .collect(Collectors.toMap(AggregatorResourceDateDeliveryOffer::getDate, Function.identity(), (v1, v2) -> v1, LinkedHashMap::new));

        List<AggregatorApplyOfferResourceDateResp> result = new ArrayList<>(dateList.size());
        for (String targetDate : dateList) {
            AggregatorApplyOfferResourceDateResp item = new AggregatorApplyOfferResourceDateResp();
            item.setDate(targetDate);
            AggregatorResourceDateDeliveryOffer offer = dateOfferMap.get(targetDate);
            item.setOfferList(offer == null ? new ArrayList<>() : parseOfferRespList(offer.getPriceDetail()));
            result.add(item);
        }
        return result;
    }

    private int resolveApplyEntCount(String aggregatorId, String targetDate) {
        List<String> entList = aggregatorDateDeliveryChartMapper.getPlanApplyEntNum(aggregatorId, targetDate);
        return CollectionUtils.isEmpty(entList) ? 0 : entList.size();
    }

    private String resolveApplyResourceType(String aggregatorId, String targetDate) {
        List<String> typeList = aggregatorDeviceDateDeliveryChartMapper.getApplyResourceType(aggregatorId, targetDate);
        if (CollectionUtils.isEmpty(typeList)) {
            return "无";
        }
        return String.join("、", typeList);
    }

    private void fillApplyDetail(AggregatorApplyResp resp, AggregatorDateApplyDetail detail) {
        resp.setApplyStatus(StringUtils.defaultIfBlank(detail.getApplyStatus(), ApplyStatusEnum.APPLYING.getCode()));
        resp.setWinStatus(detail.getWinStatus());
        resp.setApplyType(detail.getApplyType());
        resp.setApplyTime(formatApplyTime(detail.getApplyTime()));
        resp.setApplyBy(detail.getApplyBy());
    }

    private void fillApplyDetailContext(AggregatorApplyResp resp,
                                        AggregatorDateApplyDetail detail,
                                        List<String> applyDateList,
                                        AggregatorInfo aggregatorInfo) {
        if (StringUtils.isBlank(detail.getWinStatus())) {
            resp.setApplyContext(StringUtils.defaultIfBlank(resp.getApplyBy(), "系统")
                    + "已于"
                    + StringUtils.defaultString(resp.getApplyTime())
                    + "完成申报");
            return;
        }
        if ("1".equals(detail.getWinStatus())) {
            resp.setApplyContext("系统已为您自动下发功率曲线");
            return;
        }
        resp.setApplyContext(buildNextRoundApplyContext(applyDateList, aggregatorInfo));
    }

    private String buildNextRoundApplyContext(List<String> applyDateList, AggregatorInfo aggregatorInfo) {
        String date = CollectionUtils.isEmpty(applyDateList)
                ? LocalDate.now().plusDays(1).format(DATE_FORMATTER)
                : applyDateList.get(applyDateList.size() - 1);
        return date + " " + formatClock(StringUtils.defaultString(aggregatorInfo.getApplyStartTime(), "09:00:00")) + "开始新一轮申报";
    }

    private String formatClock(String time) {
        if (StringUtils.isBlank(time)) {
            return "";
        }
        return time.length() >= 5 ? time.substring(0, 5) : time;
    }

    private String formatApplyTime(String applyTime) {
        if (StringUtils.isBlank(applyTime)) {
            return "";
        }
        return applyTime.length() >= 16 ? applyTime.substring(0, 16) : applyTime;
    }

    private String formatPlanDate(List<String> dateList) {
        if (CollectionUtils.isEmpty(dateList)) {
            return null;
        }
        if (dateList.size() == 1) {
            return dateList.get(0);
        }
        return dateList.get(0) + "至" + dateList.get(dateList.size() - 1);
    }

    private void initZeroOffer(String aggregatorId, List<String> dateList) {
        List<AggregatorResourceDateDeliveryOffer> saveList = new ArrayList<>();
        for (String targetDate : dateList) {
            for (AggregatorResourceType resourceType : aggregatorResourceTypeService.getAggregatorDisplayResourceTypeList(aggregatorId)) {
                AggregatorResourceDateDeliveryOffer save = new AggregatorResourceDateDeliveryOffer();
                save.setAggregatorId(aggregatorId);
                save.setResourceTypeId(resourceType.getId());
                save.setDate(targetDate);
                save.setOffer(0D);
                save.setStatus("1");
                save.setPriceStatus(Boolean.TRUE);
                AggregatorResourceDateDeliveryOfferResp detail = new AggregatorResourceDateDeliveryOfferResp();
                detail.setStartTime("00:00");
                detail.setEndTime("24:00");
                detail.setOffer(0D);
                save.setPriceDetail(JSONObject.toJSONString(Collections.singletonList(detail)));
                save.setPriceChart(JSONObject.toJSONString(buildPriceChart(targetDate, Collections.singletonList(toOfferReq(detail)))));
                saveList.add(save);
            }
        }
        if (!saveList.isEmpty()) {
            aggregatorResourceDateDeliveryOfferService.batchInsert(saveList);
        }
    }

    private AggregatorResourceDateDeliveryOfferReq toOfferReq(AggregatorResourceDateDeliveryOfferResp detail) {
        AggregatorResourceDateDeliveryOfferReq req = new AggregatorResourceDateDeliveryOfferReq();
        req.setStartTime(detail.getStartTime());
        req.setEndTime(detail.getEndTime());
        req.setOffer(detail.getOffer());
        return req;
    }

    private List<AggregatorResourceDateOfferResp> resolveOfferDetailList(String aggregatorId,
                                                                         String resourceTypeId,
                                                                         String date,
                                                                         AggregatorResourceDateDeliveryOffer offer) {
        if (offer == null || StringUtils.isBlank(offer.getPriceDetail())) {
            AggregatorResourceDateOfferResp defaultOffer = new AggregatorResourceDateOfferResp();
            defaultOffer.setAggregatorId(aggregatorId);
            defaultOffer.setResourceTypeId(resourceTypeId);
            defaultOffer.setDate(date);
            defaultOffer.setStartTime("00:00");
            defaultOffer.setEndTime("24:00");
            defaultOffer.setOffer(0D);
            return Collections.singletonList(defaultOffer);
        }
        List<AggregatorResourceDateOfferResp> detailList = parseOfferRespList(offer.getPriceDetail());
        return detailList.isEmpty() ? resolveOfferDetailList(aggregatorId, resourceTypeId, date, null) : detailList;
    }

    private List<AggregatorResourceDateOfferResp> parseOfferRespList(String priceDetail) {
        if (StringUtils.isBlank(priceDetail)) {
            return new ArrayList<>();
        }
        List<AggregatorResourceDateOfferResp> result = JSONObject.parseArray(priceDetail, AggregatorResourceDateOfferResp.class);
        return result == null ? new ArrayList<>() : result;
    }

    private List<AggregatorResourceDateDeliveryOfferResp> buildOfferRespList(List<AggregatorResourceDateDeliveryOfferReq> offerReqList) {
        List<AggregatorResourceDateDeliveryOfferResp> result = new ArrayList<>(offerReqList.size());
        for (AggregatorResourceDateDeliveryOfferReq offerReq : offerReqList) {
            AggregatorResourceDateDeliveryOfferResp item = new AggregatorResourceDateDeliveryOfferResp();
            item.setStartTime(offerReq.getStartTime());
            item.setEndTime(offerReq.getEndTime());
            item.setOffer(offerReq.getOffer());
            result.add(item);
        }
        return result;
    }

    private List<DataResp> buildPriceChart(String date, List<AggregatorResourceDateDeliveryOfferReq> offerReqList) {
        List<String> axis = buildQuarterAxis(LocalDate.parse(date, DATE_FORMATTER));
        Map<String, Double> valueMap = new LinkedHashMap<>();
        for (AggregatorResourceDateDeliveryOfferReq offerReq : offerReqList) {
            if (offerReq == null || StringUtils.isBlank(offerReq.getStartTime()) || StringUtils.isBlank(offerReq.getEndTime())) {
                continue;
            }
            String start = date + " " + normalizeSectionTime(offerReq.getStartTime());
            String end = "24:00".equals(offerReq.getEndTime())
                    ? LocalDate.parse(date, DATE_FORMATTER).plusDays(1).format(DATE_FORMATTER) + " 00:00"
                    : date + " " + normalizeSectionTime(offerReq.getEndTime());
            List<String> sectionAxis = buildQuarterAxis(start, end);
            for (String time : sectionAxis) {
                valueMap.put(time, offerReq.getOffer());
            }
        }
        return LoadAggregationChartSupport.alignQuarterAxis(axis, valueMap);
    }

    private String normalizeSectionTime(String time) {
        return StringUtils.length(time) == 5 ? time : StringUtils.substring(time, 0, 5);
    }

    private List<String> buildQuarterAxis(LocalDate date) {
        return LoadAggregationChartSupport.toShortTimeAxis(
                LoadAggregationChartSupport.buildQuarterDateTimeAxis(Collections.singletonList(date.format(DATE_FORMATTER))));
    }

    private List<String> buildQuarterAxis(String start, String end) {
        List<String> result = new ArrayList<>();
        java.time.LocalDateTime current = java.time.LocalDateTime.parse(start, DATE_TIME_FORMATTER);
        java.time.LocalDateTime endTime = java.time.LocalDateTime.parse(end, DATE_TIME_FORMATTER);
        while (!current.isAfter(endTime)) {
            result.add(DATE_TIME_FORMATTER.format(current));
            current = current.plusMinutes(15);
        }
        return result;
    }

    private <T> List<DataResp> buildSingleDeviceQuarterChart(List<String> axis,
                                                             T record,
                                                             Function<T, String> chartGetter) {
        if (record == null) {
            return emptyQuarterChart(axis);
        }
        Map<String, Double> valueMap = LoadAggregationChartSupport.parseCurveJson(LocalDate.now().plusDays(1).format(DATE_FORMATTER), chartGetter.apply(record))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> LoadAggregationChartSupport.toShortTime(entry.getKey()),
                        Map.Entry::getValue,
                        (v1, v2) -> v1,
                        LinkedHashMap::new));
        return LoadAggregationChartSupport.alignQuarterAxis(axis, valueMap);
    }

    private <T> List<DataResp> buildQuarterChart(List<String> axis,
                                                 List<T> recordList,
                                                 Function<T, String> dateGetter,
                                                 Function<T, String> chartGetter) {
        if (CollectionUtils.isEmpty(recordList)) {
            return emptyQuarterChart(axis);
        }
        List<Map<String, Double>> mapList = new ArrayList<>(recordList.size());
        for (T item : recordList) {
            mapList.add(LoadAggregationChartSupport.parseCurveJson(dateGetter.apply(item), chartGetter.apply(item)));
        }
        return LoadAggregationChartSupport.alignQuarterAxis(axis, LoadAggregationChartSupport.mergeCurveMaps(mapList));
    }

    private List<DataResp> emptyQuarterChart(List<String> axis) {
        return LoadAggregationChartSupport.alignQuarterAxis(axis, Collections.emptyMap());
    }
}
