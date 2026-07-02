package cn.sl.ehub.service.dto.iot;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("物联原始明细响应")
public class IotTelemetryRawResp {

    @ApiModelProperty("三方来源编码")
    private String sourceCode;

    @ApiModelProperty("匹配的本地设备ID")
    private Long deviceId;

    @ApiModelProperty("三方设备标识")
    private String externalDeviceId;

    @ApiModelProperty("三方metric")
    private String externalMetric;

    @ApiModelProperty("数据时间")
    private Date dataTime;

    @ApiModelProperty("原始值")
    private String rawValue;

    @ApiModelProperty("接收时间")
    private Date receiveTime;

    @ApiModelProperty("原始报文")
    private String rawPayload;

    @ApiModelProperty("匹配状态：matched/device_not_found/point_not_found")
    private String matchStatus;

    @ApiModelProperty("创建时间")
    private Date createTime;
}
