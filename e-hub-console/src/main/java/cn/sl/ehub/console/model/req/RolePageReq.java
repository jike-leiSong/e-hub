package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("角色分页请求")
public class RolePageReq {

    @ApiModelProperty("页码")
    private Integer pageIndex = 1;

    @ApiModelProperty("每页大小")
    private Integer pageSize = 20;

    @ApiModelProperty("关键字")
    private String keyword;

    @ApiModelProperty("平台类型")
    private String platformType;

    @ApiModelProperty("状态")
    private Integer status;
}
