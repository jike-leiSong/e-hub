-- 修复鑫泰能源数据（作为聚合商）
-- 执行前请确保 aggregator_info 和 aggregator_ent 表已创建

USE e_hub;

-- 1. 创建聚合商记录
INSERT INTO aggregator_info (
    aggregator_id, aggregator_name,
    contact_person, contact_phone,
    province, city, district,
    status, create_time, update_time
) VALUES (
    'AGG_XINTAI',
    '鑫泰能源',
    '张三',
    '13800138000',
    '北京市', '北京市', '朝阳区',
    1,
    DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'),
    DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
);

-- 2. 更新登录用户关联聚合商
UPDATE console_user
SET aggregator_id = 'AGG_XINTAI',
    ent_id = NULL,
    update_time = DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE user_id = 'xintai';

-- 3. 更新产品开通记录
UPDATE console_customer_product
SET customer_id = 'AGG_XINTAI',
    update_time = DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
WHERE user_id = 'xintai' AND product_code = 'load_aggregation';

-- 4. 创建测试企业（用于测试运营总览功能）
INSERT INTO aggregator_ent (
    ent_id, ent_name, aggregator_id,
    longitude, latitude, total_power,
    province, city, district,
    status, create_time, update_time
) VALUES
    (
        'ENT_TEST_01', '测试企业A', 'AGG_XINTAI',
        '116.407526', '39.904030', 1000.0,
        '北京市', '北京市', '朝阳区',
        1,
        DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'),
        DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
    ),
    (
        'ENT_TEST_02', '测试企业B', 'AGG_XINTAI',
        '116.397526', '39.914030', 800.0,
        '北京市', '北京市', '海淀区',
        1,
        DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'),
        DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
    ),
    (
        'ENT_TEST_03', '测试企业C', 'AGG_XINTAI',
        '116.417526', '39.894030', 1200.0,
        '北京市', '北京市', '丰台区',
        1,
        DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'),
        DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
    );

-- 验证数据
SELECT '========== console_user ==========' AS section;
SELECT user_id, username, user_type, aggregator_id, ent_id FROM console_user WHERE user_id = 'xintai';

SELECT '========== console_customer_product ==========' AS section;
SELECT user_id, customer_id, product_code FROM console_customer_product WHERE user_id = 'xintai';

SELECT '========== aggregator_info ==========' AS section;
SELECT aggregator_id, aggregator_name FROM aggregator_info;

SELECT '========== aggregator_ent ==========' AS section;
SELECT ent_id, ent_name, aggregator_id FROM aggregator_ent;
