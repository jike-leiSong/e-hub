package cn.sl.ehub.service.dto.iot;

import lombok.Data;

@Data
public class IotDataReceiveFailItem {

    private String projectId;

    private String projectName;

    private String deviceId;

    private String deviceName;

    private String metric;

    private String metricName;

    private String reason;
}
