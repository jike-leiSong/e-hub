package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorBaseLineLoadChartMapper;
import cn.sl.ehub.service.mapper.AggregatorDateIssueChartMapper;
import cn.sl.ehub.console.service.IAggregatorBaseLineLoadChartService;
import cn.sl.ehub.console.service.IAggregatorDateIssueChartService;
import cn.sl.ehub.service.vo.AggregatorBaseLineLoadChart;
import cn.sl.ehub.service.vo.AggregatorDateIssueChart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
@RequiredArgsConstructor
@Service
public class AggregatorBaseLineLoadChartServiceImpl implements IAggregatorBaseLineLoadChartService {

    private final AggregatorBaseLineLoadChartMapper aggregatorBaseLineLoadChartMapper;
    @Override
    public int delete(String aggregatorId, String date, String resourceTypeId) {

        Weekend<AggregatorBaseLineLoadChart> weekendDelete = Weekend.of(AggregatorBaseLineLoadChart.class);
        WeekendCriteria<AggregatorBaseLineLoadChart, Object> criteriaDelete = weekendDelete.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorBaseLineLoadChart::getAggregatorId, aggregatorId);
        criteriaDelete.andEqualTo(AggregatorBaseLineLoadChart::getBaseDate, date);
        criteriaDelete.andEqualTo(AggregatorBaseLineLoadChart::getResourceType, resourceTypeId);
        return aggregatorBaseLineLoadChartMapper.deleteByExample(weekendDelete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorBaseLineLoadChart> aggregatorBaseLineLoadChartList) {
        return aggregatorBaseLineLoadChartMapper.batchInsert(aggregatorBaseLineLoadChartList);
    }

    @Override
    public List<AggregatorBaseLineLoadChart> getAggregatorBaseLine(String aggregatorId, String resourceTypeId, String startDate, String endDate) {
        Weekend<AggregatorBaseLineLoadChart> weekend = Weekend.of(AggregatorBaseLineLoadChart.class);
        WeekendCriteria<AggregatorBaseLineLoadChart, Object> criteriaDelete = weekend.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorBaseLineLoadChart::getAggregatorId, aggregatorId)
                      .andEqualTo(AggregatorBaseLineLoadChart::getResourceType, resourceTypeId)
                      .andGreaterThanOrEqualTo(AggregatorBaseLineLoadChart::getBaseDate,startDate)
                      .andLessThanOrEqualTo(AggregatorBaseLineLoadChart::getBaseDate,endDate);
        weekend.orderBy("baseDate");

        return aggregatorBaseLineLoadChartMapper.selectByExample(weekend);
    }
}
