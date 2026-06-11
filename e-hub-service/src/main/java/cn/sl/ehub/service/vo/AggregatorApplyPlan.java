package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * 聚合商申报计划表
 *
 * @author sl
 * @classes cn.sl.ehub.upstream.vo.AggregatorApplyPlan
 * @date 2026-05-28
 */
@Data
@ApiModel("聚合商申报计划表")
@Table(name = "aggregator_apply_plan")
public class AggregatorApplyPlan {

    @Id
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Integer id;

    @ApiModelProperty("聚合商ID")
    @Column(name = "aggregator_id")
    private String aggregatorId;

    @ApiModelProperty("资源id")
    @Column(name = "source_id")
    private String sourceId;


    @ApiModelProperty("参考日")
    @Column(name = "refer_date")
    private String referDate;

    @ApiModelProperty("申报计划开始时间")
    @Column(name = "start_date")
    private String startDate;

    @ApiModelProperty("申报计划开始时间")
    @Column(name = "end_date")
    private String endDate;

    @ApiModelProperty("参考日功率")
    @Column(name = "refer_date_power")
    private String referDatePower;


    @ApiModelProperty("调整系数")
    @Column(name = "adjust_factor")
    private String adjustFactor;


    @ApiModelProperty("调整值")
    @Column(name = "adjust_value")
    private String adjustValue;

    @ApiModelProperty("申报功率")
    @Column(name = "apply_power")
    private String applyPower;

    @ApiModelProperty("申报价格")
    @Column(name = "apply_price")
    private String applyPrice;

    @ApiModelProperty("创建时间")
    @Column(name = "create_time")
    private String createTime;

    @ApiModelProperty("更新时间")
    @Column(name = "update_time")
    private String updateTime;
}
