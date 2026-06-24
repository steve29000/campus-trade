# campus-web · 校园集市前端

面向高校学生的校园二手交易平台前端（MVP）。Vue 3 + Vite + TypeScript + Pinia + Vue Router。

> 设计理念：**逻辑优先，页面其次**。先把三张图（购买 / 发布 / 商品状态流转）和数据模型扎稳，
> 再让页面长出来。详见 [`../docs/frontend-mvp.md`](../docs/frontend-mvp.md)。

## 运行

```bash
npm install
npm run dev      # http://localhost:5180
npm run build    # 类型检查 + production 构建
```

首次进入可用「快速体验」一键登录：

- 🦊 **已认证学生（小南 · 南校区）** —— 体验完整闭环
- 🐤 **未认证新生** —— 体验「认证前置」守卫

## 代码分层

```text
src/
  domain/      纯 TS 领域层：实体类型、商品状态机、AI 估价规则、校园交易地点（无 UI 依赖）
  data/        内存 Mock 仓库 + 种子数据 + localStorage 持久化
  api/         API 客户端抽象，返回与后端一致的 ApiResponse 信封，路由对齐 campus-gateway
  stores/      Pinia 状态（auth / catalog / favorites / chat）
  router/      路由 + 登录/认证守卫
  components/  复用组件（商品卡、占位图、底部导航、Toast…）
  pages/       页面（结果层）
```

## 与后端对接

当前数据由 `src/api/client.ts` 的 Mock 实现提供。后端（Spring Cloud Alibaba）就绪后，
把 client 的方法体替换为对 `GATEWAY_BASE + EP.*`（见 `src/api/endpoints.ts`）的 `fetch` 即可，
路由前缀与 `campus-gateway` 完全一致：`/user`、`/product`、`/order`、`/ai`、`/message`。

## MVP 进度

- [x] **P1 浏览闭环**：登录/认证 · 首页 · 分类搜索 · 商品详情 · 收藏
- [ ] **P2 发布闭环**：发布(3步) · AI 估价/文案 · 我的发布(状态管理)
- [ ] **P3 沟通闭环**：消息列表 · 聊天 · 约交易地点 · 标记已售出
- [ ] **P4 后台**：用户 / 商品 / 认证 / 举报管理
