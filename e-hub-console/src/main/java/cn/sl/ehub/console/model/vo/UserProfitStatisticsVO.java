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
@ApiModel(value = "用户收益统计结果对象")
@NoArgsConstructor
@AllArgsConstructor
public class UserProfitStatisticsVO {

    @ApiModelProperty(value = "用户收益合计")
    private Double userAmount;
    @ApiModelProperty(value = "颜色")
    private List<UserProfitStatisticsDetailsVO> userProfitStatisticsList;
}
