package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorEntProfitTimeMapper;
import cn.sl.ehub.console.service.IAggregatorEntProfitTimeService;
import cn.sl.ehub.service.vo.AggregatorEntProfitTime;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 企业有效用电和收益配置ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorEntProfitTimeServiceImpl implements IAggregatorEntProfitTimeService {

    private final AggregatorEntProfitTimeMapper aggregatorEntProfitTimeMapper;

    @Override
    public List<AggregatorEntProfitTime> getAggregatorEntProfitTimeList(String aggregatorId) {
        Weekend<AggregatorEntProfitTime> weekend = Weekend.of(AggregatorEntProfitTime.class);
        WeekendCriteria<AggregatorEntProfitTime, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntProfitTime::getAggregatorId, aggregatorId);
        return aggregatorEntProfitTimeMapper.selectByExample(weekend);
    }

    @Override
    public Map<String, List<AggregatorEntProfitTime>> getEntMap(String aggregatorId) {
        Weekend<AggregatorEntProfitTime> weekend = Weekend.of(AggregatorEntProfitTime.class);
        WeekendCriteria<AggregatorEntProfitTime, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntProfitTime::getAggregatorId, aggregatorId);
        List<AggregatorEntProfitTime> aggregatorEntProfitTimeList = aggregatorEntProfitTimeMapper.selectByExample(weekend);
        if (CollectionUtils.isNotEmpty(aggregatorEntProfitTimeList)) {
            return aggregatorEntProfitTimeList.stream().collect(Collectors.groupingBy(AggregatorEntProfitTime::getEntId));
        }
        return null;
    }

    @Override
    public Map<String, List<AggregatorEntProfitTime>> getEntMap() {
        List<AggregatorEntProfitTime> aggregatorEntProfitTimeList = aggregatorEntProfitTimeMapper.selectAll();
        if (CollectionUtils.isNotEmpty(aggregatorEntProfitTimeList)) {
            return aggregatorEntProfitTimeList.stream().collect(Collectors.groupingBy(AggregatorEntProfitTime::getEntId));
        }
        return null;
    }

    @Override
    public Map<String, List<AggregatorEntProfitTime>> getEntMapByEntId(String entId) {
        Weekend<AggregatorEntProfitTime> weekend = Weekend.of(AggregatorEntProfitTime.class);
        WeekendCriteria<AggregatorEntProfitTime, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntProfitTime::getEntId, entId);
        List<AggregatorEntProfitTime> aggregatorEntProfitTimeList = aggregatorEntProfitTimeMapper.selectByExample(weekend);
        if (CollectionUtils.isNotEmpty(aggregatorEntProfitTimeList)) {
            return aggregatorEntProfitTimeList.stream().collect(Collectors.groupingBy(AggregatorEntProfitTime::getEntId));
        }
        return null;
    }
}
