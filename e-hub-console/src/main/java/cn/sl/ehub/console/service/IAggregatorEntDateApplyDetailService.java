package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorEntDateApplyDetail;

import java.util.List;

/**
 * 企业申报情况Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorEntDateApplyDetailService {

    /**
     * 查询申报状态
     *
     * @param entId
     * @param date
     * @return
     */
    AggregatorEntDateApplyDetail getAggregatorEntDateApplyDetail(String entId, String date);

    /**
     * 删除数据
     *
     * @param entId
     * @param dateList
     * @return
     */
    int delete(String entId, List<String> dateList);

    /**
     * 添加数据
     *
     * @param aggregatorEntDateApplyDetailList
     * @return
     */
    int batchInsert(List<AggregatorEntDateApplyDetail> aggregatorEntDateApplyDetailList);

    /**
     * 查询数据
     *
     * @param entId
     * @param date
     * @return
     */
    List<AggregatorEntDateApplyDetail> getAggregatorEntDateApplyDetailList(String entId, String date);

    /**
     * 查询数据
     *
     * @param entIdList
     * @param date
     * @return
     */
    List<AggregatorEntDateApplyDetail> getAggregatorEntDateApplyDetailList(List<String> entIdList, String date);

    /**
     * 查询数据
     *
     * @param entIdList
     * @param dateList
     * @return
     */
    List<AggregatorEntDateApplyDetail> getAggregatorEntDateApplyDetailList(List<String> entIdList, List<String> dateList);

    /**
     * 查询数据
     *
     * @param entId
     * @param dateList
     * @param planStatus
     * @return
     */
    List<AggregatorEntDateApplyDetail> getAggregatorEntDateApplyDetailList(String entId, List<String> dateList, Boolean planStatus);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param date
     * @return
     */
    int getCount(String aggregatorId, String date);

    /**
     * 查询数据
     *
     * @param entId
     * @param dateList
     * @return
     */
    Boolean checkDate(String entId, List<String> dateList);

    /**
     * 更新数据
     *
     * @param update
     * @param aggregatorId
     * @param dateList
     * @return
     */
    int update(AggregatorEntDateApplyDetail update, String aggregatorId, List<String> dateList);

    /**
     * 更新数据
     *
     * @param update
     * @param entId
     * @param date
     * @return
     */
    int update(AggregatorEntDateApplyDetail update, String entId, String date);
}
