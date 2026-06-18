<template>
  <div class="AggregationHistory" :style="{ height: allHeight ? allHeight + 'px' : '100%' }">
    <div class="history-head">
      <div>
        <p class="history-title">历史查询</p>
        <p class="history-subtitle">按结算、调节、运行、收益和出清价格维度追踪历史业务表现</p>
      </div>
      <div class="history-tabs">
        <button
          v-for="item in menuItems"
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
        :simulate="simulate"
        :refreshId="refreshId"
      ></userCompletes>
      <equipmentOperation
        v-if="showPage == '2'"
        :simulate="simulate"
        :refreshId="refreshId"
      ></equipmentOperation>
      <profitStatis
        v-if="showPage == '3'"
        :simulate="simulate"
        :refreshId="refreshId"
      ></profitStatis>
      <userProfit
        v-if="showPage == '4'"
        :simulate="simulate"
        :refreshId="refreshId"
      ></userProfit>
      <clearPrice
        v-if="showPage == '5'"
        :activeObj="activeObj"
        :simulate="simulate"
        :refreshId="refreshId"
      ></clearPrice>
      <incomeAccount
        v-if="showPage == '6'"
        :simulate="simulate"
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
      showPage: "6",
      refreshId: null,
      allHeight: 0,
      menuItems: [
        { index: "6", label: "收益结算" },
        { index: "1", label: "用户完成调节情况" },
        { index: "2", label: "设备运行情况" },
        { index: "3", label: "收益统计" },
        { index: "4", label: "用户收益统计" },
        { index: "5", label: "出清价格" },
      ],
    };
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
  watch: {
    activeObj: {
      deep: true,
      handler(val) {
        this.simulate = String(val.option.data.dataType);
      },
    },
    "activeObj.option.data.dataType": function() {
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
    this.simulate = String(this.activeObj.option.data.dataType);
  },
};
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
    min-width: 620px;
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
}
</style>
