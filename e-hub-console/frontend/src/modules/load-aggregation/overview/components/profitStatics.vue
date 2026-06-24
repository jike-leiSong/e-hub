<template>
  <div class="profitPage">
    <div class="back">
      <div class="backOut" @click="doBack()">
        <img src="../images/left.png" alt="" />
        <span>收益详情</span>
      </div>
    </div>
    <div class="searchBox">
      <div class="searchLeft">
        <div class="tabBox1">
          <div
            class="tabBoxItem"
            style="border-right:1px solid #BBBBBB"
            :class="{ tabBoxItemActive: selTab == '2' }"
            @click="doSelTab('2')"
          >
            上月
          </div>
          <div
            class="tabBoxItem"
            :class="{ tabBoxItemActive: selTab == '3' }"
            @click="doSelTab('3')"
          >
            本月
          </div>
        </div>
        <div class="date">
          <el-date-picker
            v-model="timeValue"
            type="daterange"
            range-separator="至"
            @change="changeDate"
            value-format="yyyy-MM-dd"
            :picker-options="pickerOptions"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          >
          </el-date-picker>
        </div>
        <div class="multipleBox">
          <div class="label">筛选用户</div>
          <el-select
            v-model="entIdList"
            collapse-tags
            multiple
            placeholder="请选择企业"
            class="multiple"
          >
            <el-option
              v-for="item in entList"
              :key="item.entId"
              :label="item.entName"
              :value="item.entId"
            >
            </el-option>
          </el-select>
        </div>
      </div>
      <div class="btnBox">
        <div class="searchBtn" @click="doSearch()">查询</div>
        <div class="resetBtn" @click="doReset()">重置</div>
      </div>
    </div>
    <div class="tableBox">
      <div class="tableBoxHeader">
        <div class="name">用户情况</div>
        <div
          class="btn"
          @click="doExport()"
        >
          导出
        </div>
      </div>
      <el-table
        v-if="showTable === '1'"
        :data="proFitList"
        :row-key="getRowKeys"
        :expand-row-keys="expands"
        :header-cell-style="{
          background: '#EEF5FC',
          color: '#666',
          'text-align': center,
        }"
        max-height="600"
        style="width: 100%"
      >
        <el-table-column type="expand" width="40">
          <template slot-scope="props">
            <div class="expandHeader">
              <span class="expandHeader-name">用户名称</span>
              <span class="expandHeader-name1">收益(元)</span>
            </div>
            <div
              class="compItem"
              v-for="(item, index) in props.row.entDateProfitRespList"
              :key="index"
            >
              <span class="expandHeader-name">{{ item.entName }}</span>
              <span class="expandHeader-name1">{{ item.entProfit }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="日期" prop="date"> </el-table-column>
        <el-table-column label="电网调度下发金额(元)" prop="issueProfit">
        </el-table-column>
        <el-table-column label="负荷聚合商收益(元)" prop="aggregatorProfit">
        </el-table-column>
        <el-table-column label="用户收益(元)" width="180" prop="entProfit">
        </el-table-column>
      </el-table>
      <div class="table2" v-if="showTable === '2'">
        <div class="tableHeader">
          <div class="headerItem">日期</div>
          <div
            class="headerItem"
            v-for="(item, index) in showEntIdList"
            :key="index"
          >
            {{ item.entName }}
          </div>
        </div>
        <div class="tableContent">
          <div
            class="tableContentItem"
            v-for="(item, index) in listByEntIdList"
            :key="index"
          >
            <div
              class="tableContentItem-item"
              v-for="(value, index2) in item"
              :key="index2"
            >
              {{ value === null ? "--" : value }}
            </div>
          </div>
        </div>
      </div>
      <div class="pagination" style="margin-top: 10px">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 30, 50]"
          :page-size="10"
          layout="total, sizes, prev, pager, next, jumper"
          :total="proFitTotal"
        >
        </el-pagination>
      </div>
    </div>
  </div>
