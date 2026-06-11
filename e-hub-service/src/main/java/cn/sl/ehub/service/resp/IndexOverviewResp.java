package cn.sl.ehub.service.resp;

import cn.sl.ehub.common.vo.DataResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 首页总览返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("首页总览返回实体")
public class IndexOverviewResp {

    @ApiModelProperty("收益时间")
    private String totalProfitTime;
    @ApiModelProperty("总收益")
    private Double totalProfit;
    @ApiModelProperty("聚合申报功率")
    private List<DataResp> issueChart;
    @ApiModelProperty("碳因子曲线")
    private List<DataResp> crChart;
    @ApiModelProperty("调度下发功率曲线:电网下发dap曲线")
    private List<DataResp> dapChart;
    @ApiModelProperty(value = "设备有效功率")
    private List<DataResp> issueUseChart;
    @ApiModelProperty("实际功率曲线")
    private List<DataResp> powerChart;
    @ApiModelProperty("用户申报功率曲线")
    private List<DataResp> deliveryChart;
    @ApiModelProperty("申报价格")
    private List<DataResp> deliveryPrice;
    @ApiModelProperty("出清价格")
    private List<DataResp> issuePrice;
    @ApiModelProperty("颜色标记")
    private List<List<IndexOverviewTimeColorResp>> timeColorRespList;
    @ApiModelProperty("总览时间轴")
    private List<String> timeList;

    @ApiModelProperty("基线曲线")
    private List<DataResp> baseLineChart;
    @ApiModelProperty("火电平均负荷功率曲线")
    private List<DataResp> avgRtChart;
}
