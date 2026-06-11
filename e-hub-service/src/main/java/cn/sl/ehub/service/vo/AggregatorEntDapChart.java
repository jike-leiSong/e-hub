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
@Table(name = "aggregator_ent_dap_chart")
public class AggregatorEntDapChart {
    @Id
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Integer id;
    @ApiModelProperty("聚合商ID")
    @Column(name = "aggregator_id")
    private String aggregatorId;
    @ApiModelProperty("企业用户ID")
    @Column(name = "ent_id")
    private String entId;
    @ApiModelProperty("企业编码")
    @Column(name = "station_id")
    private String stationId;
    @ApiModelProperty("资源类型")
    @Column(name = "resource_type")
    private String resourceType;
    @ApiModelProperty("基线负荷曲线")
    @Column(name = "dap_chart")
    private String dapChart;
    @ApiModelProperty("基线日期")
    @Column(name = "dap_date")
    private String dapDate;
}
