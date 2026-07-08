CREATE TABLE IF NOT EXISTS `e_agent_price` (
  `id` varchar(255) NOT NULL,
  `version` varchar(24) DEFAULT NULL COMMENT '电价版本',
  `province_code` varchar(20) DEFAULT NULL COMMENT '省份code',
  `province_name` varchar(64) DEFAULT NULL COMMENT '省份名称',
  `second_type` varchar(64) DEFAULT NULL COMMENT '二级分类',
  `third_type` varchar(64) DEFAULT NULL COMMENT '三级分类',
  `dy_level` varchar(64) DEFAULT NULL COMMENT '电压等级',
  `user_type` varchar(64) DEFAULT NULL COMMENT '用户属性',
  `other_type` varchar(64) DEFAULT NULL COMMENT '其他属性',
  `price_type` varchar(20) DEFAULT NULL COMMENT '价格类型',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标记',
  `capacity_electricity_price` decimal(20,7) DEFAULT NULL COMMENT '容量电价',
  `demand_electricity_price` decimal(20,7) DEFAULT NULL COMMENT '需量电价',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_val` (`province_code`,`version`,`second_type`,`third_type`,`dy_level`,`user_type`,`other_type`,`del_flag`,`price_type`,`capacity_electricity_price`,`demand_electricity_price`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='电网代理价格';

CREATE TABLE IF NOT EXISTS `e_agent_price_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `en_agent_price_id` varchar(255) NOT NULL COMMENT 'en_agent_price_id',
  `biz_time` varchar(20) DEFAULT NULL COMMENT '时点',
  `price` decimal(20,7) DEFAULT NULL COMMENT '价格',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_val` (`en_agent_price_id`,`biz_time`,`price`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='电网代理价格详情';

CREATE TABLE IF NOT EXISTS `e_fpgj_type` (
  `id` varchar(255) NOT NULL,
  `version` varchar(24) DEFAULT NULL COMMENT '电价版本',
  `province_code` varchar(20) DEFAULT NULL COMMENT '省份code',
  `province_name` varchar(64) DEFAULT NULL COMMENT '省份名称',
  `second_type` varchar(64) DEFAULT NULL COMMENT '二级分类',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_val` (`version`,`province_code`,`second_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='电网-峰平谷尖';

CREATE TABLE IF NOT EXISTS `e_fpgj_type_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `en_fpgj_type_id` varchar(255) NOT NULL COMMENT 'en_fpgj_type_id',
  `biz_time` varchar(20) DEFAULT NULL COMMENT '时点',
  `fpgj_type` varchar(10) DEFAULT NULL COMMENT '峰平谷尖类型',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_val` (`en_fpgj_type_id`,`biz_time`,`fpgj_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='峰平谷尖数据详情';

CREATE TABLE IF NOT EXISTS `tariff_source_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `province_code` varchar(20) NOT NULL COMMENT '省份编码',
  `province_name` varchar(64) NOT NULL COMMENT '省份名称',
  `source_name` varchar(128) NOT NULL COMMENT '来源名称',
  `source_type` varchar(32) NOT NULL COMMENT '来源类型',
  `source_url` varchar(512) DEFAULT NULL COMMENT '来源页面',
  `publish_rule` varchar(128) DEFAULT NULL COMMENT '发布日期规律',
  `enabled` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_source_province` (`province_code`,`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='代理电价数据来源配置';

CREATE TABLE IF NOT EXISTS `tariff_source_document` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `source_config_id` bigint(20) DEFAULT NULL COMMENT '来源配置ID',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '关联导入批次号',
  `year_month` varchar(7) NOT NULL COMMENT '电价月份',
  `version` varchar(24) NOT NULL COMMENT '内部版本',
  `province_code` varchar(20) NOT NULL COMMENT '省份编码',
  `province_name` varchar(64) NOT NULL COMMENT '省份名称',
  `source_type` varchar(32) DEFAULT NULL COMMENT '来源类型',
  `source_name` varchar(128) DEFAULT NULL COMMENT '来源名称',
  `source_url` varchar(512) DEFAULT NULL COMMENT '来源地址',
  `source_file_name` varchar(255) DEFAULT NULL COMMENT '来源文件名',
  `source_file_path` varchar(512) DEFAULT NULL COMMENT '来源文件路径',
  `source_file_hash` varchar(128) DEFAULT NULL COMMENT '来源文件hash',
  `document_title` varchar(255) DEFAULT NULL COMMENT '文档标题',
  `document_no` varchar(128) DEFAULT NULL COMMENT '文号',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `effective_start` date DEFAULT NULL COMMENT '生效开始日期',
  `effective_end` date DEFAULT NULL COMMENT '生效结束日期',
  `status` varchar(32) NOT NULL DEFAULT 'DRAFT' COMMENT '状态 DRAFT/PUBLISHED/ARCHIVED',
  `operator_id` varchar(64) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人名称',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_tariff_source_doc_version` (`version`,`province_code`,`status`),
  KEY `idx_tariff_source_doc_month` (`year_month`,`province_code`,`status`),
  KEY `idx_tariff_source_doc_batch` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='电价来源文档';

CREATE TABLE IF NOT EXISTS `tariff_import_batch` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_no` varchar(64) NOT NULL COMMENT '导入批次号',
  `year_month` varchar(7) NOT NULL COMMENT '电价月份',
  `version` varchar(24) NOT NULL COMMENT '内部版本',
  `province_code` varchar(20) NOT NULL COMMENT '省份编码',
  `province_name` varchar(64) NOT NULL COMMENT '省份名称',
  `source_type` varchar(32) NOT NULL COMMENT '来源类型',
  `source_name` varchar(128) DEFAULT NULL COMMENT '来源名称',
  `source_url` varchar(512) DEFAULT NULL COMMENT '来源地址',
  `source_file_name` varchar(255) DEFAULT NULL COMMENT '来源文件名',
  `source_file_path` varchar(512) DEFAULT NULL COMMENT '来源文件路径',
  `status` varchar(32) NOT NULL COMMENT '批次状态',
  `total_rows` int(11) DEFAULT '0' COMMENT '总行数',
  `valid_rows` int(11) DEFAULT '0' COMMENT '有效行数',
  `error_rows` int(11) DEFAULT '0' COMMENT '错误行数',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `operator_id` varchar(64) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人名称',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tariff_batch_no` (`batch_no`),
  KEY `idx_tariff_batch_version` (`version`,`province_code`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='代理电价导入批次';

CREATE TABLE IF NOT EXISTS `tariff_agent_price_staging` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_no` varchar(64) NOT NULL COMMENT '导入批次号',
  `row_no` int(11) NOT NULL COMMENT '来源行号',
  `version` varchar(24) NOT NULL COMMENT '内部版本',
  `province_code` varchar(20) NOT NULL COMMENT '省份编码',
  `province_name` varchar(64) NOT NULL COMMENT '省份名称',
  `second_type` varchar(64) NOT NULL COMMENT '二级分类',
  `third_type` varchar(64) NOT NULL COMMENT '三级分类',
  `dy_level` varchar(64) NOT NULL COMMENT '电压等级',
  `user_type` varchar(64) NOT NULL COMMENT '用户属性',
  `other_type` varchar(64) NOT NULL COMMENT '收费类型',
  `price_type` varchar(32) NOT NULL COMMENT '价格类型',
  `capacity_electricity_price` decimal(20,7) DEFAULT NULL COMMENT '容量电价',
  `demand_electricity_price` decimal(20,7) DEFAULT NULL COMMENT '需量电价',
  `validate_status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '校验状态',
  `validate_message` varchar(1024) DEFAULT NULL COMMENT '校验信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_tariff_price_staging_batch` (`batch_no`),
  KEY `idx_tariff_price_staging_biz` (`version`,`province_code`,`second_type`,`third_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='代理电价导入暂存主表';

CREATE TABLE IF NOT EXISTS `tariff_agent_price_data_staging` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `staging_price_id` bigint(20) NOT NULL COMMENT '暂存主表ID',
  `biz_time` varchar(20) NOT NULL COMMENT '时点',
  `price` decimal(20,7) NOT NULL COMMENT '价格',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tariff_price_staging_time` (`staging_price_id`,`biz_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='代理电价导入暂存96点';

CREATE TABLE IF NOT EXISTS `tariff_fpgj_type_staging` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_no` varchar(64) NOT NULL COMMENT '导入批次号',
  `row_no` int(11) NOT NULL COMMENT '来源行号',
  `version` varchar(24) NOT NULL COMMENT '内部版本',
  `province_code` varchar(20) NOT NULL COMMENT '省份编码',
  `province_name` varchar(64) NOT NULL COMMENT '省份名称',
  `second_type` varchar(64) NOT NULL COMMENT '二级分类',
  `validate_status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '校验状态',
  `validate_message` varchar(1024) DEFAULT NULL COMMENT '校验信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_tariff_fpgj_staging_batch` (`batch_no`),
  KEY `idx_tariff_fpgj_staging_biz` (`version`,`province_code`,`second_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='尖峰平谷导入暂存主表';

CREATE TABLE IF NOT EXISTS `tariff_fpgj_type_data_staging` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `staging_fpgj_id` bigint(20) NOT NULL COMMENT '暂存主表ID',
  `biz_time` varchar(20) NOT NULL COMMENT '时点',
  `fpgj_type` varchar(10) NOT NULL COMMENT '尖峰平谷类型',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tariff_fpgj_staging_time` (`staging_fpgj_id`,`biz_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='尖峰平谷导入暂存96点';

CREATE TABLE IF NOT EXISTS `tariff_api_call_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_key` varchar(64) NOT NULL COMMENT '调用方',
  `api_path` varchar(128) NOT NULL COMMENT '接口路径',
  `request_id` varchar(64) NOT NULL COMMENT '请求ID',
  `province_code` varchar(20) DEFAULT NULL COMMENT '省份编码',
  `year_month` varchar(7) DEFAULT NULL COMMENT '电价月份',
  `selected_date` varchar(10) DEFAULT NULL COMMENT '查询日期',
  `response_code` varchar(32) DEFAULT NULL COMMENT '响应码',
  `cost_ms` int(11) DEFAULT NULL COMMENT '耗时毫秒',
  `client_ip` varchar(64) DEFAULT NULL COMMENT '客户端IP',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_tariff_api_call_app_time` (`app_key`,`create_time`),
  KEY `idx_tariff_api_call_request` (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='代理电价开放接口调用日志';

INSERT INTO tariff_source_config (province_code, province_name, source_name, source_type, source_url, publish_rule, enabled, remark)
SELECT '000000', '全国', '国家发展改革委', 'NDRC', 'https://www.ndrc.gov.cn/', '政策文件、输配电价和分时电价机制通知', 1, '全国政策来源'
WHERE NOT EXISTS (
  SELECT 1 FROM tariff_source_config WHERE province_code = '000000' AND source_type = 'NDRC' AND source_name = '国家发展改革委'
);

INSERT INTO tariff_source_config (province_code, province_name, source_name, source_type, source_url, publish_rule, enabled, remark)
SELECT '000000', '全国', '国家电网95598智能互动网站', 'SGCC', 'https://www.95598.cn/', '各省电价政策、95598和网上国网发布入口', 1, '国网区域来源入口'
WHERE NOT EXISTS (
  SELECT 1 FROM tariff_source_config WHERE province_code = '000000' AND source_type = 'SGCC' AND source_name = '国家电网95598智能互动网站'
);

INSERT INTO tariff_source_config (province_code, province_name, source_name, source_type, source_url, publish_rule, enabled, remark)
SELECT '000000', '全国', '南网在线', 'CSG', 'https://95598.csg.cn/', '南方五省区电价政策、代理购电公告发布入口', 1, '南网区域来源入口'
WHERE NOT EXISTS (
  SELECT 1 FROM tariff_source_config WHERE province_code = '000000' AND source_type = 'CSG' AND source_name = '南网在线'
);
