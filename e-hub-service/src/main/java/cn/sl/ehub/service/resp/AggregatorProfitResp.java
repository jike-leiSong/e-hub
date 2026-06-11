package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 聚合商收益
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("聚合商收益")
public class AggregatorProfitResp {

    @ApiModelProperty("下发收益")
    private Double issueProfit;
    @ApiModelProperty("聚合商收益")
    private Double aggregatorProfit;
    @ApiModelProperty("调节电量")
    private Double electricQuantity;
}
