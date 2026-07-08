<template>
  <div class="tariff-sources-page">
    <section class="sources-shell">
      <div class="source-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          type="button"
          :class="{ active: activeTab === tab.key }"
          @click="switchTab(tab.key)"
        >
          {{ tab.label }}
        </button>
      </div>

      <div v-if="activeTab === 'configs'" class="tab-panel">
        <div class="content-header">
          <h3>来源配置</h3>
          <el-button
            type="primary"
            size="small"
            icon="el-icon-plus"
            @click="openConfigDialog('create')"
          >
            新增来源
          </el-button>
        </div>

        <div class="filter-panel">
          <el-form :inline="true" :model="configFilters" size="small">
            <el-form-item label="省份编码">
              <el-input
                v-model.trim="configFilters.provinceCode"
                clearable
                placeholder="如 11"
                @keyup.enter.native="reloadConfigs"
              />
            </el-form-item>
            <el-form-item label="来源类型">
              <el-select
                v-model="configFilters.sourceType"
                clearable
                filterable
                placeholder="全部"
              >
                <el-option
                  v-for="item in sourceTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="来源名称">
              <el-input
                v-model.trim="configFilters.sourceName"
                clearable
                placeholder="国网/南网/发改委"
                @keyup.enter.native="reloadConfigs"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="configFilters.enabled" clearable placeholder="全部">
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" @click="reloadConfigs">
                查询
              </el-button>
              <el-button icon="el-icon-refresh" @click="resetConfigFilters">
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="data-table">
          <el-table
            v-loading="configLoading"
            :data="sourceConfigs"
            size="small"
            stripe
            border
          >
            <el-table-column label="省份" min-width="150" fixed>
              <template slot-scope="{ row }">
                <strong>{{ row.provinceName || "-" }}</strong>
                <span class="sub-text">{{ row.provinceCode || "-" }}</span>
              </template>
            </el-table-column>
            <el-table-column label="来源类型" min-width="130">
              <template slot-scope="{ row }">
                <el-tag size="mini" type="info">
                  {{ sourceTypeLabel(row.sourceType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sourceName" label="来源名称" min-width="180" show-overflow-tooltip />
            <el-table-column label="发布地址" min-width="260" show-overflow-tooltip>
              <template slot-scope="{ row }">
                <el-link
                  v-if="row.sourceUrl"
                  :href="row.sourceUrl"
                  type="primary"
                  target="_blank"
                  :underline="false"
                >
                  {{ row.sourceUrl }}
                </el-link>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="publishRule" label="发布规则" min-width="220" show-overflow-tooltip />
            <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
            <el-table-column label="状态" width="100" align="center">
              <template slot-scope="{ row }">
                <el-switch
                  :value="row.enabled === 1"
                  active-color="#13c2c2"
                  inactive-color="#c0c4cc"
                  @change="toggleConfig(row, $event)"
                />
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" min-width="160" show-overflow-tooltip />
            <el-table-column label="操作" width="110" fixed="right">
              <template slot-scope="{ row }">
                <el-button type="text" size="mini" @click="openConfigDialog('edit', row)">
                  编辑
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <div v-if="activeTab === 'documents'" class="tab-panel">
        <div class="content-header">
          <h3>来源文档</h3>
          <el-button
            type="primary"
            size="small"
            icon="el-icon-plus"
            @click="openDocumentDialog('create')"
          >
            新增文档
          </el-button>
        </div>

        <div class="filter-panel document-filter">
          <el-form :inline="true" :model="documentFilters" size="small">
            <el-form-item label="电价月份">
              <el-date-picker
                v-model="documentFilters.yearMonth"
                type="month"
                value-format="yyyy-MM"
                format="yyyy/MM"
                clearable
                placeholder="全部"
              />
            </el-form-item>
            <el-form-item label="版本">
              <el-input
                v-model.trim="documentFilters.version"
                clearable
                placeholder="请输入版本"
                @keyup.enter.native="reloadDocuments"
              />
            </el-form-item>
            <el-form-item label="省份编码">
              <el-input
                v-model.trim="documentFilters.provinceCode"
                clearable
                placeholder="如 11"
                @keyup.enter.native="reloadDocuments"
              />
            </el-form-item>
            <el-form-item label="来源类型">
              <el-select
                v-model="documentFilters.sourceType"
                clearable
                filterable
                placeholder="全部"
              >
                <el-option
                  v-for="item in sourceTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="documentFilters.status" clearable placeholder="全部">
                <el-option
                  v-for="item in statusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="批次号">
              <el-input
                v-model.trim="documentFilters.batchNo"
                clearable
                placeholder="请输入批次号"
                @keyup.enter.native="reloadDocuments"
              />
            </el-form-item>
            <el-form-item label="来源名称">
              <el-input
                v-model.trim="documentFilters.sourceName"
                clearable
                placeholder="请输入来源名称"
                @keyup.enter.native="reloadDocuments"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" @click="reloadDocuments">
                查询
              </el-button>
              <el-button icon="el-icon-refresh" @click="resetDocumentFilters">
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="data-table">
          <el-table
            v-loading="documentLoading"
            :data="sourceDocuments"
            size="small"
            stripe
            border
          >
            <el-table-column prop="yearMonth" label="月份" min-width="100" fixed />
            <el-table-column prop="version" label="版本" min-width="120" show-overflow-tooltip />
            <el-table-column label="省份" min-width="140" show-overflow-tooltip>
              <template slot-scope="{ row }">
                {{ provinceText(row) }}
              </template>
            </el-table-column>
            <el-table-column label="来源类型" min-width="130">
              <template slot-scope="{ row }">
                <el-tag size="mini" type="info">
                  {{ sourceTypeLabel(row.sourceType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sourceName" label="来源名称" min-width="170" show-overflow-tooltip />
            <el-table-column prop="documentTitle" label="文件标题" min-width="220" show-overflow-tooltip />
            <el-table-column prop="documentNo" label="文号" min-width="150" show-overflow-tooltip />
            <el-table-column prop="batchNo" label="导入批次" min-width="150" show-overflow-tooltip />
            <el-table-column label="文件" min-width="180" show-overflow-tooltip>
              <template slot-scope="{ row }">
                {{ row.sourceFileName || row.sourceFilePath || "-" }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template slot-scope="{ row }">
                <el-tag size="mini" :type="statusTagType(row.status)">
                  {{ statusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="publishTime" label="发布时间" min-width="160" show-overflow-tooltip />
            <el-table-column label="生效期" min-width="210" show-overflow-tooltip>
              <template slot-scope="{ row }">
                {{ effectiveText(row) }}
              </template>
            </el-table-column>
            <el-table-column prop="operatorName" label="操作人" min-width="120" show-overflow-tooltip />
            <el-table-column prop="updateTime" label="更新时间" min-width="160" show-overflow-tooltip />
            <el-table-column label="操作" width="180" fixed="right">
              <template slot-scope="{ row }">
                <el-button type="text" size="mini" @click="openDocumentDialog('edit', row)">
                  编辑
                </el-button>
                <el-button
                  v-if="row.status !== 'PUBLISHED'"
                  type="text"
                  size="mini"
                  @click="setDocumentStatus(row, 'PUBLISHED')"
                >
                  发布
                </el-button>
                <el-button
                  v-if="row.status !== 'ARCHIVED'"
                  type="text"
                  size="mini"
                  @click="archiveDocument(row)"
                >
                  归档
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="table-pagination"
            background
            layout="total, prev, pager, next, sizes"
            :current-page.sync="documentPage.pageIndex"
            :page-size.sync="documentPage.pageSize"
            :total="documentPage.total"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="loadDocuments"
            @size-change="handleDocumentSizeChange"
          />
        </div>
      </div>
    </section>

    <el-dialog
      :title="configDialog.mode === 'create' ? '新增来源配置' : '编辑来源配置'"
      :visible.sync="configDialog.visible"
      width="720px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="configForm"
        class="source-form"
        :model="configForm"
        :rules="configRules"
        label-width="96px"
        size="small"
      >
        <div class="form-grid">
          <el-form-item label="省份编码" prop="provinceCode">
            <el-input v-model.trim="configForm.provinceCode" placeholder="如 11" />
          </el-form-item>
          <el-form-item label="省份名称" prop="provinceName">
            <el-input v-model.trim="configForm.provinceName" placeholder="如 北京" />
          </el-form-item>
          <el-form-item label="来源类型" prop="sourceType">
            <el-select
              v-model="configForm.sourceType"
              filterable
              allow-create
              default-first-option
              placeholder="请选择"
            >
              <el-option
                v-for="item in sourceTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="来源名称" prop="sourceName">
            <el-input v-model.trim="configForm.sourceName" placeholder="请输入来源名称" />
          </el-form-item>
          <el-form-item class="full-row" label="发布地址">
            <el-input v-model.trim="configForm.sourceUrl" placeholder="https://..." />
          </el-form-item>
          <el-form-item class="full-row" label="发布规则">
            <el-input
              v-model.trim="configForm.publishRule"
              type="textarea"
              :rows="2"
              placeholder="如每月末发布下月代理购电价格"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-switch
              v-model="configForm.enabled"
              :active-value="1"
              :inactive-value="0"
              active-text="启用"
              inactive-text="停用"
            />
          </el-form-item>
          <el-form-item class="full-row" label="备注">
            <el-input v-model.trim="configForm.remark" type="textarea" :rows="2" />
          </el-form-item>
        </div>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="configDialog.visible = false">取消</el-button>
        <el-button
          type="primary"
          size="small"
          :loading="configDialog.saving"
          @click="submitConfig"
        >
          保存
        </el-button>
      </div>
    </el-dialog>

    <el-dialog
      :title="documentDialog.mode === 'create' ? '新增来源文档' : '编辑来源文档'"
      :visible.sync="documentDialog.visible"
      width="920px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="documentForm"
        class="source-form"
        :model="documentForm"
        :rules="documentRules"
        label-width="98px"
        size="small"
      >
        <div class="form-grid three-cols">
          <el-form-item label="电价月份" prop="yearMonth">
            <el-date-picker
              v-model="documentForm.yearMonth"
              type="month"
              value-format="yyyy-MM"
              format="yyyy/MM"
              placeholder="请选择"
            />
          </el-form-item>
          <el-form-item label="版本" prop="version">
            <el-input v-model.trim="documentForm.version" placeholder="如 2026-07-v1" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="documentForm.status">
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item class="full-row" label="来源配置">
            <el-select
              v-model="documentForm.sourceConfigId"
              clearable
              filterable
              placeholder="请选择"
              @change="handleDocumentConfigChange"
            >
              <el-option
                v-for="item in sourceConfigs"
                :key="item.id"
                :label="configOptionLabel(item)"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="省份编码" prop="provinceCode">
            <el-input v-model.trim="documentForm.provinceCode" placeholder="如 11" />
          </el-form-item>
          <el-form-item label="省份名称" prop="provinceName">
            <el-input v-model.trim="documentForm.provinceName" placeholder="如 北京" />
          </el-form-item>
          <el-form-item label="来源类型">
            <el-select
              v-model="documentForm.sourceType"
              clearable
              filterable
              allow-create
              default-first-option
              placeholder="请选择"
            >
              <el-option
                v-for="item in sourceTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="来源名称">
            <el-input v-model.trim="documentForm.sourceName" placeholder="请输入来源名称" />
          </el-form-item>
          <el-form-item class="span-two" label="来源地址">
            <el-input v-model.trim="documentForm.sourceUrl" placeholder="https://..." />
          </el-form-item>
          <el-form-item label="批次号">
            <el-input v-model.trim="documentForm.batchNo" placeholder="请输入批次号" />
          </el-form-item>
          <el-form-item class="span-two" label="文件标题">
            <el-input v-model.trim="documentForm.documentTitle" placeholder="请输入文件标题" />
          </el-form-item>
          <el-form-item label="文号">
            <el-input v-model.trim="documentForm.documentNo" placeholder="请输入文号" />
          </el-form-item>
          <el-form-item label="发布时间">
            <el-date-picker
              v-model="documentForm.publishTime"
              type="datetime"
              value-format="yyyy-MM-dd HH:mm:ss"
              format="yyyy/MM/dd HH:mm"
              clearable
              placeholder="请选择"
            />
          </el-form-item>
          <el-form-item label="生效开始">
            <el-date-picker
              v-model="documentForm.effectiveStart"
              type="date"
              value-format="yyyy-MM-dd"
              format="yyyy/MM/dd"
              clearable
              placeholder="请选择"
            />
          </el-form-item>
          <el-form-item label="生效结束">
            <el-date-picker
              v-model="documentForm.effectiveEnd"
              type="date"
              value-format="yyyy-MM-dd"
              format="yyyy/MM/dd"
              clearable
              placeholder="请选择"
            />
          </el-form-item>
          <el-form-item label="文件名">
            <el-input v-model.trim="documentForm.sourceFileName" placeholder="请输入文件名" />
          </el-form-item>
          <el-form-item class="span-two" label="文件路径">
            <el-input v-model.trim="documentForm.sourceFilePath" placeholder="请输入文件路径" />
          </el-form-item>
          <el-form-item class="full-row" label="文件哈希">
            <el-input v-model.trim="documentForm.sourceFileHash" placeholder="请输入文件哈希" />
          </el-form-item>
          <el-form-item label="操作人ID">
            <el-input v-model.trim="documentForm.operatorId" />
          </el-form-item>
          <el-form-item label="操作人">
            <el-input v-model.trim="documentForm.operatorName" />
          </el-form-item>
          <el-form-item class="full-row" label="备注">
            <el-input v-model.trim="documentForm.remark" type="textarea" :rows="2" />
          </el-form-item>
        </div>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="documentDialog.visible = false">取消</el-button>
        <el-button
          type="primary"
          size="small"
          :loading="documentDialog.saving"
          @click="submitDocument"
        >
          保存
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import moment from "moment";
import {
  archiveSourceDocument,
  createSourceConfig,
  createSourceDocument,
  listSourceConfigs,
  listSourceDocuments,
  setSourceConfigEnabled,
  updateSourceConfig,
  updateSourceDocument,
  updateSourceDocumentStatus,
} from "./api/index.js";

const SOURCE_TYPES = [
  { value: "NDRC", label: "发改委" },
  { value: "SGCC", label: "国家电网" },
  { value: "CSG", label: "南方电网" },
  { value: "POWER_EXCHANGE", label: "电力交易中心" },
  { value: "SELLER", label: "售电公司" },
  { value: "MANUAL", label: "人工录入" },
  { value: "API", label: "接口同步" },
];

const STATUS_OPTIONS = [
  { value: "DRAFT", label: "草稿", type: "info" },
  { value: "PUBLISHED", label: "已发布", type: "success" },
  { value: "ARCHIVED", label: "已归档", type: "warning" },
];

function emptyConfigForm() {
  return {
    id: null,
    provinceCode: "",
    provinceName: "",
    sourceName: "",
    sourceType: "",
    sourceUrl: "",
    publishRule: "",
    enabled: 1,
    remark: "",
  };
}

function emptyDocumentForm() {
  return {
    id: null,
    sourceConfigId: "",
    batchNo: "",
    yearMonth: moment().format("YYYY-MM"),
    version: "",
    provinceCode: "",
    provinceName: "",
    sourceType: "",
    sourceName: "",
    sourceUrl: "",
    sourceFileName: "",
    sourceFilePath: "",
    sourceFileHash: "",
    documentTitle: "",
    documentNo: "",
    publishTime: "",
    effectiveStart: "",
    effectiveEnd: "",
    status: "DRAFT",
    operatorId: "",
    operatorName: "",
    remark: "",
  };
}

function normalizeParams(params) {
  const result = {};
  Object.keys(params || {}).forEach(key => {
    const value = params[key];
    if (value !== "" && value !== null && value !== undefined) {
      result[key] = value;
    }
  });
  return result;
}

export default {
  name: "TariffSources",
  data() {
    return {
      tabs: [
        { key: "configs", label: "来源配置" },
        { key: "documents", label: "来源文档" },
      ],
      activeTab: "configs",
      sourceTypeOptions: SOURCE_TYPES,
      statusOptions: STATUS_OPTIONS,
      sourceConfigs: [],
      sourceDocuments: [],
      configLoading: false,
      documentLoading: false,
      configFilters: {
        provinceCode: "",
        sourceType: "",
        sourceName: "",
        enabled: "",
      },
      documentFilters: {
        yearMonth: "",
        version: "",
        provinceCode: "",
        sourceType: "",
        status: "",
        batchNo: "",
        sourceName: "",
      },
      documentPage: {
        pageIndex: 1,
        pageSize: 20,
        total: 0,
      },
      configDialog: {
        visible: false,
        mode: "create",
        saving: false,
      },
      documentDialog: {
        visible: false,
        mode: "create",
        saving: false,
      },
      configForm: emptyConfigForm(),
      documentForm: emptyDocumentForm(),
      configRules: {
        provinceCode: [{ required: true, message: "请输入省份编码", trigger: "blur" }],
        provinceName: [{ required: true, message: "请输入省份名称", trigger: "blur" }],
        sourceType: [{ required: true, message: "请选择来源类型", trigger: "change" }],
        sourceName: [{ required: true, message: "请输入来源名称", trigger: "blur" }],
      },
      documentRules: {
        yearMonth: [{ required: true, message: "请选择电价月份", trigger: "change" }],
        version: [{ required: true, message: "请输入电价版本", trigger: "blur" }],
        provinceCode: [{ required: true, message: "请输入省份编码", trigger: "blur" }],
        provinceName: [{ required: true, message: "请输入省份名称", trigger: "blur" }],
      },
    };
  },
  created() {
    this.loadConfigs();
    this.loadDocuments();
  },
  methods: {
    switchTab(tab) {
      this.activeTab = tab;
      if (tab === "documents" && !this.sourceDocuments.length) {
        this.loadDocuments();
      }
    },
    readResult(res) {
      const body = res && res.data ? res.data : {};
      if (body.code && body.code !== 200) {
        throw new Error(body.msg || "请求失败");
      }
      return body.data;
    },
    errorMessage(error, fallback) {
      if (error && error.response && error.response.data && error.response.data.msg) {
        return error.response.data.msg;
      }
      return error && error.message ? error.message : fallback;
    },
    async loadConfigs() {
      this.configLoading = true;
      try {
        const data = this.readResult(await listSourceConfigs(normalizeParams(this.configFilters)));
        this.sourceConfigs = Array.isArray(data) ? data : [];
      } catch (error) {
        this.sourceConfigs = [];
        this.$message.error(this.errorMessage(error, "来源配置加载失败"));
      } finally {
        this.configLoading = false;
      }
    },
    reloadConfigs() {
      this.loadConfigs();
    },
    resetConfigFilters() {
      this.configFilters = {
        provinceCode: "",
        sourceType: "",
        sourceName: "",
        enabled: "",
      };
      this.loadConfigs();
    },
    openConfigDialog(mode, row) {
      this.configDialog.mode = mode;
      this.configForm = mode === "edit" && row ? { ...emptyConfigForm(), ...row } : emptyConfigForm();
      this.configDialog.visible = true;
      this.$nextTick(() => {
        if (this.$refs.configForm) {
          this.$refs.configForm.clearValidate();
        }
      });
    },
    submitConfig() {
      this.$refs.configForm.validate(async valid => {
        if (!valid) {
          return;
        }
        this.configDialog.saving = true;
        try {
          const payload = { ...this.configForm };
          payload.sourceType = String(payload.sourceType || "").toUpperCase();
          if (this.configDialog.mode === "edit") {
            await updateSourceConfig(payload.id, payload);
          } else {
            await createSourceConfig(payload);
          }
          this.$message.success("来源配置已保存");
          this.configDialog.visible = false;
          await this.loadConfigs();
        } catch (error) {
          this.$message.error(this.errorMessage(error, "来源配置保存失败"));
        } finally {
          this.configDialog.saving = false;
        }
      });
    },
    async toggleConfig(row, enabled) {
      const nextEnabled = enabled ? 1 : 0;
      try {
        await setSourceConfigEnabled(row.id, nextEnabled);
        row.enabled = nextEnabled;
        this.$message.success(nextEnabled === 1 ? "来源配置已启用" : "来源配置已停用");
      } catch (error) {
        this.$message.error(this.errorMessage(error, "来源配置状态更新失败"));
        this.loadConfigs();
      }
    },
    async loadDocuments() {
      this.documentLoading = true;
      try {
        const params = normalizeParams({
          ...this.documentFilters,
          pageIndex: this.documentPage.pageIndex,
          pageSize: this.documentPage.pageSize,
        });
        const data = this.readResult(await listSourceDocuments(params)) || {};
        this.sourceDocuments = Array.isArray(data.list) ? data.list : [];
        this.documentPage.total = Number(data.total) || 0;
        this.documentPage.pageIndex = Number(data.pageIndex) || this.documentPage.pageIndex;
        this.documentPage.pageSize = Number(data.pageSize) || this.documentPage.pageSize;
      } catch (error) {
        this.sourceDocuments = [];
        this.documentPage.total = 0;
        this.$message.error(this.errorMessage(error, "来源文档加载失败"));
      } finally {
        this.documentLoading = false;
      }
    },
    reloadDocuments() {
      this.documentPage.pageIndex = 1;
      this.loadDocuments();
    },
    resetDocumentFilters() {
      this.documentFilters = {
        yearMonth: "",
        version: "",
        provinceCode: "",
        sourceType: "",
        status: "",
        batchNo: "",
        sourceName: "",
      };
      this.reloadDocuments();
    },
    handleDocumentSizeChange(size) {
      this.documentPage.pageSize = size;
      this.documentPage.pageIndex = 1;
      this.loadDocuments();
    },
    openDocumentDialog(mode, row) {
      this.documentDialog.mode = mode;
      this.documentForm = mode === "edit" && row
        ? { ...emptyDocumentForm(), ...row, sourceConfigId: row.sourceConfigId || "" }
        : emptyDocumentForm();
      this.documentDialog.visible = true;
      this.$nextTick(() => {
        if (this.$refs.documentForm) {
          this.$refs.documentForm.clearValidate();
        }
      });
    },
    handleDocumentConfigChange(configId) {
      if (!configId) {
        return;
      }
      const config = this.sourceConfigs.find(item => item.id === configId);
      if (!config) {
        return;
      }
      this.documentForm.provinceCode = config.provinceCode || "";
      this.documentForm.provinceName = config.provinceName || "";
      this.documentForm.sourceType = config.sourceType || "";
      this.documentForm.sourceName = config.sourceName || "";
      this.documentForm.sourceUrl = config.sourceUrl || "";
    },
    submitDocument() {
      this.$refs.documentForm.validate(async valid => {
        if (!valid) {
          return;
        }
        this.documentDialog.saving = true;
        try {
          const payload = { ...this.documentForm };
          payload.sourceConfigId = payload.sourceConfigId || null;
          payload.sourceType = payload.sourceType ? String(payload.sourceType).toUpperCase() : "";
          if (this.documentDialog.mode === "edit") {
            await updateSourceDocument(payload.id, payload);
          } else {
            await createSourceDocument(payload);
          }
          this.$message.success("来源文档已保存");
          this.documentDialog.visible = false;
          await this.loadDocuments();
        } catch (error) {
          this.$message.error(this.errorMessage(error, "来源文档保存失败"));
        } finally {
          this.documentDialog.saving = false;
        }
      });
    },
    async setDocumentStatus(row, status) {
      const label = this.statusLabel(status);
      try {
        await this.$confirm(`确认将该来源文档标记为${label}？`, "确认操作", {
          type: "warning",
        });
        await updateSourceDocumentStatus(row.id, status);
        this.$message.success("来源文档状态已更新");
        this.loadDocuments();
      } catch (error) {
        if (error !== "cancel" && error !== "close") {
          this.$message.error(this.errorMessage(error, "来源文档状态更新失败"));
        }
      }
    },
    async archiveDocument(row) {
      try {
        await this.$confirm("确认归档该来源文档？", "确认操作", {
          type: "warning",
        });
        await archiveSourceDocument(row.id);
        this.$message.success("来源文档已归档");
        this.loadDocuments();
      } catch (error) {
        if (error !== "cancel" && error !== "close") {
          this.$message.error(this.errorMessage(error, "来源文档归档失败"));
        }
      }
    },
    sourceTypeLabel(value) {
      const item = SOURCE_TYPES.find(option => option.value === value);
      return item ? item.label : (value || "-");
    },
    statusLabel(value) {
      const item = STATUS_OPTIONS.find(option => option.value === value);
      return item ? item.label : (value || "-");
    },
    statusTagType(value) {
      const item = STATUS_OPTIONS.find(option => option.value === value);
      return item ? item.type : "info";
    },
    provinceText(row) {
      if (!row) {
        return "-";
      }
      if (row.provinceName && row.provinceCode) {
        return `${row.provinceName} (${row.provinceCode})`;
      }
      return row.provinceName || row.provinceCode || "-";
    },
    effectiveText(row) {
      const start = row && row.effectiveStart ? row.effectiveStart : "";
      const end = row && row.effectiveEnd ? row.effectiveEnd : "";
      if (start && end) {
        return `${start} 至 ${end}`;
      }
      return start || end || "-";
    },
    configOptionLabel(item) {
      if (!item) {
        return "";
      }
      const province = item.provinceName || item.provinceCode || "-";
      return `${province} / ${this.sourceTypeLabel(item.sourceType)} / ${item.sourceName || "-"}`;
    },
  },
};
</script>

<style scoped lang="less">
.tariff-sources-page {
  min-height: 100%;
  color: #1f2933;
}

.sources-shell {
  min-height: 100%;
  padding: 24px;
  border: 1px solid #dde6ed;
  border-radius: 8px;
  background: #ffffff;
}

.source-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.source-tabs button {
  min-width: 96px;
  height: 34px;
  border: 1px solid #cfdce5;
  border-radius: 6px;
  background: #f7fbff;
  color: #334e5c;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.source-tabs button.active,
.source-tabs button:hover {
  border-color: #0780ed;
  background: #0780ed;
  color: #ffffff;
}

.tab-panel {
  min-width: 0;
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.content-header h3 {
  margin: 0;
  color: #0e2638;
  font-size: 18px;
  font-weight: 700;
}

.filter-panel {
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid #dde6ed;
  border-radius: 6px;
  background: #f8fafc;
}

.filter-panel ::v-deep .el-form-item {
  margin-bottom: 0;
}

.filter-panel ::v-deep .el-input,
.filter-panel ::v-deep .el-select,
.filter-panel ::v-deep .el-date-editor.el-input {
  width: 190px;
}

.document-filter ::v-deep .el-form-item {
  margin-bottom: 10px;
}

.data-table ::v-deep .el-table {
  border: 1px solid #dde6ed;
}

.data-table ::v-deep .el-table th {
  background: #f5f7fa;
  color: #0e2638;
  font-weight: 600;
}

.sub-text {
  display: block;
  margin-top: 2px;
  color: #718096;
  font-size: 12px;
}

.table-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.source-form {
  max-height: 64vh;
  overflow: auto;
  padding-right: 8px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 16px;
}

.form-grid.three-cols {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.full-row {
  grid-column: 1 / -1;
}

.span-two {
  grid-column: span 2;
}

.source-form ::v-deep .el-select,
.source-form ::v-deep .el-input,
.source-form ::v-deep .el-date-editor.el-input {
  width: 100%;
}

::v-deep .el-dialog {
  border-radius: 8px;
}

@media (max-width: 1100px) {
  .form-grid.three-cols {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .sources-shell {
    padding: 16px;
  }

  .content-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }

  .filter-panel ::v-deep .el-form-item {
    display: block;
    margin-bottom: 12px;
  }

  .filter-panel ::v-deep .el-input,
  .filter-panel ::v-deep .el-select,
  .filter-panel ::v-deep .el-date-editor.el-input {
    width: 100%;
  }

  .form-grid,
  .form-grid.three-cols {
    grid-template-columns: 1fr;
  }

  .span-two {
    grid-column: 1 / -1;
  }
}
</style>
