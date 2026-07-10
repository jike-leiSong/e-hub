package cn.sl.ehub.console.controller.iot;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.service.IotMockTelemetryService;
import cn.sl.ehub.service.dto.iot.IotMockPowerDataReq;
import cn.sl.ehub.service.dto.iot.IotMockPowerDataResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/iot/mock")
@RequiredArgsConstructor
@Api(tags = "IoT模拟数据推送")
public class IotMockTelemetryController {

    private final IotMockTelemetryService mockTelemetryService;

    @PostMapping("/push-telemetry")
    @ApiOperation("模拟物联数据推送（调用三方数据接入接口）")
    public ResultVO<String> pushMockTelemetry(
            @ApiParam(value = "聚合商ID，不传则使用数据范围中的聚合商", required = false)
            @RequestParam(value = "aggregatorId", required = false) String aggregatorId,

            @ApiParam(value = "企业ID，不传则使用数据范围中的企业", required = false)
            @RequestParam(value = "entId", required = false) String entId,

            @ApiParam(value = "推送天数（默认1天）", defaultValue = "1")
            @RequestParam(value = "days", defaultValue = "1") Integer days,

            @ApiParam(value = "每个设备推送间隔（秒，默认60，即每分钟一个数据点）", defaultValue = "60")
            @RequestParam(value = "intervalSeconds", defaultValue = "60") Integer intervalSeconds,

            @ApiParam(value = "每个设备最大测点数（默认5，0表示推送全部测点）", defaultValue = "5")
            @RequestParam(value = "maxPointsPerDevice", defaultValue = "5") Integer maxPointsPerDevice,

            @ApiParam(value = "X-GW-AccessKey（不传则使用默认凭证）", required = false)
            @RequestParam(value = "accessKey", required = false) String accessKey,

            @ApiParam(value = "数据时间偏移小时（默认0，可填负数往前推，如-24表示从昨天开始）", defaultValue = "0")
            @RequestParam(value = "hourOffset", defaultValue = "0") Integer hourOffset
    ) {
        String result = mockTelemetryService.pushMockTelemetry(
                aggregatorId, entId, days, intervalSeconds,
                maxPointsPerDevice, accessKey, hourOffset);
        return ResultVO.success(result);
    }

    @PostMapping("/power-data")
    @ApiOperation("手动生成现有设备P功率物联数据")
    public ResultVO<IotMockPowerDataResp> generatePowerData(@RequestBody IotMockPowerDataReq req) {
        return ResultVO.success(mockTelemetryService.generatePowerData(req));
    }
}
