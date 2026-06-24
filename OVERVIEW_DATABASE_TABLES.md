# 运营总览页面功能与数据库表依赖分析

## 📊 页面功能模块概览

运营总览页面包含以下6个核心功能模块：

```
运营总览页面
├── 上次申报总收益 (lastApply)
├── 申报计划 (applyPlan)
├── 实时汇总 (realTime)
├── 用户分布 (userDistribution)
├── 当月收益统计 (income)
└── 详情页面 (detail/userDetail/profitStatics)
```

---

## 🔍 功能模块详细分析

### 1. 上次申报总收益 (lastApply)

**组件**: `lastApply.vue`

**使用API**:
- `getLastProfit` - 查询上次申报总收益

**对应Controller**: 
- `YesterdayController.getLastProfit()`

**需要的数据库表**:
```sql
✅ aggregator_date_profit          -- 聚合商日期收益表（主表）
   - id
   - aggregator_id                 -- 聚合商ID
   - date                          -- 日期
   - total_profit                  -- 总收益
   - aggregator_profit             -- 聚合商收益
   - ent_profit                    -- 企业收益
   - create_time
   - update_time
```

**状态**: ⚠️ 需要确认表是否已迁移

---

### 2. 申报计划 (applyPlan)

**组件**: `applyPlan.vue`

**使用API**:
- `getPlanList` - 查询计划列表
- `queryReferenceDailyPower` - 查询参考日功率
- `addEditPlan` - 新增/编辑计划
- `getPlanDetail` - 查询计划详情
- `getPlanDistribution` - 查询计划分布

**对应Controller**: 
- `AggregatorApplyPlanController` (已完成 ✅)

**需要的数据库表**:
```sql
✅ aggregator_apply_plan            -- 聚合商申报计划表（主表）
   - id
   - aggregator_id                  -- 聚合商ID
   - date                           -- 申报日期
   - plan_capacity                  -- 计划容量
   - plan_status                    -- 计划状态
   - create_time
   - update_time

✅ aggregator_date_apply_detail     -- 日期申报详情表
   - id
   - apply_plan_id                  -- 关联申报计划ID
   - time_point                     -- 时间点(0-95，96个点)
   - capacity                       -- 容量值
   - create_time

✅ aggregator_date_apply_detail_offer  -- 申报报价表
   - id
   - apply_plan_id
   - time_point
   - price                          -- 报价
   - create_time
```

**状态**: ⚠️ 需要确认表是否已迁移

---

### 3. 实时汇总 (realTime)

**组件**: `realTime.vue`

**使用API**:
- `getOverview` - 获取总览数据

**对应Controller**: 
- `YesterdayController.getOverview()` / `TodayController` / `TomorrowController`

**需要的数据库表**:
```sql
✅ aggregator_date_profit           -- 聚合商日期收益（主表）

✅ aggregator_apply_plan            -- 聚合商申报计划

✅ aggregator_ent                   -- 企业信息表
   - id
   - ent_id                         -- 企业ID
   - ent_name                       -- 企业名称
   - aggregator_id                  -- 所属聚合商ID
   - status                         -- 状态
   - create_time
   - update_time

✅ aggregator_ent_device            -- 企业设备表
   - id
   - device_id                      -- 设备ID
   - device_name                    -- 设备名称
   - ent_id                         -- 所属企业ID
   - rated_power                    -- 额定功率
   - status                         -- 状态
   - create_time
```

**状态**: ⚠️ 需要确认表是否已迁移

---

### 4. 用户分布 (userDistribution)

**组件**: `userDistribution.vue`

**使用API**:
- `getEntUserDetailRespList` - 获取企业用户详情列表

**对应Controller**: 
- `EntUserDetailController.getEntUserDetailRespList()` (已完成 ✅)

**需要的数据库表**:
```sql
✅ aggregator_ent                   -- 企业信息表（主表）
   - ent_id
   - ent_name
   - aggregator_id
   - longitude                      -- 经度（地图定位）
   - latitude                       -- 纬度（地图定位）
   - total_power                    -- 最大调节容量
   - address                        -- 地址
   - contact                        -- 联系人
   - phone                          -- 电话

✅ aggregator_ent_device            -- 企业设备表
   - device_id
   - ent_id
   - rated_power                    -- 用于汇总企业总容量
```

**状态**: ⚠️ 需要确认表是否已迁移

---

### 5. 当月收益统计 (income)

**组件**: `income.vue`

**使用API**:
- `getWeekProfit` - 获取周收益数据

**对应Controller**: 
- `ProfitController.getWeekProfit()` (已完成 ✅)

