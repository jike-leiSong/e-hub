-- 已部署一期 grid_delivery_quality.sql 的环境执行本迁移。
-- 全新环境只需执行最新版 grid_delivery_quality.sql。

CREATE TABLE IF NOT EXISTS la_grid_delivery_period (
  id BIGINT NOT NULL AUTO_INCREMENT,
  aggregator_id VARCHAR(20) NOT NULL,
  period_code VARCHAR(64) NOT NULL,
  period_name VARCHAR(128) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  grid_channel_no VARCHAR(64) NOT NULL DEFAULT '',
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
  standard_rate DECIMAL(10,4) NOT NULL DEFAULT 99,
  required_days INT NOT NULL DEFAULT 7,
  remark VARCHAR(512) DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id), UNIQUE KEY uk_la_grid_period_code (aggregator_id, period_code),
  KEY idx_la_grid_period_date (aggregator_id, start_date, end_date, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聚合商电网上送周期';

-- 兼容脚本重复执行及历史环境只完成了部分字段升级。以下动态 DDL 兼容 MySQL 5.7。
SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_quality_issue' AND COLUMN_NAME = 'period_id'),
    'SELECT 1', 'ALTER TABLE la_grid_quality_issue ADD COLUMN period_id BIGINT DEFAULT NULL AFTER data_date');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_quality_issue' AND COLUMN_NAME = 'issue_key'),
    'SELECT 1', 'ALTER TABLE la_grid_quality_issue ADD COLUMN issue_key VARCHAR(255) NULL COMMENT ''同一问题的幂等业务键'' AFTER period_id');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_quality_issue' AND COLUMN_NAME = 'handler_id'),
    'SELECT 1', 'ALTER TABLE la_grid_quality_issue ADD COLUMN handler_id VARCHAR(64) DEFAULT NULL AFTER remark');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_quality_issue' AND COLUMN_NAME = 'handler_name'),
    'SELECT 1', 'ALTER TABLE la_grid_quality_issue ADD COLUMN handler_name VARCHAR(128) DEFAULT NULL AFTER handler_id');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_quality_issue' AND COLUMN_NAME = 'first_detected_time'),
    'SELECT 1', 'ALTER TABLE la_grid_quality_issue ADD COLUMN first_detected_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER handler_name');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_quality_issue' AND COLUMN_NAME = 'last_detected_time'),
    'SELECT 1', 'ALTER TABLE la_grid_quality_issue ADD COLUMN last_detected_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER first_detected_time');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_quality_issue' AND COLUMN_NAME = 'resolved_time'),
    'SELECT 1', 'ALTER TABLE la_grid_quality_issue ADD COLUMN resolved_time DATETIME DEFAULT NULL AFTER last_detected_time');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_quality_issue' AND COLUMN_NAME = 'current_detected'),
    'SELECT 1', 'ALTER TABLE la_grid_quality_issue ADD COLUMN current_detected TINYINT NOT NULL DEFAULT 1 AFTER resolved_time');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

UPDATE la_grid_quality_issue
SET issue_key = CONCAT(issue_type, '|', DATE_FORMAT(issue_time, '%Y%m%d%H%i%s'), '|',
                       IFNULL(grid_channel_no, ''), '|', IFNULL(single_code, ''), '|', IFNULL(device_id, ''))
WHERE issue_key IS NULL OR issue_key = '';

UPDATE la_grid_quality_issue i
JOIN la_grid_delivery_period p ON p.aggregator_id = i.aggregator_id
  AND p.start_date <= i.data_date AND p.end_date >= i.data_date
  AND IFNULL(p.grid_channel_no, '') = IFNULL(i.grid_channel_no, '')
SET i.period_id = p.id
WHERE i.period_id IS NULL;

UPDATE la_grid_quality_issue
SET issue_key = CONCAT(IFNULL(period_id, 0), '|', issue_key)
WHERE issue_key NOT LIKE CONCAT(IFNULL(period_id, 0), '|%');

ALTER TABLE la_grid_quality_issue
  MODIFY COLUMN issue_key VARCHAR(255) NOT NULL COMMENT '同一问题的幂等业务键';

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_quality_issue' AND INDEX_NAME = 'uk_la_grid_issue_key'),
    'SELECT 1', 'ALTER TABLE la_grid_quality_issue ADD UNIQUE KEY uk_la_grid_issue_key (aggregator_id, issue_key)');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_quality_issue' AND INDEX_NAME = 'idx_la_grid_issue_period'),
    'SELECT 1', 'ALTER TABLE la_grid_quality_issue ADD KEY idx_la_grid_issue_period (aggregator_id, period_id, data_date, status)');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

