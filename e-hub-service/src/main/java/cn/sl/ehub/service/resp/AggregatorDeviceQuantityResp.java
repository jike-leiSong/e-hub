package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 设备电量详情
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("设备电量详情")
public class AggregatorDeviceQuantityResp {

    @ApiModelProperty("企业用户ID")
    private String entId;
    @ApiModelProperty("资源类型ID")
    private String resourceTypeId;
    @ApiModelProperty("设备ID")
    private String deviceBaseId;
    @ApiModelProperty("日期")
    private String date;
    @ApiModelProperty("开始时间")
    private String startTime;
    @ApiModelProperty("结束时间")
    private String endTime;
    @ApiModelProperty("申报调节功率")
    private Double deliveryPower;
    @ApiModelProperty("实际调节功率")
    private Double reallyPower;
    @ApiModelProperty("基线负荷")
    private Double baseLinePower;
    @ApiModelProperty("预计调节电量")
    private Double totalQuantity;
    @ApiModelProperty("实际调节电量")
    private Double finishQuantity;
}
