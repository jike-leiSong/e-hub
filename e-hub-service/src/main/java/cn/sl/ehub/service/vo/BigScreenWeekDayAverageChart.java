package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 一周日平均曲线
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("一周日平均曲线")
@Table(name = "big_screen_week_day_average_chart")
public class BigScreenWeekDayAverageChart {

    @Id
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Long id;
    @ApiModelProperty("价格")
    @Column(name = "offer")
    private String offer;
    @ApiModelProperty("功率")
    @Column(name = "power")
    private String power;
}
