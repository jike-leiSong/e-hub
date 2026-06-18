<template>
  <el-dialog :title="pageType === 'EDIT' ? '编辑申报计划' : '创建申报计划'" :visible.sync="show" class="element_custom-dialog-plan-info" width="1280px" :close-on-click-modal="false" :close-on-press-escape="false" append-to-body :before-close="cancel">
    <div class="main" v-loading="pageLoading">
      <div class="echarts-part">
        <p @click="echartsShow = !echartsShow">{{echartsShow ? '收起' : '展开'}}此图表区域</p>
        <div v-if="echartsShow">
          <echartsLine :echartsDataList="echartsDataList" />
        </div>
      </div>
      <div class="fill-part">
        <div class="declaration-period">
          <div>
            <p>资源类型</p>
            <p>
              <el-select v-model="resourceType" @change="resourceTypeChange" size="small" placeholder="请选择资源类型">
                <el-option v-for="item in resourceTypeList" :key="item.id" :label="item.name" :value="item.id" :disabled="item.display !== 1" />
              </el-select>
            </p>
          </div>
          <div>
            <p>创建申报周期</p>
            <p>
              <el-date-picker v-model="declarationPeriod" size="small" :clearable="false" type="daterange" value-format="yyyy-MM-dd" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" :picker-options="declarationDateOptions">
              </el-date-picker>
            </p>
            <p class="date-tip"> 请注意：当日上午8：55后对明日计划的创建、修改无效 </p>
          </div>
        </div>
        <div class="info-operation">
          <div class="condition">
            <div>
              <p>设置参考日</p>
              <p>
                <el-date-picker v-model="referenceDay" @change="referenceDayChange" size="small" :clearable="false" type="date" value-format="yyyy-MM-dd" placeholder="请选择" :picker-options="referenceDateOptions" />
              </p>
            </div>
            <div>
              <p>时段选择</p>
              <p>
                <el-time-select v-model="timeSlot[0]" :disabled="!(resourceType && referenceDay)" :picker-options="{ start: '00:15', end: timeSlot[1] || '24:00', step: '00:15' }" :clearable="false" size="small" placeholder="开始时间" />
                <span>~</span>
                <el-time-select v-model="timeSlot[1]" :disabled="!(resourceType && referenceDay)" :picker-options="{ start: timeSlot[0] || '00:15', end: '24:00', step: '00:15' }" :clearable="false" size="small" placeholder="结束时间" />
              </p>
            </div>
            <div>
              <p>调整列</p>
              <p>
                <el-select v-model="adjustColumn" :disabled="!(resourceType && referenceDay)" size="small" clearable placeholder="请选择">
                  <el-option v-for="item in adjustColumnList" :key="item.code" :label="item.name" :value="item.code" />
                </el-select>
              </p>
            </div>
            <div>
              <p>计算符号</p>
              <p>
                <el-select v-model="calculationSymbol" :disabled="!(resourceType && referenceDay)" size="small" clearable placeholder="请选择">
                  <el-option v-for="item in calculationSymbolList" :key="item.code" :label="item.name" :value="item.code" />
                </el-select>
              </p>
            </div>
            <div>
              <p>值</p>
              <p>
                <el-input v-model="calculationValue" :disabled="!(resourceType && referenceDay)" type="number" size="small" clearable placeholder="请输入" />
              </p>
            </div>
          </div>
          <div class="operate">
            <el-button @click="calculateTable" size="small" plain>计算填表</el-button>
            <el-button @click="refreshEcharts" size="small" plain>刷新图表</el-button>
          </div>
        </div>
      </div>
      <div class="table-part">
        <el-table :data="tableData" stripe border height="100%" tooltip-effect="dark">
          <el-table-column label="时段" prop="dateTime" align="left" :min-width="60" :show-overflow-tooltip="true" />
          <el-table-column label="参考日功率" prop="referDatePower" align="left" :min-width="120" :show-overflow-tooltip="true" />
          <el-table-column label="调整系数" prop="adjustFactor" align="left" :min-width="120" :show-overflow-tooltip="true" />
          <el-table-column :min-width="120" :show-overflow-tooltip="true">
            <template slot="header" slot-scope="scope">
              <div class="custom-header-1">
                <el-tooltip content="参考日功率 x 调整系数" placement="top-start">
                  <p>调整值</p>
                </el-tooltip>
                <el-tooltip content="将本栏数值导入申报功率栏" placement="top-start">
                  <i @click="tableImportData('adjustValue')" class="el-icon-upload2" />
                </el-tooltip>
              </div>
            </template>
            <template slot-scope="scope">
              <p>{{scope.row.adjustValue}}</p>
            </template>
          </el-table-column>
          <el-table-column label="申报功率" prop="applyPower" align="left" :min-width="180" :show-overflow-tooltip="true">
            <template slot-scope="scope">
              <el-input v-model="scope.row.applyPower" @change="tableInputChange(scope.row.applyPower, scope.$index, 'applyPower')" type="number" clearable size="mini" placeholder="请输入申报功率" />
            </template>
          </el-table-column>
          <el-table-column label="申报价格" prop="applyPrice" align="left" :min-width="180" :show-overflow-tooltip="true">
            <template slot-scope="scope">
              <el-input v-model="scope.row.applyPrice" @change="tableInputChange(scope.row.applyPrice, scope.$index, 'applyPrice')" type="number" clearable size="mini" placeholder="请输入申报价格" />
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <div slot="footer">
      <el-button @click="reset" plain size="small">重置</el-button>
      <el-button @click="confirm" :loading="buttonLoading" type="primary" size="small">{{pageType === 'EDIT' ? '保存计划' : '创建计划'}}</el-button>
    </div>
  </el-dialog>
