package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 企业累计收益
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("企业累计收益")
@Table(name = "big_screen_ent_profit")
public class BigScreenEntProfit {

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
    @ApiModelProperty("电量")
    @Column(name = "quantity")
    private Double quantity;
    @ApiModelProperty("收益")
    @Column(name = "profit")
    private Double profit;
    @ApiModelProperty("创建时间")
    @Column(name = "create_time")
    private String createTime;
    @ApiModelProperty("更新时间")
    @Column(name = "update_time")
    private String updateTime;
}
