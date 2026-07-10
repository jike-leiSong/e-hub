package cn.sl.ehub.service.dto.iot;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class IotMockPowerDataResp {

    private Integer deviceCount = 0;

    private Integer pointCount = 0;

    private Integer total = 0;

    private Integer success = 0;

    private Integer fail = 0;

    private Integer skippedDeviceCount = 0;

    private Integer skippedPointCount = 0;

    private String startTime;

    private String endTime;

    private String message;

    private List<String> warnings = new ArrayList<>();
}