</template>
<script type="text/javascript">
import echartsLine from './echartsLine'
import {
  queryReferenceDailyPower,
  addEditPlan,
  getPlanDetail,
} from '../../api'
// 加法运算
function addition(num1, num2) {
  let r1, r2, m;
  try {
    r1 = num1.toString().split(".")[1].length;
  } catch (e) {
    r1 = 0;
  }
  try {
    r2 = num2.toString().split(".")[1].length;
  } catch (e) {
    r2 = 0;
  }
  m = Math.pow(10, Math.max(r1, r2));
  return (num1 * m + num2 * m) / m;
}
// 减法运算
function subtract(num1, num2) {
  let r1, r2, m, n;
  try {
    r1 = num1.toString().split(".")[1].length;
  } catch (e) {
    r1 = 0;
  }
  try {
    r2 = num2.toString().split(".")[1].length;
  } catch (e) {
    r2 = 0;
  }
  m = Math.pow(10, Math.max(r1, r2));
  n = (r1 >= r2) ? r1 : r2;
  return ((num1 * m - num2 * m) / m).toFixed(n);
}
// 乘法运算
function multiply(num1, num2) {
  let m = 0,
    s1 = num1.toString(),
    s2 = num2.toString();
  try {
    m += s1.split(".")[1].length;
  } catch (e) {}
  try {
    m += s2.split(".")[1].length;
  } catch (e) {}
  return Number(s1.replace(".", "")) * Number(s2.replace(".", "")) / Math.pow(10, m);
}
// 除法运算
function divided(num1, num2) {
  let t1 = 0,
    t2 = 0,
    r1, r2;
  try {
    t1 = num1.toString().split(".")[1].length;
  } catch (e) {}
  try {
    t2 = num2.toString().split(".")[1].length;
  } catch (e) {}
  r1 = Number(num1.toString().replace(".", ""));
  r2 = Number(num2.toString().replace(".", ""));
  return (r1 / r2) * Math.pow(10, t2 - t1);
}
export default {
  components: {
    echartsLine,
  },
  data() {
    return {
      show: false,
      // echarts
      echartsShow: true,
      // 页面类型
      pageType: '',
      // 资源类型
      resourceType: '',
      resourceTypeList: [],
      // 请求头参数
      simulate: '',
      // 计划详情
      planData: null,
      // echarts
      echartsDataList: [],
      // 申报周期
      declarationPeriod: [],
      declarationDateSelect: '',
      declarationDateOptions: {
        onPick: ({ maxDate, minDate }) => {
          this.declarationDateSelect = minDate.getTime()
          maxDate ? this.declarationDateSelect = '' : null
        },
        disabledDate: date => {
          // 从明日开始选择，至多180天
          if (date.getTime() < Date.now()) {
            return true
          } else {
            if (this.declarationDateSelect) {
              const limitDays = 180 * 86400000
              const minDate = this.declarationDateSelect - limitDays
              const maxDate = this.declarationDateSelect + limitDays
              return date.getTime() < minDate || date.getTime() > maxDate
            } else {
              return false
            }
          }
        }
      },
      // 参考日
      referenceDay: '',
      referenceDateOptions: {
        disabledDate: date => {
          return date.getTime() > Date.now() - 86400000
        }
      },
      // 时段选择
      timeSlot: [],
      // 调整列
      adjustColumn: '',
      adjustColumnList: [],
      // 计算符号
      calculationSymbol: '',
      calculationSymbolList: [],
      // 计算值
      calculationValue: '',
      // table
      tableData: [],
      // pageLoading
      pageLoading: false,
      // buttonLoading
      buttonLoading: false,
    }
  },
  created() {},
  mounted() {},
  methods: {
    init(pageType, resourceTypeList, simulate, planData) {
      this.pageType = pageType
      this.resourceType = ''
      this.resourceTypeList = JSON.parse(JSON.stringify(resourceTypeList))
      this.simulate = simulate
      this.planData = planData ? JSON.parse(JSON.stringify(planData)) : null
      this.echartsDataList = []
      this.declarationPeriod = []
      this.declarationDateSelect = ''
      this.referenceDay = this.pageType === 'EDIT' ? '' : moment().subtract(1, 'days').format("YYYY-MM-DD")
      this.timeSlot = ['00:15', '24:00']
      this.adjustColumn = ''
      this.adjustColumnList = [ // 
        { name: '调整系数', code: 'adjustFactor' }, // 
        { name: '申报功率', code: 'applyPower' }, //
        { name: '申报价格', code: 'applyPrice' }, //
      ]
      this.calculationSymbol = ''
      this.calculationSymbolList = [ // 
        { name: '+', code: '+' }, // 
        { name: '-', code: '-' }, // 
        { name: '×', code: '×' }, //
        { name: '=', code: '=' }, //
      ]
      this.calculationValue = ''
      this.tableData = []
      this.pageLoading = false
      this.buttonLoading = false
      this.pageType === 'EDIT' ? this.queryPlanDetail() : null
      this.show = true
    },
    // 获取计划详情
    queryPlanDetail() {
      const params = {
        planId: this.planData.id,
      }
      getPlanDetail(params, this.simulate).then(res => {
        if (res.data.code === 200) {
          const responseData = JSON.parse(JSON.stringify(res.data.data))
          this.resourceType = responseData.sourceId
          this.declarationPeriod = [responseData.startDate, responseData.endDate]
          this.referenceDay = responseData.referDate
          this.tableData = JSON.parse(JSON.stringify(responseData.dataList))
          this.$nextTick(() => {
            this.refreshEcharts()
          })
        } else {
          this.$message.error(res.data.msg)
        }
      })
    },
    // 资源类型change
    resourceTypeChange() {
      if (this.resourceType && this.referenceDay) {
        this.timeSlot = ['00:15', '24:00']
        this.adjustColumn = ''
        this.calculationSymbol = ''
        this.calculationValue = ''
        this.getTable()
      }
    },
    // 参考日change
    referenceDayChange() {
      if (this.resourceType && this.referenceDay) {
        this.timeSlot = ['00:15', '24:00']
        this.adjustColumn = ''
        this.calculationSymbol = ''
        this.calculationValue = ''
        this.getTable()
      }
    },
    // 获取table数据（参考日功率）
    getTable() {
      this.tableData = []
      const params = {
        aggregatorId: sessionStorage.getItem("entId"),
        sourceId: this.resourceType,
        referDate: this.referenceDay,
      }
      this.pageLoading = true
      queryReferenceDailyPower(params, this.simulate).then(res => {
        this.pageLoading = false
        if (res.data.code === 200) {
          try {
            if (res.data.data.list && res.data.data.list.length > 0) {
              const responseList = JSON.parse(JSON.stringify(res.data.data.list))
              let dataList = []
              responseList.map(item => {
                dataList.push({
                  dateTime: item.date,
                  referDatePower: Number(item.value || 0),
                  adjustFactor: 1,
                  adjustValue: Number(item.value || 0),
                  applyPower: 0,
                  applyPrice: 0,
                })
              })
              this.tableData = JSON.parse(JSON.stringify(dataList))
              this.$nextTick(() => {
                this.refreshEcharts()
              })
            } else {
              this.tableData = []
              this.$nextTick(() => {
                this.refreshEcharts()
              })
            }
          } catch (err) {
            this.tableData = []
            this.$nextTick(() => {
              this.refreshEcharts()
            })
          }
        } else {
          this.$message.error(res.data.msg)
        }
      })
    },
    // 刷新图表
    refreshEcharts() {
      this.echartsDataList = []
      let dataList = []
      this.tableData.map(item => {
        dataList.push({
          dateTime: item.dateTime,
          applyPower: item.applyPower,
          referDatePower: item.referDatePower,
          applyPrice: item.applyPrice,
        })
      })
      this.echartsDataList = JSON.parse(JSON.stringify(dataList))
    },
    // 计算填表
    calculateTable() {
      if (this.timeSlot && this.timeSlot[0] && this.timeSlot[1]) {
        if (!(this.tableData && this.tableData.length > 0)) {
          this.$message.warning('请更换“资源类型”或“设置参考日”，当前条件下暂无数据')
          return false
        }
        if (!this.adjustColumn) {
          this.$message.warning('请选择“调整列”')
          return false
        }
        if (!this.calculationSymbol) {
          this.$message.warning('请选择“计算符号”')
          return false
        }
        if (!(this.calculationValue || this.calculationValue === 0)) {
          this.$message.warning('请填写“值”')
          return false
        }
        this.tableData.map(item => {
          const itemTimeNumber = Number(item.dateTime.replace(':', ''))
          const slotTimeNumber1 = Number(this.timeSlot[0].replace(':', ''))
          const slotTimeNumber2 = Number(this.timeSlot[1].replace(':', ''))
          if (slotTimeNumber1 <= itemTimeNumber && itemTimeNumber <= slotTimeNumber2) {
            if (this.calculationSymbol === '=') {
              this.$set(item, this.adjustColumn, Number(this.calculationValue))
              if (this.adjustColumn === 'adjustFactor') { // 调整系数：[调整系数]修改时[调整值]自动修改
                this.$set(item, 'adjustValue', multiply(Number(item.referDatePower), Number(item.adjustFactor)))
              }
            } else {
              let number = 0
              if (this.calculationSymbol === '+') {
                number = addition(Number(item[this.adjustColumn]), Number(this.calculationValue))
              } else if (this.calculationSymbol === '-') {
                number = subtract(Number(item[this.adjustColumn]), Number(this.calculationValue))
              } else if (this.calculationSymbol === '×') {
                number = multiply(Number(item[this.adjustColumn]), Number(this.calculationValue))
              }
              this.$set(item, this.adjustColumn, Number(number))
              if (this.adjustColumn === 'adjustFactor') { // 调整系数：[调整系数]修改时[调整值]自动修改
                this.$set(item, 'adjustValue', multiply(Number(item.referDatePower), Number(item.adjustFactor)))
              }
            }
          }
        })
      } else {
        this.$message.warning('请选择“时段”后再进行操作')
      }
    },
    // 表格导入数据
    tableImportData(code) {
      if (code === 'adjustValue') { // 调整值（导入：申报价格）
        this.tableData.map(item => {
          this.$set(item, 'applyPower', Number(item.adjustValue))
        })
      }
    },
    // 表格inputChange
    tableInputChange(number, index, key) {
      number === '' ? this.$set(this.tableData[index], key, 0) : null
    },
    // 取消
    cancel() {
      this.show = false
    },
    // 重置
    reset() {
      this.init(this.pageType, this.resourceTypeList, this.simulate, this.planData)
    },
    // 确认
    confirm() {
      if (!this.resourceType) {
        this.$message.warning('请选择“资源类型”')
        return false
      }
      if (!(this.declarationPeriod && this.declarationPeriod.length > 0)) {
        this.$message.warning('请选择“创建申报周期”')
        return false
      }
      if (!this.referenceDay) {
        this.$message.warning('请选择“设置参考日”')
        return false
      }
      if (!(this.tableData && this.tableData.length > 0)) {
        this.$message.warning('请更换“资源类型”或“设置参考日”，当前条件下暂无数据')
        return false
      }
      let params = {
        aggregatorId: sessionStorage.getItem("entId"),
        sourceId: this.resourceType,
        startDate: this.declarationPeriod[0],
        endDate: this.declarationPeriod[1],
        referDate: this.referenceDay,
        dataList: this.tableData && this.tableData.length > 0 ? JSON.parse(JSON.stringify(this.tableData)) : [],
      }
      if (this.pageType === 'EDIT') {
        params.id = this.planData.id
      }
      this.buttonLoading = true
      addEditPlan(params, this.simulate).then(res => {
        this.buttonLoading = false
        if (res.data.code === 200) {
          this.$message.success(this.pageType === 'EDIT' ? '修改成功' : '创建成功')
          this.$emit('updatePlan')
          this.cancel()
        } else {
          this.$message.error(res.data.msg)
        }
      })
    },
  },
}
</script>
<style lang="less" scoped>
.element_custom-dialog-plan-info {
  display: flex;
  justify-content: center;
  align-items: center;
  ::v-deep .el-dialog {
    margin: 0 !important;
    height: 90vh;
    overflow: hidden;
    background: #FFFFFF;
    box-shadow: 0px 2px 12px 0px rgba(0, 0, 0, 0.1);
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    .el-dialog__header {
      height: 24px;
      line-height: 24px;
      margin: 16px 20px 20px;
      padding: 0;
      background: #FFFFFF;
      display: flex;
      justify-content: space-between;
      align-items: center;
      .el-dialog__title {
        padding: 0;
        font-size: 16px;
        font-family: PingFangSC-Medium, PingFang SC;
        font-weight: 500;
        color: #191919;
        line-height: 24px;
      }
      .el-dialog__headerbtn {
        top: 18px;
        cursor: pointer;
        .el-dialog__close {
          font-size: 16px;
          color: #999999;
        }
      }
    }
    .el-dialog__body {
      flex: 1;
      overflow: hidden;
      padding: 0;
      display: flex;
      flex-direction: column;
      .main {
        flex: 1;
        margin: 0 20px;
        overflow: hidden;
        display: flex;
        flex-direction: column;
        .echarts-part {
          margin-top: 20px;
          >p {
            line-height: 20px;
            color: #0780ED;
            cursor: pointer;
          }
          >div {
            height: 240px;
          }
        }
        .fill-part {
          margin: 20px 0;
          .declaration-period {
            display: flex;
            align-items: center;
            margin-bottom: 10px;
            >div {
              display: flex;
              align-items: center;
              margin-right: 20px;
              >p {
                &:nth-child(1) {
                  margin-right: 4px;
                  font-size: 14px;
                  font-family: PingFangSC-Regular, PingFang SC;
                  font-weight: 400;
                  color: #333333;
                }
              }
              .date-tip {
                color: #999999;
                font-size: 12px;
                margin-left: 10px;
              }
            }
          }
          .info-operation {
            display: flex;
            justify-content: space-between;
            align-items: center;
            .condition {
              flex: 1;
              margin-right: 40px;
              display: flex;
              align-items: center;
              >div {
                display: flex;
                align-items: center;
                margin-left: 20px;
                &:nth-child(1) {
                  margin-left: 0;
                }
                >p {
                  &:nth-child(1) {
                    margin-right: 4px;
                    font-size: 14px;
                    font-family: PingFangSC-Regular, PingFang SC;
                    font-weight: 400;
                    color: #333333;
                  }
                  &:nth-child(2) {
                    .el-date-editor {
                      width: 100%;
                      .el-input__inner {
                        padding-right: 10px;
                      }
                    }
                    .el-select {
                      width: 100%;
                    }
                    .el-input {
                      width: 100%;
                    }
                  }
                }
                &:nth-child(1) {
                  >p {
                    &:nth-child(2) {
                      width: 120px;
                    }
                  }
                }
                &:nth-child(2) {
                  >p {
                    &:nth-child(2) {
                      width: 180px;
                      display: flex;
                      align-items: center;
                      >span {
                        margin: 0 6px;
                      }
                      .el-date-editor {
                        .el-input__inner {
                          padding: 0 12px;
                        }
                        .el-input__prefix {
                          display: none;
                        }
                      }
                    }
                  }
                }
                &:nth-child(3) {
                  >p {
                    &:nth-child(2) {
                      width: 100px;
                    }
                  }
                }
                &:nth-child(4) {
                  >p {
                    &:nth-child(2) {
                      width: 88px;
                    }
                  }
                }
                &:nth-child(5) {
                  >p {
                    &:nth-child(2) {
                      width: 88px;
                    }
                  }
                }
              }
            }
            .operate {
              display: flex;
              align-items: center;
              .el-button {
                padding: 4px 19px;
                line-height: 22px;
                font-size: 14px;
                font-family: PingFangSC-Regular, PingFang SC;
                font-weight: 400;
                color: #0780ED;
                border-color: #0780ED;
              }
            }
          }
        }
        .table-part {
          flex: 1;
          overflow-y: auto;
          .el-table {
            .el-table__header {
              th {
                background: #F0F8FE;
                height: 20px;
                line-height: 20px;
                padding: 10px 0;
              }
            }
            .el-table__body {
              td {
                padding: 8px 0;
              }
            }
          }
          .custom-header-1 {
            display: flex;
            align-items: center;
            i {
              font-size: 18px;
              font-weight: bolder;
              margin-left: 10px;
              transform: rotate(90deg);
              margin-top: 1px;
              cursor: pointer;
              &:hover {
                color: #0780ED;
              }
            }
          }
        }
      }
    }
    .el-dialog__footer {
      padding: 20px;
      display: flex;
      justify-content: flex-end;
      align-items: center;
      .el-button {
        font-size: 14px;
        font-family: PingFangSC-Regular, PingFang SC;
        font-weight: 400;
        line-height: 22px;
        border-radius: 4px;
        border-color: #0780ED;
        padding: 4px 19px;
        margin: 0;
        &:nth-child(1) {
          color: #0780ED;
          margin-right: 16px;
        }
        &:nth-child(2) {
          color: #FFFFFF;
          background: #0780ED;
        }
      }
    }
  }
}
</style>
