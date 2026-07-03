# 调节情况页汇总功率曲线方案

## 1. 目标

补齐 `GET /historyQuery/getTotalPowerChart` 的后端实现，返回前端 `userCompletes.vue` 需要的 6 条曲线：

- `powerChart` 实际汇总功率
- `issueChart` 聚合申报功率
- `dapChart` 调度下发功率
- `baseLineChart` 基线
- `crChart` 碳排因子
- `issuePrice` 出清价格

返回结构继续复用 `IndexOverviewResp`：

- `timeList`
- 各条 `List<DataResp>`

## 2. 当前仓库现状

### 2.1 接口是空实现

- Controller:
  [HistoryQueryController.java](/Users/sl/Documents/java/enn/e-hub/e-hub-console/src/main/java/cn/sl/ehub/console/controller/loadaggregation/HistoryQueryController.java:60)

- Service:
  [HistoryQueryServiceImpl.java](/Users/sl/Documents/java/enn/e-hub/e-hub-console/src/main/java/cn/sl/ehub/console/service/impl/HistoryQueryServiceImpl.java:65)

当前 `getTotalPowerChart()` 只返回空对象。

### 2.2 前端已经固定依赖 6 条曲线

前端页面：

- [userCompletes.vue](/Users/sl/Documents/java/enn/e-hub/e-hub-console/frontend/src/modules/load-aggregation/history/components/userCompletes.vue:192)

它不会再拆多个接口，而是等 `getTotalPowerChart` 一次性返回全部曲线。

## 3. 实际汇总功率实现方案

## 3.1 业务口径

按你确认的口径，实际汇总功率应这样计算：

1. 用 `aggregatorId + resourceTypeId` 查询 `aggregator_ent_device`
2. 过滤：
   - `status = 1`
   - `model_flag = 1`
3. 取这些设备对应的 IoT 设备
4. 在 `iot_telemetry_minute` 中查询指定时间段的功率点
5. 按相同时间点聚合求和

## 3.2 关系桥接口径

这里必须按最新确认的桥接规则走：

- `aggregator_ent_device.device_id -> iot_device.device_code`
- `aggregator_ent_device.iot_device_base_id -> iot_device.id`

建议实现：

- 主路径优先用 `device_id` 匹配 `iot_device.device_code`
- 兼容路径仅对历史数据回退用 `iot_device_base_id -> iot_device.id`

## 3.3 功率测点口径

这里有一个必须先统一的问题：

- 你描述的是设备功率点 `P`
- 当前 IoT 模块默认标准测点编码是 `active_power`
  见 [IotDeviceService.java](/Users/sl/Documents/java/enn/e-hub/e-hub-service/src/main/java/cn/sl/ehub/service/service/IotDeviceService.java:28)

建议口径：

- 如果已经统一把 `P` 映射为 IoT 标准功率测点，则查询 `point_code = 'active_power'`
- 如果当前历史数据里仍写 `P`，则需要兼容 `point_code in ('active_power', 'P')`

否则会直接查不出实际功率曲线。

## 3.4 SQL/查询步骤

建议步骤：

1. 查业务设备

```text
aggregator_ent_device
where aggregator_id = ?
  and resource_type_id = ?
  and status = 1
  and model_flag = 1
```

2. 提取设备桥接键

- 主集合：`device_id`
- 兼容集合：`iot_device_base_id`

3. 查 IoT 设备映射

```text
iot_device
where ent_id in (...)
  and deleted = 0
  and asset_status = 1
  and status = 1
```

匹配方式：

- `iot_device.device_code in (aggregator_ent_device.device_id)`
- 或 `iot_device.id in (aggregator_ent_device.iot_device_base_id)`

4. 查分钟时序

```text
iot_telemetry_minute
where aggregator_id = ?
  and device_id in (...)
  and point_code in ('active_power', 'P')
  and minute_time between ? and ?
```

5. 聚合

```text
group by minute_time
sum(point_value)
```

6. 输出为 `List<DataResp>`

## 3.5 时间轴建议

建议 `timeList` 直接使用统一后的分钟时间序列：

- 单天：`HH:mm`
- 多天：`yyyy-MM-dd HH:mm`

