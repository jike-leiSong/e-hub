package cn.sl.ehub.console.auth.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("当前登录用户")
public class AuthUserInfoResp {

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("用户ID")
    private String userId;

    @ApiModelProperty("展示名称")
    private String displayName;

    @ApiModelProperty("用户类型 ADMIN/CUSTOMER")
    private String userType;

    @ApiModelProperty("聚合商ID")
    private String aggregatorId;

    @ApiModelProperty("企业用户ID")
    private String entId;

    @ApiModelProperty("租户ID")
    private String tenantId;

    @ApiModelProperty("平台类型 owner/customer")
    private String platformType;

    @ApiModelProperty("角色编码")
    private String role;

    @ApiModelProperty("开通产品")
    private List<String> products;

    @ApiModelProperty("权限点")
    private List<String> permissions;

    @ApiModelProperty("允许访问页面")
    private List<String> allowedPages;

    @ApiModelProperty("默认页面")
    private String defaultPage;

    @ApiModelProperty("菜单")
    private List<AuthMenuGroupResp> menuGroups;
}
