<template>
  <div class="iot-management-page">
    <div class="iot-tabs">
      <button
        v-for="item in tabs"
        :key="item.key"
        type="button"
        class="iot-tab"
        :class="{ active: activeTab === item.key }"
        @click="activeTab = item.key"
      >
        {{ item.label }}
      </button>
    </div>

    <section v-if="activeTab === 'device-management'" class="device-management">
      <div class="toolbar">
        <div class="toolbar-copy">
          <p class="toolbar-kicker">IOT DEVICE WORKSPACE</p>
          <h3>按企业与设备组维护物联设备</h3>
          <p class="toolbar-subtitle">统一维护企业下的设备台账、网关绑定和设备点表。</p>
        </div>
        <div class="toolbar-left">
          <span class="toolbar-label">所属企业</span>
          <el-select
            v-model="selectedEntId"
            size="small"
            filterable
            class="enterprise-select"
            placeholder="请选择企业"
            @change="handleEntChange"
          >
            <el-option
              v-for="item in entOptions"
              :key="item.entId"
              :label="item.entName || item.entId"
              :value="item.entId"
            />
          </el-select>
        </div>
      </div>

      <div class="layout">
        <aside class="group-column">
          <section class="group-panel">
            <div class="panel-head">
              <div>
                <p class="panel-kicker">DEVICE GROUP</p>
                <h3>设备组</h3>
              </div>
              <el-button type="text" icon="el-icon-plus" @click="openGroupDialog()">新增</el-button>
            </div>
            <div v-loading="groupLoading" class="group-list">
              <div
                v-for="item in deviceGroups"
                :key="item.id"
                class="group-item"
                :class="{ active: selectedGroupId === item.id }"
                @click="selectGroup(item)"
              >
                <div class="group-main">
                  <div class="group-title-row">
                    <span class="group-name">{{ item.deviceGroupName }}</span>
                    <span v-if="item.virtualFlag === 1" class="group-flag">默认</span>
                  </div>
                  <p class="group-meta">{{ item.deviceGroupTypeName || "未配置设备组类型" }}</p>
                </div>
                <div class="group-side">
                  <span class="group-badge">{{ item.deviceCount || 0 }} 台</span>
                  <div class="group-actions">
                    <el-button type="text" size="mini" @click.stop="openGroupDialog(item)">编辑</el-button>
                    <el-button
                      v-if="item.virtualFlag !== 1"
                      type="text"
                      size="mini"
                      @click.stop="removeGroup(item)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
              </div>
              <div v-if="!groupLoading && !deviceGroups.length" class="group-empty">
                <div class="group-empty-icon">组</div>
                <p class="group-empty-title">暂无设备组</p>
                <p class="group-empty-desc">先创建设备组，再按设备组维护物联设备。</p>
                <el-button type="primary" plain size="small" @click="openGroupDialog()">新建设备组</el-button>
              </div>
            </div>
          </section>

          <section class="group-overview">
            <div class="overview-head">
              <div>
                <p class="panel-kicker">GROUP OVERVIEW</p>
                <h4>设备组概览</h4>
              </div>
              <span class="overview-status" :class="{ virtual: selectedGroup && selectedGroup.virtualFlag === 1 }">
                {{ selectedGroup ? (selectedGroup.virtualFlag === 1 ? "默认组" : "已选择") : "未选择" }}
              </span>
            </div>
            <template v-if="selectedGroup">
              <div class="overview-metrics">
                <div class="metric-card">
                  <span>设备数</span>
                  <strong>{{ selectedGroup.deviceCount || 0 }}</strong>
                </div>
                <div class="metric-card">
                  <span>网关</span>
                  <strong>{{ selectedGroup.gatewayName || "--" }}</strong>
                </div>
              </div>
              <div class="overview-list">
                <div class="overview-item">
                  <span>所属企业</span>
                  <strong>{{ selectedEntName || selectedGroup.entId || "--" }}</strong>
                </div>
                <div class="overview-item">
                  <span>设备组类型</span>
                  <strong>{{ selectedGroup.deviceGroupTypeName || "--" }}</strong>
                </div>
                <div class="overview-item">
                  <span>供能类型</span>
                  <strong>{{ selectedGroup.energyType || "--" }}</strong>
                </div>
                <div class="overview-item">
                  <span>备注</span>
                  <strong>{{ selectedGroup.remark || "暂无备注" }}</strong>
                </div>
              </div>
            </template>
            <div v-else class="overview-empty">
              <p>选择左侧设备组后，这里会展示设备组摘要信息。</p>
            </div>
          </section>
        </aside>

        <section class="device-panel">
          <div class="device-panel-head">
            <div>
              <p class="panel-kicker">DEVICE INVENTORY</p>
              <h3>{{ devicePanelTitle }}</h3>
              <p class="device-panel-subtitle">{{ devicePanelSubtitle }}</p>
            </div>
            <div class="actions">
              <el-button
                type="primary"
                size="small"
                icon="el-icon-plus"
                :disabled="!selectedGroupId"
                @click="openDeviceDialog()"
              >
                新增设备
              </el-button>
            </div>
          </div>

          <div class="device-filter-bar">
            <div class="filters">
              <el-select
                v-model="filters.deviceTypeCode"
                clearable
                size="small"
                placeholder="设备类型"
                @change="reloadDevices"
              >
                <el-option
                  v-for="item in deviceTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
              <el-input
                v-model.trim="filters.deviceName"
                size="small"
                clearable
                placeholder="输入设备名称搜索"
                @input="handleDeviceSearch"
              >
                <i slot="prefix" class="el-input__icon el-icon-search" />
              </el-input>
            </div>
            <div class="device-pills">
              <span class="device-pill">{{ selectedEntName || "未选择企业" }}</span>
              <span class="device-pill">{{ selectedGroup ? `${selectedGroup.deviceCount || 0} 台设备` : "等待设备组" }}</span>
            </div>
          </div>

          <div v-if="!selectedGroupId" class="device-empty-state">
            <div class="device-empty-mark">IoT</div>
            <h4>暂无可维护的物联设备</h4>
            <p>先在左侧创建设备组，再添加设备并维护设备点表。</p>
            <el-button type="primary" size="small" @click="openGroupDialog()">创建设备组</el-button>
          </div>

          <div v-else class="device-table-wrapper">
            <el-table
              v-loading="deviceLoading"
              :data="deviceList"
              border
              stripe
              size="small"
              class="device-table"
            >
              <el-table-column prop="deviceName" label="设备名称" min-width="140" />
              <el-table-column prop="deviceTypeName" label="设备类型" min-width="120" />
              <el-table-column label="品牌" min-width="120">
                <template slot-scope="{ row }">
                  {{ findParam(row, "brand") }}
                </template>
              </el-table-column>
              <el-table-column label="型号" min-width="120">
                <template slot-scope="{ row }">
                  {{ findParam(row, "model") }}
                </template>
              </el-table-column>
              <el-table-column prop="gatewayName" label="所属网关" min-width="120" />
              <el-table-column prop="thirdPartyApi" label="第三方API" min-width="120" />
              <el-table-column prop="thirdPartyCode" label="第三方标识" min-width="140" />
              <el-table-column label="操作" width="220">
                <template slot-scope="{ row }">
                  <el-button type="text" size="mini" @click="openDeviceDialog(row)">基础信息</el-button>
                  <el-button type="text" size="mini" @click="openPointDialog(row)">设备点表</el-button>
                  <el-button type="text" size="mini" @click="removeDevice(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-pagination
              class="pagination"
              background
              layout="total, sizes, prev, pager, next, jumper"
              :current-page="page.pageNum"
              :page-sizes="[10, 20, 50, 100]"
              :page-size="page.pageSize"
              :total="page.total"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
            />
          </div>
        </section>
      </div>

      <DeviceGroupDialog
        :visible.sync="showGroupDialog"
        :aggregator-id="aggregatorId"
        :ent-id="selectedEntId"
        :edit-data="editingGroup"
        @success="handleGroupSaved"
      />
      <DeviceDialog
        :visible.sync="showDeviceDialog"
        :aggregator-id="aggregatorId"
        :ent-id="selectedEntId"
        :device-group-id="selectedGroupId"
        :edit-data="editingDevice"
        @success="reloadDevices"
      />
      <DevicePointDialog
        :visible.sync="showPointDialog"
        :device="activeDevice"
      />
    </section>

    <div v-else class="telemetry-wrapper">
      <AggregationHistory
        :key="`${selectedEntId || 'none'}-device-operation`"
        :active-obj="{}"
        :active-comp-name="['AggregationHistory']"
        view-type="device-operation"
      />
    </div>
  </div>
