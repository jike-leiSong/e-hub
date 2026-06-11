package cn.sl.ehub.common.dto;

/**
 * @Description: 电动汽车充电站
 * @Author sl
 * @Date 2026-05-28
 */

public class SingleModeElectricVehicleStationDTO {

    private String stationName;

    private String area;

    private String totalCapacity;

    private String chargingEquipNo;

    private String innerStationId;

    private String stationType;

    private String controllable;

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

    public String getChargingEquipNo() {
        return chargingEquipNo;
    }

    public void setChargingEquipNo(String chargingEquipNo) {
        this.chargingEquipNo = chargingEquipNo;
    }

    public String getInnerStationId() {
        return innerStationId;
    }

    public void setInnerStationId(String innerStationId) {
        this.innerStationId = innerStationId;
    }

    public String getStationType() {
        return stationType;
    }

    public void setStationType(String stationType) {
        this.stationType = stationType;
    }

    public String getControllable() {
        return controllable;
    }

    public void setControllable(String controllable) {
        this.controllable = controllable;
    }

    public SingleModeElectricVehicleStationDTO() {
    }

    public SingleModeElectricVehicleStationDTO(String stationName, String area, String totalCapacity, String chargingEquipNo, String innerStationId, String stationType, String controllable) {
        this.stationName = stationName;
        this.area = area;
        this.totalCapacity = totalCapacity;
        this.chargingEquipNo = chargingEquipNo;
        this.innerStationId = innerStationId;
        this.stationType = stationType;
        this.controllable = controllable;
    }
}
