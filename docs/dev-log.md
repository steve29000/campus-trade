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

## 2026-06-19：AI mock 服务

### 本次完成

- 为 `campus-ai` 增加描述优化、分类预测和内容检查三组 DTO。
- 增加 `AiProvider` 接口和确定性的 `MockAiProvider` 实现。
- 增加 `AiController`，提供 `POST /ai/description/optimize`、`POST /ai/category/predict` 和 `POST /ai/content/check` 三个接口。
- 三个接口统一返回 `ApiResponse`，缺少必要字段时返回 `BAD_REQUEST`。
- 为 mock provider 增加单元测试，覆盖描述优化、分类预测和内容检查的核心规则。

### 学到的内容

- 先抽出 provider 接口，可以让 controller 的服务契约先稳定下来。
- mock AI 结果需要保持确定性，这样课堂演示和自动化测试都更可靠。
- 当前阶段不接真实模型、不保存数据，可以把重点放在接口边界和模块职责上。

## 2026-06-22：AI 服务阶段收尾文档

### 本次完成

- 新增 `docs/api/ai-service.md`，记录当前 `campus-ai` 的三个 mock 接口。
- 为描述优化、分类预测和内容检查补充请求、成功响应、参数校验失败和内容安全失败示例。
- 在 README 的接口文档入口中补充 AI 服务 API 文档链接。
- 将 README 的开发方式从早期“10 个小 commit 计划”调整为按功能阶段推进，更贴合当前 user 和 AI 服务的阶段式开发节奏。

### 学到的内容

- 功能阶段收尾不只是写完代码，还应该补齐测试、API 示例、README 入口和开发日志。
- API 文档应该准确描述当前实现，尤其要说明 `code` 是响应体里的应用级 code，不等同于 HTTP 状态码映射。
- mock AI 服务必须明确写出 mock-only、确定性、不调用外部模型，避免读者误以为已经接入真实大模型。
- 内容安全检查的“未通过”属于 mock 检查结果，当前通过 `data.passed = false` 表达；参数缺失才是接口校验失败。

## 2026-06-22：商品服务第一阶段

### 本次完成

- 为 `campus-product` 梳理第一阶段接口：商品发布、商品列表筛选、商品详情查询和商品状态更新。
- 明确商品状态为 `ON_SALE`、`OFF_SALE` 和 `SOLD`，方便后续订单和上下架流程复用。
- 新增 `docs/api/product-service.md`，记录四个商品接口的请求、响应、筛选规则和校验失败示例。
- 在 README 中补充商品服务 API 文档入口，并把 `campus-product` 加入当前已完成阶段。
- 更新架构文档，说明商品服务当前使用内存数据，后续再接入 MyBatis Plus 和 MySQL。

### 学到的内容

- 商品服务是交易链路的核心边界，先固定发布、浏览、详情和状态四类接口，有助于后续订单服务接入。
- 第一阶段使用内存存储可以先验证接口契约和状态流转，不必过早引入数据库迁移复杂度。
- API 文档需要把校验消息写得足够精确，避免前端或后续服务联调时出现错误提示不一致。
- MockMvc 路由测试可以发现直接调用 Controller 测不到的问题，例如 `@PathVariable` 和 `@RequestParam` 在缺少参数名编译信息时需要显式声明名称。

## 2026-06-22：订单服务第一阶段

### 本次完成

- 为 `campus-order` 梳理第一阶段接口：订单创建、订单详情查询、买家订单列表、卖家订单列表、取消订单和完成订单。
- 明确订单状态为 `CREATED`、`CANCELLED` 和 `COMPLETED`，并固定取消、完成两个状态流转的错误提示。
- 新增 `docs/api/order-service.md`，记录六个订单接口的请求、响应、内存存储说明和校验失败示例。
- 在 README 中补充订单服务 API 文档入口，并把 `campus-order` 加入当前已完成阶段。
- 更新架构文档，说明订单服务当前使用内存数据和请求里的商品快照，后续再接入 OpenFeign、MyBatis Plus 和 MySQL。
- 明确当前阶段不实现在线支付，校园二手交易仍保持线下面交。

