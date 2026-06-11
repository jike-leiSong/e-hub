package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 节假日
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("设备申报计划")
@Table(name = "aggregator_date_holiday")
public class AggregatorDateHoliday {

    @Id
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Integer id;
    @ApiModelProperty("年")
    @Column(name = "year")
    private String year;
    @ApiModelProperty("日期")
    @Column(name = "date")
    private String date;
    @ApiModelProperty("法定节假日（元旦1、春节2、清明节3、劳动节4、端午节5、中秋节6、国庆节7）")
    @Column(name = "legal_holiday")
    private String legalHoliday;
}
