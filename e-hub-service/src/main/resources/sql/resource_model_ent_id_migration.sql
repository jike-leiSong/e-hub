-- 资源模型表增加企业归属，用于资源设备新增时按企业直接查询模型项目。
-- 如果字段或索引已存在，跳过对应语句即可。

ALTER TABLE `aggregator_single_model_data`
  ADD COLUMN `ent_id` varchar(20) DEFAULT NULL COMMENT '企业ID' AFTER `aggregator_id`;

CREATE INDEX `idx_aggregator_single_model_ent`
  ON `aggregator_single_model_data` (`ent_id`);

UPDATE `aggregator_single_model_data` m
JOIN (
  SELECT `energy_station_code`, MIN(`ent_id`) AS `ent_id`
  FROM `aggregator_ent_device`
  WHERE `ent_id` IS NOT NULL
    AND `ent_id` <> ''
    AND `energy_station_code` IS NOT NULL
    AND `energy_station_code` <> ''
  GROUP BY `energy_station_code`
) d ON d.`energy_station_code` = m.`energy_station_code`
SET m.`ent_id` = d.`ent_id`
WHERE m.`ent_id` IS NULL OR m.`ent_id` = '';
