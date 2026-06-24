-- 测试查询：模拟后端查询产品代码的SQL
-- 用户：xintai (aggregatorId=NULL, entId=NULL, userId='xintai')

-- 这是后端实际执行的查询逻辑
SELECT DISTINCT product_code
FROM console_customer_product
WHERE enabled = 1
  AND (valid_from IS NULL OR valid_from = '' OR valid_from <= CURDATE())
  AND (valid_to IS NULL OR valid_to = '' OR valid_to >= CURDATE())
  AND (
      user_id = 'xintai'
      OR customer_id IN ('xintai')
  )
ORDER BY product_code;

-- 调试信息：查看当前日期
SELECT CURDATE() AS current_date;

-- 调试信息：查看记录的详细信息
SELECT
    user_id,
    customer_id,
    product_code,
    enabled,
    valid_from,
    valid_to,
    valid_from <= CURDATE() AS valid_from_check,
    valid_to >= CURDATE() AS valid_to_check,
    CASE
        WHEN enabled = 1
             AND (valid_from IS NULL OR valid_from = '' OR valid_from <= CURDATE())
             AND (valid_to IS NULL OR valid_to = '' OR valid_to >= CURDATE())
             AND (user_id = 'xintai' OR customer_id = 'xintai')
        THEN '✓ 满足条件'
        ELSE '✗ 不满足条件'
    END AS query_match
FROM console_customer_product
WHERE user_id = 'xintai' OR customer_id = 'xintai';

-- 检查用户信息
SELECT
    user_id,
    username,
    display_name,
    user_type,
    aggregator_id,
    ent_id,
    status
FROM console_user
WHERE user_id = 'xintai';
