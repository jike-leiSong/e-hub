package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("日收益返回实体")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AggregatorEntDateProfitResp {

    @ApiModelProperty("聚合商ID")
    private String aggregatorId;

    @ApiModelProperty("企业ID")
    private String entId;

    private String entName;

    @ApiModelProperty("时间")
    private String date;

    @ApiModelProperty("企业用户总收益")
    private Double entProfit;
    @ApiModelProperty("企业用户总收益")
    private Double totalProfit;
}
