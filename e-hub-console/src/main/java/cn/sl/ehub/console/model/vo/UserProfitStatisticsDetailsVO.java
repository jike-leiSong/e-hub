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
@ApiModel(value = "用户收益统计详情结果对象")
@NoArgsConstructor
@AllArgsConstructor
public class UserProfitStatisticsDetailsVO {

    @ApiModelProperty(value = "颜色")
    private String color;
    @ApiModelProperty(value = "企业名称")
    private String entName;
    @ApiModelProperty(value = "企业收益")
    private String entProfit;
    @ApiModelProperty(value = "企业收益排序用")
    private Double entProfitSort;
    @ApiModelProperty(value = "收益百分比")
    private String profitPercent;
}
