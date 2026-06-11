package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "用户完成调节情况图入参")
public class NewUserAdjustmentGraphReq {

    @ApiModelProperty(value = "开始时间", notes = "yyyy-MM-dd", example = "2020-11-03", required = true)
    @NotBlank(message = "开始时间不能为空")
    private String startTime;
    @ApiModelProperty(value = "结束时间", notes = "yyyy-MM-dd", example = "2020-11-03", required = true)
    @NotBlank(message = "结束时间不能为空")
    private String endTime;
    @ApiModelProperty(value = "企业ID", required = true)
    private String subEntId;
    @ApiModelProperty(value = "资源类型ID", required = true)
    private String resourceTypeId;
//    @ApiModelProperty(value = "设备ID", required = true)
//    @NotBlank(message = "设备ID不能为空")
//    private String deviceBaseId;
//    @ApiModelProperty("设备编码")
//    private String deviceId;

}
