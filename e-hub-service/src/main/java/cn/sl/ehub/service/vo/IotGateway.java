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
