package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 查询申报计划开始结束日期返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("查询申报计划开始结束日期返回实体")
public class AggregatorEntApplyDateResp {

    @ApiModelProperty("开始日期")
    private String startDate;
    @ApiModelProperty("结束日期")
    private String endDate;
}
