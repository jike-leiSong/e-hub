package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("租户新增更新请求")
public class TenantUpsertReq {

    @ApiModelProperty("租户名称")
    private String tenantName;

    @ApiModelProperty("租户类型")
    private String tenantType;

    @ApiModelProperty("聚合商ID")
    private String aggregatorId;

    @ApiModelProperty("企业ID")
    private String entId;

    @ApiModelProperty("管理员账号ID")
    private String ownerUserId;

    @ApiModelProperty("联系人")
    private String contactName;

    @ApiModelProperty("联系电话")
    private String contactPhone;

    @ApiModelProperty("备注")
    private String remark;
}
