package cn.sl.ehub.service.service;

import cn.sl.ehub.service.dto.iot.ResolvedIotScope;
import cn.sl.ehub.service.mapper.AggregatorEntDeviceMapper;
import cn.sl.ehub.service.mapper.AggregatorSingleModelDataMapper;
import cn.sl.ehub.service.mapper.IotDeviceMapper;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorSingleModelData;
import cn.sl.ehub.service.vo.IotDevice;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IotBusinessScopeResolver {

    private final AggregatorSingleModelDataMapper aggregatorSingleModelDataMapper;
    private final AggregatorEntDeviceMapper aggregatorEntDeviceMapper;
    private final IotDeviceMapper iotDeviceMapper;

    public IotBusinessScopeResolver(AggregatorSingleModelDataMapper aggregatorSingleModelDataMapper,
                                    AggregatorEntDeviceMapper aggregatorEntDeviceMapper,
                                    IotDeviceMapper iotDeviceMapper) {
        this.aggregatorSingleModelDataMapper = aggregatorSingleModelDataMapper;
        this.aggregatorEntDeviceMapper = aggregatorEntDeviceMapper;
        this.iotDeviceMapper = iotDeviceMapper;
    }

    public ResolvedIotScope resolve(String aggregatorId,
                                    String entId,
                                    String energyStationCode,
                                    List<Long> requestedDeviceIds) {
        String scopedAggregatorId = StringUtils.trimToNull(aggregatorId);
        String scopedEntId = StringUtils.trimToNull(entId);
        String scopedEnergyStationCode = StringUtils.trimToNull(energyStationCode);
        List<Long> normalizedRequestedDeviceIds = normalizeDeviceIds(requestedDeviceIds);
        boolean relationScope = StringUtils.isNotBlank(scopedAggregatorId)
                || StringUtils.isNotBlank(scopedEntId)
                || StringUtils.isNotBlank(scopedEnergyStationCode);

        if (StringUtils.isNotBlank(scopedEnergyStationCode)) {
            AggregatorSingleModelData model = findBusinessProject(
                    scopedAggregatorId, scopedEntId, scopedEnergyStationCode);
            if (model == null) {
                return empty(scopedAggregatorId, scopedEntId, scopedEnergyStationCode, true);
            }
            scopedAggregatorId = StringUtils.defaultIfBlank(scopedAggregatorId, model.getAggregatorId());
            scopedEntId = StringUtils.defaultIfBlank(scopedEntId, model.getEntId());
        }

        if (!relationScope) {
            return resolveDirectIotDevices(scopedAggregatorId, scopedEntId,
                    scopedEnergyStationCode, normalizedRequestedDeviceIds);
        }

        List<AggregatorEntDevice> businessDevices = selectBusinessDevices(
                scopedAggregatorId, scopedEntId, scopedEnergyStationCode);
        if (CollectionUtils.isEmpty(businessDevices)) {
            return empty(scopedAggregatorId, scopedEntId, scopedEnergyStationCode, true);
        }

        ResolvedIotScope scope = mapBusinessDevices(scopedAggregatorId, scopedEntId,
                scopedEnergyStationCode, businessDevices);
        if (CollectionUtils.isNotEmpty(normalizedRequestedDeviceIds)) {
            Set<Long> requestedSet = new LinkedHashSet<>(normalizedRequestedDeviceIds);
            List<Long> filteredDeviceIds = scope.getDeviceIds().stream()
                    .filter(requestedSet::contains)
                    .collect(Collectors.toList());
            scope.setDeviceIds(filteredDeviceIds);
            scope.setDeviceCodes(Collections.emptyList());
        }
        scope.setDeviceRestricted(true);
        if (CollectionUtils.isEmpty(scope.getDeviceIds())) {
            scope.setEmptyScope(true);
        }
        return scope;
    }

    private ResolvedIotScope resolveDirectIotDevices(String aggregatorId,
                                                     String entId,
                                                     String energyStationCode,
                                                     List<Long> requestedDeviceIds) {
        ResolvedIotScope scope = new ResolvedIotScope();
        scope.setAggregatorId(aggregatorId);
        scope.setEntId(entId);
        scope.setEnergyStationCode(energyStationCode);
        if (CollectionUtils.isEmpty(requestedDeviceIds)) {
            scope.setDeviceRestricted(false);
            return scope;
        }

        List<IotDevice> devices = selectIotDevicesByIds(requestedDeviceIds, aggregatorId, entId);
        scope.setDeviceRestricted(true);
        scope.setDeviceIds(devices.stream().map(IotDevice::getId).collect(Collectors.toList()));
        scope.setDeviceCodes(devices.stream()
                .map(IotDevice::getDeviceCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList()));
        scope.setEmptyScope(CollectionUtils.isEmpty(scope.getDeviceIds()));
        return scope;
    }

    private AggregatorSingleModelData findBusinessProject(String aggregatorId,
                                                          String entId,
                                                          String energyStationCode) {
        List<AggregatorSingleModelData> models = aggregatorSingleModelDataMapper.selectModelList(
                StringUtils.trimToNull(aggregatorId),
                StringUtils.trimToNull(entId),
                null,
                StringUtils.trimToNull(energyStationCode),
                null,
                null);
        return CollectionUtils.isEmpty(models) ? null : models.get(0);
    }

    private List<AggregatorEntDevice> selectBusinessDevices(String aggregatorId,
                                                            String entId,
                                                            String energyStationCode) {
        Example example = new Example(AggregatorEntDevice.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(aggregatorId)) {
            criteria.andEqualTo("aggregatorId", aggregatorId);
        }
        if (StringUtils.isNotBlank(entId)) {
            criteria.andEqualTo("entId", entId);
        }
        if (StringUtils.isNotBlank(energyStationCode)) {
            criteria.andEqualTo("energyStationCode", energyStationCode);
        }
        List<AggregatorEntDevice> rows = aggregatorEntDeviceMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(rows)) {
            return Collections.emptyList();
        }
        return rows.stream()
                .filter(this::isUsableBusinessDevice)
                .collect(Collectors.toList());
    }

    private ResolvedIotScope mapBusinessDevices(String aggregatorId,
                                                String entId,
                                                String energyStationCode,
                                                List<AggregatorEntDevice> businessDevices) {
        ResolvedIotScope scope = new ResolvedIotScope();
        scope.setAggregatorId(aggregatorId);
        scope.setEntId(entId);
        scope.setEnergyStationCode(energyStationCode);
        scope.setDeviceRestricted(true);

        Map<Long, IotDevice> resolvedDevices = new LinkedHashMap<>();
        Set<String> deviceCodes = businessDevices.stream()
                .map(AggregatorEntDevice::getDeviceId)
                .map(StringUtils::trimToNull)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, List<IotDevice>> iotDeviceByCode = selectIotDevicesByCodes(deviceCodes, aggregatorId, entId)
                .stream()
                .collect(Collectors.groupingBy(IotDevice::getDeviceCode, LinkedHashMap::new, Collectors.toList()));

        List<AggregatorEntDevice> unresolvedRows = new ArrayList<>();
        for (AggregatorEntDevice row : businessDevices) {
            IotDevice device = findDeviceByCode(row, iotDeviceByCode);
            if (device == null) {
                unresolvedRows.add(row);
                continue;
            }
            resolvedDevices.put(device.getId(), device);
        }

        Map<Long, IotDevice> iotDeviceById = selectFallbackIotDevices(unresolvedRows, aggregatorId, entId);
        for (AggregatorEntDevice row : unresolvedRows) {
            Long iotDeviceId = parseLong(row.getIotDeviceBaseId());
            IotDevice device = iotDeviceId == null ? null : iotDeviceById.get(iotDeviceId);
            if (device != null && matchesBusinessRow(device, row)) {
                resolvedDevices.put(device.getId(), device);
            }
        }

        scope.setDeviceIds(new ArrayList<>(resolvedDevices.keySet()));
        scope.setDeviceCodes(resolvedDevices.values().stream()
                .map(IotDevice::getDeviceCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList()));
        scope.setEmptyScope(resolvedDevices.isEmpty());
        return scope;
    }

    private List<IotDevice> selectIotDevicesByCodes(Set<String> deviceCodes,
                                                    String aggregatorId,
                                                    String entId) {
        if (CollectionUtils.isEmpty(deviceCodes)) {
            return Collections.emptyList();
        }
        Example example = new Example(IotDevice.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("deviceCode", new ArrayList<>(deviceCodes));
        criteria.andEqualTo("deleted", 0);
        if (StringUtils.isNotBlank(aggregatorId)) {
            criteria.andEqualTo("aggregatorId", aggregatorId);
        }
        if (StringUtils.isNotBlank(entId)) {
            criteria.andEqualTo("entId", entId);
        }
        return iotDeviceMapper.selectByExample(example).stream()
                .filter(this::isUsableIotDevice)
                .collect(Collectors.toList());
    }

    private Map<Long, IotDevice> selectFallbackIotDevices(List<AggregatorEntDevice> unresolvedRows,
                                                          String aggregatorId,
                                                          String entId) {
        List<Long> ids = unresolvedRows.stream()
                .map(AggregatorEntDevice::getIotDeviceBaseId)
                .map(this::parseLong)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        List<IotDevice> devices = selectIotDevicesByIds(ids, aggregatorId, entId);
        return devices.stream().collect(Collectors.toMap(IotDevice::getId, d -> d, (a, b) -> a));
    }

    private List<IotDevice> selectIotDevicesByIds(List<Long> ids, String aggregatorId, String entId) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        Example example = new Example(IotDevice.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", ids);
        criteria.andEqualTo("deleted", 0);
        if (StringUtils.isNotBlank(aggregatorId)) {
            criteria.andEqualTo("aggregatorId", aggregatorId);
        }
        if (StringUtils.isNotBlank(entId)) {
            criteria.andEqualTo("entId", entId);
        }
        return iotDeviceMapper.selectByExample(example).stream()
                .filter(this::isUsableIotDevice)
                .collect(Collectors.toList());
    }

    private IotDevice findDeviceByCode(AggregatorEntDevice row,
                                       Map<String, List<IotDevice>> iotDeviceByCode) {
        String deviceCode = StringUtils.trimToNull(row.getDeviceId());
        if (StringUtils.isBlank(deviceCode)) {
            return null;
        }
        List<IotDevice> candidates = iotDeviceByCode.get(deviceCode);
        if (CollectionUtils.isEmpty(candidates)) {
            return null;
        }
        for (IotDevice candidate : candidates) {
            if (matchesBusinessRow(candidate, row)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean matchesBusinessRow(IotDevice device, AggregatorEntDevice row) {
        if (device == null || row == null) {
            return false;
        }
        if (StringUtils.isNotBlank(row.getAggregatorId())
                && !StringUtils.equals(row.getAggregatorId(), device.getAggregatorId())) {
            return false;
        }
        return StringUtils.isBlank(row.getEntId()) || StringUtils.equals(row.getEntId(), device.getEntId());
    }

    private boolean isUsableBusinessDevice(AggregatorEntDevice row) {
        return row != null && !Integer.valueOf(0).equals(row.getDelFlag());
    }

    private boolean isUsableIotDevice(IotDevice device) {
        return device != null
                && !Integer.valueOf(1).equals(device.getDeleted())
                && !Integer.valueOf(0).equals(device.getStatus());
    }

    private List<Long> normalizeDeviceIds(List<Long> deviceIds) {
        if (CollectionUtils.isEmpty(deviceIds)) {
            return Collections.emptyList();
        }
        return deviceIds.stream()
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
    }

    private ResolvedIotScope empty(String aggregatorId,
                                   String entId,
                                   String energyStationCode,
                                   boolean deviceRestricted) {
        ResolvedIotScope scope = new ResolvedIotScope();
        scope.setAggregatorId(aggregatorId);
        scope.setEntId(entId);
        scope.setEnergyStationCode(energyStationCode);
        scope.setDeviceRestricted(deviceRestricted);
        scope.setEmptyScope(true);
        return scope;
    }

    private Long parseLong(String value) {
        String trimmed = StringUtils.trimToNull(value);
        if (StringUtils.isBlank(trimmed) || !StringUtils.isNumeric(trimmed)) {
            return null;
        }
        try {
            return Long.valueOf(trimmed);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
