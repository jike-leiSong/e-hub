import service from "@/services/http";
import commonUtil from "platform-common-component";
import { getRandom, sortStr, sha1 } from "@/utils/util.js";

let urlStr = "";
let pointUrl = "";
let accessKey = "";
const runtimeConfig = window.__AGGREGATION_CONFIG__ || {};
const envConfig = import.meta.env || {};
const apiBaseUrl = runtimeConfig.apiBaseUrl || envConfig.VITE_API_BASE_URL;
const pointBaseUrl = runtimeConfig.pointBaseUrl || envConfig.VITE_POINT_BASE_URL;
const envAccessKey = runtimeConfig.accessKey || envConfig.VITE_GW_ACCESS_KEY;
const hasConfiguredApiBase =
  Object.prototype.hasOwnProperty.call(runtimeConfig, "apiBaseUrl") ||
  Object.prototype.hasOwnProperty.call(envConfig, "VITE_API_BASE_URL");

if (hasConfiguredApiBase) {
  urlStr = normalizeBaseUrl(apiBaseUrl || "");
  pointUrl = normalizeBaseUrl(pointBaseUrl || "/fnw-datamining");
  accessKey = envAccessKey || "";
} else if (commonUtil.currentENV() === "isProd") {
  urlStr = "https://gateway.fanneng.com/load-aggregator-business";
  pointUrl = "https://gateway.fanneng.com/fnw-datamining";
  accessKey = "rXddpRDLO2Z72mM6ENBysr62fIof3Mfg";
} else if (window.location.href.indexOf(".uat") !== -1) {
  urlStr = "http://load-aggregator-business.uat.fnwintranet.com";
  pointUrl = "http://fnw-datamining.cloud-common.fnwrancher-dev.enncloud.cn";
  accessKey = "BiJHUDt6wAOGB4UoSnQLDWF2K6R0KCmY";
} else if (commonUtil.currentENV() === "isFat") {
  urlStr = "http://rdfa-gateway-5.fat.fnwintranet.com/load-aggregator-business";
  pointUrl = "http://rdfa-gateway-5.fat.fnwintranet.com/fnw-datamining";
  accessKey = "6eOyl9kLYhBdWgonGp21UnLLvircDSTg";
} else {
  urlStr = "http://gateway.test.fnwintranet.com/load-aggregator-business";
  pointUrl = "http://gateway.test.fnwintranet.com/fnw-datamining";
  accessKey = "BDo9pMIbZqJCUI9WsUHsCxpFNtCJMyR0";
}

function normalizeBaseUrl(url) {
  return String(url).replace(/\/$/, "");
}

export const baseUrl = urlStr;
export const accessKeyValue = accessKey;
export function getProfitCalculation(params) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/historyQuery/getProfitCalculation`,
    headers: {
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

// 出清价格接口
export function getPrice(params) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/historyQuery/getPrice`,
    headers: {
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
// 汇总功率曲线
export function getMetricList(params) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/historyQuery/getMetricList`,
    headers: {
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
// 汇总功率曲线
export function getTotalPowerChart(params) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/historyQuery/getTotalPowerChart`,
    headers: {
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

export function getResourceTypeList(params) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/yesterday/getResourceTypeList`,
    headers: {
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
//  收益统计
export function getProfitStatistics(params) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/historyQuery/profitStatistics`,
    headers: {
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
//  用户收益统计
export function getUserProfitStatistics(params) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/historyQuery/userProfitStatistics`,
    headers: {
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
//  获取企业用户选项列表
export function getEntUserOptions(params) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/entUserDetail/options`,
    headers: {
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
//  用户完成调节情况曲线图接口
export function getUserAdjustmentGraph(params) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/historyQuery/userAdjustmentGraph`,
    headers: {
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

//  用户完成调节情况曲线图接口【新】
export function getUserCompletionEcharts(params) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/historyQuery/userAdjustmentGraphNew`,
    headers: {
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

//  查询设备列表
export function getDeviceList(params) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/yesterday/getDeviceList`,
    headers: {
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
//  查询设备列表
export function getUserAdjustmentTable(params) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/historyQuery/userAdjustmentTable`,
    headers: {
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
//  设备运行情况
export function getDeviceRunStatusChart(params) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/historyQuery/deviceRunStatusChart`,
    headers: {
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
export function doSaveOperation(params) {
  const timestamp = new Date().getTime();
  const nonce = getRandom();
  const signature = sha1(sortStr("fnwLog", timestamp, nonce));
  if (window.navigator.userAgent.split("fannengApp--")[1]) {
    const navigatorData = JSON.parse(
      window.navigator.userAgent.split("fannengApp--")[1]
    );
    params.equipId = navigatorData.equipId;
    params.equipName = navigatorData.equipName;
    params.equipSystem = navigatorData.equipSystem;
    params.equipType = navigatorData.equipType;
    params.equipVersion = navigatorData.equipVersion;
  }
  return service({
    method: "post",
    data: params,
    url: `${pointUrl}/operation/save`,
    headers: {
      token: sessionStorage.getItem("ticket"),
      timestamp: new Date().getTime(),
      nonce,
      timestamp,
      signature,
      "X-GW-AccessKey": accessKey,
    },
  });
}

// 导出excel
export function exportExcel(params) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/historyQuery/exportAdjust`,
    responseType: "blob",
    headers: {
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

export function exportBuZhaoUploadData(params) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/historyQuery/exportBuZhaoUploadData`,
    responseType: "blob",
    headers: {
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

// 导出出清价格excel
export function exportClearPriceExcel(params) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/historyQuery/getPriceExcel`,
    responseType: "blob",
    headers: {
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

// 获取出清价格表格数据
export function getClearPriceTable(params) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/historyQuery/getPriceTable`,
    headers: {
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