**需要的数据库表**:
```sql
✅ aggregator_date_profit           -- 聚合商日期收益表（主表）
   - date                           -- 日期
   - aggregator_id
   - aggregator_profit              -- 聚合商收益
   - ent_profit                     -- 用户收益
   - total_profit                   -- 总收益
```

**状态**: ⚠️ 需要确认表是否已迁移

---

### 6. 详情页面

#### 6.1 昨日/今日/明日详情 (detail.vue)

**使用API**:
- `getResourceTypeList` - 获取资源类型列表
- `getDeviceList` - 获取设备列表
- `getEntUserDeviceChartResp` - 企业用户设备曲线
- `getTodayEntUserDeviceChartResp` - 今日设备曲线
- `getTomorrowEntUserDeviceChartResp` - 明日设备曲线
- `getEntUserOverviewResp` - 企业用户总览
- `entInvite` - 企业邀约
- `getIotLog` - IoT日志

**对应Controller**: 
- `YesterdayController` (已完成 ✅)
- `TodayController` (已完成 ✅)
- `TomorrowController` (已完成 ✅)

**需要的数据库表**:
```sql
✅ aggregator_resource_type         -- 资源类型表
   - id
   - resource_type_id               -- 资源类型ID
   - resource_type_name             -- 资源类型名称
   - aggregator_id

✅ aggregator_ent_device            -- 企业设备表

✅ aggregator_device_date_base_line_load_chart  -- 设备基线负荷曲线
   - id
   - device_id
   - date
   - time_point                     -- 时间点(0-95)
   - load_value                     -- 负荷值
   - create_time

✅ aggregator_device_date_delivery_chart  -- 设备交割曲线
   - id
   - device_id
   - date
   - time_point
   - delivery_value                 -- 交割值

✅ aggregator_device_date_issue_chart  -- 设备下发曲线
   - id
   - device_id
   - date
   - time_point
   - issue_value                    -- 下发值

✅ aggregator_ent_date_invite_detail  -- 企业日期邀约详情
   - id
   - ent_id
   - date
   - invite_status                  -- 邀约状态
   - create_time

✅ aggregator_ent_device_iot_log    -- 设备IoT日志
   - id
   - device_id
   - ent_id
   - log_time
   - operation                      -- 操作
   - result                         -- 结果
```

**状态**: ⚠️ 需要确认表是否已迁移

---

#### 6.2 用户详情 (userDetail.vue)

**使用API**:
- `getEntUserOptions` - 获取企业用户选项
- `getEntUserDetailList` - 获取企业用户详情列表
- `getEntUserDetailListV2` - 获取企业用户详情列表V2
- `getEntUserDetailPercentOptions` - 获取百分比选项
- `autoUpdateEnt` - 自动更新企业
- `getCimDeviceList` - 获取CIM设备列表
- `updateEnt` - 更新企业

**对应Controller**: 
- `EntUserDetailController` (已完成 ✅)

**需要的数据库表**:
```sql
✅ aggregator_ent                   -- 企业信息表（主表）
✅ aggregator_ent_device            -- 企业设备表
✅ aggregator_ent_date_profit       -- 企业日期收益表
   - id
   - ent_id
   - date
   - profit                         -- 收益
   - capacity                       -- 容量
   - create_time

✅ aggregator_ent_apply_plan        -- 企业申报计划表
   - id
   - ent_id
   - date
   - plan_status
   - capacity
```

**状态**: ⚠️ 需要确认表是否已迁移

---

#### 6.3 收益统计 (profitStatics.vue)

**使用API**:
- `getProfitList` - 获取收益列表
- `getListByEntIdList` - 根据企业ID列表获取收益
- `getContentList` - 获取内容列表

**对应Controller**: 
- `ProfitController` (已完成 ✅)

**需要的数据库表**:
```sql
✅ aggregator_date_profit           -- 聚合商日期收益表（主表）
✅ aggregator_ent_date_profit       -- 企业日期收益表
✅ aggregator_device_date_profit    -- 设备日期收益表
   - id
   - device_id
   - date
   - profit                         -- 设备收益
   - capacity                       -- 设备容量
```

**状态**: ⚠️ 需要确认表是否已迁移

---

## 📋 核心数据库表汇总

### 优先级 P0 - 必须迁移（运营总览页面必需）

