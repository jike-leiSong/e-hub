# 代理电价服务与数据导入方案

## 背景

e-hub 当前已具备代理电价查询的基础能力：

- 页面兼容接口：`/haomaidian/index/getDefaultMenus`、`/haomaidian/index/getEnAgentPrices`、`/areaDict/getDictByType`
- 项目内部接口：`/tariff/agent-price/options`、`/tariff/agent-price/prices`
- 代理电价主表：`e_agent_price`
- 代理电价 96 点明细表：`e_agent_price_data`
- 尖峰平谷主表：`e_fpgj_type`
- 尖峰平谷 96 点明细表：`e_fpgj_type_data`

现有数据模型可以支撑北京等地区月度代理电价查询，但要对外提供稳定服务，还需要补齐版本发布、数据来源、批次导入、质量校验、对外鉴权、调用审计和无数据处理规则。

本方案目标是将代理电价能力从内部页面查询能力，建设为可对外开放的标准电价数据服务。

## 市场服务形态参考

市场上的电价数据服务通常不只返回一个电价值，而是围绕以下要素提供标准化能力：

- 按地区、用户类型、电压等级、收费类型、日期或月份查询电价。
- 支持分时电价，返回尖、峰、平、谷、深谷等时段信息。
- 支持 24 点、96 点或更细颗粒度的电价曲线。
- 保留数据来源、发布时间、生效时间、版本号和审核状态。
- 对外通过 API Key、签名、限流、调用日志等方式提供服务。
- 数据导入通常来自官方公告、Excel、PDF、网页表格和第三方数据源，导入后需要人工复核。

国内代理购电数据通常来自电网企业、发改委、交易中心等公开发布渠道。由于各省公告格式不统一，完全自动化导入风险较高，适合采用“人工上传 + 半自动解析 + 规则校验 + 审批发布”的方式逐步建设。

参考资料：

- 国家发展改革委关于进一步深化燃煤发电上网电价市场化改革的通知：https://www.ndrc.gov.cn/xxgk/zcfb/tz/202110/t20211012_1299461.html
- 国家发展改革委关于进一步完善分时电价机制的通知：https://www.ndrc.gov.cn/xxgk/zcfb/tz/202107/t20210729_1292067.html
- OpenEI Utility Rates API：https://openei.org/services/doc/rest/util_rates/

## 服务边界

### 对外提供

- 代理购电电度电价。
- 输配电价、政府性基金及附加、线损折价、系统运行费等拆分价格。
- 代理购电价格、输配及系统运行价格、附加价格等汇总口径。
- 尖、峰、平、谷、深谷时段。
- 96 点明细价格曲线。
- 容量电价、需量电价。
- 省份、二级区域、三级区域三级联动选项。
- 企业用电属性、电压等级、收费类型选项。
- 数据版本、发布时间、来源文件、生效期。

### 不在一期提供

- 市场化交易客户的个性化合同电价。
- 企业实际结算账单复算。
- 现货市场实时电价预测。
- 电费账单生成。
- 非官方来源数据的兜底推测。

## 总体架构

```text
官方公告/Excel/PDF/网页
        |
        v
数据采集与上传
        |
        v
导入暂存表 staging
        |
        v
规则校验、差异比对、人工复核
        |
        v
审批发布
        |
        v
正式电价库
        |
        v
内部查询接口 + 对外 OpenAPI + 缓存 + 调用审计
```

## 对外 API 设计

对外接口建议新增 `/openapi/v1/tariff/agent` 前缀，和现有内部页面接口解耦。

现有 `/haomaidian/index/*` 保持页面兼容；对外客户使用 OpenAPI，不直接依赖页面接口。

### 查询版本列表

```http
GET /openapi/v1/tariff/agent/versions
```

请求参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| provinceCode | 否 | 省份编码 |
| yearMonth | 否 | 查询月份，格式 `yyyy-MM` |
| status | 否 | 版本状态，默认只返回已发布 |

