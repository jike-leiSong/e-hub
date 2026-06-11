package cn.sl.ehub.service.vo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * 调峰市场第三方申报数据表
 *
 * @author sl
 * @date 2026-05-28
 */
@Data
@Table(name = "mos_peak_third_party_bid_data")
public class MosPeakThirdPartyBidData {

    /**
     * 主键ID
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Long id;

    /**
     * 聚合商ID
     */
    @Column(name = "aggregator_id")
    private String aggregatorId;

    /**
     * 资源ID
     */
    @Column(name = "source_id")
    private String sourceId;

    /**
     * 日期
     */
    @Column(name = "data_time")
    private Date dataTime;

    /**
     * 报价
     */
    @Column(name = "bid_price")
    private java.math.BigDecimal bidPrice;

    /**
     * 最大充电电量
     */
    @Column(name = "max_in_power")
    private java.math.BigDecimal maxInPower;

    /**
     * 最大放电电量
     */
    @Column(name = "max_out_power")
    private java.math.BigDecimal maxOutPower;

    /**
     * 日最大充电次数
     */
    @Column(name = "max_in_times")
    private java.math.BigDecimal maxInTimes;

    /**
     * 日最大放电次数
     */
    @Column(name = "max_out_times")
    private java.math.BigDecimal maxOutTimes;

    /**
     * 充电速率
     */
    @Column(name = "in_rate")
    private java.math.BigDecimal inRate;

    /**
     * 放电速率
     */
    @Column(name = "out_rate")
    private java.math.BigDecimal outRate;

    /**
     * 充电起始SOC
     */
    @Column(name = "soc")
    private java.math.BigDecimal soc;

    /**
     * 备用2
     */
    @Column(name = "value2")
    private java.math.BigDecimal value2;

    /**
     * 备用3
     */
    @Column(name = "value3")
    private java.math.BigDecimal value3;

    /**
     * 备用4
     */
    @Column(name = "value4")
    private java.math.BigDecimal value4;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;
}
