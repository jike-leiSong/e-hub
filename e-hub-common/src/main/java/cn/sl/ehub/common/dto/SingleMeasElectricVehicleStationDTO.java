package cn.sl.ehub.common.dto;

/**
 * @Description: 电动汽车充电站
 * @Author sl
 * @Date 2026-05-28
 */

public class SingleMeasElectricVehicleStationDTO {

    private String stationName;

    private String totalPower;

    private String regularTotalPower;

    private String todayZeroElecQuantity;

    private String innerStationId;

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getTotalPower() {
        return totalPower;
    }

    public void setTotalPower(String totalPower) {
        this.totalPower = totalPower;
    }

    public String getRegularTotalPower() {
        return regularTotalPower;
    }

    public void setRegularTotalPower(String regularTotalPower) {
        this.regularTotalPower = regularTotalPower;
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

    public SingleMeasElectricVehicleStationDTO() {
    }

    public SingleMeasElectricVehicleStationDTO(String stationName, String totalPower, String regularTotalPower, String todayZeroElecQuantity, String innerStationId) {
        this.stationName = stationName;
        this.totalPower = totalPower;
        this.regularTotalPower = regularTotalPower;
        this.todayZeroElecQuantity = todayZeroElecQuantity;
        this.innerStationId = innerStationId;
    }
}
