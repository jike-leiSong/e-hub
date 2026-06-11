package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 查询计划资源列表返回实体
 *
 * @author sl
 * @classes cn.sl.ehub.upstream.resp.QueryPlanListResp
 * @date 2026-05-28
 */
@Data
@ApiModel("查询计划资源列表返回实体")
public class QueryPlanSourceListResp {

    @ApiModelProperty("资源类型id")
    private String sourceId;

    @ApiModelProperty("数据集合")
    private List<QueryPlanListDataResp> planDataList;


}
