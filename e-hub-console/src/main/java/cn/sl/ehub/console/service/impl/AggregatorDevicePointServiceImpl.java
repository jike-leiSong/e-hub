package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.vo.DeviceGroupPointInfo;
import cn.sl.ehub.console.service.IAggregatorDevicePointService;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AggregatorDevicePointServiceImpl implements IAggregatorDevicePointService {

    @Override
    public List<DeviceGroupPointInfo> getDeviceGroupPointInfoList(String stationId, String ids) {
        return Collections.emptyList();
    }

    @Override
    public List<DeviceGroupPointInfo> getDeviceGroupPointInfoList(String stationId, String ids, String metricCode) {
        return Collections.emptyList();
    }

    @Override
    public List<DeviceGroupPointInfo> getDeviceGroupPointInfoList(List<AggregatorEntDevice> aggregatorEntDeviceList, String metricCode) {
        return Collections.emptyList();
    }

    @Override
    public List<DeviceGroupPointInfo> getDeviceGroupPointInfoList(List<AggregatorEntDevice> aggregatorEntDeviceList, List<String> metricCodeList) {
        return Collections.emptyList();
    }
}
