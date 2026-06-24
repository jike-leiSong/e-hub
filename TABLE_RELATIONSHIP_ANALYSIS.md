# 表关系分析与数据修复方案

## 📊 表结构关系

### 1. console_user (登录用户表)
**用途**: Console平台的登录账号管理

**关键字段**:
- `user_id` - 用户ID（唯一标识）
- `username` - 登录账号
- `user_type` - 用户类型：ADMIN（平台管理员）/ CUSTOMER（客户）
- `aggregator_id` - 聚合商ID（如果是聚合商客户）
- `ent_id` - 企业用户ID（如果是企业客户）

**用户类型说明**:
- **ADMIN**: 平台管理员（原PLATFORM），可管理整个系统
- **CUSTOMER**: 客户用户（原AGGREGATOR/ENT），分为聚合商客户和企业客户

---

### 2. console_customer_product (客户产品开通表)
**用途**: 管理客户开通了哪些产品（负荷聚合、分时电价等）

**关键字段**:
- `user_id` - 关联 console_user.user_id
- `customer_id` - 客户ID（优先企业ID，其次聚合商ID）
- `product_code` - 产品编码（load_aggregation / tariff）
- `enabled` - 是否启用（1启用 0停用）
- `valid_from` / `valid_to` - 有效期

**关系**: `console_customer_product.user_id` → `console_user.user_id`

---

### 3. aggregator_info (聚合商信息表)
**用途**: 存储聚合商的详细信息

**关键字段**:
- `aggregator_id` - 聚合商ID（唯一标识）
- `aggregator_name` - 聚合商名称
- `contact_person` - 联系人
- `contact_phone` - 联系电话

**关系**: `console_user.aggregator_id` → `aggregator_info.aggregator_id`

---

### 4. aggregator_ent (企业用户表)
**用途**: 存储企业用户的详细信息（隶属于某个聚合商）

**关键字段**:
- `ent_id` - 企业用户ID（唯一标识）
- `ent_name` - 企业名称
- `aggregator_id` - 所属聚合商ID
- `longitude` / `latitude` - 地理位置（用于地图展示）
- `total_power` - 总装机容量

**关系**: 
- `console_user.ent_id` → `aggregator_ent.ent_id`
- `aggregator_ent.aggregator_id` → `aggregator_info.aggregator_id`

---

## 🔗 完整关系链

```
console_user (登录账号)
├── user_type = ADMIN: 平台管理员
│   ├── aggregator_id = NULL
│   └── ent_id = NULL
│
└── user_type = CUSTOMER: 客户用户
    ├── 聚合商客户
    │   ├── aggregator_id → aggregator_info.aggregator_id (聚合商详情)
    │   ├── ent_id = NULL
    │   └── console_customer_product.customer_id = aggregator_id
    │
    └── 企业客户
        ├── ent_id → aggregator_ent.ent_id (企业详情)
        │   └── aggregator_ent.aggregator_id → aggregator_info.aggregator_id
        ├── aggregator_id = NULL (或填写所属聚合商ID)
        └── console_customer_product.customer_id = ent_id
```

---

## ❌ 当前数据问题分析

### 现有数据
```sql
-- console_user 表
user_id: 'xintai'
username: 'xintai'
display_name: '鑫泰能源'
user_type: 'CUSTOMER'
aggregator_id: NULL  ❌ 问题1
ent_id: NULL         ❌ 问题2

-- console_customer_product 表
user_id: 'xintai'
customer_id: 'xintai'  ⚠️ 应该关联到实际业务ID
product_code: 'load_aggregation'

-- aggregator_info 表
(无数据) ❌ 问题3

-- aggregator_ent 表
(无数据) ❌ 问题4
```

### 问题详解

#### 问题1: aggregator_id 和 ent_id 都是 NULL
**影响**:
- ✅ 可以登录（认证成功）
- ❌ sessionStorage中没有entId（导致前端API调用失败）
- ❌ 无法关联到具体的聚合商或企业
- ❌ 无法查询业务数据（所有查询都需要entId或aggregatorId）

#### 问题2: customer_id = 'xintai' 但没有对应的业务实体
**影响**:
- ⚠️ 产品开通记录存在，但customer_id指向的实体不存在
- ❌ 无法通过customer_id查询业务数据

#### 问题3: aggregator_info 表无数据
**影响**:
- ❌ 运营总览页面无法加载聚合商信息
- ❌ 无法显示聚合商基本信息

