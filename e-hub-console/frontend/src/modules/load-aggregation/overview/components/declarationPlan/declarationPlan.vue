<template>
  <div class="declaration-plan">
    <div class="go-back" @click="goBack">
      <p>
        <i class="el-icon-arrow-left" />
        <span>调峰辅助服务度电价格申报</span>
      </p>
    </div>
    <div class="main-content">
      <div class="overview-info">
        <div class="weather">
          <div class="weather-date">
            <p>{{today}}</p>
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
          <p v-for="item in planList" :class="{'active': planId === item.sourceId}">
            <span @click="planChange(item)">{{codeToName('resourceType', item.sourceId)}}</span>
          </p>
        </div>
      </div>
      <div class="content-part">
        <div class="operation">
          <el-button @click="reportData" type="primary" size="small">预测数据上报</el-button>
          <el-button @click="addPlan" type="primary" size="small">创建申报计划</el-button>
        </div>
        <div v-if="planList.length > 0" class="content-list">
          <div v-for="(item, index) in planList" :ref="`ref_details${item.sourceId}`" class="content-item">
            <p class="title">{{codeToName('resourceType', item.sourceId)}}</p>
            <div v-for="(childItem, childIndex) in item.planDataList" class="item-details">
              <div class="item-details-1">
                <div>
                  <p>申报周期（{{childItem.startDate}} - {{childItem.endDate}}）</p>
                  <p>{{codeToName('planStatus', childItem.planStatus)}}</p>
                </div>
                <div>
                  <el-button @click="editPlan(childItem)" plain size="small">编辑申报计划</el-button>
                </div>
              </div>
              <div class="item-details-other">
                <div>
                  <p>申报曲线</p>
                  <p @click="seeEcharts(childItem, index, childIndex)">（点击查看申报曲线）</p>
                </div>
                <div v-if="childItem.echartsShow">
                  <echartsLine :echartsDataList="childItem.echartsDataList" />
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="content-default">
          <p>暂无申报计划</p>
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
      getDayWeather(params, this.simulate).then(res => {
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
      getPlanList(params, this.simulate).then(res => {
        if (res.data.code === 200) {
          try {
            if (res.data.data.list && res.data.data.list.length > 0) {
              const responseData = JSON.parse(JSON.stringify(res.data.data.list))
              responseData.map(item => {
                item.planDataList.map(child => {
                  this.$set(child, 'echartsShow', false)
                  this.$set(child, 'echartsDataList', [])
                })
              })
              this.planList = JSON.parse(JSON.stringify(responseData))
              this.planId = this.planList[0].sourceId
            } else {
              this.planList = []
              this.planId = ''
            }
          } catch (err) {
            this.planList = []
            this.planId = ''
          }
        } else {
          this.$message.error(res.data.msg)
        }
      })
    },
    // 查看申报曲线
    seeEcharts(data, index, childIndex) {
      if (this.planList[index].planDataList[childIndex].echartsShow) {
        this.$set(this.planList[index].planDataList[childIndex], 'echartsShow', !this.planList[index].planDataList[childIndex].echartsShow)
      } else {
        if (!(this.planList[index].planDataList[childIndex].echartsDataList && this.planList[index].planDataList[childIndex].echartsDataList.length > 0)) {
          const params = {
            planId: data.id,
          }
          getPlanDetail(params, this.simulate).then(res => {
            if (res.data.code === 200) {
              if (res.data.data.dataList && res.data.data.dataList.length > 0) {
                this.$set(this.planList[index].planDataList[childIndex], 'echartsDataList', JSON.parse(JSON.stringify(res.data.data.dataList)))
                this.$set(this.planList[index].planDataList[childIndex], 'echartsShow', !this.planList[index].planDataList[childIndex].echartsShow)
              } else {
                this.$message.warning('该曲线暂无数据')
              }
            } else {
              this.$message.error(res.data.msg)
            }
          })
        } else {
          this.$set(this.planList[index].planDataList[childIndex], 'echartsShow', !this.planList[index].planDataList[childIndex].echartsShow)
        }
      }
    },
    // 预测数据上报
    reportData() {
      this.$refs.ref_predictDataReport.init();
    },
    addPlan() {
      this.$refs.ref_planInfo.init('ADD', this.resourceTypeList, this.simulate)
    },
    editPlan(data) {
      this.$refs.ref_planInfo.init('EDIT', this.resourceTypeList, this.simulate, data)
    },
    planChange(data) {
      this.planId = data.sourceId
      this.$refs[`ref_details${this.planId}`][0].scrollIntoView({ behavior: 'smooth' })
    },
    updatePlan() {
      this.queryResourceTypeList()
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
  background: #F4F5F9;
  display: flex;
  flex-direction: column;
  overflow-y: hidden;
  .go-back {
    padding: 20px;
    background: #FFFFFF;
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
  .main-content {
    flex: 1;
    overflow-y: hidden;
    margin: 20px 60px;
    display: flex;
    .overview-info {
      width: 280px;
      margin-right: 20px;
      display: flex;
      flex-direction: column;
      align-items: center;
      .weather {
        width: 100%;
        height: 140px;
        padding: 20px 0;
        background: #FFFFFF;
        border-radius: 12px;
        display: flex;
        flex-direction: column;
        .weather-date {
          padding: 0 20px;
          font-size: 18px;
          height: 20px;
          line-height: 20px;
          >p {
            color: #666666;
          }
        }
        .weather-details {
          flex: 1;
          display: flex;
          justify-content: center;
          align-items: center;
          margin-top: 20px;
          .img-part {
            margin: 8px 20px 0 20px;
            width: 60px;
            height: 60px;
            display: flex;
            justify-content: center;
            align-items: center;
            >img {
              width: auto;
              height: auto;
              max-width: 100%;
              max-height: 100%;
            }
          }
          .info-part {
            width: 130px;
            >p {
              color: #666666;
              overflow: hidden;
              white-space: nowrap;
              text-overflow: ellipsis;
              &:nth-child(1) {
                font-size: 24px;
                line-height: 32px;
                color: #333333;
              }
              &:nth-child(2) {
                font-size: 14px;
                line-height: 20px;
                margin: 16px 0 12px;
              }
              &:nth-child(3) {
                font-size: 14px;
                line-height: 20px;
              }
            }
          }
          .default-part {
            color: #666666;
          }
        }
      }
      .tab {
        width: 90%;
        margin-top: 40px;
        border-left: 1px solid #CCCCCC;
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow-y: auto;
        >p {
          line-height: 20px;
          padding-left: 20px;
          margin-top: 20px;
          border-left: 3px solid transparent;
          &:nth-child(1) {
            margin-top: 0;
          }
          >span {
            color: #999999;
            font-size: 18px;
            font-weight: 600;
            cursor: pointer;
          }
        }
        .active {
          border-color: #0780ED;
          >span {
            color: #333333;
          }
        }
        .disabled {
          >span {
            cursor: not-allowed;
          }
        }
      }
    }
    .content-part {
      flex: 1;
      overflow-y: hidden;
      display: flex;
      flex-direction: column;
      .operation {
        display: flex;
        justify-content: flex-end;
      }
      .content-list {
        margin-top: 20px;
        padding: 0 20px 20px;
        flex: 1;
        overflow-y: auto;
        background: #FFFFFF;
        .content-item {
          position: sticky;
          margin-top: 20px;
          &:nth-child(1) {
            margin-top: 0;
          }
          .title {
            font-size: 18px;
            font-weight: 600;
            color: #333333;
            line-height: 20px;
            padding-top: 20px;
            margin-bottom: 10px;
          }
          .item-details {
            padding: 20px;
            border: 1px solid #BBBBBB;
            border-radius: 4px;
            margin-top: 10px;
            .item-details-1 {
              display: flex;
              justify-content: space-between;
              align-items: center;
              >div {
                &:nth-child(1) {
                  display: flex;
                  align-items: center;
                  >p {
                    &:nth-child(1) {
                      font-size: 16px;
                    }
                    &:nth-child(2) {
                      font-size: 12px;
                      margin-left: 10px;
                      color: #0780ED;
                      background: rgba(7, 128, 237, 0.1);
                      padding: 6px 10px;
                      border-radius: 2px;
                    }
                  }
                }
              }
            }
            .item-details-other {
              margin-top: 20px;
              >div {
                &:nth-child(1) {
                  display: flex;
                  align-items: center;
                  >p {
                    &:nth-child(1) {
                      font-size: 16px;
                    }
                    &:nth-child(2) {
                      color: #999999;
                      font-size: 12px;
                      cursor: pointer;
                    }
                  }
                }
                &:nth-child(2) {
                  margin-top: 20px;
                  height: 200px;
                }
              }
            }
          }
        }
      }
      .content-default {
        margin-top: 20px;
        flex: 1;
        overflow: hidden;
        background: #FFFFFF;
        display: flex;
        justify-content: center;
        align-items: center;
        >p {
          font-size: 16px;
        }
      }
    }
  }
}
</style>