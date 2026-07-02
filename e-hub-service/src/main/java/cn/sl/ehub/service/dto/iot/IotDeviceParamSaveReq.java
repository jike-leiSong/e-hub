package cn.sl.ehub.service.dto.iot;

import lombok.Data;

@Data
public class IotDeviceParamSaveReq {

    private String attrCode;

    private String attrName;

    private String aliasName;

    private String attrValue;

    private String attrUnit;

    private String attrType;

    private Integer sort;

    private String remark;

    public String getParamCode() {
        return attrCode;
    }

    public void setParamCode(String paramCode) {
        this.attrCode = paramCode;
    }

    public String getParamName() {
        return attrName;
    }

    public void setParamName(String paramName) {
        this.attrName = paramName;
    }

    public String getParamValue() {
        return attrValue;
    }

    public void setParamValue(String paramValue) {
        this.attrValue = paramValue;
    }

    public String getUnit() {
        return attrUnit;
    }

    public void setUnit(String unit) {
        this.attrUnit = unit;
    }
}