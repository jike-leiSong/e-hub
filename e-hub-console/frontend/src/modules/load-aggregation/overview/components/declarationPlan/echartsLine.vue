<template>
  <div ref="ref_echartsLine" class="echarts-line"></div>
</template>
<script type="text/javascript">
import * as echarts from 'echarts'
export default {
  props: {
    echartsDataList: {
      type: Array,
    },
  },
  data() {
    return {}
  },
  created() {},
  mounted() {
    this.echartsInit()
  },
  methods: {
    echartsInit() {
      let e = echarts.init(this.$refs.ref_echartsLine)
      let option = {
        color: ['#45BA80', '#0780ED', '#FFB449'],
        /*tooltip: {
          trigger: 'axis',
          padding: [8, 12],
          backgroundColor: '#FFFFFF',
          extraCssText: 'color: #343A40; font-size: 12px; box-shadow: 0px 4px 6px 0px rgba(28, 119, 201, 0.21);'
        },*/
        tooltip: {
          trigger: 'axis',
          formatter: params => {
            return `
              <div>
                <p style="font-size: 12px;color: #ADB5BD;">${params[0].name}</p>
                <div style="display: flex;align-items: center;">
                  <p style="width: 8px;height: 8px;background: #45BA80;border-radius: 8px;margin-right: 8px;"></p>
                  <p style="font-size: 12px;color: #343A40;">申报功率 ${params[0].value}</p>
                </div>
                <div style="display: flex;align-items: center;">
                  <p style="width: 8px;height: 8px;background: #0780ED;border-radius: 8px;margin-right: 8px;"></p>
                  <p style="font-size: 12px;color: #343A40;">参考日功率 ${params[1].value}</p>
                </div>
                <div style="display: flex;align-items: center;">
                  <p style="width: 8px;height: 8px;background: #FFB449;border-radius: 8px;margin-right: 8px;"></p>
                  <p style="font-size: 12px;color: #343A40;">申报价格 ${params[2].value} 元</p>
                </div>
              </div>
            `
          },
          padding: [8, 12],
          backgroundColor: '#FFFFFF',
          extraCssText: 'color: #343A40; font-size: 12px; box-shadow: 0px 4px 6px 0px rgba(28, 119, 201, 0.21);'
        },
        grid: {
          top: '50',
          bottom: '10',
          left: '30',
          right: '30',
          containLabel: true,
        },
        legend: {
          data: ['申报功率', '参考日功率', '申报价格'],
        },
        xAxis: [{
          type: 'category',
          boundaryGap: true, // 留白
          data: this.echartsDataList.map(item => item.dateTime),
          axisLine: { // 坐标轴轴线相关设置
            lineStyle: {
              color: '#E4E4E4',
            },
          },
          axisTick: { // 坐标轴刻度相关设置
            show: false,
          },
          axisLabel: { // 坐标轴刻度标签的相关设置
            margin: 10,
            textStyle: {
              color: '#999999',
            }
          },
        }],
        yAxis: [{
          type: 'value',
          name: 'kW',
          nameTextStyle: {
            color: '#999999',
            padding: [0, 38, 0, 0],
          },
          nameGap: 30, // 坐标轴名称与轴线之间的距离
          scale: true, // 刻度不会强制包含零
          axisLine: { // 坐标轴轴线相关设置
            lineStyle: {
              color: '#E4E4E4',
            },
          },
          axisTick: { // 坐标轴刻度相关设置
            show: false
          },
          axisLabel: { // 坐标轴刻度标签的相关设置
            margin: 10,
            textStyle: {
              color: '#999999',
            }
          },
        }],
        series: [{
          name: '申报功率',
          type: 'line',
          data: this.echartsDataList.map(item => item.applyPower || item.applyPower === 0 ? item.applyPower : ''),
          symbol: 'none',
        }, {
          name: '参考日功率',
          type: 'line',
          data: this.echartsDataList.map(item => item.referDatePower || item.referDatePower === 0 ? item.referDatePower : ''),
          symbol: 'none',
        }, {
          name: '申报价格',
          type: 'line',
          data: this.echartsDataList.map(item => item.applyPrice || item.applyPrice === 0 ? item.applyPrice : ''),
          symbol: 'none',
        }],
      }
      e.setOption(option)
      window.addEventListener("resize", () => {
        e.resize()
      })
    },
  },
  watch: {
    echartsDataList() {
      this.echartsInit()
    }
  },
}
</script>
<style lang="less" scoped>
.echarts-line {
  width: 100%;
  height: 100%;
  background: #FFFFFF;
}
</style>
<style lang="less">
.echarts-line {
  >div {
    &:nth-child(1) {
      width: 100% !important;
      height: 100% !important;
      >canvas {
        width: 100% !important;
        height: 100% !important;
      }
    }
  }
}
</style>