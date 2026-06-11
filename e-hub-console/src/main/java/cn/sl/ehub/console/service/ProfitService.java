package cn.sl.ehub.console.service;

import cn.sl.ehub.console.enums.AggregatorProfitTypeEnum;
import cn.sl.ehub.console.enums.OrderTypeEnum;
import cn.sl.ehub.console.enums.ProfitDownloadDateTypeEnum;
import cn.sl.ehub.service.mapper.AggregatorDateProfitMapper;
import cn.sl.ehub.service.mapper.AggregatorEntDateProfitMapper;
import cn.sl.ehub.service.mapper.AggregatorEntMapper;
import cn.sl.ehub.console.model.vo.DateProfitWithEntDetailExcelData;
import cn.sl.ehub.console.model.vo.EntDateProfitExcelData;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.model.vo.ProfitExcelData;
import cn.sl.ehub.service.resp.AggregatorDateProfitResp;
import cn.sl.ehub.service.resp.AggregatorEntDateProfitResp;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.service.vo.AggregatorDateProfit;
import cn.sl.ehub.service.vo.AggregatorEnt;
import cn.sl.ehub.service.vo.AggregatorEntDateProfit;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Service
public class ProfitService {

    private final AggregatorDateProfitMapper aggregatorDateProfitMapper;
    private final AggregatorEntDateProfitMapper aggregatorEntDateProfitMapper;
    private final AggregatorEntMapper aggregatorEntMapper;

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");

    /**
     * 查询本月聚合商收益信息
     *
     * @param aggregatorId
     * @return
     */
    public List<AggregatorDateProfit> getAggregatorProfit(String aggregatorId, String startDate, String endDate) {
        List<AggregatorDateProfit> aggregatorDateProfitList = Lists.newArrayList();
        List<String> dateList = DateUtils.getDayList(startDate, endDate);
        Weekend<AggregatorDateProfit> weekend = Weekend.of(AggregatorDateProfit.class);
        WeekendCriteria<AggregatorDateProfit, Object> weekendCriteria = weekend.weekendCriteria();
        weekendCriteria.andEqualTo(AggregatorDateProfit::getAggregatorId, aggregatorId);
        weekendCriteria.andGreaterThanOrEqualTo(AggregatorDateProfit::getDate, startDate);
        weekendCriteria.andLessThanOrEqualTo(AggregatorDateProfit::getDate, endDate);
        weekend.orderBy("date").asc();
        List<AggregatorDateProfit> profitList = aggregatorDateProfitMapper.selectByExample(weekend);
        Map<String, AggregatorDateProfit> dateMap = new HashMap<>();
        if (null != profitList && profitList.size() > 0) {
            Map<String, AggregatorDateProfit> dateValueMap = profitList.stream().collect(toMap(AggregatorDateProfit::getDate, Function.identity(), (v1, v2) -> v1));
            dateMap.putAll(dateValueMap);
        }
        dateList.forEach(date -> {
            AggregatorDateProfit aggregatorDateProfit = dateMap.get(date);
            if (null == aggregatorDateProfit) {
                aggregatorDateProfit = new AggregatorDateProfit();
                aggregatorDateProfit.setAggregatorId(aggregatorId);
                aggregatorDateProfit.setDate(date);
                aggregatorDateProfit.setIssueProfit(0D);
                aggregatorDateProfit.setEntProfit(0D);
                aggregatorDateProfit.setAggregatorProfit(0D);
            }
            aggregatorDateProfit.setIssueProfit(MathUtils.doublePointNotRounding(aggregatorDateProfit.getIssueProfit(), 2));
            aggregatorDateProfit.setEntProfit(MathUtils.doublePointNotRounding(aggregatorDateProfit.getEntProfit(), 2));
            aggregatorDateProfit.setAggregatorProfit(MathUtils.doublePointNotRounding(aggregatorDateProfit.getAggregatorProfit(), 2));
            aggregatorDateProfit.setElectricQuantity(MathUtils.doublePointNotRounding(aggregatorDateProfit.getElectricQuantity(), 2));
            aggregatorDateProfitList.add(aggregatorDateProfit);
        });
        return aggregatorDateProfitList;
    }

