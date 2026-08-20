package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("电价规则展开点")
public class TariffRulePointResp {

    @ApiModelProperty("时点 HH:mm")
    private String bizTime;

    @ApiModelProperty("时段类型")
    private String periodType;

    @ApiModelProperty("价格，元/kWh")
    private BigDecimal price;
}
