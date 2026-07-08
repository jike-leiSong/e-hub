<template>
  <div class="declaration-plan">
    <header class="page-header">
      <button type="button" class="back-button" @click="goBack">
        <i class="el-icon-arrow-left" />
        <span>返回运营总览</span>
      </button>
      <div class="page-title">
        <h1>申报计划</h1>
        <p>{{activeResourceName || '全部资源类型'}}</p>
      </div>
      <div class="header-actions">
        <el-button @click="reportData" icon="el-icon-upload2" size="small">预测数据上报</el-button>
        <el-button @click="addPlan" icon="el-icon-plus" type="primary" size="small">创建申报计划</el-button>
      </div>
    </header>
    <div class="main-content">
      <div class="overview-info">
        <div class="weather">
          <div class="weather-date">
            <p>今日天气</p>
            <span>{{today}}</span>
          </div>
          <div class="weather-details">
            <template v-if="weatherData !== 'noData'">
              <div class="img-part">
                <img v-if="weatherData.condition_day === '晴'" src="../../images/qing.png" alt="" />
                <img v-else-if="weatherData.condition_day === '阴'" src="../../images/yin.png" alt="" />
                <img v-else-if="weatherData.condition_day === '多云'" src="../../images/duoyun.png" alt="" />
                <img v-else-if="weatherData.condition_day === '雾霾'" src="../../images/wumai.png" alt="" />
                <img v-else-if="weatherData.condition_day === '雾'" src="../../images/dawu.png" alt="" />
                <img v-else-if="weatherData.condition_day === '雨'" src="../../images/xiaoyu.png" alt="" />
                <img v-else-if="weatherData.condition_day === '雪'" src="../../images/xue.png" alt="" />
              </div>
              <div class="info-part">
                <p>{{weatherData.temp_day || '--'}}/{{weatherData.temp_night || '--'}}℃</p>
                <p>{{weatherData.condition_day || '--'}}</p>
                <p>{{weatherData.wind_dir_day || '--'}}{{weatherData.wind_speed_day || '--'}}级</p>
              </div>
            </template>
            <template v-else>
              <p class="default-part">暂无天气数据</p>
            </template>
          </div>
        </div>
        <div class="tab">
          <div class="tab-title">资源类型</div>
          <p v-for="item in resourcePlanList" :key="item.sourceId" :class="{'active': planId === item.sourceId}">
            <span @click="planChange(item)">{{item.resourceName || codeToName('resourceType', item.sourceId)}}</span>
            <em>{{planDataCount(item)}}</em>
          </p>
        </div>
      </div>
      <div class="content-part">
        <div class="content-toolbar">
          <div>
            <h2>申报计划</h2>
            <p>{{activeResourceName || '按资源类型维护计划周期和申报曲线'}}</p>
          </div>
          <div class="summary-boxes">
            <div>
              <span>资源类型</span>
              <strong>{{resourcePlanList.length}}</strong>
            </div>
            <div>
              <span>计划数量</span>
              <strong>{{planTotal}}</strong>
            </div>
          </div>
        </div>
        <div v-if="resourcePlanList.length > 0" class="content-list">
          <section v-for="(item, index) in resourcePlanList" :key="item.sourceId" :ref="`ref_details${item.sourceId}`" class="content-item">
            <div class="resource-heading">
              <h3>{{item.resourceName || codeToName('resourceType', item.sourceId)}}</h3>
              <span>{{planDataCount(item)}}个计划</span>
            </div>
            <article v-for="(childItem, childIndex) in item.planDataList" :key="childItem.id || childIndex" class="item-details">
              <div class="item-details-1">
                <div>
                  <strong>{{childItem.startDate}} 至 {{childItem.endDate}}</strong>
                  <p :class="['status-badge', statusClass(childItem.planStatus)]">{{codeToName('planStatus', childItem.planStatus)}}</p>
                </div>
                <div>
                  <el-button @click="editPlan(childItem)" icon="el-icon-edit" plain size="small">编辑</el-button>
                </div>
              </div>
              <div class="item-details-other">
                <div>
                  <p>申报曲线</p>
                  <button type="button" @click="seeEcharts(childItem)">
                    {{childItem.echartsShow ? '收起曲线' : '查看曲线'}}
                    <i :class="childItem.echartsShow ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" />
                  </button>
                </div>
                <div v-if="childItem.echartsShow">
                  <echartsLine :echartsDataList="childItem.echartsDataList" />
                </div>
              </div>
            </article>
            <div v-if="!item.planDataList || item.planDataList.length === 0" class="empty-resource-plan">
              <span>该资源类型暂无申报计划</span>
              <el-button @click="addPlan" icon="el-icon-plus" plain size="mini">创建计划</el-button>
            </div>
          </section>
        </div>
        <div v-else class="content-default">
          <h3>暂无申报计划</h3>
          <el-button @click="addPlan" icon="el-icon-plus" type="primary" size="small">创建申报计划</el-button>
        </div>
      </div>
    </div>
    <planInfo ref="ref_planInfo" @updatePlan="updatePlan" />
    <predictDataReport ref="ref_predictDataReport" />
  </div>
