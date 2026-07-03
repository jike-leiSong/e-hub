package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.console.model.resp.OperationLogSimpleResp;
import cn.sl.ehub.console.model.resp.WorkbenchSummaryResp;
import cn.sl.ehub.console.model.resp.WorkbenchTodoResp;
import cn.sl.ehub.console.service.IPlatformWorkbenchService;
import cn.sl.ehub.service.mapper.ConsoleConfigItemMapper;
import cn.sl.ehub.service.mapper.ConsoleOperationLogMapper;
import cn.sl.ehub.service.mapper.ConsoleRoleMapper;
import cn.sl.ehub.service.mapper.ConsoleTenantMapper;
import cn.sl.ehub.service.mapper.ConsoleTenantProductMapper;
import cn.sl.ehub.service.mapper.ConsoleUserMapper;
import cn.sl.ehub.service.mapper.ConsoleUserRoleMapper;
import cn.sl.ehub.service.vo.ConsoleConfigItem;
import cn.sl.ehub.service.vo.ConsoleOperationLog;
import cn.sl.ehub.service.vo.ConsoleTenant;
import cn.sl.ehub.service.vo.ConsoleUser;
import cn.sl.ehub.service.vo.ConsoleUserRole;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PlatformWorkbenchServiceImpl implements IPlatformWorkbenchService {

    private final ConsoleTenantMapper consoleTenantMapper;
    private final ConsoleTenantProductMapper consoleTenantProductMapper;
    private final ConsoleRoleMapper consoleRoleMapper;
    private final ConsoleConfigItemMapper consoleConfigItemMapper;
    private final ConsoleOperationLogMapper consoleOperationLogMapper;
    private final ConsoleUserMapper consoleUserMapper;
    private final ConsoleUserRoleMapper consoleUserRoleMapper;

    @Override
    public WorkbenchSummaryResp summary() {
        WorkbenchSummaryResp resp = new WorkbenchSummaryResp();
        resp.setTenantCount(safeCount(consoleTenantMapper.countAll()));
        resp.setActiveTenantCount(safeCount(consoleTenantMapper.countActive()));
        resp.setUserCount(consoleUserMapper.selectCount(new ConsoleUser()));
        resp.setRoleCount(safeCount(consoleRoleMapper.countAll()));
        resp.setEnabledProductTenantCount(safeCount(consoleTenantProductMapper.countEnabledTenants()));
        resp.setConfigCount(safeCount(consoleConfigItemMapper.countAll()));
        String sinceTime = DateUtils.getTime(new java.util.Date(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000));
        resp.setLast7dOperationCount(safeCount(consoleOperationLogMapper.countRecentSince(sinceTime)));
        return resp;
    }

    @Override
    public List<WorkbenchTodoResp> todos() {
        WorkbenchSummaryResp summary = summary();
        List<WorkbenchTodoResp> list = new ArrayList<>();

        WorkbenchTodoResp tenantTodo = new WorkbenchTodoResp();
        tenantTodo.setType("TENANT_PRODUCT");
        tenantTodo.setTitle("待补产品订阅租户");
        tenantTodo.setCount(Math.max(0, safe(summary.getActiveTenantCount()) - safe(summary.getEnabledProductTenantCount())));
        tenantTodo.setRouteKey("tenant-center");
        tenantTodo.setRouteLabel("租户中心");
        list.add(tenantTodo);

        List<ConsoleUser> users = consoleUserMapper.page(null, null, 1);
        Set<String> userIds = new HashSet<>();
        for (ConsoleUser user : users) {
            if (StringUtils.isNotBlank(user.getUserId())) {
                userIds.add(user.getUserId());
            }
        }
        Set<String> grantedUserIds = new HashSet<>();
        if (!userIds.isEmpty()) {
            List<ConsoleUserRole> roles = consoleUserRoleMapper.listByUserIds(new ArrayList<>(userIds));
            for (ConsoleUserRole role : roles) {
                if (StringUtils.isNotBlank(role.getUserId())) {
                    grantedUserIds.add(role.getUserId());
                }
            }
        }
        WorkbenchTodoResp userTodo = new WorkbenchTodoResp();
        userTodo.setType("USER_ROLE");
        userTodo.setTitle("待授权账号");
        userTodo.setCount(Math.max(0, userIds.size() - grantedUserIds.size()));
        userTodo.setRouteKey("identity-access");
        userTodo.setRouteLabel("身份与权限中心");
        list.add(userTodo);

        List<ConsoleConfigItem> disabledConfigs = consoleConfigItemMapper.page(null, null, 0);
        WorkbenchTodoResp configTodo = new WorkbenchTodoResp();
        configTodo.setType("CONFIG_DISABLED");
        configTodo.setTitle("待启用配置项");
        configTodo.setCount(disabledConfigs == null ? 0 : disabledConfigs.size());
        configTodo.setRouteKey("platform-settings");
        configTodo.setRouteLabel("平台设置中心");
        list.add(configTodo);
        return list;
    }

    @Override
    public List<OperationLogSimpleResp> recentLogs(Integer limit) {
        int actualLimit = limit == null || limit < 1 ? 10 : Math.min(limit, 50);
        List<ConsoleOperationLog> logs = consoleOperationLogMapper.listRecent(actualLimit);
        List<OperationLogSimpleResp> respList = new ArrayList<>();
        for (ConsoleOperationLog item : logs) {
            OperationLogSimpleResp resp = new OperationLogSimpleResp();
            resp.setBizType(item.getBizType());
            resp.setBizId(item.getBizId());
            resp.setAction(item.getAction());
            resp.setOperatorName(item.getOperatorName());
            resp.setResult(item.getResult());
            resp.setCreateTime(item.getCreateTime());
            respList.add(resp);
        }
        return respList;
    }

    private int safeCount(Integer count) {
        return count == null ? 0 : count;
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }
}
