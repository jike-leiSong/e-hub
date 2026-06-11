package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 聚合商申报报价
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("聚合商申报报价")
@Table(name = "aggregator_date_apply_detail_offer")
public class AggregatorDateApplyDetailOffer {

    @Id
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Integer id;
    @ApiModelProperty("聚合商ID")
    @Column(name = "aggregator_id")
    private String aggregatorId;
    @ApiModelProperty("日期")
    @Column(name = "date")
    private String date;
    @ApiModelProperty("开始时间")
    @Column(name = "start_time")
    private String startTime;
    @ApiModelProperty("结束时间")
    @Column(name = "end_time")
    private String endTime;
    @ApiModelProperty("申报报价")
    @Column(name = "delivery_offer")
    private Double deliveryOffer;
}
