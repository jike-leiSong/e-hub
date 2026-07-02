# IoT 数据查询数据层方案

## 1. 背景

当前仓库已经具备 IoT 数据查询的基础能力：

- 资产主数据：`iot_device`、`iot_device_point`
- 原始接入数据：`iot_telemetry_raw`
- 标准分钟数据：`iot_telemetry_minute`
- 业务侧项目编码现阶段主要复用：`aggregator_single_model_data.energy_station_code`

同时，现有代码已经存在一版查询实现：

- `IotTelemetryQueryService` 提供分钟明细、小时/日聚合、原始数据追溯
- `IotTelemetryQueryMapper.xml` 已支持 `aggregatorId`、`entId`、`deviceIds`、`pointCodes`
- `iot_device.project_id` 当前实际按注释和用法对齐 `energyStationCode`，这是兼容现状，不是推荐的长期边界

但如果要支撑业务侧统一使用，还存在三个明显问题：

1. 查询范围没有统一抽象
   业务方仍需要自己决定传 `aggregatorId`、`entId`、`projectId`、`deviceIds`

2. 实时查询和历史查询没有统一入口
   当前只有分钟明细、分钟聚合、原始追溯，缺少“当前最新值”以及“按业务层级聚合”的统一模型

3. 设备功能和测点语义没有进入查询层
   当前查询以 `pointCode` 为主，业务无法稳定按“功率、状态、温度、累计电量”等业务指标发起查询

本方案目标是在现有表结构基础上，封装一个面向业务的 IoT 数据层，用于：

- 实时数据查询
- 历史数据查询
- 企业级设备聚合
- 聚合商级设备聚合
- 项目级设备聚合

## 2. 设计目标

### 2.1 对外一个查询入口

对业务域暴露一个统一入口：

```java
IotDataQueryResp query(IotDataQueryReq req)
```

内部再根据 `queryMode` 路由到实时、历史、聚合、追溯等实现。

这样做的目的不是把所有 SQL 塞进一个方法，而是：

- 对业务只有一个统一心智模型
- 对内仍然保持实时/历史/原始数据分层
- 后续可平滑增加新查询模式

### 2.2 查询范围统一建模

业务层不能再直接关心底层表字段组合，而是声明查询范围：

- `AGGREGATOR`：聚合商级
- `ENTERPRISE`：企业级
- `PROJECT`：业务项目级
- `DEVICE`：设备级

范围解析后，底层统一转换为设备集合，再访问时序数据表。

注意：

- 这里的 `PROJECT` 是业务查询维度
- 不代表 IoT 域必须维护一张业务项目主表
- 业务项目和 IoT 设备的关系，应通过独立关系层解析，不应天然固化在 IoT 资产模型里

### 2.3 查询指标统一建模

业务层应传“业务指标编码”，而不是直接传底层 `point_code`。

例如：

- `ACTIVE_POWER`
- `STATUS`
- `TEMPERATURE`
- `TOTAL_ENERGY`

再由指标语义层映射到具体设备类型和测点编码。

### 2.4 实时与历史解耦但统一封装

实时查询和历史查询的数据源、索引策略、返回结构不同，但通过统一请求对象和统一范围模型对外输出一致能力。

## 3. 现状约束

### 3.1 项目维度当前不是独立主模型

现有代码中：

- `iot_device.project_id` 实际承载 `energyStationCode`
- 项目信息主要从 `aggregator_single_model_data` 补充名称、资源类型

因此本期方案必须兼容：

```text
project_id == energy_station_code
```

但这只是兼容现状，不是最终推荐模型。

更合理的目标边界是：

- IoT 域维护设备、测点、采集、时序数据
- 业务域维护聚合商/企业/项目与设备的业务归属关系
- 查询时由范围解析层把“业务项目”翻译成设备集合

也就是说，长期不建议让 `iot_device.project_id` 继续承担业务项目关系。

### 3.1.1 当前方案中不需要 `iot_project`、`iot_project_external_ref`

基于当前仓库现状，这两张表都不应进入本期方案：

1. `iot_project`
   不需要

原因：

