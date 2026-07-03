package cn.sl.ehub.service.dto.iot;

import lombok.Data;

@Data
public class IotOriginDataItem {

    private String dataTime;

    private String deviceId;

    private String metric;

    private String value;
}
