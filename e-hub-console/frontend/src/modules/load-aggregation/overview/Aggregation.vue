<template>
  <div>
    <div class="aggregation" v-show="showPage === 'home'">
      <section class="overview-toolbar">
        <div class="resource-switch">
          <span class="toolbar-label">资源类型</span>
          <button
            v-for="item in resourceTypeList"
            :key="item.id"
            type="button"
            class="resource-tab"
            :class="{ active: currentResourceTypeId === item.id, disabled: item.display !== 1 }"
            @click="changeResourceType(item)"
          >
            {{ item.name }}
          </button>
        </div>
        <div class="toolbar-scope">当前口径：{{ currentResourceTypeName || "--" }}</div>
      </section>

      <section class="metric-grid">
        <button
          v-for="item in metricCards"
          :key="item.key"
          type="button"
          class="metric-card"
          :class="{ clickable: item.target }"
          @click="goMetricTarget(item)"
        >
          <span class="metric-label">{{ item.label }}</span>
          <div v-if="item.type === 'participation'" class="participation-values">
            <div>
              <strong>{{ item.joined }}</strong>
              <span>参与</span>
            </div>
            <div>
              <strong>{{ item.notJoined }}</strong>
              <span>未参与</span>
            </div>
            <div>
              <strong>{{ item.total }}</strong>
              <span>总计</span>
            </div>
          </div>
          <template v-else>
            <strong>{{ item.value }}</strong>
            <span class="metric-unit">{{ item.unit }}</span>
          </template>
          <span class="metric-desc">{{ item.desc }}</span>
        </button>
      </section>

      <section class="overview-section realtime-section">
        <realTime
          :refreshId="refreshId"
          :resourceTypeId="currentResourceTypeId"
          :resourceTypeName="currentResourceTypeName"
          @goRealTimeDetail="goRealTimeDetail()"
          @goBack="goBack()"
        />
      </section>

      <section class="overview-section apply-section">
        <applyPlan
          :refreshId="refreshId"
          :activeObj="activeObj"
          :resourceTypeId="currentResourceTypeId"
          :resourceTypeName="currentResourceTypeName"
          @goApplyPlanDetail="goApplyPlanDetail()"
          @goApplyPage="goApplyPage($event)"
          @goBack="goBack()"
        />
      </section>

      <section class="overview-section income-section">
        <income :refreshId="refreshId"></income>
      </section>
    </div>
    <profitStatics v-if="showPage === 'profitStatics'" :activeObj="activeObj" @goBack="goBack()"></profitStatics>
    <userDetail v-if="showPage === 'userDetail'" @goBack="goBack()" :searchEntId="searchEntId"></userDetail>
    <detailPage v-if="showPage === 'detailPage'" @goBack="goBack()" :refreshId="refreshId" :dateType="dateTypeValue"></detailPage>
    <!-- 原申报计划 -->
    <applyPrice v-if="showPage === 'applyPrice'" :activeObj="activeObj" :applyPriceStatus="applyPriceStatus" @goBack="goBack()"></applyPrice>
    <!-- 新申报计划 -->
    <declarationPlan v-if="showPage === 'declarationPlan'" :activeObj="activeObj" :applyPriceStatus="applyPriceStatus" @goBack="goBack()"></declarationPlan>
  </div>
</template>
<script>
import Vue from "vue"
import userDetail from "./components/userDetail"
import detailPage from "./components/detail"
import profitStatics from "./components/profitStatics"
import realTime from "./components/realTime"
import applyPlan from "./components/applyPlan"
import income from "./components/income"
import applyPrice from "./components/applyPrice"
import declarationPlan from './components/declarationPlan/declarationPlan'
import {
  getAggregatorApply,
  getEntUserDetailRespList,
  getLastProfit,
  getResourceTypeList,
  getWeekProfit,
} from "./api"

function resolveAggregatorId() {
  return (
    sessionStorage.getItem("aggregatorId") ||
    sessionStorage.getItem("entId") ||
    sessionStorage.getItem("cid") ||
    ""
  )
}

function toNumber(value) {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : 0
}

