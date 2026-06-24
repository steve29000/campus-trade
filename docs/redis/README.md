# Redis（JWT 登出 / Token 吊销）

JWT 是无状态的，签发后在过期前一直有效。为支持「登出即失效」，引入 Redis 黑名单：

- `campus-user` 的 `POST /user/logout` 把当前 token 写入 Redis 黑名单
  键 `jwt:blocklist:<token>`，TTL 设为该 token 的剩余有效期（到期自动清理）。
- `campus-gateway` 的 `JwtAuthFilter` 在校验签名后，额外用响应式 Redis 查黑名单；
  命中则即使签名有效也返回 401 `token has been revoked`。
- 黑名单键前缀统一定义在 `campus-common` 的 `JwtUtil.BLOCKLIST_PREFIX`，user 与 gateway 共用。

## 启动 Redis

本地用 Docker（已在本机启动，容器名 `campus-redis`）：

```bash
docker run -d --name campus-redis -p 6379:6379 redis:7-alpine
```

`campus-user` 和 `campus-gateway` 默认连接 `localhost:6379`（见各自 `application.yml`
的 `spring.data.redis`）。

## 验证登出吊销

启动 `campus-user`、`campus-gateway`、任一受保护服务（如 `campus-product`）后：

```bash
# 登录拿 token
TOKEN=$(curl -s -X POST http://localhost:8080/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"secret"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['token'])")

# 登出前：带 token 访问受保护接口 → 200
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/product -H "Authorization: Bearer $TOKEN"

# 登出（加入黑名单）
curl -s -X POST http://localhost:8080/user/logout -H "Authorization: Bearer $TOKEN"

# 登出后：同一 token → 401 token has been revoked
curl -s http://localhost:8080/product -H "Authorization: Bearer $TOKEN"

# Redis 里查看黑名单键与 TTL
docker exec campus-redis redis-cli --scan --pattern "jwt:blocklist:*"
```

## 说明

- 黑名单方案存储成本低（只存到期前的已登出 token），且自动过期，无需手动清理。
- 后续可扩展：登录态/在线用户、商品详情缓存、热点数据缓存等都可以用同一个 Redis。
