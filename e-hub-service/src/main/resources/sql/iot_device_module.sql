CREATE TABLE IF NOT EXISTS `iot_project` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `aggregator_id` varchar(20) DEFAULT NULL COMMENT 'aggregator id',
  `ent_id` varchar(20) NOT NULL COMMENT 'enterprise id',
  `project_code` varchar(64) NOT NULL COMMENT 'project code',
  `project_name` varchar(128) NOT NULL COMMENT 'project name',
  `parent_id` bigint DEFAULT NULL COMMENT 'parent project id',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 enabled, 0 disabled',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0 normal, 1 deleted',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_project_ent_code` (`ent_id`, `project_code`),
  KEY `idx_iot_project_ent` (`ent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iot project';

CREATE TABLE IF NOT EXISTS `iot_gateway` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `aggregator_id` varchar(20) DEFAULT NULL COMMENT 'aggregator id',
  `ent_id` varchar(20) NOT NULL COMMENT 'enterprise id',
  `gateway_code` varchar(64) NOT NULL COMMENT 'gateway code',
  `gateway_name` varchar(128) NOT NULL COMMENT 'gateway name',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 enabled, 0 disabled',
  `default_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '1 default, 0 normal',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0 normal, 1 deleted',
  `remark` varchar(500) DEFAULT NULL COMMENT 'remark',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_gateway_ent_code` (`ent_id`, `gateway_code`),
  KEY `idx_iot_gateway_ent` (`ent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iot gateway';

CREATE TABLE IF NOT EXISTS `iot_device_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `aggregator_id` varchar(20) DEFAULT NULL COMMENT 'aggregator id',
  `ent_id` varchar(20) NOT NULL COMMENT 'enterprise id',
  `device_group_code` varchar(64) NOT NULL COMMENT 'group code',
  `device_group_name` varchar(128) NOT NULL COMMENT 'group name',
  `device_group_type` varchar(64) DEFAULT NULL COMMENT 'group type code',
  `device_group_type_name` varchar(128) DEFAULT NULL COMMENT 'group type name',
  `energy_type` varchar(64) DEFAULT NULL COMMENT 'energy type',
  `gateway_id` bigint DEFAULT NULL COMMENT 'gateway id',
  `virtual_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '1 virtual default group',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 enabled, 0 disabled',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0 normal, 1 deleted',
  `remark` varchar(500) DEFAULT NULL COMMENT 'remark',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_group_ent_code` (`ent_id`, `device_group_code`),
  KEY `idx_iot_device_group_ent` (`ent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iot device group';

CREATE TABLE IF NOT EXISTS `iot_device_group_param` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `device_group_id` bigint NOT NULL COMMENT 'device group id',
  `attr_code` varchar(64) NOT NULL COMMENT 'attribute code',
  `attr_name` varchar(128) NOT NULL COMMENT 'attribute name',
  `alias_name` varchar(128) DEFAULT NULL COMMENT 'alias name',
  `attr_value` varchar(500) DEFAULT NULL COMMENT 'attribute value',
  `attr_unit` varchar(32) DEFAULT NULL COMMENT 'attribute unit',
  `attr_type` varchar(32) DEFAULT NULL COMMENT 'attribute type',
  `sort` int NOT NULL DEFAULT '0' COMMENT 'sort',
  `remark` varchar(500) DEFAULT NULL COMMENT 'remark',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_group_param` (`device_group_id`, `attr_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iot device group param';

CREATE TABLE IF NOT EXISTS `iot_device_group_point` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `device_group_id` bigint NOT NULL COMMENT 'device group id',
  `property_code` varchar(64) NOT NULL COMMENT 'property code',
  `property_name` varchar(128) NOT NULL COMMENT 'property name',
  `data_type` varchar(32) DEFAULT NULL COMMENT 'data type',
  `data_type_name` varchar(64) DEFAULT NULL COMMENT 'data type name',
  `value_type` varchar(32) DEFAULT NULL COMMENT 'value type',
  `unit` varchar(32) DEFAULT NULL COMMENT 'unit',
  `read_write_role` varchar(20) NOT NULL DEFAULT 'readOnly' COMMENT 'readOnly/readWrite',
  `sort` int NOT NULL DEFAULT '0' COMMENT 'sort',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 enabled, 0 disabled',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0 normal, 1 deleted',
  `remark` varchar(500) DEFAULT NULL COMMENT 'remark',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_group_point` (`device_group_id`, `property_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iot device group point';

CREATE TABLE IF NOT EXISTS `iot_device` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `aggregator_id` varchar(20) DEFAULT NULL COMMENT 'aggregator id',
  `ent_id` varchar(20) NOT NULL COMMENT 'enterprise id',
  `project_id` bigint DEFAULT NULL COMMENT 'project id',
  `device_group_id` bigint DEFAULT NULL COMMENT 'device group id',
  `device_code` varchar(64) NOT NULL COMMENT 'standard device code',
  `device_name` varchar(128) NOT NULL COMMENT 'device name',
  `device_type_code` varchar(32) DEFAULT NULL COMMENT 'device type code',
  `device_type_name` varchar(64) DEFAULT NULL COMMENT 'device type name',
  `communication_method` varchar(32) DEFAULT NULL COMMENT 'communication method',
  `manufacturer` varchar(128) DEFAULT NULL COMMENT 'manufacturer',
  `model` varchar(128) DEFAULT NULL COMMENT 'device model',
  `third_party_api` varchar(64) DEFAULT NULL COMMENT 'third party api code',
  `third_party_code` varchar(128) DEFAULT NULL COMMENT 'third party code',
  `asset_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 enabled, 0 disabled',
  `online_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '1 online, 0 offline',
  `last_data_time` datetime DEFAULT NULL COMMENT 'last data time',
  `remark` varchar(500) DEFAULT NULL COMMENT 'remark',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0 normal, 1 deleted',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_ent_code` (`ent_id`, `device_code`),
  KEY `idx_iot_device_ent` (`ent_id`),
  KEY `idx_iot_device_project` (`project_id`),
  KEY `idx_iot_device_group` (`device_group_id`),
  KEY `idx_iot_device_type` (`device_type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iot device';

CREATE TABLE IF NOT EXISTS `iot_device_param` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `device_id` bigint NOT NULL COMMENT 'device id',
  `param_code` varchar(64) NOT NULL COMMENT 'param code',
  `param_name` varchar(128) NOT NULL COMMENT 'param name',
  `param_value` varchar(500) DEFAULT NULL COMMENT 'param value',
  `unit` varchar(32) DEFAULT NULL COMMENT 'unit',
  `sort` int NOT NULL DEFAULT '0' COMMENT 'sort',
  `remark` varchar(500) DEFAULT NULL COMMENT 'remark',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_param` (`device_id`, `param_code`),
  KEY `idx_iot_device_param_device` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iot device param';

CREATE TABLE IF NOT EXISTS `iot_device_point` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `device_id` bigint NOT NULL COMMENT 'device id',
  `point_code` varchar(64) NOT NULL COMMENT 'standard point code',
  `point_name` varchar(128) NOT NULL COMMENT 'point name',
  `value_type` varchar(32) NOT NULL DEFAULT 'double' COMMENT 'value type',
  `unit` varchar(32) DEFAULT NULL COMMENT 'unit',
  `data_frequency` int DEFAULT NULL COMMENT 'data frequency seconds',
  `required_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '1 required, 0 optional',
  `read_write_role` varchar(20) NOT NULL DEFAULT 'readOnly' COMMENT 'readOnly/readWrite',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 enabled, 0 disabled',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0 normal, 1 deleted',
  `sort` int NOT NULL DEFAULT '0' COMMENT 'sort',
  `remark` varchar(500) DEFAULT NULL COMMENT 'remark',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_point` (`device_id`, `point_code`),
  KEY `idx_iot_device_point_device` (`device_id`),
  KEY `idx_iot_device_point_code` (`point_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iot device point';

CREATE TABLE IF NOT EXISTS `iot_device_point_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `device_point_id` bigint NOT NULL COMMENT 'device point id',
  `value` varchar(128) NOT NULL COMMENT 'status value',
  `description` varchar(255) DEFAULT NULL COMMENT 'description',
  `tags` varchar(128) DEFAULT NULL COMMENT 'tags',
  `sort` int NOT NULL DEFAULT '0' COMMENT 'sort',
  `remark` varchar(500) DEFAULT NULL COMMENT 'remark',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_iot_device_point_definition_point` (`device_point_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iot device point definition';

CREATE TABLE IF NOT EXISTS `iot_device_group_point_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `device_group_point_id` bigint NOT NULL COMMENT 'device group point id',
  `value` varchar(128) NOT NULL COMMENT 'status value',
  `description` varchar(255) DEFAULT NULL COMMENT 'description',
  `tags` varchar(128) DEFAULT NULL COMMENT 'tags',
  `sort` int NOT NULL DEFAULT '0' COMMENT 'sort',
  `remark` varchar(500) DEFAULT NULL COMMENT 'remark',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_iot_device_group_point_definition_point` (`device_group_point_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iot device group point definition';

CREATE TABLE IF NOT EXISTS `iot_device_type_param_metadata` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `device_type_code` varchar(32) NOT NULL COMMENT 'device type code',
  `attr_code` varchar(64) NOT NULL COMMENT 'attribute code',
  `attr_name` varchar(128) NOT NULL COMMENT 'attribute name',
  `required_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '1 required, 0 optional',
  `sort` int NOT NULL DEFAULT '0' COMMENT 'sort',
  `sample_value` varchar(255) DEFAULT NULL COMMENT 'sample value',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_type_param_meta` (`device_type_code`, `attr_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iot device type param metadata';

CREATE TABLE IF NOT EXISTS `iot_device_type_point_metadata` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `device_type_code` varchar(32) NOT NULL COMMENT 'device type code',
  `property_code` varchar(64) NOT NULL COMMENT 'property code',
  `property_name` varchar(128) NOT NULL COMMENT 'property name',
  `data_type` varchar(32) DEFAULT NULL COMMENT 'data type',
  `data_type_name` varchar(64) DEFAULT NULL COMMENT 'data type name',
  `value_type` varchar(32) DEFAULT NULL COMMENT 'value type',
  `unit` varchar(32) DEFAULT NULL COMMENT 'unit',
  `read_write_role` varchar(20) NOT NULL DEFAULT 'readOnly' COMMENT 'readOnly/readWrite',
  `sort` int NOT NULL DEFAULT '0' COMMENT 'sort',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_type_point_meta` (`device_type_code`, `property_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iot device type point metadata';

CREATE TABLE IF NOT EXISTS `iot_access_app` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `source_code` varchar(64) NOT NULL COMMENT 'source code',
  `source_name` varchar(128) DEFAULT NULL COMMENT 'source name',
  `aggregator_id` varchar(20) DEFAULT NULL COMMENT 'aggregator id',
  `ent_id` varchar(20) NOT NULL COMMENT 'enterprise id',
  `project_id` bigint DEFAULT NULL COMMENT 'default project id',
  `access_key` varchar(128) NOT NULL COMMENT 'X-GW-AccessKey',
  `user_key` varchar(128) DEFAULT NULL COMMENT 'userKey',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 enabled, 0 disabled',
  `remark` varchar(500) DEFAULT NULL COMMENT 'remark',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_access_source` (`source_code`),
  UNIQUE KEY `uk_iot_access_key_user` (`access_key`, `user_key`),
  KEY `idx_iot_access_ent` (`ent_id`),
  KEY `idx_iot_access_key_ent` (`access_key`, `ent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iot access app';

CREATE TABLE IF NOT EXISTS `iot_project_external_ref` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `source_code` varchar(64) NOT NULL COMMENT 'source code',
  `ent_id` varchar(20) NOT NULL COMMENT 'enterprise id',
  `project_id` bigint DEFAULT NULL COMMENT 'project id',
  `external_project_id` varchar(128) NOT NULL COMMENT 'external project id',
  `external_project_name` varchar(128) DEFAULT NULL COMMENT 'external project name',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 enabled, 0 disabled',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_project_ext` (`source_code`, `ent_id`, `external_project_id`),
  KEY `idx_iot_project_ext_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iot project external ref';

CREATE TABLE IF NOT EXISTS `iot_device_external_ref` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `source_code` varchar(64) NOT NULL COMMENT 'source code',
  `ent_id` varchar(20) NOT NULL COMMENT 'enterprise id',
  `project_id` bigint DEFAULT NULL COMMENT 'project id',
  `device_id` bigint NOT NULL COMMENT 'iot device id',
  `external_device_id` varchar(128) NOT NULL COMMENT 'external device id',
  `external_device_code` varchar(128) DEFAULT NULL COMMENT 'external device code',
  `external_device_name` varchar(128) DEFAULT NULL COMMENT 'external device name',
  `gateway_code` varchar(128) DEFAULT NULL COMMENT 'gateway code',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 enabled, 0 disabled',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_ext` (`source_code`, `ent_id`, `external_device_id`),
  KEY `idx_iot_device_ext_device` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iot device external ref';

CREATE TABLE IF NOT EXISTS `iot_point_external_ref` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `source_code` varchar(64) NOT NULL COMMENT 'source code',
  `device_id` bigint NOT NULL COMMENT 'iot device id',
  `point_id` bigint NOT NULL COMMENT 'iot point id',
  `external_metric` varchar(128) NOT NULL COMMENT 'external metric',
  `external_metric_name` varchar(128) DEFAULT NULL COMMENT 'external metric name',
  `ratio` double NOT NULL DEFAULT '1' COMMENT 'ratio',
  `offset_value` double NOT NULL DEFAULT '0' COMMENT 'offset value',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 enabled, 0 disabled',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_point_ext` (`source_code`, `device_id`, `external_metric`),
  KEY `idx_iot_point_ext_point` (`point_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iot point external ref';

CREATE TABLE IF NOT EXISTS `iot_telemetry_minute` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `aggregator_id` varchar(20) DEFAULT NULL COMMENT 'aggregator id',
  `ent_id` varchar(20) NOT NULL COMMENT 'enterprise id',
  `project_id` bigint DEFAULT NULL COMMENT 'project id',
  `device_id` bigint NOT NULL COMMENT 'iot device id',
  `device_code` varchar(64) NOT NULL COMMENT 'standard device code',
  `point_code` varchar(64) NOT NULL COMMENT 'standard point code',
  `data_time` datetime NOT NULL COMMENT 'original data time',
  `minute_time` datetime NOT NULL COMMENT 'minute time',
  `point_value` double DEFAULT NULL COMMENT 'standard value',
  `unit` varchar(32) DEFAULT NULL COMMENT 'unit',
  `quality` varchar(32) NOT NULL DEFAULT 'normal' COMMENT 'quality',
  `source_code` varchar(64) DEFAULT NULL COMMENT 'source code',
  `receive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'receive time',
  `raw_value` varchar(128) DEFAULT NULL COMMENT 'raw value',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_telemetry_minute` (`device_id`, `point_code`, `minute_time`),
  KEY `idx_iot_telemetry_point_time_device` (`point_code`, `minute_time`, `device_id`),
  KEY `idx_iot_telemetry_ent_time` (`ent_id`, `point_code`, `minute_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iot telemetry minute';

CREATE TABLE IF NOT EXISTS `iot_unmatched_telemetry_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `source_code` varchar(64) DEFAULT NULL COMMENT 'source code',
  `interface_type` varchar(32) NOT NULL COMMENT 'originData/cimData',
  `external_project_id` varchar(128) DEFAULT NULL COMMENT 'external project id',
  `external_device_id` varchar(128) DEFAULT NULL COMMENT 'external device id',
  `external_device_name` varchar(128) DEFAULT NULL COMMENT 'external device name',
  `external_metric` varchar(128) DEFAULT NULL COMMENT 'external metric',
  `external_metric_name` varchar(128) DEFAULT NULL COMMENT 'external metric name',
  `data_time` datetime DEFAULT NULL COMMENT 'data time',
  `value` varchar(128) DEFAULT NULL COMMENT 'value',
  `reason` varchar(64) NOT NULL COMMENT 'reason',
  `raw_payload` text COMMENT 'raw payload',
  `handled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0 unhandled, 1 handled',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_iot_unmatched_source` (`source_code`, `handled`),
  KEY `idx_iot_unmatched_device` (`external_device_id`, `external_metric`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iot unmatched telemetry log';
