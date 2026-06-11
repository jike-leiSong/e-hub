todo

新增电网服务预警接口  每15分钟检查一次

SQL
```
CREATE TABLE `tripart_service_alert` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tripart_code` varchar(45) NOT NULL COMMENT '第三方编码',
  `name` varchar(45) NOT NULL COMMENT '姓名',
  `phone` varchar(11) NOT NULL COMMENT '手机号',
  `status` varchar(2) NOT NULL COMMENT '删除标识 0 删除 ',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='第三方服务告警'
```

短信模板
```
告警通知：${tripartName} 服务不可用，请及时处理！
```

```
SELECT * FROM `load-aggregator`.aggregator_ent;
新增：
state_grid_code
state_grid_name

```
