package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorResourceTypeMapper;
import cn.sl.ehub.console.model.vo.OptionVO;
import cn.sl.ehub.console.service.IAggregatorEntDeviceService;
import cn.sl.ehub.console.service.IAggregatorEntDeviceService;
import cn.sl.ehub.console.service.IAggregatorResourceTypeService;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorResourceDateIssueOffer;
import cn.sl.ehub.service.vo.AggregatorResourceType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 资源类型ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorResourceTypeServiceImpl implements IAggregatorResourceTypeService {

    private final AggregatorResourceTypeMapper aggregatorResourceTypeMapper;
    private final IAggregatorEntDeviceService aggregatorEntDeviceService;

    @Override
    public List<AggregatorResourceType> getAggregatorResourceTypeList() {
        Weekend<AggregatorResourceType> weekend = Weekend.of(AggregatorResourceType.class);
        weekend.orderBy("resourceOrder");
        return aggregatorResourceTypeMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorResourceType> getAggregatorShowResourceTypeList() {
        Weekend<AggregatorResourceType> weekend = Weekend.of(AggregatorResourceType.class);
        WeekendCriteria<AggregatorResourceType, Object> criteria = weekend.weekendCriteria();
        criteria.
                andEqualTo(AggregatorResourceType::getDisplay,1);
        weekend.orderBy("resourceOrder");
        return aggregatorResourceTypeMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorResourceType> getAggregatorResourceTypeListByAggregatorId(String aggregatorId) {
        Weekend<AggregatorResourceType> weekend = Weekend.of(AggregatorResourceType.class);
        WeekendCriteria<AggregatorResourceType, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorResourceType::getAggregatorId,aggregatorId);
        weekend.orderBy("resourceOrder");
        return aggregatorResourceTypeMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorResourceType> getAggregatorResourceTypeListByAggregatorId(String aggregatorId, String entId) {
        Weekend<AggregatorResourceType> weekend = Weekend.of(AggregatorResourceType.class);
        WeekendCriteria<AggregatorResourceType, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorResourceType::getAggregatorId, aggregatorId);
        weekend.orderBy("resourceOrder");
        List<AggregatorResourceType> aggregatorResourceTypeList = aggregatorResourceTypeMapper.selectByExample(weekend);
        if (StringUtils.isNotEmpty(entId)) {
            List<AggregatorEntDevice> deviceListByAggregatorId = aggregatorEntDeviceService.getDeviceListByAggregatorId(aggregatorId);
            if (CollectionUtils.isEmpty(deviceListByAggregatorId)) {
                return new ArrayList<>();
            }
            // 筛选该企业下设备数量大于0的资源类型
            List<String> resourceTypeIdList = deviceListByAggregatorId.stream()
                    .filter(device -> null != device && device.getModelFlag() == 1 && device.getEntId().equals(entId))
                    .collect(Collectors.groupingBy(AggregatorEntDevice::getResourceTypeId, Collectors.counting()))
                    .entrySet().stream().filter(entry -> entry.getValue() > 0)
                    .map(Map.Entry::getKey).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(resourceTypeIdList)) {
                return new ArrayList<>();
            }
            return aggregatorResourceTypeList.stream().filter(resourceType -> null != resourceType && resourceTypeIdList.contains(resourceType.getId())).collect(Collectors.toList());
        }
        return aggregatorResourceTypeList;
    }

    @Override
    public List<AggregatorResourceType> getAggregatorDisplayResourceTypeList(String aggregatorId) {
        Weekend<AggregatorResourceType> weekend = Weekend.of(AggregatorResourceType.class);
        WeekendCriteria<AggregatorResourceType, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorResourceType::getAggregatorId,aggregatorId).
                andEqualTo(AggregatorResourceType::getDisplay,1);
        weekend.orderBy("resourceOrder");
        return aggregatorResourceTypeMapper.selectByExample(weekend);
    }

    @Override
    public AggregatorResourceType getTypeById(String id) {
        return aggregatorResourceTypeMapper.selectByPrimaryKey(id);
    }
}
