<template>
  <div class="equipmentOperation">
    <div class="tabbar">
      <div
        class="tabbarItem"
        :class="{ activeTabBar: selType == '1' }"
        @click="doSelType('1')"
      >
        <span class="name">单设备多指标</span>
        <span class="line" :class="{ activeTabBarLine: selType == '1' }"></span>
      </div>
      <div
        class="tabbarItem"
        :class="{ activeTabBar: selType == '2' }"
        @click="doSelType('2')"
      >
        <span class="name">多设备单指标</span>
        <span class="line" :class="{ activeTabBarLine: selType == '2' }"></span>
      </div>
    </div>
    <div class="searchBox">
      <div class="searchBox-top">
        <div class="searchBox-top-item">
          <div class="label">企业名称：</div>
          <el-select
            v-model="form.subEntId"
            filterable
            placeholder="请选择企业"
            class="comp"
            @change="changeEnt"
          >
            <el-option
              v-for="item in entList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
        </div>
        <div class="searchBox-top-item" style="justify-content:center">
          <div class="label">资源类型：</div>
          <el-select
            v-model="form.resourceTypeId"
            filterable
            placeholder="请选择企业"
            class="comp"
            @change="changeResourceTypeId"
          >
            <el-option
              v-for="item in resourceTypeList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
              :disabled="item.display !== 1"
            >
            </el-option>
          </el-select>
        </div>
        <div
          class="searchBox-top-item"
          v-if="selType == '1'"
          style="justify-content:flex-end"
        >
          <div class="label">设备名称：</div>
          <el-select
            v-model="form.deviceBaseId"
            placeholder="请选择设备"
            class="comp"
            style="margin-right: 0"
          >
            <el-option
              v-for="item in deviceList"
              :key="item.deviceBaseId"
              :label="item.deviceName"
              :value="item.deviceBaseId"
            >
            </el-option>
          </el-select>
        </div>
        <div
          class="searchBox-top-item"
          v-if="selType == '2'"
          style="justify-content:flex-end"
        >
          <div class="label">设备名称：</div>
          <el-select
            v-model="form.deviceBaseListId"
            multiple
            collapse-tags
            placeholder="请选择设备"
            :multiple-limit="10"
            class="multiple"
            style="margin-right: 0"
          >
            <el-option
              v-for="item in deviceList"
              :key="item.deviceBaseId"
              :label="item.deviceName"
              :value="item.deviceBaseId"
            >
            </el-option>
          </el-select>
        </div>
      </div>
      <div class="searchBox-bottom">
        <div class="searchBox-bottom-item">
          <div class="label">时间：</div>
          <div class="date1">
            <el-date-picker
              v-model="timeValue"
              @change="changeDate"
              :picker-options="pickerOptions"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            >
            </el-date-picker>
          </div>
        </div>
        <div class="searchBox-bottom-item" style="justify-content:center">
          <div class="label">指标类型：</div>
          <el-select
            v-model="form.metricList"
            multiple
            collapse-tags
            placeholder="请选择指标"
            :multiple-limit="2"
            class="multiple"
            v-if="selType == '1'"
          >
            <el-option
              v-for="item in metricOptions"
              :key="item.metricCode"
              :label="item.metricName"
              :value="item.metricCode"
            >
            </el-option>
          </el-select>
          <el-select
            v-model="form.metricId"
            placeholder="请选择指标"
            class="multiple"
            v-if="selType == '2'"
          >
            <el-option
              v-for="item in metricOptions"
              :key="item.metricCode"
              :label="item.metricName"
              :value="item.metricCode"
            >
            </el-option>
          </el-select>
        </div>
        <div
          class="btnBox bottom searchBox-bottom-item"
          style="justify-content:flex-end"
        >
          <div class="searchBtn" @click="doSearch()">查询</div>
          <div class="resetBtn" @click="doReset()">重置</div>
        </div>
      </div>
    </div>
    <div class="echartBox">
      <div class="echartHeader">
        {{ deviceChartData[0] ? deviceChartData[0].chartName : " --" }}
      </div>
      <ecline
        :refreshId="refreshId"
        :unit="unit"
        :unit2="unit2"
        :ecdata="deviceChartData"
        style="height:600px"
        width="100%"
        height="100%"
      ></ecline>
    </div>
  </div>
</template>
<script>
import ecline from "./ec_line";
import {
  getEntUserOptions,
  getDeviceList,
  getMetricList,
  getDeviceRunStatusChart,
  getResourceTypeList,
  doSaveOperation,
} from "../api";
import moment from "moment";

