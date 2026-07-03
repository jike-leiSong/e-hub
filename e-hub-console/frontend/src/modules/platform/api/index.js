import service from "@/services/http"

function unwrap(response) {
  const body = response && response.data ? response.data : {}
  if (body.code !== 200) {
    throw new Error(body.msg || "请求失败")
  }
  return body.data
}

function get(url, params) {
  return service({
    method: "get",
    url,
    params,
  }).then(unwrap)
}

function post(url, data) {
  return service({
    method: "post",
    url,
    data,
  }).then(unwrap)
}

function put(url, data) {
  return service({
    method: "put",
    url,
    data,
  }).then(unwrap)
}

export function fetchWorkbenchSummary() {
  return get("/platform/workbench/summary")
}

export function fetchWorkbenchTodos() {
  return get("/platform/workbench/todos")
}

export function fetchWorkbenchRecentLogs(limit = 8) {
  return get("/platform/workbench/recent-logs", { limit })
}

export function fetchProductOptions() {
  return get("/product/options")
}

export function fetchTenantPage(params) {
  return get("/tenant/page", params)
}

export function fetchTenantDetail(tenantId) {
  return get(`/tenant/${encodeURIComponent(tenantId)}`)
}

export function createTenant(data) {
  return post("/tenant", data)
}

export function updateTenant(tenantId, data) {
  return put(`/tenant/${encodeURIComponent(tenantId)}`, data)
}

export function updateTenantStatus(tenantId, status) {
  return put(`/tenant/${encodeURIComponent(tenantId)}/status`, { status })
}

export function saveTenantProducts(tenantId, products) {
  return put(`/tenant/${encodeURIComponent(tenantId)}/products`, {
    products,
  })
}

export function fetchConsoleUserPage(params) {
  return get("/console-user/page", params)
}

export function createConsoleUser(data) {
  return post("/console-user", data)
}

export function updateConsoleUser(userId, data) {
  return put(`/console-user/${encodeURIComponent(userId)}`, data)
}

export function updateConsoleUserStatus(userId, status) {
  return put(`/console-user/${encodeURIComponent(userId)}/status`, { status })
}

export function saveConsoleUserRoles(userId, roleIds) {
  return put(`/console-user/${encodeURIComponent(userId)}/roles`, {
    roleIds,
  })
}

export function fetchRolePage(params) {
  return get("/permission/roles", params)
}

export function createRole(data) {
  return post("/permission/roles", data)
}

export function updateRole(roleId, data) {
  return put(`/permission/roles/${encodeURIComponent(roleId)}`, data)
}

export function fetchPermissionTree(params) {
  return get("/permission/tree", params)
}

export function saveRolePermissions(roleId, permissionCodes) {
  return put(`/permission/roles/${encodeURIComponent(roleId)}/permissions`, {
    permissionCodes,
  })
}

export function fetchConfigItems(params) {
  return get("/platform/config/items", params)
}

export function createConfigItem(data) {
  return post("/platform/config/items", data)
}

export function updateConfigItem(id, data) {
  return put(`/platform/config/items/${id}`, data)
}

export function fetchDictTypes() {
  return get("/platform/dict/types")
}

export function fetchDictItems(dictType) {
  return get("/platform/dict/items", { dictType })
}

export function fetchAuditLogs(params) {
  return get("/platform/audit/logs", params)
}
