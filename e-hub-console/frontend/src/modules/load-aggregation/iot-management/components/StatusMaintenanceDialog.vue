<template>
  <el-dialog
    :visible.sync="dialogVisible"
    title="状态值维护"
    width="960px"
    append-to-body
  >
    <div class="content">
      <div class="left">
        <el-input
          v-model.trim="searchKeyword"
          size="small"
          clearable
          placeholder="搜索测点"
        >
          <i slot="prefix" class="el-input__icon el-icon-search" />
        </el-input>
        <div class="point-list">
          <button
            v-for="item in filteredPoints"
            :key="item.id"
            type="button"
            class="point-item"
            :class="{ active: currentPoint && currentPoint.id === item.id }"
            @click="selectPoint(item)"
          >
            <span>{{ item.propertyName }}</span>
            <span>{{ item.propertyCode }}</span>
          </button>
        </div>
      </div>

      <div class="right">
        <div class="head">
          <span>状态值列表</span>
          <el-button type="primary" size="small" @click="addRow">新增状态值</el-button>
        </div>
        <el-table :data="definitionRows" border size="small">
          <el-table-column label="状态值" min-width="140">
            <template slot-scope="{ row }">
              <el-input v-model.trim="row.value" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="释义" min-width="180">
            <template slot-scope="{ row }">
              <el-input v-model.trim="row.description" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="标签" min-width="140">
            <template slot-scope="{ row }">
              <el-input v-model.trim="row.tags" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template slot-scope="{ row }">
              <el-button type="text" size="mini" @click="saveRow(row)">保存</el-button>
              <el-button type="text" size="mini" @click="removeRow(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import {
  createDevicePointDefinition,
  deleteDevicePointDefinition,
  listDevicePointDefinitions,
  updateDevicePointDefinition,
} from "../api/index.js";

export default {
  name: "StatusMaintenanceDialog",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    device: {
      type: Object,
      default: null,
    },
    points: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      searchKeyword: "",
      currentPoint: null,
      definitionRows: [],
    };
  },
  watch: {
    visible(value) {
      if (value) {
        const first = this.points[0] || null;
        if (first) {
          this.selectPoint(first);
        } else {
          this.currentPoint = null;
          this.definitionRows = [];
        }
      }
    },
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
    filteredPoints() {
      const keyword = this.searchKeyword.toLowerCase();
      if (!keyword) {
        return this.points;
      }
      return this.points.filter(item =>
        String(item.propertyName || "").toLowerCase().includes(keyword) ||
        String(item.propertyCode || "").toLowerCase().includes(keyword)
      );
    },
  },
  methods: {
    async selectPoint(point) {
      this.currentPoint = point;
      if (!point || !point.id) {
        this.definitionRows = [];
        return;
      }
      const res = await listDevicePointDefinitions(point.id);
      const body = res.data || {};
      this.definitionRows = body.code === 200 && Array.isArray(body.data) ? body.data : [];
    },
    addRow() {
      if (!this.currentPoint) {
        this.$message.warning("请选择测点");
        return;
      }
      this.definitionRows.push({
        id: null,
        value: "",
        description: "",
        tags: "",
      });
    },
    async saveRow(row) {
      if (!this.currentPoint || !row.value) {
        this.$message.warning("请输入状态值");
        return;
      }
      const payload = {
        value: row.value,
        description: row.description,
        tags: row.tags,
      };
      const res = row.id
        ? await updateDevicePointDefinition(row.id, payload)
        : await createDevicePointDefinition(this.currentPoint.id, payload);
      const body = res.data || {};
      if (body.code === 200) {
        this.$message.success("保存成功");
        this.selectPoint(this.currentPoint);
      } else {
        this.$message.error(body.msg || "保存失败");
      }
    },
    async removeRow(row) {
      if (!row.id) {
        this.definitionRows = this.definitionRows.filter(item => item !== row);
        return;
      }
      const res = await deleteDevicePointDefinition(row.id);
      const body = res.data || {};
      if (body.code === 200) {
        this.$message.success("删除成功");
        this.selectPoint(this.currentPoint);
      } else {
        this.$message.error(body.msg || "删除失败");
      }
    },
  },
};
</script>

<style scoped lang="less">
.content {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 16px;
}

.left,
.right {
  border: 1px solid #e1eaf1;
  border-radius: 8px;
  padding: 14px;
}

.point-list {
  margin-top: 12px;
  max-height: 420px;
  overflow: auto;
}

.point-item {
  width: 100%;
  display: flex;
  justify-content: space-between;
  padding: 10px 12px;
  margin-bottom: 8px;
  border: 1px solid #e8eef4;
  border-radius: 8px;
  background: #f9fbfd;
  cursor: pointer;
  text-align: left;
}

.point-item.active {
  border-color: #0b84ff;
  background: #eef7ff;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
</style>
