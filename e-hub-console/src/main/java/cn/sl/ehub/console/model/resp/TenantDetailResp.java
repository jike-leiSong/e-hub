package cn.sl.ehub.console.model.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("租户详情响应")
public class TenantDetailResp {

    @ApiModelProperty("租户ID")
    private String tenantId;

    @ApiModelProperty("租户名称")
    private String tenantName;

    @ApiModelProperty("租户类型")
    private String tenantType;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("聚合商ID")
    private String aggregatorId;

    @ApiModelProperty("企业ID")
    private String entId;

    @ApiModelProperty("管理员账号ID")
    private String ownerUserId;

    @ApiModelProperty("管理员名称")
    private String ownerDisplayName;

    @ApiModelProperty("联系人")
    private String contactName;

    @ApiModelProperty("联系电话")
    private String contactPhone;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("产品列表")
    private List<TenantProductResp> products;

    @ApiModelProperty("账号列表")
    private List<ConsoleUserPageItemResp> users;
}
