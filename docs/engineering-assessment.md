# e-hub 工程化检查与治理建议

## 结论

当前项目已经具备基础工程化雏形：

- 后端采用 Maven 多模块结构。
- 公共层、服务层、上游服务、运营平台模块已经拆分。
- 运营平台前端已迁入 `e-hub-console/frontend`。
- 前端构建产物由 `e-hub-console` 作为 `/console` 静态页面托管。
- 产品和权限方案、前端开发发布流程已有文档。

但还不是完整成熟的工程化体系。主要缺口集中在构建自动化、质量门禁、配置治理、测试体系和前后端权限契约。

## 已优化内容

本次已补齐以下工程化能力：

- 前端源码统一纳入 e-hub 仓库：`e-hub-console/frontend`。
- 前端构建产物固定输出到：`e-hub-console/src/main/resources/static/console`。
- `e-hub-console` 增加 Maven profile：`console-frontend`。
- 发布构建可使用单条命令完成：

```bash
mvn -s /Users/sl/.m2/settings.xml clean package -DskipTests -Pconsole-frontend
```

- 前端增加 Node/npm 版本约束：
  - Node.js `>=18 <23`
  - npm `>=9`
- 前端增加 `.nvmrc`，统一本地 Node 主版本。
- 根 README 增加文档入口。
- `.gitignore` 增加前端依赖、缓存、环境文件和 macOS 临时文件忽略规则。

## 工程结构

推荐维持当前结构：

```text
e-hub
├─ e-hub-common       # 公共模型、异常、结果对象、工具类
├─ e-hub-service      # 持久化、Mapper、领域数据服务
├─ e-hub-upstream     # 上游服务集成
├─ e-hub-console      # 运营平台后端和前端静态托管
│  ├─ frontend        # 运营平台前端源码
│  └─ src/main/resources/static/console
└─ docs               # 架构、流程和工程规范文档
```

## 构建与发布

### 本地前端开发

```bash
cd e-hub-console/frontend
npm install
npm run dev
```

### 前端单独构建

```bash
cd e-hub-console/frontend
npm run build
```

### 后端常规构建

不重新构建前端，只打当前静态资源：

```bash
mvn -s /Users/sl/.m2/settings.xml clean package -DskipTests
```

### 发布构建

重新安装前端依赖、构建前端并打包后端：

```bash
mvn -s /Users/sl/.m2/settings.xml clean package -DskipTests -Pconsole-frontend
```

## 当前主要风险

### 1. 测试体系不足

根 POM 默认配置了：

```xml
<maven.test.skip>true</maven.test.skip>
```

这会让测试长期失效。短期可以保留以保证当前项目可打包，但后续应逐步恢复：

- 公共工具类单元测试。
- 权限鉴权单元测试。
- Mapper 或 Service 层集成测试。
- 前端核心权限菜单逻辑测试。

### 2. 前端缺少质量门禁

当前前端只有 `dev/build/preview`，没有 lint、format、type check 或单元测试。后续建议补齐：

- ESLint
- Prettier
- Vitest
- 关键页面的 Playwright 冒烟测试

### 3. 权限契约仍是 mock

前端当前仍保留 mock 用户权限逻辑。正式工程化应由后端接口返回：

```js
{
  platformType: "owner" | "customer",
  entId: "customer_a",
  products: ["load_aggregation", "tariff"],
  permissions: ["load:overview:view"]
}
```

前端只消费契约，不在登录页选择平台或产品。

### 4. 配置分层需要继续收敛

当前有 `application.yml`、`application-dev.yml`、`application-prod.yml`，但本地 Redis、MySQL、日志路径等依赖仍容易影响启动体验。后续建议：

- 提供 `.env.example` 或本地启动说明。
- 日志路径通过配置显式设置，避免生成 `LOG_PATH_IS_UNDEFINED`。
- 本地依赖用 Docker Compose 固化。

### 5. 依赖版本存在历史负担

后端使用 Java 8、Spring Boot 2.3.x；前端使用 Vue 2。它们都能运行，但已不是长期维护选型。短期不建议大版本升级，避免影响业务；中期应规划：

- Spring Boot 2.7 或 3.x 升级评估。
- Vue 2 到 Vue 3 的迁移评估。
- 清理重复依赖版本声明，把版本尽量收敛到父 POM 的 `dependencyManagement`。

## 后续治理优先级

1. 接入真实登录和权限接口，替换前端 mock 权限。
2. 增加最小测试集，先覆盖鉴权、菜单权限和核心历史查询接口。
3. 增加前端 lint/format/test 脚本。
4. 固化本地依赖启动方式，建议增加 Docker Compose。
5. 清理 Maven 依赖重复声明和历史报告文件。
6. 建立 CI 流程：前端构建、后端编译、单元测试、jar 产物检查。

## 工程化验收标准

短期验收标准：

- `npm run build` 在 `e-hub-console/frontend` 成功。
- `mvn clean package -DskipTests -Pconsole-frontend` 成功。
- jar 内包含 `BOOT-INF/classes/static/console/index.html`。
- 后端启动后 `/console/` 可访问。
- 登录页不暴露产品选择，菜单由用户权限决定。

中期验收标准：

- 有 CI 流水线。
- 有可运行测试。
- 有统一配置说明。
- 有清晰的权限接口契约。
- 前端和后端发布命令稳定可重复。
