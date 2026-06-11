package cn.sl.ehub.service.resp;

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
@ApiModel("设备执行记录")
public class AggregatorEntDeviceIotLogResp implements Comparable<AggregatorEntDeviceIotLogResp> {

    @ApiModelProperty("设备名称")
    private String deviceName;
    @ApiModelProperty("执行结果")
    private String resultMsg;
    @ApiModelProperty("下发时间")
    private String sendTime;

    @Override
    public int compareTo(AggregatorEntDeviceIotLogResp resp) {
        if (null != resp) {
            if (null == this.sendTime) {
                this.sendTime = "";
            }
            if (null == resp.getSendTime()) {
                resp.setSendTime("");
            }
            return this.sendTime.compareTo(resp.getSendTime());
        }
        return 0;
    }
}
