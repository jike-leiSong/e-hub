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
