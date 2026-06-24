package cn.sl.ehub.console.service;

import cn.sl.ehub.service.resp.AggregatorRealTimeDataResp;
import cn.sl.ehub.service.vo.AggregatorEntDevice;

import java.util.List;

/**
 * 实时数据查询Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorRealTimeDateService {

    /**
     * 查询数据
     *
     * @param deviceList
     * @return
     */
    List<AggregatorRealTimeDataResp> getDataByDeviceList(List<AggregatorEntDevice> deviceList);
}
