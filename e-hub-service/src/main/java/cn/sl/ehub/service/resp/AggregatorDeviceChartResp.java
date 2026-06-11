package cn.sl.ehub.service.resp;

import cn.sl.ehub.common.vo.DataResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 设备图表实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("设备图表实体")
public class AggregatorDeviceChartResp {

    @ApiModelProperty("资源类型ID")
    private String resourceTypeId;
    @ApiModelProperty("设备主键ID")
    private String deviceBaseId;
    @ApiModelProperty("设备名称")
    private String deviceName;
    @ApiModelProperty("设备编码")
    private String deviceId;
    @ApiModelProperty("调度下发功率曲线")
    private List<DataResp> issueChart;
    @ApiModelProperty("调度申报功率曲线")
    private List<DataResp> deliveryChart;
    @ApiModelProperty("实际功率曲线")
    private List<DataResp> powerChart;
    @ApiModelProperty("有效功率曲线")
    private List<DataResp> issueUseChart;
    @ApiModelProperty("基线负荷曲线")
    private List<DataResp> baseLineChartList;
    @ApiModelProperty("业务域")
    private String dataSource;
    @ApiModelProperty("设备类型")
    private String deviceType;
    @ApiModelProperty("企业站ID")
    private String stationId;
}
