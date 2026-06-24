<template>
  <div v-if="authLoading" class="auth-loading">加载中...</div>
  <Login v-else-if="!isAuthenticated" @login="handleLogin" />
  <div v-else class="ehub-platform">
    <aside class="platform-sidebar">
      <div class="brand">
        <div class="brand-mark">e</div>
        <div>
          <p class="brand-name">e-hub</p>
          <p class="brand-subtitle">能源电力聚合平台</p>
        </div>
      </div>

      <nav class="platform-nav">
        <template v-for="group in menuGroups">
          <p :key="`${group.title}-title`" class="nav-section">{{ group.title }}</p>
          <template v-for="item in group.items">
            <button
              :key="item.key"
              type="button"
              class="nav-item"
              :class="{ active: isMenuItemActive(item) }"
              @click="switchPage(defaultPageOf(item))"
            >
              <span class="nav-icon">{{ item.icon }}</span>
              <span>{{ item.label }}</span>
            </button>
            <div
              v-if="item.children && item.children.length"
              :key="`${item.key}-children`"
              class="nav-sub-group"
            >
              <button
                v-for="child in item.children"
                :key="child.key"
                type="button"
                class="nav-sub-item"
                :class="{ active: activePage === child.key }"
                @click="switchPage(child.key)"
              >
                {{ child.label }}
              </button>
            </div>
          </template>
        </template>
      </nav>
    </aside>

    <section class="platform-main">
      <header class="platform-header">
        <div>
          <p class="eyebrow">{{ activeMeta.eyebrow }}</p>
          <h1>{{ activeMeta.title }}</h1>
        </div>
        <div class="header-actions">
          <div class="operator">
            <p>{{ operatorName }}</p>
            <button type="button" @click="logout">退出</button>
          </div>
        </div>
      </header>

      <main class="platform-content">
        <PlatformWorkbench
          v-if="activePage === 'workbench'"
          :user="currentUser"
          @navigate="switchPage"
        />
        <Aggregation
          v-else-if="activePage === 'load-overview'"
          :active-obj="activeObj"
          :active-comp-name="activeCompName"
        />
        <AggregationHistory
          v-else-if="historyViewType"
          :active-obj="activeObj"
          :active-comp-name="activeCompName"
          :view-type="historyViewType"
        />
        <LoadResources v-else-if="activePage === 'load-resources'" />
        <ProductProvisioning v-else-if="activePage === 'product-provisioning'" />
        <ComingSoon v-else v-bind="comingSoonConfig" />
      </main>
    </section>
  </div>
</template>

<script>
import Login from "./Login.vue";
import PlatformWorkbench from "./PlatformWorkbench.vue";
import ComingSoon from "./ComingSoon.vue";
import ProductProvisioning from "./ProductProvisioning.vue";
import Aggregation from "@/modules/load-aggregation/overview/Aggregation.vue";
import AggregationHistory from "@/modules/load-aggregation/history/src/AggregationHistory.vue";
import LoadResources from "@/modules/load-aggregation/resources/LoadResources.vue";
import {
  buildMenu,
  clearAuthStorage,
  clearUserSession,
  fetchCurrentUser,
  getAllowedPages,
  getDefaultPage,
  persistUserSession,
  requestLogout,
  readUserSession,
} from "./auth/session.js";

