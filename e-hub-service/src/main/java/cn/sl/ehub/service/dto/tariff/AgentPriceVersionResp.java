package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("代理电价版本响应")
public class AgentPriceVersionResp {

    @ApiModelProperty("内部版本")
    private String version;

    @ApiModelProperty("电费年月 yyyy-MM")
    private String yearMonth;

    @ApiModelProperty("省份编码")
    private String provinceCode;

    @ApiModelProperty("省份名称")
    private String provinceName;

    @ApiModelProperty("版本状态")
    private String status;

    @ApiModelProperty("生效开始日期")
    private String effectiveStart;

    @ApiModelProperty("生效结束日期")
    private String effectiveEnd;

    @ApiModelProperty("发布时间")
    private String publishTime;
}