前端只消费字符串，不限制格式，但后端必须保证：

- 6 条曲线使用同一份 `timeList`
- 没有值的点要补 `0` 或 `null`

建议汇总功率补 `0`，避免曲线断裂。

## 4. 其他 5 条曲线参考原有逻辑

这 5 条曲线不建议现阶段从 IoT 重新计算，按原有业务表读取更稳。

## 4.1 聚合申报功率 `issueChart`

参考表：

- `aggregator_date_issue_chart`

代码依据：

- VO: [AggregatorDateIssueChart.java](/Users/sl/Documents/java/enn/e-hub/e-hub-service/src/main/java/cn/sl/ehub/service/vo/AggregatorDateIssueChart.java:18)
- Service: [AggregatorDateIssueChartServiceImpl.java](/Users/sl/Documents/java/enn/e-hub/e-hub-console/src/main/java/cn/sl/ehub/console/service/impl/AggregatorDateIssueChartServiceImpl.java:24)

关键字段：

- `aggregator_id`
- `resource_type_id`
- `date`
- `issue_chart`

实现方式：

- 按日期范围查询多条记录
- `issue_chart` 解析成 `List<DataResp>`
- 按日期顺序拼接

## 4.2 调度下发功率 `dapChart`

参考表：

- `aggregator_dap_chart`

代码依据：

- VO: [AggregatorDapChart.java](/Users/sl/Documents/java/enn/e-hub/e-hub-service/src/main/java/cn/sl/ehub/service/vo/AggregatorDapChart.java:1)
- Service: [AggregatorDapChartServiceImpl.java](/Users/sl/Documents/java/enn/e-hub/e-hub-console/src/main/java/cn/sl/ehub/console/service/impl/AggregatorDapChartServiceImpl.java:14)

关键字段：

- `aggregator_id`
- `resource_type`
- `date`
- `dap_chart`

实现方式：

- 按日期范围查询
- 解析 `dap_chart`
- 拼接曲线

## 4.3 基线 `baseLineChart`

参考表：

- `aggregator_base_line_load_chart`

代码依据：

- VO: [AggregatorBaseLineLoadChart.java](/Users/sl/Documents/java/enn/e-hub/e-hub-service/src/main/java/cn/sl/ehub/service/vo/AggregatorBaseLineLoadChart.java:14)
- Service: [AggregatorBaseLineLoadChartServiceImpl.java](/Users/sl/Documents/java/enn/e-hub/e-hub-console/src/main/java/cn/sl/ehub/console/service/impl/AggregatorBaseLineLoadChartServiceImpl.java:21)

关键字段：

- `aggregator_id`
- `resource_type`
- `base_date`
- `base_line_load_chart`

说明：

- 这是聚合商级基线表
- 企业级还有 `aggregator_ent_base_line_load_chart`
- 当前调节情况页用的是聚合商维度，优先取聚合商表

## 4.4 碳排因子 `crChart`

参考表：

- `aggregator_cr_chart`

代码依据：

- VO: [AggregatorCrChart.java](/Users/sl/Documents/java/enn/e-hub/e-hub-service/src/main/java/cn/sl/ehub/service/vo/AggregatorCrChart.java:14)
- Service: [AggregatorCrChartServiceImpl.java](/Users/sl/Documents/java/enn/e-hub/e-hub-console/src/main/java/cn/sl/ehub/console/service/impl/AggregatorCrChartServiceImpl.java:24)

关键字段：

- `aggregator_id`
- `resource_type`
- `cr_date`
- `cr_load_chart`

## 4.5 出清价格 `issuePrice`

参考表：

- `aggregator_resource_date_issue_offer`

代码依据：

- VO: [AggregatorResourceDateIssueOffer.java](/Users/sl/Documents/java/enn/e-hub/e-hub-service/src/main/java/cn/sl/ehub/service/vo/AggregatorResourceDateIssueOffer.java:18)
- Service: [AggregatorResourceDateIssueOfferServiceImpl.java](/Users/sl/Documents/java/enn/e-hub/e-hub-console/src/main/java/cn/sl/ehub/console/service/impl/AggregatorResourceDateIssueOfferServiceImpl.java:26)