</template>

<script>
import AggregationHistory from "@/modules/load-aggregation/history/src/AggregationHistory.vue";
import {
  deleteDevice,
  listDeviceGroups,
  listDevices,
  listDeviceTypes,
  listEnterprises,
} from "./api/index.js";
import DeviceDialog from "./components/DeviceDialog.vue";
import DeviceGroupDialog from "./components/DeviceGroupDialog.vue";
import DevicePointDialog from "./components/DevicePointDialog.vue";

export default {
  name: "IotManagement",
  components: {
    AggregationHistory,
    DeviceDialog,
    DeviceGroupDialog,
    DevicePointDialog,
  },
  props: {
    aggregatorId: {
      type: String,
      default: "",
    },
  },
  data() {
    return {
      tabs: [
        { key: "device-management", label: "物联设备" },
        { key: "telemetry", label: "物联数据" },
      ],
      activeTab: "device-management",
      entOptions: [],
      selectedEntId: "",
      groupLoading: false,
      deviceLoading: false,
      deviceGroups: [],
      selectedGroupId: null,
      deviceTypeOptions: [],
      filters: {
        deviceTypeCode: "",
        deviceName: "",
      },
      deviceList: [],
      page: {
        pageNum: 1,
        pageSize: 10,
        total: 0,
      },
      showGroupDialog: false,
      showDeviceDialog: false,
      showPointDialog: false,
      editingGroup: null,
      editingDevice: null,
      activeDevice: null,
      searchTimer: null,
    };
  },
  watch: {
    aggregatorId: {
      immediate: true,
      handler() {
        this.bootstrap();
      },
    },
  },
  computed: {
    selectedGroup() {
      return this.deviceGroups.find(item => item.id === this.selectedGroupId) || null;
    },
    selectedEntName() {
      const ent = this.entOptions.find(item => item.entId === this.selectedEntId);
      return ent ? (ent.entName || ent.entId) : "";
    },
    devicePanelTitle() {
      return this.selectedGroup ? this.selectedGroup.deviceGroupName : "物联设备";
    },
    devicePanelSubtitle() {
      if (!this.selectedEntId) {
        return "请先选择企业，再加载设备组与设备数据。";
      }
      if (!this.selectedGroup) {
        return "当前企业下还没有可用设备组，先在左侧完成设备组配置。";
      }
      return `${this.selectedEntName || this.selectedEntId} · 当前设备组共 ${this.selectedGroup.deviceCount || 0} 台设备`;
    },
  },
  methods: {
    async bootstrap() {
      await this.loadEntOptions();
      await this.loadDeviceTypes();
      if (this.selectedEntId) {
        await this.reloadGroups();
      }
    },
    async loadEntOptions() {
      const aggregatorId = this.aggregatorId || sessionStorage.getItem("aggregatorId") || "";
      if (!aggregatorId) {
        this.entOptions = [];
        this.selectedEntId = "";
        return;
      }
      const res = await listEnterprises({ aggregatorId });
      const body = res.data || {};
      this.entOptions = body.code === 200 && Array.isArray(body.data) ? body.data : [];
      if (!this.entOptions.length) {
        this.selectedEntId = "";
        return;
      }
      const current = this.entOptions.find(item => item.entId === this.selectedEntId);
      this.selectedEntId = current ? current.entId : this.entOptions[0].entId;
      sessionStorage.setItem("entId", this.selectedEntId);
    },
    async loadDeviceTypes() {
      const res = await listDeviceTypes();
      const body = res.data || {};
      this.deviceTypeOptions = body.code === 200 && Array.isArray(body.data) ? body.data : [];
    },
    async reloadGroups() {
      if (!this.selectedEntId) {
        this.deviceGroups = [];
        this.selectedGroupId = null;
        this.deviceList = [];
        return;
      }
      this.groupLoading = true;
      try {
        const res = await listDeviceGroups({
          aggregatorId: this.aggregatorId,
          entId: this.selectedEntId,
        });
        const body = res.data || {};
        this.deviceGroups = body.code === 200 && Array.isArray(body.data) ? body.data : [];
        if (!this.deviceGroups.length) {
          this.selectedGroupId = null;
          this.deviceList = [];
          return;
        }
        const selected = this.deviceGroups.find(item => item.id === this.selectedGroupId);
        this.selectedGroupId = selected ? selected.id : this.deviceGroups[0].id;
        await this.reloadDevices();
      } finally {
        this.groupLoading = false;
      }
    },
    async reloadDevices() {
      if (!this.selectedEntId || !this.selectedGroupId) {
        this.deviceList = [];
        this.page.total = 0;
        return;
      }
      this.deviceLoading = true;
      try {
        const res = await listDevices({
          aggregatorId: this.aggregatorId,
          entId: this.selectedEntId,
          deviceGroupId: this.selectedGroupId,
          deviceTypeCode: this.filters.deviceTypeCode || undefined,
          deviceName: this.filters.deviceName || undefined,
          pageNum: this.page.pageNum,
          pageSize: this.page.pageSize,
        });
        const body = res.data || {};
        const page = body.code === 200 && body.data ? body.data : {};
        this.deviceList = Array.isArray(page.list) ? page.list : [];
        this.page.total = page.total || 0;
      } finally {
        this.deviceLoading = false;
      }
    },
    handleEntChange() {
      sessionStorage.setItem("entId", this.selectedEntId || "");
      this.page.pageNum = 1;
      this.reloadGroups();
    },
    selectGroup(group) {
      this.selectedGroupId = group.id;
      this.page.pageNum = 1;
      this.reloadDevices();
    },
    handlePageChange(pageNum) {
      this.page.pageNum = pageNum;
      this.reloadDevices();
    },
    handleSizeChange(pageSize) {
      this.page.pageSize = pageSize;
      this.page.pageNum = 1;
      this.reloadDevices();
    },
    handleDeviceSearch() {
      window.clearTimeout(this.searchTimer);
      this.searchTimer = window.setTimeout(() => {
        this.page.pageNum = 1;
        this.reloadDevices();
      }, 300);
    },
    openDeviceDialog(device) {
      this.editingDevice = device || null;
      this.showDeviceDialog = true;
    },
    openPointDialog(device) {
      this.activeDevice = device;
      this.showPointDialog = true;
    },
    async removeDevice(device) {
      await this.$confirm(`确认删除设备“${device.deviceName}”吗？`, "删除确认", {
        type: "warning",
      });
      const res = await deleteDevice(device.id);
      const body = res.data || {};
      if (body.code === 200) {
        this.$message.success("删除成功");
        if (this.deviceList.length === 1 && this.page.pageNum > 1) {
          this.page.pageNum -= 1;
        }
        this.reloadDevices();
      } else {
        this.$message.error(body.msg || "删除失败");
      }
    },
    openGroupDialog(group) {
      this.editingGroup = group || null;
      this.showGroupDialog = true;
    },
    async removeGroup(group) {
      await this.$confirm(`确认删除设备组“${group.deviceGroupName}”吗？`, "删除确认", {
        type: "warning",
      });
      const api = await import("./api/index.js");
      const res = await api.deleteDeviceGroup(group.id);
      const body = res.data || {};
      if (body.code === 200) {
        this.$message.success("删除成功");
        if (this.selectedGroupId === group.id) {
          this.selectedGroupId = null;
        }
        this.reloadGroups();
      } else {
        this.$message.error(body.msg || "删除失败");
      }
    },
    handleGroupSaved() {
      this.editingGroup = null;
      this.reloadGroups();
    },
    findParam(row, code) {
      const params = Array.isArray(row.paramList) ? row.paramList : [];
      const item = params.find(param => param.paramCode === code);
      return item && item.paramValue ? item.paramValue : "--";
    },
  },
};
</script>

