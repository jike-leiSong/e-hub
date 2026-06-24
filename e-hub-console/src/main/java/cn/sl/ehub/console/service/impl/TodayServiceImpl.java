package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.console.model.vo.EnergyStationInfoAndDevice;
import cn.sl.ehub.console.model.vo.UserInfoAndDevice;
import cn.sl.ehub.console.service.IAggregatorEntDeviceService;
import cn.sl.ehub.console.service.ITodayService;
import cn.sl.ehub.service.resp.AggregatorEntDeviceIotLogResp;
import cn.sl.ehub.service.resp.EntUserDeviceTodayChartResp;
import cn.sl.ehub.service.resp.EntUserDeviceTodayElectricCurrentChartResp;
import cn.sl.ehub.service.resp.EntUserDeviceYesterdayChartResp;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodayServiceImpl implements ITodayService {

    private final IAggregatorEntDeviceService aggregatorEntDeviceService;

    @Override
    public EntUserDeviceTodayChartResp getEntUserDeviceTodayChartResp(String deviceBaseId) {
        return emptyTodayChart();
    }

    @Override
    public EntUserDeviceTodayChartResp getDeviceTreeTodayChartResp(String deviceBaseId, String energyStationcode, String systemCode) {
        return emptyTodayChart();
    }

    @Override
    public List<AggregatorEntDeviceIotLogResp> getIotLog(String entId, String stationId, String resourceTypeId, String deviceBaseId) {
        return Collections.emptyList();
    }

    @Override
    public List<UserInfoAndDevice> getDevices(String aggregatorId, String resourceType) {
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getDeviceListByAggregatorId(aggregatorId, resourceType);
        if (CollectionUtils.isEmpty(deviceList)) {
            return Collections.emptyList();
        }

        Map<String, List<AggregatorEntDevice>> devicesByEntId = deviceList.stream()
                .collect(Collectors.groupingBy(AggregatorEntDevice::getEntId));
        List<UserInfoAndDevice> result = new ArrayList<>();
        devicesByEntId.forEach((entId, entDevices) -> {
            AggregatorEntDevice firstDevice = entDevices.get(0);
            UserInfoAndDevice user = new UserInfoAndDevice();
            user.setEntId(entId);
            user.setDeviceBaseId(firstDevice.getStationId());
            user.setDeviceName(firstDevice.getUsername());
            user.setDeviceType("3");
            user.setApplyStatus("0");
            user.setWinStatu(false);
            user.setChildren(toDeviceNodes(entDevices));
            result.add(user);
        });
        return result;
    }

    private List<EnergyStationInfoAndDevice> toDeviceNodes(List<AggregatorEntDevice> deviceList) {
        List<EnergyStationInfoAndDevice> result = new ArrayList<>();
        for (AggregatorEntDevice device : deviceList) {
            EnergyStationInfoAndDevice node = new EnergyStationInfoAndDevice();
            node.setDeviceBaseId(device.getDeviceBaseId());
            node.setDeviceName(device.getDeviceName());
            node.setResourceTypeId(device.getResourceTypeId());
            node.setDeviceType("1");
            node.setChildren(Collections.singletonList(device));
            result.add(node);
        }
        return result;
    }

    private EntUserDeviceTodayChartResp emptyTodayChart() {
        EntUserDeviceTodayChartResp resp = new EntUserDeviceTodayChartResp();
        EntUserDeviceYesterdayChartResp powerResp = new EntUserDeviceYesterdayChartResp();
        powerResp.setBaseLineChart(Collections.emptyList());
        powerResp.setIssueChart(Collections.emptyList());
        powerResp.setPowerChart(Collections.emptyList());
        powerResp.setTimeColorRespList(Collections.emptyList());
        resp.setEntUserDeviceYesterdayChartResp(powerResp);
        resp.setNoPowerChart(Collections.emptyList());
        resp.setZeroPointElectricityQuantityChart(Collections.emptyList());

        EntUserDeviceTodayElectricCurrentChartResp currentResp = new EntUserDeviceTodayElectricCurrentChartResp();
        currentResp.setIaList(Collections.emptyList());
        currentResp.setIbList(Collections.emptyList());
        currentResp.setIcList(Collections.emptyList());
        resp.setEntUserDeviceTodayElectricCurrentChartResp(currentResp);
        return resp;
    }
}
