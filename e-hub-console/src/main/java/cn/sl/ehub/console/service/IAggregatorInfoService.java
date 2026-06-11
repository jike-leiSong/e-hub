package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorInfo;

import java.util.List;

/**
 * 聚合商Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorInfoService {

    /**
     * 查询数据
     *
     * @return
     */
    List<AggregatorInfo> getAggregatorInfoList();

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @return
     */
    AggregatorInfo getAggregatorInfo(String aggregatorId);

    /**
     * 查询数据
     *
     * @return
     */
    AggregatorInfo getFirst();

    /**
     * @description 根据聚合商电网标识查询数据
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    //AggregatorInfo getAggregatorInfoByRemoteId(String remoteId);
}
