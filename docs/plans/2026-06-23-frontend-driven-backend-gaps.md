# 计划：原型驱动的后端缺口补齐（M2）

> 日期：2026-06-23　｜　来源：[PRD](../prd/campus-trade-prd.md) + 桌面原型 [`campus-web-prototype/`](../../campus-web-prototype/index.html) 的 `API_MAP`
> 目标里程碑：M2（在现有后端上补齐"原型需要但后端没有"的能力）

## Goal
原型与 PRD 已把产品逻辑钉成规格。本计划把规格里**后端尚未支持**的点，拆成 5 个有序功能阶段，逐阶段实现（每阶段：代码 + 测试 + 文档），最终让网关接口能支撑原型全部交互。

## 总体顺序与依赖
1. **密码哈希**（campus-user，安全打底，独立）
2. **商品字段补全**：`imageUrl` + `campus` + `sellerId` 筛选 + 分页（campus-product）
3. **昵称透出**：product / message 响应带昵称（依赖能调用 campus-user 的 Feign）
4. **收藏**：`favorite` 表 + 收藏接口（campus-product，依赖阶段 2 的商品响应）
5. **个人资料**：user `avatarUrl` / `campus` + 资料查询/编辑（campus-user）

阶段 1 独立可先做；3、4 依赖 2 的商品 DTO 已定型；5 独立。

## Out of Scope
真实图片上传到对象存储（本轮 `imageUrl` 仅存 URL 字符串）、移动端、真实大模型、在线支付、评价/通知/举报/管理员后台。

---

## 阶段 1 · 密码哈希（campus-user）

**Goal**：注册落库前对密码做 BCrypt 哈希，登录时用 `matches` 校验，去掉明文存储与明文比较。

**现状**：[`UserService.java:40-41`](../../campus-user/src/main/java/com/campustrade/user/service/UserService.java) 注释已标注 `Mock-only: replace plain text storage with password hashing`，注册 `user.setPassword(request.password())` 明文存储，登录 `user.getPassword().equals(request.password())` 明文比较。

**触及文件**
- `campus-user/pom.xml`：引入 `spring-security-crypto`（仅要 `BCryptPasswordEncoder`，无需整套 Spring Security）。
- `campus-user/.../config/`：新增 `PasswordEncoderConfig`，暴露 `PasswordEncoder` Bean。
- `campus-user/.../service/UserService.java`：
  - register：`user.setPassword(encoder.encode(request.password()))`。
  - login：`encoder.matches(request.password(), user.getPassword())`。

**实现要点 / 风险**
- 既有明文 dev 数据无法再登录——重置 `campus_user_db.user` 或重新注册即可（无真实数据）。
- `password` 列已是 `VARCHAR(255)`，容纳 BCrypt 60 字符无需改表。

**测试**：`UserServiceTest` 既有用例改为"注册后库里不等于明文、`matches` 通过"；错误密码仍返回 `UNAUTHORIZED`。

---

## 阶段 2 · 商品字段补全（campus-product）

**Goal**：商品支持图片与面交校区；列表支持按卖家筛选与分页。这是原型商品卡/详情/我的商品/分页的后端基础。

**触及文件**
- `docs/sql/schema.sql`：`product` 表加 `image_url VARCHAR(512) NULL`、`campus VARCHAR(64) NULL`。
- `campus-product/.../entity/ProductEntity.java`：加 `imageUrl`、`campus` 字段 + getter/setter。
- `campus-product/.../dto/ProductCreateRequest`：record 加 `imageUrl`、`campus`（可空）。
- `campus-product/.../dto/ProductResponse`：record 加 `imageUrl`、`campus`。
- `campus-product/.../service/ProductService.java`：
  - `publish` 写入 `imageUrl`/`campus`；`toResponse` 带出。
  - `list(...)` 增加 `sellerId`、`page`、`size` 参数；`sellerId` 非空时 `wrapper.eq(ProductEntity::getSellerId, sellerId)`；用 MyBatis Plus `Page<ProductEntity>` 分页。
