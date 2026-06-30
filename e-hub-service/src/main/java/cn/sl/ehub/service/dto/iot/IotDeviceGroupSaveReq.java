package cn.sl.ehub.service.dto.iot;

import lombok.Data;

import java.util.List;

@Data
public class IotDeviceGroupSaveReq {

    private String aggregatorId;

    private String entId;

    private String deviceGroupName;

    private String deviceGroupType;

    private String deviceGroupTypeName;

    private String energyType;

    private Long gatewayId;

    private String remark;

    private List<IotDeviceGroupParamSaveReq> paramList;

    private List<IotDeviceGroupPointSaveReq> pointList;
}
