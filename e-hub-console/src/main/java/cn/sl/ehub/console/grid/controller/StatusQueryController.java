package cn.sl.ehub.console.grid.controller;

import cn.sl.ehub.service.service.ClearIssueLogService;
import cn.sl.ehub.service.service.PlanDeliveryLogService;
import cn.sl.ehub.service.vo.ClearIssueLog;
import cn.sl.ehub.service.vo.PlanDeliveryLog;
import cn.sl.ehub.common.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Description: 下发状态查询
 * @Author sl
 * @Date 2026-05-28
 */
@RestController
@RequestMapping("/statusQuery")
@Api(tags = "状态查询")
public class StatusQueryController {

    @Resource
    private ClearIssueLogService clearIssueLogService;

    @Resource
    private PlanDeliveryLogService planDeliveryLogService;

    @ApiOperation(value = "出清状态查询")
    @RequestMapping(value = "/getClearIssueStatus", method = RequestMethod.GET)
    public ResultVO<ClearIssueLog> getClearIssueStatus() {
        return ResultVO.success(clearIssueLogService.getLastedLog());
    }

    @ApiOperation(value = "申报状态查询")
    @RequestMapping(value = "/getDeliveryStatus", method = RequestMethod.GET)
    public ResultVO<PlanDeliveryLog> getDeliveryStatus() {
        return ResultVO.success(planDeliveryLogService.getLastedLog());
    }
}
