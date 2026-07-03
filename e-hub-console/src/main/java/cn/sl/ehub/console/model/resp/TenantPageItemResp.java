package cn.sl.ehub.console.model.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("租户分页项响应")
public class TenantPageItemResp {

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

    @ApiModelProperty("产品编码列表")
    private List<String> productCodes;

    @ApiModelProperty("更新时间")
    private String updateTime;
}
