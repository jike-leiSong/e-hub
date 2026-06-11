# E-Hub 项目改造执行计划

## 一、保留功能清单

### 1.1 e-hub-upstream（原delivery）保留功能

#### 定时任务（Job）
- ✅ **XinTaiFinalJob** - 鑫泰最终数据处理
- ✅ **PeakPlanDailyDataDeliveryJob** - 调峰计划日数据上送
- ✅ **PeakPlanDeliveryJob** - 调峰计划上送
- ❌ SingleMeasDataDeliveryJob - 删除（改为手动调用接口）
- ❌ TotalDataDeliveryJob - 删除（改为手动调用接口）
- ❌ HuabeiHeartBeatJob - 删除
- ❌ XinTaiSingleMeasDataDeliveryJob - 删除
- ❌ XinTaiTotalDataDeliveryJob - 删除

#### Controller接口
**RetryDeliveryController**
- ✅ `/retryDelivery/singleMeasRetry` - 单体量测补招

**DeliveryController**
- ✅ `/delivery/singleMeasDataDelivery` - 单体量测数据上送
- ✅ `/delivery/singleModelDataDelivery` - 单体模型数据上送
- ✅ `/delivery/peakPlan96PointDeliveryByDate` - 调峰计划96点上送
- ✅ `/delivery/peakPlanDailyDataDelivery` - 调峰计划日数据上送

### 1.2 e-hub-console（原issue + business）保留功能

#### 电网下发（原issue）
**IssueController**
- ✅ `/issue/clearIssue` - 出清下发接口

#### 业务功能（原business）
- ✅ 聚合商管理
- ✅ 企业管理
- ✅ 设备管理
- ✅ 调峰计划管理
- ✅ 申报管理
- ✅ 收益计算
- ✅ 大屏展示
- ✅ 所有业务REST接口

---

## 二、改造步骤

### 阶段1：项目复制和基础重命名（今天完成）

#### 步骤1.1：复制项目
```bash
cd /Users/sl/Documents/java/enn/
cp -r load-aggregator e-hub
cd e-hub
```

#### 步骤1.2：删除不需要的模块
```bash
rm -rf bigdata-service
rm -rf cim-service
rm -rf uac-service
rm -rf sms-service
rm -rf iot-service
rm -rf core-feign-service
rm -rf load-aggregator-tripart
rm -rf load-aggregator-guangzhou
rm -rf load-aggregator-iec
```

#### 步骤1.3：重命名模块目录
```bash
mv load-aggregator-common e-hub-common
mv load-aggregator-service e-hub-service
mv load-aggregator-delivery e-hub-upstream
mv load-aggregator-business e-hub-console
```

#### 步骤1.4：合并issue到console
```bash
# 创建grid目录
mkdir -p e-hub-console/src/main/java/cn/enn/la/grid

# 复制issue的controller和service
cp -r load-aggregator-issue/src/main/java/cn/enn/la/issue/* e-hub-console/src/main/java/cn/enn/la/grid/

# 删除issue模块
rm -rf load-aggregator-issue
```

#### 步骤1.5：修改父pom.xml
- artifactId: load-aggregator → e-hub
- version: 0.0.1-SNAPSHOT → 1.0.0-SNAPSHOT
- modules: 更新为4个模块

---

### 阶段2：包名和类名重命名（明天完成）

#### 步骤2.1：批量替换包名
```bash
# 替换包名 cn.enn.la → cn.enn.ehub
find . -type f \( -name "*.java" -o -name "*.xml" \) -exec sed -i '' 's/cn\.enn\.la\./cn.enn.ehub./g' {} +

# 替换项目名 load-aggregator → e-hub
find . -type f \( -name "*.yml" -o -name "*.properties" -o -name "*.xml" \) -exec sed -i '' 's/load-aggregator/e-hub/g' {} +
```

#### 步骤2.2：重命名包目录
```bash
# e-hub-common
cd e-hub-common/src/main/java/cn/enn/
mv la ehub

# e-hub-service
cd e-hub-service/src/main/java/cn/enn/
mv la ehub

# e-hub-upstream
cd e-hub-upstream/src/main/java/cn/enn/
mv la ehub

# e-hub-console
cd e-hub-console/src/main/java/cn/enn/
mv la ehub
```

#### 步骤2.3：重命名主类
```bash
# e-hub-upstream
mv LaDeliveryApplication.java EHubUpstreamApplication.java

# e-hub-console
mv LoadAggregatorBusinessApplication.java EHubConsoleApplication.java
```

---

### 阶段3：清理upstream服务（明天完成）

#### 步骤3.1：保留的Job
- XinTaiFinalJob.java
- PeakPlanDailyDataDeliveryJob.java
- PeakPlanDeliveryJob.java

