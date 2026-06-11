package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.IAggregatorEntBaseLineLoadChartMapper;
import cn.sl.ehub.console.service.IAggregatorEntBaseLineLoadChartService;
import cn.sl.ehub.service.vo.AggregatorBaseLineLoadChart;
import cn.sl.ehub.service.vo.AggregatorDeviceDateBaseLineLoadChart;
import cn.sl.ehub.service.vo.AggregatorEntBaseLineLoadChart;
import cn.sl.ehub.service.vo.AggregatorEntDapChart;
import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
@RequiredArgsConstructor
@Service
public class AggregatorEntBaseLineLoadChartServiceImpl implements IAggregatorEntBaseLineLoadChartService {

    private final IAggregatorEntBaseLineLoadChartMapper  aggregatorEntBaseLineLoadChartMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<String> entIdList, String date) {
        Weekend<AggregatorEntBaseLineLoadChart> weekend = Weekend.of(AggregatorEntBaseLineLoadChart.class);
        WeekendCriteria<AggregatorEntBaseLineLoadChart, Object> criteriaProfit = weekend.weekendCriteria();
        criteriaProfit.andIn(AggregatorEntBaseLineLoadChart::getEntId, entIdList);
        criteriaProfit.andEqualTo(AggregatorEntBaseLineLoadChart::getBaseDate, date);
        aggregatorEntBaseLineLoadChartMapper.deleteByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteByTypeId(String aggregatorId, String typeId, String date) {
        Weekend<AggregatorEntBaseLineLoadChart> weekend = Weekend.of(AggregatorEntBaseLineLoadChart.class);
        WeekendCriteria<AggregatorEntBaseLineLoadChart, Object> criteriaProfit = weekend.weekendCriteria();
        criteriaProfit.andEqualTo(AggregatorEntBaseLineLoadChart::getAggregatorId, aggregatorId);
        criteriaProfit.andEqualTo(AggregatorEntBaseLineLoadChart::getResourceType, typeId);
        criteriaProfit.andEqualTo(AggregatorEntBaseLineLoadChart::getBaseDate, date);
        aggregatorEntBaseLineLoadChartMapper.deleteByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorEntBaseLineLoadChart> aggregatorEntBaseLineLoadChartList) {
        aggregatorEntBaseLineLoadChartList.forEach(aggregatorEntBaseLineLoadChartMapper::insert);
        return aggregatorEntBaseLineLoadChartList.size();
    }

    @Override
    public List<AggregatorEntBaseLineLoadChart> getEntBaseLine(String entId, String resourceTypeId, String startDate, String endDate) {
        Weekend<AggregatorEntBaseLineLoadChart> weekend = Weekend.of(AggregatorEntBaseLineLoadChart.class);
        WeekendCriteria<AggregatorEntBaseLineLoadChart, Object> criteriaDelete = weekend.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorEntBaseLineLoadChart::getEntId, entId)
                .andEqualTo(AggregatorEntBaseLineLoadChart::getResourceType, resourceTypeId)
                .andGreaterThanOrEqualTo(AggregatorEntBaseLineLoadChart::getBaseDate,startDate)
                .andLessThanOrEqualTo(AggregatorEntBaseLineLoadChart::getBaseDate,endDate);
        weekend.orderBy("baseDate");

        return aggregatorEntBaseLineLoadChartMapper.selectByExample(weekend);
    }

    @Override
    public Map<String, List<AggregatorEntBaseLineLoadChart>> getMoreEntBaseLine(List<String> entIdList, String resourceTypeId, String startDate, String endDate) {

        Weekend<AggregatorEntBaseLineLoadChart> weekend = Weekend.of(AggregatorEntBaseLineLoadChart.class);
        WeekendCriteria<AggregatorEntBaseLineLoadChart, Object> criteriaDelete = weekend.weekendCriteria();
        criteriaDelete.andIn(AggregatorEntBaseLineLoadChart::getEntId, entIdList)
                .andEqualTo(AggregatorEntBaseLineLoadChart::getResourceType, resourceTypeId)
                .andGreaterThanOrEqualTo(AggregatorEntBaseLineLoadChart::getBaseDate,startDate)
                .andLessThanOrEqualTo(AggregatorEntBaseLineLoadChart::getBaseDate,endDate);
        weekend.orderBy("baseDate");
        List<AggregatorEntBaseLineLoadChart> list = aggregatorEntBaseLineLoadChartMapper.selectByExample(weekend);
        Map<String, List<AggregatorEntBaseLineLoadChart>> map =new HashMap<>();
        if(CollectionUtil.isNotEmpty(list)){
            map = list.stream().collect(Collectors.groupingBy(AggregatorEntBaseLineLoadChart::getEntId));
        }
        return map;
    }

    @Override
    public List<AggregatorEntBaseLineLoadChart> getEntBaseLineBySystemCode(String systemCode, String date) {
        Weekend<AggregatorEntBaseLineLoadChart> weekend = Weekend.of(AggregatorEntBaseLineLoadChart.class);
        WeekendCriteria<AggregatorEntBaseLineLoadChart, Object> criteriaDelete = weekend.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorEntBaseLineLoadChart::getStationId, systemCode)
                .andEqualTo(AggregatorEntBaseLineLoadChart::getBaseDate, date);
        weekend.orderBy("baseDate");
        return aggregatorEntBaseLineLoadChartMapper.selectByExample(weekend);
    }
}