const pageMeta = {
  workbench: {
    title: "工作台",
    eyebrow: "E-HUB ENERGY OPERATIONS",
  },
  "no-product": {
    title: "未开通产品",
    eyebrow: "PRODUCT ACCESS",
  },
  "load-overview": {
    title: "负荷聚合 / 运营总览",
    eyebrow: "LOAD AGGREGATION",
  },
  "load-adjustment": {
    title: "负荷聚合 / 调节情况",
    eyebrow: "LOAD AGGREGATION",
  },
  "load-settlement": {
    title: "负荷聚合 / 收益结算",
    eyebrow: "LOAD AGGREGATION",
  },
  "load-device-operation": {
    title: "负荷聚合 / 设备运行",
    eyebrow: "LOAD AGGREGATION",
  },
  "load-history": {
    title: "负荷聚合 / 调节情况",
    eyebrow: "LOAD AGGREGATION",
  },
  "load-resources": {
    title: "负荷聚合 / 企业与设备",
    eyebrow: "LOAD AGGREGATION",
  },
  "tariff-query": {
    title: "电价服务 / 全国电价查询",
    eyebrow: "POWER TARIFF",
  },
  "tariff-api": {
    title: "电价服务 / 接口能力",
    eyebrow: "POWER TARIFF",
  },
  "tariff-logs": {
    title: "电价服务 / 调用记录",
    eyebrow: "POWER TARIFF",
  },
  "customer-management": {
    title: "客户管理",
    eyebrow: "OWNER OPERATIONS",
  },
  "user-management": {
    title: "用户管理",
    eyebrow: "OWNER OPERATIONS",
  },
  "product-provisioning": {
    title: "产品开通",
    eyebrow: "OWNER OPERATIONS",
  },
  "permission-management": {
    title: "权限管理",
    eyebrow: "OWNER OPERATIONS",
  },
  settings: {
    title: "系统设置",
    eyebrow: "SYSTEM SETTINGS",
  },
};

const comingSoonMap = {
  "no-product": {
    title: "未开通产品",
    description: "当前账号未开通产品能力，请联系平台管理员开通负荷聚合或电价服务。",
    items: ["负荷聚合", "电价服务", "产品开通", "权限生效"],
  },
  "load-resources": {
    title: "企业与设备",
    description: "维护企业、设备、测点、三方绑定和接入数据，支撑调节与结算分析。",
    items: ["企业资产", "设备资源", "测点配置", "接入诊断"],
  },
  "tariff-query": {
    title: "全国电价查询",
    description: "提供全国分省电价查询、峰谷平时段展示、现货价格和历史价格趋势。",
    items: ["分省查询", "峰谷平时段", "现货价格", "历史趋势"],
  },
  "tariff-api": {
    title: "接口能力",
    description: "为已开通电价服务的客户提供接口凭证、接口说明和调用额度管理。",
    items: ["接口凭证", "接口说明", "调用额度", "安全策略"],
  },
  "tariff-logs": {
    title: "调用记录",
    description: "展示电价接口调用明细、成功率、耗时和异常记录。",
    items: ["调用明细", "成功率", "响应耗时", "异常记录"],
  },
  "customer-management": {
    title: "客户管理",
    description: "面向内部运营，维护客户档案、租户状态和合作信息。",
    items: ["客户档案", "租户状态", "合作信息", "数据范围"],
  },
  "user-management": {
    title: "用户管理",
    description: "面向内部运营，维护用户账号、角色和组织归属。",
    items: ["用户账号", "角色分配", "组织归属", "登录状态"],
  },
  "product-provisioning": {
    title: "产品开通",
    description: "为客户开通负荷聚合、电价服务等产品，并控制产品有效期。",
    items: ["负荷聚合", "电价服务", "有效期", "开通记录"],
  },
  "permission-management": {
    title: "权限管理",
    description: "按租户、角色、产品和权限点管理页面、按钮、接口与数据范围。",
    items: ["租户权限", "角色权限", "产品权限", "接口权限"],
  },
  settings: {
    title: "系统设置",
    description: "规划账号权限、平台参数、接口配置和操作审计。",
    items: ["账号权限", "平台参数", "接口配置", "操作审计"],
  },
};

function resolveInitialPage() {
  const params = new URLSearchParams(window.location.search);
  const page = params.get("page");
  if (page === "history") {
    return "load-adjustment";
  }
  if (page === "overview") {
    return "load-overview";
  }
  if (page && pageMeta[page]) {
    return page;
  }
  return "workbench";
}

