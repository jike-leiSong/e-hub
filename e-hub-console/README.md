# e-hub-console

`e-hub-console` 是 e-hub 运营平台服务模块，包含：

- 运营平台后端接口
- 登录认证与权限拦截
- 负荷聚合业务接口
- grid/电网调度接口
- `/console` 前端静态页面托管

## 后端能力边界

```text
cn.sl.ehub.console
├─ auth        # 登录认证、用户上下文、鉴权拦截
├─ config      # Web MVC、RedisLock 等基础配置
├─ controller  # 运营平台 REST 接口
│  ├─ loadaggregation # 负荷聚合、历史查询、申报计划、今日/明日/昨日数据
│  ├─ profit          # 收益统计
│  └─ system          # 健康检查等系统接口
├─ grid        # 电网/调度侧接口和 WebService 能力
├─ job         # 定时任务
├─ model       # console 层请求、响应和视图对象
└─ service     # console 层业务编排
```

`e-hub-console` 当前定位是“运营平台服务”，不是纯前端控制台。请求对象统一放在 `model/req`，响应对象放在 `model/resp`，页面/导出视图对象放在 `model/vo`。

前端源码位于：

```text
e-hub-console/frontend
```

前端构建产物位于：

```text
e-hub-console/src/main/resources/static/console
```

推荐发布构建命令：

```bash
cd /Users/sl/Documents/java/enn/e-hub
mvn -s /Users/sl/.m2/settings.xml clean package -DskipTests -Pconsole-frontend
```

启动后访问：

```text
http://127.0.0.1:8009/console/
```

完整流程见：

```text
docs/frontend-development-release.md
```
