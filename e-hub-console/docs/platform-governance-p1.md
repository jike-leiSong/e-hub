# 平台治理 P1 方案

## 目标

在当前 `工作台 / 租户中心 / 身份与权限中心 / 平台设置中心` 四大菜单结构已经收口的基础上，先完成 P1 可运营能力：

- 工作台：提供平台治理汇总、待办、最近操作
- 租户中心：提供租户主数据与产品订阅维护
- 身份与权限中心：提供账号、角色、角色授权基础能力
- 平台设置中心：提供平台参数、字典、操作日志

## 设计原则

- 保留现有 `console_user` 登录体系
- 保留现有 `console_customer_product` 兼容逻辑
- 新增 `tenant / role / permission / config / log` 模型逐步替换代码推导
- P1 不改负荷聚合、电价服务业务表结构
- P1 不引入审批流、SSO、复杂数据权限

## P1 数据模型

- `console_tenant`
- `console_tenant_product`
- `console_role`
- `console_permission`
- `console_user_role`
- `console_role_permission`
- `console_config_item`
- `console_dict_type`
- `console_dict_item`
- `console_operation_log`

同时补充：

- `console_user.tenant_id`

## P1 接口范围

### 工作台

- `GET /platform/workbench/summary`
- `GET /platform/workbench/todos`
- `GET /platform/workbench/recent-logs`

### 租户中心

- `GET /tenant/page`
- `GET /tenant/{tenantId}`
- `POST /tenant`
- `PUT /tenant/{tenantId}`
- `PUT /tenant/{tenantId}/status`
- `GET /tenant/{tenantId}/products`
- `PUT /tenant/{tenantId}/products`

### 身份与权限中心

- `GET /console-user/page`
- `POST /console-user`
- `PUT /console-user/{userId}`
- `PUT /console-user/{userId}/status`
- `PUT /console-user/{userId}/roles`
- `GET /permission/roles`
- `POST /permission/roles`
- `PUT /permission/roles/{roleId}`
- `GET /permission/tree`
- `PUT /permission/roles/{roleId}/permissions`

### 平台设置中心

- `GET /platform/config/items`
- `POST /platform/config/items`
- `PUT /platform/config/items/{id}`
- `GET /platform/dict/types`
- `GET /platform/dict/items`
- `GET /platform/audit/logs`

## 认证与权限改造

- 登录态补充 `tenantId`
- 角色权限从“纯代码推导”升级为“数据库优先，代码兜底”
- 接口权限按角色授权结果控制，未配置时走现有默认逻辑
- 负荷聚合的数据范围控制继续复用 `LoadAggregationScopeService`

## 开发顺序

1. SQL 脚本、实体、Mapper
2. 租户中心后端接口
3. 角色权限后端接口
4. 平台配置/审计接口
5. 工作台汇总接口
6. 接入认证上下文与默认权限兜底

## P1 边界

P1 不做：

- 审批流
- 按钮级权限
- 数据权限配置化
- SSO / LDAP
- 产品额度、API 凭证
- 前端复杂交互改版
