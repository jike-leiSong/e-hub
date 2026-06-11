package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.vo.DeviceGroupDeviceInfo;
import cn.sl.ehub.service.vo.DeviceGroupInfo;
import cn.sl.ehub.common.vo.DeviceGroupPointInfo;
import cn.enn.cim.service.MetaService;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.console.service.IAggregatorDevicePointService;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.common.vo.ResultVO;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 设备测点ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorDevicePointServiceImpl implements IAggregatorDevicePointService {

    private final MetaService metaService;

    private final String path = "LoadDeviceGroupV1";

    @Override
    public List<DeviceGroupPointInfo> getDeviceGroupPointInfoList(String stationId, String ids) {
        List<DeviceGroupPointInfo> resultList = Lists.newArrayList();
        ResultVO<List<DeviceGroupInfo>> deviceGroupInfoListResult = metaService.metaCustomQuery(path, stationId, ids, null);
        if (null == deviceGroupInfoListResult || !deviceGroupInfoListResult.getCode().equals(StatusCode.SUCCESS.getCode()) || CollectionUtils.isEmpty(deviceGroupInfoListResult.getData())) {
            return resultList;
        }
        List<DeviceGroupInfo> deviceGroupInfoList = deviceGroupInfoListResult.getData();
        deviceGroupInfoList.forEach(deviceGroupInfo -> {
            List<DeviceGroupDeviceInfo> deviceGroupDeviceInfoList = deviceGroupInfo.getDeviceList();
            if (CollectionUtils.isNotEmpty(deviceGroupDeviceInfoList)) {
                deviceGroupDeviceInfoList.forEach(deviceGroupDeviceInfo -> {
                    List<DeviceGroupPointInfo> deviceGroupDeviceInfoPointList = deviceGroupDeviceInfo.getPointList();
                    if (CollectionUtils.isNotEmpty(deviceGroupDeviceInfoPointList)) {
                        resultList.addAll(deviceGroupDeviceInfoPointList);
                    }
                });
            }
        });
        return resultList;
    }

    @Override
    public List<DeviceGroupPointInfo> getDeviceGroupPointInfoList(String stationId, String ids, String metricCode) {
        List<DeviceGroupPointInfo> resultList = Lists.newArrayList();
        ResultVO<List<DeviceGroupInfo>> deviceGroupInfoListResult = metaService.metaCustomQuery(path, stationId, ids, null);
        if (null == deviceGroupInfoListResult || !deviceGroupInfoListResult.getCode().equals(StatusCode.SUCCESS.getCode()) || CollectionUtils.isEmpty(deviceGroupInfoListResult.getData())) {
            return resultList;
        }
        List<DeviceGroupInfo> deviceGroupInfoList = deviceGroupInfoListResult.getData();
        deviceGroupInfoList.forEach(deviceGroupInfo -> {
            List<DeviceGroupDeviceInfo> deviceGroupDeviceInfoList = deviceGroupInfo.getDeviceList();
            if (CollectionUtils.isNotEmpty(deviceGroupDeviceInfoList)) {
                deviceGroupDeviceInfoList.forEach(deviceGroupDeviceInfo -> {
                    List<DeviceGroupPointInfo> deviceGroupDeviceInfoPointList = deviceGroupDeviceInfo.getPointList();
                    if (CollectionUtils.isNotEmpty(deviceGroupDeviceInfoPointList)) {
                        List<DeviceGroupPointInfo> deviceGroupPointInfoList = deviceGroupDeviceInfoPointList.stream().filter(point -> null != point && point.getPointCode().equals(metricCode)).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(deviceGroupDeviceInfoPointList)) {
                            resultList.addAll(deviceGroupPointInfoList);
                        }
                    }
                });
            }
        });
        return resultList;
    }

    @Override
    public List<DeviceGroupPointInfo> getDeviceGroupPointInfoList(List<AggregatorEntDevice> aggregatorEntDeviceList, String metricCode) {
        List<DeviceGroupPointInfo> resultList = Lists.newArrayList();
        Map<String, String> stationIdMap = aggregatorEntDeviceList.stream().collect(Collectors.toMap(AggregatorEntDevice::getStationId, AggregatorEntDevice::getDeviceBaseId, (v1, v2) -> v1 + "," + v2));
        stationIdMap.entrySet().forEach(stationIdEntryMap -> {
            ResultVO<List<DeviceGroupInfo>> deviceGroupInfoListResult = metaService.metaCustomQuery(path, stationIdEntryMap.getKey(), stationIdEntryMap.getValue(), null);
            if (null != deviceGroupInfoListResult && deviceGroupInfoListResult.getCode().equals(StatusCode.SUCCESS.getCode()) && CollectionUtils.isNotEmpty(deviceGroupInfoListResult.getData())) {
                List<DeviceGroupInfo> deviceGroupInfoList = deviceGroupInfoListResult.getData();
                deviceGroupInfoList.forEach(deviceGroupInfo -> {
                    List<DeviceGroupDeviceInfo> deviceGroupDeviceInfoList = deviceGroupInfo.getDeviceList();
                    if (CollectionUtils.isNotEmpty(deviceGroupDeviceInfoList)) {
                        deviceGroupDeviceInfoList.forEach(deviceGroupDeviceInfo -> {
                            List<DeviceGroupPointInfo> deviceGroupDeviceInfoPointList = deviceGroupDeviceInfo.getPointList();
                            if (CollectionUtils.isNotEmpty(deviceGroupDeviceInfoPointList)) {
                                List<DeviceGroupPointInfo> deviceGroupPointInfoList = deviceGroupDeviceInfoPointList.stream().filter(point -> null != point && point.getPointCode().equals(metricCode)).collect(Collectors.toList());
                                if (CollectionUtils.isNotEmpty(deviceGroupDeviceInfoPointList)) {
                                    resultList.addAll(deviceGroupPointInfoList);
                                }
                            }
                        });
                    }
                });
            }
        });
        return resultList;
    }

    @Override
    public List<DeviceGroupPointInfo> getDeviceGroupPointInfoList(List<AggregatorEntDevice> aggregatorEntDeviceList, List<String> metricCodeList) {
        List<DeviceGroupPointInfo> resultList = Lists.newArrayList();
        List<String> deviceBaseIdList = aggregatorEntDeviceList.stream().map(AggregatorEntDevice::getDeviceBaseId).collect(Collectors.toList());
        Map<String, String> stationIdMap = aggregatorEntDeviceList.stream().collect(Collectors.toMap(AggregatorEntDevice::getStationId, AggregatorEntDevice::getDeviceBaseId, (v1, v2) -> v1 + "," + v2));
        stationIdMap.entrySet().forEach(stationIdEntryMap -> {
            ResultVO<List<DeviceGroupInfo>> deviceGroupInfoListResult = metaService.metaCustomQuery(path, stationIdEntryMap.getKey(), stationIdEntryMap.getValue(), null);
            if (null != deviceGroupInfoListResult && deviceGroupInfoListResult.getCode().equals(StatusCode.SUCCESS.getCode()) && CollectionUtils.isNotEmpty(deviceGroupInfoListResult.getData())) {
                List<DeviceGroupInfo> deviceGroupInfoList = deviceGroupInfoListResult.getData();
                deviceGroupInfoList.forEach(deviceGroupInfo -> {
                    List<DeviceGroupDeviceInfo> deviceGroupDeviceInfoList = deviceGroupInfo.getDeviceList();
                    if (CollectionUtils.isNotEmpty(deviceGroupDeviceInfoList)) {
                        deviceGroupDeviceInfoList.stream().filter(deviceGroupDeviceInfo -> null != deviceGroupDeviceInfo && deviceBaseIdList.contains(deviceGroupDeviceInfo.getDeviceId())).forEach(deviceGroupDeviceInfo -> {
                            List<DeviceGroupPointInfo> deviceGroupDeviceInfoPointList = deviceGroupDeviceInfo.getPointList();
                            if (CollectionUtils.isNotEmpty(deviceGroupDeviceInfoPointList)) {
                                List<DeviceGroupPointInfo> deviceGroupPointInfoList = deviceGroupDeviceInfoPointList.stream().filter(point -> null != point && metricCodeList.contains(point.getPointCode())).collect(Collectors.toList());
                                if (CollectionUtils.isNotEmpty(deviceGroupDeviceInfoPointList)) {
                                    resultList.addAll(deviceGroupPointInfoList);
                                }
                            }
                        });
                    }
                });
            }
        });
        return resultList;
    }
}
