<template>
  <div class="iot-resource-page">
    <section class="domain-nav">
      <button
        v-for="item in domainItems"
        :key="item.key"
        type="button"
        :class="{ active: activeDomain === item.key }"
        @click="switchDomain(item.key)"
      >
        <span>{{ item.title }}</span>
        <small>{{ item.desc }}</small>
      </button>
    </section>

    <section v-if="activeDomain === 'enterprise'" class="domain-section">
      <div class="section-head">
        <div>
          <p>企业管理</p>
          <span>维护聚合商下企业档案，替代 SQL 手工录入</span>
        </div>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openEnterpriseDialog('create')">
          新增企业
        </el-button>
      </div>

      <section class="resource-toolbar">
      <el-form
        :inline="true"
        :model="enterpriseFilters"
        size="small"
        class="filter-form"
      >
        <el-form-item label="聚合商">
          <el-input v-model.trim="enterpriseFilters.aggregatorId" clearable placeholder="aggregatorId" />
        </el-form-item>
        <el-form-item label="企业ID">
          <el-input v-model.trim="enterpriseFilters.entId" clearable placeholder="entId" />
        </el-form-item>
        <el-form-item label="企业名称">
          <el-input v-model.trim="enterpriseFilters.entName" clearable placeholder="名称关键字" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="enterpriseFilters.status" clearable placeholder="全部">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="reloadEnterprises">查询</el-button>
          <el-button icon="el-icon-refresh" @click="resetEnterpriseFilters">重置</el-button>
        </el-form-item>
      </el-form>
      </section>

      <section class="resource-summary compact">
        <div class="summary-item">
          <span>企业总数</span>
          <strong>{{ enterprisePage.total || 0 }}</strong>
        </div>
        <div class="summary-item">
          <span>当前企业</span>
          <strong>{{ selectedEnterprise ? selectedEnterprise.entName : "-" }}</strong>
        </div>
        <div class="summary-item">
          <span>当前企业ID</span>
          <strong>{{ selectedEnterprise ? selectedEnterprise.entId : "-" }}</strong>
        </div>
      </section>

      <section class="data-panel">
        <div class="table-actions">
          <span>企业列表</span>
          <el-button size="mini" icon="el-icon-refresh" @click="reloadEnterprises">刷新</el-button>
        </div>
        <el-table
          v-loading="enterpriseLoading"
          :data="enterprises"
          size="small"
          height="520"
          stripe
          highlight-current-row
          @current-change="handleEnterpriseCurrentChange"
        >
          <el-table-column prop="entName" label="企业名称" min-width="220" fixed />
          <el-table-column prop="entId" label="企业ID" min-width="150" />
          <el-table-column prop="aggregatorId" label="聚合商ID" min-width="150" />
          <el-table-column prop="stationId" label="企业编码" min-width="120" />
          <el-table-column prop="installCap" label="装机容量" min-width="100" />
          <el-table-column prop="stateGridName" label="电网名称" min-width="140" />
          <el-table-column prop="serviceStartDate" label="服务开始" min-width="120" />
          <el-table-column prop="serviceEndDate" label="服务结束" min-width="120" />
          <el-table-column prop="status" label="状态" width="80">
            <template slot-scope="{ row }">
              <el-tag size="mini" :type="row.status === 1 ? 'success' : 'info'">
                {{ row.status === 1 ? "启用" : "停用" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="planRunStatus" label="按计划运行" width="100">
            <template slot-scope="{ row }">
              <el-tag size="mini" :type="row.planRunStatus === 1 ? 'success' : 'info'">
                {{ row.planRunStatus === 1 ? "是" : "否" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="190" fixed="right">
            <template slot-scope="{ row }">
              <el-button type="text" size="mini" @click="viewEnterpriseDevices(row)">查看设备</el-button>
              <el-button type="text" size="mini" @click="openEnterpriseDialog('edit', row)">编辑</el-button>
              <el-button type="text" size="mini" @click="removeEnterprise(row)">停用</el-button>
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
      </section>
    </section>

    <section v-if="activeDomain === 'device'" class="domain-section">
      <div class="section-head">
        <div>
          <p>设备管理</p>
          <span>维护设备资产，并在设备详情中管理属性、测点和绑定关系</span>
        </div>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openDeviceDialog('create')">
          新增设备
        </el-button>
      </div>

      <section class="resource-toolbar">
      <el-form :inline="true" :model="filters" size="small" class="filter-form">
        <el-form-item label="聚合商">
          <el-input v-model.trim="filters.aggregatorId" clearable placeholder="aggregatorId" />
        </el-form-item>
        <el-form-item label="企业">
          <el-select v-model="filters.entId" clearable filterable placeholder="选择企业">
            <el-option
              v-for="item in enterprises"
              :key="item.entId"
              :label="enterpriseOptionLabel(item)"
              :value="item.entId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备">
          <el-input v-model.trim="filters.deviceName" clearable placeholder="名称/编码" />
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model.trim="filters.deviceTypeCode" clearable placeholder="METE" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="reloadDevices">查询</el-button>
          <el-button icon="el-icon-refresh" @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
      </section>

      <section class="resource-summary compact">
      <div class="summary-item">
        <span>设备总数</span>
        <strong>{{ devicePage.total || 0 }}</strong>
      </div>
      <div class="summary-item">
        <span>在线设备</span>
        <strong>{{ onlineCount }}</strong>
      </div>
      <div class="summary-item">
        <span>当前企业</span>
        <strong>{{ selectedEnterprise ? selectedEnterprise.entName : "-" }}</strong>
      </div>
      <div class="summary-item">
        <span>当前设备</span>
        <strong>{{ selectedDevice ? selectedDevice.deviceCode : "-" }}</strong>
      </div>
      </section>

      <section class="device-domain-layout">
        <aside class="device-list-panel">
          <div class="table-actions">
            <span>设备列表</span>
          <el-button type="text" size="mini" @click="reloadDevices">刷新</el-button>
        </div>
            <el-table
              v-loading="deviceLoading"
              :data="devices"
              size="small"
              height="560"
              stripe
              highlight-current-row
              @current-change="handleDeviceCurrentChange"
            >
              <el-table-column prop="deviceCode" label="设备编码" min-width="120" fixed />
              <el-table-column prop="deviceName" label="设备名称" min-width="160" />
              <el-table-column prop="entId" label="企业ID" min-width="120" />
              <el-table-column prop="projectId" label="项目ID" min-width="90" />
              <el-table-column prop="deviceTypeCode" label="类型" min-width="90" />
              <el-table-column prop="manufacturer" label="厂商" min-width="120" />
              <el-table-column prop="onlineStatus" label="在线" width="80">
                <template slot-scope="{ row }">
                  <el-tag size="mini" :type="row.onlineStatus === 1 ? 'success' : 'info'">
                    {{ row.onlineStatus === 1 ? "在线" : "离线" }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="lastDataTime" label="最近数据时间" min-width="160" />
              <el-table-column label="操作" width="150" fixed="right">
                <template slot-scope="{ row }">
                  <el-button type="text" size="mini" @click="openDeviceDialog('edit', row)">编辑</el-button>
                  <el-button type="text" size="mini" @click="removeDevice(row)">删除</el-button>
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
        </aside>

        <main class="device-detail-panel">
          <div class="device-detail-head">
            <div>
              <p>{{ selectedDevice ? selectedDevice.deviceName : "未选择设备" }}</p>
              <span>{{ selectedDevice ? selectedDevice.deviceCode : "请从左侧设备列表选择设备" }}</span>
            </div>
            <el-button
              size="mini"
              icon="el-icon-edit"
              :disabled="!selectedDevice"
              @click="openDeviceDialog('edit', selectedDevice)"
            >
              编辑设备
            </el-button>
          </div>
          <el-tabs v-model="deviceDetailTab" class="detail-tabs">
            <el-tab-pane label="基础属性" name="profile">
              <div v-if="selectedDevice" class="device-profile">
                <div><span>所属企业</span><strong>{{ enterpriseNameMap[selectedDevice.entId] || selectedDevice.entId }}</strong></div>
                <div><span>设备编码</span><strong>{{ selectedDevice.deviceCode }}</strong></div>
                <div><span>设备类型</span><strong>{{ selectedDevice.deviceTypeName || selectedDevice.deviceTypeCode || "-" }}</strong></div>
                <div><span>项目ID</span><strong>{{ selectedDevice.projectId || "-" }}</strong></div>
                <div><span>厂商</span><strong>{{ selectedDevice.manufacturer || "-" }}</strong></div>
                <div><span>型号</span><strong>{{ selectedDevice.model || "-" }}</strong></div>
                <div><span>资产状态</span><strong>{{ selectedDevice.assetStatus === 1 ? "启用" : "停用" }}</strong></div>
                <div><span>在线状态</span><strong>{{ selectedDevice.onlineStatus === 1 ? "在线" : "离线" }}</strong></div>
                <div><span>最近数据时间</span><strong>{{ selectedDevice.lastDataTime || "-" }}</strong></div>
              </div>
              <div v-else class="empty-state">请选择设备</div>
            </el-tab-pane>

            <el-tab-pane label="测点配置" name="points">
            <div class="table-actions">
                <span>测点列表</span>
              <el-button
                size="mini"
                type="primary"
                icon="el-icon-plus"
                :disabled="!selectedDevice"
                @click="openPointDialog('create')"
              >
                新增测点
              </el-button>
            </div>
            <el-table
              v-loading="pointLoading"
              :data="points"
              size="small"
              height="430"
              stripe
              highlight-current-row
              @current-change="handlePointCurrentChange"
            >
              <el-table-column prop="pointCode" label="测点编码" min-width="140" />
              <el-table-column prop="pointName" label="测点名称" min-width="140" />
              <el-table-column prop="valueType" label="值类型" width="90" />
              <el-table-column prop="unit" label="单位" width="80" />
              <el-table-column prop="dataFrequency" label="频率(s)" width="90" />
              <el-table-column prop="requiredFlag" label="核心" width="80">
                <template slot-scope="{ row }">
                  <el-tag size="mini" :type="row.requiredFlag === 1 ? 'warning' : 'info'">
                    {{ row.requiredFlag === 1 ? "是" : "否" }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="80">
                <template slot-scope="{ row }">
                  <el-tag size="mini" :type="row.status === 1 ? 'success' : 'info'">
                    {{ row.status === 1 ? "启用" : "停用" }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template slot-scope="{ row }">
                  <el-button type="text" size="mini" @click="openPointDialog('edit', row)">编辑</el-button>
                  <el-button type="text" size="mini" @click="removePoint(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            </el-tab-pane>

            <el-tab-pane label="绑定关系" name="refs">
            <div class="binding-layout">
              <section class="binding-section">
                <div class="table-actions">
                  <span>设备标识绑定</span>
                  <el-button
                    size="mini"
                    type="primary"
                    icon="el-icon-plus"
                    :disabled="!selectedDevice"
                    @click="openDeviceRefDialog('create')"
                  >
                    新增
                  </el-button>
                </div>
                <el-table :data="deviceRefs" size="small" height="190" stripe>
                  <el-table-column prop="sourceCode" label="来源" min-width="110" />
                  <el-table-column prop="externalDeviceId" label="三方设备ID" min-width="150" />
                  <el-table-column prop="externalDeviceName" label="三方设备名" min-width="150" />
                  <el-table-column prop="status" label="状态" width="80">
                    <template slot-scope="{ row }">
                      <el-tag size="mini" :type="row.status === 1 ? 'success' : 'info'">
                        {{ row.status === 1 ? "启用" : "停用" }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="130" fixed="right">
                    <template slot-scope="{ row }">
                      <el-button type="text" size="mini" @click="openDeviceRefDialog('edit', row)">编辑</el-button>
                      <el-button type="text" size="mini" @click="disableDeviceRef(row)">停用</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </section>

              <section class="binding-section">
                <div class="table-actions">
                  <div class="point-select">
                    <span>测点标识绑定</span>
                    <el-select
                      v-model="selectedPointId"
                      size="mini"
                      placeholder="选择测点"
                      :disabled="!points.length"
                      @change="handlePointSelectChange"
                    >
                      <el-option
                        v-for="point in points"
                        :key="point.id"
                        :label="`${point.pointName} (${point.pointCode})`"
                        :value="point.id"
                      />
                    </el-select>
                  </div>
                  <el-button
                    size="mini"
                    type="primary"
                    icon="el-icon-plus"
                    :disabled="!selectedPoint"
                    @click="openPointRefDialog('create')"
                  >
                    新增
                  </el-button>
                </div>
                <el-table :data="pointRefs" size="small" height="190" stripe>
                  <el-table-column prop="sourceCode" label="来源" min-width="110" />
                  <el-table-column prop="externalMetric" label="三方测点" min-width="140" />
                  <el-table-column prop="externalMetricName" label="三方测点名" min-width="150" />
                  <el-table-column prop="ratio" label="倍率" width="80" />
                  <el-table-column prop="offsetValue" label="偏移" width="80" />
                  <el-table-column label="操作" width="130" fixed="right">
                    <template slot-scope="{ row }">
                      <el-button type="text" size="mini" @click="openPointRefDialog('edit', row)">编辑</el-button>
                      <el-button type="text" size="mini" @click="disablePointRef(row)">停用</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </section>
            </div>
            </el-tab-pane>
          </el-tabs>
        </main>
      </section>
    </section>

    <section v-if="activeDomain === 'iot-data'" class="domain-section">
      <div class="section-head">
        <div>
          <p>物联数据</p>
          <span>查看标准测点分钟数据和接入未匹配数据，排查设备上送链路</span>
        </div>
      </div>

      <section class="resource-toolbar">
        <el-form :inline="true" :model="filters" size="small" class="filter-form">
          <el-form-item label="企业">
            <el-select v-model="filters.entId" clearable filterable placeholder="选择企业" @change="reloadDevices">
              <el-option
                v-for="item in enterprises"
                :key="item.entId"
                :label="enterpriseOptionLabel(item)"
                :value="item.entId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="设备">
            <el-select
              :value="selectedDevice ? selectedDevice.id : null"
              clearable
              filterable
              placeholder="选择设备"
              @change="handleTelemetryDeviceChange"
            >
              <el-option
                v-for="item in devices"
                :key="item.id"
                :label="`${item.deviceName} (${item.deviceCode})`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="测点">
                <el-input
                  v-model.trim="telemetryQuery.pointCode"
                  clearable
                  placeholder="pointCode"
                />
          </el-form-item>
          <el-form-item label="时间">
                <el-date-picker
                  v-model="telemetryQuery.range"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  value-format="yyyy-MM-dd HH:mm:ss"
                />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="loadTelemetry">查询</el-button>
          </el-form-item>
        </el-form>
      </section>

      <section class="data-panel">
        <el-tabs v-model="iotDataTab" class="detail-tabs">
          <el-tab-pane label="标准测点数据" name="telemetry">
            <el-table v-loading="telemetryLoading" :data="telemetryRows" size="small" height="430" stripe>
              <el-table-column prop="minuteTime" label="分钟时间" min-width="160" />
              <el-table-column prop="deviceCode" label="设备编码" min-width="120" />
              <el-table-column prop="pointCode" label="测点编码" min-width="120" />
              <el-table-column prop="pointValue" label="值" min-width="100" />
              <el-table-column prop="unit" label="单位" width="80" />
              <el-table-column prop="quality" label="质量" width="100" />
              <el-table-column prop="sourceCode" label="来源" min-width="120" />
              <el-table-column prop="receiveTime" label="接收时间" min-width="160" />
            </el-table>
            <el-pagination
              class="table-pagination"
              background
              layout="total, prev, pager, next, sizes"
              :page-size="telemetryPage.pageSize"
              :current-page.sync="telemetryPage.pageIndex"
              :total="telemetryPage.total"
              :page-sizes="[50, 100, 200]"
              @current-change="loadTelemetry"
              @size-change="handleTelemetrySizeChange"
            />
          </el-tab-pane>

          <el-tab-pane label="未匹配数据" name="unmatched">
            <div class="table-actions telemetry-filter">
              <span>未匹配数据</span>
              <div>
                <el-input v-model.trim="unmatchedQuery.sourceCode" size="mini" clearable placeholder="sourceCode" />
                <el-select v-model="unmatchedQuery.handled" size="mini" clearable placeholder="状态">
                  <el-option label="未处理" :value="0" />
                  <el-option label="已处理" :value="1" />
                </el-select>
                <el-button size="mini" type="primary" icon="el-icon-search" @click="loadUnmatched">查询</el-button>
              </div>
            </div>
            <el-table v-loading="unmatchedLoading" :data="unmatchedRows" size="small" height="430" stripe>
              <el-table-column prop="sourceCode" label="来源" min-width="110" />
              <el-table-column prop="interfaceType" label="接口" min-width="100" />
              <el-table-column prop="externalProjectId" label="三方项目" min-width="140" />
              <el-table-column prop="externalDeviceId" label="三方设备" min-width="140" />
              <el-table-column prop="externalMetric" label="三方测点" min-width="140" />
              <el-table-column prop="value" label="值" min-width="90" />
              <el-table-column prop="reason" label="原因" min-width="150" />
              <el-table-column prop="dataTime" label="数据时间" min-width="160" />
              <el-table-column prop="createTime" label="接收时间" min-width="160" />
            </el-table>
            <el-pagination
              class="table-pagination"
              background
              layout="total, prev, pager, next, sizes"
              :page-size="unmatchedPage.pageSize"
              :current-page.sync="unmatchedPage.pageIndex"
              :total="unmatchedPage.total"
              :page-sizes="[20, 50, 100]"
              @current-change="loadUnmatched"
              @size-change="handleUnmatchedSizeChange"
            />
          </el-tab-pane>
        </el-tabs>
      </section>
    </section>

    <el-dialog :title="enterpriseDialog.title" :visible.sync="enterpriseDialog.visible" width="720px">
      <el-form :model="enterpriseForm" label-width="110px" size="small" class="enterprise-form">
        <el-form-item label="聚合商ID" required>
          <el-input v-model.trim="enterpriseForm.aggregatorId" placeholder="aggregatorId" />
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
          <el-input-number v-model="enterpriseForm.installCap" :min="0" :step="100" controls-position="right" />
        </el-form-item>
        <el-form-item label="经纬度">
          <div class="inline-fields">
            <el-input v-model.trim="enterpriseForm.longitude" placeholder="经度" />
            <el-input v-model.trim="enterpriseForm.latitude" placeholder="纬度" />
          </div>
        </el-form-item>
        <el-form-item label="电网信息">
          <div class="inline-fields">
            <el-input v-model.trim="enterpriseForm.stateGridCode" placeholder="电网编码" />
            <el-input v-model.trim="enterpriseForm.stateGridName" placeholder="电网名称" />
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
          <el-input-number v-model="enterpriseForm.percent" :min="0" :max="100" :step="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="按计划运行">
          <el-switch v-model="enterpriseForm.planRunStatus" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="enterpriseForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button size="small" @click="enterpriseDialog.visible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="enterpriseDialog.loading" @click="submitEnterprise">
          保存
        </el-button>
      </span>
    </el-dialog>

    <el-dialog :title="deviceDialog.title" :visible.sync="deviceDialog.visible" width="620px">
      <el-form :model="deviceForm" label-width="96px" size="small">
        <el-form-item label="聚合商">
          <el-input v-model.trim="deviceForm.aggregatorId" placeholder="aggregatorId" />
        </el-form-item>
        <el-form-item label="企业" required>
          <el-select v-model="deviceForm.entId" filterable placeholder="请选择企业" @change="handleDeviceEntChange">
            <el-option
              v-for="item in enterprises"
              :key="item.entId"
              :label="enterpriseOptionLabel(item)"
              :value="item.entId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="项目ID">
          <el-input-number v-model="deviceForm.projectId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="设备编码">
          <el-input v-model.trim="deviceForm.deviceCode" placeholder="为空时自动生成" />
        </el-form-item>
        <el-form-item label="设备名称" required>
          <el-input v-model.trim="deviceForm.deviceName" />
        </el-form-item>
        <el-form-item label="设备类型">
          <el-input v-model.trim="deviceForm.deviceTypeCode" placeholder="METE" />
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
        <el-form-item v-if="deviceDialog.mode === 'create'" label="默认测点">
          <el-checkbox v-model="deviceForm.createDefaultPowerPoint">有功功率 active_power</el-checkbox>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button size="small" @click="deviceDialog.visible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="deviceDialog.loading" @click="submitDevice">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog :title="pointDialog.title" :visible.sync="pointDialog.visible" width="560px">
      <el-form :model="pointForm" label-width="96px" size="small">
        <el-form-item label="测点编码" required>
          <el-input v-model.trim="pointForm.pointCode" placeholder="active_power" />
        </el-form-item>
        <el-form-item label="测点名称" required>
          <el-input v-model.trim="pointForm.pointName" placeholder="有功功率" />
        </el-form-item>
        <el-form-item label="值类型">
          <el-select v-model="pointForm.valueType">
            <el-option label="double" value="double" />
            <el-option label="int" value="int" />
            <el-option label="string" value="string" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model.trim="pointForm.unit" placeholder="kW" />
        </el-form-item>
        <el-form-item label="频率">
          <el-input-number v-model="pointForm.dataFrequency" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="核心测点">
          <el-switch v-model="pointForm.requiredFlag" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="pointForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button size="small" @click="pointDialog.visible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="pointDialog.loading" @click="submitPoint">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog :title="deviceRefDialog.title" :visible.sync="deviceRefDialog.visible" width="560px">
      <el-form :model="deviceRefForm" label-width="110px" size="small">
        <el-form-item label="来源编码" required>
          <el-input v-model.trim="deviceRefForm.sourceCode" placeholder="EMS" />
        </el-form-item>
        <el-form-item label="三方设备ID" required>
          <el-input v-model.trim="deviceRefForm.externalDeviceId" />
        </el-form-item>
        <el-form-item label="三方设备编码">
          <el-input v-model.trim="deviceRefForm.externalDeviceCode" />
        </el-form-item>
        <el-form-item label="三方设备名">
          <el-input v-model.trim="deviceRefForm.externalDeviceName" />
        </el-form-item>
        <el-form-item label="网关编码">
          <el-input v-model.trim="deviceRefForm.gatewayCode" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="deviceRefForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button size="small" @click="deviceRefDialog.visible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="deviceRefDialog.loading" @click="submitDeviceRef">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog :title="pointRefDialog.title" :visible.sync="pointRefDialog.visible" width="560px">
      <el-form :model="pointRefForm" label-width="110px" size="small">
        <el-form-item label="来源编码" required>
          <el-input v-model.trim="pointRefForm.sourceCode" placeholder="EMS" />
        </el-form-item>
        <el-form-item label="三方测点" required>
          <el-input v-model.trim="pointRefForm.externalMetric" />
        </el-form-item>
        <el-form-item label="三方测点名">
          <el-input v-model.trim="pointRefForm.externalMetricName" />
        </el-form-item>
        <el-form-item label="倍率">
          <el-input-number v-model="pointRefForm.ratio" :step="0.1" controls-position="right" />
        </el-form-item>
        <el-form-item label="偏移">
          <el-input-number v-model="pointRefForm.offsetValue" :step="0.1" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="pointRefForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button size="small" @click="pointRefDialog.visible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="pointRefDialog.loading" @click="submitPointRef">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  createEnterprise,
  createDevice,
  createDeviceExternalRef,
  createPoint,
  createPointExternalRef,
  disableEnterprise,
  deleteDevice,
  deletePoint,
  disableDeviceExternalRef,
  disablePointExternalRef,
  listEnterprises,
  listDeviceExternalRefs,
  listDevices,
  listPointExternalRefs,
  listPoints,
  listTelemetryMinute,
  listUnmatchedTelemetry,
  updateEnterprise,
  updateDevice,
  updateDeviceExternalRef,
  updatePoint,
  updatePointExternalRef,
} from "./api";

export default {
  name: "LoadResources",
  data() {
    return {
      activeDomain: "enterprise",
      deviceDetailTab: "profile",
      iotDataTab: "telemetry",
      domainItems: [
        {
          key: "enterprise",
          title: "企业管理",
          desc: "聚合商下企业主数据",
        },
        {
          key: "device",
          title: "设备管理",
          desc: "设备属性、测点、绑定关系",
        },
        {
          key: "iot-data",
          title: "物联数据",
          desc: "标准数据与未匹配数据",
        },
      ],
      enterpriseFilters: {
        aggregatorId: sessionStorage.getItem("aggregatorId") || "",
        entId: sessionStorage.getItem("entId") || "",
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
      selectedEnterprise: null,
      enterpriseServiceRange: [],
      filters: {
        aggregatorId: sessionStorage.getItem("aggregatorId") || "",
        entId: sessionStorage.getItem("entId") || sessionStorage.getItem("cid") || "",
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
      selectedDevice: null,
      selectedPoint: null,
      selectedPointId: null,
      points: [],
      pointLoading: false,
      deviceRefs: [],
      pointRefs: [],
      telemetryRows: [],
      telemetryLoading: false,
      telemetryQuery: {
        pointCode: "active_power",
        range: [],
      },
      telemetryPage: {
        pageIndex: 1,
        pageSize: 100,
        total: 0,
      },
      unmatchedRows: [],
      unmatchedLoading: false,
      unmatchedQuery: {
        sourceCode: "",
        handled: 0,
      },
      unmatchedPage: {
        pageIndex: 1,
        pageSize: 20,
        total: 0,
      },
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
      pointDialog: {
        visible: false,
        loading: false,
        mode: "create",
        title: "新增测点",
      },
      deviceRefDialog: {
        visible: false,
        loading: false,
        mode: "create",
        title: "新增设备绑定",
      },
      pointRefDialog: {
        visible: false,
        loading: false,
        mode: "create",
        title: "新增测点绑定",
      },
      enterpriseForm: this.defaultEnterpriseForm(),
      deviceForm: this.defaultDeviceForm(),
      pointForm: this.defaultPointForm(),
      deviceRefForm: this.defaultDeviceRefForm(),
      pointRefForm: this.defaultPointRefForm(),
    };
  },
  computed: {
    onlineCount() {
      return this.devices.filter(item => item.onlineStatus === 1).length;
    },
    enterpriseNameMap() {
      return this.enterprises.reduce((map, item) => {
        map[item.entId] = item.entName || item.entId;
        return map;
      }, {});
    },
  },
  mounted() {
    this.reloadEnterprises();
    this.reloadDevices();
  },
  methods: {
    defaultEnterpriseForm() {
      return {
        id: null,
        aggregatorId: sessionStorage.getItem("aggregatorId") || "",
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
        aggregatorId: "",
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
        createDefaultPowerPoint: true,
      };
    },
    defaultPointForm() {
      return {
        id: null,
        pointCode: "active_power",
        pointName: "有功功率",
        valueType: "double",
        unit: "kW",
        dataFrequency: 60,
        requiredFlag: 1,
        readWriteRole: "readOnly",
        status: 1,
        sort: 0,
      };
    },
    defaultDeviceRefForm() {
      return {
        id: null,
        sourceCode: "EMS",
        externalDeviceId: "",
        externalDeviceCode: "",
        externalDeviceName: "",
        gatewayCode: "",
        status: 1,
      };
    },
    defaultPointRefForm() {
      return {
        id: null,
        sourceCode: "EMS",
        externalMetric: "",
        externalMetricName: "",
        ratio: 1,
        offsetValue: 0,
        status: 1,
      };
    },
    switchDomain(domain) {
      this.activeDomain = domain;
      if (domain === "device" && !this.devices.length) {
        this.reloadDevices();
      }
      if (domain === "iot-data" && !this.devices.length) {
        this.reloadDevices();
      }
    },
    enterpriseOptionLabel(item) {
      return item.entName ? `${item.entName} (${item.entId})` : item.entId;
    },
    reloadEnterprises() {
      this.enterpriseLoading = true;
      const params = {
        ...this.enterpriseFilters,
        pageIndex: this.enterprisePage.pageIndex,
        pageSize: this.enterprisePage.pageSize,
      };
      listEnterprises(params)
        .then(res => {
          const page = this.unwrapPage(res);
          this.enterprises = page.list;
          this.enterprisePage.total = page.total;
          if (this.enterprises.length && !this.selectedEnterprise) {
            this.selectedEnterprise = this.enterprises[0];
          }
          if (this.selectedEnterprise) {
            const fresh = this.enterprises.find(item => item.entId === this.selectedEnterprise.entId);
            if (fresh) {
              this.selectedEnterprise = fresh;
            }
          }
        })
        .finally(() => {
          this.enterpriseLoading = false;
        });
    },
    resetEnterpriseFilters() {
      this.enterpriseFilters = {
        aggregatorId: sessionStorage.getItem("aggregatorId") || "",
        entId: sessionStorage.getItem("entId") || "",
        entName: "",
        status: undefined,
      };
      this.enterprisePage.pageIndex = 1;
      this.reloadEnterprises();
    },
    handleEnterpriseSizeChange(size) {
      this.enterprisePage.pageSize = size;
      this.enterprisePage.pageIndex = 1;
      this.reloadEnterprises();
    },
    handleEnterpriseCurrentChange(row) {
      if (row) {
        this.selectedEnterprise = row;
      }
    },
    openEnterpriseDialog(mode, row) {
      this.enterpriseDialog.mode = mode;
      this.enterpriseDialog.title = mode === "create" ? "新增企业" : "编辑企业";
      this.enterpriseForm = this.defaultEnterpriseForm();
      this.enterpriseServiceRange = [];
      if (mode === "edit" && row) {
        this.enterpriseForm = { ...this.defaultEnterpriseForm(), ...row };
        if (row.serviceStartDate && row.serviceEndDate) {
          this.enterpriseServiceRange = [row.serviceStartDate, row.serviceEndDate];
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
        .then(res => {
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
      this.$confirm(`确认停用企业 ${row.entName || row.entId}？`, "停用企业", { type: "warning" }).then(() => {
        disableEnterprise(row.entId).then(res => {
          this.ensureSuccess(res);
          this.$message.success("已停用");
          this.reloadEnterprises();
        });
      });
    },
    viewEnterpriseDevices(row) {
      this.selectedEnterprise = row;
      this.filters.aggregatorId = row.aggregatorId || this.filters.aggregatorId;
      this.filters.entId = row.entId;
      this.devicePage.pageIndex = 1;
      this.activeDomain = "device";
      this.reloadDevices();
    },
    handleDeviceEntChange(entId) {
      const ent = this.enterprises.find(item => item.entId === entId);
      if (ent) {
        this.deviceForm.aggregatorId = ent.aggregatorId;
      }
    },
    reloadDevices() {
      this.deviceLoading = true;
      const params = {
        ...this.filters,
        pageIndex: this.devicePage.pageIndex,
        pageSize: this.devicePage.pageSize,
      };
      listDevices(params)
        .then(res => {
          const page = this.unwrapPage(res);
          this.devices = page.list;
          this.devicePage.total = page.total;
          if (!this.devices.length) {
            this.selectedDevice = null;
            this.selectedPoint = null;
            this.selectedPointId = null;
            this.points = [];
            this.deviceRefs = [];
            this.pointRefs = [];
            return;
          }
          const fresh = this.selectedDevice
            ? this.devices.find(item => item.id === this.selectedDevice.id)
            : null;
          if (fresh) {
            this.selectDevice(fresh);
          } else {
            this.selectDevice(this.devices[0]);
          }
        })
        .finally(() => {
          this.deviceLoading = false;
        });
    },
    resetFilters() {
      this.filters = {
        aggregatorId: "",
        entId: "",
        deviceName: "",
        deviceTypeCode: "",
      };
      this.devicePage.pageIndex = 1;
      this.selectedDevice = null;
      this.reloadDevices();
    },
    handleDeviceSizeChange(size) {
      this.devicePage.pageSize = size;
      this.devicePage.pageIndex = 1;
      this.reloadDevices();
    },
    handleDeviceCurrentChange(row) {
      if (row) {
        this.selectDevice(row);
      }
    },
    selectDevice(row) {
      this.selectedDevice = row;
      this.selectedEnterprise = this.enterprises.find(item => item.entId === row.entId) || this.selectedEnterprise;
      this.selectedPoint = null;
      this.selectedPointId = null;
      this.telemetryQuery.pointCode = "active_power";
      this.loadPoints();
      this.loadDeviceRefs();
    },
    handleTelemetryDeviceChange(deviceId) {
      const device = this.devices.find(item => item.id === deviceId);
      if (device) {
        this.selectDevice(device);
        return;
      }
      this.selectedDevice = null;
      this.selectedPoint = null;
      this.selectedPointId = null;
    },
    loadPoints() {
      if (!this.selectedDevice) {
        this.points = [];
        return;
      }
      this.pointLoading = true;
      listPoints(this.selectedDevice.id)
        .then(res => {
          this.points = this.unwrapData(res, []);
          if (this.points.length) {
            this.selectedPoint = this.points[0];
            this.selectedPointId = this.selectedPoint.id;
            this.loadPointRefs();
          } else {
            this.selectedPoint = null;
            this.selectedPointId = null;
            this.pointRefs = [];
          }
        })
        .finally(() => {
          this.pointLoading = false;
        });
    },
    handlePointCurrentChange(row) {
      if (row) {
        this.selectedPoint = row;
        this.selectedPointId = row.id;
        this.telemetryQuery.pointCode = row.pointCode;
        this.loadPointRefs();
      }
    },
    handlePointSelectChange(pointId) {
      const point = this.points.find(item => item.id === pointId);
      if (point) {
        this.selectedPoint = point;
        this.telemetryQuery.pointCode = point.pointCode;
        this.loadPointRefs();
      }
    },
    loadDeviceRefs() {
      if (!this.selectedDevice) {
        this.deviceRefs = [];
        return;
      }
      listDeviceExternalRefs(this.selectedDevice.id).then(res => {
        this.deviceRefs = this.unwrapData(res, []);
      });
    },
    loadPointRefs() {
      if (!this.selectedPoint) {
        this.pointRefs = [];
        return;
      }
      listPointExternalRefs(this.selectedPoint.id).then(res => {
        this.pointRefs = this.unwrapData(res, []);
      });
    },
    openDeviceDialog(mode, row) {
      this.deviceDialog.mode = mode;
      this.deviceDialog.title = mode === "create" ? "新增设备" : "编辑设备";
      this.deviceForm = this.defaultDeviceForm();
      if (mode === "create") {
        this.deviceForm.aggregatorId = this.filters.aggregatorId;
        this.deviceForm.entId = this.filters.entId;
        this.handleDeviceEntChange(this.deviceForm.entId);
      } else if (row) {
        this.deviceForm = {
          ...this.defaultDeviceForm(),
          ...row,
          createDefaultPowerPoint: false,
        };
      }
      this.deviceDialog.visible = true;
    },
    submitDevice() {
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
        .then(res => {
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
      this.$confirm(`确认删除设备 ${row.deviceCode}？`, "删除设备", { type: "warning" }).then(() => {
        deleteDevice(row.id).then(res => {
          this.ensureSuccess(res);
          this.$message.success("已删除");
          if (this.selectedDevice && this.selectedDevice.id === row.id) {
            this.selectedDevice = null;
          }
          this.reloadDevices();
        });
      });
    },
    openPointDialog(mode, row) {
      if (!this.selectedDevice) {
        this.$message.warning("请选择设备");
        return;
      }
      this.pointDialog.mode = mode;
      this.pointDialog.title = mode === "create" ? "新增测点" : "编辑测点";
      this.pointForm = mode === "create" ? this.defaultPointForm() : { ...this.defaultPointForm(), ...row };
      this.pointDialog.visible = true;
    },
    submitPoint() {
      if (!this.pointForm.pointCode || !this.pointForm.pointName) {
        this.$message.warning("测点编码和名称不能为空");
        return;
      }
      this.pointDialog.loading = true;
      const request =
        this.pointDialog.mode === "create"
          ? createPoint(this.selectedDevice.id, this.pointForm)
          : updatePoint(this.pointForm.id, this.pointForm);
      request
        .then(res => {
          this.ensureSuccess(res);
          this.$message.success("保存成功");
          this.pointDialog.visible = false;
          this.loadPoints();
        })
        .finally(() => {
          this.pointDialog.loading = false;
        });
    },
    removePoint(row) {
      this.$confirm(`确认删除测点 ${row.pointCode}？`, "删除测点", { type: "warning" }).then(() => {
        deletePoint(row.id).then(res => {
          this.ensureSuccess(res);
          this.$message.success("已删除");
          this.loadPoints();
        });
      });
    },
    openDeviceRefDialog(mode, row) {
      if (!this.selectedDevice) {
        this.$message.warning("请选择设备");
        return;
      }
      this.deviceRefDialog.mode = mode;
      this.deviceRefDialog.title = mode === "create" ? "新增设备绑定" : "编辑设备绑定";
      this.deviceRefForm = mode === "create" ? this.defaultDeviceRefForm() : { ...this.defaultDeviceRefForm(), ...row };
      if (mode === "create") {
        this.deviceRefForm.externalDeviceName = this.selectedDevice.deviceName;
      }
      this.deviceRefDialog.visible = true;
    },
    submitDeviceRef() {
      if (!this.deviceRefForm.sourceCode || !this.deviceRefForm.externalDeviceId) {
        this.$message.warning("来源编码和三方设备ID不能为空");
        return;
      }
      this.deviceRefDialog.loading = true;
      const request =
        this.deviceRefDialog.mode === "create"
          ? createDeviceExternalRef(this.selectedDevice.id, this.deviceRefForm)
          : updateDeviceExternalRef(this.deviceRefForm.id, this.deviceRefForm);
      request
        .then(res => {
          this.ensureSuccess(res);
          this.$message.success("保存成功");
          this.deviceRefDialog.visible = false;
          this.loadDeviceRefs();
        })
        .finally(() => {
          this.deviceRefDialog.loading = false;
        });
    },
    disableDeviceRef(row) {
      disableDeviceExternalRef(row.id).then(res => {
        this.ensureSuccess(res);
        this.$message.success("已停用");
        this.loadDeviceRefs();
      });
    },
    openPointRefDialog(mode, row) {
      if (!this.selectedPoint) {
        this.$message.warning("请选择测点");
        return;
      }
      this.pointRefDialog.mode = mode;
      this.pointRefDialog.title = mode === "create" ? "新增测点绑定" : "编辑测点绑定";
      this.pointRefForm = mode === "create" ? this.defaultPointRefForm() : { ...this.defaultPointRefForm(), ...row };
      if (mode === "create") {
        this.pointRefForm.externalMetricName = this.selectedPoint.pointName;
      }
      this.pointRefDialog.visible = true;
    },
    submitPointRef() {
      if (!this.pointRefForm.sourceCode || !this.pointRefForm.externalMetric) {
        this.$message.warning("来源编码和三方测点不能为空");
        return;
      }
      this.pointRefDialog.loading = true;
      const request =
        this.pointRefDialog.mode === "create"
          ? createPointExternalRef(this.selectedPoint.id, this.pointRefForm)
          : updatePointExternalRef(this.pointRefForm.id, this.pointRefForm);
      request
        .then(res => {
          this.ensureSuccess(res);
          this.$message.success("保存成功");
          this.pointRefDialog.visible = false;
          this.loadPointRefs();
        })
        .finally(() => {
          this.pointRefDialog.loading = false;
        });
    },
    disablePointRef(row) {
      disablePointExternalRef(row.id).then(res => {
        this.ensureSuccess(res);
        this.$message.success("已停用");
        this.loadPointRefs();
      });
    },
    loadTelemetry() {
      this.telemetryLoading = true;
      const range = this.telemetryQuery.range || [];
      const params = {
        entId: this.selectedDevice ? this.selectedDevice.entId : this.filters.entId,
        deviceId: this.selectedDevice ? this.selectedDevice.id : undefined,
        pointCode: this.telemetryQuery.pointCode || undefined,
        startTime: range[0],
        endTime: range[1],
        pageIndex: this.telemetryPage.pageIndex,
        pageSize: this.telemetryPage.pageSize,
      };
      listTelemetryMinute(params)
        .then(res => {
          const page = this.unwrapPage(res);
          this.telemetryRows = page.list;
          this.telemetryPage.total = page.total;
        })
        .finally(() => {
          this.telemetryLoading = false;
        });
    },
    handleTelemetrySizeChange(size) {
      this.telemetryPage.pageSize = size;
      this.telemetryPage.pageIndex = 1;
      this.loadTelemetry();
    },
    loadUnmatched() {
      this.unmatchedLoading = true;
      listUnmatchedTelemetry({
        ...this.unmatchedQuery,
        pageIndex: this.unmatchedPage.pageIndex,
        pageSize: this.unmatchedPage.pageSize,
      })
        .then(res => {
          const page = this.unwrapPage(res);
          this.unmatchedRows = page.list;
          this.unmatchedPage.total = page.total;
        })
        .finally(() => {
          this.unmatchedLoading = false;
        });
    },
    handleUnmatchedSizeChange(size) {
      this.unmatchedPage.pageSize = size;
      this.unmatchedPage.pageIndex = 1;
      this.loadUnmatched();
    },
    unwrapData(res, fallback) {
      const body = res && res.data ? res.data : {};
      if (body.code && body.code !== 200) {
        this.$message.error(body.msg || "请求失败");
        return fallback;
      }
      return body.data === undefined || body.data === null ? fallback : body.data;
    },
    unwrapPage(res) {
      const data = this.unwrapData(res, {});
      return {
        list: data.list || [],
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

.domain-nav {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.domain-nav button {
  min-height: 76px;
  padding: 14px 16px;
  border: 1px solid #d9e5ec;
  border-radius: 8px;
  background: #ffffff;
  cursor: pointer;
  text-align: left;
}

.domain-nav button.active,
.domain-nav button:hover {
  border-color: #0780ed;
  box-shadow: inset 3px 0 0 #0780ed;
}

.domain-nav span,
.section-head p,
.table-actions span,
.device-detail-head p {
  margin: 0;
  color: #0e2638;
  font-weight: 700;
}

.domain-nav span {
  display: block;
  font-size: 16px;
}

.domain-nav small {
  display: block;
  margin-top: 8px;
  color: #6b7f8d;
  font-size: 13px;
}

.domain-section {
  margin-top: 12px;
}

.section-head,
.resource-toolbar,
.data-panel,
.device-list-panel,
.device-detail-panel {
  border: 1px solid #d9e5ec;
  border-radius: 8px;
  background: #ffffff;
}

.section-head {
  min-height: 66px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.section-head p {
  font-size: 18px;
}

.section-head span {
  display: block;
  margin-top: 6px;
  color: #6b7f8d;
  font-size: 13px;
}

.resource-toolbar {
  margin-top: 12px;
  min-height: 58px;
  padding: 12px 14px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.filter-form {
  flex: 1;
}

.filter-form ::v-deep .el-form-item {
  margin-bottom: 0;
}

.resource-summary {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.summary-item {
  min-height: 72px;
  padding: 14px 16px;
  border: 1px solid #d9e5ec;
  border-radius: 8px;
  background: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
}

.summary-item span {
  color: #6b7f8d;
  font-size: 13px;
}

.summary-item strong {
  color: #0e2638;
  font-size: 24px;
  line-height: 1.1;
  font-weight: 700;
}

.data-panel {
  margin-top: 12px;
  padding: 14px;
}

.device-domain-layout {
  margin-top: 12px;
  display: grid;
  grid-template-columns: minmax(520px, 0.95fr) minmax(420px, 1.05fr);
  gap: 12px;
}

.device-list-panel,
.device-detail-panel {
  min-width: 0;
  padding: 14px;
}

.table-actions {
  min-height: 36px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.table-actions span {
  font-size: 15px;
}

.table-pagination {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.device-detail-head {
  min-height: 54px;
  padding-bottom: 12px;
  border-bottom: 1px solid #edf2f5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.device-detail-head span {
  display: block;
  margin-top: 6px;
  color: #6b7f8d;
  font-size: 13px;
}

.detail-tabs {
  margin-top: 10px;
}

.device-profile {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.device-profile div {
  min-height: 70px;
  padding: 12px;
  border: 1px solid #edf2f5;
  border-radius: 8px;
  background: #fbfdff;
}

.device-profile span {
  display: block;
  color: #6b7f8d;
  font-size: 13px;
}

.device-profile strong {
  display: block;
  margin-top: 8px;
  color: #0e2638;
  font-size: 15px;
}

.empty-state {
  height: 220px;
  color: #8a9aa6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.binding-layout {
  display: grid;
  grid-template-rows: auto auto;
  gap: 14px;
}

.binding-section {
  border: 1px solid #edf2f5;
  border-radius: 8px;
  padding: 12px;
}

.point-select {
  display: flex;
  align-items: center;
  gap: 10px;
}

.point-select .el-select {
  width: 240px;
}

.telemetry-filter > div {
  display: flex;
  align-items: center;
  gap: 8px;
}

.telemetry-filter .el-input {
  width: 150px;
}

.telemetry-filter .el-select {
  width: 120px;
}

.enterprise-form .inline-fields {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

::v-deep .el-dialog {
  border-radius: 8px;
}

@media (max-width: 1280px) {
  .device-domain-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .domain-nav {
    grid-template-columns: 1fr;
  }

  .device-profile {
    grid-template-columns: 1fr;
  }
}
</style>
