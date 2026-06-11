package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorDateDeliveryChartMapper;
import cn.sl.ehub.console.service.IAggregatorDateDeliveryChartService;
import cn.sl.ehub.service.vo.AggregatorDateDeliveryChart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * 聚合商申报曲线ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorDateDeliveryChartServiceImpl implements IAggregatorDateDeliveryChartService {

    private final AggregatorDateDeliveryChartMapper aggregatorDateDeliveryChartMapper;

    @Override
    public List<AggregatorDateDeliveryChart> getAggregatorDateDeliveryChartList(String aggregatorId, List<String> dateList) {
        Weekend<AggregatorDateDeliveryChart> weekend = Weekend.of(AggregatorDateDeliveryChart.class);
        WeekendCriteria<AggregatorDateDeliveryChart, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDateDeliveryChart::getAggregatorId, aggregatorId);
        criteria.andIn(AggregatorDateDeliveryChart::getDate, dateList);
        return aggregatorDateDeliveryChartMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorDateDeliveryChart> getAggregatorDateDeliveryChartList(String aggregatorId, String date) {
        Weekend<AggregatorDateDeliveryChart> weekend = Weekend.of(AggregatorDateDeliveryChart.class);
        WeekendCriteria<AggregatorDateDeliveryChart, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDateDeliveryChart::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorDateDeliveryChart::getDate, date);
        return aggregatorDateDeliveryChartMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorDateDeliveryChart> getAggregatorDateDeliveryChartList(String aggregatorId, String resourceTypeId, String date) {
        Weekend<AggregatorDateDeliveryChart> weekendDeliveryChart = Weekend.of(AggregatorDateDeliveryChart.class);
        WeekendCriteria<AggregatorDateDeliveryChart, Object> criteriaDeliveryChart = weekendDeliveryChart.weekendCriteria();
        criteriaDeliveryChart.andEqualTo(AggregatorDateDeliveryChart::getAggregatorId, aggregatorId);
        criteriaDeliveryChart.andEqualTo(AggregatorDateDeliveryChart::getResourceTypeId, resourceTypeId);
        criteriaDeliveryChart.andEqualTo(AggregatorDateDeliveryChart::getDate, date);
        return aggregatorDateDeliveryChartMapper.selectByExample(weekendDeliveryChart);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(String aggregatorId, List<String> dateList) {
        Weekend<AggregatorDateDeliveryChart> weekendDelete = Weekend.of(AggregatorDateDeliveryChart.class);
        WeekendCriteria<AggregatorDateDeliveryChart, Object> criteriaDelete = weekendDelete.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorDateDeliveryChart::getAggregatorId, aggregatorId);
        criteriaDelete.andIn(AggregatorDateDeliveryChart::getDate, dateList);
        return aggregatorDateDeliveryChartMapper.deleteByExample(weekendDelete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorDateDeliveryChart> aggregatorDateDeliveryChartList) {
        return aggregatorDateDeliveryChartMapper.batchInsert(aggregatorDateDeliveryChartList);
    }

    @Override
    public List<AggregatorDateDeliveryChart> getAggregatorDateDeliveryChartList(String aggregatorId, String resourceTypeId, List<String> dateList) {
        Weekend<AggregatorDateDeliveryChart> weekend = Weekend.of(AggregatorDateDeliveryChart.class);
        WeekendCriteria<AggregatorDateDeliveryChart, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDateDeliveryChart::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorDateDeliveryChart::getResourceTypeId, resourceTypeId);
        criteria.andIn(AggregatorDateDeliveryChart::getDate, dateList);
        return aggregatorDateDeliveryChartMapper.selectByExample(weekend);
    }

    @Override
    public int delete(String aggregatorId, List<String> dateList, String sourceTypeId) {
        Weekend<AggregatorDateDeliveryChart> weekendDelete = Weekend.of(AggregatorDateDeliveryChart.class);
        WeekendCriteria<AggregatorDateDeliveryChart, Object> criteriaDelete = weekendDelete.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorDateDeliveryChart::getAggregatorId, aggregatorId)
                      .andEqualTo(AggregatorDateDeliveryChart::getResourceTypeId,sourceTypeId);
        criteriaDelete.andIn(AggregatorDateDeliveryChart::getDate, dateList);
        return aggregatorDateDeliveryChartMapper.deleteByExample(weekendDelete);
    }
}
