package cn.sl.ehub.service.dto.iot;

import lombok.Data;

@Data
public class IotDevicePointSaveReq {

    private Long deviceId;

    private Long tenantId;

    private String propertyCode;

    private String propertyName;

    private String thirdPartyCode;

    private String dataType;

    private String dataTypeName;

    private String valueType;

    private String unit;

    private Integer dataFrequency;

    private Integer requiredFlag;

    private String readWriteRole;

    private String upWay;

    private String upWayName;

    private String upPeriod;

    private String upPeriodName;

    private String valueLowerLimit;

    private String valueHighLimit;

    private Integer deadZoneType;

    private Integer type;

    private Integer status;

    private Integer sort;

    private String remark;

    public String getPointCode() {
        return propertyCode;
    }

    public void setPointCode(String pointCode) {
        this.propertyCode = pointCode;
    }

    public String getPointName() {
        return propertyName;
    }

    public void setPointName(String pointName) {
        this.propertyName = pointName;
    }
}
