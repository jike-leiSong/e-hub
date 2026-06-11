package cn.sl.ehub.upstream.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @Description: 单体测量数据接入ftl模板
 * @Author sl
 * @Date 2026-05-28
 */
@ApiModel("单体模型数据接入ftl模板")
public class SingleMEASTemplateVO {

    @ApiModelProperty(value = "聚合商名称")
    private String company;

    @ApiModelProperty(value = "数据详情")
    private List<SingleMeasDetailTemplateVO> detailList;


}
