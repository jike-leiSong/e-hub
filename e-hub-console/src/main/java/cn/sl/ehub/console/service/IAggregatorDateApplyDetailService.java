package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorDateApplyDetail;

import java.util.List;

/**
 * 聚合商申报Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorDateApplyDetailService {

    /**
     * 添加数据
     *
     * @param aggregatorDateApplyDetailList
     * @return
     */
    Integer batchInsert(List<AggregatorDateApplyDetail> aggregatorDateApplyDetailList);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param date
     * @param winStatus
     * @return
     */
    List<AggregatorDateApplyDetail> getAggregatorDateApplyDetailList(String aggregatorId, String date, String winStatus);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param date
     * @return
     */
    AggregatorDateApplyDetail getAggregatorDateApplyDetail(String aggregatorId, String date);

    /**
     * 修改数据
     *
     * @param update
     * @param aggregatorId
     * @param date
     * @return
     */
    Integer updateAggregatorDateApplyDetail(AggregatorDateApplyDetail update, String aggregatorId, String date);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param date
     * @return
     */
    int getCount(String aggregatorId, String date);

    /**
     * 添加数据
     *
     * @param aggregatorDateApplyDetail
     * @return
     */
    Integer insert(AggregatorDateApplyDetail aggregatorDateApplyDetail);

    /**
     * 删除数据
     *
     * @param aggregatorId
     * @param dateList
     * @return
     */
    int delete(String aggregatorId, List<String> dateList);

    /**
     * @description 判断某天是否已经自动申报过
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    boolean checkDateAutoApply(String aggregatorId,String date);
}
