# Service和Mapper清理方案

## 📊 当前状态

- **console/service**：204个Service
- **service/service**：23个Service  
- **service/mapper**：100个Mapper

## 🎯 保留的核心Service（约13个）

根据Controller分析，需要保留以下Service：

### Console Controller使用的Service

1. **IYesterdayService** - YesterdayController使用
2. **ITomorrowService** - TomorrowController使用
3. **ProfitService** - ProfitController使用
4. **IEntUserDetailService** - EntUserDetailController使用
5. **IHistoryQueryService** - HistoryQueryController使用
6. **ITodayService** - TodayController使用
7. **IAggregatorApplyPlanService** - AggregatorApplyPlanController使用
8. **WeatherService** - WeatherController使用
9. **IAggregatorResourceTypeService** - 资源类型服务
10. **IAggregatorEntService** - 企业服务
11. **IAggregatorDateHolidayService** - 节假日服务
12. **IAggregatorInfoService** - 聚合商信息服务
13. **IAggregatorEntDapChartService** - 图表服务

### 工具类（保留）
- RedisUtil
- DateUtils
- DingUtil

---

## ⚠️ 清理风险评估

### 高风险
由于Service之间存在复杂的依赖关系：
- Service A 可能调用 Service B
- Service B 可能调用 Service C
- Mapper 被多个Service共享

**问题：**
1. 如果删除Service B，Service A会编译失败
2. 如果删除Mapper，多个Service会失败
3. 需要递归分析所有依赖关系

### 工作量评估
- 分析所有Service依赖：2-3小时
- 逐个验证和删除：3-4小时
- 编译测试和修复：2-3小时
- **总计：7-10小时**

---

## 💡 推荐方案

### 方案一：保守清理（推荐）✅

**只删除明显无用的Service**

删除以下类型的Service：
1. 名称包含"Guangzhou"的Service（广州相关，约50个）
2. 名称包含"BigScreen"的Service（大屏相关，约10个）
3. 名称包含"DataSupport"的Service（数据支持，约5个）

**优点：**
- 风险低
- 工作量小（30分钟）
- 不影响核心功能

**缺点：**
- 清理不彻底
- 仍有部分无用代码

---

### 方案二：彻底清理

**完全清理所有无用Service和Mapper**

**步骤：**
1. 分析所有Controller使用的Service
2. 递归分析Service的依赖关系
3. 找出所有被使用的Mapper
4. 删除未被使用的Service和Mapper
5. 编译测试并修复错误

**优点：**
- 代码最精简
- 完全清理无用代码

**缺点：**
- 工作量大（7-10小时）
- 风险高
- 可能需要多次调试

---

### 方案三：延后清理

**暂不清理，标记为TODO**

**理由：**
1. 当前编译已有警告，但不影响运行
2. 核心功能已完整保留
3. 可以在后续迭代中逐步清理

**优点：**
- 零风险
- 节省时间

**缺点：**
- 代码冗余

---

## 📋 我的建议

**推荐方案一：保守清理**

### 执行步骤

1. **删除Guangzhou相关Service**（约50个）
   ```bash
   cd e-hub-console/src/main/java/cn/sl/ehub/console/service
   rm -f Guangzhou*.java
   
   cd e-hub-service/src/main/java/cn/sl/ehub/service/service
   rm -f Guangzhou*.java
   ```

2. **删除BigScreen相关Service**（约10个）
   ```bash
   rm -f BigScreen*.java
   ```

3. **删除DataSupport相关Service**（约5个）
   ```bash
   rm -f *DataSupport*.java
   ```

4. **编译测试**
   ```bash
   mvn clean compile -DskipTests -rf :e-hub-console
   ```

5. **如果有错误，恢复对应文件**

### 预期效果
- 删除约65个明显无用的Service
- 清理率：约30%
- 风险：低
- 时间：30分钟

---

## ❓ 请选择方案

1. **方案一**：保守清理（推荐，30分钟）
2. **方案二**：彻底清理（7-10小时）
3. **方案三**：延后清理（暂不处理）

**请告诉我你选择哪个方案？**
