package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 申报请求实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel(value = "申报请求实体")
public class AggregatorEntDeviceApplyPlanReq {

    @ApiModelProperty("企业ID")
    private String entId;
    @ApiModelProperty("企业编码")
    private String stationId;
    @ApiModelProperty("申报日期请求实体")
    private List<AggregatorEntDeviceApplyPlanDateReq> detailList;
}