- 业务项目关系已经由 `aggregator_single_model_data` 维护
- 设备到业务项目的关联已可通过现有业务关系表和设备表解析
- IoT 域不需要再单独维护一套“项目主数据”

2. `iot_project_external_ref`
   也不需要

原因：

- 当前采集匹配是“设备 + 测点”口径，不是“项目 + 测点”口径
- 匹配成功后，项目归属可由设备归属或业务关系解析得到
- 匹配失败时，也不保留三方项目标识，项目归属完全通过内部模型关系解析

因此本期设计建议：

- 不引入 `iot_project`
- 不引入 `iot_project_external_ref`
- 不保留 `external_project_id`
- 业务项目范围统一从 `aggregator_single_model_data`、`aggregator_ent_device` 解析到设备集合

### 3.2 现有分钟表更像“标准历史事实表”

`iot_telemetry_minute` 当前适合承担：

- 分钟级历史明细
- 小时/日聚合的源表
- 最新值的兜底来源

但它不适合长期承担大范围实时最新值主查询，因为当前最新值查询依赖 `MAX(minute_time)` 子查询，随着设备数增长会变慢。

### 3.3 现有表缺少“指标语义配置”

当前元数据表 `iot_device_point` / `iot_device_type_point_metadata` 只描述测点本身，不足以描述：

- 业务指标编码
- 默认聚合方式
- 是否允许跨设备求和
- 实时展示口径
- 历史统计口径

这一层需要补齐。

## 4. 总体方案

### 4.1 分层结构

```text
业务服务 / controller / upstream
        |
        v
IotDataQueryFacade
        |
        +-- IotQueryScopeResolver
        +-- IotMetricSemanticResolver
        +-- IotRealtimeQueryRepository
        +-- IotHistoryQueryRepository
        +-- IotRawTraceRepository
        +-- IotAggregationAssembler
```

职责划分：

- `IotDataQueryFacade`
  统一入口，校验参数，按模式分发

- `IotQueryScopeResolver`
  将聚合商/企业/项目/设备范围解析为底层查询范围

- `IotMetricSemanticResolver`
  将业务指标编码解析成具体测点编码和聚合口径

- `IotRealtimeQueryRepository`
  负责当前最新值、实时聚合查询

- `IotHistoryQueryRepository`
  负责分钟明细、小时聚合、日聚合

- `IotRawTraceRepository`
  负责原始报文和匹配追溯

- `IotAggregationAssembler`
  将设备点位数据聚合成企业级、项目级、聚合商级结果

### 4.2 模块落位

- `e-hub-service`
  放查询领域对象、Facade、Repository、Mapper

- `e-hub-console`
  负责接口暴露、权限注入、分页导出

- `e-hub-upstream`
  可直接调用 `e-hub-service` 的查询 Facade 获取实时或历史上送数据

注意：

- 权限解析仍然留在 `console` 层
- 数据层只接收已经标准化的查询范围
- 这样 `upstream` 不依赖 `console.auth`
- 业务项目到设备集合的翻译，也应放在范围解析层而不是 IoT 设备表

## 5. 查询模型设计

### 5.1 统一请求对象

建议新增：

```java
public class IotDataQueryReq {
    private String queryMode;
    private IotQueryScope scope;
    private List<String> metricCodes;
    private String timeGranularity;
    private String aggregateFunc;
    private String groupBy;
    private String startTime;
    private String endTime;
    private Integer limit;
    private Integer offset;
}
```

字段建议：

- `queryMode`
  `LATEST`、`HISTORY_SERIES`、`HISTORY_AGG`、`RAW_TRACE`

- `scope`
  查询范围对象

- `metricCodes`
  业务指标编码列表，例如 `ACTIVE_POWER`

- `timeGranularity`
  `MINUTE`、`HOUR`、`DAY`

- `aggregateFunc`
  `SUM`、`AVG`、`MAX`、`MIN`

- `groupBy`
  `AGGREGATOR`、`ENTERPRISE`、`PROJECT`、`DEVICE`、`POINT`、`TIME`

### 5.1.1 查询范围对象

```java
public class IotQueryScope {
    private String scopeType;
    private String aggregatorId;
    private String entId;
    private String projectId;
    private List<Long> deviceIds;
}
```

