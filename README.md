# e-hub

E-HUB是一个统一的控制台管理平台，整合了多个业务模块，包括负荷聚合、设备资产管理等。

## 📚 项目文档

### 核心文档
- [产品与权限方案](docs/product-architecture.md)
- [前端开发与发布流程](docs/frontend-development-release.md)
- [工程化检查与治理建议](docs/engineering-assessment.md)
- [设备资产与负荷聚合数据接入技术方案](docs/iot-device-load-aggregation-design.md)

### 🔥 负荷聚合平台升级改造文档

**🎯 快速导航**:
- [**PROJECT_OVERVIEW.md**](PROJECT_OVERVIEW.md) - 📊 项目总览，了解整体进度和规划
- [**E-HUB_UPGRADE_PLAN.md**](E-HUB_UPGRADE_PLAN.md) - 📋 主规划文档，详细的升级改造方案

**进度追踪**:
- [CONTROLLER_MIGRATION_STATUS.md](CONTROLLER_MIGRATION_STATUS.md) - Controller迁移进度追踪（实时更新）
- [BATCH1_MIGRATION_COMPLETED.md](BATCH1_MIGRATION_COMPLETED.md) - 批次1完成报告

**操作指南**:
- [OVERVIEW_MIGRATION_GUIDE.md](OVERVIEW_MIGRATION_GUIDE.md) - 运营总览模块迁移指南
- [QUICK_START_OVERVIEW.md](QUICK_START_OVERVIEW.md) - 快速启动指南（20分钟）
- [OVERVIEW_MODULE_COMPLETED.md](OVERVIEW_MODULE_COMPLETED.md) - 运营总览模块完成总结

## 🚀 快速开始

### 编译项目
```bash
mvn clean compile -DskipTests
```

### 启动应用
```bash
cd e-hub-console
mvn spring-boot:run
```

### 访问Swagger文档
```
http://localhost:8080/swagger-ui.html
```

## 📊 当前进度

**整体进度**: 40% (12/30)

- ✅ 运营总览模块: 90% 完成
- ✅ 企业管理模块: 100% 完成
- ⚠️ 数据查询模块: 待迁移
- ⚠️ 实时数据模块: 待迁移

详见 [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md)

## 🏗️ 项目结构

```
e-hub/
├── e-hub-common/              # 公共模块
├── e-hub-service/             # Service层（Mapper/VO）
├── e-hub-console/             # Console控制台 ⭐
│   ├── src/main/java/
│   │   └── cn/sl/ehub/console/
│   │       ├── controller/
│   │       │   └── loadaggregation/  # 负荷聚合Controller
│   │       └── service/
│   └── frontend/              # 前端Vue3项目
│       └── src/modules/
│           └── load-aggregation/
└── e-hub-upstream/            # 上游服务
```

## 🛠️ 技术栈

### 后端
- Spring Boot 3.x
- Java 17+
- MyBatis-Plus
- Swagger 3 / Knife4j

### 前端
- Vue 3
- Composition API
- Element Plus

## 📝 开发规范

### Controller命名
- 包路径: `cn.sl.ehub.console.controller.loadaggregation`
- 统一使用 `@Slf4j` 添加日志
- 返回值统一使用 `ResultVO`

### Service命名
- 接口: `cn.sl.ehub.console.service.I*Service`
- 实现: `cn.sl.ehub.console.service.impl.*ServiceImpl`

### 日志规范
- INFO: 记录关键业务操作
- ERROR: 记录异常信息
- DEBUG: 记录详细调试信息

## 👥 联系方式

如有问题，请查看相关文档或联系项目负责人。
