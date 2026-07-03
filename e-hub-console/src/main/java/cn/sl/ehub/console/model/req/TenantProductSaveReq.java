package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("租户产品保存请求")
public class TenantProductSaveReq {

    @ApiModelProperty("产品列表")
    private List<TenantProductItemReq> products;
}
