package cn.sl.ehub.console.model.resp;/**
 * @ProjectName: load-aggregator
 * @Package: cn.sl.ehub.upstream.model.resp
 * @ClassName: GuangzhouProfitCalculationExcelResp
 * @Author sl
 * @Description: 收益结算
 * @Date 2026-05-28
 * @Version: 1.0
 */

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author sl
 * @date 2026-05-28
 */
@Data
@ApiModel("收益结算")
public class GuangzhouProfitCalculationExcelResp {

    @ApiModelProperty("日期")
    @Excel(name = "日期")
    private String date;

    @ApiModelProperty("总有效调节电量(kWh)")
    @Excel(name = "总有效调节电量(kWh)", width = 20.0)
    private Double electricQuantity;

    @ApiModelProperty("度电收益(元/kWh)")
    @Excel(name = "度电收益(元/kWh)", width = 20.0)
    private Double electricOffer;

    @ApiModelProperty("平均出清价格(元/kWh)")
    @Excel(name = "平均出清价格(元/kWh)", width = 20.0)
    private Double offer;

    @ApiModelProperty("电网下发收益(元)")
    @Excel(name = "电网下发收益(元)", width = 20.0)
    private Double issueProfit;

    @ApiModelProperty("用户收益(元)")
    @Excel(name = "用户收益(元)", width = 20.0)
    private Double entProfit;
}