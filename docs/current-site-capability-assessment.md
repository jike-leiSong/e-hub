# e-hub 当前能力梳理与完成度评估

更新时间：2026-07-06

## 1. 文档目的

本文档用于沉淀当前项目的真实现状，作为后续逐项补齐能力的开发底稿。  
本次梳理以代码实现为准，不以 README、历史汇报或口头结论为准。

适用范围：

- `e-hub-console` 运营平台前后端
- 平台治理
- 负荷聚合
- 电价服务
- 资源管理与 IoT 管理

## 2. 总体结论

当前项目不是“整体不可用”，而是能力分化明显：

- 平台治理已经形成真实页面、真实接口、真实数据闭环。
- 电价服务已经形成真实查询能力，兼容新接口和旧接口。
- 资源管理、IoT 管理已经具备较完整的主数据维护和时序查询能力。
- 负荷聚合核心运营链路仍未闭环，尤其是运营总览、调节情况、收益结算、申报计划等模块，存在大量空实现或半实现。

整体判断：

- 页面壳与菜单完成度：约 `80%`
- 真实业务闭环完成度：约 `55% ~ 60%`
- 平台治理完成度：约 `75% ~ 80%`
- 电价服务完成度：约 `70%`
- 资源管理 + IoT 管理完成度：约 `80%`
- 负荷聚合核心业务完成度：约 `30% ~ 40%`

## 3. 代码事实基线

### 3.1 实际技术栈

后端实际技术栈：

- Java `1.8`
- Spring Boot `2.3.4.RELEASE`
- MyBatis + tk-mapper + PageHelper

前端实际技术栈：

- Vue `2.7.16`
- Element UI `2.15.14`
- Vite `5`

对应代码：

- `pom.xml`
- `e-hub-console/frontend/package.json`

说明：

- README 中存在 `Spring Boot 3.x / Java 17 / Vue 3` 等说法，与当前代码不一致。
- 当前评估必须以现有代码和库表映射为准。

### 3.2 页面实际挂载情况

当前主界面实际挂载页面见：

- `e-hub-console/frontend/src/App.vue`
- `e-hub-console/frontend/src/auth/session.js`

已真实挂载页面：

- 工作台
- 负荷聚合 / 运营总览
- 负荷聚合 / 调节情况
- 负荷聚合 / 收益结算
- 负荷聚合 / 资源管理
- 负荷聚合 / 物联管理
- 电价服务 / 电网代理价格
- 租户中心
- 身份与权限中心
- 平台设置中心

仍为占位页面：

- 电价服务 / 接口能力
- 电价服务 / 调用记录
- 无产品页

## 4. 模块能力梳理

### 4.1 平台治理

#### 4.1.1 工作台

前端状态：

- 已完成真实页面。
- 展示汇总指标、治理待办、最近操作、业务入口跳转。

后端状态：

- 已完成真实接口与真实统计逻辑。
- 接口：
  - `GET /platform/workbench/summary`
  - `GET /platform/workbench/todos`
  - `GET /platform/workbench/recent-logs`

主要数据依赖：

- `console_tenant`
- `console_tenant_product`
- `console_user`
- `console_role`
- `console_config_item`
- `console_operation_log`
- `console_user_role`

当前可用性：

- 可用。

缺口：

- 待办规则目前较轻，更多是平台运营提醒，尚未覆盖业务异常待办。

对应代码：

- `e-hub-console/frontend/src/PlatformWorkbench.vue`
- `e-hub-console/src/main/java/cn/sl/ehub/console/controller/platform/PlatformWorkbenchController.java`
- `e-hub-console/src/main/java/cn/sl/ehub/console/service/impl/PlatformWorkbenchServiceImpl.java`

#### 4.1.2 租户中心

前端状态：

- 已完成真实页面。
- 支持分页、筛选、详情、新增、编辑、状态切换、产品订阅配置。

后端状态：

- 已完成真实租户 CRUD 和产品订阅保存。
- 操作会写入平台审计日志。

主要数据依赖：

- `console_tenant`
- `console_tenant_product`
- `console_customer_product`
- `console_user`

当前可用性：

- 可用。

缺口：

