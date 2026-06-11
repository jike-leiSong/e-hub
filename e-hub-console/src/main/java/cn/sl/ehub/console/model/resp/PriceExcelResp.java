package cn.sl.ehub.console.model.resp;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 出清价格导出
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("出清价格导出")
public class PriceExcelResp {

    @ApiModelProperty("时间")
    @Excel(name = "时间", width = 24)
    private String time;
    @ApiModelProperty("出清价格")
    @Excel(name = "出清价格", width = 12)
    private String issuePrice;
    @ApiModelProperty("申报价格")
    @Excel(name = "申报价格", width = 12)
    private String deliveryPrice;
    @ApiModelProperty("火电平均功率")
    @Excel(name = "火电平均功率", width = 12)
    private String avgPower;
}
