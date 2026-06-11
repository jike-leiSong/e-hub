package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 企业用户社会责任返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("企业用户社会责任返回实体")
public class AggregatorEntSocialResponsibilityResp {

    @ApiModelProperty("清洁电量")
    private String cleanPower;
    @ApiModelProperty("减排CO2量")
    private String co2;
    @ApiModelProperty("植树量")
    private String tree;
    @ApiModelProperty("节约标准煤量")
    private String coal;
    @ApiModelProperty("减排SO2量")
    private String so2;
    @ApiModelProperty("减排氮氧化物量")
    private String nox;
}
