package cn.sl.ehub.service.resp;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;

/**
 * 计划详情数据返回实体
 *
 * @author sl
 * @classes cn.sl.ehub.upstream.resp.PlanDetailDataResp
 * @date 2026-05-28
 */
@Data
@ApiModel("计划详情数据返回实体")
public class PlanDetailDataResp {

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
