# 设备资产与负荷聚合数据接入技术方案

## 目标

本方案面向 e-hub 当前负荷聚合业务，建设一个边界清晰、可长期演进的设备与测点数据底座。

本期目标：

- 建立企业/项目下的设备资产、标准设备编码、标准测点模型。
- 支持手动录入设备、批量导入设备、三方设备/测点标识绑定。
- 兼容数能物联 HTTP 云对云接入协议，完成三方测点数据标准化入库。
- 为负荷聚合提供稳定的设备与分钟测点数据，不把业务规则放进设备管理。
- 建立负荷聚合业务关系快照，支撑上送与结算追溯。

不在本期做：

- 通用指标/公式计算引擎。
- 将总加、单体量测预计算为业务结果表。
- 在设备管理中维护是否参与负荷聚合、是否参与模型上送、结算口径等业务规则。

## 设计原则

### 设备资产和业务规则解耦

设备管理只关心设备、测点、三方绑定和原始/标准化测点数据。

负荷聚合业务负责模型、单体、市场参与范围、上送口径、结算追溯。

```text
三方物联平台
   -> IoT 接入与标准化
      -> 标准设备、标准测点、分钟测点数据
         -> 负荷聚合业务快照
            -> upstream 华北电网上送
```

依赖方向：

```text
e-hub-console  -> e-hub-service
e-hub-upstream -> e-hub-service

upstream 读取负荷聚合快照和 IoT 标准测点数据。
IoT 设备模块不依赖负荷聚合业务。
```

### 标准编码和三方标识分离

系统内部使用我方标准设备编码 `device_code` 和标准测点编码 `point_code`。

三方平台的 `projectId`、`deviceId`、`metric` 只作为外部绑定关系，不直接作为系统主键或业务口径。参考协议里的 `aliasCode` 不作为本项目的核心映射字段，企业上下文以 `ent_id` 为准。

### 实时链路不依赖离线指标任务

当前华北电网上送要求时效性强，总加每分钟上送，单体量测每 15 分钟上送。上送前的数据源应直接来自标准化分钟测点数据和当前有效业务快照，不依赖调度指标计算结果。

指标/公式能力后续可用于离线分析、报表或非实时业务，不作为本期实时上送主链路。

## 领域边界

### IoT 设备域

负责：

- 企业/项目下设备资产管理。
- 标准设备编码管理。
- 设备测点管理。
- 三方项目、设备、测点映射。
- 三方 HTTP 数据接入。
- 分钟测点数据标准化入库。
- 未匹配数据处理。
- 设备在线状态、最新值查询等展示能力。

不负责：

- 是否参与负荷聚合。
- 是否参与模型上送。
- 是否参与总加或单体量测。
- 电网编码、市场资源类型、响应能力。
- 结算周期内设备范围。

### 负荷聚合域

负责：

- 聚合商、企业/项目、模型资源关系。
- 单体量测分组。
- 参与市场设备范围。
- 模型快照和设备快照。
- 总加、单体量测上送口径。
- 上送与结算追溯。

### upstream 上送域

负责：

- 华北电网协议适配。
- 当前分钟或当前周期数据组装。
- 上送请求、响应、失败日志。
- 允许范围内的重试。

upstream 不维护设备资产，不维护三方设备绑定。

## 设备编码规则

设备表需要两个层次的标识：

- `id`：数据库主键，可自增或雪花 ID，只用于内部关联。
- `device_code`：我方标准设备编码，企业内唯一，用于展示、接口查询、业务快照和数据追溯。

唯一约束：

```text
ent_id + device_code 唯一
```

默认编码规则：

```text
设备类型编码 + 企业内同类型流水号
```

示例：

```text
METE001  电表 1
METE002  电表 2
PUMP001  水泵 1
HEAT001  热泵 1
```

说明：

