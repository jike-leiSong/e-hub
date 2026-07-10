<template>
  <div class="iot-telemetry-page">
    <div class="page-toolbar">
      <div class="toolbar-copy">
        <p class="toolbar-kicker">IOT TELEMETRY</p>
        <h3>物联数据查询</h3>
        <p class="toolbar-subtitle">查询设备测点时序数据，支持分钟/聚合维度，支持导出 Excel。</p>
      </div>
      <div class="toolbar-actions">
        <el-button type="warning" size="small" icon="el-icon-magic-stick" @click="openMockDialog">
          生成P功率数据
        </el-button>
      </div>
    </div>

    <div class="query-bar">
      <div class="query-row">
        <div class="query-field">
          <span class="field-label">企业</span>
          <el-select
            v-model="form.entId"
            filterable
            clearable
            size="small"
            class="field-select"
            placeholder="全部企业"
            @change="onEntChange"
          >
            <el-option label="全部企业" value="" />
            <el-option
              v-for="item in entOptions"
              :key="item.entId"
              :label="item.entName || item.entId"
              :value="item.entId"
            />
          </el-select>
        </div>

        <div class="query-field">
          <span class="field-label">项目</span>
          <el-select
            v-model="form.energyStationCode"
            filterable
            clearable
            size="small"
            class="field-select-wide"
            placeholder="选择项目"
            :disabled="!form.entId"
            @change="onProjectChange"
          >
            <el-option
              v-for="item in projectOptions"
              :key="item.energyStationCode"
              :label="formatProjectLabel(item)"
              :value="item.energyStationCode"
            />
          </el-select>
        </div>

        <div class="query-field">
          <span class="field-label">设备</span>
          <el-select
            v-model="form.deviceIds"
            multiple
            filterable
            clearable
            collapse-tags
            size="small"
            class="field-select-wide"
            placeholder="选择设备（可多选）"
            @visible-change="onDeviceDropdownToggle"
          >
            <el-option
              v-for="item in deviceOptions"
              :key="item.id"
              :label="item.deviceName"
              :value="item.id"
            />
          </el-select>
        </div>

        <div class="query-field">
          <span class="field-label">测点编码</span>
          <el-select
            v-model="form.pointCodes"
            multiple
            filterable
            allow-create
            default-first-option
            clearable
            collapse-tags
            size="small"
            class="field-select-wide"
            placeholder="输入或选择测点编码"
          >
            <el-option
              v-for="code in pointCodeOptions"
              :key="code"
              :label="code"
              :value="code"
            />
          </el-select>
        </div>
      </div>

      <div class="query-row">
        <div class="query-field">
          <span class="field-label">开始时间</span>
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            size="small"
            class="field-select"
            placeholder="开始时间"
            format="yyyy-MM-dd HH:mm"
            value-format="yyyy-MM-dd HH:mm:ss"
            :default-time="'00:00:00'"
          />
        </div>

        <div class="query-field">
          <span class="field-label">结束时间</span>
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            size="small"
            class="field-select"
            placeholder="结束时间"
            format="yyyy-MM-dd HH:mm"
            value-format="yyyy-MM-dd HH:mm:ss"
            :default-time="'23:59:59'"
          />
        </div>

        <div class="query-field">
          <span class="field-label">数据粒度</span>
          <el-select v-model="form.aggType" size="small" class="field-select-sm">
            <el-option label="分钟粒度" value="minute" />
            <el-option label="小时聚合" value="hour" />
            <el-option label="日聚合" value="day" />
          </el-select>
        </div>

        <div class="query-field" v-if="form.aggType !== 'minute'">
          <span class="field-label">聚合函数</span>
          <el-select v-model="form.aggFunc" size="small" class="field-select-sm">
            <el-option label="平均值(avg)" value="avg" />
            <el-option label="最大值(max)" value="max" />
            <el-option label="最小值(min)" value="min" />
            <el-option label="求和(sum)" value="sum" />
          </el-select>
        </div>

        <div class="query-actions">
          <el-button type="primary" size="small" icon="el-icon-search" :loading="loading" @click="doSearch">
            查询
          </el-button>
          <el-button size="small" icon="el-icon-refresh" @click="resetForm">重置</el-button>
          <el-button
            type="success"
            size="small"
            icon="el-icon-download"
            :loading="exporting"
            @click="doExport"
          >
            导出 Excel
          </el-button>
        </div>
      </div>
    </div>

    <el-dialog
      :visible.sync="showMockDialog"
      title="生成P功率数据"
      width="760px"
      top="8vh"
      @close="mockResult = null"
    >
      <el-form :model="mockForm" label-width="100px" size="small" class="mock-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="企业">
              <el-select
                v-model="mockForm.entId"
                class="full-field"
                clearable
                filterable
                placeholder="全部企业"
                @change="onMockEntChange"
              >
                <el-option label="全部企业" value="" />
                <el-option
                  v-for="item in entOptions"
                  :key="item.entId"
                  :label="item.entName || item.entId"
                  :value="item.entId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目">
              <el-select
                v-model="mockForm.energyStationCode"
                class="full-field"
                clearable
                filterable
                :disabled="!mockForm.entId"
                :placeholder="mockForm.entId ? '全部项目' : '选择企业后筛选项目'"
                @change="onMockProjectChange"
              >
                <el-option
                  v-for="item in mockProjectOptions"
                  :key="item.energyStationCode"
                  :label="formatProjectLabel(item)"
                  :value="item.energyStationCode"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="设备">
          <el-select
            v-model="mockForm.deviceIds"
            class="full-field"
            multiple
            filterable
            clearable
            collapse-tags
            :disabled="!mockForm.entId"
            :placeholder="mockForm.entId ? '不选则生成所选企业/项目下全部设备' : '不选则生成全部企业设备'"
          >
            <el-option
              v-for="item in mockDeviceOptions"
              :key="item.id"
              :label="formatDeviceLabel(item)"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始时间">
              <el-date-picker
                v-model="mockForm.startTime"
                type="datetime"
                class="full-field"
                format="yyyy-MM-dd HH:mm"
                value-format="yyyy-MM-dd HH:mm:ss"
                :default-time="'00:00:00'"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间">
              <el-date-picker
                v-model="mockForm.endTime"
                type="datetime"
                class="full-field"
                format="yyyy-MM-dd HH:mm"
                value-format="yyyy-MM-dd HH:mm:ss"
                :default-time="'23:59:59'"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="采样间隔">
              <el-input-number
                v-model="mockForm.intervalSeconds"
                class="full-field"
                :min="60"
                :max="3600"
                :step="60"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="测点">
              <el-input value="P 功率" disabled />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div v-if="mockResult" class="mock-result">
        <div class="mock-result-grid">
          <div><span>设备</span><strong>{{ mockResult.deviceCount || 0 }}</strong></div>
          <div><span>P测点</span><strong>{{ mockResult.pointCount || 0 }}</strong></div>
          <div><span>成功</span><strong>{{ mockResult.success || 0 }}</strong></div>
          <div><span>失败</span><strong>{{ mockResult.fail || 0 }}</strong></div>
        </div>
        <p class="mock-message">{{ mockResult.message }}</p>
        <ul v-if="mockResult.warnings && mockResult.warnings.length" class="mock-warnings">
          <li v-for="item in mockResult.warnings" :key="item">{{ item }}</li>
        </ul>
      </div>

      <span slot="footer">
        <el-button @click="showMockDialog = false">关闭</el-button>
        <el-button type="primary" :loading="mockSubmitting" @click="submitMockPowerData">
          开始生成
        </el-button>
      </span>
    </el-dialog>

    <div class="result-area">
      <div v-if="loading" v-loading="loading" class="loading-mask" />
      <div v-else-if="!tableData.length && searched" class="empty-state">
        <p class="empty-title">暂无数据</p>
        <p class="empty-desc">当前筛选条件下无物联测点分钟数据，请调整查询条件。</p>
      </div>
      <div v-else-if="!searched" class="empty-state">
        <p class="empty-title">请设置查询条件后点击查询</p>
        <p class="empty-desc">支持按企业、设备、测点、时间范围筛选分钟粒度时序数据。</p>
      </div>
      <template v-else>
        <el-table
          :data="tableData"
          border
          stripe
          size="small"
          class="data-table"
        >
          <el-table-column prop="deviceCode" label="设备编码" min-width="120" />
          <el-table-column prop="deviceName" label="设备名称" min-width="140" />
          <el-table-column prop="pointCode" label="测点编码" min-width="140" />
          <el-table-column prop="pointName" label="测点名称" min-width="120" />
          <el-table-column v-if="form.aggType !== 'minute'" label="区间" min-width="160">
            <template slot-scope="{ row }">
              {{ fmtTime(row.startTime) }} ~ {{ fmtTime(row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column v-if="form.aggType === 'minute'" prop="dataTime" label="数据时间" min-width="160">
            <template slot-scope="{ row }">
              {{ fmtTime(row.dataTime) }}
            </template>
          </el-table-column>
          <el-table-column label="数据值" min-width="100">
            <template slot-scope="{ row }">
              <span v-if="form.aggType === 'minute'">{{ fmtValue(row.value) }} {{ row.unit }}</span>
              <span v-else>{{ fmtValue(getAggValue(row)) }} {{ row.unit }}</span>
            </template>
          </el-table-column>
          <template v-if="form.aggType !== 'minute'">
            <el-table-column prop="maxValue" label="最大值" min-width="100">
              <template slot-scope="{ row }">{{ fmtValue(row.maxValue) }}</template>
            </el-table-column>
            <el-table-column prop="minValue" label="最小值" min-width="100">
              <template slot-scope="{ row }">{{ fmtValue(row.minValue) }}</template>
            </el-table-column>
            <el-table-column prop="count" label="有效条数" min-width="100" />
          </template>
          <el-table-column prop="quality" label="质量" min-width="80">
            <template slot-scope="{ row }">
              <span class="point-quality" :class="row.quality">{{ row.quality }}</span>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-if="page.total > 0"
          class="result-pagination"
          background
          layout="total, prev, pager, next"
          :current-page="page.pageNum"
          :page-size="page.pageSize"
          :total="page.total"
          @current-change="handlePageChange"
        />
      </template>
    </div>
  </div>
</template>

<script>
import moment from "moment";
import { downLoadXls } from "@/utils/util.js";
import axios from "axios";
import {
  baseUrl,
} from "../history/api/index.js";
import {
  listEnterprises,
  listDevices,
  listDevicePoints,
  listProjectsByEnt,
  generatePowerData,
  queryTelemetryData,
} from "./api/index.js";

export default {
  name: "IotTelemetry",
  props: {
    aggregatorId: {
      type: String,
      default: "",
    },
  },
  data() {
    return {
      entOptions: [],
      projectOptions: [],
      deviceOptions: [],
      pointCodeOptions: [],
      mockProjectOptions: [],
      mockDeviceOptions: [],

      form: {
        entId: "",
        energyStationCode: "",
        deviceIds: [],
        pointCodes: [],
        startTime: "",
        endTime: "",
        aggType: "minute",
        aggFunc: "avg",
      },

      tableData: [],
      searched: false,

      loading: false,
      exporting: false,
      mockSubmitting: false,
      showMockDialog: false,
      mockResult: null,
      mockForm: {
        entId: "",
        energyStationCode: "",
        deviceIds: [],
        startTime: "",
        endTime: "",
        intervalSeconds: 60,
      },

      page: { pageNum: 1, pageSize: 20, total: 0 },
    };
  },
  watch: {
    aggregatorId: {
      immediate: true,
      handler(val) {
        this.loadEnts();
      },
    },
  },
  methods: {
    async loadEnts() {
      const aggId = this.aggregatorId || sessionStorage.getItem("aggregatorId") || "";
      if (!aggId) return;
      const res = await listEnterprises({ aggregatorId: aggId });
      const body = res.data || {};
      const list = body.code === 200 && Array.isArray(body.data) ? body.data : [];
      this.entOptions = list;
      if (this.form.entId && !list.some(item => item.entId === this.form.entId)) {
        this.form.entId = "";
      }
      this.resetEntDependentFields();
    },

    onEntChange() {
      sessionStorage.setItem("entId", this.form.entId || "");
      this.resetEntDependentFields();
    },

    resetEntDependentFields() {
      this.form.energyStationCode = "";
      this.form.deviceIds = [];
      this.form.pointCodes = [];
      this.projectOptions = [];
      this.pointCodeOptions = [];
      this.loadProjects();
      this.loadDevices();
    },

    onProjectChange() {
      this.form.deviceIds = [];
      this.form.pointCodes = [];
      this.pointCodeOptions = [];
    },

    async loadProjects() {
      const aggId = this.aggregatorId || sessionStorage.getItem("aggregatorId") || "";
      if (!this.form.entId) {
        this.projectOptions = [];
        return;
      }
      const res = await listProjectsByEnt(this.form.entId, aggId);
      const body = res.data || {};
      this.projectOptions = body.code === 200 && Array.isArray(body.data) ? body.data : [];
    },

    async loadDevices() {
      const aggId = this.aggregatorId || sessionStorage.getItem("aggregatorId") || "";
      if (!this.form.entId) {
        this.deviceOptions = [];
        return;
      }
      const res = await listDevices({
        aggregatorId: aggId,
        entId: this.form.entId,
        pageNum: 1,
        pageSize: 1000,
      });
      const body = res.data || {};
      const page = body.code === 200 && body.data ? body.data : {};
      this.deviceOptions = Array.isArray(page.list) ? page.list : [];
      this.pointCodeOptions = [];
    },

    onDeviceDropdownToggle(visible) {
      if (visible) return;
      this.loadDevicePoints();
    },

    loadDevicePoints() {
      this.pointCodeOptions = [];
      const ids = this.form.deviceIds;
      if (!ids || ids.length === 0) return;
      const promises = ids.map(id =>
        listDevicePoints(id, { pageNum: 1, pageSize: 1000 }).then(res => {
          const body = res.data || {};
          const page = body.code === 200 && body.data ? body.data : {};
          return (Array.isArray(page.list) ? page.list : []).map(p => p.pointCode);
        }).catch(() => [])
      );
      Promise.all(promises).then(results => {
        const all = new Set();
        results.forEach(arr => arr.forEach(c => all.add(c)));
        this.pointCodeOptions = [...all];
      });
    },

    resetForm() {
      this.form.startTime = "";
      this.form.endTime = "";
      this.form.energyStationCode = "";
      this.form.deviceIds = [];
      this.form.pointCodes = [];
      this.form.aggType = "minute";
      this.form.aggFunc = "avg";
      this.tableData = [];
      this.page = { pageNum: 1, pageSize: 20, total: 0 };
      this.searched = false;
    },

    async doSearch() {
      this.page.pageNum = 1;
      this.searched = true;
      this.loading = true;
      try {
        await this.loadData();
      } finally {
        this.loading = false;
      }
    },

    async loadData() {
      const res = await queryTelemetryData({
        entId: this.form.entId,
        energyStationCode: this.form.energyStationCode || undefined,
        deviceIds: this.form.deviceIds.length ? this.form.deviceIds : undefined,
        pointCodes: this.form.pointCodes.length ? this.form.pointCodes : undefined,
        startTime: this.form.startTime || undefined,
        endTime: this.form.endTime || undefined,
        aggType: this.form.aggType,
        aggFunc: this.form.aggFunc,
        limit: this.page.pageSize,
        offset: (this.page.pageNum - 1) * this.page.pageSize,
      });
      const body = res.data || {};
      const page = body.code === 200 && body.data ? body.data : {};
      this.tableData = Array.isArray(page.list) ? page.list : [];
      this.page.total = page.total || 0;
    },

    handlePageChange(page) {
      this.page.pageNum = page;
      this.loadData();
    },

    async doExport() {
      this.exporting = true;
      try {
        const ticket = sessionStorage.getItem("ticket");
        const token = sessionStorage.getItem("token") || sessionStorage.getItem("ticket");
        if (!ticket && !token) {
          this.$message.error("导出失败：用户未登录，请重新登录");
          return;
        }
        const params = {
          entId: this.form.entId,
          energyStationCode: this.form.energyStationCode || undefined,
          aggType: this.form.aggType,
          aggFunc: this.form.aggFunc,
          limit: 10000,
        };
        if (this.form.deviceIds && this.form.deviceIds.length) {
          params.deviceIds = this.form.deviceIds.join(",");
        }
        if (this.form.pointCodes && this.form.pointCodes.length) {
          params.pointCodes = this.form.pointCodes.join(",");
        }
        if (this.form.startTime) params.startTime = this.form.startTime;
        if (this.form.endTime) params.endTime = this.form.endTime;
        axios.defaults.headers.common.ticket = ticket;
        axios.defaults.headers.common.token = token;
        axios.defaults.headers.common.Authorization = `Bearer ${token}`;
        const res = await axios({
          method: "get",
          url: `${baseUrl}/iot/telemetry/export`,
          params,
          responseType: "blob",
        });
        const fileName = `物联测点数据_${this.form.entId}_${this.form.startTime || ""}~${this.form.endTime || ""}.xlsx`;
        downLoadXls(res.data, fileName);
        this.$message.success("导出成功");
      } catch (e) {
        const msg = e?.response?.data?.msg || e.message || "导出失败";
        this.$message.error("导出失败：" + msg);
      } finally {
        this.exporting = false;
      }
    },

    async openMockDialog() {
      this.mockResult = null;
      this.mockProjectOptions = [];
      this.mockDeviceOptions = [];
      this.mockForm = {
        entId: this.form.entId || "",
        energyStationCode: this.form.entId ? (this.form.energyStationCode || "") : "",
        deviceIds: this.form.entId && Array.isArray(this.form.deviceIds) ? [...this.form.deviceIds] : [],
        startTime: this.form.startTime || moment().startOf("day").format("YYYY-MM-DD HH:mm:ss"),
        endTime: this.form.endTime || moment().format("YYYY-MM-DD HH:mm:ss"),
        intervalSeconds: 60,
      };
      this.showMockDialog = true;
      if (this.mockForm.entId) {
        await this.loadMockScopeOptions();
      }
    },

    onMockEntChange() {
      this.mockForm.energyStationCode = "";
      this.mockForm.deviceIds = [];
      this.mockProjectOptions = [];
      this.mockDeviceOptions = [];
      if (this.mockForm.entId) {
        this.loadMockScopeOptions();
      }
    },

    onMockProjectChange() {
      this.mockForm.deviceIds = [];
    },

    async loadMockScopeOptions() {
      await Promise.all([
        this.loadMockProjects(),
        this.loadMockDevices(),
      ]);
    },

    async loadMockProjects() {
      const aggId = this.aggregatorId || sessionStorage.getItem("aggregatorId") || "";
      if (!this.mockForm.entId) {
        this.mockProjectOptions = [];
        return;
      }
      const res = await listProjectsByEnt(this.mockForm.entId, aggId);
      const body = res.data || {};
      this.mockProjectOptions = body.code === 200 && Array.isArray(body.data) ? body.data : [];
    },

    async loadMockDevices() {
      const aggId = this.aggregatorId || sessionStorage.getItem("aggregatorId") || "";
      if (!this.mockForm.entId) {
        this.mockDeviceOptions = [];
        return;
      }
      const res = await listDevices({
        aggregatorId: aggId,
        entId: this.mockForm.entId,
        pageNum: 1,
        pageSize: 1000,
      });
      const body = res.data || {};
      const page = body.code === 200 && body.data ? body.data : {};
      this.mockDeviceOptions = Array.isArray(page.list) ? page.list : [];
    },

    async submitMockPowerData() {
      if (!this.mockForm.startTime || !this.mockForm.endTime) {
        this.$message.warning("请选择生成时间范围");
        return;
      }
      this.mockSubmitting = true;
      try {
        const res = await generatePowerData({
          aggregatorId: this.aggregatorId || sessionStorage.getItem("aggregatorId") || "",
          entId: this.mockForm.entId || undefined,
          energyStationCode: this.mockForm.energyStationCode || undefined,
          deviceIds: this.mockForm.deviceIds && this.mockForm.deviceIds.length ? this.mockForm.deviceIds : undefined,
          startTime: this.mockForm.startTime,
          endTime: this.mockForm.endTime,
          intervalSeconds: this.mockForm.intervalSeconds || 60,
        });
        const body = res.data || {};
        if (body.code !== 200) {
          this.$message.error(body.msg || "生成失败");
          return;
        }
        this.mockResult = body.data || {};
        const hasFail = Number(this.mockResult.fail || 0) > 0;
        if (hasFail) {
          this.$message.warning(this.mockResult.message || "生成完成，存在失败数据");
        } else {
          this.$message.success(this.mockResult.message || "生成成功");
        }
        this.applyMockQueryCondition();
        await this.doSearch();
      } catch (e) {
        const msg = e && e.response && e.response.data ? e.response.data.msg : e.message;
        this.$message.error(msg || "生成失败");
      } finally {
        this.mockSubmitting = false;
      }
    },

    applyMockQueryCondition() {
      this.form.entId = this.mockForm.entId || "";
      this.form.energyStationCode = this.mockForm.energyStationCode || "";
      this.form.deviceIds = Array.isArray(this.mockForm.deviceIds) ? [...this.mockForm.deviceIds] : [];
      this.form.pointCodes = ["P"];
      this.form.startTime = this.mockForm.startTime;
      this.form.endTime = this.mockForm.endTime;
      this.form.aggType = "minute";
      this.form.aggFunc = "avg";
      this.projectOptions = [...this.mockProjectOptions];
      this.deviceOptions = [...this.mockDeviceOptions];
    },

    fmtValue(v) {
      if (v === null || v === undefined) return "--";
      return typeof v === "number" ? v.toFixed(4) : v;
    },

    getAggValue(row) {
      const keyMap = {
        avg: "avgValue",
        max: "maxValue",
        min: "minValue",
        sum: "sumValue",
      };
      return row[keyMap[this.form.aggFunc] || "avgValue"];
    },

    fmtTime(v) {
      if (!v) return "--";
      return moment(v).format("YYYY-MM-DD HH:mm:ss");
    },

    formatProjectLabel(item) {
      if (!item) return "";
      if (item.energyStation && item.energyStationCode) {
        return `${item.energyStation}（${item.energyStationCode}）`;
      }
      return item.energyStation || item.energyStationCode || "";
    },

    formatDeviceLabel(item) {
      if (!item) return "";
      return item.deviceCode ? `${item.deviceName}（${item.deviceCode}）` : item.deviceName;
    },
  },
};
</script>

<style scoped lang="less">
.iot-telemetry-page {
  min-height: 100%;
  min-width: 0;
  padding: 20px;
  border: 1px solid #dde6ed;
  border-radius: 8px;
  background: #ffffff;
  color: #1f2933;
}

.page-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.toolbar-copy {
  min-width: 0;
}

.toolbar-actions {
  flex: 0 0 auto;
}

.toolbar-kicker {
  margin: 0 0 4px;
  color: #607d8f;
  font-size: 12px;
  font-weight: 700;
  line-height: 1.2;
}
.page-toolbar h3 {
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

.query-bar {
  padding: 14px 16px;
  margin-bottom: 16px;
  background: #f7fbff;
  border: 1px solid #d9e9f4;
  border-radius: 8px;
}
.query-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 10px;
  &:last-child { margin-bottom: 0; }
}
.query-field { display: flex; align-items: center; gap: 6px; }
.field-label {
  color: #607d8f;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}
.field-select { width: 180px; }
.field-select-sm { width: 120px; }
.field-select-wide { width: 240px; }
.query-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.full-field {
  width: 100%;
}

.mock-form {
  padding-right: 8px;
}

.mock-result {
  margin-top: 14px;
  padding: 12px;
  border: 1px solid #e5edf4;
  border-radius: 6px;
  background: #f8fbfd;
}

.mock-result-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.mock-result-grid div {
  min-width: 0;
  padding: 10px;
  border: 1px solid #e5edf4;
  border-radius: 6px;
  background: #ffffff;
}

.mock-result-grid span {
  display: block;
  color: #607d8f;
  font-size: 12px;
  line-height: 1.4;
}

.mock-result-grid strong {
  display: block;
  margin-top: 4px;
  color: #0e2638;
  font-size: 18px;
  line-height: 1.2;
}

.mock-message {
  margin: 10px 0 0;
  color: #334e5c;
  font-size: 13px;
  line-height: 1.5;
}

.mock-warnings {
  margin: 8px 0 0;
  padding-left: 18px;
  color: #b45309;
  font-size: 12px;
  line-height: 1.6;
}

.result-area { min-height: 400px; }
.result-body { position: relative; min-height: 300px; }
.loading-mask { height: 300px; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  height: 300px;
  color: #607d8f;
}
.empty-title {
  margin: 0 0 6px;
  color: #0e2638;
  font-size: 15px;
  font-weight: 700;
}
.empty-desc {
  margin: 0;
  color: #607d8f;
  font-size: 13px;
  line-height: 1.6;
  max-width: 320px;
}

.data-table {
  width: 100%;
  margin-bottom: 12px;
}
.result-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.point-quality {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  &.normal { color: #67c23a; background: #f0f9eb; }
  &.matched { color: #67c23a; background: #f0f9eb; }
  &.abnormal { color: #f56c6c; background: #fef0f0; }
  &.device_not_found { color: #e6a23c; background: #fdf6ec; }
  &.point_not_found { color: #e6a23c; background: #fdf6ec; }
}

@media (max-width: 1100px) {
  .query-row {
    flex-direction: column;
    align-items: flex-start;
  }
  .query-actions { margin-left: 0; }
  .field-select, .field-select-sm, .field-select-wide { width: 160px; }
}
</style>
