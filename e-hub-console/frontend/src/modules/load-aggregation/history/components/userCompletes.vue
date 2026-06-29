<template>
  <div class="user-completed-adjustment-status">
    <div class="summary-part">
      <div>
        <p>汇总功率曲线</p>
        <div>
          <tabbar @tabClick="resourceTypeChange($event)"></tabbar>
          <el-date-picker v-model="topDateData" @change="topDateChange" type="daterange" value-format="yyyy-MM-dd" :clearable="false" size="small" :picker-options="topDatePickerOptions" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
          <el-button @click="topExport" type="primary" size="small">导出</el-button>
          <el-button :loading="buzhaoLoading" @click="buzhaoExport" type="primary" size="small">上送数据导出</el-button>
        </div>
      </div>
      <div v-loading="echartsLoading1">
        <ecline1 :refreshId='refreshId' :ecdata='topEchartsData' :timeList="timeList" :markArea='topEchartsMarkArea' />
      </div>
    </div>
    <div class="condition-part">
      <div class="condition-item">
        <div>
          <p>企业名称</p>
          <p>
            <el-select v-model="form.enterpriseName" @change="queryData('entName')" filterable placeholder="请选择" size="small">
              <el-option v-for="item in enterpriseList" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </p>
        </div>
        <!-- <div>
          <p>资源类型</p>
          <p>
            <el-select v-model="form.resourceTypeId" @change="queryData('resourceType')" filterable placeholder="请选择" size="small">
              <el-option v-for="item in resourceTypeList" :key="item.id" :label="item.name" :value="item.id" :disabled="item.display !== 1" />
            </el-select>
          </p>
        </div> -->
        <div>
          <p>时间</p>
          <p>
            <el-date-picker v-model="form.bottomDateData" @change="queryData" value-format="yyyy-MM-dd" type="daterange" size="small" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
          </p>
        </div>
      </div>
      <div class="buttons-part">
        <el-button @click="reset" size="small" plain>重置</el-button>
        <el-button @click="bottomExport" size="small" type="primary">导出</el-button>
      </div>
    </div>
    <div class="content-part">
      <div v-if="incomeNumber" class="income-part">
        <div>
          <img src="../images/money.png" alt="" />
          <span>用户收益</span>
          <span class="number">{{incomeNumber}}</span>
          <span>{{incomeUnit}}</span>
        </div>
      </div>
      <div v-loading="echartsLoading2" class="echarts-part">
        <ecline :color="['#AE716E',
        '#911FA8',
        '#C13042']" unit='kW' :timeList="timeList" :refreshId='refreshId' :ecdata='bottomEchartsData' :markArea='bottomEchartsMarkArea' />
      </div>
    </div>
  </div>
