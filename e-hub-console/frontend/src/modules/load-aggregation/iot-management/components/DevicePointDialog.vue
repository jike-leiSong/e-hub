<template>
  <el-dialog
    :visible.sync="dialogVisible"
    :title="device ? `${device.deviceName} - 设备点表` : '设备点表'"
    width="1000px"
    top="5vh"
    @open="handleOpen"
  >
    <div class="toolbar">
      <div class="filters">
        <el-input
          v-model.trim="filters.pointQuery"
          size="small"
          clearable
          placeholder="请输入测点名称或测点编码"
          @input="handleSearch"
        >
          <i slot="prefix" class="el-input__icon el-icon-search" />
        </el-input>
      </div>
      <div class="actions">
        <el-button type="primary" size="small" @click="showStatusDialog = true">状态值维护</el-button>
        <el-button type="primary" size="small" @click="showAddPointDialog = true">添加测点</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="pointList" border stripe size="small">
      <el-table-column prop="propertyName" label="测点名称" min-width="140" />
      <el-table-column prop="propertyCode" label="测点编码" min-width="120" />
      <el-table-column prop="dataTypeName" label="数据类型" min-width="100" />
      <el-table-column prop="unit" label="单位" min-width="80" />
      <el-table-column prop="thirdPartyCode" label="第三方标识" min-width="120" />
      <el-table-column prop="readWriteRoleName" label="读写权限" min-width="100" />
      <el-table-column prop="upWayName" label="上报方式" min-width="100" />
      <el-table-column prop="upPeriodName" label="上报周期" min-width="100" />
      <el-table-column label="操作" width="100" fixed="right">
        <template slot-scope="{ row }">
          <el-button type="text" size="mini" @click="removePoint(row)">删除</el-button>
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

    <AddPointDialog
      :visible.sync="showAddPointDialog"
      :device="device"
      @success="reload"
    />
    <StatusMaintenanceDialog
      :visible.sync="showStatusDialog"
      :device="device"
      :points="pointList"
    />
  </el-dialog>
</template>

<script>
import { deleteDevicePoint, listDevicePoints } from "../api/index.js";
import AddPointDialog from "./AddPointDialog.vue";
import StatusMaintenanceDialog from "./StatusMaintenanceDialog.vue";

export default {
  name: "DevicePointDialog",
  components: {
    AddPointDialog,
    StatusMaintenanceDialog,
  },
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    device: {
      type: Object,
      default: null,
    },
  },
  data() {
    return {
      loading: false,
      filters: {
        pointQuery: "",
      },
      pointList: [],
      page: {
        pageNum: 1,
        pageSize: 10,
        total: 0,
      },
      showAddPointDialog: false,
      showStatusDialog: false,
      searchTimer: null,
    };
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible;
      },
      set(value) {
        this.$emit("update:visible", value);
      },
    },
  },
  methods: {
    handleOpen() {
      this.page.pageNum = 1;
      this.reload();
    },
    async reload() {
      if (!this.device || !this.device.id) {
        this.pointList = [];
        return;
      }
      this.loading = true;
      try {
        const res = await listDevicePoints(this.device.id, {
          pointQuery: this.filters.pointQuery || undefined,
          pageNum: this.page.pageNum,
          pageSize: this.page.pageSize,
        });
        const body = res.data || {};
        const page = body.code === 200 && body.data ? body.data : {};
        this.pointList = Array.isArray(page.list) ? page.list : [];
        this.page.total = page.total || 0;
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      window.clearTimeout(this.searchTimer);
      this.searchTimer = window.setTimeout(() => {
        this.page.pageNum = 1;
        this.reload();
      }, 300);
    },
    handlePageChange(pageNum) {
      this.page.pageNum = pageNum;
      this.reload();
    },
    handleSizeChange(pageSize) {
      this.page.pageSize = pageSize;
      this.page.pageNum = 1;
      this.reload();
    },
    async removePoint(row) {
      await this.$confirm(`确认删除测点“${row.propertyName}”吗？`, "删除确认", {
        type: "warning",
      });
      const res = await deleteDevicePoint(row.id);
      const body = res.data || {};
      if (body.code === 200) {
        this.$message.success("删除成功");
        this.reload();
      } else {
        this.$message.error(body.msg || "删除失败");
      }
    },
  },
};
</script>

<style scoped lang="less">
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.filters,
.actions {
  display: flex;
  gap: 12px;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
