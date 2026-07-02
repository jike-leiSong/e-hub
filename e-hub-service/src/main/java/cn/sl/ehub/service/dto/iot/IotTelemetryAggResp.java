package cn.sl.ehub.service.dto.iot;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("物联时序聚合数据响应")
public class IotTelemetryAggResp {

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

    @ApiModelProperty("聚合区间开始时间")
    private Date startTime;

    @ApiModelProperty("聚合区间结束时间")
    private Date endTime;

    @ApiModelProperty("平均值")
    private Double avgValue;

    @ApiModelProperty("最大值")
    private Double maxValue;

    @ApiModelProperty("最小值")
    private Double minValue;

    @ApiModelProperty("求和值")
    private Double sumValue;

    @ApiModelProperty("有效数据条数")
    private Integer count;

    @ApiModelProperty("数据质量")
    private String quality;
}
