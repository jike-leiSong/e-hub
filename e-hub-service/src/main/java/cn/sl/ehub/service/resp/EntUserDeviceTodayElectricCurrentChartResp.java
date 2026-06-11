package cn.sl.ehub.service.resp;

import cn.sl.ehub.common.vo.DataResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 企业用户今日用电电流返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("企业用户今日用电电流返回实体")
public class EntUserDeviceTodayElectricCurrentChartResp {

    @ApiModelProperty("A相电流")
    private List<DataResp> iaList;
    @ApiModelProperty("B相电流")
    private List<DataResp> ibList;
    @ApiModelProperty("C相电流")
    private List<DataResp> icList;
}
