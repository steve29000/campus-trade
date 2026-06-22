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
- 全局异常处理（servlet MVC 服务统一兜底）
- 公共常量
- 公共工具类
- 基础 DTO

`campus-common` 通过 Spring Boot 自动配置注册全局异常处理器 `GlobalExceptionHandler`，引入 servlet MVC 的服务（user、product、order、ai、message）无需额外配置即可统一兜底业务异常、请求解析错误和未预期异常，避免直接暴露框架 500 堆栈。自动配置使用 `@ConditionalOnClass(DispatcherServlet)` 守卫，`campus-gateway` 这类 WebFlux 模块会自动跳过，其响应式异常处理留待后续阶段单独接入。各服务保持现有约定：HTTP 状态固定为 200，真正的语义放在响应体的 `code` 字段里。

### campus-gateway

系统统一入口。

当前职责：

- 路由转发

当前网关路由表：

| Public Path | Service ID | Target URI |
| --- | --- | --- |
| `/user/**` | `campus-user` | `lb://campus-user` |
| `/product/**` | `campus-product` | `lb://campus-product` |
| `/order/**` | `campus-order` | `lb://campus-order` |
| `/ai/**` | `campus-ai` | `lb://campus-ai` |
| `/message/**` | `campus-message` | `lb://campus-message` |

当前阶段 `campus-gateway` 只负责把请求按照路径转发到对应服务，暂不做路径重写。JWT 鉴权、CORS 自定义、Sentinel 限流/降级和更完整的统一请求日志会在后续阶段逐步接入。
`lb://` 目标地址依赖 Spring Cloud LoadBalancer 和服务发现能力，当前测试会校验路由表以及 LoadBalancer 运行时支持是否存在。

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

第一阶段商品数据暂存在服务内存中，支持发布、按关键词/分类/状态筛选、详情查询和状态更新。商品状态包括 `ON_SALE`、`OFF_SALE` 和 `SOLD`。商品发布时，`campus-product` 通过 OpenFeign 调用 `campus-ai` 的内容检查接口（`POST /ai/content/check`）对标题和描述做违规检测：命中违规返回 `FORBIDDEN` 并拒绝发布，AI 服务不可用返回 `SYSTEM_ERROR`，字段校验先于内容检查执行。描述优化和智能分类的接入留待后续阶段。后续持久化阶段会接入 MyBatis Plus 和 MySQL，将当前内存存储替换为数据库表，并为订单、搜索和 AI 审核流程提供更稳定的数据基础。

### campus-order

订单服务。

职责：

- 创建订单
- 查询订单
- 取消订单
- 完成订单
- 通过 OpenFeign 调用 campus-user 校验买家和卖家
- 通过 OpenFeign 调用 campus-product 查询商品信息
- 从商品服务返回结果生成订单商品快照

当前接口：

- `POST /order`
- `GET /order/{id}`
- `GET /order/buyer/{buyerId}`
- `GET /order/seller/{sellerId}`
- `PUT /order/{id}/cancel`
- `PUT /order/{id}/complete`

当前订单数据暂存在服务内存中，支持订单创建、详情查询、买家订单列表、卖家订单列表、取消和完成。订单状态包括 `CREATED`、`CANCELLED` 和 `COMPLETED`。创建订单时，客户端只提交 `buyerId`、`sellerId` 和 `productId`，订单服务通过 OpenFeign 查询用户和商品：买家或卖家不存在时拒绝创建，商品不存在、非 `ON_SALE` 或商品发布者与 `sellerId` 不匹配时拒绝创建。订单响应中的商品标题和价格来自 `campus-product` 的商品快照。订单创建成功前，`campus-order` 会调用 `campus-product` 将商品状态更新为 `SOLD`，避免当前内存演示流程中重复下单。本阶段不实现在线支付，校园二手交易仍默认线下面交。后续持久化阶段会通过 MyBatis Plus 和 MySQL 将当前内存订单迁移到数据库，并进一步处理并发下单和跨服务一致性问题。

### campus-ai

AI 服务。

职责：

- 商品描述优化
- 商品智能分类
- 商品违规内容检测
- 当前使用 `MockAiProvider` 返回确定性 mock 结果
- 后续可替换为真实大模型 API provider

### campus-message

留言服务。

职责：

- 商品留言
- 买卖双方沟通
- 留言查询
- 留言删除或隐藏

当前接口：

- `POST /message`
- `GET /message/product/{productId}`
- `PUT /message/{id}/hide`
- `DELETE /message/{id}`

当前留言数据暂存在服务内存中，支持留言发布、按商品查询、隐藏和删除。留言状态包括 `VISIBLE` 和 `HIDDEN`，按商品查询只返回 `VISIBLE` 留言并按 id 升序排列。发布留言时，`campus-message` 通过 OpenFeign 调用 `campus-user` 和 `campus-product` 校验发送者和商品是否存在：任一不存在时拒绝创建，远程服务不可用时返回 `SYSTEM_ERROR`。后续持久化阶段会通过 MyBatis Plus 和 MySQL 将内存留言迁移到数据库。

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

`campus-user`、`campus-product`、`campus-order`、`campus-message` 已接入 MyBatis Plus + MySQL，采用**每服务独立库**，内存存储已全部替换为数据库表。`campus-ai` 为无状态 mock，不使用数据库。

当前数据库与主表：

- `campus_user_db.user`
- `campus_product_db.product`
- `campus_order_db.orders`（`order` 为保留字，表名用 `orders`）
- `campus_message_db.message`
- campus_ai_db 不需要

建库建表脚本见 [`docs/sql/schema.sql`](sql/schema.sql)。本地用 Docker 运行 MySQL，可执行：

```bash
docker exec -i campus-mysql mysql -uroot -pcampus1234 < docs/sql/schema.sql
```

实现要点：

- 主键为 BIGINT 自增（`@TableId(IdType.AUTO)`），替换原先的内存自增 id。
- 枚举（`ProductStatus`/`OrderStatus`/`MessageStatus`）通过全局 `EnumTypeHandler` 按名称存为 VARCHAR。
- 不建跨库外键；买家/卖家/商品/发送者的存在性仍由 OpenFeign 在 service 层校验，保持服务库独立。
- 单元测试使用 H2（MySQL 兼容模式）跑真实 SQL，运行时连接 MySQL。
- 数据源连接信息当前写在各服务 `application.yml`，后续可迁移到 Nacos 配置中心或环境变量。

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
