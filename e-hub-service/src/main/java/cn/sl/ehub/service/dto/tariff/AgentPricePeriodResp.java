package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("电网代理价格分时段价格")
public class AgentPricePeriodResp {

    @ApiModelProperty("电度价格")
    private String ddPrice;

    @ApiModelProperty("代理购电价格")
    private String dlPrice;

    @ApiModelProperty("输配/系统运行价格")
    private String spPrice;

    @ApiModelProperty("附加价格")
    private String fjPrice;

    @ApiModelProperty("时间段")
    private List<String> times;
}
