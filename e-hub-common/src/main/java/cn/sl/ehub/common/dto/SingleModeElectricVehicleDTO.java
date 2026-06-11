package cn.sl.ehub.common.dto;

/**
 * @Description: 电动汽车转换类
 * @Author sl
 * @Date 2026-05-28
 */

public class SingleModeElectricVehicleDTO {

    private SingleModeElectricVehicleEquipDTO electricVehicleEquip;

    private SingleModeElectricVehicleStationDTO electricVehicleStation;

    public SingleModeElectricVehicleEquipDTO getElectricVehicleEquip() {
        return electricVehicleEquip;
    }

    public void setElectricVehicleEquip(SingleModeElectricVehicleEquipDTO electricVehicleEquip) {
        this.electricVehicleEquip = electricVehicleEquip;
    }

    public SingleModeElectricVehicleStationDTO getElectricVehicleStation() {
        return electricVehicleStation;
    }

    public void setElectricVehicleStation(SingleModeElectricVehicleStationDTO electricVehicleStation) {
        this.electricVehicleStation = electricVehicleStation;
    }

    public SingleModeElectricVehicleDTO() {
    }

    public SingleModeElectricVehicleDTO(SingleModeElectricVehicleEquipDTO electricVehicleEquip, SingleModeElectricVehicleStationDTO electricVehicleStation) {
        this.electricVehicleEquip = electricVehicleEquip;
        this.electricVehicleStation = electricVehicleStation;
    }
}
