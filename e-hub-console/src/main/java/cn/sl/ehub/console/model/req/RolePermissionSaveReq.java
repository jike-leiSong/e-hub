package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("角色权限保存请求")
public class RolePermissionSaveReq {

    @ApiModelProperty("权限编码列表")
    private List<String> permissionCodes;
}
