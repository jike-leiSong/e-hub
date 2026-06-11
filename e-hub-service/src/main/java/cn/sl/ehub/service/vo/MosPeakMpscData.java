package cn.sl.ehub.service.vo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * 调峰市场最大调峰能力数据表(96点数据)
 *
 * @author sl
 * @date 2026-05-28
 */
@Data
@Table(name = "mos_peak_mpsc_data")
public class MosPeakMpscData {

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
     * 时间点(从1-96)
     */
    @Column(name = "point_index")
    private Integer pointIndex;

    /**
     * 能力值(MW)
     */
    @Column(name = "value")
    private java.math.BigDecimal value;

    /**
     * 秒级时间戳
     */
    @Column(name = "timestamp")
    private Long timestamp;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;
}
