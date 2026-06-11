package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorAvgRtChartMapper;
import cn.sl.ehub.service.mapper.AggregatorCrChartMapper;
import cn.sl.ehub.console.service.IAggregatorAvgRtChartService;
import cn.sl.ehub.console.service.IAggregatorCrChartService;
import cn.sl.ehub.service.vo.AggregatorAvgRtChart;
import cn.sl.ehub.service.vo.AggregatorBaseLineLoadChart;
import cn.sl.ehub.service.vo.AggregatorCrChart;
import cn.sl.ehub.service.vo.AggregatorDateIssueChart;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
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
public class AggregatorCrChartServiceImpl implements IAggregatorCrChartService {
    private final AggregatorCrChartMapper aggregatorCrChartMapper;

    /**
     * 根据聚合商id\日期删除曲线
     * @param aggregatorId
     * @param date
     * @param resourceTypeId
     * @return
     */
    @Override
    public int delete(String aggregatorId, String date, String resourceTypeId) {
        Weekend<AggregatorCrChart> weekendDelete = Weekend.of(AggregatorCrChart.class);
        WeekendCriteria<AggregatorCrChart, Object> criteriaDelete = weekendDelete.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorCrChart::getAggregatorId, aggregatorId);
        criteriaDelete.andEqualTo(AggregatorCrChart::getCrDate, date);
        criteriaDelete.andEqualTo(AggregatorCrChart::getResourceType, resourceTypeId);
        return aggregatorCrChartMapper.deleteByExample(weekendDelete);
    }

    /**
     * 批量删除曲线
     * @param aggregatorCrChartList
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorCrChart> aggregatorCrChartList) {
        return aggregatorCrChartMapper.batchInsert(aggregatorCrChartList);
    }

    /**
     * cr碳排放因子查询
     * @param aggregatorId
     * @param resourceTypeId
     * @param dateList
     * @return
     */
    @Override
    public List<AggregatorCrChart> getCrChart(String aggregatorId, String resourceTypeId, List<String> dateList) {
        Weekend<AggregatorCrChart> weekend = Weekend.of(AggregatorCrChart.class);
        WeekendCriteria<AggregatorCrChart, Object> criteriaDelete = weekend.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorCrChart::getAggregatorId, aggregatorId)
                .andEqualTo(AggregatorCrChart::getResourceType,resourceTypeId)
                .andIn(AggregatorCrChart::getCrDate,dateList);
        weekend.orderBy("crDate");

        return aggregatorCrChartMapper.selectByExample(weekend);
    }

    @Override
    public AggregatorCrChart getAggregatorDateCrChart(String aggregatorId, String resourceTypeId, String date) {
        Weekend<AggregatorCrChart> weekendIssueChart = Weekend.of(AggregatorCrChart.class);
        WeekendCriteria<AggregatorCrChart, Object> criteriaIssueChart = weekendIssueChart.weekendCriteria();
        criteriaIssueChart.andEqualTo(AggregatorCrChart::getAggregatorId, aggregatorId);
        criteriaIssueChart.andEqualTo(AggregatorCrChart::getResourceType, resourceTypeId);
        criteriaIssueChart.andEqualTo(AggregatorCrChart::getCrDate, date);
        List<AggregatorCrChart> aggregatorCrChartList = aggregatorCrChartMapper.selectByExample(weekendIssueChart);
        if (CollectionUtils.isNotEmpty(aggregatorCrChartList)) {
            return aggregatorCrChartList.get(0);
        }
        return null;
    }

    @Override
    public List<AggregatorCrChart> getAggregatorDateCrChartListNew(String aggregatorId, String resourceTypeId, List<String> dateList) {
        Weekend<AggregatorCrChart> weekendIssueChart = Weekend.of(AggregatorCrChart.class);
        WeekendCriteria<AggregatorCrChart, Object> criteriaIssueChart = weekendIssueChart.weekendCriteria();
        criteriaIssueChart.andEqualTo(AggregatorCrChart::getAggregatorId, aggregatorId);
        criteriaIssueChart.andEqualTo(AggregatorCrChart::getResourceType, resourceTypeId);
        criteriaIssueChart.andIn(AggregatorCrChart::getCrDate, dateList);
        weekendIssueChart.orderBy("crDate");
        return aggregatorCrChartMapper.selectByExample(weekendIssueChart);
    }

    @Override
    public List<AggregatorCrChart> getAggregatorCrLine(String aggregatorId, String resourceTypeId, String startDate, String endDate) {
        Weekend<AggregatorCrChart> weekend = Weekend.of(AggregatorCrChart.class);
        WeekendCriteria<AggregatorCrChart, Object> criteriaDelete = weekend.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorCrChart::getAggregatorId, aggregatorId)
                .andEqualTo(AggregatorCrChart::getResourceType, resourceTypeId)
                .andGreaterThanOrEqualTo(AggregatorCrChart::getCrDate,startDate)
                .andLessThanOrEqualTo(AggregatorCrChart::getCrDate,endDate);
        weekend.orderBy("crDate");

        return aggregatorCrChartMapper.selectByExample(weekend);
    }
}
