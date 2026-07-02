package cn.sl.ehub.service.dto.iot;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("物联时序数据查询请求")
public class IotTelemetryQueryReq {

    @ApiModelProperty("企业ID（数据权限注入）")
    private String entId;

    @ApiModelProperty("聚合商ID（数据权限注入）")
    private String aggregatorId;

    @ApiModelProperty("业务项目编码（aggregator_single_model_data.energy_station_code）")
    private String energyStationCode;

    @ApiModelProperty("设备ID列表")
    private List<Long> deviceIds;

    @ApiModelProperty("测点编码列表")
    private List<String> pointCodes;

    @ApiModelProperty("开始时间 yyyy-MM-dd HH:mm:ss")
    private String startTime;

    @ApiModelProperty("结束时间 yyyy-MM-dd HH:mm:ss")
    private String endTime;

    @ApiModelProperty("聚合粒度：minute/hour/day，默认 minute")
    private String aggType = "minute";

    @ApiModelProperty("聚合函数：avg/max/min/sum，默认 avg（仅 aggType!=minute 时生效）")
    private String aggFunc = "avg";

    @ApiModelProperty("最大返回条数，默认 1000，上限 10000")
    private Integer limit = 1000;

    @ApiModelProperty("分页偏移")
    private Integer offset;

    @ApiModelProperty("范围解析为空时强制返回空结果")
    private Boolean emptyScope = false;
}