    /**
     * 筛选内容列表
     *
     * @param aggregatorId
     * @return
     */
    public List<AggregatorEnt> getAggregatorEntList(String aggregatorId) {
        List<AggregatorEnt> resultList = Lists.newArrayList();
        //下发金额
        AggregatorEnt issueProfit = new AggregatorEnt();
        issueProfit.setEntId(AggregatorProfitTypeEnum.ISSUE.getCode());
        issueProfit.setEntName(AggregatorProfitTypeEnum.ISSUE.getDesc());
        resultList.add(issueProfit);
        //聚合商金额
        AggregatorEnt aggregatorProfit = new AggregatorEnt();
        aggregatorProfit.setEntId(AggregatorProfitTypeEnum.AGGREGATOR.getCode());
        aggregatorProfit.setEntName(AggregatorProfitTypeEnum.AGGREGATOR.getDesc());
        resultList.add(aggregatorProfit);
        //企业用户金额
        AggregatorEnt entProfit = new AggregatorEnt();
        entProfit.setEntId(AggregatorProfitTypeEnum.ENT.getCode());
        entProfit.setEntName(AggregatorProfitTypeEnum.ENT.getDesc());
        resultList.add(entProfit);
        Weekend<AggregatorEnt> weekend = Weekend.of(AggregatorEnt.class);
        WeekendCriteria<AggregatorEnt, Object> weekendCriteria = weekend.weekendCriteria();
        weekendCriteria.andEqualTo(AggregatorEnt::getAggregatorId, aggregatorId);
        List<AggregatorEnt> aggregatorEntList = aggregatorEntMapper.selectByExample(weekend);
        if (null != aggregatorEntList && aggregatorEntList.size() > 0) {
            resultList.addAll(aggregatorEntList);
        }
        return resultList;
    }

    /**
     * 查询近一周每日聚合商收益信息
     *
     * @param aggregatorId 聚合商ID
     */
    public List<AggregatorDateProfitResp> getTheLastWeekProfit(String aggregatorId) {
        DateTime dateTime = new DateTime();
        return this.getProfitPageResult(aggregatorId,
                dateTime.minusDays(7).millisOfDay().withMinimumValue().toDate(),
                dateTime.minusDays(1).millisOfDay().withMaximumValue().toDate(),
                null, null, OrderTypeEnum.ASC, null, false).getList();
    }

    public List<ProfitExcelData> getProfitExcelData(String aggregatorId, Integer dateType) {
        List<AggregatorDateProfitResp> data = new ArrayList<>();
        DateTime dateTime = new DateTime();
        if (ProfitDownloadDateTypeEnum.CURRENT_MONTH.getCode().equals(dateType)) {
            data = this.getProfitPageResult(aggregatorId,
                    dateTime.dayOfMonth().withMinimumValue().millisOfDay().withMinimumValue().toDate(),
                    dateTime.dayOfMonth().withMaximumValue().millisOfDay().withMaximumValue().toDate(),
                    null, null, OrderTypeEnum.ASC, null, false).getList();
        } else if (ProfitDownloadDateTypeEnum.PREVIOUS_MONTH.getCode().equals(dateType)) {
            data = this.getProfitPageResult(aggregatorId,
                    dateTime.minusMonths(1).dayOfMonth().withMinimumValue().millisOfDay().withMinimumValue().toDate(),
                    dateTime.minusMonths(1).dayOfMonth().withMaximumValue().millisOfDay().withMaximumValue().toDate(),
                    null, null, OrderTypeEnum.ASC, null, false).getList();
        }
        if (CollectionUtils.isEmpty(data)) {
            return null;
        }
        return ProfitExcelData.transform(data);
    }

