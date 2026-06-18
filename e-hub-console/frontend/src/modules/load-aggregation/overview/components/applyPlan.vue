<template>
  <div class="applyPlan">
    <div class="applyPlan-left">
      <div class="commonHeader">
        <div class="text">申报计划</div>
        <div class="headerRight" @click="goApplyPlanDetail()">
          <span class="detailText">详情</span>
          <img class="detailImg" src="../images/right.png" alt="" />
        </div>
      </div>
      <div class="applyInfo">
        <tabbar @tabClick="tabClick($event)"></tabbar>
        <div class="priceBox">
          <!-- <div class="baojia">
            <div class="title">
              <div class="text">报</div>
              <div class="text">价</div>
            </div>
            <div class="baojiaBox">
              <el-tooltip
                class="item"
                effect="dark"
                :content="showPriceResourceTypeListString"
                placement="top-start"
              >
                <div>
                  <div
                    class="baojiaBox-item"
                    v-for="(item, index) in priceResourceTypeList"
                    :key="index"
                  >
                    <div class="baojiaBox-item-date">{{ item.date }}</div>
                    <div
                      class="baojiaBox-item-value"
                      v-for="(offer, index2) in item.offerList"
                      :key="index2"
                    >
                      <div class="time">
                        {{ offer.startTime }}～{{ offer.endTime }}
                      </div>
                      <div class="value">{{ offer.offer }}</div>
                      <div class="unit">
                        元/kWh
                        <span v-show="index2 < item.offerList.length - 1"
                          >、</span
                        >
                      </div>
                    </div>
                  </div>
                </div>
              </el-tooltip>
            </div>
          </div> -->
          <div class="plan">
            <div class="title">
              <div class="text">用</div>
              <div class="text">户</div>
              <div class="text">计</div>
              <div class="text">划</div>
            </div>
            <div class="chartBox">
              <ecline
                :unit="'kW'"
                :refreshId="refreshId"
                :ecdata="tomorrowOverviewData.chartList"
                style="height:388px"
                width="100%"
                height="100%"
              ></ecline>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="applyPlan-right">
      <div class="applyPlan-right-in">
        <div
          class="endApply"
          v-if="applyData.applyStatus === '3' && applyData.winStatus == null"
        >
          <div class="applyText">立即申报</div>
          <div class="applyTextTip">{{ applyData.applyContext }}</div>
        </div>
        <div
          class="canApply"
          v-if="applyData.applyStatus === '0' && applyData.winStatus == null"
        >
          <div class="applyText" @click="doApply()">立即申报</div>
          <div class="applyTextTip">{{ applyData.applyContext }}</div>
        </div>
        <div
          class="endApply"
          v-if="applyData.applyStatus === '2' && applyData.winStatus == null"
        >
          <div class="applyText">申报结束</div>
          <div class="applyTextTip">{{ applyData.applyContext }}</div>
        </div>
        <div class="winStatus" v-if="applyData.winStatus === '0'">
          <img src="../images/winfail.png" alt="" />
          <div class="winStatus-text">{{ applyData.applyContext }}</div>
        </div>
        <div class="winStatus" v-if="applyData.winStatus === '1'">
          <img src="../images/winsuccess.png" alt="" />
          <div class="winStatus-text" style="color: #0780ED">
            {{ applyData.applyContext }}
          </div>
        </div>
        <div class="contentItem">
          <div class="title">计划日</div>
          <div class="value">{{ applyData.planDate }}</div>
        </div>
        <div class="contentItem">
          <div class="title">申报资源类型</div>
          <el-tooltip
            :content="applyData.applyResourceType"
            placement="top"
          >
          <div class="value valueTip">{{ applyData.applyResourceType }}</div>
          </el-tooltip>
        </div>
        <div class="contentItem">
          <div class="title">计划参与用户</div>
          <div class="value">{{ applyData.applyYesNum }}家</div>
        </div>
        <div class="contentItem">
          <div class="title">报价</div>
          <div class="value">
            {{ applyData.applyPriceStatus === "0" ? "未提交" : "已提交" }}
          </div>
          <div class="goNext" @click="goApplyPage(applyData.applyPriceStatus)">
            <span>{{
              applyData.applyPriceStatus === "0" ? "进入" : "查看"
            }}</span>
            <img src="../images/blueright.png" alt="" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import tabbar from "./tabbar";
import ecline from "./ec_line";
import { sendWSPush } from "@/websocket";

