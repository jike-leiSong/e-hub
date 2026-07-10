package cn.sl.ehub.service.dto.iot;

import lombok.Data;

import java.util.List;

@Data
public class IotMockPowerDataReq {

    private String aggregatorId;

    private String entId;

    private String energyStationCode;

    private List<Long> deviceIds;

    private String startTime;

    private String endTime;

    private Integer intervalSeconds;
}
