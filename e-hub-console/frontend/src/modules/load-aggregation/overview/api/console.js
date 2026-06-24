/**
 * 负荷聚合 - 运营总览模块 Console API
 * 使用Console本地接口，替代原有的外部网关调用
 */
import service from "@/services/http";

// 统一请求封装
function request(config) {
  return service({
    ...config,
    headers: {
      ...config.headers,
      // 使用Console的token认证
      Authorization: `Bearer ${sessionStorage.getItem("token") || sessionStorage.getItem("console-token")}`,
    },
  });
}

// ==================== 收益相关 ====================
export function getWeekProfit(params) {
  return request({
    method: "get",
    url: "/profit/week",
    params,
  });
}

export function getContentList(params) {
  return request({
    method: "get",
    url: "/profit/getContentList",
    params,
  });
}

export function getProfitList(params) {
  return request({
    method: "get",
    url: "/profit/list",
    params,
  });
}

export function getListByEntIdList(params) {
  return request({
    method: "get",
    url: "/profit/listByEntIdList",
    params,
  });
}

// ==================== 昨日详情 ====================
export function getResourceTypeList(params) {
  return request({
    method: "get",
    url: "/yesterday/getResourceTypeList",
    params,
  });
}

export function getLastProfit(params) {
  return request({
    method: "get",
    url: "/yesterday/getLastProfit",
    params,
  });
}

export function getOverview(params) {
  return request({
    method: "get",
    url: "/yesterday/getOverview",
    params,
  });
}

export function getDeviceList(params) {
  return request({
    method: "get",
    url: "/yesterday/getDeviceList",
    params,
  });
}

export function getEntUserDeviceChartResp(params) {
  return request({
    method: "get",
    url: "/yesterday/getEntUserDeviceChartResp",
    params,
  });
}

export function getEntUserOverviewResp(params) {
  return request({
    method: "get",
    url: "/yesterday/getEntUserOverviewResp",
    params,
  });
}

export function entInvite(data) {
  return request({
    method: "post",
    url: "/yesterday/entInvite",
    data,
  });
}

// ==================== 今日详情 ====================
export function getTodayEntUserDeviceChartResp(params) {
  return request({
    method: "get",
    url: "/today/getEntUserDeviceChartResp",
    params,
  });
}

export function getIotLog(params) {
  return request({
    method: "get",
    url: "/today/getIotLog",
    params,
  });
}

export function getEntUserTree(params) {
  return request({
    method: "get",
    url: "/today/get/device/tree",
    params,
  });
}

export function getRealTimeSummaryEcharts4(params) {
  return request({
    method: "get",
    url: "/today/getMultiDeviceChartResp",
    params,
  });
}

// ==================== 明日详情 ====================
export function getTomorrowEntUserDeviceChartResp(params) {
  return request({
    method: "get",
    url: "/tomorrow/getEntUserDeviceChartResp",
    params,
  });
}

export function getAggregatorApply(params) {
  return request({
    method: "get",
    url: "/tomorrow/getAggregatorApply",
    params,
  });
}

export function updateAggregatorApply(data) {
  return request({
    method: "post",
    url: "/tomorrow/updateAggregatorApply",
    data,
  });
}

export function getAggregatorApplyOfferResp(params) {
  return request({
    method: "get",
    url: "/tomorrow/getAggregatorApplyOfferResp",
    params,
  });
}

export function getPriceByResourceTypeId(params) {
  return request({
    method: "get",
    url: "/tomorrow/getPriceByResourceTypeId",
    params,
  });
}

export function saveAggregatorApplyOffer(data) {
  return request({
    method: "post",
    url: "/tomorrow/saveAggregatorApplyOffer",
    data,
  });
}

export function submitAggregatorApplyOffer(data) {
  return request({
    method: "post",
    url: "/tomorrow/submitAggregatorApplyOffer",
    data,
  });
}

export function getAggregatorDeliveryChart(params) {
  return request({
    method: "get",
    url: "/tomorrow/getAggregatorDeliveryChart",
    params,
  });
}

// ==================== 企业用户详情 ====================
export function getEntUserOptions(params) {
  return request({
    method: "get",
    url: "/entUserDetail/options",
    params,
  });
}

export function getEntUserDetailRespList(params) {
  return request({
    method: "get",
    url: "/entUserDetail/getEntUserDetailRespList",
    params,
  });
}

export function getEntUserDetailList(params) {
  return request({
    method: "get",
    url: "/entUserDetail/list",
    params,
  });
}

export function getEntUserDetailListV2(params) {
  return request({
    method: "get",
    url: "/entUserDetail/listV2",
    params,
  });
}

export function getEntUserDetailPercentOptions(params) {
  return request({
    method: "get",
    url: "/entUserDetail/percent/options",
    params,
  });
}

export function autoUpdateEnt(data) {
  return request({
    method: "post",
    url: "/entUserDetail/autoUpdateEnt",
    data,
  });
}

export function getCimDeviceList(params) {
  return request({
    method: "get",
    url: "/entUserDetail/getCimDeviceList",
    params,
  });
}

export function updateEnt(data) {
  return request({
    method: "post",
    url: "/entUserDetail/updateEnt",
    data,
  });
}

// ==================== 申报计划 ====================
export function getApplyDateList(params) {
  return request({
    method: "get",
    url: "/applyPlan/getApplyDateList",
    params,
  });
}

export function getPlanList(data) {
  return request({
    method: "post",
    url: "/aggregatorPlan/getPlanList",
    data,
  });
}

export function queryReferenceDailyPower(data) {
  return request({
    method: "post",
    url: "/aggregatorPlan/getReferDatePower",
    data,
  });
}

export function addEditPlan(data) {
  return request({
    method: "post",
    url: "/aggregatorPlan/addOrUpdatePlan",
    data,
  });
}

export function getPlanDetail(params) {
  return request({
    method: "get",
    url: "/aggregatorPlan/getPlanDetail",
    params,
  });
}

export function getPlanDistribution(params) {
  return request({
    method: "get",
    url: "/aggregatorPlan/getRunPlan",
    params,
  });
}

// ==================== 天气 ====================
export function getDayWeather(data) {
  return request({
    method: "post",
    url: "/weather/getDayWeather",
    data,
  });
}

// ==================== 文件上传 ====================
export const uploadUrl = "/file/uploadFile";

export function uploadFile(file) {
  const formData = new FormData();
  formData.append("file", file);
  return request({
    method: "post",
    url: "/file/uploadFile",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}

// ==================== 峰值计划申报 ====================
export function submitPredictionData(data) {
  return request({
    method: "post",
    url: "/peakPlanDeclare/import",
    data,
  });
}

// ==================== 操作日志（可选，保留原有实现或移除） ====================
export function doSaveOperation(params) {
  // 这个接口是埋点接口，可以考虑：
  // 1. 在前端直接调用第三方埋点服务
  // 2. 在Console实现一个简单的代理
  // 3. 暂时不实现，返回成功
  console.log("操作日志:", params);
  return Promise.resolve({ code: 200, message: "success" });
}

// ==================== WebSocket（如果需要） ====================
// WebSocket连接需要根据实际情况配置
export const websocketUrl = window.location.protocol === "https:"
  ? `wss://${window.location.host}/ws`
  : `ws://${window.location.host}/ws`;

// 导出baseUrl用于兼容
export const baseUrl = "";
export const accessKeyValue = ""; // 不再需要AccessKey