<style scoped lang="less">
.iot-management-page {
  min-height: 100%;
  min-width: 0;
  padding: 20px;
  border: 1px solid #dde6ed;
  border-radius: 8px;
  background: #ffffff;
  color: #1f2933;
}

.iot-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.iot-tab {
  height: 34px;
  padding: 0 14px;
  border: 1px solid #cfdce5;
  border-radius: 6px;
  background: #f7fbff;
  color: #334e5c;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.iot-tab.active,
.iot-tab:hover {
  border-color: #0780ed;
  background: #0780ed;
  color: #ffffff;
}

.device-management {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
}

.toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  min-width: 0;
}

.toolbar-copy {
  min-width: 0;
}

.toolbar-kicker,
.panel-kicker {
  margin: 0 0 4px;
  color: #607d8f;
  font-size: 12px;
  font-weight: 700;
  line-height: 1.2;
}

.toolbar-copy h3 {
  margin: 0;
  color: #0e2638;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.4;
}

.toolbar-subtitle {
  margin: 6px 0 0;
  color: #607d8f;
  font-size: 13px;
  line-height: 1.5;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
  min-height: 34px;
  padding-top: 2px;
}

.toolbar-label {
  color: #607d8f;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.enterprise-select {
  width: 260px;
}

.layout {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 16px;
  min-width: 0;
  min-height: 600px;
  align-items: start;
}

