# e-hub Console Frontend

`e-hub-console/frontend` 是 e-hub 运营平台前端源码目录。

## 目录结构

```text
frontend
├─ public/config.js
├─ src
│  ├─ App.vue
│  ├─ Login.vue
│  ├─ PlatformWorkbench.vue
│  ├─ auth
│  ├─ modules
│  │  └─ load-aggregation
│  │     ├─ overview
│  │     │  ├─ Aggregation.vue
│  │     │  ├─ api
│  │     │  ├─ components
│  │     │  └─ images
│  │     └─ history
│  │        ├─ api
│  │        ├─ components
│  │        ├─ images
│  │        └─ src
│  ├─ services
│  └─ utils
└─ vite.config.js
```

## 环境要求

- Node.js 18 到 22
- npm 9+

建议使用 `.nvmrc`：

```bash
nvm use
```

## 本地开发

先启动后端 `e-hub-console`，默认端口 `8009`，再启动前端：

```bash
cd /Users/sl/Documents/java/enn/e-hub/e-hub-console/frontend
npm install
npm run dev
```

访问：

```text
http://127.0.0.1:5173/
```

开发期接口通过 Vite 代理到后端。默认代理目标：

```text
http://127.0.0.1:8009
```

如果后端端口变化：

```bash
VITE_PROXY_TARGET=http://127.0.0.1:8010 npm run dev
```

## 构建输出

```bash
npm run build
```

构建产物直接输出到后端静态资源目录：

```text
../src/main/resources/static/console
```

后端启动后访问：

```text
http://127.0.0.1:8009/console/
```

## 发布构建

推荐从 e-hub 根目录使用 Maven profile 一次性构建前端和后端：

```bash
cd /Users/sl/Documents/java/enn/e-hub
mvn -s /Users/sl/.m2/settings.xml clean package -DskipTests -Pconsole-frontend
```

该 profile 会在 `e-hub-console` 模块中执行：

```bash
npm ci
npm run build
```

然后把 `static/console` 一起打入 `e-hub-console.jar`。

## 权限规则

登录页只负责身份认证，不提供平台或产品选择。用户登录后，由后端返回用户类型、租户、产品开通和权限点，前端据此生成菜单。

当前 mock 账号仅用于开发期演示：

- `admin` / `owner` / `ehub`：我的运营平台
- `tariff` / `price`：电价能力客户
- `all` / `both`：负荷聚合 + 电价能力客户
- 其他账号：负荷聚合客户

详细流程见：

```text
/Users/sl/Documents/java/enn/e-hub/docs/frontend-development-release.md
```
