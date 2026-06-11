package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 聚合商申报价格返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("聚合商申报价格返回实体")
public class AggregatorApplyOfferResp {

    @ApiModelProperty("状态 0暂存 1提交")
    private String status;
    @ApiModelProperty("聚合商申报价格资源类型返回实体")
    List<AggregatorApplyOfferResourceResp> resourceList;
}