    public PageResultVO<AggregatorDateProfitResp> getProfitPageResult(String aggregatorId, Date startDate, Date endDate,
                                                                      Integer pageIndex, Integer pageSize,
                                                                      OrderTypeEnum orderType, List<String> entIds,
                                                                      Boolean needEntDetail) {
        PageResultVO<AggregatorDateProfitResp> pageResultVO = new PageResultVO<>();
        // 若筛选参数有企业ID，需查询出所有包含该企业的日期，以便后续查询聚合商日收益列表使用。待优化
        boolean filterDate = false;
        List<Date> filterDateList = null;
        if (CollectionUtils.isNotEmpty(entIds)) {
            filterDateList = aggregatorEntDateProfitMapper.selectDateListByEntIds(aggregatorId, entIds);
            filterDate = true;
        }
        if (filterDate && CollectionUtils.isEmpty(filterDateList)) {
            return pageResultVO;
        }
        Weekend<AggregatorDateProfit> weekend = Weekend.of(AggregatorDateProfit.class);
        WeekendCriteria<AggregatorDateProfit, Object> weekendCriteria = weekend.weekendCriteria();
        if (StringUtils.isNotBlank(aggregatorId)) {
            weekendCriteria.andEqualTo(AggregatorDateProfit::getAggregatorId, aggregatorId);
        }
        if (startDate != null) {
            weekendCriteria.andGreaterThanOrEqualTo(AggregatorDateProfit::getDate, startDate);
        }
        if (endDate != null) {
            weekendCriteria.andLessThanOrEqualTo(AggregatorDateProfit::getDate, endDate);
        }
        if (CollectionUtils.isNotEmpty(filterDateList)) {
            weekendCriteria.andIn(AggregatorDateProfit::getDate, filterDateList);
        }
        if (orderType == null || OrderTypeEnum.ASC.equals(orderType)) {
            weekend.orderBy("date").asc();
        } else {
            weekend.orderBy("date").desc();
        }
        boolean page = false;
        if (pageIndex != null && pageSize != null) {
            page = true;
            PageHelper.startPage(pageIndex, pageSize);
        }
        List<AggregatorDateProfit> aggregatorDateProfitList = aggregatorDateProfitMapper.selectByExample(weekend);
        if (CollectionUtils.isEmpty(aggregatorDateProfitList)) {
            pageResultVO.setList(new ArrayList<>());
            return pageResultVO;
        }
        if (page) {
            pageResultVO.setTotal((int) ((Page<AggregatorDateProfit>) aggregatorDateProfitList).getTotal());
            pageResultVO.setPageIndex(pageIndex);
            pageResultVO.setPageSize(pageSize);
        }
        pageResultVO.setList(aggregatorDateProfitList.stream()
                .map(e -> transformAggregatorDateProfitToRespVO(e, entIds, needEntDetail))
                .collect(Collectors.toList()));
        return pageResultVO;
    }

    public PageResultVO<AggregatorEntDateProfitResp> getEntProfitPageResult(String aggregatorId, String date,
                                                                            Integer pageIndex, Integer pageSize,
                                                                            List<String> entIds) {
        PageResultVO<AggregatorEntDateProfitResp> pageResultVO = new PageResultVO<>();
        boolean page = false;
        if (pageIndex != null && pageSize != null) {
            page = true;
            PageHelper.startPage(pageIndex, pageSize);
        }
        List<AggregatorEntDateProfitResp> list = aggregatorEntDateProfitMapper.selectRespListByParam(aggregatorId,
                date, entIds);
        if (CollectionUtils.isEmpty(list)) {
            pageResultVO.setList(new ArrayList<>());
            return pageResultVO;
        }
        if (page) {
            pageResultVO.setTotal((int) ((Page<AggregatorEntDateProfitResp>) list).getTotal());
            pageResultVO.setPageIndex(pageIndex);
            pageResultVO.setPageSize(pageSize);
        }
        pageResultVO.setList(list);
        return pageResultVO;
    }

    private AggregatorDateProfitResp transformAggregatorDateProfitToRespVO(AggregatorDateProfit aggregatorDateProfit,
                                                                           List<String> entIds, Boolean needEntDetail) {
        if (aggregatorDateProfit == null) {
            return null;
        }
        AggregatorDateProfitResp aggregatorDateProfitResp = new AggregatorDateProfitResp();
        aggregatorDateProfitResp.setAggregatorId(aggregatorDateProfit.getAggregatorId());
        aggregatorDateProfitResp.setDate(new DateTime(aggregatorDateProfit.getDate()).toString(DATE_FORMATTER));
        aggregatorDateProfitResp.setIssueProfit(MathUtils.doublePointNotRounding(aggregatorDateProfit.getIssueProfit(), 2));
        aggregatorDateProfitResp.setAggregatorProfit(MathUtils.doublePointNotRounding(aggregatorDateProfit.getAggregatorProfit(), 2));
        aggregatorDateProfitResp.setEntProfit(MathUtils.doublePointNotRounding(aggregatorDateProfit.getEntProfit(), 2));
        if (needEntDetail != null && needEntDetail) {
            List<AggregatorEntDateProfitResp> entDateProfitRespList = aggregatorEntDateProfitMapper.selectRespListByParam(
                    aggregatorDateProfit.getAggregatorId(), aggregatorDateProfit.getDate(), entIds);
            if (CollectionUtils.isNotEmpty(entDateProfitRespList)) {
                entDateProfitRespList.forEach(resp -> {
                    resp.setEntProfit(MathUtils.doublePointNotRounding(resp.getEntProfit(), 2));
                    resp.setTotalProfit(MathUtils.doublePointNotRounding(resp.getTotalProfit(), 2));
                });
            }
            aggregatorDateProfitResp.setEntDateProfitRespList(entDateProfitRespList);
        }
        return aggregatorDateProfitResp;
    }

