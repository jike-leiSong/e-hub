package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 设备申报计划
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("设备申报计划")
@Table(name = "aggregator_ent_device_apply_plan")
public class AggregatorEntDeviceApplyPlan {

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
    @ApiModelProperty("资源类型ID")
    @Column(name = "resource_type_id")
    private String resourceTypeId;
    @ApiModelProperty("设备ID")
    @Column(name = "device_base_id")
    private String deviceBaseId;
    @ApiModelProperty("日期")
    @Column(name = "date")
    private String date;
    @ApiModelProperty("开始时间")
    @Column(name = "start_time")
    private String startTime;
    @ApiModelProperty("结束时间")
    @Column(name = "end_time")
    private String endTime;
    @ApiModelProperty("计划功率")
    @Column(name = "plan_power")
    private Double planPower;
}
