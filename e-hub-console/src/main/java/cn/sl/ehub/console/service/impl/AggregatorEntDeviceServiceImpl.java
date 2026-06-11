package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorEntDeviceMapper;
import cn.sl.ehub.service.req.UpdateEntDeviceReq;
import cn.sl.ehub.service.req.UpdateEntReq;
import cn.sl.ehub.console.service.IAggregatorEntDeviceService;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import javax.persistence.Column;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 设备ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AggregatorEntDeviceServiceImpl implements IAggregatorEntDeviceService {

    private final AggregatorEntDeviceMapper aggregatorEntDeviceMapper;

    @Override
    public List<AggregatorEntDevice> getAggregatorEntDeviceList() {
        return aggregatorEntDeviceMapper.selectAll();
    }

    @Override
    public AggregatorEntDevice getAggregatorEntDevice(String deviceBaseId) {
        Weekend<AggregatorEntDevice> weekend = Weekend.of(AggregatorEntDevice.class);
        WeekendCriteria<AggregatorEntDevice, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDevice::getDeviceBaseId, deviceBaseId);
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceMapper.selectByExample(weekend);
        if (null != deviceList && deviceList.size() > 0) {
            return deviceList.get(0);
        }
        return null;
    }

    @Override
    public List<AggregatorEntDevice> getDeviceByStationCode(String energyStationCode) {
        Weekend<AggregatorEntDevice> weekend = Weekend.of(AggregatorEntDevice.class);
        WeekendCriteria<AggregatorEntDevice, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDevice::getEnergyStationCode, energyStationCode);
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceMapper.selectByExample(weekend);
        if (null != deviceList && deviceList.size() > 0) {
            return deviceList;
        }
        return null;
    }

    @Override
    public List<AggregatorEntDevice> getDeviceBySystemCode(String systemCode) {
        Weekend<AggregatorEntDevice> weekend = Weekend.of(AggregatorEntDevice.class);
        WeekendCriteria<AggregatorEntDevice, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDevice::getStationId, systemCode);
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceMapper.selectByExample(weekend);
        if (null != deviceList && deviceList.size() > 0) {
            return deviceList;
        }
        return null;
    }


    @Override
    public List<AggregatorEntDevice> getAggregatorEntDeviceList(List<String> deviceBaseIdList) {
        Weekend<AggregatorEntDevice> weekend = Weekend.of(AggregatorEntDevice.class);
        WeekendCriteria<AggregatorEntDevice, Object> criteria = weekend.weekendCriteria();
        criteria.andIn(AggregatorEntDevice::getDeviceBaseId, deviceBaseIdList);
        return aggregatorEntDeviceMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorEntDevice> getAggregatorEntDeviceList(String aggregatorId, String entId, String stationId, String resourceTypeId) {
        Weekend<AggregatorEntDevice> weekend = Weekend.of(AggregatorEntDevice.class);
        WeekendCriteria<AggregatorEntDevice, Object> criteria = weekend.weekendCriteria();
        if (StringUtils.isNotEmpty(aggregatorId)) {
            criteria.andEqualTo(AggregatorEntDevice::getAggregatorId, aggregatorId);
        }
        if (StringUtils.isNotEmpty(entId)) {
            criteria.andEqualTo(AggregatorEntDevice::getEntId, entId);
        }
        if (StringUtils.isNotEmpty(stationId)) {
            criteria.andEqualTo(AggregatorEntDevice::getStationId, stationId);
        }
        if (StringUtils.isNotEmpty(resourceTypeId)) {
            criteria.andEqualTo(AggregatorEntDevice::getResourceTypeId, resourceTypeId);
        }
        // modify by sl 2022-11-21 删除上送企业
        criteria.andEqualTo(AggregatorEntDevice::getModelFlag, 1);

        return aggregatorEntDeviceMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorEntDevice> getAggregatorEntDeviceListModel(String aggregatorId, String entId, String stationId, String resourceTypeId) {
        Weekend<AggregatorEntDevice> weekend = Weekend.of(AggregatorEntDevice.class);
        WeekendCriteria<AggregatorEntDevice, Object> criteria = weekend.weekendCriteria();
        if (StringUtils.isNotEmpty(aggregatorId)) {
            criteria.andEqualTo(AggregatorEntDevice::getAggregatorId, aggregatorId);
        }
        if (StringUtils.isNotEmpty(entId)) {
            criteria.andEqualTo(AggregatorEntDevice::getEntId, entId);
        }
        if (StringUtils.isNotEmpty(stationId)) {
            criteria.andEqualTo(AggregatorEntDevice::getStationId, stationId);
        }
        if (StringUtils.isNotEmpty(resourceTypeId)) {
            criteria.andEqualTo(AggregatorEntDevice::getResourceTypeId, resourceTypeId);
        }
        criteria.andEqualTo(AggregatorEntDevice::getModelFlag, 1);
        return aggregatorEntDeviceMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorEntDevice> getAggregatorEntDeviceList(String aggregatorId, String entId) {
        Weekend<AggregatorEntDevice> weekend = Weekend.of(AggregatorEntDevice.class);
        WeekendCriteria<AggregatorEntDevice, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDevice::getAggregatorId, aggregatorId);
        criteria.andEqualTo(AggregatorEntDevice::getEntId, entId);
        return aggregatorEntDeviceMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorEntDevice> getAggregatorEntDeviceList(String entId) {
        Weekend<AggregatorEntDevice> weekend = Weekend.of(AggregatorEntDevice.class);
        WeekendCriteria<AggregatorEntDevice, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDevice::getEntId, entId);
        return aggregatorEntDeviceMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorEntDevice> getAggregatorEntDeviceListByEntIdList(List<String> entIdList) {
        Weekend<AggregatorEntDevice> weekend = Weekend.of(AggregatorEntDevice.class);
        WeekendCriteria<AggregatorEntDevice, Object> criteria = weekend.weekendCriteria();
        criteria.andIn(AggregatorEntDevice::getEntId, entIdList);
        return aggregatorEntDeviceMapper.selectByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDeviceInfoList(List<UpdateEntDeviceReq> deviceList, String entId) {
        List<AggregatorEntDevice> addList = Lists.newArrayList();
        List<AggregatorEntDevice> aggregatorEntDeviceList = getAggregatorEntDeviceList(entId);
        if (CollectionUtils.isEmpty(deviceList)) {
            aggregatorEntDeviceList.forEach(aggregatorEntDevice -> {
                AggregatorEntDevice addDevice = new AggregatorEntDevice();
                BeanUtils.copyProperties(aggregatorEntDevice, addDevice);
                addDevice.setDelFlag(0);
                addList.add(addDevice);
            });
        }else if (CollectionUtils.isEmpty(aggregatorEntDeviceList)) {
            //如果第一次录入设备直接入库
            deviceList.forEach(device -> {
                AggregatorEntDevice addDevice = new AggregatorEntDevice();
                addDevice.setAggregatorId(device.getAggregatorId());
                addDevice.setEntId(device.getEntId());
                addDevice.setStationId(device.getStationId());
                addDevice.setDeviceBaseId(device.getDeviceBaseId());
                addDevice.setDeviceName(device.getDeviceName());
                addDevice.setDeviceType("METE");
                addDevice.setDeviceId(device.getDeviceId());
                addDevice.setAccountNo(device.getAccountNo());
                addDevice.setPower(device.getPower());
                addDevice.setMaxPower(device.getMaxPower());
                addDevice.setResponsePower(device.getResponsePower());
                addDevice.setDataSource("EMS");
                addDevice.setResourceTypeId(StringUtils.isEmpty(device.getResourceTypeId()) ? "26" : device.getResourceTypeId());
                addDevice.setStatus(0);
                addDevice.setDelFlag(1);
                addList.add(addDevice);
            });
        }  else {
            aggregatorEntDeviceList.forEach(aggregatorEntDevice -> {
                AggregatorEntDevice addDevice = new AggregatorEntDevice();
                BeanUtils.copyProperties(aggregatorEntDevice, addDevice);
                addDevice.setDelFlag(0);
                addList.add(addDevice);
            });
            Map<String, AggregatorEntDevice> deviceBaseIdMap = addList.stream().collect(Collectors.toMap(AggregatorEntDevice::getDeviceBaseId, Function.identity(), (v1, v2) -> v1));
            deviceList.forEach(device -> {
                AggregatorEntDevice aggregatorEntDevice = deviceBaseIdMap.get(device.getDeviceBaseId());
                if (null != aggregatorEntDevice) {
                    aggregatorEntDevice.setAccountNo(device.getAccountNo());
                    aggregatorEntDevice.setPower(device.getPower());
                    aggregatorEntDevice.setMaxPower(device.getMaxPower());
                    aggregatorEntDevice.setResponsePower(device.getResponsePower());
                    aggregatorEntDevice.setDelFlag(1);
                } else {
                    aggregatorEntDevice = new AggregatorEntDevice();
                    aggregatorEntDevice.setAggregatorId(device.getAggregatorId());
                    aggregatorEntDevice.setEntId(device.getEntId());
                    aggregatorEntDevice.setStationId(device.getStationId());
                    aggregatorEntDevice.setDeviceBaseId(device.getDeviceBaseId());
                    aggregatorEntDevice.setDeviceName(device.getDeviceName());
                    aggregatorEntDevice.setDeviceType("METE");
                    aggregatorEntDevice.setDeviceId(device.getDeviceId());
                    aggregatorEntDevice.setAccountNo(device.getAccountNo());
                    aggregatorEntDevice.setPower(device.getPower());
                    aggregatorEntDevice.setMaxPower(device.getMaxPower());
                    aggregatorEntDevice.setResponsePower(device.getResponsePower());
                    aggregatorEntDevice.setDataSource("EMS");
                    aggregatorEntDevice.setResourceTypeId(StringUtils.isEmpty(device.getResourceTypeId()) ? "26" : device.getResourceTypeId());
                    aggregatorEntDevice.setStatus(0);
                    aggregatorEntDevice.setDelFlag(1);
                    addList.add(aggregatorEntDevice);
                }
            });
        }
        Weekend<AggregatorEntDevice> weekend = Weekend.of(AggregatorEntDevice.class);
        WeekendCriteria<AggregatorEntDevice, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDevice::getEntId, entId);
        aggregatorEntDeviceMapper.deleteByExample(weekend);
        if(CollectionUtils.isNotEmpty(addList)) {
            aggregatorEntDeviceMapper.batchInsert(addList);
        }
        return deviceList.size();
    }

    @Override
    public List<AggregatorEntDevice> getDeviceList(String aggregatorId, String resourceTypeId) {
        Weekend<AggregatorEntDevice> weekend = Weekend.of(AggregatorEntDevice.class);
        WeekendCriteria<AggregatorEntDevice, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDevice::getAggregatorId, aggregatorId)
                .andEqualTo(AggregatorEntDevice::getResourceTypeId,resourceTypeId)
                .andEqualTo(AggregatorEntDevice::getModelFlag,1);
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceMapper.selectByExample(weekend);
        return deviceList;
    }

    @Override
    public List<AggregatorEntDevice> getDeviceListByEntId (String entId, String resourceTypeId) {
        Weekend<AggregatorEntDevice> weekend = Weekend.of(AggregatorEntDevice.class);
        WeekendCriteria<AggregatorEntDevice, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDevice::getResourceTypeId,resourceTypeId)
                .andEqualTo(AggregatorEntDevice::getEntId,entId)
                .andEqualTo(AggregatorEntDevice::getModelFlag,1);
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceMapper.selectByExample(weekend);
        return deviceList;
    }

    @Override
    public List<AggregatorEntDevice> getDeviceListByAggregatorId(String aggregatorId, String resourceType) {
        Weekend<AggregatorEntDevice> weekend = Weekend.of(AggregatorEntDevice.class);
        WeekendCriteria<AggregatorEntDevice, Object> criteria = weekend.weekendCriteria();
        // modify by sl 2024-04-28 根据资源类型查询设备
        criteria.andEqualTo(AggregatorEntDevice::getAggregatorId, aggregatorId).andEqualTo(AggregatorEntDevice::getResourceTypeId, resourceType);
        //  modify by sl 2024-10-23 删除不上送企业
        criteria.andEqualTo(AggregatorEntDevice::getModelFlag,1);
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceMapper.selectByExample(weekend);
        log.info("查询到设备：{}", JSON.toJSONString(deviceList));
        return deviceList;
    }

    @Override
    public List<AggregatorEntDevice> getDeviceListByAggregatorId(String aggregatorId) {
        Weekend<AggregatorEntDevice> weekend = Weekend.of(AggregatorEntDevice.class);
        WeekendCriteria<AggregatorEntDevice, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDevice::getAggregatorId, aggregatorId);
        //  modify by sl 2024-10-23 删除不上送企业
        criteria.andEqualTo(AggregatorEntDevice::getModelFlag,1);
        return aggregatorEntDeviceMapper.selectByExample(weekend);
    }
}
