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

## 2026-06-19：公共基础结构

### 本次完成

- 为 `campus-common` 增加简单的 `ResultCode` 枚举，用于统一常见成功和失败状态。
- 让 `ApiResponse` 可以直接使用结果码生成成功或失败响应。
- 让 `BusinessException` 可以携带结果码，同时保留直接传入数字 code 的简单构造方式。

### 学到的内容

- 公共模块适合放多个服务都会用到的返回结构和异常约定。
- 统一结果码能减少各个服务自己随意写 code 和 message 的情况。
- 公共模块不能变成杂物间，当前阶段只放响应、结果码和业务异常最容易解释、也最实用。

## 2026-06-19：Maven 构建验证

### 本次完成

- 确认当前终端没有全局 `mvn` 命令。
- 找到 IntelliJ IDEA 自带的 Maven 3.9.11。
- 使用 IntelliJ IDEA Maven 成功执行多模块 `test` 生命周期。

### 验证结果

- `campus-common`：SUCCESS
- `campus-gateway`：SUCCESS
- `campus-user`：SUCCESS
- `campus-product`：SUCCESS
- `campus-order`：SUCCESS
- `campus-ai`：SUCCESS
- `campus-message`：SUCCESS

### 学到的内容

- Maven 不一定要全局安装，IDE 也可能自带可用 Maven。
- Maven 会把依赖下载到本机 `~/.m2/repository` 缓存目录。
- 后续每个小功能完成后，都应该优先跑 Maven 验证，而不是只靠单文件 `javac`。

## 2026-06-19：用户服务接口骨架

### 本次完成

- 为 `campus-user` 增加注册、登录和用户资料查询三个基础接口。
- 增加 `UserRegisterRequest`、`UserLoginRequest`、`UserProfileResponse` 和 `LoginResponse` 四个 DTO。
- 增加简单的内存版 `UserService`，用于演示 controller-service 分层。
- 登录接口暂时返回 mock token，后续可以替换为 JWT。
- 重复用户名返回 `CONFLICT` 结果码，并在密码相关 DTO 的 `toString()` 中隐藏密码。

### 学到的内容

- DTO 可以让接口入参和返回结果更清楚，避免直接暴露内部存储对象。
- 在课程项目早期，内存实现能先验证接口结构，再逐步替换成数据库实现。
- 骨架阶段先用简单 `if` 判断做参数检查，不急着引入额外依赖。
- 即使是 mock 代码，也要标注明文密码只是临时方案，避免后续误用。

## 2026-06-19：用户服务单元测试

### 本次完成

- 为 `campus-user` 增加测试依赖，方便使用 JUnit 5 编写单元测试。
- 为内存版 `UserService` 增加注册、重复用户名、登录成功、密码错误和资料查询测试。
- 补充 DTO `toString()` 的密码隐藏测试，避免日志输出明文密码。

### 学到的内容

- 服务层单元测试可以直接 new 真实对象，不需要启动 Spring 容器。
- 测试应该验证接口返回结果，而不是依赖内部 Map 的实现细节。
- 在引入数据库前先固定当前服务行为，后续重构会更安心。

## 2026-06-19：用户服务 API 文档

### 本次完成

- 新增 `docs/api/user-service.md`，记录当前 `campus-user` 骨架接口。
- 为注册、登录和用户资料查询接口补充请求与响应 JSON 示例。
- 在 README 中增加用户服务 API 文档入口。
- 明确当前用户数据只保存在内存中，登录 token 只是 mock-only。

### 学到的内容

- API 文档应该先描述当前已经实现的行为，避免把后续计划误写成现状。
- 统一响应结构让接口示例更容易阅读，也方便后续接 Swagger/Knife4j。
- 把 mock token 和内存存储写清楚，可以减少读者对 JWT、数据库状态的误解。
