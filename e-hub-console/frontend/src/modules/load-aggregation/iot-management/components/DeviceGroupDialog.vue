<template>
  <el-dialog
    :visible.sync="dialogVisible"
    :title="editMode ? '编辑设备组' : '新增设备组'"
    width="960px"
    @open="handleOpen"
  >
    <el-form ref="form" :model="form" :rules="rules" label-width="110px" size="small">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="设备组名称" prop="deviceGroupName">
            <el-input v-model.trim="form.deviceGroupName" maxlength="15" show-word-limit />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="设备组类型" prop="deviceGroupType">
            <el-select v-model="form.deviceGroupType" filterable>
              <el-option
                v-for="item in groupTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="所属网关" prop="gatewayId">
            <el-select v-model="form.gatewayId" filterable>
              <el-option
                v-for="item in gatewayOptions"
                :key="item.id"
                :label="item.gatewayName"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="供能类型">
            <el-select v-model="form.energyType" clearable>
              <el-option
                v-for="item in energyTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <div class="meta-section">
      <div class="section-head">
        <span>设备组测点</span>
        <div class="header-buttons">
          <el-button type="primary" size="small" @click="handleSelectPoints">
            {{ isAllPointsSelected ? "取消选择全部测点" : "选择全部测点" }}
          </el-button>
          <el-button v-if="editMode" type="primary" size="small" @click="showStatusDialog = true">状态值维护</el-button>
        </div>
      </div>
      <el-table
        ref="pointsTable"
        v-loading="pointsLoading"
        :data="pagedPoints"
        border
        stripe
        size="small"
        row-key="propertyCode"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" reserve-selection />
        <el-table-column prop="propertyName" label="测点名称" min-width="140" />
        <el-table-column prop="propertyCode" label="测点编码" min-width="120" />
        <el-table-column prop="dataTypeName" label="数据类型" min-width="100" />
        <el-table-column prop="unit" label="单位" min-width="80" />
      </el-table>
      <el-pagination
        class="pagination"
        background
        layout="total, sizes, prev, pager, next"
        :current-page="pointPage.pageNum"
        :page-sizes="[10, 20, 30, 50]"
        :page-size="pointPage.pageSize"
        :total="allPointsData.length"
        @current-change="handlePointPageChange"
        @size-change="handlePointSizeChange"
      />
    </div>

    <DeviceGroupStatusMaintenanceDialog
      :visible.sync="showStatusDialog"
      :device-group-id="editData ? editData.id : null"
      :points="selectedPoints"
    />

    <span slot="footer">
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import {
  createDeviceGroup,
  getDeviceGroupDetail,
  getDeviceGroupPointMetadata,
  listDeviceGroupTypes,
  listDeviceGroupPoints,
  listEnergyTypes,
  listGateways,
  updateDeviceGroup,
} from "../api/index.js";

import DeviceGroupStatusMaintenanceDialog from "./DeviceGroupStatusMaintenanceDialog.vue";

