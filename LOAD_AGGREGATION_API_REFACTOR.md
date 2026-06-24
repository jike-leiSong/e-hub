# 负荷聚合功能接口调用改造方案

## 一、现状分析

### 1. 前端API调用现状

**当前问题**：
- 前端通过硬编码的外部网关URL调用接口
- 使用多个环境配置（prod/uat/fat/test）
- 需要额外的AccessKey验证
- 直接调用 `load-aggregator-business` 和 `fnw-datamining` 服务

**前端API配置**（`overview/api/index.js` 和 `history/api/index.js`）：
```javascript
// 当前配置方式
urlStr = "https://gateway.fanneng.com/load-aggregator-business";
pointUrl = "https://gateway.fanneng.com/fnw-datamining";
accessKey = "rXddpRDLO2Z72mM6ENBysr62fIof3Mfg";

// 每个接口调用都需要添加：
headers: {
  ticket: sessionStorage.getItem("ticket"),
  "X-GW-AccessKey": accessKey,
}
```

### 2. 后端Controller现状

**已有的Console Controller**：
- `/profit/*` - 收益统计相关（ProfitController）
- `/yesterday/*` - 昨日详情（YesterdayController）
- `/today/*` - 今日详情（TodayController）
- `/tomorrow/*` - 明日详情（TomorrowController）
- `/historyQuery/*` - 历史查询（HistoryQueryController）
- `/aggregatorPlan/*` - 申报计划（AggregatorApplyPlanController）

**Controller特点**：
- 使用标准的 Spring MVC 注解
- 统一返回 `ResultVO` 包装
- 集成 Swagger 文档
- 已实现认证拦截

### 3. 前端调用的接口清单

#### Overview模块（运营总览）
```
GET  /profit/week                        -> 本月收益
GET  /profit/getContentList              -> 筛选内容列表
GET  /tomorrow/getPriceByResourceTypeId  -> 获取报价
GET  /applyPlan/getApplyDateList         -> 申报日期列表
GET  /tomorrow/getAggregatorDeliveryChart -> 聚合商交割曲线
GET  /yesterday/getResourceTypeList      -> 资源类型列表
GET  /yesterday/getLastProfit            -> 上次收益
GET  /yesterday/getOverview              -> 实时汇总
GET  /tomorrow/getAggregatorApply        -> 查询申报
GET  /tomorrow/getAggregatorApplyOfferResp -> 查询报价
POST /tomorrow/saveAggregatorApplyOffer   -> 暂存报价
POST /tomorrow/submitAggregatorApplyOffer -> 提交报价
GET  /yesterday/getEntUserOverviewResp   -> 用户情况
GET  /yesterday/getDeviceList            -> 设备列表
GET  /entUserDetail/getEntUserDetailRespList -> 企业用户详情列表
GET  /yesterday/getEntUserDeviceChartResp -> 昨日设备曲线
GET  /today/getEntUserDeviceChartResp    -> 今日设备曲线
GET  /tomorrow/getEntUserDeviceChartResp -> 明日设备曲线
POST /tomorrow/updateAggregatorApply     -> 更新申报
GET  /today/getIotLog                    -> 物联网日志
GET  /entUserDetail/options              -> 企业用户选项
GET  /profit/list                        -> 收益列表（无筛选）
GET  /profit/listByEntIdList             -> 收益列表（有筛选）
GET  /entUserDetail/list                 -> 企业用户详情列表
GET  /entUserDetail/percent/options      -> 企业用户百分比选项
POST /yesterday/entInvite                -> 企业用户邀约
GET  /entUserDetail/listV2               -> 企业用户详情列表V2
POST /entUserDetail/autoUpdateEnt        -> 自动更新企业
GET  /entUserDetail/getCimDeviceList     -> CIM设备列表
POST /entUserDetail/updateEnt            -> 更新企业
POST /aggregatorPlan/getPlanList         -> 获取申报列表
POST /aggregatorPlan/getReferDatePower   -> 参考日功率
POST /aggregatorPlan/addOrUpdatePlan     -> 新增/编辑计划
GET  /aggregatorPlan/getPlanDetail       -> 计划详情
GET  /today/get/device/tree              -> 设备树
GET  /today/getMultiDeviceChartResp      -> 多设备曲线
GET  /aggregatorPlan/getRunPlan          -> 运行计划
POST /peakPlanDeclare/import             -> 预测数据上报
POST /file/uploadFile                    -> 文件上传
POST /operation/save                     -> 操作日志（埋点）
```

