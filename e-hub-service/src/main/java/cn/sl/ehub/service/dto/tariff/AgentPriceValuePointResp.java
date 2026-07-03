package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("代理电价原始点值")
public class AgentPriceValuePointResp {

    @ApiModelProperty("时点 HH:mm")
    private String bizTime;

    @ApiModelProperty("价格")
    private BigDecimal price;
}
