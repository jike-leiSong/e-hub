-- 修复 console_user 表的 ent_id
-- 聚合商用户不应该有 ent_id，只有 aggregator_id

USE e_hub;

-- 查看当前数据
SELECT user_id, username, user_type, aggregator_id, ent_id 
FROM console_user 
WHERE user_id = 'xintai';

-- 修复：聚合商用户的 ent_id 应该为 NULL
UPDATE console_user 
SET ent_id = NULL,
    update_time = NOW()
WHERE user_id = 'xintai' AND user_type = 'CUSTOMER';

-- 验证修复结果
SELECT user_id, username, user_type, aggregator_id, ent_id 
FROM console_user 
WHERE user_id = 'xintai';
