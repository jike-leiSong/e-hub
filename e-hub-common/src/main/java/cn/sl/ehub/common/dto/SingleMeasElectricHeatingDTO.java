package cn.sl.ehub.common.dto;

/**
 * @Description: 工业负荷转换类
 * @Author sl
 * @Date 2026-05-28
 */

public class SingleMeasElectricHeatingDTO {

    private String username;

    private String activePower;

    private String reactivePower;

    private String userElecCurrent;

    private String todayZeroElecQuanlity;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getActivePower() {
        return activePower;
    }

    public void setActivePower(String activePower) {
        this.activePower = activePower;
    }

    public String getReactivePower() {
        return reactivePower;
    }

    public void setReactivePower(String reactivePower) {
        this.reactivePower = reactivePower;
    }

    public String getUserElecCurrent() {
        return userElecCurrent;
    }

    public void setUserElecCurrent(String userElecCurrent) {
        this.userElecCurrent = userElecCurrent;
    }

    public String getTodayZeroElecQuanlity() {
        return todayZeroElecQuanlity;
    }

    public void setTodayZeroElecQuanlity(String todayZeroElecQuanlity) {
        this.todayZeroElecQuanlity = todayZeroElecQuanlity;
    }

    public SingleMeasElectricHeatingDTO() {
    }

    public SingleMeasElectricHeatingDTO(String username, String activePower, String reactivePower, String userElecCurrent, String todayZeroElecQuanlity) {
        this.username = username;
        this.activePower = activePower;
        this.reactivePower = reactivePower;
        this.userElecCurrent = userElecCurrent;
        this.todayZeroElecQuanlity = todayZeroElecQuanlity;
    }
}
