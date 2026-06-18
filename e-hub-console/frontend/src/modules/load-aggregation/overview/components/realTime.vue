<template>
  <div class="realTime">
    <div class="commonHeader">
      <div class="text">实时汇总</div>
      <div class="headerRight" @click="goDetail()">
        <span class="detailText">详情</span>
        <img class="detailImg" src="../images/right.png" alt="" />
      </div>
    </div>
    <div>
      <tabbar @tabClick="tabClick($event)"></tabbar>
      <div style="margin-top:20px">
        <ecline
          :unit="'kW'"
          :refreshId="refreshId"
          :ecdata="todayOverviewData.chartList"
          :timeList="todayOverviewData.timeList"
          style="height:520px"
          width="100%"
          height="100%"
        ></ecline>
      </div>
    </div>
  </div>
</template>
<script>
import tabbar from "./tabbar";
import ecline from "./ec_line1";

import { getOverview } from "../api";

export default {
  name: "realTime",
  components: {
    tabbar,
    ecline,
  },
  data() {
    return {
      todayOverviewData: {},
    };
  },
  props: {
    activeObj: {
      type: Object,
      require: true,
    },
    simulate: {
      type: String,
      require: true,
    },
    refreshId: {
      type: Number,
      require: false,
    },
  },
  methods: {
    goDetail() {
      this.$emit("goRealTimeDetail");
    },
    tabClick(e) {
      this.doGetTodayOverview(e.id);
    },
    doGetTodayOverview(resourceTypeId) {
      const query = {
        aggregatorId: this.aggregatorId,
        resourceTypeId,
        dayType: "today",
      };
      getOverview(query, this.simulate).then(res => {
        if (res.data.code === 200) {
          const chartList = [
            {
              name: "实时功率",
              yAxisIndex: 0,
              value: res.data.data.powerChart ? res.data.data.powerChart : [],
            },
            {
              name: "聚合申报功率",
              yAxisIndex: 0,
              value: res.data.data.issueChart ? res.data.data.issueChart : [],
            },
            {
              name: "调度下发功率",
              yAxisIndex: 0,
              value: res.data.data.dapChart ? res.data.data.dapChart : [],
            },
            {
              name: "碳排因子",
              yAxisIndex: 1,
              value: res.data.data.crChart ? res.data.data.crChart : [],
            },
          ];
          res.data.data.chartList = chartList;
          this.todayOverviewData = res.data.data;
        }
      });
    },
  },
  created() {
    this.aggregatorId = sessionStorage.getItem("entId");
  },
};
</script>
<style lang="less"></style>
<style lang="less" type="text/less" scoped>
.realTime {
  width: calc(100% - 40px);
  height: 100%;
  background: #ffffff;
  border-radius: 12px;
  padding: 0 20px;
  .commonHeader {
    display: flex;
    height: 58px;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    .text {
      font-size: 18px;
      font-weight: 600;
      color: #333333;
    }
    .headerRight {
      display: flex;
      height: 58px;
      align-items: center;
      cursor: pointer;
      .detailText {
        font-size: 14px;
        font-weight: 400;
        color: #666666;
      }
      .detailImg {
        margin-left: 3px;
        width: 7px;
        height: 11px;
      }
    }
  }
}
</style>
