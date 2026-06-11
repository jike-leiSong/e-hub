package cn.sl.ehub.common.dto;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @Description: 工业负荷转换类
 * @Author sl
 * @Date 2026-05-28
 * [{"area":"200","innerStationId":"0001","stationName":"新奥数能","totalCapacity":"500"},{"area":"250","innerStationId":"0002","stationName":"新奥数能","totalCapacity":"300"}]
 */

public class SingleModeDistributedEnergyStorageDTO {

    private String stationName;

    private String area;

    private String totalCapacity;

    private String innerStationId;

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(String totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public String getInnerStationId() {
        return innerStationId;
    }

    public void setInnerStationId(String innerStationId) {
        this.innerStationId = innerStationId;
    }

    public SingleModeDistributedEnergyStorageDTO() {
    }

    public SingleModeDistributedEnergyStorageDTO(String stationName, String area, String totalCapacity, String userType, String owner, String innerStationId) {
        this.stationName = stationName;
        this.area = area;
        this.totalCapacity = totalCapacity;
        this.innerStationId = innerStationId;
    }

    public static void main(String[] args) {
        SingleModeDistributedEnergyStorageDTO singleModeDistributedEnergyStorage1 = new SingleModeDistributedEnergyStorageDTO();
        singleModeDistributedEnergyStorage1.setArea("200");
        singleModeDistributedEnergyStorage1.setInnerStationId("0001");
        singleModeDistributedEnergyStorage1.setStationName("新奥数能");
        singleModeDistributedEnergyStorage1.setTotalCapacity("500");

        SingleModeDistributedEnergyStorageDTO singleModeDistributedEnergyStorage2 = new SingleModeDistributedEnergyStorageDTO();
        singleModeDistributedEnergyStorage2.setArea("250");
        singleModeDistributedEnergyStorage2.setInnerStationId("0002");
        singleModeDistributedEnergyStorage2.setStationName("新奥数能");
        singleModeDistributedEnergyStorage2.setTotalCapacity("300");

        List<SingleModeDistributedEnergyStorageDTO> list = Lists.newArrayList(singleModeDistributedEnergyStorage1, singleModeDistributedEnergyStorage2);

        System.out.println(JSONObject.toJSONString(list));
    }
}
