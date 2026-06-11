package cn.sl.ehub.service.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 新增计划数据请求实体
 *
 * @author sl
 * @classes ccn.sl.ehub.upstream.req.AddPlanDataReq
 * @date 2026-05-28
 */
@Data
@ApiModel("新增计划数据请求实体")
public class AddPlanDataReq {

    @ApiModelProperty("时段")
    private String dateTime;

    @ApiModelProperty("参考日功率")
    private String referDatePower;


    @ApiModelProperty("调整系数")
    private String adjustFactor;


    @ApiModelProperty("调整值")
    private String adjustValue;

    @ApiModelProperty("申报功率")
    private String applyPower;

    @ApiModelProperty("申报价格")
    private String applyPrice;


}
