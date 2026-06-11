package cn.sl.ehub.console.service.impl;

import cn.enn.bigdata.req.OpentsdbReq;
import cn.enn.bigdata.req.RealTimeReq;
import cn.enn.bigdata.req.TagVO;
import cn.enn.bigdata.resp.BigDataRealTimeResp;
import cn.enn.bigdata.service.IBigDataHandlerService;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.console.enums.MetricEnum;
import cn.sl.ehub.service.resp.AggregatorRealTimeDataResp;
import cn.sl.ehub.console.service.IAggregatorRealTimeDateService;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

/**
 * 实时数据查询ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorRealTimeDateServiceImpl implements IAggregatorRealTimeDateService {

    private final IBigDataHandlerService bigDataHandlerService;

    @Override
    public List<AggregatorRealTimeDataResp> getDataByDeviceList(List<AggregatorEntDevice> deviceList, String simulate) {
        return getBigDataRealTime(deviceList, simulate);
    }

    /**
     * 查询大数据
     *
     * @param deviceList
     * @param simulate
     * @return
     */
    private List<AggregatorRealTimeDataResp> getBigDataRealTime(List<AggregatorEntDevice> deviceList, String simulate) {
        List<AggregatorRealTimeDataResp> aggregatorRealTimeDataRespList = Lists.newArrayList();
        List<RealTimeReq> realTimeReqList = Lists.newArrayList();
        Map<String, List<AggregatorEntDevice>> dataSourceMap = deviceList.stream().collect(groupingBy(AggregatorEntDevice::getDataSource));
        dataSourceMap.entrySet().forEach(dataSourceMapEntry -> {
            RealTimeReq realTimeReq = new RealTimeReq();
            realTimeReq.setDataSource(dataSourceMapEntry.getKey());
            List<OpentsdbReq> opentsdbReqList = Lists.newArrayList();
            dataSourceMapEntry.getValue().forEach(device -> {
                OpentsdbReq opentsdbReq = new OpentsdbReq();
                opentsdbReq.setAggregator("none");
                opentsdbReq.setDownsample("1m-first-null");
                opentsdbReq.setExplicitTags("true");
                opentsdbReq.setMetric(dataSourceMapEntry.getKey() + "." + MetricEnum.YES_POWER.getCode());
                TagVO tagVO = new TagVO();
                tagVO.setEquipID(device.getDeviceId().split("_")[1]);
                tagVO.setEquipMK(device.getDeviceType());
                tagVO.setStaId(device.getStationId());
                opentsdbReq.setTags(tagVO);
                opentsdbReqList.add(opentsdbReq);
            });
            realTimeReq.setListQueries(opentsdbReqList);
            realTimeReqList.add(realTimeReq);
        });

        // Process each RealTimeReq individually
        for (RealTimeReq req : realTimeReqList) {
            List<BigDataRealTimeResp> bigDataRealTimeRespList = bigDataHandlerService.getRealTime(req, simulate);
            if (CollectionUtils.isNotEmpty(bigDataRealTimeRespList)) {
                Map<String, DataResp> staIdMap = bigDataRealTimeRespList.stream()
                    .collect(toMap(
                        bigDataRealTimeResp -> bigDataRealTimeResp.getStaId() + "_" + bigDataRealTimeResp.getEquipMK() + "_" + bigDataRealTimeResp.getEquipID(),
                        bigDataRealTimeResp -> bigDataRealTimeResp.getDataResp(),
                        (v1, v2) -> v1));
                deviceList.forEach(device -> {
                    DataResp dataResp = staIdMap.get(device.getStationId() + "_" + device.getDeviceId());
                    if (null != dataResp && StringUtils.isNotEmpty(dataResp.getTime())) {
                        AggregatorRealTimeDataResp aggregatorRealTimeDataResp = new AggregatorRealTimeDataResp();
                        aggregatorRealTimeDataResp.setDeviceBaseId(device.getDeviceBaseId());
                        aggregatorRealTimeDataResp.setTime(dataResp.getTime());
                        aggregatorRealTimeDataResp.setValue(null == dataResp.getValue() ? null : MathUtils.doublePoint(dataResp.getValue(), 8));
                        aggregatorRealTimeDataRespList.add(aggregatorRealTimeDataResp);
                    }
                });
            }
        }
        return aggregatorRealTimeDataRespList;
    }
}
