package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.service.mapper.AggregatorResourceDateIssueOfferMapper;
import cn.sl.ehub.console.service.IAggregatorResourceDateIssueOfferService;
import cn.sl.ehub.service.vo.AggregatorResourceDateIssueOffer;
import cn.sl.ehub.service.vo.AggregatorResourceDateDeliveryOffer;
import cn.sl.ehub.service.vo.AggregatorResourceDateIssueOffer;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * 下发报价Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorResourceDateIssueOfferServiceImpl implements IAggregatorResourceDateIssueOfferService {

    private final AggregatorResourceDateIssueOfferMapper aggregatorResourceDateIssueOfferMapper;

    @Override
    public List<AggregatorResourceDateIssueOffer> getAggregatorResourceDateIssueOfferList(List<String> dateList) {
        Weekend<AggregatorResourceDateIssueOffer> weekend = Weekend.of(AggregatorResourceDateIssueOffer.class);
        WeekendCriteria<AggregatorResourceDateIssueOffer, Object> criteria = weekend.weekendCriteria();
        criteria.andIn(AggregatorResourceDateIssueOffer::getDate, dateList);
        return aggregatorResourceDateIssueOfferMapper.selectByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer saveAggregatorResourceDateDeliveryOffer(String aggregatorId, String date, List<AggregatorResourceDateIssueOffer> aggregatorResourceDateIssueOfferList) {
        deleteAggregatorResourceDateDeliveryOffer(aggregatorId, date);
        return batchInsertAggregatorResourceDateDeliveryOffer(aggregatorResourceDateIssueOfferList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteAggregatorResourceDateDeliveryOffer(String aggregatorId, String date) {
        Weekend<AggregatorResourceDateIssueOffer> weekendDelete = Weekend.of(AggregatorResourceDateIssueOffer.class);
        WeekendCriteria<AggregatorResourceDateIssueOffer, Object> criteriaDelete = weekendDelete.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorResourceDateIssueOffer::getAggregatorId, aggregatorId);
        criteriaDelete.andEqualTo(AggregatorResourceDateIssueOffer::getDate, date);
        return aggregatorResourceDateIssueOfferMapper.deleteByExample(weekendDelete);
    }

    @Override
    public Integer deleteAggregatorResourceDateDeliveryOffer(String aggregatorId, String date, String resourceTypeId) {
        Weekend<AggregatorResourceDateIssueOffer> weekendDelete = Weekend.of(AggregatorResourceDateIssueOffer.class);
        WeekendCriteria<AggregatorResourceDateIssueOffer, Object> criteriaDelete = weekendDelete.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorResourceDateIssueOffer::getAggregatorId, aggregatorId);
        criteriaDelete.andEqualTo(AggregatorResourceDateIssueOffer::getDate, date);
        criteriaDelete.andEqualTo(AggregatorResourceDateIssueOffer::getResourceTypeId, resourceTypeId);
        return aggregatorResourceDateIssueOfferMapper.deleteByExample(weekendDelete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchInsertAggregatorResourceDateDeliveryOffer(List<AggregatorResourceDateIssueOffer> aggregatorResourceDateIssueOfferList) {
        return aggregatorResourceDateIssueOfferMapper.batchInsert(aggregatorResourceDateIssueOfferList);
    }

    @Override
    public List<AggregatorResourceDateIssueOffer> getAggregatorResourceDateIssueOfferList(String date) {
        Weekend<AggregatorResourceDateIssueOffer> weekend = Weekend.of(AggregatorResourceDateIssueOffer.class);
        WeekendCriteria<AggregatorResourceDateIssueOffer, Object> criteriaDelete = weekend.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorResourceDateIssueOffer::getDate, date);
        return aggregatorResourceDateIssueOfferMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorResourceDateIssueOffer> getAggregatorResourceDateIssueOfferList(String aggregatorId, List<String> dateList) {
        Weekend<AggregatorResourceDateIssueOffer> weekend = Weekend.of(AggregatorResourceDateIssueOffer.class);
        WeekendCriteria<AggregatorResourceDateIssueOffer, Object> criteriaDelete = weekend.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorResourceDateIssueOffer::getAggregatorId, aggregatorId);
        criteriaDelete.andIn(AggregatorResourceDateIssueOffer::getDate, dateList);
        return aggregatorResourceDateIssueOfferMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorResourceDateIssueOffer> getAggregatorResourceDateIssueOfferList(String aggregatorId, String resourceTypeId, List<String> dateList) {
        Weekend<AggregatorResourceDateIssueOffer> weekend = Weekend.of(AggregatorResourceDateIssueOffer.class);
        WeekendCriteria<AggregatorResourceDateIssueOffer, Object> criteriaDelete = weekend.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorResourceDateIssueOffer::getAggregatorId, aggregatorId);
        criteriaDelete.andEqualTo(AggregatorResourceDateIssueOffer::getResourceTypeId, resourceTypeId);
        criteriaDelete.andIn(AggregatorResourceDateIssueOffer::getDate, dateList);
        return aggregatorResourceDateIssueOfferMapper.selectByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(AggregatorResourceDateIssueOffer aggregatorResourceDateIssueOffer) {
        return aggregatorResourceDateIssueOfferMapper.updateByPrimaryKeySelective(aggregatorResourceDateIssueOffer);
    }

    @Override
    public List<AggregatorResourceDateIssueOffer> getAggregatorIssuePriceChart(String aggregatorId, String resourceTypeId, String startDate, String endDate) {
        Weekend<AggregatorResourceDateIssueOffer> weekend = Weekend.of(AggregatorResourceDateIssueOffer.class);
        WeekendCriteria<AggregatorResourceDateIssueOffer, Object> criteriaDelete = weekend.weekendCriteria();
        criteriaDelete.andEqualTo(AggregatorResourceDateIssueOffer::getAggregatorId, aggregatorId)
                .andEqualTo(AggregatorResourceDateIssueOffer::getResourceTypeId, resourceTypeId)
                .andGreaterThanOrEqualTo(AggregatorResourceDateIssueOffer::getDate, startDate)
                .andLessThanOrEqualTo(AggregatorResourceDateIssueOffer::getDate, endDate);
        weekend.orderBy("date");

        return aggregatorResourceDateIssueOfferMapper.selectByExample(weekend);
    }
}
