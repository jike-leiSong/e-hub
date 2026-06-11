package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorEntDeviceApplyPlanMapper;
import cn.sl.ehub.console.service.IAggregatorEntDeviceApplyPlanService;
import cn.sl.ehub.service.vo.AggregatorEntDeviceApplyPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * 设备申报计划ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorEntDeviceApplyPlanServiceImpl implements IAggregatorEntDeviceApplyPlanService {

    private final AggregatorEntDeviceApplyPlanMapper aggregatorEntDeviceApplyPlanMapper;

    @Override
    public List<AggregatorEntDeviceApplyPlan> getAggregatorEntDeviceApplyPlanList(String entId, String date, String deviceBaseId) {
        Weekend<AggregatorEntDeviceApplyPlan> weekend = Weekend.of(AggregatorEntDeviceApplyPlan.class);
        WeekendCriteria<AggregatorEntDeviceApplyPlan, Object> criteriaEnt = weekend.weekendCriteria();
        criteriaEnt.andEqualTo(AggregatorEntDeviceApplyPlan::getEntId, entId);
        criteriaEnt.andEqualTo(AggregatorEntDeviceApplyPlan::getDate, date);
        criteriaEnt.andEqualTo(AggregatorEntDeviceApplyPlan::getDeviceBaseId, deviceBaseId);
        return aggregatorEntDeviceApplyPlanMapper.selectByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(String entId, List<String> dateList) {
        Weekend<AggregatorEntDeviceApplyPlan> weekend = Weekend.of(AggregatorEntDeviceApplyPlan.class);
        WeekendCriteria<AggregatorEntDeviceApplyPlan, Object> criteriaEnt = weekend.weekendCriteria();
        criteriaEnt.andEqualTo(AggregatorEntDeviceApplyPlan::getEntId, entId);
        criteriaEnt.andIn(AggregatorEntDeviceApplyPlan::getDate, dateList);
        return aggregatorEntDeviceApplyPlanMapper.deleteByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorEntDeviceApplyPlan> planList) {
        return aggregatorEntDeviceApplyPlanMapper.batchInsert(planList);
    }

    @Override
    public List<AggregatorEntDeviceApplyPlan> getAggregatorEntDeviceApplyPlanListByDate(String entId, String startDate, String endDate) {
        Weekend<AggregatorEntDeviceApplyPlan> weekend = Weekend.of(AggregatorEntDeviceApplyPlan.class);
        WeekendCriteria<AggregatorEntDeviceApplyPlan, Object> criteriaEnt = weekend.weekendCriteria();
        criteriaEnt.andEqualTo(AggregatorEntDeviceApplyPlan::getEntId, entId);
        criteriaEnt.andBetween(AggregatorEntDeviceApplyPlan::getDate, startDate, endDate);
        return aggregatorEntDeviceApplyPlanMapper.selectByExample(weekend);
    }
}
