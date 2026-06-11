package cn.sl.ehub.common.dto;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @Description: 工业负荷转换类
 * @Author sl
 * @Date 2026-05-28
 * [{"area":"100","capacity":"1000","controllable":"1","equipManufactor":"1","innerEquipId":"200","owner":"新奥数能","storageType":"电加热","userType":"用能","username":"新奥数能"},{"area":"100","capacity":"1000","controllable":"1","equipManufactor":"1","innerEquipId":"200","owner":"新奥数能","storageType":"电加热","userType":"用能","username":"新奥数能"}]
 */

public class SingleModeElectricHeatingDTO {

    private String username;

    private String capacity;

    private String area;

    private String userType;

    private String equipManufactor;

    private String storageType;

    private String owner;

    private String controllable;

    private String innerEquipId;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getEquipManufactor() {
        return equipManufactor;
    }

    public void setEquipManufactor(String equipManufactor) {
        this.equipManufactor = equipManufactor;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getControllable() {
        return controllable;
    }

    public void setControllable(String controllable) {
        this.controllable = controllable;
    }

    public String getInnerEquipId() {
        return innerEquipId;
    }

    public void setInnerEquipId(String innerEquipId) {
        this.innerEquipId = innerEquipId;
    }

    public SingleModeElectricHeatingDTO() {
    }

    public SingleModeElectricHeatingDTO(String username, String capacity, String area, String userType, String equipManufactor, String storageType, String owner, String controllable, String innerEquipId) {
        this.username = username;
        this.capacity = capacity;
        this.area = area;
        this.userType = userType;
        this.equipManufactor = equipManufactor;
        this.storageType = storageType;
        this.owner = owner;
        this.controllable = controllable;
        this.innerEquipId = innerEquipId;
    }

    public static void main(String[] args) {
        SingleModeElectricHeatingDTO singleModeElectricHeating1 = new SingleModeElectricHeatingDTO();
        singleModeElectricHeating1.setArea("100");
        singleModeElectricHeating1.setCapacity("1000");
        singleModeElectricHeating1.setControllable("1");
        singleModeElectricHeating1.setEquipManufactor("1");
        singleModeElectricHeating1.setInnerEquipId("200");
        singleModeElectricHeating1.setOwner("新奥数能");
        singleModeElectricHeating1.setStorageType("电加热");
        singleModeElectricHeating1.setUsername("新奥数能");
        singleModeElectricHeating1.setUserType("用能");

        SingleModeElectricHeatingDTO singleModeElectricHeating2 = new SingleModeElectricHeatingDTO();
        singleModeElectricHeating2.setArea("100");
        singleModeElectricHeating2.setCapacity("1000");
        singleModeElectricHeating2.setControllable("1");
        singleModeElectricHeating2.setEquipManufactor("1");
        singleModeElectricHeating2.setInnerEquipId("200");
        singleModeElectricHeating2.setOwner("新奥数能");
        singleModeElectricHeating2.setStorageType("电加热");
        singleModeElectricHeating2.setUsername("新奥数能");
        singleModeElectricHeating2.setUserType("用能");

        List<SingleModeElectricHeatingDTO> list = Lists.newArrayList(singleModeElectricHeating1, singleModeElectricHeating2);

        System.out.println(JSONObject.toJSONString(list));

    }
}
