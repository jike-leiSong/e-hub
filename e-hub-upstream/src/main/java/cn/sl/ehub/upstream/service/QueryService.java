package cn.sl.ehub.upstream.service;

import cn.sl.ehub.service.mapper.AggregatorEntDeviceMapper;
import cn.sl.ehub.service.mapper.AggregatorInfoMapper;
import cn.sl.ehub.service.mapper.AggregatorResourceTypeMapper;
import cn.sl.ehub.service.mapper.EnergyStationInfoMapper;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorInfo;
import cn.sl.ehub.service.vo.AggregatorResourceType;

import cn.sl.ehub.service.vo.EnergyStationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
@Service
public class QueryService {

    @Autowired
    AggregatorResourceTypeMapper aggregatorResourceTypeMapper;
    @Autowired
    AggregatorInfoMapper aggregatorInfoMapper;
    @Autowired
    AggregatorEntDeviceMapper aggregatorEntDeviceMapper;
    @Autowired
    EnergyStationInfoMapper energyStationInfoMapper;
    public List<AggregatorResourceType> getAggregatorResourceTypeListByAggregatorId(String aggregatorId) {
        Weekend<AggregatorResourceType> weekend = Weekend.of(AggregatorResourceType.class);
        WeekendCriteria<AggregatorResourceType, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorResourceType::getDisplay,1)
                .andEqualTo(AggregatorResourceType::getAggregatorId,aggregatorId);
        weekend.orderBy("resourceOrder");
        return aggregatorResourceTypeMapper.selectByExample(weekend);
    }

    public List<AggregatorInfo> getAggregatorInfoByAggregatorId(String aggregatorId) {
        Weekend<AggregatorInfo> weekend = Weekend.of(AggregatorInfo.class);
        WeekendCriteria<AggregatorInfo, Object> criteria = weekend.weekendCriteria();
        criteria
                .andEqualTo(AggregatorInfo::getAggregatorId,aggregatorId);
        return aggregatorInfoMapper.selectByExample(weekend);
    }

    public List<AggregatorEntDevice> getAggregatorEntDeviceByArea(String aggregatorId,String stateGridCode){
        Weekend<AggregatorEntDevice> weekend = Weekend.of(AggregatorEntDevice.class);
        WeekendCriteria<AggregatorEntDevice, Object> criteria = weekend.weekendCriteria();
        criteria
                .andEqualTo(AggregatorEntDevice::getAggregatorId,aggregatorId)
                .andEqualTo(AggregatorEntDevice::getStateGridCode,stateGridCode);
        return aggregatorEntDeviceMapper.selectByExample(weekend);
    }

    /**
     * 根据能源站编码查询能源站信息
     * @param energyStationCode
     * @return
     */
    public List<EnergyStationInfo> getEnergyStationInfoByEnergyStationCode(String energyStationCode){
        Weekend<EnergyStationInfo> weekend = Weekend.of(EnergyStationInfo.class);
        WeekendCriteria<EnergyStationInfo, Object> criteria = weekend.weekendCriteria();
        criteria
                .andEqualTo(EnergyStationInfo::getEnergyStationCode,energyStationCode);
        return energyStationInfoMapper.selectByExample(weekend);
    }

}
