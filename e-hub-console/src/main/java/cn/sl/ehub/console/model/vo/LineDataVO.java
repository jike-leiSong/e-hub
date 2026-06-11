package cn.sl.ehub.console.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: 曲线对象
 * @Author sl
 * @Date 2026-05-28
 */

@Data
@ApiModel(value = "曲线对象")
@AllArgsConstructor
@NoArgsConstructor
public class LineDataVO {

    @ApiModelProperty(value = "时间轴列表")
    private List<String> timeList;

    @ApiModelProperty(value = "值列表")
    private List<String> valueList;

}
