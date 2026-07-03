package cn.sl.ehub.console.controller.platform;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.model.resp.OperationLogSimpleResp;
import cn.sl.ehub.console.model.resp.WorkbenchSummaryResp;
import cn.sl.ehub.console.model.resp.WorkbenchTodoResp;
import cn.sl.ehub.console.service.IPlatformWorkbenchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/workbench")
@Api(tags = "平台工作台")
public class PlatformWorkbenchController {

    private final IPlatformWorkbenchService platformWorkbenchService;

    @GetMapping("/summary")
    @ApiOperation("平台工作台汇总")
    public ResultVO<WorkbenchSummaryResp> summary() {
        return ResultVO.success(platformWorkbenchService.summary());
    }

    @GetMapping("/todos")
    @ApiOperation("平台工作台待办")
    public ResultVO<List<WorkbenchTodoResp>> todos() {
        return ResultVO.success(platformWorkbenchService.todos());
    }

    @GetMapping("/recent-logs")
    @ApiOperation("平台工作台最近操作")
    public ResultVO<List<OperationLogSimpleResp>> recentLogs(@RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return ResultVO.success(platformWorkbenchService.recentLogs(limit));
    }
}
