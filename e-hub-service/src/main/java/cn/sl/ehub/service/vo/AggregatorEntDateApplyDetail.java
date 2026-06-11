package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 企业用户申报详情
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("企业用户申报详情")
@Table(name = "aggregator_ent_date_apply_detail")
public class AggregatorEntDateApplyDetail {

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
    @ApiModelProperty("企业ID")
    @Column(name = "ent_id")
    private String entId;
    @ApiModelProperty("企业编码")
    @Column(name = "station_id")
    private String stationId;
    @ApiModelProperty("申报日期")
    @Column(name = "apply_date")
    private String applyDate;
    @ApiModelProperty("申报时间")
    @Column(name = "apply_time")
    private String applyTime;
    @ApiModelProperty("计划状态 0默认计划 1临时计划")
    @Column(name = "plan_status")
    private Boolean planStatus;
    @ApiModelProperty("中标状态 0未中标 1中标")
    @Column(name = "win_status")
    private Boolean winStatus;
    @ApiModelProperty("申报状态 0未申报 1申报")
    @Column(name = "apply_status")
    private String applyStatus;
}
