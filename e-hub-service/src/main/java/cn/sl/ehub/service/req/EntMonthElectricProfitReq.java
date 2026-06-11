package cn.sl.ehub.service.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 每月各企业有效调节电量和收益请求实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("每月各企业有效调节电量和收益请求实体")
public class EntMonthElectricProfitReq {

    @ApiModelProperty("企业ID列表")
    private List<String> entIdList;
    @ApiModelProperty("月份")
    private String month;
}
