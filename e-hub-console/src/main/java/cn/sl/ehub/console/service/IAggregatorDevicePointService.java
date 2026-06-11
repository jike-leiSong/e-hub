package cn.sl.ehub.console.service;

import cn.sl.ehub.common.vo.DeviceGroupPointInfo;
import cn.sl.ehub.service.vo.AggregatorEntDevice;

import java.util.List;

/**
 * 设备测点Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorDevicePointService {

    /**
     * 查询设备测点
     *
     * @param stationId
     * @param ids
     * @return
     */
    List<DeviceGroupPointInfo> getDeviceGroupPointInfoList(String stationId, String ids);

    /**
     * 查询设备测点
     *
     * @param stationId
     * @param ids
     * @return
     */
    List<DeviceGroupPointInfo> getDeviceGroupPointInfoList(String stationId, String ids, String metricCode);

    /**
     * 查询设备测点
     *
     * @param aggregatorEntDeviceList
     * @param metricCode
     * @return
     */
    List<DeviceGroupPointInfo> getDeviceGroupPointInfoList(List<AggregatorEntDevice> aggregatorEntDeviceList, String metricCode);

    /**
     * 查询设备测点
     *
     * @param aggregatorEntDeviceList
     * @param metricCodeList
     * @return
     */
    List<DeviceGroupPointInfo> getDeviceGroupPointInfoList(List<AggregatorEntDevice> aggregatorEntDeviceList, List<String> metricCodeList);
}
