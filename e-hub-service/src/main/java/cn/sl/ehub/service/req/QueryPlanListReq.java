package cn.sl.ehub.service.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

  /**
   * 查询计划列表请求实体类
   *
   * @author sl
   * @classes cn.sl.ehub.upstream.req.QueryPlanListReq
   * @date 2026-05-28
   */
@Data
@ApiModel("查询计划列表请求实体类")
public class QueryPlanListReq {

    @ApiModelProperty("聚合商ID")
    @NotBlank(message = "聚合商ID不能为空")
    private String aggregatorId;


}
