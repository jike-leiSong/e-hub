package cn.sl.ehub.console.service;

import cn.sl.ehub.service.req.AggregatorEntDeviceIotLogReq;
import cn.sl.ehub.service.vo.AggregatorEntDeviceIotLog;

import java.util.List;

/**
 * 设备下发指令日志Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorEntDeviceIotLogService {

    /**
     * 添加数据
     *
     * @param aggregatorEntDeviceIotLogList
     * @return
     */
    int batchInsert(List<AggregatorEntDeviceIotLog> aggregatorEntDeviceIotLogList);

    /**
     * 添加数据
     *
     * @param aggregatorEntDeviceIotLogReqList
     * @return
     */
    int batchInsertReq(List<AggregatorEntDeviceIotLogReq> aggregatorEntDeviceIotLogReqList);
}
