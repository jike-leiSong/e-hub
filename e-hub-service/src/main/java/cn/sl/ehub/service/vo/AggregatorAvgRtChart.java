package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
@Data
@Table(name = "aggregator_avg_rt_chart")
public class AggregatorAvgRtChart {

    @Id
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Integer id;
    @ApiModelProperty("聚合商ID")
    @Column(name = "aggregator_id")
    private String aggregatorId;
    @ApiModelProperty("资源类型")
    @Column(name = "resource_type")
    private String resourceType;
    @ApiModelProperty("火电平均负荷率曲线")
    @Column(name = "avg_rt_chart")
    private String avgRtChart;
    @ApiModelProperty("基线日期")
    @Column(name = "date")
    private String date;
}
