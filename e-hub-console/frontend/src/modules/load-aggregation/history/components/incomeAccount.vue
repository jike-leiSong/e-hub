<template>
  <div class="equipmentOperation">
    <div class="searchBox">
      <div class="searchBox-top">
        <div class="searchBox-top-item">
          <div class="label">时间：</div>
          <div class="date1">
            <el-date-picker
              v-model="timeValue"
              @change="changeDate"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            >
            </el-date-picker>
          </div>
        </div>
        <div class="searchBox-top-item" style="justify-content:center">
          <div class="label">企业名称：</div>
          <el-select
            v-model="entItem"
            filterable
            placeholder="请选择企业"
            class="comp"
          >
            <el-option
              v-for="item in entUserDetailOptions"
              :key="item.value"
              :label="item.label"
              :value="item"
            >
            </el-option>
          </el-select>
        </div>
        <div class="searchBox-top-item" style="justify-content:flex-end">
          <div
            class="btnBox bottom searchBox-bottom-item"
            style="justify-content:flex-end"
          >
            <div class="searchBtn" @click="doSearch()">查询</div>
            <div
              class="resetBtn"
              @click="doExport()"
              :class="{ disabledBtn: activeObj.option.data.canSet === '1' }"
            >
              导出
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="echartBox">
      <div class="tableBox">
        <el-table
          ref="filterTable"
          :data="tableData"
          border
          :header-cell-style="{
            background: '#EEF5FC',
            color: '#666',
            'text-align': center,
          }"
          style="width: 100%"
        >
          <el-table-column prop="date" label="日期" align="center">
          </el-table-column>
          <el-table-column
            label="总有效调节电量(kWh)"
            align="center"
            prop="electricQuantity"
          >
          </el-table-column>
          <el-table-column
            prop="electricOffer"
            align="center"
            label="度电收益（元/kWh）"
          >
          </el-table-column>
          <el-table-column
            prop="offer"
            align="center"
            label="平均出清价格(元/kWh)"
          >
          </el-table-column>
          <el-table-column
            prop="issueProfit"
            align="center"
            label="电网下发收益(元)"
          >
          </el-table-column>
          <el-table-column prop="entProfit" align="center" label="用户收益(元)">
          </el-table-column>
        </el-table>
        <!-- <div style="margin-top: 20px">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[10, 20, 30, 40]"
            :page-size="10"
            layout="total, sizes, prev, pager, next, jumper"
            :total="tableData.total">
          </el-pagination>
        </div> -->
      </div>
    </div>
  </div>
</template>
<script>
import { downLoadXls } from "@/utils/util.js";
import axios from "axios";
import {
  baseUrl,
  accessKeyValue,
  getEntUserOptions,
  getProfitCalculation,
} from "../api/index";
import moment from "moment";

