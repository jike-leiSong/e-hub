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
public class GuangzhouProfitCalculationTimeExcelResp {

    @ApiModelProperty("日期")
    private String date;

    @ApiModelProperty("sheet名称 日期+邀约类型+数字")
    private String sheetId;

    @ApiModelProperty("时间")
    @Excel(name = "时间")
    private String time;
    @ApiModelProperty(name = "电网下发收益收益")
    @Excel(name = "电网下发收益(元)", width = 20.0)
    private Double profit;
    @ApiModelProperty("邀约内容")
    private String content;
    @ApiModelProperty("度电收益(元/kWh)")
    @Excel(name = "度电收益(元/kWh)", width = 20.0)
    private Double electricOffer;
    @Excel(name = "平均出清价格(元/kWh)", width = 20.0)
    @ApiModelProperty("平均出清价格(元/kWh)")
    private Double averagePrice;
    @ApiModelProperty("用户收益(元)")
    @Excel(name = "用户收益(元)", width = 20.0)
    private Double issueProfit;
}