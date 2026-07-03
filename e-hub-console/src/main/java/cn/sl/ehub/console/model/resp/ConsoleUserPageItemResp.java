package cn.sl.ehub.console.model.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("平台用户分页项响应")
public class ConsoleUserPageItemResp {

    @ApiModelProperty("用户ID")
    private String userId;

    @ApiModelProperty("账号")
    private String username;

    @ApiModelProperty("姓名")
    private String displayName;

    @ApiModelProperty("租户ID")
    private String tenantId;

    @ApiModelProperty("租户名称")
    private String tenantName;

    @ApiModelProperty("用户类型")
    private String userType;

    @ApiModelProperty("聚合商ID")
    private String aggregatorId;

    @ApiModelProperty("企业ID")
    private String entId;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("最近登录时间")
    private String lastLoginTime;

    @ApiModelProperty("角色ID列表")
    private List<String> roleIds;

    @ApiModelProperty("角色名称列表")
    private List<String> roleNames;
}