- 不建议使用纯自增数字作为 `device_code`，因为业务人员难以识别，也不利于跨系统排查。
- 不建议使用 `METE_METE01` 这种重复编码，除非前缀代表项目、区域或设备组。
- 页面支持手动录入 `device_code`，为空时系统自动生成。
- 手动录入时必须校验企业内唯一。
- 三方 `deviceId` 不能直接当作 `device_code`，应进入三方绑定表。

## 数据模型

### 项目/企业映射

企业和项目信息可复用现有客户、企业、能源站数据。如果设备管理需要独立展示项目树，可增加轻量项目表。

```text
iot_project
- id
- aggregator_id        聚合商 ID
- ent_id               企业 ID
- project_code         我方项目编码，企业内唯一
- project_name         项目/能源站名称
- parent_id            上级节点，可为空
- status               1 启用，0 停用
- deleted              0 未删除，1 删除
- create_time
- update_time
```

唯一约束：

```text
ent_id + project_code
```

如果现有企业/能源站表已经能满足树形展示，`iot_project` 可以不单独落表，只保留三方项目映射表。

### 设备主表

```text
iot_device
- id
- aggregator_id        聚合商 ID
- ent_id               企业 ID
- project_id           项目/能源站 ID，可为空
- device_code          我方标准设备编码，企业内唯一
- device_name          设备名称
- device_type_code     设备类型编码，如 METE/PUMP/HEAT
- device_type_name     设备类型名称
- manufacturer         厂商
- model                型号
- asset_status         资产状态：1 启用，0 停用
- online_status        在线状态：1 在线，0 离线
- last_data_time       最近一次数据时间
- remark
- deleted
- create_time
- update_time
```

唯一约束：

```text
ent_id + device_code
```

注意：

- 不在设备主表放 `rated_power`、`power_cap`、`response_power`。
- 单体容量、响应能力属于负荷聚合业务模型。
- 如果确有设备铭牌参数，放到 `iot_device_param`，仅作为资产参数，不作为负荷聚合计算口径。

### 设备参数表

```text
iot_device_param
- id
- device_id
- param_code           参数编码，如 nameplate_power
- param_name           参数名称
- param_value
- unit
- sort
- remark
- create_time
- update_time
```

### 设备测点表

```text
iot_device_point
- id
- device_id
- point_code           我方标准测点编码，如 active_power
- point_name           测点名称，如有功功率
- value_type           double/int/string
- unit                 单位，如 kW
- data_frequency       采集频率，秒
- required_flag        是否核心测点
- read_write_role      readOnly/readWrite
- status               1 启用，0 停用
- sort
- remark
- create_time
- update_time
```

唯一约束：

```text
device_id + point_code
```

负荷聚合当前核心测点建议统一为：

```text
active_power  有功功率，单位 kW
```

后续可扩展电压、电流、累计电量、运行状态等测点。

### 三方接入应用表

```text
iot_access_app
- id
- source_code          三方来源编码
- source_name
- aggregator_id        聚合商 ID
- ent_id               企业 ID，接入企业唯一口径
- project_id           默认项目/能源站 ID，可为空
- access_key           对应请求头 X-GW-AccessKey
- user_key             对应 body.userKey
- enabled
- remark
- create_time
- update_time
```

唯一约束：

```text
source_code
access_key + user_key
access_key + ent_id
```

### 三方项目映射表（可选）

企业识别不依赖 `projectId`，而是由 `iot_access_app.ent_id` 或受控的 `entId` 参数确定。

如果同一企业下需要继续区分项目/能源站，可使用项目映射表；如果当前业务只到企业级，项目映射表可以不使用。

```text
iot_project_external_ref
- id
- source_code
- ent_id
- project_id           我方项目 ID，可为空
- external_project_id  协议 originData.projectId
- external_project_name
- status
- create_time
- update_time
```

唯一约束建议：

```text
source_code + ent_id + external_project_id
```

### 三方设备映射表

