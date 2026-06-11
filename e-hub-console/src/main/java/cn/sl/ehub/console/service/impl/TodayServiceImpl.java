package cn.sl.ehub.console.service.impl;

import cn.enn.bigdata.resp.BigDataHistoryAndCalculationResp;
import cn.enn.bigdata.resp.BigDataHistoryResp;
import cn.enn.bigdata.service.IBigDataHandlerService;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.common.vo.DeviceGroupPointInfo;
import cn.sl.ehub.console.enums.ApplyStatusEnum;

import cn.sl.ehub.console.enums.MetricEnum;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.service.mapper.AggregatorEntDeviceIotLogMapper;
import cn.sl.ehub.console.model.vo.AggregatorDevicesVO;
import cn.sl.ehub.console.model.vo.EnergyStationInfoAndDevice;
import cn.sl.ehub.console.model.vo.UserInfoAndDevice;
import cn.sl.ehub.service.resp.AggregatorEntDeviceIotLogResp;
import cn.sl.ehub.service.resp.EntUserDeviceTodayChartResp;
import cn.sl.ehub.service.resp.EntUserDeviceTodayElectricCurrentChartResp;
import cn.sl.ehub.console.service.*;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.service.vo.*;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 曲线图ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class TodayServiceImpl implements ITodayService {

    private final IYesterdayService yesterdayService;
    private final IBigDataHandlerService bigDataHistoryService;
    private final IAggregatorEntDeviceService aggregatorEntDeviceService;
    private final AggregatorEntDeviceIotLogMapper aggregatorEntDeviceIotLogMapper;
    private final IAggregatorDevicePointService aggregatorDevicePointService;
    private final IAggregatorEntDateApplyDetailService aggregatorEntDateApplyDetailService;
    private final IAggregatorEntDapChartService aggregatorEntDapChartService;

    @Resource(name = "threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor executor;

    @Override
    public EntUserDeviceTodayChartResp getEntUserDeviceTodayChartResp(String simulate, String deviceBaseId) {
        EntUserDeviceTodayChartResp resp = new EntUserDeviceTodayChartResp();
        String dateStr = DateUtils.getDay();
        String startTime = dateStr + " 00:00:00";
        String endTime = DateUtils.getAddDate(dateStr) + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime);
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(Arrays.asList(deviceBaseId));
        CountDownLatch countDownLatch = new CountDownLatch(2);
        //有功功率
        executor.execute(() -> {
            try {
                resp.setEntUserDeviceYesterdayChartResp(yesterdayService.getEntUserDeviceChartResp(simulate, deviceBaseId, deviceList, dateStr));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        });
        //查询大数据
        executor.execute(() -> {
            try {
                Map<String, List<DataResp>> metricDataRespListMap = new HashMap<>();
                List<DeviceGroupPointInfo> deviceGroupPointInfoList = aggregatorDevicePointService.getDeviceGroupPointInfoList(deviceList, Arrays.asList(MetricEnum.NO_POWER.getCode(), MetricEnum.IA.getCode(), MetricEnum.IB.getCode(), MetricEnum.IC.getCode(), MetricEnum.ZERO_POINT_ELECTRIC_QUANTITY.getCode()));
                List<BigDataHistoryAndCalculationResp> bigDataHistoryAndCalculationRespList = bigDataHistoryService.queryBigData(deviceList, deviceGroupPointInfoList, "1minute", startTime, endTime, simulate);
                if (null != bigDataHistoryAndCalculationRespList && bigDataHistoryAndCalculationRespList.size() > 0) {
                    metricDataRespListMap = bigDataHistoryAndCalculationRespList.stream().collect(Collectors.toMap(history -> history.getMetric().split("\\.")[1], history -> history.getDataResp(), (v1, v2) -> v1));
                }
                if (null == metricDataRespListMap || metricDataRespListMap.size() <= 0) {
                    metricDataRespListMap = new HashMap<>();
                }
                //无功功率
                List<DataResp> dataRespListNoPower = metricDataRespListMap.get(MetricEnum.NO_POWER.getCode());
                resp.setNoPowerChart(getDataRespList(dataRespListNoPower, minuteList, deviceList.get(0).getResourceTypeId()));
                //用电电流
                EntUserDeviceTodayElectricCurrentChartResp electricCurrentChartResp = new EntUserDeviceTodayElectricCurrentChartResp();
                List<DataResp> iaList = metricDataRespListMap.get(MetricEnum.IA.getCode());
                electricCurrentChartResp.setIaList(getDataRespList(iaList, minuteList, deviceList.get(0).getResourceTypeId()));
                List<DataResp> ibList = metricDataRespListMap.get(MetricEnum.IB.getCode());
                electricCurrentChartResp.setIbList(getDataRespList(ibList, minuteList, deviceList.get(0).getResourceTypeId()));
                List<DataResp> icList = metricDataRespListMap.get(MetricEnum.IC.getCode());
                electricCurrentChartResp.setIcList(getDataRespList(icList, minuteList, deviceList.get(0).getResourceTypeId()));
                resp.setEntUserDeviceTodayElectricCurrentChartResp(electricCurrentChartResp);
                //当日零点电量
                List<DataResp> dataRespListZeroPointElectricQuantity = metricDataRespListMap.get(MetricEnum.ZERO_POINT_ELECTRIC_QUANTITY.getCode());
                resp.setZeroPointElectricityQuantityChart(getDataRespListByEptp(dataRespListZeroPointElectricQuantity, minuteList));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new BaseException(StatusCode.ERROR.getCode(), "今日曲线请求失败！");
        }
        return resp;
    }

    @Override
    public EntUserDeviceTodayChartResp getDeviceTreeTodayChartResp(String simulate, String deviceBaseId,String energyStationcode,String systemCode) {
        EntUserDeviceTodayChartResp result = new EntUserDeviceTodayChartResp();
        if(StrUtil.isNotEmpty(deviceBaseId)){
            result = getEntUserDeviceTodayChartResp(simulate, deviceBaseId);
            return result;
        }
        if(StrUtil.isNotEmpty(energyStationcode)){
            List<AggregatorEntDevice> deviceByStationCodeList = aggregatorEntDeviceService.getDeviceByStationCode(energyStationcode);
            if(CollectionUtils.isEmpty(deviceByStationCodeList)){
                return result;
            }

            List<String> deviceBaseIdList= deviceByStationCodeList.stream().map(e -> e.getDeviceBaseId()).collect(Collectors.toList());
            result = getEntUserDeviceListTodayChartResp(simulate, deviceBaseIdList);
            return result;
        }
        if(StrUtil.isNotEmpty(systemCode)){
            List<AggregatorEntDevice> deviceBySystemCodeList = aggregatorEntDeviceService.getDeviceBySystemCode(systemCode);
            if(CollectionUtils.isEmpty(deviceBySystemCodeList)){
                return result;
            }
            List<String> deviceBaseIdList= deviceBySystemCodeList.stream().map(e -> e.getDeviceBaseId()).collect(Collectors.toList());
            result = getEntUserTodayChartResp(simulate, deviceBaseIdList,systemCode);
            return result;
        }
        return result;
    }

    @Override
    public List<AggregatorEntDeviceIotLogResp> getIotLog(String entId, String stationId, String resourceTypeId, String deviceBaseId) {
        List<AggregatorEntDeviceIotLogResp> respList = Lists.newArrayList();
        String date = DateUtils.getDay();
        String startTime = date + " 00:00:00";
        String endTime = date + " 23:59:59";
        Weekend<AggregatorEntDeviceIotLog> weekend = Weekend.of(AggregatorEntDeviceIotLog.class);
        WeekendCriteria<AggregatorEntDeviceIotLog, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDeviceIotLog::getEntId, entId);
        criteria.andEqualTo(AggregatorEntDeviceIotLog::getStationId, stationId);
        criteria.andEqualTo(AggregatorEntDeviceIotLog::getResourceTypeId, resourceTypeId);
        if (StringUtils.isNotEmpty(deviceBaseId)) {
            criteria.andEqualTo(AggregatorEntDeviceIotLog::getDeviceBaseId, deviceBaseId);
        }
        criteria.andBetween(AggregatorEntDeviceIotLog::getSendTime, startTime, endTime);
        List<AggregatorEntDeviceIotLog> iotLogList = aggregatorEntDeviceIotLogMapper.selectByExample(weekend);
        if (null != iotLogList && iotLogList.size() > 0) {
            iotLogList.forEach(iotLog -> {
                AggregatorEntDeviceIotLogResp resp = new AggregatorEntDeviceIotLogResp();
                resp.setDeviceName(iotLog.getDeviceName());
                resp.setResultMsg(iotLog.getResultMsg());
                resp.setSendTime(StringUtils.isEmpty(iotLog.getSendTime()) ? iotLog.getSendTime() : DateUtils.format(iotLog.getSendTime(), "HH:mm"));
                respList.add(resp);
            });
        }
        if (null != respList && respList.size() > 0) {
            Collections.sort(respList);
        }
        return respList;
    }



    /**
     * 处理数据
     *
     * @param dataRespList
     * @param minuteList
     * @param resourceTypeId
     * @return
     */
    private List<DataResp> getDataRespList(List<DataResp> dataRespList, List<String> minuteList, String resourceTypeId) {
        List<DataResp> resultList = Lists.newArrayList();
        Map<String, Double> timeValueMap = new HashMap<>();
        if (null != dataRespList && dataRespList.size() > 0) {
            timeValueMap = dataRespList.stream().filter(dataResp -> null != dataResp && StringUtils.isNotEmpty(dataResp.getTime()))
                    .collect(Collectors.toMap(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm"), dataResp -> null == dataResp.getValue() ? null : MathUtils.doublePoint(dataResp.getValue(), 2)));
        }
        if (null == timeValueMap || timeValueMap.size() <= 0) {
            timeValueMap = new HashMap<>();
        }
        Map<String, Double> finalTimeValueMap = timeValueMap;
//        minuteList.forEach(minute -> {
//            DataResp dataResp = new DataResp();
//            dataResp.setTime(DateUtils.format(minute, "HH:mm"));
//            dataResp.setValue(finalTimeValueMap.get(dataResp.getTime()));
//            resultList.add(dataResp);
//        });
        for (int i = 1; i < minuteList.size(); i++) {
            DataResp dataResp = new DataResp();
            dataResp.setTime(DateUtils.format(minuteList.get(i), "HH:mm"));
            dataResp.setValue(finalTimeValueMap.get(dataResp.getTime()));
            if (StringUtils.isNotEmpty(resourceTypeId) && resourceTypeId.equals("27")) {
                dataResp.setValue(null == dataResp.getValue() ? null : 0 - dataResp.getValue());
            }
            resultList.add(dataResp);
        }
        return resultList;
    }

    /**
     * 处理数据
     *
     * @param dataRespList
     * @param minuteList
     * @return
     */
    private List<DataResp> getDataRespListByEptp(List<DataResp> dataRespList, List<String> minuteList) {
        List<DataResp> resultList = Lists.newArrayList();
        Map<String, Double> timeValueMap = new HashMap<>();
        if (null != dataRespList && dataRespList.size() > 0) {
            timeValueMap = dataRespList.stream().filter(dataResp -> null != dataResp && StringUtils.isNotEmpty(dataResp.getTime()))
                    .collect(Collectors.toMap(dataResp -> DateUtils.format(dataResp.getTime(), "HH:mm"), dataResp -> null == dataResp.getValue() ? null : MathUtils.doublePoint(dataResp.getValue(), 2)));
        }
        if (null == timeValueMap || timeValueMap.size() <= 0) {
            timeValueMap = new HashMap<>();
        }
        Map<String, Double> finalTimeValueMap = timeValueMap;
        Double firstValue = finalTimeValueMap.get(DateUtils.format(minuteList.get(0), "HH:mm"));
        for (int i = 1; i < minuteList.size() - 1; i++) {
            DataResp dataResp = new DataResp();
            dataResp.setTime(DateUtils.format(minuteList.get(i), "HH:mm"));
            dataResp.setValue(MathUtils.subDouble(finalTimeValueMap.get(dataResp.getTime()), firstValue));
            if (null != dataResp.getValue()) {
                dataResp.setValue(MathUtils.doublePoint(dataResp.getValue(), 2));
            }
            resultList.add(dataResp);
        }
        return resultList;
    }

    @Override
    public List<UserInfoAndDevice> getDevices(String aggregatorId, String resourceType) {
        List<UserInfoAndDevice> result = new ArrayList<>();
        // modify by sl 2024-04-28 根据资源类型查询设备
        List<AggregatorEntDevice> AggregatorEntDeviceList = aggregatorEntDeviceService.getDeviceListByAggregatorId(aggregatorId, resourceType);
        if(CollectionUtils.isEmpty(AggregatorEntDeviceList)){
            return result;
        }
        Map<String, List<AggregatorEntDevice>> AggregatorEntDeviceGroupByStationId = AggregatorEntDeviceList.stream().collect(Collectors.groupingBy(AggregatorEntDevice::getStationId));
        List<String> entIdList = AggregatorEntDeviceList.stream().map(e -> e.getEntId()).collect(Collectors.toList());
        String dateStr = DateUtils.getDay();
        //查询企业用户申报状态
        Map<String, AggregatorEntDateApplyDetail> entIdApplyDetailMap = getEntIdApplyDetailMap(entIdList, dateStr);
        //查询企业用户中标状态
        Map<String, AggregatorEntDapChart> entIdWinStatuMap = getEntIdWinStatuMap(entIdList, dateStr);
        for (Map.Entry<String, List<AggregatorEntDevice>> stringListEntry : AggregatorEntDeviceGroupByStationId.entrySet()) {
            UserInfoAndDevice userInfoAndDevice = new UserInfoAndDevice();
            String stationId = stringListEntry.getKey();
            List<AggregatorEntDevice> stationDeviceList = stringListEntry.getValue();
            AggregatorEntDevice aggregatorEntDevice = stationDeviceList.get(0);
            userInfoAndDevice.setEntId(aggregatorEntDevice.getEntId());
            userInfoAndDevice.setDeviceBaseId(stationId);
            userInfoAndDevice.setDeviceName(aggregatorEntDevice.getUsername());
            userInfoAndDevice.setDeviceType("3");
            userInfoAndDevice.setApplyStatus("0");
            AggregatorEntDateApplyDetail applyDetail = entIdApplyDetailMap.get(aggregatorEntDevice.getEntId());
            if (null != applyDetail && applyDetail.getApplyStatus().equals("1")) {
                userInfoAndDevice.setApplyStatus("1");
            }
            AggregatorEntDapChart winDetail = entIdWinStatuMap.get(aggregatorEntDevice.getEntId());
            if (!Objects.isNull(winDetail)&&!Objects.isNull(winDetail.getDapChart())) {
                userInfoAndDevice.setWinStatu(true);
            }
            //直接挂在企业下的设备
            List<AggregatorEntDevice> deviceInfos = stationDeviceList.stream().filter(e -> StrUtil.isEmpty(e.getEnergyStation())).collect(Collectors.toList());
            List<EnergyStationInfoAndDevice> energyStationInfoAndDeviceList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(deviceInfos)){
                for (AggregatorEntDevice deviceInfo : deviceInfos) {
                    EnergyStationInfoAndDevice energyStationInfoAndDevice = new EnergyStationInfoAndDevice();
                    energyStationInfoAndDevice.setDeviceBaseId(deviceInfo.getDeviceBaseId());
                    energyStationInfoAndDevice.setDeviceName(deviceInfo.getDeviceName());
                    energyStationInfoAndDevice.setResourceTypeId(deviceInfo.getResourceTypeId());
                    energyStationInfoAndDevice.setDeviceType("1");
                    energyStationInfoAndDeviceList.add(energyStationInfoAndDevice);
                }
            }
            List<AggregatorEntDevice> energyStationDeviceInfos = stationDeviceList.stream().filter(e -> StrUtil.isNotEmpty(e.getEnergyStation())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(energyStationDeviceInfos)){
                userInfoAndDevice.setChildren(energyStationInfoAndDeviceList);
                result.add(userInfoAndDevice);
                continue;
            }
            Map<String, List<AggregatorEntDevice>> energyStationMap = energyStationDeviceInfos.stream().collect(Collectors.groupingBy(AggregatorEntDevice::getEnergyStationCode));
            for (Map.Entry<String, List<AggregatorEntDevice>> listEntry : energyStationMap.entrySet()) {
                EnergyStationInfoAndDevice energyStationInfoAndDevice = new EnergyStationInfoAndDevice();
                String energyStationCode = listEntry.getKey();
                List<AggregatorEntDevice> energyStationDevices = listEntry.getValue();
                energyStationDevices.stream().map(e-> {
                    e.setDeviceType("1");
                    return e;}).collect(Collectors.toList());
                AggregatorEntDevice energyStationDevice = energyStationDevices.get(0);
                energyStationInfoAndDevice.setDeviceBaseId(energyStationCode);
                energyStationInfoAndDevice.setDeviceName(energyStationDevice.getEnergyStation());
                energyStationInfoAndDevice.setDeviceType("0");
                energyStationInfoAndDevice.setChildren(energyStationDevices);
                energyStationInfoAndDeviceList.add(energyStationInfoAndDevice);
            }
            userInfoAndDevice.setChildren(energyStationInfoAndDeviceList);
            result.add(userInfoAndDevice);
        }
        List<UserInfoAndDevice> resultOrder = result.stream()
                .sorted(Comparator.comparing(UserInfoAndDevice::getWinStatu, Comparator.nullsLast(Comparator.comparing(Boolean::booleanValue).reversed())))
                .collect(Collectors.toList());
        return resultOrder;
    }

    public EntUserDeviceTodayChartResp getEntUserDeviceListTodayChartResp(String simulate, List<String> deviceBaseIdList) {
        EntUserDeviceTodayChartResp resp = new EntUserDeviceTodayChartResp();
        String dateStr = DateUtils.getDay();
        String startTime = dateStr + " 00:00:00";
        String endTime = DateUtils.getAddDate(dateStr) + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime);
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(deviceBaseIdList);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        //有功功率
        executor.execute(() -> {
            try {
                resp.setEntUserDeviceYesterdayChartResp(yesterdayService.getEntUserDeviceListChartResp(simulate, deviceList, dateStr));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new BaseException(StatusCode.ERROR.getCode(), "今日曲线请求失败！");
        }
        return resp;
    }

    public EntUserDeviceTodayChartResp getEntUserTodayChartResp(String simulate, List<String> deviceBaseIdList,String stationId) {
        EntUserDeviceTodayChartResp resp = new EntUserDeviceTodayChartResp();
        String dateStr = DateUtils.getDay();
        String startTime = dateStr + " 00:00:00";
        String endTime = DateUtils.getAddDate(dateStr) + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime);
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(deviceBaseIdList);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        //有功功率
        executor.execute(() -> {
            try {
                resp.setEntUserDeviceYesterdayChartResp(yesterdayService.getEntUserChartResp(simulate, deviceList, dateStr,stationId));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new BaseException(StatusCode.ERROR.getCode(), "今日曲线请求失败！");
        }
        return resp;
    }

    /**
     * 查询企业用户申报状态
     *
     * @param entIdList
     * @return
     */
    private Map<String, AggregatorEntDateApplyDetail> getEntIdApplyDetailMap(List<String> entIdList, String date) {
        Map<String, AggregatorEntDateApplyDetail> entIdApplyDetailMap = new HashMap<>();
        List<AggregatorEntDateApplyDetail> aggregatorEntDateApplyDetailList = aggregatorEntDateApplyDetailService.getAggregatorEntDateApplyDetailList(entIdList, date);
        if (null != aggregatorEntDateApplyDetailList && aggregatorEntDateApplyDetailList.size() > 0) {
            entIdApplyDetailMap = aggregatorEntDateApplyDetailList.stream().collect(Collectors.toMap(AggregatorEntDateApplyDetail::getEntId, Function.identity(), (v1, v2) -> v1));
            if (null == entIdApplyDetailMap || entIdApplyDetailMap.size() <= 0) {
                entIdApplyDetailMap = new HashMap<>();
            }
        }
        return entIdApplyDetailMap;
    }

    private Map<String,AggregatorEntDapChart> getEntIdWinStatuMap(List<String> entIdList, String date){
        Map<String,AggregatorEntDapChart> result = new HashMap<>();
        List<AggregatorEntDapChart> aggregatorEntDapChartList = aggregatorEntDapChartService.getBatchDapLineByEntId(entIdList, date);
        if (CollectionUtils.isNotEmpty(aggregatorEntDapChartList)) {
            result = aggregatorEntDapChartList.stream().collect(Collectors.toMap(AggregatorEntDapChart::getEntId, Function.identity(), (v1, v2) -> v1));
            if (null == result || result.size() <= 0) {
                result = new HashMap<>();
            }
        }
        return result;
    }

}
