package cn.sl.ehub.console.model.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 出清价格日期导出
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("出清价格日期导出")
public class PriceExcelDateResp {

    @ApiModelProperty("日期")
    private String date;
    @ApiModelProperty("出清价格导出")
    private List<PriceExcelResp> priceExcelRespList;
}
