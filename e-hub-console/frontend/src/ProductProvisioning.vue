<template>
  <div class="product-provisioning">
    <section class="provision-toolbar">
      <div class="search-group">
        <el-input
          v-model.trim="keyword"
          size="small"
          clearable
          placeholder="客户账号、名称、ID"
          prefix-icon="el-icon-search"
          @keyup.enter.native="loadCustomers"
          @clear="loadCustomers"
        />
        <el-button size="small" type="primary" icon="el-icon-search" @click="loadCustomers">
          查询
        </el-button>
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
      height="calc(100vh - 190px)"
    >
      <el-table-column prop="username" label="登录账号" min-width="130" show-overflow-tooltip />
      <el-table-column prop="displayName" label="客户名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="customerId" label="客户ID" min-width="150" show-overflow-tooltip />
      <el-table-column prop="aggregatorId" label="聚合商ID" min-width="130" show-overflow-tooltip />
      <el-table-column prop="entId" label="企业ID" min-width="130" show-overflow-tooltip />
      <el-table-column label="产品开通" min-width="260">
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
  name: "ProductProvisioning",
  data() {
    return {
      keyword: "",
      loading: false,
      savingUserId: "",
      productOptions: [],
      customers: [],
    };
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
  },
};
</script>

<style lang="less" scoped>
.product-provisioning {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.provision-toolbar {
  min-height: 52px;
  padding: 10px 12px;
  border: 1px solid #dde6ed;
  border-radius: 8px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.search-group {
  display: flex;
  align-items: center;
  gap: 8px;
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
</style>
