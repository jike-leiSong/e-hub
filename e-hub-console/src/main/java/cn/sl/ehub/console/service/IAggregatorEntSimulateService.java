package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorEntSimulate;

import java.util.List;

/**
 * 企业仿真配置Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorEntSimulateService {

    /**
     * 查询数据
     *
     * @return
     */
    List<AggregatorEntSimulate> getAggregatorEntSimulateList();
}
