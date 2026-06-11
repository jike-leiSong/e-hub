package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 资源类型
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("资源类型")
@Table(name = "aggregator_resource_type")
public class AggregatorResourceType {

    @Id
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private String id;
    @ApiModelProperty("名称")
    @Column(name = "name")
    private String name;
    @ApiModelProperty("排序")
    @Column(name = "resource_order")
    private Integer resourceOrder;

    @ApiModelProperty("是否展示")
    @Column(name = "display")
    private Integer display;


    @ApiModelProperty("聚合商id")
    @Column(name = "aggregator_id")
    private String aggregatorId;
}
