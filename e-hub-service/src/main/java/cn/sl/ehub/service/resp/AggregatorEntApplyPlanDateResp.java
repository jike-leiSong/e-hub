package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 查询调峰日历返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("查询调峰日历返回实体")
public class AggregatorEntApplyPlanDateResp {

    @ApiModelProperty("展示日期")
    private String showDate;
    @ApiModelProperty("计划状态 false默认计划 true临时计划")
    private Boolean showPlanStatus;
}