const historyPageViewMap = {
  "load-adjustment": "adjustment",
  "load-settlement": "settlement",
  "load-device-operation": "device-operation",
  "load-history": "adjustment",
};

export default {
  name: "App",
  components: {
    Login,
    PlatformWorkbench,
    ComingSoon,
    ProductProvisioning,
    Aggregation,
    AggregationHistory,
    LoadResources,
  },
  data() {
    const currentUser = readUserSession() || {};
    const page = resolveInitialPage();
    const allowedPages = getAllowedPages(currentUser);
    const initialPage = allowedPages.includes(page) ? page : allowedPages[0];
    const hasToken = Boolean(sessionStorage.getItem("ticket") || sessionStorage.getItem("token"));
    return {
      authLoading: hasToken,
      isAuthenticated: false,
      currentUser,
      activePage: initialPage || getDefaultPage(currentUser),
      activeCompName: [thisComponentName(initialPage || getDefaultPage(currentUser))],
      activeObj: {},
    };
  },
  created() {
    this.bootstrapAuth();
  },
  computed: {
    activeMeta() {
      return pageMeta[this.activePage] || pageMeta.workbench;
    },
    historyViewType() {
      return historyPageViewMap[this.activePage] || "";
    },
    menuGroups() {
      return buildMenu(this.currentUser);
    },
    comingSoonConfig() {
      return comingSoonMap[this.activePage] || comingSoonMap.settings;
    },
    operatorName() {
      return this.currentUser.displayName || this.currentUser.account || "运营用户";
    },
  },
  methods: {
    bootstrapAuth() {
      if (!sessionStorage.getItem("ticket") && !sessionStorage.getItem("token")) {
        clearAuthStorage();
        this.authLoading = false;
        this.isAuthenticated = false;
        return;
      }
      this.authLoading = true;
      fetchCurrentUser()
        .then(user => {
          this.applyAuthUser(user, resolveInitialPage());
        })
        .catch(() => {
          clearAuthStorage();
          this.isAuthenticated = false;
          this.currentUser = {};
        })
        .finally(() => {
          this.authLoading = false;
        });
    },
    applyAuthUser(user, preferredPage) {
      const normalized = persistUserSession(user);
      const allowedPages = getAllowedPages(normalized);
      const nextPage = allowedPages.includes(preferredPage)
        ? preferredPage
        : getDefaultPage(normalized);
      this.currentUser = normalized;
      this.activePage = nextPage;
      this.activeCompName = [thisComponentName(nextPage)];
      this.isAuthenticated = true;
      sessionStorage.setItem("ehub-authenticated", "1");
    },
    switchPage(page) {
      if (!getAllowedPages(this.currentUser).includes(page)) {
        this.$message.warning("当前账号未开通该能力");
        return;
      }
      this.activePage = page;
      this.activeCompName = [thisComponentName(page)];
    },
    defaultPageOf(item) {
      return item.children && item.children.length ? item.children[0].key : item.key;
    },
    isMenuItemActive(item) {
      if (item.children && item.children.length) {
        return item.children.some(child => child.key === this.activePage);
      }
      return item.key === this.activePage;
    },
    handleLogin(form) {
      this.applyAuthUser(form.authUser || {}, getDefaultPage(form.authUser || {}));
      const account = this.currentUser.account || this.currentUser.username;
      if (!sessionStorage.getItem("openId")) {
        sessionStorage.setItem("openId", account);
      }
      if (!sessionStorage.getItem("entId") && !sessionStorage.getItem("cid")) {
        sessionStorage.setItem("cid", account);
      }
    },
    logout() {
      requestLogout().finally(() => {
        clearAuthStorage();
        clearUserSession();
        this.currentUser = {};
        this.activePage = "workbench";
        this.activeCompName = [thisComponentName("workbench")];
        this.isAuthenticated = false;
      });
    },
  },
};