export default {
  name: "Aggregation",
  components: {
    realTime,
    applyPlan,
    userDetail,
    detailPage,
    profitStatics,
    income,
    applyPrice,
    declarationPlan,
  },
  data() {
    return {
      map: null,
      showPage: "home",
      aggregatorId: null,
      searchEntId: null,
      interValData: null,
      dateTypeValue: "2",
      autoApply: true,
      yesterdayOverviewData: {},
      todayOverviewData: {},
      tomorrowOverviewData: {},
      weekProfitData: [],
      applyData: {},
      entUserDetailRespListData: [],
      refreshId: null,
      applyPriceStatus: null,
      lastProfitData: {},
      metricApplyData: {},
      metricUserCount: 0,
      monthProfitTotal: 0,
      resourceTypeList: [],
      currentResourceTypeId: "",
      currentResourceTypeName: "",
    }
  },
  props: {
    activeObj: {
      type: Object,
      require: true,
    },
    activeCompName: {
      type: Array,
      require: false,
    },
  },
  created() {
    Vue.prototype.$bus = new Vue()
    this.refreshId = new Date().getTime()
    this.aggregatorId = resolveAggregatorId()
    this.loadResourceTypes()
    this.loadMetricCards()
  },
  computed: {
    metricCards() {
      const applyYesNum = toNumber(this.metricApplyData.applyYesNum)
      const applyNoNum = this.metricApplyData.applyNoNum === null || this.metricApplyData.applyNoNum === undefined
        ? Math.max(toNumber(this.metricApplyData.entNum) - applyYesNum, 0)
        : toNumber(this.metricApplyData.applyNoNum)
      const applyTotal = this.metricApplyData.entNum === null || this.metricApplyData.entNum === undefined
        ? applyYesNum + applyNoNum
        : toNumber(this.metricApplyData.entNum)

      return [
        {
          key: "last-profit",
          label: "上次申报收益",
          value: this.formatMetricValue(this.lastProfitData.totalProfit),
          unit: "元",
          desc: this.lastProfitData.totalProfitTime
            ? `统计日 ${this.lastProfitData.totalProfitTime}`
            : "暂无统计日",
          target: "profitStatics",
        },
        {
          key: "month-profit",
          label: "当月累计收益",
          value: this.formatMetricValue(this.monthProfitTotal),
          unit: "元",
          desc: "用户收益 + 聚合商收益",
          target: "profitStatics",
        },
        {
          key: "apply-users",
          label: "用户参与情况",
          type: "participation",
          joined: this.formatMetricValue(applyYesNum),
          notJoined: this.formatMetricValue(applyNoNum),
          total: this.formatMetricValue(applyTotal),
          desc: this.metricApplyData.planDate
            ? `当前申报计划 ${this.metricApplyData.planDate}`
            : "当前申报计划",
          target: "detailPage",
        },
        {
          key: "price-status",
          label: "申报准备",
          value: this.formatPreparationStatus(this.metricApplyData),
          unit: "",
          desc: this.currentResourceTypeName || this.metricApplyData.applyResourceType || "暂无资源类型",
          target: "declarationPlan",
        },
        {
          key: "ent-count",
          label: "资源企业",
          value: this.formatMetricValue(this.metricUserCount),
          unit: "家",
          desc: this.currentResourceTypeName ? `当前资源类型 ${this.currentResourceTypeName}` : "当前资源类型",
          target: "userDetail",
        },
      ]
    },
  },
  methods: {
    loadResourceTypes() {
      if (!this.aggregatorId) {
        return
      }
      getResourceTypeList({ aggregatorId: this.aggregatorId }).then(res => {
        if (res.data && res.data.code === 200 && Array.isArray(res.data.data)) {
          this.resourceTypeList = res.data.data
          const activeItem = this.resourceTypeList.find(item => item.display === 1) || this.resourceTypeList[0]
          if (activeItem) {
            this.setCurrentResourceType(activeItem)
          }
        }
      })
    },
    setCurrentResourceType(item) {
      this.currentResourceTypeId = item.id
      this.currentResourceTypeName = item.name
      this.loadResourceMetrics()
    },
    changeResourceType(item) {
      if (!item || item.display !== 1 || item.id === this.currentResourceTypeId) {
        return
      }
      this.setCurrentResourceType(item)
    },
    loadMetricCards() {
      if (!this.aggregatorId) {
        return
      }
      getLastProfit({ aggregatorId: this.aggregatorId }).then(res => {
        if (res.data && res.data.code === 200) {
          this.lastProfitData = res.data.data || {}
        }
      })
      getWeekProfit({ aggregatorId: this.aggregatorId }).then(res => {
        if (res.data && res.data.code === 200 && Array.isArray(res.data.data)) {
          this.monthProfitTotal = res.data.data.reduce((total, item) => {
            return total + toNumber(item.entProfit) + toNumber(item.aggregatorProfit)
          }, 0)
        }
      })
      this.loadResourceMetrics()
    },
    loadResourceMetrics() {
      if (!this.aggregatorId) {
        return
      }
      const query = {
        aggregatorId: this.aggregatorId,
        resourceTypeId: this.currentResourceTypeId,
      }
      getAggregatorApply(query).then(res => {
        if (res.data && res.data.code === 200) {
          this.metricApplyData = res.data.data || {}
        }
      })
      getEntUserDetailRespList(query).then(res => {
        if (res.data && res.data.code === 200 && Array.isArray(res.data.data)) {
          this.metricUserCount = res.data.data.length
        }
      })
    },
    formatMetricValue(value) {
      if (value === null || value === undefined || value === "") {
        return "--"
      }
      if (typeof value === "string" && Number.isNaN(Number(value))) {
        return value
      }
      return Number(value).toLocaleString("zh-CN", {
        maximumFractionDigits: 2,
      })
    },
    formatPriceStatus(status) {
      if (status === null || status === undefined || status === "") {
        return "--"
      }
      return String(status) === "1" ? "已提交" : "未提交"
    },
    formatPreparationStatus(applyData) {
      if (!applyData) {
        return "--"
      }
      const applyYesNum = Number(applyData.applyYesNum)
      const resourceType = String(applyData.applyResourceType || "")
      const hasPlan = Number.isFinite(applyYesNum) && applyYesNum > 0 && resourceType !== "无"
      if (!hasPlan) {
        return "待完善"
      }
      return this.formatPriceStatus(applyData.applyPriceStatus)
    },
    formatApplyStatus(status) {
      const statusMap = {
        0: "可申报",
        1: "已申报",
        2: "已结束",
        3: "未开始",
      }
      return statusMap[String(status)] || "--"
    },
    goMetricTarget(item) {
      if (!item.target) {
        return
      }
      if (item.target === "detailPage") {
        this.goApplyPlanDetail()
        return
      }
      if (item.target === "declarationPlan") {
        this.goApplyPage(this.metricApplyData.applyPriceStatus)
        return
      }
      this.goDetail(item.target)
    },
    goRealTimeDetail() {
      this.dateTypeValue = "2"
      this.showPage = "detailPage"
    },
    goApplyPlanDetail() {
      this.dateTypeValue = "3"
      this.showPage = "detailPage"
    },
    goDetail(item) {
      this.showPage = item
    },
    goUserDetail(item) {
      if (item) {
        this.searchEntId = item
      } else {
        this.searchEntId = null
      }
      this.showPage = "userDetail"
    },
    goProfitStatics() {
      this.showPage = "profitStatics"
    },
    goBack() {
      this.refreshId = new Date().getTime()
      this.showPage = "home"
      this.loadMetricCards()
    },
    goApplyPage(e) {
      this.applyPriceStatus = e
      this.showPage = 'declarationPlan'
    },
  },
  watch: {
    activeCompName(val) {
      if (val instanceof Array && val && val[0] === "Aggregation") {
        this.refreshId = new Date().getTime()
        this.loadResourceTypes()
        this.loadMetricCards()
      }
    },
  },
}
</script>
<style lang="less" scoped>
.aggregation {
  width: 100%;
  min-height: 768px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.overview-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 48px;
  padding: 8px 16px;
  border: 1px solid #dde6ed;
  border-radius: 8px;
  background: #ffffff;
}

