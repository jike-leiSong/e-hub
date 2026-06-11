package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 聚合商信息
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("聚合商信息")
@Table(name = "aggregator_info")
public class AggregatorInfo {

    @Id
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Integer id;
    @ApiModelProperty("聚合商ID")
    @Column(name = "aggregator_id")
    private String aggregatorId;
    @ApiModelProperty("聚合商名称")
    @Column(name = "aggregator_name")
    private String aggregatorName;
    @ApiModelProperty("删除标记 0未删除 1已删除")
    @Column(name = "del_flag")
    private String delFlag;
    @ApiModelProperty("申报开始时间")
    @Column(name = "apply_start_time")
    private String applyStartTime;
    @ApiModelProperty("申报结束时间")
    @Column(name = "apply_end_time")
    private String applyEndTime;
    @ApiModelProperty("聚合商别名")
    @Column(name = "aggregator_alias_name")
    private String aggregatorAliasName;

//    @ApiModelProperty("聚合商电网标识")
//    @Column(name = "remote_id")
//    private String remoteId;


}
