<template>
  <div class="applyPlan">
    <div class="commonHeader">
      <div class="titleBlock">
        <div class="text">明日申报</div>
        <span>{{ resourceTypeName || "当前资源类型" }}</span>
      </div>
      <div class="headerActions">
        <button
          type="button"
          class="headerAction primary"
          @click="goApplyPage(applyData.applyPriceStatus)"
        >
          申报计划
        </button>
        <button type="button" class="headerAction" @click="goApplyPlanDetail()">
          <span>明日详情</span>
          <img src="../images/right.png" alt="" />
        </button>
      </div>
    </div>

    <div class="applyWorkbench">
      <div class="applyMain">
        <div class="applySummary">
          <div class="summaryItem">
            <span>申报状态</span>
            <strong>{{ applyStatusText }}</strong>
          </div>
          <div class="summaryItem">
            <span>计划日</span>
            <strong>{{ planDateText }}</strong>
          </div>
          <div class="summaryItem">
            <span>申报资源</span>
            <strong>{{ resourceTypeText }}</strong>
          </div>
          <div class="summaryItem">
            <span>计划用户</span>
            <strong>{{ applyUserText }}家</strong>
          </div>
          <div class="summaryItem">
            <span>申报准备</span>
            <strong>{{ preparationStatusText }}</strong>
          </div>
        </div>

        <div class="chartBox">
          <ecline
            :unit="'kW'"
            :refreshId="refreshId"
            :ecdata="tomorrowOverviewData.chartList"
            style="height:250px"
            width="100%"
            height="100%"
          ></ecline>
        </div>
      </div>

      <aside class="applyActionPanel">
        <button
          type="button"
          class="primaryApply"
          :class="{ disabled: !isPrimaryActionActive }"
          :disabled="!isPrimaryActionActive"
          @click="handlePrimaryAction"
        >
          {{ actionText }}
        </button>
        <div class="actionTip">{{ applyContextText }}</div>
        <div class="actionRows">
          <div class="actionRow">
            <span>申报准备</span>
            <strong>{{ preparationStatusText }}</strong>
            <button type="button" @click="goApplyPage(applyData.applyPriceStatus)">
              {{ needsApplyPreparation ? "去处理" : "查看" }}
            </button>
          </div>
          <div class="actionRow">
            <span>计划用户</span>
            <strong>{{ applyUserText }}家</strong>
          </div>
          <div class="actionRow">
            <span>资源类型</span>
            <strong>{{ resourceTypeText }}</strong>
          </div>
        </div>
        <div class="winStatus" v-if="applyData.winStatus === '0' || applyData.winStatus === '1'">
          <img v-if="applyData.winStatus === '0'" src="../images/winfail.png" alt="" />
          <img v-else src="../images/winsuccess.png" alt="" />
          <div class="winStatus-text" :class="{ success: applyData.winStatus === '1' }">
            {{ applyData.applyContext }}
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>
<script>
import ecline from "./ec_line";
import { sendWSPush } from "@/websocket";

import {
  getOverview,
  getAggregatorApply,
  updateAggregatorApply,
  getPriceByResourceTypeId,
  websocketUrl,
} from "../api";

function resolveAggregatorId() {
  return (
    sessionStorage.getItem("aggregatorId") ||
    sessionStorage.getItem("entId") ||
    sessionStorage.getItem("cid") ||
    ""
  );
}

