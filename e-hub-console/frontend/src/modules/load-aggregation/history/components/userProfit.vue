<template>
  <div class="equipmentOperation userProfit">
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
    <div class="echartBox">
      <div class="header">用户收益合计</div>
      <div class="profit">
        <span class="value">{{ userAmount }}</span
        >元
      </div>
      <div class="lineBox">
        <div
          class="lineBoxItem"
          v-for="(index) in 1"
          :key="index"
          :style="{ backgroundColor: '#bbbbbb', width: '100%' }"
        ></div>
      </div>
      <div class="tableBox">
        <div class="tableBoxItem">
          <div
            class="item"
            v-for="(item, index) in showTableList[0]"
            :key="index"
          >
            <span class="color" :style="{ backgroundColor: item.color }"></span>
            <span class="name">{{ item.entName }}</span>
            <span class="value">{{ item.entProfit }}</span>
            <span class="line"></span>
            <span class="percent">{{ item.profitPercent }}</span>
          </div>
        </div>
        <div class="tableBoxItem">
          <div
            class="item"
            v-for="(item, index) in showTableList[1]"
            :key="index"
          >
            <span class="color" :style="{ backgroundColor: item.color }"></span>
            <span class="name">{{ item.entName }}</span>
            <span class="value">{{ item.entProfit }}</span>
            <span class="line"></span>
            <span class="percent">{{ item.profitPercent }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import ecline from "./ec_line";
import { getUserProfitStatistics, doSaveOperation } from "../api";
import moment from "moment";

export default {
  name: "userProfit",
  components: {
    ecline,
  },
  data() {
    return {
      selTab: "1",
      timeValue: [],
      userAmount: "",
      startTime: "",
      endTime: "",
      percentData: [],
      showTableList: [[], []],
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
      this.doGetUserProfitStatistics();
    },
    doGetUserProfitStatistics() {
      const query = {
        startTime: this.startTime,
        endTime: this.endTime,
        aggregatorId: this.aggregatorId,
      };
      getUserProfitStatistics(query, this.simulate).then(res => {
        if (res.data.code === 200) {
          this.userAmount = res.data.data.userAmount;
          if (!res.data.data.userProfitStatisticsList) {
            res.data.data.userProfitStatisticsList = [];
          }
          this.percentData = res.data.data.userProfitStatisticsList;
          this.showTableList = [[], []];
          this.showTableList[0].value = [];
          this.showTableList[1].value = [];
          res.data.data.userProfitStatisticsList.forEach((item, index) => {
            if (index % 2 === 0) {
              this.showTableList[0].push(item);
            } else {
              this.showTableList[1].push(item);
            }
          });
        }
      });
    },
    changeDate() {
      if (this.timeValue) {
        this.startTime = moment(this.timeValue[0]).format("YYYY-MM-DD");
        this.endTime = moment(this.timeValue[1]).format("YYYY-MM-DD");
        this.selTab = "3";
        this.doGetUserProfitStatistics();
      }
    },
  },
  created() {
    this.startTime = moment()
      .month(moment().month())
      .startOf("month")
      .format("YYYY-MM-DD");
    // this.endTime = moment().subtract(2, "days").format("YYYY-MM-DD")
    this.endTime = moment().format("YYYY-MM-DD");
    this.timeValue = [this.startTime, this.endTime];
    this.aggregatorId = sessionStorage.getItem("entId");
    this.doGetUserProfitStatistics();
  },
};
</script>
<style lang="less">
.userProfit {
  .el-icon-date {
    line-height: 29px !important;
  }
  .el-range-separator {
    line-height: 28px !important;
  }
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
.equipmentOperation {
  width: 100%;
  height: 100%;
  max-height: calc(100vh - 59px);
  position: relative;
  overflow-y: auto;
  .headerTop {
    width: calc(100% - 40px);
    height: 54px;
    background: #ffffff;
    border-radius: 12px;
    display: flex;
    align-items: center;
    padding: 0 20px;
    margin: 20px 0;
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
  .searchBox {
    width: calc(100% - 40px);
    min-height: 54px;
    height: auto;
    background: #ffffff;
    border-radius: 12px;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    padding: 0 20px;
    margin-top: 20px;
    justify-content: space-between;
    .searchBoxLeft {
      flex: 1;
      display: flex;
      align-items: center;
    }
    .tabBox1 {
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
      .tabBoxItemActive {
        background: #0780ed;
        color: #ffffff;
      }
    }
  }
  .echartBox {
    border-radius: 12px;
    padding: 20px;
    width: calc(100% - 40px);
    background: white;
    .header {
      font-size: 18px;
      font-weight: 600;
      color: #333333;
    }
    .profit {
      margin: 20px 0;
      font-size: 18px;
      font-weight: 400;
      color: #333333;
      .value {
        font-size: 42px;
        font-weight: 600;
        color: #333333;
      }
    }
    .lineBox {
      width: 100%;
      height: 20px;
      display: flex;
      .lineBoxItem {
        height: 20px;
        border-radius: 10px;
        opacity: 0.7;
      }
    }
    .tableBox {
      width: 100%;
      display: flex;
      margin-top: 40px;
      overflow: hidden;
      .tableBoxItem {
        flex: 1;
        .item {
          width: 100%;
          height: 40px;
          display: flex;
          align-items: center;
          padding: 0 9px;
          .color {
            display: inline-block;
            width: 14px;
            height: 14px;
            border-radius: 2px;
          }
          .name {
            margin: 0 20px 0 10px;
            font-size: 14px;
            color: #333333;
          }
          .value {
            font-size: 14px;
            font-weight: 400;
            color: #666666;
          }
          .line {
            display: inline-block;
            width: 1px;
            height: 12px;
            margin: 0 20px;
            background: #cccccc;
          }
          .percent {
            font-size: 14px;
            font-weight: 400;
            color: #666666;
          }
        }
        .item:nth-child(odd) {
          background: #fafafa;
        }
        .item:nth-child(even) {
          background: #ffffff;
        }
      }
      .tableBoxItem:first-child {
        margin-right: 40px;
      }
    }
  }
}
.equipmentOperation::-webkit-scrollbar {
  display: none;
}
</style>