</template>
<script>
import {
  getContentList,
  getProfitList,
  getListByEntIdList,
  doSaveOperation,
  accessKeyValue,
  baseUrl,
} from "../api";
import { downLoadXls } from "@/utils/util.js";
import moment from "moment";
import axios from "axios";

export default {
  name: "profitStatics",
  data() {
    return {
      aggregatorId: null,
      selTab: "1",
      startTime: "",
      endTime: "",
      pageNo: 1,
      pageSize: 10,
      currentPage: 1,
      timeValue: [],
      entIdList: [],
      showEntIdList: [],
      listByEntIdList: [],
      entList: [],
      proFitList: [],
      proFitTotal: 0,
      showTable: "1",
      getRowKeys(row) {
        return row.date;
      },
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now();
        },
      },
      expands: [],
    };
  },
  props: {
    activeObj: {
      type: Object,
      require: true,
    },
  },
  methods: {
    doExport() {
      if (
        (this.proFitList.length === 0 && this.entIdList.length === 0) ||
        (this.entIdList.length > 0 && this.listByEntIdList.length === 0)
      ) {
        this.$message({
          message: "暂无可导出数据",
          type: "warning",
        });
        return;
      }
      const entIds = [];
      this.entIdList.forEach(item => {
        entIds.push(item);
      });
      const query = {
        aggregatorId: this.aggregatorId,
        startDate: this.startTime,
        endDate: this.endTime,
        entIds: entIds.join(","),
      };
      axios.defaults.headers.common.ticket = sessionStorage.getItem("ticket");
      axios.defaults.headers.common.token = sessionStorage.getItem("token") || sessionStorage.getItem("ticket");
      axios.defaults.headers.common.Authorization = `Bearer ${sessionStorage.getItem("token") || sessionStorage.getItem("ticket")}`;
      axios.defaults.headers.common["X-GW-AccessKey"] = accessKeyValue;
      let downUrl = "";
      if (this.entIdList.length === 0) {
        downUrl = "/profit/list/download";
      } else {
        downUrl = "/profit/listByEntIdListExcel";
      }
      axios({
        method: "get",
        url: `${baseUrl}${downUrl}`, // 请求地址
        params: query, // 参数
        responseType: "blob", // 表明返回服务器返回的数据类型
      }).then(res => {
        const fileName = `收益统计${moment().format(
          "YYYY-MM-DD-HH-mm-ss"
        )}.xls`;
        downLoadXls(res.data, fileName);
      });
    },
    doReset() {
      this.selTab = "1";
      this.startTime = moment()
        .subtract(2, "days")
        .format("YYYY-MM-DD");
      this.endTime = moment()
        .subtract(2, "days")
        .format("YYYY-MM-DD");
      this.timeValue = [this.startTime, this.endTime];
    },
    doBack() {
      this.$emit("goBack");
    },
    // 有筛选条件查询
    doGetListByEntIdList() {
      const entIds = [];
      this.entIdList.forEach(item => {
        entIds.push(item);
      });
      const query = {
        aggregatorId: this.aggregatorId,
        startDate: this.startTime,
        endDate: this.endTime,
        entIds: entIds.join(","),
        pageIndex: this.pageNo,
        pageSize: this.pageSize,
      };
      getListByEntIdList(query).then(res => {
        if (res.data.code === 200) {
          this.showTable = "2";
          this.showEntIdList = [];
          this.entIdList.forEach(ent => {
            this.entList.forEach(item => {
              if (item.entId === ent) {
                this.showEntIdList.push(item);
              }
            });
          });
          this.proFitTotal = res.data.total;
          this.listByEntIdList = res.data.data;
        }
      });
    },
    // 无筛选条件查询
    doGetProfitList() {
      const query = {
        aggregatorId: this.aggregatorId,
        startDate: this.startTime,
        endDate: this.endTime,
        pageIndex: this.pageNo,
        pageSize: this.pageSize,
      };
      getProfitList(query).then(res => {
        if (res.data.code === 200) {
          this.proFitTotal = res.data.total;
          this.proFitList = res.data.data;
          this.showTable = "1";
          if (this.proFitList.length > 0) {
            this.expands = [];
            this.expands.push(this.proFitList[0].date);
          }
        }
      });
    },
    doSearch() {
      this.pageNo = 1;
      if (this.entIdList.length === 0) {
        this.doGetProfitList();
      } else {
        this.doGetListByEntIdList();
      }
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1;
      if (this.entIdList.length === 0) {
        this.doGetProfitList();
      } else {
        this.doGetListByEntIdList();
      }
    },
    handleCurrentChange(val) {
      this.pageNo = val;
      this.currentPage = val;
      if (this.entIdList.length === 0) {
        this.doGetProfitList();
      } else {
        this.doGetListByEntIdList();
      }
    },
    doGetEntUserOptions() {
      getContentList({ aggregatorId: this.aggregatorId }).then(
        res => {
          if (res.data.code === 200) {
            this.entList = res.data.data;
            if (this.entList.length > 0) {
              this.doSearch();
            }
          }
        }
      );
    },
    changeDate() {
      this.selTab = "1";
      if (this.timeValue) {
        this.startTime = moment(this.timeValue[0]).format("YYYY-MM-DD");
        this.endTime = moment(this.timeValue[1]).format("YYYY-MM-DD");
      }
    },
    doSelTab(item) {
      this.selTab = item;
      if (item == "3") {
        this.startTime = moment()
          .month(moment().month())
          .startOf("month")
          .format("YYYY-MM-DD");
        this.endTime = moment().format("YYYY-MM-DD");
      } else if (item == "2") {
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
    },
  },
  created() {
    this.startTime = moment()
      .subtract(2, "days")
      .format("YYYY-MM-DD");
    this.endTime = moment()
      .subtract(2, "days")
      .format("YYYY-MM-DD");
    this.timeValue = [this.startTime, this.endTime];
    this.aggregatorId =
      sessionStorage.getItem("entId") || sessionStorage.getItem("cid");
    this.doGetEntUserOptions();
  },
};
</script>
<style lang="less">
.profitPage {
  .el-table th > .cell {
    text-align: center;
  }
  .el-table .cell {
    text-align: center;
    padding-left: 0 !important;
    padding-right: 0 !important;
    .deviceName {
      height: 40px;
      line-height: 40px;
    }
    .deviceName:nth-child(even) {
      background: #fafafa;
    }
    .deviceName:nth-child(odd) {
      background: #fff;
    }
  }
  .date {
    margin-right: 20px;
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
  .multipleBox {
    .multiple {
      flex: 1;
      max-width: 300px !important;
      .el-select__tags {
        max-width: 300px !important;
      }
    }
    .el-input__inner {
      height: 28px !important;
      border: none !important;
    }
    .el-input__icon {
      line-height: 27px;
    }
  }
}
.profitPage {
  .el-table__expanded-cell {
    padding: 0 !important;
    .expandHeader {
      width: 100%;
      height: 40px;
      display: flex;
      align-items: center;
      background: #fafafa;
      border-bottom: 1px solid #e8e8e8;
      .expandHeader-name {
        font-size: 14px;
        font-family: PingFangSC-Regular, PingFang SC;
        font-weight: 400;
        color: #666666;
        flex: 1;
        padding-left: 60px;
      }
      .expandHeader-name1 {
        width: 180px;
        font-size: 14px;
        font-family: PingFangSC-Regular, PingFang SC;
        font-weight: 400;
        color: #666666;
        text-align: center;
      }
    }
    .compItem {
      width: 100%;
      height: 40px;
      display: flex;
      align-items: center;
      background: #fafafa;
      border-bottom: 1px solid #e8e8e8;
      .expandHeader-name {
        font-size: 14px;
        font-family: PingFangSC-Regular, PingFang SC;
        font-weight: 400;
        color: #333;
        flex: 1;
        padding-left: 60px;
      }
      .expandHeader-name1 {
        width: 180px;
        font-size: 14px;
        font-family: PingFangSC-Regular, PingFang SC;
        font-weight: 400;
        color: #333;
        text-align: center;
      }
    }
    .compItem:last-child {
      border-bottom: none;
    }
  }
}
</style>
<style lang="less" type="text/less" scoped>
.multipleBox {
  display: flex;
  height: 30px;
  align-items: center;
  border: 1px solid #bbbbbb;
  border-radius: 6px;
  box-sizing: border-box;
  width: 350px;
  .label {
    padding: 0 10px;
    font-size: 14px;
    font-family: PingFangSC-Regular, PingFang SC;
    font-weight: 400;
    border-right: 1px solid #bbbbbb;
    height: 30px;
    line-height: 30px;
  }
}
.profitPage {
  width: calc(100% - 40px);
  min-height: 768px;
  position: relative;
  padding: 0 20px;
  background: #f4f5f9;
  .back {
    height: 54px;
    display: flex;
    align-items: center;
    .backOut {
      cursor: pointer;
      height: 54px;
      display: flex;
      align-items: center;
      img {
        width: 7px;
        height: 11px;
        margin-right: 4px;
      }
      span {
        font-size: 14px;
        color: #666666;
      }
    }
  }
  .searchBox {
    width: calc(100% - 40px);
    height: 54px;
    background: #ffffff;
    border-radius: 12px;
    display: flex;
    padding: 0 20px;
    justify-content: space-between;
    .searchLeft {
      display: flex;
      height: 54px;
      align-items: center;
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
  .tableBox {
    margin-top: 20px;
    padding: 20px;
    background: white;
    border-radius: 12px;
    .tableBoxHeader {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 34px;
      margin-bottom: 12px;
      .name {
        font-size: 18px;
        font-weight: 600;
        color: #333333;
      }
      .btn {
        width: 108px;
        height: 34px;
        background: #0780ed;
        border-radius: 5px;
        border: 1px solid #0780ed;
        line-height: 34px;
        text-align: center;
        font-size: 14px;
        font-family: PingFangSC-Regular, PingFang SC;
        font-weight: 400;
        color: #ffffff;
        cursor: pointer;
      }
      .disabledBtn {
        color: #c0c4cc;
        border: 1px solid #c0c4cc;
      }
    }
  }
}
.table2 {
  width: 100%;
  border: 1px solid #e8e8e8;
  .tableHeader {
    width: 100%;
    height: 40px;
    background: #eef4fc;
    display: flex;
    .dateHeader {
      width: 180px;
      height: 40px;
      border-right: 1px solid #e8e8e8;
      font-size: 14px;
      font-family: PingFangSC-Regular, PingFang SC;
      font-weight: 400;
      color: #666666;
      text-align: center;
      line-height: 40px;
    }
    .headerItem {
      flex: 1;
      height: 40px;
      border-right: 1px solid #e8e8e8;
      font-size: 14px;
      font-family: PingFangSC-Regular, PingFang SC;
      font-weight: 400;
      color: #666666;
      text-align: center;
      line-height: 40px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      padding: 0 10px;
    }
    .headerItem:last-child {
      border-right: none;
    }
  }
  .tableContent {
    width: 100%;
    height: auto;
    background: #ffffff;
    .tableContentItem {
      width: 100%;
      height: 40px;
      display: flex;
      border-bottom: 1px solid #e8e8e8;
      .tableContentItem-item {
        flex: 1;
        height: 40px;
        border-right: 1px solid #e8e8e8;
        font-size: 14px;
        font-family: PingFangSC-Regular, PingFang SC;
        font-weight: 400;
        color: #333;
        text-align: center;
        line-height: 40px;
      }
      .tableContentItem-item:last-child {
        border-right: none;
      }
    }
    .tableContentItem:last-child {
      border-bottom: none;
    }
  }
}
</style>
