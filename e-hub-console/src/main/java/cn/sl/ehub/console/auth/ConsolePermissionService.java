package cn.sl.ehub.console.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import cn.sl.ehub.console.auth.model.AuthLoginResp;
import cn.sl.ehub.console.auth.model.AuthMenuGroupResp;
import cn.sl.ehub.console.auth.model.AuthMenuItemResp;
import cn.sl.ehub.console.auth.model.AuthUserInfoResp;

@Service
public class ConsolePermissionService {

    private static final String PLATFORM_OWNER = "owner";
    private static final String PLATFORM_CUSTOMER = "customer";

    private static final String PAGE_WORKBENCH = "workbench";
    private static final String PAGE_TENANT_CENTER = "tenant-center";
    private static final String PAGE_IDENTITY_ACCESS = "identity-access";
    private static final String PAGE_PLATFORM_SETTINGS = "platform-settings";
    private static final String PAGE_NO_PRODUCT = "no-product";

    private static final String PERM_LOAD_OVERVIEW = "load:overview:view";
    private static final String PERM_LOAD_ADJUSTMENT = "load:adjustment:view";
    private static final String PERM_LOAD_SETTLEMENT = "load:settlement:view";
    private static final String PERM_LOAD_DEVICE_OPERATION = "load:device-operation:view";
    private static final String PERM_LOAD_RESOURCES = "load:resources:view";
    private static final String PERM_TARIFF_QUERY = "tariff:query:view";
    private static final String PERM_TARIFF_API = "tariff:api:view";
    private static final String PERM_TARIFF_LOGS = "tariff:logs:view";
    private static final String PERM_OWNER_TENANT = "owner:tenant:manage";
    private static final String PERM_OWNER_ACCESS = "owner:access:manage";
    private static final String PERM_OWNER_SETTINGS = "owner:settings:manage";

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
            "/entUserDetail/",
            "/userManagement/",
            "/weather/",
            "/issue/",
            "/statusQuery/",
            "/synchronize/",
            "/iot/",
            "/peakPlanDeclare/",
            "/file/"
    );

    private static final List<String> TARIFF_API_PREFIXES = Arrays.asList(
            "/tariff/"
    );

    private static final List<String> OWNER_API_PREFIXES = Arrays.asList(
            "/platform/",
            "/customer/",
            "/console-user/",
            "/permission/",
            "/product/"
    );

    private final ConsoleProductService productService;

    public ConsolePermissionService(ConsoleProductService productService) {
        this.productService = productService;
    }

    public AuthUserInfoResp buildUserInfo(AuthUser user) {
        AuthUserInfoResp resp = new AuthUserInfoResp();
        if (user == null) {
            return resp;
        }
        resp.setUserId(user.getUserId());
        resp.setUsername(user.getUsername());
        resp.setDisplayName(user.getDisplayName());
        resp.setUserType(productService.normalizeUserType(user.getUserType()));
        resp.setAggregatorId(user.getAggregatorId());
        resp.setEntId(user.getEntId());
        resp.setPlatformType(platformType(user));
        resp.setRole(role(user));
        resp.setProducts(products(user));
        resp.setPermissions(permissions(user));
        resp.setAllowedPages(allowedPages(user));
        resp.setDefaultPage(defaultPage(user));
        resp.setMenuGroups(menuGroups(user));
        return resp;
    }

    public void fillLoginResp(AuthLoginResp resp, AuthUser user) {
        AuthUserInfoResp profile = buildUserInfo(user);
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
        if (isAdmin(user)) {
            return true;
        }
        List<String> products = products(user);
        if (startsWithAny(path, LOAD_API_PREFIXES)) {
            return products.contains(ConsoleProductService.PRODUCT_LOAD);
        }
        if (startsWithAny(path, TARIFF_API_PREFIXES)) {
            return products.contains(ConsoleProductService.PRODUCT_TARIFF);
        }
        if (startsWithAny(path, OWNER_API_PREFIXES)) {
            return false;
        }
        return false;
    }

    private String platformType(AuthUser user) {
        return isAdmin(user) ? PLATFORM_OWNER : PLATFORM_CUSTOMER;
    }

    private String role(AuthUser user) {
        if (isAdmin(user)) {
            return "owner_admin";
        }
        return "customer_user";
    }

    private List<String> products(AuthUser user) {
        return productService.enabledProducts(user);
    }

    private List<String> permissions(AuthUser user) {
        List<String> permissions = new ArrayList<>();
        if (isAdmin(user)) {
            permissions.add(PERM_OWNER_TENANT);
            permissions.add(PERM_OWNER_ACCESS);
            permissions.add(PERM_OWNER_SETTINGS);
        }
        List<String> products = products(user);
        if (products.contains(ConsoleProductService.PRODUCT_LOAD)) {
            permissions.add(PERM_LOAD_OVERVIEW);
            permissions.add(PERM_LOAD_ADJUSTMENT);
            permissions.add(PERM_LOAD_SETTLEMENT);
            permissions.add(PERM_LOAD_DEVICE_OPERATION);
            permissions.add(PERM_LOAD_RESOURCES);
        }
        if (products.contains(ConsoleProductService.PRODUCT_TARIFF)) {
            permissions.add(PERM_TARIFF_QUERY);
            permissions.add(PERM_TARIFF_API);
            permissions.add(PERM_TARIFF_LOGS);
        }
        return permissions;
    }

    private List<String> allowedPages(AuthUser user) {
        List<String> pages = new ArrayList<>();
        if (isAdmin(user)) {
            pages.add(PAGE_WORKBENCH);
            pages.add(PAGE_TENANT_CENTER);
            pages.add(PAGE_IDENTITY_ACCESS);
            pages.add(PAGE_PLATFORM_SETTINGS);
        }
        List<String> products = products(user);
        if (products.contains(ConsoleProductService.PRODUCT_LOAD)) {
            pages.add("load-overview");
            pages.add("load-adjustment");
            pages.add("load-settlement");
            pages.add("load-resources");
            pages.add("load-device-operation");
        }
        if (products.contains(ConsoleProductService.PRODUCT_TARIFF)) {
            pages.add("tariff-query");
            pages.add("tariff-api");
            pages.add("tariff-logs");
        }
        if (!isAdmin(user) && pages.isEmpty()) {
            pages.add(PAGE_NO_PRODUCT);
        }
        return pages;
    }

    private String defaultPage(AuthUser user) {
        if (isAdmin(user)) {
            return PAGE_WORKBENCH;
        }
        List<String> products = products(user);
        if (products.contains(ConsoleProductService.PRODUCT_LOAD)) {
            return "load-overview";
        }
        if (products.contains(ConsoleProductService.PRODUCT_TARIFF)) {
            return "tariff-query";
        }
        return PAGE_NO_PRODUCT;
    }

    private List<AuthMenuGroupResp> menuGroups(AuthUser user) {
        List<String> products = products(user);
        if (isAdmin(user)) {
            List<AuthMenuGroupResp> groups = new ArrayList<>();
            groups.add(group("平台治理", Arrays.asList(
                    item(PAGE_WORKBENCH, "工作台", "01"),
                    item(PAGE_TENANT_CENTER, "租户中心", "02"),
                    item(PAGE_IDENTITY_ACCESS, "身份与权限中心", "03"),
                    item(PAGE_PLATFORM_SETTINGS, "平台设置中心", "04")
            )));
            groups.add(group("产品能力", productMenuItems(products)));
            return groups;
        }
        List<AuthMenuItemResp> productMenus = productMenuItems(products);
        if (productMenus.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(group("产品能力", productMenus));
    }

    private List<AuthMenuItemResp> productMenuItems(List<String> products) {
        List<AuthMenuItemResp> items = new ArrayList<>();
        if (products.contains(ConsoleProductService.PRODUCT_LOAD)) {
            items.add(item("load", "负荷聚合", "07", Arrays.asList(
                    item("load-overview", "运营总览", null),
                    item("load-adjustment", "调节情况", null),
                    item("load-settlement", "收益结算", null),
                    item("load-resources", "资源管理", null),
                    item("load-device-operation", "物联管理", null)
            )));
        }
        if (products.contains(ConsoleProductService.PRODUCT_TARIFF)) {
            items.add(item("tariff", "电价服务", "08", Arrays.asList(
                    item("tariff-query", "电网代理价格", null),
                    item("tariff-api", "接口能力", null),
                    item("tariff-logs", "调用记录", null)
            )));
        }
        return items;
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

    private boolean isAdmin(AuthUser user) {
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

}
