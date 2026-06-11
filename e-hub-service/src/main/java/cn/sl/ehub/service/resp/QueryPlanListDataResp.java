package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 查询计划列表返回实体
 *
 * @author sl
 * @classes cn.sl.ehub.upstream.resp.QueryPlanListResp
 * @date 2026-05-28
 */
@Data
@ApiModel("查询计划列表返回实体")
public class QueryPlanListDataResp {


    @ApiModelProperty("计划id")
    private Integer id;


    @ApiModelProperty("申报计划开始时间")
    private String startDate;

    @ApiModelProperty("申报计划开始时间")
    private String endDate;


    @ApiModelProperty("计划状态 0:已过期 1:待开始 2:执行中")
    private String planStatus;


}
