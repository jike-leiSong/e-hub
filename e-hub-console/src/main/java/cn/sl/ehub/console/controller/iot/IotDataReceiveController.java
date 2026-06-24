package cn.sl.ehub.console.controller.iot;

import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.service.dto.iot.IotCimDataReceiveReq;
import cn.sl.ehub.service.dto.iot.IotDataReceiveResp;
import cn.sl.ehub.service.dto.iot.IotOriginDataReceiveReq;
import cn.sl.ehub.service.service.IotTelemetryIngestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data-collector/thirdPart/data/receive")
@Api(tags = "IoT三方数据接入")
public class IotDataReceiveController {

    private static final String ACCESS_KEY_HEADER = "X-GW-AccessKey";

    private final IotTelemetryIngestService iotTelemetryIngestService;

    public IotDataReceiveController(IotTelemetryIngestService iotTelemetryIngestService) {
        this.iotTelemetryIngestService = iotTelemetryIngestService;
    }

    @PostMapping("/originData")
    @ApiOperation("第三方非CIM化数据推送接口")
    public ResultVO<IotDataReceiveResp> originData(@RequestHeader(value = ACCESS_KEY_HEADER, required = false) String accessKey,
                                                   @RequestBody(required = false) IotOriginDataReceiveReq req) {
        try {
            IotDataReceiveResp resp = iotTelemetryIngestService.ingestOriginData(accessKey, req);
            return ResultVO.success(resp, buildMsg(resp));
        } catch (BaseException e) {
            return new ResultVO<>(e.getCode(), e.getMessage(), new IotDataReceiveResp());
        }
    }

    @PostMapping("/cimData")
    @ApiOperation("第三方CIM化数据推送接口")
    public ResultVO<IotDataReceiveResp> cimData(@RequestHeader(value = ACCESS_KEY_HEADER, required = false) String accessKey,
                                                @RequestBody(required = false) IotCimDataReceiveReq req) {
        try {
            IotDataReceiveResp resp = iotTelemetryIngestService.ingestCimData(accessKey, req);
            return ResultVO.success(resp, buildMsg(resp));
        } catch (BaseException e) {
            return new ResultVO<>(e.getCode(), e.getMessage(), new IotDataReceiveResp());
        }
    }

    private String buildMsg(IotDataReceiveResp resp) {
        int total = resp.getSuccess() + resp.getFail();
        return "此次共接收 " + total + " 条，入库成功 " + resp.getSuccess() + " 条，入库失败 " + resp.getFail() + " 条。";
    }
}
