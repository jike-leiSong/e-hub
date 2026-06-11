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
public class HistoryProfitCalculationExcelResp {

    @ApiModelProperty("日期")
    @Excel(name = "日期")
    private String date;
    @ApiModelProperty("总有效调节电量(kWh)")
    @Excel(name = "总有效调节电量(kWh)")
    private Double electricQuantity;
    @ApiModelProperty("度电收益(元/kWh)")
    @Excel(name = "度电收益(元/kWh)")
    private Double electricOffer;
    @ApiModelProperty("平均出清价格(元/kWh)")
    @Excel(name = "平均出清价格(元/kWh)")
    private Double offer;
    @ApiModelProperty("电网下发收益(元)")
    @Excel(name = "电网下发收益(元)")
    private Double issueProfit;
    @ApiModelProperty("用户收益(元)")
    @Excel(name = "用户收益(元)")
    private Double entProfit;
}
