<template>
  <div class="commontabbar">
    <div
      class="tabbarItem"
      :class="{ 'active': selTab === item.id, 'disabled': item.display !== 1 }"
      v-for="(item, index) in resourceTypeList"
      :key="index"
      @click="selTabClick(item)"
    >
      {{ item.name }}
    </div>
  </div>
</template>
<script>
import { getResourceTypeList } from "../api";

export default {
  name: "tabbar",
  data() {
    return {
      selTab: "1",
      resourceTypeList: [],
    };
  },
  props: {},
  methods: {
    selTabClick(item) {
      if (item.display === 1) {
        this.$emit("tabClick", item)
        this.selTab = item.id
      }
    },
    doGetResourceTypeList() {
      getResourceTypeList(
        {
          aggregatorId: sessionStorage.getItem("entId"),
        }
      ).then(res => {
        this.resourceTypeList = res.data.data
        if (this.resourceTypeList.length > 0) {
          for (let i = 0; i < this.resourceTypeList.length; i++) {
            if (this.resourceTypeList[i].display === 1) {
              this.selTabClick(this.resourceTypeList[i])
              break
            }
          }
        }
      });
    },
  },
  created() {
    this.doGetResourceTypeList();
  },
};
</script>
<style lang="less"></style>
<style lang="less" type="text/less" scoped>
.commontabbar {
  width: auto;
  border-radius: 6px;
  overflow: hidden;
  height: 30px;
  display: inline-flex;
  border: 1px solid #e9e9e9;
  .tabbarItem {
    width: auto;
    font-size: 14px;
    font-weight: 400;
    color: #666666;
    height: 30px;
    line-height: 30px;
    padding: 0 16px;
    border-right: 1px solid #e9e9e9;
    cursor: pointer;
  }
  .active {
    background: #0780ed;
    color: #ffffff;
  }
  .disabled {
    cursor: not-allowed;
    color: #C0C4CC;
  }
  .tabbarItem:last-child {
    border-right: none;
  }
}
</style>
