package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorInfoMapper;
import cn.sl.ehub.console.service.IAggregatorInfoService;
import cn.sl.ehub.service.vo.AggregatorInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * 聚合商ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorInfoServiceImpl implements IAggregatorInfoService {

    private final AggregatorInfoMapper aggregatorInfoMapper;

    @Override
    public List<AggregatorInfo> getAggregatorInfoList() {
        Weekend<AggregatorInfo> weekend = Weekend.of(AggregatorInfo.class);
        WeekendCriteria<AggregatorInfo, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorInfo::getDelFlag, 0);
        return aggregatorInfoMapper.selectByExample(weekend);
    }

    @Override
    public AggregatorInfo getAggregatorInfo(String aggregatorId) {
        Weekend<AggregatorInfo> weekend = Weekend.of(AggregatorInfo.class);
        WeekendCriteria<AggregatorInfo, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorInfo::getAggregatorId, aggregatorId);
        List<AggregatorInfo> aggregatorInfoList = aggregatorInfoMapper.selectByExample(weekend);
        if (null != aggregatorInfoList && aggregatorInfoList.size() > 0) {
            return aggregatorInfoList.get(0);
        }
        return null;
    }

    @Override
    public AggregatorInfo getFirst() {
        return aggregatorInfoMapper.getFirst();
    }

//    @Override
//    public AggregatorInfo getAggregatorInfoByRemoteId(String remoteId) {
//        Weekend<AggregatorInfo> weekend = Weekend.of(AggregatorInfo.class);
//        WeekendCriteria<AggregatorInfo, Object> criteria = weekend.weekendCriteria();
//        criteria.andEqualTo(AggregatorInfo::getDelFlag, 0)
//                .andEqualTo(AggregatorInfo::getRemoteId,remoteId);
//        List<AggregatorInfo> aggregatorInfoList = aggregatorInfoMapper.selectByExample(weekend);
//        if (null != aggregatorInfoList && aggregatorInfoList.size() > 0) {
//            return aggregatorInfoList.get(0);
//        }
//        return null;
//    }
}