#### 问题4: aggregator_ent 表无数据
**影响**:
- ❌ 运营总览页面无法加载企业列表
- ❌ 用户分布地图无数据
- ❌ 所有企业相关功能无法使用

---

## ✅ 解决方案

### 方案选择

根据业务逻辑，"鑫泰能源"应该是什么角色？

#### 选项A: 鑫泰能源是**聚合商** ✅ 推荐

适用场景：鑫泰能源管理多个企业用户，提供负荷聚合服务

**数据修复步骤**:

```sql
-- Step 1: 在 aggregator_info 中创建聚合商记录
INSERT INTO aggregator_info (
    aggregator_id, aggregator_name, contact_person, contact_phone, 
    status, create_time, update_time
) VALUES (
    'AGG_XINTAI', -- 聚合商ID
    '鑫泰能源',    -- 聚合商名称
    '张三',        -- 联系人（请修改为实际值）
    '13800138000', -- 联系电话（请修改为实际值）
    1,
    NOW(),
    NOW()
);

-- Step 2: 更新 console_user 关联聚合商ID
UPDATE console_user 
SET aggregator_id = 'AGG_XINTAI',
    ent_id = NULL,
    update_time = NOW()
WHERE user_id = 'xintai';

-- Step 3: 更新 console_customer_product 的 customer_id
UPDATE console_customer_product
SET customer_id = 'AGG_XINTAI',
    update_time = NOW()
WHERE user_id = 'xintai' AND product_code = 'load_aggregation';

-- Step 4: 创建测试企业用户（隶属于鑫泰能源）
INSERT INTO aggregator_ent (
    ent_id, ent_name, aggregator_id, 
    longitude, latitude, total_power,
    status, create_time, update_time
) VALUES 
    ('ENT_TEST_01', '测试企业1', 'AGG_XINTAI', '116.407526', '39.904030', 1000.0, 1, NOW(), NOW()),
    ('ENT_TEST_02', '测试企业2', 'AGG_XINTAI', '116.397526', '39.914030', 800.0, 1, NOW(), NOW()),
    ('ENT_TEST_03', '测试企业3', 'AGG_XINTAI', '116.417526', '39.894030', 1200.0, 1, NOW(), NOW());
```

**修复后效果**:
- ✅ 登录后 sessionStorage 有 `aggregatorId = 'AGG_XINTAI'`
- ✅ 前端可以调用运营总览API（传递aggregatorId参数）
- ✅ 运营总览页面可以查询聚合商数据
- ✅ 用户分布地图显示3个测试企业

---

#### 选项B: 鑫泰能源是**企业用户**

适用场景：鑫泰能源本身是一个企业，隶属于某个聚合商

**数据修复步骤**:

```sql
-- Step 1: 先创建上级聚合商
INSERT INTO aggregator_info (
    aggregator_id, aggregator_name, contact_person, contact_phone,
    status, create_time, update_time
) VALUES (
    'AGG_PARENT', -- 上级聚合商ID
    '某聚合商',    -- 上级聚合商名称
    '李四',
    '13900139000',
    1,
    NOW(),
    NOW()
);

-- Step 2: 在 aggregator_ent 中创建企业记录
INSERT INTO aggregator_ent (
    ent_id, ent_name, aggregator_id,
    longitude, latitude, total_power,
    status, create_time, update_time
) VALUES (
    'ENT_XINTAI',  -- 企业ID
    '鑫泰能源',     -- 企业名称
    'AGG_PARENT',  -- 所属聚合商
    '116.407526',  -- 经度（北京示例）
    '39.904030',   -- 纬度
    1500.0,        -- 总装机容量(kW)
    1,
    NOW(),
    NOW()
);

-- Step 3: 更新 console_user 关联企业ID
UPDATE console_user 
SET ent_id = 'ENT_XINTAI',
    aggregator_id = 'AGG_PARENT',  -- 可选：填写所属聚合商
    update_time = NOW()
WHERE user_id = 'xintai';

-- Step 4: 更新 console_customer_product 的 customer_id
UPDATE console_customer_product
SET customer_id = 'ENT_XINTAI',
    update_time = NOW()
WHERE user_id = 'xintai' AND product_code = 'load_aggregation';
```

**修复后效果**:
- ✅ 登录后 sessionStorage 有 `entId = 'ENT_XINTAI'`
- ✅ 前端可以调用运营总览API（传递entId参数）
- ⚠️ 只能查看自己企业的数据（不能查看其他企业）

---

## 🎯 推荐方案：选项A（聚合商）

### 理由

