CREATE TABLE IF NOT EXISTS `sys_dict` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_type_code` varchar(100) NOT NULL COMMENT '字典类型编码',
  `dict_type_name` varchar(200) DEFAULT NULL COMMENT '字典类型名称',
  `dict_code` varchar(100) NOT NULL COMMENT '字典编码',
  `dict_value` varchar(200) NOT NULL COMMENT '字典值',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int(11) DEFAULT '0' COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_dict_type_code` (`dict_type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典表';

CREATE TABLE IF NOT EXISTS `iot_gateway` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `aggregator_id` varchar(20) DEFAULT NULL COMMENT '聚合商ID',
  `ent_id` varchar(20) DEFAULT NULL COMMENT '企业ID',
  `gateway_code` varchar(64) DEFAULT NULL COMMENT '网关编码',
  `gateway_name` varchar(128) NOT NULL COMMENT '网关名称',
  `gateway_type_code` varchar(100) DEFAULT 'gateway' COMMENT '网关类型编码',
  `gateway_type_name` varchar(200) DEFAULT NULL COMMENT '网关类型名称',
  `communication_method_code` varchar(100) DEFAULT NULL COMMENT '通讯方式编码',
  `communication_method_name` varchar(200) DEFAULT NULL COMMENT '通讯方式名称',
  `manufacturer` varchar(128) DEFAULT NULL COMMENT '厂家',
  `model` varchar(128) DEFAULT NULL COMMENT '型号',
  `gateway_serial_number` varchar(50) DEFAULT NULL COMMENT '网关序列号',
  `gateway_sn_number` varchar(50) DEFAULT NULL COMMENT '网关SN号',
  `iot_status` varchar(50) DEFAULT NULL COMMENT '物联状态',
  `bound_device_count` int NOT NULL DEFAULT '0' COMMENT '绑定设备数量',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态：0禁用，1启用',
  `default_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '默认标识：1默认，0普通',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_gateway_ent_code` (`ent_id`, `gateway_code`),
  KEY `idx_iot_gateway_ent` (`ent_id`),
  KEY `idx_iot_gateway_type` (`gateway_type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联网关';

CREATE TABLE IF NOT EXISTS `iot_device_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `aggregator_id` varchar(20) DEFAULT NULL COMMENT '聚合商ID',
  `ent_id` varchar(20) DEFAULT NULL COMMENT '企业ID',
  `device_group_code` varchar(100) NOT NULL COMMENT '设备组编码',
  `device_group_name` varchar(200) NOT NULL COMMENT '设备组名称',
  `device_group_type` varchar(50) DEFAULT NULL COMMENT '设备组类型编码',
  `device_group_type_name` varchar(200) DEFAULT NULL COMMENT '设备组类型名称',
  `energy_type` varchar(64) DEFAULT NULL COMMENT '能源类型',
  `gateway_id` bigint DEFAULT NULL COMMENT '关联网网ID',
  `trd_pty_code` varchar(100) DEFAULT NULL COMMENT '第三方编码',
  `virtual_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '虚拟默认组标识：1是，0否',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态：0禁用，1启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_group_ent_code` (`ent_id`, `device_group_code`),
  KEY `idx_iot_device_group_ent` (`ent_id`),
  KEY `idx_iot_device_group_type` (`device_group_type`),
  KEY `idx_iot_device_group_gateway` (`gateway_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备组';

CREATE TABLE IF NOT EXISTS `iot_device_group_param` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_group_id` bigint NOT NULL COMMENT '设备组ID',
  `attr_code` varchar(100) NOT NULL COMMENT '属性编码',
  `attr_name` varchar(200) NOT NULL COMMENT '属性名称',
  `alias_name` varchar(200) DEFAULT NULL COMMENT '别名',
  `attr_value` varchar(500) DEFAULT NULL COMMENT '属性值',
  `attr_unit` varchar(50) DEFAULT NULL COMMENT '属性单位',
  `attr_type` varchar(50) DEFAULT NULL COMMENT '属性类型',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_group_param` (`device_group_id`, `attr_code`),
  KEY `idx_iot_device_group_param_code` (`attr_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备组参数';

CREATE TABLE IF NOT EXISTS `iot_device_group_point` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_group_id` bigint NOT NULL COMMENT '设备组ID',
  `property_code` varchar(100) NOT NULL COMMENT '测点编码',
  `property_name` varchar(200) NOT NULL COMMENT '测点名称',
  `data_type` varchar(50) DEFAULT NULL COMMENT '数据类型：instantaneous瞬时值，accumulated累计值，state状态值',
  `data_type_name` varchar(100) DEFAULT NULL COMMENT '数据类型名称',
  `value_type` varchar(50) DEFAULT NULL COMMENT '值类型',
  `unit` varchar(50) DEFAULT NULL COMMENT '单位',
  `read_write_role` varchar(20) NOT NULL DEFAULT 'readOnly' COMMENT '读写角色：readOnly只读，readWrite读写',
  `value_lower_limit` varchar(50) DEFAULT NULL COMMENT '下限值',
  `value_high_limit` varchar(50) DEFAULT NULL COMMENT '上限值',
  `dead_zone_type` int DEFAULT '1' COMMENT '死区类型',
  `type` int DEFAULT '1' COMMENT '类型',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态：0禁用，1启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_group_point` (`device_group_id`, `property_code`),
  KEY `idx_iot_device_group_point_code` (`property_code`),
  KEY `idx_iot_device_group_point_data_type` (`data_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备组测点';

CREATE TABLE IF NOT EXISTS `iot_device` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `aggregator_id` varchar(20) DEFAULT NULL COMMENT '聚合商ID',
  `ent_id` varchar(20) DEFAULT NULL COMMENT '企业ID',
  `project_id` varchar(64) DEFAULT NULL COMMENT '项目ID',
  `device_group_id` bigint DEFAULT NULL COMMENT '设备组ID',
  `gateway_id` bigint DEFAULT NULL COMMENT '所属网关ID',
  `device_code` varchar(100) NOT NULL COMMENT '设备编码',
  `device_name` varchar(200) NOT NULL COMMENT '设备名称',
  `third_party_api` varchar(50) DEFAULT NULL COMMENT '第三方API',
  `third_party_code` varchar(128) DEFAULT NULL COMMENT '第三方标识',
  `device_type_code` varchar(50) DEFAULT NULL COMMENT '设备类型编码',
  `device_type_name` varchar(200) DEFAULT NULL COMMENT '设备类型名称',
  `communication_method` varchar(32) DEFAULT NULL COMMENT '通讯方式',
  `manufacturer` varchar(128) DEFAULT NULL COMMENT '设备制造商',
  `model` varchar(128) DEFAULT NULL COMMENT '设备型号',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态：0禁用，1启用',
  `asset_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '资产状态：1启用，0停用',
  `online_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '在线状态：1在线，0离线',
  `last_data_time` datetime DEFAULT NULL COMMENT '最后数据时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_ent_code` (`ent_id`, `device_code`),
  KEY `idx_iot_device_ent` (`ent_id`),
  KEY `idx_iot_device_project` (`project_id`),
  KEY `idx_iot_device_group` (`device_group_id`),
  KEY `idx_iot_device_gateway` (`gateway_id`),
  KEY `idx_iot_device_type` (`device_type_code`),
  UNIQUE KEY `uk_iot_device_third_party` (`ent_id`, `third_party_api`, `third_party_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备';

CREATE TABLE IF NOT EXISTS `iot_device_param` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `attr_code` varchar(100) NOT NULL COMMENT '属性编码',
  `attr_name` varchar(200) NOT NULL COMMENT '属性名称',
  `alias_name` varchar(200) DEFAULT NULL COMMENT '别名',
  `attr_value` varchar(500) DEFAULT NULL COMMENT '属性值',
  `attr_unit` varchar(50) DEFAULT NULL COMMENT '属性单位',
  `attr_type` varchar(50) DEFAULT NULL COMMENT '属性类型',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_param` (`device_id`, `attr_code`),
  KEY `idx_iot_device_param_code` (`attr_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备参数';

CREATE TABLE IF NOT EXISTS `iot_device_point` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `property_code` varchar(100) NOT NULL COMMENT '测点编码',
  `property_name` varchar(200) NOT NULL COMMENT '测点名称',
  `third_party_code` varchar(50) DEFAULT NULL COMMENT '第三方测点编码',
  `data_type` varchar(50) DEFAULT NULL COMMENT '数据类型：instantaneous瞬时值，accumulated累计值，state状态值',
  `data_type_name` varchar(100) DEFAULT NULL COMMENT '数据类型名称',
  `value_type` varchar(50) DEFAULT NULL COMMENT '值类型',
  `unit` varchar(50) DEFAULT NULL COMMENT '单位',
  `read_write_role` varchar(20) NOT NULL DEFAULT 'readOnly' COMMENT '读写角色：readOnly只读，readWrite读写',
  `up_way` varchar(20) DEFAULT NULL COMMENT '上报方式',
  `up_way_name` varchar(50) DEFAULT NULL COMMENT '上报方式名称',
  `up_period` varchar(20) DEFAULT NULL COMMENT '上报周期',
  `up_period_name` varchar(50) DEFAULT NULL COMMENT '上报周期名称',
  `value_lower_limit` varchar(50) DEFAULT NULL COMMENT '下限值',
  `value_high_limit` varchar(50) DEFAULT NULL COMMENT '上限值',
  `dead_zone_type` int DEFAULT '1' COMMENT '死区类型',
  `type` int DEFAULT '1' COMMENT '类型',
  `data_frequency` int DEFAULT NULL COMMENT '数据频率，单位秒',
  `required_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '必选标识：1必选，0可选',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态：0禁用，1启用',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_point` (`device_id`, `property_code`),
  UNIQUE KEY `uk_iot_device_point_third_party` (`device_id`, `third_party_code`),
  KEY `idx_iot_device_point_device` (`device_id`),
  KEY `idx_iot_device_point_code` (`property_code`),
  KEY `idx_iot_device_point_data_type` (`data_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备测点';

CREATE TABLE IF NOT EXISTS `iot_device_point_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_point_id` bigint NOT NULL COMMENT '设备测点ID',
  `value` varchar(50) NOT NULL COMMENT '状态值',
  `description` varchar(200) DEFAULT NULL COMMENT '状态值描述',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_iot_device_point_definition_point` (`device_point_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备测点释义';

CREATE TABLE IF NOT EXISTS `iot_device_group_point_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_group_point_id` bigint NOT NULL COMMENT '设备组测点ID',
  `value` varchar(50) DEFAULT NULL COMMENT '状态值',
  `description` varchar(200) DEFAULT NULL COMMENT '状态值描述',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_iot_device_group_point_definition_point` (`device_group_point_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备组测点释义';

CREATE TABLE IF NOT EXISTS `iot_device_group_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_group_id` bigint NOT NULL COMMENT '设备组ID',
  `value` varchar(50) NOT NULL COMMENT '状态值',
  `description` varchar(200) NOT NULL COMMENT '状态值描述',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_iot_device_group_definition_group` (`device_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备组释义';

CREATE TABLE IF NOT EXISTS `iot_device_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `value` varchar(50) NOT NULL COMMENT '状态值',
  `description` varchar(200) NOT NULL COMMENT '状态值描述',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_iot_device_definition_device` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备释义';

CREATE TABLE IF NOT EXISTS `iot_device_type_point_metadata` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_type_code` varchar(50) NOT NULL COMMENT '设备类型编码',
  `device_type_name` varchar(200) DEFAULT NULL COMMENT '设备类型名称',
  `property_code` varchar(100) NOT NULL COMMENT '测点编码',
  `property_name` varchar(200) NOT NULL COMMENT '测点名称',
  `data_type` varchar(50) DEFAULT NULL COMMENT '数据类型',
  `data_type_name` varchar(100) DEFAULT NULL COMMENT '数据类型名称',
  `value_type` varchar(50) DEFAULT NULL COMMENT '值类型',
  `unit` varchar(50) DEFAULT NULL COMMENT '测点单位',
  `read_write_role` varchar(20) NOT NULL DEFAULT 'readOnly' COMMENT '读写角色',
  `up_way` varchar(20) DEFAULT NULL COMMENT '上报方式',
  `up_period` varchar(20) DEFAULT NULL COMMENT '上报周期',
  `type` varchar(50) DEFAULT '设备' COMMENT '类型',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_type_point_meta` (`device_type_code`, `property_code`),
  KEY `idx_iot_device_type_point_meta_type` (`device_type_code`),
  KEY `idx_iot_device_type_point_meta_code` (`property_code`),
  KEY `idx_iot_device_type_point_meta_data_type` (`data_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备类型测点元数据';

CREATE TABLE IF NOT EXISTS `iot_device_group_point_metadata` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_group_type` varchar(50) NOT NULL COMMENT '设备组类型编码',
  `device_group_type_name` varchar(200) DEFAULT NULL COMMENT '设备组类型名称',
  `property_code` varchar(100) NOT NULL COMMENT '测点编码',
  `property_name` varchar(200) NOT NULL COMMENT '测点名称',
  `property_name_en` varchar(200) DEFAULT NULL COMMENT '测点名称英文',
  `data_type` varchar(50) DEFAULT NULL COMMENT '数据类型',
  `data_type_name` varchar(100) DEFAULT NULL COMMENT '数据类型名称',
  `value_type` varchar(50) DEFAULT NULL COMMENT '值类型',
  `read_write_role` varchar(20) NOT NULL DEFAULT 'readOnly' COMMENT '读写角色',
  `unit` varchar(50) DEFAULT NULL COMMENT '测点单位',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_group_point_meta` (`device_group_type`, `property_code`),
  KEY `idx_iot_device_group_point_meta_type` (`device_group_type`),
  KEY `idx_iot_device_group_point_meta_code` (`property_code`),
  KEY `idx_iot_device_group_point_meta_data_type` (`data_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备组测点元数据';

CREATE TABLE IF NOT EXISTS `iot_device_group_param_metadata` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_group_type` varchar(50) NOT NULL COMMENT '设备组类型编码',
  `device_group_type_name` varchar(200) DEFAULT NULL COMMENT '设备组类型名称',
  `attr_code` varchar(100) NOT NULL COMMENT '属性编码',
  `attr_name` varchar(200) NOT NULL COMMENT '属性名称',
  `attr_name_en` varchar(200) DEFAULT NULL COMMENT '属性名称英文',
  `attr_type` varchar(50) DEFAULT NULL COMMENT '属性类型',
  `attr_unit` varchar(50) DEFAULT NULL COMMENT '属性单位',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_group_param_meta` (`device_group_type`, `attr_code`),
  KEY `idx_iot_device_group_param_meta_type` (`device_group_type`),
  KEY `idx_iot_device_group_param_meta_code` (`attr_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备组参数元数据';

CREATE TABLE IF NOT EXISTS `iot_device_param_metadata` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_type_code` varchar(50) NOT NULL COMMENT '设备类型编码',
  `device_type_name` varchar(200) DEFAULT NULL COMMENT '设备类型名称',
  `attr_code` varchar(100) NOT NULL COMMENT '属性编码',
  `attr_name` varchar(200) NOT NULL COMMENT '属性名称',
  `attr_name_en` varchar(200) DEFAULT NULL COMMENT '属性名称英文',
  `attr_type` varchar(50) DEFAULT NULL COMMENT '属性类型',
  `attr_unit` varchar(50) DEFAULT NULL COMMENT '属性单位',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_param_meta` (`device_type_code`, `attr_code`),
  KEY `idx_iot_device_param_meta_type` (`device_type_code`),
  KEY `idx_iot_device_param_meta_code` (`attr_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备参数元数据';

CREATE TABLE IF NOT EXISTS `iot_device_group_display_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_group_id` bigint NOT NULL COMMENT '设备组ID',
  `display_order` int NOT NULL DEFAULT '0' COMMENT '展示顺序',
  `is_display` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否展示：0不展示，1展示',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_iot_device_group_display_group` (`device_group_id`),
  KEY `idx_iot_device_group_display_order` (`display_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备组展示配置';

CREATE TABLE IF NOT EXISTS `iot_device_type_display_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_group_id` bigint NOT NULL COMMENT '设备组ID',
  `device_type_code` varchar(50) NOT NULL COMMENT '设备类型编码',
  `device_type_name` varchar(200) DEFAULT NULL COMMENT '设备类型名称',
  `display_order` int NOT NULL DEFAULT '0' COMMENT '展示顺序',
  `is_display` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否展示：0不展示，1展示',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_iot_device_type_display_group` (`device_group_id`),
  KEY `idx_iot_device_type_display_type` (`device_type_code`),
  KEY `idx_iot_device_type_display_order` (`display_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备类型展示配置';

CREATE TABLE IF NOT EXISTS `iot_device_real_point_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_type_code` varchar(50) DEFAULT NULL COMMENT '设备类型编码',
  `device_id` bigint DEFAULT NULL COMMENT '设备ID',
  `device_point_id` bigint NOT NULL COMMENT '设备测点ID',
  `display_order` int NOT NULL DEFAULT '0' COMMENT '展示顺序',
  `is_display` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否展示：0不展示，1展示',
  `custom_name` varchar(200) DEFAULT NULL COMMENT '自定义测点名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_iot_device_real_point_type` (`device_type_code`),
  KEY `idx_iot_device_real_point_device` (`device_id`),
  KEY `idx_iot_device_real_point_point` (`device_point_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备实时点位配置';

CREATE TABLE IF NOT EXISTS `iot_device_control_point_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_type_code` varchar(50) DEFAULT NULL COMMENT '设备类型编码',
  `device_id` bigint DEFAULT NULL COMMENT '设备ID',
  `device_point_id` bigint NOT NULL COMMENT '设备测点ID',
  `display_order` int NOT NULL DEFAULT '0' COMMENT '展示顺序',
  `is_display` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否展示：0不展示，1展示',
  `custom_name` varchar(200) DEFAULT NULL COMMENT '自定义测点名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_iot_device_control_point_type` (`device_type_code`),
  KEY `idx_iot_device_control_point_device` (`device_id`),
  KEY `idx_iot_device_control_point_point` (`device_point_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备遥控点位配置';

CREATE TABLE IF NOT EXISTS `iot_device_group_control_point_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_group_type` varchar(50) DEFAULT NULL COMMENT '设备组类型编码',
  `device_group_id` bigint DEFAULT NULL COMMENT '设备组ID',
  `device_group_point_id` bigint NOT NULL COMMENT '设备组测点ID',
  `display_order` int NOT NULL DEFAULT '0' COMMENT '展示顺序',
  `is_display` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否展示：0不展示，1展示',
  `custom_name` varchar(200) DEFAULT NULL COMMENT '自定义测点名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_iot_device_group_control_type` (`device_group_type`),
  KEY `idx_iot_device_group_control_group` (`device_group_id`),
  KEY `idx_iot_device_group_control_point` (`device_group_point_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备组遥控点位配置';

CREATE TABLE IF NOT EXISTS `iot_device_command` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `device_type` varchar(20) DEFAULT NULL COMMENT '设备类型：deviceGroup设备组，device设备',
  `device_group_id` bigint DEFAULT NULL COMMENT '设备组ID',
  `device_id` bigint DEFAULT NULL COMMENT '设备ID',
  `equip_name` varchar(255) DEFAULT NULL COMMENT '设备组或设备名称',
  `device_point_id` bigint DEFAULT NULL COMMENT '设备测点ID',
  `command_id` varchar(100) NOT NULL COMMENT '指令ID',
  `command_type` varchar(20) NOT NULL COMMENT '指令类型：manual手动，algorithm算法',
  `command_source` varchar(20) NOT NULL COMMENT '消息来源：cloud云端，edge边端',
  `property_code` varchar(100) NOT NULL COMMENT '测点编码',
  `property_name` varchar(200) DEFAULT NULL COMMENT '测点名称',
  `old_value` varchar(500) DEFAULT NULL COMMENT '原值',
  `new_value` varchar(500) NOT NULL COMMENT '新值',
  `unit` varchar(50) DEFAULT NULL COMMENT '单位',
  `command_status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '指令状态',
  `execute_time` datetime DEFAULT NULL COMMENT '执行时间',
  `feedback_time` datetime DEFAULT NULL COMMENT '反馈时间',
  `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `operator_name` varchar(255) DEFAULT NULL COMMENT '操作人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_command_id` (`command_id`),
  KEY `idx_iot_device_command_user` (`user_id`),
  KEY `idx_iot_device_command_device` (`device_id`),
  KEY `idx_iot_device_command_point` (`device_point_id`),
  KEY `idx_iot_device_command_status` (`command_status`),
  KEY `idx_iot_device_command_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备控制指令';

CREATE TABLE IF NOT EXISTS `iot_device_group_remote_local_status` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_group_id` bigint NOT NULL COMMENT '设备组ID',
  `status` int NOT NULL DEFAULT '0' COMMENT '状态：0本地，1远程',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_group_remote_status` (`device_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联设备组远程本地状态';

CREATE TABLE IF NOT EXISTS `iot_access_app` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `source_code` varchar(64) NOT NULL COMMENT '来源编码',
  `source_name` varchar(128) DEFAULT NULL COMMENT '来源名称',
  `aggregator_id` varchar(20) DEFAULT NULL COMMENT '聚合商ID',
  `ent_id` varchar(20) NOT NULL COMMENT '企业ID',
  `project_id` varchar(64) DEFAULT NULL COMMENT '默认项目编码',
  `access_key` varchar(128) NOT NULL COMMENT '接入密钥',
  `user_key` varchar(128) DEFAULT NULL COMMENT '用户密钥',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '启用状态：1启用，0停用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_access_source` (`source_code`),
  UNIQUE KEY `uk_iot_access_key_user` (`access_key`, `user_key`),
  KEY `idx_iot_access_ent` (`ent_id`),
  KEY `idx_iot_access_key_ent` (`access_key`, `ent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联三方接入应用';

CREATE TABLE IF NOT EXISTS `iot_telemetry_raw` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `interface_type` varchar(32) NOT NULL COMMENT '接口类型：originData原始数据，cimData标准数据',
  `source_code` varchar(64) NOT NULL COMMENT '三方来源编码',
  `ent_id` varchar(20) NOT NULL COMMENT '企业ID',
  `project_id` varchar(64) DEFAULT NULL COMMENT '项目编码',
  `device_id` bigint DEFAULT NULL COMMENT '匹配的本地设备ID',
  `device_code` varchar(100) DEFAULT NULL COMMENT '标准设备编码',
  `point_code` varchar(100) DEFAULT NULL COMMENT '标准测点编码',
  `external_device_id` varchar(128) DEFAULT NULL COMMENT '三方设备标识',
  `external_metric` varchar(128) DEFAULT NULL COMMENT '三方metric',
  `data_time` datetime DEFAULT NULL COMMENT '数据时间',
  `raw_value` varchar(256) DEFAULT NULL COMMENT '原始值',
  `receive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '接收时间',
  `raw_payload` text COMMENT '原始报文',
  `match_status` varchar(32) NOT NULL DEFAULT 'matched' COMMENT '匹配状态：matched匹配成功，device_not_found设备未找到，point_not_found测点未找到',
  `match_reason` varchar(64) DEFAULT NULL COMMENT '匹配失败原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_telemetry_raw_source_ent` (`source_code`, `ent_id`, `interface_type`),
  KEY `idx_telemetry_raw_project` (`project_id`),
  KEY `idx_telemetry_raw_device` (`device_id`),
  KEY `idx_telemetry_raw_data_time` (`data_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联遥测原始明细';

CREATE TABLE IF NOT EXISTS `iot_telemetry_minute` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `aggregator_id` varchar(20) DEFAULT NULL COMMENT '聚合商ID',
  `ent_id` varchar(20) NOT NULL COMMENT '企业ID',
  `project_id` varchar(64) DEFAULT NULL COMMENT '项目编码',
  `device_id` bigint NOT NULL COMMENT '物联设备ID',
  `device_code` varchar(100) NOT NULL COMMENT '标准设备编码',
  `point_code` varchar(100) NOT NULL COMMENT '标准测点编码',
  `data_time` datetime NOT NULL COMMENT '原始数据时间',
  `minute_time` datetime NOT NULL COMMENT '分钟时间',
  `point_value` double DEFAULT NULL COMMENT '标准值',
  `unit` varchar(32) DEFAULT NULL COMMENT '单位',
  `quality` varchar(32) NOT NULL DEFAULT 'normal' COMMENT '数据质量',
  `source_code` varchar(64) DEFAULT NULL COMMENT '来源编码',
  `receive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '接收时间',
  `raw_value` varchar(128) DEFAULT NULL COMMENT '原始值',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_telemetry_minute` (`device_id`, `point_code`, `minute_time`),
  KEY `idx_iot_telemetry_point_time_device` (`point_code`, `minute_time`, `device_id`),
  KEY `idx_iot_telemetry_ent_time` (`ent_id`, `point_code`, `minute_time`),
  KEY `idx_iot_telemetry_agg_point_time_device` (`aggregator_id`, `point_code`, `minute_time`, `device_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联分钟测点数据';

CREATE TABLE IF NOT EXISTS `iot_unmatched_telemetry_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `source_code` varchar(64) DEFAULT NULL COMMENT '来源编码',
  `interface_type` varchar(32) NOT NULL COMMENT '接口类型：originData原始数据，cimData标准数据',
  `external_device_id` varchar(128) DEFAULT NULL COMMENT '三方设备ID',
  `external_device_name` varchar(128) DEFAULT NULL COMMENT '三方设备名称',
  `external_metric` varchar(128) DEFAULT NULL COMMENT '三方指标编码',
  `external_metric_name` varchar(128) DEFAULT NULL COMMENT '三方指标名称',
  `data_time` datetime DEFAULT NULL COMMENT '数据时间',
  `value` varchar(128) DEFAULT NULL COMMENT '数据值',
  `reason` varchar(64) NOT NULL COMMENT '未匹配原因',
  `raw_payload` text COMMENT '原始报文',
  `handled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '处理状态：0未处理，1已处理',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_iot_unmatched_source` (`source_code`, `handled`),
  KEY `idx_iot_unmatched_device` (`external_device_id`, `external_metric`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联未匹配遥测日志';

INSERT INTO `sys_dict` (`dict_type_code`, `dict_type_name`, `dict_code`, `dict_value`, `sort`, `remark`, `create_time`, `update_time`, `deleted`)
SELECT 'device_type', '设备类型', 'ENERGY_STORAGE', '储能设备', 1, '物联设备类型', NOW(), NOW(), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict` WHERE `dict_type_code` = 'device_type' AND `dict_code` = 'ENERGY_STORAGE');

INSERT INTO `sys_dict` (`dict_type_code`, `dict_type_name`, `dict_code`, `dict_value`, `sort`, `remark`, `create_time`, `update_time`, `deleted`)
SELECT 'device_type', '设备类型', 'AIR_CONDITIONER', '空调设备', 2, '物联设备类型', NOW(), NOW(), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict` WHERE `dict_type_code` = 'device_type' AND `dict_code` = 'AIR_CONDITIONER');

INSERT INTO `sys_dict` (`dict_type_code`, `dict_type_name`, `dict_code`, `dict_value`, `sort`, `remark`, `create_time`, `update_time`, `deleted`)
SELECT 'device_type', '设备类型', 'CHILLER', '冷机设备', 3, '物联设备类型', NOW(), NOW(), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict` WHERE `dict_type_code` = 'device_type' AND `dict_code` = 'CHILLER');

INSERT INTO `sys_dict` (`dict_type_code`, `dict_type_name`, `dict_code`, `dict_value`, `sort`, `remark`, `create_time`, `update_time`, `deleted`)
SELECT 'device_type', '设备类型', 'METER', '电表设备', 4, '物联设备类型', NOW(), NOW(), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict` WHERE `dict_type_code` = 'device_type' AND `dict_code` = 'METER');

INSERT INTO `sys_dict` (`dict_type_code`, `dict_type_name`, `dict_code`, `dict_value`, `sort`, `remark`, `create_time`, `update_time`, `deleted`)
SELECT 'device_group_type', '设备组类型', 'ENERGY_STORAGE', '储能设备组', 1, '物联设备组类型', NOW(), NOW(), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict` WHERE `dict_type_code` = 'device_group_type' AND `dict_code` = 'ENERGY_STORAGE');

INSERT INTO `sys_dict` (`dict_type_code`, `dict_type_name`, `dict_code`, `dict_value`, `sort`, `remark`, `create_time`, `update_time`, `deleted`)
SELECT 'device_group_type', '设备组类型', 'AIR_CONDITIONER', '空调设备组', 2, '物联设备组类型', NOW(), NOW(), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict` WHERE `dict_type_code` = 'device_group_type' AND `dict_code` = 'AIR_CONDITIONER');

INSERT INTO `sys_dict` (`dict_type_code`, `dict_type_name`, `dict_code`, `dict_value`, `sort`, `remark`, `create_time`, `update_time`, `deleted`)
SELECT 'device_group_type', '设备组类型', 'CHILLER', '冷机设备组', 3, '物联设备组类型', NOW(), NOW(), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict` WHERE `dict_type_code` = 'device_group_type' AND `dict_code` = 'CHILLER');