关键字段：

- `aggregator_id`
- `resource_type_id`
- `date`
- `offer`
- `price_detail`
- `price_chart`

建议优先使用：

- `price_chart`

原因：

- 前端要的是曲线，不是单值
- `offer` 更像汇总价格
- `price_detail` 更像明细描述

## 5. 当前仓库缺少哪些表

如果按“原有逻辑”去实现其他 5 条曲线，当前仓库代码引用了以下业务表：

1. `aggregator_date_issue_chart`
2. `aggregator_dap_chart`
3. `aggregator_base_line_load_chart`
4. `aggregator_cr_chart`
5. `aggregator_resource_date_issue_offer`

问题在于：

- 当前仓库里有 VO、Mapper、Service
- 但在 `e-hub-service/src/main/resources/sql` 下没有这些表的建表 SQL

也就是说：

- 这些表大概率在外部业务库中存在
- 但当前仓库本身并没有提供 DDL/初始化脚本

这个需要明确补齐，否则即使后端代码写完，单独部署当前项目也无法初始化这些业务曲线表。

## 6. 还存在的几个关键问题

## 6.1 `issueChart` / `dapChart` 语义有命名冲突

当前 `IndexOverviewResp` 对前端语义定义是：

- `issueChart` = 聚合申报功率
- `dapChart` = 调度下发功率

但 `AggregatorDateIssueChart.java` 里的注释写的是“调度下发功率曲线”。

这说明至少存在一个问题：

- 要么历史命名有误
- 要么表含义和前端展示语义已经漂移

实现前必须确认：

- `aggregator_date_issue_chart.issue_chart` 到底是“申报”还是“下发”

否则会把两条曲线画反。

## 6.2 日期字段类型不统一

这几张表的日期字段类型不一致：

- `aggregator_date_issue_chart.date` 是 `String`
- `aggregator_dap_chart.date` 是 `Date`
- `aggregator_cr_chart.cr_date` 是 `String`
- `aggregator_base_line_load_chart.base_date` 是 `String`
- `aggregator_resource_date_issue_offer.date` 是 `String`

实现时必须统一排序和格式化逻辑，否则跨天范围拼接容易乱序。

## 6.3 曲线字段是 JSON 字符串，不是明细表

除实际汇总功率外，其他 5 条曲线当前都不是明细行存储，而是：

- 每天一条记录
- 曲线内容存在 `*_chart` 字段里

这意味着：

- 不能直接 SQL 聚合成 `timeList`
- 要先查记录
- 再反序列化 JSON
- 再按时间轴合并

## 6.4 实际功率和其余 5 条曲线时间轴可能不一致

实际功率来自 `iot_telemetry_minute`，天然是分钟粒度。

而其余 5 条曲线来自业务表里的 JSON 曲线，时间粒度可能是：

- 15 分钟
- 30 分钟
- 1 小时

因此必须确认：

- 调节情况页最终希望所有曲线统一到什么粒度

建议：

- 如果其余 5 条曲线是 15 分钟粒度，则实际汇总功率也按 15 分钟聚合
- 不建议前端自己对齐粒度

## 7. 建议实施顺序

### 第一步：先实现 `powerChart`

原因：

- 来源清晰
- 不依赖缺失的业务曲线表
- 可以直接验证 IoT 聚合链路

### 第二步：把其余 5 条曲线按原表拼上

顺序建议：

1. `issueChart`
2. `dapChart`
3. `baseLineChart`
4. `crChart`
5. `issuePrice`

### 第三步：补齐缺失 DDL 和语义确认

至少需要补：

- 5 张业务曲线表的建表脚本
- `issueChart` / `dapChart` 的真实业务语义说明
- 曲线 JSON 字段示例

## 8. 最终建议

建议把这次优化拆成两层：

1. 当前项目内立即可做的
   实现 `powerChart`，并把其余 5 条曲线按已有业务表读取拼装

2. 必须补齐外部依赖的
   补 5 张业务曲线表 DDL，确认 `issue/dap` 语义，确认曲线粒度

这样风险最小，也最符合当前仓库真实状态。
