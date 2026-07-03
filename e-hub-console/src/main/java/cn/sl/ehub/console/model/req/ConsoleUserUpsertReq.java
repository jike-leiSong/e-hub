package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("平台用户新增更新请求")
public class ConsoleUserUpsertReq {

    @ApiModelProperty("账号")
    private String username;

    @ApiModelProperty("姓名")
    private String displayName;

    @ApiModelProperty("租户ID")
    private String tenantId;

    @ApiModelProperty("用户类型")
    private String userType;

    @ApiModelProperty("聚合商ID")
    private String aggregatorId;

    @ApiModelProperty("企业ID")
    private String entId;

    @ApiModelProperty("状态")
    private Integer status;
}
