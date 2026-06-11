package cn.sl.ehub.service.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 新增计划请求实体
 *
 * @author sl
 * @classes ccn.sl.ehub.upstream.req.AddPlanReq
 * @date 2026-05-28
 */
@Data
@ApiModel("新增计划请求实体")
public class AddPlanReq {


    @ApiModelProperty("计划id 新增时为空 修改时必传")
    private String id;


    @ApiModelProperty("申报计划开始时间")
    @NotBlank(message = "申报计划开始时间不能为空")
    private String startDate;

    @ApiModelProperty("申报计划结束时间")
    @NotBlank(message = "申报计划开始时间不能为空")
    private String endDate;


    @ApiModelProperty("参考日")
    @NotBlank(message = "参考日不能为空")
    private String referDate;

    @ApiModelProperty("资源类型id")
    @NotBlank(message = "资源类型id不能为空")
    private String sourceId;

    @ApiModelProperty("聚合商id")
    @NotBlank(message = "聚合商id不能为空")
    private String aggregatorId;

    @ApiModelProperty("数据集合")
    //@NotEmpty(message = "数据集合不能为空")
    //@Size(min = 96,max = 96,message = "数据集合必须包括96个时间点值")
    private List<AddPlanDataReq> dataList;


}
