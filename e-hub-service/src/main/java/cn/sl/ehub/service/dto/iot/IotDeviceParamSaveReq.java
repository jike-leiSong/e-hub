package cn.sl.ehub.service.dto.iot;

import lombok.Data;

@Data
public class IotDeviceParamSaveReq {

    private String paramCode;

    private String paramName;

    private String paramValue;

    private String unit;

    private Integer sort;

    private String remark;
}
