package cn.sl.ehub.common.dto;

/**
 * @Description: 充电桩转换类
 * @Author sl
 * @Date 2026-05-28
 */

public class SingleMeasElectricVehicleEquipDTO {

    private String equipName;

    private String stationName;

    private String equipPower;

    private String equipElecCurrent;

    private String equipzeroElecQuanlity;

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

    public String getEquipPower() {
        return equipPower;
    }

    public void setEquipPower(String equipPower) {
        this.equipPower = equipPower;
    }

    public String getEquipElecCurrent() {
        return equipElecCurrent;
    }

    public void setEquipElecCurrent(String equipElecCurrent) {
        this.equipElecCurrent = equipElecCurrent;
    }

    public String getEquipzeroElecQuanlity() {
        return equipzeroElecQuanlity;
    }

    public void setEquipzeroElecQuanlity(String equipzeroElecQuanlity) {
        this.equipzeroElecQuanlity = equipzeroElecQuanlity;
    }

    public String getInnerEquipId() {
        return innerEquipId;
    }

    public void setInnerEquipId(String innerEquipId) {
        this.innerEquipId = innerEquipId;
    }

    public SingleMeasElectricVehicleEquipDTO() {
    }

    public SingleMeasElectricVehicleEquipDTO(String equipName, String stationName, String equipPower, String equipElecCurrent, String equipzeroElecQuanlity, String innerEquipId) {
        this.equipName = equipName;
        this.stationName = stationName;
        this.equipPower = equipPower;
        this.equipElecCurrent = equipElecCurrent;
        this.equipzeroElecQuanlity = equipzeroElecQuanlity;
        this.innerEquipId = innerEquipId;
    }
}
