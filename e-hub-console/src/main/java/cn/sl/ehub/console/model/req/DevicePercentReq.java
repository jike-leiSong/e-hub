package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 设备基线比例请求
 *
 * @Author sl
 * @phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("设备基线比例请求")
public class DevicePercentReq {

    private String aggregatorId;
    private String entId;
    private String stationId;
    private String deviceBaseId;
    private Double percent;
    private String startDate;
    private String endDate;
}
