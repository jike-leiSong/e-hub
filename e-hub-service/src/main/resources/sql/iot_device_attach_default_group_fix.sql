-- 修复已导入的本批物联设备：补默认网关/默认设备组，并把设备挂到默认设备组。
-- 适用场景：iot_device 已生成，但页面按 device_group_id 查询为空。

DROP TEMPORARY TABLE IF EXISTS tmp_iot_device_seed_ids;

CREATE TEMPORARY TABLE tmp_iot_device_seed_ids (
  id int NOT NULL PRIMARY KEY
) ENGINE=MEMORY;

INSERT INTO tmp_iot_device_seed_ids (id)
SELECT a.id
FROM aggregator_ent_device a
WHERE a.aggregator_id = '1711340903453614082'
  AND (
    a.id BETWEEN 52 AND 57
    OR a.id BETWEEN 72 AND 75
    OR a.id IN (111, 113)
    OR a.id BETWEEN 116 AND 117
    OR a.id BETWEEN 150 AND 153
    OR a.id BETWEEN 157 AND 160
    OR a.id BETWEEN 163 AND 170
    OR a.id BETWEEN 172 AND 178
    OR a.id BETWEEN 190 AND 192
    OR a.id BETWEEN 216 AND 224
    OR a.id BETWEEN 228 AND 231
    OR a.id BETWEEN 267 AND 274
    OR a.id IN (279, 281)
    OR a.id BETWEEN 283 AND 292
    OR a.id BETWEEN 304 AND 305
    OR a.id BETWEEN 307 AND 310
    OR a.id BETWEEN 334 AND 340
    OR a.id BETWEEN 343 AND 344
    OR a.id = 352
    OR a.id BETWEEN 413 AND 475
    OR a.id BETWEEN 486 AND 518
    OR a.id BETWEEN 520 AND 531
    OR a.id BETWEEN 533 AND 539
  );

START TRANSACTION;

INSERT INTO sys_dict (`dict_type_code`, `dict_type_name`, `dict_code`, `dict_value`, `sort`, `remark`, `create_time`, `update_time`, `deleted`)
SELECT 'device_group_type', '设备组类型', 'DEFAULT_GROUP', '默认设备组', 0, '物联默认设备组', NOW(), NOW(), 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1
  FROM sys_dict
  WHERE dict_type_code = 'device_group_type'
    AND dict_code = 'DEFAULT_GROUP'
    AND deleted = 0
);

INSERT INTO iot_gateway (
  aggregator_id,
  ent_id,
  gateway_code,
  gateway_name,
  status,
  default_flag,
  deleted,
  remark,
  create_time,
  update_time
)
SELECT
  b.aggregator_id,
  b.ent_id,
  CONCAT('GW', b.ent_id, '001') AS gateway_code,
  '默认网关' AS gateway_name,
  1 AS status,
  1 AS default_flag,
  0 AS deleted,
  '业务设备导入生成默认网关' AS remark,
  NOW() AS create_time,
  NOW() AS update_time
FROM (
  SELECT a.aggregator_id, a.ent_id
  FROM aggregator_ent_device a
  JOIN tmp_iot_device_seed_ids s ON s.id = a.id
  WHERE a.del_flag = 1
    AND a.ent_id IS NOT NULL
    AND a.ent_id <> ''
  GROUP BY a.aggregator_id, a.ent_id
) b
WHERE NOT EXISTS (
  SELECT 1
  FROM iot_gateway g
  WHERE g.ent_id = b.ent_id
    AND g.default_flag = 1
    AND g.deleted = 0
)
ON DUPLICATE KEY UPDATE
  aggregator_id = VALUES(aggregator_id),
  gateway_name = VALUES(gateway_name),
  status = 1,
  default_flag = 1,
  deleted = 0,
  remark = VALUES(remark),
  update_time = NOW();

INSERT INTO iot_device_group (
  aggregator_id,
  ent_id,
  device_group_code,
  device_group_name,
  device_group_type,
  device_group_type_name,
  gateway_id,
  virtual_flag,
  status,
  deleted,
  remark,
  create_time,
  update_time
)
SELECT
  b.aggregator_id,
  b.ent_id,
  'DEFAULTGROUP001' AS device_group_code,
  '默认设备组' AS device_group_name,
  'DEFAULT_GROUP' AS device_group_type,
  '默认设备组' AS device_group_type_name,
  gw.id AS gateway_id,
  1 AS virtual_flag,
  1 AS status,
  0 AS deleted,
  '业务设备导入生成默认设备组' AS remark,
  NOW() AS create_time,
  NOW() AS update_time
