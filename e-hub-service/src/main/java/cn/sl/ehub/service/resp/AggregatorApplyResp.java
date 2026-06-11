package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 聚合商申报返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("聚合商申报返回实体")
public class AggregatorApplyResp {

    @ApiModelProperty("申报时间")
    private String applyTime;
    @ApiModelProperty("申报人")
    private String applyBy;
    @ApiModelProperty("申报状态 0=立即申报，1=申报中，2=申报结束")
    private String applyStatus;
    @ApiModelProperty("中标状态 0=未中标，1=中标")
    private String winStatus;
    @ApiModelProperty("申报类型 0=自动申报，1=手动申报")
    private String applyType;
    @ApiModelProperty("用户总量")
    private Integer entNum;
    @ApiModelProperty("未申报企业")
    private Integer applyNoNum;
    @ApiModelProperty("计划参与用户")
    private Integer applyYesNum;
    @ApiModelProperty("申报资源类型")
    private String applyResourceType;
    @ApiModelProperty("计划日")
    private String planDate;
    @ApiModelProperty("报价状态 0未提交  1已提交")
    private String applyPriceStatus;
    @ApiModelProperty("申报内容")
    private String applyContext;
}
