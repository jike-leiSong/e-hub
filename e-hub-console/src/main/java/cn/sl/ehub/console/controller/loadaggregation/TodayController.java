package cn.sl.ehub.console.controller.loadaggregation;

import cn.sl.ehub.console.model.vo.UserInfoAndDevice;
import cn.sl.ehub.service.resp.AggregatorEntDeviceIotLogResp;
import cn.sl.ehub.service.resp.EntUserDeviceTodayChartResp;
import cn.sl.ehub.console.service.ITodayService;
import cn.sl.ehub.common.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 今日详情
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Slf4j
@RestController
@RequestMapping("/today")
@Api(tags = "今日详情")
public class TodayController {

    private final ITodayService detailService;

    public TodayController(ITodayService detailService) {
        this.detailService = detailService;
    }

    @ApiOperation(value = "查询设备曲线")
    @RequestMapping(value = "/getEntUserDeviceChartResp", method = RequestMethod.GET)
    public ResultVO<EntUserDeviceTodayChartResp> getEntUserDeviceChartResp(HttpServletRequest request, @RequestParam("deviceBaseId") String deviceBaseId) {
        String simulate = request.getHeader("simulate");
        if (StringUtils.isEmpty(simulate) || "null".equals(simulate) || !"1".equals(simulate)) {
            simulate = "0";
        }
        return ResultVO.success(detailService.getEntUserDeviceTodayChartResp(simulate, deviceBaseId));
    }

    @ApiOperation(value = "查询用户、能源站、设备曲线")
    @RequestMapping(value = "/getMultiDeviceChartResp", method = RequestMethod.GET)
    public ResultVO<EntUserDeviceTodayChartResp> getMultiDeviceChartResp(@RequestParam("deviceBaseId") String deviceBaseId,
                                                                         @RequestParam("systemCode") String systemCode,@RequestParam("energyStationcode") String energyStationcode) {
        String simulate = "0";
//        if (StringUtils.isEmpty(simulate) || "null".equals(simulate) || !"1".equals(simulate)) {
//            simulate = "0";
//        }
        return ResultVO.success(detailService.getDeviceTreeTodayChartResp(simulate, deviceBaseId,energyStationcode,systemCode));
    }

    @ApiOperation(value = "查询执行记录")
    @RequestMapping(value = "/getIotLog", method = RequestMethod.GET)
    public ResultVO<List<AggregatorEntDeviceIotLogResp>> getIotLog(
            @RequestParam("aggregatorId") String aggregatorId,
            @RequestParam("entId") String entId,
            @RequestParam("resourceTypeId") String resourceTypeId,
            @RequestParam("stationId") String stationId,
            @RequestParam(value = "deviceBaseId", required = false) String deviceBaseId) {
        return ResultVO.success(detailService.getIotLog(entId, stationId, resourceTypeId, deviceBaseId));
    }

    @ApiOperation(value = "查询聚合商设备层级")
    @RequestMapping(value = "/get/device/tree", method = RequestMethod.GET)
    public ResultVO<List<UserInfoAndDevice>> getDevices(@RequestParam("aggregatorId") String aggregatorId, @RequestParam("resourceType") String resourceType) {
        return ResultVO.success(detailService.getDevices(aggregatorId, resourceType));
    }



}
