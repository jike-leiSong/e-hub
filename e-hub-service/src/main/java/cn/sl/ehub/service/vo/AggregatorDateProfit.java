package cn.sl.ehub.service.vo;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "aggregator_date_profit")
public class AggregatorDateProfit {
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
     * 下发收益
     */
    @Column(name = "issue_profit")
    private Double issueProfit;

    /**
     * 聚合商收益
     */
    @Column(name = "aggregator_profit")
    private Double aggregatorProfit;

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
}