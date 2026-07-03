package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("代理电价版本查询请求")
public class AgentPriceVersionQueryReq {

    @ApiModelProperty("省份编码")
    private String provinceCode;

    @ApiModelProperty("电费年月 yyyy-MM")
    private String yearMonth;

    @ApiModelProperty("版本状态")
    private String status;
}
