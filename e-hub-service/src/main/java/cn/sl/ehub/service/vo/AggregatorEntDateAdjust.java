package cn.sl.ehub.service.vo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
@Data
@Table(name = "aggregator_ent_date_adjust")
public class AggregatorEntDateAdjust {
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
