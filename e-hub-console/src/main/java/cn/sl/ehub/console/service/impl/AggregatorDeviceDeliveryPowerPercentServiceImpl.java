package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorDeviceDeliveryPowerPercentMapper;
import cn.sl.ehub.service.resp.AggregatorDeviceDeliveryPowerPercentDetail;
import cn.sl.ehub.console.service.IAggregatorDeviceDeliveryPowerPercentService;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.common.utils.GZIPUtil;
import cn.sl.ehub.service.vo.AggregatorDeviceDeliveryPowerPercent;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * 设备申报功率比例ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AggregatorDeviceDeliveryPowerPercentServiceImpl implements IAggregatorDeviceDeliveryPowerPercentService {

    private final AggregatorDeviceDeliveryPowerPercentMapper aggregatorDeviceDeliveryPowerPercentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchInsert(List<AggregatorDeviceDeliveryPowerPercent> aggregatorDeviceDeliveryPowerPercentList) {
        return aggregatorDeviceDeliveryPowerPercentMapper.batchInsert(aggregatorDeviceDeliveryPowerPercentList);
    }

    @Override
    public List<AggregatorDeviceDeliveryPowerPercentDetail> getAggregatorDeviceDeliveryPowerPercentDetailList(String resourceTypeId, String time) {
        Weekend<AggregatorDeviceDeliveryPowerPercent> weekend = Weekend.of(AggregatorDeviceDeliveryPowerPercent.class);
        WeekendCriteria<AggregatorDeviceDeliveryPowerPercent, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDeliveryPowerPercent::getResourceTypeId, resourceTypeId);
        criteria.andEqualTo(AggregatorDeviceDeliveryPowerPercent::getDate, DateUtils.format(time, "yyyy-MM-dd"));
        criteria.andGreaterThanOrEqualTo(AggregatorDeviceDeliveryPowerPercent::getTime, time);
        weekend.orderBy("time");
        List<AggregatorDeviceDeliveryPowerPercent> aggregatorDeviceDeliveryPowerPercentList = aggregatorDeviceDeliveryPowerPercentMapper.selectByExampleAndRowBounds(weekend, new RowBounds(0, 1));
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDeliveryPowerPercentList)) {
            AggregatorDeviceDeliveryPowerPercent aggregatorDeviceDeliveryPowerPercent = aggregatorDeviceDeliveryPowerPercentList.get(0);
            if (null != aggregatorDeviceDeliveryPowerPercent && StringUtils.isNotEmpty(aggregatorDeviceDeliveryPowerPercent.getDetail())) {
                return JSONArray.parseArray(aggregatorDeviceDeliveryPowerPercent.getDetail(), AggregatorDeviceDeliveryPowerPercentDetail.class);
            }
        }
        return Lists.newArrayList();
    }

    @Override
    public List<AggregatorDeviceDeliveryPowerPercent> getAggregatorDeviceDeliveryPowerPercentList(String resourceTypeId, String date) {
        Weekend<AggregatorDeviceDeliveryPowerPercent> weekend = Weekend.of(AggregatorDeviceDeliveryPowerPercent.class);
        WeekendCriteria<AggregatorDeviceDeliveryPowerPercent, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDeviceDeliveryPowerPercent::getResourceTypeId, resourceTypeId);
        criteria.andEqualTo(AggregatorDeviceDeliveryPowerPercent::getDate, date);
        weekend.orderBy("time");
        return aggregatorDeviceDeliveryPowerPercentMapper.selectByExample(weekend);
    }

    @Override
    public Integer delete(List<String> dateList) {
        Weekend<AggregatorDeviceDeliveryPowerPercent> weekend = Weekend.of(AggregatorDeviceDeliveryPowerPercent.class);
        WeekendCriteria<AggregatorDeviceDeliveryPowerPercent, Object> criteria = weekend.weekendCriteria();
        criteria.andIn(AggregatorDeviceDeliveryPowerPercent::getDate, dateList);
        return aggregatorDeviceDeliveryPowerPercentMapper.deleteByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetailAggregatorId(String oldAggregatorId, String newAggregatorId, Long startId, Long endId) {
        Weekend<AggregatorDeviceDeliveryPowerPercent> weekend = Weekend.of(AggregatorDeviceDeliveryPowerPercent.class);
        WeekendCriteria<AggregatorDeviceDeliveryPowerPercent, Object> criteria = weekend.weekendCriteria();
        if (null != startId) {
            criteria.andGreaterThanOrEqualTo(AggregatorDeviceDeliveryPowerPercent::getId, startId);
        }
        if (null != endId) {
            criteria.andLessThanOrEqualTo(AggregatorDeviceDeliveryPowerPercent::getId, endId);
        }
        List<AggregatorDeviceDeliveryPowerPercent> aggregatorDeviceDeliveryPowerPercentList = aggregatorDeviceDeliveryPowerPercentMapper.selectByExample(weekend);
        aggregatorDeviceDeliveryPowerPercentList.forEach(record -> {
            record.setDetail(record.getDetail().replaceAll(oldAggregatorId, newAggregatorId));
            record.setDetailByte(GZIPUtil.compressString(record.getDetail()));
            aggregatorDeviceDeliveryPowerPercentMapper.updateByPrimaryKeySelective(record);
        });
    }
}
