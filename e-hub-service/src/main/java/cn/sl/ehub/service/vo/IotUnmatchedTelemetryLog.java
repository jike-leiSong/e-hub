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
@ApiModel("IoT未匹配测点数据")
@Table(name = "iot_unmatched_telemetry_log")
public class IotUnmatchedTelemetryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ApiModelProperty("三方来源编码")
    @Column(name = "source_code")
    private String sourceCode;

    @ApiModelProperty("接口类型")
    @Column(name = "interface_type")
    private String interfaceType;

    @ApiModelProperty("三方设备ID")
    @Column(name = "external_device_id")
    private String externalDeviceId;

    @ApiModelProperty("三方设备名称")
    @Column(name = "external_device_name")
    private String externalDeviceName;

    @ApiModelProperty("三方测点编码")
    @Column(name = "external_metric")
    private String externalMetric;

    @ApiModelProperty("三方测点名称")
    @Column(name = "external_metric_name")
    private String externalMetricName;

    @ApiModelProperty("数据时间")
    @Column(name = "data_time")
    private Date dataTime;

    @ApiModelProperty("值")
    @Column(name = "value")
    private String value;

    @ApiModelProperty("原因")
    @Column(name = "reason")
    private String reason;

    @ApiModelProperty("原始报文")
    @Column(name = "raw_payload")
    private String rawPayload;

    @ApiModelProperty("是否处理")
    @Column(name = "handled")
    private Integer handled;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;
}