#### 步骤3.2：删除的Job
```bash
cd e-hub-upstream/src/main/java/cn/enn/ehub/upstream/job/
rm -f DemoJob.java
rm -f HuabeiHeartBeatJob.java
rm -f SingleMeasDataDeliveryJob.java
rm -f TotalDataDeliveryJob.java
rm -f XinTaiSingleMeasDataDeliveryJob.java
rm -f XinTaiTotalDataDeliveryJob.java
rm -f XinTaiFinalRetryJob.java
```

#### 步骤3.3：保留的Controller接口
- RetryDeliveryController: singleMeasRetry
- DeliveryController: singleMeasDataDelivery, singleModelDataDelivery, peakPlan96PointDeliveryByDate, peakPlanDailyDataDelivery

#### 步骤3.4：清理不需要的接口方法
手动编辑Controller，删除不需要的方法

---

### 阶段4：整合console服务（后天完成）

#### 步骤4.1：创建模块目录结构
```bash
cd e-hub-console/src/main/java/cn/enn/ehub/console/

mkdir -p auth/controller
mkdir -p auth/service
mkdir -p auth/entity
mkdir -p auth/config

mkdir -p device/controller
mkdir -p device/service

mkdir -p iot/controller
mkdir -p iot/service
mkdir -p iot/req

mkdir -p grid/controller
mkdir -p grid/service
mkdir -p grid/ws

mkdir -p aggregator/controller
mkdir -p aggregator/service

mkdir -p peak/controller
mkdir -p peak/service
```

#### 步骤4.2：迁移issue功能到grid模块
- 将grid目录下的IssueController保留
- 将IssueWebService保留

#### 步骤4.3：保留business所有业务功能
- 保持原有的rest目录结构
- 保持原有的service目录结构
- 所有业务接口和逻辑不变

---

### 阶段5：删除依赖（第4天）

#### 步骤5.1：删除rdfa依赖
```xml
<!-- 删除 -->
<dependency>
    <groupId>top.rdfa</groupId>
    <artifactId>rdfa-timer-client</artifactId>
</dependency>
<dependency>
    <groupId>top.rdfa.framework</groupId>
    <artifactId>rdfa-actuator</artifactId>
</dependency>
```

#### 步骤5.2：删除Eureka依赖
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
</dependency>
```

#### 步骤5.3：删除Apollo依赖
```xml
<dependency>
    <groupId>com.ctrip.framework.apollo</groupId>
    <artifactId>apollo-client</artifactId>
</dependency>
```

#### 步骤5.4：删除监控依赖
```xml
<dependency>
    <groupId>com.fanneng</groupId>
    <artifactId>devops-monitoring-v2</artifactId>
</dependency>
<dependency>
    <groupId>com.fanneng</groupId>
    <artifactId>devops-logging</artifactId>
</dependency>
<dependency>
    <groupId>com.fanneng</groupId>
    <artifactId>requestlog</artifactId>
</dependency>
```

---

### 阶段6：实现新功能（第5-7天）

#### 步骤6.1：实现JWT认证
- 创建sys_user表
- 实现JwtUtil
- 实现AuthController
- 实现AuthInterceptor

#### 步骤6.2：实现物联数据管理
- 创建device_point_data表
- 实现IotDataReceiveController
- 实现IotDataReceiveService

#### 步骤6.3：替换定时任务
- 将保留的Job从rdfa-timer改为@Scheduled
- 实现Redis分布式锁

---

### 阶段7：配置文件整理（第8天）

#### 步骤7.1：整理application.yml
- 删除eureka配置
- 删除apollo配置
- 添加jwt配置
- 添加iot配置

#### 步骤7.2：配置多环境
- application-dev.yml
- application-test.yml
- application-prod.yml

---

### 阶段8：测试验证（第9-11天）

#### 步骤8.1：编译测试
```bash
mvn clean compile
```

#### 步骤8.2：启动测试
```bash
# 启动upstream
cd e-hub-upstream
mvn spring-boot:run

# 启动console
cd e-hub-console
mvn spring-boot:run
```

#### 步骤8.3：功能测试
- 测试保留的接口
- 测试定时任务
- 测试业务功能

---

## 三、注意事项

1. **保留所有业务功能**：business的所有REST接口和Service必须保留
2. **包名一致性**：确保所有import语句正确更新
3. **配置文件**：确保所有配置文件中的包名和类名正确
4. **数据库**：暂时保持load-aggregator数据库名不变，避免数据迁移
5. **测试充分**：每个阶段完成后都要编译测试

---

## 四、当前状态

- [ ] 阶段1：项目复制和基础重命名
- [ ] 阶段2：包名和类名重命名
- [ ] 阶段3：清理upstream服务
- [ ] 阶段4：整合console服务
- [ ] 阶段5：删除依赖
- [ ] 阶段6：实现新功能
- [ ] 阶段7：配置文件整理
- [ ] 阶段8：测试验证