### 学到的内容

- 订单服务连接买家、卖家和商品，第一阶段先用商品快照固定接口契约，可以避免过早引入跨服务调用复杂度。
- 状态流转需要把业务限制写清楚，例如已完成订单不能取消、已取消订单不能完成，这样后续前端和服务联调更容易保持一致。
- 线下面交和无在线支付属于产品边界，也应该写进 API 文档和架构说明，避免读者误以为订单阶段已经包含支付能力。
- 内存实现适合当前课程项目的阶段演示，但后续接入 MyBatis Plus 后需要重新考虑订单编号、并发更新和跨服务一致性。

## 2026-06-22：Gateway 路由阶段

### 本次完成

- 梳理 `campus-gateway` 当前路由表，确认 `/user/**`、`/product/**`、`/order/**`、`/ai/**` 和 `/message/**` 分别转发到对应的 `lb://` 服务地址。
- 新增 `docs/api/gateway-routes.md`，记录公共访问路径、服务 ID 和目标 URI。
- 在 README 中补充网关路由文档入口，并把 gateway 路由验证加入已完成阶段。
- 更新架构文档中的 `campus-gateway` 说明，明确当前网关只做路由转发。
- 明确 JWT 鉴权、CORS 自定义、Sentinel 和路径重写都属于后续阶段，避免把未完成能力写成当前能力。

### 学到的内容

- API 网关可以先作为统一入口和路由层落地，再逐步承载认证、跨域、限流等横切能力。
- `lb://service-id` 路由能把网关和具体实例地址解耦，更适合后续接入 Nacos Discovery。
- `lb://` 路由除了写配置，还需要 Spring Cloud LoadBalancer 运行时支持；测试不能只看 YAML 是否绑定成功。
- 网关文档需要同时写清楚“已经能转发什么”和“暂时不会处理什么”，这样后续联调时边界更明确。

## 2026-06-22：本地运行验证与 Gateway 冒烟测试

### 本次完成

- 使用 Docker Desktop 启动本地 Nacos standalone 容器，验证服务注册发现可用。
- 启动 `campus-user`、`campus-product`、`campus-order`、`campus-ai` 和 `campus-gateway` 五个服务。
- 通过 gateway 跑通用户注册登录、商品发布查询、订单创建查询和 AI mock 接口。
- 修复 `campus-user` 的 `/user/{id}` 路径变量绑定问题。
- 为 `campus-user` 新增 Web 层测试，覆盖注册、登录和用户详情路径绑定。

### 学到的内容

- 直连服务接口通过，不代表 gateway 链路一定通过，微服务项目需要分别验证单服务和网关入口。
- `@PathVariable` 建议显式写变量名，例如 `@PathVariable("id")`，避免编译参数未保留方法参数名时运行期绑定失败。
- 中文 query 参数用 curl 测试时应使用 `--get --data-urlencode`，否则容易因为 URL 编码问题得到异常结果。
- 当前阶段仍使用内存数据，不需要 MySQL 和 Redis；Nacos 是 gateway `lb://` 路由验证的必要依赖。

## 2026-06-22：订单服务 OpenFeign 联调阶段

### 本次完成

- 为 `campus-order` 增加 `UserClient` 和 `ProductClient`，通过 OpenFeign 调用 `campus-user` 和 `campus-product`。
- 调整订单创建请求，只保留 `buyerId`、`sellerId` 和 `productId`。
- 创建订单时校验买家、卖家、商品是否存在，商品是否 `ON_SALE`，以及 `sellerId` 是否匹配商品发布者。
- 订单响应中的 `productTitle` 和 `price` 改为来自商品服务返回的商品快照。
- 为订单服务补充 Feign 联调相关单元测试，并修复 Feign 运行所需的 LoadBalancer 依赖。
- 创建订单成功前会调用商品服务把商品状态改为 `SOLD`，避免同一商品在当前演示流程中重复下单。
- 为 Feign 远程异常增加稳定的 `ApiResponse` 兜底，避免直接暴露框架 500。
- 更新订单 API 文档、README 和架构文档，避免继续把 OpenFeign 写成后续计划。

