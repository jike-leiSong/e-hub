package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 收益账单详情返回结果
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("收益账单详情返回结果")
public class ProfitBillDetailDateResp {

    @ApiModelProperty("日期")
    private String date;
    @ApiModelProperty("收益")
    private Double profit;
    @ApiModelProperty("电量")
    private Double electricQuantity;
    @ApiModelProperty("出清价格")
    private Double price;
    @ApiModelProperty("出清金额")
    private Double money;
}
