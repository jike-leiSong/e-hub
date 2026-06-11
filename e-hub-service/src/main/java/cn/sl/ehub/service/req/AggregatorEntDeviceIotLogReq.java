package cn.sl.ehub.service.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 设备执行记录
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("设备执行保存记录")
public class AggregatorEntDeviceIotLogReq {

    @ApiModelProperty("设备名称")
    private String deviceBaseId;
    @ApiModelProperty("执行结果")
    private String resultMsg;
    @ApiModelProperty("下发时间")
    private String sendTime;
}
