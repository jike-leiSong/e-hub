package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@ApiModel("Console登录用户")
@Table(name = "console_user")
public class ConsoleUser {

    @Id
    @GeneratedValue(generator = "JDBC")
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Integer id;

    @ApiModelProperty("用户ID")
    @Column(name = "user_id")
    private String userId;

    @ApiModelProperty("登录账号")
    @Column(name = "username")
    private String username;

    @ApiModelProperty("展示名称")
    @Column(name = "display_name")
    private String displayName;

    @ApiModelProperty("密码盐")
    @Column(name = "password_salt")
    private String passwordSalt;

    @ApiModelProperty("密码SHA-256哈希")
    @Column(name = "password_hash")
    private String passwordHash;

    @ApiModelProperty("用户类型 ADMIN/CUSTOMER，兼容历史 PLATFORM/AGGREGATOR/ENT")
    @Column(name = "user_type")
    private String userType;

    @ApiModelProperty("聚合商ID")
    @Column(name = "aggregator_id")
    private String aggregatorId;

    @ApiModelProperty("企业用户ID")
    @Column(name = "ent_id")
    private String entId;

    @ApiModelProperty("租户ID")
    @Column(name = "tenant_id")
    private String tenantId;

    @ApiModelProperty("状态 1启用 0禁用")
    @Column(name = "status")
    private Integer status;

    @ApiModelProperty("最近登录时间")
    @Column(name = "last_login_time")
    private String lastLoginTime;

    @ApiModelProperty("创建时间")
    @Column(name = "create_time")
    private String createTime;

    @ApiModelProperty("更新时间")
    @Column(name = "update_time")
    private String updateTime;
}
