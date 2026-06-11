package cn.sl.ehub.console.service.impl;

import cn.enn.bigdata.req.HistoryReq;
import cn.enn.bigdata.req.OpentsdbReq;
import cn.enn.bigdata.resp.BigDataHistoryResp;
import cn.enn.bigdata.resp.TagVO;
import cn.enn.bigdata.service.IBigDataHandlerService;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.service.mapper.AggregatorApplyPlanMapper;
import cn.sl.ehub.service.mapper.AggregatorDateDeliveryChartMapper;
import cn.sl.ehub.console.model.resp.ChartDataResp;
import cn.sl.ehub.service.req.AddPlanDataReq;
import cn.sl.ehub.service.req.AddPlanReq;
import cn.sl.ehub.service.req.QueryPlanListReq;
import cn.sl.ehub.service.req.ReferDatePowerReq;
import cn.sl.ehub.service.resp.*;
import cn.sl.ehub.console.service.*;
import cn.sl.ehub.service.vo.*;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class AggregatorApplyPlanServiceImpl implements IAggregatorApplyPlanService {

   @Autowired
   private IAggregatorEntDeviceService aggregatorEntDeviceService;

   @Autowired
   private IBigDataHandlerService bigDataHandlerService;

   @Autowired
   private AggregatorApplyPlanMapper aggregatorApplyPlanMapper;

   @Autowired
   private AggregatorDateDeliveryChartMapper aggregatorDateDeliveryChartMapper;

   @Autowired
   private IAggregatorDateDeliveryChartService aggregatorDateDeliveryChartService;

   @Autowired
   private IAggregatorDateIssueChartService aggregatorDateIssueChartService;

   @Autowired
   private IAggregatorDateApplyDetailService aggregatorDateApplyDetailService;

    @Autowired
    private  IAggregatorDateHolidayService aggregatorDateHolidayService;


    @Override
    public ReferDatePowerResp getReferDatePower(ReferDatePowerReq referDatePowerReq) {
        ReferDatePowerResp referDatePowerResp = new ReferDatePowerResp();
        String aggregatorId = referDatePowerReq.getAggregatorId();
         String sourceId = referDatePowerReq.getSourceId();
         String referDate = referDatePowerReq.getReferDate();
        //1.根据资源类型+聚合商id查询设备
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getDeviceList(aggregatorId, sourceId);
        if(CollectionUtil.isEmpty(deviceList)){
            throw new BaseException(StatusCode.ERROR.getCode(), "设备信息不存在");
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        //2.调用大数据结果


        String startTime = referDate + " 00:15:00";
        String endTime   = LocalDate.parse(referDate,dateTimeFormatter).plusDays(1).format(dateTimeFormatter) + "  00:00:00";

        HistoryReq historyReq = new HistoryReq();
        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");

        List<OpentsdbReq> listQueries = Lists.newArrayList();
        deviceList.forEach(config -> {
            OpentsdbReq opentsdbReq = new OpentsdbReq();
            opentsdbReq.setDownsample("15m-first");
            opentsdbReq.setMetric("EMS.P");
            opentsdbReq.setAggregator("last");
            cn.enn.bigdata.req.TagVO tag = new cn.enn.bigdata.req.TagVO();
            tag.setStaId(config.getStationId());
            tag.setEquipMK(config.getDeviceType());
            tag.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq.setTags(tag);
            listQueries.add(opentsdbReq);
        });
        historyReq.setListQueries(listQueries);
        List<BigDataHistoryResp> bigDataHistoryRespList = bigDataHandlerService.getHistory(historyReq, "0");

        LocalDate beginDate = LocalDate.parse(referDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDateTime beginTime = LocalDateTime.of(beginDate, LocalTime.MIN).plusMinutes(15);

        //3.数据处理返回结果
        List<ReferDatePowerDataResp> list = new ArrayList<>();
        List<Map<String, Double>> mapList = bigDataHistoryRespList.stream()
            .filter(a -> a != null && a.getDataResp() != null)
            .map(a -> {
                List<DataResp> dataRespList = a.getDataResp();
                Map<String, Double> map = dataRespList.stream()
                    .filter(b -> b != null && b.getTime() != null)
                    .collect(Collectors.toMap(b -> b.getTime(), b -> b.getValue() != null ? b.getValue() : 0.0));
                return map;
            })
            .collect(Collectors.toList());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        for(int i=0;i<96;i++){
            LocalDateTime time = beginTime.plusMinutes(15*i);
            String formatTime = time.format(formatter);
            Double timeValue = mapList.stream().mapToDouble(a -> null == a.get(formatTime) ? 0 : a.get(formatTime)).sum();

            ReferDatePowerDataResp referDatePowerDataResp = new ReferDatePowerDataResp();
            if(i ==95){
                referDatePowerDataResp.setDate("24:00");
            }else {
                referDatePowerDataResp.setDate(time.toLocalTime().format(timeFormatter));
            }
            referDatePowerDataResp.setValue(BigDecimal.valueOf(timeValue).setScale(2,BigDecimal.ROUND_HALF_UP).toPlainString());
            list.add(referDatePowerDataResp);
        }
        referDatePowerResp.setList(list);

        return referDatePowerResp;
    }

    @Override
    public QueryPlanListResp getPlanList(QueryPlanListReq queryPlanListReq) {
        QueryPlanListResp queryPlanListResp = new QueryPlanListResp();
        String aggregatorId = queryPlanListReq.getAggregatorId();
        LocalDate now = LocalDate.now();
        String todayDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        //查询所有未过期的计划
        Weekend<AggregatorApplyPlan> weekend = Weekend.of(AggregatorApplyPlan.class);
        WeekendCriteria<AggregatorApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorApplyPlan::getAggregatorId, aggregatorId);
        List<AggregatorApplyPlan> planList = aggregatorApplyPlanMapper.selectByExample(weekend);


        //计划根据资源id分组
        Map<String, List<AggregatorApplyPlan>> map = planList.stream().collect(Collectors.groupingBy(AggregatorApplyPlan::getSourceId));
        List<QueryPlanSourceListResp> resList = new ArrayList<>();
        map.forEach((key,value)->{
            List<AggregatorApplyPlan> list = map.get(key);
            QueryPlanSourceListResp queryPlanSourceListResp = new QueryPlanSourceListResp();
            queryPlanSourceListResp.setSourceId(key);
            list =  list.stream().sorted(Comparator.comparing(AggregatorApplyPlan::getUpdateTime).reversed()).collect(Collectors.toList());


            List<QueryPlanListDataResp> planListDataRepList= list.stream().map(b -> {
                QueryPlanListDataResp queryPlanListDataResp = new QueryPlanListDataResp();
                BeanUtils.copyProperties(b, queryPlanListDataResp);
                String planStatus = getPlanStatus(now, LocalDate.parse(b.getStartDate()), LocalDate.parse(b.getEndDate()));
                queryPlanListDataResp.setPlanStatus(planStatus);
                return queryPlanListDataResp;
            }).filter(a-> !a.getPlanStatus().equals("0")).collect(Collectors.toList());
            queryPlanSourceListResp.setPlanDataList(planListDataRepList);
            resList.add(queryPlanSourceListResp);

        });
        queryPlanListResp.setList(resList);

        return queryPlanListResp;
    }

    @Override
    public PlanDetailResp getPlanDetailById(String planId) {

        LocalTime minTime = LocalTime.MIN.plusMinutes(15);
        PlanDetailResp planDetailResp = new PlanDetailResp();
        //查询计划
        AggregatorApplyPlan aggregatorApplyPlan = aggregatorApplyPlanMapper.selectByPrimaryKey(planId);

        BeanUtils.copyProperties(aggregatorApplyPlan,planDetailResp);

        String referDatePower = aggregatorApplyPlan.getReferDatePower();
        List<ChartDataResp> referDatePowerList = JSONObject.parseArray(referDatePower, ChartDataResp.class);
        Map<String, String> referDatePowerMap = referDatePowerList.stream().collect(Collectors.toMap(a -> a.getTime(), b -> b.getValue()));

        String adjustFactor = aggregatorApplyPlan.getAdjustFactor();
        List<ChartDataResp> adjustFactorList = JSONObject.parseArray(adjustFactor, ChartDataResp.class);
        Map<String, String> adjustFactorMap = adjustFactorList.stream().collect(Collectors.toMap(a -> a.getTime(), b -> b.getValue()));

        String adjustJsonValue = aggregatorApplyPlan.getAdjustValue();
        List<ChartDataResp> adjustValueList = JSONObject.parseArray(adjustJsonValue, ChartDataResp.class);
        Map<String, String> adjustValueMap = adjustValueList.stream().collect(Collectors.toMap(a -> a.getTime(), b -> b.getValue()));

        String applyPower = aggregatorApplyPlan.getApplyPower();
        List<ChartDataResp> applyPowerList = JSONObject.parseArray(applyPower, ChartDataResp.class);
        Map<String, String> applyPowerMap = applyPowerList.stream().collect(Collectors.toMap(a -> a.getTime(), b -> b.getValue()));


        String applyPrice = aggregatorApplyPlan.getApplyPrice();
        List<ChartDataResp> applyPriceList = JSONObject.parseArray(applyPrice, ChartDataResp.class);
        Map<String, String> applyPriceMap = applyPriceList.stream().collect(Collectors.toMap(a -> a.getTime(), b -> b.getValue()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        List<PlanDetailDataResp> dataList = new ArrayList<>();
        for(int i=0;i<96;i++){
            PlanDetailDataResp planDetailDataResp = new PlanDetailDataResp();
            String time = minTime.plusMinutes(15 * i).format(formatter);
            if(time.equals("00:00")){
                time = "24:00";
            }

            String referDatePowerValue = referDatePowerMap.get(time);
            String adjustFactorValue = adjustFactorMap.get(time);
            String adjustValue = adjustValueMap.get(time);
            String applyPowerValue = applyPowerMap.get(time);
            String applyPriceValue = applyPriceMap.get(time);
            planDetailDataResp.setReferDatePower(referDatePowerValue);
            planDetailDataResp.setAdjustFactor(adjustFactorValue);
            planDetailDataResp.setAdjustValue(adjustValue);
            planDetailDataResp.setApplyPower(applyPowerValue);
            planDetailDataResp.setApplyPrice(applyPriceValue);
            planDetailDataResp.setDateTime(time);
            dataList.add(planDetailDataResp);

        }
        planDetailResp.setDataList(dataList);

        return planDetailResp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addOrUpdatePlan(AddPlanReq addPlanReq) {
        List<AddPlanDataReq> reqDataList = addPlanReq.getDataList();
        if(CollectionUtil.isEmpty(reqDataList) || reqDataList.size()!=96){
            throw new BaseException(StatusCode.ERROR.getCode(), "数据集合必须包括96个时间点值");
        }
        AggregatorApplyPlan beforeAggregatorApplyPlan =null;
        if(StringUtil.isNotEmpty(addPlanReq.getId())){
            //查询计划
            beforeAggregatorApplyPlan = aggregatorApplyPlanMapper.selectByPrimaryKey(addPlanReq.getId());
            if(null == beforeAggregatorApplyPlan){
                throw new BaseException(StatusCode.ERROR.getCode(), "计划不存在");
            }
        }
         String aggregatorId = addPlanReq.getAggregatorId();
        String sourceId = addPlanReq.getSourceId();
        //校验是否有重复的计划
        Weekend<AggregatorApplyPlan> weekend = Weekend.of(AggregatorApplyPlan.class);
        WeekendCriteria<AggregatorApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorApplyPlan::getAggregatorId, aggregatorId).
                andEqualTo(AggregatorApplyPlan::getSourceId,sourceId);
        if(StringUtil.isNotEmpty(addPlanReq.getId())){
            criteria.andNotEqualTo(AggregatorApplyPlan::getId,addPlanReq.getId());
        }
        List<AggregatorApplyPlan> planList = aggregatorApplyPlanMapper.selectByExample(weekend);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate reqStartDate = LocalDate.parse(addPlanReq.getStartDate(), formatter);
        LocalDate reqEndDate = LocalDate.parse(addPlanReq.getEndDate(), formatter);
        if(CollectionUtil.isNotEmpty(planList)){
            boolean anyMatch = planList.stream().anyMatch(a ->
                    checkRepeatPlan(reqStartDate, reqEndDate, LocalDate.parse(a.getStartDate(), formatter), LocalDate.parse(a.getEndDate())));
            if(anyMatch){
                throw new BaseException(StatusCode.ERROR.getCode(), "计划周期重复，请重新选择周期");
            }
        }

        //数据处理
        List<AddPlanDataReq> dataList = addPlanReq.getDataList();
        List<ChartDataResp> referDatePowerDataList = new ArrayList<>();
        List<ChartDataResp> adjustFactorDataList = new ArrayList<>();
        List<ChartDataResp> adjustValueDataList = new ArrayList<>();
        List<ChartDataResp> applyPowerDataList = new ArrayList<>();
        List<ChartDataResp> applyPriceDataList = new ArrayList<>();
        dataList.stream().forEach(a->{
            ChartDataResp referDatePowerData = new ChartDataResp();
            String dateTime = a.getDateTime();
            referDatePowerData.setTime(dateTime);
            referDatePowerData.setValue(a.getReferDatePower());
            referDatePowerDataList.add(referDatePowerData);


            ChartDataResp adjustFactorData = new ChartDataResp();
            adjustFactorData.setTime(dateTime);
            adjustFactorData.setValue(a.getAdjustFactor());
            adjustFactorDataList.add(adjustFactorData);


            ChartDataResp adjustValueData = new ChartDataResp();
            adjustValueData.setTime(dateTime);
            adjustValueData.setValue(a.getAdjustValue());
            adjustValueDataList.add(adjustValueData);


            ChartDataResp applyPowerData = new ChartDataResp();
            applyPowerData.setTime(dateTime);
            applyPowerData.setValue(a.getApplyPower());
            applyPowerDataList.add(applyPowerData);


            ChartDataResp applyPriceData = new ChartDataResp();
            applyPriceData.setTime(dateTime);
            applyPriceData.setValue(a.getApplyPrice());
            applyPriceDataList.add(applyPriceData);


        });

        AggregatorApplyPlan aggregatorApplyPlan = new AggregatorApplyPlan();
        BeanUtils.copyProperties(addPlanReq,aggregatorApplyPlan);
        aggregatorApplyPlan.setReferDatePower(JSONObject.toJSONString(referDatePowerDataList));
        aggregatorApplyPlan.setAdjustFactor(JSONObject.toJSONString(adjustFactorDataList));
        aggregatorApplyPlan.setAdjustValue(JSONObject.toJSONString(adjustValueDataList));
        aggregatorApplyPlan.setApplyPower(JSONObject.toJSONString(applyPowerDataList));
        aggregatorApplyPlan.setApplyPrice(JSONObject.toJSONString(applyPriceDataList));

        if(StringUtil.isNotEmpty(addPlanReq.getId())){
            aggregatorApplyPlan.setId(Integer.valueOf(addPlanReq.getId()));
            aggregatorApplyPlanMapper.updateByPrimaryKeySelective(aggregatorApplyPlan);
        }else {
            aggregatorApplyPlanMapper.insertSelective(aggregatorApplyPlan);
        }
        //新增/修改调度申报功率曲线表数据
        LocalDate startDate = LocalDate.parse(addPlanReq.getStartDate(), formatter);
        LocalDate endDate = LocalDate.parse(addPlanReq.getEndDate(), formatter);
        long days = Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toDays();

        List<String> dateList = new ArrayList<>();
        List<AggregatorDateDeliveryChart> aggregatorDateDeliveryChartList = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for(int i=0;i<days+1;i++){
            AggregatorDateDeliveryChart aggregatorDateDeliveryChart = new AggregatorDateDeliveryChart();
            LocalDate date = startDate.plusDays(i);
            String formatDate = date.format(formatter);
            List<DataResp> deliveryChart = applyPowerDataList.stream().map(a -> {
                DataResp dataResp = new DataResp();
                if(a.getTime().equals("24:00")){
                    dataResp.setTime(LocalDateTime.of(date.plusDays(1),LocalTime.MIN).format(dateTimeFormatter));
                }else {
                    dataResp.setTime(formatDate + " " + a.getTime()+":00");
                }

                dataResp.setValue(StringUtil.isNotEmpty(a.getValue()) ? Double.valueOf(a.getValue()) : Double.valueOf("0"));
                return dataResp;
            }).collect(Collectors.toList());
            aggregatorDateDeliveryChart.setAggregatorId(aggregatorId);
            aggregatorDateDeliveryChart.setDate(formatDate);
            aggregatorDateDeliveryChart.setResourceTypeId(addPlanReq.getSourceId());
            aggregatorDateDeliveryChart.setDeliveryChart(JSONObject.toJSONString(deliveryChart));
            dateList.add(formatDate);
            aggregatorDateDeliveryChartList.add(aggregatorDateDeliveryChart);
        }
        if(StringUtil.isNotEmpty(addPlanReq.getId())){
            //判断入参和计划中的时间哪个更靠后
            LocalDate  deleteEndDate =null;
            LocalDate  deleteStartDate =reqStartDate;
            LocalDate beforeEndDate = LocalDate.parse(beforeAggregatorApplyPlan.getEndDate(), formatter);
           // LocalDate beforeStartDate = LocalDate.parse(beforeAggregatorApplyPlan.getStartDate(), formatter);
            if(beforeEndDate.isAfter(reqEndDate) || beforeEndDate.isEqual(reqEndDate)){
                deleteEndDate = beforeEndDate;
            }else {
                deleteEndDate = reqEndDate;
            }
            LocalDate tomorrowDate = LocalDate.now().plusDays(1);
            //判断明天时间是否已经直接执行
            boolean checkDateAutoApply = aggregatorDateApplyDetailService.checkDateAutoApply(aggregatorId, tomorrowDate.format(formatter));
            //明天的计划如果已经申报 从后天开始删除
            if(checkDateAutoApply){
                //如果明天已经申报，并且是节假日前的最后一天
                List<String> holidayDateList = aggregatorDateHolidayService.getApplyDateList(tomorrowDate.format(formatter), false);
                LocalDate parse = LocalDate.parse(holidayDateList.get(holidayDateList.size() - 1), formatter);
                if(parse.isAfter(tomorrowDate.plusDays(1))){
                    deleteStartDate = parse.plusDays(1);
                }else {
                    deleteStartDate=tomorrowDate.plusDays(1);
                }
            }
            dateList.clear();
            while (!deleteStartDate.isAfter(deleteEndDate)){
                dateList.add(deleteStartDate.format(formatter));
                deleteStartDate=deleteStartDate.plusDays(1);
            }
        }
        log.info("删除调度申报功率曲线参数dateList={}",JSONObject.toJSONString(dateList));
        if(CollectionUtil.isNotEmpty(dateList)){
            aggregatorDateDeliveryChartService.delete(aggregatorId,dateList,sourceId);
        }

            aggregatorDateDeliveryChartList=aggregatorDateDeliveryChartList.stream().filter(a->dateList.contains(a.getDate())).collect(Collectors.toList());

        log.info("新增调度申报功率曲线参数aggregatorDateDeliveryChartList={}",JSONObject.toJSONString(aggregatorDateDeliveryChartList));
        if(CollectionUtil.isNotEmpty(aggregatorDateDeliveryChartList)){
            aggregatorDateDeliveryChartService.batchInsert(aggregatorDateDeliveryChartList);
        }
        List<AggregatorDateIssueChart> issueChartList = aggregatorDateDeliveryChartList.stream().map(a -> {
            AggregatorDateIssueChart aggregatorDateIssueChart = new AggregatorDateIssueChart();
            BeanUtils.copyProperties(a, aggregatorDateIssueChart);
            aggregatorDateIssueChart.setIssueChart(a.getDeliveryChart());
            return aggregatorDateIssueChart;
        }).collect(Collectors.toList());

        log.info("删除调度下发功率曲线参数dateList={}",JSONObject.toJSONString(dateList));
        log.info("新增调度下发功率曲线参数issueChartList={}",JSONObject.toJSONString(issueChartList));
        if(CollectionUtil.isNotEmpty(dateList)){
            aggregatorDateIssueChartService.delete(aggregatorId,dateList,sourceId);
        }
        if(CollectionUtil.isNotEmpty(issueChartList)){
            aggregatorDateIssueChartService.batchInsert(issueChartList);
        }


        return true;
    }

    @Override
    public AggregatorApplyPlan getPlan(String aggregatorId, String sourceId, String date) {
        AggregatorApplyPlan aggregatorApplyPlan=null;
        Weekend<AggregatorApplyPlan> weekend = Weekend.of(AggregatorApplyPlan.class);
        WeekendCriteria<AggregatorApplyPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorApplyPlan::getAggregatorId, aggregatorId)
                 .andEqualTo(AggregatorApplyPlan::getSourceId,sourceId);
        List<AggregatorApplyPlan> planList = aggregatorApplyPlanMapper.selectByExample(weekend);
        if(CollectionUtil.isNotEmpty(planList)){
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate dateFormat= LocalDate.parse(date, dateTimeFormatter);
            planList =  planList.stream().filter(a-> LocalDate.parse(a.getStartDate(), dateTimeFormatter).isEqual(dateFormat)
                                       || LocalDate.parse(a.getEndDate(), dateTimeFormatter).isEqual(dateFormat)
                         || dateFormat.isAfter( LocalDate.parse(a.getStartDate(), dateTimeFormatter))&& dateFormat.isBefore(LocalDate.parse(a.getEndDate(), dateTimeFormatter)))
                    .collect(Collectors.toList());
            if(CollectionUtil.isNotEmpty(planList)){
                aggregatorApplyPlan =  planList.get(0);
            }
        }

        return aggregatorApplyPlan;
    }

    private Boolean checkRepeatPlan(LocalDate reqBeginDate,LocalDate reqEndDate,LocalDate beginDate,LocalDate endDate){
        Boolean check = Boolean.FALSE;
        if(reqBeginDate.isEqual(beginDate) || reqBeginDate.isEqual(endDate) || reqEndDate.isEqual(beginDate) || reqEndDate.isEqual(endDate)
          || reqEndDate.isAfter(beginDate)&& reqEndDate.isBefore(endDate) || reqBeginDate.isAfter(beginDate)&& reqBeginDate.isBefore(endDate)
           || beginDate.isAfter(reqBeginDate) && beginDate.isBefore(reqEndDate) &&  endDate.isAfter(reqBeginDate) && endDate.isBefore(reqEndDate)){
            check = Boolean.TRUE;
        }
        return check;
    }

    /**
     * @description 0:已过期 1:待开始 2:执行中 获取计划状态
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private String getPlanStatus(LocalDate todayDate,LocalDate beginDate,LocalDate endDate){
        String planStatus=null;
        //未开始
        if(todayDate.isBefore(beginDate)){
            planStatus="1";
        //进行中
        }else if (todayDate.isEqual(beginDate) || todayDate.isAfter(beginDate) && todayDate.isBefore(endDate) || todayDate.isEqual(endDate)){
            planStatus="2";
        //已过期
        }else if(todayDate.isAfter(endDate)){
            planStatus="0";
        }
        return planStatus;
    }
}
