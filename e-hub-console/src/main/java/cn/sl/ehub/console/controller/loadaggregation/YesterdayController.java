package cn.sl.ehub.console.controller.loadaggregation;

import cn.sl.ehub.service.req.AggregatorEntDateInviteDetailReq;
import cn.sl.ehub.service.resp.EntUserDeviceYesterdayChartResp;
import cn.sl.ehub.service.resp.EntUserOverviewResp;
import cn.sl.ehub.service.resp.IndexOverviewResp;
import cn.sl.ehub.console.service.IAggregatorResourceTypeService;
import cn.sl.ehub.console.service.IYesterdayService;
import cn.sl.ehub.common.utils.RedisUtil;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorResourceType;
import cn.sl.ehub.common.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 昨日详情
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/yesterday")
@Api(tags = "昨日详情")
public class YesterdayController {

    private final IYesterdayService detailService;
    private final RedisUtil redis;
    private final IAggregatorResourceTypeService aggregatorResourceTypeService;

    @ApiOperation(value = "查询资源类型")
    @RequestMapping(value = "/getResourceTypeList", method = RequestMethod.GET)
    public ResultVO<List<AggregatorResourceType>> getResourceTypeList(@RequestParam("aggregatorId") String aggregatorId,
                                                                      @RequestParam(value = "entId", required = false) String entId) {
        return ResultVO.success(aggregatorResourceTypeService.getAggregatorResourceTypeListByAggregatorId(aggregatorId, entId));
    }

    @ApiOperation(value = "上次收益")
    @RequestMapping(value = "/getLastProfit", method = RequestMethod.GET)
    public ResultVO<IndexOverviewResp> getLastProfit(@RequestParam("aggregatorId") String aggregatorId) {
        return ResultVO.success(detailService.getLastProfit(aggregatorId));
    }

    @ApiOperation(value = "总览(dayType：昨日=yesterday，今日=today，明日=tomorrow)")
    @RequestMapping(value = "/getOverview", method = RequestMethod.GET)
    public ResultVO<IndexOverviewResp> getOverview(@RequestParam("aggregatorId") String aggregatorId,
                                                   @RequestParam("resourceTypeId") String resourceTypeId,
                                                   @RequestParam(value = "dayType", defaultValue = "today") String dayType) {
        return ResultVO.success(detailService.getOverview(aggregatorId, resourceTypeId, dayType));
    }

    @ApiOperation(value = "查询设备列表")
    @RequestMapping(value = "/getDeviceList", method = RequestMethod.GET)
    public ResultVO<List<AggregatorEntDevice>> getDeviceList(
            @RequestParam(value = "aggregatorId", required = false) String aggregatorId,
            @RequestParam(value = "entId") String entId,
            @RequestParam(value = "stationId", required = false) String stationId,
            @RequestParam(value = "resourceTypeId", required = false) String resourceTypeId) {
        return ResultVO.success(detailService.getDeviceList(aggregatorId, entId, stationId, resourceTypeId));
    }

    @ApiOperation(value = "查询昨日设备曲线")
    @RequestMapping(value = "/getEntUserDeviceChartResp", method = RequestMethod.GET)
    public ResultVO<EntUserDeviceYesterdayChartResp> getEntUserDeviceChartResp(@RequestParam("deviceBaseId") String deviceBaseId) {
        return ResultVO.success(detailService.getEntUserDeviceChartResp(deviceBaseId, null, null));
    }

    @ApiOperation(value = "用户情况(dayType：昨日=yesterday，今日=today，明日=tomorrow)")
    @RequestMapping(value = "/getEntUserOverviewResp", method = RequestMethod.GET)
    public ResultVO<List<EntUserOverviewResp>> getEntUserOverviewResp(@RequestParam("aggregatorId") String aggregatorId, @RequestParam(value = "dayType", defaultValue = "today") String dayType) {
        return ResultVO.success(detailService.getEntUserOverviewResp(aggregatorId, dayType));
    }

    @ApiOperation(value = "企业用户邀约")
    @RequestMapping(value = "/entInvite", method = RequestMethod.POST)
    public ResultVO<String> entInvite(@RequestBody AggregatorEntDateInviteDetailReq req) {
        return ResultVO.success(detailService.entInvite(req));
    }
}
