package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@ApiModel("IoT分钟测点数据")
@Table(name = "iot_telemetry_minute")
public class IotTelemetryMinute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ApiModelProperty("聚合商ID")
    @Column(name = "aggregator_id")
    private String aggregatorId;

    @ApiModelProperty("企业ID")
    @Column(name = "ent_id")
    private String entId;

    @ApiModelProperty("项目编码")
    @Column(name = "project_id")
    private String projectId;

    @ApiModelProperty("设备ID")
    @Column(name = "device_id")
    private Long deviceId;

    @ApiModelProperty("标准设备编码")
    @Column(name = "device_code")
    private String deviceCode;

    @ApiModelProperty("标准测点编码")
    @Column(name = "point_code")
    private String pointCode;

    @ApiModelProperty("原始数据时间")
    @Column(name = "data_time")
    private Date dataTime;

    @ApiModelProperty("分钟时间")
    @Column(name = "minute_time")
    private Date minuteTime;

    @ApiModelProperty("标准值")
    @Column(name = "point_value")
    private Double pointValue;

    @ApiModelProperty("单位")
    @Column(name = "unit")
    private String unit;

    @ApiModelProperty("数据质量")
    @Column(name = "quality")
    private String quality;

    @ApiModelProperty("三方来源编码")
    @Column(name = "source_code")
    private String sourceCode;

    @ApiModelProperty("接收时间")
    @Column(name = "receive_time")
    private Date receiveTime;

    @ApiModelProperty("原始值")
    @Column(name = "raw_value")
    private String rawValue;
}
