package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorDapChartMapper;
import cn.sl.ehub.console.service.IAggregatorDapChartService;
import cn.sl.ehub.service.vo.AggregatorDapChart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AggregatorDapChartServiceImpl implements IAggregatorDapChartService {

    private final AggregatorDapChartMapper aggregatorDapChartMapper;

    @Override
    public int delete(String aggregatorId, String date, String resourceTypeId) {
        Weekend<AggregatorDapChart> weekendDelete = Weekend.of(AggregatorDapChart.class);
        WeekendCriteria<AggregatorDapChart, Object> criteriaDelete = weekendDelete.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorDapChart::getAggregatorId, aggregatorId);
        criteriaDelete.andEqualTo(AggregatorDapChart::getDate, date);
        criteriaDelete.andEqualTo(AggregatorDapChart::getResourceType, resourceTypeId);
        return aggregatorDapChartMapper.deleteByExample(weekendDelete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorDapChart> aggregatorDapCharts) {
        return aggregatorDapChartMapper.batchInsert(aggregatorDapCharts);
    }

    @Override
    public List<AggregatorDapChart> getDapChart(String aggregatorId, String resourceTypeId, List<String> dateList) {
        Weekend<AggregatorDapChart> weekend = Weekend.of(AggregatorDapChart.class);
        WeekendCriteria<AggregatorDapChart, Object> criteriaDelete = weekend.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorDapChart::getAggregatorId, aggregatorId)
                .andEqualTo(AggregatorDapChart::getResourceType, resourceTypeId)
                .andIn(AggregatorDapChart::getDate, dateList);
        weekend.orderBy("date");

        return aggregatorDapChartMapper.selectByExample(weekend);
    }


    @Override
    public AggregatorDapChart getAggregatorDateDapChart(String aggregatorId, String resourceTypeId, String date) {
        Weekend<AggregatorDapChart> weekendIssueChart = Weekend.of(AggregatorDapChart.class);
        WeekendCriteria<AggregatorDapChart, Object> criteriaIssueChart = weekendIssueChart.weekendCriteria();
        criteriaIssueChart.andEqualTo(AggregatorDapChart::getAggregatorId, aggregatorId);
        criteriaIssueChart.andEqualTo(AggregatorDapChart::getResourceType, resourceTypeId);
        criteriaIssueChart.andEqualTo(AggregatorDapChart::getDate, date);
        List<AggregatorDapChart> aggregatorDateIssueChartList = aggregatorDapChartMapper.selectByExample(weekendIssueChart);
        if (null != aggregatorDateIssueChartList && aggregatorDateIssueChartList.size() > 0) {
            return aggregatorDateIssueChartList.get(0);
        }
        return null;
    }

    @Override
    public List<AggregatorDapChart> getAggregatorDateDapChart(String aggregatorId, String resourceTypeId, List<String> dateList) {
        Weekend<AggregatorDapChart> weekendIssueChart = Weekend.of(AggregatorDapChart.class);
        WeekendCriteria<AggregatorDapChart, Object> criteriaIssueChart = weekendIssueChart.weekendCriteria();
        criteriaIssueChart.andEqualTo(AggregatorDapChart::getAggregatorId, aggregatorId);
        criteriaIssueChart.andEqualTo(AggregatorDapChart::getResourceType, resourceTypeId);
        criteriaIssueChart.andIn(AggregatorDapChart::getDate, dateList);
        return aggregatorDapChartMapper.selectByExample(weekendIssueChart);
    }

    @Override
    public List<AggregatorDapChart> getAggregatorDateDapChartListNew(String aggregatorId, String resourceTypeId, List<String> dateList) {
        Weekend<AggregatorDapChart> weekendIssueChart = Weekend.of(AggregatorDapChart.class);
        WeekendCriteria<AggregatorDapChart, Object> criteriaIssueChart = weekendIssueChart.weekendCriteria();
        criteriaIssueChart.andEqualTo(AggregatorDapChart::getAggregatorId, aggregatorId);
        criteriaIssueChart.andEqualTo(AggregatorDapChart::getResourceType, resourceTypeId);
        criteriaIssueChart.andIn(AggregatorDapChart::getDate, dateList);
        weekendIssueChart.orderBy("date");
        return aggregatorDapChartMapper.selectByExample(weekendIssueChart);
    }

    @Override
    public List<AggregatorDapChart> getAggregatorDapChart(String aggregatorId, String resourceTypeId, String startDate, String endDate) {
        Weekend<AggregatorDapChart> weekend = Weekend.of(AggregatorDapChart.class);
        WeekendCriteria<AggregatorDapChart, Object> criteriaDelete = weekend.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorDapChart::getAggregatorId, aggregatorId)
                .andEqualTo(AggregatorDapChart::getResourceType, resourceTypeId)
                .andGreaterThanOrEqualTo(AggregatorDapChart::getDate, startDate)
                .andLessThanOrEqualTo(AggregatorDapChart::getDate, endDate);
        weekend.orderBy("date");

        return aggregatorDapChartMapper.selectByExample(weekend);
    }
}
