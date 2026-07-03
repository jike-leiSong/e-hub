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

    <section class="metric-grid">
      <div v-for="item in metrics" :key="item.label" class="metric-card">
        <p>{{ item.label }}</p>
        <strong>{{ item.value }}</strong>
        <span>{{ item.desc }}</span>
      </div>
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
export default {
  name: "PlatformWorkbench",
  props: {
    user: {
      type: Object,
      required: true,
    },
  },
  computed: {
    isOwner() {
      return this.user.platformType === "owner";
    },
    heroCopy() {
      return this.isOwner
        ? "当前进入我的运营平台，可管理客户、用户、产品开通和系统权限。"
        : "当前进入客户运营平台，仅展示该客户已开通的产品能力。";
    },
    primaryActions() {
      if (this.isOwner) {
        return [
          { key: "customer-management", label: "客户管理" },
          { key: "product-provisioning", label: "产品开通" },
        ];
      }
      const actions = [];
      if (this.user.products.includes("load_aggregation")) {
        actions.push({ key: "load-overview", label: "进入负荷聚合" });
      }
      if (this.user.products.includes("tariff")) {
        actions.push({ key: "tariff-query", label: "查看代理价格" });
      }
      return actions;
    },
    metrics() {
      if (this.isOwner) {
        return [
          { label: "客户管理", value: "可用", desc: "客户档案、租户状态、合作信息" },
          { label: "用户管理", value: "可用", desc: "账号、角色、组织归属" },
          { label: "产品开通", value: "可用", desc: "负荷聚合、电价服务、有效期" },
          { label: "权限管理", value: "可用", desc: "租户、角色、产品和接口权限" },
        ];
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
        { label: "平台类型", value: "客户", desc: "菜单由产品开通状态控制" },
        { label: "权限点", value: String(this.user.permissions.length), desc: "页面、接口、数据范围权限" },
      ];
    },
    domains() {
      if (this.isOwner) {
        return [
          { key: "customer-management", title: "客户管理", desc: "维护客户档案、租户状态和合作信息" },
          { key: "user-management", title: "用户管理", desc: "维护内部和客户账号、角色及组织归属" },
          { key: "product-provisioning", title: "产品开通", desc: "给客户开通负荷聚合、电价服务和有效期" },
          { key: "permission-management", title: "权限管理", desc: "管理租户、角色、产品、接口和数据范围权限" },
        ];
      }

      const domains = [];
      if (this.user.products.includes("load_aggregation")) {
        domains.push({
          key: "load-overview",
          title: "负荷聚合",
          desc: "聚合商运营、调节情况、收益结算、物联数据和企业设备管理",
        });
      }
      if (this.user.products.includes("tariff")) {
        domains.push({
          key: "tariff-query",
          title: "电价服务",
          desc: "电网代理价格查询展示、接口能力和调用记录",
        });
      }
      return domains;
    },
  },
};
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
.domain-card span {
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
  gap: 10px;
}

.hero-actions button,
.domain-card {
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
.domain-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.metric-card,
.domain-card {
  min-height: 120px;
  padding: 18px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #dde6ed;
}

.metric-card p,
.domain-card span {
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

.domain-card {
  text-align: left;
}

.domain-card:hover {
  border-color: #0780ed;
}

.domain-card p {
  color: #0e2638;
  font-size: 18px;
  font-weight: 700;
}

.domain-card span {
  display: block;
  margin-top: 12px;
  line-height: 1.6;
}
</style>
