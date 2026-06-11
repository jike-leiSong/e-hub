package cn.sl.ehub.console.model.resp;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 收益结算
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("收益结算")
public class HistoryProfitCalculationTimeExcelResp {

    @ApiModelProperty("日期")
    private String date;
    @ApiModelProperty("时间")
    @Excel(name = "时间")
    private String time;
    @ApiModelProperty("有效调节电量(kWh)")
    @Excel(name = "有效调节电量(kWh)")
    private Double electricQuantity;
    @ApiModelProperty("出清价格(元/kWh)")
    @Excel(name = "出清价格(元/kWh)")
    private Double offer;
    @ApiModelProperty("电网下发收益(元)")
    @Excel(name = "电网下发收益(元)")
    private Double issueProfit;
    @ApiModelProperty("用户收益(元)")
    @Excel(name = "用户收益(元)")
    private Double entProfit;
}
