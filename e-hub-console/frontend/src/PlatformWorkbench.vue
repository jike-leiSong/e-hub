<template>
  <div class="workbench">
    <section class="workbench-hero">
      <div>
        <p class="hero-kicker">ENERGY ELECTRICITY HUB</p>
        <h2>统一聚合负荷、设备、电价和企业资源</h2>
        <p class="hero-copy">
          {{ heroCopy }}
        </p>
      </div>
      <div class="hero-actions">
        <button
          v-for="item in primaryActions"
          :key="item.key"
          type="button"
          @click="$emit('navigate', item.key)"
        >
          {{ item.label }}
        </button>
      </div>
    </section>

    <section class="metric-grid" v-loading="summaryLoading">
      <div v-for="item in metrics" :key="item.label" class="metric-card">
        <p>{{ item.label }}</p>
        <strong>{{ item.value }}</strong>
        <span>{{ item.desc }}</span>
      </div>
    </section>

    <section v-if="isOwner" class="owner-grid">
      <article class="owner-card" v-loading="todoLoading">
        <div class="section-head">
          <div>
            <p class="section-kicker">治理待办</p>
            <h3>优先处理需要运营动作的能力项</h3>
          </div>
          <span>{{ todos.length }} 项</span>
        </div>
        <div v-if="todos.length" class="todo-list">
          <button
            v-for="item in todos"
            :key="`${item.type}-${item.routeKey}`"
            type="button"
            class="todo-item"
            @click="navigateTodo(item)"
          >
            <div>
              <p>{{ item.title }}</p>
              <span>{{ item.routeLabel || item.routeKey }}</span>
            </div>
            <strong>{{ item.count || 0 }}</strong>
          </button>
        </div>
        <div v-else class="empty-block">当前没有待处理项</div>
      </article>

      <article class="owner-card" v-loading="logLoading">
        <div class="section-head">
          <div>
            <p class="section-kicker">最近操作</p>
            <h3>查看平台治理最近留痕</h3>
          </div>
          <span>{{ recentLogs.length }} 条</span>
        </div>
        <div v-if="recentLogs.length" class="log-list">
          <div
            v-for="(item, index) in recentLogs"
            :key="`${item.bizType}-${item.bizId}-${index}`"
            class="log-item"
          >
            <div>
              <p>{{ item.action }} / {{ item.bizType }}</p>
              <span>{{ item.operatorName || "-" }} · {{ item.createTime || "-" }}</span>
            </div>
            <em :class="item.result === 'SUCCESS' ? 'success' : 'fail'">
              {{ item.result || "-" }}
            </em>
          </div>
        </div>
        <div v-else class="empty-block">暂无操作日志</div>
      </article>
    </section>

    <section class="domain-grid">
      <button
        v-for="item in domains"
        :key="item.key"
        type="button"
        class="domain-card"
        @click="$emit('navigate', item.key)"
      >
        <p>{{ item.title }}</p>
        <span>{{ item.desc }}</span>
      </button>
    </section>
  </div>
</template>

<script>
import {
  fetchWorkbenchRecentLogs,
  fetchWorkbenchSummary,
  fetchWorkbenchTodos,
} from "@/modules/platform/api"

