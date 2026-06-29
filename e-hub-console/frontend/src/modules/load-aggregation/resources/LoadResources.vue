<template>
  <div class="iot-resource-page">
    <section class="management-content">
      <div class="resource-tabs">
        <button
          v-for="tab in resourceTabs"
          :key="tab.key"
          type="button"
          :class="{ active: activeResourceTab === tab.key }"
          @click="switchResourceTab(tab.key)"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- 企业管理 -->
      <div v-if="activeResourceTab === 'enterprise'" class="tab-content">
        <div class="content-header">
          <h3>企业管理</h3>
          <el-button
            type="primary"
            size="small"
            icon="el-icon-plus"
            @click="openEnterpriseDialog('create')"
          >
            新增企业
          </el-button>
        </div>

        <div class="filter-panel">
          <el-form :inline="true" :model="enterpriseFilters" size="small">
            <el-form-item label="聚合商">
              <el-input :value="currentAggregatorLabel" disabled />
            </el-form-item>
            <el-form-item label="企业名称">
              <el-input
                v-model.trim="enterpriseFilters.entName"
                clearable
                placeholder="请输入企业名称"
              />
            </el-form-item>
            <el-form-item label="企业ID">
              <el-input
                v-model.trim="enterpriseFilters.entId"
                clearable
                placeholder="请输入企业ID"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select
                v-model="enterpriseFilters.status"
                clearable
                placeholder="全部"
              >
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                icon="el-icon-search"
                @click="reloadEnterprises"
              >
                查询
              </el-button>
              <el-button icon="el-icon-refresh" @click="resetEnterpriseFilters">
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="data-table">
          <el-table
            v-loading="enterpriseLoading"
            :data="validEnterprises"
            size="small"
            stripe
            border
          >
            <el-table-column
              prop="entName"
              label="企业名称"
              min-width="200"
              fixed
            />
            <el-table-column prop="entId" label="企业ID" min-width="150" />
            <el-table-column
              prop="aggregatorId"
              label="聚合商ID"
              min-width="150"
            />
            <el-table-column
              prop="stationId"
              label="企业编码"
              min-width="120"
            />
            <el-table-column
              prop="installCap"
              label="装机容量"
              min-width="100"
            />
            <el-table-column
              prop="stateGridName"
              label="电网名称"
              min-width="140"
            />
            <el-table-column
              prop="serviceStartDate"
              label="服务开始"
              min-width="120"
            />
            <el-table-column
              prop="serviceEndDate"
              label="服务结束"
              min-width="120"
            />
            <el-table-column prop="status" label="状态" width="80">
              <template slot-scope="{ row }">
                <el-tag
                  size="mini"
                  :type="row.status === 1 ? 'success' : 'info'"
                >
                  {{ row.status === 1 ? "启用" : "停用" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template slot-scope="{ row }">
                <el-button
                  type="text"
                  size="mini"
                  @click="openEnterpriseDialog('edit', row)"
                >
                  修改
                </el-button>
                <el-button
                  type="text"
                  size="mini"
                  @click="removeEnterprise(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="table-pagination"
            background
            layout="total, prev, pager, next, sizes"
            :page-size="enterprisePage.pageSize"
            :current-page.sync="enterprisePage.pageIndex"
            :total="enterprisePage.total"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="reloadEnterprises"
            @size-change="handleEnterpriseSizeChange"
          />
        </div>
      </div>

      <!-- 模型管理 -->
      <div v-if="activeResourceTab === 'model'" class="tab-content">
        <div class="content-header">
          <h3>模型管理</h3>
          <el-button
            type="primary"
            size="small"
            icon="el-icon-plus"
            @click="openModelDialog('create')"
          >
            新增模型
          </el-button>
        </div>

        <div class="filter-panel">
          <el-form :inline="true" :model="modelFilters" size="small">
            <el-form-item label="模型名称">
              <el-input
                v-model.trim="modelFilters.modelName"
                clearable
                placeholder="请输入模型名称"
              />
            </el-form-item>
            <el-form-item label="模型类型">
              <el-input
                v-model.trim="modelFilters.modelType"
                clearable
                placeholder="请输入模型类型"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                icon="el-icon-search"
                @click="reloadModels"
              >
                查询
              </el-button>
              <el-button icon="el-icon-refresh" @click="resetModelFilters">
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="data-table">
          <el-table
            v-loading="modelLoading"
            :data="models"
            size="small"
            stripe
            border
          >
            <el-table-column
              prop="modelName"
              label="模型名称"
              min-width="200"
            />
            <el-table-column prop="modelType" label="模型类型" min-width="150" />
            <el-table-column
              prop="description"
              label="描述"
              min-width="200"
              show-overflow-tooltip
            />
            <el-table-column prop="status" label="状态" width="80">
              <template slot-scope="{ row }">
                <el-tag
                  size="mini"
                  :type="row.status === 1 ? 'success' : 'info'"
                >
                  {{ row.status === 1 ? "启用" : "停用" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column
              prop="createTime"
              label="创建时间"
              min-width="160"
            />
            <el-table-column label="操作" width="180" fixed="right">
              <template slot-scope="{ row }">
                <el-button
                  type="text"
                  size="mini"
                  @click="openModelDialog('edit', row)"
                >
                  修改
                </el-button>
                <el-button type="text" size="mini" @click="removeModel(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="table-pagination"
            background
            layout="total, prev, pager, next, sizes"
            :page-size="modelPage.pageSize"
            :current-page.sync="modelPage.pageIndex"
            :total="modelPage.total"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="reloadModels"
            @size-change="handleModelSizeChange"
          />
        </div>
      </div>

      <!-- 设备管理 -->
      <div v-if="activeResourceTab === 'device'" class="tab-content">
        <div class="content-header">
          <h3>设备管理</h3>
          <el-button
            type="primary"
            size="small"
            icon="el-icon-plus"
            @click="openDeviceDialog('create')"
          >
            新增设备
          </el-button>
        </div>

        <div class="filter-panel">
          <el-form :inline="true" :model="deviceFilters" size="small">
            <el-form-item label="聚合商">
              <el-input :value="currentAggregatorLabel" disabled />
            </el-form-item>
            <el-form-item label="企业">
              <el-select
                v-model="deviceFilters.entId"
                clearable
                filterable
                placeholder="选择企业"
              >
                <el-option
                  v-for="item in filteredEnterpriseOptions"
                  :key="item.entId"
                  :label="enterpriseOptionLabel(item)"
                  :value="item.entId"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="设备名称">
              <el-input
                v-model.trim="deviceFilters.deviceName"
                clearable
                placeholder="请输入设备名称"
              />
            </el-form-item>
            <el-form-item label="设备类型">
              <el-input
                v-model.trim="deviceFilters.deviceTypeCode"
                clearable
                placeholder="请输入类型"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                icon="el-icon-search"
                @click="reloadDevices"
              >
                查询
              </el-button>
              <el-button icon="el-icon-refresh" @click="resetDeviceFilters">
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="data-table">
          <el-table
            v-loading="deviceLoading"
            :data="devices"
            size="small"
            stripe
            border
          >
            <el-table-column
              prop="deviceCode"
              label="设备编码"
              min-width="140"
              fixed
            />
            <el-table-column
              prop="deviceName"
              label="设备名称"
              min-width="180"
            />
            <el-table-column prop="entId" label="企业ID" min-width="140" />
            <el-table-column
              prop="deviceTypeCode"
              label="设备类型"
              min-width="100"
            />
            <el-table-column
              prop="manufacturer"
              label="厂商"
              min-width="120"
            />
            <el-table-column prop="model" label="型号" min-width="120" />
            <el-table-column prop="onlineStatus" label="在线状态" width="90">
              <template slot-scope="{ row }">
                <el-tag
                  size="mini"
                  :type="row.onlineStatus === 1 ? 'success' : 'info'"
                >
                  {{ row.onlineStatus === 1 ? "在线" : "离线" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="assetStatus" label="资产状态" width="90">
              <template slot-scope="{ row }">
                <el-tag
                  size="mini"
                  :type="row.assetStatus === 1 ? 'success' : 'info'"
                >
                  {{ row.assetStatus === 1 ? "启用" : "停用" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template slot-scope="{ row }">
                <el-button
                  type="text"
                  size="mini"
                  @click="openDeviceDialog('edit', row)"
                >
                  修改
                </el-button>
                <el-button type="text" size="mini" @click="removeDevice(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="table-pagination"
            background
            layout="total, prev, pager, next, sizes"
            :page-size="devicePage.pageSize"
            :current-page.sync="devicePage.pageIndex"
            :total="devicePage.total"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="reloadDevices"
            @size-change="handleDeviceSizeChange"
          />
        </div>
      </div>
    </section>


    <el-dialog
      :title="modelDialog.title"
      :visible.sync="modelDialog.visible"
      width="620px"
    >
      <el-form :model="modelForm" label-width="96px" size="small">
        <el-form-item label="模型名称" required>
          <el-input v-model.trim="modelForm.modelName" />
        </el-form-item>
        <el-form-item label="模型类型" required>
          <el-input v-model.trim="modelForm.modelType" placeholder="METE" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model.trim="modelForm.description"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="modelForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button size="small" @click="modelDialog.visible = false"
          >取消</el-button
        >
        <el-button
          size="small"
          type="primary"
          :loading="modelDialog.loading"
          @click="submitModel"
          >保存</el-button
        >
      </span>
    </el-dialog>

    <el-dialog
      :title="enterpriseDialog.title"
      :visible.sync="enterpriseDialog.visible"
      width="720px"
    >
      <el-form
        :model="enterpriseForm"
        label-width="110px"
        size="small"
        class="enterprise-form"
      >
        <el-form-item label="聚合商ID" required>
          <el-input
            v-model.trim="enterpriseForm.aggregatorId"
            :disabled="!isOwner"
            placeholder="aggregatorId"
          />
        </el-form-item>
        <el-form-item label="企业ID" required>
          <el-input
            v-model.trim="enterpriseForm.entId"
            :disabled="enterpriseDialog.mode === 'edit'"
            placeholder="entId"
          />
        </el-form-item>
        <el-form-item label="企业名称" required>
          <el-input v-model.trim="enterpriseForm.entName" />
        </el-form-item>
        <el-form-item label="企业编码">
          <el-input v-model.trim="enterpriseForm.stationId" />
        </el-form-item>
        <el-form-item label="装机容量">
          <el-input-number
            v-model="enterpriseForm.installCap"
            :min="0"
            :step="100"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="经纬度">
          <div class="inline-fields">
            <el-input
              v-model.trim="enterpriseForm.longitude"
              placeholder="经度"
            />
            <el-input
              v-model.trim="enterpriseForm.latitude"
              placeholder="纬度"
            />
          </div>
        </el-form-item>
        <el-form-item label="电网信息">
          <div class="inline-fields">
            <el-input
              v-model.trim="enterpriseForm.stateGridCode"
              placeholder="电网编码"
            />
            <el-input
              v-model.trim="enterpriseForm.stateGridName"
              placeholder="电网名称"
            />
          </div>
        </el-form-item>
        <el-form-item label="服务周期">
          <el-date-picker
            v-model="enterpriseServiceRange"
            type="daterange"
            size="small"
            value-format="yyyy-MM-dd"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="handleEnterpriseServiceRange"
          />
        </el-form-item>
        <el-form-item label="企业收益占比">
          <el-input-number
            v-model="enterpriseForm.percent"
            :min="0"
            :max="100"
            :step="1"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="按计划运行">
          <el-switch
            v-model="enterpriseForm.planRunStatus"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="enterpriseForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button size="small" @click="enterpriseDialog.visible = false"
          >取消</el-button
        >
        <el-button
          size="small"
          type="primary"
          :loading="enterpriseDialog.loading"
          @click="submitEnterprise"
        >
          保存
        </el-button>
      </span>
    </el-dialog>

    <el-dialog
      :title="deviceDialog.title"
      :visible.sync="deviceDialog.visible"
      width="620px"
    >
      <el-form :model="deviceForm" label-width="96px" size="small">
        <el-form-item label="聚合商">
          <el-input
            v-model.trim="deviceForm.aggregatorId"
            :disabled="!isOwner"
            placeholder="aggregatorId"
          />
        </el-form-item>
        <el-form-item label="企业" required>
          <el-select
            v-model="deviceForm.entId"
            filterable
            placeholder="请选择企业"
            @change="handleDeviceEntChange"
          >
            <el-option
              v-for="item in deviceFormEnterpriseOptions"
              :key="item.entId"
              :label="enterpriseOptionLabel(item)"
              :value="item.entId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="项目ID">
          <el-input-number
            v-model="deviceForm.projectId"
            :min="1"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="设备编码">
          <el-input
            v-model.trim="deviceForm.deviceCode"
            placeholder="为空时自动生成"
          />
        </el-form-item>
        <el-form-item label="设备名称" required>
          <el-input v-model.trim="deviceForm.deviceName" />
        </el-form-item>
        <el-form-item label="设备类型">
          <el-input
            v-model.trim="deviceForm.deviceTypeCode"
            placeholder="METE"
          />
        </el-form-item>
        <el-form-item label="类型名称">
          <el-input v-model.trim="deviceForm.deviceTypeName" />
        </el-form-item>
        <el-form-item label="厂商">
          <el-input v-model.trim="deviceForm.manufacturer" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model.trim="deviceForm.model" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="deviceForm.assetStatus">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button size="small" @click="deviceDialog.visible = false"
          >取消</el-button
        >
        <el-button
          size="small"
          type="primary"
          :loading="deviceDialog.loading"
          @click="submitDevice"
          >保存</el-button
        >
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  createDevice,
  createEnterprise,
  disableEnterprise,
  deleteDevice,
  listEnterprises,
  listDevices,
  updateEnterprise,
  updateDevice,
} from "./api";

export default {
  name: "LoadResources",
  props: {
    user: {
      type: Object,
      default: () => ({ platformType: "customer" }),
    },
    activePage: {
      type: String,
      default: "load-resources",
    },
    aggregatorId: {
      type: String,
      default: "",
    },
  },
  data() {
    const isOwner = this.user && this.user.platformType === "owner";
    const fixedAggregatorId = isOwner
      ? this.aggregatorId || sessionStorage.getItem("aggregatorId") || ""
      : this.user.aggregatorId || sessionStorage.getItem("aggregatorId") || "";
    return {
      activeResourceTab: "enterprise",
      scopeAggregatorId: fixedAggregatorId,
      resourceTabs: [
        { key: "enterprise", label: "企业管理" },
        { key: "model", label: "模型管理" },
        { key: "device", label: "设备管理" },
      ],
      enterpriseFilters: {
        aggregatorId: fixedAggregatorId,
        entId: "",
        entName: "",
        status: undefined,
      },
      enterprises: [],
      enterpriseLoading: false,
      enterprisePage: {
        pageIndex: 1,
        pageSize: 20,
        total: 0,
      },
      modelFilters: {
        modelName: "",
        modelType: "",
      },
      models: [],
      modelLoading: false,
      modelPage: {
        pageIndex: 1,
        pageSize: 20,
        total: 0,
      },
      deviceFilters: {
        aggregatorId: fixedAggregatorId,
        entId: "",
        deviceName: "",
        deviceTypeCode: "",
      },
      devices: [],
      deviceLoading: false,
      devicePage: {
        pageIndex: 1,
        pageSize: 20,
        total: 0,
      },
      enterpriseServiceRange: [],
      deviceDialog: {
        visible: false,
        loading: false,
        mode: "create",
        title: "新增设备",
      },
      enterpriseDialog: {
        visible: false,
        loading: false,
        mode: "create",
        title: "新增企业",
      },
      modelDialog: {
        visible: false,
        loading: false,
        mode: "create",
        title: "新增模型",
      },
      enterpriseForm: {},
      deviceForm: {},
      modelForm: {},
    };
  },
  computed: {
    isOwner() {
      return this.user && this.user.platformType === "owner";
    },
    aggregatorOptions() {
      const aggregatorMap = new Map();

      this.enterprises.forEach((item) => {
        if (item && item.aggregatorId) {
          if (!aggregatorMap.has(item.aggregatorId)) {
            aggregatorMap.set(item.aggregatorId, {
              id: item.aggregatorId,
              name: item.aggregatorName || `聚合商${item.aggregatorId.slice(-4)}`,
            });
          }
        }
      });

      this.devices.forEach((item) => {
        if (item && item.aggregatorId && !aggregatorMap.has(item.aggregatorId)) {
          aggregatorMap.set(item.aggregatorId, {
            id: item.aggregatorId,
            name: item.aggregatorName || `聚合商${item.aggregatorId.slice(-4)}`,
          });
        }
      });

      if (this.scopeAggregatorId && !aggregatorMap.has(this.scopeAggregatorId)) {
        aggregatorMap.set(this.scopeAggregatorId, {
          id: this.scopeAggregatorId,
          name: `聚合商${this.scopeAggregatorId.slice(-4)}`,
        });
      }

      return Array.from(aggregatorMap.values());
    },
    validEnterprises() {
      return this.enterprises.filter(item => item && item.entId);
    },
    filteredEnterpriseOptions() {
      const aggregatorId = this.deviceFilters.aggregatorId;
      if (!aggregatorId) {
        return this.validEnterprises;
      }
      return this.validEnterprises.filter(item => item.aggregatorId === aggregatorId);
    },
    deviceFormEnterpriseOptions() {
      const aggregatorId = this.deviceForm && this.deviceForm.aggregatorId;
      if (!aggregatorId) {
        return this.validEnterprises;
      }
      return this.validEnterprises.filter(item => item.aggregatorId === aggregatorId);
    },
    currentAggregatorLabel() {
      const aggregatorId = this.scopeAggregatorId;
      if (!aggregatorId) {
        return "-";
      }
      const option = this.aggregatorOptions.find(item => item.id === aggregatorId);
      return option ? `${option.name} (${option.id})` : aggregatorId;
    },
  },
  mounted() {
    this.enterpriseForm = this.defaultEnterpriseForm();
    this.deviceForm = this.defaultDeviceForm();
    this.modelForm = this.defaultModelForm();

    // 根据 activePage 设置初始菜单和标签
    this.syncMenuFromActivePage();

    this.reloadEnterprises();
    if (this.activeResourceTab === "model") {
      this.reloadModels();
    }
  },
  watch: {
    activePage() {
      this.syncMenuFromActivePage();
    },
    aggregatorId(value) {
      if (!this.isOwner || !value || value === this.scopeAggregatorId) {
        return;
      }
      this.scopeAggregatorId = value;
      this.enterpriseFilters.aggregatorId = value;
      this.deviceFilters.aggregatorId = value;
      this.deviceFilters.entId = "";
      this.enterprisePage.pageIndex = 1;
      this.devicePage.pageIndex = 1;
      if (this.activeResourceTab === "enterprise") {
        this.reloadEnterprises();
      } else if (this.activeResourceTab === "device") {
        this.reloadEnterprises();
        this.reloadDevices();
      }
    },
  },
  methods: {
    syncMenuFromActivePage() {
      if (this.activePage === 'load-resources') {
        this.activeResourceTab = "enterprise";
      }
    },
    switchResourceTab(tabKey) {
      this.activeResourceTab = tabKey;
      if (tabKey === "enterprise") {
        this.reloadEnterprises();
      } else if (tabKey === "model") {
        this.reloadModels();
      } else if (tabKey === "device") {
        this.reloadEnterprises();
        this.reloadDevices();
      }
    },
    defaultEnterpriseForm() {
      return {
        id: null,
        aggregatorId: this.defaultAggregatorId(this.enterpriseFilters.aggregatorId),
        entId: "",
        stationId: "",
        entName: "",
        status: 1,
        longitude: "",
        latitude: "",
        percent: undefined,
        serviceStartDate: "",
        serviceEndDate: "",
        stateGridCode: "",
        stateGridName: "",
        installCap: undefined,
        planRunStatus: 1,
      };
    },
    defaultDeviceForm() {
      return {
        id: null,
        aggregatorId: this.defaultAggregatorId(this.deviceFilters.aggregatorId),
        entId: "",
        projectId: undefined,
        deviceCode: "",
        deviceName: "",
        deviceTypeCode: "METE",
        deviceTypeName: "电表",
        manufacturer: "",
        model: "",
        assetStatus: 1,
        onlineStatus: 0,
      };
    },
    defaultModelForm() {
      return {
        id: null,
        modelName: "",
        modelType: "",
        description: "",
        status: 1,
      };
    },
    enterpriseOptionLabel(item) {
      if (!item) return "";
      return item.entName ? `${item.entName} (${item.entId})` : item.entId;
    },
    defaultAggregatorId(selectedAggregatorId) {
      return selectedAggregatorId || this.scopeAggregatorId;
    },
    reloadEnterprises() {
      this.enterpriseLoading = true;
      this.enterpriseFilters.aggregatorId = this.scopeAggregatorId;
      const params = {
        ...this.enterpriseFilters,
        pageIndex: this.enterprisePage.pageIndex,
        pageSize: this.enterprisePage.pageSize,
      };
      listEnterprises(params)
        .then((res) => {
          const page = this.unwrapPage(res);
          this.enterprises = (page.list || []).filter(item => item != null);
          this.enterprisePage.total = page.total || 0;
        })
        .catch((error) => {
          console.error("加载企业列表失败:", error);
          this.enterprises = [];
          this.enterprisePage.total = 0;
        })
        .finally(() => {
          this.enterpriseLoading = false;
        });
    },
    resetEnterpriseFilters() {
      this.enterpriseFilters = {
        aggregatorId: this.scopeAggregatorId,
        entId: "",
        entName: "",
        status: undefined,
      };
      this.enterprisePage.pageIndex = 1;
      this.reloadEnterprises();
    },
    resetDeviceFilters() {
      this.deviceFilters = {
        aggregatorId: this.scopeAggregatorId,
        entId: "",
        deviceName: "",
        deviceTypeCode: "",
      };
      this.devicePage.pageIndex = 1;
      this.reloadDevices();
    },
    resetModelFilters() {
      this.modelFilters = {
        modelName: "",
        modelType: "",
      };
      this.modelPage.pageIndex = 1;
      this.reloadModels();
    },
    handleEnterpriseSizeChange(size) {
      this.enterprisePage.pageSize = size;
      this.enterprisePage.pageIndex = 1;
      this.reloadEnterprises();
    },
    handleDeviceSizeChange(size) {
      this.devicePage.pageSize = size;
      this.devicePage.pageIndex = 1;
      this.reloadDevices();
    },
    handleModelSizeChange(size) {
      this.modelPage.pageSize = size;
      this.modelPage.pageIndex = 1;
      this.reloadModels();
    },
    reloadModels() {
      this.modelLoading = true;
      // 模拟数据，实际需要调用API
      setTimeout(() => {
        this.models = [
          {
            id: 1,
            modelName: "电表模型",
            modelType: "METE",
            description: "标准电表设备模型",
            status: 1,
            createTime: "2024-01-01 10:00:00",
          },
          {
            id: 2,
            modelName: "储能模型",
            modelType: "STORAGE",
            description: "储能设备模型",
            status: 1,
            createTime: "2024-01-02 10:00:00",
          },
        ];
        this.modelPage.total = 2;
        this.modelLoading = false;
      }, 300);
    },
    openModelDialog(mode, row) {
      this.modelDialog.mode = mode;
      this.modelDialog.title = mode === "create" ? "新增模型" : "编辑模型";
      this.modelForm =
        mode === "create"
          ? this.defaultModelForm()
          : { ...this.defaultModelForm(), ...row };
      this.modelDialog.visible = true;
    },
    submitModel() {
      if (!this.modelForm.modelName || !this.modelForm.modelType) {
        this.$message.warning("模型名称和类型不能为空");
        return;
      }
      this.modelDialog.loading = true;
      // 模拟保存，实际需要调用API
      setTimeout(() => {
        this.$message.success("保存成功");
        this.modelDialog.visible = false;
        this.modelDialog.loading = false;
        this.reloadModels();
      }, 500);
    },
    removeModel(row) {
      this.$confirm(`确认删除模型 ${row.modelName}？`, "删除模型", {
        type: "warning",
      }).then(() => {
        this.$message.success("已删除");
        this.reloadModels();
      });
    },
    openEnterpriseDialog(mode, row) {
      this.enterpriseDialog.mode = mode;
      this.enterpriseDialog.title = mode === "create" ? "新增企业" : "编辑企业";
      this.enterpriseForm = this.defaultEnterpriseForm();
      this.enterpriseServiceRange = [];
      if (mode === "edit" && row) {
        this.enterpriseForm = { ...this.defaultEnterpriseForm(), ...row };
        if (row.serviceStartDate && row.serviceEndDate) {
          this.enterpriseServiceRange = [
            row.serviceStartDate,
            row.serviceEndDate,
          ];
        }
      }
      this.enterpriseDialog.visible = true;
    },
    handleEnterpriseServiceRange(value) {
      const range = value || [];
      this.enterpriseForm.serviceStartDate = range[0] || "";
      this.enterpriseForm.serviceEndDate = range[1] || "";
    },
    submitEnterprise() {
      if (!this.isOwner) {
        this.enterpriseForm.aggregatorId = this.scopeAggregatorId;
      }
      if (!this.enterpriseForm.aggregatorId) {
        this.$message.warning("聚合商ID不能为空");
        return;
      }
      if (!this.enterpriseForm.entId) {
        this.$message.warning("企业ID不能为空");
        return;
      }
      if (!this.enterpriseForm.entName) {
        this.$message.warning("企业名称不能为空");
        return;
      }
      this.enterpriseDialog.loading = true;
      const request =
        this.enterpriseDialog.mode === "create"
          ? createEnterprise(this.enterpriseForm)
          : updateEnterprise(this.enterpriseForm.entId, this.enterpriseForm);
      request
        .then((res) => {
          this.ensureSuccess(res);
          this.$message.success("保存成功");
          this.enterpriseDialog.visible = false;
          this.reloadEnterprises();
        })
        .finally(() => {
          this.enterpriseDialog.loading = false;
        });
    },
    removeEnterprise(row) {
      this.$confirm(`确认停用企业 ${row.entName || row.entId}？`, "停用企业", {
        type: "warning",
      }).then(() => {
        disableEnterprise(row.entId).then((res) => {
          this.ensureSuccess(res);
          this.$message.success("已停用");
          this.reloadEnterprises();
        });
      });
    },
    handleDeviceEntChange(entId) {
      const ent = this.enterprises.find((item) => item && item.entId === entId);
      if (ent) {
        this.deviceForm.aggregatorId = ent.aggregatorId;
      } else if (!this.isOwner) {
        this.deviceForm.aggregatorId = this.scopeAggregatorId;
      }
    },
    reloadDevices() {
      this.deviceLoading = true;
      if (!this.isOwner) {
        this.deviceFilters.aggregatorId = this.scopeAggregatorId;
      }
      const params = {
        ...this.deviceFilters,
        pageIndex: this.devicePage.pageIndex,
        pageSize: this.devicePage.pageSize,
      };
      listDevices(params)
        .then((res) => {
          const page = this.unwrapPage(res);
          this.devices = page.list || [];
          this.devicePage.total = page.total || 0;
        })
        .catch((error) => {
          console.error("加载设备列表失败:", error);
          this.devices = [];
          this.devicePage.total = 0;
        })
        .finally(() => {
          this.deviceLoading = false;
        });
    },
    openDeviceDialog(mode, row) {
      this.deviceDialog.mode = mode;
      this.deviceDialog.title = mode === "create" ? "新增设备" : "编辑设备";
      this.deviceForm = this.defaultDeviceForm();
      if (mode === "create") {
        this.deviceForm.aggregatorId = this.defaultAggregatorId(this.deviceFilters.aggregatorId);
        this.deviceForm.entId = this.deviceFilters.entId;
        this.handleDeviceEntChange(this.deviceForm.entId);
      } else if (row) {
        this.deviceForm = {
          ...this.defaultDeviceForm(),
          ...row,
        };
      }
      if (!this.isOwner) {
        this.deviceForm.aggregatorId = this.scopeAggregatorId;
      }
      this.deviceDialog.visible = true;
    },
    submitDevice() {
      if (!this.isOwner) {
        this.deviceForm.aggregatorId = this.scopeAggregatorId;
      }
      if (!this.deviceForm.entId) {
        this.$message.warning("企业ID不能为空");
        return;
      }
      if (!this.deviceForm.deviceName) {
        this.$message.warning("设备名称不能为空");
        return;
      }
      this.deviceDialog.loading = true;
      const request =
        this.deviceDialog.mode === "create"
          ? createDevice(this.deviceForm)
          : updateDevice(this.deviceForm.id, this.deviceForm);
      request
        .then((res) => {
          this.ensureSuccess(res);
          this.$message.success("保存成功");
          this.deviceDialog.visible = false;
          this.reloadDevices();
        })
        .finally(() => {
          this.deviceDialog.loading = false;
        });
    },
    removeDevice(row) {
      this.$confirm(`确认删除设备 ${row.deviceCode || row.deviceName}？`, "删除设备", {
        type: "warning",
      }).then(() => {
        deleteDevice(row.id).then((res) => {
          this.ensureSuccess(res);
          this.$message.success("已删除");
          this.reloadDevices();
        });
      });
    },
    unwrapData(res, fallback) {
      const body = res && res.data ? res.data : {};
      if (body.code && body.code !== 200) {
        this.$message.error(body.msg || "请求失败");
        return fallback;
      }
      return body.data === undefined || body.data === null
        ? fallback
        : body.data;
    },
    unwrapPage(res) {
      const data = this.unwrapData(res, {});
      const list = data.list || [];
      return {
        list: Array.isArray(list) ? list.filter(item => item != null) : [],
        total: data.total || 0,
      };
    },
    ensureSuccess(res) {
      const body = res && res.data ? res.data : {};
      if (body.code && body.code !== 200) {
        throw new Error(body.msg || "请求失败");
      }
    },
  },
};
</script>

<style lang="less" scoped>
.iot-resource-page {
  min-height: 100%;
  color: #1f2933;
}

.management-content {
  padding: 20px;
  border: 1px solid #dde6ed;
  border-radius: 8px;
  background: #ffffff;
}

.resource-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.resource-tabs button {
  height: 34px;
  padding: 0 14px;
  border: 1px solid #cfdce5;
  border-radius: 6px;
  background: #f7fbff;
  color: #334e5c;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.resource-tabs button.active,
.resource-tabs button:hover {
  border-color: #0780ed;
  background: #0780ed;
  color: #ffffff;
}

.tab-content {
  min-width: 0;
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
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
  border: 1px solid #e7e3da;
  border-radius: 6px;
  background: #fafafa;
}

.filter-panel ::v-deep .el-form-item {
  margin-bottom: 0;
}

.filter-panel ::v-deep .el-input,
.filter-panel ::v-deep .el-select {
  width: 200px;
}

.data-table ::v-deep .el-table {
  border: 1px solid #dde6ed;
}

.data-table ::v-deep .el-table th {
  background: #f5f7fa;
  color: #0e2638;
  font-weight: 600;
}

.table-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.enterprise-form .inline-fields {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

::v-deep .el-dialog {
  border-radius: 8px;
}

@media (max-width: 900px) {
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
  .filter-panel ::v-deep .el-select {
    width: 100%;
  }

  .enterprise-form .inline-fields {
    grid-template-columns: 1fr;
  }
}
</style>
