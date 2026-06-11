package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.IAggregatorEntBaseLineLoadChartMapper;
import cn.sl.ehub.service.mapper.IAggregatorVppBasePowerChartMapper;
import cn.sl.ehub.console.service.IAggregatorEntBaseLineLoadChartService;
import cn.sl.ehub.console.service.IAggregatorVppBasePowerChartService;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorVppBasePowerChart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
@RequiredArgsConstructor
@Service
public class AggregatorVppBasePowerChartServiceImpl implements IAggregatorVppBasePowerChartService {
    private final IAggregatorVppBasePowerChartMapper aggregatorVppBasePowerChartMapper;

    @Override
    public List<AggregatorVppBasePowerChart> getVppBasePowerBySystemCode(String systemCode) {
        Weekend<AggregatorVppBasePowerChart> weekend = Weekend.of(AggregatorVppBasePowerChart.class);
        WeekendCriteria<AggregatorVppBasePowerChart, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorVppBasePowerChart::getStationId, systemCode);
        List<AggregatorVppBasePowerChart> deviceList = aggregatorVppBasePowerChartMapper.selectByExample(weekend);
        if (null != deviceList && deviceList.size() > 0) {
            return deviceList;
        }
        return null;
    }

    @Override
    public List<AggregatorVppBasePowerChart> getVppBasePowerByEntId(String entId) {
        Weekend<AggregatorVppBasePowerChart> weekend = Weekend.of(AggregatorVppBasePowerChart.class);
        WeekendCriteria<AggregatorVppBasePowerChart, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorVppBasePowerChart::getEntId, entId);
        List<AggregatorVppBasePowerChart> deviceList = aggregatorVppBasePowerChartMapper.selectByExample(weekend);
        if (null != deviceList && deviceList.size() > 0) {
            return deviceList;
        }
        return null;
    }
}