export default {
  name: "equipmentOperation",
  components: {
    ecline,
  },
  data() {
    return {
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now();
        },
      },
      selType: "1",
      value1: "",
      unit: null,
      unit2: null,
      aggregatorId: null,
      resourceTypeList: [],
      timeValue: [],
      deviceList: [],
      entList: [],
      postMetricList: [],
      postDeviceIdList: [],
      isFirst: true,
      deviceChartData: [],
      form: {
        subEntId: null,
        deviceBaseId: null,
        metricList: null,
        resourceTypeId: null,
        startTime: "",
        endTime: "",
        metricId: null,
        deviceBaseListId: null,
      },
      metricOptions: [],
      value: "",
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
  },
  methods: {
    doGetMetricList() {
      getMetricList().then(res => {
        if (res.data.code === 200) {
          this.metricOptions = res.data.data;
          if (res.data.data && res.data.data.length > 0 && res.data.data[0]) {
            this.form.metricList = [res.data.data[0].metricCode];
            this.form.metricId = res.data.data[0].metricCode;
          }
        }
      });
    },
    changeResourceTypeId() {
      this.form.deviceBaseId = null;
      this.doGetDeviceList();
    },
    doGetResourceTypeList() {
      getResourceTypeList(
        {
          aggregatorId: sessionStorage.getItem("entId"),
        }
      ).then(res => {
        this.resourceTypeList = res.data.data
        if (this.resourceTypeList.length > 0) {
          for (let i = 0; i < this.resourceTypeList.length; i++) {
            if (this.resourceTypeList[i].display === 1) {
              this.form.resourceTypeId = this.resourceTypeList[i].id
              this.changeResourceTypeId()
              break
            }
          }
        }
      });
    },
    changeDate() {
      if (this.timeValue) {
        this.form.startTime = moment(this.timeValue[0]).format("YYYY-MM-DD");
        this.form.endTime = moment(this.timeValue[1]).format("YYYY-MM-DD");
      }
    },
    doSearch() {
      if (!this.form.metricList) {
        this.$message({
          message: "请选择指标类型",
          type: "warning",
        });
        return;
      }
      this.doGetDeviceRunStatusChart();
    },
    doGetDeviceRunStatusChart() {
      const query = {
        deviceBaseIdList:
          this.selType == "1"
            ? [this.form.deviceBaseId]
            : this.form.deviceBaseListId,
        endTime: this.form.endTime,
        startTime: this.form.startTime,
        metricList:
          this.selType == "1" ? this.form.metricList : [this.form.metricId],
        subEntId: this.form.subEntId,
        resourceTypeId: this.form.resourceTypeId,
        aggregatorId: this.aggregatorId,
        status: this.selType == "1" ? "0" : "1",
      };
      getDeviceRunStatusChart(query).then(res => {
        if (res.data.code === 200) {
          const list = [];
          this.unit = null;
          this.unit2 = null;
          res.data.data.forEach((item, index) => {
            if (index === 0) {
              this.unit = item.unit;
            } else if (index === 1) {
              this.unit2 = item.unit;
            }
            item.lineDataGraphVOList.forEach(line => {
              if (index === 0) {
                line.yAxisIndex = 0;
              } else if (index === 1) {
                line.yAxisIndex = 1;
              }
              line.name = line.lineName;
              line.value = line.dataRespList;
              list.push(line);
            });
          });
          this.deviceChartData = list;
        }
      });
    },
    doReset() {
      this.form.startTime = moment()
        .subtract(1, "days")
        .format("YYYY-MM-DD");
      this.form.endTime = moment()
        .subtract(1, "days")
        .format("YYYY-MM-DD");
      this.timeValue = [this.form.startTime, this.form.endTime];
      this.doGetEntUserOptions();
    },
    changeMetricList() {
      if (this.selType === "1") {
        this.postMetricList = this.form.metricList;
      } else {
        this.postMetricList = [this.form.metricList];
      }
    },
    doSelType(item) {
      this.selType = item;
      if (item == "1") {
        if (this.form.metricId) {
          this.form.metricList = [this.form.metricId];
        }
      } else {
        this.form.deviceBaseListId = [this.form.deviceBaseId];
      }
    },
    doGetEntUserOptions() {
      getEntUserOptions(
        { aggregatorId: this.aggregatorId }
      ).then(res => {
        if (res.data.code === 200) {
          this.entList = (res.data.data || []).filter(item => item != null);
          if (this.entList.length > 0 && this.entList[0] && this.entList[0].value) {
            this.form.subEntId = this.entList[0].value;
            this.doGetResourceTypeList();
          }
        }
      });
    },
    changeEnt() {
      this.form.deviceBaseId = null;
      this.doGetDeviceList();
    },
    doGetDeviceList() {
      const query = {
        aggregatorId: this.aggregatorId,
        entId: this.form.subEntId,
        resourceTypeId: this.form.resourceTypeId,
      };
      getDeviceList(query).then(res => {
        if (res.data.code === 200) {
          this.deviceList = (res.data.data || []).filter(item => item != null);
          if (this.deviceList.length > 0 && this.deviceList[0] && this.deviceList[0].deviceBaseId) {
            this.form.deviceBaseId = this.deviceList[0].deviceBaseId;
            this.form.deviceBaseListId = [this.deviceList[0].deviceBaseId];
            if (this.isFirst) {
              this.doSearch();
            }
          }
        }
      });
    },
  },
  created() {
    this.refreshId = new Date().getTime();
    this.aggregatorId =
      sessionStorage.getItem("entId") || sessionStorage.getItem("cid");
    this.systemCode = sessionStorage.getItem("systemCode");
    this.form.startTime = moment()
      .subtract(1, "days")
      .format("YYYY-MM-DD");
    this.form.endTime = moment()
      .subtract(1, "days")
      .format("YYYY-MM-DD");
    this.timeValue = [this.form.startTime, this.form.endTime];
    this.doGetEntUserOptions();
    this.doGetMetricList();
  },
};
</script>
<style lang="less">
.equipmentOperation {
  .el-input {
    .el-input__suffix {
      top: 2px;
    }
  }
  .is-focus {
    .el-input__suffix {
      top: -3px;
    }
  }
  .comp {
    margin-right: 20px;
    .el-input__inner {
      width: 140px !important;
    }
  }
  .device {
    margin-right: 20px;
    .el-input__inner {
      width: 120px !important;
    }
  }
  .multiple {
    margin-right: 20px;
    .el-input__inner {
      // width: 150px !important;
    }
  }
  .date {
    margin-right: 20px;
    .el-input__inner {
      width: 200px !important;
    }
  }
  .el-input__inner {
    height: 30px !important;
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
  .tabbar {
    height: 54px;
    display: flex;
    align-items: center;
    .tabbarItem {
      padding-top: 8px;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      align-items: center;
      margin-right: 20px;
      cursor: pointer;
      .name {
        font-size: 15px;
        color: #666666;
        margin-bottom: 6px;
      }
      .line {
        display: inline-block;
        width: 20px;
        height: 3px;
        background: #f4f5f9;
        border-radius: 2px;
      }
      .activeTabBarLine {
        background: #0780ed;
      }
    }
    .activeTabBar {
      .name {
        color: #0780ed;
      }
    }
  }
  .searchBox {
    width: calc(100% - 40px);
    height: auto;
    background: #ffffff;
    border-radius: 12px;
    margin-bottom: 20px;
    padding: 15px 20px;
    .searchBox-top {
      height: 30px;
      margin-bottom: 20px;
      width: 100%;
      display: flex;
      justify-content: space-between;
      .searchBox-top-item {
        flex: 1;
        display: flex;
        height: 30px;
        align-items: center;
        .label {
          font-size: 14px;
          font-family: PingFangSC-Regular, PingFang SC;
          font-weight: 400;
          margin-right: 10px;
        }
      }
    }
    .searchBox-bottom {
      height: 30px;
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: space-between;
      .searchBox-bottom-item {
        flex: 1;
        display: flex;
        height: 30px;
        align-items: center;
        .label {
          font-size: 14px;
          font-family: PingFangSC-Regular, PingFang SC;
          font-weight: 400;
          margin-right: 10px;
        }
      }
    }
    .searchBoxLeft {
      flex: 1;
      display: flex;
      align-items: center;
    }
    .tabBox1 {
      width: 180px;
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
    .btnBox {
      display: flex;
      align-items: center;
      .searchBtn {
        width: 108px;
        height: 34px;
        margin-right: 20px;
        box-sizing: border-box;
        border-radius: 5px;
        background: #0780ed;
        text-align: center;
        line-height: 34px;
        color: white;
        font-size: 12px;
        cursor: pointer;
      }
      .resetBtn {
        width: 108px;
        height: 34px;
        box-sizing: border-box;
        border-radius: 5px;
        background: white;
        text-align: center;
        line-height: 34px;
        color: #0780ed;
        font-size: 12px;
        border: 1px solid #0780ed;
        cursor: pointer;
      }
    }
  }
  .echartBox {
    border-radius: 12px;
    padding: 10px 0;
    width: 100%;
    height: 600px;
    background: white;
    .echartHeader {
      padding-left: 20px;
      font-size: 16px;
      font-family: PingFangSC-Medium, PingFang SC;
      font-weight: 600;
      color: #333333;
      margin: 5px 0;
    }
  }
}
.equipmentOperation::-webkit-scrollbar {
  display: none;
}
</style>
