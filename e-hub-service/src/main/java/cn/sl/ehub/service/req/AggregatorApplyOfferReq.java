package cn.sl.ehub.service.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 聚合商申报价格请求实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("聚合商申报价格请求实体")
public class AggregatorApplyOfferReq {

    @ApiModelProperty("聚合商ID")
    private String aggregatorId;
    @ApiModelProperty("资源类型")
    private List<AggregatorApplyOfferResourceReq> resourceList;
}
