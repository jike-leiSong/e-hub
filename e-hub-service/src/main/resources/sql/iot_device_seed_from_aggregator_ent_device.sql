-- 根据本批 aggregator_ent_device 业务设备生成物联设备，并回填业务侧关联。
-- 前置条件：先执行用户提供的 aggregator_ent_device INSERT 数据。
-- 本批设备期望数量：204。

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

-- 补齐本批业务设备使用到的物联设备类型字典，避免物联设备类型名称为空。
INSERT INTO sys_dict (`dict_type_code`, `dict_type_name`, `dict_code`, `dict_value`, `sort`, `remark`, `create_time`, `update_time`, `deleted`)
SELECT 'device_type', '设备类型', 'METE', '计量设备', 10, '业务设备类型迁移', NOW(), NOW(), 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1
  FROM sys_dict
  WHERE dict_type_code = 'device_type'
    AND dict_code = 'METE'
    AND deleted = 0
);

INSERT INTO sys_dict (`dict_type_code`, `dict_type_name`, `dict_code`, `dict_value`, `sort`, `remark`, `create_time`, `update_time`, `deleted`)
SELECT 'device_type', '设备类型', 'TRAN', '变压器', 11, '业务设备类型迁移', NOW(), NOW(), 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1
  FROM sys_dict
  WHERE dict_type_code = 'device_type'
    AND dict_code = 'TRAN'
    AND deleted = 0
);

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

-- 物联设备查询按设备组过滤，批量导入时需要先保证每个企业都有默认网关和默认设备组。
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

-- 物联设备唯一键为 (ent_id, device_code)，第三方唯一键为 (ent_id, third_party_api, third_party_code)。
-- 这里沿用业务设备编码：aggregator_ent_device.device_id -> iot_device.device_code / third_party_code。
INSERT INTO iot_device (
  aggregator_id,
  ent_id,
  project_id,
  device_group_id,
  gateway_id,
  device_code,
  device_name,
  third_party_api,
  third_party_code,
  device_type_code,
  device_type_name,
  communication_method,
  manufacturer,
  model,
  status,
  asset_status,
  online_status,
  remark,
  create_time,
  update_time,
  deleted
)
SELECT
  a.aggregator_id,
  a.ent_id,
  a.energy_station_code,
  dg.id AS device_group_id,
  COALESCE(dg.gateway_id, gw.id) AS gateway_id,
  a.device_id,
  a.device_name,
  COALESCE(NULLIF(a.data_source, ''), 'EMS') AS third_party_api,
  a.device_id AS third_party_code,
  a.device_type AS device_type_code,
  COALESCE(
    dt.dict_value,
    CASE a.device_type
      WHEN 'METE' THEN '计量设备'
      WHEN 'TRAN' THEN '变压器'
      ELSE a.device_type
    END
  ) AS device_type_name,
  COALESCE(NULLIF(a.data_source, ''), 'EMS') AS communication_method,
  a.equip_manufactor AS manufacturer,
  a.storage_type AS model,
  1 AS status,
  1 AS asset_status,
  CASE WHEN a.status = 1 THEN 1 ELSE 0 END AS online_status,
  CONCAT('来源业务设备ID:', a.id, ',能源站:', COALESCE(a.energy_station, '')) AS remark,
  NOW() AS create_time,
  NOW() AS update_time,
  0 AS deleted
FROM aggregator_ent_device a
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
LEFT JOIN (
  SELECT dict_code, MAX(dict_value) AS dict_value
  FROM sys_dict
  WHERE dict_type_code = 'device_type'
    AND deleted = 0
  GROUP BY dict_code
) dt ON dt.dict_code = a.device_type
WHERE a.del_flag = 1
  AND a.ent_id IS NOT NULL
  AND a.ent_id <> ''
  AND a.device_id IS NOT NULL
  AND a.device_id <> ''
  AND a.device_name IS NOT NULL
  AND a.device_name <> ''
ON DUPLICATE KEY UPDATE
  aggregator_id = VALUES(aggregator_id),
  project_id = VALUES(project_id),
  device_group_id = VALUES(device_group_id),
  gateway_id = VALUES(gateway_id),
  device_name = VALUES(device_name),
  third_party_api = VALUES(third_party_api),
  third_party_code = VALUES(third_party_code),
  device_type_code = VALUES(device_type_code),
  device_type_name = VALUES(device_type_name),
  communication_method = VALUES(communication_method),
  manufacturer = VALUES(manufacturer),
  model = VALUES(model),
  status = VALUES(status),
  asset_status = VALUES(asset_status),
  online_status = VALUES(online_status),
  remark = VALUES(remark),
  update_time = NOW(),
  deleted = 0;

-- 回填业务设备物联 ID。业务设备表字段是 varchar，因此显式转成字符。
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

-- 回填单体模型企业 ID：按聚合商 + energy_station_code 与本批业务设备关联。
UPDATE aggregator_single_model_data m
JOIN (
  SELECT
    a.aggregator_id,
    a.energy_station_code,
    MIN(a.ent_id) AS ent_id
  FROM aggregator_ent_device a
  JOIN tmp_iot_device_seed_ids s ON s.id = a.id
  WHERE a.del_flag = 1
    AND a.ent_id IS NOT NULL
    AND a.ent_id <> ''
    AND a.energy_station_code IS NOT NULL
    AND a.energy_station_code <> ''
  GROUP BY a.aggregator_id, a.energy_station_code
) d
  ON d.aggregator_id = m.aggregator_id
  AND d.energy_station_code = m.energy_station_code
SET m.ent_id = d.ent_id
WHERE d.ent_id IS NOT NULL
  AND d.ent_id <> ''
  AND (
    m.ent_id IS NULL
    OR m.ent_id = ''
    OR m.ent_id <> d.ent_id
  );

COMMIT;

-- 执行后校验：第一行应为 204；后两行用于确认关联数量。
SELECT 'batch_business_device_count' AS check_item, COUNT(1) AS cnt
FROM tmp_iot_device_seed_ids;

SELECT 'business_device_linked_iot_count' AS check_item, COUNT(1) AS cnt
FROM aggregator_ent_device a
JOIN tmp_iot_device_seed_ids s ON s.id = a.id
WHERE a.iot_device_base_id IS NOT NULL
  AND a.iot_device_base_id <> '';

SELECT 'single_model_linked_ent_count' AS check_item, COUNT(1) AS cnt
FROM aggregator_single_model_data m
JOIN (
  SELECT DISTINCT a.aggregator_id, a.energy_station_code
  FROM aggregator_ent_device a
  JOIN tmp_iot_device_seed_ids s ON s.id = a.id
  WHERE a.energy_station_code IS NOT NULL
    AND a.energy_station_code <> ''
) d
  ON d.aggregator_id = m.aggregator_id
  AND d.energy_station_code = m.energy_station_code
WHERE m.ent_id IS NOT NULL
  AND m.ent_id <> '';

DROP TEMPORARY TABLE IF EXISTS tmp_iot_device_seed_ids;
