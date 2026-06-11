package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 设备启停计划返回结果
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("设备启停计划返回结果")
public class AggregatorEntDateDeviceStartStopPlanResp {

    @ApiModelProperty("时间")
    private String time;
    @ApiModelProperty("计划")
    private List<String> contentList;
}