返回示例：

```json
{
  "code": "0",
  "message": "success",
  "data": [
    {
      "version": "2606",
      "yearMonth": "2026-06",
      "provinceCode": "110000000000",
      "provinceName": "北京市",
      "status": "PUBLISHED",
      "effectiveStart": "2026-06-01",
      "effectiveEnd": "2026-06-30",
      "publishTime": "2026-05-28 10:00:00"
    }
  ]
}
```

### 查询区域菜单

```http
GET /openapi/v1/tariff/agent/areas
```

请求参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| yearMonth | 是 | 查询月份 |
| provinceCode | 否 | 省份编码 |

返回省份、二级区域、三级区域三级联动树。现有页面接口 `/haomaidian/index/getDefaultMenus` 可以复用该能力。

### 查询业务选项

```http
GET /openapi/v1/tariff/agent/options
```

请求参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| yearMonth | 是 | 查询月份 |
| provinceCode | 是 | 省份编码 |
| secondType | 是 | 二级区域 |
| thirdType | 是 | 三级区域 |
| userType | 否 | 企业用电属性 |
| sfType | 否 | 收费类型 |

返回：

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "userTypes": ["工商业用电"],
    "sfTypes": ["两部制"],
    "dyLevels": ["1-10千伏"]
  }
}
```

注意：对外版必须支持按 `yearMonth` 查询选项，不能永远读取最新版本。

### 查询代理电价

```http
POST /openapi/v1/tariff/agent/prices/query
```

请求参数：

```json
{
  "provinceCode": "110000000000",
  "secondType": "全域",
  "thirdType": "不限",
  "yearMonth": "2026-06",
  "selectedDate": "",
  "queryDimension": "month",
  "userType": "工商业用电",
  "dyLevel": "1-10千伏",
  "sfType": "两部制",
  "returnPoints": true
}
```

字段说明：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| provinceCode | 是 | 省份编码 |
| secondType | 是 | 二级区域 |
| thirdType | 是 | 三级区域 |
| yearMonth | 条件必填 | 查询月份，`selectedDate` 为空时必填 |
| selectedDate | 条件必填 | 查询日期，优先级高于 `yearMonth` |
| queryDimension | 否 | `month` 或 `day` |
| userType | 是 | 企业用电属性 |
| dyLevel | 是 | 电压等级 |
| sfType | 是 | 收费类型 |
| returnPoints | 否 | 是否返回 96 点明细 |

返回示例：

```json
{
  "code": "0",
  "message": "success",
  "traceId": "20260703103000001",
  "data": {
    "version": "2606",
    "yearMonth": "2026-06",
    "provinceCode": "110000000000",
    "provinceName": "北京市",
    "secondType": "全域",
    "thirdType": "不限",
    "userType": "工商业用电",
    "dyLevel": "1-10千伏",
    "sfType": "两部制",
    "unit": "元/kWh",
    "capacityElectricityPrice": null,
    "demandElectricityPrice": null,
    "periodSummary": {
      "jian": {
        "periodType": "尖",
        "dlPrice": "0.4161",
        "ddPrice": "0.5100",
        "spPrice": "0.0800",
        "fjPrice": "0.0139",
        "times": ["18:00,20:00"]
      },
      "feng": {
        "periodType": "峰",
        "dlPrice": "0.3161",
        "ddPrice": "0.4100",
        "spPrice": "0.0800",
        "fjPrice": "0.0139",
        "times": ["08:00,11:00"]
      },
      "ping": {
        "periodType": "平",
        "dlPrice": "0.2161",
        "ddPrice": "0.3100",
        "spPrice": "0.0800",
        "fjPrice": "0.0139",
        "times": ["11:00,18:00"]
      },
      "gu": {
        "periodType": "谷",
        "dlPrice": "0.1161",
        "ddPrice": "0.2100",
        "spPrice": "0.0800",
        "fjPrice": "0.0139",
        "times": ["00:00,08:00"]
      },
      "shengu": {
        "periodType": "深谷",
        "dlPrice": "0",
        "ddPrice": "0",
        "spPrice": "0",
        "fjPrice": "0",
        "times": []
      }
    },
    "points96": [
      {
        "time": "00:00",
        "periodType": "谷",
        "ddPrice": "0.2100",
        "spPrice": "0.0800",
        "fjPrice": "0.0139",
        "xsPrice": "0",
        "systemPrice": "0",
        "dlPrice": "0.1161"
      }
    ],
    "source": {
      "sourceType": "OFFICIAL_NOTICE",
      "sourceName": "国网北京市电力公司",
      "sourceUrl": "https://example.com/source.pdf",
      "sourceFileName": "2026年6月代理购电价格表.pdf",
      "importBatchNo": "TARIFF-202606-BJ-001",
      "publishTime": "2026-05-28 10:00:00"
    }
  }
}
```

### 无数据处理规则

无数据时返回明确错误码，不能跨月取旧版本兜底。

例如查询 `2026-07-03`：

- 优先查日版本 `2026-07-03`。
- 没有日版本时查月版本 `2026-07`。
- 如果 `2026-07` 没有发布，返回 `NO_DATA`。
- 不能回退到 `2026-06`。

返回示例：

```json
{
  "code": "TARIFF_NO_DATA",
  "message": "未查询到 2026-07-03 对应的代理电价数据",
  "data": null
}
```

## 版本与单位规则

### 版本规则

当前库中存在 `2606` 这种版本格式，可继续兼容，但对外建议统一暴露 `yearMonth=2026-06`。

内部版本映射规则：

| 输入 | 内部 version |
| --- | --- |
| `2026-06` | `2606` |
| `2026-06-01` | 优先 `2026-06-01`，其次 `2606` |
| `2606` | `2606` |

版本匹配必须遵守“请求月份内匹配”，禁止跨月 fallback。

### 单位规则

当前服务代码中存在 `RATE = 0.001`，说明数据库价格与接口输出价格之间存在单位换算。对外服务必须在文档和返回值中明确：

- 数据库存储单位。
- 接口输出单位。
- 是否经过换算。
- 小数精度和四舍五入规则。

建议对外统一输出 `元/kWh`，保留 4 到 6 位小数，内部按 BigDecimal 计算。

## 数据模型增强

现有正式表继续保留：

- `e_agent_price`
- `e_agent_price_data`
- `e_fpgj_type`
- `e_fpgj_type_data`

建议新增以下表。

### 电价数据来源表

```sql
CREATE TABLE tariff_source_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  province_code VARCHAR(20) NOT NULL COMMENT '省份编码',
  province_name VARCHAR(64) NOT NULL COMMENT '省份名称',
  source_name VARCHAR(128) NOT NULL COMMENT '来源名称',
  source_type VARCHAR(32) NOT NULL COMMENT 'OFFICIAL_NOTICE/EXCHANGE/CUSTOM_UPLOAD',
  source_url VARCHAR(512) DEFAULT NULL COMMENT '来源页面',
  publish_rule VARCHAR(128) DEFAULT NULL COMMENT '发布日期规律',
  enabled TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(512) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 导入批次表

