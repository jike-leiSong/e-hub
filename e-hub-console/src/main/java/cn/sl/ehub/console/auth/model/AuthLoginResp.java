package cn.sl.ehub.console.auth.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("登录响应")
public class AuthLoginResp {

    @ApiModelProperty("访问令牌")
    private String token;

    @ApiModelProperty("令牌类型")
    private String tokenType;

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

    @ApiModelProperty("过期时间戳")
    private Long expireAt;

    @ApiModelProperty("过期秒数")
    private Long expireSeconds;
}
