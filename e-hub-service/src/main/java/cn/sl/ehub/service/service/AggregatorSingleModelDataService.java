package cn.sl.ehub.service.service;

import cn.sl.ehub.service.mapper.AggregatorSingleModelDataMapper;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorSingleModelData;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
@Service
public class AggregatorSingleModelDataService {
    @Resource
    private AggregatorSingleModelDataMapper aggregatorSingleModelDataMapper;

    public List<AggregatorSingleModelData> getByAggregatorAndResoureId(String aggregatorId, String resourceTypeId, List<String> noUpModelEnergyStationCodes){
        Example example = new Example(AggregatorSingleModelData.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("aggregatorId", aggregatorId);
        criteria.andEqualTo("resourceTypeId",resourceTypeId);
        // modify by sl 024-10-24 增加不上送模型
        if (CollectionUtils.isNotEmpty(noUpModelEnergyStationCodes)) {
            criteria.andNotIn("energyStationCode", noUpModelEnergyStationCodes);
        }
        return aggregatorSingleModelDataMapper.selectByExample(example);
    }

    public List<AggregatorSingleModelData> list(String aggregatorId,
                                                String resourceTypeId,
                                                String energyStationCode,
                                                String energyStation,
                                                List<String> energyStationCodes) {
        Example example = new Example(AggregatorSingleModelData.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(aggregatorId)) {
            criteria.andEqualTo("aggregatorId", StringUtils.trim(aggregatorId));
        }
        if (StringUtils.isNotBlank(resourceTypeId)) {
            criteria.andEqualTo("resourceTypeId", StringUtils.trim(resourceTypeId));
        }
        if (StringUtils.isNotBlank(energyStationCode)) {
            criteria.andEqualTo("energyStationCode", StringUtils.trim(energyStationCode));
        }
        if (StringUtils.isNotBlank(energyStation)) {
            criteria.andLike("energyStation", "%" + StringUtils.trim(energyStation) + "%");
        }
        if (energyStationCodes != null) {
            if (energyStationCodes.isEmpty()) {
                return java.util.Collections.emptyList();
            }
            criteria.andIn("energyStationCode", energyStationCodes);
        }
        example.orderBy("id").desc();
        return aggregatorSingleModelDataMapper.selectByExample(example);
    }

    public AggregatorSingleModelData getById(Integer id) {
        if (id == null) {
            return null;
        }
        return aggregatorSingleModelDataMapper.selectByPrimaryKey(id);
    }

    public AggregatorSingleModelData create(AggregatorSingleModelData data) {
        if (StringUtils.isBlank(data.getEnergyStationCode())) {
            data.setEnergyStationCode(generateEnergyStationCode());
        }
        aggregatorSingleModelDataMapper.insertSelective(data);
        return data.getId() == null ? data : getById(data.getId());
    }

    public AggregatorSingleModelData update(Integer id, AggregatorSingleModelData data) {
        data.setId(id);
        aggregatorSingleModelDataMapper.updateByPrimaryKeySelective(data);
        return getById(id);
    }

    public void delete(Integer id) {
        aggregatorSingleModelDataMapper.deleteByPrimaryKey(id);
    }

    public boolean existsByEnergyStationCode(String energyStationCode, Integer excludeId) {
        if (StringUtils.isBlank(energyStationCode)) {
            return false;
        }
        Example example = new Example(AggregatorSingleModelData.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("energyStationCode", StringUtils.trim(energyStationCode));
        if (excludeId != null) {
            criteria.andNotEqualTo("id", excludeId);
        }
        return aggregatorSingleModelDataMapper.selectCountByExample(example) > 0;
    }

    public String generateEnergyStationCode() {
        for (int i = 0; i < 10; i++) {
            String code = UUID.randomUUID().toString().replace("-", "");
            if (code.length() > 24) {
                code = code.substring(0, 24);
            }
            if (!existsByEnergyStationCode(code, null)) {
                return code;
            }
        }
        throw new IllegalStateException("项目编码生成失败");
    }

    public List<AggregatorSingleModelData> listByEntId(String entId, List<String> energyStationCodes) {
        if (CollectionUtils.isEmpty(energyStationCodes)) {
            return Collections.emptyList();
        }
        return list(null, null, null, null, energyStationCodes);
    }

    public List<AggregatorSingleModelData> getByEnergyStationCodes(List<String> energyStationCodes) {
        if (CollectionUtils.isEmpty(energyStationCodes)) {
            return Collections.emptyList();
        }
        Example example = new Example(AggregatorSingleModelData.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("energyStationCode", energyStationCodes);
        return aggregatorSingleModelDataMapper.selectByExample(example);
    }
}