function thisComponentName(page) {
  if (historyPageViewMap[page]) {
    return "AggregationHistory";
  }
  if (page === "load-overview") {
    return "Aggregation";
  }
  return "Platform";
}
</script>

<style>
* {
  box-sizing: border-box;
}

html,
body,
#app {
  min-height: 100%;
  margin: 0;
}

body {
  font-family: PingFang SC, Microsoft YaHei, Arial, sans-serif;
  background: #eef3f7;
  color: #1f2933;
}

button {
  font-family: inherit;
}

.auth-loading {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef3f7;
  color: #234257;
  font-size: 15px;
}

.platform-nav button {
  appearance: none;
  -webkit-appearance: none;
  outline: none;
}

.ehub-platform {
  min-height: 100vh;
  display: flex;
  background: #eef3f7;
}

.platform-sidebar {
  width: 248px;
  min-width: 248px;
  min-height: 100vh;
  background: #0e2638;
  color: #fff;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
}

.brand {
  min-height: 82px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-mark {
  width: 38px;
  height: 38px;
  border-radius: 8px;
  background: #13c2c2;
  color: #0e2638;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 800;
}

.brand-name,
.brand-subtitle,
.nav-section,
.eyebrow,
.platform-header h1,
.operator p {
  margin: 0;
}

.brand-name {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0;
}

.brand-subtitle {
  margin-top: 3px;
  color: #a7c4d6;
  font-size: 13px;
}

.platform-nav {
  padding: 14px 12px;
  overflow: auto;
}

.nav-section {
  padding: 14px 10px 8px;
  color: #6f91a6;
  font-size: 12px;
  font-weight: 700;
}

.nav-item {
  width: 100%;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #c7d8e3;
  display: flex;
  align-items: center;
  cursor: pointer;
  text-align: left;
}

.nav-item {
  height: 42px;
  gap: 12px;
  padding: 0 12px;
  font-size: 15px;
}

.nav-sub-group {
  margin: 6px 0 10px 25px;
  padding: 4px 0 4px 16px;
  border-left: 1px solid rgba(94, 234, 212, 0.24);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-sub-item {
  width: 100%;
  height: 32px;
  border: 0;
  border-radius: 6px;
  padding: 0 12px;
  background: transparent;
  color: #98b6c8;
  display: flex;
  align-items: center;
  font-size: 14px;
  cursor: pointer;
  text-align: left;
}

.nav-item + .nav-item,
.nav-sub-group + .nav-item {
  margin-top: 8px;
}

.nav-item.active,
.nav-item:hover,
.nav-sub-item.active,
.nav-sub-item:hover {
  background: #123a54;
  color: #fff;
}

.nav-item.active,
.nav-sub-item.active {
  box-shadow: inset 3px 0 0 #13c2c2;
}

.nav-icon {
  width: 26px;
  height: 26px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(19, 194, 194, 0.16);
  color: #5eead4;
  font-size: 12px;
  font-weight: 700;
}

.platform-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.platform-header {
  height: 76px;
  min-height: 76px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.94);
  border-bottom: 1px solid #dde6ed;
}

.eyebrow {
  color: #607d8f;
  font-size: 12px;
  font-weight: 700;
}

.platform-header h1 {
  margin-top: 4px;
  color: #0e2638;
  font-size: 24px;
  font-weight: 700;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.operator {
  height: 38px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding-left: 14px;
  border-left: 1px solid #dde6ed;
  color: #334e5c;
  font-size: 14px;
}

.operator button {
  height: 30px;
  padding: 0 12px;
  border: 1px solid #cfdce5;
  border-radius: 6px;
  background: #fff;
  color: #334e5c;
  cursor: pointer;
}

.platform-content {
  height: calc(100vh - 76px);
  overflow: auto;
  padding: 20px;
}

.platform-content > div {
  min-height: 100%;
}
</style>
