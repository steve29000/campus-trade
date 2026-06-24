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

- 用户注册、登录、JWT 鉴权
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
| campus-web | 学生端 + 管理后台前端（Vue 3），见下方说明 |

## 前端（campus-web）

面向高校学生的 Web 前端 MVP，遵循"逻辑优先、页面其次"：先定三张流程图 + 数据模型 + 状态机（见 `docs/frontend-mvp.md`），再长出页面。

- 技术栈：Vue 3 + Vite + TypeScript + Pinia + Vue Router。
- 已跑通四个闭环：浏览（登录/认证/首页/搜索/详情/收藏）、发布（3 步 + AI 估价/文案 + 状态管理）、沟通（聊天 + 约交易 + 标记成交）、后台（概览/用户/商品/认证/举报）。
- 默认用内存 Mock 数据，返回与 `campus-common` 一致的 `ApiResponse` 信封；可独立运行。
- 支持 mock ↔ 真后端切换：设 `VITE_API_BASE` 后，登录/注册/商品浏览/发布走真实网关，其余域仍 mock（适配层见 `campus-web/src/api/`）。

```bash
cd campus-web && npm install && npm run dev   # http://localhost:5180（默认 mock）
```

### 前后端联调

一键起中间件（MySQL/Redis/Nacos）+ 初始化 SQL，再起后端服务、前端指向网关，详见 **[`deploy/README.md`](deploy/README.md)**：

```bash
docker compose -f deploy/docker-compose.yml up -d        # 中间件
# 后端分支起各服务 + gateway(:8080)，再：
cd campus-web && cp .env.example .env.local && npm run dev # 取消注释 VITE_API_BASE
```

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

当前阶段只包含 Maven 多模块骨架和最小 Spring Boot 启动类，可以先执行：

```bash
mvn test
```

后续接入 Nacos 后，再分别启动各服务模块。

## 第一阶段 10 个小 commit 计划

1. `docs: 初始化项目说明文档`
   - 创建 `README.md`、`AGENTS.md`、`docs/dev-log.md`、`docs/architecture.md`、`docs/prompts/`。
2. `chore: 初始化 Maven 父工程`
   - 创建根 `pom.xml`，锁定 Java 17、Spring Boot、Spring Cloud Alibaba 版本管理。
3. `chore: 添加 common 公共模块`
   - 创建 `campus-common`，添加统一响应结构和基础异常占位。
4. `chore: 添加 gateway 服务模块`
   - 创建 `campus-gateway`，添加启动类和基础配置。
5. `chore: 添加核心业务服务模块`
   - 创建 `campus-user`、`campus-product`、`campus-order` 的基础启动类和配置。
6. `chore: 添加 AI 服务模块`
   - 创建 `campus-ai`，只保留启动类和 mock AI 能力规划。
7. `feat: 接入 Nacos 服务发现基础配置`
   - 为 gateway、user、product、order、ai 添加 Nacos Discovery 配置。
8. `feat: 添加 gateway 基础路由`
   - 配置 gateway 转发到 user、product、order、ai 服务。
9. `feat: 添加 OpenFeign 示例调用`
   - 选择 product 调用 ai 或 order 调用 product，完成一个最小服务间调用示例。
10. `docs: 更新第一阶段开发记录和架构说明`
    - 更新 `docs/dev-log.md` 和 `docs/architecture.md`，记录已完成内容、运行方式和下一阶段计划。

## 开发原则

- 每次只做一个小任务。
- 每次修改都同步 README、dev-log 或 architecture。
- 每次提交保持小步、清晰、可回滚。
- 先保证项目能运行，再扩展完整功能。
- AI 服务第一版只使用 mock provider。
- 不引入过度复杂的中间件和抽象。
