<template>
  <div class="userDetail">
    <div class="back" @click="doBack()">
      <img src="../images/left.png" alt="" />
      <span>用户详情</span>
    </div>
    <div class="searchBox">
      <div class="searchLeft">
        <div class="formItem">
          <span class="label">企业名称:</span>
          <el-select
            v-model="form.entId"
            filterable
            placeholder="请输入企业名称"
            clearable
            class="comp"
          >
            <el-option
              v-for="item in entList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
        </div>
        <div class="formItem">
          <span class="label">合同年份：</span>
          <div class="formItemInfo">
            <el-date-picker
              v-model="form.startYear"
              value-format="yyyy"
              type="year"
              placeholder="选择开始年份"
            >
            </el-date-picker>
            <span class="linew"></span>
            <el-date-picker
              v-model="form.endYear"
              type="year"
              value-format="yyyy"
              placeholder="选择结束年份"
            >
            </el-date-picker>
          </div>
        </div>
      </div>
      <div class="btnBox">
        <div class="searchBtn" @click="doSearch()">查询</div>
        <div class="resetBtn" @click="doReset()">重置</div>
      </div>
    </div>
    <div class="tableBox">
      <div class="tabelHeader">
        <span class="text">企业信息</span>
        <!-- <div class="btn" @click="doUpdateEnt">同步用户</div> -->
      </div>
      <div class="table2">
        <div class="tableHeader">
          <div class="headerItem">企业名称</div>
          <div class="headerItem">设备</div>
          <div class="headerItem">设备额定功率(kW)</div>
          <div class="headerItem">最高运行负荷(kW)</div>
          <div class="headerItem">响应能力(kW)</div>
          <div class="headerItem">联系人/联系方式</div>
          <div class="headerItem">合同期限</div>
          <div class="headerItem">操作</div>
        </div>
        <div class="nodata" v-if="entUserDetailList.length === 0">暂无数据</div>
        <div class="tableContent" v-else>
          <div
            class="tableContentItem"
            v-for="(item, index) in entUserDetailList"
            :key="index"
          >
            <div class="tableContentItem-item1">{{ item.entName }}</div>
            <div class="tableContentItem-item-flex3" style="flex:4">
              <div
                class="tableContentItem-item-in"
                v-for="(device, index) in item.devices"
                :key="index"
              >
                <div class="item">{{ device.deviceName }}</div>
                <div class="item">
                  {{ device.power === null ? "--" : device.power }}
                </div>
                <div class="item">
                  {{ device.maxPower === null ? "--" : device.maxPower }}
                </div>
                <div class="item">
                  {{
                    device.responsePower === null ? "--" : device.responsePower
                  }}
                </div>
              </div>
            </div>
            <div
              class="tableContentItem-item1 phoneBox"
              style="display:block;position:relative;padding: 7px 0;"
            >
              <div style="position:relative;top:50%;transform:translateY(-50%)">
                <div
                  v-for="(phone, index) in item.showPhones"
                  :key="index"
                  style="margin-bottom:5px;"
                >
                  【{{ phone.smsName }}】{{ phone.smsPhone }}
                </div>
                <div v-if="item.phones.length > 3">...</div>

                <div class="tooltip" v-if="item.phones.length > 3">
                  <div
                    v-for="(phone, index) in item.phones"
                    :key="index"
                    style="margin-bottom:5px;color: #FFFFFF;font-size:14px;"
                  >
                    【{{ phone.smsName }}】{{ phone.smsPhone }}
                  </div>
                </div>
              </div>
            </div>
            <div class="tableContentItem-item1" style="font-size:13px;">
              {{ item.serviceStartDate }}-{{ item.serviceEndDate }}
            </div>
            <div class="tableContentItem-item1">
              <div
                style="font-size: 14px;color: #0780ED;cursor:pointer"
                @click="doEdit(item)"
              >
                编辑
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <el-drawer
      size="824px"
      class="editBox drawerBox"
      :visible.sync="drawer"
      :modal="false"
      :direction="direction"
    >
      <span class="drawerBoxTitle">编辑</span>
      <div class="entName">
        <div class="label">企业名称：</div>
        <div style="width:200px;">
          <el-input v-model="ruleForm.entName" disabled></el-input>
        </div>
      </div>
      <div class="drawerItem">
        <div class="label">设备信息：</div>
        <div class="deviceList">
          <div class="deviceHeader">
            <div class="item">设备名称</div>
            <div class="item">户号</div>
            <div class="item">设备额定功率</div>
            <div class="item">最高运行负荷</div>
            <div class="item">响应能力</div>
            <div class="item"></div>
          </div>
          <div
            class="deviceItem"
            v-for="(item, index) in ruleForm.devices"
            :key="index"
          >
            <div class="inputItem">
              <el-select
                v-model="item.deviceId"
                placeholder="请选择"
                @focus="selectfocus"
                @change="changeDevice(item)"
              >
                <el-option
                  v-for="item in deviceList"
                  :key="item.deviceId"
                  :label="item.deviceName"
                  :disabled="item.disabled"
                  :value="item.deviceId"
                >
                </el-option>
              </el-select>
            </div>
            <div class="inputItem">
              <el-input
                v-model="item.accountNo"
                @keydown.native="channelInputLimit"
                oninput="if(value.length>50)value=value.slice(0,50)"
                type="number"
              ></el-input>
            </div>
            <div class="inputItem">
              <el-input
                v-model="item.power"
                @keydown.native="channelInputLimit"
                oninput="if(value.length>8)value=value.slice(0,8)"
                type="number"
              ></el-input>
            </div>
            <div class="inputItem">
              <el-input
                v-model="item.maxPower"
                @keydown.native="channelInputLimit"
                oninput="if(value.length>8)value=value.slice(0,8)"
                type="number"
              ></el-input>
            </div>
            <div class="inputItem">
              <el-input
                v-model="item.responsePower"
                @keydown.native="channelInputLimit"
                oninput="if(value.length>8)value=value.slice(0,8)"
                type="number"
              ></el-input>
            </div>
            <div class="inputItem" style="display:flex;align-items:center;">
              <section style="display:flex;">
                <img
                  style="margin:0px 5px;width:24px;height:24px"
                  @click="addDevice(ruleForm.devices, index)"
                  src="../images/add.png"
                  alt=""
                />
                <img
                  style="width:24px;height:24px"
                  v-if="index > 0"
                  @click="reduceDevice(ruleForm.devices, index)"
                  src="../images/reduce.png"
                  alt=""
                />
              </section>
            </div>
          </div>
        </div>
      </div>
      <div class="drawerItem">
        <div class="label">
          联系人信息：<span style="font-size:14px;color: #666666;"
            >最多可添加10个联系人方式</span
          >
        </div>
        <div
          class="contact"
          v-for="(smsReq, index) in ruleForm.phones"
          :key="index"
        >
          <el-input
            class="name"
            v-model="smsReq.smsName"
            placeholder="姓名"
          ></el-input>
          <el-input
            class="phone"
            v-model="smsReq.smsPhone"
            placeholder="手机号"
            type="number"
            style="margin-right:10px;"
          ></el-input>
          <section style="min-width:100px;display:flex">
            <img
              style="margin-right:10px;width:24px;height:24px"
              v-if="index < 9"
              @click="addSms(ruleForm.phones, index)"
              src="../images/add.png"
              alt=""
            />
            <img
              style="width:24px;height:24px"
              v-if="index > 0"
              @click="reduceSms(ruleForm.phones, index)"
              src="../images/reduce.png"
              alt=""
            />
          </section>
        </div>
      </div>
      <div class="entName uploadBox">
        <div class="label">合同上传：</div>
        <div style="display:flex;">
          <el-upload
            class="upload-demo"
            :action="imgUploadUrl"
            :multiple="false"
            :limit="1"
            accept=".pdf"
            :headers="{
              ticket: authToken,
              token: authToken,
              Authorization: `Bearer ${authToken}`,
              'X-GW-AccessKey': accessKey,
            }"
            :on-success="handleSucess"
            :on-remove="handleRemove"
            :before-remove="beforeRemove"
            :on-exceed="handleExceed"
            :before-upload="beforeUpload"
            :file-list="fileList"
          >
            <el-button
              size="small"
              style="margin-top:20px;"
              v-if="showUploadBtn"
              type="primary"
            >
              点击上传
            </el-button>
            <div slot="tip" v-if="showUploadBtn" class="el-upload__tip">
              只能上传pdf文件
            </div>
          </el-upload>
          <el-button
            class="downloadBtn"
            v-if="!showUploadBtn"
            size="small"
            @click="downLoad()"
          >
            <i class="el-icon-download"></i>下载合同
          </el-button>
        </div>
      </div>
      <div class="entName" style="border-bottom:none;margin-top:20px;">
        <div class="label">合同期限：</div>
        <div>
          <el-date-picker
            v-model="timeValue"
            type="daterange"
            :value-format="'yyyy-MM-dd'"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          >
          </el-date-picker>
        </div>
      </div>
      <div class="entName" style="border-bottom:none;margin-top:20px;">
        <div class="label">负荷聚合商分成比例：</div>
        <div>
          <el-input
            class="phone"
            v-model="ruleForm.percent"
            placeholder="请输入0-100整数数值"
            type="number"
          ></el-input>
        </div>
      </div>
      <div style="text-align:center;margin-top: 20px;padding-bottom:20px;">
        <el-button type="primary" @click="submitForm('ruleForm')">
          确定
        </el-button>
        <el-button @click="cancle('ruleForm')">取消</el-button>
      </div>
    </el-drawer>
  </div>
