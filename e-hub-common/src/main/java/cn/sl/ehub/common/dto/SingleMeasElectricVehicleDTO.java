package cn.sl.ehub.common.dto;

/**
 * @Description: 电动汽车转换类
 * @Author sl
 * @Date 2026-05-28
 */

public class SingleMeasElectricVehicleDTO {

    private SingleMeasElectricVehicleEquipDTO electricVehicleEquip;

    private SingleMeasElectricVehicleStationDTO electricVehicleStation;

    public SingleMeasElectricVehicleEquipDTO getElectricVehicleEquip() {
        return electricVehicleEquip;
    }

    public void setElectricVehicleEquip(SingleMeasElectricVehicleEquipDTO electricVehicleEquip) {
        this.electricVehicleEquip = electricVehicleEquip;
    }

    public SingleMeasElectricVehicleStationDTO getElectricVehicleStation() {
        return electricVehicleStation;
    }

    public void setElectricVehicleStation(SingleMeasElectricVehicleStationDTO electricVehicleStation) {
        this.electricVehicleStation = electricVehicleStation;
    }

    public SingleMeasElectricVehicleDTO() {
    }

    public SingleMeasElectricVehicleDTO(SingleMeasElectricVehicleEquipDTO electricVehicleEquip, SingleMeasElectricVehicleStationDTO electricVehicleStation) {
        this.electricVehicleEquip = electricVehicleEquip;
        this.electricVehicleStation = electricVehicleStation;
    }
}
