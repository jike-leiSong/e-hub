package cn.sl.ehub.console.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: 收益统计结果对象
 * @Author sl
 * @Date 2026-05-28
 */
@Data
@ApiModel(value = "收益统计结果对象")
@NoArgsConstructor
@AllArgsConstructor
public class ProfitStatisticsVO {

    @ApiModelProperty(value = "金额合计")
    private ProfitStatisticsDailyVO profitStatisticsAmount;
    @ApiModelProperty(value = "每日收益列表")
    private List<ProfitStatisticsDailyVO> profitStatisticsDailyList;
    @ApiModelProperty(value = "时间列表")
    private List<String> dateList;
}
