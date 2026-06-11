package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 调度下发功率曲线
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("调度下发功率曲线")
@Table(name = "aggregator_date_issue_chart")
public class AggregatorDateIssueChart {

    @Id
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Integer id;
    @ApiModelProperty("聚合商ID")
    @Column(name = "aggregator_id")
    private String aggregatorId;
    @ApiModelProperty("资源类型ID")
    @Column(name = "resource_type_id")
    private String resourceTypeId;
    @ApiModelProperty("日期")
    @Column(name = "date")
    private String date;
    @ApiModelProperty("下发功率曲线")
    @Column(name = "issue_chart")
    private String issueChart;
}
