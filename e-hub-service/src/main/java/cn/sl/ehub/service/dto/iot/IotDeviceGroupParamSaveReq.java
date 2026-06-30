package cn.sl.ehub.service.dto.iot;

import lombok.Data;

@Data
public class IotDeviceGroupParamSaveReq {

    private String attrCode;

    private String attrName;

    private String aliasName;

    private String attrValue;

    private String attrUnit;

    private String attrType;

    private Integer sort;

    private String remark;
}
