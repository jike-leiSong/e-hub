package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.console.service.IAggregatorEntDapChartService;
import cn.sl.ehub.service.mapper.AggregatorEntDapChartMapper;
import cn.sl.ehub.service.vo.AggregatorEntDapChart;
import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AggregatorEntDapChartServiceImpl implements IAggregatorEntDapChartService {

    private final AggregatorEntDapChartMapper aggregatorEntDapChartMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<String> entIdList, String date) {
        if (CollectionUtil.isEmpty(entIdList)) {
            return;
        }
        Weekend<AggregatorEntDapChart> weekend = Weekend.of(AggregatorEntDapChart.class);
        WeekendCriteria<AggregatorEntDapChart, Object> criteria = weekend.weekendCriteria();
        criteria.andIn(AggregatorEntDapChart::getEntId, entIdList);
        criteria.andEqualTo(AggregatorEntDapChart::getDapDate, date);
        aggregatorEntDapChartMapper.deleteByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorEntDapChart> aggregatorEntDapChartList) {
        if (CollectionUtil.isEmpty(aggregatorEntDapChartList)) {
            return 0;
        }
        int count = 0;
        for (AggregatorEntDapChart item : aggregatorEntDapChartList) {
            count += aggregatorEntDapChartMapper.insertSelective(item);
        }
        return count;
    }

    @Override
    public List<AggregatorEntDapChart> getEntDapLine(String entId, String resourceTypeId, String startDate, String endDate) {
        Weekend<AggregatorEntDapChart> weekend = Weekend.of(AggregatorEntDapChart.class);
        WeekendCriteria<AggregatorEntDapChart, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDapChart::getEntId, entId);
        criteria.andEqualTo(AggregatorEntDapChart::getResourceType, resourceTypeId);
        criteria.andGreaterThanOrEqualTo(AggregatorEntDapChart::getDapDate, startDate);
        criteria.andLessThanOrEqualTo(AggregatorEntDapChart::getDapDate, endDate);
        weekend.orderBy("dapDate");
        return aggregatorEntDapChartMapper.selectByExample(weekend);
    }

    @Override
    public Map<String, List<AggregatorEntDapChart>> getMoreEntDapLine(List<String> entIdList, String resourceTypeId, String startDate, String endDate) {
        if (CollectionUtil.isEmpty(entIdList)) {
            return Collections.emptyMap();
        }
        Weekend<AggregatorEntDapChart> weekend = Weekend.of(AggregatorEntDapChart.class);
        WeekendCriteria<AggregatorEntDapChart, Object> criteria = weekend.weekendCriteria();
        criteria.andIn(AggregatorEntDapChart::getEntId, entIdList);
        criteria.andEqualTo(AggregatorEntDapChart::getResourceType, resourceTypeId);
        criteria.andGreaterThanOrEqualTo(AggregatorEntDapChart::getDapDate, startDate);
        criteria.andLessThanOrEqualTo(AggregatorEntDapChart::getDapDate, endDate);
        weekend.orderBy("dapDate");
        List<AggregatorEntDapChart> list = aggregatorEntDapChartMapper.selectByExample(weekend);
        if (CollectionUtil.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().collect(Collectors.groupingBy(AggregatorEntDapChart::getEntId));
    }

    @Override
    public List<AggregatorEntDapChart> getEntDapLineByDate(String entId, String resourceTypeId, String date) {
        Weekend<AggregatorEntDapChart> weekend = Weekend.of(AggregatorEntDapChart.class);
        WeekendCriteria<AggregatorEntDapChart, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDapChart::getEntId, entId);
        criteria.andEqualTo(AggregatorEntDapChart::getResourceType, resourceTypeId);
        criteria.andEqualTo(AggregatorEntDapChart::getDapDate, date);
        return aggregatorEntDapChartMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorEntDapChart> getAggregatorDapLineByDate(String aggregatorId, String resourceTypeId, String date) {
        Weekend<AggregatorEntDapChart> weekend = Weekend.of(AggregatorEntDapChart.class);
        WeekendCriteria<AggregatorEntDapChart, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDapChart::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorEntDapChart::getResourceType, resourceTypeId);
        criteria.andEqualTo(AggregatorEntDapChart::getDapDate, date);
        return aggregatorEntDapChartMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorEntDapChart> getBatchDapLineByEntId(List<String> entIds, String date) {
        if (CollectionUtil.isEmpty(entIds)) {
            return Collections.emptyList();
        }
        Weekend<AggregatorEntDapChart> weekend = Weekend.of(AggregatorEntDapChart.class);
        WeekendCriteria<AggregatorEntDapChart, Object> criteria = weekend.weekendCriteria();
        criteria.andIn(AggregatorEntDapChart::getEntId, entIds);
        criteria.andEqualTo(AggregatorEntDapChart::getDapDate, date);
        return aggregatorEntDapChartMapper.selectByExample(weekend);
    }
}
