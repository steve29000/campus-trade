# Nacos 配置中心

`campus-user` 和 `campus-gateway` 通过 Spring Cloud Alibaba Nacos Config 从配置中心导入
共享配置 `campus-shared.yaml`（当前放 `jwt.secret` / `jwt.expiration`），两个服务共用同一份
JWT 配置。

服务侧通过 `spring.config.import: "optional:nacos:campus-shared.yaml"` 导入；`optional:`
保证 Nacos 不可达时不阻断启动。为了方便本地学习和演示，`campus-user` 与
`campus-gateway` 的 `application.yml` 也提供了开发环境 JWT 兜底值；如果 Nacos
或环境变量提供同名配置，会覆盖本地默认值。

## 前置条件

Nacos 已启动（与服务注册发现是同一个 Nacos，默认 `localhost:8848`）。本地用 Docker：

```bash
docker run -d --name campus-nacos -p 8848:8848 -p 9848:9848 \
  -e MODE=standalone nacos/nacos-server:v2.3.2
```

## 推送共享配置到 Nacos

配置内容见 [`campus-shared.yaml`](campus-shared.yaml)。推送（dataId 必须是 `campus-shared.yaml`，group 用 `DEFAULT_GROUP`）：

```bash
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" \
  --data-urlencode "dataId=campus-shared.yaml" \
  --data-urlencode "group=DEFAULT_GROUP" \
  --data-urlencode "type=yaml" \
  --data-urlencode "content=$(cat docs/nacos/campus-shared.yaml)"
```

也可以打开 Nacos 控制台 `http://localhost:8848/nacos`（默认账号/密码 `nacos`/`nacos`）→
配置管理 → 新建配置，手动粘贴内容。

## 验证

```bash
# 读回配置
curl "http://localhost:8848/nacos/v1/cs/configs?dataId=campus-shared.yaml&group=DEFAULT_GROUP"
```

启动 `campus-user` 和 `campus-gateway` 后，日志会出现
`[Nacos Config] Listening config: dataId=campus-shared.yaml`，说明已成功从配置中心加载。

## 说明

- 本地兜底 JWT secret 只适合开发环境。正式演示或部署时，建议通过 Nacos 或
  `JWT_SECRET` 环境变量提供更强的密钥，并保持 `campus-user` 与 `campus-gateway`
  使用同一份配置。
- 测试不依赖 Nacos：`campus-user` 测试用 `src/test/resources/application.yml` 自带的 `jwt.secret`；
  `campus-gateway` 的 `@SpringBootTest` 用 `properties` 注入测试用 `jwt.secret` 并关闭 Nacos config。
- 后续可把各服务的数据源、MyBatis、Sentinel 规则等也迁入 Nacos，实现配置集中管理与动态刷新。