```text
iot_device_external_ref
- id
- source_code
- ent_id
- project_id
- device_id            我方设备 ID
- external_device_id   协议 deviceId
- external_device_code 可选
- external_device_name
- gateway_code         可选
- status
- create_time
- update_time
```

唯一约束：

```text
source_code + ent_id + external_device_id
```

### 三方测点映射表

```text
iot_point_external_ref
- id
- source_code
- device_id
- point_id             我方测点 ID
- external_metric      协议 metric
- external_metric_name
- ratio                倍率，默认 1
- offset               偏移，默认 0
- status
- create_time
- update_time
```

唯一约束：

```text
source_code + device_id + external_metric
```

标准化值计算：

```text
standard_value = raw_value * ratio + offset
```

### 分钟测点数据表

```text
iot_telemetry_minute
- id
- aggregator_id
- ent_id
- project_id
- device_id
- device_code
- point_code
- data_time            原始数据时间，保留秒
- minute_time          归整到分钟，如 2026-06-22 10:31:00
- point_value
- unit
- quality              normal/missing/invalid/unmatched
- source_code
- receive_time
- raw_value
```

唯一约束：

```text
device_id + point_code + minute_time
```

核心索引：

```text
point_code + minute_time + device_id
ent_id + point_code + minute_time
device_id + point_code + minute_time
```

说明：

- 三方协议中的 `dataTime` 带秒，负荷聚合按分钟取数，因此同时保留 `data_time` 和 `minute_time`。
- 同一设备同一测点同一分钟重复上送时，按幂等更新处理，保留最新接收时间。
- 设备模块可以存储迟到数据，但 upstream 当前分钟上送不能依赖迟到补偿作为正常路径。

### 未匹配数据日志

```text
iot_unmatched_telemetry_log
- id
- source_code
- interface_type       originData/cimData
- external_project_id
- external_device_id
- external_device_name
- external_metric
- external_metric_name
- data_time
- value
- reason               PROJECT_NOT_MATCHED/DEVICE_NOT_MATCHED/POINT_NOT_MATCHED/VALUE_INVALID
- raw_payload
- handled              0 未处理，1 已处理
- create_time
- update_time
```

找不到项目、设备或测点映射时，不自动生成正式设备，先进入未匹配日志，由页面人工处理。

### 最新值表

`iot_device_latest_value` 是可选性能表，不作为业务事实来源。

如果需要设备详情页快速展示最新值，可增加：

```text
iot_device_latest_value
- id
- device_id
- point_code
- point_value
- data_time
- receive_time
```

约束：

- 只能用于展示和快速查询。
- 负荷聚合上送必须校验 `data_time/minute_time`，不能直接拿最新值冒充当前分钟值。
- 本期如果页面性能压力不大，可以先不建。

## 三方 HTTP 接入设计

参考文档：

```text
数能物联标准-HTTP协议-云对云数据接入规范 V2.1
```

### 兼容接口

对外兼容协议路径：

```text
POST /data-collector/thirdPart/data/receive/originData
POST /data-collector/thirdPart/data/receive/cimData
```

### 鉴权

请求头：

```text
X-GW-AccessKey: 平台分配的 appKey
```

非 CIM 请求体：

```json
{
  "userKey": "SqHGQJw5RBaB0RcklhexenWgvsgmbmJAl",
  "dataList": []
}
```

处理规则：

```text
X-GW-AccessKey + userKey -> iot_access_app
```

校验失败返回：

```json
{
  "code": 401,
  "msg": "认证失败",
  "data": {
    "success": 0,
    "fail": 0,
    "failList": []
  }
}
```

CIM 请求体原协议未包含 `userKey`，本系统仍以 `X-GW-AccessKey` 识别来源，并通过 `iot_access_app.ent_id` 确定企业。若一个 accessKey 服务多个企业，则必须增加受控的 `entId` 参数并校验授权范围。

### originData 请求

参考协议示例：

