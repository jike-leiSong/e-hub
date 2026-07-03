package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@ApiModel("平台角色")
@Table(name = "console_role")
public class ConsoleRole {

    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name = "id")
    @ApiModelProperty("主键ID")
    private Long id;

    @Column(name = "role_id")
    @ApiModelProperty("角色ID")
    private String roleId;

    @Column(name = "role_name")
    @ApiModelProperty("角色名称")
    private String roleName;

    @Column(name = "role_code")
    @ApiModelProperty("角色编码")
    private String roleCode;

    @Column(name = "platform_type")
    @ApiModelProperty("平台类型")
    private String platformType;

    @Column(name = "status")
    @ApiModelProperty("状态")
    private Integer status;

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
