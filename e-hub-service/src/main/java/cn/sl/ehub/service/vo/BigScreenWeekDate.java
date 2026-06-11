package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 近一周有效调节电量
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("近一周有效调节电量")
@Table(name = "big_screen_week_date")
public class BigScreenWeekDate {

    @Id
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Long id;
    @ApiModelProperty("日期")
    @Column(name = "date")
    private String date;
    @ApiModelProperty("调节电量")
    @Column(name = "quantity")
    private Double quantity;
    @ApiModelProperty("调节收益")
    @Column(name = "profit")
    private Double profit;
}