.group-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.group-panel,
.group-overview,
.device-panel {
  background: #ffffff;
  border: 1px solid #dde6ed;
  border-radius: 8px;
  min-width: 0;
  overflow: hidden;
}

.group-panel {
  display: flex;
  flex-direction: column;
}

.device-panel {
  display: flex;
  flex-direction: column;
  min-height: 600px;
}

.panel-head,
.device-panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border-bottom: 1px solid #eef3f7;
}

.panel-head h3,
.device-panel-head h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #163247;
  line-height: 1.4;
}

.device-panel-subtitle {
  margin: 4px 0 0;
  color: #607d8f;
  font-size: 13px;
  line-height: 1.5;
}

.group-list {
  padding: 12px;
  min-height: 0;
  max-height: 380px;
  overflow: auto;
}

.group-item {
  width: 100%;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  padding: 12px 14px;
  margin-bottom: 8px;
  border: 1px solid #e5edf4;
  border-radius: 6px;
  background: #f9fbfd;
  cursor: pointer;
  text-align: left;
  transition: all 0.2s;
}

.group-item:hover,
.group-item.active {
  border-color: #0780ed;
  background: #eef7ff;
}

.group-main {
  min-width: 0;
}

.group-title-row {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.group-name {
  overflow: hidden;
  color: #0e2638;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.5;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-flag,
.group-badge,
.overview-status,
.device-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 22px;
  padding: 0 8px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 1;
  white-space: nowrap;
}

.group-flag {
  border: 1px solid #bfe8e8;
  background: #ecfafa;
  color: #0f8f8f;
}

.group-meta {
  margin: 4px 0 0;
  overflow: hidden;
  color: #6d8797;
  font-size: 12px;
  line-height: 1.5;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex: 0 0 auto;
}

.group-badge {
  background: #edf2f7;
  color: #486172;
}

.group-actions {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.group-empty,
.device-empty-state,
.overview-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  text-align: center;
}

.group-empty {
  min-height: 220px;
  padding: 20px 12px;
}

.group-empty-icon,
.device-empty-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #eef7ff;
  color: #0780ed;
  font-weight: 700;
}