范围规则：

- `AGGREGATOR`
  必填 `aggregatorId`

- `ENTERPRISE`
  必填 `entId`

- `PROJECT`
  必填 `projectId`
  这里表示业务项目编码
  当前阶段可兼容映射到 `energyStationCode`

- `DEVICE`
  必填 `deviceIds`

### 5.1.2 统一响应对象

```java
public class IotDataQueryResp {
    private String queryMode;
    private String scopeType;
    private List<IotDataItem> list;
    private Long total;
}
```

其中 `IotDataItem` 建议按模式扩展：

- 最新值项 `IotLatestItem`
- 时序点项 `IotSeriesItem`
- 聚合结果项 `IotAggregateItem`
- 原始追溯项 `IotRawTraceItem`

对外仍挂在统一响应结构下。

### 5.2 一个入口，四种模式

### 5.2.1 `LATEST`

查询当前最新值，用于：

- 设备实时看板
- 企业实时负荷
- 项目实时功率
- 聚合商总实时功率

### 5.2.2 `HISTORY_SERIES`

查询分钟/小时/日序列，用于：

- 历史趋势图
- 对账曲线
- 单设备运行轨迹

### 5.2.3 `HISTORY_AGG`

查询历史时间段内统计结果，用于：

- 某企业某天累计功率求和
- 项目级平均负荷
- 聚合商下所有项目的日最大值

### 5.2.4 `RAW_TRACE`

查询原始上报明细，用于：

- 问题追溯
- 三方数据对账
- 映射排错

## 6. 范围解析设计

### 6.1 核心原则

业务不直接拼 `WHERE` 条件，统一先做范围解析。

范围解析输出建议为：

```java
public class IotResolvedScope {
    private String scopeType;
    private String aggregatorId;
    private String entId;
    private String projectId;
    private List<String> projectIds;
    private List<Long> deviceIds;
}
```

这里的关键点是：

- `projectId` 是业务查询输入
- `deviceIds` 才是 IoT 数据层真正稳定的查询对象
- 业务项目到设备集合的转换，基于现有业务关系表完成，不依赖 `iot_project`

### 6.2 三类业务聚合范围

### 6.2.1 聚合商级聚合

规则：

- 设备来源：`iot_device.aggregator_id = ?`
- 可选再叠加状态过滤：`deleted=0`、`asset_status=1`

典型结果：

- 聚合商总设备实时功率
- 聚合商所有项目分组趋势

### 6.2.2 企业级聚合

规则：

- 设备来源：`iot_device.ent_id = ?`
- 项目集合可从这些设备的 `project_id` 得到

典型结果：

- 企业所有设备当前功率求和
- 企业内各项目分组趋势

### 6.2.3 项目级聚合

规则：

- 输入是业务项目编码
- 当前兼容实现可先映射到 `energyStationCode`
- 长期目标是先解析出 `deviceIds`，再查 IoT 时序表

典型结果：

- 单项目所有设备实时功率
- 项目分钟负荷曲线

### 6.3 为什么范围解析不能放在 Controller

Controller 可以做权限收口，但不能承担业务范围建模，否则：

- `console` 和 `upstream` 会复制逻辑
- 业务服务也会自己拼 `deviceIds`
- 业务关系表一旦变化，会出现多处同步修改

建议做法：

- `console` 只做权限注入
- `service` 层做统一范围解析

## 7. 指标语义层设计

### 7.1 问题

目前查询接口用的是 `pointCode`，这对业务并不稳定：

- 不同设备类型同一个业务含义，底层测点可能不同
- 某些测点可以求和，某些测点不能求和
- 某些测点适合最新值，某些测点适合累计量

### 7.2 建议新增语义配置表

建议新增一张查询语义表：

```text
iot_point_query_semantic
- id
- device_type_code
- point_code
- metric_code
- metric_name
- realtime_enabled
- history_enabled
- scope_agg_func
- time_agg_func
- unit
- value_type
- deleted
```

核心含义：

- `metric_code`
  业务统一指标编码

- `scope_agg_func`
  跨设备聚合默认函数，如 `SUM`

