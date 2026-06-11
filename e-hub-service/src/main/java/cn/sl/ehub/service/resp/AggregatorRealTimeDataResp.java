package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 聚合商实时数据查询返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("聚合商实时数据查询返回实体")
public class AggregatorRealTimeDataResp {

    @ApiModelProperty("设备ID")
    private String deviceBaseId;
    @ApiModelProperty("时间")
    private String time;
    @ApiModelProperty("数据值")
    private Double value;
}
