package cn.sl.ehub.console.controller.platform;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.model.req.OperationLogPageReq;
import cn.sl.ehub.console.model.resp.OperationLogPageItemResp;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.service.IPlatformAuditLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/audit")
@Api(tags = "平台审计日志")
public class PlatformAuditLogController {

    private final IPlatformAuditLogService platformAuditLogService;

    @GetMapping("/logs")
    @ApiOperation("操作日志分页")
    public ResultVO<PageResultVO<OperationLogPageItemResp>> logs(OperationLogPageReq req) {
        return ResultVO.success(platformAuditLogService.logs(req));
    }
}
