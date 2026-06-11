package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorEntProfitTime;

import java.util.List;
import java.util.Map;

/**
 * 企业有效用电和收益配置Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorEntProfitTimeService {

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @return
     */
    List<AggregatorEntProfitTime> getAggregatorEntProfitTimeList(String aggregatorId);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @return
     */
    Map<String, List<AggregatorEntProfitTime>> getEntMap(String aggregatorId);

    /**
     * 查询数据
     *
     * @return
     */
    Map<String, List<AggregatorEntProfitTime>> getEntMap();

    /**
     * 查询数据
     *
     * @return
     */
    Map<String, List<AggregatorEntProfitTime>> getEntMapByEntId(String entId);
}