#### History模块（历史查询）
```
GET  /historyQuery/getProfitCalculation    -> 收益计算
POST /historyQuery/getPrice                -> 出清价格
POST /historyQuery/getMetricList           -> 汇总功率曲线（测点列表）
GET  /historyQuery/getTotalPowerChart      -> 汇总功率曲线
GET  /yesterday/getResourceTypeList        -> 资源类型列表
POST /historyQuery/profitStatistics        -> 收益统计
POST /historyQuery/userProfitStatistics    -> 用户收益统计
GET  /entUserDetail/options                -> 企业用户选项
POST /historyQuery/userAdjustmentGraph     -> 用户调节情况曲线
POST /historyQuery/userAdjustmentGraphNew  -> 用户调节情况曲线（新）
GET  /yesterday/getDeviceList              -> 设备列表
POST /historyQuery/userAdjustmentTable     -> 用户调节情况表格
POST /historyQuery/deviceRunStatusChart    -> 设备运行情况曲线
GET  /historyQuery/exportAdjust            -> 导出调节情况
GET  /historyQuery/exportBuZhaoUploadData  -> 导出补招数据
GET  /historyQuery/getPriceExcel           -> 导出出清价格
POST /historyQuery/getPriceTable           -> 出清价格表格
POST /operation/save                       -> 操作日志（埋点）
```

## 二、改造方案

### 方案设计原则

1. **统一入口**：所有接口通过console模块统一提供
2. **简化认证**：使用console已有的认证机制，无需额外AccessKey
3. **向下兼容**：Controller保持现有接口路径不变
4. **渐进式改造**：前端逐步迁移，可新老接口并存

### 改造目标

**改造前**：
```
前端 -> 外部网关(gateway.fanneng.com) -> load-aggregator-business -> 业务逻辑
       需要：ticket + X-GW-AccessKey
```

**改造后**：
```
前端 -> console模块 -> 业务逻辑
       需要：token (Bearer)
```

### 具体实现步骤

#### 步骤1：确认Console模块已有接口覆盖率

✅ **已实现的接口**（无需改造）：
- `/profit/*` - 完整实现
- `/yesterday/*` - 完整实现
- `/historyQuery/*` - 完整实现

⚠️ **缺失的接口**（需要补充）：
- `/today/*` - TodayController 部分实现
- `/tomorrow/*` - TomorrowController 部分实现
- `/entUserDetail/*` - 需要新建EntUserDetailController
- `/applyPlan/*` - 需要检查AggregatorApplyPlanController
- `/aggregatorPlan/*` - 需要检查是否完整
- `/peakPlanDeclare/*` - 需要新建
- `/file/*` - 需要新建FileController
- `/operation/save` - 埋点接口，可选实现或代理

#### 步骤2：补充缺失的Controller

需要创建或补充以下Controller：

1. **EntUserDetailController** (`/entUserDetail/*`)
   - options - 企业用户选项
   - getEntUserDetailRespList - 企业用户详情响应列表
   - list - 企业用户详情列表
   - listV2 - 企业用户详情列表V2
   - percent/options - 百分比选项
   - autoUpdateEnt - 自动更新企业
   - getCimDeviceList - CIM设备列表
   - updateEnt - 更新企业

2. **完善TodayController** (`/today/*`)
   - getEntUserDeviceChartResp - 今日设备曲线
   - getIotLog - 物联网日志
   - get/device/tree - 设备树
   - getMultiDeviceChartResp - 多设备曲线

