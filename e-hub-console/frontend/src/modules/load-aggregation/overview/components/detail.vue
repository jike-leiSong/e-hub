<template>
  <div class="details-page">
    <div class="page-top">
      <div @click="goBack" class="back-part">
        <p>
          <i class="el-icon-arrow-left" />
          <span>详情</span>
        </p>
      </div>
      <div class="tab-part">
        <div @click="tabChange('2')" :class="{'tab-active': currentTab == '2' }">
          <p>实时汇总</p>
          <p></p>
        </div>
        <div @click="tabChange('3')" :class="{'tab-active': currentTab == '3' }">
          <p>申报计划</p>
          <p></p>
        </div>
      </div>
    </div>
    <div class="summary-situation">
      <div class="title-part">
        <p class="title">汇总情况</p>
        <tabbar @tabClick="resourceTypeChange($event)" />
      </div>
      <div class="echarts-part" v-loading="topLoading">
        <ecline1 :color="currentTab == '3' ? [
        '#44D428',
        '#B06EF3',
      ] : [
        '#0780ED',
        '#44D428',
        '#B06EF3',
        '#F5A623',
      ]" :refreshId="refreshId" :markArea="echartLindData.markAreaObj" :ecdata="echartLindData.chartList" :timeList="echartLindData.timeList" height="300px" />
      </div>
    </div>
    <div v-if="currentTab === '2'" class="user-details">
      <div class="title">
        <p>用户详情</p>
      </div>
      <div class="content-part">
        <div class="user-tree-part">
          <el-tree ref="ref_tree" :data="entUserList" :props="treeDefaultProps" @node-click="entUserChange" node-key="deviceBaseId" :expand-on-click-node="false" highlight-current>
            <div class="custom-tree-node" slot-scope="{ node, data }">
              <!-- 用户 -->
              <div v-if="data.deviceType === '3'" class="tree-user">
                <p class="user-label" :title="data.deviceName">{{data.deviceName}}</p>
                <p v-if="data.winStatu === true" class="user-tag user-successful-bidder">中标</p>
                <p v-if="data.applyStatus === '1'" class="user-tag user-reported">已报</p>
                <p v-else class="user-tag">未报</p>
              </div>
              <!-- 能源站 -->
              <div v-else-if="data.deviceType === '0'" class="tree-station">
                <p :title="data.deviceName">{{data.deviceName}}</p>
              </div>
              <!-- 设备 -->
              <div v-else-if="data.deviceType === '1'" class="tree-device">
                <template>
                  <p v-if="data.resourceTypeId === resourceTypeNameToId('电采暖')" class="label-electric-heating">电采暖</p>
                  <p v-else-if="data.resourceTypeId === resourceTypeNameToId('充电桩')" class="label-charging-station">充电桩</p>
                  <p v-else-if="data.resourceTypeId === resourceTypeNameToId('储能')" class="label-energy-storage">储能</p>
                  <p v-else-if="data.resourceTypeId === resourceTypeNameToId('工业负荷')" class="label-industrial-load">工业负荷</p>
                  <p v-else>--</p>
                </template>
                <p :title="data.deviceName">{{data.deviceName}}</p>
              </div>
            </div>
          </el-tree>
        </div>
        <div class="details-info">
          <!-- 实时汇总 -->
          <div v-if="currentTab === '2'" v-loading="tab2BottomLoading" class="real-time-summary">
            <!-- 用户/能源站：仅显示“有功功率” -->
            <!-- 设备：全部显示 -->
            <div class="rts-echarts">
              <div class="rts-row">
                <div class="rts-item">
                  <p>有功功率</p>
                  <div>
                    <ecline :color="['#0780ED','#FF5227']" unit="kW" :refreshId="refreshId" :ecdata="activePower" :height="entUserData.deviceType === '1' ? '174px' : '450px'" />
                  </div>
                </div>
                <div v-if="entUserData.deviceType === '1'" class="rts-item">
                  <p>无功功率</p>
                  <div>
                    <ecline :color="['#0780ED']" unit="kVar" :refreshId="refreshId" :ecdata="reactivePower" height="174px" />
                  </div>
                </div>
              </div>
              <div v-if="entUserData.deviceType === '1'" class="rts-row">
                <div class="rts-item">
                  <p>用电电流</p>
                  <div>
                    <ecline :color="['#0780ED','#44D428','#B06EF3']" unit="A" :refreshId="refreshId" :ecdata="electricalCurrent" height="174px" />
                  </div>
                </div>
                <div class="rts-item">
                  <p>当日零点电量</p>
                  <div>
                    <ecline :color="['#0780ED']" unit="kWh" :refreshId="refreshId" :ecdata="zeroHourBattery" height="174px" />
                  </div>
                </div>
              </div>
            </div>
            <div class="rts-record">
              <p>执行记录</p>
              <div v-if="executionRecordList.length > 0" class="record-list">
                <div class="record-item" v-for="item in executionRecordList">
                  <p>{{item.sendTime}}</p>
                  <p>
                    <span>{{item.deviceName}}</span>
                    <span>{{item.resultMsg}}</span>
                  </p>
                </div>
              </div>
              <div v-else class="record-default">
                <img src="../images/nodata.png" alt="" />
                <p>暂无执行记录</p>
              </div>
            </div>
          </div>
          <!-- 申报计划 -->
          <div v-else-if="currentTab === '3'" class="declaration-plan">
            <ecline unit="kW" :refreshId="refreshId" :ecdata="planEchartsData" />
          </div>
        </div>
      </div>
    </div>
    <div v-if="currentTab === '3'" class="plan-details">
      <div class="date-part">
        <p v-if="distributionStatus">
          <span class="date-text">{{todayList[0]}}年{{todayList[1]}}月{{todayList[2]}}日</span>
          <span>的调峰服务运行计划已自动拆分并下发至中标用户</span>
        </p>
        <p v-else>
          <span>未收到调度下发的</span>
          <span class="date-text">{{todayList[0]}}年{{todayList[1]}}月{{todayList[2]}}日</span>
          <span>行计划功率曲线</span>
        </p>
      </div>
    </div>
  </div>
