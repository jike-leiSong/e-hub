package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 每月各企业有效调节电量和收益
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("每月各企业有效调节电量和收益")
public class EntMonthElectricProfitResp {

    @ApiModelProperty("企业ID")
    private String entId;
    @ApiModelProperty("企业名称")
    private String entName;
    @ApiModelProperty("月份")
    private String month;
    @ApiModelProperty("用效调节电量")
    private Double electric;
    @ApiModelProperty("收益")
    private Double profit;
}