3. **完善TomorrowController** (`/tomorrow/*`)
   - getPriceByResourceTypeId - 获取报价
   - getAggregatorDeliveryChart - 聚合商交割曲线
   - getAggregatorApply - 查询申报
   - getAggregatorApplyOfferResp - 查询报价
   - saveAggregatorApplyOffer - 暂存报价
   - submitAggregatorApplyOffer - 提交报价
   - getEntUserDeviceChartResp - 明日设备曲线
   - updateAggregatorApply - 更新申报

4. **完善AggregatorApplyPlanController** (`/applyPlan/*` 或 `/aggregatorPlan/*`)
   - getApplyDateList - 申报日期列表
   - getPlanList - 获取申报列表
   - getReferDatePower - 参考日功率
   - addOrUpdatePlan - 新增/编辑计划
   - getPlanDetail - 计划详情
   - getRunPlan - 运行计划

5. **PeakPlanDeclareController** (`/peakPlanDeclare/*`)
   - import - 预测数据上报

6. **FileController** (`/file/*`)
   - uploadFile - 文件上传

#### 步骤3：修改前端API调用

创建新的API配置文件，使用相对路径：

**新建 `frontend/src/modules/load-aggregation/api/console.js`**：
```javascript
import service from "@/services/http";

// Console API 基础配置
const API_BASE = ""; // 相对路径，自动使用当前域名

// 通用请求封装
function request(config) {
  return service({
    ...config,
    headers: {
      ...config.headers,
      // 使用标准的Bearer Token认证
      Authorization: `Bearer ${sessionStorage.getItem("token")}`,
    },
  });
}

// 导出所有API
export default {
  // 收益相关
  profit: {
    week: (params) => request({ method: "get", url: "/profit/week", params }),
    getContentList: (params) => request({ method: "get", url: "/profit/getContentList", params }),
    list: (params) => request({ method: "get", url: "/profit/list", params }),
    listByEntIdList: (params) => request({ method: "get", url: "/profit/listByEntIdList", params }),
  },
  
  // 昨日详情
  yesterday: {
    getResourceTypeList: (params) => request({ method: "get", url: "/yesterday/getResourceTypeList", params }),
    getLastProfit: (params) => request({ method: "get", url: "/yesterday/getLastProfit", params }),
    getOverview: (params) => request({ method: "get", url: "/yesterday/getOverview", params }),
    getDeviceList: (params) => request({ method: "get", url: "/yesterday/getDeviceList", params }),
    getEntUserDeviceChartResp: (params) => request({ method: "get", url: "/yesterday/getEntUserDeviceChartResp", params }),
    getEntUserOverviewResp: (params) => request({ method: "get", url: "/yesterday/getEntUserOverviewResp", params }),
    entInvite: (data) => request({ method: "post", url: "/yesterday/entInvite", data }),
  },
  
  // 历史查询
  historyQuery: {
    getTotalPowerChart: (params) => request({ method: "get", url: "/historyQuery/getTotalPowerChart", params }),
    getPrice: (data) => request({ method: "post", url: "/historyQuery/getPrice", data }),
    getMetricList: (data) => request({ method: "post", url: "/historyQuery/getMetricList", data }),
    profitStatistics: (data) => request({ method: "post", url: "/historyQuery/profitStatistics", data }),
    userProfitStatistics: (data) => request({ method: "post", url: "/historyQuery/userProfitStatistics", data }),
    userAdjustmentGraph: (data) => request({ method: "post", url: "/historyQuery/userAdjustmentGraph", data }),
    userAdjustmentGraphNew: (data) => request({ method: "post", url: "/historyQuery/userAdjustmentGraphNew", data }),
    userAdjustmentTable: (data) => request({ method: "post", url: "/historyQuery/userAdjustmentTable", data }),
    deviceRunStatusChart: (data) => request({ method: "post", url: "/historyQuery/deviceRunStatusChart", data }),
    getProfitCalculation: (params) => request({ method: "get", url: "/historyQuery/getProfitCalculation", params }),
    getPriceTable: (data) => request({ method: "post", url: "/historyQuery/getPriceTable", data }),
    exportAdjust: (params) => request({ method: "get", url: "/historyQuery/exportAdjust", params, responseType: "blob" }),
    exportBuZhaoUploadData: (params) => request({ method: "get", url: "/historyQuery/exportBuZhaoUploadData", params, responseType: "blob" }),
    getPriceExcel: (params) => request({ method: "get", url: "/historyQuery/getPriceExcel", params, responseType: "blob" }),
  },
  
  // 企业用户详情（需要补充Controller）
  entUserDetail: {
    options: (params) => request({ method: "get", url: "/entUserDetail/options", params }),
    getEntUserDetailRespList: (params) => request({ method: "get", url: "/entUserDetail/getEntUserDetailRespList", params }),
    list: (params) => request({ method: "get", url: "/entUserDetail/list", params }),
    listV2: (params) => request({ method: "get", url: "/entUserDetail/listV2", params }),
    percentOptions: (params) => request({ method: "get", url: "/entUserDetail/percent/options", params }),
    autoUpdateEnt: (data) => request({ method: "post", url: "/entUserDetail/autoUpdateEnt", data }),
    getCimDeviceList: (params) => request({ method: "get", url: "/entUserDetail/getCimDeviceList", params }),
    updateEnt: (data) => request({ method: "post", url: "/entUserDetail/updateEnt", data }),
  },
  
  // ... 其他模块
};
```

