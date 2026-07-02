package cn.sl.ehub.service.dto.iot;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("物联时序数据响应")
public class IotTelemetryDataResp {

    @ApiModelProperty("设备ID")
    private Long deviceId;

    @ApiModelProperty("设备编码")
    private String deviceCode;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("测点编码")
    private String pointCode;

    @ApiModelProperty("测点名称")
    private String pointName;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("数据时间")
    private Date dataTime;

    @ApiModelProperty("数据值")
    private Double value;

    @ApiModelProperty("数据质量")
    private String quality;

    @ApiModelProperty("聚合值（当 aggType!=minute 时返回聚合值）")
    private Double aggValue;
}
