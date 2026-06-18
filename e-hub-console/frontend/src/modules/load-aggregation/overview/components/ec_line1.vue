<template>
  <div ref="ec" :style="{ height: height, width: width }"></div>
</template>

<script>
import echarts from "echarts";
import resize from "./resize.js";

export default {
  mixins: [resize],
  props: {
    width: {
      type: String,
      default: "100%",
    },
    min: {
      type: Number,
      default: null,
    },
    max: {
      type: Number,
      default: null,
    },
    height: {
      type: String,
      default: "100%",
    },
    type: {
      type: String,
      default: "line",
    },
    backgroundColor: {
      type: String,
      default: "#fff",
    },
    lineType: {
      type: String,
      default: null,
    },
    barWidth: {
      type: Number,
      default: null,
    },
    markArea: {
      type: Object,
      default: null,
    },
    selected: {
      type: Object,
      default: null,
    },
    zoom: {
      type: Boolean,
      default: false,
    },
    visualMap: {
      type: Number,
      default: 0,
    },
    endValue: {
      type: Number,
      default: 0,
    },
    unit: {
      type: String,
      default: "",
    },
    markLine: {
      type: Object,
      default: null,
    },
    ecdata: {
      type: Array,
      default: () => [],
    },
    timeList: {
      type: Array,
      default: () => [],
    },
    isStack: {
      type: Boolean,
      default: false,
    },
    tooltip_show: {
      type: Boolean,
      default: true,
    },
    unit2: {
      type: String,
      default: "",
    },
    color: {
      type: Array,
      default: () => [
        "#0780ED",
        "#44D428",
        "#B06EF3",
        "#F5A623",
        "#68C4FD",
        "#FF5227",
        "#AE716E",
        "#0050E2",
        "#F6D159",
        "#911FA8",
      ],
    },
    isScreen: {
      type: Boolean,
      default: false,
    },
    refreshId: {
      type: Number,
    },
  },
  data() {
    return {
      chart: null,
      datas: [],
    };
  },
  computed: {
    origin() {
      return this.ecdata;
    },
    abscissa() {
      let list = []
      for (let i = 0; i < this.origin.length; i++) {
        if (this.origin[i] && this.origin[i].value && this.origin[i].value.length > 0) {
          list = this.origin[i].value.map(item => {
             return item.time || item.date || item.dateTime || item.readTime
          })
        }
      }
      return list
    },
    opt() {
      const that = this;
      const opt = {
        backgroundColor: this.backgroundColor,
        color: this.color,
        // 提示框组件
        tooltip: {
          // alwaysShowContent:true,
          // 触发类型
          trigger: "axis",
          // 提示框添加单位  默认无单位
          formatter(p) {
            if (that.tooltip_show) {
              let y_name = p[0].name;
              p.forEach((item, index) => {
                if (index === that.origin.length) {
                  return;
                }
                y_name += `<p style="min-width: 150px;display:flex;justify-content:space-between;align-items:center"><span>${
                  item.marker
                }${item.seriesName}：</span>${
                  item.value === undefined ? "--" : item.value
                } ${item.seriesName === '碳排因子' ? '' : 'kW'} </p>`;
              });
              return y_name;
            }
            that.$emit("tooltip_hover", p);
          },
          // 鼠标悬浮到图上，可以出现标线和刻度文本。
          axisPointer: {
            type: "cross",
            // 是否显示
            show: true,
            // 标线样式
            lineStyle: {
              // 线的类型 可选'solid' 'dashed''dotted'
              type: "dashed",
              // 线颜色
              color: "#000",
            },
            // 坐标轴指示器的文本标签。
            label: {
              // 是否显示
              show: false,
              // 文本颜色
              color: "#fff",
              // 指示器北京颜色
              backgroundColor: "rgba(7, 128, 237, 0.8)",
            },
          },
        },
        // 直角坐标系内绘图网格
        grid: {
          top: 50,
          // grid 组件离容器左侧的距离。默认10%
          left: this.isScreen ? 75 : 55,
          // grid 组件离容器右侧的距离。默认10%
          right: 40,
          // grid 组件离容器下侧的距离。默认60px。值可以是像60这样的具体像素值，可以是像 '20%' 这样相对于容器高宽的百分比。
          bottom: this.zoom ? 80 : 30,
        },
        // 图例组件。
        legend: {
          // selectedMode: 'single',
          type: "scroll",
          show: !!(that.origin[0] && that.origin[0].name),
          // left:80,
          // 图例标记的图形宽度。默认25px
          itemWidth: 6,
          // 图例标记的图形高度。默认14px
          itemHeight: 6,
          // right: 16,
          // 图例的公用文本样式。
          textStyle: {
            // 文字颜色
            color: "#000000",
            // 文字大小
            fontSize: "12",
          },
          selected: that.selected,
          // 图例的数据数组
          data: that.origin
            ? that.origin.map((item, index) => {
                return {
                  name: item.name,
                  icon: "circle",
                  textStyle: { color: item.color ? item.color : that.color },
                };
              })
            : [],
        },
        dataZoom: [
          // 滑动条型数据区域缩放组件
          {
            show: that.zoom,
            type: "slider",
            // 滚动条高度
            height: 40,
            // 滚动条近距离底部距离
            bottom: 10,
            // 数据窗口范围的起始百分比。范围是：0 ~ 100。表示 0% ~ 100%。默认0
            start: 0,
            // 数据窗口范围的结束百分比。范围是：0 ~ 100。默认100
            end: that.endValue ? that.endValue : 100,
            // 手柄的样式配置
            handleStyle: {
              // 手柄颜色 默认#a7b7cc
              color: false,
            },
            // 文字样式
            textStyle: {
              //  文字的颜色 默认#333
              color: false,
            },
          },
          // 内置型数据区域缩放组件
          {
            type: "inside",
          },
        ],
        // 直角坐标系 grid 中的 x 轴，一般情况下单个 grid 组件最多只能放上下两个 x 轴，多于两个 x 轴需要通过配置 offset 属性防止同个位置多个 x 轴的重叠。
        xAxis: [
          {
            // 是否显示
            show: true,
            // 是否是脱离 0 值比例。设置成 true 后坐标刻度不会强制包含零刻度
            scale: true,
            // 坐标轴类型
            type: "category",
            // 坐标轴轴线相关设置
            axisLine: {
              // 坐标轴样式
              lineStyle: {
                // 坐标轴颜色
                color: "#EEEEEE",
              },
            },
            // 坐标轴刻度相关设置
            axisTick: {
              // 是否显示坐标轴刻度
              show: false,
            },

            // 坐标轴两边留白策略，类目轴和非类目轴的设置和表现不一样
            boundaryGap: false,
            // 坐标轴刻度标签的相关设置
            axisLabel: {
              // 坐标轴刻度颜色
              color: "#666666",
            },
            // 类目数据，在类目轴（type: 'category'）中有效
            data: that.timeList,
          },
        ],
        // 直角坐标系 grid 中的 y 轴，一般情况下单个 grid 组件最多只能放左右两个 y 轴，多于两个 y 轴需要通过配置 offset 属性防止同个位置多个 Y 轴的重叠。
        yAxis: [
          {
            // 坐标轴显示
            show: true,
            min: that.min ? that.min : null,
            max: that.max ? that.max : null,
            // 坐标轴名称。
            name: 'kW',
            // 坐标轴名称的文字样式。
            nameTextStyle: {
              color: "#999999",
              padding: [20, 20, 0, 0],
            },
            // 坐标轴类型
            type: "value",
            // 是否是脱离 0 值比例。设置成 true 后坐标刻度不会强制包含零刻度
            scale: true,
            // 坐标轴在 grid 区域中的分隔线
            splitLine: {
              // 是否显示
              show: true,
              lineStyle: {
                color: "#EEEEEE",
                //分隔线的类型 可选'solid' 'dashed''dotted'
                type: "solid",
              },
            },
            axisLabel: {
              color: "#999999",
            },
            // 坐标轴轴线相关设置。
            axisLine: {
              // 是否显示
              show: false,
              lineStyle: {
                // 坐标轴线线的颜色。
                color: "#EEEEEE",
                // 坐标轴线线的宽度
                width: 1,
              },
            },

            // 坐标轴刻度相关设置
            axisTick: {
              // 是否显示坐标轴刻度
              show: false,
            },
          },
          {
            // 坐标轴显示
            show: true,
            min: that.min ? that.min : null,
            max: that.max ? that.max : null,
            // 坐标轴名称。
            name: '',
            // 坐标轴名称的文字样式。
            nameTextStyle: {
              color: "#999999",
              padding: [0, 20, 0, 0],
            },
            // 坐标轴类型
            type: "value",
            // 是否是脱离 0 值比例。设置成 true 后坐标刻度不会强制包含零刻度
            scale: true,
            // 坐标轴在 grid 区域中的分隔线
            splitLine: {
              // 是否显示
              show: true,
              lineStyle: {
                color: "#EEEEEE",
                //分隔线的类型 可选'solid' 'dashed''dotted'
                type: "solid",
              },
            },
            axisLabel: {
              color: "#999999",
            },
            // 坐标轴轴线相关设置。
            axisLine: {
              // 是否显示
              show: false,
              lineStyle: {
                // 坐标轴线线的颜色。
                color: "#EEEEEE",
                // 坐标轴线线的宽度
                width: 1,
              },
            },

            // 坐标轴刻度相关设置
            axisTick: {
              // 是否显示坐标轴刻度
              show: false,
            },
          },
        ],
        visualMap: that.visualMap
          ? {
              top: 10,
              right: 10,
              pieces: [
                {
                  lt: that.visualMap - 0.000001,
                  // color: colorDown
                },
                {
                  gte: that.visualMap,
                  // color: colorUp
                },
              ],
              show: false,
            }
          : null,
        animationThreshold: 720,
        // animation:false,
        series: that.origin
          ? that.origin.map((item, index) => {
              return {
                yAxisIndex: item.yAxisIndex,
                barWidth: that.barWidth ? that.barWidth : null,
                markArea: that.markArea ? that.markArea : null,
                stack: that.isStack ? "总量" : null,
                // 折线/面积图
                type: that.type,
                // 系列名称，用于tooltip的显示，legend 的图例筛选，在 setOption 更新数据和配置项时用于指定对应的系列
                name: item.name,
                // 是否平滑曲线显示
                smooth: true,
                // 是否显示 symbol, 如果 false 则只有在 tooltip hover 的时候显示。
                showSymbol: false,
                lineStyle: {
                  normal: {
                    width: 3,
                    shadowColor: "rgba(0,0,0,0.28)",
                    shadowBlur: 10,
                    shadowOffsetX: 4,
                    shadowOffsetY: 10,
                  },
                },
                markLine: this.markLine,
                // 区域填充样式
                // areaStyle: {
                //   normal: {
                //     color: new echarts.graphic.LinearGradient(
                //       0,
                //       0,
                //       0,
                //       1,
                //       [
                //         {
                //           offset: 0,
                //           color: this.r_color[index].a
                //         },
                //         {
                //           offset: 0.5,
                //           color: this.r_color[index].b
                //         },
                //         {
                //           offset: 1,
                //           color: "rgba(255, 255, 255, 0)"
                //         }
                //       ],
                //       false
                //     ),
                //     shadowColor: "rgba(0, 0, 0, 0.1)",
                //     shadowBlur: 10
                //   }
                // },
                // 系列中的数据内容数组。数组项通常为具体的数据项。
                data:
                  item.value && item.value.length
                    ? item.value.map((item, index) => {
                        if (
                          item.quantity === 0 ||
                          item.value === 0 ||
                          item.dateValue === 0 ||
                          item.useQuantity === 0
                        ) {
                          return 0;
                        }
                        return (
                          item.quantity ||
                          item.value ||
                          item.dateValue ||
                          item.useQuantity
                        );
                      })
                    : [],
              };
            })
          : [],
      };
      return opt;
    },
  },
  watch: {
    origin: {
      handler() {
        this.chart.setOption(this.opt, true);
        this.chart.resize();
      },
      deep: true,
    },
    endValue: {
      handler() {
        this.chart.setOption(this.opt, true);
        this.chart.resize();
      },
      deep: true,
    },
    unit(newVal) {
      if (newVal) {
        this.chart.setOption(this.opt, true);
        this.chart.resize();
      }
    },
    ecdata: {
      handler() {
        this.chart.setOption(this.opt, true);
        this.chart.resize();
      },
      deep: true,
    },
    refreshId: {
      handler() {
        this.chart.resize();
      },
      deep: true,
    },
  },
  mounted() {
    this.initChart();
  },
  beforeDestroy() {
    if (!this.chart) {
      return;
    }
    this.chart.dispose();
    this.chart = null;
  },
  created() {
    this.r_color = this.rgba_color(this.color);
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$refs.ec);
      this.chart.setOption(this.opt);
      this.chart.resize();
    },
    rgba_color(color) {
      const color_arr = [];
      for (let i = 0; i < color.length; i++) {
        const num = color[i].split("#")[1];
        color_arr.push({
          a: `rgba(${parseInt(`0x${num.substr(0, 2)}`)},${parseInt(
            `0x${num.substr(2, 2)}`
          )},${parseInt(`0x${num.substr(4, 2)}`)},0.4)`,
          b: `rgba(${parseInt(`0x${num.substr(0, 2)}`)},${parseInt(
            `0x${num.substr(2, 2)}`
          )},${parseInt(`0x${num.substr(4, 2)}`)},0.1)`,
        });
      }
      return color_arr;
    },
  },
};
</script>
