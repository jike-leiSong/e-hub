package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "获取用户本日运行计划入参")
public class RunPlanTodayReq {
    @ApiModelProperty(value = "日期", notes = "yyyy-MM-dd", example = "2023-11-03", required = true)
    @NotBlank(message = "结束时间不能为空")
    private String dateTime;
    @ApiModelProperty(value = "企业ID", required = true)
    private String entId;
    @ApiModelProperty(value = "资源类型ID", required = true)
    private String resourceTypeId;
}
