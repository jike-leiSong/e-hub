# E-Hub 包名重命名方案

## 📋 当前包结构分析

### 当前包名
```
cn.enn.la
```

### 问题分析
- `la` 是 `load-aggregator` 的缩写
- 与新项目名 `e-hub` 不匹配
- 不符合命名规范

---

## 🎯 重命名方案

### 方案一：使用ehub（推荐）✅

**新包名：** `cn.enn.ehub`

**优点：**
- 与项目名e-hub一致
- 简洁明了
- 符合Java包命名规范（全小写）

**包结构：**
```
cn.enn.ehub
├── upstream                    # e-hub-upstream模块
│   ├── EHubUpstreamApplication
│   ├── controller
│   ├── service
│   ├── job
│   ├── dto
│   ├── config
│   └── ...
└── console                     # e-hub-console模块
    ├── EHubConsoleApplication
    ├── grid
    ├── rest
    └── ...
```

**影响范围：**
- 需要修改的Java文件：约800个
- 需要重命名的目录：4个
- 需要更新的配置文件：约10个

---

### 方案二：使用hub

**新包名：** `cn.enn.hub`

**优点：**
- 更简洁
- 通用性强

**缺点：**
- 可能与其他hub项目冲突
- 不够明确

---

### 方案三：保持cn.enn.la，但添加子包

**新包名：** 保持 `cn.enn.la`，但按模块分包

**包结构：**
```
cn.enn.la
├── upstream                    # e-hub-upstream模块
│   ├── EHubUpstreamApplication
│   └── ...
└── console                     # e-hub-console模块
    ├── EHubConsoleApplication
    └── ...
```

**优点：**
- 改动最小
- 只需要移动文件到子包

**缺点：**
- 包名与项目名不匹配
- 不够规范

---

## 💡 推荐方案：方案一（cn.enn.ehub）

### 详细实施步骤

#### 1. e-hub-upstream模块

**当前结构：**
```
e-hub-upstream/src/main/java/cn/enn/la/
├── EHubUpstreamApplication.java
├── controller/
├── service/
├── job/
├── dto/
├── config/
└── ...
```

**目标结构：**
```
e-hub-upstream/src/main/java/cn/enn/ehub/upstream/
├── EHubUpstreamApplication.java
├── controller/
├── service/
├── job/
├── dto/
├── config/
└── ...
```

**修改内容：**
1. 创建新目录：`cn/enn/ehub/upstream/`
2. 移动所有文件到新目录
3. 批量替换package声明：`cn.enn.la` → `cn.enn.ehub.upstream`
4. 批量替换import语句：`cn.enn.la` → `cn.enn.ehub.upstream`
5. 更新配置文件中的包扫描路径

#### 2. e-hub-console模块

**当前结构：**
```
e-hub-console/src/main/java/cn/enn/la/
├── EHubConsoleApplication.java
├── grid/
├── rest/
└── ...
```

**目标结构：**
```
e-hub-console/src/main/java/cn/enn/ehub/console/
├── EHubConsoleApplication.java
├── grid/
├── rest/
└── ...
```

**修改内容：**
1. 创建新目录：`cn/enn/ehub/console/`
2. 移动所有文件到新目录
3. 批量替换package声明：`cn.enn.la` → `cn.enn.ehub.console`
4. 批量替换import语句：`cn.enn.la` → `cn.enn.ehub.console`
5. 更新配置文件中的包扫描路径

#### 3. e-hub-common模块

**当前结构：**
```
e-hub-common/src/main/java/cn/enn/la/
└── ...
```

**目标结构：**
```
e-hub-common/src/main/java/cn/enn/ehub/common/
└── ...
```

#### 4. e-hub-service模块

**当前结构：**
```
e-hub-service/src/main/java/cn/enn/la/
└── ...
```

**目标结构：**
```
e-hub-service/src/main/java/cn/enn/ehub/service/
└── ...
```

---

## 📊 影响评估

### 需要修改的文件类型

| 文件类型 | 数量 | 修改内容 |
|---------|------|---------|
| Java文件 | ~800个 | package声明、import语句 |
| XML配置 | ~10个 | 包扫描路径、mapper路径 |
| YML配置 | ~6个 | 包扫描路径 |
| 目录结构 | 4个模块 | 重命名目录 |

### 修改示例

**Java文件修改：**
```java
// 修改前
package cn.enn.la.controller;
import cn.enn.la.service.DeliveryService;

// 修改后
package cn.enn.ehub.upstream.controller;
import cn.enn.ehub.upstream.service.DeliveryService;
```

**配置文件修改：**
```yaml
# 修改前
mybatis:
  mapper-locations: classpath:/mapper/*Mapper.xml
  type-aliases-package: cn.enn.la.vo

# 修改后
mybatis:
  mapper-locations: classpath:/mapper/*Mapper.xml
  type-aliases-package: cn.enn.ehub.upstream.vo
```

**主类注解修改：**
```java
// 修改前
@SpringBootApplication
@MapperScan(basePackages = "cn.enn.la.mapper")

// 修改后
@SpringBootApplication
@MapperScan(basePackages = "cn.enn.ehub.upstream.mapper")
```

---

## ⏱️ 工作量评估

### 自动化脚本执行时间
- 批量替换package声明：5分钟
- 批量替换import语句：5分钟
- 移动目录结构：2分钟
- 更新配置文件：3分钟

**总计：约15分钟**

### 验证测试时间
- 编译测试：5分钟
- 启动测试：5分钟
- 功能验证：10分钟

**总计：约20分钟**

**整体时间：约35分钟**

---

## ⚠️ 风险评估

### 低风险
- ✅ 纯文本替换，可回滚
- ✅ 不影响业务逻辑
- ✅ 不影响数据库

### 需要注意
- ⚠️ 确保所有import都更新
- ⚠️ 确保配置文件都更新
- ⚠️ 编译后再提交

---

## 🚀 执行计划

### 步骤1：备份（可选）
```bash
# 创建备份分支
git checkout -b backup-before-package-rename
git add .
git commit -m "备份：包名重命名前"
```

### 步骤2：执行重命名脚本
```bash
# 自动化脚本会：
# 1. 创建新目录结构
# 2. 移动所有Java文件
# 3. 批量替换package声明
# 4. 批量替换import语句
# 5. 更新配置文件
```

### 步骤3：验证
```bash
# 编译测试
mvn clean compile -DskipTests

# 启动测试
mvn spring-boot:run
```

### 步骤4：提交
```bash
git add .
git commit -m "重构：包名从cn.enn.la重命名为cn.enn.ehub"
```

---

## 📋 决策清单

请确认以下问题：

1. **包名选择**
   - [ ] 方案一：cn.enn.ehub（推荐）
   - [ ] 方案二：cn.enn.hub
   - [ ] 方案三：保持cn.enn.la

2. **子包结构**
   - [ ] 添加模块子包（upstream/console/common/service）
   - [ ] 不添加子包，直接使用cn.enn.ehub

3. **执行时机**
   - [ ] 立即执行
   - [ ] 稍后执行

---

## 💡 我的建议

**推荐方案：方案一 + 添加模块子包**

**最终包结构：**
```
cn.enn.ehub.upstream    # e-hub-upstream
cn.enn.ehub.console     # e-hub-console
cn.enn.ehub.common      # e-hub-common
cn.enn.ehub.service     # e-hub-service
```

**理由：**
1. 与项目名e-hub完全一致
2. 模块划分清晰
3. 避免包冲突
4. 符合Java命名规范
5. 便于后续扩展

---

**请确认方案后，我将立即执行重命名操作！**
