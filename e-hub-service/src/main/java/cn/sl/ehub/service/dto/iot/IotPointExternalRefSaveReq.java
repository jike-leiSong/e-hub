package cn.sl.ehub.service.dto.iot;

import lombok.Data;

@Data
public class IotPointExternalRefSaveReq {

    private String sourceCode;

    private Long deviceId;

    private Long pointId;

    private String externalMetric;

    private String externalMetricName;

    private Double ratio;

    private Double offsetValue;

    private Integer status;
}
