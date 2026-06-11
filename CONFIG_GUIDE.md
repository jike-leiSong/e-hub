# E-Hub 配置文件说明

## 📁 配置文件结构

### e-hub-upstream（电网上行服务）
```
e-hub-upstream/src/main/resources/
├── application.yml           # 主配置文件（默认dev环境）
├── application-dev.yml       # 开发环境配置
└── application-prod.yml      # 生产环境配置
```

### e-hub-console（控制台服务）
```
e-hub-console/src/main/resources/
├── application.yml           # 主配置文件（默认dev环境）
├── application-dev.yml       # 开发环境配置
└── application-prod.yml      # 生产环境配置
```

---

## 🔧 配置说明

### 1. e-hub-upstream 配置

#### application.yml（主配置）
```yaml
server:
  port: 8088

spring:
  profiles:
    active: dev  # 默认使用dev环境
  application:
    name: e-hub-upstream
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Chongqing
```

#### application-dev.yml（开发环境）
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/e_hub
    username: root
    password: root
  redis:
    host: localhost
    port: 6379

# 鑫泰聚合商ID
xintai:
  aggregator:
    id: 1711340903453614082

# 电网上送地址（Mock）
nari:
  url:
    total: http://localhost:8080/mock/PostValue
```

#### application-prod.yml（生产环境）
```yaml
spring:
  datasource:
    url: jdbc:mysql://10.39.48.241:3306/e_hub
    username: rootroot
    password: Abcd1234
  redis:
    sentinel:
      master: master6379
      nodes: 10.39.47.6:26379,10.39.47.7:26379,10.39.47.8:26379

# 电网上送地址（真实）
nari:
  url:
    total: https://202.96.15.45:39091/PostValue,https://202.96.15.46:39091/PostValue
```

---

### 2. e-hub-console 配置

#### application.yml（主配置）
```yaml
server:
  port: 8009

spring:
  profiles:
    active: dev  # 默认使用dev环境
  application:
    name: e-hub-console

mybatis:
  mapper-locations: classpath:/mapper/*Mapper.xml
  configuration:
    map-underscore-to-camel-case: true
```

#### application-dev.yml（开发环境）
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/e_hub
    username: root
    password: root
  redis:
    host: localhost
    port: 6379

swagger:
  enable: true
```

#### application-prod.yml（生产环境）
```yaml
spring:
  datasource:
    url: jdbc:mysql://10.39.48.241:3306/e_hub
    username: rootroot
    password: Abcd1234
  redis:
    sentinel:
      master: master6379
      nodes: 10.39.47.6:26379,10.39.47.7:26379,10.39.47.8:26379

swagger:
  enable: false
```

---

## 🚀 启动方式

### 开发环境启动
```bash
# 方式1：使用默认配置（dev）
mvn spring-boot:run

# 方式2：显式指定dev环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 方式3：使用java -jar
java -jar e-hub-upstream.jar --spring.profiles.active=dev
```

### 生产环境启动
```bash
# 方式1：使用maven
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# 方式2：使用java -jar
java -jar e-hub-upstream.jar --spring.profiles.active=prod

# 方式3：使用环境变量
export SPRING_PROFILES_ACTIVE=prod
java -jar e-hub-upstream.jar
```

---

## 📝 配置项说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://host:port/database
    username: 用户名
    password: 密码
    hikari:
      maximum-pool-size: 连接池最大连接数
      minimum-idle: 最小空闲连接数
```

### Redis配置

**单机模式（开发环境）**
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
    password: 密码（可选）
```

**哨兵模式（生产环境）**
```yaml
spring:
  redis:
    sentinel:
      master: master6379
      nodes: 节点1:端口,节点2:端口,节点3:端口
    database: 15
    password: FNpreadmin123
```

### 定时任务配置

**鑫泰华北电网上送**
```yaml
xintai:
  aggregator:
    id: 1711340903453614082  # 聚合商ID
```

**调峰计划上送**
```yaml
peak:
  plan:
    aggregator:
      id: 聚合商ID（如需要请配置）
```

### 电网上送配置
```yaml
nari:
  url:
    total: 总加数据上送地址（多个用逗号分隔）
    peakPlan: 调峰计划上送地址
    single: 单体量测上送地址
    connectionTimeout: 60000  # 连接超时（毫秒）
    receiveTimeout: 120000    # 接收超时（毫秒）
```

---

## ⚙️ 环境切换

### 修改默认环境
编辑 `application.yml`：
```yaml
spring:
  profiles:
    active: prod  # 改为prod
```

### 临时切换环境
```bash
# 启动时指定
java -jar app.jar --spring.profiles.active=prod

# 或使用环境变量
export SPRING_PROFILES_ACTIVE=prod
java -jar app.jar
```

---

## 🔒 安全建议

1. **生产环境密码**：不要提交到Git，使用环境变量或配置中心
2. **敏感信息加密**：使用Jasypt等工具加密敏感配置
3. **配置文件权限**：生产环境配置文件设置只读权限

### 使用环境变量（推荐）
```bash
# 设置环境变量
export DB_PASSWORD=your_password
export REDIS_PASSWORD=your_password

# 配置文件中引用
spring:
  datasource:
    password: ${DB_PASSWORD}
  redis:
    password: ${REDIS_PASSWORD}
```

---

## 📋 配置检查清单

### 部署前检查
- [ ] 数据库连接信息正确
- [ ] Redis连接信息正确
- [ ] 电网上送地址正确
- [ ] 聚合商ID配置正确
- [ ] 日志级别设置合理
- [ ] Swagger在生产环境已关闭

### 启动后验证
- [ ] 服务正常启动
- [ ] 数据库连接成功
- [ ] Redis连接成功
- [ ] 定时任务正常执行
- [ ] 接口调用正常

---

## 🆘 常见问题

### 1. 数据库连接失败
```
检查：
- 数据库地址、端口是否正确
- 用户名密码是否正确
- 数据库是否已创建
- 网络是否可达
```

### 2. Redis连接失败
```
检查：
- Redis地址、端口是否正确
- 密码是否正确
- 哨兵节点是否正常
- 网络是否可达
```

### 3. 定时任务不执行
```
检查：
- 聚合商ID是否配置
- @EnableScheduling注解是否添加
- 定时任务类是否被Spring扫描
```

---

## 📞 技术支持

如有问题，请检查：
1. 日志文件：`logs/e-hub-upstream.log`
2. 配置文件：确认环境配置正确
3. 网络连接：确认各服务可达

---

**配置文件已简化为dev和prod两个环境，便于管理和维护！**