export default {
  name: "incomeAccount",
  components: {},
  data() {
    return {
      form: {
        startTime: "",
        endTime: "",
      },
      entItem: {},
      timeValue: "",
      pageNo: 1,
      pageSize: 20,
      entUserDetailOptions: [],
      tableData: [],
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
    changeEnt() {},
    handleSizeChange(val) {
      this.currentPage = 1;
      this.pageNo = 1;
      this.pageSize = val;
      this.getProfitCalculationTable();
    },
    handleCurrentChange(val) {
      this.pageNo = val;
      this.currentPage = val;
      this.getProfitCalculationTable();
    },
    getProfitCalculationTable() {
      const query = {
        entId: this.entItem.value,
        endDate: this.form.endTime,
        startDate: this.form.startTime,
        aggregatorId: sessionStorage.getItem("entId"),
      };
      getProfitCalculation(query, this.simulate).then(res => {
        if (res.data.code === 200) {
          this.tableData = res.data.data;
        }
      });
    },
    doExport() {
      if (this.activeObj.option.data.canSet === "1") {
        return;
      }
      const query = {
        entId: this.entItem.value,
        endDate: this.form.endTime,
        startDate: this.form.startTime,
      };
      axios.defaults.headers.common.ticket = sessionStorage.getItem("ticket");
      axios.defaults.headers.common["X-GW-AccessKey"] = accessKeyValue;
      axios({
        method: "get",
        url: `${baseUrl}/historyQuery/getProfitCalculationExcel`, // 请求地址
        params: query, // 参数
        responseType: "blob", // 表明返回服务器返回的数据类型
      }).then(res => {
        const fileName = `${this.entItem.label}${this.form.startTime}~${this.form.endTime}.xls`;
        downLoadXls(res.data, fileName);
      });
    },
    doSearch() {
      this.getProfitCalculationTable();
    },
    changeDate() {
      if (this.timeValue) {
        this.form.startTime = moment(this.timeValue[0]).format("YYYY-MM-DD");
        this.form.endTime = moment(this.timeValue[1]).format("YYYY-MM-DD");
      }
    },
    doGetEntUserOptions() {
      getEntUserOptions(
        { aggregatorId: this.aggregatorId },
        this.simulate
      ).then(res => {
        if (res.data.code === 200) {
          this.entUserDetailOptions = res.data.data;
          if (this.entUserDetailOptions.length > 0) {
            this.entItem = this.entUserDetailOptions[0];
            this.getProfitCalculationTable();
          }
        }
      });
    },
  },
  created() {
    this.form.startTime = moment()
      .subtract(9, "days")
      .format("YYYY-MM-DD");
    this.form.endTime = moment()
      .subtract(2, "days")
      .format("YYYY-MM-DD");
    this.timeValue = [this.form.startTime, this.form.endTime];
    this.aggregatorId =
      sessionStorage.getItem("entId") || sessionStorage.getItem("cid");
    // this.aggregatorId = '1351782569954816001'
    this.systemCode = sessionStorage.getItem("systemCode");
    this.doGetEntUserOptions();
    try {
      doSaveOperation({
        entId: sessionStorage.getItem("entId") || sessionStorage.getItem("cid"),
        eventKey: "FHJHS_QH_WCTJ",
        openId: sessionStorage.getItem("openId"),
        operationTime: new Date().getTime(),
        systemCode: sessionStorage.getItem("systemCode"),
      });
    } catch (err) {}
  },
};
</script>
<style lang="less">
.el-popover {
  .deviceItem:hover {
    color: #0780ed;
  }
}
.equipmentOperation {
  .el-input {
    .el-input__suffix {
      top: 6px;
    }
  }
  .is-focus {
    .el-input__suffix {
      top: -5px;
    }
  }
  .comp {
    margin-right: 20px;
    .el-input__inner {
      width: 200px !important;
      height: 30px;
    }
  }
  .device {
    margin-right: 20px;
    .el-input__inner {
      width: 150px !important;
    }
  }
  .date {
    margin-right: 20px;
    .el-input__inner {
      width: 250px !important;
    }
  }
  .popoverBtn {
    background: none;
    border: none;
    img {
      width: 14px;
      height: 14px;
    }
  }
  .date1 {
    margin-left: 10px;
    .el-input__inner {
      width: 250px !important;
      height: 30px !important;
    }
    .el-range__icon {
      line-height: 25px;
    }
    .el-range-separator {
      line-height: 24px;
    }
    .el-range__close-icon {
      line-height: 25px;
    }
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
  .searchBox {
    width: calc(100% - 40px);
    height: auto;
    background: #ffffff;
    border-radius: 12px;
    margin-bottom: 20px;
    padding: 15px 20px;
    margin-top: 20px;
    .searchBox-top {
      height: 30px;
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
      }
    }
    .searchBoxLeft {
      flex: 1;
      display: flex;
      align-items: center;
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
      .disabledBtn {
        color: #c0c4cc;
        border: 1px solid #c0c4cc;
      }
    }
  }
  .echartBox {
    border-radius: 12px;
    padding: 20px;
    width: calc(100% - 40px);
    background: white;
    .shouyiBox {
      .shouyi {
        width: auto;
        height: 30px;
        line-height: 30px;
        padding: 0 9px;
        display: inline-block;
        background: #f7fbff;
        border-radius: 15px;
        font-size: 14px;
        color: #666666;
        img {
          width: 18px;
          height: 20px;
          margin-right: 7px;
        }
      }
    }
  }
}
.equipmentOperation::-webkit-scrollbar {
  display: none;
}
</style>