</template>
<script type="text/javascript">
import planInfo from './planInfo'
import echartsLine from './echartsLine'
import predictDataReport from "./predictDataReport"
import {
  getDayWeather,
  getResourceTypeList,
  getPlanList,
  getPlanDetail,
} from '../../api'
export default {
  components: {
    planInfo,
    echartsLine,
    predictDataReport,
  },
  props: {
    activeObj: {
      type: Object,
      require: true,
    },
    applyPriceStatus: {
      type: String,
      require: true,
    },
  },
  data() {
    return {
      today: '',
      // 天气数据
      weatherData: 'noData',
      // 资源类型列表
      resourceTypeList: [],
      // 计划状态列表
      planStatusList: [{
        code: '0',
        name: '已过期'
      }, {
        code: '1',
        name: '待开始'
      }, {
        code: '2',
        name: '执行中'
      }],
      // 计划列表
      planId: '',
      planList: [],
    }
  },
  computed: {
    planTotal() {
      return this.resourcePlanList.reduce((total, item) => total + this.planDataCount(item), 0)
    },
    activeResourceName() {
      return this.planId ? this.codeToName('resourceType', this.planId) : ''
    },
    resourcePlanList() {
      const planMap = {}
      this.planList.forEach(item => {
        if (item && item.sourceId) {
          planMap[item.sourceId] = item
        }
      })
      const resourceList = this.resourceTypeList.map(item => {
        const plan = planMap[item.id] || {}
        return {
          ...item,
          sourceId: item.id,
          resourceName: item.name,
          planDataList: plan.planDataList || [],
        }
      })
      this.planList.forEach(item => {
        if (item && item.sourceId && !this.resourceTypeList.some(resource => resource.id === item.sourceId)) {
          resourceList.push({
            ...item,
            resourceName: this.codeToName('resourceType', item.sourceId),
            planDataList: item.planDataList || [],
          })
        }
      })
      return resourceList
    },
  },
  created() {
    this.today = moment().format("YYYY-MM-DD")
    this.queryDayWeather()
    this.queryResourceTypeList()
  },
  mounted() {},
  methods: {
    goBack() {
      this.$emit("goBack")
    },
    // 获取天气数据
    queryDayWeather() {
      const params = {
        stationId: sessionStorage.getItem("systemCode"),
        startTime: moment().format("YYYY-MM-DD 00:00:00"),
        endTime: moment().format("YYYY-MM-DD 24:00:00"),
      }
      getDayWeather(params).then(res => {
        if (res.data.code === 200) {
          if (res.data.data && res.data.data.length > 0) {
            try {
              this.weatherData = JSON.parse(JSON.stringify(res.data.data[0].result[0]))
            } catch (err) {
              this.weatherData = 'noData'
            }
          } else {
            this.weatherData = 'noData'
          }
        } else {
          this.weatherData = 'noData'
          this.$message.error(res.data.msg)
        }
      })
    },
    // 获取资源类型列表
    queryResourceTypeList() {
      const params = {
        aggregatorId: sessionStorage.getItem("entId"),
      }
      getResourceTypeList(params).then(res => {
        if (res.data.code === 200) {
          try {
            if (res.data.data && res.data.data.length > 0) {
              this.resourceTypeList = JSON.parse(JSON.stringify(res.data.data))
            } else {
              this.resourceTypeList = []
            }
            this.setDefaultPlanId()
            this.queryPlanList()
          } catch (err) {
            this.resourceTypeList = []
          }
        } else {
          this.$message.error(res.data.msg)
        }
      })
    },
    // 获取计划列表
    queryPlanList() {
      const params = {
        aggregatorId: sessionStorage.getItem("entId"),
      }
      getPlanList(params).then(res => {
        if (res.data.code === 200) {
          try {
            if (res.data.data.list && res.data.data.list.length > 0) {
              const responseData = JSON.parse(JSON.stringify(res.data.data.list))
              responseData.forEach(item => {
                item.planDataList = item.planDataList || []
                item.planDataList.forEach(child => {
                  this.$set(child, 'echartsShow', false)
                  this.$set(child, 'echartsDataList', [])
                })
              })
              this.planList = JSON.parse(JSON.stringify(responseData))
            } else {
              this.planList = []
            }
            this.setDefaultPlanId()
          } catch (err) {
            this.planList = []
            this.setDefaultPlanId()
          }
        } else {
          this.$message.error(res.data.msg)
        }
      })
    },
    // 查看申报曲线
    seeEcharts(data) {
      if (data.echartsShow) {
        this.$set(data, 'echartsShow', !data.echartsShow)
      } else {
        if (!(data.echartsDataList && data.echartsDataList.length > 0)) {
          const params = {
            planId: data.id,
          }
          getPlanDetail(params).then(res => {
            if (res.data.code === 200) {
              if (res.data.data.dataList && res.data.data.dataList.length > 0) {
                this.$set(data, 'echartsDataList', JSON.parse(JSON.stringify(res.data.data.dataList)))
                this.$set(data, 'echartsShow', !data.echartsShow)
              } else {
                this.$message.warning('该曲线暂无数据')
              }
            } else {
              this.$message.error(res.data.msg)
            }
          })
        } else {
          this.$set(data, 'echartsShow', !data.echartsShow)
        }
      }
    },
    // 预测数据上报
    reportData() {
      this.$refs.ref_predictDataReport.init();
    },
    addPlan() {
      this.$refs.ref_planInfo.init('ADD', this.resourceTypeList)
    },
    editPlan(data) {
      this.$refs.ref_planInfo.init('EDIT', this.resourceTypeList, data)
    },
    planChange(data) {
      this.planId = data.sourceId
      const refs = this.$refs[`ref_details${this.planId}`]
      if (refs && refs[0]) {
        refs[0].scrollIntoView({ behavior: 'smooth' })
      }
    },
    updatePlan() {
      this.queryResourceTypeList()
    },
    setDefaultPlanId() {
      const currentResource = this.resourcePlanList.find(item => item.sourceId === this.planId)
      if (currentResource) {
        return
      }
      this.planId = this.resourcePlanList.length > 0 ? this.resourcePlanList[0].sourceId : ''
    },
    planDataCount(item) {
      return item && item.planDataList ? item.planDataList.length : 0
    },
    statusClass(status) {
      return {
        0: 'expired',
        1: 'pending',
        2: 'running',
      }[String(status)] || 'pending'
    },
    codeToName(type, code) {
      let name = ''
      if (type === 'resourceType') { // 资源类型
        this.resourceTypeList.map(item => {
          if (item.id === code) {
            name = item.name
          }
        })
      } else if (type === 'planStatus') { // 计划状态
        this.planStatusList.map(item => {
          if (item.code === code) {
            name = item.name
          }
        })
      }
      return name || '--'
    },
  },
}
</script>
<style lang="less" scoped>
.declaration-plan {
  width: 100%;
  height: calc(100vh - 144px);
  background: #f4f5f9;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .page-header {
    min-height: 72px;
    padding: 14px 24px;
    box-sizing: border-box;
    background: #ffffff;
    border-bottom: 1px solid #e3e8ee;
    display: grid;
    grid-template-columns: auto minmax(0, 1fr) auto;
    gap: 18px;
    align-items: center;
  }

  .back-button {
    height: 32px;
    padding: 0 10px;
    border: 1px solid #d8e0e8;
    border-radius: 6px;
    background: #ffffff;
    color: #456577;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }

  .page-title {
    min-width: 0;
    h1 {
      margin: 0;
      color: #0e2638;
      font-size: 22px;
      line-height: 28px;
      font-weight: 600;
    }
    p {
      margin: 4px 0 0;
      color: #607d8f;
      font-size: 13px;
      line-height: 18px;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .main-content {
    flex: 1;
    min-height: 0;
    overflow: hidden;
    padding: 20px 24px;
    display: grid;
    grid-template-columns: 280px minmax(0, 1fr);
    gap: 20px;
  }

  .overview-info {
    min-width: 0;
    min-height: 0;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .weather,
  .tab,
  .content-toolbar,
  .item-details,
  .content-default {
    background: #ffffff;
    border: 1px solid #e3e8ee;
    border-radius: 8px;
  }

  .weather {
    width: 100%;
    min-height: 132px;
    padding: 16px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    .weather-date {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 10px;
      p,
      span {
        margin: 0;
      }
      p {
        color: #0e2638;
        font-size: 16px;
        font-weight: 600;
      }
      span {
        color: #607d8f;
        font-size: 13px;
      }
    }
    .weather-details {
      flex: 1;
      min-height: 0;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-top: 16px;
      .img-part {
        margin-right: 18px;
        width: 54px;
        height: 54px;
        display: flex;
        justify-content: center;
        align-items: center;
        >img {
          max-width: 100%;
          max-height: 100%;
        }
      }
      .info-part {
        min-width: 0;
        flex: 1;
        >p {
          margin: 0;
          color: #607d8f;
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
          &:nth-child(1) {
            color: #0e2638;
            font-size: 22px;
            line-height: 28px;
            font-weight: 600;
          }
          &:nth-child(2) {
            font-size: 14px;
            line-height: 20px;
            margin: 10px 0 6px;
          }
          &:nth-child(3) {
            font-size: 13px;
            line-height: 18px;
          }
        }
      }
      .default-part {
        color: #607d8f;
      }
    }
  }

  .tab {
    flex: 1;
    min-height: 0;
    padding: 16px 0;
    box-sizing: border-box;
    overflow-y: auto;
    .tab-title {
      padding: 0 16px 12px;
      color: #0e2638;
      font-size: 16px;
      line-height: 22px;
      font-weight: 600;
      border-bottom: 1px solid #edf1f5;
    }
    >p {
      min-height: 44px;
      margin: 0;
      padding: 0 16px;
      box-sizing: border-box;
      border-left: 3px solid transparent;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 10px;
      >span {
        min-width: 0;
        color: #456577;
        font-size: 14px;
        cursor: pointer;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
      }
      >em {
        min-width: 28px;
        height: 20px;
        line-height: 20px;
        border-radius: 10px;
        background: #edf3f8;
        color: #607d8f;
        font-style: normal;
        font-size: 12px;
        text-align: center;
      }
    }
    .active {
      border-color: #0780ed;
      background: #f2f8fe;
      >span {
        color: #0e2638;
        font-weight: 600;
      }
      >em {
        background: #0780ed;
        color: #ffffff;
      }
    }
  }

  .content-part {
    min-width: 0;
    min-height: 0;
    display: flex;
    flex-direction: column;
  }

  .content-toolbar {
    min-height: 72px;
    padding: 14px 18px;
    box-sizing: border-box;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    h2 {
      margin: 0;
      color: #0e2638;
      font-size: 18px;
      line-height: 24px;
      font-weight: 600;
    }
    p {
      margin: 4px 0 0;
      color: #607d8f;
      font-size: 13px;
      line-height: 18px;
    }
  }

  .summary-boxes {
    display: flex;
    gap: 10px;
    >div {
      width: 96px;
      min-height: 48px;
      padding: 6px 10px;
      box-sizing: border-box;
      border: 1px solid #e3e8ee;
      border-radius: 6px;
      background: #f8fafc;
      span,
      strong {
        display: block;
      }
      span {
        color: #607d8f;
        font-size: 12px;
        line-height: 16px;
      }
      strong {
        margin-top: 4px;
        color: #0e2638;
        font-size: 18px;
        line-height: 22px;
      }
    }
  }

  .content-list {
    flex: 1;
    min-height: 0;
    margin-top: 16px;
    overflow-y: auto;
    .content-item {
      margin-top: 18px;
      &:nth-child(1) {
        margin-top: 0;
      }
    }
    .resource-heading {
      height: 32px;
      display: flex;
      align-items: center;
      gap: 10px;
      h3 {
        margin: 0;
        color: #0e2638;
        font-size: 17px;
        line-height: 24px;
        font-weight: 600;
      }
      span {
        color: #607d8f;
        font-size: 13px;
      }
    }
    .empty-resource-plan {
      min-height: 72px;
      margin-top: 10px;
      padding: 14px 16px;
      box-sizing: border-box;
      border: 1px dashed #d8e0e8;
      border-radius: 6px;
      background: #f8fafc;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      span {
        color: #607d8f;
        font-size: 13px;
        line-height: 18px;
      }
    }
  }

  .item-details {
    margin-top: 10px;
    padding: 16px;
    .item-details-1 {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 12px;
      >div {
        min-width: 0;
        &:nth-child(1) {
          display: flex;
          align-items: center;
          gap: 10px;
        }
      }
      strong {
        color: #0e2638;
        font-size: 16px;
        line-height: 22px;
        font-weight: 600;
      }
    }
    .status-badge {
      min-width: 56px;
      margin: 0;
      padding: 4px 9px;
      border-radius: 6px;
      font-size: 12px;
      line-height: 16px;
      text-align: center;
      &.expired {
        color: #8a5a00;
        background: #fff3d9;
      }
      &.pending {
        color: #456577;
        background: #edf3f8;
      }
      &.running {
        color: #0b7a3b;
        background: #e7f7ee;
      }
    }
    .item-details-other {
      margin-top: 14px;
      padding-top: 14px;
      border-top: 1px solid #edf1f5;
      >div {
        &:nth-child(1) {
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 12px;
          >p {
            margin: 0;
            color: #0e2638;
            font-size: 14px;
            line-height: 20px;
            font-weight: 600;
          }
          button {
            border: none;
            background: transparent;
            color: #0780ed;
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            gap: 4px;
          }
        }
        &:nth-child(2) {
          margin-top: 14px;
          height: 240px;
        }
      }
    }
  }

  .content-default {
    flex: 1;
    min-height: 260px;
    margin-top: 16px;
    border-style: dashed;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    gap: 14px;
    h3 {
      margin: 0;
      color: #0e2638;
      font-size: 18px;
      line-height: 24px;
      font-weight: 600;
    }
  }
}

@media (max-width: 1180px) {
  .declaration-plan {
    height: auto;
    min-height: calc(100vh - 144px);
    overflow: auto;
    .page-header {
      grid-template-columns: 1fr;
      align-items: flex-start;
    }
    .header-actions {
      flex-wrap: wrap;
    }
    .main-content {
      grid-template-columns: 1fr;
      overflow: visible;
    }
    .tab {
      max-height: 260px;
    }
    .content-toolbar {
      align-items: flex-start;
      flex-direction: column;
    }
  }
}
</style>