```json
{
  "dataList": [
    {
      "dataTime": "2024-7-4 10:19:46",
      "deviceId": "220VP01",
      "deviceName": "云对云测试",
      "metric": "lac01",
      "metricName": "交流电流",
      "projectId": "chongqinglongran",
      "projectName": "重庆龙冉能源科技有限公司",
      "value": "1"
    }
  ],
  "userKey": "xxx"
}
```

字段映射：

```text
ent_id     -> 来自 iot_access_app.ent_id 或受控 entId
projectId  -> 可选项目映射，不作为企业唯一口径
deviceId   -> iot_device_external_ref.external_device_id
metric     -> iot_point_external_ref.external_metric
dataTime   -> iot_telemetry_minute.data_time/minute_time
value      -> iot_telemetry_minute.point_value
```

### cimData 请求

本项目请求示例：

```json
{
  "entId": "10001",
  "data": [
    {
      "dataTime": "2024-7-11 13:55:49",
      "deviceId": "ECR20",
      "deviceType": "ECR",
      "metric": "STUnitSwit",
      "value": 2
    }
  ]
}
```

字段映射：

```text
entId             -> 企业唯一标识；如果 accessKey 已绑定企业，可不传
deviceId           -> iot_device_external_ref.external_device_id
metric             -> iot_point_external_ref.external_metric
dataTime           -> iot_telemetry_minute.data_time/minute_time
value              -> iot_telemetry_minute.point_value
```

说明：

- 参考协议中的 `aliasCode` 本项目不需要。
- 如果三方仍按参考协议传 `aliasCode`，接入层可以兼容接收，但只作为原始字段记录，不作为企业或项目映射条件。

### 标准化处理流程

```text
1. 校验 X-GW-AccessKey 和 userKey。
2. 识别 source_code。
3. 解析 dataList/data。
4. 根据 iot_access_app.ent_id 或受控 entId 确定企业；projectId 仅用于可选项目映射。
5. 根据 deviceId 映射我方 device_id/device_code。
6. 根据 metric 映射我方 point_code。
7. 校验 value 是否为合法数值。
8. 根据 ratio/offset 换算标准值。
9. dataTime 归整 minute_time。
10. upsert iot_telemetry_minute。
11. 更新设备 online_status 和 last_data_time。
12. 返回 success/fail/failList。
```

### 响应结构

与协议保持一致：

```json
{
  "code": 200,
  "msg": "此次共接收 1 条，入库成功 1 条，入库失败 0 条。",
  "data": {
    "success": 1,
    "fail": 0,
    "failList": []
  }
}
```

失败详情保留原始定位字段：

```json
{
  "projectId": "chongqinglongran",
  "projectName": "重庆龙冉",
  "deviceId": "73",
  "deviceName": "变压器",
  "metric": "ua",
  "metricName": "电压",
  "reason": "POINT_NOT_MATCHED"
}
```

### 幂等策略

协议没有强制 `messageId`，因此以数据自然键幂等：

```text
source_code + device_id + point_code + minute_time
```

同一分钟重复上送：

- 后到数据覆盖前值。
- 更新 `receive_time`。
- 保留接入日志可选。

如果后续三方能提供消息 ID，可增加：

```text
iot_ingest_message_log
- source_code
- message_id
- request_hash
- receive_time
```

### 缓存策略

项目、设备、测点映射可以使用短缓存提升接入性能，但不能使用一小时级别的不可控缓存。

建议：

- 映射缓存 TTL 1-5 分钟。
- 设备/测点绑定保存后主动清理对应缓存。
- 未匹配数据处理完成后主动清理缓存。

## 负荷聚合快照设计

### 为什么需要快照

设备资产会变化，负荷聚合业务口径也会变化。

例如供暖季前几个月 50 个设备参与市场，后几个月 100 个设备参与市场。结算时必须能准确知道某个时间段真实参与上送的是哪批设备。

因此参与关系不能只查当前设备表，而要有业务快照。

### 模型快照表

