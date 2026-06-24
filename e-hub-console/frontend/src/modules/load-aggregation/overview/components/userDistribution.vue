<template>
  <div class="user">
    <div class="common-header">
      <span class="name">用户分布</span>
      <div class="header-right">
        <div
          style="height: 58px;display:flex;align-items:center;margin-right:20px"
        >
          <span class="header-right-text"
            >用户&nbsp;<span style="color:#0780ED">{{
              entUserDetailRespListData.length
            }}</span
            >&nbsp;家</span
          >
        </div>
        <div
          style="height: 58px;display:flex;align-items:center"
          @click="goUserDetail()"
        >
          <span class="header-right-text">用户详情</span>
          <img class="detailImg" src="../images/right.png" alt="" />
        </div>
      </div>
    </div>
    <div class="userMap">
      <div id="container"></div>
    </div>
  </div>
</template>
<script>
import { getEntUserDetailRespList } from "../api";
import markerIcon from "../images/dingwei.png";

function resolveAggregatorId() {
  return (
    sessionStorage.getItem("aggregatorId") ||
    sessionStorage.getItem("entId") ||
    sessionStorage.getItem("cid") ||
    ""
  );
}

export default {
  name: "userDistribution",
  data() {
    return {
      entUserDetailRespListData: [],
      map: null,
      infoMarker: null,
      aggregatorId: null,
    };
  },
  props: {
  },
  methods: {
    goUserDetail(e) {
      if (!e) {
        this.$emit("goUserDetail", null);
      } else {
        this.$emit("goUserDetail", e.entId);
      }
    },
    addMarker(item) {
      const vm = this;
      const icon = new AMap.Icon({
        size: new AMap.Size(20, 31), // 图标尺寸
        image: markerIcon, // Icon的图像
        imageSize: new AMap.Size(20, 31), // 根据所设置的大小拉伸或压缩图片
      });
      const marker = new AMap.Marker({
        icon,
        position: new AMap.LngLat(
          Number(item.longitude),
          Number(item.latitude)
        ),
        offset: new AMap.Pixel(-20, -31),
        zIndex: 1001,
      });
      marker.on("mousedown", function(e) {
        vm.cleartMarker();
        vm.addInfoMarker(item);
      });
      this.map.add(marker);
    },
    cleartMarker() {
      if (this.infoMarker) {
        this.infoMarker.setMap(null);
      }
    },
    // 添加文本标记
    addInfoMarker(item) {
      const vm = this;
      const markerContent = `
      <div style="z-index:10000;position:relative;flex-direction:column;padding:10px;display:flex;border-radius: 12px;width: auto;height: auto;background: #FFFFFF;box-shadow: 0px 2px 6px 0px rgba(0, 0, 0, 0.28);">
        <div style="position:absolute;left:-7px;top:50%;width:0;height:0;border-top:7px solid transparent;border-bottom:7px solid transparent;border-right:7px solid #FFFFFF;"></div>
        <div style="font-size: 14px;font-weight: 600;color: #333333;margin-bottom:7px">${item.entName}</div>
        <div style="display:flex;height:13px;justify-content:space-between;align-items:center">
          <div style="font-size: 12px;font-weight: 400;color: #999999;">最大调节容量 <span style="font-size: 14px;font-weight: 600;color: #333;">${item.totalPower}kW</span></div>
          <div style="margin-left:10px;font-size: 12px;font-weight: 400;color: #666666;">详情></div>
        </div>
      </div>
      `;
      // 点标记显示内容，HTML要素字符串
      this.infoMarker = new AMap.Marker({
        position: new AMap.LngLat(
          Number(item.longitude),
          Number(item.latitude)
        ),
        // 将 html 传给 content
        content: markerContent,
        // 以 icon 的 [center bottom] 为原点
        offset: new AMap.Pixel(15, -85),
      });
      this.infoMarker.on("mousedown", function(e) {
        vm.goUserDetail(item);
      });
      // 将 markers 添加到地图
      this.map.add(this.infoMarker);
    },
    doGetEntUserDetailRespList() {
      getEntUserDetailRespList(
        { aggregatorId: this.aggregatorId }
      ).then(res => {
        if (res.data.code === 200) {
          this.entUserDetailRespListData = res.data.data;
          if (
            this.entUserDetailRespListData &&
            this.entUserDetailRespListData.length > 0
          ) {
            this.addInfoMarker(this.entUserDetailRespListData[0]);
            res.data.data.forEach(item => {
              if (item.latitude && item.longitude) {
                this.addMarker(item);
              }
            });
          }
        }
      });
    },
  },
  created() {
    this.aggregatorId = resolveAggregatorId();
    const vm = this;
    const url =
      "https://webapi.amap.com/maps?v=1.4.15&key=c6900ccd32da4bc931626e3a100dce2b&callback=onLoad&plugin=AMap.DistrictSearch,AMap.Scale";
    const jsapi = document.createElement("script");
    jsapi.charset = "utf-8";
    jsapi.src = url;
    document.head.appendChild(jsapi);
    window.onLoad = function() {
      vm.map = new AMap.Map("container", {
        zoom: 5,
        resizeEnable: true,
      });
      vm.doGetEntUserDetailRespList();
      // 添加缩放控件
      AMap.plugin(["AMap.ToolBar"], function() {
        // 在图面添加工具条控件，工具条控件集成了缩放、平移、定位等功能按钮在内的组合控件
        vm.map.addControl(
          new AMap.ToolBar({
            // 简易缩放模式，默认为 false
            liteStyle: true,
          })
        );
      });
    };
  },
};
</script>
<style lang="less"></style>
<style lang="less" type="text/less" scoped>
.user {
  width: 100%;
  height: 100%;
  background: #ffffff;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  .userMap {
    flex: 1;
    #container {
      width: 100%;
      height: 100%;
    }
  }
  .common-header {
    height: 58px;
    display: flex;
    align-items: center;
    padding: 0 20px;
    justify-content: space-between;
    .name {
      font-size: 18px;
      font-weight: 600;
      color: #333333;
    }
    .header-right {
      height: 58px;
      display: flex;
      align-items: center;
      cursor: pointer;
      .header-right-text {
        font-size: 14px;
        font-weight: 400;
        color: #666666;
      }
      .detailImg {
        width: 7px;
        height: 11px;
        margin-left: 3px;
      }
    }
  }
}
</style>
