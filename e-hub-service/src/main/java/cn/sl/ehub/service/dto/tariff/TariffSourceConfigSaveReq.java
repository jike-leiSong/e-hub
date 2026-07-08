package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("电价数据来源配置保存请求")
public class TariffSourceConfigSaveReq {

    @ApiModelProperty("主键ID，更新时必填")
    private Long id;

    @ApiModelProperty("省份编码")
    private String provinceCode;

    @ApiModelProperty("省份名称")
    private String provinceName;

    @ApiModelProperty("来源名称")
    private String sourceName;

    @ApiModelProperty("来源类型，NDRC/SGCC/CSG/POWER_EXCHANGE/SELLER/MANUAL/API")
    private String sourceType;

    @ApiModelProperty("来源页面")
    private String sourceUrl;

    @ApiModelProperty("发布日期规律")
    private String publishRule;

    @ApiModelProperty("是否启用")
    private Integer enabled;

    @ApiModelProperty("备注")
    private String remark;
}
