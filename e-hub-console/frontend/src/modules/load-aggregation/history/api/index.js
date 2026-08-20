import service from "@/services/http";
import { getRandom, sortStr, sha1 } from "@/utils/util.js";

const runtimeConfig = window.__AGGREGATION_CONFIG__ || {};
const envConfig = import.meta.env || {};
const apiBaseUrl = runtimeConfig.apiBaseUrl || envConfig.VITE_API_BASE_URL;
const pointBaseUrl = runtimeConfig.pointBaseUrl || envConfig.VITE_POINT_BASE_URL;
const envAccessKey = runtimeConfig.accessKey || envConfig.VITE_GW_ACCESS_KEY;
const jsonContentType = "application/json;charset=UTF-8";
const urlStr = normalizeBaseUrl(apiBaseUrl || "");
const pointUrl = normalizeBaseUrl(pointBaseUrl || "/fnw-datamining");
const accessKey = envAccessKey || "";

function normalizeBaseUrl(url) {
  return String(url).replace(/\/$/, "");
}

function loadAggregationHeaders() {
  return {
    ticket: sessionStorage.getItem("ticket"),
    "X-GW-AccessKey": accessKey,
  };
}

function jsonHeaders() {
  return {
    ...loadAggregationHeaders(),
    "Content-Type": jsonContentType,
  };
}

export const baseUrl = urlStr;
export const accessKeyValue = accessKey;
export function getProfitCalculation(params) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/historyQuery/getProfitCalculation`,
    headers: loadAggregationHeaders(),
  });
}

// 出清价格接口
export function getPrice(params) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/historyQuery/getPrice`,
    headers: jsonHeaders(),
  });
}
// 汇总功率曲线
export function getMetricList(params) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/historyQuery/getMetricList`,
    headers: jsonHeaders(),
  });
}
// 汇总功率曲线
export function getTotalPowerChart(params) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/historyQuery/getTotalPowerChart`,
    headers: loadAggregationHeaders(),
  });
}

export function getResourceTypeList(params) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/yesterday/getResourceTypeList`,
    headers: loadAggregationHeaders(),
  });
}
//  收益统计
export function getProfitStatistics(params) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/historyQuery/profitStatistics`,
    headers: jsonHeaders(),
  });
}
//  用户收益统计
export function getUserProfitStatistics(params) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/historyQuery/userProfitStatistics`,
    headers: jsonHeaders(),
  });
}
//  获取企业用户选项列表
export function getEntUserOptions(params) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/entUserDetail/options`,
    headers: loadAggregationHeaders(),
  });
}

export function getEntUserDetailRespList(params) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/entUserDetail/getEntUserDetailRespList`,
    headers: loadAggregationHeaders(),
  });
}
//  用户完成调节情况曲线图接口
export function getUserAdjustmentGraph(params) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/historyQuery/userAdjustmentGraph`,
    headers: jsonHeaders(),
  });
}

//  用户完成调节情况曲线图接口【新】
export function getUserCompletionEcharts(params) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/historyQuery/userAdjustmentGraphNew`,
    headers: jsonHeaders(),
  });
}

//  查询设备列表
export function getDeviceList(params) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/yesterday/getDeviceList`,
    headers: loadAggregationHeaders(),
  });
}
//  查询设备列表
export function getUserAdjustmentTable(params) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/historyQuery/userAdjustmentTable`,
    headers: jsonHeaders(),
  });
}
//  设备运行情况
export function getDeviceRunStatusChart(params) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/historyQuery/deviceRunStatusChart`,
    headers: jsonHeaders(),
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
    headers: loadAggregationHeaders(),
  });
}

export function exportBuZhaoUploadData(params) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/historyQuery/exportBuZhaoUploadData`,
    responseType: "blob",
    headers: loadAggregationHeaders(),
  });
}

// 导出出清价格excel
export function exportClearPriceExcel(params) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/historyQuery/getPriceExcel`,
    responseType: "blob",
    headers: loadAggregationHeaders(),
  });
}

// 获取出清价格表格数据
export function getClearPriceTable(params) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/historyQuery/getPriceTable`,
    headers: jsonHeaders(),
  });
}
