package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorDeviceDateDeliveryChartMapper;
import cn.sl.ehub.console.service.IAggregatorDeviceDateDeliveryChartService;
import cn.sl.ehub.service.vo.AggregatorDeviceDateDeliveryChart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * 设备申报曲线ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorDeviceDateDeliveryChartServiceImpl implements IAggregatorDeviceDateDeliveryChartService {

    private final AggregatorDeviceDateDeliveryChartMapper aggregatorDeviceDateDeliveryChartMapper;

    @Override
    public List<AggregatorDeviceDateDeliveryChart> getAggregatorDeviceDateDeliveryChartList(String date) {
        Weekend<AggregatorDeviceDateDeliveryChart> weekend = Weekend.of(AggregatorDeviceDateDeliveryChart.class);
        WeekendCriteria<AggregatorDeviceDateDeliveryChart, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDateDeliveryChart::getDate, date);
        return aggregatorDeviceDateDeliveryChartMapper.selectByExample(weekend);
    }

    @Override
    public AggregatorDeviceDateDeliveryChart getAggregatorDeviceDateDeliveryChart(String deviceBaseId, String date) {
        Weekend<AggregatorDeviceDateDeliveryChart> weekend = Weekend.of(AggregatorDeviceDateDeliveryChart.class);
        WeekendCriteria<AggregatorDeviceDateDeliveryChart, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDateDeliveryChart::getDeviceBaseId, deviceBaseId);
        criteria.andEqualTo(AggregatorDeviceDateDeliveryChart::getDate, date);
        List<AggregatorDeviceDateDeliveryChart> aggregatorDeviceDateDeliveryChartList = aggregatorDeviceDateDeliveryChartMapper.selectByExample(weekend);
        if (null != aggregatorDeviceDateDeliveryChartList && aggregatorDeviceDateDeliveryChartList.size() > 0) {
            return aggregatorDeviceDateDeliveryChartList.get(0);
        }
        return null;
    }

    @Override
    public List<AggregatorDeviceDateDeliveryChart> getAggregatorDeviceDateDeliveryChartList(List<String> aggregatorIdList, String date) {
        Weekend<AggregatorDeviceDateDeliveryChart> weekend = Weekend.of(AggregatorDeviceDateDeliveryChart.class);
        WeekendCriteria<AggregatorDeviceDateDeliveryChart, Object> criteria = weekend.weekendCriteria();
        criteria.andIn(AggregatorDeviceDateDeliveryChart::getAggregatorId, aggregatorIdList);
        criteria.andEqualTo(AggregatorDeviceDateDeliveryChart::getDate, date);
        return aggregatorDeviceDateDeliveryChartMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorDeviceDateDeliveryChart> getAggregatorDeviceDateDeliveryChartList(String aggregatorId, String date) {
        Weekend<AggregatorDeviceDateDeliveryChart> weekend = Weekend.of(AggregatorDeviceDateDeliveryChart.class);
        WeekendCriteria<AggregatorDeviceDateDeliveryChart, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDateDeliveryChart::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorDeviceDateDeliveryChart::getDate, date);
        return aggregatorDeviceDateDeliveryChartMapper.selectByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(String entId, List<String> dateList) {
        Weekend<AggregatorDeviceDateDeliveryChart> weekendDelete = Weekend.of(AggregatorDeviceDateDeliveryChart.class);
        WeekendCriteria<AggregatorDeviceDateDeliveryChart, Object> criteriaDelete = weekendDelete.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorDeviceDateDeliveryChart::getEntId, entId);
        criteriaDelete.andIn(AggregatorDeviceDateDeliveryChart::getDate, dateList);
        return aggregatorDeviceDateDeliveryChartMapper.deleteByExample(weekendDelete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorDeviceDateDeliveryChart> aggregatorDeviceDateDeliveryChartList) {
        return aggregatorDeviceDateDeliveryChartMapper.batchInsert(aggregatorDeviceDateDeliveryChartList);
    }

    @Override
    public List<AggregatorDeviceDateDeliveryChart> getAggregatorDeviceDateDeliveryChartList(String aggregatorId, List<String> dateList) {
        Weekend<AggregatorDeviceDateDeliveryChart> weekend = Weekend.of(AggregatorDeviceDateDeliveryChart.class);
        WeekendCriteria<AggregatorDeviceDateDeliveryChart, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDateDeliveryChart::getAggregatorId, aggregatorId);
        criteria.andIn(AggregatorDeviceDateDeliveryChart::getDate, dateList);
        return aggregatorDeviceDateDeliveryChartMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorDeviceDateDeliveryChart> getAggregatorDeviceDateDeliveryChartListByResourceTypeId(String resourceTypeId, String date) {
        Weekend<AggregatorDeviceDateDeliveryChart> weekend = Weekend.of(AggregatorDeviceDateDeliveryChart.class);
        WeekendCriteria<AggregatorDeviceDateDeliveryChart, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDateDeliveryChart::getResourceTypeId, resourceTypeId);
        criteria.andEqualTo(AggregatorDeviceDateDeliveryChart::getDate, date);
        return aggregatorDeviceDateDeliveryChartMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorDeviceDateDeliveryChart> getAggregatorDeviceDateDeliveryChartList(List<String> deviceBaseIdList, List<String> dateList) {
        Weekend<AggregatorDeviceDateDeliveryChart> weekend = Weekend.of(AggregatorDeviceDateDeliveryChart.class);
        WeekendCriteria<AggregatorDeviceDateDeliveryChart, Object> criteria = weekend.weekendCriteria();
        criteria.andIn(AggregatorDeviceDateDeliveryChart::getDeviceBaseId, deviceBaseIdList);
        criteria.andIn(AggregatorDeviceDateDeliveryChart::getDate, dateList);
        return aggregatorDeviceDateDeliveryChartMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorDeviceDateDeliveryChart> getAggregatorDeviceDateDeliveryChartListByDeviceBaseId(String deviceBaseId, List<String> dateList) {
        Weekend<AggregatorDeviceDateDeliveryChart> weekend = Weekend.of(AggregatorDeviceDateDeliveryChart.class);
        WeekendCriteria<AggregatorDeviceDateDeliveryChart, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDateDeliveryChart::getDeviceBaseId, deviceBaseId);
        criteria.andIn(AggregatorDeviceDateDeliveryChart::getDate, dateList);
        return aggregatorDeviceDateDeliveryChartMapper.selectByExample(weekend);
    }
}
