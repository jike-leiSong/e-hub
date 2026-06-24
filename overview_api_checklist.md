# 运营总览模块接口清单

## 接口分类与状态

### ✅ 已实现（无需修改）

1. **收益相关** - `/profit/*`
   - `GET /profit/week` - 本周收益 (ProfitController)
   - `GET /profit/getContentList` - 筛选内容列表 (ProfitController)
   - `GET /profit/list` - 收益列表 (ProfitController)
   - `GET /profit/listByEntIdList` - 按企业ID收益列表 (ProfitController)

2. **昨日详情** - `/yesterday/*`
   - `GET /yesterday/getResourceTypeList` - 资源类型列表 (YesterdayController)
   - `GET /yesterday/getLastProfit` - 上次收益 (YesterdayController)
   - `GET /yesterday/getOverview` - 总览 (YesterdayController)
   - `GET /yesterday/getDeviceList` - 设备列表 (YesterdayController)
   - `GET /yesterday/getEntUserDeviceChartResp` - 昨日设备曲线 (YesterdayController)
   - `GET /yesterday/getEntUserOverviewResp` - 用户情况 (YesterdayController)
   - `POST /yesterday/entInvite` - 企业用户邀约 (YesterdayController)

### ⚠️ 需要补充的接口

3. **天气接口** - `/weather/*`
   - `POST /weather/getDayWeather` - 获取天气 (需创建WeatherController)

4. **明日详情** - `/tomorrow/*` (TomorrowController需要补充)
   - `GET /tomorrow/getPriceByResourceTypeId` - 获取报价
   - `GET /tomorrow/getAggregatorDeliveryChart` - 聚合商交割曲线
   - `GET /tomorrow/getAggregatorApply` - 查询申报
   - `GET /tomorrow/getAggregatorApplyOfferResp` - 查询报价响应
   - `POST /tomorrow/saveAggregatorApplyOffer` - 暂存报价
   - `POST /tomorrow/submitAggregatorApplyOffer` - 提交报价
   - `GET /tomorrow/getEntUserDeviceChartResp` - 明日设备曲线
   - `POST /tomorrow/updateAggregatorApply` - 更新申报

5. **今日详情** - `/today/*` (TodayController需要补充)
   - `GET /today/getEntUserDeviceChartResp` - 今日设备曲线
   - `GET /today/getIotLog` - 物联网日志
   - `GET /today/get/device/tree` - 设备树
   - `GET /today/getMultiDeviceChartResp` - 多设备曲线

6. **企业用户详情** - `/entUserDetail/*` (需创建EntUserDetailController)
   - `GET /entUserDetail/getEntUserDetailRespList` - 企业用户详情响应列表
   - `GET /entUserDetail/options` - 企业用户选项
   - `GET /entUserDetail/list` - 企业用户详情列表
   - `GET /entUserDetail/listV2` - 企业用户详情列表V2
   - `GET /entUserDetail/percent/options` - 百分比选项
   - `POST /entUserDetail/autoUpdateEnt` - 自动更新企业
   - `GET /entUserDetail/getCimDeviceList` - CIM设备列表
   - `POST /entUserDetail/updateEnt` - 更新企业

7. **申报计划** - `/applyPlan/*` 或 `/aggregatorPlan/*`
   - `GET /applyPlan/getApplyDateList` - 申报日期列表
   - `POST /aggregatorPlan/getPlanList` - 获取申报列表
   - `POST /aggregatorPlan/getReferDatePower` - 参考日功率
   - `POST /aggregatorPlan/addOrUpdatePlan` - 新增/编辑计划
   - `GET /aggregatorPlan/getPlanDetail` - 计划详情
   - `GET /aggregatorPlan/getRunPlan` - 运行计划

8. **文件上传** - `/file/*` (需创建FileController)
   - `POST /file/uploadFile` - 文件上传

9. **峰值计划申报** - `/peakPlanDeclare/*` (需创建PeakPlanDeclareController)
   - `POST /peakPlanDeclare/import` - 预测数据上报

10. **操作日志（埋点）** - `/operation/*`
   - `POST /operation/save` - 保存操作日志（可选实现）

## 实施优先级

### 第一批（核心功能）
1. TomorrowController 补充 - 明日申报相关
2. TodayController 补充 - 今日实时数据
3. EntUserDetailController 新建 - 企业用户管理

### 第二批（扩展功能）
1. AggregatorPlanController 检查补充 - 申报计划
2. WeatherController 新建 - 天气接口
3. FileController 新建 - 文件上传

### 第三批（可选功能）
1. PeakPlanDeclareController - 峰值计划
2. OperationController - 操作日志（可考虑前端直接调用或不实现）
