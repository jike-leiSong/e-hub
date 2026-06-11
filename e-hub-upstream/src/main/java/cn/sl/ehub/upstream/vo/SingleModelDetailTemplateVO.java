package cn.sl.ehub.upstream.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Description: 单体模型数据接入详情ftl模板
 * @Author sl
 * @Date 2026-05-28
 */
@ApiModel("单体模型数据接入详情ftl模板")
@Data
public class SingleModelDetailTemplateVO {

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "容量")
    private String capacity;

    @ApiModelProperty(value = "所在区域")
    private String region;

    @ApiModelProperty(value = "用户类型")
    private String userType;

    @ApiModelProperty(value = "业主方")
    private String proprietor;

    @ApiModelProperty(value = "运营系统内部用户ID")
    private String innerUserId;


}
