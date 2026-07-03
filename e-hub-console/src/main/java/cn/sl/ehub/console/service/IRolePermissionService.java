package cn.sl.ehub.console.service;

import cn.sl.ehub.console.model.req.RolePageReq;
import cn.sl.ehub.console.model.req.RolePermissionSaveReq;
import cn.sl.ehub.console.model.req.RoleUpsertReq;
import cn.sl.ehub.console.model.resp.PermissionTreeNodeResp;
import cn.sl.ehub.console.model.resp.RolePageItemResp;
import cn.sl.ehub.console.model.vo.PageResultVO;

import java.util.List;

public interface IRolePermissionService {

    PageResultVO<RolePageItemResp> roles(RolePageReq req);

    RolePageItemResp createRole(RoleUpsertReq req);

    RolePageItemResp updateRole(String roleId, RoleUpsertReq req);

    List<PermissionTreeNodeResp> permissionTree(String platformType, String roleId);

    Boolean saveRolePermissions(String roleId, RolePermissionSaveReq req);
}