- `time_agg_func`
  历史时间聚合默认函数，如 `AVG`

示例：

```text
ENERGY_STORAGE + active_power -> ACTIVE_POWER -> scope SUM -> time AVG
ENERGY_STORAGE + status       -> STATUS       -> scope MAX -> time MAX
METER          + total_energy -> TOTAL_ENERGY -> scope SUM -> time MAX
```

### 7.3 为什么不用业务直接传 `pointCode`

如果业务直接传 `pointCode`，会产生两个问题：

1. 指标口径散落在各业务模块
2. 同类设备换测点编码时，所有业务都要改

所以对业务应暴露 `metricCode`，在数据层转成 `pointCode`。

## 8. 存储与查询设计

### 8.1 历史事实表

继续使用：

- `iot_telemetry_raw`
- `iot_telemetry_minute`

定位不变：

- `iot_telemetry_raw`
  原始接入追溯

- `iot_telemetry_minute`
  标准分钟事实表

### 8.2 建议新增实时快照表

建议新增：

```text
iot_telemetry_latest
- id
- aggregator_id
- ent_id
- project_id
- device_id
- device_code
- point_code
- point_value
- unit
- quality
- data_time
- minute_time
- source_code
- receive_time
```

唯一键：

```text
device_id + point_code
```

作用：

- 解决大范围实时最新值查询性能问题
- 让聚合商级、企业级、项目级实时聚合直接走快照表
- 避免每次从 `iot_telemetry_minute` 做最新值子查询

写入策略：

- `IotTelemetryIngestService.saveMinuteRecord` 成功后同步 upsert `iot_telemetry_latest`

### 8.3 历史聚合表

第一阶段可以不建，直接基于分钟表计算小时/日聚合。

当数据量增大后，再引入：

- `iot_telemetry_hour`
- `iot_telemetry_day`

策略：

- 小时表按整点离线汇总
- 日表按天离线汇总

这样能保证：

- 当前阶段先快落地
- 后续性能扩容时不改业务接口

### 8.4 索引建议

当前 `iot_telemetry_minute` 需要补充面向范围查询的索引。

建议增加：

```text
idx_iot_telemetry_agg_point_time   (aggregator_id, point_code, minute_time, device_id)
idx_iot_telemetry_ent_point_time   (ent_id, point_code, minute_time, device_id)
idx_iot_telemetry_project_point_time (project_id, point_code, minute_time, device_id)
idx_iot_telemetry_device_time      (device_id, minute_time)
```

`iot_telemetry_latest` 建议索引：

```text
uk_iot_telemetry_latest            (device_id, point_code)
idx_iot_latest_agg_point           (aggregator_id, point_code, project_id)
idx_iot_latest_ent_point           (ent_id, point_code, project_id)
idx_iot_latest_project_point       (project_id, point_code)
```

## 9. 查询执行流程

### 9.1 实时查询流程

```text
业务请求
  -> 统一请求对象
  -> 范围解析
  -> 指标语义解析(metricCode -> pointCode)
  -> 查询 iot_telemetry_latest
  -> 按 groupBy 做聚合
  -> 返回业务结果
```

示例：

```text
查询企业 ENT1001 当前总有功功率
scopeType=ENTERPRISE
metricCode=ACTIVE_POWER
queryMode=LATEST
groupBy=ENTERPRISE
aggregateFunc=SUM
```

执行口径：

- 查出该企业下所有有效设备
- 找出这些设备中与 `ACTIVE_POWER` 语义匹配的测点
- 从 `iot_telemetry_latest` 取各设备最新值
- `SUM(point_value)` 输出企业级实时值

### 9.2 历史查询流程

```text
业务请求
  -> 范围解析
  -> 指标语义解析
  -> 查询 iot_telemetry_minute
  -> 按 MINUTE/HOUR/DAY 归桶
  -> 按 scope/device/project/time 维度聚合
  -> 返回序列结果
```

示例：

```text
查询项目 P001 昨天 00:00-24:00 的分钟负荷曲线
scopeType=PROJECT
queryMode=HISTORY_SERIES
timeGranularity=MINUTE
metricCode=ACTIVE_POWER
groupBy=TIME
aggregateFunc=SUM
```