| 序号 | 表名 | 用途 | 依赖模块 | 状态 |
|------|------|------|----------|------|
| 1 | **aggregator_info** | 聚合商信息 | 所有 | ⚠️ 待迁移 |
| 2 | **aggregator_ent** | 企业信息 | 用户分布、用户详情 | ⚠️ 待迁移 |
| 3 | **aggregator_ent_device** | 企业设备 | 实时汇总、用户分布 | ⚠️ 待迁移 |
| 4 | **aggregator_date_profit** | 聚合商日期收益 | 上次申报、收益统计 | ⚠️ 待迁移 |
| 5 | **aggregator_ent_date_profit** | 企业日期收益 | 用户详情、收益统计 | ⚠️ 待迁移 |
| 6 | **aggregator_apply_plan** | 聚合商申报计划 | 申报计划 | ⚠️ 待迁移 |
| 7 | **aggregator_date_apply_detail** | 日期申报详情 | 申报计划 | ⚠️ 待迁移 |
| 8 | **aggregator_date_apply_detail_offer** | 申报报价 | 申报计划 | ⚠️ 待迁移 |
| 9 | **aggregator_resource_type** | 资源类型 | 详情页面 | ⚠️ 待迁移 |

### 优先级 P1 - 重要功能表

| 序号 | 表名 | 用途 | 依赖模块 | 状态 |
|------|------|------|----------|------|
| 10 | **aggregator_ent_apply_plan** | 企业申报计划 | 用户详情 | ⚠️ 待迁移 |
| 11 | **aggregator_device_date_profit** | 设备日期收益 | 收益统计 | ⚠️ 待迁移 |
| 12 | **aggregator_device_date_base_line_load_chart** | 设备基线负荷曲线 | 详情页面 | ⚠️ 待迁移 |
| 13 | **aggregator_device_date_delivery_chart** | 设备交割曲线 | 详情页面 | ⚠️ 待迁移 |
| 14 | **aggregator_device_date_issue_chart** | 设备下发曲线 | 详情页面 | ⚠️ 待迁移 |
| 15 | **aggregator_ent_date_invite_detail** | 企业日期邀约详情 | 详情页面 | ⚠️ 待迁移 |
| 16 | **aggregator_ent_device_iot_log** | 设备IoT日志 | 详情页面 | ⚠️ 待迁移 |

### 优先级 P2 - 辅助功能表

| 序号 | 表名 | 用途 | 依赖模块 | 状态 |
|------|------|------|----------|------|
| 17 | **aggregator_date_holiday** | 日期假期 | 日期选择 | ⚠️ 待迁移 |
| 18 | **aggregator_ent_app_apply_plan** | APP企业申报计划 | APP申报 | ⚠️ 待迁移 |

---

## 🔧 数据库表DDL导出方案

### Step 1: 从原项目数据库导出表结构

```bash
#!/bin/bash
# 数据库连接信息（根据E-HUB_UPGRADE_PLAN.md）
DB_HOST="10.39.41.241"
DB_PORT="3306"
DB_NAME="load-aggregator"
DB_USER="load_admin_r"
DB_PASS="pAmBiCabP4Qz7ojG50RrcZqX"

OUTPUT_DIR="./sql/overview_tables"
mkdir -p $OUTPUT_DIR

# P0核心表（运营总览必需）
TABLES_P0=(
    "aggregator_info"
    "aggregator_ent"
    "aggregator_ent_device"
    "aggregator_date_profit"
    "aggregator_ent_date_profit"
    "aggregator_apply_plan"
    "aggregator_date_apply_detail"
    "aggregator_date_apply_detail_offer"
    "aggregator_resource_type"
)

echo "导出P0核心表结构..."
for table in "${TABLES_P0[@]}"; do
    echo "导出 $table ..."
    mysqldump -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS \
        --no-data --skip-add-drop-table \
        $DB_NAME $table > "$OUTPUT_DIR/${table}.sql"
done

# P1重要表
TABLES_P1=(
    "aggregator_ent_apply_plan"
    "aggregator_device_date_profit"
    "aggregator_device_date_base_line_load_chart"
    "aggregator_device_date_delivery_chart"
    "aggregator_device_date_issue_chart"
    "aggregator_ent_date_invite_detail"
    "aggregator_ent_device_iot_log"
)

echo "导出P1重要表结构..."
for table in "${TABLES_P1[@]}"; do
    echo "导出 $table ..."
    mysqldump -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS \
        --no-data --skip-add-drop-table \
        $DB_NAME $table > "$OUTPUT_DIR/${table}.sql"
done

echo "导出完成！文件位置: $OUTPUT_DIR"
```

### Step 2: 合并SQL文件

