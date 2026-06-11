package cn.sl.ehub.console.service.impl;

import cn.enn.bigdata.enums.StrategyEnum;
import cn.enn.bigdata.req.HistoryReq;
import cn.enn.bigdata.req.OpentsdbReq;
import cn.enn.bigdata.resp.BigDataHistoryResp;
import cn.enn.bigdata.resp.BigDataResultVO;
import cn.enn.bigdata.resp.LineDataDTO;
import cn.enn.bigdata.resp.TagVO;
import cn.enn.bigdata.service.BigDataServiceContext;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.common.vo.DataResp;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 大数据历史数据查询ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
/*@Slf4j
//@Service
public class BigDataHistoryServiceImpl implements IBigDataHistoryService {

    private final BigDataServiceContext bigDataServiceContext;

    public BigDataHistoryServiceImpl(BigDataServiceContext bigDataServiceContext) {
        this.bigDataServiceContext = bigDataServiceContext;
    }

    @Value("${bigdata.userKey}")
    private String userKey;

    @Override
    public List<BigDataHistoryResp> getHistory(HistoryReq historyReq, String simulate) {
        List<BigDataHistoryResp> respList = Lists.newArrayList();
        historyReq.setUserKey(userKey);
        BigDataResultVO<List<LineDataDTO>> result = bigDataServiceContext.getHistory(historyReq, StrategyEnum.getServiceName(simulate));
        if (null != result && null != result.getRetCode() && result.getRetCode().equals(0)) {
            List<LineDataDTO> lineDataDTOList = result.getData();
            if (null != lineDataDTOList && lineDataDTOList.size() > 0) {
                lineDataDTOList.forEach(lineDataDTO -> {
                    BigDataHistoryResp resp = new BigDataHistoryResp();
                    resp.setMetric(lineDataDTO.getMetric());
                    TagVO tagVO = lineDataDTO.getTags();
                    if (null != tagVO) {
                        resp.setEquipID(tagVO.getEquipID());
                        resp.setEquipMK(tagVO.getEquipMK());
                        resp.setStaId(tagVO.getStaId());
                    }
                    TreeMap<String, Double> dpsTreeMap = lineDataDTO.getDps();
                    if (null != dpsTreeMap && dpsTreeMap.size() > 0) {
                        List<DataResp> dataRespList = Lists.newArrayList();
                        dpsTreeMap.entrySet().forEach(dpsMap -> {
                            DataResp dataResp = new DataResp();
                            try {
                                dataResp.setTime(DateUtils.stampToDate(dpsMap.getKey()));
                            } catch (Exception e) {
                                log.info("大数据返回时间转换异常：值：{}，参数：{}", dpsMap.getValue(), JSONObject.toJSONString(historyReq));
                            }
                            try {
                                dataResp.setValue(dpsMap.getValue());
                            } catch (Exception e) {
                                log.info("大数据返回值转换异常：值：{}，参数：{}", dpsMap.getValue(), JSONObject.toJSONString(historyReq));
                            }
                            dataRespList.add(dataResp);
                        });
                        resp.setDataResp(dataRespList);
                    }
                    respList.add(resp);
                });
            }
        }
        return respList;
    }

    @Override
    public List<BigDataHistoryResp> getHistory(List<HistoryReq> historyReqList, String simulate) {
        List<BigDataHistoryResp> respList = Lists.newArrayList();
        historyReqList.forEach(historyReq -> {
            historyReq.setUserKey(userKey);
            BigDataResultVO<List<LineDataDTO>> result = bigDataServiceContext.getHistory(historyReq, StrategyEnum.getServiceName(simulate));
            if (null != result && null != result.getRetCode() && result.getRetCode().equals(0)) {
                List<LineDataDTO> lineDataDTOList = result.getData();
                if (null != lineDataDTOList && lineDataDTOList.size() > 0) {
                    lineDataDTOList.forEach(lineDataDTO -> {
                        BigDataHistoryResp resp = new BigDataHistoryResp();
                        resp.setMetric(lineDataDTO.getMetric());
                        TagVO tagVO = lineDataDTO.getTags();
                        if (null != tagVO) {
                            resp.setEquipID(tagVO.getEquipID());
                            resp.setEquipMK(tagVO.getEquipMK());
                            resp.setStaId(tagVO.getStaId());
                        }
                        TreeMap<String, Double> dpsTreeMap = lineDataDTO.getDps();
                        if (null != dpsTreeMap && dpsTreeMap.size() > 0) {
                            List<DataResp> dataRespList = Lists.newArrayList();
                            dpsTreeMap.entrySet().forEach(dpsMap -> {
                                DataResp dataResp = new DataResp();
                                try {
                                    dataResp.setTime(DateUtils.stampToDate(dpsMap.getKey()));
                                } catch (Exception e) {
                                    log.info("大数据返回时间转换异常：值：{}，参数：{}", dpsMap.getValue(), JSONObject.toJSONString(historyReq));
                                }
                                try {
                                    dataResp.setValue(dpsMap.getValue());
                                } catch (Exception e) {
                                    log.info("大数据返回值转换异常：值：{}，参数：{}", dpsMap.getValue(), JSONObject.toJSONString(historyReq));
                                }
                                dataRespList.add(dataResp);
                            });
                            resp.setDataResp(dataRespList);
                        }
                        respList.add(resp);
                    });
                }
            }
        });
        return respList;
    }

    @Override
    public List<BigDataHistoryResp> getBigData(List<AggregatorEntDevice> deviceList, List<String> metricList, String startTime, String endTime, String simulate) {
        if (null != deviceList && deviceList.size() > 0 && null != metricList && metricList.size() > 0) {
            Map<String, List<AggregatorEntDevice>> dataSourceMap = deviceList.stream().filter(device -> null != device && StringUtils.isNotEmpty(device.getDataSource())).collect(Collectors.groupingBy(AggregatorEntDevice::getDataSource));
            if (null != dataSourceMap && dataSourceMap.size() > 0) {
                List<HistoryReq> historyReqList = Lists.newArrayList();
                for (Map.Entry<String, List<AggregatorEntDevice>> dataSourceMapEntry : dataSourceMap.entrySet()) {
                    if (null != dataSourceMapEntry && null != dataSourceMapEntry.getValue() && dataSourceMapEntry.getValue().size() > 0) {
                        HistoryReq req = new HistoryReq();
                        req.setDataSource(dataSourceMapEntry.getKey());
                        req.setStartTime(startTime);
                        req.setEndTime(endTime);
                        List<OpentsdbReq> opentsdbReqList = Lists.newArrayList();
                        List<AggregatorEntDevice> aggregatorEntDeviceList = dataSourceMapEntry.getValue();
                        aggregatorEntDeviceList.forEach(device -> {
                            metricList.forEach(metric -> {
                                OpentsdbReq opentsdbReq = new OpentsdbReq();
                                opentsdbReq.setAggregator("none");
                                opentsdbReq.setDownsample("1m-first");
                                opentsdbReq.setExplicitTags("true");
                                opentsdbReq.setMetric(dataSourceMapEntry.getKey() + "." + metric);
                                TagVO tagVO = new TagVO();
                                tagVO.setEquipID(device.getDeviceId().split("_")[1]);
                                tagVO.setEquipMK(device.getDeviceType());
                                tagVO.setStaId(device.getStationId());
                                opentsdbReq.setTags(tagVO);
                                opentsdbReqList.add(opentsdbReq);
                            });
                        });
                        req.setListQueries(opentsdbReqList);
                        historyReqList.add(req);
                    }
                }
                if (null != historyReqList && historyReqList.size() > 0) {
                    log.info("请求大数据参数：{}", JSONObject.toJSONString(historyReqList));
                    return getHistory(historyReqList, simulate);
                }
            }
        }
        return Lists.newArrayList();
    }
}*/