import {
  getOverview,
  getAggregatorApply,
  updateAggregatorApply,
  getPriceByResourceTypeId,
  websocketUrl,
} from "../api";

export default {
  name: "applyPlan",
  components: {
    tabbar,
    ecline,
  },
  data() {
    return {
      tomorrowOverviewData: {},
      applyData: {},
      clickId: null,
      showPriceResourceTypeListString: "",
      priceResourceTypeList: [],
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
    getConfigResult(res) {
      if (res && res === "load-aggregator-business-3") {
        this.doGetTomorrowOverview(this.clickId);
      } else if (res && res === "load-aggregator-business-4") {
        this.doGetAggregatorApply();
      }
    },
    doGetPriceByResourceTypeId(resourceTypeId) {
      const query = {
        aggregatorId: this.aggregatorId,
        resourceTypeId,
      };
      getPriceByResourceTypeId(query).then(res => {
        if (res.data.code === 200) {
          let string = "";
          res.data.data.forEach(item => {
            let itemStr = "";
            if (!item.offerList) {
              item.offerList = [];
            }
            itemStr = `${itemStr + item.date}  `;
            item.offerList.forEach(offer => {
              if (offer.offer === null) {
                offer.offer = "--";
              }
              itemStr =
                `${itemStr + offer.startTime}~${offer.endTime} ${
                  offer.offer
                }元/kWh` + ` `;
            });
            string += itemStr;
          });
          this.showPriceResourceTypeListString = string;
          this.priceResourceTypeList = res.data.data;
        }
      });
    },
    goApplyPage(e) {
      this.$emit("goApplyPage", e);
    },
    doApply() {
      if (this.activeObj.option.data.canSet === "1") {
        return;
      }
      if (this.applyData.applyPriceStatus === "0") {
        this.$message({
          message: "请提交报价后再申报",
          type: "warning",
        });
        return;
      }
      const query = {
        aggregatorId: this.aggregatorId,
        applyBy: sessionStorage.getItem("ticket"),
        applyType: "1", //  0=自动申报，1=手动申报
      };
      updateAggregatorApply(query).then(res => {
        if (res.data.code === 200) {
          this.$message({
            message: "申报成功",
            type: "success",
          });
          this.doGetAggregatorApply();
        }
      });
    },
    goApplyPlanDetail() {
      this.$emit("goApplyPlanDetail");
    },
    tabClick(e) {
      this.clickId = e.id;
      this.doGetTomorrowOverview(e.id);
      this.doGetPriceByResourceTypeId(e.id);
    },
    doGetAggregatorApply() {
      const query = {
        aggregatorId: this.aggregatorId,
      };
      getAggregatorApply(query, this.simulate).then(res => {
        if (res.data.code === 200) {
          this.applyData = res.data.data;
          this.$forceUpdate();
        }
      });
    },
    doGetTomorrowOverview(resourceTypeId) {
      const query = {
        aggregatorId: this.aggregatorId,
        resourceTypeId,
        dayType: "tomorrow",
      };
      getOverview(query, this.simulate).then(res => {
        if (res.data.code === 200) {
          const chartList = [
            {
              name: "聚合申报功率",
              value: res.data.data.deliveryChart
                ? res.data.data.deliveryChart
                : [],
            },
            {
              name: "调度下发功率",
              value: res.data.data.dapChart ? res.data.data.dapChart : [],
            },
          ];
          res.data.data.chartList = chartList;
          this.tomorrowOverviewData = res.data.data;
        }
      });
    },
  },
  mounted() {
    // sendWSPush(`${websocketUrl}${this.aggregatorId}/${sessionStorage.getItem('openId')}/${sessionStorage.getItem('ticket')}`, this.getConfigResult)
  },
  created() {
    this.$bus.$on("log", content => {
      this.doGetAggregatorApply();
      this.doGetPriceByResourceTypeId(this.clickId);
    });
    this.aggregatorId = sessionStorage.getItem("entId");
    // sessionStorage.setItem('openId', '1253604095892152322')
    // sessionStorage.setItem('ticket', '12536040958921523221610525422256APP')
    // this.aggregatorId = '1330710231317684226'
    this.doGetAggregatorApply();
  },
};
</script>
<style lang="less"></style>
<style lang="less" type="text/less" scoped>
.applyPlan {
  width: calc(100% - 40px);
  height: 100%;
  background: #ffffff;
  border-radius: 12px;
  padding: 0 20px;
  display: flex;
  .applyPlan-left {
    width: calc(100% - 220px);
    max-width: calc(100% - 220px);
    margin-right: 20px;
  }
  .applyPlan-right {
    width: 200px;
    padding: 20px 0;
    .applyPlan-right-in {
      background: #f7fbff;
      border-radius: 12px;
      height: calc(100% - 20px);
      padding: 20px 20px 0;
      .winStatus {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        img {
          width: 82px;
          height: 59px;
          margin-bottom: 11px;
        }
        .winStatus-text {
          font-size: 14px;
          font-weight: 600;
          color: #666666;
        }
      }
      .canApply {
        width: auto;
        height: 84px;
        background: linear-gradient(339deg, #0780ed 0%, #11b7f7 100%);
        box-shadow: 0px 6px 10px 2px rgba(7, 128, 237, 0.32);
        border-radius: 12px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        cursor: pointer;
        .applyText {
          font-size: 22px;
          font-weight: 600;
          color: #ffffff;
          margin-bottom: 8px;
        }
        .applyTextTip {
          font-size: 14px;
          font-weight: 600;
          color: #ffffff;
          text-align: center;
        }
      }
      .endApply {
        width: auto;
        height: 84px;
        background: #d8d8d8;
        box-shadow: 0px 6px 10px 2px rgba(153, 153, 153, 0.1);
        border-radius: 12px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        cursor: pointer;
        .applyText {
          font-size: 22px;
          font-weight: 600;
          color: #ffffff;
          margin-bottom: 8px;
        }
        .applyTextTip {
          font-size: 14px;
          font-weight: 600;
          color: #ffffff;
          text-align: center;
        }
      }
      .contentItem {
        width: 100%;
        border-bottom: 1px solid #e8e8e8;
        .title {
          font-size: 14px;
          font-weight: 600;
          color: #999999;
          margin: 20px 0;
        }
        .value {
          font-size: 15px;
          font-weight: 600;
          color: #333333;
          margin-bottom: 20px;
        }
        .valueTip {
          // 超出 1 行显示...
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .goNext {
          display: flex;
          height: 14px;
          align-items: center;
          margin-bottom: 15px;
          cursor: pointer;
          span {
            font-size: 14px;
            font-family: PingFangSC-Regular, PingFang SC;
            color: #0780ed;
          }
          img {
            margin-left: 3px;
            width: 7px;
            height: 11px;
          }
        }
      }
      .contentItem:last-child {
        border-bottom: none;
      }
    }
  }
}
.applyInfo {
  width: 100%;
  .priceBox {
    width: 100%;
    height: 52px;
    background: #ffffff;
    // box-shadow: 0px 0px 6px 0px rgba(7, 128, 237, 0.11);
    border-radius: 6px;
    margin: 20px 0;
    .baojia {
      width: 100%;
      height: 52px;
      display: flex;
      .title {
        width: 30px;
        height: 100%;
        background: #f7fbff;
        border-radius: 6px;
        font-size: 14px;
        font-weight: 600;
        color: #6589a8;
        .text {
          width: 100%;
          text-align: center;
          margin-top: 8px;
        }
      }
      .baojiaBox {
        width: calc(100% - 50px);
        padding: 0 10px;
        margin-top: 12px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        .baojiaBox-item {
          display: inline-block;
          height: 30px;
          width: auto;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          .baojiaBox-item-date {
            background: #f7fbff;
            border-radius: 6px;
            height: 30px;
            padding: 0 16px;
            font-size: 14px;
            font-weight: 600;
            color: #666666;
            line-height: 30px;
            text-align: center;
            display: inline-block;
            margin-right: 2px;
          }
          .baojiaBox-item-value {
            display: inline-flex;
            align-items: center;
            .time {
              font-size: 14px;
              font-weight: 600;
              color: #999999;
            }
            .value {
              font-size: 16px;
              font-weight: 600;
              color: #333333;
              margin: 0 7px;
            }
            .unit {
              font-size: 14px;
              font-weight: 600;
              color: #999999;
            }
          }
        }
      }
    }
    .plan {
      width: 100%;
      height: 388px;
      margin-top: 20px;
      display: flex;
      .title {
        width: 30px;
        height: calc(100% - 110px);
        background: #f7fbff;
        border-radius: 6px;
        font-size: 14px;
        font-weight: 600;
        color: #6589a8;
        padding-top: 110px;
        .text {
          width: 100%;
          text-align: center;
          margin-top: 7px;
        }
      }
      .chartBox {
        flex: 1;
      }
    }
  }
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
</style>
