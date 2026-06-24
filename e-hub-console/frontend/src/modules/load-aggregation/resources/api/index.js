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

export function listPoints(deviceId) {
  return service({
    method: "get",
    url: `/iot/devices/${deviceId}/points`,
  });
}

export function createPoint(deviceId, data) {
  return service({
    method: "post",
    url: `/iot/devices/${deviceId}/points`,
    data,
    headers: JSON_HEADERS,
  });
}

export function updatePoint(id, data) {
  return service({
    method: "put",
    url: `/iot/points/${id}`,
    data,
    headers: JSON_HEADERS,
  });
}

export function deletePoint(id) {
  return service({
    method: "delete",
    url: `/iot/points/${id}`,
  });
}

export function listDeviceExternalRefs(deviceId) {
  return service({
    method: "get",
    url: `/iot/devices/${deviceId}/external-refs`,
  });
}

export function createDeviceExternalRef(deviceId, data) {
  return service({
    method: "post",
    url: `/iot/devices/${deviceId}/external-refs`,
    data,
    headers: JSON_HEADERS,
  });
}

export function updateDeviceExternalRef(id, data) {
  return service({
    method: "put",
    url: `/iot/device-external-refs/${id}`,
    data,
    headers: JSON_HEADERS,
  });
}

export function disableDeviceExternalRef(id) {
  return service({
    method: "delete",
    url: `/iot/device-external-refs/${id}`,
  });
}

export function listPointExternalRefs(pointId) {
  return service({
    method: "get",
    url: `/iot/points/${pointId}/external-refs`,
  });
}

export function createPointExternalRef(pointId, data) {
  return service({
    method: "post",
    url: `/iot/points/${pointId}/external-refs`,
    data,
    headers: JSON_HEADERS,
  });
}

export function updatePointExternalRef(id, data) {
  return service({
    method: "put",
    url: `/iot/point-external-refs/${id}`,
    data,
    headers: JSON_HEADERS,
  });
}

export function disablePointExternalRef(id) {
  return service({
    method: "delete",
    url: `/iot/point-external-refs/${id}`,
  });
}

export function listTelemetryMinute(params) {
  return service({
    method: "get",
    url: "/iot/telemetry/minute",
    params,
  });
}

export function listUnmatchedTelemetry(params) {
  return service({
    method: "get",
    url: "/iot/unmatched-telemetry",
    params,
  });
}
