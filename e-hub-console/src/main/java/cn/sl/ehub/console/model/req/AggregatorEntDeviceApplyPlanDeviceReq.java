package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 申报设备请求实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel(value = "申报设备请求实体")
public class AggregatorEntDeviceApplyPlanDeviceReq {

    @ApiModelProperty("资源类型ID")
    private String resourceTypeId;
    @ApiModelProperty("设备ID")
    private String deviceBaseId;
    @ApiModelProperty("申报详情请求实体")
    private List<AggregatorEntDeviceApplyPlanDetailReq> timeList;
}
