<template>
  <div class="grid-quality-page">
    <header class="page-head">
      <div>
        <h2>电网上送核查</h2>
        <p>按日核对总加分钟、单体量测和参与口径下的功率加和。</p>
      </div>
      <div class="head-actions">
        <el-date-picker v-model="dailyDate" type="date" value-format="yyyy-MM-dd" size="small" :clearable="false" @change="reload" />
        <el-button size="small" icon="el-icon-refresh" :loading="loading" @click="reload">刷新</el-button>
        <el-button v-if="canExport" size="small" icon="el-icon-download" @click="downloadReport">导出当日</el-button>
      </div>
    </header>

    <div class="cutoff">数据截至 {{ overview.cutoff || '--' }}</div>

    <el-alert v-if="overview.marketEnabled === false" class="market-alert" title="当前人工标记为未参与电网市场" description="该标记仅用于说明当前市场状态；实际上送、数据核查、异常记录和统计结果均不受影响。" type="info" :closable="false" show-icon />

    <section v-loading="loading" class="metric-grid">
      <article class="metric-block">
        <span>总加数据完整性</span><strong>{{ overview.totalActual || 0 }} / {{ overview.totalExpected || 0 }}</strong>
        <div><b :class="overview.totalMissing ? 'danger' : 'success'">缺失 {{ overview.totalMissing || 0 }} 分钟</b><em class="danger">发送失败 {{ overview.totalSendFailed || 0 }}</em></div>
      </article>
      <article class="metric-block">
        <span>参与单体数据完整性</span><strong>{{ overview.singleActual || 0 }} / {{ overview.singleExpected || 0 }}</strong>
        <div><b :class="overview.singleMissing ? 'danger' : 'success'">缺失 {{ overview.singleMissing || 0 }} 点</b><em class="danger">发送失败 {{ overview.singleSendFailed || 0 }}</em></div>
      </article>
      <article class="metric-block">
        <span>总加 / 单体对账</span><strong>{{ overview.reconcileMatched || 0 }} 一致</strong>
        <div><b class="precision">精度差异 {{ overview.reconcilePrecision || 0 }}</b><em class="danger">异常 {{ overview.reconcileMismatch || 0 }}</em></div>
      </article>
      <article class="metric-block scope-block">
        <span>单体参与口径</span><strong>{{ overview.participantCount || 0 }} 参与 / {{ overview.nonParticipantCount || 0 }} 不参与</strong>
        <div><b class="success">完整 {{ overview.completeParticipantCount || 0 }}</b><em :class="overview.incompleteParticipantCount ? 'danger' : ''">缺点 {{ overview.incompleteParticipantCount || 0 }}</em></div>
      </article>
    </section>

    <section class="peak-audit">
      <div class="peak-audit-head"><div><h3>调峰计划上送状态</h3><p>仅核查运营总览申报计划在指定日期的上送结果，不在此页面执行申报。</p></div><span>申报日期 {{ dailyDate }}</span></div>
      <div class="peak-audit-grid">
        <div><span>96点数据</span><strong>{{ peakPlan.bepfCount || 0 }} / 96 基础用电，{{ peakPlan.mpscCount || 0 }} / 96 最大调峰能力</strong><el-tag size="small" :type="peakStatusType(peakPlan.latest96Point)">{{ peakStatusLabel(peakPlan.latest96Point) }}</el-tag><small>{{ peakLog(peakPlan.latest96Point) }}</small></div>
        <div><span>日运行数据</span><strong>{{ peakPlan.dailyCount || 0 }} 条申报数据</strong><el-tag size="small" :type="peakStatusType(peakPlan.latestDaily)">{{ peakStatusLabel(peakPlan.latestDaily) }}</el-tag><small>{{ peakLog(peakPlan.latestDaily) }}</small></div>
      </div>
    </section>

    <nav class="quality-tabs">
      <button v-for="tab in tabs" :key="tab.key" type="button" :class="{ active: activeTab === tab.key }" @click="switchTab(tab.key)">{{ tab.label }}</button>
    </nav>

    <section v-if="activeTab === 'scope'" class="quality-content">
      <div class="scope-summary">
        <button type="button" :class="{ active: scopeType === 'participant' }" @click="scopeType = 'participant'">
          <span>参与单体</span><strong>{{ overview.participantCount || 0 }}</strong><small>计入总加/单体对账</small>
        </button>
        <button type="button" :class="{ active: scopeType === 'nonParticipant' }" @click="scopeType = 'nonParticipant'">
          <span>不参与单体</span><strong>{{ overview.nonParticipantCount || 0 }}</strong><small>展示上送，不计入对账</small>
        </button>
      </div>
      <el-table :data="scopeRows" border stripe size="small" height="500">
        <el-table-column prop="singleCode" label="单体编码" min-width="150" />
        <el-table-column prop="singleName" label="单体名称" min-width="160" />
        <el-table-column prop="deviceCount" label="设备数" width="80" />
        <el-table-column label="点位完整性" width="160"><template slot-scope="{ row }"><span :class="row.missing ? 'danger' : 'success'">{{ row.actual }} / {{ row.expected }}</span></template></el-table-column>
        <el-table-column prop="missing" label="缺失点" width="90" />
        <el-table-column prop="lastTime" label="最近上送时间" width="170" />
        <el-table-column label="状态" width="100"><template slot-scope="{ row }"><el-tag size="mini" :type="row.status === 'COMPLETE' ? 'success' : 'danger'">{{ row.status === 'COMPLETE' ? '完整' : '有缺点' }}</el-tag></template></el-table-column>
      </el-table>
    </section>

    <section v-else-if="activeTab === 'trend'" class="quality-content">
      <div class="sub-toolbar"><el-radio-group v-model="trendDays" size="small" @change="loadTrend"><el-radio-button :label="7">近 7 天</el-radio-button><el-radio-button :label="30">近 30 天</el-radio-button></el-radio-group><span>内部完整性核查，不代表电网达标结论</span></div>
      <div ref="trendChart" class="trend-chart" />
    </section>

    <section v-else-if="activeTab === 'daily'" class="quality-content">
      <div class="sub-toolbar">
        <el-radio-group v-model="dailyType" size="small" @change="loadDaily(1)"><el-radio-button label="TOTAL">总加分钟</el-radio-button><el-radio-button label="SINGLE">单体 15 分钟</el-radio-button></el-radio-group>
        <el-input v-if="dailyType === 'SINGLE'" v-model="dailySingleCode" size="small" clearable placeholder="单体编码" style="width:180px" @keyup.enter.native="loadDaily(1)" />
        <el-select v-model="pointStatus" size="small" clearable placeholder="全部状态"><el-option v-for="item in pointStatuses" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        <el-button size="small" type="primary" icon="el-icon-search" @click="loadDaily(1)">查询</el-button>
      </div>
      <el-table v-loading="loading" :data="dailyRows" border stripe size="small" height="520">
        <el-table-column prop="time" label="时间" width="170" /><el-table-column v-if="dailyType === 'SINGLE'" prop="singleCode" label="单体编码" min-width="150" /><el-table-column prop="value" label="有功功率" min-width="120" />
        <el-table-column prop="source" label="数据来源" width="130"><template slot-scope="{ row }">{{ sourceLabel(row.source) }}</template></el-table-column>
        <el-table-column prop="status" label="状态" width="110"><template slot-scope="{ row }"><el-tag size="mini" :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag></template></el-table-column>
      </el-table>
      <el-pagination class="pagination" layout="total, sizes, prev, pager, next" :total="dailyTotal" :page-size="dailyPageSize" :current-page="dailyPageIndex" :page-sizes="[50,100,200]" @current-change="loadDaily" @size-change="changeDailyPageSize" />
    </section>

    <section v-else-if="activeTab === 'reconciliation'" class="quality-content">
      <el-alert title="总加由 MW 折算为 kW 后比较。精度差异用于识别低负荷量化误差，仍保留记录，超过内部精度容差才列为异常。" type="info" :closable="false" show-icon />
      <el-table v-loading="loading" :data="reconciliationRows" border stripe size="small" height="530" class="reconciliation-table">
        <el-table-column prop="time" label="15 分钟时刻" width="170" /><el-table-column prop="totalValue" label="总加原值(MW)" /><el-table-column prop="totalValueKw" label="总加折算(kW)" /><el-table-column prop="singleSum" label="参与单体加和(kW)" /><el-table-column prop="difference" label="绝对偏差(kW)" />
        <el-table-column prop="missingSingleCount" label="缺失单体" width="90" /><el-table-column prop="tolerance" label="精度容差" width="100" />
        <el-table-column label="结果" width="120"><template slot-scope="{ row }"><el-tag size="mini" :type="reconcileType(row.status)">{{ reconcileLabel(row.status) }}</el-tag></template></el-table-column>
      </el-table>
    </section>

    <section v-else class="quality-content">
      <div class="sub-toolbar"><el-select v-model="issueFilters.issueType" size="small" clearable placeholder="全部异常类型"><el-option v-for="item in issueTypes" :key="item.value" :label="item.label" :value="item.value" /></el-select><el-select v-model="issueFilters.status" size="small" clearable placeholder="全部处理状态"><el-option v-for="item in issueStatuses" :key="item.value" :label="item.label" :value="item.value" /></el-select><el-button size="small" type="primary" @click="loadIssues(1)">查询</el-button></div>
      <el-table :data="issues.list || []" border stripe size="small" height="500"><el-table-column prop="issueTime" label="异常时间" width="170" /><el-table-column prop="issueType" label="异常类型" width="170"><template slot-scope="{ row }">{{ issueTypeLabel(row.issueType) }}</template></el-table-column><el-table-column prop="singleCode" label="单体编码" min-width="140" /><el-table-column prop="reason" label="原因" min-width="260" show-overflow-tooltip /><el-table-column prop="status" label="处理状态" width="100" /><el-table-column label="操作" width="90"><template slot-scope="{ row }"><el-button type="text" @click="openIssue(row)">记录处理</el-button></template></el-table-column></el-table>
    </section>

    <el-dialog title="核查记录" :visible.sync="issueDialog.visible" width="620px" append-to-body><el-form label-width="90px"><el-form-item label="处理状态"><el-select v-model="issueDialog.status"><el-option v-for="item in issueStatuses" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item><el-form-item label="处理备注"><el-input v-model="issueDialog.remark" type="textarea" :rows="4" placeholder="可记录补送结果、精度原因或电网外部工单号" /></el-form-item></el-form><span slot="footer"><el-button @click="issueDialog.visible=false">取消</el-button><el-button type="primary" @click="saveIssue">保存</el-button></span></el-dialog>
  </div>