export default {
  name: "applyPlan",
  components: {
    ecline,
  },
  data() {
    return {
      aggregatorId: resolveAggregatorId(),
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
  computed: {
    applyStatusText() {
      const statusMap = {
        0: "可申报",
        1: "已申报",
        2: "申报结束",
        3: "未开始",
      };
      return statusMap[String(this.applyData.applyStatus)] || "--";
    },
    priceStatusText() {
      if (this.applyData.applyPriceStatus === null || this.applyData.applyPriceStatus === undefined || this.applyData.applyPriceStatus === "") {
        return "--";
      }
      return String(this.applyData.applyPriceStatus) === "1" ? "已提交" : "未提交";
    },
    preparationStatusText() {
      if (!this.hasApplyPlan) {
        return "待完善";
      }
      return this.priceStatusText;
    },
    planDateText() {
      return this.applyData.planDate || "--";
    },
    resourceTypeText() {
      return this.applyData.applyResourceType || this.resourceTypeName || "--";
    },
    applyUserText() {
      return this.formatNumber(this.applyData.applyYesNum);
    },
    hasApplyPlan() {
      const userCount = Number(this.applyData.applyYesNum);
      const hasUser = Number.isFinite(userCount) && userCount > 0;
      const resourceType = String(this.applyData.applyResourceType || this.resourceTypeName || "");
      return hasUser && resourceType !== "无";
    },
    needsApplyPreparation() {
      return String(this.applyData.applyStatus) === "0"
        && this.applyData.winStatus == null
        && (!this.hasApplyPlan || String(this.applyData.applyPriceStatus) === "0");
    },
    isPrimaryActionActive() {
      return this.canApply || this.needsApplyPreparation;
    },
    canApply() {
      return String(this.applyData.applyStatus) === "0"
        && this.applyData.winStatus == null
        && !this.needsApplyPreparation;
    },
    actionText() {
      if (this.needsApplyPreparation) {
        return "去完善申报";
      }
      if (this.canApply) {
        return "立即申报";
      }
      if (String(this.applyData.applyStatus) === "1") {
        return "已申报";
      }
      if (String(this.applyData.applyStatus) === "2") {
        return "申报结束";
      }
      if (String(this.applyData.applyStatus) === "3") {
        return "未开始";
      }
      return "暂无操作";
    },
    applyContextText() {
      return this.applyData.applyContext || this.applyStatusText;
    },
  },
  methods: {
    formatNumber(value) {
      if (value === null || value === undefined || value === "") {
        return "--";
      }
      const numberValue = Number(value);
      if (!Number.isFinite(numberValue)) {
        return value;
      }
      return numberValue.toLocaleString("zh-CN", {
        maximumFractionDigits: 2,
      });
    },
    getConfigResult(res) {
      if (res && res === "load-aggregator-business-3") {
        this.doGetTomorrowOverview(this.resourceTypeId);
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
    handlePrimaryAction() {
      if (!this.isPrimaryActionActive) {
        return;
      }
      if (this.needsApplyPreparation) {
        this.goApplyPage(this.applyData.applyPriceStatus);
        return;
      }
      if (this.canApply) {
        this.doApply();
      }
    },
    doApply() {
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
    doGetAggregatorApply() {
      const query = {
        aggregatorId: this.aggregatorId,
        resourceTypeId: this.resourceTypeId,
      };
      getAggregatorApply(query).then(res => {
        if (res.data.code === 200) {
          this.applyData = res.data.data;
          this.$forceUpdate();
        }
      });
    },
    doGetTomorrowOverview(resourceTypeId) {
      if (!resourceTypeId) {
        this.tomorrowOverviewData = {};
        return;
      }
      const query = {
        aggregatorId: this.aggregatorId,
        resourceTypeId,
        dayType: "tomorrow",
      };
      getOverview(query).then(res => {
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
      this.doGetPriceByResourceTypeId(this.resourceTypeId);
    });
    // sessionStorage.setItem('openId', '1253604095892152322')
    // sessionStorage.setItem('ticket', '12536040958921523221610525422256APP')
    // this.aggregatorId = '1330710231317684226'
  },
  watch: {
    resourceTypeId: {
      immediate: true,
      handler(value) {
        this.clickId = value;
        this.doGetAggregatorApply();
        this.doGetTomorrowOverview(value);
        if (value) {
          this.doGetPriceByResourceTypeId(value);
        }
      },
    },
  },
};
</script>
<style lang="less"></style>
<style lang="less" type="text/less" scoped>
.applyPlan {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  background: #ffffff;
  border-radius: 8px;
  padding: 0 20px 20px;
  display: flex;
  flex-direction: column;
}

.commonHeader {
  display: flex;
  height: 58px;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  .titleBlock {
    display: flex;
    align-items: baseline;
    gap: 12px;
    min-width: 0;
    span {
      color: #607d8f;
      font-size: 13px;
    }
  }
  .text {
    font-size: 18px;
    font-weight: 600;
    color: #333333;
  }
  .headerActions {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  .headerAction {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 86px;
    height: 32px;
    padding: 0 12px;
    border: 1px solid #d8e6f0;
    border-radius: 6px;
    background: #ffffff;
    color: #456577;
    cursor: pointer;
    span {
      font-size: 14px;
      font-weight: 400;
    }
    img {
      margin-left: 3px;
      width: 7px;
      height: 11px;
    }
  }
  .headerAction.primary {
    border-color: #0780ed;
    background: #0780ed;
    color: #ffffff;
  }
}

.applyWorkbench {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 18px;
  flex: 1;
  min-height: 0;
}

.applyMain,
.applyActionPanel {
  min-width: 0;
  min-height: 0;
}

.applyMain {
  display: flex;
  flex-direction: column;
}

.applySummary {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.summaryItem {
  min-width: 0;
  min-height: 74px;
  padding: 12px 14px;
  box-sizing: border-box;
  border: 1px solid #e7eef4;
  border-radius: 8px;
  background: #f7fbff;
  span,
  strong {
    display: block;
  }
  span {
    color: #607d8f;
    font-size: 13px;
  }
  strong {
    margin-top: 12px;
    color: #0e2638;
    font-size: 18px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.chartBox {
  flex: 1;
  min-height: 0;
  margin-top: 12px;
}

.applyActionPanel {
  display: flex;
  flex-direction: column;
  padding: 16px;
  box-sizing: border-box;
  border: 1px solid #e7eef4;
  border-radius: 8px;
  background: #f7fbff;
}

.primaryApply {
  width: 100%;
  height: 64px;
  border: none;
  border-radius: 8px;
  background: #0780ed;
  color: #ffffff;
  font-size: 20px;
  font-weight: 600;
  cursor: pointer;
}

.primaryApply.disabled {
  background: #8ba2b3;
  cursor: not-allowed;
}

.actionTip {
  min-height: 38px;
  margin-top: 12px;
  color: #607d8f;
  font-size: 13px;
  line-height: 19px;
}

.actionRows {
  margin-top: 10px;
  border-top: 1px solid #dce7ef;
}

.actionRow {
  display: grid;
  grid-template-columns: 76px minmax(0, 1fr) auto;
  align-items: center;
  min-height: 48px;
  border-bottom: 1px solid #dce7ef;
  gap: 8px;
  span {
    color: #607d8f;
    font-size: 13px;
  }
  strong {
    min-width: 0;
    color: #0e2638;
    font-size: 15px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  button {
    border: none;
    background: transparent;
    color: #0780ed;
    cursor: pointer;
  }
}

.winStatus {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: auto;
  padding-top: 14px;
  img {
    width: 36px;
    height: auto;
  }
  .winStatus-text {
    color: #607d8f;
    font-size: 13px;
    line-height: 18px;
  }
  .success {
    color: #0780ed;
  }
}

@media (max-width: 1280px) {
  .applyWorkbench {
    grid-template-columns: 1fr;
  }

  .applySummary {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
