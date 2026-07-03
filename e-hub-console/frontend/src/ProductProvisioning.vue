<template>
  <div class="tenant-center">
    <section class="tenant-hero">
      <div class="hero-main">
        <p class="hero-kicker">TENANT CENTER</p>
        <h2>统一管理租户主体、产品订阅和后续数据范围能力</h2>
        <p class="hero-copy">
          当前版本先承接租户产品订阅，后续在同一中心扩展租户档案、合作状态、数据范围和 API 凭证管理。
        </p>
      </div>
      <div class="hero-badges">
        <span>已落地：产品订阅</span>
        <span>规划中：租户档案</span>
        <span>规划中：数据范围</span>
      </div>
    </section>

    <section class="summary-grid">
      <article v-for="item in summaryCards" :key="item.label" class="summary-card">
        <p>{{ item.label }}</p>
        <strong>{{ item.value }}</strong>
        <span>{{ item.desc }}</span>
      </article>
    </section>

    <section class="provision-toolbar">
      <div class="toolbar-left">
        <div class="search-group">
          <el-input
            v-model.trim="keyword"
            size="small"
            clearable
            placeholder="租户账号、名称、ID"
            prefix-icon="el-icon-search"
            @keyup.enter.native="loadCustomers"
            @clear="loadCustomers"
          />
          <el-button size="small" type="primary" icon="el-icon-search" @click="loadCustomers">
            查询
          </el-button>
        </div>
        <p class="toolbar-note">租户产品订阅已可维护；客户主体、合作状态、数据范围后续接入该中心。</p>
      </div>
      <el-button size="small" icon="el-icon-refresh" @click="reload">刷新</el-button>
    </section>

    <el-table
      v-loading="loading"
      class="customer-table"
      :data="customers"
      border
      stripe
      size="small"
      height="calc(100vh - 320px)"
    >
      <el-table-column prop="username" label="租户账号" min-width="130" show-overflow-tooltip />
      <el-table-column prop="displayName" label="租户名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="customerId" label="租户ID" min-width="150" show-overflow-tooltip />
      <el-table-column label="租户层级" min-width="110">
        <template slot-scope="scope">
          <span>{{ tenantLevelLabel(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="aggregatorId" label="聚合商ID" min-width="130" show-overflow-tooltip />
      <el-table-column prop="entId" label="企业ID" min-width="130" show-overflow-tooltip />
      <el-table-column label="产品订阅" min-width="260">
        <template slot-scope="scope">
          <el-checkbox-group v-model="scope.row.products" class="product-checks">
            <el-checkbox
              v-for="product in productOptions"
              :key="product.code"
              :label="product.code"
            >
              {{ product.name }}
            </el-checkbox>
          </el-checkbox-group>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="110" fixed="right">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="primary"
            :loading="savingUserId === scope.row.userId"
            @click="saveCustomer(scope.row)"
          >
            保存
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import service from "@/services/http";

export default {
  name: "TenantCenter",
  data() {
    return {
      keyword: "",
      loading: false,
      savingUserId: "",
      productOptions: [],
      customers: [],
    };
  },
  computed: {
    subscribedTenantCount() {
      return this.customers.filter(item => Array.isArray(item.products) && item.products.length > 0).length;
    },
    summaryCards() {
      return [
        {
          label: "租户数量",
          value: String(this.customers.length),
          desc: "当前检索结果中的租户主体数量",
        },
        {
          label: "产品类型",
          value: String(this.productOptions.length),
          desc: "当前平台已定义的产品能力类型",
        },
        {
          label: "已订阅租户",
          value: String(this.subscribedTenantCount),
          desc: "至少开通一种产品能力的租户数量",
        },
        {
          label: "待接入能力",
          value: "3",
          desc: "租户档案、合作状态、数据范围",
        },
      ];
    },
  },
  mounted() {
    this.reload();
  },
  methods: {
    reload() {
      this.loadOptions();
      this.loadCustomers();
    },
    loadOptions() {
      service({
        method: "get",
        url: "/product/options",
      }).then(response => {
        const body = response.data || {};
        if (body.code === 200) {
          this.productOptions = body.data || [];
        } else {
          this.$message.error(body.msg || "产品列表加载失败");
        }
      });
    },
    loadCustomers() {
      this.loading = true;
      service({
        method: "get",
        url: "/product/customers",
        params: {
          keyword: this.keyword,
        },
      })
        .then(response => {
          const body = response.data || {};
          if (body.code === 200) {
            this.customers = (body.data || []).map(item => ({
              ...item,
              products: Array.isArray(item.products) ? item.products.slice() : [],
            }));
          } else {
            this.$message.error(body.msg || "客户列表加载失败");
          }
        })
        .finally(() => {
          this.loading = false;
        });
    },
    saveCustomer(row) {
      this.savingUserId = row.userId;
      service({
        method: "put",
        url: `/product/customers/${encodeURIComponent(row.userId)}/products`,
        data: {
          productCodes: row.products || [],
        },
      })
        .then(response => {
          const body = response.data || {};
          if (body.code === 200) {
            this.$message.success("保存成功");
            this.loadCustomers();
          } else {
            this.$message.error(body.msg || "保存失败");
          }
        })
        .finally(() => {
          this.savingUserId = "";
        });
    },
    tenantLevelLabel(row) {
      if (row && row.entId) {
        return "企业租户";
      }
      if (row && row.aggregatorId) {
        return "聚合商租户";
      }
      return "账号租户";
    },
  },
};
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
.toolbar-note {
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

.provision-toolbar {
  min-height: 74px;
  padding: 12px 14px;
  border: 1px solid #dde6ed;
  border-radius: 8px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.toolbar-left {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.search-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-note {
  color: #607d8f;
  font-size: 12px;
}

.search-group .el-input {
  width: 260px;
}

.customer-table {
  background: #ffffff;
}

.product-checks {
  display: flex;
  flex-wrap: wrap;
  gap: 0 18px;
}

@media (max-width: 1280px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 960px) {
  .tenant-hero,
  .provision-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-badges {
    justify-content: flex-start;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .search-group {
    width: 100%;
    flex-wrap: wrap;
  }

  .search-group .el-input {
    width: 100%;
  }
}
</style>
