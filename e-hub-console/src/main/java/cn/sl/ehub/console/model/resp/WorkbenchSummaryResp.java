package cn.sl.ehub.console.model.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("工作台汇总响应")
public class WorkbenchSummaryResp {

    @ApiModelProperty("租户数")
    private Integer tenantCount;

    @ApiModelProperty("启用租户数")
    private Integer activeTenantCount;

    @ApiModelProperty("账号数")
    private Integer userCount;

    @ApiModelProperty("角色数")
    private Integer roleCount;

    @ApiModelProperty("开通产品租户数")
    private Integer enabledProductTenantCount;

    @ApiModelProperty("配置项数")
    private Integer configCount;

    @ApiModelProperty("近7天操作数")
    private Integer last7dOperationCount;
}
