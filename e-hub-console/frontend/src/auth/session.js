import service from "@/services/http";

const AUTH_USER_KEY = "ehub-auth-user";
const PRODUCT_LOAD = "load_aggregation";
const PRODUCT_TARIFF = "tariff";
const ALL_PRODUCTS = [PRODUCT_LOAD, PRODUCT_TARIFF];

export function normalizeAuthUser(authUser = {}) {
  const userType = normalizeUserType(authUser.userType, authUser.platformType);
  const platformType = userType === "ADMIN" ? "owner" : "customer";
  const products = Array.isArray(authUser.products)
    ? normalizeProducts(authUser.products)
    : platformType === "owner"
      ? ALL_PRODUCTS
      : [];
  const fallback = fallbackProfile(platformType, products);
  const permissions = Array.isArray(authUser.permissions) ? authUser.permissions : fallback.permissions;
  const allowedPages = nonEmptyArray(authUser.allowedPages) ? authUser.allowedPages : fallback.allowedPages;
  const menuGroups = nonEmptyArray(authUser.menuGroups) ? authUser.menuGroups : fallback.menuGroups;
  const defaultPage = allowedPages.includes(authUser.defaultPage)
    ? authUser.defaultPage
    : allowedPages.includes(fallback.defaultPage)
      ? fallback.defaultPage
      : allowedPages[0] || (platformType === "owner" ? "workbench" : "no-product");
  return {
    userId: authUser.userId || "",
    account: authUser.username || authUser.account || "",
    username: authUser.username || authUser.account || "",
    displayName: authUser.displayName || authUser.username || authUser.account || "运营用户",
    userType,
    platformType,
    role: authUser.role || (userType === "ADMIN" ? "owner_admin" : "customer_user"),
    aggregatorId: authUser.aggregatorId || "",
    entId: authUser.entId || "",
    products,
    permissions,
    allowedPages,
    defaultPage,
    menuGroups,
  };
}

function nonEmptyArray(value) {
  return Array.isArray(value) && value.length > 0;
}

function normalizeUserType(userType, platformType) {
  const value = String(userType || "").toUpperCase();
  if (value === "ADMIN" || value === "PLATFORM" || platformType === "owner") {
    return "ADMIN";
  }
  return "CUSTOMER";
}

function normalizeProducts(products) {
  const selected = products.map(product => String(product || "").trim().toLowerCase());
  return ALL_PRODUCTS.filter(product => selected.includes(product));
}

function fallbackProfile(platformType, products) {
  if (platformType === "owner") {
    return {
      products: ALL_PRODUCTS,
      permissions: [
        "owner:customer:manage",
        "owner:user:manage",
        "owner:product:provision",
        "owner:permission:manage",
        "owner:settings:manage",
        "load:overview:view",
        "load:adjustment:view",
        "load:settlement:view",
        "load:device-operation:view",
        "load:resources:view",
        "tariff:query:view",
        "tariff:api:view",
        "tariff:logs:view",
      ],
      allowedPages: [
        "workbench",
        "customer-management",
        "user-management",
        "product-provisioning",
        "permission-management",
        "load-overview",
        "load-adjustment",
        "load-settlement",
        "load-device-operation",
        "load-resources",
        "tariff-query",
        "tariff-api",
        "tariff-logs",
        "settings",
      ],
      defaultPage: "workbench",
      menuGroups: [
        {
          title: "我的运营平台",
          items: [
            { key: "workbench", label: "工作台", icon: "01" },
            { key: "customer-management", label: "客户管理", icon: "02" },
            { key: "user-management", label: "用户管理", icon: "03" },
            { key: "product-provisioning", label: "产品开通", icon: "04" },
            { key: "permission-management", label: "权限管理", icon: "05" },
            { key: "settings", label: "系统设置", icon: "06" },
          ],
        },
        {
          title: "产品能力",
          items: [
            {
              key: "load",
              label: "负荷聚合",
              icon: "07",
              children: [
                { key: "load-overview", label: "运营总览" },
                { key: "load-adjustment", label: "调节情况" },
                { key: "load-settlement", label: "收益结算" },
                { key: "load-resources", label: "资源管理" },
                { key: "load-device-operation", label: "物联管理" },
              ],
            },
            {
              key: "tariff",
              label: "电价服务",
              icon: "08",
              children: [
                { key: "tariff-query", label: "全国电价查询" },
                { key: "tariff-api", label: "接口能力" },
                { key: "tariff-logs", label: "调用记录" },
              ],
            },
          ],
        },
      ],
    };
  }
  const permissions = [];
  const allowedPages = [];
  const productItems = [];
  if (products.includes(PRODUCT_LOAD)) {
    permissions.push(
      "load:overview:view",
      "load:adjustment:view",
      "load:settlement:view",
      "load:device-operation:view",
      "load:resources:view",
    );
    allowedPages.push(
      "load-overview",
      "load-adjustment",
      "load-settlement",
      "load-resources",
      "load-device-operation"
    );
    productItems.push({
      key: "load",
      label: "负荷聚合",
      icon: "01",
      children: [
        { key: "load-overview", label: "运营总览" },
        { key: "load-adjustment", label: "调节情况" },
        { key: "load-settlement", label: "收益结算" },
        { key: "load-resources", label: "资源管理" },
        { key: "load-device-operation", label: "物联管理" },
      ],
    });
  }
  if (products.includes(PRODUCT_TARIFF)) {
    permissions.push("tariff:query:view", "tariff:api:view", "tariff:logs:view");
    allowedPages.push("tariff-query", "tariff-api", "tariff-logs");
    productItems.push({
      key: "tariff",
      label: "电价服务",
      icon: "04",
      children: [
        { key: "tariff-query", label: "全国电价查询" },
        { key: "tariff-api", label: "接口能力" },
        { key: "tariff-logs", label: "调用记录" },
      ],
    });
  }
  const menuGroups = [];
  if (productItems.length) {
    menuGroups.push({
      title: "产品能力",
      items: productItems,
    });
  }
  return {
    products,
    permissions,
    allowedPages: allowedPages.length ? allowedPages : ["no-product"],
    defaultPage: products.includes(PRODUCT_LOAD)
      ? "load-overview"
      : products.includes(PRODUCT_TARIFF)
        ? "tariff-query"
        : "no-product",
    menuGroups,
  };
}