### 学到的内容

- OpenFeign 使用服务名进行负载均衡时，需要 `spring-cloud-starter-loadbalancer` 支持。
- 服务间调用不应该直接复用其他业务模块的 DTO，可以在调用方模块维护最小 client DTO，降低模块耦合。
- 订单创建不能信任客户端传入的商品标题和价格，应由订单服务调用商品服务生成快照。
- 增加跨服务校验后，测试数据也要符合业务规则，例如订单的 `sellerId` 必须匹配商品发布者。
- Java 包装类型比较要使用 `.equals`，不能用 `Integer == Integer`；本地构造对象和远程 JSON 反序列化对象可能不是同一个引用。

## 2026-06-22：全局异常处理基础

### 本次完成

- 在 `campus-common` 增加 `GlobalExceptionHandler`，统一兜底业务异常、请求体解析失败、路径/参数类型不匹配、Bean Validation 失败和未预期异常。
- 通过 Spring Boot 自动配置（`AutoConfiguration.imports`）注册处理器，servlet MVC 服务无需改启动类即可生效。
- 自动配置使用 `@ConditionalOnClass(DispatcherServlet)` 守卫，`campus-gateway`（WebFlux）自动跳过，不会因 servlet 处理器报错。
- 为 `campus-common` 补充 web 与自动配置的 optional 依赖，并新增基于 MockMvc standalone 的处理器单元测试。
- 更新架构文档，记录公共模块新增的全局异常处理职责。

### 学到的内容

- `campus-common` 的包不在各服务启动类扫描路径下，让公共组件生效的干净做法是 Spring Boot 自动配置，而不是要求每个服务改 `scanBasePackages`。
- 公共模块引入 web 依赖应使用 `optional`，避免把 servlet/web 强加给 WebFlux 的 gateway 模块。
- `@ConditionalOnClass` 能让同一个公共模块在 servlet 和响应式服务里安全共存。
- 全局异常处理要与项目既有约定一致：HTTP 状态保持 200，语义放在响应体 `code`，避免一边返回统一结构、一边又抛框架 500。
- WebFlux 的统一异常处理与 servlet 的 `@RestControllerAdvice` 机制不同，gateway 的响应式兜底需要后续单独设计。

## 2026-06-22：订单取消回滚商品状态

### 本次完成

- 修复订单取消逻辑：取消未完成订单时，通过 `ProductClient` 把商品状态从 `SOLD` 回滚为 `ON_SALE`，让商品可以被再次购买。
- 取消已取消订单做幂等处理，不再重复调用商品服务。
- 商品状态回滚失败或远程异常时返回 `SYSTEM_ERROR`，并保持订单仍为 `CREATED`。
- 补充 4 个订单服务单元测试，覆盖回滚成功、回滚失败、远程异常和幂等取消。
- 更新订单 API 文档说明取消时的商品状态回滚行为。

### 学到的内容

- 跨服务的状态变更要成对考虑：创建订单把商品改成 `SOLD`，取消订单就必须把它改回去，否则会出现“商品被锁死、永远买不了”的数据不一致。
- 远程回滚失败时不能把订单本地状态改掉，否则会出现订单已取消但商品仍是 `SOLD` 的不一致；先确认远程成功再改本地。
- 幂等性在涉及远程副作用的操作里很重要，重复取消不应触发第二次商品回滚。

## 2026-06-22：商品发布接入 campus-ai 内容审核

### 本次完成

