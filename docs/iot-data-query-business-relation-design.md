# IoT 数据查询方案（业务关系解耦版）

## 1. 结论先行

按当前边界，IoT 域不维护项目关系。

具体结论：

- `iot_device` 不保存 `project_id`
- `iot_access_app` 不保存默认项目
- `iot_telemetry_raw`、`iot_telemetry_minute`、后续 `iot_telemetry_latest` 不保存项目字段
- 项目级、企业级、聚合商级查询范围，全部通过业务关系表解析成 IoT 设备集合

IoT 域只做四件事：

- 设备资产管理
- 测点管理
- 三方接入匹配
- 时序数据存储与查询

项目、企业资源聚合、设备参与关系，都属于业务域。

## 2. 目标边界

### 2.1 IoT 域负责什么

IoT 域负责：

- `iot_device`
- `iot_device_point`
- `iot_gateway`
- `iot_device_group`
- `iot_telemetry_raw`
- `iot_telemetry_minute`
- 后续可新增 `iot_telemetry_latest`

IoT 查询只认：

- `aggregator_id`
- `ent_id`
- `device_id`
- `device_code`
- `point_code`
- `time`

### 2.2 IoT 域不负责什么

IoT 域不负责：

- 项目归属
- 能源站归属
- 业务设备参与关系
- 聚合商项目范围
- 企业项目范围
- 市场资源范围

这些范围全部由业务关系表维护。

### 2.3 业务域负责什么

当前仓库里业务关系主要由以下表承担：

- `aggregator_single_model_data`
  维护业务项目主数据，例如 `energy_station_code`

- `aggregator_ent_device`
  维护业务设备、企业、项目和 IoT 设备之间的关系

这个边界意味着：

- 业务项目查询先查业务关系表
- 解析出对应的 IoT 设备
- 再查 IoT 时序表

## 3. 现状里的可复用关系

### 3.1 项目主数据

当前业务项目主键可继续使用：

```text
aggregator_single_model_data.energy_station_code
```

这代表业务项目编码，不属于 IoT 域。

### 3.2 业务设备到 IoT 设备的桥接

按当前确认的真实口径，`aggregator_ent_device` 里有两条不同用途的桥接键：

1. 业务可读桥

```text
aggregator_ent_device.device_id = iot_device.device_code
```

说明：

- `aggregator_ent_device.device_id` 在业务表中表达的是“设备编码”
- 它对应 IoT 侧的标准设备编码 `iot_device.device_code`
- 这是项目范围解析、设备清单对齐、业务查询时应优先使用的桥接键

2. 内部主键桥

```text
aggregator_ent_device.iot_device_base_id = iot_device.id
```

说明：

- `iot_device_base_id` 表达的是 IoT 设备内部 ID
- 适合内部跳转、已有存量逻辑兼容
- 不适合作为长期业务关系主桥接键

现有代码里 `getDeviceByIdOrCode` 已经兼容 “主键或编码” 两种定位方式，这也是当前还能工作的重要原因。

### 3.3 需要统一的桥接口径

方案建议统一成：

- 业务关系解析默认走：
  `aggregator_ent_device.device_id -> iot_device.device_code`

- 内部兼容和过渡逻辑可保留：
  `aggregator_ent_device.iot_device_base_id -> iot_device.id`

原因：

- `device_code` 是业务可读主键
- 不依赖数据库自增 ID
- 更适合跨表、跨系统、导入导出和排障
- `iot_device.id` 只作为内部兼容键，不作为业务关系主键

## 4. 目标数据模型

## 4.1 IoT 核心表

### `iot_device`

保留字段：

- `id`
- `aggregator_id`
- `ent_id`
- `device_code`
- `device_name`
- `device_type_code`
- `device_type_name`
- `device_group_id`
- `gateway_id`
- `third_party_api`
- `third_party_code`
- `status`
- `asset_status`
- `online_status`
- `last_data_time`

移除字段：

- `project_id`

### `iot_access_app`

保留字段：

- `id`
- `source_code`
- `source_name`
- `aggregator_id`
- `ent_id`
- `access_key`
- `user_key`
- `enabled`

移除字段：

- `project_id`

### `iot_telemetry_raw`

保留字段：

- `interface_type`
- `source_code`
- `ent_id`
- `device_id`
- `device_code`
- `point_code`
- `external_device_id`
- `external_metric`
- `data_time`
- `raw_value`
- `receive_time`
- `raw_payload`
- `match_status`
- `match_reason`

移除字段：

- `project_id`

