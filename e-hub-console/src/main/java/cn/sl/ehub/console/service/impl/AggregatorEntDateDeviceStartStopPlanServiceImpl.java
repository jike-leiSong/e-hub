package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorEntDateDeviceStartStopPlanMapper;
import cn.sl.ehub.console.service.IAggregatorEntDateDeviceStartStopPlanService;
import cn.sl.ehub.service.vo.AggregatorEntDateDeviceStartStopPlan;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * 设备启停计划ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorEntDateDeviceStartStopPlanServiceImpl implements IAggregatorEntDateDeviceStartStopPlanService {

    private final AggregatorEntDateDeviceStartStopPlanMapper aggregatorEntDateDeviceStartStopPlanMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorEntDateDeviceStartStopPlan> aggregatorEntDateDeviceStartStopPlanList) {
        return aggregatorEntDateDeviceStartStopPlanMapper.batchInsert(aggregatorEntDateDeviceStartStopPlanList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(String entId, List<String> dateList) {
        Weekend<AggregatorEntDateDeviceStartStopPlan> weekend = Weekend.of(AggregatorEntDateDeviceStartStopPlan.class);
        WeekendCriteria<AggregatorEntDateDeviceStartStopPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDateDeviceStartStopPlan::getEntId, entId);
        criteria.andIn(AggregatorEntDateDeviceStartStopPlan::getDate, dateList);
        return aggregatorEntDateDeviceStartStopPlanMapper.deleteByExample(weekend);
    }

    @Override
    public AggregatorEntDateDeviceStartStopPlan getAggregatorEntDateDeviceStartStopPlan(String entId, String date) {
        Weekend<AggregatorEntDateDeviceStartStopPlan> weekend = Weekend.of(AggregatorEntDateDeviceStartStopPlan.class);
        WeekendCriteria<AggregatorEntDateDeviceStartStopPlan, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDateDeviceStartStopPlan::getEntId, entId);
        criteria.andEqualTo(AggregatorEntDateDeviceStartStopPlan::getDate, date);
        List<AggregatorEntDateDeviceStartStopPlan> aggregatorEntDateDeviceStartStopPlanList = aggregatorEntDateDeviceStartStopPlanMapper.selectByExample(weekend);
        if (CollectionUtils.isNotEmpty(aggregatorEntDateDeviceStartStopPlanList)) {
            return aggregatorEntDateDeviceStartStopPlanList.get(0);
        }
        return null;
    }
}
