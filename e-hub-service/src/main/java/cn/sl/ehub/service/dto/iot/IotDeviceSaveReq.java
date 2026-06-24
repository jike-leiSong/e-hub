package cn.sl.ehub.service.dto.iot;

import lombok.Data;

@Data
public class IotDeviceSaveReq {

    private String aggregatorId;

    private String entId;

    private Long projectId;

    private String deviceCode;

    private String deviceName;

    private String deviceTypeCode;

    private String deviceTypeName;

    private String manufacturer;

    private String model;

    private Integer assetStatus;

    private Integer onlineStatus;

    private String remark;

    private Boolean createDefaultPowerPoint;
}
