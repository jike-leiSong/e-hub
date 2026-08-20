<template>
  <div v-loading="loading" class="grid-connection">
    <div class="connection-toolbar">
      <div>
        <strong>{{ resourceTypeName }}</strong>
        <span>主动上送操作均记录操作人和电网响应</span>
      </div>
      <el-button size="small" icon="el-icon-refresh" @click="reload">刷新状态</el-button>
    </div>

    <section class="connection-section">
      <div class="section-title"><i class="el-icon-connection" /><div><h3>单体模型</h3><p>模型发生变化或年度市场参与前，上送当前能源的全量模型及参与标识。</p></div></div>
      <div class="status-grid model-grid">
        <div><span>模型总数</span><strong>{{ model.totalCount || 0 }}</strong></div>
        <div><span>参与单体</span><strong>{{ model.participantCount || 0 }}</strong></div>
        <div><span>不参与单体</span><strong>{{ model.nonParticipantCount || 0 }}</strong></div>
        <div><span>最近上送</span><strong class="small-value">{{ operationSummary(model.lastOperation) }}</strong></div>
      </div>
      <div class="section-actions"><el-button v-if="canDeliver" size="small" type="primary" icon="el-icon-upload2" @click="confirmModel">预览并上送全量模型</el-button></div>
    </section>

    <section class="connection-section">
      <div class="section-title"><i class="el-icon-odometer" /><div><h3>实时量测</h3><p>正常总加和单体量测由自动任务上送，人工操作仅用于失败批次补送。</p></div></div>
      <div class="status-grid">
        <div><span>总加最近上送</span><strong class="small-value">{{ formatTime(measurement.latestTotalTime) }}</strong></div>
        <div><span>单体最近上送</span><strong class="small-value">{{ formatTime(measurement.latestSingleTime) }}</strong></div>
        <div><span>最近异常批次</span><strong class="small-value" :class="measurement.latestFailedBatch ? 'danger' : 'success'">{{ formatTime(measurement.latestFailedBatch) }}</strong></div>
      </div>
      <div class="section-actions"><el-button v-if="canDeliver" size="small" icon="el-icon-refresh-right" @click="openRetry">补送15分钟批次</el-button></div>
    </section>

    <section class="connection-section records-section">
      <div class="section-title"><i class="el-icon-tickets" /><div><h3>操作记录</h3><p>单体模型上送和单体量测补送记录。</p></div></div>
      <div class="record-filter"><el-select v-model="operationType" size="small" clearable placeholder="全部业务类型" @change="loadRecords(1)"><el-option v-for="item in operationTypes" :key="item.value" :label="item.label" :value="item.value" /></el-select></div>
      <el-table :data="records.list || []" border stripe size="small">
        <el-table-column prop="operationType" label="业务类型" width="140"><template slot-scope="{ row }">{{ operationLabel(row.operationType) }}</template></el-table-column>
        <el-table-column prop="businessTime" label="业务时间" width="170" />
        <el-table-column prop="createTime" label="操作时间" width="170" />
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="status" label="结果" width="90"><template slot-scope="{ row }"><el-tag size="mini" :type="row.status === 'SUCCESS' ? 'success' : 'danger'">{{ row.status === 'SUCCESS' ? '成功' : '失败' }}</el-tag></template></el-table-column>
        <el-table-column prop="responseMessage" label="响应摘要" min-width="260" show-overflow-tooltip />
      </el-table>
      <el-pagination class="pagination" layout="total, prev, pager, next" :total="Number(records.total || 0)" :page-size="20" :current-page="Number(records.pageIndex || 1)" @current-change="loadRecords" />
    </section>

    <el-dialog title="确认上送全量单体模型" :visible.sync="modelDialog" width="520px" append-to-body>
      <el-descriptions :column="1" border size="small"><el-descriptions-item label="能源类型">{{ resourceTypeName }}</el-descriptions-item><el-descriptions-item label="模型总数">{{ model.totalCount || 0 }}</el-descriptions-item><el-descriptions-item label="参与 / 不参与">{{ model.participantCount || 0 }} / {{ model.nonParticipantCount || 0 }}</el-descriptions-item></el-descriptions>
      <el-alert class="dialog-note" title="电网将收到该能源当前全部模型及参与标识，请确认模型口径已经维护完成。" type="warning" :closable="false" />
      <span slot="footer"><el-button @click="modelDialog=false">取消</el-button><el-button type="primary" :loading="sendingModel" @click="sendModel">确认上送</el-button></span>
    </el-dialog>

    <el-dialog title="补送单体量测批次" :visible.sync="retryDialog" width="520px" append-to-body>
      <el-form label-width="100px" size="small"><el-form-item label="能源类型">{{ resourceTypeName }}</el-form-item><el-form-item label="补送时间段"><el-date-picker v-model="retryRange" type="datetimerange" value-format="yyyy-MM-dd HH:mm:ss" start-placeholder="开始时间" end-placeholder="结束时间" range-separator="至" /></el-form-item></el-form>
      <el-alert title="系统按15分钟拆分时间段并逐批补送该能源的全量单体量测；起止时间须为00、15、30或45分钟，单次最多16个批次。" type="warning" :closable="false" />
      <span slot="footer"><el-button @click="retryDialog=false">取消</el-button><el-button type="primary" :loading="retrying" @click="retryMeasurement">确认补送</el-button></span>
    </el-dialog>
  </div>
</template>

<script>
import { getConnectionOverview, getOperationRecords, retrySingleMeasurementRange, sendSingleModel } from "./api";

