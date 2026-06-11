package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
@Data
@ApiModel("用户调节详情")
public class AggregatorEntDateAdjustResp {
    @ApiModelProperty("聚合商ID")
    private String aggregatorId;
    @ApiModelProperty("企业用户ID")
    private String entId;
    @ApiModelProperty("资源类型ID")
    private String resourceTypeId;
    @ApiModelProperty("日期")
    private String date;
    @ApiModelProperty("开始时间")
    private String startTime;
    @ApiModelProperty("结束时间")
    private String endTime;
    @ApiModelProperty("实际调节功率")
    private Double reallyPower;
    @ApiModelProperty("最小功率")
    private Double minPower;
    @ApiModelProperty("基线负荷")
    private Double baseLinePower;
    @ApiModelProperty("计算负荷")
    private Double countPower;
    @ApiModelProperty("用电量")
    private Double electricQuantity;
    @ApiModelProperty("用电量")
    private Double countElectricQuantity;
    @ApiModelProperty("收益")
    private Double profit;
    @ApiModelProperty("收益是否有效 1有效 2无效")
    private String profitStatus;
    @ApiModelProperty("出清价格")
    private Double countPrice;
    @ApiModelProperty("功率占比")
    private Double powerPercent;
}
