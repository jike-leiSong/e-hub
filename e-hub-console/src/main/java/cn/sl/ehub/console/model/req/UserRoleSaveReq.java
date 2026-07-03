package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("用户角色保存请求")
public class UserRoleSaveReq {

    @ApiModelProperty("角色ID列表")
    private List<String> roleIds;
}
