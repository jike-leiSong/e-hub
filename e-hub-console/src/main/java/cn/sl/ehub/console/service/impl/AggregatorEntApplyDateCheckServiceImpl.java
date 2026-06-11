package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorEntApplyDateCheckMapper;
import cn.sl.ehub.console.service.IAggregatorEntApplyDateCheckService;
import cn.sl.ehub.service.vo.AggregatorDateApplyDetail;
import cn.sl.ehub.service.vo.AggregatorEntApplyDateCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * 企业申报计划日期校验ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorEntApplyDateCheckServiceImpl implements IAggregatorEntApplyDateCheckService {

    private final AggregatorEntApplyDateCheckMapper aggregatorEntApplyDateCheckMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorEntApplyDateCheck> aggregatorEntApplyDateCheckList) {
        return aggregatorEntApplyDateCheckMapper.batchInsert(aggregatorEntApplyDateCheckList);
    }

    @Override
    public Boolean checkDate(String entId, List<String> dateList) {
        Weekend<AggregatorEntApplyDateCheck> weekend = Weekend.of(AggregatorEntApplyDateCheck.class);
        WeekendCriteria<AggregatorEntApplyDateCheck, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntApplyDateCheck::getEntId, entId);
        criteria.andIn(AggregatorEntApplyDateCheck::getDate, dateList);
        return aggregatorEntApplyDateCheckMapper.selectCountByExample(weekend) > 0;
    }
}
