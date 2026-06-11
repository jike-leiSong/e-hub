package cn.sl.ehub.service.resp;

import cn.sl.ehub.common.vo.DataResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 企业用户设备今日曲线返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("企业用户设备今日曲线返回实体")
public class EntUserDeviceTodayChartResp {

    @ApiModelProperty("有功功率")
    private EntUserDeviceYesterdayChartResp entUserDeviceYesterdayChartResp;
    @ApiModelProperty("无功功率")
    private List<DataResp> noPowerChart;
    @ApiModelProperty("用电电流")
    private EntUserDeviceTodayElectricCurrentChartResp entUserDeviceTodayElectricCurrentChartResp;
    @ApiModelProperty("当日零点电量")
    private List<DataResp> zeroPointElectricityQuantityChart;

}
