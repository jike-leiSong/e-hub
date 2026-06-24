<template>
  <div class="realTime">
    <div class="commonHeader">
      <div class="text">当月收益统计</div>
    </div>
    <div>
      <ecbar
        :unit="'元'"
        :refreshId="refreshId"
        :ecdata="weekProfitData"
        :isStack="true"
        :barWidth="20"
        style="height:320px"
        width="100%"
        height="100%"
      ></ecbar>
    </div>
  </div>
</template>
<script>
import ecbar from "./ec_bar";
import { getWeekProfit } from "../api";

function resolveAggregatorId() {
  return (
    sessionStorage.getItem("aggregatorId") ||
    sessionStorage.getItem("entId") ||
    sessionStorage.getItem("cid") ||
    ""
  );
}

export default {
  name: "income",
  components: {
    ecbar,
  },
  data() {
    return {
      weekProfitData: [],
      barWidth: "",
    };
  },
  props: {
    refreshId: {
      type: Number,
      require: false,
    },
  },
  methods: {
    doGetWeekProfit() {
      getWeekProfit({
        aggregatorId: this.aggregatorId,
      }).then(res => {
        if (res.data.code === 200) {
          const chartList = [
            {
              name: "用户收益",
              value: [],
            },
            {
              name: "负荷聚合商收益",
              value: [],
            },
          ];
          res.data.data.forEach(item => {
            const obj1 = {
              time: item.date,
              value: item.entProfit,
            };
            const obj2 = {
              time: item.date,
              value: item.aggregatorProfit,
            };
            chartList[0].value.push(obj1);
            chartList[1].value.push(obj2);
          });
          this.weekProfitData = chartList;
        }
      });
    },
  },
  created() {
    this.aggregatorId = resolveAggregatorId();
    this.doGetWeekProfit();
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
