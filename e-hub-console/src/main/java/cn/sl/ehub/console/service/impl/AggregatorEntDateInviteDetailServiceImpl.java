package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorEntDateInviteDetailMapper;
import cn.sl.ehub.console.service.IAggregatorEntDateInviteDetailService;
import cn.sl.ehub.service.vo.AggregatorEntDateInviteDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * 企业邀约ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorEntDateInviteDetailServiceImpl implements IAggregatorEntDateInviteDetailService {

    private final AggregatorEntDateInviteDetailMapper aggregatorEntDateInviteDetailMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(AggregatorEntDateInviteDetail aggregatorEntDateInviteDetail) {
        return aggregatorEntDateInviteDetailMapper.insertSelective(aggregatorEntDateInviteDetail);
    }

    @Override
    public List<AggregatorEntDateInviteDetail> getAggregatorEntDateInviteDetailList(List<String> entIdList, String date) {
        Weekend<AggregatorEntDateInviteDetail> weekend = Weekend.of(AggregatorEntDateInviteDetail.class);
        WeekendCriteria<AggregatorEntDateInviteDetail, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDateInviteDetail::getDate, date);
        criteria.andIn(AggregatorEntDateInviteDetail::getEntId, entIdList);
        return aggregatorEntDateInviteDetailMapper.selectByExample(weekend);
    }
}
