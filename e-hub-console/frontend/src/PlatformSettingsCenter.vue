<template>
  <div class="platform-settings-center">
    <section class="center-hero">
      <div>
        <p class="hero-kicker">PLATFORM SETTINGS</p>
        <h2>统一治理平台参数、字典中心与操作审计</h2>
        <p class="hero-copy">
          当前页面已接入平台配置项分页、新增更新、字典类型与字典项浏览，以及平台治理操作日志查询。
        </p>
      </div>
      <div class="hero-tags">
        <span>平台参数</span>
        <span>字典中心</span>
        <span>操作审计</span>
      </div>
    </section>

    <section class="summary-grid">
      <article v-for="item in summaryCards" :key="item.label" class="summary-card">
        <p>{{ item.label }}</p>
        <strong>{{ item.value }}</strong>
        <span>{{ item.desc }}</span>
      </article>
    </section>

    <section class="board">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="配置项治理" name="configs">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input
                v-model.trim="configFilters.keyword"
                class="filter-keyword"
                size="small"
                clearable
                placeholder="配置键 / 配置名称"
                prefix-icon="el-icon-search"
                @keyup.enter.native="loadConfigs"
                @clear="loadConfigs"
              />
              <el-select
                v-model="configFilters.configGroup"
                class="filter-select"
                size="small"
                clearable
                filterable
                placeholder="配置分组"
              >
                <el-option
                  v-for="item in configGroupOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
              <el-select
                v-model="configFilters.status"
                class="filter-select filter-status"
                size="small"
                clearable
                placeholder="状态"
              >
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
              <el-button
                class="toolbar-query-btn"
                size="small"
                type="primary"
                icon="el-icon-search"
                @click="loadConfigs"
              >
                查询
              </el-button>
              <el-button class="toolbar-reset-btn" size="small" @click="resetConfigFilters">重置</el-button>
            </div>
            <div class="toolbar-actions">
              <el-button class="toolbar-action-btn" size="small" icon="el-icon-refresh-right" @click="loadConfigs">
                刷新
              </el-button>
              <el-button
                class="toolbar-create-btn"
                size="small"
                type="primary"
                icon="el-icon-plus"
                @click="openConfigDialog()"
              >
                新增配置项
              </el-button>
            </div>
          </div>

          <el-table
            v-loading="configLoading"
            :data="configs"
            border
            stripe
            size="small"
            height="calc(100vh - 500px)"
          >
            <el-table-column prop="configKey" label="配置键" min-width="180" show-overflow-tooltip />
            <el-table-column prop="configName" label="配置名称" min-width="160" show-overflow-tooltip />
            <el-table-column prop="configGroup" label="配置分组" min-width="140" show-overflow-tooltip />
            <el-table-column label="值类型" width="100" align="center">
              <template slot-scope="scope">
                <el-tag size="mini" effect="plain">{{ scope.row.valueType || "STRING" }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="configValue" label="配置值" min-width="220" show-overflow-tooltip />
            <el-table-column label="状态" width="90" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="mini">
                  {{ scope.row.status === 1 ? "启用" : "停用" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
            <el-table-column prop="updateTime" label="更新时间" min-width="160" show-overflow-tooltip />
            <el-table-column label="操作" width="100" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="mini" @click="openConfigDialog(scope.row)">
                  编辑
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pager">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :total="configPagination.total"
              :current-page.sync="configPagination.pageIndex"
              :page-size.sync="configPagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              @current-change="loadConfigs"
              @size-change="handleConfigPageSizeChange"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="字典中心" name="dicts">
          <div class="dict-layout">
            <aside class="dict-side">
              <div class="dict-side-head">
                <div>
                  <p>字典类型</p>
                  <span>当前能力仅支持浏览</span>
                </div>
                <el-button
                  size="mini"
                  icon="el-icon-refresh-right"
                  :loading="dictLoading"
                  @click="loadDictTypes"
                >
                  刷新
                </el-button>
              </div>
              <el-input
                v-model.trim="dictKeyword"
                size="small"
                clearable
                placeholder="搜索字典类型"
                prefix-icon="el-icon-search"
              />
              <div v-loading="dictLoading" class="dict-type-list">
                <button
                  v-for="item in filteredDictTypes"
                  :key="item.dictType"
                  type="button"
                  class="dict-type-item"
                  :class="{ active: activeDictType === item.dictType }"
                  @click="selectDictType(item.dictType)"
                >
                  <div>
                    <p>{{ item.dictName || item.dictType }}</p>
                    <span>{{ item.dictType }}</span>
                  </div>
                  <el-tag :type="item.status === 1 ? 'success' : 'info'" size="mini">
                    {{ item.status === 1 ? "启用" : "停用" }}
                  </el-tag>
                </button>
                <div v-if="!filteredDictTypes.length" class="empty-block">暂无字典类型</div>
              </div>
            </aside>

            <section class="dict-main">
              <div class="section-head">
                <div>
                  <p class="section-kicker">字典项</p>
                  <h3>{{ activeDictTypeLabel }}</h3>
                </div>
                <span>{{ dictItems.length }} 项</span>
              </div>

              <div v-if="activeDictMeta && activeDictMeta.remark" class="dict-remark">
                {{ activeDictMeta.remark }}
              </div>

              <el-table
                v-loading="dictItemsLoading"
                :data="dictItems"
                border
                stripe
                size="small"
                height="calc(100vh - 510px)"
              >
                <el-table-column prop="itemCode" label="编码" min-width="160" show-overflow-tooltip />
                <el-table-column prop="itemName" label="名称" min-width="160" show-overflow-tooltip />
                <el-table-column prop="itemValue" label="值" min-width="180" show-overflow-tooltip />
                <el-table-column prop="sortNo" label="排序" width="90" align="center" />
                <el-table-column label="状态" width="90" align="center">
                  <template slot-scope="scope">
                    <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="mini">
                      {{ scope.row.status === 1 ? "启用" : "停用" }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="extJson" label="扩展信息" min-width="260" show-overflow-tooltip />
              </el-table>
            </section>
          </div>
        </el-tab-pane>

        <el-tab-pane label="操作审计" name="audit">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input
                v-model.trim="auditFilters.bizType"
                class="filter-keyword"
                size="small"
                clearable
                placeholder="业务类型"
                prefix-icon="el-icon-search"
                @keyup.enter.native="loadAuditLogs"
                @clear="loadAuditLogs"
              />
              <el-input
                v-model.trim="auditFilters.operatorUserId"
                class="filter-select"
                size="small"
                clearable
                placeholder="操作人ID"
                @keyup.enter.native="loadAuditLogs"
                @clear="loadAuditLogs"
              />
              <el-date-picker
                v-model="auditFilters.timeRange"
                class="filter-date"
                size="small"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                value-format="yyyy-MM-dd HH:mm:ss"
              />
              <el-button
                class="toolbar-query-btn"
                size="small"
                type="primary"
                icon="el-icon-search"
                @click="loadAuditLogs"
              >
                查询
              </el-button>
              <el-button class="toolbar-reset-btn" size="small" @click="resetAuditFilters">重置</el-button>
            </div>
            <div class="toolbar-actions">
              <el-button class="toolbar-action-btn" size="small" icon="el-icon-refresh-right" @click="loadAuditLogs">
                刷新
              </el-button>
            </div>
          </div>

          <el-table
            v-loading="auditLoading"
            :data="auditLogs"
            border
            stripe
            size="small"
            height="calc(100vh - 500px)"
          >
            <el-table-column prop="bizType" label="业务类型" min-width="120" show-overflow-tooltip />
            <el-table-column prop="bizId" label="业务标识" min-width="160" show-overflow-tooltip />
            <el-table-column prop="action" label="操作" min-width="120" show-overflow-tooltip />
            <el-table-column prop="operatorName" label="操作人" min-width="120" show-overflow-tooltip />
            <el-table-column prop="operatorUserId" label="操作人ID" min-width="150" show-overflow-tooltip />
            <el-table-column prop="requestPath" label="请求路径" min-width="220" show-overflow-tooltip />
            <el-table-column label="结果" width="90" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.result === 'SUCCESS' ? 'success' : 'danger'" size="mini">
                  {{ scope.row.result || "-" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="errorMsg" label="错误信息" min-width="200" show-overflow-tooltip />
            <el-table-column prop="createTime" label="时间" min-width="160" show-overflow-tooltip />
          </el-table>

          <div class="pager">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :total="auditPagination.total"
              :current-page.sync="auditPagination.pageIndex"
              :page-size.sync="auditPagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              @current-change="loadAuditLogs"
              @size-change="handleAuditPageSizeChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>

    <el-dialog
      :title="editingConfigId ? '编辑配置项' : '新增配置项'"
      :visible.sync="configDialogVisible"
      width="760px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="configForm"
        :model="configForm"
        :rules="configRules"
        label-width="100px"
        size="small"
      >
        <div class="form-grid">
          <el-form-item label="配置键" prop="configKey">
            <el-input v-model.trim="configForm.configKey" />
          </el-form-item>
          <el-form-item label="配置名称" prop="configName">
            <el-input v-model.trim="configForm.configName" />
          </el-form-item>
          <el-form-item label="配置分组" prop="configGroup">
            <el-input v-model.trim="configForm.configGroup" />
          </el-form-item>
          <el-form-item label="值类型">
            <el-select v-model="configForm.valueType" placeholder="请选择">
              <el-option
                v-for="item in valueTypeOptions"
                :key="item"
                :label="item"
                :value="item"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="configForm.status" placeholder="请选择">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="配置值" class="full">
            <el-input v-model="configForm.configValue" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item label="备注" class="full">
            <el-input v-model.trim="configForm.remark" type="textarea" :rows="3" />
          </el-form-item>
        </div>
      </el-form>
      <span slot="footer">
        <el-button size="small" @click="configDialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="configSaving" @click="saveConfig">
          保存
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  createConfigItem,
  fetchAuditLogs,
  fetchConfigItems,
  fetchDictItems,
  fetchDictTypes,
  updateConfigItem,
} from "@/modules/platform/api"

const EMPTY_CONFIG_FORM = () => ({
  configKey: "",
  configName: "",
  configValue: "",
  configGroup: "",
  valueType: "STRING",
  status: 1,
  remark: "",
})

export default {
  name: "PlatformSettingsCenter",
  data() {
    return {
      activeTab: "configs",
      configLoading: false,
      configSaving: false,
      dictLoading: false,
      dictItemsLoading: false,
      auditLoading: false,
      configs: [],
      dictTypes: [],
      dictItems: [],
      auditLogs: [],
      editingConfigId: null,
      configDialogVisible: false,
      dictKeyword: "",
      activeDictType: "",
      configFilters: {
        keyword: "",
        configGroup: "",
        status: null,
      },
      configPagination: {
        pageIndex: 1,
        pageSize: 20,
        total: 0,
      },
      auditFilters: {
        bizType: "",
        operatorUserId: "",
        timeRange: [],
      },
      auditPagination: {
        pageIndex: 1,
        pageSize: 20,
        total: 0,
      },
      configForm: EMPTY_CONFIG_FORM(),
      valueTypeOptions: ["STRING", "NUMBER", "BOOLEAN", "JSON", "TEXT"],
      configRules: {
        configKey: [{ required: true, message: "请输入配置键", trigger: "blur" }],
        configName: [{ required: true, message: "请输入配置名称", trigger: "blur" }],
        configGroup: [{ required: true, message: "请输入配置分组", trigger: "blur" }],
      },
    }
  },
  computed: {
    configGroupOptions() {
      const map = {}
      this.configs.forEach(item => {
        if (item && item.configGroup) {
          map[item.configGroup] = true
        }
      })
      if (this.configFilters.configGroup) {
        map[this.configFilters.configGroup] = true
      }
      return Object.keys(map)
    },
    filteredDictTypes() {
      if (!this.dictKeyword) {
        return this.dictTypes
      }
      const keyword = this.dictKeyword.toLowerCase()
      return this.dictTypes.filter(item => {
        const dictType = (item.dictType || "").toLowerCase()
        const dictName = (item.dictName || "").toLowerCase()
        return dictType.includes(keyword) || dictName.includes(keyword)
      })
    },
    activeDictMeta() {
      return this.dictTypes.find(item => item.dictType === this.activeDictType) || null
    },
    activeDictTypeLabel() {
      const meta = this.activeDictMeta
      if (!meta) {
        return "请选择字典类型"
      }
      return meta.dictName ? `${meta.dictName} / ${meta.dictType}` : meta.dictType
    },
    summaryCards() {
      const activeConfigCount = this.configs.filter(item => item.status === 1).length
      const successAuditCount = this.auditLogs.filter(item => item.result === "SUCCESS").length
      return [
        {
          label: "配置项总数",
          value: String(this.configPagination.total || 0),
          desc: `当前页启用 ${activeConfigCount} 项`,
        },
        {
          label: "配置分组",
          value: String(this.configGroupOptions.length),
          desc: "按当前列表聚合出的配置分组数量",
        },
        {
          label: "字典类型",
          value: String(this.dictTypes.length),
          desc: `当前选中字典 ${this.dictItems.length} 项`,
        },
        {
          label: "审计记录",
          value: String(this.auditPagination.total || 0),
          desc: `当前页成功 ${successAuditCount} 条`,
        },
      ]
    },
  },
  mounted() {
    this.reload()
  },
  methods: {
    reload() {
      this.loadConfigs()
      this.loadDictTypes()
      this.loadAuditLogs()
    },
    loadConfigs() {
      this.configLoading = true
      fetchConfigItems({
        ...this.configFilters,
        pageIndex: this.configPagination.pageIndex,
        pageSize: this.configPagination.pageSize,
      })
        .then(data => {
          this.configs = Array.isArray(data.list) ? data.list : []
          this.configPagination.total = data.total || 0
          this.configPagination.pageIndex = data.pageIndex || this.configPagination.pageIndex
          this.configPagination.pageSize = data.pageSize || this.configPagination.pageSize
        })
        .catch(error => {
          this.$message.error(error.message || "配置项加载失败")
        })
        .finally(() => {
          this.configLoading = false
        })
    },
    loadDictTypes() {
      this.dictLoading = true
      fetchDictTypes()
        .then(data => {
          this.dictTypes = Array.isArray(data) ? data : []
          if (!this.dictTypes.length) {
            this.activeDictType = ""
            this.dictItems = []
            return
          }
          const exists = this.dictTypes.some(item => item.dictType === this.activeDictType)
          const targetDictType = exists ? this.activeDictType : this.dictTypes[0].dictType
          this.selectDictType(targetDictType)
        })
        .catch(error => {
          this.$message.error(error.message || "字典类型加载失败")
        })
        .finally(() => {
          this.dictLoading = false
        })
    },
    selectDictType(dictType) {
      if (!dictType) {
        this.activeDictType = ""
        this.dictItems = []
        return
      }
      this.activeDictType = dictType
      this.loadDictItems(dictType)
    },
    loadDictItems(dictType) {
      this.dictItemsLoading = true
      fetchDictItems(dictType)
        .then(data => {
          this.dictItems = Array.isArray(data) ? data : []
        })
        .catch(error => {
          this.$message.error(error.message || "字典项加载失败")
        })
        .finally(() => {
          this.dictItemsLoading = false
        })
    },
    loadAuditLogs() {
      this.auditLoading = true
      const timeRange = Array.isArray(this.auditFilters.timeRange) ? this.auditFilters.timeRange : []
      fetchAuditLogs({
        bizType: this.auditFilters.bizType,
        operatorUserId: this.auditFilters.operatorUserId,
        startTime: timeRange[0] || "",
        endTime: timeRange[1] || "",
        pageIndex: this.auditPagination.pageIndex,
        pageSize: this.auditPagination.pageSize,
      })
        .then(data => {
          this.auditLogs = Array.isArray(data.list) ? data.list : []
          this.auditPagination.total = data.total || 0
          this.auditPagination.pageIndex = data.pageIndex || this.auditPagination.pageIndex
          this.auditPagination.pageSize = data.pageSize || this.auditPagination.pageSize
        })
        .catch(error => {
          this.$message.error(error.message || "审计日志加载失败")
        })
        .finally(() => {
          this.auditLoading = false
        })
    },
    resetConfigFilters() {
      this.configFilters = {
        keyword: "",
        configGroup: "",
        status: null,
      }
      this.configPagination.pageIndex = 1
      this.loadConfigs()
    },
    resetAuditFilters() {
      this.auditFilters = {
        bizType: "",
        operatorUserId: "",
        timeRange: [],
      }
      this.auditPagination.pageIndex = 1
      this.loadAuditLogs()
    },
    handleConfigPageSizeChange() {
      this.configPagination.pageIndex = 1
      this.loadConfigs()
    },
    handleAuditPageSizeChange() {
      this.auditPagination.pageIndex = 1
      this.loadAuditLogs()
    },
    openConfigDialog(row) {
      this.editingConfigId = row && row.id ? row.id : null
      this.configForm = row
        ? {
          configKey: row.configKey || "",
          configName: row.configName || "",
          configValue: row.configValue || "",
          configGroup: row.configGroup || "",
          valueType: row.valueType || "STRING",
          status: row.status == null ? 1 : row.status,
          remark: row.remark || "",
        }
        : EMPTY_CONFIG_FORM()
      this.configDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.configForm) {
          this.$refs.configForm.clearValidate()
        }
      })
    },
    saveConfig() {
      this.$refs.configForm.validate(valid => {
        if (!valid) {
          return
        }
        this.configSaving = true
        const payload = { ...this.configForm }
        const request = this.editingConfigId
          ? updateConfigItem(this.editingConfigId, payload)
          : createConfigItem(payload)
        request
          .then(() => {
            this.$message.success("配置项保存成功")
            this.configDialogVisible = false
            this.configPagination.pageIndex = 1
            this.loadConfigs()
            this.loadAuditLogs()
          })
          .catch(error => {
            this.$message.error(error.message || "配置项保存失败")
          })
          .finally(() => {
            this.configSaving = false
          })
      })
    },
  },
}
</script>

<style lang="less" scoped>
.platform-settings-center {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.center-hero {
  min-height: 168px;
  padding: 24px;
  border-radius: 10px;
  background:
    linear-gradient(135deg, rgba(27, 73, 93, 0.96), rgba(10, 138, 105, 0.88)),
    #1c4c4f;
  color: #ffffff;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
}

.hero-kicker,
.hero-copy,
.center-hero h2,
.hero-tags span,
.summary-card p,
.summary-card strong,
.summary-card span,
.section-kicker,
.section-head h3,
.dict-side-head p,
.dict-side-head span,
.dict-type-item p,
.dict-type-item span {
  margin: 0;
}

.hero-kicker {
  color: #b7f5d7;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.center-hero h2 {
  margin-top: 10px;
  max-width: 680px;
  font-size: 30px;
  line-height: 1.25;
}

.hero-copy {
  margin-top: 12px;
  max-width: 760px;
  color: #d6f0e8;
  font-size: 14px;
  line-height: 1.7;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.hero-tags span {
  display: inline-flex;
  align-items: center;
  height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.18);
  font-size: 12px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.summary-card,
.board,
.dict-side,
.dict-main {
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #dde6ed;
}

.summary-card {
  min-height: 122px;
  padding: 18px;
}

.summary-card p {
  color: #607d8f;
  font-size: 13px;
}

.summary-card strong {
  display: block;
  margin-top: 12px;
  color: #0e2638;
  font-size: 24px;
  font-weight: 700;
}

.summary-card span {
  display: block;
  margin-top: 10px;
  color: #607d8f;
  font-size: 13px;
  line-height: 1.6;
}

.board {
  padding: 18px;
}

.toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.toolbar-filters,
.toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.toolbar-filters /deep/ .filter-keyword {
  width: 300px;
}

.toolbar-filters /deep/ .filter-select {
  width: 180px;
}

.toolbar-filters /deep/ .filter-status {
  width: 140px;
}

.toolbar-filters /deep/ .filter-date {
  width: 340px;
}

.toolbar-query-btn,
.toolbar-reset-btn,
.toolbar-action-btn {
  min-width: 88px;
}

.toolbar-create-btn {
  min-width: 112px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

.dict-layout {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 14px;
}

.dict-side,
.dict-main {
  padding: 16px;
}

.dict-side {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dict-side-head,
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.dict-side-head p,
.section-head h3 {
  color: #0e2638;
  font-size: 18px;
  font-weight: 700;
}

.dict-side-head span,
.section-kicker,
.section-head > span {
  color: #607d8f;
  font-size: 12px;
}

.dict-type-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 420px;
  max-height: calc(100vh - 430px);
  overflow-y: auto;
}

.dict-type-item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 14px;
  border-radius: 8px;
  border: 1px solid #dde6ed;
  background: #f7fafc;
  cursor: pointer;
  transition: all 0.2s ease;
}

.dict-type-item:hover,
.dict-type-item.active {
  border-color: #0b8a69;
  background: #edf8f4;
}

.dict-type-item p {
  color: #0e2638;
  font-size: 14px;
  font-weight: 700;
  text-align: left;
}

.dict-type-item span {
  display: block;
  margin-top: 6px;
  color: #607d8f;
  font-size: 12px;
  text-align: left;
}

.dict-main {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.dict-remark {
  padding: 12px 14px;
  border-radius: 8px;
  background: #f6faf8;
  border: 1px solid #d8ebe2;
  color: #607d8f;
  font-size: 13px;
  line-height: 1.6;
}

.empty-block {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  color: #90a4ae;
  font-size: 13px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.form-grid /deep/ .el-form-item {
  margin-bottom: 18px;
}

.full {
  grid-column: 1 / -1;
}

@media (max-width: 1360px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dict-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1080px) {
  .center-hero,
  .toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-tags {
    justify-content: flex-start;
  }

  .toolbar-filters /deep/ .filter-keyword,
  .toolbar-filters /deep/ .filter-select,
  .toolbar-filters /deep/ .filter-status,
  .toolbar-filters /deep/ .filter-date {
    width: 100%;
  }

  .toolbar-filters,
  .toolbar-actions {
    width: 100%;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .board,
  .dict-side,
  .dict-main {
    padding: 14px;
  }
}
</style>
