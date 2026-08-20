package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("电价规则导入请求")
public class TariffRuleImportReq {

    @ApiModelProperty("生效类型 MONTH/DAY/RANGE")
    private String effectiveType;

    @ApiModelProperty("电价月份 yyyy-MM")
    private String yearMonth;

    @ApiModelProperty("单日日期 yyyy-MM-dd")
    private String selectedDate;

    @ApiModelProperty("区间开始日期 yyyy-MM-dd")
    private String startDate;

    @ApiModelProperty("区间结束日期 yyyy-MM-dd")
    private String endDate;

    @ApiModelProperty("直接指定版本，优先级最高")
    private String version;

    @ApiModelProperty("省份编码")
    private String provinceCode;

    @ApiModelProperty("省份名称")
    private String provinceName;

    @ApiModelProperty("二级分类")
    private String secondType;

    @ApiModelProperty("三级分类")
    private String thirdType;

    @ApiModelProperty("操作人")
    private String operatorName;

    @ApiModelProperty("时段规则")
    private List<TariffRulePeriodReq> periods;

    @ApiModelProperty("价格行")
    private List<TariffRulePriceRowReq> priceRows;
}
