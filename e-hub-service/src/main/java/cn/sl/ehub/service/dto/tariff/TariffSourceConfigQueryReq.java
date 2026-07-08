package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("电价数据来源配置查询请求")
public class TariffSourceConfigQueryReq {

    @ApiModelProperty("省份编码")
    private String provinceCode;

    @ApiModelProperty("来源类型")
    private String sourceType;

    @ApiModelProperty("来源名称")
    private String sourceName;

    @ApiModelProperty("是否启用")
    private Integer enabled;
}
