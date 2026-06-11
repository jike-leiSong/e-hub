package cn.sl.ehub.console.controller;

import cn.sl.ehub.service.req.AggregatorApplyOfferReq;
import cn.sl.ehub.service.req.AggregatorApplyReq;
import cn.sl.ehub.service.resp.*;
import cn.sl.ehub.console.service.ITomorrowService;
import cn.sl.ehub.common.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 明日详情
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Slf4j
@RestController
@RequestMapping("/tomorrow")
@Api(tags = "明日详情")
public class TomorrowController {

    private final ITomorrowService detailService;

    public TomorrowController(ITomorrowService detailService) {
        this.detailService = detailService;
    }

    @ApiOperation(value = "查询设备曲线")
    @RequestMapping(value = "/getEntUserDeviceChartResp", method = RequestMethod.GET)
    public ResultVO<EntUserDeviceTomorrowChartResp> getEntUserDeviceChartResp(@RequestParam("deviceBaseId") String deviceBaseId) {
        return ResultVO.success(detailService.getEntUserDeviceTomorrowChartResp(deviceBaseId));
    }

    @ApiOperation(value = "查询申报")
    @RequestMapping(value = "/getAggregatorApply", method = RequestMethod.GET)
    public ResultVO<AggregatorApplyResp> getAggregatorApply(
            @RequestParam("aggregatorId") String aggregatorId,
            @RequestParam(value = "date", required = false) String date) {
        return ResultVO.success(detailService.getAggregatorApply(aggregatorId, date));
    }

    @ApiOperation(value = "立即申报")
    @RequestMapping(value = "/updateAggregatorApply", method = RequestMethod.POST)
    public ResultVO<Boolean> updateAggregatorApply(HttpServletRequest request, @RequestBody AggregatorApplyReq req) {
        String ticket = request.getHeader("ticket");
        req.setApplyBy(ticket);
        return ResultVO.success(detailService.updateAggregatorApply(req));
    }

    @ApiOperation(value = "查询报价")
    @RequestMapping(value = "/getAggregatorApplyOfferResp", method = RequestMethod.GET)
    public ResultVO<AggregatorApplyOfferResp> getAggregatorApplyOfferResp(
            @RequestParam("aggregatorId") String aggregatorId,
            @RequestParam(value = "date", required = false) String date) {
        return ResultVO.success(detailService.getAggregatorApplyOfferResp(aggregatorId, date));
    }

    @ApiOperation(value = "按资源类型查询报价")
    @RequestMapping(value = "/getPriceByResourceTypeId", method = RequestMethod.GET)
    public ResultVO<List<AggregatorApplyOfferResourceDateResp>> getPriceByResourceTypeId(
            @RequestParam("aggregatorId") String aggregatorId,
            @RequestParam("resourceTypeId") String resourceTypeId,
            @RequestParam(value = "date", required = false) String date) {
        return ResultVO.success(detailService.getPriceByResourceTypeId(aggregatorId, resourceTypeId, date));
    }

    @ApiOperation(value = "暂存报价")
    @RequestMapping(value = "/saveAggregatorApplyOffer", method = RequestMethod.POST)
    public ResultVO<Boolean> saveAggregatorApplyOffer(@RequestBody AggregatorApplyOfferReq req) {
        return ResultVO.success(detailService.saveAggregatorApplyOffer(req, "0"));
    }

    @ApiOperation(value = "提交报价")
    @RequestMapping(value = "/submitAggregatorApplyOffer", method = RequestMethod.POST)
    public ResultVO<Boolean> submitAggregatorApplyOffer(@RequestBody AggregatorApplyOfferReq req) {
        return ResultVO.success(detailService.saveAggregatorApplyOffer(req, "1"));
    }

    @ApiOperation(value = "用户申报汇总曲线")
    @RequestMapping(value = "/getAggregatorDeliveryChart", method = RequestMethod.GET)
    public ResultVO<IndexOverviewResp> getAggregatorDeliveryChart(
            @RequestParam("aggregatorId") String aggregatorId,
            @RequestParam("resourceTypeId") String resourceTypeId,
            @RequestParam(value = "date", required = false) String date) {
        return ResultVO.success(detailService.getAggregatorDeliveryChart(aggregatorId, resourceTypeId, date));
    }


    @ApiOperation(value = "自动申报-自测")
    @RequestMapping(value = "/autoAggregatorApply", method = RequestMethod.POST)
    public ResultVO<Boolean> autoAggregatorApply(HttpServletRequest request, @RequestBody AggregatorApplyReq req) {
        String ticket = request.getHeader("ticket");
        req.setApplyBy(ticket);
        return ResultVO.success(detailService.autoAggregatorApply(req));
    }
}