- 为 `campus-product` 增加 `AiClient`，通过 OpenFeign 调用 `campus-ai` 的内容检查接口。
- 商品发布在字段校验之后、写入之前调用内容检查：命中违规返回 `FORBIDDEN` 并附带原因，AI 服务异常或返回失败返回 `SYSTEM_ERROR`。
- 在调用方模块维护最小 client DTO（`ContentCheckClientRequest`/`ContentCheckClientResponse`），不直接依赖 AI 模块的 DTO。
- 为 `campus-product` 补充 Feign 运行所需的 `spring-cloud-starter-loadbalancer` 依赖。
- 调整 `ProductService` 为构造注入，并更新相关单元测试，新增内容审核相关用例。
- 更新商品 API 文档、架构文档和工作留痕。

### 学到的内容

- 内容安全检查应在字段校验之后执行，避免对明显非法的请求浪费一次跨服务调用。
- 跨服务的拒绝原因应透传给调用方（这里把 AI 的违规原因放进响应 message），方便前端展示。
- 引入构造注入会破坏 `new Service()` 形式的测试，需要同步更新 service 测试和继承式 Capturing 测试桩的 `super(...)`。
- 商品服务以服务名调用 `campus-ai` 同样依赖 `spring-cloud-starter-loadbalancer`，和订单服务接入 Feign 时遇到的依赖问题一致。

## 2026-06-22：留言服务第一阶段

### 本次完成

- 为 `campus-message` 落地留言发布、按商品查询、隐藏和删除四个接口，留言状态包括 `VISIBLE` 和 `HIDDEN`。
- 发布留言时通过 OpenFeign 调用 `campus-user` 和 `campus-product` 校验发送者和商品是否存在，远程异常返回 `SYSTEM_ERROR`。
- 按商品查询只返回 `VISIBLE` 留言并按 id 升序排列；隐藏做幂等处理，删除按物理移除处理。
- 为 `campus-message` 补充 OpenFeign 和 LoadBalancer 依赖，并在启动类启用 `@EnableFeignClients`。
- 新增 service 层单元测试和 controller 层 MockMvc 路由测试。
- 新增留言服务 API 文档，更新 README 和架构文档。

### 学到的内容

- 留言服务是用户和商品之间的轻量交互入口，第一阶段先固定发布、查询、隐藏、删除四类接口，可以为后续买卖双方沟通打基础。
- 隐藏和删除是两种不同的下线方式：隐藏保留数据只是不展示，删除直接移除，应该在接口语义上区分清楚。
- 复用订单服务的跨服务校验模式（client DTO + try/catch 远程异常 + 统一兜底），可以让新服务快速达到一致的健壮性。
- 列表查询默认过滤掉隐藏内容，避免被隐藏的留言继续出现在商品页。

## 2026-06-22：持久化阶段（MySQL + MyBatis Plus）

### 本次完成

- 为 `campus-user`、`campus-product`、`campus-order`、`campus-message` 接入 MyBatis Plus + MySQL，采用每服务独立库（`campus_user_db`/`campus_product_db`/`campus_order_db`/`campus_message_db`），内存存储全部替换为数据库表。
- 每个服务新增 Entity（`@TableId(IdType.AUTO)` 自增主键）和 Mapper（`BaseMapper`），启动类加 `@MapperScan`，`application.yml` 配置 MySQL 数据源与 MyBatis Plus。
- 枚举统一用全局 `EnumTypeHandler` 按名称存为 VARCHAR；订单表名用 `orders` 规避保留字；商品列表关键词用 `LOWER(...) LIKE` 做大小写不敏感匹配。
- 父 pom 统一管理 mybatis-plus 版本；新增 `docs/sql/schema.sql` 建库建表脚本。
- service 层测试改为 Spring 上下文 + H2（MySQL 兼容模式）跑真实 SQL；带 Feign 的服务用真实 Mapper + 假 client 组装，规避 Feign 默认 `@Primary` bean 冲突。
- 用 Docker 起本地 MySQL 8，跑通整条跨服务链路（注册→发布→下单→留言）并核对四个库真实落库。

