package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorDateIssueChartMapper;
import cn.sl.ehub.console.service.IAggregatorDateIssueChartService;
import cn.sl.ehub.service.vo.AggregatorDateIssueChart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * 聚合商下发曲线ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorDateIssueChartServiceImpl implements IAggregatorDateIssueChartService {

    private final AggregatorDateIssueChartMapper aggregatorDateIssueChartMapper;

    @Override
    public AggregatorDateIssueChart getAggregatorDateIssueChart(String aggregatorId, String resourceTypeId, String date) {
        Weekend<AggregatorDateIssueChart> weekendIssueChart = Weekend.of(AggregatorDateIssueChart.class);
        WeekendCriteria<AggregatorDateIssueChart, Object> criteriaIssueChart = weekendIssueChart.weekendCriteria();
        criteriaIssueChart.andEqualTo(AggregatorDateIssueChart::getAggregatorId, aggregatorId);
        criteriaIssueChart.andEqualTo(AggregatorDateIssueChart::getResourceTypeId, resourceTypeId);
        criteriaIssueChart.andEqualTo(AggregatorDateIssueChart::getDate, date);
        List<AggregatorDateIssueChart> aggregatorDateIssueChartList = aggregatorDateIssueChartMapper.selectByExample(weekendIssueChart);
        if (null != aggregatorDateIssueChartList && aggregatorDateIssueChartList.size() > 0) {
            return aggregatorDateIssueChartList.get(0);
        }
        return null;
    }

    @Override
    public List<AggregatorDateIssueChart> getAggregatorDateIssueChartList(String aggregatorId, String resourceTypeId, List<String> dateList) {
        Weekend<AggregatorDateIssueChart> weekendIssueChart = Weekend.of(AggregatorDateIssueChart.class);
        WeekendCriteria<AggregatorDateIssueChart, Object> criteriaIssueChart = weekendIssueChart.weekendCriteria();
        criteriaIssueChart.andEqualTo(AggregatorDateIssueChart::getAggregatorId, aggregatorId);
        criteriaIssueChart.andEqualTo(AggregatorDateIssueChart::getResourceTypeId, resourceTypeId);
        criteriaIssueChart.andIn(AggregatorDateIssueChart::getDate, dateList);
        return aggregatorDateIssueChartMapper.selectByExample(weekendIssueChart);
    }

    @Override
    public List<AggregatorDateIssueChart> getAggregatorDateIssueChartListNew(String aggregatorId, String resourceTypeId, List<String> dateList) {
        Weekend<AggregatorDateIssueChart> weekendIssueChart = Weekend.of(AggregatorDateIssueChart.class);
        WeekendCriteria<AggregatorDateIssueChart, Object> criteriaIssueChart = weekendIssueChart.weekendCriteria();
        criteriaIssueChart.andEqualTo(AggregatorDateIssueChart::getAggregatorId, aggregatorId);
        criteriaIssueChart.andEqualTo(AggregatorDateIssueChart::getResourceTypeId, resourceTypeId);
        criteriaIssueChart.andIn(AggregatorDateIssueChart::getDate, dateList);
        weekendIssueChart.orderBy("date");
        return aggregatorDateIssueChartMapper.selectByExample(weekendIssueChart);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(String aggregatorId, String date) {
        Weekend<AggregatorDateIssueChart> weekendDelete = Weekend.of(AggregatorDateIssueChart.class);
        WeekendCriteria<AggregatorDateIssueChart, Object> criteriaDelete = weekendDelete.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorDateIssueChart::getAggregatorId, aggregatorId);
        criteriaDelete.andEqualTo(AggregatorDateIssueChart::getDate, date);
        return aggregatorDateIssueChartMapper.deleteByExample(weekendDelete);
    }

    @Override
    public int delete(String aggregatorId, String date, String resourceTypeId) {
        Weekend<AggregatorDateIssueChart> weekendDelete = Weekend.of(AggregatorDateIssueChart.class);
        WeekendCriteria<AggregatorDateIssueChart, Object> criteriaDelete = weekendDelete.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorDateIssueChart::getAggregatorId, aggregatorId);
        criteriaDelete.andEqualTo(AggregatorDateIssueChart::getDate, date);
        criteriaDelete.andEqualTo(AggregatorDateIssueChart::getResourceTypeId, resourceTypeId);
        return aggregatorDateIssueChartMapper.deleteByExample(weekendDelete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorDateIssueChart> aggregatorDateIssueChartList) {
        return aggregatorDateIssueChartMapper.batchInsert(aggregatorDateIssueChartList);
    }

    @Override
    public int delete(String aggregatorId, List<String> dateList, String resourceTypeId) {
        Weekend<AggregatorDateIssueChart> weekendDelete = Weekend.of(AggregatorDateIssueChart.class);
        WeekendCriteria<AggregatorDateIssueChart, Object> criteriaDelete = weekendDelete.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorDateIssueChart::getAggregatorId, aggregatorId)
                      .andEqualTo(AggregatorDateIssueChart::getResourceTypeId, resourceTypeId)
                      .andIn(AggregatorDateIssueChart::getDate, dateList);
        return aggregatorDateIssueChartMapper.deleteByExample(weekendDelete);
    }
}
