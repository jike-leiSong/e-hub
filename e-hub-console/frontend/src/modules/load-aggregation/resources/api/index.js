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
    url: "/iot/devices",
    params,
  });
}

export function createDevice(data) {
  return service({
    method: "post",
    url: "/iot/devices",
    data,
    headers: JSON_HEADERS,
  });
}

export function updateDevice(id, data) {
  return service({
    method: "put",
    url: `/iot/devices/${id}`,
    data,
    headers: JSON_HEADERS,
  });
}

export function deleteDevice(id) {
  return service({
    method: "delete",
    url: `/iot/devices/${id}`,
  });
}
