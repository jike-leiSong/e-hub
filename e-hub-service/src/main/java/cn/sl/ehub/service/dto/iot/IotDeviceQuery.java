package cn.sl.ehub.service.dto.iot;

import lombok.Data;

@Data
public class IotDeviceQuery {

    private String aggregatorId;

    private String entId;

    private String projectId;

    private String energyStation;

    private String deviceCode;

    private String deviceName;

    private String deviceTypeCode;

    private Integer assetStatus;

    private Integer onlineStatus;
}
