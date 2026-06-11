package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("参考日功率数据实体")
public class ReferDatePowerDataResp {

    @ApiModelProperty("日期")
    private String date;

    @ApiModelProperty("功率值")
    private String value;
}
