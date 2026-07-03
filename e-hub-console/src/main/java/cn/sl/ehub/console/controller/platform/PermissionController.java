package cn.sl.ehub.console.controller.platform;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.model.req.RolePageReq;
import cn.sl.ehub.console.model.req.RolePermissionSaveReq;
import cn.sl.ehub.console.model.req.RoleUpsertReq;
import cn.sl.ehub.console.model.resp.PermissionTreeNodeResp;
import cn.sl.ehub.console.model.resp.RolePageItemResp;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.service.IRolePermissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/permission")
@Api(tags = "角色权限管理")
public class PermissionController {

    private final IRolePermissionService rolePermissionService;

    @GetMapping("/roles")
    @ApiOperation("角色分页")
    public ResultVO<PageResultVO<RolePageItemResp>> roles(RolePageReq req) {
        return ResultVO.success(rolePermissionService.roles(req));
    }

    @PostMapping("/roles")
    @ApiOperation("新增角色")
    public ResultVO<RolePageItemResp> createRole(@RequestBody RoleUpsertReq req) {
        return ResultVO.success(rolePermissionService.createRole(req));
    }

    @PutMapping("/roles/{roleId}")
    @ApiOperation("更新角色")
    public ResultVO<RolePageItemResp> updateRole(@PathVariable("roleId") String roleId,
                                                 @RequestBody RoleUpsertReq req) {
        return ResultVO.success(rolePermissionService.updateRole(roleId, req));
    }

    @GetMapping("/tree")
    @ApiOperation("权限树")
    public ResultVO<List<PermissionTreeNodeResp>> permissionTree(@RequestParam(value = "platformType", required = false) String platformType,
                                                                 @RequestParam(value = "roleId", required = false) String roleId) {
        return ResultVO.success(rolePermissionService.permissionTree(platformType, roleId));
    }

    @PutMapping("/roles/{roleId}/permissions")
    @ApiOperation("保存角色权限")
    public ResultVO<Boolean> saveRolePermissions(@PathVariable("roleId") String roleId,
                                                 @RequestBody RolePermissionSaveReq req) {
        return ResultVO.success(rolePermissionService.saveRolePermissions(roleId, req));
    }
}
