package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 全局概况
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("全局概况")
@Table(name = "big_screen_general_situation")
public class BigScreenGeneralSituation {

    @Id
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Long id;
    @ApiModelProperty("交易天数")
    @Column(name = "transaction_day")
    private Integer transactionDay;
    @ApiModelProperty("调节电量")
    @Column(name = "quantity")
    private Double quantity;
    @ApiModelProperty("收益统计")
    @Column(name = "profit")
    private Double profit;
    @ApiModelProperty("减排CO2")
    @Column(name = "co2")
    private Double co2;
    @ApiModelProperty("累计植树")
    @Column(name = "tree")
    private Integer tree;
    @ApiModelProperty("森林面积")
    @Column(name = "area")
    private Double area;
    @ApiModelProperty("节约标准煤")
    @Column(name = "coal")
    private Double coal;
    @ApiModelProperty("减排SO2")
    @Column(name = "so2")
    private Double so2;
    @ApiModelProperty("减排氮氧化物")
    @Column(name = "nox")
    private Double nox;
    @ApiModelProperty("创建时间")
    @Column(name = "create_time")
    private String createTime;
    @ApiModelProperty("修改时间")
    @Column(name = "update_time")
    private String updateTime;
}
