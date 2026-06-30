package cn.sl.ehub.console.service;

import cn.sl.ehub.service.req.UpdateEntDeviceReq;
import cn.sl.ehub.service.req.UpdateEntReq;
import cn.sl.ehub.service.vo.AggregatorEntDevice;

import java.util.List;
import java.util.Map;

/**
 * 设备Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorEntDeviceService {

    /**
     * 查询数据
     *
     * @return
     */
    List<AggregatorEntDevice> getAggregatorEntDeviceList();

    /**
     * 查询数据
     *
     * @param deviceBaseId
     * @return
     */
    AggregatorEntDevice getAggregatorEntDevice(String deviceBaseId);

    /**
     * 根据能源站查询数据
     *
     * @param energyStationCode
     * @return
     */
    List<AggregatorEntDevice> getDeviceByStationCode(String energyStationCode);

    /**
     * 根据能源站编码查询绑定的企业设备
     *
     * @param energyStationCodes 能源站编码列表
     * @return 设备列表
     */
    List<AggregatorEntDevice> getDevicesByEnergyStationCodes(List<String> energyStationCodes);

    /**
     * 根据企业ID查询绑定的能源站编码
     *
     * @param entId 企业ID
     * @return 能源站编码列表
     */
    List<String> getEnergyStationCodesByEntId(String entId);

    /**
     * 根据电站查询数据
     *
     * @param systemCode
     * @return
     */
    List<AggregatorEntDevice> getDeviceBySystemCode(String systemCode);
    /**
     * 查询数据
     *
     * @param deviceBaseIdList
     * @return
     */
    List<AggregatorEntDevice> getAggregatorEntDeviceList(List<String> deviceBaseIdList);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param entId
     * @param stationId
     * @param resourceTypeId
     * @return
     */
    List<AggregatorEntDevice> getAggregatorEntDeviceList(String aggregatorId, String entId, String stationId, String resourceTypeId);


    /**
     * 查询申报设备数据
     *
     * @param aggregatorId
     * @param entId
     * @param stationId
     * @param resourceTypeId
     * @return
     */
    List<AggregatorEntDevice> getAggregatorEntDeviceListModel(String aggregatorId, String entId, String stationId, String resourceTypeId);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param entId
     * @return
     */
    List<AggregatorEntDevice> getAggregatorEntDeviceList(String aggregatorId, String entId);

    /**
     * 查询数据
     *
     * @param entId
     * @return
     */
    List<AggregatorEntDevice> getAggregatorEntDeviceList(String entId);

    /**
     * 查询数据
     *
     * @param entIdList
     * @return
     */
    List<AggregatorEntDevice> getAggregatorEntDeviceListByEntIdList(List<String> entIdList);

    /**
     * 更新设备
     *
     * @param deviceList
     * @param entId
     * @return
     */
    int updateDeviceInfoList(List<UpdateEntDeviceReq> deviceList, String entId);


    /**
     * @description 根据聚合商id、资源类型id查询设备
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    List<AggregatorEntDevice> getDeviceList(String aggregatorId,  String resourceTypeId);


    /**
     * @description 资源类型id、企业id查询设备
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    List<AggregatorEntDevice> getDeviceListByEntId( String entId, String resourceTypeId);


    /**
     * @description 根据聚合商id、资源类型id查询设备
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    List<AggregatorEntDevice> getDeviceListByAggregatorId(String aggregatorId, String resourceType);


    /**
     * @description 根据聚合商id、资源类型id查询设备
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    List<AggregatorEntDevice> getDeviceListByAggregatorId(String aggregatorId);

    List<AggregatorEntDevice> queryDeviceList(String aggregatorId, String entId, String deviceName, Integer status);

    AggregatorEntDevice getDeviceById(Integer id);

    AggregatorEntDevice createDevice(AggregatorEntDevice device);

    void updateDevice(AggregatorEntDevice device);

    void deleteDevice(Integer id);
}
