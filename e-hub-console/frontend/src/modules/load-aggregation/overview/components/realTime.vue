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
      <div class="resource-pill">{{ resourceTypeName || "当前资源类型" }}</div>
      <div style="margin-top:20px">
        <ecline
          :unit="'kW'"
          :refreshId="refreshId"
          :ecdata="todayOverviewData.chartList"
          :timeList="todayOverviewData.timeList"
          style="height:390px"
          width="100%"
          height="100%"
        ></ecline>
      </div>
    </div>
  </div>
</template>
<script>
import ecline from "./ec_line1";

import { getOverview } from "../api";

function resolveAggregatorId() {
  return (
    sessionStorage.getItem("aggregatorId") ||
    sessionStorage.getItem("entId") ||
    sessionStorage.getItem("cid") ||
    ""
  );
}

export default {
  name: "realTime",
  components: {
    ecline,
  },
  data() {
    return {
      aggregatorId: resolveAggregatorId(),
      todayOverviewData: {},
    };
  },
  props: {
    activeObj: {
      type: Object,
      require: true,
    },
    refreshId: {
      type: Number,
      require: false,
    },
    resourceTypeId: {
      type: [String, Number],
      default: "",
    },
    resourceTypeName: {
      type: String,
      default: "",
    },
  },
  methods: {
    goDetail() {
      this.$emit("goRealTimeDetail");
    },
    doGetTodayOverview(resourceTypeId) {
      if (!resourceTypeId) {
        this.todayOverviewData = {};
        return;
      }
      const query = {
        aggregatorId: this.aggregatorId,
        resourceTypeId,
        dayType: "today",
      };
      getOverview(query).then(res => {
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
  watch: {
    resourceTypeId: {
      immediate: true,
      handler(value) {
        this.doGetTodayOverview(value);
      },
    },
  },
};
</script>
<style lang="less"></style>
<style lang="less" type="text/less" scoped>
.realTime {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  background: #ffffff;
  border-radius: 8px;
  padding: 0 20px;
  .resource-pill {
    display: inline-flex;
    align-items: center;
    height: 30px;
    padding: 0 12px;
    border-radius: 6px;
    background: #f3f8fc;
    color: #456577;
    font-size: 13px;
  }
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
