package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("电价规则价格行请求")
public class TariffRulePriceRowReq {

    @ApiModelProperty("企业用电类别")
    private String userType;

    @ApiModelProperty("企业用电压等级")
    private String dyLevel;

    @ApiModelProperty("收费类型/其他属性")
    private String sfType;

    @ApiModelProperty("价格类型，默认电度")
    private String priceType;

    @ApiModelProperty("尖时价格，元/kWh")
    private BigDecimal jianPrice;

    @ApiModelProperty("峰时价格，元/kWh")
    private BigDecimal fengPrice;

    @ApiModelProperty("平时价格，元/kWh")
    private BigDecimal pingPrice;

    @ApiModelProperty("谷时价格，元/kWh")
    private BigDecimal guPrice;

    @ApiModelProperty("深谷价格，元/kWh")
    private BigDecimal shenguPrice;

    @ApiModelProperty("容量电价")
    private BigDecimal capacityElectricityPrice;

    @ApiModelProperty("需量电价")
    private BigDecimal demandElectricityPrice;
}
