package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("电价规则时段请求")
public class TariffRulePeriodReq {

    @ApiModelProperty("时段类型：尖/峰/平/谷/深谷")
    private String periodType;

    @ApiModelProperty("时间段列表，例如 08:00-10:00")
    private List<String> ranges;

    @ApiModelProperty("时间段文本，例如 08:00-10:00, 14:00-17:00")
    private String rangeText;
}
