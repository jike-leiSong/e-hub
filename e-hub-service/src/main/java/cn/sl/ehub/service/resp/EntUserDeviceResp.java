package cn.sl.ehub.service.resp;

import lombok.Data;

@Data
public class EntUserDeviceResp {

    private String aggregatorId;
    private String entId;
    private String resourceTypeId;
    private String resourceTypeName;
    private String deviceBaseId;
    private String deviceId;
    private String accountNo;
    private String deviceName;
    private Double power;
    private Double maxPower;
    private Double responsePower;
    private String stationId;
}