- `campus-product/.../controller/ProductController.java`：`list` 增加 `@RequestParam sellerId/page/size`（[现状仅 keyword/category/status](../../campus-product/src/main/java/com/campustrade/product/controller/ProductController.java#L38)）。
- `campus-product/.../config/`：新增 MyBatis Plus `PaginationInnerInterceptor` 配置（分页插件）。

**契约变化（需同步原型/PRD）**
- `GET /product` 响应由 `List<ProductResponse>` 改为分页结构 `{ records, total, page, size }`。原型 `API_MAP.listProducts` 已预留 `page&size`，落地后更新原型渲染与 PRD 页面字段表。

**测试**：`ProductServiceTest` / `ProductControllerWebTest` 增：按 `sellerId` 过滤、分页 `total`/页大小、发布带 `imageUrl`/`campus` 回显。H2 内存库需支持分页插件（MyBatis Plus 通用）。

---

## 阶段 3 · 昵称透出（campus-product / campus-message）

**Goal**：商品列表/详情带 `sellerNickname`、留言带 `senderNickname`，免去前端再逐个查用户。

**触及文件**
- campus-product 当前只有 `AiClient`，**新增 `UserClient`**（OpenFeign），复用 [campus-order 的 UserClient 模式](../../campus-order/src/main/java/com/campustrade/order/client)。
  - `ProductResponse` 加 `sellerNickname`；`toResponse` 时按 `sellerId` 调 `GET /user/{id}` 取昵称。
- campus-message 已有 `UserClient`：`MessageResponse` 加 `senderNickname`，按 `senderId` 取昵称。

**实现要点 / 风险**
- 列表批量取昵称避免 N 次调用：可一次性收集 `sellerId` 去重后查（或先实现逐个、后续优化）。MVP 可接受逐个，注意加 Feign 降级（用户查不到时回退为"用户{id}"）。
- 不在数据库冗余昵称，保持单一数据源在 campus-user。

**测试**：mock `UserClient` 返回昵称，断言响应带 `sellerNickname`/`senderNickname`；用户服务不可用时降级不报错。

---

## 阶段 4 · 收藏（campus-product）

**Goal**：登录用户可收藏/取消收藏商品，并列出我的收藏。落在 campus-product，避免跨库。

**触及文件**
- `docs/sql/schema.sql`：`campus_product_db` 新增
  ```sql
  CREATE TABLE favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_product (user_id, product_id),
    KEY idx_user_id (user_id)
  );
  ```
- campus-product 新增 `FavoriteEntity`、`FavoriteMapper`、`FavoriteService`，并在 `ProductController`（或新增 `FavoriteController`）暴露：
  - `POST /product/{id}/favorite`：`user_id` 取自 `X-User-Id`；幂等（唯一键冲突视为已收藏）。
  - `DELETE /product/{id}/favorite`：取消收藏。
  - `GET /product/favorites`：按 `X-User-Id` 查我的收藏商品列表（join 商品，复用 `ProductResponse`）。

**实现要点**：身份一律取网关透传的 `X-User-Id`，不信任请求体（沿用既有授权加固原则）。

**测试**：收藏→列表含该商品；重复收藏幂等；取消后列表不含；不同用户互不可见。

---

## 阶段 5 · 个人资料（campus-user）

**Goal**：用户有头像与校区，可查看与编辑，支撑原型「我的主页」。

**触及文件**
- `docs/sql/schema.sql`：`user` 表加 `avatar_url VARCHAR(512) NULL`、`campus VARCHAR(64) NULL`。
- `campus-user/.../entity/UserEntity.java`：加 `avatarUrl`、`campus`。
- `campus-user/.../dto/UserProfileResponse`：加 `avatarUrl`、`campus`；`UserService.toProfile` 带出（[现状仅 id/username/nickname](../../campus-user/src/main/java/com/campustrade/user/service/UserService.java#L87)）。
- 注册 `UserRegisterRequest` 可选带 `campus`。
- 新增 `PUT /user/profile`：按 `X-User-Id` 更新 `nickname`/`campus`/`avatarUrl`（`UserController` + `UserService.updateProfile`）。

**测试**：注册带 campus 回显；`GET /user/{id}` 带新字段；`PUT /user/profile` 仅改本人、未登录拒绝。

---

## 验证（每阶段通用）
- 单元/集成测试：`mvn test`（持久化层 H2 内存库，无需真实 DB）。若 `mvn` 不在 PATH，用 IntelliJ 自带：`"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" test`。
- 真实链路（可选）：起 MySQL/Nacos 后过网关手测，对照原型 `API_MAP` 逐项点检。
- 一致性：每阶段落地后回填原型字段/渲染与 PRD「页面字段表」，保持三者同步。

## Learning Notes（实现时补）
- 各阶段完成后在 [docs/dev-log.md](../dev-log.md) 追加一行，并在重要阶段同步 README / architecture / API 文档（项目既有习惯）。
