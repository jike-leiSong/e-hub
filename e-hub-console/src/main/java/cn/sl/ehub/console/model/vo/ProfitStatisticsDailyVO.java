package cn.sl.ehub.console.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 收益统计结果对象
 * @Author sl
 * @Date 2026-05-28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "收益统计-每日收益")
public class ProfitStatisticsDailyVO {

    @ApiModelProperty(value = "调度下发金额")
    private Double issueAmount;
    @ApiModelProperty(value = "负荷聚合商收益")
    private Double aggregatorProfits;
    @ApiModelProperty(value = "用户收益")
    private Double userProfits;
}