- 数据范围治理尚未形成更细粒度能力。
- API 凭证、客户级限额等产品运营能力未纳入。

对应代码：

- `e-hub-console/frontend/src/ProductProvisioning.vue`
- `e-hub-console/src/main/java/cn/sl/ehub/console/controller/platform/TenantController.java`
- `e-hub-console/src/main/java/cn/sl/ehub/console/service/impl/TenantServiceImpl.java`

#### 4.1.3 身份与权限中心

前端状态：

- 已完成真实页面。
- 包含账号管理、角色权限两大页签。

后端状态：

- 已完成账号分页、新增、修改、状态切换。
- 已完成角色分页、新增、修改。
- 已完成角色授权、用户角色绑定。
- 权限优先按数据库角色授权生效，未配置时使用代码默认权限兜底。

主要数据依赖：

- `console_user`
- `console_role`
- `console_permission`
- `console_user_role`
- `console_role_permission`
- `console_tenant`

当前可用性：

- 可用。

缺口：

- 没有单独的数据权限模型表。
- 菜单权限、接口权限和数据范围权限仍有部分耦合在代码里。

对应代码：

- `e-hub-console/frontend/src/IdentityAccessCenter.vue`
- `e-hub-console/src/main/java/cn/sl/ehub/console/controller/platform/ConsoleUserController.java`
- `e-hub-console/src/main/java/cn/sl/ehub/console/controller/platform/PermissionController.java`
- `e-hub-console/src/main/java/cn/sl/ehub/console/service/impl/ConsoleUserManageServiceImpl.java`
- `e-hub-console/src/main/java/cn/sl/ehub/console/service/impl/RolePermissionServiceImpl.java`
- `e-hub-console/src/main/java/cn/sl/ehub/console/auth/ConsolePermissionService.java`

#### 4.1.4 平台设置中心

前端状态：

- 已完成真实页面。
- 包含配置项治理、字典中心、操作审计三部分。

后端状态：

- 配置项：真分页、真新增、真修改。
- 字典中心：真查询，但仅支持浏览。
- 操作审计：真分页查询。

主要数据依赖：

- `console_config_item`
- `console_dict_type`
- `console_dict_item`
- `console_operation_log`

当前可用性：

- 大体可用。

缺口：

- 字典中心暂不支持增删改。
- 平台级集成配置、第三方接口配置尚未统一纳管。

对应代码：

- `e-hub-console/frontend/src/PlatformSettingsCenter.vue`
- `e-hub-console/src/main/java/cn/sl/ehub/console/controller/platform/PlatformConfigController.java`
- `e-hub-console/src/main/java/cn/sl/ehub/console/controller/platform/PlatformDictController.java`
- `e-hub-console/src/main/java/cn/sl/ehub/console/controller/platform/PlatformAuditLogController.java`

### 4.2 电价服务

#### 4.2.1 电网代理价格

前端状态：

- 已完成真实页面。
- 支持按年月、区域、用电属性、收费类型、电压等级进行查询。
- 当前前端对三级区域有完整交互入口。

后端状态：

- 已完成真实查询能力。
- 同时支持新接口和兼容旧接口。

新接口：

- `GET /tariff/agent-price/options`
- `POST /tariff/agent-price/prices`

兼容接口：

- `POST /haomaidian/index/getDefaultMenus`
- `POST /haomaidian/index/getEnAgentPrices`
- `POST /areaDict/getDictByType`
- `GET/POST /openapi/v1/tariff/agent/*`

主要数据依赖：

- `e_agent_price`
- `e_agent_price_data`
- `e_fpgj_type`
- `e_fpgj_type_data`

当前可用性：

- 可用。

缺口：

- 需要继续核对不同省份下 `secondType/thirdType/dyLevel` 与真实数据字典的一致性。
- 接口运营能力和调用留痕平台页未建设完成。

对应代码：

- `e-hub-console/frontend/src/modules/tariff/agent-price/AgentPrice.vue`
- `e-hub-console/src/main/java/cn/sl/ehub/console/controller/tariff/TariffAgentPriceController.java`
- `e-hub-console/src/main/java/cn/sl/ehub/console/controller/tariff/HaomaidianIndexController.java`
- `e-hub-console/src/main/java/cn/sl/ehub/console/controller/tariff/AreaDictController.java`
- `e-hub-service/src/main/java/cn/sl/ehub/service/service/TariffAgentPriceService.java`

