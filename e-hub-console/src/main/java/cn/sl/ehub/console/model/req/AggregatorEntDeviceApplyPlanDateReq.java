package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 申报日期请求实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel(value = "申报日期请求实体")
public class AggregatorEntDeviceApplyPlanDateReq {

    @ApiModelProperty("日期")
    private String date;
    @ApiModelProperty("申报设备请求实体")
    private List<AggregatorEntDeviceApplyPlanDeviceReq> deviceList;
}
