package cn.sl.ehub.upstream.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Description: 单体模型数据接入详情ftl模板
 * @Author sl
 * @Date 2026-05-28
 */
@ApiModel("单体测量数据接入详情ftl模板")
@Data
public class SingleMeasDetailTemplateVO {

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "用户有功")
    private String power;

    @ApiModelProperty(value = "用户无功")
    private String noPower;

    @ApiModelProperty(value = "用户电流")
    private String electricity;

    @ApiModelProperty(value = "用户当日零点电量")
    private String electricQuantity;

    @ApiModelProperty(value = "运营系统内部用户ID")
    private String innerUserId;


}