```text
la_model_snapshot
- id
- aggregator_id
- ent_id
- snapshot_code
- snapshot_name
- effective_start
- effective_end
- status               draft/effective/expired
- created_by
- create_time
- update_time
```

### 快照设备表

```text
la_model_snapshot_device
- id
- snapshot_id
- ent_id
- project_id
- device_id
- device_code
- point_code           当前主测点，如 active_power
- create_time
```

快照保存时冗余 `device_code` 和 `point_code`，用于历史追溯。设备名称也可按需要冗余。

### 单体量测分组

```text
la_single_meas_group
- id
- snapshot_id
- group_code
- group_name
- resource_type_id
- power_cap            单体/资源容量，业务属性
- response_power       响应能力，业务属性
- controllable         是否可控，业务属性
- grid_area
- account_no
- status
- create_time
- update_time
```

```text
la_single_meas_group_device
- id
- group_id
- device_id
- device_code
- point_code
- create_time
```

说明：

- `power_cap` 是单体/资源级业务属性，不是单个设备资产属性。
- `resource_type_id`、`grid_area`、`account_no` 属于负荷聚合业务或电网模型，不进入设备主表。
- `model_flag` 应由快照设备范围替代，不继续作为设备属性扩展。

### 上送取数方式

总加每分钟上送：

```text
1. 获取当前有效 la_model_snapshot。
2. 获取 snapshot 下参与总加的设备和 point_code。
3. 查询 iot_telemetry_minute 当前 minute_time 的设备测点值。
4. 缺点按业务规则记录失败或异常，不用旧值补当前分钟。
5. 组装 upstream 上送数据。
```

单体量测每 15 分钟上送：

```text
1. 获取当前有效 snapshot。
2. 获取单体量测分组。
3. 按 group_id 查询设备测点。
4. 查询同一标准分钟数据。
5. 组装单体量测上送数据。
```

本期不新增：

```text
la_total_power_minute
la_single_meas_minute
```

原因：

- 当前总加和单体量测的数据源一致，都是快照设备关系 + 标准分钟测点数据。
- 实时上送场景下，预计算调度会增加延迟和链路复杂度。
- 如需审计，以 upstream 上送日志保存请求体、响应体、取数时间和设备范围即可。

## 旧表治理

### aggregator_ent_device

旧表混合了设备资产、企业关系、业务参与、电网字段。

字段归属建议：

```text
设备资产：
- device_name
- device_type
- device_id
- device_base_id
- account_no
- status
- equip_manufactor
- storage_type
- controllable 可作为资产参数或业务属性，按实际含义拆分

企业/项目：
- aggregator_id
- ent_id
- station_id
- energy_station
- energy_station_code
- area_code

三方绑定：
- data_source
- iot_device_base_id

负荷聚合业务：
- model_flag
- resource_type_id
- state_grid_code
- is_public
- is_direct
- response_power
- max_power
- user_type
```

处理策略：

- 短期保留 `aggregator_ent_device`，用于兼容现有代码。
- 不继续向该表增加字段。
- 新设备能力写入 `iot_device`、`iot_device_point` 和三方映射表。
- 负荷聚合参与关系逐步迁移到快照表。

### aggregator_single_model_data

该表是当前华北电网模型上送所需的数据投影，不应作为设备资产表。

字段如 `power_cap`、`resource_type_id`、`grid_area`、`account_no`、`controll` 都属于负荷聚合模型或电网模型侧。

后续可以逐步由 `la_single_meas_group` 或模型快照生成。

## 迁移方案

### 第一步：建新表

新增 IoT 设备域表：

```text
iot_project
iot_device
iot_device_param
iot_device_point
iot_access_app
iot_project_external_ref
iot_device_external_ref
iot_point_external_ref
iot_telemetry_minute
iot_unmatched_telemetry_log
```

新增负荷聚合快照表：

```text
la_model_snapshot
la_model_snapshot_device
la_single_meas_group
la_single_meas_group_device
```

### 第二步：初始化设备资产