1. **功能完整性**: 
   - 聚合商可以管理多个企业
   - 运营总览页面设计为聚合商视角（查看所有企业数据）

2. **测试便利性**:
   - 可以创建多个测试企业
   - 可以测试地图、列表等功能

3. **符合业务逻辑**:
   - "运营总览"通常是聚合商的管理功能
   - 企业用户一般只查看自己的数据

---

## 📝 执行步骤（选项A）

### Step 1: 检查表是否存在

```bash
cd /Users/sl/Documents/java/enn/e-hub
./export_overview_tables.sh  # 如果表不存在，先执行迁移脚本
```

### Step 2: 执行数据修复SQL

创建修复脚本：

```bash
cat > fix_xintai_data.sql << 'EOF'
-- 修复鑫泰能源数据（作为聚合商）

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
SELECT 'console_user:' AS table_name;
SELECT user_id, username, user_type, aggregator_id, ent_id FROM console_user WHERE user_id = 'xintai';

SELECT 'console_customer_product:' AS table_name;
SELECT user_id, customer_id, product_code FROM console_customer_product WHERE user_id = 'xintai';

SELECT 'aggregator_info:' AS table_name;
SELECT aggregator_id, aggregator_name FROM aggregator_info;

SELECT 'aggregator_ent:' AS table_name;
SELECT ent_id, ent_name, aggregator_id FROM aggregator_ent;
EOF
```

### Step 3: 执行修复

```bash
# 如果aggregator_info和aggregator_ent表还不存在，先迁移
# （参考 OVERVIEW_DATABASE_TABLES.md 中的表迁移步骤）

# 执行修复脚本
mysql -h 127.0.0.1 -u root -proot e_hub < fix_xintai_data.sql
```

### Step 4: 重新登录测试

1. 退出登录，清除sessionStorage
2. 重新登录（用户名: xintai, 密码: xintai123）
3. 检查浏览器开发者工具 → Application → Session Storage
   - 应该有 `aggregatorId = 'AGG_XINTAI'`
4. 访问运营总览页面
   - 应该可以看到API调用
   - 应该可以看到3个测试企业的数据

---

## ⚠️ 注意事项

### 1. 表迁移顺序
必须先迁移基础表：
- `aggregator_info` (P0核心表)
- `aggregator_ent` (P0核心表)

### 2. 字段适配
检查表结构是否有这些字段：
- `aggregator_info`: contact_person, contact_phone, province, city, district
- `aggregator_ent`: longitude, latitude, total_power, province, city, district

如果缺少字段，需要调整SQL。

### 3. 数据一致性
- `console_user.aggregator_id` 必须存在于 `aggregator_info.aggregator_id`
- `console_user.ent_id` 必须存在于 `aggregator_ent.ent_id`
- `aggregator_ent.aggregator_id` 必须存在于 `aggregator_info.aggregator_id`

---

## 📊 修复前后对比

| 项目 | 修复前 | 修复后 (选项A) |
|-----|--------|---------------|
| 登录 | ✅ 成功 | ✅ 成功 |
| sessionStorage.aggregatorId | ❌ null | ✅ 'AGG_XINTAI' |
| sessionStorage.entId | ❌ null | - (聚合商不需要) |
| 前端API调用 | ❌ 不调用（缺少参数） | ✅ 正常调用 |
| 运营总览-聚合商信息 | ❌ 无数据 | ✅ 显示"鑫泰能源" |
| 运营总览-企业列表 | ❌ 无数据 | ✅ 显示3个测试企业 |
| 运营总览-地图分布 | ❌ 空白 | ✅ 显示3个企业位置 |
| 收益统计 | ❌ 空数据 | ⚠️ 空数据（需要迁移收益表） |
| 申报计划 | ❌ 空数据 | ⚠️ 空数据（需要迁移申报表） |

---

## 🎯 总结

**核心问题**: `console_user` 的 `aggregator_id` 和 `ent_id` 都是 NULL

**根本原因**: 登录账号没有关联到具体的业务实体

**解决方案**: 
1. 创建 `aggregator_info` 记录（鑫泰能源作为聚合商）
2. 更新 `console_user.aggregator_id` 关联到聚合商
3. 创建测试企业数据用于功能验证

**预期效果**:
- ✅ 登录后有 aggregatorId
- ✅ 前端可以正常调用API
- ✅ 运营总览页面可以加载基础数据
- ⚠️ 收益、申报等数据仍需后续迁移

---

**创建时间**: 2026-06-23 16:30  
**文档版本**: v1.0
