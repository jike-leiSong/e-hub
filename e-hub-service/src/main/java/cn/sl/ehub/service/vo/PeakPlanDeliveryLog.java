package cn.sl.ehub.service.vo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * 调峰计划申报电网上送日志表
 *
 * @author sl
 * @date 2026-05-28
 */
@Data
@Table(name = "peak_plan_delivery_log")
public class PeakPlanDeliveryLog {

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
     * 资源ID/机组ID/组号
     */
    @Column(name = "source_id")
    private String sourceId;

    /**
     * 数据类型（96POINT:96点数据，DAILY:日数据）
     */
    @Column(name = "data_type")
    private String dataType;

    /**
     * 上送方法（declare或chandleBid）
     */
    @Column(name = "method")
    private String method;

    /**
     * 上送数据内容
     */
    @Column(name = "delivery_data")
    private String deliveryData;

    /**
     * 上送响应结果
     */
    @Column(name = "delivery_status")
    private String deliveryStatus;

    /**
     * 数据日期（次日日期）
     */
    @Column(name = "data_date")
    private Date dataDate;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 备注
     */
    @Column(name = "remark")
    private String remark;
}
