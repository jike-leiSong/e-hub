package cn.sl.ehub.upstream.service;

import cn.sl.ehub.upstream.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * BigData服务（临时替代类）
 * 原：cn.enn.bigdata.service.BigDataHandlerService
 *
 * 说明：返回空数据，后续需要实现真实的数据查询逻辑
 * TODO: 实现从device_point_data表查询数据
 */
@Service
@Slf4j
public class BigDataHandlerService {

    /**
     * 获取实时数据
     */
    public BigDataRealTimeResp getRealTimeData(RealTimeReq req) {
        log.warn("BigData服务已删除，返回空数据 - deviceId: {}", req.getDeviceId());
        return new BigDataRealTimeResp();
    }

    /**
     * 获取实时数据（带标志参数）
     */
    public List<BigDataRealTimeResp> getRealTime(RealTimeReq req, String flag) {
        log.warn("BigData服务已删除，返回空数据");
        return new ArrayList<>();
    }

    /**
     * 获取历史数据
     */
    public List<BigDataHistoryResp> getHistory(HistoryReq req, String flag) {
        log.warn("BigData服务已删除，返回空数据");
        return new ArrayList<>();
    }

    /**
     * 查询OpenTSDB数据
     */
    public BigDataRealTimeResp queryOpentsdb(OpentsdbReq req) {
        log.warn("BigData服务已删除，返回空数据 - metric: {}", req.getMetric());
        return new BigDataRealTimeResp();
    }
}