.resource-switch {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex-wrap: wrap;
}

.toolbar-label,
.toolbar-scope {
  color: #607d8f;
  font-size: 13px;
}

.toolbar-label {
  margin-right: 4px;
}

.resource-tab {
  height: 30px;
  padding: 0 14px;
  border: 1px solid #dde6ed;
  border-radius: 6px;
  background: #ffffff;
  color: #456577;
  cursor: pointer;
}

.resource-tab.active {
  border-color: #0780ed;
  background: #0780ed;
  color: #ffffff;
}

.resource-tab.disabled {
  color: #b0bec8;
  cursor: not-allowed;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
}

.metric-card {
  min-width: 0;
  min-height: 112px;
  padding: 16px 18px;
  border: 1px solid #dde6ed;
  border-radius: 8px;
  background: #ffffff;
  color: #0e2638;
  text-align: left;
  cursor: default;
}

.metric-card.clickable {
  cursor: pointer;
}

.metric-card.clickable:hover {
  border-color: #0780ed;
}

.metric-label,
.metric-unit,
.metric-desc {
  display: block;
}

.metric-label {
  color: #607d8f;
  font-size: 13px;
}

.metric-card strong {
  display: inline-block;
  margin-top: 12px;
  font-size: 28px;
  line-height: 1;
  font-weight: 700;
  color: #0e2638;
}

.metric-unit {
  display: inline-block;
  margin-left: 6px;
  color: #607d8f;
  font-size: 13px;
}

.metric-desc {
  margin-top: 12px;
  color: #607d8f;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.participation-values {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-top: 12px;
}

.participation-values div {
  min-width: 0;
}

.participation-values strong {
  display: block;
  margin-top: 0;
  font-size: 24px;
}

.participation-values span {
  display: block;
  margin-top: 8px;
  color: #607d8f;
  font-size: 12px;
}

.overview-section {
  min-width: 0;
}

.realtime-section {
  height: 520px;
}

.apply-section {
  height: 430px;
}

.income-section {
  height: 380px;
}

@media (max-width: 1280px) {
  .metric-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .realtime-section {
    height: 500px;
  }

  .apply-section {
    height: 620px;
  }

  .income-section {
    height: 360px;
  }
}

@media (max-width: 900px) {
  .overview-toolbar {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }

  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
