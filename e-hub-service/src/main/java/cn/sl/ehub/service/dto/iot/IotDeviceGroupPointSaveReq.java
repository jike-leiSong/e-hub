package cn.sl.ehub.service.dto.iot;

import lombok.Data;

@Data
public class IotDeviceGroupPointSaveReq {

    private String propertyCode;

    private String propertyName;

    private String dataType;

    private String dataTypeName;

    private String valueType;

    private String unit;

    private String readWriteRole;

    private String valueLowerLimit;

    private String valueHighLimit;

    private Integer deadZoneType;

    private Integer type;

    private Integer sort;

    private Integer status;

    private String remark;
}
