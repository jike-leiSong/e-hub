package cn.sl.ehub.service.service;

import cn.sl.ehub.service.mapper.AggregatorSingleModelDataMapper;
import cn.sl.ehub.service.vo.AggregatorSingleModelData;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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
        List<AggregatorSingleModelData> list = aggregatorSingleModelDataMapper.selectModelList(
                StringUtils.trimToNull(aggregatorId),
                null,
                StringUtils.trimToNull(resourceTypeId),
                null,
                null,
                null);
        if (CollectionUtils.isNotEmpty(noUpModelEnergyStationCodes)) {
            return list.stream()
                    .filter(item -> !noUpModelEnergyStationCodes.contains(item.getEnergyStationCode()))
                    .collect(java.util.stream.Collectors.toList());
        }
        return list;
    }

    public List<AggregatorSingleModelData> list(String aggregatorId,
                                                String resourceTypeId,
                                                String energyStationCode,
                                                String energyStation,
                                                List<String> energyStationCodes) {
        return list(aggregatorId, null, resourceTypeId, energyStationCode, energyStation, energyStationCodes);
    }

    public List<AggregatorSingleModelData> list(String aggregatorId,
                                                String entId,
                                                String resourceTypeId,
                                                String energyStationCode,
                                                String energyStation,
                                                List<String> energyStationCodes) {
        if (energyStationCodes != null) {
            if (energyStationCodes.isEmpty()) {
                return java.util.Collections.emptyList();
            }
        }
        return aggregatorSingleModelDataMapper.selectModelList(
                StringUtils.trimToNull(aggregatorId),
                StringUtils.trimToNull(entId),
                StringUtils.trimToNull(resourceTypeId),
                StringUtils.trimToNull(energyStationCode),
                StringUtils.trimToNull(energyStation),
                energyStationCodes);
    }

    public List<AggregatorSingleModelData> listByEnt(String aggregatorId, String entId) {
        if (StringUtils.isBlank(entId)) {
            return Collections.emptyList();
        }
        return list(aggregatorId, entId, null, null, null, null);
    }

    public AggregatorSingleModelData getById(Integer id) {
        if (id == null) {
            return null;
        }
        return aggregatorSingleModelDataMapper.selectModelById(id);
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
        return aggregatorSingleModelDataMapper.countByEnergyStationCode(StringUtils.trim(energyStationCode), excludeId) > 0;
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
        if (StringUtils.isBlank(entId) && CollectionUtils.isEmpty(energyStationCodes)) {
            return Collections.emptyList();
        }
        return list(null, entId, null, null, null, energyStationCodes);
    }

    public List<AggregatorSingleModelData> getByEnergyStationCodes(List<String> energyStationCodes) {
        if (CollectionUtils.isEmpty(energyStationCodes)) {
            return Collections.emptyList();
        }
        return aggregatorSingleModelDataMapper.selectModelList(null, null, null, null, null, energyStationCodes);
    }
}
