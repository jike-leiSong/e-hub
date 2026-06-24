-- 修复聚合商用户产品开通问题
-- 请先运行 diagnose_product_issue.sql 查看问题，再执行此脚本

-- 步骤1：为所有CUSTOMER类型用户添加负荷聚合产品（如果不存在）
INSERT INTO console_customer_product (user_id, customer_id, product_code, enabled, create_time, update_time)
SELECT
    u.user_id,
    COALESCE(NULLIF(u.ent_id, ''), NULLIF(u.aggregator_id, ''), u.user_id) AS customer_id,
    'load_aggregation' AS product_code,
    1 AS enabled,
    DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s') AS create_time,
    DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s') AS update_time
FROM console_user u
WHERE UPPER(u.user_type) = 'CUSTOMER'
  AND u.status = 1
  AND NOT EXISTS (
      SELECT 1
      FROM console_customer_product p
      WHERE p.user_id = u.user_id
        AND p.product_code = 'load_aggregation'
  );

-- 步骤2：修正customer_id不匹配的记录
UPDATE console_customer_product p
INNER JOIN console_user u ON p.user_id = u.user_id
SET p.customer_id = COALESCE(NULLIF(u.ent_id, ''), NULLIF(u.aggregator_id, ''), u.user_id),
    p.update_time = DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE UPPER(u.user_type) = 'CUSTOMER'
  AND p.customer_id != COALESCE(NULLIF(u.ent_id, ''), NULLIF(u.aggregator_id, ''), u.user_id);

-- 步骤3：验证修复结果
SELECT
    u.user_id,
    u.username,
    u.display_name,
    u.user_type,
    u.aggregator_id,
    u.ent_id,
    p.customer_id,
    p.product_code,
    p.enabled,
    CASE
        WHEN p.customer_id = COALESCE(NULLIF(u.ent_id, ''), NULLIF(u.aggregator_id, ''), u.user_id)
        THEN '✓ MATCH'
        ELSE '✗ MISMATCH'
    END AS status
FROM console_user u
LEFT JOIN console_customer_product p
    ON p.user_id = u.user_id
WHERE UPPER(u.user_type) = 'CUSTOMER'
ORDER BY u.user_id, p.product_code;
