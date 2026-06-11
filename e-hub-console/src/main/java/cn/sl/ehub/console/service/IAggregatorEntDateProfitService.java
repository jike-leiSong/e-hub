package cn.sl.ehub.console.service;

import cn.sl.ehub.service.resp.AggregatorEntApplyPlanStatusResp;
import cn.sl.ehub.service.resp.AggregatorEntDateProfitResp;
import cn.sl.ehub.service.vo.AggregatorEntDateProfit;

import java.util.List;

/**
 * 企业收益Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorEntDateProfitService {

    /**
     * 查询数据
     *
     * @return
     */
    List<AggregatorEntDateProfit> getAggregatorEntDateProfitList();

    /**
     * 查询数据
     *
     * @param entIdList
     * @param startDate
     * @param endDate
     * @return
     */
    List<AggregatorEntDateProfit> getAggregatorEntDateProfitList(List<String> entIdList, String startDate, String endDate);

    /**
     * 查询数据
     *
     * @param entIdList
     * @param date
     * @return
     */
    List<AggregatorEntDateProfit> getAggregatorEntDateProfitList(List<String> entIdList, String date);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param dateList
     * @return
     */
    List<AggregatorEntDateProfit> getAggregatorEntDateProfitList(String aggregatorId, List<String> dateList);

    /**
     * 添加数据
     *
     * @param aggregatorEntDateProfitList
     * @return
     */
    int batchInsert(List<AggregatorEntDateProfit> aggregatorEntDateProfitList);

    /**
     * 删除数据
     *
     * @param entId
     * @param date
     * @return
     */
    int delete(String entId, String date);

    /**
     * 删除数据
     *
     * @param aggregatorId
     * @param date
     * @return
     */
    int deleteByAggregatorId(String aggregatorId, String date);

    /**
     * 保存数据
     *
     * @param entId
     * @param date
     * @param aggregatorEntDateProfitList
     * @return
     */
    int save(String entId, String date, List<AggregatorEntDateProfit> aggregatorEntDateProfitList);

    /**
     * 保存数据
     *
     * @param aggregatorId
     * @param date
     * @param aggregatorEntDateProfitList
     * @return
     */
    int saveByAggregatorId(String aggregatorId, String date, List<AggregatorEntDateProfit> aggregatorEntDateProfitList);

    /**
     * 查询数据
     *
     * @param entId
     * @return
     */
    AggregatorEntDateProfitResp getProfit(String entId);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param startDate
     * @param endDate
     * @return
     */
    List<AggregatorEntDateProfit> getAggregatorEntDateProfitList(String aggregatorId, String startDate, String endDate);

    /**
     * 查询数据
     *
     * @param entId
     * @param dateList
     * @return
     */
    List<AggregatorEntDateProfit> getAggregatorEntDateProfitListByEntId(String entId, List<String> dateList);

    /**
     * 保存数据
     *
     * @param aggregatorEntDateProfitList
     * @return
     */
    int save(List<AggregatorEntDateProfit> aggregatorEntDateProfitList);

    /**
     * 更新数据
     *
     * @param update
     * @param entId
     * @param date
     * @return
     */
    int update(AggregatorEntDateProfit update, String entId, String date);
}
