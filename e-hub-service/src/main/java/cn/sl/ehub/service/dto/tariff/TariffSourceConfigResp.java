package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("电价数据来源配置响应")
public class TariffSourceConfigResp {

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("省份编码")
    private String provinceCode;

    @ApiModelProperty("省份名称")
    private String provinceName;

    @ApiModelProperty("来源名称")
    private String sourceName;

    @ApiModelProperty("来源类型")
    private String sourceType;

    @ApiModelProperty("来源页面")
    private String sourceUrl;

    @ApiModelProperty("发布日期规律")
    private String publishRule;

    @ApiModelProperty("是否启用")
    private Integer enabled;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    private String createTime;

    @ApiModelProperty("更新时间")
    private String updateTime;
}
