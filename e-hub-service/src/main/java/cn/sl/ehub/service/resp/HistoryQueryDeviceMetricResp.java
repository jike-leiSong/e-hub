package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 历史查询设备运行测点
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("历史查询设备运行测点")
public class HistoryQueryDeviceMetricResp {

    @ApiModelProperty("测点编码")
    private String metricCode;
    @ApiModelProperty("测点名称")
    private String metricName;
}
