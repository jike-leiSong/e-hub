<template>
  <div class="clearing-price">
    <div class="condition-operate-part">
      <div class="condition">
        <div class="condition-select">
          <p>资源类型</p>
          <el-select v-model="resourceTypeId" filterable @change="resourceTypeChange" size="small" placeholder="请选择资源类型">
            <el-option v-for="item in resourceTypeList" :key="item.id" :label="item.name" :value="item.id" :disabled="item.display !== 1" />
          </el-select>
        </div>
        <div class="condition-tab">
          <p :class="{ 'active': monthType == 'current' }" @click="monthChange('current')">本月</p>
          <p :class="{ 'active': monthType == 'last' }" @click="monthChange('last')">上月</p>
        </div>
        <el-date-picker v-model="dateData" value-format="yyyy-MM-dd" type="daterange" size="small" range-separator="至" :picker-options="datePickerOptions" @change="dateChange" start-placeholder="开始日期" end-placeholder="结束日期" />
      </div>
      <div class="operate">
        <el-button @click="exportData" :disabled="activeObj.option.data.canSet === '1'" size="small" type="primary">导出</el-button>
      </div>
    </div>
    <div v-loading="echartsLoading" class="echarts-part">
      <ecline unit='元/kWh' min="0" :ecdata='echartsData' :refreshId='refreshId' />
    </div>
    <div v-loading="tableLoading" class="table-part">
      <el-table :data="tableData" max-height="400" stripe border tooltip-effect="dark" :header-cell-style="{ 'background': '#EEF5FC', 'color': '#666666', 'text-align': 'center' }" style="width: 100%;">
        <el-table-column fixed label="日期" prop="date" min-width="120" />
        <el-table-column fixed label="资源类型" prop="sourceTypeName" min-width="120" />
        <el-table-column fixed label="类型" prop="type" min-width="120" />
        <el-table-column v-for="item in tableHeader" :key='item.prop' :label="item.label" show-overflow-tooltip align="left" min-width="100">
          <template slot-scope="scope">{{scope.row[item.prop]}}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