#### 4.2.2 接口能力

前端状态：

- 占位页。

后端状态：

- 仅有开放接口能力本身，没有运营配置页。

当前可用性：

- 控制台不可用。

缺口：

- 接口凭证管理
- 接口订阅管理
- 调用额度/限流策略
- 客户级 API 配置

#### 4.2.3 调用记录

前端状态：

- 占位页。

后端状态：

- 未形成完整的控制台调用记录查询能力。

当前可用性：

- 不可用。

缺口：

- 调用明细
- 调用成功率
- 响应耗时
- 错误追踪

### 4.3 负荷聚合 / 资源管理

前端状态：

- 已完成三个真实页签：
  - 企业管理
  - 模型管理
  - 设备管理

后端状态：

- 已完成企业、模型、设备映射的真实 CRUD。
- 已接入负荷聚合数据范围校验。

主要数据依赖：

- `aggregator_ent`
- `aggregator_single_model_data`
- `aggregator_ent_device`
- `aggregator_resource_type`
- `iot_device`

当前可用性：

- 可用。

缺口：

- 业务规则仍偏主数据维护，缺少批量导入、校验报告、历史版本审计。
- 设备映射与后续业务运行状态的联动关系还需继续补齐。

对应代码：

- `e-hub-console/frontend/src/modules/load-aggregation/resources/LoadResources.vue`
- `e-hub-console/src/main/java/cn/sl/ehub/console/controller/loadaggregation/AggregatorEntController.java`
- `e-hub-console/src/main/java/cn/sl/ehub/console/controller/loadaggregation/AggregatorSingleModelDataController.java`
- `e-hub-console/src/main/java/cn/sl/ehub/console/controller/loadaggregation/AggregatorEntDeviceController.java`

### 4.4 负荷聚合 / 物联管理

#### 4.4.1 物联设备

前端状态：

- 已完成真实页面。
- 支持企业切换、设备组管理、设备管理、设备点表维护。

后端状态：

- 已完成设备组、设备、设备点位、元数据、默认网关等管理逻辑。

主要数据依赖：

- `iot_device_group`
- `iot_device_group_param`
- `iot_device_group_point`
- `iot_gateway`
- `iot_device`
- `iot_device_param`
- `iot_device_point`
- `sys_dict`

当前可用性：

- 可用。

缺口：

- 默认第三方 API、默认网关逻辑仍偏技术兜底，平台化治理不够。
- 设备接入链路的错误回放、对账工具尚未形成产品能力。

对应代码：

- `e-hub-console/frontend/src/modules/load-aggregation/iot-management/IotManagement.vue`
- `e-hub-console/src/main/java/cn/sl/ehub/console/controller/iot/IotDeviceManagementController.java`
- `e-hub-service/src/main/java/cn/sl/ehub/service/service/IotDeviceManagementService.java`

#### 4.4.2 物联数据

前端状态：

- 已完成真实页面。
- 支持分钟数据、聚合数据、原始明细、导出、设备概览。

后端状态：

- 已完成时序查询、聚合查询、原始数据追溯、导出、设备概览。

主要数据依赖：

- `iot_telemetry_minute`
- 其他 IoT 原始数据与关联表

当前可用性：

- 可用。

缺口：

- 更高层业务指标和 IoT 诊断能力尚未接入。

对应代码：

- `e-hub-console/src/main/java/cn/sl/ehub/console/controller/iot/IotTelemetryController.java`

### 4.5 负荷聚合 / 运营总览

前端状态：

- 页面存在，结构较完整。
- 已挂载实时、申报计划、收益等多个组件。

后端状态：

- 大量核心接口为空返回或仅返回空数组。
- `Yesterday/Today/Tomorrow` 只有少量设备树和设备列表能力，核心业务图表未实现。

主要数据依赖：

- `aggregator_ent_device`
- 若干历史业务表

当前可用性：

- 仅能看页面骨架和少量设备树数据。
- 不可视为真实运营总览。

缺口：

