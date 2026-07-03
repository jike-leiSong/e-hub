UPDATE console_user
SET tenant_id = CASE
    WHEN ent_id IS NOT NULL AND ent_id != '' THEN ent_id
    WHEN aggregator_id IS NOT NULL AND aggregator_id != '' THEN aggregator_id
    ELSE user_id
END
WHERE tenant_id IS NULL OR tenant_id = '';

INSERT INTO console_tenant (
    tenant_id,
    tenant_name,
    tenant_type,
    status,
    aggregator_id,
    ent_id,
    owner_user_id,
    create_time,
    update_time
)
SELECT
    u.tenant_id,
    COALESCE(NULLIF(u.display_name, ''), u.username, u.user_id),
    CASE
        WHEN UPPER(u.user_type) = 'ADMIN' THEN 'PLATFORM'
        WHEN u.ent_id IS NOT NULL AND u.ent_id != '' THEN 'ENT'
        WHEN u.aggregator_id IS NOT NULL AND u.aggregator_id != '' THEN 'AGGREGATOR'
        ELSE 'ACCOUNT'
    END,
    IFNULL(u.status, 1),
    u.aggregator_id,
    u.ent_id,
    u.user_id,
    COALESCE(u.create_time, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')),
    COALESCE(u.update_time, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'))
FROM console_user u
LEFT JOIN console_tenant t ON t.tenant_id = u.tenant_id
WHERE u.tenant_id IS NOT NULL
  AND u.tenant_id != ''
  AND t.id IS NULL;

INSERT INTO console_tenant_product (
    tenant_id,
    product_code,
    enabled,
    valid_from,
    valid_to,
    create_time,
    update_time
)
SELECT
    t.tenant_id,
    p.product_code,
    p.enabled,
    p.valid_from,
    p.valid_to,
    COALESCE(p.create_time, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')),
    COALESCE(p.update_time, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'))
FROM console_customer_product p
INNER JOIN console_tenant t ON t.tenant_id = p.customer_id
LEFT JOIN console_tenant_product tp ON tp.tenant_id = t.tenant_id AND tp.product_code = p.product_code
WHERE tp.id IS NULL;
