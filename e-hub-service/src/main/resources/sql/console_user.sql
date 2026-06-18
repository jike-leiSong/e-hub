CREATE TABLE IF NOT EXISTS console_user (
    id INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    username VARCHAR(64) NOT NULL COMMENT '登录账号',
    display_name VARCHAR(128) DEFAULT NULL COMMENT '展示名称',
    password_salt VARCHAR(64) NOT NULL COMMENT '密码盐',
    password_hash VARCHAR(64) NOT NULL COMMENT 'SHA-256(salt + password)',
    user_type VARCHAR(32) NOT NULL COMMENT 'PLATFORM/AGGREGATOR/ENT',
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
       'PLATFORM', 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE NOT EXISTS (SELECT 1 FROM console_user WHERE username = 'admin');