    private AggregatorEntDateProfitResp transformAggregatorEntDateProfitToRespVO(AggregatorEntDateProfit profit) {
        if (profit == null) {
            return null;
        }
        AggregatorEntDateProfitResp resp = new AggregatorEntDateProfitResp();
        resp.setAggregatorId(profit.getAggregatorId());
        resp.setEntId(profit.getEntId());
        resp.setDate(new DateTime(profit.getDate()).toString(DATE_FORMATTER));
        resp.setEntProfit(profit.getEntProfit());
        return resp;
    }

    public List<DateProfitWithEntDetailExcelData> getDateProfitWithEntDetailExcelData(String aggregatorId, Date startDate, Date endDate, List<String> entIds) {
        List<AggregatorDateProfitResp> data = aggregatorDateProfitMapper.selectDateProfitWithEntDetail(
                aggregatorId, startDate, endDate, entIds);
        if (CollectionUtils.isEmpty(data)) {
            return null;
        }
        List<DateProfitWithEntDetailExcelData> dateProfitExcelDataList = new ArrayList<>();
        for (AggregatorDateProfitResp datum : data) {
            DateProfitWithEntDetailExcelData dateProfitExcelData = new DateProfitWithEntDetailExcelData();
            BeanUtils.copyProperties(datum, dateProfitExcelData);
            List<AggregatorEntDateProfitResp> entDateProfitList = datum.getEntDateProfitRespList();
            if (CollectionUtils.isEmpty(entDateProfitList)) {
                dateProfitExcelDataList.add(dateProfitExcelData);
                continue;
            }
            List<EntDateProfitExcelData> entDateProfitExcelDataList = new ArrayList<>();
            for (AggregatorEntDateProfitResp entDateProfit : entDateProfitList) {
                EntDateProfitExcelData entDateProfitExcelData = new EntDateProfitExcelData();
                BeanUtils.copyProperties(entDateProfit, entDateProfitExcelData);
                entDateProfitExcelDataList.add(entDateProfitExcelData);
            }
            dateProfitExcelData.setEntDateProfitList(entDateProfitExcelDataList);
            dateProfitExcelDataList.add(dateProfitExcelData);
        }
        return dateProfitExcelDataList;
    }

