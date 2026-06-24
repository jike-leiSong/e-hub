-- 诊断聚合商用户产品开通问题
-- 1. 查看所有用户的类型和ID信息
SELECT
    user_id,
    username,
    display_name,
    user_type,
    aggregator_id,
    ent_id,
    status
FROM console_user
ORDER BY user_type, user_id;

-- 2. 查看所有产品开通记录
SELECT
    id,
    user_id,
    customer_id,
    product_code,
    enabled,
    valid_from,
    valid_to,
    create_time
FROM console_customer_product
ORDER BY user_id, product_code;

-- 3. 查看客户类型用户的产品开通情况（左连接，包含未开通的）
SELECT
    u.user_id,
    u.username,
    u.display_name,
    u.user_type,
    u.aggregator_id,
    u.ent_id,
    p.product_code,
    p.enabled,
    COALESCE(NULLIF(u.ent_id, ''), NULLIF(u.aggregator_id, ''), u.user_id) AS computed_customer_id
FROM console_user u
LEFT JOIN console_customer_product p
    ON p.user_id = u.user_id
WHERE UPPER(u.user_type) = 'CUSTOMER'
ORDER BY u.user_id, p.product_code;

-- 4. 找出customer_id不匹配的记录（可能导致查询失败）
SELECT
    u.user_id,
    u.username,
    u.aggregator_id,
    u.ent_id,
    COALESCE(NULLIF(u.ent_id, ''), NULLIF(u.aggregator_id, ''), u.user_id) AS expected_customer_id,
    p.customer_id AS actual_customer_id,
    p.product_code,
    CASE
        WHEN p.customer_id = COALESCE(NULLIF(u.ent_id, ''), NULLIF(u.aggregator_id, ''), u.user_id)
        THEN 'MATCH'
        ELSE 'MISMATCH'
    END AS match_status
FROM console_user u
LEFT JOIN console_customer_product p
    ON p.user_id = u.user_id
WHERE UPPER(u.user_type) = 'CUSTOMER';