</template>

<script>
import echarts from "echarts";
import { exportReport, getDailyOverview, getDailyPage, getIssueDetail, getIssues, getPeakPlanStatus, getReconciliation, getTrend, updateIssue } from "./api";

function formatDate(date) { const y = date.getFullYear(); const m = String(date.getMonth() + 1).padStart(2, "0"); const d = String(date.getDate()).padStart(2, "0"); return `${y}-${m}-${d}`; }
function payload(response, fallback) { return response && response.data && response.data.data !== undefined ? response.data.data : fallback; }

export default {
  name: "GridDeliveryQuality",
  props: { aggregatorId: { type: String, default: "" }, resourceTypeId: { type: String, default: "" }, user: { type: Object, default: () => ({}) } },
  data() {
    return {
      dailyDate: formatDate(new Date()), overview: {}, peakPlan: {}, loading: false,
      activeTab: "scope", tabs: [{ key: "scope", label: "参与及不参与" }, { key: "trend", label: "质量趋势" }, { key: "daily", label: "单日点位" }, { key: "reconciliation", label: "总加 / 单体对账" }, { key: "issues", label: "核查记录" }],
      scopeType: "participant", trendDays: 7, trend: { rows: [] }, chart: null, dailyType: "TOTAL", dailySingleCode: "", pointStatus: "", dailyRows: [], dailyTotal: 0, dailyPageIndex: 1, dailyPageSize: 100, reconciliationRows: [],
      issues: { list: [], total: 0 }, issueFilters: { issueType: "", status: "" }, issueDialog: { visible: false, id: null, status: "PROCESSING", remark: "" },
      pointStatuses: [{ value: "NORMAL", label: "正常" }, { value: "MISSING", label: "缺失" }, { value: "INVALID", label: "无效" }, { value: "SEND_FAILED", label: "上送失败" }],
      issueStatuses: [{ value: "OPEN", label: "待处理" }, { value: "PROCESSING", label: "处理中" }, { value: "RESOLVED", label: "已解决" }, { value: "IGNORED", label: "已登记外部工单" }],
      issueTypes: ["TOTAL_MISSING", "TOTAL_SEND_FAILED", "SINGLE_MISSING", "SINGLE_SEND_FAILED", "RECONCILIATION_MISMATCH", "PARTICIPATION_MISSING"].map(value => ({ value, label: value })),
    };
  },
  computed: {
    permissions() { return Array.isArray(this.user.permissions) ? this.user.permissions : []; }, canManage() { return this.permissions.includes("load:grid-interaction:audit") || this.permissions.includes("load:grid-delivery:manage"); }, canExport() { return this.permissions.includes("load:grid-interaction:export") || this.permissions.includes("load:grid-delivery:export") || this.canManage; },
    scopeRows() { return this.scopeType === "participant" ? (this.overview.participants || []) : (this.overview.nonParticipants || []); },
  },
  watch: { aggregatorId() { this.reload(); }, resourceTypeId() { this.reload(); } },
  created() { this.reload(); },
  beforeDestroy() { if (this.chart) this.chart.dispose(); },
  methods: {
    params() { return { aggregatorId: this.aggregatorId, resourceTypeId: this.resourceTypeId }; },
    async reload() { if (!this.aggregatorId || !this.resourceTypeId) return; this.loading = true; try { this.overview = payload(await getDailyOverview({ ...this.params(), date: this.dailyDate }), {}); this.peakPlan = payload(await getPeakPlanStatus({ ...this.params(), dataDate: this.dailyDate }), {}); if (this.activeTab === "trend") await this.loadTrend(); if (this.activeTab === "daily") await this.loadDaily(1); if (this.activeTab === "reconciliation") await this.loadReconciliation(); if (this.activeTab === "issues") await this.loadIssues(1); } finally { this.loading = false; } },
    switchTab(tab) { this.activeTab = tab; this.$nextTick(() => { if (tab === "trend") this.loadTrend(); if (tab === "daily") this.loadDaily(1); if (tab === "reconciliation") this.loadReconciliation(); if (tab === "issues") this.loadIssues(1); }); },
    async loadTrend() { const end = new Date(`${this.dailyDate}T00:00:00`); const start = new Date(end.getTime() - (this.trendDays - 1) * 86400000); this.trend = payload(await getTrend({ ...this.params(), startDate: formatDate(start), endDate: this.dailyDate }), { rows: [] }); this.$nextTick(this.renderTrend); },
    renderTrend() { if (!this.$refs.trendChart) return; if (!this.chart) this.chart = echarts.init(this.$refs.trendChart); const rows = this.trend.rows || []; this.chart.setOption({ tooltip: { trigger: "axis" }, legend: { data: ["总加完整率", "单体完整率", "对账完整率"] }, grid: { left: 48, right: 28, top: 54, bottom: 40 }, xAxis: { type: "category", data: rows.map(row => String(row.date).slice(0, 10)) }, yAxis: { type: "value", min: 0, max: 100, axisLabel: { formatter: "{value}%" } }, series: [{ name: "总加完整率", type: "line", smooth: true, data: rows.map(row => row.totalRate), itemStyle: { color: "#1677ff" } }, { name: "单体完整率", type: "line", smooth: true, data: rows.map(row => row.singleRate), itemStyle: { color: "#12a182" } }, { name: "对账完整率", type: "line", smooth: true, data: rows.map(row => row.reconciliationRate), itemStyle: { color: "#d9822b" } }] }, true); },
    async loadDaily(page) { this.loading = true; try { const result = payload(await getDailyPage({ ...this.params(), date: this.dailyDate, type: this.dailyType, status: this.pointStatus || undefined, singleCode: this.dailySingleCode || undefined, pageIndex: page || 1, pageSize: this.dailyPageSize }), {}); this.dailyRows = result.list || []; this.dailyTotal = Number(result.total || 0); this.dailyPageIndex = Number(result.pageIndex || 1); } finally { this.loading = false; } },
    changeDailyPageSize(size) { this.dailyPageSize = size; this.loadDaily(1); }, async loadReconciliation() { this.reconciliationRows = payload(await getReconciliation({ ...this.params(), date: this.dailyDate }), []); },
    async loadIssues(page) { const result = payload(await getIssues({ ...this.params(), startDate: this.dailyDate, endDate: this.dailyDate, ...this.issueFilters, pageIndex: page || 1, pageSize: 50 }), {}); this.issues = result; },
    async downloadReport() { const response = await exportReport({ ...this.params(), startDate: this.dailyDate, endDate: this.dailyDate }); this.saveBlob(response.data, `电网上送核查-${this.dailyDate}.xlsx`); },
    async openIssue(row) { const detail = payload(await getIssueDetail(row.id, { aggregatorId: this.aggregatorId }), {}); const issue = detail.issue || {}; this.issueDialog = { visible: true, id: row.id, status: issue.status || row.status, remark: issue.remark || "" }; },
    async saveIssue() { await updateIssue(this.issueDialog.id, { aggregatorId: this.aggregatorId, status: this.issueDialog.status, remark: this.issueDialog.remark }); this.issueDialog.visible = false; await this.loadIssues(1); },
    saveBlob(data, fileName) { const url = URL.createObjectURL(new Blob([data])); const link = document.createElement("a"); link.href = url; link.download = fileName; link.click(); URL.revokeObjectURL(url); },
    formatRate(value) { return value === undefined || value === null ? "--" : `${Number(value).toFixed(2)}%`; }, peakStatusLabel(log) { if (!log) return "暂无上送记录"; return log.status === "SUCCESS" ? "上送成功" : "上送失败"; }, peakStatusType(log) { return !log ? "info" : log.status === "SUCCESS" ? "success" : "danger"; }, peakLog(log) { return log ? `${String(log.createTime || "").replace("T", " ")} · ${log.remark || ""}` : "暂无上送记录"; }, statusLabel(status) { return { NORMAL: "正常", MISSING: "缺失", INVALID: "无效", SEND_FAILED: "上送失败" }[status] || status; }, statusType(status) { return status === "NORMAL" ? "success" : "danger"; }, sourceLabel(source) { return { UPSTREAM_AUDIT: "实际上送", TELEMETRY_REBUILD: "量测复算", NO_DATA: "无数据" }[source] || source || "-"; }, reconcileLabel(status) { return { MATCH: "一致", PRECISION_DIFFERENCE: "精度差异", MISMATCH: "异常不一致", UNAVAILABLE: "无法对账" }[status] || status; }, reconcileType(status) { return { MATCH: "success", PRECISION_DIFFERENCE: "warning", MISMATCH: "danger", UNAVAILABLE: "info" }[status] || "info"; }, issueTypeLabel(type) { return { TOTAL_MISSING: "总加缺点", TOTAL_SEND_FAILED: "总加上送失败", SINGLE_MISSING: "单体缺点", SINGLE_SEND_FAILED: "单体上送失败", RECONCILIATION_MISMATCH: "总加/单体不一致", PARTICIPATION_MISSING: "参与口径缺失" }[type] || type; },
  },
};
</script>