### 学到的内容

- JDBC URL 的 `characterEncoding` 要填 Java 字符集名（`UTF-8`），不能填 MySQL 的 `utf8mb4`，否则报 Unsupported character encoding。
- H2 把 `USER` 当保留字，测试库需要 `NON_KEYWORDS=USER` 才能用 `user` 表名。
- Spring Cloud 的 Feign client bean 默认是 `@Primary`，测试里再注册 `@Primary` 假 bean 会冲突；可改用「真实 Mapper + 直接 new service 注入假 client」的方式。
- 数据库化后主键由数据库自增生成，插入后回填到实体；不再依赖应用内的自增计数器。
- 微服务用服务名调用时，注册到 Nacos 的实例 IP 必须可达；机器换网络后旧实例会带着失效 IP 注册，需要重启服务重新注册。

## 2026-06-22：JWT 网关鉴权

### 本次完成

- 在 `campus-common` 增加共享的 `JwtUtil`（HMAC 签名/校验）、`JwtProperties` 和自动配置，user 与 gateway 通过相同 `jwt.secret` 共用同一套 JWT。
- `campus-user` 登录成功改为签发真实 JWT（subject 为用户 id，附带 username），替换原 mock token。
- `campus-gateway` 增加响应式全局过滤器 `JwtAuthFilter`：放行 `/user/login`、`/user/register`，其余请求校验 `Authorization: Bearer <token>`，非法或缺失直接返回 401，校验通过后把用户 id 放进 `X-User-Id` 传给下游。
- 引入 jjwt（父 pom 统一管理版本），为 `JwtUtil` 和网关过滤器补充单元测试。
- 真实运行验证：经网关注册/登录拿 token，受保护路由不带 token 返回 401、带合法 token 返回 200、带伪造 token 返回 401。

### 学到的内容

- 无状态 JWT 不需要 Redis：网关只用共享密钥校验签名和过期即可，token 吊销等场景才需要引入 Redis。
- 网关是 WebFlux，鉴权要用 `GlobalFilter` 响应式写法，拒绝时直接写 `ServerHttpResponse` 的 401 JSON，而不是抛异常。
- 共享密钥要在 user 和 gateway 两处保持一致；密钥写在 `application.yml` 只适合本地，生产应走环境变量或配置中心。
- 父 pom 改了 dependencyManagement 后，必须重新 install 父 pom，否则子模块按服务名引用的依赖会找不到版本。
- 网关鉴权失败返回真实 HTTP 401（与业务接口「200 + body code」约定不同），因为请求在安全边界就被拦截，没有进入具体服务。

## 2026-06-22：Knife4j 接口文档

### 本次完成

- 为 5 个 servlet 服务（user/product/order/ai/message）接入 Knife4j（基于 springdoc-openapi），每个服务提供交互式文档 `/doc.html` 和 OpenAPI JSON `/v3/api-docs`。
- 每个服务增加 `OpenApiConfig`，设置服务专属标题（如「CampusTrade 用户服务 API」）和描述；接口路径由 springdoc 从现有 controller 自动生成。
- 父 pom 统一管理 knife4j 版本。
- 真实运行验证：user 与 ai 的 `/v3/api-docs` 返回正确标题和全部接口路径，`/doc.html` 返回 200。

### 学到的内容

- springdoc 能直接从 controller 自动生成 OpenAPI 文档，无需额外注解即可用；注解只是用来补充更详细的描述。
- 自定义文档标题/版本通过一个 `OpenAPI` bean（`io.swagger.v3.oas.models`）设置即可。
- 网关是 WebFlux，与各 servlet 服务的 knife4j 不同源，统一聚合文档需要单独配置，作为后续项。
- 文档接口（`/doc.html`、`/v3/api-docs`）当前直连各服务访问；若要经网关访问需在网关白名单放行。
