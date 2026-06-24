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

## 2026-06-24：前后端联调打通 + 打磨

### 本次完成

- **联调全链路验证通过**：本机起 MySQL/Redis/Nacos（已有）+ 后端 user/product/gateway（JDK 17，IntelliJ 自带 Maven）。curl 与浏览器双验证：注册 → 登录拿真实 JWT → 无 token 访问商品 **401** → 带 token **200 + 真实 DB 数据** → 前端 UI 登录后首页渲染真实后端商品。
- **联调管线固化**：`vite.config.ts` 内置 `/api` dev 代理（`/api/** → :8080`），浏览器同源绕开网关未配的 CORS；`VITE_API_BASE=/api` 设为推荐，`.env.example`、`deploy/README.md` 同步更新。
- **DTO 适配打磨**：`api/adapters.ts` 把后端 `category` 中文名映射到本地 `categoryId`，真实商品显示正确分类图标（机械键盘→📱、生活用品→🛋️），无匹配回退 📦。
- **HTTP 模式登录守卫**：后端除登录/注册外全部鉴权，新增路由守卫——联调模式未登录访问任意页自动跳登录；`http.ts` 收到 401 统一 `clearToken()`。Mock 模式不受影响（守卫按 `useHttp` 开关）。

### 学到的内容

- 网关 `JwtAuthFilter` 把登录/注册精确放行、其余强制 `Authorization: Bearer`，校验后注入 `X-User-Id`；前端只管存登录拿到的 token、每次请求带上即可。
- 网关没配 CORS 时，浏览器直连会被预检拦（OPTIONS 也走鉴权返回 403）；用 dev server 的 `/api` 代理同源转发是最省事的联调方式。
- 用 `docker exec mysql -e "INSERT ... 中文"` 插数据要带 `--default-character-set=utf8mb4`，否则会乱码；App 走 JDBC（URL 带 `characterEncoding=UTF-8`）则正常。

### 下一步计划

- 后端补齐 收藏 / 分类 / 认证 / 聊天 域，前端把对应 mock 方法切到真实接口。
- 列表卖家昵称目前是 `sellerId` 兜底（后端 `ProductResponse` 不含卖家），后续后端补卖家摘要或前端按需查 `/user/{id}`。