<script>
import axios from "axios"
import moment from "moment"
import { downLoadXls } from "@/utils/util.js"
import ecline from "./ec_line"
import {
  getPrice,
  doSaveOperation,
  getResourceTypeList,
  exportClearPriceExcel,
  getClearPriceTable,
} from "../api/index"
export default {
  name: "clearPrice",
  components: {
    ecline,
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
  data() {
    return {
      aggregatorId: '',
      resourceTypeId: '',
      resourceTypeList: [],
      monthType: "current",
      dateData: [],
      datePickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now()
        },
      },
      echartsData: [],
      echartsLoading: false,
      tableHeader: [],
      tableData: [],
      tableLoading: false,
    }
  },
  created() {
    this.refreshId = new Date().getTime()
    this.aggregatorId = sessionStorage.getItem("entId")
    this.handleTableHeader()
    const monthStart = moment().month(moment().month()).startOf("month").format("YYYY-MM-DD")
    const monthEnd = moment().format("YYYY-MM-DD")
    this.dateData = [monthStart, monthEnd]
    this.queryResourceTypeList()
  },
  methods: {
    // 处理tableHeader
    handleTableHeader() {
      const fill = number => {
        return number < 10 ? `0${number}` : number
      }
      for (let i = 0; i < 24; i++) {
        this.tableHeader.push({ 'label': `${fill(i)}:00`, 'prop': `${fill(i)}:00` })
        this.tableHeader.push({ 'label': `${fill(i)}:15`, 'prop': `${fill(i)}:15` })
        this.tableHeader.push({ 'label': `${fill(i)}:30`, 'prop': `${fill(i)}:30` })
        this.tableHeader.push({ 'label': `${fill(i)}:45`, 'prop': `${fill(i)}:45` })
      }
    },
    // 获取资源类型数据
    queryResourceTypeList() {
      const params = {
        aggregatorId: sessionStorage.getItem("entId")
      }
      getResourceTypeList(params, this.simulate).then(res => {
        if (res.data.code === 200) {
          if (res.data.data && res.data.data.length > 0) {
            this.resourceTypeList = JSON.parse(JSON.stringify(res.data.data))
            if (this.resourceTypeList.length > 0) {
              for (let i = 0; i < this.resourceTypeList.length; i++) {
                if (this.resourceTypeList[i].display === 1) {
                  this.resourceTypeId = this.resourceTypeList[i].id
                  this.queryData()
                  break
                }
              }
            }
          } else {
            this.resourceTypeList = []
            this.resourceTypeId = ''
          }
        } else {
          this.$message.error(res.data.msg)
        }
      })
    },
    // 资源类型change
    resourceTypeChange() {
      this.queryData()
    },
    // 月类型change
    monthChange(type) {
      this.monthType = type
      let monthStart = ''
      let monthEnd = ''
      if (type === 'current') {
        monthStart = moment().month(moment().month()).startOf("month").format("YYYY-MM-DD")
        monthEnd = moment().format("YYYY-MM-DD")
      } else if (type === 'last') {
        monthStart = moment().month(moment().month() - 1).startOf("month").format("YYYY-MM-DD")
        monthEnd = moment().month(moment().month() - 1).endOf("month").format("YYYY-MM-DD")
      }
      this.dateData = [monthStart, monthEnd]
      this.queryData()
    },
    // 日期change
    dateChange() {
      this.monthType = 'other'
      this.queryData()
    },
    // 查询数据
    queryData() {
      this.queryEcharts()
      this.queryTable()
    },
    // 获取echarts数据
    queryEcharts() {
      const params = {
        aggregatorId: this.aggregatorId,
        resourceTypeId: this.resourceTypeId,
        startTime: this.dateData[0],
        endTime: this.dateData[1],
      }
      this.echartsLoading = true
      getPrice(params, this.simulate).then(res => {
        this.echartsLoading = false
        if (res.data.code === 200) {
          this.echartsData = [ // 
            { name: "出清价格", value: res.data.data.issuePrice }, // 
            { name: "申报价格", value: res.data.data.deliveryPrice }, // 
          ]
        } else {
          this.$message.error(res.data.msg)
          this.echartsData = [ // 
            { name: "出清价格", value: [] }, // 
            { name: "申报价格", value: [] }, // 
          ]
        }
      }).catch(err => {
        this.echartsLoading = false
      })
    },
    // 获取表格数据
    queryTable() {
      const params = {
        aggregatorId: this.aggregatorId,
        resourceTypeId: this.resourceTypeId,
        startTime: this.dateData[0],
        endTime: this.dateData[1],
      }
      this.tableLoading = true
      getClearPriceTable(params, this.simulate).then(res => {
        this.tableLoading = false
        if (res.data.code === 200) {
          const response = res.data.data && res.data.data.rowDataList && res.data.data.rowDataList.length > 0 ? JSON.parse(JSON.stringify(res.data.data.rowDataList)) : []
          let dataList = []
          response.map(rowItem => {
            let rowData = {
              date: rowItem.date,
              sourceTypeName: rowItem.sourceTypeName,
              type: rowItem.type,
            }
            Object.entries(rowItem.valueMap).map(timeItem => {
              rowData[timeItem[0]] = timeItem[1]
            })
            dataList.push(rowData)
          })
          this.tableData = JSON.parse(JSON.stringify(dataList))
        } else {
          this.tableData = []
        }
      }).catch(err => {
        this.tableLoading = false
      })
    },
    // 导出
    exportData() {
      const params = {
        aggregatorId: this.aggregatorId,
        resourceTypeId: this.resourceTypeId,
        startTime: this.dateData[0],
        endTime: this.dateData[1],
      }
      exportClearPriceExcel(params, this.simulate).then(res => {
        const dateRange = `${this.dateData[0]}~${this.dateData[1]}`
        const resourceType = this.codeToName(this.resourceTypeId)
        const fileName = `${dateRange}${resourceType}出清情况.xls`
        downLoadXls(res.data, fileName)
      }).catch(err => {
        console.log(err)
      })
    },
    // codeToName
    codeToName(code) {
      let name = ''
      this.resourceTypeList.map(item => {
        if (item.id === code) {
          name = item.name
        }
      })
      return name
    },
  },
}
</script>
<style lang="less" scoped>
.clearing-price {
  width: calc(100vw - 240px);
  height: calc(100vh - 40px);
  overflow-y: auto;
  .condition-operate-part {
    margin-top: 20px;
    padding: 0 20px;
    height: 60px;
    background: #FFFFFF;
    border-radius: 12px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    .condition {
      display: flex;
      align-items: center;
      >div {
        margin-left: 20px;
        display: flex;
        align-items: center;
      }
      .condition-select {
        margin-left: 0;
        >p {
          &:nth-child(1) {
            margin-right: 10px;
          }
        }
        ::v-deep .el-select {
          width: 120px;
        }
      }
      .condition-tab {
        >p {
          width: 60px;
          height: 30px;
          line-height: 30px;
          text-align: center;
          border: 1px solid #BBBBBB;
          cursor: pointer;
          &:nth-child(1) {
            border-radius: 6px 0 0 6px;
          }
          &:nth-child(2) {
            border-radius: 0 6px 6px 0;
          }
        }
        .active {
          background: #0780ED;
          color: #FFFFFF;
          border-color: #0780ED;
        }
      }
      ::v-deep .el-date-editor {
        width: auto;
        .el-range-input {
          width: 100px;
        }
        .el-input__icon {
          display: none;
        }
      }
    }
    .operate {
      margin-left: 40px;
      ::v-deep .el-button {
        margin-left: 20px;
        &:nth-child(1) {
          margin-left: 0;
        }
      }
    }
  }
  .echarts-part {
    height: 400px;
    padding: 20px;
    margin-top: 20px;
    background: #FFFFFF;
    border-radius: 12px;
  }
  .table-part {
    margin-top: 20px;
    padding: 20px;
    background: #FFFFFF;
    border-radius: 12px;
  }
  /*&::-webkit-scrollbar {
    display: none;
  }
  & ::-webkit-scrollbar {
    height: 6px;
  }*/
}
</style>
