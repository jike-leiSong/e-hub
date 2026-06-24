package cn.sl.ehub.service.dto.iot;

import lombok.Data;

@Data
public class IotCimDataItem {

    private String dataTime;

    private String deviceId;

    private String deviceType;

    private String metric;

    private String value;
}