    /**
     * 收益统计日收益列表
     *
     * @param aggregatorId
     * @param startDate
     * @param endDate
     * @param pageIndex
     * @param pageSize
     * @param entIds
     * @return
     */
    public PageResultVO<List<String>> getProfitPageResult(String aggregatorId, String startDate, String endDate, Integer pageIndex, Integer pageSize, List<String> entIds) {
        PageResultVO<List<String>> pageResultVO = new PageResultVO<>();
        List<String> dayList = DateUtils.getDayList(startDate, endDate);
        if (null == dayList || dayList.size() <= 0) {
            pageResultVO.setTotal(0);
            pageResultVO.setPageSize(pageSize);
            pageResultVO.setPageIndex(pageIndex);
            return pageResultVO;
        }
        //分页
        Integer pageStart = 0;
        Integer pageEnd = dayList.size() - 1;
        if (null != pageIndex && null != pageSize) {
            pageResultVO.setTotal(dayList.size());
            pageResultVO.setPageSize(pageSize);
            Integer pageIndexMax = (int) Math.ceil(dayList.size() / Double.parseDouble(pageSize.toString()));
            if (pageIndex > pageIndexMax) {
                pageIndex = pageIndexMax;
            }
            pageResultVO.setPageIndex(pageIndex);
            pageStart = (pageIndex - 1) * pageSize;
            pageEnd = pageStart + pageSize - 1;
            if (pageStart >= dayList.size()) {
                pageStart = dayList.size() - 1;
            }
            if (pageEnd >= dayList.size()) {
                pageEnd = dayList.size() - 1;
            }
        }
        String startDay = dayList.get(pageStart);
        String endDay = dayList.get(pageEnd);
        //聚合商收益
        Map<String, Map<String, Double>> dateAggregatorProfitMap = new HashMap<>();
        if (entIds.contains(AggregatorProfitTypeEnum.ISSUE.getCode()) || entIds.contains(AggregatorProfitTypeEnum.AGGREGATOR.getCode()) || entIds.contains(AggregatorProfitTypeEnum.ENT.getCode())) {
            List<AggregatorDateProfit> aggregatorDateProfitList = getAggregatorProfit(aggregatorId, startDay, endDay);
            if (null != aggregatorDateProfitList && aggregatorDateProfitList.size() > 0) {
                aggregatorDateProfitList.forEach(profit -> {
                    Map<String, Double> aggregatorProfitMap = new HashMap<>();
                    aggregatorProfitMap.put(AggregatorProfitTypeEnum.ISSUE.getCode(), profit.getIssueProfit());
                    aggregatorProfitMap.put(AggregatorProfitTypeEnum.AGGREGATOR.getCode(), profit.getAggregatorProfit());
                    aggregatorProfitMap.put(AggregatorProfitTypeEnum.ENT.getCode(), profit.getEntProfit());
                    dateAggregatorProfitMap.put(profit.getDate(), aggregatorProfitMap);
                });
            }
        }
        //企业收益
        Map<String, Map<String, Double>> dateEntProfitMap = new HashMap<>();
        List<String> entIdList = entIds.stream().filter(entId ->
                !entId.equals(AggregatorProfitTypeEnum.ISSUE.getCode())
                        && !entId.equals(AggregatorProfitTypeEnum.AGGREGATOR.getCode())
                        && !entId.equals(AggregatorProfitTypeEnum.ENT.getCode())).collect(Collectors.toList());
        if (null != entIdList && entIdList.size() > 0) {
            List<AggregatorEntDateProfit> aggregatorEntDateProfitList = getAggregatorEntDateProfitList(aggregatorId, startDay, endDay, entIdList);
            if (null != aggregatorEntDateProfitList && aggregatorEntDateProfitList.size() > 0) {
                dateEntProfitMap = aggregatorEntDateProfitList.stream().collect(groupingBy(AggregatorEntDateProfit::getDate, toMap(AggregatorEntDateProfit::getEntId, AggregatorEntDateProfit::getEntProfit, (v1, v2) -> v1)));
            }
        }
        if (null != dateEntProfitMap && dateEntProfitMap.size() > 0) {
            dateEntProfitMap.entrySet().forEach(entEntry -> {
                Map<String, Double> entProfitMap = dateAggregatorProfitMap.get(entEntry.getKey());
                if (null == entProfitMap) {
                    dateAggregatorProfitMap.put(entEntry.getKey(), entEntry.getValue());
                } else if (null != entEntry.getValue()) {
                    entProfitMap.putAll(entEntry.getValue());
                }
            });
        }
        List<List<String>> respList = Lists.newArrayList();
        for (int i = pageStart; i <= pageEnd; i++) {
            String date = dayList.get(i);
            List<String> resultList = Lists.newArrayList();
            resultList.add(date);
            Map<String, Double> entProfitMap = dateAggregatorProfitMap.get(date);
            if (null == entProfitMap) {
                entProfitMap = new HashMap<>();
            }
            Map<String, Double> finalEntProfitMap = entProfitMap;
            entIds.forEach(entId -> {
                resultList.add(null == finalEntProfitMap.get(entId) ? null : MathUtils.doublePointNotRounding(finalEntProfitMap.get(entId), 2).toString());
            });
            respList.add(resultList);
        }
        pageResultVO.setList(respList);
        return pageResultVO;
    }

    /**
     * 查询企业收益
     *
     * @param aggregatorId
     * @param startDate
     * @param endDate
     * @param entIdList
     * @return
     */
    public List<AggregatorEntDateProfit> getAggregatorEntDateProfitList(String aggregatorId, String startDate, String endDate, List<String> entIdList) {
        Weekend<AggregatorEntDateProfit> weekend = Weekend.of(AggregatorEntDateProfit.class);
        WeekendCriteria<AggregatorEntDateProfit, Object> weekendCriteria = weekend.weekendCriteria();
        weekendCriteria.andEqualTo(AggregatorEntDateProfit::getAggregatorId, aggregatorId);
        weekendCriteria.andGreaterThanOrEqualTo(AggregatorEntDateProfit::getDate, startDate);
        weekendCriteria.andLessThanOrEqualTo(AggregatorEntDateProfit::getDate, endDate);
        weekendCriteria.andIn(AggregatorEntDateProfit::getEntId, entIdList);
        weekend.orderBy("date").asc();
        return aggregatorEntDateProfitMapper.selectByExample(weekend);
    }

