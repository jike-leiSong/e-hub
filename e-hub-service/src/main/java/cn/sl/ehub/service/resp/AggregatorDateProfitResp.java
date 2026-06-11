package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel("日收益返回实体1")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AggregatorDateProfitResp {

    @ApiModelProperty("聚合商ID")
    private String aggregatorId;

    @ApiModelProperty("时间")
    private String date;

    @ApiModelProperty("下发收益")
    private Double issueProfit;

    @ApiModelProperty("聚合商收益")
    private Double aggregatorProfit;

    @ApiModelProperty("企业用户总收益")
    private Double entProfit;

    @ApiModelProperty("企业用户详情集合")
    private List<AggregatorEntDateProfitResp> entDateProfitRespList;
}
