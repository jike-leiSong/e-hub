<template>
  <div class="AggregationHistory" :style="{ height: allHeight ? allHeight + 'px' : '100%' }">
    <div class="history-head">
      <div>
        <p class="history-title">{{ pageConfig.title }}</p>
        <p class="history-subtitle">{{ pageConfig.subtitle }}</p>
      </div>
      <div v-if="visibleMenuItems.length > 1" class="history-tabs">
        <button
          v-for="item in visibleMenuItems"
          :key="item.index"
          type="button"
          class="history-tab"
          :class="{ active: showPage === item.index }"
          @click="select(item.index)"
        >
          {{ item.label }}
        </button>
      </div>
    </div>
    <div class="history-body">
      <userCompletes
        v-if="showPage == '1'"
        :refreshId="refreshId"
      ></userCompletes>
      <equipmentOperation
        v-if="showPage == '2'"
        :refreshId="refreshId"
      ></equipmentOperation>
      <profitStatis
        v-if="showPage == '3'"
        :refreshId="refreshId"
      ></profitStatis>
      <userProfit
        v-if="showPage == '4'"
        :refreshId="refreshId"
      ></userProfit>
      <clearPrice
        v-if="showPage == '5'"
        :activeObj="activeObj"
        :refreshId="refreshId"
      ></clearPrice>
      <incomeAccount
        v-if="showPage == '6'"
        :activeObj="activeObj"
        :refreshId="refreshId"
      ></incomeAccount>
    </div>
  </div>
</template>
<script>
import profitStatis from "../components/profitStatis";
import equipmentOperation from "../components/equipmentOperation";
import userCompletes from "../components/userCompletes";
import userProfit from "../components/userProfit";
import clearPrice from "../components/clearPrice";
import incomeAccount from "../components/incomeAccount";

export default {
  name: "AggregationHistory",
  components: {
    profitStatis,
    equipmentOperation,
    userCompletes,
    userProfit,
    clearPrice,
    incomeAccount,
  },
  data() {
    return {
      showPage: defaultShowPage(this.viewType),
      refreshId: null,
      allHeight: 0,
      menuItems: [
        { index: "1", label: "调节情况", group: "adjustment" },
        { index: "6", label: "收益结算", group: "settlement" },
        { index: "3", label: "收益统计" },
        { index: "4", label: "用户收益统计" },
        { index: "5", label: "出清价格" },
        { index: "2", label: "物联数据", group: "device-operation" },
      ],
    };
  },
  props: {
    viewType: {
      type: String,
      default: "settlement",
    },
    activeObj: {
      type: Object,
      require: true,
    },
    activeCompName: {
      type: Array,
      require: false,
    },
  },
  computed: {
    pageConfig() {
      return pageConfigs[this.viewType] || pageConfigs.settlement;
    },
    visibleMenuItems() {
      if (this.viewType === "settlement") {
        return this.menuItems.filter(item => ["6", "3", "4", "5"].includes(item.index));
      }
      return this.menuItems.filter(item => item.index === this.showPage);
    },
  },
  watch: {
    viewType(val) {
      this.showPage = defaultShowPage(val);
      this.doGetData();
    },
    activeCompName(val) {
      if (val instanceof Array && val && val[0] === "AggregationHistory") {
        this.refreshId = new Date().getTime();
      }
    },
  },
  methods: {
    doGetData() {
      this.refreshId = new Date().getTime();
    },
    select(index) {
      this.showPage = index;
    },
  },
  mounted() {
    const dom = document.getElementsByClassName("pc-container");
    if (dom.length > 0) {
      this.allHeight = dom[0].offsetHeight - 44;
    }
  },
  created() {
    this.refreshId = new Date().getTime();
    // sessionStorage.setItem("entId", '1711340903453614082')
    this.aggregatorId =
      sessionStorage.getItem("entId") || sessionStorage.getItem("cid");
  },
};

const pageConfigs = {
  adjustment: {
    title: "调节情况",
    subtitle: "查看聚合商汇总功率和各企业调节曲线，支持时间段查询、导出和上送数据导出",
  },
  settlement: {
    title: "收益结算",
    subtitle: "汇总收益统计、用户收益统计和出清价格，支撑运营结算与对账",
  },
  "device-operation": {
    title: "物联管理 / 物联数据",
    subtitle: "查看各企业、各设备、各测点的历史运行数据，支持时间段查询",
  },
};

function defaultShowPage(viewType) {
  if (viewType === "adjustment") {
    return "1";
  }
  if (viewType === "device-operation") {
    return "2";
  }
  return "6";
}
</script>
<style lang="less" type="text/less" scoped>
.AggregationHistory {
  width: 100%;
  position: relative;
  display: flex;
  flex-direction: column;
  background: transparent;
  overflow: hidden;
  .history-head {
    min-height: 92px;
    padding: 18px 20px;
    background: #ffffff;
    border: 1px solid #dde6ed;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 20px;
    .history-title {
      margin: 0;
      color: #0e2638;
      font-size: 20px;
      font-weight: 700;
    }
    .history-subtitle {
      margin: 8px 0 0;
      color: #607d8f;
      font-size: 13px;
    }
  }
  .history-tabs {
    max-width: 620px;
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    gap: 8px;
  }
  .history-tab {
    height: 34px;
    padding: 0 14px;
    border: 1px solid #cfdce5;
    border-radius: 6px;
    background: #f7fbff;
    color: #334e5c;
    font-size: 14px;
    cursor: pointer;
  }
  .history-tab.active,
  .history-tab:hover {
    border-color: #0780ed;
    background: #0780ed;
    color: #ffffff;
  }
  .history-body {
    flex: 1;
    min-height: 0;
    margin-top: 16px;
    overflow: auto;
  }
  ::v-deep .el-date-editor.el-range-editor,
  ::v-deep .el-date-editor.el-range-editor.el-input__inner {
    width: 330px !important;
    max-width: 100%;
    height: 32px !important;
    padding: 3px 10px;
    border-radius: 6px;
    display: inline-flex;
    align-items: center;
    box-sizing: border-box;
  }
  ::v-deep .el-date-editor .el-range-input {
    flex: 1 1 0;
    min-width: 0;
    width: auto !important;
    height: 24px;
    line-height: 24px;
  }
  ::v-deep .el-date-editor .el-range-separator {
    flex: 0 0 24px;
    width: 24px !important;
    min-width: 24px;
    padding: 0;
    color: #607d8f;
    line-height: 24px !important;
    text-align: center;
  }
  ::v-deep .el-date-editor .el-range__icon,
  ::v-deep .el-date-editor .el-range__close-icon,
  ::v-deep .el-date-editor .el-input__icon {
    flex: 0 0 18px;
    width: 18px;
    line-height: 24px !important;
  }
}
</style>
