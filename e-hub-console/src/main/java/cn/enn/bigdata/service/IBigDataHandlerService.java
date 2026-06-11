package cn.enn.bigdata.service;

import cn.enn.bigdata.req.HistoryReq;
import cn.enn.bigdata.req.RealTimeReq;
import cn.enn.bigdata.resp.BigDataHistoryAndCalculationResp;
import cn.enn.bigdata.resp.BigDataHistoryResp;
import cn.enn.bigdata.resp.BigDataRealTimeResp;
import cn.sl.ehub.common.vo.DeviceGroupPointInfo;
import cn.sl.ehub.service.vo.AggregatorEntDevice;

import java.util.List;

public interface IBigDataHandlerService {

    List<BigDataRealTimeResp> getRealTime(RealTimeReq req);

    List<BigDataRealTimeResp> getRealTime(RealTimeReq req, String flag);

    List<BigDataHistoryAndCalculationResp> queryBigData(
        List<AggregatorEntDevice> deviceList,
        List<DeviceGroupPointInfo> pointInfoList,
        String interval,
        String startTime,
        String endTime,
        String simulate
    );

    List<BigDataHistoryResp> getBigData(
        List<AggregatorEntDevice> deviceList,
        List<String> metricCodes,
        String startTime,
        String endTime,
        String simulate
    );

    List<BigDataHistoryResp> getHistory(HistoryReq req, String simulate);
}
