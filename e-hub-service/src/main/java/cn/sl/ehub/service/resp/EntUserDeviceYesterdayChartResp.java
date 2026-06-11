package cn.sl.ehub.service.resp;

import cn.sl.ehub.common.vo.DataResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 企业用户设备昨日曲线返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("企业用户设备昨日曲线返回实体")
public class EntUserDeviceYesterdayChartResp {

    @ApiModelProperty("设备基线负荷")
    private List<DataResp> baseLineChart;
    @ApiModelProperty("调度下发功率曲线")
    private List<DataResp> issueChart;
    @ApiModelProperty("实际功率曲线")
    private List<DataResp> powerChart;
    @ApiModelProperty("颜色标记")
    private List<List<IndexOverviewTimeColorResp>> timeColorRespList;
}