export function readUserSession() {
  const raw = sessionStorage.getItem(AUTH_USER_KEY);
  if (!raw) {
    return null;
  }
  try {
    return normalizeAuthUser(JSON.parse(raw));
  } catch (error) {
    clearUserSession();
    return null;
  }
}

export function persistUserSession(user) {
  const normalized = normalizeAuthUser(user);
  sessionStorage.setItem(AUTH_USER_KEY, JSON.stringify(normalized));
  sessionStorage.setItem("ehub-platform-type", normalized.platformType);
  sessionStorage.setItem("ehub-products", normalized.products.join(","));
  sessionStorage.setItem("ehub-user", normalized.account);
  if (normalized.aggregatorId) {
    sessionStorage.setItem("aggregatorId", normalized.aggregatorId);
  } else {
    sessionStorage.removeItem("aggregatorId");
  }
  if (normalized.entId) {
    sessionStorage.setItem("entId", normalized.entId);
  } else {
    sessionStorage.removeItem("entId");
  }
  return normalized;
}

export function clearUserSession() {
  sessionStorage.removeItem(AUTH_USER_KEY);
  sessionStorage.removeItem("ehub-platform-type");
  sessionStorage.removeItem("ehub-products");
  sessionStorage.removeItem("ehub-user");
  sessionStorage.removeItem("ehub-authenticated");
  sessionStorage.removeItem("aggregatorId");
  sessionStorage.removeItem("entId");
}

export function clearAuthStorage() {
  clearUserSession();
  sessionStorage.removeItem("token");
  sessionStorage.removeItem("ehub-token");
  sessionStorage.removeItem("console-token");
  sessionStorage.removeItem("ticket");
  sessionStorage.removeItem("aggregatorId");
  sessionStorage.removeItem("entId");
}

export function getAllowedPages(user) {
  return user && Array.isArray(user.allowedPages) ? user.allowedPages : [];
}

export function getDefaultPage(user) {
  const pages = getAllowedPages(user);
  if (user && pages.includes(user.defaultPage)) {
    return user.defaultPage;
  }
  if (pages.length) {
    return pages[0];
  }
  return user && user.platformType === "owner" ? "workbench" : "no-product";
}

export function buildMenu(user) {
  return user && Array.isArray(user.menuGroups) ? user.menuGroups : [];
}

export function hasStoredToken() {
  return Boolean(sessionStorage.getItem("token") || sessionStorage.getItem("ticket"));
}

export function fetchCurrentUser() {
  return service({
    method: "get",
    url: "/auth/me",
  }).then(response => {
    const body = response.data || {};
    if (body.code !== 200) {
      throw new Error(body.msg || "登录已失效");
    }
    return normalizeAuthUser(body.data || {});
  });
}

export function requestLogout() {
  return service({
    method: "post",
    url: "/auth/logout",
  });
}
