package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("开放接口代理电价查询请求")
public class AgentPriceOpenApiQueryReq extends HaomaidianAgentPriceQueryReq {

    @ApiModelProperty("是否返回 96 点明细")
    private Boolean returnPoints;
}
