-- iot_access_app mock 凭证
-- aggregator_id: 1711340903453614182
BEGIN;

INSERT INTO `iot_access_app`
    (`source_code`, `source_name`, `aggregator_id`,
     `access_key`, `enabled`, `remark`, `create_time`, `update_time`)
VALUES
    ('MOCK_IOT', '物联模拟数据接入', '1711340903453614182',
     'mock-access-key-2024', 1,
     '模拟推送凭证，仅需 accessKey 鉴权',
     NOW(), NOW());

COMMIT;
