<template>
  <div class="profitStatis">
    <div class="headerTop">
      <div class="tabBox">
        <div
          class="tabBoxItem"
          :class="{ tabBoxItemActive: selTab == '1' }"
          @click="doSelTab('1')"
        >
          本月
        </div>
        <div
          class="tabBoxItem"
          :class="{ tabBoxItemActive: selTab == '2' }"
          @click="doSelTab('2')"
        >
          上月
        </div>
      </div>
      <el-date-picker
        v-model="timeValue"
        type="daterange"
        range-separator="至"
        :picker-options="pickerOptions"
        @change="changeDate"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
      >
      </el-date-picker>
    </div>
    <div class="totalBox">
      <div class="totalBoxHeader">合计</div>
      <div class="totalBoxContent">
        <div class="totalBoxContentItem">
          <div class="name">调度下发金额(元)</div>
          <div class="value">{{ totalNum.issueAmount }}</div>
        </div>
        <div class="unit">=</div>
        <div class="totalBoxContentItem">
          <div class="name">负荷聚合商收益(元)</div>
          <div class="value">{{ totalNum.aggregatorProfits }}</div>
        </div>
        <div class="unit">+</div>
        <div class="totalBoxContentItem">
          <div class="name">用户收益(元)</div>
          <div class="value">{{ totalNum.userProfits }}</div>
        </div>
      </div>
    </div>
    <div class="echartBox">
      <div class="totalBoxHeader">合计</div>
      <ecbar
        :unit="'元'"
        :ecdata="chartData"
        :refreshId="refreshId"
        :isStack="'总量'"
        :barWidth="20"
        style="height:580px"
        width="100%"
        height="100%"
      ></ecbar>
    </div>
  </div>
</template>
<script>
import ecbar from "./ec_bar";
import moment from "moment";
import { getProfitStatistics, doSaveOperation } from "../api";

export default {
  name: "profitStatis",
  components: {
    ecbar,
  },
  data() {
    return {
      selTab: "1",
      timeValue: [],
      startTime: "",
      endTime: "",
      barWidth: null,
      chartData: [
        {
          name: "负荷聚合商收益",
          value: [],
        },
        {
          name: "用户收益",
          value: [],
        },
      ],
      totalNum: {},
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now();
        },
      },
      aggregatorId: '',
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
    doSelTab(item) {
      this.selTab = item;
      if (item == "1") {
        this.startTime = moment()
          .month(moment().month())
          .startOf("month")
          .format("YYYY-MM-DD");
        this.endTime = moment().format("YYYY-MM-DD");
      } else {
        this.startTime = moment()
          .month(moment().month() - 1)
          .startOf("month")
          .format("YYYY-MM-DD");
        this.endTime = moment()
          .month(moment().month() - 1)
          .endOf("month")
          .format("YYYY-MM-DD");
      }
      this.timeValue = [this.startTime, this.endTime];
      this.doGetProfitStatistics();
    },
    changeDate() {
      if (this.timeValue) {
        this.startTime = moment(this.timeValue[0]).format("YYYY-MM-DD");
        this.endTime = moment(this.timeValue[1]).format("YYYY-MM-DD");
        this.selTab = "3";
        this.doGetProfitStatistics();
      }
    },
    doGetProfitStatistics() {
      const query = {
        startTime: this.startTime,
        endTime: this.endTime,
        aggregatorId: this.aggregatorId,
      };
      getProfitStatistics(query, this.simulate).then(res => {
        if (res.data.code === 200) {
          this.totalNum = res.data.data.profitStatisticsAmount;
          if (res.data.data.dateList) {
            this.chartData[0].value = [];
            this.chartData[1].value = [];
            res.data.data.dateList.forEach((item, index) => {
              const ojb = {
                time: item,
                value: res.data.data.profitStatisticsDailyList[index]
                  ? res.data.data.profitStatisticsDailyList[index]
                      .aggregatorProfits
                  : "",
              };
              const ojb1 = {
                time: item,
                value: res.data.data.profitStatisticsDailyList[index]
                  ? res.data.data.profitStatisticsDailyList[index].userProfits
                  : "",
              };
              this.chartData[0].value.push(ojb);
              this.chartData[1].value.push(ojb1);
            });
            this.barWidth = `${100 / this.chartData[0].value.length}%`;
          }
        }
      });
    },
  },
  created() {
    this.refreshId = new Date().getTime();
    this.startTime = moment()
      .month(moment().month())
      .startOf("month")
      .format("YYYY-MM-DD");
    // this.endTime = moment().subtract(2, "days").format('YYYY-MM-DD')
    this.endTime = moment().format("YYYY-MM-DD");
    this.timeValue = [this.startTime, this.endTime];
    this.aggregatorId = sessionStorage.getItem("entId");
    this.doGetProfitStatistics();
  },
};
</script>
<style lang="less">
// .profitStatis{
//   .el-input__inner{
//     height: 34px;
//     width: 150px;
//   }
//   .el-input{
//     .el-input__suffix{
//       top: 4px;
//     }
//   }
//   .is-focus{
//     .el-input__suffix{
//       top: -3px;
//     }
//   }
// }
.profitStatis {
  .el-input__inner {
    width: 250px !important;
    height: 35px !important;
  }
  .el-icon-date {
    line-height: 29px !important;
  }
  .el-range-separator {
    line-height: 28px !important;
  }
}
</style>
<style lang="less" type="text/less" scoped>
.profitStatis::-webkit-scrollbar {
  display: none; /* Chrome Safari */
}
.profitStatis {
  width: 100%;
  height: 100%;
  max-height: calc(100vh - 59px);
  position: relative;
  overflow-y: auto;
  margin-top: 20px;
  .headerTop {
    width: calc(100% - 40px);
    height: 54px;
    background: #ffffff;
    border-radius: 12px;
    display: flex;
    align-items: center;
    padding: 0 20px;
    .tabBox {
      width: 120px;
      height: 30px;
      border-radius: 5px;
      border: 1px solid #bbbbbb;
      display: flex;
      overflow: hidden;
      cursor: pointer;
      margin-right: 20px;
      .tabBoxItem {
        flex: 1;
        line-height: 30px;
        height: 30px;
        text-align: center;
        font-size: 14px;
        color: #333333;
      }
      .tabBoxItem:first-child {
        border-right: 1px solid #bbbbbb;
      }
      .tabBoxItemActive {
        background: #0780ed;
        color: #ffffff;
      }
    }
  }
  .totalBoxHeader {
    font-size: 18px;
    font-weight: 600;
    color: #333333;
  }
  .totalBox {
    width: calc(100% - 40px);
    height: 108px;
    background: #ffffff;
    border-radius: 12px;
    padding: 20px;
    margin: 20px 0;
    .totalBoxContent {
      display: flex;
      margin-top: 20px;
      .totalBoxContentItem {
        .name {
          font-size: 14px;
          font-weight: 400;
          color: #999999;
          margin-bottom: 10px;
        }
        .value {
          font-size: 42px;
          font-weight: 600;
          color: #333333;
        }
      }
      .unit {
        color: #999999;
        padding-top: 35px;
        font-size: 18px;
        margin: 0 20px;
      }
    }
  }
  .echartBox {
    width: calc(100% - 40px);
    height: 588px;
    background: #ffffff;
    border-radius: 12px;
    padding: 20px;
  }
}
</style>
