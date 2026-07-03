package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("代理电价主表信息")
public class AgentPriceHeaderResp {

    @ApiModelProperty("省份名称")
    private String provinceName;

    @ApiModelProperty("容量电价")
    private BigDecimal capacityElectricityPrice;

    @ApiModelProperty("需量电价")
    private BigDecimal demandElectricityPrice;
}