- 昨日收益
- 昨日概览
- 今日实时汇总
- 今日 IoT 日志
- 明日申报/报价/交付曲线
- 自动申报逻辑

对应代码：

- `e-hub-console/frontend/src/modules/load-aggregation/overview/Aggregation.vue`
- `e-hub-console/src/main/java/cn/sl/ehub/console/service/impl/YesterdayServiceImpl.java`
- `e-hub-console/src/main/java/cn/sl/ehub/console/service/impl/TodayServiceImpl.java`
- `e-hub-console/src/main/java/cn/sl/ehub/console/service/impl/TomorrowServiceImpl.java`

### 4.6 负荷聚合 / 调节情况

前端状态：

- 页面存在。
- 包含：
  - 调节情况
  - 物联数据
  - 收益统计
  - 用户收益统计
  - 出清价格
  - 收益结算

后端状态：

- `getTotalPowerChart` 已基本形成真实逻辑。
- 其余大部分接口仍为空实现或仅返回空对象。

当前已实现能力：

- 聚合商汇总功率曲线
- 业务曲线对齐展示：
  - 实际汇总功率
  - 聚合申报功率
  - 调度下发功率
  - 基线
  - 碳排因子
  - 出清价格

汇总功率曲线依赖：

- `aggregator_ent_device`
- `iot_telemetry_minute`
- `aggregator_date_issue_chart`
- `aggregator_dap_chart`
- `aggregator_base_line_load_chart`
- `aggregator_cr_chart`
- `aggregator_resource_date_issue_offer`

当前可用性：

- 仅“汇总功率曲线”部分可用。
- 调节情况整体仍不能算完成。

缺口：

- 用户调节曲线
- 用户调节表格
- 设备运行情况
- 调节导出
- 补招上传导出

对应代码：

- `e-hub-console/frontend/src/modules/load-aggregation/history/src/AggregationHistory.vue`
- `e-hub-console/src/main/java/cn/sl/ehub/console/controller/loadaggregation/HistoryQueryController.java`
- `e-hub-console/src/main/java/cn/sl/ehub/console/service/impl/HistoryQueryServiceImpl.java`

### 4.7 负荷聚合 / 收益结算

前端状态：

- 页签和页面结构存在。
- 展示上已具备收益统计、用户收益统计、出清价格、收益结算入口。

后端状态：

- 大部分核心接口为空实现：
  - `profitStatistics`
  - `userProfitStatistics`
  - `getPrice`
  - `getPriceTable`
  - `getPriceExcel`
  - `getProfitCalculation`
  - `getProfitCalculationMap`

当前可用性：

- 基本不可用。

缺口：

- 收益统计口径计算
- 用户分摊收益计算
- 出清价格查询
- 收益结算台账
- Excel 导出

## 5. 登录、权限与数据范围

### 5.1 登录与会话

已实现能力：

- `POST /auth/login`
- `POST /auth/logout`
- `GET /auth/me`

当前实现特点：

- 登录态保存在应用进程内 `ConcurrentHashMap`
- 不是 Redis
- 不是共享会话
- 服务重启后会失效
- 多实例场景下不安全

对应代码：

- `e-hub-console/src/main/java/cn/sl/ehub/console/auth/AuthController.java`
- `e-hub-console/src/main/java/cn/sl/ehub/console/auth/ConsoleAuthService.java`

### 5.2 数据范围

负荷聚合侧已实现基本范围收敛：

- 平台管理员：可看全部
- 企业客户：固定到自身 `aggregatorId + entId`
- 聚合商客户：固定到自身 `aggregatorId`，并校验企业归属

对应代码：

- `e-hub-console/src/main/java/cn/sl/ehub/console/auth/LoadAggregationScopeService.java`

说明：

- 该能力已经可以约束资源管理、IoT 管理和部分负荷聚合查询。
- 但更细的业务数据权限模型还未平台化。

## 6. 本网站实际使用流程

### 6.1 平台运营侧使用流程

建议按以下顺序使用：

1. 登录平台
2. 进入工作台查看待办与总体情况
3. 在租户中心创建或维护租户
4. 为租户开通产品
5. 在身份与权限中心创建账号、角色并授权
6. 在平台设置中心维护平台配置项和基础字典
7. 进入具体业务产品进行运营

