-- 电网上送核查增加能源维度。兼容 MySQL 5.7，可重复执行。
-- 历史记录仅从审计批次明确回填能源；无法可靠识别的记录保留 NULL。

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_delivery_period' AND COLUMN_NAME = 'resource_type_id'),
    'SELECT 1', 'ALTER TABLE la_grid_delivery_period ADD COLUMN resource_type_id VARCHAR(64) DEFAULT NULL COMMENT ''能源类型ID'' AFTER aggregator_id');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_delivery_batch' AND COLUMN_NAME = 'resource_type_id'),
    'SELECT 1', 'ALTER TABLE la_grid_delivery_batch ADD COLUMN resource_type_id VARCHAR(64) DEFAULT NULL COMMENT ''能源类型ID'' AFTER delivery_type');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_total_minute' AND COLUMN_NAME = 'resource_type_id'),
    'SELECT 1', 'ALTER TABLE la_grid_total_minute ADD COLUMN resource_type_id VARCHAR(64) DEFAULT NULL COMMENT ''能源类型ID'' AFTER aggregator_id');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_single_quarter' AND COLUMN_NAME = 'resource_type_id'),
    'SELECT 1', 'ALTER TABLE la_grid_single_quarter ADD COLUMN resource_type_id VARCHAR(64) DEFAULT NULL COMMENT ''能源类型ID'' AFTER aggregator_id');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_reconciliation' AND COLUMN_NAME = 'resource_type_id'),
    'SELECT 1', 'ALTER TABLE la_grid_reconciliation ADD COLUMN resource_type_id VARCHAR(64) DEFAULT NULL COMMENT ''能源类型ID'' AFTER data_date');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_quality_issue' AND COLUMN_NAME = 'resource_type_id'),
    'SELECT 1', 'ALTER TABLE la_grid_quality_issue ADD COLUMN resource_type_id VARCHAR(64) DEFAULT NULL COMMENT ''能源类型ID'' AFTER period_id');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_quality_daily' AND COLUMN_NAME = 'resource_type_id'),
    'SELECT 1', 'ALTER TABLE la_grid_quality_daily ADD COLUMN resource_type_id VARCHAR(64) DEFAULT NULL COMMENT ''能源类型ID'' AFTER period_id');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_export_task' AND COLUMN_NAME = 'resource_type_id'),
    'SELECT 1', 'ALTER TABLE la_grid_export_task ADD COLUMN resource_type_id VARCHAR(64) DEFAULT NULL COMMENT ''能源类型ID'' AFTER period_id');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

-- 现有新泰上送以 resourceTypeId 作为通道号；仅在能与聚合商能源表精确匹配时回填。
UPDATE la_grid_delivery_batch b
JOIN aggregator_resource_type r ON r.aggregator_id = b.aggregator_id AND r.id = b.grid_channel_no
SET b.resource_type_id = r.id
WHERE b.resource_type_id IS NULL;

UPDATE la_grid_delivery_period p
JOIN aggregator_resource_type r ON r.aggregator_id = p.aggregator_id AND r.id = p.grid_channel_no
SET p.resource_type_id = r.id
WHERE p.resource_type_id IS NULL;

UPDATE la_grid_total_minute t
JOIN la_grid_delivery_batch b ON b.id = t.batch_id
SET t.resource_type_id = b.resource_type_id
WHERE t.resource_type_id IS NULL AND b.resource_type_id IS NOT NULL;

UPDATE la_grid_single_quarter s
JOIN la_grid_delivery_batch b ON b.id = s.batch_id
SET s.resource_type_id = b.resource_type_id
WHERE s.resource_type_id IS NULL AND b.resource_type_id IS NOT NULL;

UPDATE la_grid_quality_issue i
JOIN la_grid_delivery_period p ON p.id = i.period_id AND p.aggregator_id = i.aggregator_id
SET i.resource_type_id = p.resource_type_id
WHERE i.resource_type_id IS NULL AND p.resource_type_id IS NOT NULL;

UPDATE la_grid_quality_issue
SET issue_key = CONCAT(SUBSTRING_INDEX(issue_key, '|', 1), '|', resource_type_id, '|',
                       SUBSTRING(issue_key, LOCATE('|', issue_key) + 1))
WHERE resource_type_id IS NOT NULL
  AND issue_key NOT LIKE CONCAT(IFNULL(period_id, 0), '|', resource_type_id, '|%');

UPDATE la_grid_quality_daily d
JOIN la_grid_delivery_period p ON p.id = d.period_id AND p.aggregator_id = d.aggregator_id
SET d.resource_type_id = p.resource_type_id
WHERE d.resource_type_id IS NULL AND p.resource_type_id IS NOT NULL;

UPDATE la_grid_export_task t
JOIN la_grid_delivery_period p ON p.id = t.period_id AND p.aggregator_id = t.aggregator_id
SET t.resource_type_id = p.resource_type_id
WHERE t.resource_type_id IS NULL AND p.resource_type_id IS NOT NULL;

-- 旧唯一键不包含能源，必须替换，否则同通道的不同能源无法分别保存。
SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_reconciliation' AND INDEX_NAME = 'uk_la_grid_reconcile'),
    'ALTER TABLE la_grid_reconciliation DROP INDEX uk_la_grid_reconcile', 'SELECT 1');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;
ALTER TABLE la_grid_reconciliation
  ADD UNIQUE KEY uk_la_grid_reconcile (aggregator_id, resource_type_id, grid_channel_no, quarter_time);

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_quality_daily' AND INDEX_NAME = 'uk_la_grid_quality_daily'),
    'ALTER TABLE la_grid_quality_daily DROP INDEX uk_la_grid_quality_daily', 'SELECT 1');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;
ALTER TABLE la_grid_quality_daily
  ADD UNIQUE KEY uk_la_grid_quality_daily (aggregator_id, resource_type_id, grid_channel_no, data_date);

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_delivery_period' AND INDEX_NAME = 'idx_la_grid_period_energy_date'),
    'SELECT 1', 'ALTER TABLE la_grid_delivery_period ADD KEY idx_la_grid_period_energy_date (aggregator_id, resource_type_id, start_date, end_date, status)');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_total_minute' AND INDEX_NAME = 'idx_la_grid_total_energy_latest'),
    'SELECT 1', 'ALTER TABLE la_grid_total_minute ADD KEY idx_la_grid_total_energy_latest (aggregator_id, resource_type_id, grid_channel_no, minute_time, id)');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_single_quarter' AND INDEX_NAME = 'idx_la_grid_single_energy_latest'),
    'SELECT 1', 'ALTER TABLE la_grid_single_quarter ADD KEY idx_la_grid_single_energy_latest (aggregator_id, resource_type_id, grid_channel_no, quarter_time, single_code, id)');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_quality_issue' AND INDEX_NAME = 'idx_la_grid_issue_energy'),
    'SELECT 1', 'ALTER TABLE la_grid_quality_issue ADD KEY idx_la_grid_issue_energy (aggregator_id, resource_type_id, period_id, data_date, status)');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_quality_daily' AND INDEX_NAME = 'idx_la_grid_daily_energy'),
    'SELECT 1', 'ALTER TABLE la_grid_quality_daily ADD KEY idx_la_grid_daily_energy (aggregator_id, resource_type_id, period_id, data_date)');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;
