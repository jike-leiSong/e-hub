package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 设备启停计划
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("设备启停计划")
@Table(name = "aggregator_ent_date_device_start_stop_plan")
public class AggregatorEntDateDeviceStartStopPlan {

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
    @ApiModelProperty("日期")
    @Column(name = "date")
    private String date;
    @ApiModelProperty("设备启停详情")
    @Column(name = "detail")
    private String detail;
}
