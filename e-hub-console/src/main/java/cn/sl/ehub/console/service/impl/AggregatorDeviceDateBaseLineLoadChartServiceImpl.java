package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorDeviceDateBaseLineLoadChartMapper;
import cn.sl.ehub.console.service.IAggregatorDeviceDateBaseLineLoadChartService;
import cn.sl.ehub.service.vo.AggregatorDeviceDateBaseLineLoadChart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * 设备基线负荷
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorDeviceDateBaseLineLoadChartServiceImpl implements IAggregatorDeviceDateBaseLineLoadChartService {

    private final AggregatorDeviceDateBaseLineLoadChartMapper aggregatorDeviceDateBaseLineLoadChartMapper;

    @Override
    public List<AggregatorDeviceDateBaseLineLoadChart> getAggregatorDeviceDateBaseLineLoadChartList(List<String> deviceBaseIdList, String date) {
        Weekend<AggregatorDeviceDateBaseLineLoadChart> weekend = Weekend.of(AggregatorDeviceDateBaseLineLoadChart.class);
        WeekendCriteria<AggregatorDeviceDateBaseLineLoadChart, Object> criteria = weekend.weekendCriteria();
        criteria.andIn(AggregatorDeviceDateBaseLineLoadChart::getDeviceBaseId, deviceBaseIdList);
        criteria.andLessThanOrEqualTo(AggregatorDeviceDateBaseLineLoadChart::getStartDate, date);
        criteria.andGreaterThanOrEqualTo(AggregatorDeviceDateBaseLineLoadChart::getEndDate, date);
        return aggregatorDeviceDateBaseLineLoadChartMapper.selectByExample(weekend);
    }

    @Override
    public AggregatorDeviceDateBaseLineLoadChart getAggregatorDeviceDateBaseLineLoadChart(String deviceBaseId, String date) {
        Weekend<AggregatorDeviceDateBaseLineLoadChart> weekend = Weekend.of(AggregatorDeviceDateBaseLineLoadChart.class);
        WeekendCriteria<AggregatorDeviceDateBaseLineLoadChart, Object> criteriaProfit = weekend.weekendCriteria();
        criteriaProfit.andEqualTo(AggregatorDeviceDateBaseLineLoadChart::getDeviceBaseId, deviceBaseId);
        criteriaProfit.andLessThanOrEqualTo(AggregatorDeviceDateBaseLineLoadChart::getStartDate, date);
        criteriaProfit.andGreaterThanOrEqualTo(AggregatorDeviceDateBaseLineLoadChart::getEndDate, date);
        List<AggregatorDeviceDateBaseLineLoadChart> aggregatorDeviceDateBaseLineLoadChartList = aggregatorDeviceDateBaseLineLoadChartMapper.selectByExample(weekend);
        if (null != aggregatorDeviceDateBaseLineLoadChartList && aggregatorDeviceDateBaseLineLoadChartList.size() > 0) {
            return aggregatorDeviceDateBaseLineLoadChartList.get(0);
        }
        return null;
    }

    @Override
    public List<AggregatorDeviceDateBaseLineLoadChart> getAggregatorDeviceDateBaseLineLoadChartList(List<String> deviceBaseIdList, List<String> dateList) {
        return aggregatorDeviceDateBaseLineLoadChartMapper.getAggregatorDeviceDateBaseLineLoadChartList(deviceBaseIdList, dateList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(AggregatorDeviceDateBaseLineLoadChart aggregatorDeviceDateBaseLineLoadChart) {
        return aggregatorDeviceDateBaseLineLoadChartMapper.insertSelective(aggregatorDeviceDateBaseLineLoadChart);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(String deviceBaseId, String date) {
        Weekend<AggregatorDeviceDateBaseLineLoadChart> weekend = Weekend.of(AggregatorDeviceDateBaseLineLoadChart.class);
        WeekendCriteria<AggregatorDeviceDateBaseLineLoadChart, Object> criteriaProfit = weekend.weekendCriteria();
        criteriaProfit.andEqualTo(AggregatorDeviceDateBaseLineLoadChart::getDeviceBaseId, deviceBaseId);
        criteriaProfit.andLessThanOrEqualTo(AggregatorDeviceDateBaseLineLoadChart::getStartDate, date);
        criteriaProfit.andGreaterThanOrEqualTo(AggregatorDeviceDateBaseLineLoadChart::getEndDate, date);
        return aggregatorDeviceDateBaseLineLoadChartMapper.deleteByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<String> deviceBaseIdList, String date) {
        Weekend<AggregatorDeviceDateBaseLineLoadChart> weekend = Weekend.of(AggregatorDeviceDateBaseLineLoadChart.class);
        WeekendCriteria<AggregatorDeviceDateBaseLineLoadChart, Object> criteriaProfit = weekend.weekendCriteria();
        criteriaProfit.andIn(AggregatorDeviceDateBaseLineLoadChart::getDeviceBaseId, deviceBaseIdList);
        criteriaProfit.andLessThanOrEqualTo(AggregatorDeviceDateBaseLineLoadChart::getStartDate, date);
        criteriaProfit.andGreaterThanOrEqualTo(AggregatorDeviceDateBaseLineLoadChart::getEndDate, date);
        aggregatorDeviceDateBaseLineLoadChartMapper.deleteByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorDeviceDateBaseLineLoadChart> aggregatorDeviceDateBaseLineLoadChartList) {
        aggregatorDeviceDateBaseLineLoadChartList.forEach(aggregatorDeviceDateBaseLineLoadChartMapper::insert);
        return aggregatorDeviceDateBaseLineLoadChartList.size();
    }
}
