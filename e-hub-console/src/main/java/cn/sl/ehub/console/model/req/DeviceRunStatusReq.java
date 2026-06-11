package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "设备运行情况入参")
public class DeviceRunStatusReq {

    @ApiModelProperty(value = "开始时间", notes = "yyyy-MM-dd", example = "2020-11-03")
    private String startTime;
    @ApiModelProperty(value = "结束时间", notes = "yyyy-MM-dd", example = "2020-11-03")
    private String endTime;
    @ApiModelProperty(value = "聚合商id", notes = "聚合商id", example = "")
    private String aggregatorId;
    @ApiModelProperty(value = "子企业id", notes = "", example = "")
    private String subEntId;
    @ApiModelProperty(value = "资源类型ID", notes = "", example = "")
    private String resourceTypeId;
    @ApiModelProperty(value = "设备id列表", notes = "分单设备和多设备", example = "")
    private List<String> deviceIdList;
    @ApiModelProperty(value = "cim设备id列表", notes = "分单设备和多设备", example = "")
    private List<String> deviceBaseIdList;
    @ApiModelProperty(value = "指标", notes = "单设备多指标，多设备单指标", example = "")
    private List<String> metricList;
    @ApiModelProperty(value = "状态 0单设备多指标 1多设备单指标", notes = "状态 0单设备多指标 1多设备单指标", example = "")
    private String status;
}
