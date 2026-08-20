package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("电价规则导入预览")
public class TariffRulePreviewResp {

    @ApiModelProperty("首个版本")
    private String version;

    @ApiModelProperty("写入版本列表")
    private List<String> versions;

    @ApiModelProperty("省份编码")
    private String provinceCode;

    @ApiModelProperty("省份名称")
    private String provinceName;

    @ApiModelProperty("二级分类")
    private String secondType;

    @ApiModelProperty("三级分类")
    private String thirdType;

    @ApiModelProperty("是否有效")
    private Boolean valid;

    @ApiModelProperty("时段 96 点")
    private List<TariffRulePointResp> fpgjPoints;

    @ApiModelProperty("价格行预览")
    private List<TariffRulePricePreviewResp> priceRows;

    @ApiModelProperty("峰谷时段点数")
    private Integer fpgjPointCount;

    @ApiModelProperty("价格主表行数")
    private Integer priceRowCount;

    @ApiModelProperty("价格明细点数")
    private Integer pricePointCount;
}
