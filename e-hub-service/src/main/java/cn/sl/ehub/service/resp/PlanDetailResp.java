package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.util.List;

/**
 * 计划详情返回实体
 *
 * @author sl
 * @classes cn.sl.ehub.upstream.resp.QueryPlanListResp
 * @date 2026-05-28
 */
@Data
@ApiModel("计划详情返回实体")
public class PlanDetailResp {


    @ApiModelProperty("计划id")
    private Integer id;


    @ApiModelProperty("申报计划开始时间")
    private String startDate;

    @ApiModelProperty("申报计划结束时间")
    private String endDate;


    @ApiModelProperty("参考日")
    private String referDate;

    @ApiModelProperty("资源类型id")
    private String sourceId;

    @ApiModelProperty("数据集合")
    private List<PlanDetailDataResp> dataList;


}
