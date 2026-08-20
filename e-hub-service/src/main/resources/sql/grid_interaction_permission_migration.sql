-- 电网上送核查升级为电网交互。兼容 MySQL 5.7，可重复执行。
INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'load:grid-interaction:view', '负荷聚合电网交互', 'PAGE', 'load', NULL, 'load-grid-interaction', 85, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'load:grid-interaction:view');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'load:grid-interaction:delivery', '电网交互上送操作', 'ACTION', 'load', 'load:grid-interaction:view', NULL, 86, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'load:grid-interaction:delivery');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'load:grid-interaction:audit', '电网上送核查处理', 'ACTION', 'load', 'load:grid-interaction:view', NULL, 87, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'load:grid-interaction:audit');

INSERT INTO console_permission (permission_code, permission_name, permission_type, module_code, parent_code, path, sort_no, status, create_time, update_time)
SELECT 'load:grid-interaction:export', '电网上送核查导出', 'ACTION', 'load', 'load:grid-interaction:view', NULL, 88, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM console_permission WHERE permission_code = 'load:grid-interaction:export');

-- 将已有旧权限角色平移到新权限，旧编码继续保留用于滚动发布兼容。
INSERT INTO console_role_permission (role_id, permission_code, create_time, update_time)
SELECT DISTINCT old_rp.role_id, mapping.new_code, NOW(), NOW()
FROM console_role_permission old_rp
JOIN (
  SELECT 'load:grid-delivery:view' old_code, 'load:grid-interaction:view' new_code
  UNION ALL SELECT 'load:grid-delivery:manage', 'load:grid-interaction:delivery'
  UNION ALL SELECT 'load:grid-delivery:manage', 'load:grid-interaction:audit'
  UNION ALL SELECT 'load:grid-delivery:export', 'load:grid-interaction:export'
) mapping ON mapping.old_code = old_rp.permission_code
LEFT JOIN console_role_permission new_rp
  ON new_rp.role_id = old_rp.role_id AND new_rp.permission_code = mapping.new_code
WHERE new_rp.role_id IS NULL;
