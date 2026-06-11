package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 设备启停计划详情
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("设备启停计划")
public class AggregatorEntDateDeviceStartStopPlanDetailResp {

    @ApiModelProperty("资源类型ID")
    private String resourceTypeId;
    @ApiModelProperty("设备ID")
    private String deviceBaseId;
    @ApiModelProperty("时间")
    private String time;
    @ApiModelProperty("设备启停详情")
    private String detail;
}
