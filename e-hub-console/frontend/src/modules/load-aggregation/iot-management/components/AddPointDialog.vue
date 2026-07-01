<template>
  <el-dialog
    :visible.sync="dialogVisible"
    title="添加测点"
    width="860px"
    append-to-body
    @open="handleOpen"
  >
    <div class="content">
      <div class="panel">
        <div class="panel-head">
          <span>可选测点</span>
          <el-input
            v-model.trim="searchKeyword"
            size="small"
            clearable
            placeholder="搜索测点"
          >
            <i slot="prefix" class="el-input__icon el-icon-search" />
          </el-input>
        </div>
        <div v-loading="loading" class="point-list">
          <label
            v-for="item in filteredPoints"
            :key="item.propertyCode"
            class="point-item"
          >
            <el-checkbox :value="isSelected(item)" @change="toggle(item)">
              <div>
                <div>{{ item.propertyName }}</div>
                <div class="sub">{{ item.propertyCode }}</div>
              </div>
            </el-checkbox>
          </label>
        </div>
      </div>
      <div class="panel">
        <div class="panel-head">
          <span>已选测点</span>
          <span class="count">{{ selectedPoints.length }}</span>
        </div>
        <div class="point-list">
          <div
            v-for="item in selectedPoints"
            :key="item.propertyCode"
            class="selected-item"
          >
            <div>
              <div>{{ item.propertyName }}</div>
              <div class="sub">{{ item.propertyCode }}</div>
            </div>
            <el-button type="text" icon="el-icon-close" @click="toggle(item)" />
          </div>
          <el-empty v-if="!selectedPoints.length" :image-size="80" description="未选择测点" />
        </div>
      </div>
    </div>
    <span slot="footer">
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { batchAddDevicePoints, listAvailablePoints } from "../api/index.js";

export default {
  name: "AddPointDialog",
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
      submitting: false,
      searchKeyword: "",
      availablePoints: [],
      selectedPoints: [],
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
    filteredPoints() {
      const keyword = this.searchKeyword.toLowerCase();
      if (!keyword) {
        return this.availablePoints;
      }
      return this.availablePoints.filter(item =>
        String(item.propertyName || "").toLowerCase().includes(keyword) ||
        String(item.propertyCode || "").toLowerCase().includes(keyword)
      );
    },
  },
  methods: {
    async handleOpen() {
      if (!this.device || !this.device.id) {
        return;
      }
      this.selectedPoints = [];
      this.searchKeyword = "";
      this.loading = true;
      try {
        const res = await listAvailablePoints(this.device.id);
        const body = res.data || {};
        this.availablePoints = body.code === 200 && Array.isArray(body.data) ? body.data : [];
      } finally {
        this.loading = false;
      }
    },
    isSelected(item) {
      return this.selectedPoints.some(point => point.propertyCode === item.propertyCode);
    },
    toggle(item) {
      const index = this.selectedPoints.findIndex(point => point.propertyCode === item.propertyCode);
      if (index >= 0) {
        this.selectedPoints.splice(index, 1);
      } else {
        this.selectedPoints.push(item);
      }
    },
    async submit() {
      if (!this.selectedPoints.length) {
        this.$message.warning("请选择测点");
        return;
      }
      this.submitting = true;
      try {
        const payload = this.selectedPoints.map((item, index) => ({
          propertyCode: item.propertyCode,
          propertyName: item.propertyName,
          dataType: item.dataType,
          dataTypeName: item.dataTypeName,
          valueType: item.valueType,
          unit: item.unit,
          readWriteRole: item.readWriteRole,
          sort: item.sort || index + 1,
        }));
        const res = await batchAddDevicePoints(this.device.id, payload);
        const body = res.data || {};
        if (body.code === 200) {
          this.$message.success("添加成功");
          this.$emit("success");
          this.dialogVisible = false;
        } else {
          this.$message.error(body.msg || "添加失败");
        }
      } finally {
        this.submitting = false;
      }
    },
  },
};
</script>

<style scoped lang="less">
.content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  min-height: 420px;
}

.panel {
  border: 1px solid #e1eaf1;
  border-radius: 8px;
  overflow: hidden;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: #f8fbfd;
}

.point-list {
  max-height: 360px;
  padding: 12px 16px;
  overflow: auto;
}

.point-item,
.selected-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f0f4f7;
}

.sub,
.count {
  color: #7c93a3;
  font-size: 12px;
}
</style>
