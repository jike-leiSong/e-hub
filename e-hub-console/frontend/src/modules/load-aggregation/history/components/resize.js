export default {
  data() {
    return {
      mian: null,
    };
  },
  mounted() {
    this.__resizeHandler = () => {
      if (this.chart) {
        this.chart.resize();
      }
    };
    window.addEventListener("resize", this.__resizeHandler);
    this.mian = document.getElementById("mian");
    this.mian &&
      this.mian.addEventListener("transitionend", this.sidebarResizeHandler);
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.__resizeHandler);
    this.mian &&
      this.mian.removeEventListener("transitionend", this.sidebarResizeHandler);
  },
  methods: {
    sidebarResizeHandler(e) {
      if (e.propertyName === "margin-left") {
        this.__resizeHandler();
      }
    },
  },
};
