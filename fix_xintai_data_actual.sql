-- 修复鑫泰能源数据（使用实际的聚合商ID）
-- 聚合商ID: 1711340903453614082

USE e_hub;

-- 1. 聚合商信息已存在，跳过插入
-- aggregator_info 表已有数据

-- 2. 更新登录用户关联聚合商ID
UPDATE console_user
SET aggregator_id = '1711340903453614082',
    ent_id = NULL,
    update_time = DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE user_id = 'xintai';

-- 3. 产品开通记录已正确（customer_id = '1711340903453614082'）
-- console_customer_product 表数据正确，无需修改

-- 4. 企业用户已存在（3个企业）
-- aggregator_ent 表已有数据

-- ============ 验证数据 ============

SELECT '========== console_user ==========' AS section;
SELECT user_id, username, display_name, user_type, aggregator_id, ent_id
FROM console_user
WHERE user_id = 'xintai';

SELECT '========== console_customer_product ==========' AS section;
SELECT user_id, customer_id, product_code, enabled
FROM console_customer_product
WHERE user_id = 'xintai';

SELECT '========== aggregator_info ==========' AS section;
SELECT aggregator_id, aggregator_name, apply_start_time, apply_end_time
FROM aggregator_info
WHERE aggregator_id = '1711340903453614082';

SELECT '========== aggregator_ent ==========' AS section;
SELECT ent_id, ent_name, aggregator_id, longitude, latitude, status
FROM aggregator_ent
WHERE aggregator_id = '1711340903453614082';

SELECT '========== 统计 ==========' AS section;
SELECT
    '鑫泰能源下属企业数' AS metric,
    COUNT(*) AS count
FROM aggregator_ent
WHERE aggregator_id = '1711340903453614082';
