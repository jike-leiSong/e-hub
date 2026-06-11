package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 今日调节完成率和实时贡献率
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("今日调节完成率和实时贡献率")
@Table(name = "big_screen_ent_today_rate")
public class BigScreenEntTodayRate {

    @Id
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Long id;
    @ApiModelProperty("企业ID")
    @Column(name = "ent_id")
    private String entId;
    @ApiModelProperty("企业名称")
    @Column(name = "ent_name")
    private String entName;
    @ApiModelProperty("时间")
    @Column(name = "time")
    private String time;
    @ApiModelProperty("预计调节电量")
    @Column(name = "total_quantity")
    private Double totalQuantity;
    @ApiModelProperty("已完成调节电量")
    @Column(name = "finish_quantity")
    private Double finishQuantity;
    @ApiModelProperty("已完成进度")
    @Column(name = "finish_rate")
    private Double finishRate;
    @ApiModelProperty("森林面积")
    @Column(name = "area")
    private Double area;
    @ApiModelProperty("总方格")
    @Column(name = "total_square")
    private Integer totalSquare;
    @ApiModelProperty("已完成方格")
    @Column(name = "finish_square")
    private Double finishSquare;
    @ApiModelProperty("上升下降名次")
    @Column(name = "order_num")
    private Integer orderNum;
}
