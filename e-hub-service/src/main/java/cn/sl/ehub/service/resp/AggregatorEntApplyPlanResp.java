package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 企业用户申报计划返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("企业用户申报计划返回实体")
public class AggregatorEntApplyPlanResp {

    @ApiModelProperty("主键ID")
    private Integer id;
    @ApiModelProperty("聚合商ID")
    private String aggregatorId;
    @ApiModelProperty("企业ID")
    private String entId;
    @ApiModelProperty("申报计划开始时间")
    private String startDate;
    @ApiModelProperty("申报计划结束时间")
    private String endDate;
    @ApiModelProperty("申报时间")
    private String applyTime;
    @ApiModelProperty("计划状态 false默认计划 true临时计划")
    private Boolean planStatus;
    @ApiModelProperty("时间显示")
    private String showDate;
    @ApiModelProperty("调节状态 false不参与 true参与")
    private Boolean status;
    @ApiModelProperty("展示计划状态 0=全部计划不包含默认，1=默认计划，2=已完成计划，3=当前计划，4=未开始计划")
    private String showPlanStatus;
    @ApiModelProperty("展示计划排序")
    private String showPlanSort;
    @ApiModelProperty("设备列表")
    List<AppApplyIndexDeviceDetailResp> appApplyIndexDeviceDetailRespList;
}
