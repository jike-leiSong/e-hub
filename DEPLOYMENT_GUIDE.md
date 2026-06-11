# E-Hub 项目部署指南

## 一、项目概述

E-Hub 是负荷聚合运营管理平台，包含以下服务：

- **e-hub-upstream**：电网上行服务（数据上送）
- **e-hub-console**：控制台服务（运营管理 + 前端页面）

## 二、域名配置

### 生产环境
```
控制台（前端+后端）：e-hub.{your-domain}.com
上行服务：e-hub-upstream.{your-domain}.com
```

### 测试环境
```
控制台：e-hub.test.{your-domain}.com
上行服务：e-hub-upstream.test.{your-domain}.com
```

### 配置说明
- `{your-domain}` 为实际域名占位符
- 部署时需要在配置文件中替换为真实域名
- 建议使用环境变量管理域名配置

## 三、前端部署方式

### 方式：前端打包到后端（推荐）

**优势**：
- 单体部署，运维简单
- 统一域名，无需跨域
- 适合内部运营系统

**URL规划**：
```
前端页面：
http://e-hub.{your-domain}.com/                    → 首页
http://e-hub.{your-domain}.com/login               → 登录
http://e-hub.{your-domain}.com/dashboard           → 仪表盘
http://e-hub.{your-domain}.com/device              → 设备管理
http://e-hub.{your-domain}.com/declare             → 申报管理
http://e-hub.{your-domain}.com/income              → 收益报表
http://e-hub.{your-domain}.com/monitor             → 运行监视

后端API：
http://e-hub.{your-domain}.com/api/auth/login      → 登录接口
http://e-hub.{your-domain}.com/api/device/list     → 设备列表
http://e-hub.{your-domain}.com/api/iot/data/upload → 物联数据上传
```

## 四、构建和部署

### 4.1 前端构建
```bash
cd e-hub-frontend
npm install
npm run build
# 输出到：../e-hub-console/src/main/resources/static/
```

### 4.2 后端打包
```bash
cd e-hub-console
mvn clean package
# 输出：target/e-hub-console.jar
```

### 4.3 部署
```bash
# 上传jar包
scp target/e-hub-console.jar root@server:/opt/e-hub/

# 启动服务
ssh root@server
cd /opt/e-hub
./start-console.sh
```

## 五、环境变量配置

```bash
# 数据库
export DB_HOST=your-db-host
export DB_USERNAME=your-db-user
export DB_PASSWORD=your-db-password

# Redis
export REDIS_HOST=your-redis-host
export REDIS_PORT=6379
export REDIS_PASSWORD=your-redis-password

# JWT
export JWT_SECRET=your-jwt-secret-key

# 域名（可选）
export DOMAIN=your-domain.com
```

## 六、Nginx配置模板

```nginx
server {
    listen 80;
    server_name e-hub.{your-domain}.com;
    
    location / {
        proxy_pass http://127.0.0.1:8009;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 七、注意事项

1. 部署前需要替换所有 `{your-domain}` 为实际域名
2. 生产环境建议使用HTTPS
3. 定期备份数据库
4. 监控服务运行状态
