package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 调峰辅助服务设备详情
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("调峰辅助服务设备详情")
public class AppApplyIndexDeviceDetailResp {

    @ApiModelProperty("资源类型ID")
    private String resourceTypeId;
    @ApiModelProperty("设备ID")
    private String deviceBaseId;
    @ApiModelProperty("设备名称")
    private String deviceName;
    @ApiModelProperty("设备功率")
    private Double devicePower;
    @ApiModelProperty("调峰辅助服务设备时间详情")
    private List<AppApplyIndexDeviceTimeDetailResp> timeList;
}
