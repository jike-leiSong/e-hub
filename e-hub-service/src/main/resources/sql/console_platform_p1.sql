ALTER TABLE console_user
    ADD COLUMN tenant_id VARCHAR(64) DEFAULT NULL COMMENT '租户ID' AFTER ent_id,
    ADD KEY idx_console_user_tenant (tenant_id);

CREATE TABLE IF NOT EXISTS console_tenant (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    tenant_name VARCHAR(128) NOT NULL,
    tenant_type VARCHAR(32) NOT NULL COMMENT 'PLATFORM/AGGREGATOR/ENT/ACCOUNT',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    aggregator_id VARCHAR(64) DEFAULT NULL,
    ent_id VARCHAR(64) DEFAULT NULL,
    owner_user_id VARCHAR(64) DEFAULT NULL,
    contact_name VARCHAR(64) DEFAULT NULL,
    contact_phone VARCHAR(32) DEFAULT NULL,
    remark VARCHAR(500) DEFAULT NULL,
    create_time VARCHAR(19) DEFAULT NULL,
    update_time VARCHAR(19) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_id (tenant_id),
    KEY idx_tenant_type_status (tenant_type, status),
    KEY idx_tenant_aggregator (aggregator_id),
    KEY idx_tenant_ent (ent_id),
    KEY idx_tenant_owner (owner_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台租户';

CREATE TABLE IF NOT EXISTS console_tenant_product (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    product_code VARCHAR(64) NOT NULL,
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    valid_from VARCHAR(10) DEFAULT NULL,
    valid_to VARCHAR(10) DEFAULT NULL,
    config_json TEXT,
    create_time VARCHAR(19) DEFAULT NULL,
    update_time VARCHAR(19) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tp_tenant_product (tenant_id, product_code),
    KEY idx_tp_product_enabled (product_code, enabled),
    KEY idx_tp_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户产品订阅';

CREATE TABLE IF NOT EXISTS console_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_id VARCHAR(64) NOT NULL,
    role_name VARCHAR(64) NOT NULL,
    role_code VARCHAR(64) NOT NULL,
    platform_type VARCHAR(32) NOT NULL COMMENT 'owner/customer',
    status TINYINT NOT NULL DEFAULT 1,
    remark VARCHAR(500) DEFAULT NULL,
    create_time VARCHAR(19) DEFAULT NULL,
    update_time VARCHAR(19) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_id (role_id),
    UNIQUE KEY uk_role_code_platform (platform_type, role_code),
    KEY idx_role_platform_status (platform_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台角色';

CREATE TABLE IF NOT EXISTS console_permission (
    id BIGINT NOT NULL AUTO_INCREMENT,
    permission_code VARCHAR(128) NOT NULL,
    permission_name VARCHAR(128) NOT NULL,
    permission_type VARCHAR(32) NOT NULL COMMENT 'PAGE/API/ACTION',
    module_code VARCHAR(64) NOT NULL,
    parent_code VARCHAR(128) DEFAULT NULL,
    path VARCHAR(256) DEFAULT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time VARCHAR(19) DEFAULT NULL,
    update_time VARCHAR(19) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (permission_code),
    KEY idx_perm_module_type (module_code, permission_type, status),
    KEY idx_perm_parent (parent_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台权限点';

CREATE TABLE IF NOT EXISTS console_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id VARCHAR(64) NOT NULL,
    role_id VARCHAR(64) NOT NULL,
    create_time VARCHAR(19) DEFAULT NULL,
    update_time VARCHAR(19) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ur_user_role (user_id, role_id),
    KEY idx_ur_role (role_id),
    KEY idx_ur_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关系';

CREATE TABLE IF NOT EXISTS console_role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_id VARCHAR(64) NOT NULL,
    permission_code VARCHAR(128) NOT NULL,
    create_time VARCHAR(19) DEFAULT NULL,
    update_time VARCHAR(19) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_rp_role_perm (role_id, permission_code),
    KEY idx_rp_perm (permission_code),
    KEY idx_rp_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关系';

CREATE TABLE IF NOT EXISTS console_config_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    config_key VARCHAR(128) NOT NULL,
    config_name VARCHAR(128) NOT NULL,
    config_value TEXT NOT NULL,
    config_group VARCHAR(64) NOT NULL,
    value_type VARCHAR(32) NOT NULL COMMENT 'STRING/NUMBER/BOOLEAN/JSON',
    status TINYINT NOT NULL DEFAULT 1,
    remark VARCHAR(500) DEFAULT NULL,
    create_time VARCHAR(19) DEFAULT NULL,
    update_time VARCHAR(19) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key),
    KEY idx_cfg_group_status (config_group, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台配置项';

CREATE TABLE IF NOT EXISTS console_dict_type (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dict_type VARCHAR(64) NOT NULL,
    dict_name VARCHAR(128) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    remark VARCHAR(500) DEFAULT NULL,
    create_time VARCHAR(19) DEFAULT NULL,
    update_time VARCHAR(19) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_type (dict_type),
    KEY idx_dict_type_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型';

CREATE TABLE IF NOT EXISTS console_dict_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dict_type VARCHAR(64) NOT NULL,
    item_code VARCHAR(64) NOT NULL,
    item_name VARCHAR(128) NOT NULL,
    item_value VARCHAR(256) DEFAULT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    ext_json TEXT,
    create_time VARCHAR(19) DEFAULT NULL,
    update_time VARCHAR(19) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_item (dict_type, item_code),
    KEY idx_dict_item_type_status (dict_type, status, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典项';

CREATE TABLE IF NOT EXISTS console_operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    biz_type VARCHAR(64) NOT NULL,
    biz_id VARCHAR(64) DEFAULT NULL,
    action VARCHAR(64) NOT NULL,
    operator_user_id VARCHAR(64) DEFAULT NULL,
    operator_name VARCHAR(128) DEFAULT NULL,
    request_path VARCHAR(256) DEFAULT NULL,
    before_json TEXT,
    after_json TEXT,
    result VARCHAR(32) DEFAULT NULL,
    error_msg VARCHAR(500) DEFAULT NULL,
    create_time VARCHAR(19) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_log_biz (biz_type, biz_id),
    KEY idx_log_operator_time (operator_user_id, create_time),
    KEY idx_log_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台操作日志';