```sql
CREATE TABLE tariff_import_batch (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  batch_no VARCHAR(64) NOT NULL UNIQUE COMMENT '导入批次号',
  year_month VARCHAR(7) NOT NULL COMMENT '电价月份',
  version VARCHAR(24) NOT NULL COMMENT '内部版本',
  province_code VARCHAR(20) NOT NULL,
  province_name VARCHAR(64) NOT NULL,
  source_type VARCHAR(32) NOT NULL,
  source_name VARCHAR(128) DEFAULT NULL,
  source_url VARCHAR(512) DEFAULT NULL,
  source_file_name VARCHAR(255) DEFAULT NULL,
  source_file_path VARCHAR(512) DEFAULT NULL,
  status VARCHAR(32) NOT NULL COMMENT 'UPLOADED/PARSED/VALIDATED/APPROVED/PUBLISHED/FAILED/REPLACED',
  total_rows INT DEFAULT 0,
  valid_rows INT DEFAULT 0,
  error_rows INT DEFAULT 0,
  publish_time DATETIME DEFAULT NULL,
  operator_id VARCHAR(64) DEFAULT NULL,
  operator_name VARCHAR(64) DEFAULT NULL,
  remark VARCHAR(512) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 导入暂存主表

```sql
CREATE TABLE tariff_agent_price_staging (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  batch_no VARCHAR(64) NOT NULL,
  row_no INT NOT NULL,
  version VARCHAR(24) NOT NULL,
  province_code VARCHAR(20) NOT NULL,
  province_name VARCHAR(64) NOT NULL,
  second_type VARCHAR(64) NOT NULL,
  third_type VARCHAR(64) NOT NULL,
  dy_level VARCHAR(64) NOT NULL,
  user_type VARCHAR(64) NOT NULL,
  other_type VARCHAR(64) NOT NULL,
  price_type VARCHAR(32) NOT NULL,
  capacity_electricity_price DECIMAL(20,7) DEFAULT NULL,
  demand_electricity_price DECIMAL(20,7) DEFAULT NULL,
  validate_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  validate_message VARCHAR(1024) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 导入暂存 96 点表

```sql
CREATE TABLE tariff_agent_price_data_staging (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  staging_price_id BIGINT NOT NULL,
  biz_time VARCHAR(20) NOT NULL,
  price DECIMAL(20,7) NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_staging_price_time (staging_price_id, biz_time)
);
```

### 尖峰平谷暂存表

```sql
CREATE TABLE tariff_fpgj_type_staging (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  batch_no VARCHAR(64) NOT NULL,
  row_no INT NOT NULL,
  version VARCHAR(24) NOT NULL,
  province_code VARCHAR(20) NOT NULL,
  province_name VARCHAR(64) NOT NULL,
  second_type VARCHAR(64) NOT NULL,
  validate_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  validate_message VARCHAR(1024) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

```sql
CREATE TABLE tariff_fpgj_type_data_staging (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  staging_fpgj_id BIGINT NOT NULL,
  biz_time VARCHAR(20) NOT NULL,
  fpgj_type VARCHAR(10) NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_staging_fpgj_time (staging_fpgj_id, biz_time)
);
```

### 对外调用记录表

```sql
CREATE TABLE tariff_api_call_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  app_key VARCHAR(64) NOT NULL,
  api_path VARCHAR(128) NOT NULL,
  request_id VARCHAR(64) NOT NULL,
  province_code VARCHAR(20) DEFAULT NULL,
  year_month VARCHAR(7) DEFAULT NULL,
  selected_date VARCHAR(10) DEFAULT NULL,
  response_code VARCHAR(32) DEFAULT NULL,
  cost_ms INT DEFAULT NULL,
  client_ip VARCHAR(64) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

## 导入模板设计

### Sheet: agent_price

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| year_month | 是 | `yyyy-MM` |
| version | 否 | 可自动生成，如 `2606` |
| province_code | 是 | 省份编码 |
| province_name | 是 | 省份名称 |
| second_type | 是 | 二级区域，无则填 `全域` 或 `不限` |
| third_type | 是 | 三级区域，无则填 `不限` |
| user_type | 是 | 企业用电属性 |
| dy_level | 是 | 电压等级 |
| sf_type | 是 | 收费类型 |
| price_type | 是 | 电度/输配/附加/线损/系统运行 |
| capacity_electricity_price | 否 | 容量电价 |
| demand_electricity_price | 否 | 需量电价 |
| 00:00 | 是 | 96 点价格 |
| 00:15 | 是 | 96 点价格 |
| ... | 是 | ... |
| 23:45 | 是 | 96 点价格 |

### Sheet: fpgj_type

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| year_month | 是 | `yyyy-MM` |
| version | 否 | 可自动生成 |
| province_code | 是 | 省份编码 |
| province_name | 是 | 省份名称 |
| second_type | 是 | 二级区域 |
| 00:00 | 是 | 尖/峰/平/谷/深谷 |
| 00:15 | 是 | 尖/峰/平/谷/深谷 |
| ... | 是 | ... |
| 23:45 | 是 | 尖/峰/平/谷/深谷 |

## 数据导入流程

### 1. 来源登记

按省份维护来源配置：

- 省份编码和名称。
- 官方公告地址。
- 发布主体。
- 发布时间规律。
- 文件类型。
- 是否启用自动采集。

一期可以人工维护，后续再逐步接入爬虫。

### 2. 文件上传

运营人员上传官方 Excel、PDF 或整理后的标准模板，系统生成 `batch_no`。

批次初始状态为 `UPLOADED`。

### 3. 解析入暂存表

解析文件后写入 staging 表，不直接写正式表。

批次状态更新为 `PARSED`。

### 4. 规则校验

核心校验：

- `year_month`、`province_code`、`second_type`、`third_type`、`user_type`、`dy_level`、`sf_type` 不能为空。
- 每个价格类型必须有 96 个点。
- `biz_time` 必须完整覆盖 `00:00` 到 `23:45`。
- 同一组合下不能重复：
  `version + province_code + second_type + third_type + dy_level + user_type + sf_type + price_type`
- 尖峰平谷类型只能为：`尖`、`峰`、`平`、`谷`、`深谷`。
- 价格必须为数字，不能为负数。
- 同一省份同一月份已发布数据存在时，必须走替换发布流程。
- 和上月价格差异超过阈值时标记为需要人工确认。
- 正式表主从数据数量一致，不能出现主表有数据但 96 点缺失。

校验通过后批次状态为 `VALIDATED`。

### 5. 人工复核

复核页面展示：

- 文件来源。
- 解析结果。
- 校验错误。
- 与上月差异。
- 价格曲线预览。
- 尖峰平谷时段图。

复核通过后批次状态为 `APPROVED`。

### 6. 发布

发布时使用事务：

1. 如同版本已有正式数据，旧数据标记为 `del_flag=1` 或批次状态改为 `REPLACED`。
2. 将 staging 主表写入 `e_agent_price`。
3. 将 staging 明细写入 `e_agent_price_data`。
4. 将 staging 尖峰平谷写入 `e_fpgj_type` 和 `e_fpgj_type_data`。
5. 批次状态更新为 `PUBLISHED`。
6. 刷新缓存。

发布失败必须回滚，不能出现主表已写入但 96 点明细缺失。

## 缓存设计

代理电价数据发布后变化频率低，适合缓存。

缓存 key：

```text
tariff:agent:price:{version}:{provinceCode}:{secondType}:{thirdType}:{userType}:{dyLevel}:{sfType}
tariff:agent:areas:{version}
tariff:agent:options:{version}:{provinceCode}:{secondType}:{thirdType}
```

缓存策略：

- 已发布版本缓存 24 小时或长期缓存。
- 新批次发布后主动失效相关 key。
- 无数据结果可短缓存 5 分钟，避免穿透。

## 安全与治理

对外 API 需要具备：

- `appKey/appSecret` 签名。
- 时间戳和 nonce 防重放。
- IP 白名单。
- 按 appKey 限流。
- 调用日志。
- 失败告警。
- 版本发布审计。
- 数据导入审计。

签名建议：

```text
signature = HMAC-SHA256(appSecret, method + path + timestamp + nonce + bodySha256)
```

## 错误码

| 错误码 | 说明 |
| --- | --- |
| `TARIFF_PARAM_ERROR` | 参数错误 |
| `TARIFF_NO_DATA` | 当前条件没有电价数据 |
| `TARIFF_VERSION_NOT_FOUND` | 指定版本不存在 |
| `TARIFF_UNPUBLISHED` | 版本未发布 |
| `TARIFF_IMPORT_VALIDATE_FAILED` | 导入校验失败 |
| `TARIFF_AUTH_FAILED` | 鉴权失败 |
| `TARIFF_RATE_LIMITED` | 调用超过限流 |
| `TARIFF_INTERNAL_ERROR` | 服务内部异常 |

## 当前代码改造建议

### 服务层

当前 `TariffAgentPriceService` 可继续作为领域服务，但建议拆分职责：

- `TariffAgentPriceQueryService`：查询电价、区域、选项。
- `TariffAgentPriceImportService`：导入、校验、发布。
- `TariffAgentPriceVersionService`：版本解析、版本发布、版本状态。
- `TariffAgentPriceOpenApiService`：对外响应封装、鉴权上下文、调用日志。

### Mapper 层

当前 `TariffAgentPriceMapper` 主要支撑查询。导入发布建议新增独立 Mapper：

- `TariffImportBatchMapper`
- `TariffAgentPriceStagingMapper`
- `TariffFpgjTypeStagingMapper`
- `TariffOpenApiLogMapper`

### 接口层

保留：

- `/haomaidian/index/getDefaultMenus`
- `/haomaidian/index/getEnAgentPrices`
- `/areaDict/getDictByType`
- `/tariff/agent-price/options`
- `/tariff/agent-price/prices`

新增：

- `/openapi/v1/tariff/agent/versions`
- `/openapi/v1/tariff/agent/areas`
- `/openapi/v1/tariff/agent/options`
- `/openapi/v1/tariff/agent/prices/query`
- `/tariff/agent-price/import/upload`
- `/tariff/agent-price/import/validate`
- `/tariff/agent-price/import/approve`
- `/tariff/agent-price/import/publish`
- `/tariff/agent-price/import/batches`

## 实施计划

### 一期：可用版本

- 修正查询版本规则，禁止跨月 fallback。
- 新增版本列表接口。
- 新增对外查询接口，但先复用现有查询逻辑。
- 新增导入批次表和 staging 表。
- 支持标准 Excel 模板导入。
- 支持导入校验和人工发布。
- 支持北京地区 2026-06 数据完整查询。

### 二期：标准服务版本

- 对外返回 96 点明细。
- 补齐来源文件、批次、发布时间。
- 接入 appKey/appSecret 鉴权。
- 增加调用日志和限流。
- 增加缓存。
- 增加导入差异比对。

### 三期：全国数据运营版本

- 按省份维护官方来源配置。
- 支持 PDF 表格半自动解析。
- 支持发布审批流。
- 支持自动巡检缺失省份和缺失月份。
- 支持对客户发送版本发布通知。

## 验收标准

### 查询服务

- 查询北京 `2026-06` 有数据时正常返回。
- 查询北京 `2026-07-03` 且数据库没有 7 月数据时返回 `TARIFF_NO_DATA`。
- 区域三级联动、用户类型、电压等级、收费类型与当前版本数据一致。
- 返回结果包含版本、单位、来源、发布时间。
- 聚合时段和 96 点明细一致。

### 导入服务

- 标准模板可导入成功。
- 缺少 96 点时报错，不能发布。
- 同一版本重复导入必须经过替换发布。
- 发布失败可以回滚。
- 批次状态完整流转。
- 能追溯正式数据来自哪个文件和哪个批次。

## 结论

代理电价服务应从“页面查询接口”升级为“版本化电价数据服务”。

短期重点是把现有 `e_agent_price`、`e_agent_price_data`、`e_fpgj_type`、`e_fpgj_type_data` 用稳定，补齐版本、来源、导入批次和无数据规则。

中期重点是开放标准 OpenAPI，提供 96 点明细、聚合时段、数据来源和调用治理。

长期重点是建设全国电价数据运营能力，通过半自动采集、人工复核、审批发布和质量巡检，保证代理电价数据可查、可信、可追溯。
