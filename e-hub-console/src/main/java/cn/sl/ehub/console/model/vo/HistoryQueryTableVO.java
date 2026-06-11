package cn.sl.ehub.console.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.TreeMap;

/**
 * @Description: 用户完成调节情况返回结果
 * @Author sl
 * @Date 2026-05-28
 */
@Data
@ApiModel(value = "用户完成调节情况详情表返回结果")
public class HistoryQueryTableVO {

    @ApiModelProperty(value = "时间", notes = "2020/10/15 07:00 ~ 07:15")
    private String time;
    @ApiModelProperty(value = "设备名称", notes = "1#设备")
    private String deviceName;
    @ApiModelProperty(value = "申报调节功率")
    private String applyPower;
    @ApiModelProperty(value = "分解调节功率")
    private Double issuePower;
    @ApiModelProperty(value = "实际调节功率")
    private Double actualPower;
    @ApiModelProperty(value = "有效负荷")
    private Double usePower;
    @ApiModelProperty(value = "收益", notes = "收益")
    private String profit;
    @ApiModelProperty(value = "功率单位", notes = "kW/mW")
    private String powerUnit;
    @ApiModelProperty(value = "收益单位", notes = "元/万元")
    private String profitUnit;
}
