package cn.sl.ehub.console.controller.loadaggregation;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.service.IAggregatorEntService;
import cn.sl.ehub.service.req.UpdateEntReq;
import cn.sl.ehub.service.resp.EntUserDetailResp;
import cn.sl.ehub.service.vo.AggregatorEnt;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企业用户详情
 *
 * @Author sl
 * @Date 2026-06-23
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/entUserDetail")
@Api(tags = "企业用户详情")
public class EntUserDetailController {

    private final IAggregatorEntService aggregatorEntService;

    @ApiOperation(value = "获取企业用户选项列表")
    @GetMapping("/options")
    public ResultVO<List<AggregatorEnt>> getEntUserOptions(
            @ApiParam(value = "聚合商ID") @RequestParam("aggregatorId") String aggregatorId) {
        return ResultVO.success(aggregatorEntService.getAggregatorEntList(aggregatorId));
    }

    @ApiOperation(value = "获取企业用户详情响应列表")
    @GetMapping("/getEntUserDetailRespList")
    public ResultVO<List<EntUserDetailResp>> getEntUserDetailRespList(
            @ApiParam(value = "聚合商ID") @RequestParam("aggregatorId") String aggregatorId,
            @ApiParam(value = "资源类型ID") @RequestParam(value = "resourceTypeId", required = false) String resourceTypeId) {
        return ResultVO.success(aggregatorEntService.getEntUserDetailRespList(aggregatorId, resourceTypeId));
    }

    @ApiOperation(value = "获取企业用户详情列表")
    @GetMapping("/list")
    public ResultVO<List<EntUserDetailResp>> getEntUserDetailList(
            @ApiParam(value = "聚合商ID") @RequestParam(value = "aggregatorId", required = false) String aggregatorId,
            @ApiParam(value = "企业ID") @RequestParam(value = "entId", required = false) String entId,
            @ApiParam(value = "年份列表") @RequestParam(value = "yearList", required = false) List<String> yearList) {
        if (yearList != null && !yearList.isEmpty()) {
            if (aggregatorId != null) {
                return ResultVO.success(aggregatorEntService.selectEntAndDeviceListByAggregatorId(aggregatorId, entId, yearList));
            } else {
                return ResultVO.success(aggregatorEntService.selectEntAndDeviceList(entId, yearList));
            }
        }
        return ResultVO.success(aggregatorEntService.getEntUserDetailRespList(aggregatorId, null));
    }

    @ApiOperation(value = "获取企业用户详情列表V2")
    @GetMapping("/listV2")
    public ResultVO<List<EntUserDetailResp>> getEntUserDetailListV2(
            @ApiParam(value = "聚合商ID") @RequestParam("aggregatorId") String aggregatorId,
            @ApiParam(value = "企业ID") @RequestParam(value = "entId", required = false) String entId,
            @ApiParam(value = "功率大于") @RequestParam(value = "powerGetterThan", required = false) Double powerGetterThan,
            @ApiParam(value = "功率小于") @RequestParam(value = "powerLessThan", required = false) Double powerLessThan,
            @ApiParam(value = "百分比") @RequestParam(value = "percent", required = false) Double percent) {
        return ResultVO.success(aggregatorEntService.selectEntUserDetailWithDevice(
                aggregatorId, entId, powerGetterThan, powerLessThan, percent));
    }

    @ApiOperation(value = "获取百分比选项")
    @GetMapping("/percent/options")
    public ResultVO<List<Double>> getEntUserDetailPercentOptions(
            @ApiParam(value = "聚合商ID") @RequestParam("aggregatorId") String aggregatorId) {
        return ResultVO.success(aggregatorEntService.selectPercentDistinct(aggregatorId));
    }

    @ApiOperation(value = "更新企业信息")
    @PostMapping("/updateEnt")
    public ResultVO<Integer> updateEnt(@RequestBody UpdateEntReq req) {
        return ResultVO.success(aggregatorEntService.updateAggregatorEntAgreementInfo(req));
    }

    @ApiOperation(value = "自动更新企业信息")
    @PostMapping("/autoUpdateEnt")
    public ResultVO<String> autoUpdateEnt(@RequestBody UpdateEntReq req) {
        int result = aggregatorEntService.updateAggregatorEntAgreementInfo(req);
        return ResultVO.success(result > 0 ? "更新成功" : "更新失败");
    }

    @ApiOperation(value = "获取CIM设备列表")
    @GetMapping("/getCimDeviceList")
    public ResultVO<List<EntUserDetailResp>> getCimDeviceList(
            @ApiParam(value = "企业ID") @RequestParam("entId") String entId,
            @ApiParam(value = "开始年份") @RequestParam(value = "startYear", required = false) String startYear,
            @ApiParam(value = "结束年份") @RequestParam(value = "endYear", required = false) String endYear,
            @ApiParam(value = "聚合商ID") @RequestParam(value = "aggregatorId", required = false) String aggregatorId) {

        if (startYear != null) {
            if (aggregatorId != null) {
                return ResultVO.success(aggregatorEntService.selectEntAndDeviceListByStartYearAggregatorId(
                        aggregatorId, entId, startYear));
            } else {
                return ResultVO.success(aggregatorEntService.selectEntAndDeviceListByStartYear(entId, startYear));
            }
        }

        if (endYear != null) {
            if (aggregatorId != null) {
                return ResultVO.success(aggregatorEntService.selectEntAndDeviceListByEndYearAggregatorId(
                        aggregatorId, entId, endYear));
            } else {
                return ResultVO.success(aggregatorEntService.selectEntAndDeviceListByEndYear(entId, endYear));
            }
        }

        return ResultVO.success(aggregatorEntService.selectEntAndDeviceList(entId, null));
    }
}
