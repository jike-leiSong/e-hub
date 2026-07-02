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
@ApiModel("IoT遥测原始明细")
@Table(name = "iot_telemetry_raw")
public class IotTelemetryRaw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ApiModelProperty("接口类型：originData/cimData")
    @Column(name = "interface_type")
    private String interfaceType;

    @ApiModelProperty("三方来源编码")
    @Column(name = "source_code")
    private String sourceCode;

    @ApiModelProperty("企业ID")
    @Column(name = "ent_id")
    private String entId;

    @ApiModelProperty("项目编码")
    @Column(name = "project_id")
    private String projectId;

    @ApiModelProperty("匹配的本地设备ID")
    @Column(name = "device_id")
    private Long deviceId;

    @ApiModelProperty("标准设备编码")
    @Column(name = "device_code")
    private String deviceCode;

    @ApiModelProperty("标准测点编码")
    @Column(name = "point_code")
    private String pointCode;

    @ApiModelProperty("三方设备标识")
    @Column(name = "external_device_id")
    private String externalDeviceId;

    @ApiModelProperty("三方metric")
    @Column(name = "external_metric")
    private String externalMetric;

    @ApiModelProperty("数据时间")
    @Column(name = "data_time")
    private Date dataTime;

    @ApiModelProperty("原始值")
    @Column(name = "raw_value")
    private String rawValue;

    @ApiModelProperty("接收时间")
    @Column(name = "receive_time")
    private Date receiveTime;

    @ApiModelProperty("原始报文")
    @Column(name = "raw_payload")
    private String rawPayload;

    @ApiModelProperty("匹配状态：matched匹配成功，device_not_found设备未找到，point_not_found测点未找到")
    @Column(name = "match_status")
    private String matchStatus;

    @ApiModelProperty("匹配失败原因")
    @Column(name = "match_reason")
    private String matchReason;

    @ApiModelProperty("创建时间")
    @Column(name = "create_time")
    private Date createTime;
}
