package cn.sl.ehub.service.dto.iot;

import lombok.Data;

@Data
public class IotDeviceSaveReq {

    private String aggregatorId;

    private String entId;

    private String projectId;

    private Long deviceGroupId;

    private String deviceCode;

    private String deviceName;

    private String deviceTypeCode;

    private String deviceTypeName;

    private String communicationMethod;

    private String manufacturer;

    private String model;

    private String thirdPartyApi;

    private String thirdPartyCode;

    private Long gatewayId;

    private Integer assetStatus;

    private Integer onlineStatus;

    private String remark;

    private Boolean createDefaultPowerPoint;

    private java.util.List<IotDeviceParamSaveReq> paramList;

    private java.util.List<IotDevicePointSaveReq> pointList;
}
