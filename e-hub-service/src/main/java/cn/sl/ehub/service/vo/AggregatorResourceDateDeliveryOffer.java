package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 聚合商资源类型日期申报价格
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("聚合商资源类型日期申报价格")
@Table(name = "aggregator_resource_date_delivery_offer")
public class AggregatorResourceDateDeliveryOffer {

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
    @ApiModelProperty("价格")
    @Column(name = "offer")
    private Double offer;
    @ApiModelProperty("状态 0暂存 1提交")
    @Column(name = "status")
    private String status;
    @ApiModelProperty("价格详情")
    @Column(name = "price_detail")
    private String priceDetail;
    @ApiModelProperty("是否报价 false否 true是")
    @Column(name = "price_status")
    private Boolean priceStatus;
    @ApiModelProperty("报价曲线")
    @Column(name = "price_chart")
    private String priceChart;
}