</template>
<script>
import {
  uploadUrl,
  accessKeyValue,
  getEntUserDetailListV2,
  getEntUserOptions,
  autoUpdateEnt,
  doSaveOperation,
  getCimDeviceList,
  updateEnt,
  baseUrl,
} from "../api";
import { cloneDeep, downLoadXls } from "@/utils/util.js";

import axios from "axios";

export default {
  name: "tableComp",
  components: {
    // loading,
  },
  data() {
    return {
      drawer: false,
      direction: "rtl",
      aggregatorId: null,
      accessKey: "",
      imgUploadUrl: "",
      entUserDetailList: [],
      fileList: [],
      entList: [],
      deviceList: [],
      timeValue: "",
      showUploadBtn: true,
      ruleForm: {
        entName: "",
        areaCode: "",
        smsReqList: [{}],
        expectPower: "",
        delivery: false,
        powerDayBefore: null,
        powerHoursBefore: null,
        powerRealtime: null,
        agreement: "",
        shareRatio: 0,
        
      },
      form: {
        entId: "",
        startYear: "",
        endYear: "",
      },
    };
  },
  props: {
    activeObj: {
      type: Object,
      require: true,
    },
    searchEntId: {
      type: String,
      require: false,
    },
  },
  computed: {
    authToken() {
      return sessionStorage.getItem("token") || sessionStorage.getItem("ticket") || "";
    },
  },
  methods: {
    channelInputLimit(e) {
      const key = e.key;
      // 不允许输入'e'和'.'
      if (key === "e" || key === ".") {
        e.returnValue = false;
        return false;
      }
      return true;
    },
    handleRemove(file, fileList) {
      this.showUploadBtn = true;
      this.ruleForm.agreement = "";
    },
    handleSucess(response, file, fileList) {
      if (response.code === 200) {
        this.ruleForm.agreement = response.data;
        this.showUploadBtn = false;
      }
    },
    beforeUpload(file) {
      const isLt10M = file.size / 1024 / 1024 < 10;
      if (!isLt10M) {
        this.$message.error("上传合同大小不能超过10MB");
        return false;
      }
    },
    downLoad(item) {
      const query = {
        urlStr: this.ruleForm.agreement,
      };
      axios.defaults.headers.common.ticket = sessionStorage.getItem("ticket");
      axios.defaults.headers.common.token = sessionStorage.getItem("token") || sessionStorage.getItem("ticket");
      axios.defaults.headers.common.Authorization = `Bearer ${sessionStorage.getItem("token") || sessionStorage.getItem("ticket")}`;
      axios.defaults.headers.common["X-GW-AccessKey"] = accessKeyValue;
      axios({
        method: "get",
        url: `${baseUrl}/userManagement/downloadFile`, // 请求地址
        params: query, // 参数
        responseType: "blob", // 表明返回服务器返回的数据类型
      }).then(res => {
        const list = this.ruleForm.agreement.split("/");
        downLoadXls(res.data, list[list.length - 1]);
      });
    },
    changeDevice(item) {
      const data = this.deviceList.find(res => res.deviceId === item.deviceId);
      item.deviceBaseId = data.deviceBaseId;
      item.deviceName = data.deviceName;
      item.resourceTypeId = data.resourceTypeId;
    },
    checkRepeat(a) {
      return /(\x0f[^\x0f]+)\x0f[\s\S]*\1/.test(
        `\x0f${a.join("\x0f\x0f")}\x0f`
      );
    },
    submitForm() {
      if (
        this.ruleForm.phones.length === 1 &&
        (!this.ruleForm.phones[0].smsName || !this.ruleForm.phones[0].smsPhone)
      ) {
        this.$message({
          message: "请填写联系人",
          type: "warning",
        });
        return;
      }
      let isPhone = true;
      const phonesList = [];
      this.ruleForm.phones.forEach(item => {
        phonesList.push(item.smsPhone);
        if (!/^1[0-9]{10}$/.test(item.smsPhone)) {
          isPhone = false;
        }
      });
      if (!isPhone) {
        this.$message({
          message: "手机号输入有误，请重新填写",
          type: "warning",
        });
        return;
      }
      if (this.checkRepeat(phonesList)) {
        this.$message({
          message: "手机号重复，请重新填写",
          type: "warning",
        });
        return;
      }
      this.ruleForm.serviceStartDate = this.timeValue ? this.timeValue[0] : "";
      this.ruleForm.serviceEndDate = this.timeValue ? this.timeValue[1] : "";

      if (this.ruleForm.percent < 0 || this.ruleForm.percent > 100) {
        this.$message({
          message: "负荷聚合商分成比例请输入0-100整数数值",
          type: "warning",
        });
        return;
      }
      // 判断this.ruleForm.percent为整数
      if (this.ruleForm.percent % 1 !== 0) {
        this.$message({
          message: "负荷聚合商分成比例请输入0-100整数数值",
          type: "warning",
        });
        return;
      } 

      updateEnt(this.ruleForm).then(res => {
        if (res.data.code === 200) {
          this.$message({
            message: "编辑成功",
            type: "success",
          });
          this.doGetEntUserDetailList();
          this.drawer = false;
        }
      });
    },
    selectfocus() {
      this.deviceList.forEach(device => {
        device.disabled = false;
        this.ruleForm.devices.forEach(item => {
          if (item.deviceId === device.deviceId) {
            device.disabled = true;
          }
        });
      });
    },
    addDevice(item, index) {
      this.ruleForm.devices.splice(index + 1, 0, {
        aggregatorId: this.ruleForm.aggregatorId,
        stationId: this.ruleForm.stationId,
        entId: this.ruleForm.entId,
        responsePower: "",
        maxPower: "",
        power: "",
        accountNo: "",
        deviceId: "",
      });
      this.$forceUpdate();
    },
    reduceDevice(item, index) {
      this.ruleForm.devices.splice(index, 1);
      this.$forceUpdate();
    },
    addSms(item, index) {
      if (item.length === 10) {
        this.$message({
          message: "最多可添加10个联系人方式",
          type: "warning",
        });
        return;
      }
      this.ruleForm.phones.splice(index + 1, 0, {
        smsName: "",
        smsPhone: "",
      });
      this.$forceUpdate();
    },
    reduceSms(item, index) {
      this.ruleForm.phones.splice(index, 1);
      this.$forceUpdate();
    },
    doUpdateEnt() {
      const fd = new FormData();
      fd.append("aggregatorId", this.aggregatorId);
      autoUpdateEnt(fd).then(res => {
        if (res.data.code === 200) {
          this.$message({
            message: "同步用户成功",
            type: "success",
          });
        }
      });
    },
    doBack() {
      this.$emit("goBack");
    },
    doReset() {
      this.form = {
        entId: "",
        startYear: "",
        endYear: "",
      };
    },
    doGetEntUserOptions() {
      getEntUserOptions(
        { aggregatorId: this.aggregatorId }
      ).then(res => {
        if (res.data.code === 200) {
          this.entList = res.data.data;
        }
      });
    },
    cancle() {
      this.drawer = false;
    },
    doGetEntUserDetailList() {
      const query = {
        aggregatorId: this.aggregatorId,
        entId: this.form.entId,
        startYear: this.form.startYear,
        endYear: this.form.endYear,
      };
      getEntUserDetailListV2(query).then(res => {
        if (res.data.code === 200) {
          res.data.data.forEach(item => {
            if (item.phones.length > 3) {
              item.showPhones = item.phones.slice(0, 3);
            } else {
              item.showPhones = item.phones;
            }
          });
          this.entUserDetailList = res.data.data;
        }
      });
    },
    doEdit(item) {
      if (item.agreement) {
        const fileStrList = item.agreement.split("/");
        if (item.agreement !== "未上传") {
          this.fileList = [
            { name: fileStrList[fileStrList.length - 1], url: item.agreement },
          ];
          this.showUploadBtn = false;
        } else {
          this.fileList = [];
          this.showUploadBtn = true;
        }
      }
      if (item.serviceStartDate) {
        this.timeValue = [item.serviceStartDate, item.serviceEndDate];
      }
      this.doGetCimDeviceList(item);
      this.drawer = true;
      this.ruleForm = cloneDeep(item);
      if (this.ruleForm.phones.length === 0) {
        this.ruleForm.phones = [
          {
            smsName: "",
            smsPhone: "",
          },
        ];
      }
    },
    doSearch() {
      if (moment(this.form.endYear).isBefore(moment(this.form.startYear))) {
        this.$message({
          message: "结束时间不可在开始时间之前",
          type: "warning",
        });
        return;
      }
      this.doGetEntUserDetailList();
    },
    doGetCimDeviceList(item) {
      const query = {
        aggregatorId: item.aggregatorId,
        entId: item.entId,
        stationId: item.stationId,
      };
      getCimDeviceList(query).then(res => {
        if (res.data.code === 200) {
          res.data.data.forEach(item => {
            item.disabled = false;
          });
          this.deviceList = res.data.data;
        }
      });
    },
  },
  created() {
    this.imgUploadUrl = uploadUrl;
    this.accessKey = accessKeyValue;
    this.aggregatorId =
      sessionStorage.getItem("entId") || sessionStorage.getItem("cid");
    this.form.entId = this.searchEntId;
    this.doGetEntUserDetailList();
    this.doGetEntUserOptions();
  },
};
</script>
<style lang="less">
.el-drawer:focus {
  outline: 0;
}
.userDetail {
  .uploadBox {
    .el-upload-list {
      background: #f8fbfe;
      // border: 1px solid #D3DDE2;
      .el-upload-list__item:first-child {
        margin-top: 0px;
      }
    }
    .el-upload-list__item-name {
      color: #0780ed;
    }
    .downloadBtn {
      border: 1px solid #0780ed;
      margin-top: 10px;
      margin-left: 5px;
      span {
        color: #0780ed;
      }
    }
  }
  .drawerBox {
    height: calc(100vh - 44px);
    .drawerBoxTitle {
      position: absolute;
      font-size: 16px;
      font-weight: 600;
      color: #333333;
      top: 20px;
    }
    .el-drawer {
      padding: 0 32px;
      .el-drawer__header {
        padding: 20px 0 0;
      }
      .el-drawer__header {
        span {
          font-size: 16px;
          font-weight: 600;
          color: #333333;
        }
      }
      .el-drawer__body {
        height: calc(100% - 75px);
        overflow-y: auto;
      }
      .el-drawer__body::-webkit-scrollbar {
        display: none;
      }
    }
  }
  .formItem {
    .el-input__inner {
      height: 30px;
      line-height: 30px;
    }
    .formItemInfo {
      .el-input__inner {
        max-width: 150px !important;
      }
      .el-date-editor.el-input {
        max-width: 150px !important;
        .el-input__prefix {
          top: -4px;
        }
      }
    }
    .el-input {
      .el-input__suffix {
        top: 6px;
      }
    }
    .formItemInfo {
      .el-input {
        .el-input__suffix {
          top: -2px;
        }
      }
    }
    .is-focus {
      .el-input__suffix {
        top: -3px;
      }
    }
  }
  .el-table th > .cell {
    text-align: center;
  }
  .el-table .cell {
    text-align: center;
    padding-left: 0 !important;
    padding-right: 0 !important;
    .deviceName {
      height: auto;
    }
    .deviceName:nth-child(even) {
      background: #fafafa;
    }
    .deviceName:nth-child(odd) {
      background: #fff;
    }
  }
  .el-icon-circle-close {
    line-height: 40px;
  }
}
</style>
<style lang="less" type="text/less" scoped>
.table2 {
  width: 100%;
  border: 1px solid #e8e8e8;
  .nodata {
    width: 100%;
    text-align: center;
    padding: 30px 0;
    color: #666666;
  }
  .tableHeader {
    width: 100%;
    height: 40px;
    background: #eef4fc;
    display: flex;
    .dateHeader {
      width: 180px;
      height: 40px;
      border-right: 1px solid #e8e8e8;
      font-size: 14px;
      font-family: PingFangSC-Regular, PingFang SC;
      font-weight: 400;
      color: #666666;
      text-align: center;
      line-height: 40px;
    }
    .headerItem {
      flex: 1;
      height: 40px;
      border-right: 1px solid #e8e8e8;
      font-size: 14px;
      font-family: PingFangSC-Regular, PingFang SC;
      font-weight: 400;
      color: #666666;
      text-align: center;
      line-height: 40px;
    }
    .headerItem:last-child {
      border-right: none;
    }
  }
  .tableContent {
    width: 100%;
    height: auto;
    background: #ffffff;
    .tableContentItem {
      width: 100%;
      height: auto;
      display: flex;
      border-bottom: 1px solid #e8e8e8;
      .tableContentItem-item1 {
        flex: 1;
        border-right: 1px solid #e8e8e8;
        font-size: 14px;
        font-family: PingFangSC-Regular, PingFang SC;
        font-weight: 400;
        color: #333;
        text-align: center;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      .phoneBox {
        .tooltip {
          width: 165px;
          height: auto;
          background: #343a40;
          box-shadow: 0px 2px 4px 0px rgba(0, 0, 0, 0.16);
          position: absolute;
          right: -197px;
          top: 0;
          padding: 15px;
          border-radius: 5px;
          opacity: 0;
          transform: all 0.5s;
          display: none;
          z-index: 100000;
        }
      }
      .phoneBox:hover {
        .tooltip {
          opacity: 1;
          display: block;
        }
      }
      .tableContentItem-item-flex3 {
        flex-direction: column;
        display: flex;
        .tableContentItem-item-in {
          flex: 1;
          display: flex;
          .item {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            border-bottom: 1px solid #e8e8e8;
            min-height: 40px;
            height: auto;
            line-height: 20px;
            border-right: 1px solid #e8e8e8;
            text-align: center;
          }
        }
        .tableContentItem-item-in:last-child {
          .item {
            border-bottom: none;
          }
        }
      }
      .tableContentItem-item:last-child {
        border-right: none;
      }
    }
    .tableContentItem:last-child {
      border-bottom: none;
    }
  }
}
.userDetail {
  width: calc(100% - 40px);
  position: relative;
  padding: 0 20px;
  background: #f4f5f9;
  min-height: 100vh;
  .back {
    height: 54px;
    display: flex;
    align-items: center;
    cursor: pointer;
    width: 100px;
    img {
      width: 7px;
      height: 11px;
      margin-right: 4px;
    }
    span {
      font-size: 14px;
      color: #666666;
    }
  }
  .searchBox {
    width: calc(100% - 40px);
    height: 54px;
    background: #ffffff;
    border-radius: 12px;
    display: flex;
    padding: 0 20px;
    justify-content: space-between;
    .searchLeft {
      display: flex;
      height: 54px;
      align-items: center;
      .formItem {
        display: flex;
        height: 54px;
        align-items: center;
        margin-right: 50px;
        .label {
          font-size: 14px;
          font-weight: 400;
          color: #666666;
          display: inline-block;
          min-width: 60px;
          margin-right: 5px;
        }
        .formItemInfo {
          display: flex;
          height: 54px;
          align-items: center;
          .linew {
            display: inline-block;
            width: 10px;
            height: 2px;
            background: #d0d0d0;
            margin: 0 10px;
          }
        }
      }
    }
    .btnBox {
      display: flex;
      align-items: center;
      .searchBtn {
        width: 108px;
        height: 34px;
        margin-right: 20px;
        box-sizing: border-box;
        border-radius: 5px;
        background: #0780ed;
        text-align: center;
        line-height: 34px;
        color: white;
        font-size: 12px;
        cursor: pointer;
      }
      .resetBtn {
        width: 108px;
        height: 34px;
        box-sizing: border-box;
        border-radius: 5px;
        background: white;
        text-align: center;
        line-height: 34px;
        color: #0780ed;
        font-size: 12px;
        border: 1px solid #0780ed;
        cursor: pointer;
      }
    }
  }
  .tableBox {
    margin-top: 20px;
    padding: 20px;
    background: white;
    .tabelHeader {
      height: 34px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      .text {
        cursor: pointer;
        font-size: 18px;
        font-weight: 600;
        color: #333333;
      }
      .btn {
        width: 108px;
        height: 34px;
        background: #0780ed;
        border-radius: 5px;
        border: 1px solid #0780ed;
        font-size: 14px;
        font-family: PingFangSC-Regular, PingFang SC;
        font-weight: 400;
        color: #ffffff;
        text-align: center;
        line-height: 34px;
      }
    }
  }
}
.editBox {
  .entName {
    border-bottom: 1px solid #eeeeee;
    display: flex;
    align-items: center;
    padding-bottom: 24px;
    .label {
      font-size: 14px;
      font-family: PingFangSC-Regular, PingFang SC;
      font-weight: 400;
      color: #343a40;
    }
  }
  .drawerItem {
    border-bottom: 1px solid #eeeeee;
    padding: 24px 0;
    .label {
      font-size: 14px;
      font-family: PingFangSC-Regular, PingFang SC;
      font-weight: 400;
      color: #343a40;
    }
    .deviceList {
      margin-top: 14px;
      .deviceHeader {
        display: flex;
        .item {
          flex: 1;
          font-size: 14px;
          font-weight: 400;
          color: #343a40;
        }
      }
      .deviceItem {
        display: flex;
        margin-top: 15px;
        .inputItem {
          flex: 1;
          margin-right: 14px;
        }
      }
    }
    .contact {
      display: flex;
      align-items: center;
      margin-bottom: 10px;
      margin-top: 15px;
      .name {
        width: 150px;
        margin-right: 15px;
      }
      .phone {
        width: 250px;
      }
      img {
        cursor: pointer;
      }
    }
  }
}
</style>
