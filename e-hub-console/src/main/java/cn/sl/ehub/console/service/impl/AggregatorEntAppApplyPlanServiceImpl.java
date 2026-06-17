package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.console.enums.AggregatorEntPlanTypeEnum;
import cn.sl.ehub.console.enums.AggregatorEntSocialResponsibilityEnum;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.service.mapper.AggregatorEntApplyPlanMapper;
import cn.sl.ehub.service.mapper.AggregatorEntSocialResponsibilityMapper;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.service.resp.*;
import cn.sl.ehub.console.service.*;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.service.vo.*;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;

import static java.util.stream.Collectors.summingDouble;
import static java.util.stream.Collectors.toMap;

/**
 * 企业APP申报计划ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AggregatorEntAppApplyPlanServiceImpl implements IAggregatorEntAppApplyPlanService {

    private final AggregatorEntSocialResponsibilityMapper aggregatorEntSocialResponsibilityMapper;
    private final IAggregatorEntDateProfitService aggregatorEntDateProfitService;
    private final IAggregatorEntProfitTimeService aggregatorEntProfitTimeService;
    private final IAggregatorDeviceDateProfitService aggregatorDeviceDateProfitService;
    private final AggregatorEntApplyPlanMapper aggregatorEntApplyPlanMapper;
    private final IAggregatorEntDateApplyDetailService aggregatorEntDateApplyDetailService;
    private final IAggregatorDateHolidayService aggregatorDateHolidayService;
    private final IAggregatorEntService aggregatorEntService;

    @Override
    public AggregatorEntSocialResponsibilityResp getSocialResponsibility(String entId, String date) {
        AtomicDouble clearPower = new AtomicDouble(0);
        String endDate = DateUtils.getDay();
        List<String> dayList = DateUtils.getDayList(date, endDate);
        List<AggregatorEntDateProfit> aggregatorEntDateProfitList = aggregatorEntDateProfitService.getAggregatorEntDateProfitListByEntId(entId, dayList);
        if (CollectionUtils.isNotEmpty(aggregatorEntDateProfitList)) {
            Double addElectricQuantity = aggregatorEntDateProfitList.stream().collect(summingDouble(AggregatorEntDateProfit::getElectricQuantity));
            clearPower.set(MathUtils.addDouble(clearPower.get(), addElectricQuantity, 2));
        }
        //计算当天数据(因为大数据慢暂时不计算)
//        Map<String, List<AggregatorEntProfitTime>> entTimeMap = aggregatorEntProfitTimeService.getEntMap(entId);
//        List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = Lists.newArrayList();
//        List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespListByToday = dataService.dealDevicePowerAndQuantity(entId, endDate);
//        if (CollectionUtils.isNotEmpty(aggregatorDeviceDateProfitRespListByToday)) {
//            aggregatorDeviceDateProfitRespList.addAll(aggregatorDeviceDateProfitRespListByToday);
//        }
//        Map<String, Double> entIdElectricQuantityMap = dataService.getEntIdElectricQuantityMap(aggregatorDeviceDateProfitRespList, entTimeMap);
//        Double todayElectricQuantity = entIdElectricQuantityMap.entrySet().stream().collect(summingDouble(entIdElectricQuantityMapEntry -> entIdElectricQuantityMapEntry.getValue()));
//        clearPower.set(MathUtils.addDouble(clearPower.get(), todayElectricQuantity, 2));
        AggregatorEntSocialResponsibilityResp aggregatorEntSocialResponsibilityResp = new AggregatorEntSocialResponsibilityResp();
        //查询配置
        List<AggregatorEntSocialResponsibility> aggregatorEntSocialResponsibilityList = aggregatorEntSocialResponsibilityMapper.selectAll();
        aggregatorEntSocialResponsibilityList.forEach(config -> {
            if (config.getCode().equals(AggregatorEntSocialResponsibilityEnum.CLEAR_POWER.getCode())) {
                aggregatorEntSocialResponsibilityResp.setCleanPower(getShowDetail(clearPower.get(), config));
            } else if (config.getCode().equals(AggregatorEntSocialResponsibilityEnum.CO2.getCode())) {
                aggregatorEntSocialResponsibilityResp.setCo2(getShowDetail(clearPower.get(), config));
            } else if (config.getCode().equals(AggregatorEntSocialResponsibilityEnum.TREE.getCode())) {
                aggregatorEntSocialResponsibilityResp.setTree(getShowDetail(clearPower.get(), config));
            } else if (config.getCode().equals(AggregatorEntSocialResponsibilityEnum.COAL.getCode())) {
                aggregatorEntSocialResponsibilityResp.setCoal(getShowDetail(clearPower.get(), config));
            } else if (config.getCode().equals(AggregatorEntSocialResponsibilityEnum.SO2.getCode())) {
                aggregatorEntSocialResponsibilityResp.setSo2(getShowDetail(clearPower.get(), config));
            } else if (config.getCode().equals(AggregatorEntSocialResponsibilityEnum.NOX.getCode())) {
                aggregatorEntSocialResponsibilityResp.setNox(getShowDetail(clearPower.get(), config));
            }
        });
        return aggregatorEntSocialResponsibilityResp;
    }

    @Override
    public AggregatorEntApplyPlanResp getAggregatorEntDefaultApplyPlanResp(String entId) {
        List<AggregatorEntApplyPlanResp> aggregatorEntApplyPlanRespList = aggregatorEntApplyPlanMapper.getAggregatorEntApplyPlanRespListByPlanStatus(entId, false);
        if (CollectionUtils.isEmpty(aggregatorEntApplyPlanRespList)) {
            return new AggregatorEntApplyPlanResp();
        }
        AggregatorEntApplyPlanResp aggregatorEntApplyPlanResp = aggregatorEntApplyPlanRespList.get(0);
        if (null == aggregatorEntApplyPlanResp) {
            return new AggregatorEntApplyPlanResp();
        }
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
        return aggregatorEntApplyPlanResp;
    }

    @Override
    public List<AggregatorEntApplyPlanDateResp> getAggregatorEntApplyPlanDateResp(String entId, String startDate, String endDate) {
        List<String> dayList = DateUtils.getDayList(startDate, endDate);
        List<AggregatorEntDateApplyDetail> aggregatorEntDateApplyDetailList = aggregatorEntDateApplyDetailService.getAggregatorEntDateApplyDetailList(entId, dayList, true);
        if (CollectionUtils.isNotEmpty(aggregatorEntDateApplyDetailList)) {
            List<AggregatorEntApplyPlanDateResp> resultList = Lists.newArrayList();
            aggregatorEntDateApplyDetailList.forEach(detail -> {
                AggregatorEntApplyPlanDateResp result = new AggregatorEntApplyPlanDateResp();
                result.setShowDate(detail.getDate());
                result.setShowPlanStatus(detail.getPlanStatus());
                resultList.add(result);
            });
            return resultList;
        }
        return Lists.newArrayList();
    }

    /**
     * 处理数据
     *
     * @param clearPower
     * @param config
     * @return
     */
    private String getShowDetail(Double clearPower, AggregatorEntSocialResponsibility config) {
        String result = config.getName() + " ";
        Double resultDouble = MathUtils.mulDoubleZero(clearPower, config.getValue(), config.getPoint());
        if (resultDouble > 10000) {
            result += MathUtils.doublePointFormat(MathUtils.divideZero(resultDouble, 10000D, config.getPoint()), config.getPoint()) + " 万";
        } else {
            result += MathUtils.doublePointFormat(resultDouble, config.getPoint()) + " ";
        }
        result += config.getUnit();
        return result;
    }
}