```bash
# 合并P0核心表
cat $OUTPUT_DIR/aggregator_info.sql \
    $OUTPUT_DIR/aggregator_ent.sql \
    $OUTPUT_DIR/aggregator_ent_device.sql \
    $OUTPUT_DIR/aggregator_date_profit.sql \
    $OUTPUT_DIR/aggregator_ent_date_profit.sql \
    $OUTPUT_DIR/aggregator_apply_plan.sql \
    $OUTPUT_DIR/aggregator_date_apply_detail.sql \
    $OUTPUT_DIR/aggregator_date_apply_detail_offer.sql \
    $OUTPUT_DIR/aggregator_resource_type.sql \
    > $OUTPUT_DIR/00_overview_core_tables.sql

# 合并P1重要表
cat $OUTPUT_DIR/aggregator_ent_apply_plan.sql \
    $OUTPUT_DIR/aggregator_device_date_profit.sql \
    $OUTPUT_DIR/aggregator_device_date_base_line_load_chart.sql \
    $OUTPUT_DIR/aggregator_device_date_delivery_chart.sql \
    $OUTPUT_DIR/aggregator_device_date_issue_chart.sql \
    $OUTPUT_DIR/aggregator_ent_date_invite_detail.sql \
    $OUTPUT_DIR/aggregator_ent_device_iot_log.sql \
    > $OUTPUT_DIR/01_overview_important_tables.sql
```

---

## ✅ Controller与表的对应关系

| Controller | 状态 | 主要依赖表 |
|-----------|------|-----------|
| **ProfitController** | ✅ 完成 | aggregator_date_profit, aggregator_ent_date_profit |
| **YesterdayController** | ✅ 完成 | aggregator_date_profit, aggregator_resource_type, aggregator_ent_device |
| **TodayController** | ✅ 完成 | aggregator_ent_device, aggregator_ent_device_iot_log |
| **TomorrowController** | ✅ 完成 | aggregator_apply_plan, aggregator_date_apply_detail_offer |
| **EntUserDetailController** | ✅ 完成 | aggregator_ent, aggregator_ent_device, aggregator_ent_date_profit |
| **AggregatorApplyPlanController** | ✅ 完成 | aggregator_apply_plan, aggregator_date_apply_detail |

---

## 🎯 下一步行动

### 立即执行

1. **运行DDL导出脚本**
   ```bash
   chmod +x export_overview_tables.sh
   ./export_overview_tables.sh
   ```

2. **检查导出的SQL文件**
   - 确认所有表都成功导出
   - 检查字符集和引擎设置
   - 适配新项目命名规范

3. **创建Mapper接口**
   - 为每个表创建对应的Mapper接口
   - 继承MyBatis-Plus的BaseMapper
   - 添加自定义查询方法

4. **创建VO实体类**
   - 根据表结构创建VO类
   - 添加MyBatis-Plus注解
   - 配置字段映射

### 中期任务

1. **导入测试数据**
   - 准备最小测试数据集
   - 导入开发环境
   - 验证数据完整性

2. **Service实现类完善**
   - 确认Service实现类存在
   - 补充缺失的业务逻辑
   - 添加事务管理

3. **集成测试**
   - 测试每个功能模块
   - 验证数据查询正确性
   - 检查页面显示效果

---

## ⚠️ 风险提示

### 1. 表结构可能的变化

原项目的表结构可能已经演进，需要：
- 对比生产环境和开发环境的表结构
- 确认字段类型和索引
- 注意外键约束

### 2. 数据量问题

某些表可能数据量很大：
- `aggregator_device_date_*_chart` - 96点曲线数据
- `aggregator_ent_device_iot_log` - IoT日志数据

建议：
- 使用分区表
- 建立合适的索引
- 考虑数据归档策略

### 3. Service实现确认

虽然Service接口已存在，但需要确认：
- Service实现类是否完整
- 复杂查询是否已实现
- 是否有缓存策略

---

## 📊 进度追踪

| 阶段 | 任务 | 状态 | 预计时间 |
|------|------|------|----------|
| 1 | 导出表DDL | ⚠️ 待执行 | 30分钟 |
| 2 | 创建Mapper接口 | ⚠️ 待执行 | 2小时 |
| 3 | 创建VO实体类 | ⚠️ 待执行 | 2小时 |
| 4 | 导入测试数据 | ⚠️ 待执行 | 1小时 |
| 5 | Service实现验证 | ⚠️ 待执行 | 3小时 |
| 6 | 功能测试 | ⚠️ 待执行 | 4小时 |

**总预计时间**: 1.5天

---

**文档版本**: v1.0  
**创建日期**: 2026-06-23  
**负责人**: 系统架构组  
**下次更新**: 完成表导出后
