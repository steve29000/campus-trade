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