</template>
<script>
import tabbar from './tabbar'
import ecline1 from './ec_line1'
import ecline from './ec_line'
import moment from 'moment'
import { downLoadXls } from "@/utils/util.js"
import  mixin  from './mixin.js'
import {
  doSaveOperation,
  getEntUserOptions,
  getUserCompletionEcharts,
  getResourceTypeList,
  getTotalPowerChart,
  exportExcel,
  exportBuZhaoUploadData,
} from '../api'
export default {
  name: 'userCompletes',
  mixins: [mixin],
  components: {
    tabbar,
    ecline,
    ecline1,
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
  data() {
    return {
      aggregatorId: '',
      // 上方 - 资源类型
      resourceTypeId: '',
      // 上方 - 日期
      topDateData: [],
      topDatePickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now()
        },
      },
      // 上方 - echarts
      topEchartsData: [],
      timeList: [],
      topEchartsMarkArea: {},
      echartsLoading1: false,
      // 下方 - 企业列表
      enterpriseList: [],
      // 下方 - 资源类型列表
      resourceTypeList: [],
      // 下方 - form
      form: {
        enterpriseName: '',
        resourceTypeId: '',
        bottomDateData: [],
      },
      // 下方 - 用户收益
      incomeNumber: 0,
      incomeUnit: '',
      // 下方 - echarts
      bottomEchartsData: [],
      bottomEchartsMarkArea: {},
      echartsLoading2: false,
      buzhaoLoading:false,
    }
  },
  created() {
    this.aggregatorId = sessionStorage.getItem("entId") || sessionStorage.getItem("cid")
    const yesterday = moment().subtract(1, "days").format("YYYY-MM-DD")
    this.topDateData = [yesterday, yesterday]
    this.form.bottomDateData = [yesterday, yesterday]
    // await this.queryEnterpriseList()
    // await this.queryResourceTypeList()
    // await this.queryData()
  },
  methods: {
    // 上方 - 资源类型change
    resourceTypeChange(data) {
      this.resourceTypeId = data.id
      this.queryTopEcharts()
      this.queryEnterpriseList().then(() => {
        this.queryBottomEcharts()
      })
    },
    // 上方 - dateChange
    topDateChange() {
      this.queryTopEcharts()
    },
    // 【改】 上方 - 导出
    topExport() {
      const params = {
        aggregatorId: this.aggregatorId,
        sourceId: this.resourceTypeId,
        startDate: this.topDateData[0],
        endDate: this.topDateData[1],
      }
      exportExcel(params).then(res => {
        // const dateRange = `${this.topDateData[0]}~${this.topDateData[1]}`
        // const resourceType = this.codeToName('resourceType', this.resourceTypeId)
        // const fileName = `${dateRange}${resourceType}调节效果统计.xls`
        // downLoadXls(res.data, fileName)
        this.downloadFile(res)
      }).catch(err => {
        console.log(err)
      })
    },
    buzhaoExport() {
      this.buzhaoLoading = true
      const params = {
        aggregatorId: this.aggregatorId,
        sourceId: this.resourceTypeId,
        startDate: this.topDateData[0],
        endDate: this.topDateData[1],
      }
      exportBuZhaoUploadData(params).then(res => {
        this.downloadFile(res)
      }).catch(err => {
        console.log(err)
      }).finally(() => {
        this.buzhaoLoading = false
      })
    },
    // 【改】 上方 - 获取上方echarts
    queryTopEcharts() {
      const params = {
        aggregatorId: this.aggregatorId,
        resourceTypeId: this.resourceTypeId,
        startDate: this.topDateData[0],
        endDate: this.topDateData[1],
      }
      this.echartsLoading1 = true
      getTotalPowerChart(params).then(res => {
        this.echartsLoading1 = false
        if (res.data.code === 200) {
          this.topEchartsData = [ // 
            { name: "实际汇总功率", value: res.data.data.powerChart, yAxisIndex: 0 }, // 
            { name: "聚合申报功率", value: res.data.data.issueChart, yAxisIndex: 0 }, // 
            { name: "调度下发功率", value: res.data.data.dapChart, yAxisIndex: 0 }, //
            { name: "基线", value: res.data.data.baseLineChart, yAxisIndex: 0 }, //
            { name: "碳排因子", value: res.data.data.crChart, yAxisIndex: 1 }, // 
            { name: "出清价格", value: res.data.data.issuePrice, yAxisIndex: 0 }, // 
          ]
          this.timeList = res.data.data.timeList
          res.data.data.markAreaObj = {
            itemStyle: { color: "#F4D853", opacity: 0.4 },
            data: res.data.data.timeColorRespList,
          }
          this.topEchartsMarkArea = res.data.data.markAreaObj
        } else {
          this.$message.error(res.data.msg)
          this.timeList = res.data.data.timeList
          this.topEchartsData = [ // 
            { name: "调度下发功率", value: [], yAxisIndex: 0 }, // 
            { name: "实际汇总功率", value: [], yAxisIndex: 0 }, // 
            { name: "基线", value: [], yAxisIndex: 0 }, // 
            { name: "碳排因子", value: [], yAxisIndex: 1 }, // 
          ]
          this.topEchartsMarkArea = {}
        }
      }).catch(err => {
        this.echartsLoading1 = false
      })
    },
    // 下方 - 获取企业列表
    async queryEnterpriseList() {
      const params = {
        aggregatorId: this.aggregatorId,
        resourceTypeId: this.resourceTypeId,
      }
      await getEntUserOptions(params).then(res => {
        if (res.data.code === 200) {
          if (res.data.data && res.data.data.length > 0) {
            this.enterpriseList = JSON.parse(JSON.stringify(res.data.data)).filter(item => item != null)
            if (this.enterpriseList.length > 0 && this.enterpriseList[0] && this.enterpriseList[0].value) {
              this.form.enterpriseName = this.enterpriseList[0].value
            } else {
              this.form.enterpriseName = ''
            }
          } else {
            this.enterpriseList = []
            this.form.enterpriseName = ''
          }
        } else {
          this.$message.error(res.data.msg)
        }
      })
    },
    // 下方 - 获取资源类型数据
    async queryResourceTypeList() {
      const params = {
        aggregatorId: sessionStorage.getItem("entId"),
        entId: this.form.enterpriseName
      }
      await getResourceTypeList(params).then(res => {
        if (res.data.code === 200) {
          if (res.data.data && res.data.data.length > 0) {
            this.resourceTypeList = JSON.parse(JSON.stringify(res.data.data))
            /*for (let i = 0; i < this.resourceTypeList.length; i++) {
              if (this.resourceTypeList[i].display === 1) {
                this.form.resourceTypeId = this.resourceTypeList[i].id
                break
              }
            }*/
            this.form.resourceTypeId = ''
          } else {
            this.resourceTypeList = []
            this.form.resourceTypeId = ''
          }
        } else {
          this.$message.error(res.data.msg)
        }
      })
    },
    // 下方 - 查询
    queryData(type) {
      if (!this.form.enterpriseName) {
        this.$message.warning('请选择企业')
        return
      }
      // if (!this.form.resourceTypeId) {
      //   this.$message.warning('请选择资源类型')
      //   return
      // }
      this.queryBottomEcharts()
    },
    // 下方 - 重置
    reset() {
      this.bottomEchartsData = []
      this.bottomEchartsMarkArea = {}
      this.queryEnterpriseList()
      // this.queryResourceTypeList()
      const yesterday = moment().subtract(1, "days").format("YYYY-MM-DD")
      this.form.bottomDateData = [yesterday, yesterday]
    },
    // 【改】 下方 - 导出
    bottomExport() {
      const params = {
        aggregatorId: this.aggregatorId,
        sourceId: this.resourceTypeId,
        entId: this.form.enterpriseName,
        startDate: this.form.bottomDateData[0],
        endDate: this.form.bottomDateData[1],
      }
      exportExcel(params).then(res => {
        // const dateRange = `${this.form.bottomDateData[0]}~${this.form.bottomDateData[1]}`
        // const entName = this.codeToName('entName', this.form.enterpriseName)
        // const resourceType = this.codeToName('resourceType', this.resourceTypeId)
        // const fileName = `${dateRange}${entName}${resourceType}调节效果统计.xls`
        // downLoadXls(res.data, fileName)
        this.downloadFile(res)
      }).catch(err => {
        console.log(err)
      })
    },
    // 【改】 下方 - 获取下方echarts
    queryBottomEcharts() {
      const params = {
        subEntId: this.form.enterpriseName,
        resourceTypeId: this.resourceTypeId,
        startTime: this.form.bottomDateData[0],
        endTime: this.form.bottomDateData[1],
      }
      this.echartsLoading2 = true
      getUserCompletionEcharts(params).then(res => {
        this.echartsLoading2 = false
        if (res.data.code === 200) {
          this.incomeNumber = res.data.data.profit || 0
          this.incomeUnit = res.data.data.profitUnit || ''
          this.bottomEchartsData = [ //
            { name: "有效调节负荷", value: res.data.data.adjustPower }, // 
            { name: "实际调节功率", value: res.data.data.powerChart }, // 
            { name: "基线", value: res.data.data.baseLineChart }, // 
          ]
          res.data.data.markAreaObj = {
            itemStyle: { color: "#FF5227", opacity: 0.4 },
            data: res.data.data.timeColorRespList,
          }
          this.bottomEchartsMarkArea = res.data.data.markAreaObj
        } else {
          this.$message.error(res.data.msg)
          this.incomeNumber = 0
          this.incomeUnit = ''
          this.bottomEchartsData = [ // 
            { name: "有效调节负荷", value: [] }, //  
            { name: "实际调节功率", value: [] }, // 
            { name: "基线", value: [] }, // 
          ]
          this.bottomEchartsMarkArea = {}
        }
      }).catch(err => {
        this.echartsLoading2 = false
      })
    },
    // codeToName
    codeToName(type, code) {
      let name = ''
      if (type === 'resourceType') { // 资源类型
        this.resourceTypeList.map(item => {
          if (item.id === code) {
            name = item.name
          }
        })
      } else if (type === 'entName') {
        this.enterpriseList.map(item => {
          if (item.value === code) {
            name = item.label
          }
        })
      }
      return name
    },
  },
}
</script>
<style lang="less" scoped>
.user-completed-adjustment-status {
  width: calc(100vw - 240px);
  height: calc(100vh - 40px);
  overflow-y: auto;
  .summary-part {
    margin-top: 20px;
    padding: 20px;
    background: #FFFFFF;
    border-radius: 12px;
    >div {
      &:nth-child(1) {
        height: 40px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        >p {
          font-size: 18px;
          font-weight: bolder;
        }
        >div {
          display: flex;
          align-items: center;
          ::v-deep .el-date-editor {
            width: auto;
            margin: 0 20px;
            .el-range-input {
              width: 100px;
            }
            .el-input__icon {
              display: none;
            }
          }
        }
      }
      &:nth-child(2) {
        margin-top: 20px;
        width: 100%;
        height: 400px;
      }
    }
  }
  .condition-part {
    margin: 20px 0;
    padding: 20px;
    background: #FFFFFF;
    border-radius: 12px;
    display: flex;
    justify-content: space-between;
    .condition-item {
      display: flex;
      align-items: center;
      >div {
        margin-left: 20px;
        display: flex;
        align-items: center;
        &:nth-child(1) {
          margin-left: 0;
        }
        &:nth-child(2) {
          >p {
            &:nth-child(2) {
              ::v-deep .el-select {
                width: 120px;
              }
            }
          }
        }
        >p {
          &:nth-child(1) {
            margin-right: 10px;
          }
          &:nth-child(2) {
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
        }
      }
    }
    .buttons-part {
      margin-left: 40px;
      ::v-deep .el-button {
        margin-left: 20px;
        &:nth-child(1) {
          margin-left: 0;
        }
      }
    }
  }
  .content-part {
    margin-bottom: 20px;
    padding: 20px;
    background: #FFFFFF;
    border-radius: 12px;
    .income-part {
      display: inline-block;
      margin-bottom: 20px;
      >div {
        height: 32px;
        padding: 0 20px;
        display: flex;
        align-items: center;
        background: #EEF5FC;
        border-radius: 16px;
        >img {
          width: 18px;
          margin-right: 10px;
        }
        >span {
          font-size: 14px;
          color: #666666;
        }
        .number {
          margin: 0 6px;
          color: #0780ED;
        }
      }
    }
    .echarts-part {
      width: 100%;
      height: 350px;
    }
  }
  /*&::-webkit-scrollbar {
    display: none;
  }*/
}
</style>
