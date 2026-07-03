-- ============================================================
-- 物联接入凭证表精简迁移
-- 变更说明：
--   1. iot_access_app：删除 ent_id、project_id、user_key 三列
--      （这三列从未在鉴权链路中生效，属于业务属性错位）
--   2. IotAccessApp VO 同步删除了这三个字段
-- 执行前提：确认 iot_access_app 表中 project_id、user_key 列均为 NULL
-- ============================================================

BEGIN;

-- ── 1. 删除 iot_access_app 中冗余字段 ──────────────────────────
ALTER TABLE `iot_access_app`
    DROP COLUMN IF EXISTS `ent_id`,
    DROP COLUMN IF EXISTS `project_id`,
    DROP COLUMN IF EXISTS `user_key`;

-- ── 2. 更新 mock 凭证脚本参考（执行前先确认表中无其他有效数据）──
-- 以下为清理后的插入示例：
-- INSERT INTO `iot_access_app`
--     (`source_code`, `source_name`, `aggregator_id`,
--      `access_key`, `enabled`, `remark`, `create_time`, `update_time`)
-- VALUES
--     ('MOCK_IOT', '物联模拟数据接入', '1711340903453614182',
--      'mock-access-key-2024', 1,
--      '模拟推送凭证，仅需 accessKey 鉴权',
--      NOW(), NOW());

COMMIT;
