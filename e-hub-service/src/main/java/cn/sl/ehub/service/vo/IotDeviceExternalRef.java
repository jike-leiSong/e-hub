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
@ApiModel("IoT三方设备映射")
@Table(name = "iot_device_external_ref")
public class IotDeviceExternalRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ApiModelProperty("三方来源编码")
    @Column(name = "source_code")
    private String sourceCode;

    @ApiModelProperty("企业ID")
    @Column(name = "ent_id")
    private String entId;

    @ApiModelProperty("项目编码")
    @Column(name = "project_id")
    private String projectId;

    @ApiModelProperty("我方设备ID")
    @Column(name = "device_id")
    private Long deviceId;

    @ApiModelProperty("三方设备唯一标识")
    @Column(name = "external_device_id")
    private String externalDeviceId;

    @ApiModelProperty("三方设备编码")
    @Column(name = "external_device_code")
    private String externalDeviceCode;

    @ApiModelProperty("三方设备名称")
    @Column(name = "external_device_name")
    private String externalDeviceName;

    @ApiModelProperty("网关编码")
    @Column(name = "gateway_code")
    private String gatewayCode;

    @ApiModelProperty("状态：1启用，0停用")
    @Column(name = "status")
    private Integer status;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;
}
