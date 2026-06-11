package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 企业用户申报计划
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("企业用户申报计划")
@Table(name = "aggregator_ent_apply_plan")
public class AggregatorEntApplyPlan {

    @Id
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Integer id;
    @ApiModelProperty("聚合商ID")
    @Column(name = "aggregator_id")
    private String aggregatorId;
    @ApiModelProperty("企业ID")
    @Column(name = "ent_id")
    private String entId;
    @ApiModelProperty("申报计划开始时间")
    @Column(name = "start_date")
    private String startDate;
    @ApiModelProperty("申报计划结束时间")
    @Column(name = "end_date")
    private String endDate;
    @ApiModelProperty("申报时间")
    @Column(name = "apply_time")
    private String applyTime;
    @ApiModelProperty("计划状态 0默认计划 1临时计划")
    @Column(name = "plan_status")
    private Boolean planStatus;
    @ApiModelProperty("调节状态 0不参与 1参与")
    @Column(name = "status")
    private Boolean status;
    @ApiModelProperty("保存状态 0暂存 1提交")
    @Column(name = "save_status")
    private Boolean saveStatus;
    @ApiModelProperty("详情")
    @Column(name = "detail")
    private String detail;
}
