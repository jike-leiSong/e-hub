-- 电网上送核查调整为每日保障口径。兼容 MySQL 5.7，可重复执行。
SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_model_snapshot_device' AND COLUMN_NAME = 'participation_status'),
    'SELECT 1', 'ALTER TABLE la_model_snapshot_device ADD COLUMN participation_status TINYINT NOT NULL DEFAULT 1 COMMENT ''1参与，0不参与'' AFTER resource_type_id');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

CREATE TABLE IF NOT EXISTS la_grid_manual_operation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  aggregator_id VARCHAR(20) NOT NULL,
  resource_type_id VARCHAR(64) NOT NULL,
  operation_type VARCHAR(32) NOT NULL COMMENT 'SEND_MODEL/RETRY_SINGLE/SEND_PEAK_96/SEND_PEAK_DAILY',
  business_time DATETIME DEFAULT NULL,
  operation_status VARCHAR(32) NOT NULL COMMENT 'SUCCESS/FAILED',
  response_message VARCHAR(2048) DEFAULT NULL,
  operator_id VARCHAR(64) DEFAULT NULL,
  operator_name VARCHAR(128) DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_la_grid_manual_operation (aggregator_id, resource_type_id, operation_type, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电网上送人工操作记录';

-- 旧快照只保存了参与设备，因此保持 participation_status=1；新快照开始同时保存参与和不参与模型。
SET @ehub_ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_model_snapshot_device' AND INDEX_NAME = 'idx_la_snapshot_participation'),
    'SELECT 1', 'ALTER TABLE la_model_snapshot_device ADD KEY idx_la_snapshot_participation (aggregator_id, snapshot_id, resource_type_id, participation_status, single_code)');
PREPARE ehub_stmt FROM @ehub_ddl; EXECUTE ehub_stmt; DEALLOCATE PREPARE ehub_stmt;

CREATE TABLE IF NOT EXISTS la_grid_market_status (
  id BIGINT NOT NULL AUTO_INCREMENT,
  aggregator_id VARCHAR(20) NOT NULL,
  resource_type_id VARCHAR(64) NOT NULL,
  market_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '1参与市场，0当前未参与；仅作人工页面标记',
  remark VARCHAR(512) DEFAULT NULL,
  operator_id VARCHAR(64) DEFAULT NULL,
  operator_name VARCHAR(128) DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_la_grid_market_status (aggregator_id, resource_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电网市场参与人工标记';