### `iot_telemetry_minute`

保留字段：

- `aggregator_id`
- `ent_id`
- `device_id`
- `device_code`
- `point_code`
- `data_time`
- `minute_time`
- `point_value`
- `unit`
- `quality`
- `source_code`
- `receive_time`
- `raw_value`

移除字段：

- `project_id`

### `iot_telemetry_latest`

建议新增：

```text
iot_telemetry_latest
- id
- aggregator_id
- ent_id
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
- raw_value
```

不包含项目字段。

## 4.2 业务关系表

### `aggregator_single_model_data`

作为业务项目主表继续使用：

- `aggregator_id`
- `ent_id`
- `energy_station_code`
- `energy_station`
- `resource_type_id`

### `aggregator_ent_device`

作为项目到设备关系表继续使用：

- `aggregator_id`
- `ent_id`
- `energy_station_code`
- `device_base_id`
- `device_id`
- `iot_device_base_id`

其中：

- `energy_station_code`
  代表业务项目

- `device_id`
  对应 `iot_device.device_code`
  是业务查询主桥接键

- `iot_device_base_id`
  对应 `iot_device.id`
  是内部兼容桥接键

## 5. 查询范围解析方案

## 5.1 总原则

业务永远不直接查 `iot_telemetry_*` 表上的项目字段。

统一流程：

```text
业务范围
-> 业务关系解析
-> IoT 设备集合
-> IoT 时序查询
-> 聚合结果
```

## 5.2 聚合商级

输入：

- `aggregatorId`

解析：

1. 从 `aggregator_ent_device` 查 `aggregator_id = ?`
2. 优先过滤 `device_id` 非空
3. 用 `device_id` 匹配 `iot_device.device_code`
4. 对缺失 `device_id` 的存量数据，可兼容回退到 `iot_device_base_id -> iot_device.id`
4. 得到 IoT `deviceIds`

结果：

- 聚合商下所有已绑定 IoT 设备

## 5.3 企业级

输入：

- `aggregatorId`
- `entId`

解析：

1. 从 `aggregator_ent_device` 查 `aggregator_id = ? and ent_id = ?`
2. 优先过滤 `device_id` 非空
3. 映射到 `iot_device.device_code`
4. 存量兼容时回退到 `iot_device_base_id -> iot_device.id`
4. 得到 IoT `deviceIds`

## 5.4 项目级

输入：

- `aggregatorId`
- `entId`
- `businessProjectId`

其中：

```text
businessProjectId = aggregator_single_model_data.energy_station_code
```

解析：

1. 先校验该项目存在于 `aggregator_single_model_data`
2. 从 `aggregator_ent_device` 查：
   `aggregator_id = ? and ent_id = ? and energy_station_code = ?`
3. 优先取出 `device_id`
4. 映射到 `iot_device.device_code`
5. 缺失时兼容使用 `iot_device_base_id -> iot_device.id`
5. 得到 IoT `deviceIds`

## 5.5 设备级

设备级分两类：

1. 业务设备查询
   输入 `deviceBaseId`
   先查 `aggregator_ent_device`
   再优先用 `device_id` 找 `iot_device.device_code`
   缺失时再回退到 `iot_device_base_id`

2. IoT 设备查询
   输入 `iotDeviceCode` 或 `iotDeviceId`
   直接查 IoT 表

## 6. 查询服务分层

## 6.1 对外统一入口

建议统一为：

```java
IotDataQueryResp query(IotDataQueryReq req)
```

其中范围对象改为：

```java
public class IotBusinessScope {
    private String scopeType;
    private String aggregatorId;
    private String entId;
    private String businessProjectId;
    private List<String> businessDeviceIds;
    private List<String> iotDeviceCodes;
    private List<Long> iotDeviceIds;
}
```

重点：

- `businessProjectId` 是业务项目输入
- 不是 IoT 表字段
- 最终统一解析成 `deviceIds`

## 6.2 新增范围解析器

建议新增：

```text
IotBusinessScopeResolver
```

职责：

- 读取 `aggregator_single_model_data`
- 读取 `aggregator_ent_device`
- 解析业务项目、企业、聚合商范围
- 输出 `ResolvedIotScope`

建议输出：

```java
public class ResolvedIotScope {
    private String aggregatorId;
    private String entId;
    private List<Long> deviceIds;
    private List<String> deviceCodes;
}
```

## 6.3 IoT 查询仓储

建议保留三类查询仓储：

- `IotRealtimeQueryRepository`
- `IotHistoryQueryRepository`
- `IotRawTraceRepository`

