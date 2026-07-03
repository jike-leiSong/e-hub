package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("租户分页查询请求")
public class TenantPageReq {

    @ApiModelProperty("页码")
    private Integer pageIndex = 1;

    @ApiModelProperty("每页大小")
    private Integer pageSize = 20;

    @ApiModelProperty("关键字")
    private String keyword;

    @ApiModelProperty("租户类型")
    private String tenantType;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("产品编码")
    private String productCode;
}