.group-empty-icon {
  width: 38px;
  height: 38px;
  font-size: 14px;
}

.group-empty-title {
  margin: 12px 0 4px;
  color: #0e2638;
  font-size: 14px;
  font-weight: 700;
}

.group-empty-desc {
  margin: 0 0 14px;
  color: #607d8f;
  font-size: 13px;
  line-height: 1.5;
}

.group-overview {
  padding: 16px;
}

.overview-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.overview-head h4 {
  margin: 0;
  color: #163247;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.4;
}

.overview-status {
  border: 1px solid #d9e4ec;
  background: #f5f7fa;
  color: #607d8f;
}

.overview-status.virtual {
  border-color: #bfe8e8;
  background: #ecfafa;
  color: #0f8f8f;
}

.overview-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.metric-card {
  min-width: 0;
  padding: 12px;
  border: 1px solid #e5edf4;
  border-radius: 6px;
  background: #fafafa;
}

.metric-card span {
  display: block;
  color: #607d8f;
  font-size: 12px;
  line-height: 1.3;
}

.metric-card strong {
  display: block;
  margin-top: 4px;
  overflow: hidden;
  color: #0e2638;
  font-size: 18px;
  line-height: 1.3;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.overview-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.overview-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;
  color: #607d8f;
  font-size: 13px;
  line-height: 1.5;
}

