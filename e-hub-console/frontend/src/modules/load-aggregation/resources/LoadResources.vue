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
              prop="installCap"
              label="装机容量(kW)"
              min-width="120"
            />
            <el-table-column
              label="经纬度"
              min-width="140"
            >
              <template slot-scope="{ row }">
                {{ formatLngLat(row) }}
              </template>
            </el-table-column>
            <el-table-column
              prop="allowApplyTime"
              label="允许申报时间"
              min-width="140"
            />
            <el-table-column
              prop="percent"
              label="企业用户占比"
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
            <el-form-item label="企业">
              <el-select
                v-model="modelFilters.entId"
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
            <el-form-item label="项目名称">
              <el-input
                v-model.trim="modelFilters.energyStation"
                clearable
                placeholder="请输入项目名称"
              />
            </el-form-item>
            <el-form-item label="资源类型">
              <el-select
                v-model="modelFilters.resourceTypeId"
                clearable
                filterable
                placeholder="全部"
              >
                <el-option
                  v-for="item in modelResourceTypeOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
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
            <el-table-column prop="entName" label="企业名称" min-width="180" />
            <el-table-column prop="entId" label="企业ID" min-width="140" />
            <el-table-column
              prop="energyStation"
              label="项目名称"
              min-width="180"
            />
            <el-table-column
              prop="energyStationCode"
              label="项目编码"
              min-width="160"
            />
            <el-table-column
              prop="resourceTypeId"
              label="资源类型"
              min-width="120"
            />
            <el-table-column
              prop="powerCap"
              label="容量(kW)"
              min-width="120"
            />
            <el-table-column prop="area" label="区域" min-width="120" />
            <el-table-column prop="userType" label="用户类型" min-width="120" />
            <el-table-column
              prop="deviceManufacture"
              label="设备制造商"
              min-width="140"
            />
            <el-table-column prop="saveHeat" label="蓄热方式" min-width="120" />
            <el-table-column prop="controll" label="是否参与" width="100">
              <template slot-scope="{ row }">
                <el-tag
                  size="mini"
                  :type="isControllable(row.controll) ? 'success' : 'info'"
                >
                  {{ isControllable(row.controll) ? "参与" : "不参与" }}
                </el-tag>
              </template>
            </el-table-column>
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
            <el-form-item label="项目名称">
              <el-input
                v-model.trim="deviceFilters.energyStation"
                clearable
                placeholder="请输入项目名称"
              />
            </el-form-item>
            <el-form-item label="设备名称">
              <el-input
                v-model.trim="deviceFilters.deviceName"
                clearable
                placeholder="请输入设备名称"
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
            <el-table-column prop="username" label="企业名称" min-width="160" />
            <el-table-column
              prop="energyStation"
              label="项目名称"
              min-width="180"
            />
            <el-table-column
              prop="deviceName"
              label="设备名称"
              min-width="160"
            />
            <el-table-column
              prop="resourceTypeName"
              label="资源类型"
              min-width="100"
            />
            <el-table-column prop="status" label="状态" width="90">
              <template slot-scope="{ row }">
                <el-tag
                  size="mini"
                  :type="row.status === 1 ? 'success' : 'info'"
                >
                  {{ row.status === 1 ? "启用" : "停用" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="modelFlag" label="参与状态" width="90">
              <template slot-scope="{ row }">
                <el-tag
                  size="mini"
                  :type="row.modelFlag === 1 ? 'success' : 'info'"
                >
                  {{ row.modelFlag === 1 ? "参与" : "不参与" }}
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
        <el-form-item label="企业" required>
          <el-select
            v-model="modelForm.entId"
            filterable
            placeholder="请选择企业"
            @change="handleModelEntChange"
          >
            <el-option
              v-for="item in deviceFormEnterpriseOptions"
              :key="item.entId"
              :label="enterpriseOptionLabel(item)"
              :value="item.entId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="项目名称" required>
          <el-input v-model.trim="modelForm.energyStation" />
        </el-form-item>
        <el-form-item label="项目编码">
          <el-input
            v-model.trim="modelForm.energyStationCode"
            disabled
            placeholder="自动生成"
          />
        </el-form-item>
        <el-form-item label="资源类型" required>
          <el-select
            v-model="modelForm.resourceTypeId"
            filterable
            placeholder="请选择资源类型"
          >
            <el-option
              v-for="item in modelDialogResourceTypeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="容量(kW)">
          <el-input v-model.trim="modelForm.powerCap" />
        </el-form-item>
        <el-form-item label="区域">
          <el-input v-model.trim="modelForm.area" />
        </el-form-item>
        <el-form-item label="用户类型">
          <el-input v-model.trim="modelForm.userType" />
        </el-form-item>
        <el-form-item label="制造商">
          <el-input v-model.trim="modelForm.deviceManufacture" />
        </el-form-item>
        <el-form-item label="蓄热方式">
          <el-input v-model.trim="modelForm.saveHeat" />
        </el-form-item>
        <el-form-item label="是否参与">
          <el-radio-group v-model="modelForm.controll">
            <el-radio label="1">参与</el-radio>
            <el-radio label="0">不参与</el-radio>
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
        <el-form-item v-if="enterpriseDialog.mode === 'edit'" label="企业ID">
          <el-input
            v-model.trim="enterpriseForm.entId"
            disabled
            placeholder="entId"
          />
        </el-form-item>
        <el-form-item label="企业名称" required>
          <el-input v-model.trim="enterpriseForm.entName" />
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
        <el-form-item label="允许申报时间">
          <el-input
            v-model.trim="enterpriseForm.allowApplyTime"
            placeholder="例如 08:55"
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
        <el-form-item label="项目">
          <el-select
            v-model="deviceForm.energyStationCode"
            filterable
            clearable
            placeholder="请选择项目"
          >
            <el-option
              v-for="item in deviceProjectOptions"
              :key="item.energyStationCode"
              :label="item.energyStation"
              :value="item.energyStationCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备名称" required>
          <el-input v-model.trim="deviceForm.deviceName" />
        </el-form-item>
        <el-form-item label="资源类型">
          <el-select
            v-model="deviceForm.resourceTypeId"
            filterable
            clearable
            placeholder="请选择资源类型"
          >
            <el-option
              v-for="item in modelResourceTypeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="deviceForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="参与状态">
          <el-radio-group v-model="deviceForm.modelFlag">
            <el-radio :label="1">参与</el-radio>
            <el-radio :label="0">不参与</el-radio>
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
  createModel,
  deleteModel,
  disableEnterprise,
  deleteDevice,
  listEnterprises,
  listDevices,
  listModels,
  listProjectsByEnt,
  listResourceTypes,
  updateModel,
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
        aggregatorId: fixedAggregatorId,
        entId: "",
        resourceTypeId: "",
        energyStationCode: "",
        energyStation: "",
      },
      models: [],
      modelLoading: false,
      modelPage: {
        pageIndex: 1,
        pageSize: 20,
        total: 0,
      },
      modelResourceTypeOptions: [],
      deviceFilters: {
        entId: "",
        deviceName: "",
        energyStation: "",
      },
      devices: [],
      deviceLoading: false,
      devicePage: {
        pageIndex: 1,
        pageSize: 20,
        total: 0,
      },
      enterpriseServiceRange: [],
      deviceProjectOptions: [],
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
      return this.validEnterprises;
    },
    deviceFormEnterpriseOptions() {
      return this.validEnterprises;
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
      this.loadModelResourceTypes();
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
      this.modelFilters.aggregatorId = value;
      this.modelFilters.entId = "";
      this.deviceFilters.entId = "";
      this.enterprisePage.pageIndex = 1;
      this.modelPage.pageIndex = 1;
      this.devicePage.pageIndex = 1;
      if (this.activeResourceTab === "enterprise") {
        this.reloadEnterprises();
      } else if (this.activeResourceTab === "device") {
        this.reloadEnterprises();
        this.reloadDevices();
      } else if (this.activeResourceTab === "model") {
        this.reloadEnterprises();
        this.loadModelResourceTypes();
        this.reloadModels();
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
        this.reloadEnterprises();
        this.loadModelResourceTypes();
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
        entName: "",
        status: 1,
        longitude: "",
        latitude: "",
        percent: undefined,
        allowApplyTime: "",
        installCap: undefined,
      };
    },
    defaultDeviceForm() {
      return {
        id: null,
        entId: "",
        energyStationCode: "",
        deviceName: "",
        resourceTypeId: "",
        status: 1,
        modelFlag: 1,
      };
    },
    defaultModelForm() {
      return {
        id: null,
        aggregatorId: this.defaultAggregatorId(this.modelFilters.aggregatorId),
        entId: "",
        resourceTypeId: "",
        energyStation: "",
        energyStationCode: "",
        powerCap: "",
        area: "",
        userType: "",
        deviceManufacture: "",
        saveHeat: "",
        controll: "1",
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
        entId: "",
        deviceName: "",
        energyStation: "",
      };
      this.devicePage.pageIndex = 1;
      this.reloadDevices();
    },
    resetModelFilters() {
      this.modelFilters = {
        aggregatorId: this.scopeAggregatorId,
        entId: "",
        resourceTypeId: "",
        energyStation: "",
      };
      this.modelPage.pageIndex = 1;
      this.loadModelResourceTypes();
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
      if (!this.isOwner) {
        this.modelFilters.aggregatorId = this.scopeAggregatorId;
      }
      const params = {
        ...this.modelFilters,
        pageIndex: this.modelPage.pageIndex,
        pageSize: this.modelPage.pageSize,
      };
      listModels(params)
        .then((res) => {
          const page = this.unwrapPage(res);
          this.models = page.list || [];
          this.modelPage.total = page.total || 0;
        })
        .catch((error) => {
          console.error("加载模型列表失败:", error);
          this.models = [];
          this.modelPage.total = 0;
        })
        .finally(() => {
          this.modelLoading = false;
        });
    },
    loadModelResourceTypes(entId) {
      const aggregatorId = this.scopeAggregatorId;
      if (!aggregatorId) {
        this.modelResourceTypeOptions = [];
        return Promise.resolve();
      }
      return listResourceTypes({
        aggregatorId,
        entId: entId || this.modelFilters.entId || "",
      })
        .then((res) => {
          const list = this.unwrapData(res, []);
          this.modelResourceTypeOptions = Array.isArray(list) ? list : [];
        })
        .catch((error) => {
          console.error("加载资源类型失败:", error);
          this.modelResourceTypeOptions = [];
        });
    },
    openModelDialog(mode, row) {
      this.modelDialog.mode = mode;
      this.modelDialog.title = mode === "create" ? "新增模型" : "编辑模型";
      this.modelForm = this.defaultModelForm();
      if (mode === "create") {
        this.modelForm.aggregatorId = this.defaultAggregatorId(this.modelFilters.aggregatorId);
        this.modelForm.entId = this.modelFilters.entId;
      } else if (row) {
        this.modelForm = { ...this.defaultModelForm(), ...row };
      }
      if (!this.isOwner) {
        this.modelForm.aggregatorId = this.scopeAggregatorId;
      }
      this.loadModelResourceTypes(this.modelForm.entId);
      this.modelDialog.visible = true;
    },
    handleModelEntChange(entId) {
      const ent = this.enterprises.find((item) => item && item.entId === entId);
      if (ent) {
        this.modelForm.aggregatorId = ent.aggregatorId;
      } else if (!this.isOwner) {
        this.modelForm.aggregatorId = this.scopeAggregatorId;
      }
      this.modelForm.resourceTypeId = "";
      this.loadModelResourceTypes(entId);
    },
    submitModel() {
      if (!this.isOwner) {
        this.modelForm.aggregatorId = this.scopeAggregatorId;
      }
      if (!this.modelForm.entId) {
        this.$message.warning("企业不能为空");
        return;
      }
      if (!this.modelForm.energyStation) {
        this.$message.warning("项目名称不能为空");
        return;
      }
      if (!this.modelForm.resourceTypeId) {
        this.$message.warning("资源类型不能为空");
        return;
      }
      this.modelDialog.loading = true;
      const payload = { ...this.modelForm };
      delete payload.entName;
      const request =
        this.modelDialog.mode === "create"
          ? createModel(payload)
          : updateModel(this.modelForm.id, payload);
      request
        .then((res) => {
          this.ensureSuccess(res);
          this.$message.success("保存成功");
          this.modelDialog.visible = false;
          this.reloadModels();
        })
        .finally(() => {
          this.modelDialog.loading = false;
        });
    },
    removeModel(row) {
      this.$confirm(`确认删除项目 ${row.energyStation || row.energyStationCode}？`, "删除模型", {
        type: "warning",
      }).then(() => {
        deleteModel(row.id).then((res) => {
          this.ensureSuccess(res);
          this.$message.success("已删除");
          this.reloadModels();
        });
      });
    },
    openEnterpriseDialog(mode, row) {
      this.enterpriseDialog.mode = mode;
      this.enterpriseDialog.title = mode === "create" ? "新增企业" : "编辑企业";
      this.enterpriseForm = this.defaultEnterpriseForm();
      if (mode === "edit" && row) {
        this.enterpriseForm = { ...this.defaultEnterpriseForm(), ...row };
      }
      this.enterpriseDialog.visible = true;
    },
    submitEnterprise() {
      if (!this.isOwner) {
        this.enterpriseForm.aggregatorId = this.scopeAggregatorId;
      }
      if (!this.enterpriseForm.aggregatorId) {
        this.$message.warning("聚合商ID不能为空");
        return;
      }
      if (this.enterpriseDialog.mode === "edit" && !this.enterpriseForm.entId) {
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
      this.deviceForm.energyStationCode = "";
      this.deviceProjectOptions = [];
      if (entId) {
        this.loadDeviceProjects(entId);
      }
    },
    loadDeviceProjects(entId) {
      listProjectsByEnt(entId)
        .then((res) => {
          this.deviceProjectOptions = this.unwrapData(res, []);
        })
        .catch(() => {
          this.deviceProjectOptions = [];
        });
    },
    reloadDevices() {
      this.deviceLoading = true;
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
      this.deviceProjectOptions = [];
      this.loadModelResourceTypes();
      if (mode === "create") {
        this.deviceForm.entId = this.deviceFilters.entId;
        if (this.deviceForm.entId) {
          this.loadDeviceProjects(this.deviceForm.entId);
        }
      } else if (row) {
        this.deviceForm = {
          ...this.defaultDeviceForm(),
          ...row,
        };
        if (row.entId) {
          this.loadDeviceProjects(row.entId);
        }
      }
      this.deviceDialog.visible = true;
    },
    submitDevice() {
      if (!this.deviceForm.entId) {
        this.$message.warning("企业不能为空");
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
      this.$confirm(`确认删除设备 ${row.deviceName}？`, "删除设备", {
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
    formatLngLat(row) {
      if (!row) {
        return "-";
      }
      const longitude = row.longitude || "-";
      const latitude = row.latitude || "-";
      return `${longitude}, ${latitude}`;
    },
    isControllable(value) {
      return String(value) === "1";
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
