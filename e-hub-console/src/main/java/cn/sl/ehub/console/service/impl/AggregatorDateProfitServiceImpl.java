package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorDateProfitMapper;
import cn.sl.ehub.service.resp.AggregatorProfitResp;
import cn.sl.ehub.console.service.IAggregatorDateProfitService;
import cn.sl.ehub.service.vo.AggregatorDateProfit;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * 聚合商收益ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorDateProfitServiceImpl implements IAggregatorDateProfitService {

    private final AggregatorDateProfitMapper aggregatorDateProfitMapper;

    @Override
    public List<AggregatorDateProfit> getAggregatorDateProfitList() {
        return aggregatorDateProfitMapper.selectAll();
    }

    @Override
    public AggregatorDateProfit getAggregatorDateProfit(String aggregatorId, String date) {
        Weekend<AggregatorDateProfit> weekendProfit = Weekend.of(AggregatorDateProfit.class);
        WeekendCriteria<AggregatorDateProfit, Object> criteriaProfit = weekendProfit.weekendCriteria();
        criteriaProfit.andEqualTo(AggregatorDateProfit::getAggregatorId, aggregatorId);
        if (StringUtils.isNotEmpty(date)) {
            //按日期查询
            criteriaProfit.andEqualTo(AggregatorDateProfit::getDate, date);
        } else {
            //查询最后一条
            weekendProfit.orderBy("date").desc();
        }
        List<AggregatorDateProfit> aggregatorDateProfitList = aggregatorDateProfitMapper.selectByExampleAndRowBounds(weekendProfit, new RowBounds(0, 1));
        if (null != aggregatorDateProfitList && aggregatorDateProfitList.size() > 0) {
            return aggregatorDateProfitList.get(0);
        }
        return null;
    }

    @Override
    public List<AggregatorDateProfit> getAggregatorDateProfitList(String aggregatorId, List<String> dateList) {
        Weekend<AggregatorDateProfit> weekendProfit = Weekend.of(AggregatorDateProfit.class);
        WeekendCriteria<AggregatorDateProfit, Object> criteriaProfit = weekendProfit.weekendCriteria();
        criteriaProfit.andEqualTo(AggregatorDateProfit::getAggregatorId, aggregatorId);
        criteriaProfit.andIn(AggregatorDateProfit::getDate, dateList);
        return aggregatorDateProfitMapper.selectByExample(weekendProfit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorDateProfit> aggregatorDateProfitList) {
        return aggregatorDateProfitMapper.batchInsert(aggregatorDateProfitList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(String aggregatorId, String date) {
        Weekend<AggregatorDateProfit> weekendProfit = Weekend.of(AggregatorDateProfit.class);
        WeekendCriteria<AggregatorDateProfit, Object> criteriaProfit = weekendProfit.weekendCriteria();
        criteriaProfit.andEqualTo(AggregatorDateProfit::getAggregatorId, aggregatorId);
        criteriaProfit.andEqualTo(AggregatorDateProfit::getDate, date);
        return aggregatorDateProfitMapper.deleteByExample(weekendProfit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(String aggregatorId, String date, List<AggregatorDateProfit> aggregatorDateProfitList) {
        delete(aggregatorId, date);
        return aggregatorDateProfitMapper.batchInsert(aggregatorDateProfitList);
    }

    @Override
    public AggregatorProfitResp getAggregatorProfitRespTotal(String aggregatorId, String startDate, String endDate) {
        return aggregatorDateProfitMapper.getAggregatorProfitRespTotal(aggregatorId, startDate, endDate);
    }
}
