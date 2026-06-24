# 运营总览模块 - 快速启动指南

## 🚀 立即开始

### 步骤1: 编译后端代码（2分钟）

```bash
cd /Users/sl/Documents/java/enn/e-hub

# 清理并编译
mvn clean compile

# 如果出现编译错误，检查依赖
mvn dependency:tree
```

### 步骤2: 启动应用（1分钟）

```bash
# 启动Console应用
cd e-hub-console
mvn spring-boot:run

# 或者使用IDE启动
# 找到主类: cn.sl.ehub.console.ConsoleApplication
# 右键 -> Run
```

### 步骤3: 验证后端接口（3分钟）

访问 Swagger 文档：
```
http://localhost:8080/swagger-ui.html
```

检查以下Controller是否显示：
- ✅ 企业用户详情 (EntUserDetailController)
- ✅ 申报计划 (ApplyPlanController)
- ✅ 聚合商计划管理 (AggregatorApplyPlanController)
- ✅ 文件管理 (FileController)
- ✅ 天气信息 (WeatherController)
- ✅ 峰值计划申报 (PeakPlanDeclareController)

### 步骤4: 修改前端API调用（5分钟）

#### 方法A: 直接替换（推荐测试环境）

编辑 `frontend/src/modules/load-aggregation/overview/Aggregation.vue`：

```javascript
// 找到import语句，修改为：
import * as api from "./api/console.js";  // 新API

// 或者保持原有的具名导入
import {
  getWeekProfit,
  getContentList,
  // ... 其他方法
} from "./api/console.js";  // 改为console.js
```

#### 方法B: 条件切换（推荐生产环境）

创建 `frontend/src/modules/load-aggregation/overview/api/switcher.js`：

```javascript
// API切换器
const USE_CONSOLE_API = process.env.VUE_APP_USE_CONSOLE_API === 'true';

if (USE_CONSOLE_API) {
  export * from './console.js';
} else {
  export * from './index.js';
}
```

然后在组件中：
```javascript
import * as api from "./api/switcher.js";
```

在 `.env` 文件中控制：
```
VUE_APP_USE_CONSOLE_API=true
```

### 步骤5: 测试核心功能（10分钟）

1. **登录系统**
   - 使用测试账号登录
   - 确认token已保存到sessionStorage

2. **访问运营总览**
   - 打开浏览器开发者工具 (F12)
   - 访问运营总览页面
   - 检查Network标签，确认请求发送到本地

3. **测试收益统计**
   - 选择日期范围
   - 查看收益数据
   - 检查图表显示

4. **测试实时汇总**
   - 查看实时数据
   - 检查曲线图

5. **测试申报计划**
   - 创建/编辑计划
   - 查看计划列表

## ⚠️ 常见问题快速修复

### 问题1: 401 未授权

**原因**: Token未正确传递

**解决**:
```javascript
// 检查sessionStorage中的token
console.log(sessionStorage.getItem("token"));
console.log(sessionStorage.getItem("console-token"));

// 如果token键名不对，修改console.js中的键名
Authorization: `Bearer ${sessionStorage.getItem("正确的键名")}`
```

### 问题2: 404 接口不存在

**原因**: Controller未正确注册或路径不对

**解决**:
1. 检查Swagger文档确认接口路径
2. 确认Controller的 `@RequestMapping` 路径
3. 重启应用

### 问题3: 跨域错误

**原因**: 前后端域名不同

**解决**:
在 `application.yml` 添加CORS配置：
```yaml
spring:
  web:
    cors:
      allowed-origins: "*"
      allowed-methods: "*"
      allowed-headers: "*"
```

或创建CORS配置类：
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*");
    }
}
```

### 问题4: 文件上传失败

**原因**: 上传路径不存在或权限不足

**解决**:
```bash
# 创建上传目录
mkdir -p ./uploads
chmod 755 ./uploads
```

在 `application.yml` 配置：
```yaml
file:
  upload:
    path: ./uploads
    url-prefix: /uploads
```

## 📊 测试检查清单

### 后端检查
- [ ] 应用启动成功，无报错
- [ ] Swagger文档可访问
- [ ] 所有新Controller显示在文档中
- [ ] 测试接口返回200状态码

### 前端检查
- [ ] npm运行无错误
- [ ] 页面加载成功
- [ ] Console无错误
- [ ] Network显示请求发送到localhost

### 功能检查
- [ ] 登录成功
- [ ] 运营总览页面显示
- [ ] 收益统计数据加载
- [ ] 实时汇总图表显示
- [ ] 用户情况列表显示
- [ ] 设备列表加载
- [ ] 申报计划功能正常

## 🔧 调试技巧

### 后端调试

1. **添加日志**
```java
@Slf4j
public class EntUserDetailController {
    @GetMapping("/options")
    public ResultVO<List<AggregatorEnt>> getEntUserOptions(@RequestParam("aggregatorId") String aggregatorId) {
        log.info("获取企业用户选项: aggregatorId={}", aggregatorId);
        // ...
    }
}
```

2. **检查认证**
```java
AuthUser user = AuthContext.get();
log.info("当前用户: {}", user);
```

3. **查看SQL日志**
```yaml
logging:
  level:
    cn.sl.ehub: DEBUG
    org.springframework.jdbc: DEBUG
```

### 前端调试

1. **检查请求**
```javascript
// 在console.js的request方法中添加
console.log('Request:', config);
```

2. **检查响应**
```javascript
.then(response => {
  console.log('Response:', response);
  return response;
})
```

3. **检查Token**
```javascript
console.log('Token:', sessionStorage.getItem("token"));
```

## 📞 需要帮助？

如果遇到问题：

1. **查看日志**
   - 后端日志: `logs/application.log`
   - 前端Console: F12 -> Console标签

2. **查看文档**
   - `OVERVIEW_MIGRATION_GUIDE.md` - 完整迁移指南
   - `TROUBLESHOOTING.md` - 问题排查指南

3. **回滚方案**
   ```javascript
   // 改回使用旧API
   import * as api from "./api/index.js";
   ```

## 🎉 成功标志

当你看到以下情况，说明迁移成功：

1. ✅ 浏览器Network标签显示请求发送到 `localhost:8080`
2. ✅ 不再有 `X-GW-AccessKey` 请求头
3. ✅ 使用 `Authorization: Bearer <token>` 认证
4. ✅ 所有数据正常加载
5. ✅ 功能完全正常

## 下一步

运营总览模块完成后，可以继续改造：
1. 历史查询模块
2. 设备资源模块
3. 其他模块...

---

**预计总时间**: 20-30分钟

**难度**: ⭐⭐☆☆☆ (简单-中等)