<style scoped>
.grid-quality-page{min-height:100%;color:#243746}.page-head{display:flex;align-items:flex-start;justify-content:space-between;gap:20px;margin-bottom:6px}.page-head h2{margin:0;font-size:18px}.page-head p{margin:7px 0 0;color:#788692;font-size:13px}.head-actions{display:flex;flex-wrap:wrap;justify-content:flex-end;gap:8px}.cutoff{text-align:right;color:#7b8994;font-size:12px}.market-alert{margin-top:12px}.metric-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px;margin:16px 0}.metric-block{padding:16px;border:1px solid #e0e6eb;border-radius:6px;background:#fff}.metric-block span{color:#71808b;font-size:13px}.metric-block strong{display:block;margin:9px 0 13px;font-size:22px;font-weight:600}.metric-block div{display:flex;justify-content:space-between;font-size:12px}.metric-block b,.metric-block em{font-style:normal;font-weight:500}.success{color:#168b68}.danger{color:#d84a4a}.precision{color:#d27c21}.peak-audit{margin:0 0 16px;padding:16px;border:1px solid #e0e6eb;border-radius:6px;background:#fff}.peak-audit-head{display:flex;justify-content:space-between;align-items:flex-start;gap:16px}.peak-audit-head h3{margin:0;font-size:16px}.peak-audit-head p{margin:5px 0 0;color:#788692;font-size:12px}.peak-audit-head>span{color:#788692;font-size:12px}.peak-audit-grid{display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-top:14px}.peak-audit-grid>div{display:grid;grid-template-columns:1fr auto;gap:6px 12px;padding:12px;background:#f7f9fb;border-left:3px solid #dce5eb}.peak-audit-grid span{color:#71808b;font-size:12px}.peak-audit-grid strong{font-size:14px}.peak-audit-grid small{grid-column:1/-1;color:#788692;font-size:12px}.quality-tabs{display:flex;gap:4px;border-bottom:1px solid #dce3e8}.quality-tabs button{padding:11px 16px;border:0;border-bottom:2px solid transparent;background:transparent;color:#62727e;cursor:pointer}.quality-tabs button.active{border-bottom-color:#1677ff;color:#1677ff;font-weight:600}.quality-content{padding:16px;background:#fff;border:1px solid #e0e6eb;border-top:0}.scope-summary{display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:14px}.scope-summary button{display:grid;grid-template-columns:1fr auto;gap:4px 16px;text-align:left;padding:14px;border:1px solid #dfe6eb;border-radius:6px;background:#fafbfc;cursor:pointer}.scope-summary button.active{border-color:#1677ff;background:#f3f8ff}.scope-summary strong{grid-row:1/3;grid-column:2;font-size:24px}.scope-summary small{color:#7b8994}.sub-toolbar{display:flex;align-items:center;gap:12px;margin-bottom:14px;color:#778590;font-size:12px}.trend-chart{height:440px}.pagination{margin-top:14px;text-align:right}.reconciliation-table{margin-top:14px}@media(max-width:1200px){.metric-grid{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:760px){.page-head{display:block}.head-actions{justify-content:flex-start;margin-top:12px}.metric-grid{grid-template-columns:1fr}.peak-audit-head{display:block}.peak-audit-head>span{display:block;margin-top:8px}.peak-audit-grid{grid-template-columns:1fr}.scope-summary{grid-template-columns:1fr}.quality-tabs{overflow-x:auto}.quality-tabs button{flex:0 0 auto}}
</style>
