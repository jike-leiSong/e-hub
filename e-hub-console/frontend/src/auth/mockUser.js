const PRODUCT_LOAD = "load_aggregation";
const PRODUCT_TARIFF = "tariff";

export function createUserSession(form = {}) {
  const account = String(form.account || "operator").trim();
  const preset = resolveUserPreset(account);
  const platformType = form.platformType || preset.platformType;
  const products =
    platformType === "owner"
      ? [PRODUCT_LOAD, PRODUCT_TARIFF]
      : form.products && form.products.length
        ? form.products
        : preset.products;

  return {
    platformType,
    tenantId: platformType === "owner" ? "ehub" : "customer-demo",
    role: platformType === "owner" ? "owner_admin" : "customer_admin",
    account,
    displayName: preset.displayName,
    products,
    permissions: createPermissions(platformType, products),
  };
}

export function readUserSession() {
  const params = new URLSearchParams(window.location.search);
  const platformType = params.get("platform") || sessionStorage.getItem("ehub-platform-type");
  const productsParam = params.get("products");
  const storedProducts = sessionStorage.getItem("ehub-products");
  const account = sessionStorage.getItem("ehub-user") || "运营用户";

  if (!platformType) {
    return createUserSession({ account, platformType: "customer", products: [PRODUCT_LOAD] });
  }

  const products = (productsParam || storedProducts || PRODUCT_LOAD)
    .split(",")
    .map(item => item.trim())
    .filter(Boolean);

  return createUserSession({ account, platformType, products });
}

export function persistUserSession(user) {
  sessionStorage.setItem("ehub-platform-type", user.platformType);
  sessionStorage.setItem("ehub-products", user.products.join(","));
  sessionStorage.setItem("ehub-user", user.account);
}

export function clearUserSession() {
  sessionStorage.removeItem("ehub-platform-type");
  sessionStorage.removeItem("ehub-products");
  sessionStorage.removeItem("ehub-user");
}

export function getAllowedPages(user) {
  const pages = ["workbench"];
  if (user.platformType === "owner") {
    return [
      ...pages,
      "customer-management",
      "user-management",
      "product-provisioning",
      "permission-management",
      "settings",
    ];
  }

  if (user.products.includes(PRODUCT_LOAD)) {
    pages.push("load-overview", "load-history", "load-resources");
  }
  if (user.products.includes(PRODUCT_TARIFF)) {
    pages.push("tariff-query", "tariff-api", "tariff-logs");
  }
  return pages;
}

export function getDefaultPage(user) {
  if (user.platformType === "owner") {
    return "workbench";
  }
  if (user.products.includes(PRODUCT_LOAD)) {
    return "load-overview";
  }
  if (user.products.includes(PRODUCT_TARIFF)) {
    return "tariff-query";
  }
  return "workbench";
}

export function buildMenu(user) {
  if (user.platformType === "owner") {
    return [
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
    ];
  }

  const productItems = [];
  if (user.products.includes(PRODUCT_LOAD)) {
    productItems.push({
      key: "load",
      label: "负荷聚合",
      icon: "01",
      children: [
        { key: "load-overview", label: "运营总览" },
        { key: "load-history", label: "历史查询" },
        { key: "load-resources", label: "设备/资源数据" },
      ],
    });
  }
  if (user.products.includes(PRODUCT_TARIFF)) {
    productItems.push({
      key: "tariff",
      label: "电价能力",
      icon: "02",
      children: [
        { key: "tariff-query", label: "全国电价查询" },
        { key: "tariff-api", label: "接口能力" },
        { key: "tariff-logs", label: "调用记录" },
      ],
    });
  }

  return [
    {
      title: "客户运营平台",
      items: [{ key: "workbench", label: "工作台", icon: "00" }],
    },
    {
      title: "产品能力",
      items: productItems,
    },
  ];
}

function createPermissions(platformType, products) {
  if (platformType === "owner") {
    return [
      "owner:customer:manage",
      "owner:user:manage",
      "owner:product:provision",
      "owner:permission:manage",
      "owner:settings:manage",
    ];
  }

  const permissions = [];
  if (products.includes(PRODUCT_LOAD)) {
    permissions.push("load:overview:view", "load:history:view", "load:resources:view");
  }
  if (products.includes(PRODUCT_TARIFF)) {
    permissions.push("tariff:query:view", "tariff:api:view", "tariff:logs:view");
  }
  return permissions;
}

function resolveUserPreset(account) {
  const normalized = String(account || "").toLowerCase();
  const presets = {
    admin: {
      platformType: "owner",
      products: [PRODUCT_LOAD, PRODUCT_TARIFF],
      displayName: "平台管理员",
    },
    owner: {
      platformType: "owner",
      products: [PRODUCT_LOAD, PRODUCT_TARIFF],
      displayName: "平台运营",
    },
    ehub: {
      platformType: "owner",
      products: [PRODUCT_LOAD, PRODUCT_TARIFF],
      displayName: "e-hub 运营",
    },
    tariff: {
      platformType: "customer",
      products: [PRODUCT_TARIFF],
      displayName: "电价客户",
    },
    price: {
      platformType: "customer",
      products: [PRODUCT_TARIFF],
      displayName: "电价客户",
    },
    all: {
      platformType: "customer",
      products: [PRODUCT_LOAD, PRODUCT_TARIFF],
      displayName: "综合能源客户",
    },
    both: {
      platformType: "customer",
      products: [PRODUCT_LOAD, PRODUCT_TARIFF],
      displayName: "综合能源客户",
    },
  };

  return presets[normalized] || {
    platformType: "customer",
    products: [PRODUCT_LOAD],
    displayName: "负荷聚合客户",
  };
}
