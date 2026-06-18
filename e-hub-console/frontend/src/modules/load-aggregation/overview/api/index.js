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

export const websocketUrl =
  runtimeConfig.websocketUrl || envConfig.VITE_WS_URL || (commonUtil.currentENV() === "isTest"
    ? "ws://fnw-socket.cloud-common.fnwrancher-dev.enncloud.cn/webSocket/"
    : "wss://fnw-socket.fanneng.com/webSocket/");
export const baseUrl = urlStr;
export const accessKeyValue = accessKey;
export const uploadUrl = `${urlStr}/file/uploadFile`;
export function getDayWeather(params, simulate) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/weather/getDayWeather`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
export function getContentList(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/profit/getContentList`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
export function getPriceByResourceTypeId(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/tomorrow/getPriceByResourceTypeId`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
export function getApplyDateList(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/applyPlan/getApplyDateList`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
export function getAggregatorDeliveryChart(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/tomorrow/getAggregatorDeliveryChart`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

//
export function getResourceTypeList(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/yesterday/getResourceTypeList`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

export function getLastProfit(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/yesterday/getLastProfit`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

// 实时汇总
export function getOverview(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/yesterday/getOverview`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
// 查询申报
export function getAggregatorApply(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/tomorrow/getAggregatorApply`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

export function getWeekProfit(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/profit/week`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
// 查询报价
export function getAggregatorApplyOfferResp(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/tomorrow/getAggregatorApplyOfferResp`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
// 暂存报价
export function saveAggregatorApplyOffer(params, simulate) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/tomorrow/saveAggregatorApplyOffer`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
// 提交报价
export function submitAggregatorApplyOffer(params, simulate) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/tomorrow/submitAggregatorApplyOffer`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
export function getEntUserOverviewResp(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/yesterday/getEntUserOverviewResp`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

//  查询设备列表
export function getDeviceList(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/yesterday/getDeviceList`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
//
export function getEntUserDetailRespList(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/entUserDetail/getEntUserDetailRespList`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

//
export function getEntUserDeviceChartResp(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/yesterday/getEntUserDeviceChartResp`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

export function getTodayEntUserDeviceChartResp(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/today/getEntUserDeviceChartResp`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
export function getTomorrowEntUserDeviceChartResp(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/tomorrow/getEntUserDeviceChartResp`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
export function updateAggregatorApply(params, simulate) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/tomorrow/updateAggregatorApply`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
export function getIotLog(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/today/getIotLog`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
//  获取企业用户选项列表
export function getEntUserOptions(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/entUserDetail/options`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
// 无筛选条件
export function getProfitList(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/profit/list`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
// 有筛选条件
export function getListByEntIdList(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/profit/listByEntIdList`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}
export function getEntUserDetailList(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/entUserDetail/list`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

export function getEntUserDetailPercentOptions(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/entUserDetail/percent/options`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

export function entInvite(params, simulate) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/yesterday/entInvite`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

export function getEntUserDetailListV2(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/entUserDetail/listV2`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

export function autoUpdateEnt(params, simulate) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/entUserDetail/autoUpdateEnt`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

export function getCimDeviceList(params, simulate) {
  return service({
    method: "get",
    params,
    url: `${baseUrl}/entUserDetail/getCimDeviceList`,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  });
}

export function updateEnt(params, simulate) {
  return service({
    method: "post",
    data: params,
    url: `${baseUrl}/entUserDetail/updateEnt`,
    headers: {
      simulate,
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
  if (!params.equipType) {
    params.equipType = "pc"; // app--app组件/pc--pc组件   各自组件维护
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

// 申报计划 - 获取申报列表
export function getPlanList (params, simulate) {
  return service({
    url: `${baseUrl}/aggregatorPlan/getPlanList`,
    method: "post",
    data: params,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  })
}

// 申报计划 - 查询参考日功率
export function queryReferenceDailyPower (params, simulate) {
  return service({
    url: `${baseUrl}/aggregatorPlan/getReferDatePower`,
    method: "post",
    data: params,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  })
}

// 申报计划 - 新增/编辑申报计划
export function addEditPlan (params, simulate) {
  return service({
    url: `${baseUrl}/aggregatorPlan/addOrUpdatePlan`,
    method: "post",
    data: params,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  })
}

// 申报计划 - 查询计划详情
export function getPlanDetail (params, simulate) {
  return service({
    url: `${baseUrl}/aggregatorPlan/getPlanDetail`,
    method: "get",
    params,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  })
}

// 实时汇总/申报计划 - 详情 - 获取企业用户树结构
export function getEntUserTree (params, simulate) {
  return service({
    url: `${baseUrl}/today/get/device/tree`,
    method: "get",
    params,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  })
}

// 实时汇总/申报计划 - 详情 - 获取企业用户树结构
export function getRealTimeSummaryEcharts4 (params, simulate) {
  return service({
    url: `${baseUrl}/today/getMultiDeviceChartResp`,
    method: "get",
    params,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  })
}

// 查询是否有计划下发
export function getPlanDistribution (params, simulate) {
  return service({
    url: `${baseUrl}/aggregatorPlan/getRunPlan`,
    method: "get",
    params,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  })
}

// 预测数据上报
export function submitPredictionData (params, simulate) {
  return service({
    url: `${baseUrl}/peakPlanDeclare/import`,
    method: "post",
    data: params,
    headers: {
      simulate,
      ticket: sessionStorage.getItem("ticket"),
      "X-GW-AccessKey": accessKey,
    },
  })
}
