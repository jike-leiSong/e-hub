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
@ApiModel("IoT网关")
@Table(name = "iot_gateway")
public class IotGateway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ApiModelProperty("租户ID")
    @Column(name = "tenant_id")
    private Long tenantId;

    @ApiModelProperty("聚合商ID")
    @Column(name = "aggregator_id")
    private String aggregatorId;

    @ApiModelProperty("企业ID")
    @Column(name = "ent_id")
    private String entId;

    @ApiModelProperty("网关编码")
    @Column(name = "gateway_code")
    private String gatewayCode;

    @ApiModelProperty("网关名称")
    @Column(name = "gateway_name")
    private String gatewayName;

    @ApiModelProperty("网关类型编码")
    @Column(name = "gateway_type_code")
    private String gatewayTypeCode;

    @ApiModelProperty("网关类型名称")
    @Column(name = "gateway_type_name")
    private String gatewayTypeName;

    @ApiModelProperty("通讯方式编码")
    @Column(name = "communication_method_code")
    private String communicationMethodCode;

    @ApiModelProperty("通讯方式名称")
    @Column(name = "communication_method_name")
    private String communicationMethodName;

    @ApiModelProperty("厂家")
    @Column(name = "manufacturer")
    private String manufacturer;

    @ApiModelProperty("型号")
    @Column(name = "model")
    private String model;

    @ApiModelProperty("网关序列号")
    @Column(name = "gateway_serial_number")
    private String gatewaySerialNumber;

    @ApiModelProperty("网关SN号")
    @Column(name = "gateway_sn_number")
    private String gatewaySnNumber;

    @ApiModelProperty("物联状态")
    @Column(name = "iot_status")
    private String iotStatus;

    @ApiModelProperty("绑定设备数量")
    @Column(name = "bound_device_count")
    private Integer boundDeviceCount;

    @ApiModelProperty("状态")
    @Column(name = "status")
    private Integer status;

    @ApiModelProperty("是否默认")
    @Column(name = "default_flag")
    private Integer defaultFlag;

    @ApiModelProperty("删除标识")
    @Column(name = "deleted")
    private Integer deleted;

    @ApiModelProperty("备注")
    @Column(name = "remark")
    private String remark;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;
}
