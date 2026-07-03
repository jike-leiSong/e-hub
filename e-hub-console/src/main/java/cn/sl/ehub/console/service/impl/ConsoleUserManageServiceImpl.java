package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.console.model.req.ConsoleUserPageReq;
import cn.sl.ehub.console.model.req.ConsoleUserUpsertReq;
import cn.sl.ehub.console.model.req.UserRoleSaveReq;
import cn.sl.ehub.console.model.req.UserStatusUpdateReq;
import cn.sl.ehub.console.model.resp.ConsoleUserPageItemResp;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.service.IConsoleUserManageService;
import cn.sl.ehub.console.service.IPlatformAuditLogService;
import cn.sl.ehub.service.mapper.ConsoleConfigItemMapper;
import cn.sl.ehub.service.mapper.ConsoleRoleMapper;
import cn.sl.ehub.service.mapper.ConsoleTenantMapper;
import cn.sl.ehub.service.mapper.ConsoleUserMapper;
import cn.sl.ehub.service.mapper.ConsoleUserRoleMapper;
import cn.sl.ehub.service.vo.ConsoleConfigItem;
import cn.sl.ehub.service.vo.ConsoleRole;
import cn.sl.ehub.service.vo.ConsoleTenant;
import cn.sl.ehub.service.vo.ConsoleUser;
import cn.sl.ehub.service.vo.ConsoleUserRole;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ConsoleUserManageServiceImpl implements IConsoleUserManageService {

    private static final String DEFAULT_PASSWORD_CONFIG_KEY = "console.user.defaultPassword";

    private final ConsoleUserMapper consoleUserMapper;
    private final ConsoleTenantMapper consoleTenantMapper;
    private final ConsoleUserRoleMapper consoleUserRoleMapper;
    private final ConsoleRoleMapper consoleRoleMapper;
    private final ConsoleConfigItemMapper consoleConfigItemMapper;
    private final IPlatformAuditLogService platformAuditLogService;

    @Override
    public PageResultVO<ConsoleUserPageItemResp> page(ConsoleUserPageReq req) {
        Integer pageIndex = req.getPageIndex() == null || req.getPageIndex() < 1 ? 1 : req.getPageIndex();
        Integer pageSize = req.getPageSize() == null || req.getPageSize() < 1 ? 20 : req.getPageSize();
        PageHelper.startPage(pageIndex, pageSize);
        List<ConsoleUser> list = consoleUserMapper.page(
                StringUtils.trimToNull(req.getKeyword()),
                StringUtils.trimToNull(req.getTenantId()),
                req.getStatus()
        );
        List<ConsoleUserPageItemResp> respList = enrichUsers(list);
        PageInfo<ConsoleUser> pageInfo = new PageInfo<>(list);
        PageResultVO<ConsoleUserPageItemResp> page = new PageResultVO<>();
        page.setList(respList);
        page.setTotal((int) pageInfo.getTotal());
        page.setPageIndex(pageIndex);
        page.setPageSize(pageSize);
        return page;
    }

    @Override
    public ConsoleUserPageItemResp create(ConsoleUserUpsertReq req) {
        validate(req);
        if (safeCount(consoleUserMapper.countByUsername(req.getUsername(), null)) > 0) {
            throw new BaseException(StatusCode.C.getCode(), "账号已存在");
        }
        ConsoleTenant tenant = requireTenant(req.getTenantId());
        String now = DateUtils.getTime();
        String userId = "USR" + System.currentTimeMillis() + new Random().nextInt(1000);
        String salt = UUID.randomUUID().toString().replace("-", "");
        ConsoleUser entity = new ConsoleUser();
        entity.setUserId(userId);
        entity.setUsername(StringUtils.trim(req.getUsername()));
        entity.setDisplayName(StringUtils.defaultIfBlank(StringUtils.trim(req.getDisplayName()), StringUtils.trim(req.getUsername())));
        entity.setPasswordSalt(salt);
        entity.setPasswordHash(sha256(salt + defaultPassword()));
        entity.setUserType(normalizeUserType(req.getUserType(), tenant));
        entity.setTenantId(tenant.getTenantId());
        entity.setAggregatorId(StringUtils.defaultIfBlank(StringUtils.trim(req.getAggregatorId()), tenant.getAggregatorId()));
        entity.setEntId(StringUtils.defaultIfBlank(StringUtils.trim(req.getEntId()), tenant.getEntId()));
        entity.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        consoleUserMapper.insertSelective(entity);
        platformAuditLogService.record("USER", entity.getUserId(), "CREATE", null, entity, "SUCCESS", null);
        return findRespByUserId(entity.getUserId());
    }

    @Override
    public ConsoleUserPageItemResp update(String userId, ConsoleUserUpsertReq req) {
        validate(req);
        ConsoleUser existing = requireUser(userId);
        if (safeCount(consoleUserMapper.countByUsername(req.getUsername(), userId)) > 0) {
            throw new BaseException(StatusCode.C.getCode(), "账号已存在");
        }
        ConsoleTenant tenant = requireTenant(req.getTenantId());
        ConsoleUser before = copy(existing);
        existing.setUsername(StringUtils.trim(req.getUsername()));
        existing.setDisplayName(StringUtils.defaultIfBlank(StringUtils.trim(req.getDisplayName()), existing.getUsername()));
        existing.setTenantId(tenant.getTenantId());
        existing.setUserType(normalizeUserType(req.getUserType(), tenant));
        existing.setAggregatorId(StringUtils.defaultIfBlank(StringUtils.trim(req.getAggregatorId()), tenant.getAggregatorId()));
        existing.setEntId(StringUtils.defaultIfBlank(StringUtils.trim(req.getEntId()), tenant.getEntId()));
        existing.setStatus(req.getStatus() == null ? existing.getStatus() : req.getStatus());
        existing.setUpdateTime(DateUtils.getTime());
        consoleUserMapper.updateByPrimaryKeySelective(existing);
        platformAuditLogService.record("USER", userId, "UPDATE", before, existing, "SUCCESS", null);
        return findRespByUserId(userId);
    }

    @Override
    public Boolean updateStatus(String userId, UserStatusUpdateReq req) {
        if (req == null || req.getStatus() == null) {
            throw new BaseException(StatusCode.C.getCode(), "状态不能为空");
        }
        ConsoleUser existing = requireUser(userId);
        ConsoleUser before = copy(existing);
        existing.setStatus(req.getStatus());
        existing.setUpdateTime(DateUtils.getTime());
        consoleUserMapper.updateByPrimaryKeySelective(existing);
        platformAuditLogService.record("USER", userId, "STATUS", before, existing, "SUCCESS", null);
        return true;
    }

    @Override
    public Boolean saveRoles(String userId, UserRoleSaveReq req) {
        ConsoleUser user = requireUser(userId);
        List<String> roleIds = req == null ? Collections.emptyList() : req.getRoleIds();
        List<ConsoleRole> roles = roleIds == null || roleIds.isEmpty()
                ? Collections.emptyList()
                : consoleRoleMapper.listByRoleIds(new ArrayList<>(new LinkedHashSet<>(roleIds)));
        if (roles.size() != (roleIds == null ? 0 : new LinkedHashSet<>(roleIds).size())) {
            throw new BaseException(StatusCode.C.getCode(), "存在无效角色");
        }
        String userPlatformType = isAdminUser(user) ? "owner" : "customer";
        for (ConsoleRole role : roles) {
            if (!StringUtils.equals(userPlatformType, role.getPlatformType())) {
                throw new BaseException(StatusCode.C.getCode(), "角色平台类型与用户不匹配");
            }
        }
        List<ConsoleUserRole> before = consoleUserRoleMapper.listByUserId(userId);
        consoleUserRoleMapper.deleteByUserId(userId);
        String now = DateUtils.getTime();
        for (ConsoleRole role : roles) {
            ConsoleUserRole relation = new ConsoleUserRole();
            relation.setUserId(userId);
            relation.setRoleId(role.getRoleId());
            relation.setCreateTime(now);
            relation.setUpdateTime(now);
            consoleUserRoleMapper.insertSelective(relation);
        }
        List<ConsoleUserRole> after = consoleUserRoleMapper.listByUserId(userId);
        platformAuditLogService.record("USER_ROLE", userId, "GRANT", before, after, "SUCCESS", null);
        return true;
    }

    @Override
    public List<ConsoleUserPageItemResp> listByTenantId(String tenantId) {
        if (StringUtils.isBlank(tenantId)) {
            return Collections.emptyList();
        }
        List<ConsoleUser> list = consoleUserMapper.page(null, tenantId, null);
        return enrichUsers(list);
    }

    private List<ConsoleUserPageItemResp> enrichUsers(List<ConsoleUser> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> tenantIds = new LinkedHashSet<>();
        Set<String> userIds = new LinkedHashSet<>();
        for (ConsoleUser user : users) {
            if (StringUtils.isNotBlank(user.getTenantId())) {
                tenantIds.add(user.getTenantId());
            }
            if (StringUtils.isNotBlank(user.getUserId())) {
                userIds.add(user.getUserId());
            }
        }
        Map<String, ConsoleTenant> tenantsById = new HashMap<>();
        if (!tenantIds.isEmpty()) {
            for (ConsoleTenant tenant : consoleTenantMapper.listByTenantIds(new ArrayList<>(tenantIds))) {
                tenantsById.put(tenant.getTenantId(), tenant);
            }
        }
        Map<String, List<String>> roleIdsByUser = new HashMap<>();
        Set<String> roleIds = new LinkedHashSet<>();
        if (!userIds.isEmpty()) {
            for (ConsoleUserRole relation : consoleUserRoleMapper.listByUserIds(new ArrayList<>(userIds))) {
                roleIdsByUser.computeIfAbsent(relation.getUserId(), item -> new ArrayList<>()).add(relation.getRoleId());
                roleIds.add(relation.getRoleId());
            }
        }
        Map<String, ConsoleRole> rolesById = new HashMap<>();
        if (!roleIds.isEmpty()) {
            for (ConsoleRole role : consoleRoleMapper.listByRoleIds(new ArrayList<>(roleIds))) {
                rolesById.put(role.getRoleId(), role);
            }
        }
        List<ConsoleUserPageItemResp> respList = new ArrayList<>();
        for (ConsoleUser user : users) {
            ConsoleUserPageItemResp resp = new ConsoleUserPageItemResp();
            resp.setUserId(user.getUserId());
            resp.setUsername(user.getUsername());
            resp.setDisplayName(user.getDisplayName());
            resp.setTenantId(user.getTenantId());
            ConsoleTenant tenant = tenantsById.get(user.getTenantId());
            resp.setTenantName(tenant == null ? null : tenant.getTenantName());
            resp.setUserType(user.getUserType());
            resp.setAggregatorId(user.getAggregatorId());
            resp.setEntId(user.getEntId());
            resp.setStatus(user.getStatus());
            resp.setLastLoginTime(user.getLastLoginTime());
            List<String> roleIdList = roleIdsByUser.getOrDefault(user.getUserId(), Collections.emptyList());
            resp.setRoleIds(new ArrayList<>(roleIdList));
            List<String> roleNames = new ArrayList<>();
            for (String roleId : roleIdList) {
                ConsoleRole role = rolesById.get(roleId);
                if (role != null) {
                    roleNames.add(role.getRoleName());
                }
            }
            resp.setRoleNames(roleNames);
            respList.add(resp);
        }
        return respList;
    }

    private ConsoleUserPageItemResp findRespByUserId(String userId) {
        List<ConsoleUser> users = consoleUserMapper.listByUserIds(Collections.singletonList(userId));
        if (users.isEmpty()) {
            throw new BaseException(StatusCode.C.getCode(), "用户不存在");
        }
        return enrichUsers(users).get(0);
    }

    private ConsoleUser requireUser(String userId) {
        ConsoleUser user = consoleUserMapper.getByUserId(userId);
        if (user == null) {
            throw new BaseException(StatusCode.C.getCode(), "用户不存在");
        }
        return user;
    }

    private ConsoleTenant requireTenant(String tenantId) {
        if (StringUtils.isBlank(tenantId)) {
            throw new BaseException(StatusCode.C.getCode(), "租户ID不能为空");
        }
        ConsoleTenant tenant = consoleTenantMapper.getByTenantId(tenantId);
        if (tenant == null) {
            throw new BaseException(StatusCode.C.getCode(), "租户不存在");
        }
        return tenant;
    }

    private void validate(ConsoleUserUpsertReq req) {
        if (req == null || StringUtils.isBlank(req.getUsername()) || StringUtils.isBlank(req.getTenantId())) {
            throw new BaseException(StatusCode.C.getCode(), "用户参数不完整");
        }
    }

    private String defaultPassword() {
        ConsoleConfigItem item = consoleConfigItemMapper.getByConfigKey(DEFAULT_PASSWORD_CONFIG_KEY);
        return item == null || StringUtils.isBlank(item.getConfigValue()) ? "123456" : item.getConfigValue();
    }

    private String normalizeUserType(String userType, ConsoleTenant tenant) {
        String value = StringUtils.upperCase(StringUtils.trimToEmpty(userType));
        if ("ADMIN".equals(value) || "PLATFORM".equals(value) || (StringUtils.isBlank(value) && tenant != null && "PLATFORM".equals(tenant.getTenantType()))) {
            return "ADMIN";
        }
        return "CUSTOMER";
    }

    private boolean isAdminUser(ConsoleUser user) {
        return user != null && ("ADMIN".equalsIgnoreCase(user.getUserType()) || "PLATFORM".equalsIgnoreCase(user.getUserType()));
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private ConsoleUser copy(ConsoleUser source) {
        ConsoleUser target = new ConsoleUser();
        target.setId(source.getId());
        target.setUserId(source.getUserId());
        target.setUsername(source.getUsername());
        target.setDisplayName(source.getDisplayName());
        target.setPasswordSalt(source.getPasswordSalt());
        target.setPasswordHash(source.getPasswordHash());
        target.setUserType(source.getUserType());
        target.setAggregatorId(source.getAggregatorId());
        target.setEntId(source.getEntId());
        target.setTenantId(source.getTenantId());
        target.setStatus(source.getStatus());
        target.setLastLoginTime(source.getLastLoginTime());
        target.setCreateTime(source.getCreateTime());
        target.setUpdateTime(source.getUpdateTime());
        return target;
    }

    private int safeCount(Integer count) {
        return count == null ? 0 : count;
    }
}
