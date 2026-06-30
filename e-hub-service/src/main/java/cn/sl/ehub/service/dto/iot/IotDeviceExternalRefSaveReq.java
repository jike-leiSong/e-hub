package cn.sl.ehub.service.dto.iot;

import lombok.Data;

@Data
public class IotDeviceExternalRefSaveReq {

    private String sourceCode;

    private String entId;

    private String projectId;

    private Long deviceId;

    private String externalDeviceId;

    private String externalDeviceCode;

    private String externalDeviceName;

    private String gatewayCode;

    private Integer status;
}
