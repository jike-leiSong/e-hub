package cn.sl.ehub.console.auth;

import cn.sl.ehub.console.auth.model.AuthLoginResp;
import cn.sl.ehub.console.auth.model.AuthMenuGroupResp;
import cn.sl.ehub.console.auth.model.AuthMenuItemResp;
import cn.sl.ehub.console.auth.model.AuthUserInfoResp;
import cn.sl.ehub.service.mapper.ConsolePermissionMapper;
import cn.sl.ehub.service.mapper.ConsoleRoleMapper;
import cn.sl.ehub.service.mapper.ConsoleRolePermissionMapper;
import cn.sl.ehub.service.mapper.ConsoleUserRoleMapper;
import cn.sl.ehub.service.vo.ConsolePermission;
import cn.sl.ehub.service.vo.ConsoleRole;
import cn.sl.ehub.service.vo.ConsoleRolePermission;
import cn.sl.ehub.service.vo.ConsoleUserRole;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ConsolePermissionService {

    private static final String PLATFORM_OWNER = "owner";
    private static final String PLATFORM_CUSTOMER = "customer";

    private static final String PAGE_WORKBENCH = "workbench";
    private static final String PAGE_TENANT_CENTER = "tenant-center";
    private static final String PAGE_IDENTITY_ACCESS = "identity-access";
    private static final String PAGE_PLATFORM_SETTINGS = "platform-settings";
    private static final String PAGE_NO_PRODUCT = "no-product";

    private static final String PAGE_LOAD_OVERVIEW = "load-overview";
    private static final String PAGE_LOAD_ADJUSTMENT = "load-adjustment";
    private static final String PAGE_LOAD_SETTLEMENT = "load-settlement";
    private static final String PAGE_LOAD_RESOURCES = "load-resources";
    private static final String PAGE_LOAD_DEVICE_OPERATION = "load-device-operation";
    private static final String PAGE_LOAD_GRID_INTERACTION = "load-grid-interaction";
    private static final String PAGE_TARIFF_QUERY = "tariff-query";
    private static final String PAGE_TARIFF_IMPORT = "tariff-import";
    private static final String PAGE_TARIFF_SOURCES = "tariff-sources";
    private static final String PAGE_TARIFF_API = "tariff-api";
    private static final String PAGE_TARIFF_LOGS = "tariff-logs";

    private static final String PERM_LOAD_OVERVIEW = "load:overview:view";
    private static final String PERM_LOAD_ADJUSTMENT = "load:adjustment:view";
    private static final String PERM_LOAD_SETTLEMENT = "load:settlement:view";
    private static final String PERM_LOAD_DEVICE_OPERATION = "load:device-operation:view";
    private static final String PERM_LOAD_GRID_INTERACTION = "load:grid-interaction:view";
    private static final String PERM_LOAD_GRID_INTERACTION_DELIVERY = "load:grid-interaction:delivery";
    private static final String PERM_LOAD_GRID_INTERACTION_AUDIT = "load:grid-interaction:audit";
    private static final String PERM_LOAD_GRID_INTERACTION_EXPORT = "load:grid-interaction:export";
    private static final String LEGACY_PERM_LOAD_GRID_DELIVERY = "load:grid-delivery:view";
    private static final String LEGACY_PERM_LOAD_GRID_DELIVERY_MANAGE = "load:grid-delivery:manage";
    private static final String LEGACY_PERM_LOAD_GRID_DELIVERY_EXPORT = "load:grid-delivery:export";
    private static final String PERM_LOAD_RESOURCES = "load:resources:view";
    private static final String PERM_TARIFF_QUERY = "tariff:query:view";
    private static final String PERM_TARIFF_IMPORT = "tariff:import:manage";
    private static final String PERM_TARIFF_SOURCES = "tariff:sources:view";
    private static final String PERM_TARIFF_API = "tariff:api:view";
    private static final String PERM_TARIFF_LOGS = "tariff:logs:view";
    private static final String PERM_OWNER_TENANT = "owner:tenant:manage";
    private static final String PERM_OWNER_ACCESS = "owner:access:manage";
    private static final String PERM_OWNER_SETTINGS = "owner:settings:manage";

    private static final List<String> LOAD_PERMISSION_CODES = Arrays.asList(
            PERM_LOAD_OVERVIEW,
            PERM_LOAD_ADJUSTMENT,
            PERM_LOAD_SETTLEMENT,
            PERM_LOAD_RESOURCES,
            PERM_LOAD_DEVICE_OPERATION,
            PERM_LOAD_GRID_INTERACTION,
            PERM_LOAD_GRID_INTERACTION_DELIVERY,
            PERM_LOAD_GRID_INTERACTION_AUDIT,
            PERM_LOAD_GRID_INTERACTION_EXPORT,
            LEGACY_PERM_LOAD_GRID_DELIVERY,
            LEGACY_PERM_LOAD_GRID_DELIVERY_MANAGE,
            LEGACY_PERM_LOAD_GRID_DELIVERY_EXPORT
    );

    private static final List<String> TARIFF_PERMISSION_CODES = Arrays.asList(
            PERM_TARIFF_QUERY,
            PERM_TARIFF_IMPORT,
            PERM_TARIFF_SOURCES,
            PERM_TARIFF_API,
            PERM_TARIFF_LOGS
    );

    private static final List<String> LOAD_API_PREFIXES = Arrays.asList(
            "/profit/",
            "/yesterday/",
            "/today/",
            "/tomorrow/",
            "/historyQuery/",
            "/aggregator/",
            "/aggregatorPlan/",
            "/applyPlan/",
            "/ent/",
            "/ent-device/",
            "/entUserDetail/",
            "/entPlan/",
            "/entAppPlan/",
            "/weather/",
            "/issue/",
            "/statusQuery/",
            "/synchronize/",
            "/iot/",
            "/model/",
            "/peakPlanDeclare/",
            "/file/",
            "/grid-interaction/",
            "/grid-delivery-quality/"
    );

    private static final List<String> TARIFF_QUERY_API_PREFIXES = Arrays.asList(
            "/tariff/agent-price/",
            "/haomaidian/index/",
            "/areaDict/"
    );

    private static final List<String> TARIFF_IMPORT_API_PREFIXES = Collections.singletonList(
            "/tariff/agent-price/import/"
    );

    private static final List<String> TARIFF_SOURCE_API_PREFIXES = Collections.singletonList(
            "/tariff/sources/"
    );

    private static final List<String> TARIFF_OPEN_API_PREFIXES = Collections.singletonList(
            "/openapi/v1/tariff/agent/"
    );

    private static final List<String> TENANT_API_PREFIXES = Arrays.asList(
            "/tenant/",
            "/product/"
    );

    private static final List<String> ACCESS_API_PREFIXES = Arrays.asList(
            "/console-user/",
            "/permission/"
    );

    private static final List<String> SETTINGS_API_PREFIXES = Arrays.asList(
            "/platform/config/",
            "/platform/dict/",
            "/platform/audit/"
    );

    private final ConsoleProductService productService;
    private final ConsoleUserRoleMapper consoleUserRoleMapper;
    private final ConsoleRoleMapper consoleRoleMapper;
    private final ConsoleRolePermissionMapper consoleRolePermissionMapper;
    private final ConsolePermissionMapper consolePermissionMapper;

    public ConsolePermissionService(ConsoleProductService productService,
                                    ConsoleUserRoleMapper consoleUserRoleMapper,
                                    ConsoleRoleMapper consoleRoleMapper,
                                    ConsoleRolePermissionMapper consoleRolePermissionMapper,
                                    ConsolePermissionMapper consolePermissionMapper) {
        this.productService = productService;
        this.consoleUserRoleMapper = consoleUserRoleMapper;
        this.consoleRoleMapper = consoleRoleMapper;
        this.consoleRolePermissionMapper = consoleRolePermissionMapper;
        this.consolePermissionMapper = consolePermissionMapper;
    }

    public AuthUserInfoResp buildUserInfo(AuthUser user) {
        AuthUserInfoResp resp = new AuthUserInfoResp();
        if (user == null) {
            return resp;
        }
        UserPermissionProfile profile = resolvePermissionProfile(user);
        List<String> allowedPages = allowedPages(user, profile);

        resp.setUserId(user.getUserId());
        resp.setUsername(user.getUsername());
        resp.setDisplayName(user.getDisplayName());
        resp.setUserType(productService.normalizeUserType(user.getUserType()));
        resp.setAggregatorId(user.getAggregatorId());
        resp.setEntId(user.getEntId());
        resp.setTenantId(user.getTenantId());
        resp.setPlatformType(platformType(user));
        resp.setRole(primaryRole(profile, user));
        resp.setProducts(profile.getProducts());
        resp.setPermissions(profile.getPermissionCodes());
        resp.setAllowedPages(allowedPages);
        resp.setDefaultPage(defaultPage(user, allowedPages));
        resp.setMenuGroups(menuGroups(user, profile, allowedPages));
        return resp;
    }

    public void fillLoginResp(AuthLoginResp resp, AuthUser user) {
        AuthUserInfoResp profile = buildUserInfo(user);
        resp.setTenantId(profile.getTenantId());
        resp.setPlatformType(profile.getPlatformType());
        resp.setRole(profile.getRole());
        resp.setProducts(profile.getProducts());
        resp.setPermissions(profile.getPermissions());
        resp.setAllowedPages(profile.getAllowedPages());
        resp.setDefaultPage(profile.getDefaultPage());
        resp.setMenuGroups(profile.getMenuGroups());
    }

    public boolean hasRequestPermission(HttpServletRequest request, AuthUser user) {
        if (user == null) {
            return false;
        }
        String path = normalizePath(request);
        if (StringUtils.startsWith(path, "/auth/")) {
            return true;
        }

        UserPermissionProfile profile = resolvePermissionProfile(user);
        if (StringUtils.startsWith(path, "/platform/workbench/")) {
            return StringUtils.equals(platformType(user), PLATFORM_OWNER);
        }
        if (startsWithAny(path, TENANT_API_PREFIXES)) {
            return hasPermission(profile, PERM_OWNER_TENANT);
        }
        if (startsWithAny(path, ACCESS_API_PREFIXES)) {
            return hasPermission(profile, PERM_OWNER_ACCESS);
        }
        if (startsWithAny(path, SETTINGS_API_PREFIXES)) {
            return hasPermission(profile, PERM_OWNER_SETTINGS);
        }
        if (startsWithAny(path, TARIFF_IMPORT_API_PREFIXES)) {
            return hasProduct(profile, ConsoleProductService.PRODUCT_TARIFF)
                    && hasPermission(profile, PERM_TARIFF_IMPORT);
        }
        if (startsWithAny(path, TARIFF_OPEN_API_PREFIXES)) {
            return hasProduct(profile, ConsoleProductService.PRODUCT_TARIFF)
                    && hasPermission(profile, PERM_TARIFF_API);
        }
        if (startsWithAny(path, TARIFF_SOURCE_API_PREFIXES)) {
            return hasProduct(profile, ConsoleProductService.PRODUCT_TARIFF)
                    && hasPermission(profile, PERM_TARIFF_SOURCES);
        }
        if (startsWithAny(path, TARIFF_QUERY_API_PREFIXES)) {
            return hasProduct(profile, ConsoleProductService.PRODUCT_TARIFF)
                    && hasPermission(profile, PERM_TARIFF_QUERY);
        }
        if (StringUtils.startsWith(path, "/peakPlanDeclare/")) {
            return hasProduct(profile, ConsoleProductService.PRODUCT_LOAD)
                    && hasAnyPermission(profile, Arrays.asList(PERM_LOAD_GRID_INTERACTION_DELIVERY,
                    LEGACY_PERM_LOAD_GRID_DELIVERY_MANAGE));
        }
        if (StringUtils.startsWith(path, "/grid-interaction/")
                || StringUtils.startsWith(path, "/grid-delivery-quality/")) {
            if (!hasProduct(profile, ConsoleProductService.PRODUCT_LOAD)) {
                return false;
            }
            if (StringUtils.contains(path, "/export") || StringUtils.contains(path, "/report")) {
                return hasAnyPermission(profile, Arrays.asList(PERM_LOAD_GRID_INTERACTION_EXPORT,
                        PERM_LOAD_GRID_INTERACTION_DELIVERY, LEGACY_PERM_LOAD_GRID_DELIVERY_EXPORT,
                        LEGACY_PERM_LOAD_GRID_DELIVERY_MANAGE));
            }
            boolean writeRequest = !StringUtils.equalsIgnoreCase(request.getMethod(), "GET");
            if (StringUtils.contains(path, "/operations/") && writeRequest) {
                return hasAnyPermission(profile, Arrays.asList(PERM_LOAD_GRID_INTERACTION_DELIVERY,
                        LEGACY_PERM_LOAD_GRID_DELIVERY_MANAGE));
            }
            if (StringUtils.contains(path, "/recalculate")
                    || (writeRequest && (StringUtils.contains(path, "/snapshot")
                    || StringUtils.contains(path, "/periods") || StringUtils.contains(path, "/issues/")
                    || StringUtils.contains(path, "/market-status")))) {
                return hasAnyPermission(profile, Arrays.asList(PERM_LOAD_GRID_INTERACTION_AUDIT,
                        LEGACY_PERM_LOAD_GRID_DELIVERY_MANAGE));
            }
            return hasAnyPermission(profile, Arrays.asList(PERM_LOAD_GRID_INTERACTION,
                    LEGACY_PERM_LOAD_GRID_DELIVERY));
        }
        if (startsWithAny(path, LOAD_API_PREFIXES)) {
            return hasProduct(profile, ConsoleProductService.PRODUCT_LOAD)
                    && hasAnyPermission(profile, LOAD_PERMISSION_CODES);
        }
        return false;
    }

    private UserPermissionProfile resolvePermissionProfile(AuthUser user) {
        List<String> enabledProducts = products(user);
        List<String> assignedRoleIds = listAssignedRoleIds(user == null ? null : user.getUserId());
        if (!assignedRoleIds.isEmpty()) {
            List<ConsoleRole> roles = listEnabledRoles(assignedRoleIds, platformType(user));
            List<String> roleCodes = new ArrayList<>();
            for (ConsoleRole role : roles) {
                if (StringUtils.isNotBlank(role.getRoleCode()) && !roleCodes.contains(role.getRoleCode())) {
                    roleCodes.add(role.getRoleCode());
                }
            }
            return new UserPermissionProfile(enabledProducts, roleCodes, loadPermissionCodes(roles));
        }
        return new UserPermissionProfile(
                enabledProducts,
                Collections.singletonList(defaultRole(user)),
                defaultPermissionCodes(user, enabledProducts)
        );
    }

    private List<String> listAssignedRoleIds(String userId) {
        if (StringUtils.isBlank(userId)) {
            return Collections.emptyList();
        }
        List<ConsoleUserRole> relations = consoleUserRoleMapper.listByUserId(userId);
        if (relations == null || relations.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> roleIds = new LinkedHashSet<>();
        for (ConsoleUserRole relation : relations) {
            if (StringUtils.isNotBlank(relation.getRoleId())) {
                roleIds.add(relation.getRoleId());
            }
        }
        return new ArrayList<>(roleIds);
    }

    private List<ConsoleRole> listEnabledRoles(List<String> assignedRoleIds, String platformType) {
        if (assignedRoleIds == null || assignedRoleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<ConsoleRole> roles = consoleRoleMapper.listByRoleIds(assignedRoleIds);
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, ConsoleRole> roleById = new HashMap<>();
        for (ConsoleRole role : roles) {
            if (role != null) {
                roleById.put(role.getRoleId(), role);
            }
        }
        List<ConsoleRole> enabledRoles = new ArrayList<>();
        for (String roleId : assignedRoleIds) {
            ConsoleRole role = roleById.get(roleId);
            if (role == null || !Integer.valueOf(1).equals(role.getStatus())) {
                continue;
            }
            if (StringUtils.isNotBlank(platformType)
                    && StringUtils.isNotBlank(role.getPlatformType())
                    && !StringUtils.equalsIgnoreCase(platformType, role.getPlatformType())) {
                continue;
            }
            enabledRoles.add(role);
        }
        return enabledRoles;
    }

    private List<String> loadPermissionCodes(List<ConsoleRole> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> roleIds = new ArrayList<>();
        for (ConsoleRole role : roles) {
            if (StringUtils.isNotBlank(role.getRoleId())) {
                roleIds.add(role.getRoleId());
            }
        }
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<ConsoleRolePermission> rolePermissions = consoleRolePermissionMapper.listByRoleIds(roleIds);
        if (rolePermissions == null || rolePermissions.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> permissionCodeSet = new LinkedHashSet<>();
        for (ConsoleRolePermission rolePermission : rolePermissions) {
            if (StringUtils.isNotBlank(rolePermission.getPermissionCode())) {
                permissionCodeSet.add(rolePermission.getPermissionCode());
            }
        }
        if (permissionCodeSet.isEmpty()) {
            return Collections.emptyList();
        }
        List<ConsolePermission> permissions = consolePermissionMapper.listByCodes(new ArrayList<>(permissionCodeSet));
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> permissionCodes = new ArrayList<>();
        for (ConsolePermission permission : permissions) {
            if (permission != null
                    && Integer.valueOf(1).equals(permission.getStatus())
                    && StringUtils.isNotBlank(permission.getPermissionCode())
                    && !permissionCodes.contains(permission.getPermissionCode())) {
                permissionCodes.add(permission.getPermissionCode());
            }
        }
        return permissionCodes;
    }

    private List<String> products(AuthUser user) {
        return productService.enabledProducts(user);
    }

    private List<String> defaultPermissionCodes(AuthUser user, List<String> products) {
        List<String> permissionCodes = new ArrayList<>();
        if (isOwner(user)) {
            permissionCodes.add(PERM_OWNER_TENANT);
            permissionCodes.add(PERM_OWNER_ACCESS);
            permissionCodes.add(PERM_OWNER_SETTINGS);
        }
        if (products.contains(ConsoleProductService.PRODUCT_LOAD)) {
            permissionCodes.addAll(LOAD_PERMISSION_CODES);
        }
        if (products.contains(ConsoleProductService.PRODUCT_TARIFF)) {
            if (isOwner(user)) {
                permissionCodes.addAll(TARIFF_PERMISSION_CODES);
            } else {
                // 客户默认只读电价查询、接口和调用记录；电价导入、数据源维护必须通过显式运营角色授权。
                permissionCodes.add(PERM_TARIFF_QUERY);
                permissionCodes.add(PERM_TARIFF_API);
                permissionCodes.add(PERM_TARIFF_LOGS);
            }
        }
        return permissionCodes;
    }

    private List<String> allowedPages(AuthUser user, UserPermissionProfile profile) {
        List<String> pages = new ArrayList<>();
        if (isOwner(user)) {
            pages.add(PAGE_WORKBENCH);
        }
        if (hasPermission(profile, PERM_OWNER_TENANT)) {
            pages.add(PAGE_TENANT_CENTER);
        }
        if (hasPermission(profile, PERM_OWNER_ACCESS)) {
            pages.add(PAGE_IDENTITY_ACCESS);
        }
        if (hasPermission(profile, PERM_OWNER_SETTINGS)) {
            pages.add(PAGE_PLATFORM_SETTINGS);
        }
        if (hasProduct(profile, ConsoleProductService.PRODUCT_LOAD) && hasPermission(profile, PERM_LOAD_OVERVIEW)) {
            pages.add(PAGE_LOAD_OVERVIEW);
        }
        if (hasProduct(profile, ConsoleProductService.PRODUCT_LOAD) && hasPermission(profile, PERM_LOAD_ADJUSTMENT)) {
            pages.add(PAGE_LOAD_ADJUSTMENT);
        }
        if (hasProduct(profile, ConsoleProductService.PRODUCT_LOAD) && hasPermission(profile, PERM_LOAD_SETTLEMENT)) {
            pages.add(PAGE_LOAD_SETTLEMENT);
        }
        if (hasProduct(profile, ConsoleProductService.PRODUCT_LOAD) && hasPermission(profile, PERM_LOAD_RESOURCES)) {
            pages.add(PAGE_LOAD_RESOURCES);
        }
        if (hasProduct(profile, ConsoleProductService.PRODUCT_LOAD) && hasPermission(profile, PERM_LOAD_DEVICE_OPERATION)) {
            pages.add(PAGE_LOAD_DEVICE_OPERATION);
        }
        if (hasProduct(profile, ConsoleProductService.PRODUCT_LOAD)
                && hasAnyPermission(profile, Arrays.asList(PERM_LOAD_GRID_INTERACTION,
                LEGACY_PERM_LOAD_GRID_DELIVERY))) {
            pages.add(PAGE_LOAD_GRID_INTERACTION);
        }
        if (hasProduct(profile, ConsoleProductService.PRODUCT_TARIFF) && hasPermission(profile, PERM_TARIFF_QUERY)) {
            pages.add(PAGE_TARIFF_QUERY);
        }
        if (hasProduct(profile, ConsoleProductService.PRODUCT_TARIFF) && hasPermission(profile, PERM_TARIFF_IMPORT)) {
            pages.add(PAGE_TARIFF_IMPORT);
        }
        if (hasProduct(profile, ConsoleProductService.PRODUCT_TARIFF) && hasPermission(profile, PERM_TARIFF_SOURCES)) {
            pages.add(PAGE_TARIFF_SOURCES);
        }
        if (hasProduct(profile, ConsoleProductService.PRODUCT_TARIFF) && hasPermission(profile, PERM_TARIFF_API)) {
            pages.add(PAGE_TARIFF_API);
        }
        if (hasProduct(profile, ConsoleProductService.PRODUCT_TARIFF) && hasPermission(profile, PERM_TARIFF_LOGS)) {
            pages.add(PAGE_TARIFF_LOGS);
        }
        if (!isOwner(user) && pages.isEmpty()) {
            pages.add(PAGE_NO_PRODUCT);
        }
        return pages;
    }

    private String defaultPage(AuthUser user, List<String> allowedPages) {
        if (allowedPages.contains(PAGE_WORKBENCH)) {
            return PAGE_WORKBENCH;
        }
        if (!allowedPages.isEmpty()) {
            return allowedPages.get(0);
        }
        return isOwner(user) ? PAGE_WORKBENCH : PAGE_NO_PRODUCT;
    }

    private List<AuthMenuGroupResp> menuGroups(AuthUser user,
                                               UserPermissionProfile profile,
                                               List<String> allowedPages) {
        List<AuthMenuGroupResp> groups = new ArrayList<>();
        if (isOwner(user)) {
            List<AuthMenuItemResp> ownerItems = new ArrayList<>();
            ownerItems.add(item(PAGE_WORKBENCH, "工作台", "01"));
            if (allowedPages.contains(PAGE_TENANT_CENTER)) {
                ownerItems.add(item(PAGE_TENANT_CENTER, "租户中心", "02"));
            }
            if (allowedPages.contains(PAGE_IDENTITY_ACCESS)) {
                ownerItems.add(item(PAGE_IDENTITY_ACCESS, "身份与权限中心", "03"));
            }
            if (allowedPages.contains(PAGE_PLATFORM_SETTINGS)) {
                ownerItems.add(item(PAGE_PLATFORM_SETTINGS, "平台设置中心", "04"));
            }
            groups.add(group("平台治理", ownerItems));
        }

        List<AuthMenuItemResp> productItems = productMenuItems(profile, allowedPages);
        if (!productItems.isEmpty()) {
            groups.add(group("产品能力", productItems));
        }
        return groups;
    }

    private List<AuthMenuItemResp> productMenuItems(UserPermissionProfile profile, List<String> allowedPages) {
        List<AuthMenuItemResp> items = new ArrayList<>();
        if (hasProduct(profile, ConsoleProductService.PRODUCT_LOAD)) {
            List<AuthMenuItemResp> children = new ArrayList<>();
            if (allowedPages.contains(PAGE_LOAD_OVERVIEW)) {
                children.add(item(PAGE_LOAD_OVERVIEW, "运营总览", null));
            }
            if (allowedPages.contains(PAGE_LOAD_ADJUSTMENT)) {
                children.add(item(PAGE_LOAD_ADJUSTMENT, "调节情况", null));
            }
            if (allowedPages.contains(PAGE_LOAD_SETTLEMENT)) {
                children.add(item(PAGE_LOAD_SETTLEMENT, "收益结算", null));
            }
            if (allowedPages.contains(PAGE_LOAD_RESOURCES)) {
                children.add(item(PAGE_LOAD_RESOURCES, "资源管理", null));
            }
            if (allowedPages.contains(PAGE_LOAD_DEVICE_OPERATION)) {
                children.add(item(PAGE_LOAD_DEVICE_OPERATION, "物联管理", null));
            }
            if (allowedPages.contains(PAGE_LOAD_GRID_INTERACTION)) {
                children.add(item(PAGE_LOAD_GRID_INTERACTION, "电网交互", null));
            }
            if (!children.isEmpty()) {
                items.add(item("load", "负荷聚合", "07", children));
            }
        }
        if (hasProduct(profile, ConsoleProductService.PRODUCT_TARIFF)) {
            List<AuthMenuItemResp> children = new ArrayList<>();
            if (allowedPages.contains(PAGE_TARIFF_QUERY)) {
                children.add(item(PAGE_TARIFF_QUERY, "电网代理价格", null));
            }
            if (allowedPages.contains(PAGE_TARIFF_IMPORT)) {
                children.add(item(PAGE_TARIFF_IMPORT, "电价录入", null));
            }
            if (allowedPages.contains(PAGE_TARIFF_SOURCES)) {
                children.add(item(PAGE_TARIFF_SOURCES, "数据来源", null));
            }
            if (allowedPages.contains(PAGE_TARIFF_API)) {
                children.add(item(PAGE_TARIFF_API, "接口能力", null));
            }
            if (allowedPages.contains(PAGE_TARIFF_LOGS)) {
                children.add(item(PAGE_TARIFF_LOGS, "调用记录", null));
            }
            if (!children.isEmpty()) {
                items.add(item("tariff", "电价服务", "08", children));
            }
        }
        return items;
    }

    private boolean hasProduct(UserPermissionProfile profile, String productCode) {
        return profile.getProducts().contains(productCode);
    }

    private boolean hasPermission(UserPermissionProfile profile, String permissionCode) {
        return profile.getPermissionCodes().contains(permissionCode);
    }

    private boolean hasAnyPermission(UserPermissionProfile profile, List<String> permissionCodes) {
        for (String permissionCode : permissionCodes) {
            if (hasPermission(profile, permissionCode)) {
                return true;
            }
        }
        return false;
    }

    private String primaryRole(UserPermissionProfile profile, AuthUser user) {
        if (profile != null && profile.getRoleCodes() != null && !profile.getRoleCodes().isEmpty()) {
            return profile.getRoleCodes().get(0);
        }
        return defaultRole(user);
    }

    private String defaultRole(AuthUser user) {
        return isOwner(user) ? "owner_admin" : "customer_user";
    }

    private AuthMenuGroupResp group(String title, List<AuthMenuItemResp> items) {
        AuthMenuGroupResp group = new AuthMenuGroupResp();
        group.setTitle(title);
        group.setItems(items);
        return group;
    }

    private AuthMenuItemResp item(String key, String label, String icon) {
        return item(key, label, icon, null);
    }

    private AuthMenuItemResp item(String key, String label, String icon, List<AuthMenuItemResp> children) {
        AuthMenuItemResp item = new AuthMenuItemResp();
        item.setKey(key);
        item.setLabel(label);
        item.setIcon(icon);
        item.setChildren(children);
        return item;
    }

    private String normalizePath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.isNotBlank(contextPath) && StringUtils.startsWith(path, contextPath)) {
            path = StringUtils.substring(path, contextPath.length());
        }
        return StringUtils.defaultIfBlank(path, "/");
    }

    private String platformType(AuthUser user) {
        return isOwner(user) ? PLATFORM_OWNER : PLATFORM_CUSTOMER;
    }

    private boolean isOwner(AuthUser user) {
        return user != null && productService.isAdmin(user.getUserType());
    }

    private boolean startsWithAny(String path, List<String> prefixes) {
        for (String prefix : prefixes) {
            if (StringUtils.startsWith(path, prefix)) {
                return true;
            }
        }
        return false;
    }

    private static class UserPermissionProfile {
        private final List<String> products;
        private final List<String> roleCodes;
        private final List<String> permissionCodes;

        private UserPermissionProfile(List<String> products, List<String> roleCodes, List<String> permissionCodes) {
            this.products = products == null ? Collections.emptyList() : products;
            this.roleCodes = roleCodes == null ? Collections.emptyList() : roleCodes;
            this.permissionCodes = permissionCodes == null ? Collections.emptyList() : permissionCodes;
        }

        public List<String> getProducts() {
            return products;
        }

        public List<String> getRoleCodes() {
            return roleCodes;
        }

        public List<String> getPermissionCodes() {
            return permissionCodes;
        }
    }
}
