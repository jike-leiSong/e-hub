package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("电价规则发布结果")
public class TariffRulePublishResp {

    @ApiModelProperty("写入版本列表")
    private List<String> versions;

    @ApiModelProperty("峰谷时段主表行数")
    private Integer fpgjRowCount;

    @ApiModelProperty("峰谷时段明细点数")
    private Integer fpgjPointCount;

    @ApiModelProperty("价格主表行数")
    private Integer priceRowCount;

    @ApiModelProperty("价格明细点数")
    private Integer pricePointCount;
}
