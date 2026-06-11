package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 聚合商收益
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("企业用户收益")
public class AggregatorEntProfitResp {

    @ApiModelProperty("企业ID")
    private String entId;
    @ApiModelProperty("收益")
    private Double entProfit;
    @ApiModelProperty("调节电量")
    private Double electricQuantity;
}