function today(offset = 0) { const date = new Date(Date.now() + offset * 86400000); const y = date.getFullYear(); const m = String(date.getMonth() + 1).padStart(2, "0"); const d = String(date.getDate()).padStart(2, "0"); return `${y}-${m}-${d}`; }
function payload(response, fallback) { return response && response.data && response.data.data !== undefined ? response.data.data : fallback; }
function ensureSuccess(response) { const body = response && response.data ? response.data : {}; if (Number(body.code) !== 200) throw new Error(body.msg || "操作失败"); return body.data; }

export default {
  name: "GridConnection",
  props: { aggregatorId: { type: String, default: "" }, resourceTypeId: { type: String, default: "" }, resourceTypeName: { type: String, default: "" }, user: { type: Object, default: () => ({}) } },
  data() { return { dataDate: today(1), loading: false, overview: {}, records: { list: [], total: 0, pageIndex: 1 }, operationType: "", modelDialog: false, retryDialog: false, retryRange: [`${today()} 00:15:00`, `${today()} 00:15:00`], sendingModel: false, retrying: false, operationTypes: [{ value: "SEND_MODEL", label: "单体模型上送" }, { value: "RETRY_SINGLE", label: "单体量测补送" }] }; },
  computed: {
    permissions() { return Array.isArray(this.user.permissions) ? this.user.permissions : []; },
    canDeliver() { return this.permissions.includes("load:grid-interaction:delivery") || this.permissions.includes("load:grid-delivery:manage"); },
    model() { return this.overview.model || {}; }, measurement() { return this.overview.measurement || {}; },
  },
  watch: { resourceTypeId: { immediate: true, handler() { this.reload(); } } },
  methods: {
    params() { return { aggregatorId: this.aggregatorId, resourceTypeId: this.resourceTypeId }; },
    async reload() { if (!this.aggregatorId || !this.resourceTypeId) return; this.loading = true; try { this.overview = payload(await getConnectionOverview({ ...this.params(), dataDate: this.dataDate }), {}); await this.loadRecords(1); } finally { this.loading = false; } },
    async loadRecords(page) { if (!this.resourceTypeId) return; this.records = payload(await getOperationRecords({ ...this.params(), operationType: this.operationType || undefined, pageIndex: page || 1, pageSize: 20 }), { list: [], total: 0, pageIndex: 1 }); },
    confirmModel() { this.modelDialog = true; },
    async sendModel() { this.sendingModel = true; try { ensureSuccess(await sendSingleModel(this.params())); this.$message.success("单体模型上送完成"); this.modelDialog = false; await this.reload(); } catch (error) { this.$message.error(error.message || "单体模型上送失败"); } finally { this.sendingModel = false; } },
    openRetry() { this.retryDialog = true; this.retryRange = [`${today()} 00:15:00`, `${today()} 00:15:00`]; },
    async retryMeasurement() { const range = this.retryRange || []; if (range.length !== 2 || range.some(value => !/:00:00$|:15:00$|:30:00$|:45:00$/.test(value))) { this.$message.warning("起止时间必须为00、15、30或45分钟"); return; } this.retrying = true; try { const result = ensureSuccess(await retrySingleMeasurementRange({ ...this.params(), startTime: range[0], endTime: range[1] })) || {}; if (Number(result.failed || 0) > 0) this.$message.warning(`补送完成：成功${result.success || 0}批，失败${result.failed}批`); else this.$message.success(`单体量测补送完成，共${result.success || 0}批`); this.retryDialog = false; await this.reload(); } catch (error) { this.$message.error(error.message || "单体量测补送失败"); } finally { this.retrying = false; } },
    formatTime(value) { return value ? String(value).replace("T", " ") : "暂无"; },
    operationSummary(value) { return value ? `${value.status === "SUCCESS" ? "成功" : "失败"} · ${this.formatTime(value.createTime)}` : "暂无记录"; },
    operationLabel(value) { const item = this.operationTypes.find(option => option.value === value); return item ? item.label : value; },
  },
};
</script>

<style scoped>
.grid-connection{color:#243746}.connection-toolbar{display:flex;justify-content:space-between;align-items:center;margin-bottom:12px}.connection-toolbar>div{display:flex;align-items:center;gap:12px}.connection-toolbar span{color:#788692;font-size:12px}.connection-section{position:relative;margin-bottom:12px;padding:16px;border:1px solid #dde5ea;border-radius:6px;background:#fff}.section-title{display:flex;gap:11px;align-items:flex-start}.section-title>i{margin-top:2px;color:#1677ff;font-size:19px}.section-title h3{margin:0;font-size:16px}.section-title p{margin:5px 0 0;color:#798791;font-size:12px}.status-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:10px;margin-top:15px}.model-grid{grid-template-columns:repeat(4,minmax(0,1fr))}.status-grid>div{min-height:70px;padding:12px;border-left:3px solid #dce5eb;background:#f7f9fb}.status-grid span{display:block;color:#71808b;font-size:12px}.status-grid strong{display:block;margin-top:9px;font-size:21px}.status-grid .small-value{font-size:13px;line-height:20px}.section-actions{position:absolute;top:16px;right:16px}.record-filter{display:flex;justify-content:flex-end;margin:-30px 0 12px}.record-filter .el-select{width:170px}.pagination{margin-top:12px;text-align:right}.dialog-note{margin-top:14px}.success{color:#168b68}.danger{color:#d84a4a}@media(max-width:1100px){.model-grid{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:760px){.connection-toolbar{align-items:flex-start}.connection-toolbar>div{display:block}.connection-toolbar span{display:block;margin-top:4px}.status-grid,.model-grid{grid-template-columns:1fr}.section-actions{position:static;margin-top:12px}.record-filter{justify-content:flex-start;margin:12px 0}}
</style>
