package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorEntApplyDateCheck;

import java.util.List;

/**
 * 企业申报计划日期校验Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorEntApplyDateCheckService {

    /**
     * 添加数据
     *
     * @param aggregatorEntApplyDateCheckList
     * @return
     */
    int batchInsert(List<AggregatorEntApplyDateCheck> aggregatorEntApplyDateCheckList);

    /**
     * 查询数据
     *
     * @param entId
     * @param dateList
     * @return
     */
    Boolean checkDate(String entId, List<String> dateList);
}
