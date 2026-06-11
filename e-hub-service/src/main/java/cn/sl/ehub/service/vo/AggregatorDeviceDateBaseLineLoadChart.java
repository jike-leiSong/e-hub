package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 设备基线负荷曲线
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("设备基线负荷曲线")
@Table(name = "aggregator_device_date_base_line_load_chart")
public class AggregatorDeviceDateBaseLineLoadChart {

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
    @ApiModelProperty("设备ID")
    @Column(name = "device_base_id")
    private String deviceBaseId;
    @ApiModelProperty("基线负荷曲线")
    @Column(name = "base_line_load_chart")
    private String baseLineLoadChart;
    @ApiModelProperty("开始日期")
    @Column(name = "start_date")
    private String startDate;
    @ApiModelProperty("结束日期")
    @Column(name = "end_date")
    private String endDate;
}
