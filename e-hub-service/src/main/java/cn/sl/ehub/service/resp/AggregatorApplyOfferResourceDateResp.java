package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 聚合商申报价格资源类型日期返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("聚合商申报价格资源类型日期返回实体")
public class AggregatorApplyOfferResourceDateResp {

    @ApiModelProperty("日期")
    private String date;
    @ApiModelProperty("状态 false否 true是")
    private Boolean status;
    @ApiModelProperty("申报价格")
    List<AggregatorResourceDateOfferResp> offerList;
}
