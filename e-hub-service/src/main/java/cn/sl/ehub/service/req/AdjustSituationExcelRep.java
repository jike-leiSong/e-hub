package cn.sl.ehub.service.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 导出调节效果excel请求参数
 *
 * @author sl
 * @classes cn.sl.ehub.upstream.req.AdjustSituationExcelRep
 * @date 2026-05-28
 */
@Data
public class AdjustSituationExcelRep {


    @ApiModelProperty("聚合商id")
    @NotBlank(message = "聚合商id不能为空")
    private String aggregatorId;


    @ApiModelProperty("企业id 导出用户数据时必传")
    private String entId;


    @ApiModelProperty("资源类型id")
    @NotBlank(message = "资源类型id不能为空")
    private String sourceId;


    @ApiModelProperty("开始时间")
    @NotBlank(message = "开始时间不能为空")
    private String startDate;


    @ApiModelProperty("结束时间")
    @NotBlank(message = "结束时间不能为空")
    private String endDate;




}
