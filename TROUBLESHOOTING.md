# 聚合商用户登录后不显示负荷聚合功能 - 排查指南

## 问题描述
用户 `xintai` 在 `console_customer_product` 表中有 `load_aggregation` 产品记录，但登录后不显示负荷聚合功能。

## 数据确认

### 用户信息
```sql
SELECT * FROM console_user WHERE user_id = 'xintai';
```

期望结果：
- user_id: 'xintai'
- user_type: 'CUSTOMER'
- aggregator_id: NULL
- ent_id: NULL
- status: 1

### 产品开通记录
```sql
SELECT * FROM console_customer_product WHERE user_id = 'xintai';
```

期望结果：
- user_id: 'xintai'
- customer_id: 'xintai'
- product_code: 'load_aggregation'
- enabled: 1
- valid_from: '2026-06-06' (应该 <= 今天)
- valid_to: '2029-06-06' (应该 >= 今天)

## 排查步骤

### 1. 验证SQL查询逻辑

运行测试SQL：
```bash
mysql -u用户名 -p数据库名 < test_query.sql
```

这个脚本会：
- 显示当前日期
- 模拟后端查询逻辑
- 检查每个条件是否满足

### 2. 检查应用日志

我已经在 `ConsoleProductService.enabledProducts()` 方法中添加了详细日志。

重启应用后，登录 xintai 用户，查看日志输出：

```
DEBUG: Querying products for userId=xintai, customerIds=[xintai]
DEBUG: Found product codes: [load_aggregation]
DEBUG: Normalized product codes: [load_aggregation]
```

**如果看到异常日志**：
```
ERROR: Failed to query enabled products for user xintai
```
说明查询过程中抛出了异常，查看完整的堆栈信息。

### 3. 检查前端接收的数据

打开浏览器开发者工具（F12），查看 `/auth/me` 接口的响应：

```json
{
  "code": 200,
  "data": {
    "userId": "xintai",
    "username": "xintai",
    "userType": "CUSTOMER",
    "platformType": "customer",
    "products": ["load_aggregation"],  // 应该包含这个
    "allowedPages": ["load-overview", "load-history", "load-resources"],
    "defaultPage": "load-overview"
  }
}
```

**关键检查点**：
- `products` 数组是否包含 `"load_aggregation"`
- `allowedPages` 是否包含负荷聚合相关页面
- `defaultPage` 是否正确

### 4. 检查前端逻辑

前端在 `PlatformWorkbench.vue` 中判断是否显示负荷聚合：

```javascript
if (this.user.products.includes("load_aggregation")) {
  actions.push({ key: "load-overview", label: "进入负荷聚合" });
}
```

在浏览器控制台中检查：
```javascript
// 查看当前用户信息
sessionStorage.getItem('ehub-auth-user')
```

应该能看到包含 `"products":["load_aggregation"]` 的JSON。

## 常见问题和解决方案

### 问题1：数据库中没有产品记录

**解决**：执行 `fix_product_issue.sql` 中的步骤1

```sql
INSERT INTO console_customer_product (user_id, customer_id, product_code, enabled, create_time, update_time)
VALUES ('xintai', 'xintai', 'load_aggregation', 1, NOW(), NOW());
```

### 问题2：customer_id 不匹配

**原因**：customer_id 应该等于 `COALESCE(ent_id, aggregator_id, user_id)`

**解决**：执行 `fix_product_issue.sql` 中的步骤2

```sql
UPDATE console_customer_product p
INNER JOIN console_user u ON p.user_id = u.user_id
SET p.customer_id = COALESCE(NULLIF(u.ent_id, ''), NULLIF(u.aggregator_id, ''), u.user_id)
WHERE p.user_id = 'xintai';
```

### 问题3：日期范围不在有效期内

**检查**：
```sql
SELECT 
    product_code,
    valid_from,
    valid_to,
    CURDATE() AS today,
    valid_from <= CURDATE() AS from_valid,
    valid_to >= CURDATE() AS to_valid
FROM console_customer_product
WHERE user_id = 'xintai';
```

**解决**：调整日期范围
```sql
UPDATE console_customer_product
SET valid_from = NULL,
    valid_to = NULL
WHERE user_id = 'xintai' AND product_code = 'load_aggregation';
```

### 问题4：enabled 字段为 0

**检查**：
```sql
SELECT user_id, product_code, enabled
FROM console_customer_product
WHERE user_id = 'xintai';
```

**解决**：
```sql
UPDATE console_customer_product
SET enabled = 1
WHERE user_id = 'xintai' AND product_code = 'load_aggregation';
```

### 问题5：后端查询异常被吞掉

**原因**：`enabledProducts()` 方法中的 try-catch 会吞掉异常

**解决**：查看应用日志中的 ERROR 日志，我已经添加了异常打印

### 问题6：前端缓存问题

**解决**：清除浏览器缓存和 sessionStorage
```javascript
// 在浏览器控制台执行
sessionStorage.clear();
location.reload();
```

## 完整测试流程

1. 执行 `test_query.sql` 验证数据
2. 如果查询结果为空，执行 `fix_product_issue.sql`
3. 重启后端应用
4. 清除浏览器缓存
5. 重新登录 xintai 用户
6. 检查日志和前端数据
7. 验证负荷聚合功能是否显示

## 代码改进建议

### 1. 移除异常吞没
当前代码会静默吞掉异常，建议至少记录日志：
```java
} catch (Exception e) {
    log.error("Failed to query enabled products for user {}", user.getUserId(), e);
    return Collections.emptyList();
}
```

### 2. 添加数据校验
在保存产品开通时，验证 customer_id 计算逻辑：
```java
String customerId = customerId(user.getAggregatorId(), user.getEntId(), user.getUserId());
```

### 3. 前端错误提示
如果后端返回空的 products 数组，前端应该提示用户联系管理员。