FROM (
  SELECT a.aggregator_id, a.ent_id
  FROM aggregator_ent_device a
  JOIN tmp_iot_device_seed_ids s ON s.id = a.id
  WHERE a.del_flag = 1
    AND a.ent_id IS NOT NULL
    AND a.ent_id <> ''
  GROUP BY a.aggregator_id, a.ent_id
) b
JOIN (
  SELECT ent_id, MIN(id) AS id
  FROM iot_gateway
  WHERE default_flag = 1
    AND deleted = 0
  GROUP BY ent_id
) gw ON gw.ent_id = b.ent_id
WHERE NOT EXISTS (
  SELECT 1
  FROM iot_device_group g
  WHERE g.ent_id = b.ent_id
    AND g.virtual_flag = 1
    AND g.deleted = 0
)
ON DUPLICATE KEY UPDATE
  aggregator_id = VALUES(aggregator_id),
  device_group_name = VALUES(device_group_name),
  device_group_type = VALUES(device_group_type),
  device_group_type_name = VALUES(device_group_type_name),
  gateway_id = VALUES(gateway_id),
  virtual_flag = 1,
  status = 1,
  deleted = 0,
  remark = VALUES(remark),
  update_time = NOW();

UPDATE iot_device_group g
JOIN (
  SELECT a.aggregator_id, a.ent_id
  FROM aggregator_ent_device a
  JOIN tmp_iot_device_seed_ids s ON s.id = a.id
  WHERE a.del_flag = 1
    AND a.ent_id IS NOT NULL
    AND a.ent_id <> ''
  GROUP BY a.aggregator_id, a.ent_id
) b ON b.ent_id = g.ent_id
JOIN (
  SELECT ent_id, MIN(id) AS id
  FROM iot_gateway
  WHERE default_flag = 1
    AND deleted = 0
  GROUP BY ent_id
) gw ON gw.ent_id = g.ent_id
SET
  g.aggregator_id = IF(g.aggregator_id IS NULL OR g.aggregator_id = '', b.aggregator_id, g.aggregator_id),
  g.device_group_type = 'DEFAULT_GROUP',
  g.device_group_type_name = '默认设备组',
  g.gateway_id = IF(g.gateway_id IS NULL, gw.id, g.gateway_id),
  g.status = 1,
  g.deleted = 0,
  g.update_time = NOW()
WHERE g.virtual_flag = 1
  AND g.deleted = 0;

INSERT INTO iot_device_group_param (
  device_group_id,
  attr_code,
  attr_name,
  sort,
  create_time,
  update_time
)
SELECT g.id, 'Heatcold', '供能类型', 1, NOW(), NOW()
FROM iot_device_group g
JOIN (
  SELECT a.ent_id
  FROM aggregator_ent_device a
  JOIN tmp_iot_device_seed_ids s ON s.id = a.id
  WHERE a.del_flag = 1
    AND a.ent_id IS NOT NULL
    AND a.ent_id <> ''
  GROUP BY a.ent_id
) b ON b.ent_id = g.ent_id
WHERE g.virtual_flag = 1
  AND g.deleted = 0
ON DUPLICATE KEY UPDATE
  attr_name = VALUES(attr_name),
  sort = VALUES(sort),
  update_time = NOW();

INSERT INTO iot_device_group_param (
  device_group_id,
  attr_code,
  attr_name,
  sort,
  create_time,
  update_time
)
SELECT g.id, 'Password', '一体机密码', 2, NOW(), NOW()
FROM iot_device_group g
JOIN (
  SELECT a.ent_id
  FROM aggregator_ent_device a
  JOIN tmp_iot_device_seed_ids s ON s.id = a.id
  WHERE a.del_flag = 1
    AND a.ent_id IS NOT NULL
    AND a.ent_id <> ''
  GROUP BY a.ent_id
) b ON b.ent_id = g.ent_id
WHERE g.virtual_flag = 1
  AND g.deleted = 0
