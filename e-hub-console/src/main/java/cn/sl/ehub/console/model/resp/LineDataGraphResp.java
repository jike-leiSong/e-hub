package cn.sl.ehub.console.model.resp;

import cn.sl.ehub.console.model.vo.LineDataGraphVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 设备运行历史查询返回曲线对象
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel(value = "设备运行历史查询返回曲线对象")
public class LineDataGraphResp {

    @ApiModelProperty(value = "单位集合", notes = "#隔开")
    private String unit;
    @ApiModelProperty(value = "图表名称", notes = "#隔开")
    private String chartName;
    @ApiModelProperty(value = "曲线属性对象")
    List<LineDataGraphVO> lineDataGraphVOList;
}
