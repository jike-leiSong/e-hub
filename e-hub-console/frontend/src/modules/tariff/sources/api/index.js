import service from "@/services/http";

const JSON_HEADERS = {
  "Content-Type": "application/json;charset=UTF-8",
};

export function listSourceConfigs(params) {
  return service({
    method: "get",
    url: "/tariff/sources/configs",
    params,
  });
}

export function createSourceConfig(data) {
  return service({
    method: "post",
    url: "/tariff/sources/configs",
    data,
    headers: JSON_HEADERS,
  });
}

export function updateSourceConfig(id, data) {
  return service({
    method: "put",
    url: `/tariff/sources/configs/${id}`,
    data,
    headers: JSON_HEADERS,
  });
}

export function setSourceConfigEnabled(id, enabled) {
  return service({
    method: "put",
    url: `/tariff/sources/configs/${id}/enabled`,
    params: { enabled },
  });
}

export function listSourceDocuments(params) {
  return service({
    method: "get",
    url: "/tariff/sources/documents",
    params,
  });
}

export function createSourceDocument(data) {
  return service({
    method: "post",
    url: "/tariff/sources/documents",
    data,
    headers: JSON_HEADERS,
  });
}

export function updateSourceDocument(id, data) {
  return service({
    method: "put",
    url: `/tariff/sources/documents/${id}`,
    data,
    headers: JSON_HEADERS,
  });
}

export function updateSourceDocumentStatus(id, status) {
  return service({
    method: "put",
    url: `/tariff/sources/documents/${id}/status`,
    params: { status },
  });
}

export function archiveSourceDocument(id) {
  return service({
    method: "delete",
    url: `/tariff/sources/documents/${id}`,
  });
}
