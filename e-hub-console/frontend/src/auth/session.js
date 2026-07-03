import service from "@/services/http";

const AUTH_USER_KEY = "ehub-auth-user";
const PRODUCT_LOAD = "load_aggregation";
const PRODUCT_TARIFF = "tariff";
const ALL_PRODUCTS = [PRODUCT_LOAD, PRODUCT_TARIFF];
const LEGACY_PAGE_MAP = {
  "customer-management": "tenant-center",
  "product-provisioning": "tenant-center",
  "user-management": "identity-access",
  "permission-management": "identity-access",
  settings: "platform-settings",
};

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
  const allowedPages = nonEmptyArray(authUser.allowedPages)
    ? normalizeAllowedPages(authUser.allowedPages)
    : fallback.allowedPages;
  const menuGroups = nonEmptyArray(authUser.menuGroups)
    ? normalizeMenuGroups(authUser.menuGroups)
    : fallback.menuGroups;
  const requestedDefaultPage = normalizePageKey(authUser.defaultPage);
  const defaultPage = allowedPages.includes(requestedDefaultPage)
    ? requestedDefaultPage
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

function normalizePageKey(page) {
  const key = String(page || "").trim();
  return LEGACY_PAGE_MAP[key] || key;
}

function normalizeAllowedPages(pages) {
  const result = [];
  pages.forEach(page => {
    const normalized = normalizePageKey(page);
    if (normalized && !result.includes(normalized)) {
      result.push(normalized);
    }
  });
  return result;
}

function normalizeMenuItems(items) {
  if (!Array.isArray(items)) {
    return [];
  }
  const result = [];
  items.forEach(item => {
    if (!item) {
      return;
    }
    const normalizedKey = normalizePageKey(item.key);
    const normalizedItem = {
      ...item,
      key: normalizedKey || item.key,
      children: normalizeMenuItems(item.children),
    };
    if (!result.some(existing => existing.key === normalizedItem.key)) {
      result.push(normalizedItem);
    }
  });
  return result;
}

function normalizeMenuGroups(menuGroups) {
  if (!Array.isArray(menuGroups)) {
    return [];
  }
  return menuGroups.map(group => ({
    ...group,
    items: normalizeMenuItems(group.items),
  }));
}

function fallbackProfile(platformType, products) {
  if (platformType === "owner") {
    return {
      products: ALL_PRODUCTS,
      permissions: [
        "owner:tenant:manage",
        "owner:access:manage",
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
        "tenant-center",
        "identity-access",
        "platform-settings",
        "load-overview",
        "load-adjustment",
        "load-settlement",
        "load-device-operation",
        "load-resources",
        "tariff-query",
        "tariff-api",
        "tariff-logs",
      ],
      defaultPage: "workbench",
      menuGroups: [
        {
          title: "平台治理",
          items: [
            { key: "workbench", label: "工作台", icon: "01" },
            { key: "tenant-center", label: "租户中心", icon: "02" },
            { key: "identity-access", label: "身份与权限中心", icon: "03" },
            { key: "platform-settings", label: "平台设置中心", icon: "04" },
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
                { key: "tariff-query", label: "电网代理价格" },
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
        { key: "tariff-query", label: "电网代理价格" },
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
