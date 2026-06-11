package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorEntMapper;
import cn.sl.ehub.service.req.UpdateEntReq;
import cn.sl.ehub.service.resp.EntUserDetailResp;
import cn.sl.ehub.console.service.IAggregatorEntService;
import cn.sl.ehub.service.vo.AggregatorEnt;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 企业信息ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorEntServiceImpl implements IAggregatorEntService {

    private final AggregatorEntMapper aggregatorEntMapper;

    @Override
    public AggregatorEnt getAggregatorEnt(String entId) {
        Weekend<AggregatorEnt> weekend = Weekend.of(AggregatorEnt.class);
        WeekendCriteria<AggregatorEnt, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEnt::getEntId, entId);
        List<AggregatorEnt> aggregatorEntList = aggregatorEntMapper.selectByExample(weekend);
        if (null != aggregatorEntList && aggregatorEntList.size() > 0) {
            return aggregatorEntList.get(0);
        }
        return null;
    }

    @Override
    public List<AggregatorEnt> getAggregatorEntList(String aggregatorId) {
        Weekend<AggregatorEnt> weekendEnt = Weekend.of(AggregatorEnt.class);
        WeekendCriteria<AggregatorEnt, Object> criteriaEnt = weekendEnt.weekendCriteria();
        criteriaEnt.andEqualTo(AggregatorEnt::getAggregatorId, aggregatorId);
        // modify by 2021-10-23 删除未上报企业
        criteriaEnt.andEqualTo(AggregatorEnt::getStatus, 1);
        return aggregatorEntMapper.selectByExample(weekendEnt);
    }

    @Override
    public List<AggregatorEnt> getAggregatorPlanRunEntList(String aggregatorId) {
        Weekend<AggregatorEnt> weekendEnt = Weekend.of(AggregatorEnt.class);
        WeekendCriteria<AggregatorEnt, Object> criteriaEnt = weekendEnt.weekendCriteria();
        criteriaEnt.andEqualTo(AggregatorEnt::getAggregatorId, aggregatorId);
        criteriaEnt.andEqualTo(AggregatorEnt::getPlanRunStatus, 1);
        return aggregatorEntMapper.selectByExample(weekendEnt);
    }

    @Override
    public String getAggregatorIdByEntId(String entId) {
        Weekend<AggregatorEnt> weekend = Weekend.of(AggregatorEnt.class);
        WeekendCriteria<AggregatorEnt, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEnt::getEntId, entId);
        List<AggregatorEnt> aggregatorEntList = aggregatorEntMapper.selectByExample(weekend);
        if (null != aggregatorEntList && aggregatorEntList.size() > 0) {
            AggregatorEnt aggregatorEnt = aggregatorEntList.get(0);
            if (null != aggregatorEnt) {
                return aggregatorEnt.getAggregatorId();
            }
        }
        return null;
    }

    @Override
    public int getCount(String aggregatorId) {
        Weekend<AggregatorEnt> weekendEnt = Weekend.of(AggregatorEnt.class);
        WeekendCriteria<AggregatorEnt, Object> criteriaEnt = weekendEnt.weekendCriteria();
        criteriaEnt.andEqualTo(AggregatorEnt::getAggregatorId, aggregatorId);
        return aggregatorEntMapper.selectCountByExample(weekendEnt);
    }

    @Override
    public List<AggregatorEnt> getAggregatorEntList() {
        return aggregatorEntMapper.selectAll();
    }

    @Override
    public List<AggregatorEnt> getAggregatorEntList(List<String> entIdList) {
        Weekend<AggregatorEnt> weekendEnt = Weekend.of(AggregatorEnt.class);
        WeekendCriteria<AggregatorEnt, Object> criteriaEnt = weekendEnt.weekendCriteria();
        criteriaEnt.andIn(AggregatorEnt::getEntId, entIdList);
        return aggregatorEntMapper.selectByExample(weekendEnt);
    }

    @Override
    public List<EntUserDetailResp> getEntUserDetailRespList(String aggregatorId) {
        return aggregatorEntMapper.getEntUserDetailRespList(aggregatorId);
    }

    @Override
    public List<Double> selectPercentDistinct(String aggregatorId) {
        return aggregatorEntMapper.selectPercentDistinct(aggregatorId);
    }

    @Override
    public List<EntUserDetailResp> selectEntAndDeviceList(String entId, List<String> yearList) {
        return aggregatorEntMapper.selectEntAndDeviceList(entId, yearList);
    }
    @Override
    public List<EntUserDetailResp> selectEntAndDeviceListByAggregatorId(String aggregatorId,String entId, List<String> yearList) {
        return aggregatorEntMapper.selectEntAndDeviceListByAggregatorId(aggregatorId,entId, yearList);
    }

    @Override
    public List<EntUserDetailResp> selectEntAndDeviceListByStartYear(String entId, String startYear) {
        return aggregatorEntMapper.selectEntAndDeviceListByStartYear(entId, startYear);
    }

    @Override
    public List<EntUserDetailResp> selectEntAndDeviceListByStartYearAggregatorId(String aggregatorId,String entId, String startYear) {
        return aggregatorEntMapper.selectEntAndDeviceListByStartYearAggregatorId(aggregatorId,entId, startYear);
    }

    @Override
    public List<EntUserDetailResp> selectEntAndDeviceListByEndYear(String entId, String endYear) {
        return aggregatorEntMapper.selectEntAndDeviceListByEndYear(null, entId, endYear);
    }

    @Override
    public List<EntUserDetailResp> selectEntAndDeviceListByEndYearAggregatorId(String aggregatorId, String entId, String endYear) {
        return aggregatorEntMapper.selectEntAndDeviceListByEndYear(aggregatorId, entId, endYear);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAgreement(String entId) {
        return aggregatorEntMapper.deleteAgreement(entId);
    }

    @Override
    public List<EntUserDetailResp> selectEntUserDetailWithDevice(String aggregatorId, String entId, Double powerGetterThan, Double powerLessThan, Double percent) {
        return aggregatorEntMapper.selectEntUserDetailWithDevice(aggregatorId, entId, powerGetterThan, powerLessThan, percent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAggregatorEntAgreementInfo(UpdateEntReq req) {
        return aggregatorEntMapper.updateAggregatorEntAgreementInfo(req);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addAggregatorEntList(List<AggregatorEnt> aggregatorEntList) {
        List<AggregatorEnt> entList = getAggregatorEntList(aggregatorEntList.stream().map(AggregatorEnt::getEntId).collect(Collectors.toList()));
        if (CollectionUtils.isEmpty(entList)) {
            aggregatorEntList.forEach(ent -> {
                aggregatorEntMapper.insertSelective(ent);
            });
        } else {
            Map<String, AggregatorEnt> entIdMap = entList.stream().collect(Collectors.toMap(AggregatorEnt::getEntId, Function.identity(), (v1, v2) -> v1));
            aggregatorEntList.forEach(aggregatorEnt -> {
                AggregatorEnt ent = entIdMap.get(aggregatorEnt.getEntId());
                if (null == ent) {
                    aggregatorEntMapper.insertSelective(aggregatorEnt);
                }
            });
        }
        return aggregatorEntList.size();
    }
}
