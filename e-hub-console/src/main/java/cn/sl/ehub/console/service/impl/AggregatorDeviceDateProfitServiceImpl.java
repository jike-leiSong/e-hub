package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorDeviceDateProfitMapper;
import cn.sl.ehub.console.service.IAggregatorDeviceDateProfitService;
import cn.sl.ehub.common.utils.GZIPUtil;
import cn.sl.ehub.service.vo.AggregatorDeviceDateProfit;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * 设备收益ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorDeviceDateProfitServiceImpl implements IAggregatorDeviceDateProfitService {

    private final AggregatorDeviceDateProfitMapper aggregatorDeviceDateProfitMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList) {
        return aggregatorDeviceDateProfitMapper.batchInsert(aggregatorDeviceDateProfitList);
    }

    @Override
    public List<AggregatorDeviceDateProfit> getAggregatorDeviceDateProfitList(String aggregatorId, String date) {
        Weekend<AggregatorDeviceDateProfit> weekend = Weekend.of(AggregatorDeviceDateProfit.class);
        WeekendCriteria<AggregatorDeviceDateProfit, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDateProfit::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorDeviceDateProfit::getDate, date);
        return aggregatorDeviceDateProfitMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorDeviceDateProfit> getAggregatorDeviceDateProfitList(String aggregatorId, String resourceTypeId, String date) {
        Weekend<AggregatorDeviceDateProfit> weekend = Weekend.of(AggregatorDeviceDateProfit.class);
        WeekendCriteria<AggregatorDeviceDateProfit, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDateProfit::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorDeviceDateProfit::getResourceTypeId, resourceTypeId);
        criteria.andEqualTo(AggregatorDeviceDateProfit::getDate, date);
        return aggregatorDeviceDateProfitMapper.selectByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateListById(List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList) {
        aggregatorDeviceDateProfitList.forEach(profit -> aggregatorDeviceDateProfitMapper.updateByPrimaryKeySelective(profit));
        return aggregatorDeviceDateProfitList.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(String deviceBaseId, String date) {
        Weekend<AggregatorDeviceDateProfit> weekend = Weekend.of(AggregatorDeviceDateProfit.class);
        WeekendCriteria<AggregatorDeviceDateProfit, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDateProfit::getDeviceBaseId, deviceBaseId);
        criteria.andEqualTo(AggregatorDeviceDateProfit::getDate, date);
        return aggregatorDeviceDateProfitMapper.deleteByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByAggregatorId(String aggregatorId, String date) {
        Weekend<AggregatorDeviceDateProfit> weekend = Weekend.of(AggregatorDeviceDateProfit.class);
        WeekendCriteria<AggregatorDeviceDateProfit, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDateProfit::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorDeviceDateProfit::getDate, date);
        return aggregatorDeviceDateProfitMapper.deleteByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByAggregatorId(String aggregatorId, String date, String resourceTypeId) {
        Weekend<AggregatorDeviceDateProfit> weekend = Weekend.of(AggregatorDeviceDateProfit.class);
        WeekendCriteria<AggregatorDeviceDateProfit, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDateProfit::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorDeviceDateProfit::getDate, date);
        criteria.andEqualTo(AggregatorDeviceDateProfit::getResourceTypeId, resourceTypeId);
        return aggregatorDeviceDateProfitMapper.deleteByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(List<String> deviceBaseIdList, String date) {
        Weekend<AggregatorDeviceDateProfit> weekend = Weekend.of(AggregatorDeviceDateProfit.class);
        WeekendCriteria<AggregatorDeviceDateProfit, Object> criteria = weekend.weekendCriteria();
        criteria.andIn(AggregatorDeviceDateProfit::getDeviceBaseId, deviceBaseIdList);
        criteria.andEqualTo(AggregatorDeviceDateProfit::getDate, date);
        return aggregatorDeviceDateProfitMapper.deleteByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(String deviceBaseId, String date, List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList) {
        delete(deviceBaseId, date);
        return batchInsert(aggregatorDeviceDateProfitList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveByAggregatorId(String aggregatorId, String date, List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList) {
        deleteByAggregatorId(aggregatorId, date);
        return batchInsert(aggregatorDeviceDateProfitList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveByAggregatorId(String aggregatorId, String date, String resourceTypeId, List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList) {
        deleteByAggregatorId(aggregatorId, date, resourceTypeId);
        return batchInsert(aggregatorDeviceDateProfitList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(List<String> deviceBaseIdList, String date, List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList) {
        delete(deviceBaseIdList, date);
        return batchInsert(aggregatorDeviceDateProfitList);
    }

    @Override
    public List<AggregatorDeviceDateProfit> getAggregatorDeviceDateProfitList(String deviceBaseId, List<String> dateList) {
        Weekend<AggregatorDeviceDateProfit> weekend = Weekend.of(AggregatorDeviceDateProfit.class);
        WeekendCriteria<AggregatorDeviceDateProfit, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDateProfit::getDeviceBaseId, deviceBaseId);
        criteria.andIn(AggregatorDeviceDateProfit::getDate, dateList);
        return aggregatorDeviceDateProfitMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorDeviceDateProfit> getAggregatorDeviceDateProfitList(String date) {
        Weekend<AggregatorDeviceDateProfit> weekend = Weekend.of(AggregatorDeviceDateProfit.class);
        WeekendCriteria<AggregatorDeviceDateProfit, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDateProfit::getDate, date);
        return aggregatorDeviceDateProfitMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorDeviceDateProfit> getAggregatorDeviceDateProfitList(List<String> dateList) {
        Weekend<AggregatorDeviceDateProfit> weekend = Weekend.of(AggregatorDeviceDateProfit.class);
        WeekendCriteria<AggregatorDeviceDateProfit, Object> criteria = weekend.weekendCriteria();
        criteria.andIn(AggregatorDeviceDateProfit::getDate, dateList);
        return aggregatorDeviceDateProfitMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorDeviceDateProfit> getAggregatorDeviceDateProfitListByDeviceBaseIdAndDate(String deviceBaseId, String date) {
        Weekend<AggregatorDeviceDateProfit> weekend = Weekend.of(AggregatorDeviceDateProfit.class);
        WeekendCriteria<AggregatorDeviceDateProfit, Object> criteria = weekend.weekendCriteria();
        if (StringUtils.isNotEmpty(deviceBaseId)) {
            criteria.andEqualTo(AggregatorDeviceDateProfit::getDeviceBaseId, deviceBaseId);
        }
        if (StringUtils.isNotEmpty(date)) {
            criteria.andEqualTo(AggregatorDeviceDateProfit::getDate, date);
        }
        return aggregatorDeviceDateProfitMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorDeviceDateProfit> getAggregatorDeviceDateProfitListByEntId(String entId, List<String> dateList) {
        Weekend<AggregatorDeviceDateProfit> weekend = Weekend.of(AggregatorDeviceDateProfit.class);
        WeekendCriteria<AggregatorDeviceDateProfit, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDateProfit::getEntId, entId);
        criteria.andIn(AggregatorDeviceDateProfit::getDate, dateList);
        return aggregatorDeviceDateProfitMapper.selectByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAggregatorId(String oldAggregatorId, String newAggregatorId, Long startId, Long endId) {
        Weekend<AggregatorDeviceDateProfit> weekend = Weekend.of(AggregatorDeviceDateProfit.class);
        WeekendCriteria<AggregatorDeviceDateProfit, Object> criteria = weekend.weekendCriteria();
        if (null != startId) {
            criteria.andGreaterThanOrEqualTo(AggregatorDeviceDateProfit::getId, startId);
        }
        if (null != endId) {
            criteria.andLessThanOrEqualTo(AggregatorDeviceDateProfit::getId, endId);
        }
        List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList = aggregatorDeviceDateProfitMapper.selectByExample(weekend);
        aggregatorDeviceDateProfitList.forEach(record -> {
            record.setAggregatorId(newAggregatorId);
            record.setProfitDetail(record.getProfitDetail().replaceAll(oldAggregatorId, newAggregatorId));
            record.setProfitDetailByte(GZIPUtil.compressString(record.getProfitDetail()));
            aggregatorDeviceDateProfitMapper.updateByPrimaryKeySelective(record);
        });
    }
}
