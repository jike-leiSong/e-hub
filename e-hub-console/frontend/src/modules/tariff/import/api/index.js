import service from "@/services/http";

const JSON_HEADERS = {
  "Content-Type": "application/json;charset=UTF-8",
};

export function previewTariffRuleImport(data) {
  return service({
    method: "post",
    url: "/tariff/agent-price/import/rule/preview",
    data,
    headers: JSON_HEADERS,
  });
}

export function publishTariffRuleImport(data) {
  return service({
    method: "post",
    url: "/tariff/agent-price/import/rule/publish",
    data,
    headers: JSON_HEADERS,
  });
}

export function deleteTariffRuleImport(data) {
  return service({
    method: "post",
    url: "/tariff/agent-price/import/rule/delete",
    data,
    headers: JSON_HEADERS,
  });
}

export function copyTariffRuleImport(data) {
  return service({
    method: "post",
    url: "/tariff/agent-price/import/rule/copy",
    data,
    headers: JSON_HEADERS,
  });
}

export function getTariffDictByType(data) {
  return service({
    method: "post",
    url: "/areaDict/getDictByType",
    data,
    headers: JSON_HEADERS,
  });
}

export function getTariffAreaOptions(params) {
  return service({
    method: "get",
    url: "/tariff/agent-price/options",
    params,
  });
}