CREATE TABLE IF NOT EXISTS la_grid_quality_daily (
  id BIGINT NOT NULL AUTO_INCREMENT,
  aggregator_id VARCHAR(20) NOT NULL,
  data_date DATE NOT NULL,
  grid_channel_no VARCHAR(64) NOT NULL DEFAULT '',
  snapshot_id BIGINT DEFAULT NULL,
  participant_count INT NOT NULL DEFAULT 0,
  total_expected INT NOT NULL DEFAULT 0,
  total_actual INT NOT NULL DEFAULT 0,
  total_rate DECIMAL(10,4) NOT NULL DEFAULT 0,
  single_expected INT NOT NULL DEFAULT 0,
  single_actual INT NOT NULL DEFAULT 0,
  single_rate DECIMAL(10,4) NOT NULL DEFAULT 0,
  reconciliation_expected INT NOT NULL DEFAULT 0,
  reconciliation_matched INT NOT NULL DEFAULT 0,
  reconciliation_rate DECIMAL(10,4) NOT NULL DEFAULT 0,
  issue_count INT NOT NULL DEFAULT 0,
  reached_standard TINYINT NOT NULL DEFAULT 0,
  calculated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_la_grid_quality_daily (aggregator_id, grid_channel_no, data_date),
  KEY idx_la_grid_quality_daily_date (aggregator_id, data_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电网上送每日质量汇总';

-- 一期已创建的每日汇总和异步任务表补充周期关联；新环境由最新版主脚本直接创建。
SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_quality_daily' AND COLUMN_NAME = 'period_id'),
    'SELECT 1', 'ALTER TABLE la_grid_quality_daily ADD COLUMN period_id BIGINT DEFAULT NULL AFTER data_date');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_quality_daily' AND INDEX_NAME = 'idx_la_grid_quality_daily_period'),
    'SELECT 1', 'ALTER TABLE la_grid_quality_daily ADD KEY idx_la_grid_quality_daily_period (aggregator_id, period_id, data_date)');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

UPDATE la_grid_quality_daily d
JOIN la_grid_delivery_period p ON p.aggregator_id = d.aggregator_id
  AND p.start_date <= d.data_date AND p.end_date >= d.data_date
  AND IFNULL(p.grid_channel_no, '') = IFNULL(d.grid_channel_no, '')
SET d.period_id = p.id
WHERE d.period_id IS NULL;

CREATE TABLE IF NOT EXISTS la_grid_export_task (
  id BIGINT NOT NULL AUTO_INCREMENT,
  task_no VARCHAR(64) NOT NULL,
  aggregator_id VARCHAR(20) NOT NULL,
  report_type VARCHAR(32) NOT NULL DEFAULT 'FULL',
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  grid_channel_no VARCHAR(64) NOT NULL DEFAULT '',
  task_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  progress INT NOT NULL DEFAULT 0,
  file_name VARCHAR(255) DEFAULT NULL,
  file_path VARCHAR(1024) DEFAULT NULL,
  error_message VARCHAR(1024) DEFAULT NULL,
  created_by VARCHAR(64) DEFAULT NULL,
  finish_time DATETIME DEFAULT NULL,
  expire_time DATETIME DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_la_grid_export_task_no (task_no),
  KEY idx_la_grid_export_task_query (aggregator_id, create_time, task_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电网上送异步导出任务';

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_export_task' AND COLUMN_NAME = 'period_id'),
    'SELECT 1', 'ALTER TABLE la_grid_export_task ADD COLUMN period_id BIGINT DEFAULT NULL AFTER aggregator_id');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_export_task' AND INDEX_NAME = 'idx_la_grid_export_task_period'),
    'SELECT 1', 'ALTER TABLE la_grid_export_task ADD KEY idx_la_grid_export_task_period (aggregator_id, period_id, create_time)');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

UPDATE la_grid_export_task t
JOIN la_grid_delivery_period p ON p.aggregator_id = t.aggregator_id
  AND p.start_date <= t.start_date AND p.end_date >= t.end_date
  AND IFNULL(p.grid_channel_no, '') = IFNULL(t.grid_channel_no, '')
SET t.period_id = p.id
WHERE t.period_id IS NULL;

-- 优化周期摘要查询：按通道和时间一次定位最新审计记录，避免逐行相关子查询。
SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_total_minute' AND INDEX_NAME = 'idx_la_grid_total_latest'),
    'SELECT 1', 'ALTER TABLE la_grid_total_minute ADD KEY idx_la_grid_total_latest (aggregator_id, grid_channel_no, minute_time, id)');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_grid_single_quarter' AND INDEX_NAME = 'idx_la_grid_single_latest'),
    'SELECT 1', 'ALTER TABLE la_grid_single_quarter ADD KEY idx_la_grid_single_latest (aggregator_id, grid_channel_no, quarter_time, single_code, id)');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'iot_telemetry_minute' AND INDEX_NAME = 'idx_iot_telemetry_agg_point_time_device'),
    'SELECT 1', 'ALTER TABLE iot_telemetry_minute ADD KEY idx_iot_telemetry_agg_point_time_device (aggregator_id, point_code, minute_time, device_code)');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;
