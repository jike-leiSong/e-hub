package cn.sl.ehub.console.auth.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("认证菜单分组")
public class AuthMenuGroupResp {

    @ApiModelProperty("分组名称")
    private String title;

    @ApiModelProperty("菜单项")
    private List<AuthMenuItemResp> items;
}
