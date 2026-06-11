package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorResourceDateDeliveryOfferMapper;
import cn.sl.ehub.console.service.IAggregatorResourceDateDeliveryOfferService;
import cn.sl.ehub.service.vo.AggregatorResourceDateDeliveryOffer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * 申报价格ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorResourceDateDeliveryOfferServiceImpl implements IAggregatorResourceDateDeliveryOfferService {

    private final AggregatorResourceDateDeliveryOfferMapper aggregatorResourceDateDeliveryOfferMapper;

    @Override
    public List<AggregatorResourceDateDeliveryOffer> getAggregatorResourceDateDeliveryOfferList(String aggregatorId, String date) {
        Weekend<AggregatorResourceDateDeliveryOffer> weekend = Weekend.of(AggregatorResourceDateDeliveryOffer.class);
        WeekendCriteria<AggregatorResourceDateDeliveryOffer, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorResourceDateDeliveryOffer::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorResourceDateDeliveryOffer::getDate, date);
        return aggregatorResourceDateDeliveryOfferMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorResourceDateDeliveryOffer> getAggregatorResourceDateDeliveryOfferList(String aggregatorId, List<String> dateList) {
        Weekend<AggregatorResourceDateDeliveryOffer> weekend = Weekend.of(AggregatorResourceDateDeliveryOffer.class);
        WeekendCriteria<AggregatorResourceDateDeliveryOffer, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorResourceDateDeliveryOffer::getAggregatorId, aggregatorId);
        criteria.andIn(AggregatorResourceDateDeliveryOffer::getDate, dateList);
        return aggregatorResourceDateDeliveryOfferMapper.selectByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorResourceDateDeliveryOffer> aggregatorResourceDateDeliveryOfferList) {
        return aggregatorResourceDateDeliveryOfferMapper.batchInsert(aggregatorResourceDateDeliveryOfferList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(String aggregatorId, List<String> dateList) {
        Weekend<AggregatorResourceDateDeliveryOffer> weekend = Weekend.of(AggregatorResourceDateDeliveryOffer.class);
        WeekendCriteria<AggregatorResourceDateDeliveryOffer, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorResourceDateDeliveryOffer::getAggregatorId, aggregatorId);
        criteria.andIn(AggregatorResourceDateDeliveryOffer::getDate, dateList);
        return aggregatorResourceDateDeliveryOfferMapper.deleteByExample(weekend);
    }

    @Override
    public int getCount(String aggregatorId, String date, String status) {
        Weekend<AggregatorResourceDateDeliveryOffer> weekend = Weekend.of(AggregatorResourceDateDeliveryOffer.class);
        WeekendCriteria<AggregatorResourceDateDeliveryOffer, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorResourceDateDeliveryOffer::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorResourceDateDeliveryOffer::getDate, date);
        criteria.andEqualTo(AggregatorResourceDateDeliveryOffer::getStatus, status);
        return aggregatorResourceDateDeliveryOfferMapper.selectCountByExample(weekend);
    }

    @Override
    public int getCount(String aggregatorId, List<String> dateList) {
        Weekend<AggregatorResourceDateDeliveryOffer> weekend = Weekend.of(AggregatorResourceDateDeliveryOffer.class);
        WeekendCriteria<AggregatorResourceDateDeliveryOffer, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorResourceDateDeliveryOffer::getAggregatorId, aggregatorId);
        criteria.andIn(AggregatorResourceDateDeliveryOffer::getDate, dateList);
        return aggregatorResourceDateDeliveryOfferMapper.selectCountByExample(weekend);
    }

    @Override
    public int getCount(String aggregatorId, List<String> dateList, String status) {
        Weekend<AggregatorResourceDateDeliveryOffer> weekend = Weekend.of(AggregatorResourceDateDeliveryOffer.class);
        WeekendCriteria<AggregatorResourceDateDeliveryOffer, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorResourceDateDeliveryOffer::getAggregatorId, aggregatorId);
        criteria.andIn(AggregatorResourceDateDeliveryOffer::getDate, dateList);
        criteria.andEqualTo(AggregatorResourceDateDeliveryOffer::getStatus, status);
        return aggregatorResourceDateDeliveryOfferMapper.selectCountByExample(weekend);
    }

    @Override
    public List<AggregatorResourceDateDeliveryOffer> getAggregatorResourceDateDeliveryOfferList(String aggregatorId, String resourceTypeId, List<String> dateList, String status) {
        Weekend<AggregatorResourceDateDeliveryOffer> weekend = Weekend.of(AggregatorResourceDateDeliveryOffer.class);
        WeekendCriteria<AggregatorResourceDateDeliveryOffer, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorResourceDateDeliveryOffer::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorResourceDateDeliveryOffer::getResourceTypeId, resourceTypeId);
        criteria.andIn(AggregatorResourceDateDeliveryOffer::getDate, dateList);
        criteria.andEqualTo(AggregatorResourceDateDeliveryOffer::getStatus, status);
        return aggregatorResourceDateDeliveryOfferMapper.selectByExample(weekend);
    }
}
