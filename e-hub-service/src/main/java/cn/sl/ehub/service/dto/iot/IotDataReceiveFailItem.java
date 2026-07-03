package cn.sl.ehub.service.dto.iot;

import lombok.Data;

@Data
public class IotDataReceiveFailItem {

    private String deviceId;

    private String metric;

    private String reason;
}
