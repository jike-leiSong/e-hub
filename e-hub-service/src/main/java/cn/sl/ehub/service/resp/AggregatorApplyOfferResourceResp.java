package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 聚合商申报价格资源类型返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("聚合商申报价格资源类型返回实体")
public class AggregatorApplyOfferResourceResp {

    @ApiModelProperty("资源类型ID")
    private String resourceTypeId;
    @ApiModelProperty("资源类型名称")
    private String resourceTypeName;
    @ApiModelProperty("状态 false否 true是")
    private Boolean status;
    @ApiModelProperty("聚合商申报价格资源类型日期返回实体")
    List<AggregatorApplyOfferResourceDateResp> dateList;
}
