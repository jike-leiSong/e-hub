<template>
  <div class="tenant-center">
    <section class="tenant-hero">
      <div class="hero-main">
        <p class="hero-kicker">TENANT CENTER</p>
        <h2>统一管理租户主体、产品订阅和平台运营归属</h2>
        <p class="hero-copy">
          当前版本已接入租户主数据、产品订阅、租户状态和租户下账号概览，后续继续扩展数据范围和 API 凭证治理。
        </p>
      </div>
      <div class="hero-badges">
        <span>租户主数据</span>
        <span>产品订阅</span>
        <span>账号归属</span>
      </div>
    </section>

    <section class="summary-grid">
      <article v-for="item in summaryCards" :key="item.label" class="summary-card">
        <p>{{ item.label }}</p>
        <strong>{{ item.value }}</strong>
        <span>{{ item.desc }}</span>
      </article>
    </section>

    <section class="toolbar-panel">
      <div class="toolbar-filters">
        <el-input
          v-model.trim="filters.keyword"
          class="filter-keyword"
          size="small"
          clearable
          placeholder="租户ID / 租户名称 / 管理员"
          prefix-icon="el-icon-search"
          @keyup.enter.native="loadTenants"
          @clear="loadTenants"
        />
        <el-select
          v-model="filters.tenantType"
          class="filter-select"
          size="small"
          clearable
          placeholder="租户类型"
        >
          <el-option
            v-for="item in tenantTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-select
          v-model="filters.status"
          class="filter-select filter-status"
          size="small"
          clearable
          placeholder="状态"
        >
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
        <el-select
          v-model="filters.productCode"
          class="filter-select"
          size="small"
          clearable
          placeholder="产品订阅"
        >
          <el-option
            v-for="item in productOptions"
            :key="item.code"
            :label="item.name"
            :value="item.code"
          />
        </el-select>
        <el-button
          class="toolbar-query-btn"
          size="small"
          type="primary"
          icon="el-icon-search"
          @click="loadTenants"
        >
          查询
        </el-button>
        <el-button class="toolbar-reset-btn" size="small" icon="el-icon-refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
      <div class="toolbar-actions">
        <el-button class="toolbar-action-btn" size="small" icon="el-icon-refresh-right" @click="reload">
          刷新
        </el-button>
        <el-button
          class="toolbar-create-btn"
          size="small"
          type="primary"
          icon="el-icon-plus"
          @click="openCreateDialog"
        >
          新增租户
        </el-button>
      </div>
    </section>

    <section class="table-panel">
      <el-table
        v-loading="loading"
        :data="tenants"
        border
        stripe
        size="small"
        height="calc(100vh - 420px)"
      >
        <el-table-column prop="tenantId" label="租户ID" min-width="160" show-overflow-tooltip />
        <el-table-column prop="tenantName" label="租户名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="租户类型" min-width="110">
          <template slot-scope="scope">
            {{ tenantTypeLabel(scope.row.tenantType) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="mini">
              {{ scope.row.status === 1 ? "启用" : "停用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerDisplayName" label="管理员" min-width="120" show-overflow-tooltip />
        <el-table-column prop="aggregatorId" label="聚合商ID" min-width="150" show-overflow-tooltip />
        <el-table-column prop="entId" label="企业ID" min-width="150" show-overflow-tooltip />
        <el-table-column label="产品订阅" min-width="220">
          <template slot-scope="scope">
            <div class="product-tags">
              <el-tag
                v-for="code in scope.row.productCodes || []"
                :key="code"
                size="mini"
                effect="plain"
              >
                {{ productName(code) }}
              </el-tag>
              <span v-if="!(scope.row.productCodes || []).length" class="muted">未开通</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="260" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="openDetailDialog(scope.row)">
              详情
            </el-button>
            <el-button type="text" size="mini" @click="openEditDialog(scope.row)">
              编辑
            </el-button>
            <el-button
              type="text"
              size="mini"
              :loading="statusLoadingTenantId === scope.row.tenantId"
              @click="toggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? "停用" : "启用" }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="pagination.total"
          :current-page.sync="pagination.pageIndex"
          :page-size.sync="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="loadTenants"
          @size-change="handlePageSizeChange"
        />
      </div>
    </section>

    <el-dialog
      :title="detailMode === 'create' ? '新增租户' : detailReadOnly ? '租户详情' : '编辑租户'"
      :visible.sync="detailDialogVisible"
      width="960px"
      :close-on-click-modal="false"
    >
      <div v-loading="detailLoading" class="detail-layout">
        <section class="detail-section">
          <div class="section-title">
            <p>基础信息</p>
            <span>维护租户主体、归属和联系人</span>
          </div>
          <el-form
            ref="tenantForm"
            :model="detailForm"
            :rules="detailRules"
            label-width="100px"
            size="small"
            :disabled="detailReadOnly"
          >
            <div class="form-grid">
              <el-form-item label="租户名称" prop="tenantName">
                <el-input v-model.trim="detailForm.tenantName" />
              </el-form-item>
              <el-form-item label="租户类型" prop="tenantType">
                <el-select v-model="detailForm.tenantType" placeholder="请选择">
                  <el-option
                    v-for="item in tenantTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="聚合商ID">
                <el-input v-model.trim="detailForm.aggregatorId" />
              </el-form-item>
              <el-form-item label="企业ID">
                <el-input v-model.trim="detailForm.entId" />
              </el-form-item>
              <el-form-item label="管理员ID">
                <el-input v-model.trim="detailForm.ownerUserId" />
              </el-form-item>
              <el-form-item label="联系人">
                <el-input v-model.trim="detailForm.contactName" />
              </el-form-item>
              <el-form-item label="联系电话">
                <el-input v-model.trim="detailForm.contactPhone" />
              </el-form-item>
              <el-form-item label="状态">
                <el-tag :type="detailForm.status === 1 ? 'success' : 'info'" size="small">
                  {{ detailForm.status === 1 ? "启用" : "停用" }}
                </el-tag>
              </el-form-item>
              <el-form-item label="备注" class="full">
                <el-input v-model.trim="detailForm.remark" type="textarea" :rows="3" />
              </el-form-item>
            </div>
          </el-form>
        </section>

        <section class="detail-section">
          <div class="section-title">
            <p>产品订阅</p>
            <span>保存时会同步写入租户产品订阅关系</span>
          </div>
          <el-checkbox-group
            v-model="selectedProducts"
            class="product-checks"
            :disabled="detailReadOnly"
          >
            <el-checkbox
              v-for="item in productOptions"
              :key="item.code"
              :label="item.code"
            >
              {{ item.name }}
            </el-checkbox>
          </el-checkbox-group>
        </section>

        <section class="detail-section">
          <div class="section-title">
            <p>租户账号</p>
            <span>租户下已绑定的平台账号概览</span>
          </div>
          <el-table
            :data="detailUsers"
            border
            stripe
            size="mini"
            max-height="260"
          >
            <el-table-column prop="username" label="账号" min-width="120" />
            <el-table-column prop="displayName" label="姓名" min-width="120" />
            <el-table-column prop="userType" label="用户类型" min-width="100" />
            <el-table-column label="角色" min-width="160">
              <template slot-scope="scope">
                {{ (scope.row.roleNames || []).join("、") || "-" }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="mini">
                  {{ scope.row.status === 1 ? "启用" : "停用" }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!detailUsers.length" class="muted empty-users">当前租户下暂无绑定账号</div>
        </section>
      </div>

      <span slot="footer">
        <el-button size="small" @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="!detailReadOnly"
          size="small"
          type="primary"
          :loading="detailSaving"
          @click="saveTenant"
        >
          保存
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  createTenant,
  fetchProductOptions,
  fetchTenantDetail,
  fetchTenantPage,
  saveTenantProducts,
  updateTenant,
  updateTenantStatus,
} from "@/modules/platform/api"

const EMPTY_DETAIL_FORM = () => ({
  tenantId: "",
  tenantName: "",
  tenantType: "ACCOUNT",
  aggregatorId: "",
  entId: "",
  ownerUserId: "",
  ownerDisplayName: "",
  contactName: "",
  contactPhone: "",
  remark: "",
  status: 1,
})

export default {
  name: "TenantCenter",
  data() {
    return {
      loading: false,
      detailLoading: false,
      detailSaving: false,
      statusLoadingTenantId: "",
      detailDialogVisible: false,
      detailMode: "detail",
      detailReadOnly: true,
      filters: {
        keyword: "",
        tenantType: "",
        status: null,
        productCode: "",
      },
      pagination: {
        pageIndex: 1,
        pageSize: 20,
        total: 0,
      },
      productOptions: [],
      tenants: [],
      detailForm: EMPTY_DETAIL_FORM(),
      selectedProducts: [],
      detailUsers: [],
      tenantTypeOptions: [
        { label: "平台租户", value: "PLATFORM" },
        { label: "聚合商租户", value: "AGGREGATOR" },
        { label: "企业租户", value: "ENT" },
        { label: "账号租户", value: "ACCOUNT" },
      ],
      detailRules: {
        tenantName: [{ required: true, message: "请输入租户名称", trigger: "blur" }],
        tenantType: [{ required: true, message: "请选择租户类型", trigger: "change" }],
      },
    }
  },
  computed: {
    activeTenantCount() {
      return this.tenants.filter(item => item.status === 1).length
    },
    subscribedTenantCount() {
      return this.tenants.filter(item => (item.productCodes || []).length > 0).length
    },
    summaryCards() {
      return [
        {
          label: "租户总数",
          value: String(this.pagination.total || 0),
          desc: "当前筛选条件下的租户主体总数",
        },
        {
          label: "当前页启用租户",
          value: String(this.activeTenantCount),
          desc: "当前列表中状态为启用的租户数量",
        },
        {
          label: "当前页已订阅租户",
          value: String(this.subscribedTenantCount),
          desc: "至少开通一项产品能力的租户数量",
        },
        {
          label: "产品能力类型",
          value: String(this.productOptions.length),
          desc: "当前平台定义的可订阅产品类型",
        },
      ]
    },
  },
  mounted() {
    this.reload()
  },
  methods: {
    reload() {
      this.loadProductOptions()
      this.loadTenants()
    },
    loadProductOptions() {
      fetchProductOptions()
        .then(data => {
          this.productOptions = Array.isArray(data) ? data : []
        })
        .catch(error => {
          this.$message.error(error.message || "产品能力加载失败")
        })
    },
    loadTenants() {
      this.loading = true
      fetchTenantPage({
        ...this.filters,
        pageIndex: this.pagination.pageIndex,
        pageSize: this.pagination.pageSize,
      })
        .then(data => {
          this.tenants = Array.isArray(data.list) ? data.list : []
          this.pagination.total = data.total || 0
          this.pagination.pageIndex = data.pageIndex || this.pagination.pageIndex
          this.pagination.pageSize = data.pageSize || this.pagination.pageSize
        })
        .catch(error => {
          this.$message.error(error.message || "租户列表加载失败")
        })
        .finally(() => {
          this.loading = false
        })
    },
    resetFilters() {
      this.filters = {
        keyword: "",
        tenantType: "",
        status: null,
        productCode: "",
      }
      this.pagination.pageIndex = 1
      this.loadTenants()
    },
    handlePageSizeChange() {
      this.pagination.pageIndex = 1
      this.loadTenants()
    },
    productName(code) {
      const target = this.productOptions.find(item => item.code === code)
      return target ? target.name : code
    },
    tenantTypeLabel(value) {
      const target = this.tenantTypeOptions.find(item => item.value === value)
      return target ? target.label : value || "-"
    },
    openCreateDialog() {
      this.detailMode = "create"
      this.detailReadOnly = false
      this.detailForm = EMPTY_DETAIL_FORM()
      this.selectedProducts = []
      this.detailUsers = []
      this.detailDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.tenantForm) {
          this.$refs.tenantForm.clearValidate()
        }
      })
    },
    openEditDialog(row) {
      this.detailMode = "edit"
      this.detailReadOnly = false
      this.openTenantDetail(row)
    },
    openDetailDialog(row) {
      this.detailMode = "detail"
      this.detailReadOnly = true
      this.openTenantDetail(row)
    },
    openTenantDetail(row) {
      this.detailDialogVisible = true
      this.detailLoading = true
      fetchTenantDetail(row.tenantId)
        .then(data => {
          this.fillDetail(data)
        })
        .catch(error => {
          this.$message.error(error.message || "租户详情加载失败")
          this.detailDialogVisible = false
        })
        .finally(() => {
          this.detailLoading = false
        })
    },
    fillDetail(data) {
      this.detailForm = {
        tenantId: data.tenantId || "",
        tenantName: data.tenantName || "",
        tenantType: data.tenantType || "ACCOUNT",
        aggregatorId: data.aggregatorId || "",
        entId: data.entId || "",
        ownerUserId: data.ownerUserId || "",
        ownerDisplayName: data.ownerDisplayName || "",
        contactName: data.contactName || "",
        contactPhone: data.contactPhone || "",
        remark: data.remark || "",
        status: data.status == null ? 1 : data.status,
      }
      this.selectedProducts = Array.isArray(data.products)
        ? data.products
          .filter(item => item && item.enabled === 1)
          .map(item => item.productCode)
        : []
      this.detailUsers = Array.isArray(data.users) ? data.users : []
    },
    saveTenant() {
      this.$refs.tenantForm.validate(valid => {
        if (!valid) {
          return
        }
        this.detailSaving = true
        const payload = {
          tenantName: this.detailForm.tenantName,
          tenantType: this.detailForm.tenantType,
          aggregatorId: this.detailForm.aggregatorId,
          entId: this.detailForm.entId,
          ownerUserId: this.detailForm.ownerUserId,
          contactName: this.detailForm.contactName,
          contactPhone: this.detailForm.contactPhone,
          remark: this.detailForm.remark,
        }
        const request = this.detailMode === "create"
          ? createTenant(payload)
          : updateTenant(this.detailForm.tenantId, payload)
        request
          .then(data => {
            const tenantId = data.tenantId || this.detailForm.tenantId
            return saveTenantProducts(
              tenantId,
              this.selectedProducts.map(code => ({
                productCode: code,
                enabled: 1,
              }))
            )
              .then(() => fetchTenantDetail(tenantId))
              .then(detail => {
                this.fillDetail(detail)
                this.detailMode = "edit"
                this.$message.success("租户保存成功")
                this.loadTenants()
              })
          })
          .catch(error => {
            this.$message.error(error.message || "租户保存失败")
          })
          .finally(() => {
            this.detailSaving = false
          })
      })
    },
    toggleStatus(row) {
      const targetStatus = row.status === 1 ? 0 : 1
      this.statusLoadingTenantId = row.tenantId
      updateTenantStatus(row.tenantId, targetStatus)
        .then(() => {
          this.$message.success(targetStatus === 1 ? "租户已启用" : "租户已停用")
          this.loadTenants()
        })
        .catch(error => {
          this.$message.error(error.message || "租户状态更新失败")
        })
        .finally(() => {
          this.statusLoadingTenantId = ""
        })
    },
  },
}
</script>

<style lang="less" scoped>
.tenant-center {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.tenant-hero {
  min-height: 168px;
  padding: 24px;
  border-radius: 10px;
  background:
    linear-gradient(135deg, rgba(5, 51, 82, 0.96), rgba(7, 128, 237, 0.9)),
    #0e2638;
  color: #ffffff;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
}

.hero-main,
.hero-main p,
.hero-main h2,
.hero-badges span,
.summary-card p,
.summary-card strong,
.summary-card span,
.section-title p,
.section-title span {
  margin: 0;
}

.hero-kicker {
  color: #8bf3e4;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.hero-main h2 {
  margin-top: 10px;
  max-width: 680px;
  font-size: 30px;
  line-height: 1.25;
}

.hero-copy {
  margin-top: 12px;
  max-width: 760px;
  color: #d6e7f2;
  font-size: 14px;
  line-height: 1.7;
}

.hero-badges {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.hero-badges span {
  display: inline-flex;
  height: 30px;
  align-items: center;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.18);
  color: #ffffff;
  font-size: 12px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.summary-card {
  min-height: 118px;
  padding: 18px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #dde6ed;
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

.toolbar-panel,
.table-panel,
.detail-section {
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #dde6ed;
}

.toolbar-panel {
  padding: 14px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.toolbar-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  flex: 1;
}

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

.toolbar-query-btn,
.toolbar-reset-btn,
.toolbar-action-btn {
  min-width: 88px;
}

.toolbar-create-btn {
  min-width: 112px;
}

.table-panel {
  padding: 14px;
}

.pager {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.muted {
  color: #8a9dac;
  font-size: 12px;
}

.detail-layout {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.detail-section {
  padding: 16px;
}

.section-title {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.section-title p {
  color: #0e2638;
  font-size: 18px;
  font-weight: 700;
}

.section-title span {
  color: #607d8f;
  font-size: 12px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.form-grid .full {
  grid-column: 1 / -1;
}

.product-checks {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.empty-users {
  margin-top: 12px;
}

@media (max-width: 1280px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .toolbar-panel {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-actions {
    justify-content: flex-end;
  }
}

@media (max-width: 960px) {
  .tenant-hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-badges {
    justify-content: flex-start;
  }

  .summary-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }

  .toolbar-filters /deep/ .filter-keyword,
  .toolbar-filters /deep/ .filter-select,
  .toolbar-filters /deep/ .filter-status {
    width: 100%;
  }
}
</style>
