package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@ApiModel("角色权限关系")
@Table(name = "console_role_permission")
public class ConsoleRolePermission {

    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name = "id")
    @ApiModelProperty("主键ID")
    private Long id;

    @Column(name = "role_id")
    @ApiModelProperty("角色ID")
    private String roleId;

    @Column(name = "permission_code")
    @ApiModelProperty("权限编码")
    private String permissionCode;

    @Column(name = "create_time")
    @ApiModelProperty("创建时间")
    private String createTime;

    @Column(name = "update_time")
    @ApiModelProperty("更新时间")
    private String updateTime;
}
