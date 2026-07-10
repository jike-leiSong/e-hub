import service from "@/services/http";

const JSON_HEADERS = {
  "Content-Type": "application/json",
};

export function listEnterprises(params) {
  return service({
    method: "get",
    url: "/ent/list",
    params,
  });
}

export function listDeviceGroups(params) {
  return service({
    method: "get",
    url: "/iot/manage/device-groups",
    params,
  });
}

export function getDeviceGroupDetail(id) {
  return service({
    method: "get",
    url: `/iot/manage/device-groups/${id}`,
  });
}

export function createDeviceGroup(data) {
  return service({
    method: "post",
    url: "/iot/manage/device-groups",
    data,
    headers: JSON_HEADERS,
  });
}

export function updateDeviceGroup(id, data) {
  return service({
    method: "put",
    url: `/iot/manage/device-groups/${id}`,
    data,
    headers: JSON_HEADERS,
  });
}

export function deleteDeviceGroup(id) {
  return service({
    method: "delete",
    url: `/iot/manage/device-groups/${id}`,
  });
}

export function listGateways(params) {
  return service({
    method: "get",
    url: "/iot/manage/gateways",
    params,
  });
}

export function listDevices(params) {
  return service({
    method: "get",
    url: "/iot/manage/devices",
    params,
  });
}

export function listProjectsByEnt(entId, aggregatorId) {
  return service({
    method: "get",
    url: "/model/listByEnt",
    params: { entId, aggregatorId },
  });
}

export function getDeviceDetail(id) {
  return service({
    method: "get",
    url: `/iot/manage/devices/${id}`,
  });
}

export function createDevice(data) {
  return service({
    method: "post",
    url: "/iot/manage/devices",
    data,
    headers: JSON_HEADERS,
  });
}

export function updateDevice(id, data) {
  return service({
    method: "put",
    url: `/iot/manage/devices/${id}`,
    data,
    headers: JSON_HEADERS,
  });
}

export function deleteDevice(id) {
  return service({
    method: "delete",
    url: `/iot/manage/devices/${id}`,
  });
}

export function listDeviceTypes() {
  return service({
    method: "get",
    url: "/iot/manage/options/device-types",
  });
}

export function listCommunicationMethods() {
  return service({
    method: "get",
    url: "/iot/manage/options/communication-methods",
  });
}

export function listDeviceGroupTypes() {
  return service({
    method: "get",
    url: "/iot/manage/options/device-group-types",
  });
}

export function listEnergyTypes() {
  return service({
    method: "get",
    url: "/iot/manage/options/energy-types",
  });
}

export function listCarriers() {
  return service({
    method: "get",
    url: "/iot/manage/options/carriers",
  });
}

export function listThirdPartyApis() {
  return service({
    method: "get",
    url: "/iot/manage/options/third-party-apis",
  });
}

export function getDeviceTypeParamMetadata(deviceTypeCode) {
  return service({
    method: "get",
    url: "/iot/manage/device-type-param-metadata",
    params: { deviceTypeCode },
  });
}

export function getDeviceTypePointMetadata(deviceTypeCode) {
  return service({
    method: "get",
    url: "/iot/manage/device-type-point-metadata",
    params: { deviceTypeCode },
  });
}

export function getDeviceGroupParamMetadata() {
  return service({
    method: "get",
    url: "/iot/manage/device-group-param-metadata",
  });
}

export function listDevicePoints(deviceId, params) {
  return service({
    method: "get",
    url: `/iot/manage/devices/${deviceId}/points/page`,
    params,
  });
}

export function listAvailablePoints(deviceId) {
  return service({
    method: "get",
    url: `/iot/manage/devices/${deviceId}/available-points`,
  });
}

export function batchAddDevicePoints(deviceId, data) {
  return service({
    method: "post",
    url: `/iot/manage/devices/${deviceId}/points/batch`,
    data,
    headers: JSON_HEADERS,
  });
}

export function deleteDevicePoint(id) {
  return service({
    method: "delete",
    url: `/iot/manage/points/${id}`,
  });
}

export function listDevicePointDefinitions(pointId) {
  return service({
    method: "get",
    url: `/iot/manage/points/${pointId}/definitions`,
  });
}

export function createDevicePointDefinition(pointId, data) {
  return service({
    method: "post",
    url: `/iot/manage/points/${pointId}/definitions`,
    data,
    headers: JSON_HEADERS,
  });
}

export function updateDevicePointDefinition(id, data) {
  return service({
    method: "put",
    url: `/iot/manage/point-definitions/${id}`,
    data,
    headers: JSON_HEADERS,
  });
}

export function deleteDevicePointDefinition(id) {
  return service({
    method: "delete",
    url: `/iot/manage/point-definitions/${id}`,
  });
}

export function getDeviceGroupPointMetadata(params) {
  return service({
    method: "get",
    url: "/iot/manage/device-group-point-metadata",
    params,
  });
}

export function listDeviceGroupPointMetadata(deviceGroupType) {
  return service({
    method: "get",
    url: "/iot/manage/device-group-point-metadata-list",
    params: { deviceGroupType },
  });
}

export function listDeviceGroupPoints(groupId) {
  return service({
    method: "get",
    url: `/iot/manage/device-groups/${groupId}/points`,
  });
}

export function listDeviceGroupPointDefinitions(pointId) {
  return service({
    method: "get",
    url: `/iot/manage/group-points/${pointId}/definitions`,
  });
}

export function createDeviceGroupPointDefinition(pointId, data) {
  return service({
    method: "post",
    url: `/iot/manage/group-points/${pointId}/definitions`,
    data,
    headers: JSON_HEADERS,
  });
}

export function updateDeviceGroupPointDefinition(id, data) {
  return service({
    method: "put",
    url: `/iot/manage/group-point-definitions/${id}`,
    data,
    headers: JSON_HEADERS,
  });
}

export function deleteDeviceGroupPointDefinition(id) {
  return service({
    method: "delete",
    url: `/iot/manage/group-point-definitions/${id}`,
  });
}

// ==================== 物联时序数据查询 ====================

function normalizeTelemetryParams(params = {}) {
  const normalized = { ...params };
  ["deviceIds", "pointCodes"].forEach(key => {
    if (Array.isArray(normalized[key])) {
      normalized[key] = normalized[key].join(",");
    }
  });
  return normalized;
}

export function queryTelemetryData(params) {
  return service({
    method: "get",
    url: "/iot/telemetry/data",
    params: normalizeTelemetryParams(params),
  });
}

export function queryTelemetryRaw(params) {
  return service({
    method: "get",
    url: "/iot/telemetry/raw",
    params: normalizeTelemetryParams(params),
  });
}

export function getDeviceSummary(params) {
  return service({
    method: "get",
    url: "/iot/telemetry/device-summary",
    params: normalizeTelemetryParams(params),
  });
}

export function generatePowerData(data) {
  return service({
    method: "post",
    url: "/iot/mock/power-data",
    data,
    headers: JSON_HEADERS,
  });
}
