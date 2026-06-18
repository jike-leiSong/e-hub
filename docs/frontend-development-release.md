# e-hub 前端开发与发布流程

## 目录约定

e-hub 后续前后端开发统一在当前仓库内完成。

```text
e-hub
├─ e-hub-console
│  ├─ frontend                         # 运营平台前端源码
│  │  ├─ src
│  │  │  ├─ auth                       # 登录态和权限菜单模型
│  │  │  ├─ modules
│  │  │  │  └─ load-aggregation        # 负荷聚合产品域
│  │  │  │     ├─ overview             # 运营总览
│  │  │  │     └─ history              # 历史查询
│  │  │  ├─ services                   # 公共请求封装
│  │  │  └─ utils                      # 前端工具函数
│  │  ├─ public/config.js              # 运行时配置
│  │  └─ vite.config.js                # Vite 开发和构建配置
│  └─ src/main/resources/static/console # 前端构建产物，随后端 jar 发布
└─ docs
   ├─ product-architecture.md          # 产品和权限架构方案
   ├─ frontend-development-release.md  # 当前文档
   └─ engineering-assessment.md        # 工程化检查与治理建议
```

临时目录 `/Users/sl/Downloads/Aggregation` 不再作为后续开发位置。后续以前端源码目录为准：

```bash
cd /Users/sl/Documents/java/enn/e-hub/e-hub-console/frontend
```

## 本地前端开发

1. 启动后端 `e-hub-console`，默认端口为 `8009`。

2. 启动前端开发服务：

```bash
cd /Users/sl/Documents/java/enn/e-hub/e-hub-console/frontend
npm install
npm run dev
```

3. 浏览器访问：

```text
http://127.0.0.1:5173/
```

`vite.config.js` 默认把 `/auth`、`/historyQuery`、`/yesterday`、`/today`、`/tomorrow`、`/profit` 等接口代理到：

```text
http://127.0.0.1:8009
```

如果后端端口变化，可用环境变量覆盖：

```bash
VITE_PROXY_TARGET=http://127.0.0.1:8010 npm run dev
```

## 前端构建

前端构建命令：

```bash
cd /Users/sl/Documents/java/enn/e-hub/e-hub-console/frontend
npm run build
```

构建产物会直接输出到：

```text
/Users/sl/Documents/java/enn/e-hub/e-hub-console/src/main/resources/static/console
```

不需要再从临时目录手工复制 `dist`。该目录是后端托管 `/console` 页面所需的静态资源。

## 后端打包发布

推荐使用 Maven profile 一次性完成前端构建和后端打包：

```bash
cd /Users/sl/Documents/java/enn/e-hub
mvn -s /Users/sl/.m2/settings.xml clean package -DskipTests -Pconsole-frontend
```

`console-frontend` profile 会在 `e-hub-console` 模块的 `generate-resources` 阶段执行：

```bash
npm ci
npm run build
```

随后 Maven 会把 `e-hub-console/src/main/resources/static/console` 中的前端产物复制进 jar。

打包完成后，运营平台静态资源会进入：

```text
e-hub-console/target/e-hub-console.jar
```

启动后端：

```bash
java -jar e-hub-console/target/e-hub-console.jar
```

访问运营平台：

```text
http://127.0.0.1:8009/console/
```

## 集成运行原理

Spring Boot 启动后会托管 `classpath:/static/console/` 下的静态资源。

当前后端配置把：

```text
/console/**
```

映射到：

```text
e-hub-console/src/main/resources/static/console
```

浏览器访问 `/console/` 时，后端返回前端构建后的 `index.html`。随后浏览器继续加载 `assets/*.js`、`assets/*.css`、`config.js` 等静态资源。前端运行后，请求 `/auth`、`/historyQuery` 等接口，这些接口仍由同一个 `e-hub-console` 后端服务处理。

整体链路：

```text
浏览器 /console/
  ↓
e-hub-console 返回 index.html
  ↓
浏览器加载 JS/CSS/config.js
  ↓
Vue 运营平台运行
  ↓
请求 /auth、/historyQuery 等接口
  ↓
e-hub-console 后端接口处理
```

## 权限和页面开发原则

登录页只负责身份认证，不提供平台或产品选择。

登录成功后，应由后端返回用户的平台类型、租户、产品开通和权限点。前端根据这些信息生成菜单和默认首页。

核心数据模型参考：

```js
{
  platformType: "owner" | "customer",
  tenantId: "customer_a",
  role: "customer_admin",
  products: ["load_aggregation", "tariff"],
  permissions: [
    "load:overview:view",
    "load:history:view",
    "tariff:query:view"
  ]
}
```

页面开发时按产品域组织：

- 我的运营平台：客户管理、用户管理、产品开通、权限管理、系统设置。
- 客户运营平台/负荷聚合：运营总览、历史查询、设备/资源数据。
- 客户运营平台/电价能力：全国电价查询、接口能力、调用记录。

设备管理归属于负荷聚合产品域，不作为客户运营平台的独立一级产品。

## 发布检查清单

发布前至少执行：

```bash
cd /Users/sl/Documents/java/enn/e-hub
mvn -s /Users/sl/.m2/settings.xml clean package -DskipTests -Pconsole-frontend
```

然后确认 jar 内包含前端资源：

```bash
jar tf e-hub-console/target/e-hub-console.jar | grep 'BOOT-INF/classes/static/console/index.html'
```

部署后确认：

```text
/console/ 返回 HTML
/console/config.js 返回 JavaScript
/console/assets/*.js 返回前端主脚本
```