它们只接收：

- `aggregatorId`
- `entId`
- `deviceIds`
- `pointCodes`
- `timeRange`

不接收业务项目字段。

## 7. 接入链路方案

## 7.1 匹配原则

接入匹配只按以下链路处理：

```text
access_key/user_key -> accessApp -> ent_id
external_device_id  -> iot_device.third_party_code
external_metric     -> iot_device_point.third_party_code
```

匹配成功后写入：

- `aggregator_id`
- `ent_id`
- `device_id`
- `device_code`
- `point_code`

不写项目字段。

## 7.2 业务项目如何获得

如果业务后续需要知道这条时序属于哪个项目，不从 IoT 表取。

正确做法：

1. 根据 `device_code` 或 `device_id`
2. 回查 `aggregator_ent_device`
3. 再得到 `energy_station_code`

也就是：

```text
时序 -> 设备 -> 业务关系 -> 项目
```

而不是：

```text
时序表直接带项目
```

## 8. API 调整方案

## 8.1 IoT 管理接口

以下 IoT 接口移除 `projectId`：

- 设备新增
- 设备修改
- 设备列表查询
- 分钟数据查询

涉及对象：

- `IotDeviceSaveReq`
- `IotDeviceQuery`
- `IotTelemetryMinuteQuery`
- `IotDeviceController`

## 8.2 业务查询接口

项目维度查询不再走 IoT 管理接口。

应单独提供业务查询接口，例如：

```text
/load-aggregation/iot-query/realtime
/load-aggregation/iot-query/history
```

入参使用：

- `aggregatorId`
- `entId`
- `businessProjectId`
- `metricCode`
- `timeRange`

接口内部先做业务关系解析，再查询 IoT。

## 9. 迁移步骤

## 9.1 第一阶段：查询先解耦

目标：

- 先让业务查询不再依赖 IoT 表里的项目字段

动作：

1. 新增 `IotBusinessScopeResolver`
2. 项目级查询改为：
   `businessProjectId -> aggregator_ent_device -> iot_device`
3. 业务查询不再使用 `iot_device.project_id`

## 9.2 第二阶段：停止写入项目字段

目标：

- IoT 接入与 IoT 设备管理不再维护项目关系

动作：

1. 接入链路不再写 `project_id`
2. 删除 `external_project_id`
3. 设备管理不再保存 `projectId`

## 9.3 第三阶段：删除 IoT 模型项目字段

目标：

- 从 schema 和代码中彻底删掉项目字段

动作：

1. 删除 `iot_device.project_id`
2. 删除 `iot_access_app.project_id`
3. 删除 `iot_telemetry_raw.project_id`
4. 删除 `iot_telemetry_minute.project_id`
5. 删除所有 DTO、VO、Controller 中的 `projectId`

## 10. 关键风险

## 10.1 桥接键不统一

当前最大风险不是查询逻辑，而是关系桥接键是否被统一使用。

必须确认：

```text
aggregator_ent_device.device_id
```

最终统一对应：

```text
iot_device.device_code
```

同时保留兼容：

```text
aggregator_ent_device.iot_device_base_id -> iot_device.id
```

如果主桥接键和兼容桥接键混用失控，后续所有项目级查询都会出现多义性。

## 10.2 未绑定 IoT 设备的业务设备

如果业务设备还没绑定 IoT 设备，那么：

- 业务项目里能看到设备
- 但 IoT 查询无法查到时序

这类设备应在关系解析层明确过滤，并返回“未绑定 IoT 设备”的统计或告警。

## 10.3 前端仍按项目字段查询 IoT

当前前后端还有 `projectId` 查询入口。

这类入口不能继续当真，只能：

- 先兼容
- 再迁移
- 最终删除

## 11. 最终建议

最终方案建议一句话概括：

```text
IoT 域只存设备和数据，业务域存项目和关系，查询时先解业务关系再查 IoT 数据。
```

具体落地顺序：

1. 统一业务项目主键为 `energy_station_code`
2. 统一业务设备到 IoT 设备主桥接键为 `device_id -> iot_device.device_code`
3. 将 `iot_device_base_id -> iot_device.id` 降级为兼容桥
4. 新增 `IotBusinessScopeResolver`
5. 业务查询全部改成“先解析设备集合，再查 IoT 时序”
6. 删除 IoT schema 中所有项目字段

按这个方向做完之后，IoT 数据层的边界会比较干净，也不会再把业务关系反向塞回物联模型里。
