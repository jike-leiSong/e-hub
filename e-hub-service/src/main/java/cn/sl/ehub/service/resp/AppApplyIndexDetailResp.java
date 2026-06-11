package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 调峰辅助服务详情
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("调峰辅助服务详情")
public class AppApplyIndexDetailResp {

    @ApiModelProperty("日期")
    private String date;
    @ApiModelProperty("调峰辅助服务设备详情")
    private List<AppApplyIndexDeviceDetailResp> deviceList;
}
