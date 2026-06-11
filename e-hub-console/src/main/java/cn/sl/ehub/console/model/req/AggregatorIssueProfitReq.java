package cn.sl.ehub.console.model.req;

import cn.sl.ehub.common.vo.DataResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 聚合商下发收益实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel(value = "聚合商下发收益实体")
public class AggregatorIssueProfitReq {

    @ApiModelProperty(value = "聚合商ID", required = true)
    private String aggregatorId;
    @ApiModelProperty(value = "下发日期")
    private String date;
    @ApiModelProperty(value = "收益", required = true)
    private List<DataResp> profitList;
}
