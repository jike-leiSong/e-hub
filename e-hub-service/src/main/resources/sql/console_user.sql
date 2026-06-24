CREATE TABLE IF NOT EXISTS console_user (
    id INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    username VARCHAR(64) NOT NULL COMMENT '登录账号',
    display_name VARCHAR(128) DEFAULT NULL COMMENT '展示名称',
    password_salt VARCHAR(64) NOT NULL COMMENT '密码盐',
    password_hash VARCHAR(64) NOT NULL COMMENT 'SHA-256(salt + password)',
    user_type VARCHAR(32) NOT NULL COMMENT 'ADMIN/CUSTOMER，兼容历史 PLATFORM/AGGREGATOR/ENT',
    aggregator_id VARCHAR(64) DEFAULT NULL COMMENT '聚合商ID',
    ent_id VARCHAR(64) DEFAULT NULL COMMENT '企业用户ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
    last_login_time VARCHAR(32) DEFAULT NULL COMMENT '最近登录时间',
    create_time VARCHAR(32) DEFAULT NULL COMMENT '创建时间',
    update_time VARCHAR(32) DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_console_user_user_id (user_id),
    UNIQUE KEY uk_console_user_username (username),
    KEY idx_console_user_aggregator_id (aggregator_id),
    KEY idx_console_user_ent_id (ent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Console登录用户';

-- 密码生成规则：password_hash = lower(hex(sha256(password_salt + 明文密码)))
-- 示例：admin / admin123，仅用于本地初始化；生产环境请替换盐和密码哈希。
INSERT INTO console_user (user_id, username, display_name, password_salt, password_hash, user_type, status, create_time, update_time)
SELECT 'admin', 'admin', '平台管理员', 'e-hub-admin-salt',
       SHA2(CONCAT('e-hub-admin-salt', 'admin123'), 256),
       'ADMIN', 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_user WHERE username = 'admin');
 INSERT INTO console_user (
    user_id, username, display_name, password_salt, password_hash,
    user_type, status, create_time, update_time
  )
  SELECT
    'xintai',
    'xintai',
    '鑫泰能源',
    'e-hub-customer-salt',
    SHA2(CONCAT('e-hub-customer-salt', 'xintai123'), 256),
    'CUSTOMER',
    1,
    DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'),
    DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s');

CREATE TABLE IF NOT EXISTS console_customer_product (
    id INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id VARCHAR(64) DEFAULT NULL COMMENT 'console_user.user_id',
    customer_id VARCHAR(64) DEFAULT NULL COMMENT '客户ID，优先企业ID，其次聚合商ID',
    product_code VARCHAR(64) NOT NULL COMMENT '产品编码 load_aggregation/tariff',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    valid_from VARCHAR(16) DEFAULT NULL COMMENT '生效开始日期 yyyy-MM-dd',
    valid_to VARCHAR(16) DEFAULT NULL COMMENT '生效结束日期 yyyy-MM-dd',
    create_time VARCHAR(32) DEFAULT NULL COMMENT '创建时间',
    update_time VARCHAR(32) DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_console_customer_product_user (user_id, product_code),
    KEY idx_console_customer_product_customer (customer_id, product_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Console客户产品开通';

UPDATE console_user
SET user_type = 'ADMIN'
WHERE UPPER(user_type) = 'PLATFORM';

UPDATE console_user
SET user_type = 'CUSTOMER'
WHERE UPPER(user_type) IN ('AGGREGATOR', 'ENT');

-- 兼容历史逻辑：原客户侧默认可使用负荷聚合，迁移后通过产品开通表承载。
INSERT INTO console_customer_product (user_id, customer_id, product_code, enabled, create_time, update_time)
SELECT user_id,
       COALESCE(NULLIF(ent_id, ''), NULLIF(aggregator_id, ''), user_id),
       'load_aggregation',
       1,
       DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'),
       DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
FROM console_user u
LEFT JOIN console_customer_product p
  ON p.user_id = u.user_id
 AND p.product_code = 'load_aggregation'
WHERE UPPER(u.user_type) = 'CUSTOMER'
  AND p.id IS NULL;
