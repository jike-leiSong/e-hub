package cn.sl.ehub.service.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 聚合商明日计划申报
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("聚合商明日计划申报")
public class AggregatorApplyReq {

    @ApiModelProperty("聚合商ID")
    private String aggregatorId;
    @ApiModelProperty("申报类型 0=自动申报，1=手动申报")
    private String applyType;
    @ApiModelProperty("申报人")
    private String applyBy;
    @ApiModelProperty("日期")
    private String date;
}