### 6.2 负荷聚合业务使用流程

建议按以下顺序推进：

1. 维护企业主数据
2. 维护模型主数据
3. 维护设备映射关系
4. 在 IoT 管理中维护设备组、设备、点表
5. 接入分钟级时序数据
6. 先验证物联数据查询正确
7. 再推进运营总览、调节情况、收益结算等业务页面

说明：

- 如果前面的资源与 IoT 主数据不完整，后面的运营、调节、收益页即使页面存在，也很难有真实数据。

### 6.3 电价服务使用流程

建议按以下顺序使用：

1. 选择年月
2. 选择省份
3. 选择二级区域
4. 选择三级区域
5. 选择企业用电属性
6. 选择收费类型
7. 选择电压等级
8. 查询峰平谷价格与时段

## 7. 当前主要风险与注意事项

### 7.1 文档与代码漂移

当前 README、历史迁移总结和实际代码存在较大偏差，主要表现为：

- 技术栈描述不一致
- 完成度描述偏乐观
- 某些“已完成”模块在代码中仍是空实现

结论：

- 后续所有开发排期、验收和联调，都必须以代码和数据库为准。

### 7.2 前端本地代理不完整

`vite.config.js` 当前只代理了部分旧路径，没有完整覆盖新治理接口和部分新业务接口。

影响：

- `npm run dev` 联调时，部分页面可能直接 404
- 容易误判为后端未实现，实际是前端代理没转发

### 7.3 历史页仍保留外部网关回退

历史查询前端 API 里仍有外部环境回退逻辑和 `X-GW-AccessKey` 逻辑。

影响：

- 本地改了代码，但请求可能没有打到本地服务
- 调试容易出现环境混淆

### 7.4 测试体系不足

根 POM 默认配置：

- `maven.test.skip=true`

影响：

- 当前更适合做快速迭代，不适合高置信回归
- 后续逐项补功能时，需要同步补最少量回归验证

## 8. 完成度评估

| 维度 | 完成度 | 说明 |
| --- | --- | --- |
| 页面与菜单壳 | 80% | 主要页面都已挂载 |
| 平台治理 | 75% ~ 80% | 已可支撑基础平台运营 |
| 电价服务 | 70% | 查询可用，运营页缺失 |
| 资源管理 + IoT 管理 | 80% | 主数据和时序查询已具备 |
| 负荷聚合核心业务 | 30% ~ 40% | 页面多，真实逻辑少 |
| 全站真实业务闭环 | 55% ~ 60% | 可以用，但离完整平台还有明显差距 |

## 9. 后续开发优先级建议

建议按“先打通底座，再补业务闭环”的顺序推进。

### P0：先保证底座稳定

1. 修正文档与代码事实不一致问题
2. 补齐前端本地代理
3. 梳理历史页 API 是否统一走本地 console
4. 建立最小验证清单

### P1：先补负荷聚合最短闭环

1. 资源管理数据校验
2. IoT 数据链路校验
3. 运营总览的昨日、今日、明日真实能力
4. 调节情况剩余能力补齐

### P2：补收益结算

1. 收益统计
2. 用户收益统计
3. 出清价格
4. 收益结算台账
5. 导出

### P3：补平台级运营能力

1. 电价服务接口能力页
2. 电价服务调用记录页
3. 更细的数据权限能力
4. 平台配置和集成治理增强

## 10. 建议的下一步执行方式

后续建议按以下方式逐项完成：

1. 选定一个模块
2. 先确认前端页面、后端接口、库表依赖、现状缺口
3. 先补后端真实逻辑
4. 再做前端联调
5. 最后补验证说明与文档

推荐优先顺序：

1. 运营总览
2. 调节情况
3. 收益结算
4. 电价服务接口能力
5. 电价服务调用记录

## 11. 结论

当前项目已经具备“平台壳 + 治理底座 + 主数据 + IoT + 电价查询”的基础能力。  
真正的主要短板不在平台治理，而在负荷聚合核心业务链路尚未闭环。

后续开发不建议再大范围泛化改造，而应按模块逐个收口，形成可验证、可联调、可上线的最小闭环。
