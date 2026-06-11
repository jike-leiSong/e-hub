package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 收益账单返回结果
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("收益账单返回结果")
public class ProfitBillDateResp {

    @ApiModelProperty("总收益")
    private Double totalProfit;
    @ApiModelProperty("收益详情")
    private List<ProfitBillDetailDateResp> profitBillDetailDateRespList;
}
