package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorEntDateInviteDetail;

import java.util.List;

/**
 * 企业邀约Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorEntDateInviteDetailService {

    /**
     * 添加数据
     *
     * @param aggregatorEntDateInviteDetail
     * @return
     */
    int insert(AggregatorEntDateInviteDetail aggregatorEntDateInviteDetail);

    /**
     * 查询数据
     *
     * @param entIdList
     * @param date
     * @return
     */
    List<AggregatorEntDateInviteDetail> getAggregatorEntDateInviteDetailList(List<String> entIdList, String date);
}
