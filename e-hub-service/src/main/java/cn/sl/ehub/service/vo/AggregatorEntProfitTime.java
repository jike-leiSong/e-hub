package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 企业有效用电和收益配置表
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("企业有效用电和收益配置表")
@Table(name = "aggregator_ent_profit_time")
public class AggregatorEntProfitTime {

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
    @ApiModelProperty("开始时间")
    @Column(name = "start_time")
    private String startTime;
    @ApiModelProperty("结束时间")
    @Column(name = "end_time")
    private String endTime;
}
