package cn.sl.ehub.console.service;

import cn.sl.ehub.service.resp.AggregatorProfitResp;
import cn.sl.ehub.service.vo.AggregatorDateProfit;

import java.util.List;

/**
 * 聚合商收益Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorDateProfitService {

    /**
     * 查询数据
     *
     * @return
     */
    List<AggregatorDateProfit> getAggregatorDateProfitList();

    /**
     * 查询按日期倒序最后一条
     *
     * @param aggregatorId
     * @param date
     * @return
     */
    AggregatorDateProfit getAggregatorDateProfit(String aggregatorId, String date);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param dateList
     * @return
     */
    List<AggregatorDateProfit> getAggregatorDateProfitList(String aggregatorId, List<String> dateList);

    /**
     * 添加数据
     *
     * @param aggregatorDateProfitList
     * @return
     */
    int batchInsert(List<AggregatorDateProfit> aggregatorDateProfitList);

    /**
     * 删除数据
     *
     * @param aggregatorId
     * @param date
     * @return
     */
    int delete(String aggregatorId, String date);

    /**
     * 保存数据
     *
     * @param aggregatorId
     * @param date
     * @param aggregatorDateProfitList
     * @return
     */
    int save(String aggregatorId, String date, List<AggregatorDateProfit> aggregatorDateProfitList);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param startDate
     * @param endDate
     * @return
     */
    AggregatorProfitResp getAggregatorProfitRespTotal(String aggregatorId, String startDate, String endDate);
}
