package cn.sl.ehub.console.auth.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("客户产品开通信息")
public class ProductCustomerResp {

    @ApiModelProperty("用户ID")
    private String userId;

    @ApiModelProperty("登录账号")
    private String username;

    @ApiModelProperty("展示名称")
    private String displayName;

    @ApiModelProperty("用户类型")
    private String userType;

    @ApiModelProperty("聚合商ID")
    private String aggregatorId;

    @ApiModelProperty("企业用户ID")
    private String entId;

    @ApiModelProperty("客户ID")
    private String customerId;

    @ApiModelProperty("已开通产品")
    private List<String> products;
}
