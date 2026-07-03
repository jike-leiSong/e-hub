package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("代理电价 96 点明细")
public class AgentPricePointResp {

    @ApiModelProperty("时点 HH:mm")
    private String time;

    @ApiModelProperty("尖峰平谷类型")
    private String periodType;

    @ApiModelProperty("电度价格")
    private String ddPrice;

    @ApiModelProperty("输配价格")
    private String spPrice;

    @ApiModelProperty("附加价格")
    private String fjPrice;

    @ApiModelProperty("线损价格")
    private String xsPrice;

    @ApiModelProperty("系统运行价格")
    private String systemPrice;

    @ApiModelProperty("代理购电价格")
    private String dlPrice;
}
