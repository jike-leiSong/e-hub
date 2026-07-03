package cn.sl.ehub.console.model.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("租户产品响应")
public class TenantProductResp {

    @ApiModelProperty("产品编码")
    private String productCode;

    @ApiModelProperty("是否启用")
    private Integer enabled;

    @ApiModelProperty("开始日期")
    private String validFrom;

    @ApiModelProperty("结束日期")
    private String validTo;

    @ApiModelProperty("配置JSON")
    private String configJson;
}
