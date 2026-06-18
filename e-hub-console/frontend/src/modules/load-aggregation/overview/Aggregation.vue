<template>
  <div>
    <div class="aggregation" v-show="showPage === 'home'">
      <div class="top">
        <div class="top-left">
          <div class="top-left-1">
            <lastApply :simulate="simulate" @goDetail="goDetail($event)" @goBack="goBack()" />
          </div>
          <div class="top-left-2">
            <applyPlan :refreshId="refreshId" :activeObj="activeObj" @goApplyPlanDetail="goApplyPlanDetail()" @goApplyPage="goApplyPage($event)" @goBack="goBack()" />
          </div>
        </div>
        <div class="top-right">
          <realTime :refreshId="refreshId" :simulate="simulate" @goRealTimeDetail="goRealTimeDetail()" @goBack="goBack()" />
        </div>
      </div>
      <div class="bottom">
        <div class="bottom-left">
          <userDistribution :simulate="simulate" @goUserDetail="goUserDetail($event)" @goBack="goBack()"></userDistribution>
        </div>
        <div class="bottom-right">
          <income :refreshId="refreshId" :simulate="simulate"></income>
        </div>
      </div>
    </div>
    <profitStatics v-if="showPage === 'profitStatics'" :activeObj="activeObj" @goBack="goBack()" :simulate="simulate"></profitStatics>
    <userDetail v-if="showPage === 'userDetail'" @goBack="goBack()" :searchEntId="searchEntId" :simulate="simulate"></userDetail>
    <detailPage v-if="showPage === 'detailPage'" @goBack="goBack()" :refreshId="refreshId" :dateType="dateTypeValue" :simulate="simulate"></detailPage>
    <!-- 原申报计划 -->
    <applyPrice v-if="showPage === 'applyPrice'" :simulate="simulate" :activeObj="activeObj" :applyPriceStatus="applyPriceStatus" @goBack="goBack()"></applyPrice>
    <!-- 新申报计划 -->
    <declarationPlan v-if="showPage === 'declarationPlan'" :simulate="simulate" :activeObj="activeObj" :applyPriceStatus="applyPriceStatus" @goBack="goBack()"></declarationPlan>
  </div>
</template>
<script>
import Vue from "vue"
import userDetail from "./components/userDetail"
import detailPage from "./components/detail"
import profitStatics from "./components/profitStatics"
import lastApply from "./components/lastApply"
import realTime from "./components/realTime"
import applyPlan from "./components/applyPlan"
import userDistribution from "./components/userDistribution"
import income from "./components/income"
import applyPrice from "./components/applyPrice"
import declarationPlan from './components/declarationPlan/declarationPlan'
export default {
  name: "Aggregation",
  components: {
    lastApply,
    realTime,
    applyPlan,
    userDetail,
    detailPage,
    profitStatics,
    userDistribution,
    income,
    applyPrice,
    declarationPlan,
  },
  data() {
    return {
      map: null,
      showPage: "home",
      simulate: null,
      canSet: null,
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
    // sessionStorage.setItem("entId", '1711340903453614082')
    this.aggregatorId = sessionStorage.getItem("entId") || sessionStorage.getItem("cid")
    this.simulate = String(this.activeObj.option.data.dataType)
    this.canSet = this.activeObj.option.data.canSet
  },
  methods: {
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
    },
    goApplyPage(e) {
      this.applyPriceStatus = e
      this.showPage = 'declarationPlan'
    },
  },
  watch: {
    activeObj: {
      deep: true,
      handler(val) {
        this.simulate = String(val.option.data.dataType)
      },
    },
    activeCompName(val) {
      if (val instanceof Array && val && val[0] === "Aggregation") {
        this.refreshId = new Date().getTime()
      }
    },
  },
}
</script>
<style lang="less" scoped>
.aggregation {
  width: calc(100% - 40px);
  min-height: 768px;
  padding: 20px;
  background: #f4f5f9;
  .top {
    width: 100%;
    height: 650px;
    margin-bottom: 20px;
    display: flex;
    .top-left {
      width: 50%;
      display: flex;
      flex-direction: column;
      .top-left-1 {
        width: 100%;
        height: 86px;
        margin-bottom: 20px;
      }
      .top-left-2 {
        flex: 1;
      }
    }
    .top-right {
      margin-left: 20px;
      flex: 1;
      height: 100%;
    }
  }
  .bottom {
    width: 100%;
    height: 400px;
    display: flex;
    .bottom-left {
      width: 40%;
      margin-right: 20px;
    }
    .bottom-right {
      flex: 1;
    }
  }
}
</style>
