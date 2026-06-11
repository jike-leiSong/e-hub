package cn.sl.ehub.console.model.vo;

import cn.sl.ehub.common.vo.DataResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Description: 曲线属性对象
 * @Author sl
 * @Date 2026-05-28
 */
@Data
@ApiModel(value = "曲线属性对象")
public class LineDataGraphVO {

    @ApiModelProperty(value = "单位集合", notes = "#隔开")
    private String unitSet;
    @ApiModelProperty(value = "曲线单位")
    private String lineUnit;
    @ApiModelProperty(value = "曲线名称")
    private String lineName;
    @ApiModelProperty(value = "曲线对象")
    private LineDataVO lineData;
    @ApiModelProperty(value = "曲线值")
    private List<DataResp> dataRespList;
    @ApiModelProperty(value = "分组编码")
    private String groupCode;
    @ApiModelProperty(value = "分组名称")
    private String groupName;
    @ApiModelProperty(value = "曲线图表名称")
    private String chartName;
}