export default {
  name: "DeviceGroupDialog",
  components: {
    DeviceGroupStatusMaintenanceDialog,
  },
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    aggregatorId: {
      type: String,
      default: "",
    },
    entId: {
      type: String,
      default: "",
    },
    editData: {
      type: Object,
      default: null,
    },
  },
  data() {
    return {
      submitting: false,
      pointsLoading: false,
      form: {
        deviceGroupName: "",
        deviceGroupType: "",
        gatewayId: null,
        energyType: "",
      },
      allPointsData: [],
      selectedPoints: [],
      editPointList: [],
      pointPage: {
        pageNum: 1,
        pageSize: 10,
      },
      showStatusDialog: false,
      groupTypeOptions: [],
      energyTypeOptions: [],
      gatewayOptions: [],
    };
  },
  watch: {
    "form.deviceGroupType"(newVal, oldVal) {
      if (newVal && oldVal !== undefined && oldVal !== "" && oldVal !== newVal) {
        this.selectedPoints = [];
        this.editPointList = [];
        this.pointPage.pageNum = 1;
        this.$nextTick(() => {
          if (this.$refs.pointsTable) {
            this.$refs.pointsTable.clearSelection();
          }
        });
      }
      if (newVal) {
        this.fetchPointsData();
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
    editMode() {
      return Boolean(this.editData && this.editData.id);
    },
    pagedPoints() {
      const start = (this.pointPage.pageNum - 1) * this.pointPage.pageSize;
      const end = start + this.pointPage.pageSize;
      return this.allPointsData.slice(start, end);
    },
    isAllPointsSelected() {
      return this.allPointsData.length > 0 && this.selectedPoints.length === this.allPointsData.length;
    },
    rules() {
      return {
        deviceGroupName: [{ required: true, message: "请输入设备组名称", trigger: "blur" }],
        deviceGroupType: [{ required: true, message: "请选择设备组类型", trigger: "change" }],
        gatewayId: [{ required: true, message: "请选择网关", trigger: "change" }],
      };
    },
  },
  methods: {
    async handleOpen() {
      await this.loadOptions();
      if (this.editMode) {
        await this.loadDetail();
      } else {
        this.resetForm();
      }
    },
    async loadOptions() {
      const [groupTypesRes, energyTypesRes, gatewaysRes] = await Promise.all([
        listDeviceGroupTypes(),
        listEnergyTypes(),
        listGateways({ aggregatorId: this.aggregatorId, entId: this.entId }),
      ]);
      this.groupTypeOptions = ((groupTypesRes.data || {}).data) || [];
      this.energyTypeOptions = ((energyTypesRes.data || {}).data) || [];
      this.gatewayOptions = ((gatewaysRes.data || {}).data) || [];
    },
    async fetchPointsData() {
      if (!this.form.deviceGroupType) {
        this.allPointsData = [];
        return;
      }
      this.pointsLoading = true;
      try {
        const res = await getDeviceGroupPointMetadata({ deviceGroupType: this.form.deviceGroupType });
        const body = res.data || {};
        const page = body.code === 200 && body.data ? body.data : {};
        this.allPointsData = Array.isArray(page.list) ? page.list : [];
        this.pointPage.pageNum = 1;
        if (this.editMode && this.editPointList.length > 0) {
          this.handleEditPointsEcho();
        }
      } finally {
        this.pointsLoading = false;
      }
    },
    async loadDetail() {
      const [detailRes, pointsRes] = await Promise.all([
        getDeviceGroupDetail(this.editData.id),
        listDeviceGroupPoints(this.editData.id),
      ]);
      const detail = ((detailRes.data || {}).data) || {};
      this.form = {
        deviceGroupName: detail.deviceGroupName || "",
        deviceGroupType: detail.deviceGroupType || "",
        gatewayId: detail.gatewayId || null,
        energyType: detail.energyType || "",
      };
      this.editPointList = Array.isArray(detail.pointList) ? [...detail.pointList] : [];
      await this.fetchPointsData();
    },
    handleEditPointsEcho() {
      this.$nextTick(() => {
        if (!this.$refs.pointsTable) return;
        this.allPointsData.forEach(point => {
          const key = point.propertyCode || point.id;
          const isInEditList = this.editPointList.some(editPoint => {
            const editKey = editPoint.propertyCode || editPoint.pointCode || editPoint.id;
            return editKey === key;
          });
          if (isInEditList) {
            this.$refs.pointsTable.toggleRowSelection(point, true);
          }
        });
      });
    },
    resetForm() {
      this.form = {
        deviceGroupName: "",
        deviceGroupType: this.groupTypeOptions[0] ? this.groupTypeOptions[0].value : "",
        gatewayId: this.gatewayOptions[0] ? this.gatewayOptions[0].id : null,
        energyType: "",
      };
      this.allPointsData = [];
      this.selectedPoints = [];
      this.editPointList = [];
      this.pointPage.pageNum = 1;
      this.$nextTick(() => {
        if (this.$refs.pointsTable) {
          this.$refs.pointsTable.clearSelection();
        }
      });
      if (this.form.deviceGroupType) {
        this.fetchPointsData();
      }
    },
    handleSelectPoints() {
      if (!this.$refs.pointsTable) return;
      if (this.isAllPointsSelected) {
        this.$refs.pointsTable.clearSelection();
      } else {
        this.allPointsData.forEach(row => {
          this.$refs.pointsTable.toggleRowSelection(row, true);
        });
      }
    },
    handleSelectionChange(selection) {
      this.selectedPoints = selection;
    },
    handlePointPageChange(pageNum) {
      this.pointPage.pageNum = pageNum;
    },
    handlePointSizeChange(pageSize) {
      this.pointPage.pageSize = pageSize;
      this.pointPage.pageNum = 1;
    },
    async submit() {
      await this.$refs.form.validate();
      this.submitting = true;
      try {
        const payload = {
          aggregatorId: this.aggregatorId,
          entId: this.entId,
          ...this.form,
          pointList: this.selectedPoints,
        };
        const res = this.editMode
          ? await updateDeviceGroup(this.editData.id, payload)
          : await createDeviceGroup(payload);
        const body = res.data || {};
        if (body.code === 200) {
          this.$message.success("保存成功");
          this.$emit("success");
          this.dialogVisible = false;
        } else {
          this.$message.error(body.msg || "保存失败");
        }
      } finally {
        this.submitting = false;
      }
    },
  },
};
</script>

<style scoped lang="less">
.meta-section {
  margin-top: 16px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  font-weight: 600;
  color: #18364a;
}

.header-buttons {
  display: flex;
  gap: 8px;
}

.pagination {
  margin-top: 12px;
  text-align: right;
}
</style>
