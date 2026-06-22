# CampusTrade AI 系统架构设计

## 1. 项目概述

CampusTrade AI 是一个面向高校学生的校园二手交易平台。系统采用 Spring Cloud Alibaba 微服务架构，围绕用户、商品、订单、AI 和留言能力拆分服务。

平台不实现在线支付，交易方式默认为线下面交。AI 能力用于提升商品发布质量和平台内容安全。

## 2. 架构目标

- 满足课程对微服务数量、注册发现、服务调用、网关的要求。
- 保持模块边界清楚，方便解释和演示。
- 使用常见 Java 企业开发技术栈。
- 第一阶段以可运行骨架为主，后续逐步完善业务。
- AI 模块先 mock，保留后续接入真实大模型 API 的扩展点。

## 3. 技术选型

下表包含当前骨架已使用的技术，以及后续阶段计划接入的技术。第一阶段优先保证服务能启动和接口结构清楚，数据库、JWT、Redis 等能力会在后续小步提交中逐步补齐。

| 技术 | 用途 |
| --- | --- |
| Java 17 | 后端开发语言 |
| Spring Boot | 单个服务开发框架 |
| Spring Cloud Alibaba | 微服务基础能力 |
| Nacos Discovery | 服务注册与发现 |
| Nacos Config | 配置中心 |
| Spring Cloud Gateway | API 网关 |
| OpenFeign | 服务间 HTTP 调用 |
| Sentinel | 服务熔断和限流 |
| MyBatis Plus | 数据访问 |
| MySQL | 业务数据存储 |
| Redis | 缓存和登录状态辅助 |
| JWT | 用户认证 |
| Knife4j / Swagger | 接口文档 |
| Vue 3 + Element Plus | 可选前端 |

## 4. 微服务划分

### campus-common

公共模块，不作为独立服务启动。

职责：

- 统一响应结构
- 统一异常定义
- 公共常量
- 公共工具类
- 基础 DTO

### campus-gateway

系统统一入口。

职责：

- 路由转发
- 跨域配置
- JWT 鉴权入口
- 统一请求日志
- 后续接入限流策略

### campus-user

用户服务。

职责：

- 用户注册
- 用户登录
- 第一阶段返回 mock token，后续替换为 JWT 签发
- 用户信息查询
- 用户基础资料维护

当前骨架接口：

- `POST /user/register`
- `POST /user/login`
- `GET /user/{id}`

第一阶段用户数据暂存在服务内存中，便于课程演示 controller-service 分层；后续再接入数据库、密码加密和完整认证。

### campus-product

商品服务。

职责：

- 商品发布
- 商品查询
- 商品搜索
- 商品上下架
- 商品分类管理
- 调用 campus-ai 完成描述优化、智能分类和违规检测

当前第一阶段接口：

- `POST /product`
- `GET /product`
- `GET /product/{id}`
- `PUT /product/{id}/status`

第一阶段商品数据暂存在服务内存中，支持发布、按关键词/分类/状态筛选、详情查询和状态更新。商品状态包括 `ON_SALE`、`OFF_SALE` 和 `SOLD`。后续持久化阶段会接入 MyBatis Plus 和 MySQL，将当前内存存储替换为数据库表，并为订单、搜索和 AI 审核流程提供更稳定的数据基础。

### campus-order

订单服务。

职责：

- 创建订单
- 查询订单
- 取消订单
- 完成订单
- 调用 campus-product 查询商品信息
- 调用 campus-user 查询用户信息

### campus-ai

AI 服务。

职责：

- 商品描述优化
- 商品智能分类
- 商品违规内容检测
- 当前使用 `MockAiProvider` 返回确定性 mock 结果
- 后续可替换为真实大模型 API provider

### campus-message

留言服务，可选。

职责：

- 商品留言
- 买卖双方沟通
- 留言查询
- 留言删除或隐藏

## 5. 服务调用关系

```text
Client
  |
  v
campus-gateway
  |
  +--> campus-user
  +--> campus-product ----> campus-ai
  +--> campus-order ------> campus-user
  |                    \--> campus-product
  +--> campus-message ---> campus-user
                       \--> campus-product
```

## 6. 数据库规划

第一阶段可以为每个服务保留独立数据库命名，后续根据实现逐步创建表。

推荐数据库：

- campus_user_db
- campus_product_db
- campus_order_db
- campus_message_db
- campus_ai_db 可选，mock 阶段可以不建库

## 7. AI 服务设计

AI 服务当前不接真实模型，不需要 API key，也不访问外部网络；它通过 `AiProvider` 接口隔离能力实现，当前 Spring 注入的是确定性的 `MockAiProvider`。

当前接口：

- `POST /ai/description/optimize`
- `POST /ai/category/predict`
- `POST /ai/content/check`

当前行为：

- 描述优化接口接收标题和描述，返回一段更完整、适合校园二手交易展示的描述。
- 分类预测接口根据标题和描述中的关键词返回 `数码`、`图书`、`生活用品`、`运动户外` 或 `其他`，并返回固定置信度。
- 内容检查接口根据 mock 关键词判断是否通过，命中明显违规词时返回不通过和原因。
- 三个接口都使用 `campus-common` 的 `ApiResponse` 统一响应结构；请求缺少必要字段时返回 `BAD_REQUEST`。
- 当前 AI 服务不使用数据库，也不会保存请求或结果。

## 8. 第一阶段验收标准

- Maven 多模块结构创建完成。
- 至少 gateway、user、product、order、ai 五个服务模块存在。
- 项目可以执行 Maven 编译。
- README、dev-log、architecture 文档同步存在。
- 每个服务的职责可以清楚解释。
- 暂不要求完整业务代码。
