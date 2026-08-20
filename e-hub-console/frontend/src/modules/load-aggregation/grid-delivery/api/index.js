import service from "@/services/http";

const GRID_API = "/grid-interaction";

export function getResourceTypes(params) {
  return service({ method: "get", url: "/yesterday/getResourceTypeList", params });
}

export function getSummary(params) {
  return service({ method: "get", url: `${GRID_API}/summary`, params, timeout: 30000, retry: 0 });
}

export function getDailyOverview(params) {
  return service({ method: "get", url: `${GRID_API}/daily-overview`, params, timeout: 30000, retry: 0 });
}

export function getPeakPlanStatus(params) {
  return service({ method: "get", url: `${GRID_API}/peak-plan/status`, params, timeout: 30000, retry: 0 });
}

export function getDaily(params) {
  return service({ method: "get", url: `${GRID_API}/daily`, params });
}

export function getDailyPage(params) {
  return service({ method: "get", url: `${GRID_API}/daily-page`, params, timeout: 30000, retry: 0 });
}

export function getReconciliation(params) {
  return service({ method: "get", url: `${GRID_API}/reconciliation`, params });
}

export function getParticipation(params) {
  return service({ method: "get", url: `${GRID_API}/participation`, params });
}

export function createSnapshot(params) {
  return service({ method: "post", url: `${GRID_API}/snapshot`, params });
}

export function exportDaily(params) {
  return service({
    method: "get",
    url: `${GRID_API}/export`,
    params,
    responseType: "blob",
  });
}

export function getTrend(params) {
  return service({ method: "get", url: `${GRID_API}/trend`, params });
}

export function getIssues(params) {
  return service({ method: "get", url: `${GRID_API}/issues`, params });
}

export function getIssueDetail(id, params) {
  return service({ method: "get", url: `${GRID_API}/issues/${id}`, params });
}

export function updateIssue(id, params) {
  return service({ method: "put", url: `${GRID_API}/issues/${id}`, params });
}

export function recalculate(params) {
  return service({ method: "post", url: `${GRID_API}/recalculate`, params });
}

export function getSnapshots(params) {
  return service({ method: "get", url: `${GRID_API}/snapshots`, params });
}

export function getSnapshotDetail(id, params) {
  return service({ method: "get", url: `${GRID_API}/snapshots/${id}`, params });
}

export function updateSnapshot(id, params) {
  return service({ method: "put", url: `${GRID_API}/snapshots/${id}`, params });
}

export function exportReport(params) {
  return service({ method: "get", url: `${GRID_API}/report`, params, responseType: "blob" });
}

export function createExportTask(params) {
  return service({ method: "post", url: `${GRID_API}/export-tasks`, params });
}

export function getExportTasks(params) {
  return service({ method: "get", url: `${GRID_API}/export-tasks`, params });
}

export function downloadExportTask(taskNo, params) {
  return service({ method: "get", url: `${GRID_API}/export-tasks/${taskNo}/download`, params, responseType: "blob" });
}

export function getPeriods(params) {
  return service({ method: "get", url: `${GRID_API}/periods`, params });
}

export function createPeriod(params) {
  return service({ method: "post", url: `${GRID_API}/periods`, params });
}

export function updatePeriod(id, params) {
  return service({ method: "put", url: `${GRID_API}/periods/${id}`, params });
}

export function getModelPreview(params) {
  return service({ method: "get", url: `${GRID_API}/operations/model-preview`, params });
}

export function sendSingleModel(params) {
  return service({ method: "post", url: `${GRID_API}/operations/send-model`, params, timeout: 140000, retry: 0 });
}

export function retrySingleMeasurement(params) {
  return service({ method: "post", url: `${GRID_API}/operations/retry-single`, params, timeout: 140000, retry: 0 });
}

export function retrySingleMeasurementRange(params) {
  return service({ method: "post", url: `${GRID_API}/operations/retry-single-range`, params, timeout: 300000, retry: 0 });
}

export function getMarketStatus(params) {
  return service({ method: "get", url: `${GRID_API}/market-status`, params });
}

export function updateMarketStatus(params) {
  return service({ method: "post", url: `${GRID_API}/market-status`, params, retry: 0 });
}

export function getConnectionOverview(params) {
  return service({ method: "get", url: `${GRID_API}/connection-overview`, params, timeout: 30000, retry: 0 });
}

export function getOperationRecords(params) {
  return service({ method: "get", url: `${GRID_API}/operations`, params });
}
