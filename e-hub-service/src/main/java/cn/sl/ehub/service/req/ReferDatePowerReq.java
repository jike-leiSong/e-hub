package cn.sl.ehub.service.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;


/**
  * 参考日功率请求实体
  *
  * @author sl
  * @classes cn.sl.ehub.upstream.req.ReferDatePowerReq
  * @date 2026-05-28
  */
@Data
@ApiModel("企业用户申报计划请求实体")
public class ReferDatePowerReq {

    @ApiModelProperty("聚合商ID")
    @NotBlank(message = "聚合商ID不能为空")
    private String aggregatorId;

    @ApiModelProperty("资源ID")
    @NotBlank(message = "资源ID不能为空")
    private String sourceId;


    @ApiModelProperty("参考日")
    @NotBlank(message = "参考日不能为空 yyyy-MM-dd ")
    private String referDate;


}
