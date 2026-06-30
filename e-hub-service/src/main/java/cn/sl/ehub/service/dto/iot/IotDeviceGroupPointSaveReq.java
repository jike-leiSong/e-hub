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

    private Integer sort;

    private Integer status;

    private String remark;
}
