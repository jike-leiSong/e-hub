package cn.sl.ehub.service.resp;

import cn.sl.ehub.common.vo.DataResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 企业用户设备明日曲线返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("企业用户设备明日曲线返回实体")
public class EntUserDeviceTomorrowChartResp {

    @ApiModelProperty("用户申报功率")
    private List<DataResp> deliveryChart;
    @ApiModelProperty("分解后设备功率")
    private List<DataResp> issueChart;
}
