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

当前阶段包含 Maven 多模块骨架、最小 Spring Boot 启动类、`campus-user` 的注册/登录 mock/用户资料查询接口、`campus-ai` 的描述优化/分类预测/内容检查 mock 接口，以及 `campus-product` 的内存版商品发布、浏览、详情查询和状态更新接口。数据库持久化、JWT 鉴权、真实大模型接入和完整网关联调仍在后续阶段实现。可以先执行：

```bash
mvn test
```

如果终端提示 `mvn: command not found`，在本机 macOS + IntelliJ IDEA 环境中，可以先使用 IntelliJ IDEA 自带的 Maven：

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" test
```

后续接入 Nacos 后，再分别启动各服务模块。

## 接口文档

- [campus-user API](docs/api/user-service.md)：记录当前用户服务骨架接口，包括注册、登录和用户资料查询示例。当前用户数据仅保存在内存中，登录 token 为 mock-only。
- [campus-ai API](docs/api/ai-service.md)：记录当前 AI mock 服务接口，包括描述优化、分类预测和内容检查示例。当前 AI provider 为确定性 mock-only 实现，不调用外部模型。
- [campus-product API](docs/api/product-service.md)：记录当前商品服务第一阶段接口，包括商品发布、列表筛选、详情查询和状态更新示例。当前商品数据仅保存在内存中。

## 功能阶段开发方式

项目按功能阶段推进，而不是长期停留在一次只改一个微小文件的节奏。每个阶段围绕一个清晰的服务能力或基础设施能力展开，尽量同时补齐可运行代码、必要测试、接口文档和开发记录。

当前已完成的早期阶段包括：

1. 项目规划、Maven 多模块骨架和公共响应结构。
2. `campus-user` 注册、登录、用户资料查询 mock 接口及对应测试和 API 文档。
3. `campus-ai` 描述优化、分类预测、内容检查 mock 接口及对应测试和 API 文档。
4. `campus-product` 商品发布、列表筛选、详情查询、状态更新内存版接口及 API 文档。

## 开发原则

- 每个功能阶段都要有清晰边界，避免把无关服务混在一起修改。
- 重要阶段收尾时同步 README、dev-log、API 文档或 architecture。
- 每次提交保持聚焦、清晰、可回滚。
- 先保证项目能运行，再扩展完整功能。
- AI 服务第一版只使用确定性 mock provider。
- 不引入过度复杂的中间件和抽象。