ON DUPLICATE KEY UPDATE
  attr_name = VALUES(attr_name),
  sort = VALUES(sort),
  update_time = NOW();

-- 默认设备组不预置测点；若历史数据中已有默认组测点，这里软删除。
UPDATE iot_device_group_point p
JOIN iot_device_group g ON g.id = p.device_group_id
JOIN (
  SELECT a.ent_id
  FROM aggregator_ent_device a
  JOIN tmp_iot_device_seed_ids s ON s.id = a.id
  WHERE a.del_flag = 1
    AND a.ent_id IS NOT NULL
    AND a.ent_id <> ''
  GROUP BY a.ent_id
) b ON b.ent_id = g.ent_id
SET
  p.deleted = 1,
  p.update_time = NOW()
WHERE g.virtual_flag = 1
  AND g.deleted = 0
  AND p.deleted = 0;

UPDATE iot_device d
JOIN aggregator_ent_device a
  ON a.ent_id = d.ent_id
  AND a.device_id = d.device_code
JOIN tmp_iot_device_seed_ids s ON s.id = a.id
JOIN (
  SELECT ent_id, MIN(id) AS id
  FROM iot_device_group
  WHERE virtual_flag = 1
    AND deleted = 0
  GROUP BY ent_id
) dgx ON dgx.ent_id = a.ent_id
JOIN iot_device_group dg ON dg.id = dgx.id
LEFT JOIN (
  SELECT ent_id, MIN(id) AS id
  FROM iot_gateway
  WHERE default_flag = 1
    AND deleted = 0
  GROUP BY ent_id
) gw ON gw.ent_id = a.ent_id
SET
  d.device_group_id = dg.id,
  d.gateway_id = COALESCE(dg.gateway_id, gw.id),
  d.update_time = NOW()
WHERE a.del_flag = 1
  AND d.deleted = 0
  AND (
    d.device_group_id IS NULL
    OR d.device_group_id <> dg.id
    OR d.gateway_id IS NULL
    OR d.gateway_id <> COALESCE(dg.gateway_id, gw.id)
  );

UPDATE aggregator_ent_device a
JOIN tmp_iot_device_seed_ids s ON s.id = a.id
JOIN iot_device d
  ON d.ent_id = a.ent_id
  AND d.device_code = a.device_id
  AND d.deleted = 0
SET a.iot_device_base_id = CAST(d.id AS CHAR)
WHERE a.del_flag = 1
  AND (
    a.iot_device_base_id IS NULL
    OR a.iot_device_base_id = ''
    OR a.iot_device_base_id <> CAST(d.id AS CHAR)
  );

UPDATE iot_gateway gw
JOIN (
  SELECT gateway_id, COUNT(1) AS cnt
  FROM iot_device
  WHERE deleted = 0
    AND gateway_id IS NOT NULL
  GROUP BY gateway_id
) d ON d.gateway_id = gw.id
SET gw.bound_device_count = d.cnt,
    gw.update_time = NOW()
WHERE gw.deleted = 0;

COMMIT;

SELECT 'batch_business_device_count' AS check_item, COUNT(1) AS cnt
FROM tmp_iot_device_seed_ids;

SELECT 'iot_device_attached_default_group_count' AS check_item, COUNT(1) AS cnt
FROM iot_device d
JOIN aggregator_ent_device a
  ON a.ent_id = d.ent_id
  AND a.device_id = d.device_code
JOIN tmp_iot_device_seed_ids s ON s.id = a.id
JOIN iot_device_group g
  ON g.id = d.device_group_id
  AND g.ent_id = d.ent_id
  AND g.virtual_flag = 1
  AND g.deleted = 0
WHERE d.deleted = 0;

SELECT 'iot_device_without_group_count' AS check_item, COUNT(1) AS cnt
FROM iot_device d
JOIN aggregator_ent_device a
  ON a.ent_id = d.ent_id
  AND a.device_id = d.device_code
JOIN tmp_iot_device_seed_ids s ON s.id = a.id
WHERE d.deleted = 0
  AND d.device_group_id IS NULL;

DROP TEMPORARY TABLE IF EXISTS tmp_iot_device_seed_ids;
