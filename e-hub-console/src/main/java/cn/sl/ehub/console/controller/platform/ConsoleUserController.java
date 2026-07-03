package cn.sl.ehub.console.controller.platform;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.model.req.ConsoleUserPageReq;
import cn.sl.ehub.console.model.req.ConsoleUserUpsertReq;
import cn.sl.ehub.console.model.req.UserRoleSaveReq;
import cn.sl.ehub.console.model.req.UserStatusUpdateReq;
import cn.sl.ehub.console.model.resp.ConsoleUserPageItemResp;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.service.IConsoleUserManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/console-user")
@Api(tags = "平台用户管理")
public class ConsoleUserController {

    private final IConsoleUserManageService consoleUserManageService;

    @GetMapping("/page")
    @ApiOperation("平台用户分页")
    public ResultVO<PageResultVO<ConsoleUserPageItemResp>> page(ConsoleUserPageReq req) {
        return ResultVO.success(consoleUserManageService.page(req));
    }

    @PostMapping
    @ApiOperation("新增平台用户")
    public ResultVO<ConsoleUserPageItemResp> create(@RequestBody ConsoleUserUpsertReq req) {
        return ResultVO.success(consoleUserManageService.create(req));
    }

    @PutMapping("/{userId}")
    @ApiOperation("更新平台用户")
    public ResultVO<ConsoleUserPageItemResp> update(@PathVariable("userId") String userId,
                                                    @RequestBody ConsoleUserUpsertReq req) {
        return ResultVO.success(consoleUserManageService.update(userId, req));
    }

    @PutMapping("/{userId}/status")
    @ApiOperation("更新用户状态")
    public ResultVO<Boolean> updateStatus(@PathVariable("userId") String userId,
                                          @RequestBody UserStatusUpdateReq req) {
        return ResultVO.success(consoleUserManageService.updateStatus(userId, req));
    }

    @PutMapping("/{userId}/roles")
    @ApiOperation("保存用户角色")
    public ResultVO<Boolean> saveRoles(@PathVariable("userId") String userId,
                                       @RequestBody UserRoleSaveReq req) {
        return ResultVO.success(consoleUserManageService.saveRoles(userId, req));
    }
}
