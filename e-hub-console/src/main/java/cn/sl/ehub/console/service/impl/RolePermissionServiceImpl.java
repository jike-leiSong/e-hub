package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.console.model.req.RolePageReq;
import cn.sl.ehub.console.model.req.RolePermissionSaveReq;
import cn.sl.ehub.console.model.req.RoleUpsertReq;
import cn.sl.ehub.console.model.resp.PermissionTreeNodeResp;
import cn.sl.ehub.console.model.resp.RolePageItemResp;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.service.IPlatformAuditLogService;
import cn.sl.ehub.console.service.IRolePermissionService;
import cn.sl.ehub.service.mapper.ConsolePermissionMapper;
import cn.sl.ehub.service.mapper.ConsoleRoleMapper;
import cn.sl.ehub.service.mapper.ConsoleRolePermissionMapper;
import cn.sl.ehub.service.vo.ConsolePermission;
import cn.sl.ehub.service.vo.ConsoleRole;
import cn.sl.ehub.service.vo.ConsoleRolePermission;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements IRolePermissionService {

    private final ConsoleRoleMapper consoleRoleMapper;
    private final ConsolePermissionMapper consolePermissionMapper;
    private final ConsoleRolePermissionMapper consoleRolePermissionMapper;
    private final IPlatformAuditLogService platformAuditLogService;

    @Override
    public PageResultVO<RolePageItemResp> roles(RolePageReq req) {
        Integer pageIndex = req.getPageIndex() == null || req.getPageIndex() < 1 ? 1 : req.getPageIndex();
        Integer pageSize = req.getPageSize() == null || req.getPageSize() < 1 ? 20 : req.getPageSize();
        PageHelper.startPage(pageIndex, pageSize);
        List<ConsoleRole> list = consoleRoleMapper.page(
                StringUtils.trimToNull(req.getKeyword()),
                StringUtils.trimToNull(req.getPlatformType()),
                req.getStatus()
        );
        List<RolePageItemResp> respList = new ArrayList<>();
        for (ConsoleRole item : list) {
            respList.add(toResp(item));
        }
        PageInfo<ConsoleRole> pageInfo = new PageInfo<>(list);
        PageResultVO<RolePageItemResp> page = new PageResultVO<>();
        page.setList(respList);
        page.setTotal((int) pageInfo.getTotal());
        page.setPageIndex(pageIndex);
        page.setPageSize(pageSize);
        return page;
    }

    @Override
    public RolePageItemResp createRole(RoleUpsertReq req) {
        validate(req);
        if (safeCount(consoleRoleMapper.countByRoleCode(req.getPlatformType(), req.getRoleCode(), null)) > 0) {
            throw new BaseException(StatusCode.C.getCode(), "角色编码已存在");
        }
        String now = DateUtils.getTime();
        ConsoleRole entity = new ConsoleRole();
        entity.setRoleId("ROLE" + System.currentTimeMillis() + new Random().nextInt(1000));
        entity.setRoleName(StringUtils.trim(req.getRoleName()));
        entity.setRoleCode(StringUtils.trim(req.getRoleCode()));
        entity.setPlatformType(StringUtils.trim(req.getPlatformType()));
        entity.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        entity.setRemark(StringUtils.trimToNull(req.getRemark()));
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        consoleRoleMapper.insertSelective(entity);
        platformAuditLogService.record("ROLE", entity.getRoleId(), "CREATE", null, entity, "SUCCESS", null);
        return toResp(entity);
    }

    @Override
    public RolePageItemResp updateRole(String roleId, RoleUpsertReq req) {
        validate(req);
        ConsoleRole existing = consoleRoleMapper.getByRoleId(roleId);
        if (existing == null) {
            throw new BaseException(StatusCode.C.getCode(), "角色不存在");
        }
        if (safeCount(consoleRoleMapper.countByRoleCode(req.getPlatformType(), req.getRoleCode(), roleId)) > 0) {
            throw new BaseException(StatusCode.C.getCode(), "角色编码已存在");
        }
        ConsoleRole before = copy(existing);
        existing.setRoleName(StringUtils.trim(req.getRoleName()));
        existing.setRoleCode(StringUtils.trim(req.getRoleCode()));
        existing.setPlatformType(StringUtils.trim(req.getPlatformType()));
        existing.setStatus(req.getStatus() == null ? existing.getStatus() : req.getStatus());
        existing.setRemark(StringUtils.trimToNull(req.getRemark()));
        existing.setUpdateTime(DateUtils.getTime());
        consoleRoleMapper.updateByPrimaryKeySelective(existing);
        platformAuditLogService.record("ROLE", existing.getRoleId(), "UPDATE", before, existing, "SUCCESS", null);
        return toResp(existing);
    }

    @Override
    public List<PermissionTreeNodeResp> permissionTree(String platformType, String roleId) {
        List<ConsolePermission> permissions = consolePermissionMapper.listEnabled();
        Set<String> grantedCodes = new HashSet<>();
        if (StringUtils.isNotBlank(roleId)) {
            List<ConsoleRolePermission> rolePermissions = consoleRolePermissionMapper.listByRoleId(roleId);
            for (ConsoleRolePermission item : rolePermissions) {
                grantedCodes.add(item.getPermissionCode());
            }
        }
        Map<String, PermissionTreeNodeResp> roots = new LinkedHashMap<>();
        for (ConsolePermission permission : permissions) {
            if (!matchPlatform(platformType, permission.getPermissionCode())) {
                continue;
            }
            String moduleCode = permission.getModuleCode();
            PermissionTreeNodeResp root = roots.computeIfAbsent(moduleCode, code -> {
                PermissionTreeNodeResp node = new PermissionTreeNodeResp();
                node.setPermissionCode("module:" + code);
                node.setPermissionName(moduleLabel(code));
                node.setPermissionType("MODULE");
                node.setModuleCode(code);
                node.setChecked(Boolean.FALSE);
                node.setChildren(new ArrayList<>());
                return node;
            });
            PermissionTreeNodeResp child = new PermissionTreeNodeResp();
            child.setPermissionCode(permission.getPermissionCode());
            child.setPermissionName(permission.getPermissionName());
            child.setPermissionType(permission.getPermissionType());
            child.setModuleCode(permission.getModuleCode());
            child.setPath(permission.getPath());
            child.setChecked(grantedCodes.contains(permission.getPermissionCode()));
            root.getChildren().add(child);
        }
        return new ArrayList<>(roots.values());
    }

    @Override
    public Boolean saveRolePermissions(String roleId, RolePermissionSaveReq req) {
        ConsoleRole role = consoleRoleMapper.getByRoleId(roleId);
        if (role == null) {
            throw new BaseException(StatusCode.C.getCode(), "角色不存在");
        }
        List<String> permissionCodes = req == null ? Collections.emptyList() : req.getPermissionCodes();
        List<ConsolePermission> permissions = consolePermissionMapper.listEnabled();
        Set<String> validCodes = new HashSet<>();
        for (ConsolePermission permission : permissions) {
            validCodes.add(permission.getPermissionCode());
        }
        if (permissionCodes != null) {
            for (String permissionCode : permissionCodes) {
                if (StringUtils.isBlank(permissionCode) || !validCodes.contains(permissionCode)) {
                    throw new BaseException(StatusCode.C.getCode(), "存在无效权限编码");
                }
            }
        }
        List<ConsoleRolePermission> before = consoleRolePermissionMapper.listByRoleId(roleId);
        consoleRolePermissionMapper.deleteByRoleId(roleId);
        String now = DateUtils.getTime();
        if (permissionCodes != null) {
            for (String permissionCode : new LinkedHashSet<>(permissionCodes)) {
                ConsoleRolePermission item = new ConsoleRolePermission();
                item.setRoleId(roleId);
                item.setPermissionCode(permissionCode);
                item.setCreateTime(now);
                item.setUpdateTime(now);
                consoleRolePermissionMapper.insertSelective(item);
            }
        }
        List<ConsoleRolePermission> after = consoleRolePermissionMapper.listByRoleId(roleId);
        platformAuditLogService.record("ROLE_PERMISSION", roleId, "GRANT", before, after, "SUCCESS", null);
        return true;
    }

    private boolean matchPlatform(String platformType, String permissionCode) {
        if (StringUtils.isBlank(platformType)) {
            return true;
        }
        if (StringUtils.equalsIgnoreCase(platformType, "owner")) {
            return StringUtils.startsWith(permissionCode, "owner:")
                    || StringUtils.startsWith(permissionCode, "load:")
                    || StringUtils.startsWith(permissionCode, "tariff:");
        }
        return !StringUtils.startsWith(permissionCode, "owner:");
    }

    private String moduleLabel(String moduleCode) {
        if (StringUtils.equals(moduleCode, "tenant")) {
            return "租户中心";
        }
        if (StringUtils.equals(moduleCode, "access")) {
            return "身份与权限中心";
        }
        if (StringUtils.equals(moduleCode, "platform")) {
            return "平台设置中心";
        }
        if (StringUtils.equals(moduleCode, "load")) {
            return "负荷聚合";
        }
        if (StringUtils.equals(moduleCode, "tariff")) {
            return "电价服务";
        }
        return moduleCode;
    }

    private void validate(RoleUpsertReq req) {
        if (req == null
                || StringUtils.isBlank(req.getRoleName())
                || StringUtils.isBlank(req.getRoleCode())
                || StringUtils.isBlank(req.getPlatformType())) {
            throw new BaseException(StatusCode.C.getCode(), "角色参数不完整");
        }
    }

    private RolePageItemResp toResp(ConsoleRole item) {
        RolePageItemResp resp = new RolePageItemResp();
        resp.setRoleId(item.getRoleId());
        resp.setRoleName(item.getRoleName());
        resp.setRoleCode(item.getRoleCode());
        resp.setPlatformType(item.getPlatformType());
        resp.setStatus(item.getStatus());
        resp.setRemark(item.getRemark());
        resp.setUpdateTime(item.getUpdateTime());
        return resp;
    }

    private ConsoleRole copy(ConsoleRole source) {
        ConsoleRole target = new ConsoleRole();
        target.setId(source.getId());
        target.setRoleId(source.getRoleId());
        target.setRoleName(source.getRoleName());
        target.setRoleCode(source.getRoleCode());
        target.setPlatformType(source.getPlatformType());
        target.setStatus(source.getStatus());
        target.setRemark(source.getRemark());
        target.setCreateTime(source.getCreateTime());
        target.setUpdateTime(source.getUpdateTime());
        return target;
    }

    private int safeCount(Integer count) {
        return count == null ? 0 : count;
    }
}
