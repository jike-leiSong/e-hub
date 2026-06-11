package cn.sl.ehub.common.dto;

/**
 * @Description: 充电桩转换类
 * @Author sl
 * @Date 2026-05-28
 */

public class SingleModeElectricVehicleEquipDTO {

    private String equipName;

    private String stationName;

    private String equipCapacity;

    private String equipType;

    private String equipManufactor;

    private String investor;

    private String innerEquipId;

    public String getEquipName() {
        return equipName;
    }

    public void setEquipName(String equipName) {
        this.equipName = equipName;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getEquipCapacity() {
        return equipCapacity;
    }

    public void setEquipCapacity(String equipCapacity) {
        this.equipCapacity = equipCapacity;
    }

    public String getEquipType() {
        return equipType;
    }

    public void setEquipType(String equipType) {
        this.equipType = equipType;
    }

    public String getEquipManufactor() {
        return equipManufactor;
    }

    public void setEquipManufactor(String equipManufactor) {
        this.equipManufactor = equipManufactor;
    }

    public String getInvestor() {
        return investor;
    }

    public void setInvestor(String investor) {
        this.investor = investor;
    }

    public String getInnerEquipId() {
        return innerEquipId;
    }

    public void setInnerEquipId(String innerEquipId) {
        this.innerEquipId = innerEquipId;
    }

    public SingleModeElectricVehicleEquipDTO() {
    }

    public SingleModeElectricVehicleEquipDTO(String equipName, String stationName, String equipCapacity, String equipType, String equipManufactor, String investor, String innerEquipId) {
        this.equipName = equipName;
        this.stationName = stationName;
        this.equipCapacity = equipCapacity;
        this.equipType = equipType;
        this.equipManufactor = equipManufactor;
        this.investor = investor;
        this.innerEquipId = innerEquipId;
    }
}
