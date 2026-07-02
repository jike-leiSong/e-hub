package cn.sl.ehub.service.dto.iot;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("设备概览响应（单设备维度）")
public class IotDeviceSummaryResp {

    @ApiModelProperty("设备ID")
    private Long deviceId;

    @ApiModelProperty("设备编码")
    private String deviceCode;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("所属企业")
    private String entId;

    @ApiModelProperty("测点列表")
    private java.util.List<IotDeviceSummaryPointResp> points;
}
