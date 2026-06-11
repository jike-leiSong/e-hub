package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 调峰辅助服务设备时间详情
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("调峰辅助服务设备时间详情")
public class AppApplyIndexDeviceTimeDetailResp {

    @ApiModelProperty("开始时间")
    private String startTime;
    @ApiModelProperty("结束时间")
    private String endTime;
    @ApiModelProperty("计划功率")
    private Double power;
    @ApiModelProperty("充放电状态 -1充电 0默认 1放电")
    private Integer useStatus;
}
