package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("电价规则删除请求")
public class TariffRuleDeleteReq {

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

    @ApiModelProperty("二级分类")
    private String secondType;

    @ApiModelProperty("三级分类")
    private String thirdType;
}
