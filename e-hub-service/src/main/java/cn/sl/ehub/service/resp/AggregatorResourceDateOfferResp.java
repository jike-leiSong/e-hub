package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 申报价格详情
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("申报价格详情")
public class AggregatorResourceDateOfferResp {

    @ApiModelProperty("聚合商ID")
    private String aggregatorId;
    @ApiModelProperty("资源类型ID")
    private String resourceTypeId;
    @ApiModelProperty("日期")
    private String date;
    @ApiModelProperty("开始时间")
    private String startTime;
    @ApiModelProperty("结束时间")
    private String endTime;
    @ApiModelProperty("价格")
    private Double offer;
    @ApiModelProperty("是否报价 false否 true是")
    private Boolean priceStatus;
}
