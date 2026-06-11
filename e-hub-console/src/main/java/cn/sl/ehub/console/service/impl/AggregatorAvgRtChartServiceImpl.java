package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorAvgRtChartMapper;
import cn.sl.ehub.console.service.IAggregatorAvgRtChartService;
import cn.sl.ehub.console.service.IAggregatorBaseLineLoadChartService;
import cn.sl.ehub.service.vo.AggregatorAvgRtChart;
import cn.sl.ehub.service.vo.AggregatorBaseLineLoadChart;
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
public class AggregatorAvgRtChartServiceImpl implements IAggregatorAvgRtChartService {

    private final AggregatorAvgRtChartMapper aggregatorAvgRtChartMapper;
    @Override
    public int delete(String aggregatorId, String date, String resourceTypeId) {
        Weekend<AggregatorAvgRtChart> weekendDelete = Weekend.of(AggregatorAvgRtChart.class);
        WeekendCriteria<AggregatorAvgRtChart, Object> criteriaDelete = weekendDelete.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorAvgRtChart::getAggregatorId, aggregatorId);
        criteriaDelete.andEqualTo(AggregatorAvgRtChart::getDate, date);
        criteriaDelete.andEqualTo(AggregatorAvgRtChart::getResourceType, resourceTypeId);
        return aggregatorAvgRtChartMapper.deleteByExample(weekendDelete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorAvgRtChart> aggregatorAvgRtChartList) {
        return aggregatorAvgRtChartMapper.batchInsert(aggregatorAvgRtChartList);
    }

    @Override
    public List<AggregatorAvgRtChart> getAvgRtChart(String aggregatorId, String resourceTypeId, List<String> dateList) {
        Weekend<AggregatorAvgRtChart> weekend = Weekend.of(AggregatorAvgRtChart.class);
        WeekendCriteria<AggregatorAvgRtChart, Object> criteriaDelete = weekend.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorAvgRtChart::getAggregatorId, aggregatorId)
                      .andEqualTo(AggregatorAvgRtChart::getResourceType,resourceTypeId)
                      .andIn(AggregatorAvgRtChart::getDate,dateList);
        weekend.orderBy("date");

        return aggregatorAvgRtChartMapper.selectByExample(weekend);
    }
}
