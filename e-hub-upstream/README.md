# e-hub-upstream

`e-hub-upstream` 是 e-hub 的上游/电网侧集成服务模块，主要承载：

- 电网侧数据上送
- 第三方服务状态检查
- 外部接口适配
- 上游服务运行监控

本模块独立打包为 Spring Boot 服务，配置文件位于：

```text
e-hub-upstream/src/main/resources
```

常用构建命令：

```bash
cd /Users/sl/Documents/java/enn/e-hub
mvn -s /Users/sl/.m2/settings.xml -pl e-hub-upstream -am package -DskipTests
```
