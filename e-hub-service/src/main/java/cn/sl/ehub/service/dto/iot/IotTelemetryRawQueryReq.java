package cn.sl.ehub.service.dto.iot;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("物联原始明细查询请求")
public class IotTelemetryRawQueryReq {

    @ApiModelProperty("企业ID（数据权限注入）")
    private String entId;

    @ApiModelProperty("聚合商ID（数据权限注入）")
    private String aggregatorId;

    @ApiModelProperty("业务项目编码（aggregator_single_model_data.energy_station_code）")
    private String energyStationCode;

    @ApiModelProperty("设备ID")
    private Long deviceId;

    @ApiModelProperty("设备ID列表（范围解析后注入）")
    private java.util.List<Long> deviceIds;

    @ApiModelProperty("三方设备标识（模糊查询）")
    private String externalDeviceId;

    @ApiModelProperty("三方metric（模糊查询）")
    private String externalMetric;

    @ApiModelProperty("匹配状态：matched/device_not_found/point_not_found")
    private String matchStatus;

    @ApiModelProperty("开始时间 yyyy-MM-dd HH:mm:ss")
    private String startTime;

    @ApiModelProperty("结束时间 yyyy-MM-dd HH:mm:ss")
    private String endTime;

    @ApiModelProperty("最大返回条数，默认 100，上限 1000")
    private Integer limit = 100;

    @ApiModelProperty("分页偏移")
    private Integer offset;

    @ApiModelProperty("范围解析为空时强制返回空结果")
    private Boolean emptyScope = false;
}
