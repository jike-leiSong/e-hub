package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@ApiModel("平台租户")
@Table(name = "console_tenant")
public class ConsoleTenant {

    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name = "id")
    @ApiModelProperty("主键ID")
    private Long id;

    @Column(name = "tenant_id")
    @ApiModelProperty("租户ID")
    private String tenantId;

    @Column(name = "tenant_name")
    @ApiModelProperty("租户名称")
    private String tenantName;

    @Column(name = "tenant_type")
    @ApiModelProperty("租户类型")
    private String tenantType;

    @Column(name = "status")
    @ApiModelProperty("状态")
    private Integer status;

    @Column(name = "aggregator_id")
    @ApiModelProperty("聚合商ID")
    private String aggregatorId;

    @Column(name = "ent_id")
    @ApiModelProperty("企业ID")
    private String entId;

    @Column(name = "owner_user_id")
    @ApiModelProperty("管理员账号ID")
    private String ownerUserId;

    @Column(name = "contact_name")
    @ApiModelProperty("联系人")
    private String contactName;

    @Column(name = "contact_phone")
    @ApiModelProperty("联系电话")
    private String contactPhone;

    @Column(name = "remark")
    @ApiModelProperty("备注")
    private String remark;

    @Column(name = "create_time")
    @ApiModelProperty("创建时间")
    private String createTime;

    @Column(name = "update_time")
    @ApiModelProperty("更新时间")
    private String updateTime;
}
