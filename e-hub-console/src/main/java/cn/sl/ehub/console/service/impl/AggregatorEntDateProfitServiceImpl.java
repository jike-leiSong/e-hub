package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorEntDateProfitMapper;
import cn.sl.ehub.service.resp.AggregatorEntApplyPlanStatusResp;
import cn.sl.ehub.service.resp.AggregatorEntDateProfitResp;
import cn.sl.ehub.console.service.IAggregatorEntDateProfitService;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.service.vo.AggregatorDateApplyDetail;
import cn.sl.ehub.service.vo.AggregatorEntDateProfit;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 企业收益ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorEntDateProfitServiceImpl implements IAggregatorEntDateProfitService {

    private final AggregatorEntDateProfitMapper aggregatorEntDateProfitMapper;

    @Override
    public List<AggregatorEntDateProfit> getAggregatorEntDateProfitList() {
        return aggregatorEntDateProfitMapper.selectAll();
    }

    @Override
    public List<AggregatorEntDateProfit> getAggregatorEntDateProfitList(List<String> entIdList, String startDate, String endDate) {
        Weekend<AggregatorEntDateProfit> weekend = Weekend.of(AggregatorEntDateProfit.class);
        WeekendCriteria<AggregatorEntDateProfit, Object> criteria = weekend.weekendCriteria();
        criteria.andBetween(AggregatorEntDateProfit::getDate, startDate, endDate);
        criteria.andIn(AggregatorEntDateProfit::getEntId, entIdList);
        return aggregatorEntDateProfitMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorEntDateProfit> getAggregatorEntDateProfitList(List<String> entIdList, String date) {
        Weekend<AggregatorEntDateProfit> weekend = Weekend.of(AggregatorEntDateProfit.class);
        WeekendCriteria<AggregatorEntDateProfit, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDateProfit::getDate, date);
        criteria.andIn(AggregatorEntDateProfit::getEntId, entIdList);
        return aggregatorEntDateProfitMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorEntDateProfit> getAggregatorEntDateProfitList(String aggregatorId, List<String> dateList) {
        Weekend<AggregatorEntDateProfit> weekend = Weekend.of(AggregatorEntDateProfit.class);
        WeekendCriteria<AggregatorEntDateProfit, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDateProfit::getAggregatorId, aggregatorId);
        criteria.andIn(AggregatorEntDateProfit::getDate, dateList);
        return aggregatorEntDateProfitMapper.selectByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorEntDateProfit> aggregatorEntDateProfitList) {
        return aggregatorEntDateProfitMapper.batchInsert(aggregatorEntDateProfitList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(String entId, String date) {
        Weekend<AggregatorEntDateProfit> weekend = Weekend.of(AggregatorEntDateProfit.class);
        WeekendCriteria<AggregatorEntDateProfit, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDateProfit::getEntId, entId);
        criteria.andEqualTo(AggregatorEntDateProfit::getDate, date);
        return aggregatorEntDateProfitMapper.deleteByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByAggregatorId(String aggregatorId, String date) {
        Weekend<AggregatorEntDateProfit> weekend = Weekend.of(AggregatorEntDateProfit.class);
        WeekendCriteria<AggregatorEntDateProfit, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDateProfit::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorEntDateProfit::getDate, date);
        return aggregatorEntDateProfitMapper.deleteByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(String entId, String date, List<AggregatorEntDateProfit> aggregatorEntDateProfitList) {
        delete(entId, date);
        return batchInsert(aggregatorEntDateProfitList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveByAggregatorId(String aggregatorId, String date, List<AggregatorEntDateProfit> aggregatorEntDateProfitList) {
        deleteByAggregatorId(aggregatorId, date);
        if (null != aggregatorEntDateProfitList && aggregatorEntDateProfitList.size() > 0) {
            return batchInsert(aggregatorEntDateProfitList);
        }
        return 0;
    }

    @Override
    public AggregatorEntDateProfitResp getProfit(String entId) {
        AggregatorEntDateProfitResp aggregatorEntDateProfitResp = aggregatorEntDateProfitMapper.getProfit(entId);
        if (null == aggregatorEntDateProfitResp) {
            aggregatorEntDateProfitResp = new AggregatorEntDateProfitResp();
            aggregatorEntDateProfitResp.setTotalProfit(0D);
        } else {
            aggregatorEntDateProfitResp.setDate(aggregatorEntDateProfitResp.getDate().substring(5));
        }
        return aggregatorEntDateProfitResp;
    }

    @Override
    public List<AggregatorEntDateProfit> getAggregatorEntDateProfitList(String aggregatorId, String startDate, String endDate) {
        Weekend<AggregatorEntDateProfit> weekend = Weekend.of(AggregatorEntDateProfit.class);
        WeekendCriteria<AggregatorEntDateProfit, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDateProfit::getAggregatorId, aggregatorId);
        criteria.andGreaterThanOrEqualTo(AggregatorEntDateProfit::getDate, startDate);
        criteria.andLessThanOrEqualTo(AggregatorEntDateProfit::getDate, endDate);
        return aggregatorEntDateProfitMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorEntDateProfit> getAggregatorEntDateProfitListByEntId(String entId, List<String> dateList) {
        Weekend<AggregatorEntDateProfit> weekend = Weekend.of(AggregatorEntDateProfit.class);
        WeekendCriteria<AggregatorEntDateProfit, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDateProfit::getEntId, entId);
        criteria.andIn(AggregatorEntDateProfit::getDate, dateList);
        return aggregatorEntDateProfitMapper.selectByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(List<AggregatorEntDateProfit> aggregatorEntDateProfitList) {
        aggregatorEntDateProfitList.forEach(update -> {
            AggregatorEntDateProfit aggregatorEntDateProfit = aggregatorEntDateProfitMapper.getAggregatorEntDateProfit(update.getEntId(), update.getDate());
            if (null != aggregatorEntDateProfit) {
                aggregatorEntDateProfit.setElectricQuantity(update.getElectricQuantity());
                aggregatorEntDateProfitMapper.updateByPrimaryKey(aggregatorEntDateProfit);
            } else {
                aggregatorEntDateProfitMapper.insert(update);
            }
        });
        return 0;
    }

    @Override
    public int update(AggregatorEntDateProfit update, String entId, String date) {
        Weekend<AggregatorEntDateProfit> weekend = Weekend.of(AggregatorEntDateProfit.class);
        WeekendCriteria<AggregatorEntDateProfit, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDateProfit::getEntId, entId);
        criteria.andEqualTo(AggregatorEntDateProfit::getDate, date);
        return aggregatorEntDateProfitMapper.updateByExampleSelective(update, weekend);
    }
}