从 `aggregator_ent_device` 迁移：

```text
aggregator_id -> iot_device.aggregator_id
ent_id        -> iot_device.ent_id
energy_station_code/station_id -> project_code
energy_station -> project_name
device_name   -> device_name
device_type   -> device_type_code
device_id     -> device_code 候选值
```

如果旧 `device_id` 在企业内唯一，可作为新 `device_code`。

如果不唯一或不符合标准，则按新规则生成 `device_code`，旧 `device_id` 写入 `iot_device_external_ref.external_device_id/external_device_code`。

### 第三步：初始化测点

当前负荷聚合只用设备上送功率，可为参与设备初始化：

```text
point_code = active_power
point_name = 有功功率
value_type = double
unit = kW
data_frequency = 60
```

其他测点后续通过页面或导入维护。

### 第四步：初始化三方映射

根据旧字段和接入来源建立映射：

```text
data_source        -> source_code
iot_device_base_id -> external_device_id 候选
device_id          -> external_device_code 候选
```

测点映射需要根据三方协议中的 `metric` 配置，不能默认等同于我方 `point_code`。

### 第五步：建立业务快照

以当前有效业务关系生成初始快照：

```text
model_flag = 1 的设备 -> la_model_snapshot_device
单体量测分组         -> la_single_meas_group_device
```

生成后，后续业务参与范围以快照为准，不再扩展 `model_flag` 作为主口径。

### 第六步：灰度切换

- 接入侧先写新 `iot_telemetry_minute`。
- console 页面先读新设备表。
- upstream 逐步改为读取快照 + 标准分钟测点。
- 老表只保留兼容查询，确认无依赖后再下线。

## 功能页面

设备管理页面建议放在负荷聚合产品下的“设备/资源数据”中，但内部实现仍按 IoT 设备域解耦。

### 设备树

```text
客户/聚合商
  -> 企业
     -> 项目/能源站
        -> 设备
```

节点能力：

- 企业/项目：查看下属设备、测点概览、数据接入情况。
- 设备：查看基础信息、测点、三方绑定、分钟数据。

### 设备列表

字段：

```text
企业
项目
设备编码
设备名称
设备类型
在线状态
最近数据时间
三方来源
绑定状态
```

操作：

- 新增设备。
- 编辑设备。
- 批量导入。
- 配置测点。
- 配置三方绑定。
- 查看分钟数据。

### 未匹配数据

展示三方上送但未能标准化的数据。

处理方式：

- 绑定到已有项目/设备/测点。
- 创建新设备后绑定。
- 标记忽略。

处理完成后清理映射缓存，后续数据正常入库。

### 负荷聚合快照页面

该页面属于负荷聚合业务，不属于设备管理。

能力：

- 创建模型快照。
- 选择参与设备。
- 维护单体量测分组。
- 设置快照生效时间。
- 查看某日/某时刻快照设备范围。

## 接口规划

### 设备管理接口

```text
GET    /iot/devices
GET    /iot/devices/{id}
POST   /iot/devices
PUT    /iot/devices/{id}
DELETE /iot/devices/{id}

GET    /iot/devices/{id}/points
POST   /iot/devices/{id}/points
PUT    /iot/points/{id}
DELETE /iot/points/{id}

GET    /iot/devices/{id}/external-refs
POST   /iot/devices/{id}/external-refs
PUT    /iot/device-external-refs/{id}
DELETE /iot/device-external-refs/{id}

GET    /iot/points/{id}/external-refs
POST   /iot/points/{id}/external-refs
PUT    /iot/point-external-refs/{id}
DELETE /iot/point-external-refs/{id}

GET    /iot/telemetry/minute
GET    /iot/unmatched-telemetry
```

### 三方接入接口

```text
POST /data-collector/thirdPart/data/receive/originData
POST /data-collector/thirdPart/data/receive/cimData
```

### 负荷聚合快照接口

