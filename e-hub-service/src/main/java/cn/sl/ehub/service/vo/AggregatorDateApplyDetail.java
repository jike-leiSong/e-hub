package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 聚合商申报详情
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("聚合商申报详情")
@Table(name = "aggregator_date_apply_detail")
public class AggregatorDateApplyDetail {

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
    @ApiModelProperty("申报类型 0=自动申报，1=手动申报")
    @Column(name = "apply_type")
    private String applyType;
    @ApiModelProperty("申报状态 0=立即申报，1=申报中，2=申报结束")
    @Column(name = "apply_status")
    private String applyStatus;
    @ApiModelProperty("申报时间")
    @Column(name = "apply_time")
    private String applyTime;
    @ApiModelProperty("中标状态 0=未中标，1=中标")
    @Column(name = "win_status")
    private String winStatus;
    @ApiModelProperty("中标时间")
    @Column(name = "win_time")
    private String winTime;
    @ApiModelProperty("申报人")
    @Column(name = "apply_by")
    private String applyBy;
    @ApiModelProperty("创建时间")
    @Column(name = "create_time")
    private String createTime;
    @ApiModelProperty("更新时间")
    @Column(name = "update_time")
    private String updateTime;
}
