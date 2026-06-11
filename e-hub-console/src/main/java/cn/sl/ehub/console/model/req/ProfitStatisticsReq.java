package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Description: 收益统计入参
 * @Author sl
 * @Date 2026-05-28
 */

@Data
@ApiModel
public class ProfitStatisticsReq {

    @ApiModelProperty(value = "聚合商id", notes = "聚合商id", example = "")
    private String aggregatorId;
    @ApiModelProperty(value = "资源类型id", notes = "资源类型id", example = "")
    private String resourceTypeId;
    @ApiModelProperty(value = "开始时间", notes = "yyyy-MM-dd", example = "2020-11-03")
    private String startTime;
    @ApiModelProperty(value = "结束时间", notes = "yyyy-MM-dd", example = "2020-11-03")
    private String endTime;
}
