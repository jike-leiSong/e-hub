import service from "@/services/http";

const JSON_HEADERS = {
  "Content-Type": "application/json;charset=UTF-8",
};

export function listEnterprises(params) {
  return service({
    method: "get",
    url: "/ent/page",
    params,
  });
}

export function createEnterprise(data) {
  return service({
    method: "post",
    url: "/ent",
    data,
    headers: JSON_HEADERS,
  });
}

export function updateEnterprise(entId, data) {
  return service({
    method: "put",
    url: `/ent/${entId}`,
    data,
    headers: JSON_HEADERS,
  });
}

export function disableEnterprise(entId) {
  return service({
    method: "delete",
    url: `/ent/${entId}`,
  });
}

export function listDevices(params) {
  return service({
    method: "get",
    url: "/ent-device/page",
    params,
  });
}

export function createDevice(data) {
  return service({
    method: "post",
    url: "/ent-device",
    data,
    headers: JSON_HEADERS,
  });
}

export function updateDevice(id, data) {
  return service({
    method: "put",
    url: `/ent-device/${id}`,
    data,
    headers: JSON_HEADERS,
  });
}

export function deleteDevice(id) {
  return service({
    method: "delete",
    url: `/ent-device/${id}`,
  });
}

export function listModels(params) {
  return service({
    method: "get",
    url: "/model/page",
    params,
  });
}

export function createModel(data) {
  return service({
    method: "post",
    url: "/model",
    data,
    headers: JSON_HEADERS,
  });
}

export function updateModel(id, data) {
  return service({
    method: "put",
    url: `/model/${id}`,
    data,
    headers: JSON_HEADERS,
  });
}

export function deleteModel(id) {
  return service({
    method: "delete",
    url: `/model/${id}`,
  });
}

export function listProjectsByEnt(entId) {
  return service({
    method: "get",
    url: "/model/listByEnt",
    params: { entId },
  });
}

export function listResourceTypes(params) {
  return service({
    method: "get",
    url: "/yesterday/getResourceTypeList",
    params,
  });
}
