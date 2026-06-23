# CampusTrade AI

CampusTrade AI 是一个基于 Spring Cloud Alibaba 的校园二手交易平台，面向高校学生提供二手商品发布、浏览、搜索、订单交易、留言沟通等功能，并逐步加入 AI 能力，包括商品描述优化、商品智能分类和违规内容检测。

本项目既作为 Java 微服务课程大作业，也作为 GitHub 作品集项目维护。项目优先保证架构清晰、功能可运行、文档可解释，再逐步完善业务细节。

## 项目目标

- 使用 Spring Boot 和 Spring Cloud Alibaba 构建微服务系统。
- 至少包含 3 个独立微服务。
- 实现服务注册与发现、服务间调用、网关路由。
- 逐步加入配置中心、服务熔断、接口文档等进阶能力。
- 使用 mock AI provider 完成 AI 服务第一版，后续可替换为真实大模型 API。
- 不实现在线支付，校园交易默认线下面交。

## 核心功能

- 用户注册、登录、后续 JWT 鉴权
- 商品发布、编辑、浏览、搜索
- 商品分类与状态管理
- 订单创建、取消、完成
- 买卖双方留言沟通
- AI 商品描述优化
- AI 商品智能分类
- AI 违规内容检测

## 技术栈

- Java 17
- Spring Boot
- Spring Cloud Alibaba
- Nacos Discovery
- Nacos Config
- Spring Cloud Gateway
- OpenFeign
- Sentinel
- MyBatis Plus
- MySQL
- Redis
- JWT
- Knife4j / Swagger
- Vue 3 + Element Plus，可选

## 微服务模块规划

| 模块 | 说明 |
| --- | --- |
| campus-common | 公共响应、异常、工具类、基础 DTO |
| campus-gateway | API 网关、统一路由、鉴权入口 |
| campus-user | 用户注册、登录、用户信息 |
| campus-product | 商品发布、浏览、搜索、分类 |
| campus-order | 订单创建、状态流转 |
| campus-ai | AI mock 服务，提供描述优化、分类、审核 |
| campus-message | 留言服务，可选模块 |

## 第一阶段目标

第一阶段不直接实现完整业务，而是完成项目骨架、文档、Maven 多模块结构和最小可运行服务。

推荐开发顺序：

1. 初始化文档和项目规范。
2. 创建 Maven 父工程。
3. 创建公共模块 campus-common。
4. 创建 gateway、user、product、order、ai 基础服务模块。
5. 接入 Nacos Discovery。
6. 添加 Gateway 路由。
7. 添加 OpenFeign 示例调用。
8. 添加 AI mock provider。
9. 补充接口文档。
10. 更新开发日志和架构文档。

## 本地运行规划

后续项目完成基础骨架后，推荐启动顺序：

1. 启动 MySQL、Redis、Nacos。
2. 启动 campus-user。
3. 启动 campus-product。
4. 启动 campus-order。
5. 启动 campus-ai。
6. 启动 campus-gateway。
7. 通过 gateway 访问各服务接口。

## 当前运行方式

当前阶段包含 Maven 多模块骨架、`campus-user` 的注册/登录 mock/用户资料查询接口、`campus-ai` 的描述优化/分类预测/内容检查 mock 接口、`campus-product` 的商品发布/浏览/详情/状态更新接口、`campus-order` 的订单创建/查询/取消/完成接口、`campus-message` 的留言发布/查询/隐藏/删除接口，以及 `campus-gateway` 的基础路由转发配置。`campus-product` 发布时通过 OpenFeign 调用 `campus-ai` 做内容安全检查；`campus-order` 创建订单时通过 OpenFeign 调用 `campus-user` 和 `campus-product` 校验买家、卖家、商品并生成商品快照，下单成功前把商品状态更新为 `SOLD`，取消订单时回滚为 `ON_SALE`；`campus-message` 发布留言时校验发送者和商品。

`campus-user`、`campus-product`、`campus-order`、`campus-message` 已接入 **MyBatis Plus + MySQL（每服务独立库）**，数据真正落库；`campus-ai` 为无状态 mock，不用数据库。`campus-gateway` 已接入 **JWT 鉴权**：登录/注册放行，其余请求需携带 `Authorization: Bearer <token>`，登录由 `campus-user` 签发 JWT。真实大模型接入、在线支付、CORS 自定义、Sentinel 和路径重写仍在后续阶段之外；校园交易默认线下面交。

不需要数据库即可执行单元测试（持久化层测试用 H2 内存库）：

```bash
mvn test
```

如果终端提示 `mvn: command not found`，在本机 macOS + IntelliJ IDEA 环境中，可以使用 IntelliJ IDEA 自带的 Maven：

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" test
```

如果要真实运行整套服务，需要先准备 MySQL 和 Nacos：

```bash
# 1. 启动 MySQL（Docker），并建库建表
docker run -d --name campus-mysql -e MYSQL_ROOT_PASSWORD=campus1234 -p 3306:3306 mysql:8.0
docker exec -i campus-mysql mysql -uroot -pcampus1234 < docs/sql/schema.sql

