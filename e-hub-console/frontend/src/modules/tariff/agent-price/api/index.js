import service from "@/services/http";

const JSON_HEADERS = {
  "Content-Type": "application/json;charset=UTF-8",
};

export function getAgentPriceOptions(params) {
  return service({
    method: "get",
    url: "/tariff/agent-price/options",
    params,
  });
}

export function queryAgentPrices(data) {
  return service({
    method: "post",
    url: "/tariff/agent-price/prices",
    data,
    headers: JSON_HEADERS,
  });
}