```text
GET    /console/load-aggregation/snapshots
POST   /console/load-aggregation/snapshots
GET    /console/load-aggregation/snapshots/{id}
POST   /console/load-aggregation/snapshots/{id}/devices
POST   /console/load-aggregation/snapshots/{id}/effective

GET    /console/load-aggregation/single-meas-groups
POST   /console/load-aggregation/single-meas-groups
POST   /console/load-aggregation/single-meas-groups/{id}/devices
```

## 工程落位

建议包结构：

```text
e-hub-service
  cn.sl.ehub.service.iot
    entity
    mapper
    service
    dto
  cn.sl.ehub.service.loadaggregation
    snapshot

e-hub-console
  cn.sl.ehub.console.controller.iot
  cn.sl.ehub.console.controller.loadaggregation

e-hub-upstream
  读取 e-hub-service 中负荷聚合快照和 iot_telemetry_minute
```

接入接口可以放在 `e-hub-console` 或独立接入模块。当前项目没有独立 IoT 接入模块时，先落在 `e-hub-console`，后续流量变大再拆。

## 数据质量和异常规则

数据接入校验：

- `dataTime` 必填且可解析。
- `deviceId` 必填。
- `metric` 必填。
- `value` 必须能转成数值。
- 项目、设备、测点映射必须存在。

异常处理：

- 映射不存在：写 `iot_unmatched_telemetry_log`。
- 数值非法：写失败详情，不入正式分钟表。
- 重复数据：按唯一键幂等更新。
- 迟到数据：允许入库，但 upstream 当前分钟上送不以迟到补偿为正常路径。

质量标识：

```text
normal    正常
missing   缺点，由业务取数时识别
invalid   数值非法
unmatched 未匹配，未进入正式分钟表
```

## 安全策略

本期按协议实现：

- `X-GW-AccessKey`
- `userKey`
- 来源启停控制
- 接入失败日志

可扩展：

- IP 白名单。
- 请求频控。
- 签名校验。
- 原始请求日志抽样存储。

## 性能建议

实时接入：

- 批量解析、批量查询映射、批量 upsert。
- 映射短缓存，变更主动失效。
- `iot_telemetry_minute` 按月或按时间分区可作为后续优化。

upstream 取数：

- 先取当前有效快照设备列表。
- 按 `point_code + minute_time + device_id in (...)` 批量查询。
- 不逐设备循环查库。

核心索引必须覆盖：

```text
iot_telemetry_minute(point_code, minute_time, device_id)
la_model_snapshot(aggregator_id, ent_id, effective_start, effective_end, status)
la_model_snapshot_device(snapshot_id, device_id)
la_single_meas_group_device(group_id, device_id)
```

## 分期实施

### 一期

- 建 IoT 设备、测点、三方映射、分钟数据表。
- 实现设备手动录入、批量导入、测点配置。
- 实现 originData/cimData 接入。
- 实现未匹配数据查看和绑定。
- 建负荷聚合快照表。
- upstream 改造为读取快照 + 标准分钟测点。

### 二期

- 完善设备类型模板、测点模板。
- 增加设备状态规则、最新值表。
- 增加更完整的数据质量报表。
- 增加导入校验和批量绑定工具。

### 三期

- 引入通用指标/公式能力，用于离线分析和报表。
- 支持更多电网协议和资源模型。
- 拆分独立 IoT 接入服务。

## 关键结论

- `device_code` 是企业内唯一的我方标准设备编码，数据库主键可以自增，但业务编码不能只用自增 ID。
- 三方 `deviceId`、`metric` 必须通过映射表绑定，不能直接污染设备主表。
- `rated_power` 不放设备主表；单体容量、响应能力、是否可控等属于负荷聚合业务模型。
- 设备管理只提供标准设备和标准测点数据，负荷聚合通过业务快照决定哪些设备参与上送。
- 本期不需要 `la_total_power_minute`、`la_single_meas_minute` 作为预计算结果表；实时上送直接基于快照和分钟测点数据组装。