# 2. 启动 Nacos（服务注册发现）后，依次启动各服务
#    campus-user / campus-product / campus-order / campus-message / campus-ai / campus-gateway
```

各服务默认数据源为 `localhost:3306`、账号 `root`、密码 `campus1234`，可在各自的 `application.yml` 中调整。手动体验下单链路：先注册买家和卖家，再用卖家的 `sellerId` 发布商品，最后用同一个 `sellerId` 和商品 `productId` 创建订单。

## 接口文档

除下面的 Markdown 文档外，5 个 servlet 服务（user/product/order/ai/message）已接入 **Knife4j**，启动后可直接访问交互式接口文档：`http://localhost:<服务端口>/doc.html`（例如用户服务 `http://localhost:8081/doc.html`），OpenAPI JSON 为 `/v3/api-docs`。

- [campus-gateway routes](docs/api/gateway-routes.md)：记录当前网关路由表，包括 `/user/**`、`/product/**`、`/order/**`、`/ai/**` 和 `/message/**` 到各服务的转发关系。当前网关只做路由转发。
- [campus-user API](docs/api/user-service.md)：记录当前用户服务骨架接口，包括注册、登录和用户资料查询示例。当前用户数据仅保存在内存中，登录 token 为 mock-only。
- [campus-ai API](docs/api/ai-service.md)：记录当前 AI mock 服务接口，包括描述优化、分类预测和内容检查示例。当前 AI provider 为确定性 mock-only 实现，不调用外部模型。
- [campus-product API](docs/api/product-service.md)：记录当前商品服务第一阶段接口，包括商品发布、列表筛选、详情查询和状态更新示例。当前商品数据仅保存在内存中。
- [campus-order API](docs/api/order-service.md)：记录当前订单服务接口，包括基于 OpenFeign 的订单创建校验、详情查询、买家/卖家订单列表、取消和完成示例。当前订单数据仅保存在内存中，不包含在线支付。
- [campus-message API](docs/api/message-service.md)：记录当前留言服务接口，包括基于 OpenFeign 的留言发布校验、按商品查询、隐藏和删除示例。当前留言数据仅保存在内存中。

## 功能阶段开发方式

项目按功能阶段推进，而不是长期停留在一次只改一个微小文件的节奏。每个阶段围绕一个清晰的服务能力或基础设施能力展开，尽量同时补齐可运行代码、必要测试、接口文档和开发记录。

当前已完成的早期阶段包括：

1. 项目规划、Maven 多模块骨架和公共响应结构。
2. `campus-user` 注册、登录、用户资料查询 mock 接口及对应测试和 API 文档。
3. `campus-ai` 描述优化、分类预测、内容检查 mock 接口及对应测试和 API 文档。
4. `campus-product` 商品发布、列表筛选、详情查询、状态更新内存版接口及 API 文档。
5. `campus-order` 订单创建、详情查询、买家/卖家列表、取消、完成内存版接口及 API 文档。
6. `campus-gateway` 基础路由表验证与网关路由文档。
7. `campus-order` 通过 OpenFeign 调用 `campus-user` 和 `campus-product` 完成订单创建前校验。
8. `campus-product` 商品发布通过 OpenFeign 调用 `campus-ai` 完成内容安全检查。
9. `campus-message` 留言发布、按商品查询、隐藏、删除接口，通过 OpenFeign 校验发送者和商品。
10. `campus-common` 全局异常处理（自动配置，servlet 服务统一兜底，gateway 安全跳过）。
11. `campus-user`、`campus-product`、`campus-order`、`campus-message` 接入 MyBatis Plus + MySQL（每服务独立库），内存存储替换为数据库。
12. `campus-gateway` JWT 鉴权（登录/注册放行，其余校验 Bearer token），登录由 `campus-user` 签发 JWT，`JwtUtil` 在 `campus-common` 共享。
13. 5 个 servlet 服务接入 Knife4j 交互式接口文档（`/doc.html`），基于 springdoc 自动从 controller 生成。
14. `campus-user`、`campus-gateway` 接入 Nacos 配置中心，共享 `jwt.secret`（`campus-shared.yaml`），见 [docs/nacos/README.md](docs/nacos/README.md)。
15. `campus-product` 接入 Sentinel 流量控制（`GET /product` QPS 限流 + 统一 429 响应 + 控制台监控），见 [docs/sentinel/README.md](docs/sentinel/README.md)。
16. `campus-user` 登出 + Redis token 黑名单，`campus-gateway` 校验时查黑名单，已登出 token 即时失效，见 [docs/redis/README.md](docs/redis/README.md)。

## 开发原则

- 每个功能阶段都要有清晰边界，避免把无关服务混在一起修改。
- 重要阶段收尾时同步 README、dev-log、API 文档或 architecture。
- 每次提交保持聚焦、清晰、可回滚。
- 先保证项目能运行，再扩展完整功能。
- AI 服务第一版只使用确定性 mock provider。
- 不引入过度复杂的中间件和抽象。
