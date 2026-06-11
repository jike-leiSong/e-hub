package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorEntDateProfitMapper;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.service.resp.ProfitBillDateResp;
import cn.sl.ehub.service.resp.ProfitBillDetailDateResp;
import cn.sl.ehub.console.service.IProfitBillService;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.service.vo.AggregatorEntDateProfit;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 收益账单ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class ProfitBillServiceImpl implements IProfitBillService {

    private final AggregatorEntDateProfitMapper aggregatorEntDateProfitMapper;

    @Override
    public Double getTotalProfit(String entId, String startDate, String endDate) {
        return aggregatorEntDateProfitMapper.getTotalProfit(entId, startDate, endDate);
    }

    @Override
    public PageResultVO<ProfitBillDetailDateResp> getProfitBill(String entId, String startDate, String endDate, Integer pageIndex, Integer pageSize) {
        PageResultVO<ProfitBillDetailDateResp> pageResultVO = new PageResultVO<>();
        List<String> dayList = DateUtils.getDayList(startDate, endDate, "DESC");
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
        String endDay = dayList.get(pageStart);
        String startDay = dayList.get(pageEnd);
        Weekend<AggregatorEntDateProfit> weekend = Weekend.of(AggregatorEntDateProfit.class);
        WeekendCriteria<AggregatorEntDateProfit, Object> criteriaEnt = weekend.weekendCriteria();
        criteriaEnt.andEqualTo(AggregatorEntDateProfit::getEntId, entId);
        criteriaEnt.andBetween(AggregatorEntDateProfit::getDate, startDay, endDay);
        weekend.orderBy("date").desc();
        List<AggregatorEntDateProfit> aggregatorEntDateProfitList = aggregatorEntDateProfitMapper.selectByExample(weekend);
        if (null != aggregatorEntDateProfitList && aggregatorEntDateProfitList.size() > 0) {
            List<ProfitBillDetailDateResp> profitBillDetailDateRespList = Lists.newArrayList();
            aggregatorEntDateProfitList.forEach(aggregatorEntDateProfit -> {
                ProfitBillDetailDateResp profitBillDetailDateResp = new ProfitBillDetailDateResp();
                profitBillDetailDateResp.setDate(aggregatorEntDateProfit.getDate());
                profitBillDetailDateResp.setProfit(aggregatorEntDateProfit.getEntProfit());
                profitBillDetailDateResp.setElectricQuantity(100D);
                profitBillDetailDateResp.setPrice(100D);
                profitBillDetailDateResp.setMoney(100D);
                profitBillDetailDateRespList.add(profitBillDetailDateResp);
            });
            pageResultVO.setList(profitBillDetailDateRespList);
        }
        return pageResultVO;
    }

    @Override
    public List<ProfitBillDetailDateResp> getProfitBill(String entId, String startDate, String endDate) {
        List<ProfitBillDetailDateResp> profitBillDetailDateRespList = Lists.newArrayList();
        List<String> dayList = DateUtils.getDayList(startDate, endDate);
        if (null == dayList || dayList.size() <= 0) {
            return profitBillDetailDateRespList;
        }
        Weekend<AggregatorEntDateProfit> weekend = Weekend.of(AggregatorEntDateProfit.class);
        WeekendCriteria<AggregatorEntDateProfit, Object> criteriaEnt = weekend.weekendCriteria();
        criteriaEnt.andEqualTo(AggregatorEntDateProfit::getEntId, entId);
        criteriaEnt.andBetween(AggregatorEntDateProfit::getDate, startDate, endDate);
        weekend.orderBy("date").desc();
        List<AggregatorEntDateProfit> aggregatorEntDateProfitList = aggregatorEntDateProfitMapper.selectByExample(weekend);
        if (null != aggregatorEntDateProfitList && aggregatorEntDateProfitList.size() > 0) {
            aggregatorEntDateProfitList.forEach(aggregatorEntDateProfit -> {
                ProfitBillDetailDateResp profitBillDetailDateResp = new ProfitBillDetailDateResp();
                profitBillDetailDateResp.setDate(aggregatorEntDateProfit.getDate());
                profitBillDetailDateResp.setProfit(aggregatorEntDateProfit.getEntProfit());
                profitBillDetailDateResp.setElectricQuantity(aggregatorEntDateProfit.getElectricQuantity());
                profitBillDetailDateResp.setPrice(aggregatorEntDateProfit.getCountPrice());
                profitBillDetailDateResp.setMoney(aggregatorEntDateProfit.getCountProfit());
                profitBillDetailDateRespList.add(profitBillDetailDateResp);
            });
        }
        return profitBillDetailDateRespList;
    }
}
