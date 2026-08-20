-- 电网市场参与人工标记。仅控制页面提示，不控制实际上送或统计。兼容 MySQL 5.7，可重复执行。
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
