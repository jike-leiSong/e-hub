package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorEntDateAdjustMapper;
import cn.sl.ehub.console.service.IAggregatorEntDateAdjustService;
import cn.sl.ehub.service.vo.AggregatorDeviceDateProfit;
import cn.sl.ehub.service.vo.AggregatorEntDateAdjust;
import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
@RequiredArgsConstructor
@Service
public class AggregatorEntDateAdjustServiceImpl implements IAggregatorEntDateAdjustService {
    private final AggregatorEntDateAdjustMapper aggregatorEntDateAdjustMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(String entId, String date) {
        Weekend<AggregatorEntDateAdjust> weekend = Weekend.of(AggregatorEntDateAdjust.class);
        WeekendCriteria<AggregatorEntDateAdjust, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDateAdjust::getEntId, entId);
        criteria.andEqualTo(AggregatorEntDateAdjust::getDate, date);
        return aggregatorEntDateAdjustMapper.deleteByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(List<String> entIdList, String date) {
        Weekend<AggregatorEntDateAdjust> weekend = Weekend.of(AggregatorEntDateAdjust.class);
        WeekendCriteria<AggregatorEntDateAdjust, Object> criteria = weekend.weekendCriteria();
        criteria.andIn(AggregatorEntDateAdjust::getEntId, entIdList);
        criteria.andEqualTo(AggregatorEntDateAdjust::getDate, date);
        return aggregatorEntDateAdjustMapper.deleteByExample(weekend);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByType(String aggregatorId, String typeId,String date) {
        Weekend<AggregatorEntDateAdjust> weekend = Weekend.of(AggregatorEntDateAdjust.class);
        WeekendCriteria<AggregatorEntDateAdjust, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDateAdjust::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorEntDateAdjust::getResourceTypeId, typeId);
        criteria.andEqualTo(AggregatorEntDateAdjust::getDate, date);
        return aggregatorEntDateAdjustMapper.deleteByExample(weekend);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorEntDateAdjust> aggregatorEntDateAdjustList) {
        return aggregatorEntDateAdjustMapper.batchInsert(aggregatorEntDateAdjustList);
    }

    @Override
    public int save(String entId, String date, List<AggregatorEntDateAdjust> aggregatorEntDateAdjustList) {
        delete(entId, date);
        return batchInsert(aggregatorEntDateAdjustList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(String aggregatorId, String typeId,String date, List<AggregatorEntDateAdjust> aggregatorEntDateAdjustList) {
        deleteByType(aggregatorId, typeId,date);
        return batchInsert(aggregatorEntDateAdjustList);
    }

    @Override
    public List<AggregatorEntDateAdjust> getEntAdjust(String entId, String startDate, String endDate, String sourceTypeId) {
        Weekend<AggregatorEntDateAdjust> weekend = Weekend.of(AggregatorEntDateAdjust.class);
        WeekendCriteria<AggregatorEntDateAdjust, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDateAdjust::getEntId, entId)
                .andEqualTo(AggregatorEntDateAdjust::getResourceTypeId,sourceTypeId)
                .andGreaterThanOrEqualTo(AggregatorEntDateAdjust::getDate,startDate)
                .andLessThanOrEqualTo(AggregatorEntDateAdjust::getDate,endDate);
        weekend.orderBy("date");
        return aggregatorEntDateAdjustMapper.selectByExample(weekend);
    }

    @Override
    public Map<String, List<AggregatorEntDateAdjust>> getMoreEntAdjust(List<String> entIdList, String startDate, String endDate, String sourceTypeId) {
        Map<String, List<AggregatorEntDateAdjust>> map = new HashMap<>();
        Weekend<AggregatorEntDateAdjust> weekend = Weekend.of(AggregatorEntDateAdjust.class);
        WeekendCriteria<AggregatorEntDateAdjust, Object> criteria = weekend.weekendCriteria();
        criteria.andIn(AggregatorEntDateAdjust::getEntId, entIdList)
                .andEqualTo(AggregatorEntDateAdjust::getResourceTypeId,sourceTypeId)
                .andGreaterThanOrEqualTo(AggregatorEntDateAdjust::getDate,startDate)
                .andLessThanOrEqualTo(AggregatorEntDateAdjust::getDate,endDate);
        weekend.orderBy("date");
        List<AggregatorEntDateAdjust> list = aggregatorEntDateAdjustMapper.selectByExample(weekend);
        if(CollectionUtil.isNotEmpty(list)){
             map = list.stream().collect(Collectors.groupingBy(AggregatorEntDateAdjust::getEntId));
        }
        return map;
    }

    @Override
    public List<AggregatorEntDateAdjust> getAggregatorEntDateAdjustList(String aggregatorId, String date) {
        Weekend<AggregatorEntDateAdjust> weekend = Weekend.of(AggregatorEntDateAdjust.class);
        WeekendCriteria<AggregatorEntDateAdjust, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDateAdjust::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorEntDateAdjust::getDate, date);
        return aggregatorEntDateAdjustMapper.selectByExample(weekend);
    }
}
