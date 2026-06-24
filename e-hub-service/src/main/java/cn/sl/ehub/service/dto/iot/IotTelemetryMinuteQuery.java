package cn.sl.ehub.service.dto.iot;

import lombok.Data;

@Data
public class IotTelemetryMinuteQuery {

    private String aggregatorId;

    private String entId;

    private Long projectId;

    private Long deviceId;

    private String deviceCode;

    private String pointCode;

    private String startTime;

    private String endTime;
}
