<template>
  <div class="applyPrice">
    <div class="goBackHeader">
      <div class="goBackHeaderIn" @click="goBack()">
        <img src="../images/left.png" alt="" />
        <span class="text">调峰辅助服务度电价格申报</span>
      </div>
    </div>
    <div class="applyPriceInfo">
      <div class="applyPriceInfo-left" ref="applyPriceInfoBox">
        <div
          class="applyPriceInfo-item"
          v-for="(item, index1) in aggregatorApplyOfferRespData"
          :key="index1"
        >
          <div class="applyPriceInfo-item-header">
            <span class="item-header-text">{{ item.resourceTypeName }}</span>
            <div class="right" @click="openChart(item)">
              <span class="right-text">用户申报汇总功率</span>
              <img
                :class="{ showLine: item.showChart }"
                src="../images/top.png"
                alt=""
              />
            </div>
          </div>
          <div class="echartBox" v-if="item.showChart" v-loading="item.loading">
            <ecline
              :unit="'kW'"
              :backgroundColor="'#F7FBFF'"
              :ecdata="item.deliveryChart"
              style="height:280px"
              width="100%"
              height="100%"
            ></ecline>
          </div>
          <div class="isprice">
            <div class="ispriceIn">
              <span class="isprice-text">本次是否报价</span>
              <div
                class="btn"
                @click="changeApplyStatus(item, true)"
                :class="{ btnActive: item.status }"
              >
                是
              </div>
              <div
                class="btn"
                @click="changeApplyStatus(item, false)"
                :class="{ btnActive: !item.status }"
              >
                否
              </div>
            </div>
          </div>
          <div class="applyPriceBox" v-show="item.status">
            <div class="applyPriceBox-title">申报价格</div>
            <div
              class="applyPriceBox-item"
              v-for="(date, index2) in item.dateList"
              :key="index2"
            >
              <div class="applyPriceBox-item-date">{{ date.date }}</div>
              <div class="applyPriceBox-item-info">
                <div class="applyPriceBox-item-info-header">
                  <el-radio-group v-model="date.status">
                    <el-radio :label="true">报价</el-radio>
                    <el-radio :label="false">不报价</el-radio>
                  </el-radio-group>
                </div>
                <div
                  class="applyPriceBox-item-info-content"
                  v-if="date.offerList && date.status"
                >
                  <div
                    class="applyPriceBox-item-info-content-item"
                    v-for="(offer, index3) in date.offerList"
                    :key="index3"
                  >
                    <el-time-select
                      placeholder="起始时间"
                      :class="{ redBorder: offer.startTimeRed }"
                      v-model="offer.startTime"
                      @change="
                        changeStartTime(offer.startTime, date.offerList, index3)
                      "
                      :disabled="offer.startTime === '00:00' && index3 === 0"
                      :picker-options="{
                        start: '00:00',
                        step: '00:15',
                        end: '24:00',
                        minTime: date.offerList[index3 - 1]
                          ? date.offerList[index3 - 1].endTime
                          : null,
                        maxTime: offer.endTime,
                      }"
                    >
                    </el-time-select>
                    <div class="line"></div>
                    <el-time-select
                      :class="{ redBorder: offer.endTimeRed }"
                      placeholder="结束时间"
                      v-model="offer.endTime"
                      :disabled="
                        offer.endTime === '24:00' &&
                          index3 === date.offerList.length - 1
                      "
                      @change="
                        changeEndTime(offer.endTime, date.offerList, index3)
                      "
                      :picker-options="{
                        start: '00:00',
                        step: '00:15',
                        end: '24:00',
                        minTime: offer.startTime,
                        maxTime: date.offerList[index3 + 1]
                          ? date.offerList[index3 + 1].startTime
                          : null,
                      }"
                    >
                    </el-time-select>
                    <div class="lineS"></div>
                    <el-input
                      placeholder="请输入"
                      type="number"
                      :class="{ redBorder: offer.isEmpty }"
                      v-model="offer.offer"
                      min="0"
                      @blur="blurInput(offer.offer, date.offerList, index3)"
                    >
                      <template slot="append">元/kWh</template>
                    </el-input>
                    <img
                      class="addPng"
                      @click="doAdd(date.offerList, index3)"
                      src="../images/add.png"
                      alt=""
                      v-if="
                        !(
                          index3 === date.offerList.length - 1 &&
                          offer.endTime === '24:00'
                        ) || date.offerList.length === 1
                      "
                    />
                    <img
                      class="reducePng"
                      v-if="index3 > 0 && date.offerList.length > 1"
                      @click="doReduce(date.offerList, index3)"
                      src="../images/reduce.png"
                      alt=""
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="bottomBtn" v-if="applyPriceStatus === '0' && showBtn">
          <div
            class="saveBtn"
            :class="{ disabledBtn: activeObj.option.data.canSet === '1' }"
            @click="save()"
          >
            暂存
          </div>
          <div
            class="applyBtn"
            :class="{ disabledBtn: activeObj.option.data.canSet === '1' }"
            @click="doApply()"
          >
            提交
          </div>
        </div>
      </div>
      <div class="applyPriceInfo-weather">
        <div class="weather-top">
          <div class="date">
            <img
              src="../images/left.png"
              alt=""
              v-if="showApplyIndex > 0"
              @click="lastDate()"
            />
            <div class="date-text">{{ showApplyDate }}</div>
            <img
              src="../images/right.png"
              alt=""
              v-if="showApplyIndex < applyDateList.length - 1"
              @click="nextDate()"
            />
          </div>
          <div class="weatherBox">
            <div class="imgBox" v-if="weatherData && weatherData.condition_day">
              <img
                src="../images/duoyun.png"
                v-if="weatherData.condition_day === '多云'"
                alt=""
              />
              <img
                src="../images/dawu.png"
                v-if="
                  weatherData.condition_day.indexOf('雾') > -1 &&
                    weatherData.condition_day !== '雾霾'
                "
                alt=""
              />
              <img
                src="../images/xue.png"
                v-if="weatherData.condition_day.indexOf('雪') > -1"
                alt=""
              />
              <img
                src="../images/xiaoyu.png"
                v-if="weatherData.condition_day.indexOf('雨') > -1"
                alt=""
              />
              <img
                src="../images/qing.png"
                v-if="weatherData.condition_day.indexOf('晴') > -1"
                alt=""
              />
              <img
                src="../images/yin.png"
                v-if="weatherData.condition_day.indexOf('阴') > -1"
                alt=""
              />
              <img
                src="../images/wumai.png"
                v-if="weatherData.condition_day.indexOf('雾霾') > -1"
                alt=""
              />
            </div>
            <div
              class="weatherInfo"
              v-if="weatherData && weatherData.condition_day"
            >
              <div class="tem">
                {{ weatherData.temp_day }}/{{ weatherData.temp_night }}℃
              </div>
              <div class="condition">{{ weatherData.condition_day }}</div>
              <div class="win">
                {{ weatherData.wind_dir_day }}{{ weatherData.wind_speed_day }}级
              </div>
            </div>
            <div class="noWeather" v-if="weatherData === 'nodata'">
              暂无天气数据
            </div>
          </div>
        </div>
        <div class="nav">
          <div class="line"></div>
          <div
            class="nav-item"
            @click="goNav(item, index1)"
            :class="{ active: selNavId === item.resourceTypeId }"
            v-for="(item, index1) in aggregatorApplyOfferRespData"
            :key="index1"
          >
            <span class="tip"></span>
            <span class="text">{{ item.resourceTypeName }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
// import ecbar from "./ec_bar";
import ecline from "./ec_line";
import {
  getAggregatorApplyOfferResp,
  getDayWeather,
  saveAggregatorApplyOffer,
  getAggregatorDeliveryChart,
  getApplyDateList,
  submitAggregatorApplyOffer,
} from "../api";

export default {
  name: "applyPrice",
  components: {
    // ecbar,
    ecline,
  },
  data() {
    return {
      aggregatorApplyOfferRespData: [],
      applyDateList: [],
      showApplyDate: null,
      showApplyIndex: 0,
      selNavId: null,
      weatherData: {},
      showBtn: true,
    };
  },
  props: {
    simulate: {
      type: String,
      require: true,
    },
    activeObj: {
      type: Object,
      require: true,
    },
    applyPriceStatus: {
      type: String,
      require: true,
    },
  },
  methods: {
    goNav(item, index) {
      this.selNavId = item.resourceTypeId;
      const doms = document.getElementsByClassName("applyPriceInfo-item");
      let heightValue = 0;
      Array.prototype.forEach.call(doms, (element, indexValue) => {
        if (indexValue < index) {
          heightValue += element.offsetHeight;
        }
      });
      const container = this.$refs.applyPriceInfoBox; //需要滚动的目标
      container.scrollTop = heightValue;
    },
    doGetDayWeather() {
      // const query = {
      //   endTime: '2020-11-18 23:00:00',
      //   startTime: '2020-11-18 00:00:00',
      //   stationId: sessionStorage.getItem('systemCode')
      // }
      const query = {
        endTime: moment(this.showApplyDate).format("YYYY-MM-DD 23:00:00"),
        startTime: moment(this.showApplyDate).format("YYYY-MM-DD 00:00:00"),
        stationId: sessionStorage.getItem("systemCode"),
      };
      getDayWeather(query, this.simulate).then(res => {
        if (res.data.code === 200) {
          if (res.data.data.length > 0) {
            this.weatherData = res.data.data[0].result[0];
          }
        } else {
          this.weatherData = "nodata";
        }
      });
    },
    lastDate() {
      this.showApplyIndex--;
      this.showApplyDate = this.applyDateList[this.showApplyIndex];
      this.doGetDayWeather();
    },
    nextDate() {
      this.showApplyIndex++;
      this.showApplyDate = this.applyDateList[this.showApplyIndex];
      this.doGetDayWeather();
    },
    doGetApplyDateList() {
      getApplyDateList().then(res => {
        if (res.data.code === 200) {
          this.applyDateList = res.data.data;
          if (this.applyDateList.length > 0) {
            this.showApplyDate = this.applyDateList[0];
            this.showApplyIndex = 0;
            this.doGetDayWeather();
          }
        }
      });
    },
    openChart(item) {
      item.showChart = !item.showChart;
      this.$forceUpdate();
      if (item.showChart) {
        this.doGetAggregatorDeliveryChart(item);
      }
    },
    goBack() {
      this.$emit("goBack");
    },
    doGetAggregatorDeliveryChart(item) {
      item.loading = true;
      const query = {
        aggregatorId: this.aggregatorId,
        resourceTypeId: item.resourceTypeId,
      };
      getAggregatorDeliveryChart(query, this.simulate).then(res => {
        if (res.data.code === 200) {
          item.deliveryChart = [
            { name: "用户申报汇总功率", value: res.data.data.deliveryChart },
          ];
        }
        item.loading = false;
        this.$forceUpdate();
      });
    },
    getMinuts(data) {
      if (!data) {
        return;
      }
      const timeDataList = data.split(":");
      return Number(timeDataList[0] * 60) + Number(timeDataList[1]);
    },
    doApply() {
      if (this.activeObj.option.data.canSet === "1") {
        return;
      }
      let canSave = true;
      this.aggregatorApplyOfferRespData.forEach(item => {
        if (item.status) {
          item.dateList.forEach(date => {
            if (date.status) {
              date.offerList.forEach((offer, index) => {
                if (offer.offer === null || offer.offer === "") {
                  canSave = false;
                  offer.isEmpty = true;
                  this.$message.error("请完善填写内容");
                  this.$forceUpdate;
                }
                if (!offer.startTime) {
                  canSave = false;
                  offer.startTimeRed = true;
                  this.$message.error("请完善填写内容");
                }
                if (!offer.endTime) {
                  canSave = false;
                  offer.endTimeRed = true;
                  this.$message.error("请完善填写内容");
                }
                if (
                  this.getMinuts(offer.startTime) >=
                  this.getMinuts(offer.endTime)
                ) {
                  offer.endTimeRed = true;
                  offer.startTimeRed = true;
                  canSave = false;
                  this.$message.error("开始时间不可大于结束时间");
                }
                if (
                  date.offerList[index + 1] &&
                  offer.endTime &&
                  date.offerList[index + 1].startTime &&
                  offer.endTime !== date.offerList[index + 1].startTime
                ) {
                  canSave = false;
                  this.$message.error("请填写完整的时间段");
                }
                if (offer.offer && Number(offer.offer) < 0) {
                  canSave = false;
                  offer.isEmpty = true;
                  this.$message.error("申报价格不可小于0元/kWh");
                }
              });
            }
          });
        }
      });
      if (!canSave) {
        return;
      }
      this.$confirm("提交后，所有内容将不可修改", "您确定要提交报价吗?", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          const query = {
            aggregatorId: this.aggregatorId,
            resourceList: this.aggregatorApplyOfferRespData,
          };
          submitAggregatorApplyOffer(query).then(res => {
            if (res.data.code === 200) {
              this.$message({
                message: "提交成功",
                type: "success",
              });
              this.showBtn = false;
              this.$bus.$emit("log", "");
            }
          });
        })
        .catch(() => {});
    },
    save() {
      if (this.activeObj.option.data.canSet === "1") {
        return;
      }
      const query = {
        aggregatorId: this.aggregatorId,
        resourceList: this.aggregatorApplyOfferRespData,
      };
      saveAggregatorApplyOffer(query).then(res => {
        if (res.data.code === 200) {
          this.$message({
            message: "暂存成功",
            type: "success",
          });
        }
      });
    },
    changeStartTime(time, data, index) {
      if (!time) {
        return;
      }
      if (data[index - 1]) {
        data[index - 1].endTime = time;
      }
    },
    changeEndTime(time, data, index) {
      if (!time) {
        return;
      }
      if (data[index + 1]) {
        data[index + 1].startTime = time;
      }
    },
    doAdd(item, index) {
      const objData = {
        aggregatorId: "",
        date: "",
        endTime: "",
        offer: "",
        resourceTypeId: "",
        startTime: "",
      };
      if (item.length > 0) {
        objData.aggregatorId = item[0].aggregatorId;
        objData.date = item[0].date;
        objData.resourceTypeId = item[0].resourceTypeId;
      }
      item.splice(index + 1, 0, objData);
      item[item.length - 1].endTime = "24:00";
    },
    doReduce(item, index) {
      item.splice(index, 1);
      if (
        item[index - 1] &&
        item[index] &&
        item[index - 1].offer !== "" &&
        item[index].offer !== "" &&
        Number(item[index - 1].offer) === Number(item[index].offer)
      ) {
        this.$alert("该时段与相邻时段的功率值相同，请合并填写", {
          confirmButtonText: "确定",
          callback: action => {
            item[index].offer = null;
          },
        });
      }
      item[item.length - 1].endTime = "24:00";
    },
    blurInput(num, data, index) {
      if (!num && num !== 0) {
        return;
      }
      if (
        (data[index - 1] &&
          data[index - 1].offer &&
          Number(data[index - 1].offer) === Number(num)) ||
        (data[index + 1] &&
          data[index + 1].offer &&
          Number(data[index + 1].offer) === Number(num))
      ) {
        this.$alert("该时段与相邻时段的功率值相同，请合并填写", {
          confirmButtonText: "确定",
          callback: action => {
            data[index].offer = null;
          },
        });
      }
    },
    changeApplyStatus(item, value) {
      item.status = value;
    },
    doGetAggregatorApplyOfferResp() {
      getAggregatorApplyOfferResp(
        {
          aggregatorId: this.aggregatorId,
        },
        this.simulate
      ).then(res => {
        if (res.data.code === 200) {
          res.data.data.resourceList.forEach(item => {
            item.showChart = false;
            item.dateList.forEach(date => {
              date.offerList.forEach(offer => {
                offer.isEmpty = false;
                offer.startTimeRed = false;
                offer.endTimeRed = false;
              });
            });
          });
          this.aggregatorApplyOfferRespData = res.data.data.resourceList;
          if (this.aggregatorApplyOfferRespData.length > 0) {
            this.goNav(this.aggregatorApplyOfferRespData[0], 0);
          }
        }
      });
    },
  },
  created() {
    this.aggregatorId = sessionStorage.getItem("entId");
    this.doGetAggregatorApplyOfferResp();
    this.doGetApplyDateList();
  },
};
</script>
<style lang="less">
.applyPrice {
  .applyPriceBox {
    .el-input {
      width: 110px;
      height: 32px;
    }
    .el-input__inner {
      width: 110px;
      height: 32px;
    }
    .el-input__icon {
      line-height: 33px;
    }
  }
  .redBorder {
    .el-input__inner {
      border: 1px solid #f56c6c;
    }
    .el-input__inner::placeholder {
      color: #f56c6c;
    }
  }
}
.el-message-box__message {
  text-align: center !important;
}
input::-webkit-outer-spin-button,
input::-webkit-inner-spin-button {
  -webkit-appearance: none;
}
input[type="number"] {
  -moz-appearance: textfield;
}
</style>
<style lang="less" type="text/less" scoped>
.applyPrice {
  width: 100%;
  min-height: calc(100vh - 60px);
  background: #f4f5f9;
  border-radius: 12px;
  overflow-y: auto;
  .goBackHeader {
    width: calc(100% - 20px);
    height: 54px;
    background: #ffffff;
    padding-left: 20px;
    line-height: 54px;
    .goBackHeaderIn {
      display: inline-flex;
      align-items: center;
      cursor: pointer;
      img {
        width: 7px;
        height: 11px;
        margin-right: 3px;
      }
      .text {
        font-size: 14px;
        color: #666666;
      }
    }
  }
  .applyPriceInfo {
    width: 100%;
    display: flex;
    margin-top: 20px;
    .applyPriceInfo-left {
      flex: 1;
      padding-left: 200px;
      margin-right: 20px;
      height: auto;
      max-height: calc(100vh - 110px);
      overflow-y: auto;
      .applyPriceInfo-item {
        width: 100%;
        height: auto;
        background: #ffffff;
        border-radius: 12px;
        margin-bottom: 20px;
        padding-bottom: 20px;
        .applyPriceInfo-item-header {
          height: 58px;
          padding: 0 20px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          .item-header-text {
            font-size: 18px;
            font-weight: 600;
            color: #333333;
          }
          .right {
            height: 58px;
            display: flex;
            align-items: center;
            cursor: pointer;
            .right-text {
              font-size: 14px;
              font-family: PingFangSC-Regular, PingFang SC;
              font-weight: 400;
              color: #666666;
            }
            img {
              width: 11px;
              height: 7px;
              margin-left: 3px;
              transition: all 0.3s;
            }
            .showLine {
              transform: rotateX(180deg);
              transition: all 0.3s;
            }
          }
        }
        .echartBox {
          padding: 0 20px;
          background: #f7fbff;
        }
        .isprice {
          height: 54px;
          width: calc(100% - 40px);
          padding: 0 20px;
          .ispriceIn {
            height: 100%;
            display: flex;
            align-items: center;
            box-sizing: border-box;
            border-top: 1px solid #e9e9e9;
            border-bottom: 1px solid #e9e9e9;
            .isprice-text {
              font-size: 18px;
              font-weight: 600;
              color: #333333;
              margin-right: 20px;
            }
            .btn {
              width: 108px;
              height: 34px;
              line-height: 34px;
              text-align: center;
              cursor: pointer;
              font-size: 14px;
              font-family: PingFangSC-Regular, PingFang SC;
              font-weight: 400;
              border: 1px solid #bbbbbb;
              border-radius: 6px;
              margin-right: 19px;
            }
            .btnActive {
              background: #0780ed;
              border: 1px solid #0780ed;
              color: #fff;
            }
          }
        }
        .applyPriceBox {
          padding: 0 20px;
          .applyPriceBox-title {
            height: 58px;
            line-height: 58px;
            font-size: 18px;
            font-weight: 600;
          }
          .applyPriceBox-item {
            margin-bottom: 20px;
            display: flex;
            height: auto;
            background: #ffffff;
            box-shadow: 0px 0px 6px 0px rgba(7, 128, 237, 0.2);
            border-radius: 6px;
            .applyPriceBox-item-date {
              height: auto;
              background: #f7fbff;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 14px;
              font-family: PingFangSC-Medium, PingFang SC;
              font-weight: 600;
              padding: 0 16px;
              color: #666;
            }
            .applyPriceBox-item-info {
              flex: 1;
              .applyPriceBox-item-info-header {
                width: calc(100% - 20px);
                height: 40px;
                display: flex;
                align-items: center;
                box-sizing: border-box;
                border-bottom: 1px solid #e9e9e9;
                padding-left: 20px;
              }
              .applyPriceBox-item-info-content {
                padding-top: 20px;
                padding-left: 20px;
                .applyPriceBox-item-info-content-item {
                  height: 32px;
                  margin-bottom: 20px;
                  display: flex;
                  align-items: center;
                  .line {
                    width: 10px;
                    height: 2px;
                    background: #d0d0d0;
                    margin: 0 5px;
                  }
                  .lineS {
                    width: 1px;
                    height: 14px;
                    margin: 0 15px;
                    background: #d0d0d0;
                  }
                  .addPng {
                    width: 32px;
                    height: 32px;
                    margin-right: 4px;
                    cursor: pointer;
                    margin-top: 3px;
                  }
                  .reducePng {
                    width: 32px;
                    height: 32px;
                    cursor: pointer;
                    margin-top: 3px;
                  }
                }
              }
            }
          }
          .applyPriceBox-item:last-child {
            margin-bottom: 0;
          }
        }
      }
      .bottomBtn {
        width: 100%;
        display: flex;
        justify-content: center;
        margin-bottom: 20px;
        .saveBtn {
          width: 108px;
          height: 34px;
          line-height: 34px;
          text-align: center;
          cursor: pointer;
          font-size: 14px;
          font-family: PingFangSC-Regular, PingFang SC;
          font-weight: 400;
          border: 1px solid #0780ed;
          border-radius: 6px;
          margin-right: 19px;
          color: #0780ed;
        }
        .applyBtn {
          width: 108px;
          height: 34px;
          line-height: 34px;
          text-align: center;
          cursor: pointer;
          font-size: 14px;
          font-family: PingFangSC-Regular, PingFang SC;
          font-weight: 400;
          border-radius: 6px;
          background: #0780ed;
          border: 1px solid #0780ed;
          color: #fff;
        }
        .disabledBtn {
          color: #c0c4cc;
          border: 1px solid #c0c4cc;
        }
      }
    }
    .applyPriceInfo-left::-webkit-scrollbar {
      display: none;
    }
    .applyPriceInfo-weather {
      width: 300px;
      height: auto;
      .weather-top {
        width: 200px;
        height: 134px;
        background: #ffffff;
        border-radius: 12px;
        padding: 20px;
        .date {
          display: flex;
          height: 14px;
          align-items: center;
          img {
            width: 7px;
            height: 11px;
          }
          .date-text {
            font-size: 14px;
            font-family: PingFangSC-Regular, PingFang SC;
            font-weight: 400;
            color: #666666;
            margin: 0 10px;
          }
        }
        .weatherBox {
          margin-top: 20px;
          display: flex;
          .imgBox {
            width: 55px;
            margin-right: 20px;
            img {
              width: 55px;
              height: 55px;
            }
          }
          .noWeather {
            width: 100%;
            text-align: center;
            margin-top: 30px;
            font-size: 14px;
            font-family: PingFangSC-Regular, PingFang SC;
            font-weight: 400;
            color: #666666;
          }
          .weatherInfo {
            .tem {
              font-size: 32px;
              font-family: PingFang-SC-Heavy, PingFang-SC;
              font-weight: 600;
              color: #333333;
            }
            .condition {
              font-size: 14px;
              font-family: PingFangSC-Regular, PingFang SC;
              font-weight: 400;
              color: #666666;
              margin: 20px 0;
            }
            .win {
              font-size: 14px;
              font-family: PingFangSC-Regular, PingFang SC;
              font-weight: 400;
              color: #666666;
            }
          }
        }
      }
      .nav {
        margin-top: 40px;
        position: relative;
        .line {
          position: absolute;
          width: 1px;
          height: 572px;
          left: 0;
          top: 0;
          background: #e9e9e9;
        }
        .nav-item {
          display: flex;
          height: 18px;
          margin-bottom: 20px;
          cursor: pointer;
          .tip {
            display: inline-block;
            width: 4px;
            height: 18px;
            background: #f4f5f9;
            border-radius: 2px;
            margin-right: 20px;
          }
          .text {
            font-size: 18px;
            font-weight: 600;
            color: #999999;
          }
        }
        .active {
          .tip {
            display: inline-block;
            width: 4px;
            height: 18px;
            background: #0780ed;
            border-radius: 2px;
            margin-right: 20px;
          }
          .text {
            font-size: 18px;
            font-weight: 600;
            color: #333333;
          }
        }
      }
    }
  }
}
.applyPrice::-webkit-scrollbar {
  display: none;
}
</style>
