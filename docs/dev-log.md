# CampusTrade AI 开发日志

本文件用于记录 CampusTrade AI 每个阶段的开发内容、学习收获和遇到的问题。它既是课程总结素材，也方便后续维护 GitHub 作品集。

## 2026-06-19：项目规划初始化

### 本次完成

- 明确项目主题：基于 Spring Cloud Alibaba 的校园二手交易平台。
- 明确项目目标：完成用户、商品、订单、AI 等微服务。
- 明确 AI 功能第一版使用 mock provider。
- 规划 Maven 多模块结构。
- 规划第一阶段小步 commit 路线。
- 编写 README、AGENTS、architecture 和 prompt 模板初稿。
- 创建 Maven 父工程和七个子模块的最小骨架。

### 学到的内容

- 微服务项目需要先划分边界，再写业务代码。
- 课程项目不需要一开始就追求复杂架构，应该先保证能运行。
- AI 功能可以先设计接口和 mock 实现，后续再替换真实模型。
- 文档和代码同步更新，可以让项目更像一个完整作品集。
- Maven 多模块项目适合把公共代码、网关和业务服务分开管理。

### 下一步计划

- 为 campus-common 添加统一响应结构和基础异常。
- 为 gateway、user、product、order、ai 服务补充 Nacos Discovery 配置。
- 添加 gateway 基础路由。
- 确保项目可以通过 Maven 编译。

## 2026-06-23：前端 MVP（campus-web）逻辑优先落地

### 本次完成

- 坚持"逻辑优先，页面其次"：先写 `docs/frontend-mvp.md`（购买 / 发布 / 商品状态流转三张图 + 数据模型 + 状态机），再写页面。
- 新增 `campus-web`（Vue 3 + Vite + TS + Pinia + Router）：
  - `domain/` 纯 TS 领域层：实体类型、商品状态机（唯一真相来源）、规则版 AI 估价/文案、校园交易地点。
  - `data/` 内存 Mock 仓库 + 种子数据（localStorage 持久化），让闭环现在就能跑。
  - `api/` 客户端返回与后端一致的 `ApiResponse` 信封，路由前缀对齐 `campus-gateway`，后端就绪后可整体替换。
- 四个闭环全部跑通并在浏览器验证：
  - P1 浏览：登录/认证守卫、首页（校区/分类/附近/推荐）、分类搜索筛选排序、详情、收藏。
  - P2 发布：3 步发布、AI 估价（¥199→¥130）、AI 文案、我的发布状态流转（在售↔下架、→已售出终态）。
  - P3 沟通：消息列表、聊天（气泡随视角翻转）、校园约交易卡片（提议/确认）、卖家标记成交闭环。
  - P4 后台：数据概览、用户封禁、商品下架/删除、认证审核、举报处理（处理并下架）。

### 学到的内容

- `app.use(router)` 会立即触发首次路由解析与守卫；异步恢复登录态必须在 `app.use(router)` 之前完成，否则受保护路由会误跳登录。
- 状态机集中在 `domain/productStatus.ts`，页面只通过 `transition()` 改状态，避免后端状态被写乱。
- Mock 客户端只要保持后端同款返回信封与路由前缀，前端就能先于后端独立跑通业务闭环。

### 下一步计划

- 后端补齐 user/product/order 业务接口后，把 `campus-web/src/api/client.ts` 的 Mock 方法体替换为 gateway HTTP 调用。
- 接入真实图片上传与真实 AI（替换 `domain/ai.ts` 规则实现）。
