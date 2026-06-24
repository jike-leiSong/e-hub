package cn.sl.ehub.console.auth.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("客户产品开通保存请求")
public class ProductProvisionReq {

    @ApiModelProperty("产品编码列表")
    private List<String> productCodes;
}