#### 步骤4：渐进式迁移

**阶段1**：已有接口直接切换
- 修改 `overview/api/index.js` 和 `history/api/index.js`
- 将已有Console接口的调用切换到相对路径
- 移除 AccessKey 相关代码

**阶段2**：补充缺失接口
- 根据前端需求，补充缺失的Controller
- 逐个模块测试和迁移

**阶段3**：清理旧代码
- 移除环境配置代码
- 移除 AccessKey 相关代码
- 统一使用 Bearer Token

## 三、实施计划

### 第一步：创建缺失的Controller（优先级高）

1. **EntUserDetailController** - 企业用户管理
2. **完善TodayController** - 今日数据
3. **完善TomorrowController** - 明日数据
4. **完善AggregatorPlanController** - 申报计划

### 第二步：修改前端API（优先级高）

1. 创建新的 `console.js` API文件
2. 修改 `overview` 模块使用新API
3. 修改 `history` 模块使用新API
4. 测试验证

### 第三步：处理特殊接口（优先级中）

1. **文件上传** - 创建FileController
2. **操作埋点** - operation/save 接口处理
3. **WebSocket** - 实时数据推送

### 第四步：清理和优化（优先级低）

1. 移除旧的API配置
2. 统一错误处理
3. 添加API文档
4. 性能优化

## 四、注意事项

### 1. 认证兼容性

前端当前使用 `ticket`，Console使用 `token`：
- 检查 sessionStorage 中的键名
- 确认 AuthInterceptor 能正确处理
- 可能需要统一为 `token`

### 2. 参数兼容性

- 检查前端传递的 `aggregatorId` 是否自动注入
- 确认后端是否需要从 AuthContext 获取用户信息
- 验证参数命名是否一致（驼峰 vs 下划线）

### 3. 响应格式统一

Console统一使用 `ResultVO` 包装：
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

确保前端能正确解析。

### 4. 错误处理

- 统一错误码
- 统一错误消息格式
- 前端错误提示优化

### 5. 性能考虑

- 添加接口缓存
- 优化批量查询
- 考虑分页加载

## 五、验收标准

1. ✅ 所有前端接口调用改为通过Console模块
2. ✅ 移除所有外部网关URL配置
3. ✅ 移除所有AccessKey相关代码
4. ✅ 所有功能正常运行
5. ✅ 接口响应时间无明显增加
6. ✅ 错误处理完善
7. ✅ Swagger文档完整
