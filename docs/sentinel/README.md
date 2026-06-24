# Sentinel 限流

`campus-product` 接入 Sentinel 做流量控制。当前对商品列表接口 `GET /product`（资源名即 URL
路径 `/product`）配置了 QPS 限流（3 次/秒），超过阈值的请求由 `SentinelBlockHandler` 统一拦截，
返回 HTTP 429 + 统一 `ApiResponse` 结构：

```json
{"code":429,"message":"请求过于频繁，请稍后再试","data":null}
```

限流规则当前在代码里通过 `SentinelFlowConfig` 加载（便于演示和复现），后续可改为从 Nacos
数据源动态下发，配合控制台动态调整。

## Sentinel 控制台（可选，用于监控和动态规则）

控制台用 Docker 运行（已在本机启动，容器名 `campus-sentinel`）：

```bash
docker run -d --name campus-sentinel -p 8858:8858 -p 8719:8719 bladex/sentinel-dashboard:1.8.8
```

- 控制台地址：`http://localhost:8858`，默认账号/密码 `sentinel` / `sentinel`。
- `campus-product` 通过 `spring.cloud.sentinel.transport.dashboard=localhost:8858` + `eager: true`
  在启动时连上控制台；服务有流量后即可在控制台「实时监控 / 簇点链路」看到 `/product` 资源。
- 控制台未启动时只产生连接警告，不影响代码里配置的限流本身。

## 验证限流

启动 `campus-product`（需 MySQL 和 Nacos）后，1 秒内并发打 15 次：

```bash
for i in $(seq 1 15); do curl -s -o /dev/null -w "%{http_code} " http://localhost:8082/product & done; wait
```

预期：约 3 个返回 `200`，其余返回 `429`（被限流）。

## 扩展到其他服务

其他 servlet 服务接入方式相同：加 `spring-cloud-starter-alibaba-sentinel` 依赖、配置控制台地址、
按需定义资源的流控/熔断规则即可。
