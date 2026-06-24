<template>
  <div class="lastApply">
    <div class="lastApply-left">
      <div class="title">上次申报总收益</div>
      <div class="date">
        统计日：{{
          getLastProfitData.totalProfitTime
            ? getLastProfitData.totalProfitTime
            : "--"
        }}
      </div>
    </div>
    <div class="lastApply-right">
      <div class="numBox">
        <div
          class="numItem"
          v-for="(item, index) in getLastProfitData.totalProfitList"
          :key="index"
        >
          {{ item }}
        </div>
        <span class="unit">元</span>
      </div>
      <div class="goDetail" @click="goDetail()">
        <span>详情</span>
        <img src="../images/right.png" alt="" />
      </div>
    </div>
  </div>
</template>
<script>
import { getLastProfit } from "../api";

export default {
  name: "lastApply",
  components: {
    // loading,
  },
  data() {
    return {
      getLastProfitData: {},
    };
  },
  props: {
    activeObj: {
      type: Object,
      require: true,
    },
  },
  methods: {
    goDetail() {
      this.$emit("goDetail", "profitStatics");
    },
    doGetLastProfit() {
      getLastProfit(
        {
          aggregatorId: sessionStorage.getItem("entId"),
        }
      ).then(res => {
        if (res.data.code === 200) {
          if (res.data.data.totalProfit === null) {
            return;
          }
          res.data.data.totalProfitList = String(
            res.data.data.totalProfit
          ).split("");
          this.getLastProfitData = res.data.data;
        }
      });
    },
  },
  created() {
    this.doGetLastProfit();
  },
};
</script>
<style lang="less"></style>
<style lang="less" type="text/less" scoped>
.lastApply {
  width: 100%;
  height: 100%;
  background: #ffffff;
  border-radius: 12px;
  display: flex;
  .lastApply-left {
    width: 146px;
    background: #f7fbff;
    display: flex;
    flex-direction: column;
    padding-left: 20px;
    justify-content: center;
    .title {
      font-size: 18px;
      font-weight: 600;
      color: #333333;
      margin-bottom: 12px;
    }
    .date {
      font-size: 14px;
      font-weight: 400;
      color: #999999;
    }
  }
  .lastApply-right {
    flex: 1;
    display: flex;
    align-items: center;
    padding-left: 18px;
    position: relative;
    .numBox {
      display: flex;
      .numItem {
        width: 26px;
        height: 35px;
        line-height: 35px;
        background-size: 100%;
        background-repeat: no-repeat;
        background-image: url("../images/numbg.png");
        text-align: center;
        color: #fff;
        font-size: 19px;
      }
      .unit {
        font-size: 16px;
        font-weight: 600;
        color: #333333;
        margin-top: 19px;
        margin-left: 4px;
      }
    }
    .goDetail {
      position: absolute;
      height: 20px;
      display: flex;
      align-items: center;
      right: 19px;
      top: 10px;
      cursor: pointer;
      span {
        font-size: 14px;
        font-family: PingFangSC-Regular, PingFang SC;
        font-weight: 400;
        color: #666666;
        margin-right: 3px;
      }
      img {
        width: 7px;
        height: 11px;
      }
    }
  }
}
</style>
