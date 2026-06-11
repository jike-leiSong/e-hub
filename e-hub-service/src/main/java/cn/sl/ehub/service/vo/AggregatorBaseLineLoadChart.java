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
@Table(name = "aggregator_base_line_load_chart")
public class AggregatorBaseLineLoadChart {
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
    @ApiModelProperty("基线负荷曲线")
    @Column(name = "base_line_load_chart")
    private String baseLineLoadChart;
    @ApiModelProperty("基线日期")
    @Column(name = "base_date")
    private String baseDate;
}