    /**
     * 日收益列表(有筛选条件)--导出
     *
     * @param aggregatorId
     * @param startDate
     * @param endDate
     * @param entIdList
     * @return
     */
    public List<Map<String, Object>> listByEntIdListExcel(String aggregatorId, String startDate, String endDate, List<String> entIdList) {
        List<Map<String, Object>> dataList = Lists.newArrayList();
        List<String> dateList = DateUtils.getDayList(startDate, endDate);
        Map<String, Map<String, Double>> dateProfitMap = new HashMap<>();
        //聚合商收益
        if (entIdList.contains(AggregatorProfitTypeEnum.ISSUE.getCode()) || entIdList.contains(AggregatorProfitTypeEnum.AGGREGATOR.getCode()) || entIdList.contains(AggregatorProfitTypeEnum.ENT.getCode())) {
            List<AggregatorDateProfit> aggregatorDateProfitList = getAggregatorProfit(aggregatorId, startDate, endDate);
            if (null != aggregatorDateProfitList && aggregatorDateProfitList.size() > 0) {
                aggregatorDateProfitList.forEach(profit -> {
                    Map<String, Double> aggregatorProfitMap = new HashMap<>();
                    aggregatorProfitMap.put(AggregatorProfitTypeEnum.ISSUE.getCode(), profit.getIssueProfit());
                    aggregatorProfitMap.put(AggregatorProfitTypeEnum.AGGREGATOR.getCode(), profit.getAggregatorProfit());
                    aggregatorProfitMap.put(AggregatorProfitTypeEnum.ENT.getCode(), profit.getEntProfit());
                    dateProfitMap.put(profit.getDate(), aggregatorProfitMap);
                });
            }
        }
        //企业收益
        List<AggregatorEntDateProfit> aggregatorEntDateProfitList = getAggregatorEntDateProfitList(aggregatorId, startDate, endDate, entIdList);
        if (null != aggregatorEntDateProfitList && aggregatorEntDateProfitList.size() > 0) {
            Map<String, Map<String, Double>> dateEntProfitMap = aggregatorEntDateProfitList.stream().collect(Collectors.groupingBy(AggregatorEntDateProfit::getDate, Collectors.toMap(AggregatorEntDateProfit::getEntId, AggregatorEntDateProfit::getEntProfit, (v1, v2) -> v1)));
            if (null != dateEntProfitMap && dateEntProfitMap.size() > 0) {
                dateEntProfitMap.entrySet().forEach(dateEntProfitMapEntry -> {
                    Map<String, Double> totalEntProfitMap = new HashMap<>();
                    Map<String, Double> entProfitMap = dateProfitMap.get(dateEntProfitMapEntry.getKey());
                    if (null != entProfitMap && entProfitMap.size() > 0) {
                        totalEntProfitMap.putAll(entProfitMap);
                    }
                    if (null != dateEntProfitMapEntry.getValue() && dateEntProfitMapEntry.getValue().size() > 0) {
                        totalEntProfitMap.putAll(dateEntProfitMapEntry.getValue());
                    }
                    dateEntProfitMapEntry.setValue(totalEntProfitMap);
                });
                dateProfitMap.putAll(dateEntProfitMap);
            }
        }
        dateList.forEach(date -> {
            AtomicBoolean flag = new AtomicBoolean(false);
            Map<String, Object> valueMap = new HashMap<>();
            valueMap.put("date", date);
            Map<String, Double> profitMap = null == dateProfitMap.get(date) ? new HashMap<>() : dateProfitMap.get(date);
            entIdList.forEach(entId -> {
                valueMap.put(entId, "");
                Double profit = profitMap.get(entId);
                if (null != profit) {
                    valueMap.put(entId, profit);
                    flag.set(true);
                }
            });
            if (flag.get()) {
                dataList.add(valueMap);
            }
        });
        return dataList;
    }
}
