package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorDeviceDateIssueChartMapper;
import cn.sl.ehub.console.service.IAggregatorDeviceDateIssueChartService;
import cn.sl.ehub.service.vo.AggregatorDeviceDateIssueChart;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * 设备下发功率ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorDeviceDateIssueChartServiceImpl implements IAggregatorDeviceDateIssueChartService {

    private final AggregatorDeviceDateIssueChartMapper aggregatorDeviceDateIssueChartMapper;

    @Override
    public AggregatorDeviceDateIssueChart getAggregatorDeviceDateIssueChart(String deviceBaseId, String date) {
        Weekend<AggregatorDeviceDateIssueChart> weekend = Weekend.of(AggregatorDeviceDateIssueChart.class);
        WeekendCriteria<AggregatorDeviceDateIssueChart, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDateIssueChart::getDeviceBaseId, deviceBaseId);
        criteria.andEqualTo(AggregatorDeviceDateIssueChart::getDate, date);
        List<AggregatorDeviceDateIssueChart> aggregatorDeviceDateIssueChartList = aggregatorDeviceDateIssueChartMapper.selectByExample(weekend);
        if (null != aggregatorDeviceDateIssueChartList && aggregatorDeviceDateIssueChartList.size() > 0) {
            return aggregatorDeviceDateIssueChartList.get(0);
        }
        return null;
    }

    @Override
    public List<AggregatorDeviceDateIssueChart> getAggregatorDeviceDateIssueChartList(String deviceBaseId, List<String> dateList) {
        Weekend<AggregatorDeviceDateIssueChart> weekend = Weekend.of(AggregatorDeviceDateIssueChart.class);
        WeekendCriteria<AggregatorDeviceDateIssueChart, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDateIssueChart::getDeviceBaseId, deviceBaseId);
        criteria.andIn(AggregatorDeviceDateIssueChart::getDate, dateList);
        return aggregatorDeviceDateIssueChartMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorDeviceDateIssueChart> getAggregatorDeviceDateIssueChartList(List<String> aggregatorIdList, String date) {
        Weekend<AggregatorDeviceDateIssueChart> weekend = Weekend.of(AggregatorDeviceDateIssueChart.class);
        WeekendCriteria<AggregatorDeviceDateIssueChart, Object> criteria = weekend.weekendCriteria();
        criteria.andIn(AggregatorDeviceDateIssueChart::getAggregatorId, aggregatorIdList);
        criteria.andEqualTo(AggregatorDeviceDateIssueChart::getDate, date);
        return aggregatorDeviceDateIssueChartMapper.selectByExample(weekend);
    }

    @Override
    public AggregatorDeviceDateIssueChart getAggregatorDeviceDateIssueChart(String deviceBaseId) {
        Weekend<AggregatorDeviceDateIssueChart> weekend = Weekend.of(AggregatorDeviceDateIssueChart.class);
        WeekendCriteria<AggregatorDeviceDateIssueChart, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDateIssueChart::getDeviceBaseId, deviceBaseId);
        weekend.orderBy("date").desc();
        List<AggregatorDeviceDateIssueChart> aggregatorDeviceDateIssueChartList = aggregatorDeviceDateIssueChartMapper.selectByExampleAndRowBounds(weekend, new RowBounds(0, 1));
        if (null != aggregatorDeviceDateIssueChartList && aggregatorDeviceDateIssueChartList.size() > 0) {
            return aggregatorDeviceDateIssueChartList.get(0);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(String aggregatorId, String date) {
        Weekend<AggregatorDeviceDateIssueChart> weekendDelete = Weekend.of(AggregatorDeviceDateIssueChart.class);
        WeekendCriteria<AggregatorDeviceDateIssueChart, Object> criteriaDelete = weekendDelete.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorDeviceDateIssueChart::getAggregatorId, aggregatorId);
        criteriaDelete.andEqualTo(AggregatorDeviceDateIssueChart::getDate, date);
        return aggregatorDeviceDateIssueChartMapper.deleteByExample(weekendDelete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorDeviceDateIssueChart> aggregatorDeviceDateIssueChartList) {
        return aggregatorDeviceDateIssueChartMapper.batchInsert(aggregatorDeviceDateIssueChartList);
    }

    @Override
    public List<AggregatorDeviceDateIssueChart> getAggregatorDeviceDateIssueChartList(String aggregatorId, String resourceTypeId, List<String> dateList) {
        Weekend<AggregatorDeviceDateIssueChart> weekend = Weekend.of(AggregatorDeviceDateIssueChart.class);
        WeekendCriteria<AggregatorDeviceDateIssueChart, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDateIssueChart::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorDeviceDateIssueChart::getResourceTypeId, resourceTypeId);
        criteria.andIn(AggregatorDeviceDateIssueChart::getDate, dateList);
        return aggregatorDeviceDateIssueChartMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorDeviceDateIssueChart> getAggregatorDeviceDateIssueChartList(List<String> deviceBaseIdList, List<String> dateList) {
        Weekend<AggregatorDeviceDateIssueChart> weekend = Weekend.of(AggregatorDeviceDateIssueChart.class);
        WeekendCriteria<AggregatorDeviceDateIssueChart, Object> criteria = weekend.weekendCriteria();
        criteria.andIn(AggregatorDeviceDateIssueChart::getDeviceBaseId, deviceBaseIdList);
        criteria.andIn(AggregatorDeviceDateIssueChart::getDate, dateList);
        return aggregatorDeviceDateIssueChartMapper.selectByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByResourceTypeId(String resourceTypeId, String date) {
        Weekend<AggregatorDeviceDateIssueChart> weekendDelete = Weekend.of(AggregatorDeviceDateIssueChart.class);
        WeekendCriteria<AggregatorDeviceDateIssueChart, Object> criteriaDelete = weekendDelete.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorDeviceDateIssueChart::getResourceTypeId, resourceTypeId);
        criteriaDelete.andEqualTo(AggregatorDeviceDateIssueChart::getDate, date);
        return aggregatorDeviceDateIssueChartMapper.deleteByExample(weekendDelete);
    }

    @Override
    public List<AggregatorDeviceDateIssueChart> getAggregatorDeviceDateIssueChartListByResourceTypeId(String resourceTypeId, String date) {
        Weekend<AggregatorDeviceDateIssueChart> weekend = Weekend.of(AggregatorDeviceDateIssueChart.class);
        WeekendCriteria<AggregatorDeviceDateIssueChart, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDateIssueChart::getResourceTypeId, resourceTypeId);
        criteria.andEqualTo(AggregatorDeviceDateIssueChart::getDate, date);
        return aggregatorDeviceDateIssueChartMapper.selectByExample(weekend);
    }
}