执行口径：

- `project_id = P001`
- 过滤业务语义对应测点
- 对同一时间桶内所有设备值求和
- 返回项目级分钟曲线

### 9.3 原始追溯流程

```text
业务请求
  -> 范围解析
  -> 按设备/项目/来源筛选 iot_telemetry_raw
  -> 返回原始报文和匹配状态
```

此模式只用于排障，不作为业务主查询链路。

## 10. 与当前代码的衔接方式

### 10.1 不是重写，而是升级现有查询服务

建议演进路径：

1. 以 `IotTelemetryQueryService` 为基础重构为 `IotDataQueryFacade`
2. 保留现有分钟明细、分钟聚合、原始追溯查询能力
3. 补充：
   - `projectId` 查询范围
   - `LATEST` 查询模式
   - 统一请求对象
   - 指标语义解析
4. 逐步把旧接口迁移到新 Facade

### 10.2 当前已有能力可直接复用

- `IotTelemetryQueryMapper.xml`
  可复用分钟明细、小时/日聚合 SQL

- `LoadAggregationScopeService`
  可继续用于控制器权限注入

- `IotTelemetryIngestService`
  可继续承担标准化入库，并增加最新值快照写入
  项目归属优先从设备归属或接入配置兜底，不依赖项目映射表

- `aggregator_single_model_data`
  当前阶段承担业务项目名称、资源类型和项目范围解析输入

- `aggregator_ent_device`
  当前阶段承担业务设备范围解析，是业务项目到设备集合的重要关系来源

### 10.3 建议新增的主要类

建议在 `e-hub-service` 中新增：

- `dto/iot/query/IotDataQueryReq`
- `dto/iot/query/IotDataQueryResp`
- `dto/iot/query/IotQueryScope`
- `dto/iot/query/IotResolvedScope`
- `service/iot/IotDataQueryFacade`
- `service/iot/IotQueryScopeResolver`
- `service/iot/IotMetricSemanticResolver`
- `mapper/IotTelemetryLatestMapper`

如果希望控制改动面，也可以先保留在现有 `dto.iot` 包下。

## 11. 分阶段实施建议

### 11.1 第一阶段

目标：先让业务可用。

实施内容：

- 统一请求对象
- 统一范围模型
- 在现有 `IotTelemetryQueryService` 上支持业务项目查询输入
- 新增 `LATEST` 查询模式
- 新增 `iot_telemetry_latest`
- 保持 `project_id = energyStationCode` 仅作为兼容实现
- 明确不建设 `iot_project`、`iot_project_external_ref`

产出：

- 一个稳定可复用的业务查询入口
- 支持聚合商/企业/项目三层设备聚合

### 11.2 第二阶段

目标：补足指标语义和性能。

实施内容：

- 新增 `iot_point_query_semantic`
- 业务改用 `metricCode`
- 优化大范围聚合 SQL 和索引
- 将业务项目到设备范围的解析从兼容字段中剥离

产出：

- 业务不再依赖底层 `pointCode`
- 设备类型差异被数据层吸收

### 11.3 第三阶段

目标：支撑更大数据量和更长时间跨度。

实施内容：

- 引入小时/日汇总表
- 增加离线聚合作业
- 对超长时间范围查询切换到预聚合表

产出：

- 历史查询性能稳定
- 业务接口不变

## 12. 关键结论

本方案的核心不是再加几个查询接口，而是建立三层统一抽象：

1. 统一范围
   聚合商、企业、项目、设备都先转成标准范围对象

2. 统一指标
   业务按 `metricCode` 查询，数据层负责测点映射和口径控制

3. 统一入口
   对外一个查询入口，对内分实时、历史、追溯三条实现链路

按当前仓库现状，最稳妥的落地方式是：

- 继续使用 `iot_telemetry_minute` 作为历史事实表
- 新增 `iot_telemetry_latest` 承担实时快照
- 将 `project_id = energyStationCode` 视为过渡兼容，而不是 IoT 核心边界
- 在此基础上封装统一查询 Facade

这样既能满足当前企业级、聚合商级、项目级设备聚合需求，也不会和现有代码结构冲突。
