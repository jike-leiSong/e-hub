<template>
  <el-dialog title="预测数据上报" :visible.sync="show" class="custom-dialog-predict-data-report" width="600px" :close-on-click-modal="false" :close-on-press-escape="false" append-to-body :before-close="cancel">
    <div class="main">
      <!-- 资源类型 -->
      <div class="input-info">
        <div>
          <p>*</p>
          <p>资源类型</p>
        </div>
        <div>
          <el-select v-model="resourceTypeValue" placeholder="请选择资源类型" size="small">
            <el-option v-for="item in resourceTypeOptions" :key="item.id" :disabled="item.display !== 1" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </div>
      </div>
      <!-- 日期范围 -->
      <div class="input-info">
        <div>
          <p>*</p>
          <p>日期范围</p>
        </div>
        <div>
          <el-date-picker v-model="dateRangeValue" type="daterange" format="yyyy-MM-dd" value-format="yyyy-MM-dd" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" size="small"></el-date-picker>
        </div>
      </div>
      <!-- 上传文件 -->
      <div class="input-info input-info-2">
        <div>
          <p>*</p>
          <p>上传文件</p>
        </div>
        <div class="upload-file">
          <el-upload :auto-upload="false" :multiple="false" :limit="1" :on-exceed="uploadExceed" :on-remove="uploadRemove" :on-change="uploadChange" :file-list="fileList">
            <el-button size="small" type="primary">点击上传</el-button>
            <div slot="tip" class="el-upload__tip">仅支持 xls/xlsx 格式</div>
          </el-upload>
        </div>
      </div>
    </div>
    <div slot="footer">
      <el-button @click="cancel" plain size="small">取消</el-button>
      <el-button @click="confirm" :loading="buttonLoading" type="primary" size="small">确定</el-button>
    </div>
  </el-dialog>
</template>
<script type="text/javascript">
  import { getResourceTypeList, submitPredictionData } from "../../api/index.js";
  export default {
    data() {
      return {
        show: false,
        // 资源类型
        resourceTypeValue: "",
        resourceTypeOptions: [],
        // 日期范围
        dateRangeValue: [],
        // 文件
        fileList: [],
        // buttonLoading
        buttonLoading: false,
      };
    },
    created() {},
    methods: {
      init() {
        this.resourceTypeValue = "";
        this.resourceTypeOptions = [];
        this.dateRangeValue = [];
        this.fileList = [];
        this.buttonLoading = false;
        this.getResourceTypeOptions();
        this.show = true;
      },
      // 获取资源类型
      getResourceTypeOptions() {
        const params = {
          aggregatorId: sessionStorage.getItem("entId") || "",
        };
        getResourceTypeList(params).then((res) => {
          if (res && res.data && res.data.code === 200) {
            if (res.data.data && res.data.data.length > 0) {
              this.resourceTypeOptions = JSON.parse(JSON.stringify(res.data.data));
            } else {
              this.resourceTypeOptions = [];
            }
          } else {
            this.$message.error(res.data.msg);
          }
        });
      },
      // 上传文件 - 超限提示
      uploadExceed() {
        this.$message.warning("只能上传一个文件");
      },
      // 上传文件 - 删除
      uploadRemove() {
        this.fileList = [];
      },
      // 上传文件 - 文件变化（校验）
      uploadChange(file, fileList) {
        const limitFormat = ["application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"];
        const fileType = file && file.raw ? file.raw.type : file.type;
        if (limitFormat.includes(fileType)) {
          this.fileList = fileList;
        } else {
          this.fileList = [];
          this.$message.warning("只能上传 xls/xlsx 格式的文件");
        }
      },
      // 取消
      cancel() {
        this.show = false;
      },
      // 确认
      async confirm() {
        // 校验资源类型
        if (!this.resourceTypeValue) {
          this.$message.warning("请选择资源类型");
          return false;
        }
        // 校验日期范围
        if (!this.dateRangeValue || this.dateRangeValue.length !== 2 || !this.dateRangeValue[0] || !this.dateRangeValue[1]) {
          this.$message.warning("请选择日期范围");
          return false;
        }
        // 校验文件
        const file = this.fileList && this.fileList.length > 0 ? this.fileList[0].raw : null;
        if (!file) {
          this.$message.warning("请上传文件");
          return false;
        }

        // 创建 FormData
        const formData = new FormData();
        formData.append("aggregatorId", sessionStorage.getItem("entId") || "");
        formData.append("resourceType", this.resourceTypeValue);
        formData.append("startDate", this.dateRangeValue && this.dateRangeValue.length === 2 ? this.dateRangeValue[0] : "");
        formData.append("endDate", this.dateRangeValue && this.dateRangeValue.length === 2 ? this.dateRangeValue[1] : "");
        formData.append("file", file);

        this.buttonLoading = true;
        submitPredictionData(formData)
          .then((res) => {
            this.buttonLoading = false;
            if (res && res.data && res.data.code === 200) {
              this.$message.success("上报成功，预计每日06：50自动申报");
              this.cancel();
            } else {
              this.$message.error(res.data.msg);
            }
          })
          .catch(() => {
            this.buttonLoading = false;
            this.$message.error("上报失败，请稍后重试");
          });
      },
    },
  };
