package cn.sl.ehub.service.req;

import cn.sl.ehub.service.resp.AppApplyIndexDeviceDetailResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 企业用户申报计划请求实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("企业用户申报计划请求实体")
public class AggregatorEntApplyPlanReq {

    @ApiModelProperty("聚合商ID")
    private String aggregatorId;
    @ApiModelProperty("企业ID")
    private String entId;
    @ApiModelProperty("申报计划开始时间")
    private String startDate;
    @ApiModelProperty("申报计划结束时间")
    private String endDate;
    @ApiModelProperty("计划状态 false默认计划 true临时计划")
    private Boolean planStatus;
    @ApiModelProperty("调节状态 false不参与 true参与")
    private Boolean status;
    @ApiModelProperty("保存状态 0暂存 1提交")
    private Boolean saveStatus;
    @ApiModelProperty("设备列表")
    List<AppApplyIndexDeviceDetailResp> appApplyIndexDeviceDetailRespList;
}
