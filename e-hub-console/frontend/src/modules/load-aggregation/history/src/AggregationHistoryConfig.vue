<template>
  <div class="config" style="height: 100%;">
    <CommonConfig
      :activeObj="activeObj"
      :moduleName="activeObj.label"
      @changeType="changeType"
    >
      <template v-slot:attribute>
        <div>
          <div class="label">可操作配置</div>
          <el-radio-group v-model="canSet">
            <el-radio label="0">可操作</el-radio>
            <el-radio label="1">不可操作</el-radio>
          </el-radio-group>
        </div>
        <div class="btnBox">
          <el-button class="btn" type="primary" @click="doRefresh()">
            刷新数据
          </el-button>
        </div>
      </template>
      <template v-slot:dataSource> </template>
      <template v-slot:interactive></template>
    </CommonConfig>
  </div>
</template>
<script>
export default {
  name: "AggregationHistoryConfig",
  data() {
    return {
      canSet: "0",
    };
  },
  props: {
    activeObj: {
      type: Object,
      require: true,
    },
    pageConfig: {
      type: Object,
      default: () => {
        return {};
      },
    },
  },
  watch: {},
  methods: {
    changeType(val) {
      this.activeObj.option.data.dataType = val;
    },
    doRefresh() {
      this.activeObj.option.data.canSet = this.canSet;
      this.$message({
        type: "success",
        message: "操作成功",
      });
    },
  },
};
</script>
<style lang="less" type="text/less" scoped>
.config {
  .btnBox {
    width: 100%;
    height: 68px;
    background: rgba(255, 255, 255, 1);
    position: fixed;
    bottom: 0;
    display: flex;
    align-items: center;
    padding-left: 100px;
    border-top: 1px solid #e0e3e5;
    .btn {
      width: 96px;
      height: 28px;
      background: rgba(23, 141, 255, 1);
      border-radius: 2px;
      font-size: 14px;
      line-height: 28px;
      padding: 0;
    }
  }
  .label {
    margin-bottom: 10px;
    color: #333333;
  }
  .attr-title {
    color: #333333;
    font-size: 14px;
    font-weight: 400;
    margin: 0 20px 0 0px;
  }
  .config-button {
    width: 85px;
    height: 27px;
    padding: 0;
    border-radius: 2px;
  }
  .config-button.is-plain:focus,
  .config-button.is-plain:hover {
    color: #333333;
  }
}
</style>
