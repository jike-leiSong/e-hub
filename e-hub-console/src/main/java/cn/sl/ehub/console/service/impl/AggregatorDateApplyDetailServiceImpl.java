package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorDateApplyDetailMapper;
import cn.sl.ehub.console.service.IAggregatorDateApplyDetailService;
import cn.sl.ehub.service.vo.AggregatorDateApplyDetail;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * 聚合商申报ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorDateApplyDetailServiceImpl implements IAggregatorDateApplyDetailService {

    private final AggregatorDateApplyDetailMapper aggregatorDateApplyDetailMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchInsert(List<AggregatorDateApplyDetail> aggregatorDateApplyDetailList) {
        return aggregatorDateApplyDetailMapper.batchInsert(aggregatorDateApplyDetailList);
    }

    @Override
    public List<AggregatorDateApplyDetail> getAggregatorDateApplyDetailList(String aggregatorId, String date, String winStatus) {
        Weekend<AggregatorDateApplyDetail> weekend = Weekend.of(AggregatorDateApplyDetail.class);
        WeekendCriteria<AggregatorDateApplyDetail, Object> criteria = weekend.weekendCriteria();
        if (StringUtils.isNotEmpty(aggregatorId)) {
            criteria.andEqualTo(AggregatorDateApplyDetail::getAggregatorId, aggregatorId);
        }
        if (StringUtils.isNotEmpty(date)) {
            criteria.andEqualTo(AggregatorDateApplyDetail::getDate, date);
        }
        if (StringUtils.isNotEmpty(winStatus)) {
            criteria.andEqualTo(AggregatorDateApplyDetail::getWinStatus, winStatus);
        }
        return aggregatorDateApplyDetailMapper.selectByExample(weekend);
    }

    @Override
    public AggregatorDateApplyDetail getAggregatorDateApplyDetail(String aggregatorId, String date) {
        Weekend<AggregatorDateApplyDetail> weekend = Weekend.of(AggregatorDateApplyDetail.class);
        WeekendCriteria<AggregatorDateApplyDetail, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDateApplyDetail::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorDateApplyDetail::getDate, date);
        List<AggregatorDateApplyDetail> aggregatorDateApplyDetailList = aggregatorDateApplyDetailMapper.selectByExample(weekend);
        if (null != aggregatorDateApplyDetailList && aggregatorDateApplyDetailList.size() > 0) {
            return aggregatorDateApplyDetailList.get(0);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateAggregatorDateApplyDetail(AggregatorDateApplyDetail update, String aggregatorId, String date) {
        Weekend<AggregatorDateApplyDetail> weekend = Weekend.of(AggregatorDateApplyDetail.class);
        WeekendCriteria<AggregatorDateApplyDetail, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDateApplyDetail::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorDateApplyDetail::getDate, date);
        return aggregatorDateApplyDetailMapper.updateByExampleSelective(update, weekend);
    }

    @Override
    public int getCount(String aggregatorId, String date) {
        Weekend<AggregatorDateApplyDetail> weekend = Weekend.of(AggregatorDateApplyDetail.class);
        WeekendCriteria<AggregatorDateApplyDetail, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDateApplyDetail::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorDateApplyDetail::getDate, date);
        return aggregatorDateApplyDetailMapper.selectCountByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer insert(AggregatorDateApplyDetail aggregatorDateApplyDetail) {
        return aggregatorDateApplyDetailMapper.insertSelective(aggregatorDateApplyDetail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(String aggregatorId, List<String> dateList) {
        Weekend<AggregatorDateApplyDetail> weekend = Weekend.of(AggregatorDateApplyDetail.class);
        WeekendCriteria<AggregatorDateApplyDetail, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDateApplyDetail::getAggregatorId, aggregatorId);
        criteria.andIn(AggregatorDateApplyDetail::getDate, dateList);
        return aggregatorDateApplyDetailMapper.deleteByExample(weekend);
    }

    @Override
    public boolean checkDateAutoApply(String aggregatorId, String date) {
        Weekend<AggregatorDateApplyDetail> weekend = Weekend.of(AggregatorDateApplyDetail.class);
        WeekendCriteria<AggregatorDateApplyDetail, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDateApplyDetail::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorDateApplyDetail::getDate, date);
        List<AggregatorDateApplyDetail> list = aggregatorDateApplyDetailMapper.selectByExample(weekend);
        return list.size()>0;
    }
}
