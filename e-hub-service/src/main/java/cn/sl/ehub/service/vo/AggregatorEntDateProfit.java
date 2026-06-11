package cn.sl.ehub.service.vo;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "aggregator_ent_date_profit")
public class AggregatorEntDateProfit {
    /**
     * 主键ID
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * 聚合商ID
     */
    @Column(name = "aggregator_id")
    private String aggregatorId;

    /**
     * 日期
     */
    @Column(name = "date")
    private String date;

    /**
     * 企业用户ID
     */
    @Column(name = "ent_id")
    private String entId;

    /**
     * 企业用户总收益
     */
    @Column(name = "ent_profit")
    private Double entProfit;

    /**
     * 电量
     */
    @Column(name = "electric_quantity")
    private Double electricQuantity;

    /**
     * 计算出清价格
     */
    @Column(name = "count_price")
    private Double countPrice;

    /**
     * 平均出清价格
     */
    @Column(name = "average_price")
    private Double averagePrice;

    /**
     * 出清金额
     */
    @Column(name = "count_profit")
    private Double countProfit;
}