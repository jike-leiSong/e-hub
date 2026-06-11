package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 聚合商企业信息
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("聚合商企业信息")
@Table(name = "aggregator_ent")
public class AggregatorEnt {

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
    @ApiModelProperty("企业名称")
    @Column(name = "ent_name")
    private String entName;
    @ApiModelProperty("在线状态")
    @Column(name = "status")
    private Integer status;
    @ApiModelProperty("经度")
    @Column(name = "longitude")
    private String longitude;
    @ApiModelProperty("纬度")
    @Column(name = "latitude")
    private String latitude;
    @ApiModelProperty("企业用户占比")
    @Column(name = "percent")
    private Double percent;
    @ApiModelProperty("合同url地址")
    @Column(name = "agreement")
    private String agreement;
    @ApiModelProperty("允许申报时间")
    @Column(name = "allow_apply_time")
    private String allowApplyTime;
    @ApiModelProperty("服务开始时间")
    @Column(name = "service_start_date")
    private String serviceStartDate;
    @ApiModelProperty("服务结束时间")
    @Column(name = "service_end_date")
    private String serviceEndDate;
    @ApiModelProperty("编码")
    @Column(name = "sn_code")
    private String snCode;
    @ApiModelProperty("中标时间")
    @Column(name = "win_time")
    private String winTime;
    @ApiModelProperty("电网编码")
    @Column(name = "state_grid_code")
    private String stateGridCode;
    @ApiModelProperty("电网名称")
    @Column(name = "state_grid_name")
    private String stateGridName;
    @ApiModelProperty("用户容量")
    @Column(name = "install_cap")
    private Double installCap;
    @ApiModelProperty("是否按照下发计划运行")
    @Column(name = "plan_run_status")
    private Integer planRunStatus;


}
