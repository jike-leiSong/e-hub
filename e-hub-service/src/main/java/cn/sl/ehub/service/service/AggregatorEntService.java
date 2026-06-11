package cn.sl.ehub.service.service;

import cn.sl.ehub.service.mapper.AggregatorEntMapper;
import cn.sl.ehub.service.vo.AggregatorEnt;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 聚合商企业服务
 * @Author sl
 * @Date 2026-05-28
 */
@Service
public class AggregatorEntService {

    @Resource
    private AggregatorEntMapper aggregatorEntMapper;

    public List<AggregatorEnt> getEntSubListByAggEntId(String aggregatorId) {
        Example example = new Example(AggregatorEnt.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("aggregatorId", aggregatorId);
        return aggregatorEntMapper.selectByExample(example);
    }

    public List<String> getEntSubStationIdsByAggEntId(String aggregatorId) {
        List<AggregatorEnt> entSubList = getEntSubListByAggEntId(aggregatorId);
        return entSubList.stream().map(AggregatorEnt::getStationId).collect(Collectors.toList());
    }

    public List<AggregatorEnt> getAllAggregatorEnt() {
        return aggregatorEntMapper.selectAll();
    }

    public List<AggregatorEnt> getOnlineAggregatorEntList() {
        return aggregatorEntMapper.getOnlineAggregatorEntList();
    }

    public List<AggregatorEnt> getOnlineAggregatorEntListByResourTypeId(String resourTypeId) {
        return aggregatorEntMapper.getOnlineAggregatorEntListByResourTypeId(resourTypeId);
    }

    public List<AggregatorEntDevice> getOnlineEnergyStationListByResourTypeId(String resourTypeId) {
        return aggregatorEntMapper.getOnlineEnergyStationListByResourTypeId(resourTypeId);
    }
}
