<template>
  <el-dialog
    :visible.sync="dialogVisible"
    :title="editMode ? '编辑设备' : '新增设备'"
    width="1100px"
    @open="handleOpen"
  >
    <el-form ref="form" :model="form" :rules="rules" label-width="110px" size="small">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="设备名称" prop="deviceName">
            <el-input v-model.trim="form.deviceName" maxlength="15" show-word-limit />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="设备类型" prop="deviceTypeCode">
            <el-select v-model="form.deviceTypeCode" filterable :disabled="editMode" @change="handleDeviceTypeChange">
              <el-option
                v-for="item in deviceTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="通讯方式" prop="communicationMethod">
            <el-select v-model="form.communicationMethod" filterable>
              <el-option
                v-for="item in communicationMethods"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="所属网关">
            <el-select v-model="form.gatewayId" clearable filterable>
              <el-option
                v-for="item in gatewayOptions"
                :key="item.id"
                :label="item.gatewayName"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="第三方API">
            <el-select v-model="form.thirdPartyApi" clearable filterable>
              <el-option
                v-for="item in thirdPartyApis"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="第三方标识">
            <el-input v-model.trim="form.thirdPartyCode" maxlength="50" show-word-limit />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <div v-if="!editMode" class="meta-section">
      <div class="section-head">
        <span>测点选择</span>
        <span class="section-count">已选 {{ selectedPoints.length }} 个</span>
      </div>
      <el-table
        ref="pointsTable"
        :data="pointRows"
        border
        stripe
        size="small"
        max-height="320"
        row-key="propertyCode"
        @selection-change="handlePointSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" reserve-selection />
        <el-table-column prop="propertyName" label="测点名称" min-width="140" />
        <el-table-column prop="propertyCode" label="测点编码" min-width="120" />
        <el-table-column prop="dataTypeName" label="数据类型" min-width="100" />
        <el-table-column prop="unit" label="单位" min-width="80" />
      </el-table>
    </div>

    <span slot="footer">
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import {
  createDevice,
  getDeviceDetail,
  getDeviceTypePointMetadata,
  listCommunicationMethods,
  listDeviceTypes,
  listGateways,
  listThirdPartyApis,
  updateDevice,
} from "../api/index.js";

export default {
  name: "DeviceDialog",
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
    deviceGroupId: {
      type: Number,
      default: null,
    },
    editData: {
      type: Object,
      default: null,
    },
  },
  data() {
    return {
      submitting: false,
      form: {
        deviceName: "",
        deviceTypeCode: "",
        communicationMethod: "",
        gatewayId: null,
        thirdPartyApi: "",
        thirdPartyCode: "",
      },
      deviceTypeOptions: [],
      communicationMethods: [],
      gatewayOptions: [],
      thirdPartyApis: [],
      pointRows: [],
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
    editMode() {
      return Boolean(this.editData && this.editData.id);
    },
    rules() {
      return {
        deviceName: [{ required: true, message: "请输入设备名称", trigger: "blur" }],
        deviceTypeCode: [{ required: true, message: "请选择设备类型", trigger: "change" }],
        communicationMethod: [{ required: true, message: "请选择通讯方式", trigger: "change" }],
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
        if (this.form.deviceTypeCode) {
          await this.handleDeviceTypeChange(this.form.deviceTypeCode);
        }
      }
    },
    async loadOptions() {
      const [deviceTypesRes, methodsRes, gatewaysRes, apisRes] = await Promise.all([
        listDeviceTypes(),
        listCommunicationMethods(),
        listGateways({ aggregatorId: this.aggregatorId, entId: this.entId }),
        listThirdPartyApis(),
      ]);
      this.deviceTypeOptions = ((deviceTypesRes.data || {}).data) || [];
      this.communicationMethods = ((methodsRes.data || {}).data) || [];
      this.gatewayOptions = ((gatewaysRes.data || {}).data) || [];
      this.thirdPartyApis = ((apisRes.data || {}).data) || [];
    },
    resetForm() {
      this.form = {
        deviceName: "",
        deviceTypeCode: this.deviceTypeOptions[0] ? this.deviceTypeOptions[0].value : "",
        communicationMethod: this.communicationMethods[0] ? this.communicationMethods[0].value : "",
        gatewayId: this.gatewayOptions[0] ? this.gatewayOptions[0].id : null,
        thirdPartyApi: this.thirdPartyApis[0] ? this.thirdPartyApis[0].value : "",
        thirdPartyCode: "",
      };
      this.pointRows = [];
      this.selectedPoints = [];
      this.clearPointSelection();
    },
    async loadDetail() {
      const res = await getDeviceDetail(this.editData.id);
      const detail = ((res.data || {}).data) || {};
      this.form = {
        deviceName: detail.deviceName || "",
        deviceTypeCode: detail.deviceTypeCode || "",
        communicationMethod: detail.communicationMethod || "",
        gatewayId: detail.gatewayId || null,
        thirdPartyApi: detail.thirdPartyApi || "",
        thirdPartyCode: detail.thirdPartyCode || "",
      };
      this.pointRows = [];
      this.selectedPoints = [];
      this.clearPointSelection();
    },
    async handleDeviceTypeChange(deviceTypeCode) {
      this.selectedPoints = [];
      this.clearPointSelection();
      if (!deviceTypeCode || this.editMode) {
        this.pointRows = [];
        return;
      }
      const pointRes = await getDeviceTypePointMetadata(deviceTypeCode);
      const points = ((pointRes.data || {}).data) || [];
      this.pointRows = points;
    },
    clearPointSelection() {
      this.$nextTick(() => {
        if (this.$refs.pointsTable) {
          this.$refs.pointsTable.clearSelection();
        }
      });
    },
    handlePointSelectionChange(selection) {
      this.selectedPoints = selection;
    },
    async submit() {
      await this.$refs.form.validate();
      this.submitting = true;
      try {
        const selectedType = this.deviceTypeOptions.find(item => item.value === this.form.deviceTypeCode);
        const payload = {
          aggregatorId: this.aggregatorId,
          entId: this.entId,
          deviceGroupId: this.deviceGroupId,
          deviceName: this.form.deviceName,
          deviceTypeCode: this.form.deviceTypeCode,
          deviceTypeName: selectedType ? selectedType.label : "",
          communicationMethod: this.form.communicationMethod,
          gatewayId: this.form.gatewayId,
          thirdPartyApi: this.form.thirdPartyApi,
          thirdPartyCode: this.form.thirdPartyCode,
        };
        if (!this.editMode) {
          payload.pointList = this.selectedPoints.map((item, index) => ({
            propertyCode: item.propertyCode,
            propertyName: item.propertyName,
            dataType: item.dataType,
            dataTypeName: item.dataTypeName,
            valueType: item.valueType,
            unit: item.unit,
            readWriteRole: item.readWriteRole,
            sort: item.sort || index + 1,
          }));
        }
        const res = this.editMode
          ? await updateDevice(this.editData.id, payload)
          : await createDevice(payload);
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

.section-count {
  color: #607d8f;
  font-size: 12px;
  font-weight: 500;
}
</style>
