package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("代理电价字典项")
public class AgentPriceDictItemResp {

    @ApiModelProperty("标签")
    private String label;

    @ApiModelProperty("值")
    private String value;
}
