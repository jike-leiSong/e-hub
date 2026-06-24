package cn.sl.ehub.service.dto.iot;

import lombok.Data;

@Data
public class IotOriginDataItem {

    private String dataTime;

    private String projectId;

    private String projectName;

    private String deviceId;

    private String deviceName;

    private String metric;

    private String metricName;

    private String value;
}
