package cn.sl.ehub.service.vo;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "aggregator_device_date_profit")
public class AggregatorDeviceDateProfit {
    /**
     * 主键ID
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * 聚合商主键ID
     */
    @Column(name = "aggregator_id")
    private String aggregatorId;

    /**
     * 企业用户ID
     */
    @Column(name = "ent_id")
    private String entId;

    /**
     * 资源类型ID
     */
    @Column(name = "resource_type_id")
    private String resourceTypeId;

    /**
     * 设备ID
     */
    @Column(name = "device_base_id")
    private String deviceBaseId;

    /**
     * 日期
     */
    @Column(name = "date")
    private String date;

    /**
     * 收益详情
     */
    @Column(name = "profit_detail")
    private String profitDetail;

    /**
     * 收益详单压缩字节流
     */
    @Column(name = "profit_detail_byte")
    private byte[] profitDetailByte;
}