export default {
  name: "PlatformWorkbench",
  props: {
    user: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      summaryLoading: false,
      todoLoading: false,
      logLoading: false,
      summary: null,
      todos: [],
      recentLogs: [],
    }
  },
  computed: {
    isOwner() {
      return this.user.platformType === "owner"
    },
    heroCopy() {
      return this.isOwner
        ? "当前进入平台治理工作台，统一管理租户、账号权限、平台配置，并把负荷聚合和电价服务作为产品能力运营。"
        : "当前进入客户运营平台，仅展示该客户已开通的产品能力。"
    },
    primaryActions() {
      if (this.isOwner) {
        return [
          { key: "tenant-center", label: "进入租户中心" },
          { key: "identity-access", label: "进入身份与权限中心" },
          { key: "platform-settings", label: "进入平台设置中心" },
          { key: "load-overview", label: "进入负荷聚合" },
        ]
      }
      const actions = []
      if (this.user.products.includes("load_aggregation")) {
        actions.push({ key: "load-overview", label: "进入负荷聚合" })
      }
      if (this.user.products.includes("tariff")) {
        actions.push({ key: "tariff-query", label: "查看代理价格" })
      }
      return actions
    },
    metrics() {
      if (this.isOwner) {
        const summary = this.summary || {}
        return [
          {
            label: "租户数",
            value: String(summary.tenantCount || 0),
            desc: `启用 ${summary.activeTenantCount || 0} 个租户主体`,
          },
          {
            label: "平台账号",
            value: String(summary.userCount || 0),
            desc: `角色 ${summary.roleCount || 0} 个`,
          },
          {
            label: "产品开通租户",
            value: String(summary.enabledProductTenantCount || 0),
            desc: "租户中心已开通至少一种产品能力",
          },
          {
            label: "平台配置",
            value: String(summary.configCount || 0),
            desc: `近 7 天操作 ${summary.last7dOperationCount || 0} 次`,
          },
        ]
      }
      return [
        {
          label: "负荷聚合",
          value: this.user.products.includes("load_aggregation") ? "已开通" : "未开通",
          desc: "运营总览、调节情况、收益结算、物联数据",
        },
        {
          label: "电价服务",
          value: this.user.products.includes("tariff") ? "已开通" : "未开通",
          desc: "电网代理价格、接口能力、调用记录",
        },
        {
          label: "平台类型",
          value: "客户",
          desc: "菜单由产品开通状态和权限点共同控制",
        },
        {
          label: "权限点",
          value: String(this.user.permissions.length),
          desc: "页面、接口、数据范围权限",
        },
      ]
    },
    domains() {
      if (this.isOwner) {
        return [
          { key: "tenant-center", title: "租户中心", desc: "管理租户主体、产品订阅、合作状态和后续数据范围能力" },
          { key: "identity-access", title: "身份与权限中心", desc: "统一承接账号、角色、菜单、接口和数据权限治理" },
          { key: "platform-settings", title: "平台设置中心", desc: "集中治理平台参数、字典、集成配置和审计日志" },
          { key: "load-overview", title: "负荷聚合", desc: "进入负荷聚合业务产品，处理运营、资源、收益和物联管理" },
          { key: "tariff-query", title: "电价服务", desc: "进入电价服务产品，处理代理价格、接口能力和调用记录" },
        ]
      }

      const domains = []
      if (this.user.products.includes("load_aggregation")) {
        domains.push({
          key: "load-overview",
          title: "负荷聚合",
          desc: "聚合商运营、调节情况、收益结算、物联数据和企业设备管理",
        })
      }
      if (this.user.products.includes("tariff")) {
        domains.push({
          key: "tariff-query",
          title: "电价服务",
          desc: "电网代理价格查询展示、接口能力和调用记录",
        })
      }
      return domains
    },
  },
  mounted() {
    this.loadData()
  },
  watch: {
    isOwner() {
      this.loadData()
    },
  },
  methods: {
    loadData() {
      if (!this.isOwner) {
        return
      }
      this.loadSummary()
      this.loadTodos()
      this.loadRecentLogs()
    },
    loadSummary() {
      this.summaryLoading = true
      fetchWorkbenchSummary()
        .then(data => {
          this.summary = data || {}
        })
        .catch(error => {
          this.$message.error(error.message || "工作台汇总加载失败")
        })
        .finally(() => {
          this.summaryLoading = false
        })
    },
    loadTodos() {
      this.todoLoading = true
      fetchWorkbenchTodos()
        .then(data => {
          this.todos = Array.isArray(data) ? data : []
        })
        .catch(error => {
          this.$message.error(error.message || "工作台待办加载失败")
        })
        .finally(() => {
          this.todoLoading = false
        })
    },
    loadRecentLogs() {
      this.logLoading = true
      fetchWorkbenchRecentLogs(8)
        .then(data => {
          this.recentLogs = Array.isArray(data) ? data : []
        })
        .catch(error => {
          this.$message.error(error.message || "最近操作加载失败")
        })
        .finally(() => {
          this.logLoading = false
        })
    },
    navigateTodo(item) {
      if (!item || !item.routeKey) {
        return
      }
      this.$emit("navigate", item.routeKey)
    },
  },
}
</script>

<style lang="less" scoped>
.workbench {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.workbench-hero {
  min-height: 190px;
  padding: 26px;
  border-radius: 8px;
  background: #0e2638;
  color: #ffffff;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
}

.hero-kicker,
.hero-copy,
.workbench-hero h2,
.metric-card p,
.metric-card strong,
.metric-card span,
.domain-card p,
.domain-card span,
.section-kicker,
.section-head h3,
.todo-item p,
.todo-item span,
.log-item p,
.log-item span,
.empty-block {
  margin: 0;
}

.hero-kicker {
  color: #5eead4;
  font-size: 12px;
  font-weight: 700;
}

.workbench-hero h2 {
  margin-top: 10px;
  max-width: 620px;
  font-size: 30px;
  line-height: 1.25;
}

.hero-copy {
  margin-top: 12px;
  max-width: 680px;
  color: #b9cfdb;
  font-size: 15px;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-actions button,
.domain-card,
.todo-item {
  cursor: pointer;
}

.hero-actions button {
  height: 36px;
  padding: 0 16px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 6px;
  background: #0780ed;
  color: #ffffff;
}

.hero-actions button + button {
  background: rgba(255, 255, 255, 0.08);
}

.metric-grid,
.domain-grid,
.owner-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.metric-card,
.domain-card,
.owner-card {
  min-height: 120px;
  padding: 18px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #dde6ed;
}

.metric-card p,
.domain-card span,
.section-kicker,
.todo-item span,
.log-item span,
.empty-block {
  color: #607d8f;
  font-size: 13px;
}

.metric-card strong {
  display: block;
  margin-top: 12px;
  color: #0e2638;
  font-size: 24px;
}

.metric-card span {
  display: block;
  margin-top: 10px;
  color: #607d8f;
  font-size: 13px;
}

.owner-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.owner-card {
  min-height: 260px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.section-head h3 {
  margin-top: 8px;
  color: #0e2638;
  font-size: 20px;
}

.section-head > span {
  color: #607d8f;
  font-size: 12px;
}

.todo-list,
.log-list {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.todo-item,
.log-item {
  width: 100%;
  padding: 14px 16px;
  border-radius: 8px;
  background: #f7fbff;
  border: 1px solid #dbe8f2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.todo-item {
  text-align: left;
}

.todo-item p,
.log-item p {
  color: #0e2638;
  font-size: 15px;
  font-weight: 700;
}

.todo-item strong {
  color: #0780ed;
  font-size: 24px;
}

.log-item em {
  font-style: normal;
  font-size: 12px;
  font-weight: 700;
}

.log-item em.success {
  color: #0f9d58;
}

.log-item em.fail {
  color: #d14343;
}

.empty-block {
  margin-top: 18px;
  padding: 28px 18px;
  border-radius: 8px;
  background: #f7fbff;
  text-align: center;
}

.domain-card {
  text-align: left;
}

.domain-card:hover,
.todo-item:hover {
  border-color: #0780ed;
}

.domain-card p {
  color: #0e2638;
  font-size: 18px;
  font-weight: 700;
}

@media (max-width: 1100px) {
  .owner-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 960px) {
  .workbench-hero {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
