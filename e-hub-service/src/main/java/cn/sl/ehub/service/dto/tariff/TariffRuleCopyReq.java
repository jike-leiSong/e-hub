package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("电价规则复制请求")
public class TariffRuleCopyReq {

    @ApiModelProperty("来源版本，支持 yyMM/yyMMdd/yyyy-MM/yyyy-MM-dd")
    private String sourceVersion;

    @ApiModelProperty("来源省份编码")
    private String sourceProvinceCode;

    @ApiModelProperty("来源省份名称")
    private String sourceProvinceName;

    @ApiModelProperty("来源二级分类")
    private String sourceSecondType;

    @ApiModelProperty("来源三级分类")
    private String sourceThirdType;

    @ApiModelProperty("目标生效类型 MONTH/DAY/RANGE")
    private String targetEffectiveType;

    @ApiModelProperty("目标月份 yyyy-MM")
    private String targetYearMonth;

    @ApiModelProperty("目标单日 yyyy-MM-dd")
    private String targetSelectedDate;

    @ApiModelProperty("目标开始日期 yyyy-MM-dd")
    private String targetStartDate;

    @ApiModelProperty("目标结束日期 yyyy-MM-dd")
    private String targetEndDate;

    @ApiModelProperty("目标省份编码，空则沿用来源")
    private String targetProvinceCode;

    @ApiModelProperty("目标省份名称，空则沿用来源")
    private String targetProvinceName;

    @ApiModelProperty("目标二级分类，空则沿用来源")
    private String targetSecondType;

    @ApiModelProperty("目标三级分类，空则沿用来源")
    private String targetThirdType;
}
