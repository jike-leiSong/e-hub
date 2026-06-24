package cn.sl.ehub.service.dto.iot;

import lombok.Data;

@Data
public class IotDevicePointSaveReq {

    private Long deviceId;

    private String pointCode;

    private String pointName;

    private String valueType;

    private String unit;

    private Integer dataFrequency;

    private Integer requiredFlag;

    private String readWriteRole;

    private Integer status;

    private Integer sort;

    private String remark;
}
