package cn.sl.ehub.service.service;

import cn.sl.ehub.service.mapper.AggregatorSingleModelDataMapper;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorSingleModelData;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
@Service
public class AggregatorSingleModelDataService {
    @Resource
    private AggregatorSingleModelDataMapper aggregatorSingleModelDataMapper;

    public List<AggregatorSingleModelData> getByAggregatorAndResoureId(String aggregatorId, String resourceTypeId, List<String> noUpModelEnergyStationCodes){
        Example example = new Example(AggregatorEntDevice.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("aggregatorId", aggregatorId);
        criteria.andEqualTo("resourceTypeId",resourceTypeId);
        // modify by sl 024-10-24 增加不上送模型
        if (CollectionUtils.isNotEmpty(noUpModelEnergyStationCodes)) {
            criteria.andNotIn("energyStationCode", noUpModelEnergyStationCodes);
        }
        return aggregatorSingleModelDataMapper.selectByExample(example);
    }
}
