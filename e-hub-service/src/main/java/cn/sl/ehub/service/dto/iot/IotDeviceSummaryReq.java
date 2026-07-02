package cn.sl.ehub.service.dto.iot;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("设备概览查询请求")
public class IotDeviceSummaryReq {

    @ApiModelProperty("企业ID（数据权限注入）")
    private String entId;

    @ApiModelProperty("聚合商ID（数据权限注入）")
    private String aggregatorId;

    @ApiModelProperty("业务项目编码（aggregator_single_model_data.energy_station_code）")
    private String energyStationCode;

    @ApiModelProperty("设备ID列表（不传则返回该企业下所有设备）")
    private List<Long> deviceIds;

    @ApiModelProperty("测点编码列表（不传则返回该设备下所有测点）")
    private List<String> pointCodes;

    @ApiModelProperty("最大返回设备数，默认 100，上限 500")
    private Integer limit = 100;

    @ApiModelProperty("范围解析为空时强制返回空结果")
    private Boolean emptyScope = false;
}
