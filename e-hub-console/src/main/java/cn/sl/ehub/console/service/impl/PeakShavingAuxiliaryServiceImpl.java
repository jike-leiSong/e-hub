package cn.sl.ehub.console.service.impl;

import cn.enn.bigdata.resp.BigDataHistoryAndCalculationResp;
import cn.enn.bigdata.resp.BigDataHistoryResp;
import cn.enn.bigdata.service.IBigDataHandlerService;
import cn.sl.ehub.common.enums.EnergyModelEnum;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.common.vo.DeviceGroupPointInfo;
import cn.sl.ehub.console.enums.MetricEnum;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.service.resp.AggregatorDeviceChartResp;
import cn.sl.ehub.service.resp.AggregatorEntApplyPlanResp;
import cn.sl.ehub.console.service.*;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.service.vo.*;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * 调峰辅助服务ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PeakShavingAuxiliaryServiceImpl implements IPeakShavingAuxiliaryService {

    private final IAggregatorDeviceDateIssueChartService aggregatorDeviceDateIssueChartService;
    private final IAggregatorEntDeviceService aggregatorEntDeviceService;
    private final IBigDataHandlerService bigDataHistoryService;
    private final IAggregatorDevicePointService aggregatorDevicePointService;
    private final IAggregatorDeviceDateBaseLineLoadChartService aggregatorDeviceDateBaseLineLoadChartService;
    private final IAggregatorDeviceDateDeliveryChartService aggregatorDeviceDateDeliveryChartService;
    @Resource(name = "threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor executor;

    @Override
    public AggregatorDeviceChartResp getPowerChartResp(String simulate, String deviceBaseId, String startDate, String endDate, boolean historyStatus) {
        AggregatorDeviceChartResp resp = new AggregatorDeviceChartResp();
        List<String> dateList = DateUtils.getDayList(startDate, endDate);
        String startTime = startDate + " 00:01:00";
        String endTime = DateUtils.getAddDate(endDate) + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime);
        AggregatorEntDevice aggregatorEntDevice = aggregatorEntDeviceService.getAggregatorEntDevice(deviceBaseId);
        resp.setResourceTypeId(aggregatorEntDevice.getResourceTypeId());
        resp.setDeviceBaseId(aggregatorEntDevice.getDeviceBaseId());
        resp.setDeviceName(aggregatorEntDevice.getDeviceName());
        resp.setDeviceId(aggregatorEntDevice.getDeviceId());
        resp.setPowerChart(Lists.newArrayList());
        resp.setIssueChart(Lists.newArrayList());
        resp.setIssueUseChart(Lists.newArrayList());
        resp.setDataSource(aggregatorEntDevice.getDataSource());
        resp.setDeviceType(aggregatorEntDevice.getDeviceType());
        resp.setStationId(aggregatorEntDevice.getStationId());
        CountDownLatch countDownLatch = new CountDownLatch(4);
        //实际功率
        executor.execute(() -> {
            try {
                getPowerChart(aggregatorEntDevice, minuteList, simulate, resp);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        });
        //设备下发功率
        executor.execute(() -> {
            try {
                getIssueChart(deviceBaseId, dateList, minuteList, historyStatus, resp);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        });
        //设备基线负荷
        executor.execute(() -> {
            try {
                getBaseLineChart(deviceBaseId, dateList, minuteList, historyStatus, resp);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        });
        //设备申报功率
        executor.execute(() -> {
            try {
                getDeliveryChart(deviceBaseId, dateList, minuteList, historyStatus, resp);
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
            throw new BaseException(StatusCode.ERROR.getCode(), "历史功率请求失败！");
        }
        return resp;
    }

    @Override
    public List<AggregatorDeviceChartResp> getNowPowerChartResp(String simulate, String entId, String startDate, String endDate) {
        List<AggregatorDeviceChartResp> aggregatorDeviceChartRespList = Lists.newArrayList();
        List<String> dateList = DateUtils.getDayList(startDate, endDate);
        String startTime = startDate + " 00:01:00";
        String endTime = DateUtils.getAddDate(endDate) + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime);
        //查询设备
        List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(entId);
        if (null == aggregatorEntDeviceList || aggregatorEntDeviceList.size() <= 0) {
            return aggregatorDeviceChartRespList;
        }
        aggregatorEntDeviceList.forEach(device -> {
            AggregatorDeviceChartResp resp = new AggregatorDeviceChartResp();
            resp.setResourceTypeId(device.getResourceTypeId());
            resp.setDeviceBaseId(device.getDeviceBaseId());
            resp.setDeviceName(device.getDeviceName());
            resp.setDeviceId(device.getDeviceId());
            resp.setPowerChart(Lists.newArrayList());
            resp.setIssueChart(Lists.newArrayList());
            resp.setIssueUseChart(Lists.newArrayList());
            resp.setDataSource(device.getDataSource());
            resp.setDeviceType(device.getDeviceType());
            resp.setStationId(device.getStationId());
            aggregatorDeviceChartRespList.add(resp);
        });
        CountDownLatch countDownLatch = new CountDownLatch(4);
        //实际功率
        executor.execute(() -> {
            try {
                getPowerChart(aggregatorEntDeviceList, minuteList, simulate, aggregatorDeviceChartRespList);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        });
        //设备下发功率
        executor.execute(() -> {
            try {
                getIssueChart(aggregatorEntDeviceList, dateList, minuteList, aggregatorDeviceChartRespList);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        });
        //设备基线负荷
        executor.execute(() -> {
            try {
                getDeviceBaseLineChart(aggregatorEntDeviceList, dateList, minuteList, aggregatorDeviceChartRespList);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        });
        //设备申报功率
        executor.execute(() -> {
            try {
                getDeliveryChart(aggregatorEntDeviceList, dateList, minuteList, aggregatorDeviceChartRespList);
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
            throw new BaseException(StatusCode.ERROR.getCode(), "今日功率请求失败！");
        }
        return aggregatorDeviceChartRespList;
    }

    @Override
    public PageResultVO<AggregatorDeviceChartResp> getNowPowerChartRespPage(String simulate, String entId, String startDate, String endDate, Integer pageNo, Integer pageSize) {
        PageResultVO<AggregatorDeviceChartResp> pageResultVO = new PageResultVO<>();
        pageResultVO.setPageIndex(pageNo);
        pageResultVO.setPageSize(pageSize);
        pageResultVO.setTotal(0);
        List<String> dateList = DateUtils.getDayList(startDate, endDate);
        String startTime = startDate + " 00:01:00";
        String endTime = DateUtils.getAddDate(endDate) + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime);
        //查询设备
        PageHelper.startPage(pageNo, pageSize);
        List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(entId);
        if (CollectionUtils.isEmpty(aggregatorEntDeviceList)) {
            pageResultVO.setList(new ArrayList<>());
            return pageResultVO;
        }
        pageResultVO.setTotal((int) ((Page<AggregatorEntDevice>) aggregatorEntDeviceList).getTotal());
        List<AggregatorDeviceChartResp> aggregatorDeviceChartRespList = Lists.newArrayList();
        aggregatorEntDeviceList.forEach(device -> {
            AggregatorDeviceChartResp resp = new AggregatorDeviceChartResp();
            resp.setResourceTypeId(device.getResourceTypeId());
            resp.setDeviceBaseId(device.getDeviceBaseId());
            resp.setDeviceName(device.getDeviceName());
            resp.setDeviceId(device.getDeviceId());
            resp.setPowerChart(Lists.newArrayList());
            resp.setIssueChart(Lists.newArrayList());
            resp.setIssueUseChart(Lists.newArrayList());
            resp.setDataSource(device.getDataSource());
            resp.setDeviceType(device.getDeviceType());
            resp.setStationId(device.getStationId());
            aggregatorDeviceChartRespList.add(resp);
        });
        CountDownLatch countDownLatch = new CountDownLatch(4);
        //实际功率
        executor.execute(() -> {
            try {
                getPowerChart(aggregatorEntDeviceList, minuteList, simulate, aggregatorDeviceChartRespList);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        });
        //设备下发功率
        executor.execute(() -> {
            try {
                getIssueChart(aggregatorEntDeviceList, dateList, minuteList, aggregatorDeviceChartRespList);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        });
        //设备基线负荷
        executor.execute(() -> {
            try {
                getDeviceBaseLineChart(aggregatorEntDeviceList, dateList, minuteList, aggregatorDeviceChartRespList);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        });
        //设备申报功率
        executor.execute(() -> {
            try {
                getDeliveryChart(aggregatorEntDeviceList, dateList, minuteList, aggregatorDeviceChartRespList);
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
            throw new BaseException(StatusCode.ERROR.getCode(), "今日功率请求失败！");
        }
        pageResultVO.setList(aggregatorDeviceChartRespList);
        return pageResultVO;
    }

    /**
     * 设备下发功率
     *
     * @param aggregatorEntDeviceList
     * @param dateList
     * @param minuteList
     * @param aggregatorDeviceChartRespList
     */
    private void getIssueChart(List<AggregatorEntDevice> aggregatorEntDeviceList, List<String> dateList, List<String> minuteList, List<AggregatorDeviceChartResp> aggregatorDeviceChartRespList) {
        Map<String, Map<String, Double>> deviceBaseIdTimeValueMap = new HashMap<>();
        List<String> deviceBaseIdList = aggregatorEntDeviceList.stream().map(AggregatorEntDevice::getDeviceBaseId).collect(Collectors.toList());
        List<AggregatorDeviceDateIssueChart> aggregatorDeviceDateIssueChartList = aggregatorDeviceDateIssueChartService.getAggregatorDeviceDateIssueChartList(deviceBaseIdList, dateList);
        if (null != aggregatorDeviceDateIssueChartList && aggregatorDeviceDateIssueChartList.size() > 0) {
            aggregatorDeviceDateIssueChartList.stream().filter(chart -> null != chart && StringUtils.isNotEmpty(chart.getIssueChart())).forEach(aggregatorDateIssueChart -> {
                String issueChart = aggregatorDateIssueChart.getIssueChart();
                List<DataResp> dataRespList = JSONArray.parseArray(issueChart, DataResp.class);
                if (null != dataRespList && dataRespList.size() > 0) {
                    Map<String, Double> timeValueMap = dataRespList.stream().collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1));
                    deviceBaseIdTimeValueMap.put(aggregatorDateIssueChart.getDeviceBaseId(), timeValueMap);
                }
            });
        }
        aggregatorDeviceChartRespList.forEach(chart -> {
            List<DataResp> issueChartList = Lists.newArrayList();
            Map<String, Double> timeValueMap = deviceBaseIdTimeValueMap.get(chart.getDeviceBaseId());
            if (null == timeValueMap) {
                timeValueMap = new HashMap<>();
            }
            for (int i = minuteList.size() - 1; i > 0; i -= 15) {
                for (int j = i; j >= i - 14; j--) {
                    //下发功率
                    DataResp dataResp = new DataResp();
                    dataResp.setTime(DateUtils.format(minuteList.get(j), "HH:mm"));
                    dataResp.setValue(timeValueMap.get(minuteList.get(i)));
                    issueChartList.add(0, dataResp);
                }
            }
            chart.setIssueChart(issueChartList);
        });
    }

    /**
     * 设备申报功率
     *
     * @param aggregatorEntDeviceList
     * @param dateList
     * @param minuteList
     * @param aggregatorDeviceChartRespList
     */
    private void getDeliveryChart(List<AggregatorEntDevice> aggregatorEntDeviceList, List<String> dateList, List<String> minuteList, List<AggregatorDeviceChartResp> aggregatorDeviceChartRespList) {
        Map<String, Map<String, Double>> deviceBaseIdTimeValueMap = new HashMap<>();
        List<String> deviceBaseIdList = aggregatorEntDeviceList.stream().map(AggregatorEntDevice::getDeviceBaseId).collect(Collectors.toList());
        List<AggregatorDeviceDateDeliveryChart> aggregatorDeviceDateDeliveryChartList = aggregatorDeviceDateDeliveryChartService.getAggregatorDeviceDateDeliveryChartList(deviceBaseIdList, dateList);
        if (null != aggregatorDeviceDateDeliveryChartList && aggregatorDeviceDateDeliveryChartList.size() > 0) {
            aggregatorDeviceDateDeliveryChartList.stream().filter(chart -> null != chart && StringUtils.isNotEmpty(chart.getDeliveryChart())).forEach(chart -> {
                String deliveryChart = chart.getDeliveryChart();
                List<DataResp> dataRespList = JSONArray.parseArray(deliveryChart, DataResp.class);
                if (null != dataRespList && dataRespList.size() > 0) {
                    Map<String, Double> timeValueMap = dataRespList.stream().collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1));
                    deviceBaseIdTimeValueMap.put(chart.getDeviceBaseId(), timeValueMap);
                }
            });
        }
        aggregatorDeviceChartRespList.forEach(chart -> {
            List<DataResp> deliveryChartList = Lists.newArrayList();
            Map<String, Double> timeValueMap = deviceBaseIdTimeValueMap.get(chart.getDeviceBaseId());
            if (null == timeValueMap) {
                timeValueMap = new HashMap<>();
            }
            for (int i = minuteList.size() - 1; i > 0; i -= 15) {
                for (int j = i; j >= i - 14; j--) {
                    //申报功率
                    DataResp dataResp = new DataResp();
                    dataResp.setTime(DateUtils.format(minuteList.get(j), "HH:mm"));
                    dataResp.setValue(timeValueMap.get(minuteList.get(i)));
                    deliveryChartList.add(0, dataResp);
                }
            }
            chart.setDeliveryChart(deliveryChartList);
        });
    }

    /**
     * 设备基线负荷
     *
     * @param aggregatorEntDeviceList
     * @param dateList
     * @param minuteList
     * @param aggregatorDeviceChartRespList
     */
    private void getDeviceBaseLineChart(List<AggregatorEntDevice> aggregatorEntDeviceList, List<String> dateList, List<String> minuteList, List<AggregatorDeviceChartResp> aggregatorDeviceChartRespList) {
        Map<String, Map<String, Double>> deviceBaseIdTimeValueMap = new HashMap<>();
        List<String> deviceBaseIdList = aggregatorEntDeviceList.stream().map(AggregatorEntDevice::getDeviceBaseId).collect(Collectors.toList());
        List<AggregatorDeviceDateBaseLineLoadChart> aggregatorDeviceDateBaseLineLoadChartList = aggregatorDeviceDateBaseLineLoadChartService.getAggregatorDeviceDateBaseLineLoadChartList(deviceBaseIdList, dateList);
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDateBaseLineLoadChartList)) {
            deviceBaseIdTimeValueMap = getLoadChartMap(aggregatorDeviceDateBaseLineLoadChartList, dateList);
        }
        if (null == deviceBaseIdTimeValueMap) {
            deviceBaseIdTimeValueMap = new HashMap<>();
        }
        Map<String, Map<String, Double>> finalDeviceBaseIdTimeValueMap = deviceBaseIdTimeValueMap;
        aggregatorDeviceChartRespList.forEach(chart -> {
            List<DataResp> baseLineChartList = Lists.newArrayList();
            Map<String, Double> timeValueMap = finalDeviceBaseIdTimeValueMap.get(chart.getDeviceBaseId());
            if (null == timeValueMap) {
                timeValueMap = new HashMap<>();
            }
            for (int i = minuteList.size() - 1; i > 0; i -= 15) {
                for (int j = i; j >= i - 14; j--) {
                    //基线负荷
                    DataResp dataResp = new DataResp();
                    dataResp.setTime(DateUtils.format(minuteList.get(j), "HH:mm"));
                    dataResp.setValue(timeValueMap.get(minuteList.get(i)));
                    baseLineChartList.add(0, dataResp);
                }
            }
            chart.setBaseLineChartList(baseLineChartList);
        });
    }

    /**
     * 查询实际功率
     *
     * @param aggregatorEntDeviceList
     * @param minuteList
     * @param simulate
     * @param aggregatorDeviceChartRespList
     */
    private void getPowerChart(List<AggregatorEntDevice> aggregatorEntDeviceList, List<String> minuteList, String simulate, List<AggregatorDeviceChartResp> aggregatorDeviceChartRespList) {
        Map<String, Map<String, Double>> deviceIdTimeValueMap = new HashMap<>();
        List<DeviceGroupPointInfo> deviceGroupPointInfoList = aggregatorDevicePointService.getDeviceGroupPointInfoList(aggregatorEntDeviceList, MetricEnum.YES_POWER.getCode());
        List<BigDataHistoryAndCalculationResp> bigDataHistoryAndCalculationRespList = bigDataHistoryService.queryBigData(aggregatorEntDeviceList, deviceGroupPointInfoList, "1minute", minuteList.get(0), minuteList.get(minuteList.size() - 1), simulate);
        if (null != bigDataHistoryAndCalculationRespList && bigDataHistoryAndCalculationRespList.size() > 0) {
            Map<String, List<DataResp>> deviceIdMap = bigDataHistoryAndCalculationRespList
                    .stream()
                    .filter(bigDataHistoryResp -> null != bigDataHistoryResp)
                    .collect(Collectors.toMap(BigDataHistoryAndCalculationResp::getDeviceId, BigDataHistoryAndCalculationResp::getDataResp, (v1, v2) -> v1));
            for (Map.Entry<String, List<DataResp>> deviceIdMapEntry : deviceIdMap.entrySet()) {
                List<DataResp> dataRespList = deviceIdMapEntry.getValue();
                if (null != dataRespList && dataRespList.size() > 0) {
                    Map<String, Double> realPowerMap = dataRespList.stream().collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1));
                    deviceIdTimeValueMap.put(deviceIdMapEntry.getKey(), realPowerMap);
                }
            }
        }
        aggregatorDeviceChartRespList.forEach(chart -> {
            List<DataResp> powerList = Lists.newArrayList();
            Map<String, Double> timeValueMap = deviceIdTimeValueMap.get(chart.getDeviceId().split("_")[1]);
            if (null == timeValueMap) {
                timeValueMap = new HashMap<>();
            }
            Map<String, Double> finalTimeValueMap = timeValueMap;
            minuteList.forEach(minute -> {
                DataResp dataResp = new DataResp();
                dataResp.setTime(DateUtils.format(minute, "HH:mm"));
                dataResp.setValue(null == finalTimeValueMap.get(minute) ? null : MathUtils.doublePoint(finalTimeValueMap.get(minute), 2));
                if (chart.getResourceTypeId().equals(EnergyModelEnum.DISTRIBUTED_ENERGY.getChannelNo())) {
                    dataResp.setValue(null == dataResp.getValue() ? null : 0 - dataResp.getValue());
                }
                powerList.add(dataResp);
            });
            chart.setPowerChart(powerList);
        });
    }

    /**
     * 设备下发功率
     *
     * @param deviceBaseId
     * @param dateList
     * @param minuteList
     * @param resp
     */
    private void getIssueChart(String deviceBaseId, List<String> dateList, List<String> minuteList, boolean historyStatus, AggregatorDeviceChartResp resp) {
        List<DataResp> issueChartList = Lists.newArrayList();
        List<DataResp> issueChartUseList = Lists.newArrayList();
        Map<String, Double> dataRespMap = new HashMap<>();
        List<AggregatorDeviceDateIssueChart> aggregatorDeviceDateIssueChartList = aggregatorDeviceDateIssueChartService.getAggregatorDeviceDateIssueChartList(deviceBaseId, dateList);
        if (null != aggregatorDeviceDateIssueChartList && aggregatorDeviceDateIssueChartList.size() > 0) {
            List<DataResp> totalDataRespList = Lists.newArrayList();
            aggregatorDeviceDateIssueChartList.stream().filter(chart -> null != chart && StringUtils.isNotEmpty(chart.getIssueChart())).forEach(aggregatorDateIssueChart -> {
                String issueChart = aggregatorDateIssueChart.getIssueChart();
                List<DataResp> dataRespList = JSONArray.parseArray(issueChart, DataResp.class);
                if (null != dataRespList && dataRespList.size() > 0) {
                    totalDataRespList.addAll(dataRespList);
                }
            });
            if (null != totalDataRespList && totalDataRespList.size() > 0) {
                dataRespMap = totalDataRespList.stream().collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1 + v2));
            }
        }
        if (null == dataRespMap || dataRespMap.size() <= 0) {
            dataRespMap = new HashMap<>();
        }
        for (int i = minuteList.size() - 1; i > 0; i -= 15) {
            for (int j = i; j >= i - 14; j--) {
                //下发功率
                DataResp dataResp = new DataResp();
                dataResp.setTime(DateUtils.format(minuteList.get(j), "MM-dd HH:mm"));
                dataResp.setValue(dataRespMap.get(minuteList.get(i)));
                issueChartList.add(0, dataResp);
                if (historyStatus) {
                    //有效功率
                    DataResp dataRespUse = new DataResp();
                    dataRespUse.setTime(DateUtils.format(minuteList.get(j), "MM-dd HH:mm"));
                    dataRespUse.setValue(MathUtils.mulDoubleNull(dataResp.getValue(), 0.7, 2));
                    issueChartUseList.add(0, dataRespUse);
                }
            }
        }
        resp.setIssueChart(issueChartList);
        resp.setIssueUseChart(issueChartUseList);
    }

    /**
     * 设备下发功率
     *
     * @param deviceBaseId
     * @param dateList
     * @param minuteList
     * @param resp
     */
    private void getDeliveryChart(String deviceBaseId, List<String> dateList, List<String> minuteList, boolean historyStatus, AggregatorDeviceChartResp resp) {
        List<DataResp> deliveryChartList = Lists.newArrayList();
        Map<String, Double> dataRespMap = new HashMap<>();
        List<AggregatorDeviceDateDeliveryChart> aggregatorDeviceDateDeliveryChartList = aggregatorDeviceDateDeliveryChartService.getAggregatorDeviceDateDeliveryChartListByDeviceBaseId(deviceBaseId, dateList);
        if (null != aggregatorDeviceDateDeliveryChartList && aggregatorDeviceDateDeliveryChartList.size() > 0) {
            List<DataResp> totalDataRespList = Lists.newArrayList();
            aggregatorDeviceDateDeliveryChartList.stream().filter(chart -> null != chart && StringUtils.isNotEmpty(chart.getDeliveryChart())).forEach(aggregatorDateDeliveryChart -> {
                String deliveryChart = aggregatorDateDeliveryChart.getDeliveryChart();
                List<DataResp> dataRespList = JSONArray.parseArray(deliveryChart, DataResp.class);
                if (null != dataRespList && dataRespList.size() > 0) {
                    totalDataRespList.addAll(dataRespList);
                }
            });
            if (null != totalDataRespList && totalDataRespList.size() > 0) {
                dataRespMap = totalDataRespList.stream().collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1 + v2));
            }
        }
        if (null == dataRespMap || dataRespMap.size() <= 0) {
            dataRespMap = new HashMap<>();
        }
        for (int i = minuteList.size() - 1; i > 0; i -= 15) {
            for (int j = i; j >= i - 14; j--) {
                //申报功率
                DataResp dataResp = new DataResp();
                dataResp.setTime(DateUtils.format(minuteList.get(j), "MM-dd HH:mm"));
                dataResp.setValue(dataRespMap.get(minuteList.get(i)));
                deliveryChartList.add(0, dataResp);
            }
        }
        resp.setDeliveryChart(deliveryChartList);
    }

    /**
     * 设备基线负荷
     *
     * @param deviceBaseId
     * @param dateList
     * @param minuteList
     * @param resp
     */
    private void getBaseLineChart(String deviceBaseId, List<String> dateList, List<String> minuteList, boolean historyStatus, AggregatorDeviceChartResp resp) {
        Map<String, Map<String, Double>> deviceBaseIdTimeValueMap = new HashMap<>();
        List<DataResp> baseLineChartList = Lists.newArrayList();
        List<AggregatorDeviceDateBaseLineLoadChart> aggregatorDeviceDateBaseLineLoadChartList = aggregatorDeviceDateBaseLineLoadChartService.getAggregatorDeviceDateBaseLineLoadChartList(Arrays.asList(deviceBaseId), dateList);
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDateBaseLineLoadChartList)) {
            deviceBaseIdTimeValueMap = getLoadChartMap(aggregatorDeviceDateBaseLineLoadChartList, dateList);
        }
        if (null == deviceBaseIdTimeValueMap) {
            deviceBaseIdTimeValueMap = new HashMap<>();
        }
        Map<String, Double> timeValueMap = deviceBaseIdTimeValueMap.get(deviceBaseId);
        if (null == timeValueMap) {
            timeValueMap = new HashMap<>();
        }
        for (int i = minuteList.size() - 1; i > 0; i -= 15) {
            for (int j = i; j >= i - 14; j--) {
                //基线负荷
                DataResp dataResp = new DataResp();
                dataResp.setTime(DateUtils.format(minuteList.get(j), "MM-dd HH:mm"));
                dataResp.setValue(timeValueMap.get(minuteList.get(i)));
                baseLineChartList.add(0, dataResp);
            }
        }
        resp.setBaseLineChartList(baseLineChartList);
    }

    /**
     * 查询实际功率
     *
     * @param aggregatorEntDevice
     * @param minuteList
     * @param simulate
     * @param resp
     */
    private void getPowerChart(AggregatorEntDevice aggregatorEntDevice, List<String> minuteList, String simulate, AggregatorDeviceChartResp resp) {
        Map<String, Double> realPowerMap = new HashMap<>();
        List<DeviceGroupPointInfo> deviceGroupPointInfoList = aggregatorDevicePointService.getDeviceGroupPointInfoList(Arrays.asList(aggregatorEntDevice), MetricEnum.YES_POWER.getCode());
        List<BigDataHistoryAndCalculationResp> bigDataHistoryAndCalculationRespList = bigDataHistoryService.queryBigData(Arrays.asList(aggregatorEntDevice), deviceGroupPointInfoList, "1minute", minuteList.get(0), minuteList.get(minuteList.size() - 1), simulate);
        if (null != bigDataHistoryAndCalculationRespList && bigDataHistoryAndCalculationRespList.size() > 0) {
            BigDataHistoryAndCalculationResp bigDataHistoryResp = bigDataHistoryAndCalculationRespList.get(0);
            if (null != bigDataHistoryResp) {
                List<DataResp> dataRespList = bigDataHistoryResp.getDataResp();
                if (null != dataRespList && dataRespList.size() > 0) {
                    realPowerMap = dataRespList.stream().filter(dataResp -> null != dataResp && null != dataResp.getValue()).collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1));
                }
            }
        }
        if (null == realPowerMap) {
            realPowerMap = new HashMap<>();
        }
        List<DataResp> powerList = Lists.newArrayList();
        Map<String, Double> finalRealPowerMap = realPowerMap;
        minuteList.forEach(minute -> {
            DataResp dataResp = new DataResp();
            dataResp.setTime(DateUtils.format(minute, "MM-dd HH:mm"));
            dataResp.setValue(null == finalRealPowerMap.get(minute) ? null : MathUtils.doublePoint(finalRealPowerMap.get(minute), 2));
            if (aggregatorEntDevice.getResourceTypeId().equals(EnergyModelEnum.DISTRIBUTED_ENERGY.getChannelNo())) {
                dataResp.setValue(null == dataResp.getValue() ? null : 0 - dataResp.getValue());
            }
            powerList.add(dataResp);
        });
        resp.setPowerChart(powerList);
    }

    /**
     * 处理数据
     *
     * @param oldList
     * @param dateList
     * @return
     */
    private Map<String, Map<String, Double>> getLoadChartMap(List<AggregatorDeviceDateBaseLineLoadChart> oldList, List<String> dateList) {
        Map<String, Map<String, Double>> deviceMap = new HashMap<>();
        dateList.forEach(date -> {
            oldList.forEach(chart -> {
                Map<String, Double> dataRespMap = deviceMap.get(chart.getDeviceBaseId());
                if (null == dataRespMap) {
                    dataRespMap = new HashMap<>();
                }
                if (chart.getStartDate().compareTo(date) <= 0 && chart.getEndDate().compareTo(date) >= 0 && StringUtils.isNotEmpty(chart.getBaseLineLoadChart())) {
                    List<DataResp> dataRespList = JSONArray.parseArray(chart.getBaseLineLoadChart(), DataResp.class);
                    dataRespList.forEach(dataResp -> {
                        String time = DateUtils.format(dataResp.getTime(), "HH:mm:ss");
                        dataResp.setTime(date + " " + DateUtils.format(dataResp.getTime(), "HH:mm:ss"));
                        if (time.equals("00:00:00")) {
                            dataResp.setTime(DateUtils.getAddDate(date) + " " + DateUtils.format(dataResp.getTime(), "HH:mm:ss"));
                        }
                    });
                    dataRespMap.putAll(dataRespList.stream().collect(Collectors.toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1)));
                }
                deviceMap.put(chart.getDeviceBaseId(), dataRespMap);
            });
        });
        return deviceMap;
    }
}
