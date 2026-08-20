INSERT INTO console_role (role_id, role_name, role_code, platform_type, status, create_time, update_time)
SELECT 'ROLE_OWNER_ADMIN', '平台管理员', 'owner_admin', 'owner', 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_role WHERE role_code = 'owner_admin' AND platform_type = 'owner');

INSERT INTO console_role (role_id, role_name, role_code, platform_type, status, create_time, update_time)
SELECT 'ROLE_TENANT_OPERATOR', '租户运营', 'tenant_operator', 'owner', 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_role WHERE role_code = 'tenant_operator' AND platform_type = 'owner');

INSERT INTO console_role (role_id, role_name, role_code, platform_type, status, create_time, update_time)
SELECT 'ROLE_TENANT_VIEWER', '租户查看', 'tenant_viewer', 'owner', 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_role WHERE role_code = 'tenant_viewer' AND platform_type = 'owner');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'owner:tenant:manage', '租户中心管理', 'ACTION', 'tenant', NULL, '/tenant/**', 10, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'owner:tenant:manage');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'owner:access:manage', '身份权限管理', 'ACTION', 'access', NULL, '/console-user/**,/permission/**', 20, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'owner:access:manage');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'owner:settings:manage', '平台设置管理', 'ACTION', 'platform', NULL, '/platform/**', 30, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'owner:settings:manage');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'load:overview:view', '负荷聚合运营总览', 'PAGE', 'load', NULL, 'load-overview', 40, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'load:overview:view');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'load:adjustment:view', '负荷聚合调节情况', 'PAGE', 'load', NULL, 'load-adjustment', 50, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'load:adjustment:view');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'load:settlement:view', '负荷聚合收益结算', 'PAGE', 'load', NULL, 'load-settlement', 60, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'load:settlement:view');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'load:resources:view', '负荷聚合资源管理', 'PAGE', 'load', NULL, 'load-resources', 70, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'load:resources:view');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'load:device-operation:view', '负荷聚合物联管理', 'PAGE', 'load', NULL, 'load-device-operation', 80, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'load:device-operation:view');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'load:grid-interaction:view', '负荷聚合电网交互', 'PAGE', 'load', NULL, 'load-grid-interaction', 85, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'load:grid-interaction:view');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'load:grid-interaction:delivery', '电网交互上送操作', 'ACTION', 'load', 'load:grid-interaction:view', NULL, 86, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'load:grid-interaction:delivery');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'load:grid-interaction:audit', '电网上送核查处理', 'ACTION', 'load', 'load:grid-interaction:view', NULL, 87, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'load:grid-interaction:audit');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'load:grid-interaction:export', '电网上送核查导出', 'ACTION', 'load', 'load:grid-interaction:view', NULL, 88, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'load:grid-interaction:export');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'tariff:query:view', '电价服务代理价格', 'PAGE', 'tariff', NULL, 'tariff-query', 90, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'tariff:query:view');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'tariff:import:manage', '电价服务电价录入', 'PAGE', 'tariff', NULL, 'tariff-import', 92, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'tariff:import:manage');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'tariff:sources:view', '电价服务数据来源', 'PAGE', 'tariff', NULL, 'tariff-sources', 95, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'tariff:sources:view');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'tariff:api:view', '电价服务接口能力', 'PAGE', 'tariff', NULL, 'tariff-api', 100, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'tariff:api:view');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'tariff:logs:view', '电价服务调用记录', 'PAGE', 'tariff', NULL, 'tariff-logs', 110, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'tariff:logs:view');

INSERT INTO console_role_permission (role_id, permission_code, create_time, update_time)
SELECT 'ROLE_OWNER_ADMIN', p.permission_code, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
FROM console_permission p
LEFT JOIN console_role_permission rp ON rp.role_id = 'ROLE_OWNER_ADMIN' AND rp.permission_code = p.permission_code
WHERE rp.id IS NULL;
