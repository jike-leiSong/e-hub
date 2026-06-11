package cn.sl.ehub.console.controller;

import cn.enn.cim.resp.DeviceBaseInfo;
import cn.sl.ehub.console.model.vo.OptionVO;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.service.req.UpdateEntDeviceReq;
import cn.sl.ehub.service.req.UpdateEntReq;
import cn.sl.ehub.service.resp.EntUserDetailResp;
import cn.sl.ehub.console.service.IEntUserDetailService;
import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.service.vo.ResultWithPageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企业用户详情
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Slf4j
@RestController
@RequestMapping("/entUserDetail")
@Api(tags = "企业用户详情")
public class EntUserDetailController {

    private final IEntUserDetailService entUserDetailService;

    public EntUserDetailController(IEntUserDetailService entUserDetailService) {
        this.entUserDetailService = entUserDetailService;
    }

    @ApiOperation(value = "首页用户分布查询")
    @RequestMapping(value = "/getEntUserDetailRespList", method = RequestMethod.GET)
    public ResultVO<List<EntUserDetailResp>> getEntUserDetailRespList(@RequestParam("aggregatorId") String aggregatorId) {
        return ResultVO.success(entUserDetailService.getEntUserDetailRespList(aggregatorId));
    }

    @ApiOperation(value = "获取企业用户选项列表")
    @GetMapping("/options")
    public ResultVO<List<OptionVO>> getEntOptions(@RequestParam String aggregatorId,
                                                  @RequestParam(value = "resourceTypeId", required = false) String resourceTypeId) {
        return ResultVO.success(entUserDetailService.getEntOptions(aggregatorId, resourceTypeId));
    }

    @ApiOperation(value = "用户详情 企业用户列表（带设备信息）")
    @GetMapping("/list")
    public ResultVO<List<EntUserDetailResp>> getEntUserDetailWithDeviceRespList(@RequestParam String aggregatorId,
                                                                                @RequestParam(required = false) String endId,
                                                                                @RequestParam(required = false) Double powerGetterThan,
                                                                                @RequestParam(required = false) Double powerLessThan,
                                                                                @RequestParam(required = false) Double percent,
                                                                                @RequestParam(required = false, defaultValue = "1") Integer pageIndex,
                                                                                @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        PageResultVO<EntUserDetailResp> result = entUserDetailService.getEntUserDetailWithDevice(aggregatorId,
                endId, powerGetterThan, powerLessThan, percent, pageIndex, pageSize);
        return ResultWithPageVO.success4Page(result.getList(), result.getPageIndex(), result.getPageSize(), result.getTotal());
    }

    @ApiOperation(value = "分成比例选项列表")
    @GetMapping("/percent/options")
    public ResultVO<List<OptionVO>> getOptions(@RequestParam String aggregatorId) {
        return ResultVO.success(entUserDetailService.getPercentOptions(aggregatorId));
    }

    @ApiOperation(value = "用户详情 企业用户列表（带设备信息）v2")
    @GetMapping("/listV2")
    public ResultVO<List<EntUserDetailResp>> getEntUserDetailWithDeviceRespListV2(
            @RequestParam(value = "aggregatorId") String aggregatorId,
            @RequestParam(value = "entId", required = false) String entId,
            @RequestParam(value = "startYear", required = false) String startYear,
            @RequestParam(value = "endYear", required = false) String endYear) {
        return ResultVO.success(entUserDetailService.getEntAndDeviceRespList(aggregatorId,entId, startYear, endYear));
    }

    @ApiOperation(value = "删除合同")
    @PostMapping("/deleteAgreement")
    public ResultVO<Boolean> deleteAgreement(
            @RequestParam("aggregatorId") String aggregatorId,
            @RequestParam("entId") String entId) {
        return ResultVO.success(entUserDetailService.deleteAgreement(entId));
    }

    @ApiOperation(value = "编辑用户信息")
    @PostMapping("/updateEnt")
    public ResultVO<Boolean> updateEnt(@RequestBody UpdateEntReq req) {
        if (req.getPercent() == null) {
            return ResultVO.fail(500, "负荷聚合商用户分成比例不能为空");
        }
        return ResultVO.success(entUserDetailService.updateEnt(req));
    }

    @ApiOperation(value = "同步用户信息")
    @PostMapping("/autoUpdateEnt")
    public ResultVO<Boolean> autoUpdateEnt(@RequestParam("aggregatorId") String aggregatorId) {
        return ResultVO.success(entUserDetailService.autoUpdateEnt(aggregatorId));
    }

    @ApiOperation(value = "查询cim设备信息")
    @GetMapping("/getCimDeviceList")
    public ResultVO<List<UpdateEntDeviceReq>> getCimDeviceList(
            @RequestParam("aggregatorId") String aggregatorId,
            @RequestParam("entId") String entId,
            @RequestParam("stationId") String stationId) {
        return ResultVO.success(entUserDetailService.getCimDeviceList(aggregatorId, entId, stationId));
    }
}
