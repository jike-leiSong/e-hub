package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "用户完成调节情况表入参")
public class UserAdjustmentTableReq {

    @ApiModelProperty(value = "收益类型", notes = "空-全部，1-有效，0-无效", example = "0")
    private Integer profitType;
    @ApiModelProperty(value = "页码", notes = "1", example = "1")
    @NotNull(message = "页码不能为空")
    private Integer pageNo;
    @ApiModelProperty(value = "页大小", notes = "10", example = "10")
    @NotNull(message = "页大小不能为空")
    private Integer pageSize;
    @ApiModelProperty(value = "开始时间", notes = "yyyy-MM-dd", example = "2020-11-02")
    @NotBlank(message = "开始时间不能为空")
    private String startTime;
    @ApiModelProperty(value = "结束时间", notes = "yyyy-MM-dd", example = "2020-11-03")
    @NotBlank(message = "结束时间不能为空")
    private String endTime;
    @ApiModelProperty(value = "聚合商id", notes = "聚合商id", example = "")
    private String aggregatorId;
    @ApiModelProperty(value = "子企业id", notes = "", example = "")
    private String subEntId;
    @ApiModelProperty("资源类型ID")
    private String resourceTypeId;
    @ApiModelProperty(value = "设备id", notes = "为空时查全部", example = "")
    private String deviceId;
    @ApiModelProperty("设备ID")
    @NotBlank(message = "设备ID不能为空")
    private String deviceBaseId;
}
