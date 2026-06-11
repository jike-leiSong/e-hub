package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorEntDapChartMapper;
import cn.sl.ehub.console.service.IAggregatorEntDapChartService;
import cn.sl.ehub.service.vo.AggregatorEntBaseLineLoadChart;
import cn.sl.ehub.service.vo.AggregatorEntDapChart;
import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author sl
 * @Date 2026-05-28
 **/

@RequiredArgsConstructor
@Slf4j
@Service
public class AggregatorEntDapChartServiceImpl implements IAggregatorEntDapChartService {

    private final AggregatorEntDapChartMapper aggregatorEntDapChartMapper;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<String> entIdList, String date) {
        Weekend<AggregatorEntDapChart> weekend = Weekend.of(AggregatorEntDapChart.class);
        WeekendCriteria<AggregatorEntDapChart, Object> criteriaProfit = weekend.weekendCriteria();
        criteriaProfit.andIn(AggregatorEntDapChart::getEntId, entIdList);
        criteriaProfit.andEqualTo(AggregatorEntDapChart::getDapDate, date);
        aggregatorEntDapChartMapper.deleteByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorEntDapChart> AggregatorEntDapChartList) {
        AggregatorEntDapChartList.forEach(aggregatorEntDapChartMapper::insert);
        return AggregatorEntDapChartList.size();
    }

    @Override
    public List<AggregatorEntDapChart> getEntDapLine(String entId, String resourceTypeId, String startDate, String endDate) {
        Weekend<AggregatorEntDapChart> weekend = Weekend.of(AggregatorEntDapChart.class);
        WeekendCriteria<AggregatorEntDapChart, Object> criteriaDelete = weekend.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorEntDapChart::getEntId, entId)
                .andEqualTo(AggregatorEntDapChart::getResourceType, resourceTypeId)
                .andGreaterThanOrEqualTo(AggregatorEntDapChart::getDapDate,startDate)
                .andLessThanOrEqualTo(AggregatorEntDapChart::getDapDate,endDate);
        weekend.orderBy("dapDate");

        return aggregatorEntDapChartMapper.selectByExample(weekend);
    }

    @Override
    public Map<String, List<AggregatorEntDapChart>> getMoreEntDapLine(List<String> entIdList, String resourceTypeId, String startDate, String endDate) {
        Weekend<AggregatorEntDapChart> weekend = Weekend.of(AggregatorEntDapChart.class);
        WeekendCriteria<AggregatorEntDapChart, Object> criteriaDelete = weekend.weekendCriteria();
        criteriaDelete.andIn(AggregatorEntDapChart::getEntId, entIdList)
                .andEqualTo(AggregatorEntDapChart::getResourceType, resourceTypeId)
                .andGreaterThanOrEqualTo(AggregatorEntDapChart::getDapDate,startDate)
                .andLessThanOrEqualTo(AggregatorEntDapChart::getDapDate,endDate);
        weekend.orderBy("baseDate");
        List<AggregatorEntDapChart> list = aggregatorEntDapChartMapper.selectByExample(weekend);
        Map<String, List<AggregatorEntDapChart>> map =new HashMap<>();
        if(CollectionUtil.isNotEmpty(list)){
            map = list.stream().collect(Collectors.groupingBy(AggregatorEntDapChart::getEntId));
        }
        return map;
    }

    @Override
    public List<AggregatorEntDapChart> getEntDapLineByDate(String entId, String resourceTypeId, String date) {
        Weekend<AggregatorEntDapChart> weekend = Weekend.of(AggregatorEntDapChart.class);
        WeekendCriteria<AggregatorEntDapChart, Object> criteriaDelete = weekend.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorEntDapChart::getEntId, entId)
                .andEqualTo(AggregatorEntDapChart::getResourceType, resourceTypeId)
                .andEqualTo(AggregatorEntDapChart::getDapDate, date);
        weekend.orderBy("dapDate");

        return aggregatorEntDapChartMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorEntDapChart> getAggregatorDapLineByDate(String aggregatorId, String resourceTypeId, String date) {
        Weekend<AggregatorEntDapChart> weekend = Weekend.of(AggregatorEntDapChart.class);
        WeekendCriteria<AggregatorEntDapChart, Object> criteriaDelete = weekend.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorEntDapChart::getAggregatorId, aggregatorId)
                .andEqualTo(AggregatorEntDapChart::getResourceType, resourceTypeId)
                .andEqualTo(AggregatorEntDapChart::getDapDate, date);
        weekend.orderBy("dapDate");
        return aggregatorEntDapChartMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorEntDapChart> getBatchDapLineByEntId(List<String> entIds,String date) {
        Weekend<AggregatorEntDapChart> weekend = Weekend.of(AggregatorEntDapChart.class);
        WeekendCriteria<AggregatorEntDapChart, Object> criteriaDelete = weekend.weekendCriteria();
        criteriaDelete.andIn(AggregatorEntDapChart::getEntId, entIds)
                .andEqualTo(AggregatorEntDapChart::getDapDate, date);
        weekend.orderBy("dapDate");
        return aggregatorEntDapChartMapper.selectByExample(weekend);
    }

}
