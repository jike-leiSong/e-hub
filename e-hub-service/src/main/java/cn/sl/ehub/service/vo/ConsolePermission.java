package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@ApiModel("平台权限点")
@Table(name = "console_permission")
public class ConsolePermission {

    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name = "id")
    @ApiModelProperty("主键ID")
    private Long id;

    @Column(name = "permission_code")
    @ApiModelProperty("权限编码")
    private String permissionCode;

    @Column(name = "permission_name")
    @ApiModelProperty("权限名称")
    private String permissionName;

    @Column(name = "permission_type")
    @ApiModelProperty("权限类型")
    private String permissionType;

    @Column(name = "module_code")
    @ApiModelProperty("模块编码")
    private String moduleCode;

    @Column(name = "parent_code")
    @ApiModelProperty("父级权限")
    private String parentCode;

    @Column(name = "path")
    @ApiModelProperty("路由或路径")
    private String path;

    @Column(name = "sort_no")
    @ApiModelProperty("排序")
    private Integer sortNo;

    @Column(name = "status")
    @ApiModelProperty("状态")
    private Integer status;

    @Column(name = "create_time")
    @ApiModelProperty("创建时间")
    private String createTime;

    @Column(name = "update_time")
    @ApiModelProperty("更新时间")
    private String updateTime;
}
