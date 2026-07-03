package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("用户状态更新请求")
public class UserStatusUpdateReq {

    @ApiModelProperty("状态")
    private Integer status;
}
