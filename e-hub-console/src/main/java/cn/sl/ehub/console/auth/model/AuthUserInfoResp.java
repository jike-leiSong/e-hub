package cn.sl.ehub.console.auth.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("当前登录用户")
public class AuthUserInfoResp {

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("用户ID")
    private String userId;

    @ApiModelProperty("展示名称")
    private String displayName;

    @ApiModelProperty("用户类型 PLATFORM/AGGREGATOR/ENT")
    private String userType;

    @ApiModelProperty("聚合商ID")
    private String aggregatorId;

    @ApiModelProperty("企业用户ID")
    private String entId;
}