.overview-item span {
  flex: 0 0 auto;
  white-space: nowrap;
}

.overview-item strong {
  min-width: 0;
  color: #0e2638;
  font-weight: 500;
  text-align: right;
  word-break: break-word;
}

.overview-empty {
  min-height: 120px;
  padding: 12px;
  color: #90a4b2;
  font-size: 13px;
  line-height: 1.5;
}

.overview-empty p {
  margin: 0;
}

.device-filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 18px;
  border-bottom: 1px solid #eef3f7;
  background: #fafafa;
}

.filters {
  display: flex;
  gap: 12px;
  min-width: 0;
  flex-wrap: wrap;
}

.filters ::v-deep .el-select,
.filters ::v-deep .el-input {
  width: 200px;
}

.actions {
  display: flex;
  gap: 12px;
  flex: 0 0 auto;
}

.device-pills {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  min-width: 0;
  flex-wrap: wrap;
}

.device-pill {
  border: 1px solid #d9e4ec;
  background: #ffffff;
  color: #486172;
}

.device-empty-state {
  flex: 1;
  min-height: 360px;
  padding: 48px 18px;
  color: #607d8f;
}

.device-empty-mark {
  width: 52px;
  height: 52px;
  margin-bottom: 14px;
  font-size: 16px;
}

.device-empty-state h4 {
  margin: 0 0 8px;
  color: #0e2638;
  font-size: 16px;
  font-weight: 700;
}

.device-empty-state p {
  margin: 0 0 16px;
  font-size: 13px;
  line-height: 1.5;
}

.device-table-wrapper {
  min-width: 0;
  padding: 16px 18px 20px;
}

.device-table {
  width: 100%;
}

.device-table ::v-deep .el-table__header th {
  background: #f5f7fa;
  color: #0e2638;
  font-weight: 600;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.empty-text {
  margin: 24px 0;
  text-align: center;
  color: #90a4b2;
}

.telemetry-wrapper {
  min-height: 640px;
}

@media (max-width: 1180px) {
  .layout {
    grid-template-columns: 280px minmax(0, 1fr);
  }

  .device-filter-bar {
    align-items: flex-start;
    flex-direction: column;
  }

  .device-pills {
    justify-content: flex-start;
  }
}

@media (max-width: 900px) {
  .toolbar,
  .device-panel-head {
    align-items: stretch;
    flex-direction: column;
  }

  .toolbar-left {
    align-items: flex-start;
    flex-direction: column;
  }

  .enterprise-select,
  .filters ::v-deep .el-select,
  .filters ::v-deep .el-input {
    width: 100%;
  }

  .layout {
    grid-template-columns: 1fr;
  }

  .group-list {
    max-height: none;
  }
}
</style>
