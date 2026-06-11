package cn.sl.ehub.service.resp;

import cn.sl.ehub.common.vo.DataResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 聚合商组合曲线
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel(value = "聚合商组合曲线")
public class AggregatorDateDeliveryChartResp {

    @ApiModelProperty("资源类型ID")
    private String resourceTypeId;
    @ApiModelProperty("日期")
    private String date;
    @ApiModelProperty("数据")
    private List<DataResp> dataRespList;
}
