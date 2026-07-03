package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("租户状态变更请求")
public class TenantStatusUpdateReq {

    @ApiModelProperty("状态 1启用 0停用")
    private Integer status;
}