</template>
<script>
import moment from "moment"
import tabbar from "./tabbar"
import ecline from "../components/ec_line"
import ecline1 from "../components/ec_line1"
import {
  getOverview,
  getIotLog,
  getPriceByResourceTypeId,
  getResourceTypeList,
  getTomorrowEntUserDeviceChartResp,
  doSaveOperation,
  getEntUserTree,
  getRealTimeSummaryEcharts4,
  getPlanDistribution,
} from "../api"
export default {
  name: "detail",
  components: {
    tabbar,
    ecline,
    ecline1
  },
  props: {
    dateType: {
      type: String,
      require: true,
    },
    refreshId: {
      type: Number,
    },
  },
  data() {
    return {
      aggregatorId: '',
      // 今日
      todayList: '',
      // 当前资源类型ID
      currentResourceTypeId: '',
      resourceTypeList: [],
      // tab
      currentTab: "2",
      // 企业用户信息
      entUserData: {},
      entUserList: [],
      // 企业用户树配置
      treeDefaultProps: {
        label: 'deviceName',
        children: 'children',
        applyStatus: 'applyStatus',
      },
      // 汇总情况echarts数据
      echartLindData: {},
      // 实时汇总 - 执行记录
      executionRecordList: [],
      // 实时汇总 - echarts数据 - 有功功率
      activePower: [],
      // 实时汇总 - echarts数据 - 无功功率
      reactivePower: [],
      // 实时汇总 - echarts数据 - 用电电流
      electricalCurrent: [],
      // 实时汇总 - echarts数据 - 当日零点电量
      zeroHourBattery: [],
      // 申报计划 - echarts数据
      planEchartsData: [],
      // 申报计划 - 计划下发状态
      distributionStatus: false,
      // loading
      topLoading: false,
      tab2BottomLoading: false,
    }
  },
  created() {
    this.todayList = moment(new Date()).format("YYYY-MM-DD").split('-')
    this.aggregatorId = sessionStorage.getItem("entId") || sessionStorage.getItem("cid")
    this.queryResourceType()
    this.tabChange(this.dateType, 'init')
  },
  methods: {
    goBack() {
      this.$emit("goBack")
    },
    // tabChange
    tabChange(type, init) {
      this.currentTab = type
      if (type === '2') {
        init !== 'init' ? this.realTimeSummary() : null
        this.queryEntUserList("today")
      } else if (type === '3') {
        init !== 'init' ? this.planSituation() : null
        init !== 'init' ? this.planDistribution() : null
        /**
         * 鑫泰项目二期（2023/10/26）：申报计划用户详情企业用户树型结构不进行数据交互
         */
      }
    },
    // 获取资源类型
    queryResourceType() {
      const params = {
        aggregatorId: this.aggregatorId
      }
      getResourceTypeList(params).then(res => {
        if (res.data.code === 200) {
          if (res.data.data && res.data.data.length > 0) {
            this.resourceTypeList = JSON.parse(JSON.stringify(res.data.data))
            for (let i = 0; i < res.data.data.length; i++) {
              if (res.data.data[i].display === 1) {
                this.currentResourceTypeId = res.data.data[i].id
                this.resourceTypeChange(res.data.data[i])
                break
              }
            }
          } else {
            this.resourceTypeList = []
          }
        } else {
          this.$message.error(res.data.msg)
        }
      })
    },
    // 资源类型Change
    resourceTypeChange(data) {
      this.currentResourceTypeId = data.id
      if (this.currentTab === '2') {
        this.realTimeSummary()
        this.queryEntUserList()
      } else if (this.currentTab === '3') {
        this.planSituation()
        this.planDistribution()
      }
    },
    // 资源类型name换id
    resourceTypeNameToId(name) {
      let id = ''
      this.resourceTypeList.map(item => {
        if (item.name === name) {
          id = item.id
        }
      })
      return id
    },
    // 获取企业用户列表
    queryEntUserList(dayType) {
      const params = {
        aggregatorId: this.aggregatorId,
        resourceType: this.currentResourceTypeId
      }
      getEntUserTree(params).then(res => {
        if (res.data.code === 200) {
          if (res.data.data && res.data.data.length > 0) {
            this.entUserList = JSON.parse(JSON.stringify(res.data.data))
            this.entUserChange(res.data.data[0])
            this.$nextTick(() => {
              this.$refs.ref_tree.setCurrentKey(res.data.data[0].deviceBaseId)
            })
          } else {
            this.entUserList = []
            this.entUserData = {}
          }
        } else {
          this.$message.error(res.data.msg)
        }
      })
    },
    // 企业用户change
    entUserChange(item) {
      this.entUserData = item
      if (this.currentTab === '2') {
        // 实时汇总 - 获取echarts4
        this.realTimeEcharts4()
        // 实时汇总 - 获取执行记录
        this.realTimeExecutionRecord()
      } else if (this.currentTab === '3') {
        // 申报计划 - 获取echarts1
        this.planEcharts1()
      }
    },
    // 实时汇总 - 汇总情况
    realTimeSummary() {
      const params = {
        aggregatorId: this.aggregatorId,
        resourceTypeId: this.currentResourceTypeId,
        dayType: "today",
      }
      this.topLoading = true
      getOverview(params).then(res => {
        this.topLoading = false
        if (res.data.code === 200) {
          const chartList = [{
            name: "实际功率",
            yAxisIndex: 0,
            value: res.data.data.powerChart ? res.data.data.powerChart : [],
          },{
            name: "聚合申报功率",
            yAxisIndex: 0,
            value: res.data.data.issueChart ? res.data.data.issueChart : [],
          },{
            name: "调度下发功率",
            yAxisIndex: 0,
            value: res.data.data.dapChart ? res.data.data.dapChart : [],
          },{
              name: "碳排因子",
              yAxisIndex: 1,
              value: res.data.data.crChart ? res.data.data.crChart : [],
          }]
          res.data.data.chartList = chartList
          res.data.data.markAreaObj = {
            itemStyle: {
              color: "#F4D853",
              opacity: 0.1,
            },
            data: res.data.data.timeColorRespList,
          }
          this.echartLindData = res.data.data
        } else {
          this.$message.error(res.data.msg)
        }
      }).catch(err => {
        this.topLoading = false
      })
    },
    // 申报计划 - 汇总情况
    planSituation() {
      const params = {
        aggregatorId: this.aggregatorId,
        resourceTypeId: this.currentResourceTypeId,
        dayType: "tomorrow",
      }
      this.topLoading = true
      getOverview(params).then(res => {
        this.topLoading = false
        if (res.data.code === 200) {
          const chartList = [{
            name: "聚合申报功率",
            value: res.data.data.deliveryChart ? res.data.data.deliveryChart : [],
          }, {
            name: "调度下发功率",
            value: res.data.data.dapChart ? res.data.data.dapChart : [],
          }]
          res.data.data.chartList = chartList
          this.echartLindData = res.data.data
        } else {
          this.$message.error(res.data.msg)
        }
      }).catch(err => {
        this.topLoading = false
      })
    },
    // 申报计划 - 查询是否有计划下发
    planDistribution() {
      const params = {
        aggregatorId: this.aggregatorId,
        resourceTypeId: this.currentResourceTypeId,
        date: moment(new Date()).format("YYYY-MM-DD"),
      }
      getPlanDistribution(params).then(res => {
        if (res.data.code === 200) {
          if (res.data.data === true) {
            this.distributionStatus = true
          } else {
            this.distributionStatus = false
          }
        } else {
          this.distributionStatus = false
          this.$message.error(res.data.msg)
        }
      })
    },
    // 实时汇总 - 获取echarts4
    realTimeEcharts4() {
      const params = {
        systemCode: this.entUserData.deviceType === '3' ? this.entUserData.deviceBaseId : '',
        energyStationcode: this.entUserData.deviceType === '0' ? this.entUserData.deviceBaseId : '',
        deviceBaseId: this.entUserData.deviceType === '1' ? this.entUserData.deviceBaseId : '',
      }
      this.tab2BottomLoading = true
      getRealTimeSummaryEcharts4(params).then(res => {
        this.tab2BottomLoading = false
        if (res.data.code === 200) {
          const responseData = JSON.parse(JSON.stringify(res.data.data))
          // 有功功率
          const activePower = [{
            name: "实际功率",
            value: responseData.entUserDeviceYesterdayChartResp && responseData.entUserDeviceYesterdayChartResp.powerChart ? JSON.parse(JSON.stringify(responseData.entUserDeviceYesterdayChartResp.powerChart)) : [],
          },{
            name: "基线负荷",
            value: responseData.entUserDeviceYesterdayChartResp && responseData.entUserDeviceYesterdayChartResp.baseLineChart ? JSON.parse(JSON.stringify(responseData.entUserDeviceYesterdayChartResp.baseLineChart)) : [],
          }]
          /* 
            有功功率：分解后功率
            {
              name: "分解后功率",
              value: responseData.entUserDeviceYesterdayChartResp && responseData.entUserDeviceYesterdayChartResp.issueChart ? JSON.parse(JSON.stringify(responseData.entUserDeviceYesterdayChartResp.issueChart)) : [],
            }
          */
          // 无功功率
          const reactivePower = [{
            name: "无功功率",
            value: responseData.noPowerChart || [],
          }]
          // 用电电流
          const electricalCurrent = [{
            name: "a相电流",
            value: responseData.entUserDeviceTodayElectricCurrentChartResp && responseData.entUserDeviceTodayElectricCurrentChartResp.iaList ? JSON.parse(JSON.stringify(responseData.entUserDeviceTodayElectricCurrentChartResp.iaList)) : [],
          }, {
            name: "b相电流",
            value: responseData.entUserDeviceTodayElectricCurrentChartResp && responseData.entUserDeviceTodayElectricCurrentChartResp.ibList ? JSON.parse(JSON.stringify(responseData.entUserDeviceTodayElectricCurrentChartResp.ibList)) : [],
          }, {
            name: "c相电流",
            value: responseData.entUserDeviceTodayElectricCurrentChartResp && responseData.entUserDeviceTodayElectricCurrentChartResp.icList ? JSON.parse(JSON.stringify(responseData.entUserDeviceTodayElectricCurrentChartResp.icList)) : [],
          }]
          // 当日零点电量
          const zeroHourBattery = [{
            name: "电量",
            value: responseData.zeroPointElectricityQuantityChart || [],
          }]
          this.activePower = activePower
          this.reactivePower = reactivePower
          this.electricalCurrent = electricalCurrent
          this.zeroHourBattery = zeroHourBattery
        } else {
          this.$message.error(res.data.msg)
        }
      }).catch(err => {
        this.tab2BottomLoading = false
      })
    },
    // 【注释】 实时汇总 - 获取执行记录
    realTimeExecutionRecord() {
      /**
       * 鑫泰项目二期（2023/10/26）：执行记录不进行数据交互
       */
      /*const params = {
        aggregatorId: this.aggregatorId,
        entId: this.entUserData.entId,
        stationId: this.entUserData.stationId,
        deviceBaseId,
        resourceTypeId: this.currentResourceTypeId,
      }
      getIotLog(params).then(res => {
        if (res.data.code === 200) {
          this.executionRecordList = res.data.data && res.data.data.length > 0 ? JSON.parse(JSON.stringify(res.data.data)) : []
        } else {
          this.$message.error(res.data.msg)
        }
      })*/
    },
    // 【注释】 申报计划 - 获取echarts1
    planEcharts1() {
      /**
       * 鑫泰项目二期（2023/10/26）：申报计划用户详情曲线图不进行数据交互
       */
      /*const params = {
        deviceBaseId: this.deviceBaseId
      }
      getTomorrowEntUserDeviceChartResp(params).then(res => {
        if (res.data.code === 200) {
          const dataList = [{
            name: "用户申报功率",
            value: res.data.data.deliveryChart,
          }, {
            name: "分解后设备功率",
            value: res.data.data.issueChart,
          }]
          this.planEchartsData = dataList
        } else {
          this.$message.error(res.data.msg)
        }
      })*/
    },
  },
}
</script>
<style lang="less" scoped>
.details-page {
  width: 100%;
  background: #F4F5F9;
  display: flex;
  flex-direction: column;
  overflow-y: hidden;
  .page-top {
    display: flex;
    background: #FFFFFF;
    .back-part {
      padding: 20px;
      >p {
        height: 20px;
        line-height: 20px;
        color: #666666;
        display: inline-flex;
        align-items: center;
        cursor: pointer;
        >i {
          margin-right: 4px;
        }
      }
    }
    .tab-part {
      margin-left: 40px;
      display: flex;
      align-items: center;
      >div {
        margin-top: 6px;
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-right: 20px;
        cursor: pointer;
        >p {
          &:nth-child(1) {
            font-size: 16px;
            color: #666666;
          }
          &:nth-child(2) {
            margin-top: 6px;
            width: 20px;
            height: 3px;
            background: #FFFFFF;
            border-radius: 2px;
          }
        }
      }
      .tab-active {
        >p {
          &:nth-child(1) {
            color: #0780ED;
          }
          &:nth-child(2) {
            background: #0780ED;
          }
        }
      }
    }
  }
  .summary-situation {
    height: auto;
    margin: 20px;
    padding: 20px;
    background: #FFFFFF;
    border-radius: 12px;
    display: flex;
    flex-direction: column;
    .title-part {
      height: 32px;
      display: flex;
      justify-content: space-between;
      align-items: cetern;
      .title {
        line-height: 32px;
        font-size: 18px;
        font-weight: bolder;
        color: #333333;
      }
    }
    .echarts-part {
      margin-top: 20px;
    }
  }
  .user-details {
    height: 600px;
    margin: 0 20px 20px;
    padding: 20px;
    background: #FFFFFF;
    border-radius: 12px;
    display: flex;
    flex-direction: column;
    .title {
      margin-bottom: 20px;
      >p {
        line-height: 32px;
        font-size: 18px;
        font-weight: bolder;
        color: #333333;
      }
    }
    .content-part {
      flex: 1;
      display: flex;
      overflow: hidden;
      .user-tree-part {
        width: 280px;
        min-width: 280px;
        height: 100%;
        padding-right: 20px;
        border-right: 1px dashed #D8D8D8;
        overflow-y: auto;
        overflow-x: hidden;
        ::v-deep .el-tree {
          .el-tree-node {
            display: flex;
            flex-direction: column;
            .el-tree-node__content {
              height: 34px;
              display: flex;
              .custom-tree-node {
                flex: 1;
                overflow: hidden;
                height: 100%;
              }
            }
          }
          .tree-user {
            height: 100%;
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0 10px;
            .user-label {
              flex: 1;
              line-height: 20px;
              overflow: hidden;
              white-space: nowrap;
              text-overflow: ellipsis;
            }
            .user-tag {
              width: 50px;
              height: 20px;
              line-height: 18px;
              margin-left: 10px;
              border-radius: 12px;
              border: 1px solid #999999;
              box-sizing: border-box;
              text-align: center;
              font-size: 12px;
              color: #999999;
            }
            .user-successful-bidder {
              color: #FF0000 !important;
              border-color: #FF0000 !important;
            }
            .user-reported {
              color: #0780ED !important;
              border-color: #0780ED !important;
            }
          }
          .tree-station {
            height: 100%;
            display: flex;
            align-items: center;
            padding: 0 10px;
            >p {
              flex: 1;
              line-height: 20px;
              overflow: hidden;
              white-space: nowrap;
              text-overflow: ellipsis;
            }
          }
          .tree-device {
            height: 100%;
            display: flex;
            align-items: center;
            padding: 0 10px;
            >p {
              &:nth-child(1) {
                width: 56px;
                height: 18px;
                line-height: 16px;
                text-align: center;
                border-radius: 4px;
                color: #999999;
                background: #FFFFFF;
                border: 1px solid #999999;
                font-size: 12px;
                font-family: PingFangSC-Medium, PingFang SC;
                font-weight: 500;
              }
              &:nth-child(2) {
                margin-left: 10px;
                flex: 1;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
              }
            }
            .label-electric-heating {
              color: #FF9200 !important;
              border-color: #FF9200 !important;
              background: rgba(255, 146, 0, 0.05) !important;
            }
            .label-charging-station {
              color: #6DD400 !important;
              border-color: #6DD400 !important;
              background: rgba(109, 212, 0, 0.05) !important;
            }
            .label-energy-storage {
              color: #0780ED !important;
              border-color: #0780ED !important;
              background: rgba(7, 128, 237, 0.05) !important;
            }
            .label-industrial-load {
              color: #FF6B00 !important;
              border-color: #FF6B00 !important;
              background: rgba(255, 107, 0, 0.05) !important;
            }
          }
        }
      }
      .details-info {
        flex: 1;
        overflow: hidden;
        padding-left: 20px;
        height: 100%;
        .real-time-summary {
          width: 100%;
          height: 100%;
          display: flex;
          .rts-echarts {
            flex: 1;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            .rts-row {
              flex: 1;
              box-sizing: border-box;
              display: flex;
              justify-content: space-between;
              &:nth-child(2) {
                margin-top: 20px;
              }
              .rts-item {
                flex: 1;
                padding: 20px;
                margin-right: 20px;
                border-radius: 4px;
                border: 1px solid #EDEDED;
                box-sizing: border-box;
                display: flex;
                flex-direction: column;
                >p {
                  color: #333333;
                  font-size: 14px;
                  font-weight: bolder;
                  margin-bottom: 20px;
                }
                >div {
                  width: 100%;
                  flex: 1;
                }
              }
            }
          }
          .rts-record {
            width: 260px;
            min-width: 260px;
            padding-left: 20px;
            border-left: 1px dashed #D8D8D8;
            >p {
              padding: 20px 0;
              border-bottom: 1px dashed #D8D8D8;
              color: #333333;
              font-size: 14px;
              font-weight: bolder;
            }
            .record-list {
              .record-item {
                display: flex;
                align-items: center;
                line-height: 20px;
                margin-bottom: 20px;
                >p {
                  font-size: 14px;
                  &:nth-child(1) {
                    color: #333333;
                    margin-right: 20px;
                  }
                  &:nth-child(2) {
                    margin-top: 10px;
                    color: #999999;
                  }
                }
              }
            }
            .record-default {
              height: 100%;
              display: flex;
              flex-direction: column;
              justify-content: center;
              align-items: center;
              >img {
                width: 150px;
                height: auto;
              }
              >p {
                margin-top: 20px;
                color: #666666;
                font-size: 14px;
                font-weight: bolder;
              }
            }
          }
        }
        .declaration-plan {
          width: 100%;
          height: 100%;
        }
      }
    }
  }
  .plan-details {
    margin: 0 20px 20px;
    padding: 20px;
    background: #FFFFFF;
    border-radius: 12px;
    .date-part {
      >p {
        line-height: 20px;
        >span {
          line-height: 20px;
        }
        .date-text {
          padding: 0 2px;
          color: #333333;
          font-weight: bolder;
        }
      }
    }
  }
}
</style>
<style lang="less">
.details-page {
  .el-tree--highlight-current .el-tree-node.is-current>.el-tree-node__content {
    background: rgba(7, 128, 237, .2);
    border-radius: 4px;
    box-shadow: 0px 2px 12px 0px rgba(0, 0, 0, 0.1);
  }
}
</style>
