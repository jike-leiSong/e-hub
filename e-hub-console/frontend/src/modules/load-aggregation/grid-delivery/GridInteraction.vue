<template>
  <div class="grid-interaction-page">
    <header class="interaction-head">
      <div>
        <h2>电网交互</h2>
        <p>统一管理电网业务上送，并按日核查实际上送数据。</p>
      </div>
      <div class="energy-selector">
        <span>能源类型</span>
        <el-select v-model="resourceTypeId" size="small" :loading="loading" placeholder="请选择能源类型">
          <el-option v-for="item in resourceTypes" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <div v-if="resourceTypeId" class="market-state">
          <span class="market-state-label">人工标记</span>
          <el-switch v-if="canAudit" v-model="marketEnabled" :loading="marketLoading" active-text="参与市场" inactive-text="未参与市场" @change="changeMarketStatus" />
          <el-tag v-else size="small" :type="marketEnabled ? 'success' : 'info'">{{ marketEnabled ? '参与市场' : '未参与市场' }}</el-tag>
        </div>
      </div>
    </header>

    <el-tabs v-model="activeSection" class="interaction-tabs">
      <el-tab-pane label="电网对接" name="connection">
        <GridConnection
          v-if="resourceTypeId"
          :key="`connection-${resourceTypeId}-${marketVersion}`"
          :aggregator-id="aggregatorId"
          :resource-type-id="resourceTypeId"
          :resource-type-name="resourceTypeName"
          :user="user"
        />
      </el-tab-pane>
      <el-tab-pane label="上送核查" name="audit">
        <GridDeliveryQuality
          v-if="resourceTypeId"
          :key="`audit-${resourceTypeId}-${marketVersion}`"
          :aggregator-id="aggregatorId"
          :resource-type-id="resourceTypeId"
          :user="user"
        />
      </el-tab-pane>
    </el-tabs>

    <el-empty v-if="!loading && !resourceTypes.length" description="当前聚合商未配置可用能源类型" />
  </div>
</template>

<script>
import GridConnection from "./GridConnection.vue";
import GridDeliveryQuality from "./GridDeliveryQuality.vue";
import { getMarketStatus, getResourceTypes, updateMarketStatus } from "./api";

function payload(response, fallback) {
  return response && response.data && response.data.data !== undefined ? response.data.data : fallback;
}

export default {
  name: "GridInteraction",
  components: { GridConnection, GridDeliveryQuality },
  props: {
    aggregatorId: { type: String, default: "" },
    user: { type: Object, default: () => ({}) },
  },
  data() {
    return { activeSection: "connection", resourceTypeId: "", resourceTypes: [], loading: false, marketEnabled: true, marketLoading: false, marketVersion: 0 };
  },
  computed: {
    resourceTypeName() {
      const item = this.resourceTypes.find(value => value.id === this.resourceTypeId);
      return item ? item.name : "";
    },
    permissions() { return Array.isArray(this.user.permissions) ? this.user.permissions : []; },
    canAudit() { return this.permissions.includes("load:grid-interaction:audit") || this.permissions.includes("load:grid-delivery:manage"); },
  },
  watch: { aggregatorId: { immediate: true, handler() { this.loadResourceTypes(); } }, resourceTypeId() { this.loadMarketStatus(); } },
  methods: {
    async loadResourceTypes() {
      this.resourceTypeId = "";
      this.resourceTypes = [];
      if (!this.aggregatorId) return;
      this.loading = true;
      try {
        const list = payload(await getResourceTypes({ aggregatorId: this.aggregatorId }), []);
        this.resourceTypes = list.filter(item => Number(item.display) === 1);
        this.resourceTypeId = this.resourceTypes.length ? this.resourceTypes[0].id : "";
      } finally {
        this.loading = false;
      }
    },
    async loadMarketStatus() {
      if (!this.resourceTypeId) return;
      const result = payload(await getMarketStatus({ aggregatorId: this.aggregatorId, resourceTypeId: this.resourceTypeId }), {});
      this.marketEnabled = result.marketEnabled !== false;
    },
    async changeMarketStatus(enabled) {
      const action = enabled ? "标记为当前参与市场" : "标记为当前未参与市场";
      try {
        await this.$confirm(`确认${action}？该标记只用于页面说明，不影响实际上送、核查、异常记录和统计结果。`, "确认市场状态标记", { type: "warning" });
        this.marketLoading = true;
        const response = await updateMarketStatus({ aggregatorId: this.aggregatorId, resourceTypeId: this.resourceTypeId, enabled, remark: enabled ? "人工标记为参与市场" : "人工标记为当前未参与市场" });
        if (!response.data || Number(response.data.code) !== 200) throw new Error((response.data && response.data.msg) || "设置失败");
        this.marketVersion += 1;
        this.$message.success("市场状态标记已更新");
      } catch (error) {
        this.marketEnabled = !enabled;
        if (error && error !== "cancel" && error !== "close") this.$message.error(error.message || "设置失败");
      } finally {
        this.marketLoading = false;
      }
    },
  },
};
</script>

<style scoped>
.grid-interaction-page{min-height:100%;padding:18px;background:#f5f7fa;color:#243746}.interaction-head{display:flex;align-items:flex-start;justify-content:space-between;gap:20px;padding-bottom:14px;border-bottom:1px solid #dfe6eb}.interaction-head h2{margin:0;font-size:22px}.interaction-head p{margin:7px 0 0;color:#788692;font-size:13px}.energy-selector{display:flex;align-items:center;gap:10px}.energy-selector>span,.market-state-label{color:#647580;font-size:13px}.energy-selector .el-select{width:190px}.market-state{display:flex;align-items:center;gap:8px;padding-left:10px;border-left:1px solid #dce3e8}.interaction-tabs{margin-top:4px}.interaction-tabs ::v-deep .el-tabs__header{margin-bottom:16px}.interaction-tabs ::v-deep .el-tabs__item{height:46px;line-height:46px;font-size:15px}@media(max-width:760px){.grid-interaction-page{padding:10px}.interaction-head{display:block}.energy-selector{margin-top:12px;flex-wrap:wrap}.energy-selector .el-select{flex:1}.market-state{width:100%;padding:8px 0 0;border:0}}
</style>
