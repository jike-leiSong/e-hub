package cn.sl.ehub.console.model.vo;

import cn.sl.ehub.service.resp.IndexOverviewTimeColorResp;
import cn.sl.ehub.common.vo.DataResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Description: 用户完成调节情况返回结果
 * @Author sl
 * @Date 2026-05-28
 */
@Data
@ApiModel(value = "用户完成调节情况曲线图返回结果")
public class HistoryQueryGraphVO {

    @ApiModelProperty(value = "收益", notes = "收益")
    private String profit;
    @ApiModelProperty(value = "收益单位", notes = "收益单位")
    private String profitUnit;
    @ApiModelProperty(value = "设备实际功率", notes = "设备实际功率")
    private List<DataResp> actualPower;
    @ApiModelProperty(value = "设备分解后功率", notes = "设备分解后功率")
    private List<DataResp> resolvedPower;
    @ApiModelProperty(value = "设备有效功率")
    private List<DataResp> effectivePower;
    @ApiModelProperty(value = "颜色填充时间段", notes = "起止时间用#拼接")
    private List<List<IndexOverviewTimeColorResp>> fillColor;

    @ApiModelProperty(value = "基线")
    private List<DataResp> baseLineChart;

    @ApiModelProperty(value = "实际功率曲线")
    private List<DataResp> powerChart;

    @ApiModelProperty(value = "有效调节功率")
    private List<DataResp> adjustPower;


}
