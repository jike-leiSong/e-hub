package cn.sl.ehub.console.model.vo;

import cn.sl.ehub.common.vo.DataResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
@Data
@ApiModel(value = "用户运行情况曲线图返回结果")
public class RunPlanTodayVO {
    @ApiModelProperty(value = "实际功率曲线")
    private List<DataResp> dapChart;
}
