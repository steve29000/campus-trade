# 本地联调指南（前端 ↔ 后端）

前端 `campus-web` 默认走内存 mock，可独立运行。要和真实后端联调，按下面三步走。

> 后端代码不在本分支，在后端分支（如 `backend/m2-gaps`）。需要单独检出 / 启动。

## 1. 起中间件（本目录）

```bash
docker compose -f deploy/docker-compose.yml up -d
```

启动并初始化：

- **MySQL 8**（`localhost:3306`，root / `campus1234`）—— 首次启动用 `mysql/init/01-schema.sql` 自动建库建表：`campus_user_db`、`campus_product_db`、`campus_order_db`、`campus_message_db`。
- **Redis 7**（`localhost:6379`）—— 网关 JWT 黑名单用。
- **Nacos 2.3.2**（`localhost:8848`，standalone）—— 服务注册发现 + 配置中心。

> `01-schema.sql` 镜像自后端 `docs/sql/schema.sql`。若改库结构，`docker compose down -v` 清卷后重建。

## 2. 起后端服务（后端分支）

在后端分支根目录，按顺序起（各服务 `application.yml` 已指向 `localhost` 的 MySQL/Redis/Nacos）：

```bash
mvn -pl campus-user    spring-boot:run
mvn -pl campus-product spring-boot:run
mvn -pl campus-order   spring-boot:run
mvn -pl campus-ai      spring-boot:run
mvn -pl campus-message spring-boot:run
mvn -pl campus-gateway spring-boot:run   # 网关 :8080，最后起
```

网关 `JwtAuthFilter` 放行 `/user/login`、`/user/register`，其余请求需带 `Authorization: Bearer <token>`，校验后注入 `X-User-Id` 给下游。

## 3. 前端指向网关

```bash
cd campus-web
cp .env.example .env.local      # 取消注释 VITE_API_BASE=http://localhost:8080
npm install
npm run dev
```

设了 `VITE_API_BASE` 后，**登录 / 注册 / 商品浏览 / 发布 / 改状态** 走真实后端；收藏 / 分类 / 认证 / 聊天 / AI / 后台仍走 mock。

### 验证

1. 打开前端 → 登录页变为「用户名 + 密码」，演示按钮隐藏。
2. 注册一个账号（自动登录拿 JWT）→ 首页商品列表来自后端真实数据。
3. DevTools Network 看到请求打 `localhost:8080` 且带 `Authorization` 头。

## 注意

- **CORS**：浏览器直连网关需网关放行前端 origin（`http://localhost:5180`）。若网关未配 CORS，可改用 Vite 代理（在 `campus-web/vite.config.ts` 加 `server.proxy` 把 `/user`、`/product` 等转发到 `:8080`）。
- **鉴权范围**：后端除登录/注册外全部需要 token，因此 HTTP 模式下需先登录才能浏览商品（mock 模式可游客浏览）。
- **契约降级**：后端 `ProductResponse` 较精简，前端列表里成色/校区/图片/卖家昵称为占位默认值（详见 `campus-web/src/api/adapters.ts`）。
