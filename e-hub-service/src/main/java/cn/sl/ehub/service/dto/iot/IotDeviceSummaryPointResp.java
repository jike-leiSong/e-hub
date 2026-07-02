package cn.sl.ehub.service.dto.iot;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("设备概览测点响应")
public class IotDeviceSummaryPointResp {

    @ApiModelProperty("设备ID")
    private Long deviceId;

    @ApiModelProperty("设备编码")
    private String deviceCode;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("所属企业")
    private String entId;

    @ApiModelProperty("测点编码")
    private String pointCode;

    @ApiModelProperty("测点名称")
    private String pointName;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("当前值")
    private Double currentValue;

    @ApiModelProperty("当前值时间")
    private Date currentTime;

    @ApiModelProperty("前一分钟值")
    private Double prevValue;

    @ApiModelProperty("前一分钟时间")
    private Date prevTime;

    @ApiModelProperty("变化量（当前值 - 前值）")
    private Double delta;

    @ApiModelProperty("趋势：up/down/stable")
    private String trend;

    @ApiModelProperty("数据质量")
    private String quality;
}