</script>
<style lang="less" scoped>
  .custom-dialog-predict-data-report {
    display: flex;
    justify-content: center;
    align-items: center;
    ::v-deep .el-dialog {
      margin: 0 !important;
      height: 400px;
      overflow: hidden;
      background: #ffffff;
      box-shadow: 0px 2px 12px 0px rgba(0, 0, 0, 0.1);
      border-radius: 4px;
      display: flex;
      flex-direction: column;
      .el-dialog__header {
        margin: 20px;
        padding: 0;
        height: 20px;
        line-height: 20px;
        background: #ffffff;
        display: flex;
        justify-content: space-between;
        align-items: center;
        .el-dialog__title {
          padding: 0;
          line-height: 20px;
          color: #191919;
          font-size: 16px;
          font-weight: 500;
          font-family: PingFangSC-Medium, PingFang SC;
        }
        .el-dialog__headerbtn {
          top: 20px;
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
          margin: 20px;
          overflow: hidden;
          display: flex;
          flex-direction: column;
          .input-info {
            margin: 0 0 20px 0;
            display: flex;
            align-items: center;
            > div {
              &:nth-child(1) {
                margin-right: 10px;
                height: 20px;
                line-height: 20px;
                display: flex;
                align-items: center;
                > p {
                  height: 20px;
                  line-height: 20px;
                  &:nth-child(1) {
                    margin-right: 4px;
                    color: #f56c6c;
                    font-size: 18px;
                  }
                  &:nth-child(2) {
                    color: #191919;
                    font-size: 14px;
                    font-weight: 400;
                    font-family: PingFangSC-Medium, PingFang SC;
                  }
                }
              }
              &:nth-child(2) {
                flex: 1;
                overflow: hidden;
              }
            }
          }
          .input-info-2 {
            align-items: flex-start;
            > div {
              &:nth-child(1) {
                margin-top: 6px;
              }
            }
          }
          .upload-file {
            width: auto;
            ::v-deep .el-upload__tip {
              margin-top: 8px;
              color: #999999;
              font-size: 12px;
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
          margin: 0;
          padding: 4px 21px;
          line-height: 22px;
          font-size: 14px;
          font-family: PingFangSC-Regular, PingFang SC;
          font-weight: 400;
          border-radius: 4px;
          border-color: #0780ed;
          &:nth-child(1) {
            color: #0780ed;
            margin-right: 20px;
          }
          &:nth-child(2) {
            color: #ffffff;
            background: #0780ed;
          }
        }
      }
    }
  }
</style>
