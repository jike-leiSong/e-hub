package cn.sl.ehub.common.dto;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @Description: 工业负荷转换类
 * @Author sl
 * @Date 2026-05-28
 *
 * [{"innerStationId":"1","stationName":"新奥数能","todayZeroElecQuantity":"1000","totalActivePower":"900","totalReactivePower":"100"},{"innerStationId":"2","stationName":"新奥数能","todayZeroElecQuantity":"2000","totalActivePower":"1800","totalReactivePower":"200"}]
 */

public class SingleMeasDistributedEnergyStorageDTO {

    private String stationName;

    private String totalActivePower;

    private String totalReactivePower;

    private String todayZeroElecQuantity;

    private String innerStationId;

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getTotalActivePower() {
        return totalActivePower;
    }

    public void setTotalActivePower(String totalActivePower) {
        this.totalActivePower = totalActivePower;
    }

    public String getTotalReactivePower() {
        return totalReactivePower;
    }

    public void setTotalReactivePower(String totalReactivePower) {
        this.totalReactivePower = totalReactivePower;
    }

    public String getTodayZeroElecQuantity() {
        return todayZeroElecQuantity;
    }

    public void setTodayZeroElecQuantity(String todayZeroElecQuantity) {
        this.todayZeroElecQuantity = todayZeroElecQuantity;
    }

    public String getInnerStationId() {
        return innerStationId;
    }

    public void setInnerStationId(String innerStationId) {
        this.innerStationId = innerStationId;
    }

    public SingleMeasDistributedEnergyStorageDTO() {
    }

    public SingleMeasDistributedEnergyStorageDTO(String stationName, String totalActivePower, String totalReactivePower, String todayZeroElecQuantity, String innerStationId) {
        this.stationName = stationName;
        this.totalActivePower = totalActivePower;
        this.totalReactivePower = totalReactivePower;
        this.todayZeroElecQuantity = todayZeroElecQuantity;
        this.innerStationId = innerStationId;
    }

    public static void main(String[] args) {

        SingleMeasDistributedEnergyStorageDTO singleMeasDistributedEnergyStorage1 = new SingleMeasDistributedEnergyStorageDTO();
        singleMeasDistributedEnergyStorage1.setInnerStationId("1");
        singleMeasDistributedEnergyStorage1.setStationName("新奥数能");
        singleMeasDistributedEnergyStorage1.setTodayZeroElecQuantity("1000");
        singleMeasDistributedEnergyStorage1.setTotalActivePower("900");
        singleMeasDistributedEnergyStorage1.setTotalReactivePower("100");

        SingleMeasDistributedEnergyStorageDTO singleMeasDistributedEnergyStorage2 = new SingleMeasDistributedEnergyStorageDTO();
        singleMeasDistributedEnergyStorage2.setInnerStationId("2");
        singleMeasDistributedEnergyStorage2.setStationName("新奥数能");
        singleMeasDistributedEnergyStorage2.setTodayZeroElecQuantity("2000");
        singleMeasDistributedEnergyStorage2.setTotalActivePower("1800");
        singleMeasDistributedEnergyStorage2.setTotalReactivePower("200");

        List<SingleMeasDistributedEnergyStorageDTO> list = Lists.newArrayList(singleMeasDistributedEnergyStorage1,singleMeasDistributedEnergyStorage2);

        System.out.println(JSONObject.toJSONString(list));

    }
}
