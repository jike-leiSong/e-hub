package cn.sl.ehub.console.auth.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("认证菜单项")
public class AuthMenuItemResp {

    @ApiModelProperty("页面key")
    private String key;

    @ApiModelProperty("菜单名称")
    private String label;

    @ApiModelProperty("菜单图标")
    private String icon;

    @ApiModelProperty("子菜单")
    private List<AuthMenuItemResp> children;
}
