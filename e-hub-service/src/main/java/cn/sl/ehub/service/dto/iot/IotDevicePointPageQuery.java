package cn.sl.ehub.service.dto.iot;

import lombok.Data;

@Data
public class IotDevicePointPageQuery {

    private String propertyCode;

    private String propertyName;

    private String pointQuery;

    private String dataType;
}
