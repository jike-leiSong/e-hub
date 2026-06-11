package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorEntDateApplyDetailMapper;
import cn.sl.ehub.console.service.IAggregatorEntDateApplyDetailService;
import cn.sl.ehub.service.vo.AggregatorEntApplyDateCheck;
import cn.sl.ehub.service.vo.AggregatorEntDateApplyDetail;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * 企业申报情况ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorEntDateApplyDetailServiceImpl implements IAggregatorEntDateApplyDetailService {

    private final AggregatorEntDateApplyDetailMapper aggregatorEntDateApplyDetailMapper;

    @Override
    public AggregatorEntDateApplyDetail getAggregatorEntDateApplyDetail(String entId, String date) {
        Weekend<AggregatorEntDateApplyDetail> weekend = Weekend.of(AggregatorEntDateApplyDetail.class);
        WeekendCriteria<AggregatorEntDateApplyDetail, Object> criteriaEnt = weekend.weekendCriteria();
        criteriaEnt.andEqualTo(AggregatorEntDateApplyDetail::getEntId, entId);
        criteriaEnt.andEqualTo(AggregatorEntDateApplyDetail::getDate, date);
        List<AggregatorEntDateApplyDetail> aggregatorEntDateApplyDetailList = aggregatorEntDateApplyDetailMapper.selectByExample(weekend);
        if (CollectionUtils.isNotEmpty(aggregatorEntDateApplyDetailList)) {
            return aggregatorEntDateApplyDetailList.get(0);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(String entId, List<String> dateList) {
        Weekend<AggregatorEntDateApplyDetail> weekendDelete = Weekend.of(AggregatorEntDateApplyDetail.class);
        WeekendCriteria<AggregatorEntDateApplyDetail, Object> criteriaDelete = weekendDelete.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorEntDateApplyDetail::getEntId, entId);
        criteriaDelete.andIn(AggregatorEntDateApplyDetail::getDate, dateList);
        return aggregatorEntDateApplyDetailMapper.deleteByExample(weekendDelete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorEntDateApplyDetail> aggregatorEntDateApplyDetailList) {
        return aggregatorEntDateApplyDetailMapper.batchInsert(aggregatorEntDateApplyDetailList);
    }

    @Override
    public List<AggregatorEntDateApplyDetail> getAggregatorEntDateApplyDetailList(String entId, String date) {
        Weekend<AggregatorEntDateApplyDetail> weekend = Weekend.of(AggregatorEntDateApplyDetail.class);
        WeekendCriteria<AggregatorEntDateApplyDetail, Object> criteriaEnt = weekend.weekendCriteria();
        criteriaEnt.andEqualTo(AggregatorEntDateApplyDetail::getEntId, entId);
        criteriaEnt.andEqualTo(AggregatorEntDateApplyDetail::getDate, date);
        return aggregatorEntDateApplyDetailMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorEntDateApplyDetail> getAggregatorEntDateApplyDetailList(List<String> entIdList, String date) {
        Weekend<AggregatorEntDateApplyDetail> weekendApply = Weekend.of(AggregatorEntDateApplyDetail.class);
        WeekendCriteria<AggregatorEntDateApplyDetail, Object> criteriaApply = weekendApply.weekendCriteria();
        criteriaApply.andEqualTo(AggregatorEntDateApplyDetail::getDate, date);
        criteriaApply.andIn(AggregatorEntDateApplyDetail::getEntId, entIdList);
        return aggregatorEntDateApplyDetailMapper.selectByExample(weekendApply);
    }

    @Override
    public List<AggregatorEntDateApplyDetail> getAggregatorEntDateApplyDetailList(List<String> entIdList, List<String> dateList) {
        Weekend<AggregatorEntDateApplyDetail> weekendApply = Weekend.of(AggregatorEntDateApplyDetail.class);
        WeekendCriteria<AggregatorEntDateApplyDetail, Object> criteriaApply = weekendApply.weekendCriteria();
        criteriaApply.andIn(AggregatorEntDateApplyDetail::getDate, dateList);
        criteriaApply.andIn(AggregatorEntDateApplyDetail::getEntId, entIdList);
        return aggregatorEntDateApplyDetailMapper.selectByExample(weekendApply);
    }

    @Override
    public List<AggregatorEntDateApplyDetail> getAggregatorEntDateApplyDetailList(String entId, List<String> dateList, Boolean planStatus) {
        Weekend<AggregatorEntDateApplyDetail> weekendApply = Weekend.of(AggregatorEntDateApplyDetail.class);
        WeekendCriteria<AggregatorEntDateApplyDetail, Object> criteriaApply = weekendApply.weekendCriteria();
        criteriaApply.andEqualTo(AggregatorEntDateApplyDetail::getEntId, entId);
        criteriaApply.andEqualTo(AggregatorEntDateApplyDetail::getPlanStatus, planStatus);
        criteriaApply.andIn(AggregatorEntDateApplyDetail::getDate, dateList);
        return aggregatorEntDateApplyDetailMapper.selectByExample(weekendApply);
    }

    @Override
    public int getCount(String aggregatorId, String date) {
        Weekend<AggregatorEntDateApplyDetail> weekendEntApply = Weekend.of(AggregatorEntDateApplyDetail.class);
        WeekendCriteria<AggregatorEntDateApplyDetail, Object> criteriaEntApply = weekendEntApply.weekendCriteria();
        criteriaEntApply.andEqualTo(AggregatorEntDateApplyDetail::getAggregatorId, aggregatorId);
        criteriaEntApply.andEqualTo(AggregatorEntDateApplyDetail::getDate, date);
        return aggregatorEntDateApplyDetailMapper.selectCountByExample(weekendEntApply);
    }

    @Override
    public Boolean checkDate(String entId, List<String> dateList) {
        Weekend<AggregatorEntDateApplyDetail> weekend = Weekend.of(AggregatorEntDateApplyDetail.class);
        WeekendCriteria<AggregatorEntDateApplyDetail, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDateApplyDetail::getEntId, entId);
        criteria.andEqualTo(AggregatorEntDateApplyDetail::getPlanStatus, true);
        criteria.andIn(AggregatorEntDateApplyDetail::getDate, dateList);
        return aggregatorEntDateApplyDetailMapper.selectCountByExample(weekend) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(AggregatorEntDateApplyDetail update, String aggregatorId, List<String> dateList) {
        Weekend<AggregatorEntDateApplyDetail> weekend = Weekend.of(AggregatorEntDateApplyDetail.class);
        WeekendCriteria<AggregatorEntDateApplyDetail, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDateApplyDetail::getAggregatorId, aggregatorId);
        criteria.andIn(AggregatorEntDateApplyDetail::getDate, dateList);
        return aggregatorEntDateApplyDetailMapper.updateByExampleSelective(update, weekend);
    }

    @Override
    public int update(AggregatorEntDateApplyDetail update, String entId, String date) {
        Weekend<AggregatorEntDateApplyDetail> weekend = Weekend.of(AggregatorEntDateApplyDetail.class);
        WeekendCriteria<AggregatorEntDateApplyDetail, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDateApplyDetail::getEntId, entId);
        criteria.andEqualTo(AggregatorEntDateApplyDetail::getDate, date);
        return aggregatorEntDateApplyDetailMapper.updateByExampleSelective(update, weekend);
    }
}
