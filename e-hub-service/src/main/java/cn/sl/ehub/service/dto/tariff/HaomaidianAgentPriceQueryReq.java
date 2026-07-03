package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("好买电代理电价查询请求")
public class HaomaidianAgentPriceQueryReq extends AgentPriceQueryReq {

    @ApiModelProperty("查询维度")
    private String queryDimension;

    @ApiModelProperty("负荷曲线")
    private List<BigDecimal> curves;

    @ApiModelProperty("月度用电量汇总")
    private String monthCountSum;

    @ApiModelProperty("区域选择值")
    private List<String> area;

    @ApiModelProperty("附加筛选值")
    private String fieldValue;

    @ApiModelProperty("图表类型")
    private Object echartActive;
}
