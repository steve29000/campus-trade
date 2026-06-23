# Auth Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make CampusTrade AI trust JWT-derived user identity instead of request-supplied user ids, while keeping local startup practical.

**Architecture:** The gateway authenticates tokens and forwards a cleaned, trusted `X-User-Id` header. Each service enforces ownership or participation before mutating resources. Nacos remains the preferred shared config source, with local fallback values for development.

**Tech Stack:** Java 17, Spring Boot, Spring Cloud Gateway, Spring Cloud Alibaba, JUnit 5, Mockito, MyBatis Plus, Redis test mocks.

---

## File Map

- `campus-gateway/src/main/java/com/campustrade/gateway/filter/JwtAuthFilter.java`: exact whitelist matching and `X-User-Id` cleanup.
- `campus-gateway/src/test/java/com/campustrade/gateway/filter/JwtAuthFilterTest.java`: gateway auth regression tests.
- `campus-gateway/src/main/resources/application.yml`: local JWT fallback config.
- `campus-user/src/main/resources/application.yml`: local JWT fallback config.
- `campus-product/src/main/java/com/campustrade/product/controller/ProductController.java`: read authenticated user header.
- `campus-product/src/main/java/com/campustrade/product/service/ProductService.java`: enforce seller ownership.
- `campus-product/src/test/java/com/campustrade/product/service/ProductServiceTest.java`: product authorization tests.
- `campus-order/src/main/java/com/campustrade/order/controller/OrderController.java`: read authenticated user header.
- `campus-order/src/main/java/com/campustrade/order/service/OrderService.java`: enforce buyer/seller authorization.
- `campus-order/src/test/java/com/campustrade/order/service/OrderServiceTest.java`: order authorization tests.
- `campus-message/src/main/java/com/campustrade/message/controller/MessageController.java`: read authenticated user header.
- `campus-message/src/main/java/com/campustrade/message/service/MessageService.java`: enforce sender authorization.
- `campus-message/src/test/java/com/campustrade/message/service/MessageServiceTest.java`: message authorization tests.
- `README.md`, `docs/architecture.md`, `docs/dev-log.md`: documentation updates.

## Task 1: Gateway JWT Boundary

**Files:**
- Modify: `campus-gateway/src/main/java/com/campustrade/gateway/filter/JwtAuthFilter.java`
- Modify: `campus-gateway/src/test/java/com/campustrade/gateway/filter/JwtAuthFilterTest.java`

- [ ] **Step 1: Write failing whitelist test**

Add a test proving `/user/login-extra` is not public:

```java
@Test
void similarLoginPrefixRequiresToken() {
    ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/user/login-extra").build()
    );

    StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    verify(chain, never()).filter(any());
}
```

- [ ] **Step 2: Run gateway test and verify it fails**

Run:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -pl campus-gateway -Dtest=JwtAuthFilterTest#similarLoginPrefixRequiresToken test
```

Expected: FAIL because the current `startsWith` whitelist lets the path through.

- [ ] **Step 3: Write failing forged header test**

Add a test proving an inbound `X-User-Id` is replaced by the JWT user id:

```java
@Test
void validTokenReplacesForgedUserIdHeader() {
    String token = jwtUtil.generateToken(7L);
    when(redisTemplate.hasKey(JwtUtil.BLOCKLIST_PREFIX + token)).thenReturn(Mono.just(false));
    when(chain.filter(any())).thenReturn(Mono.empty());

    ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/product")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header("X-User-Id", "999")
                    .build()
    );

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
    verify(chain).filter(captor.capture());
    assertThat(captor.getValue().getRequest().getHeaders().get("X-User-Id")).containsExactly("7");
}
```

- [ ] **Step 4: Run forged header test and verify it fails**

Run:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -pl campus-gateway -Dtest=JwtAuthFilterTest#validTokenReplacesForgedUserIdHeader test
```

Expected: FAIL because the current mutation appends another `X-User-Id` header instead of removing the forged one.

- [ ] **Step 5: Implement gateway fix**

Change `isWhitelisted` to exact matching and mutate the request by removing inbound `X-User-Id` before setting the trusted value:

```java
private boolean isWhitelisted(String path) {
    return WHITELIST.contains(path);
}
```

Use a request mutation equivalent to:

```java
.request(builder -> builder.headers(headers -> headers.remove("X-User-Id"))
        .header("X-User-Id", String.valueOf(userId)))
```

- [ ] **Step 6: Run gateway tests**

Run:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -pl campus-gateway test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add campus-gateway/src/main/java/com/campustrade/gateway/filter/JwtAuthFilter.java campus-gateway/src/test/java/com/campustrade/gateway/filter/JwtAuthFilterTest.java
git commit -m "fix: harden gateway jwt identity forwarding"
```

## Task 2: Local JWT Config Fallback

**Files:**
- Modify: `campus-gateway/src/main/resources/application.yml`
- Modify: `campus-user/src/main/resources/application.yml`
- Modify: `docs/nacos/README.md`

- [ ] **Step 1: Add local fallback config**

Add local-development JWT defaults to both service YAML files:

```yaml
jwt:
  secret: ${JWT_SECRET:campus-trade-local-dev-secret-change-before-production-0123456789}
  expiration: ${JWT_EXPIRATION:86400000}
```

- [ ] **Step 2: Keep Nacos override behavior**

Leave `spring.config.import: "optional:nacos:campus-shared.yaml"` in place so Nacos can override local values.

- [ ] **Step 3: Update Nacos docs**

State that local fallback is for development only and production/demo shared config should be pushed through Nacos or environment variables.

- [ ] **Step 4: Run user and gateway tests**

Run:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -pl campus-user,campus-gateway test
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add campus-gateway/src/main/resources/application.yml campus-user/src/main/resources/application.yml docs/nacos/README.md
git commit -m "chore: add local jwt config fallback"
```

## Task 3: Product Ownership Authorization

**Files:**
- Modify: `campus-product/src/main/java/com/campustrade/product/controller/ProductController.java`
- Modify: `campus-product/src/main/java/com/campustrade/product/service/ProductService.java`
- Modify: `campus-product/src/test/java/com/campustrade/product/service/ProductServiceTest.java`

- [ ] **Step 1: Write failing publish identity test**

Add a service or controller test proving authenticated user id becomes `sellerId`, even if the request contains a different seller id.

- [ ] **Step 2: Write failing non-owner status test**

Add a test where product seller is `10L` and authenticated user is `99L`; updating status returns `FORBIDDEN`.

- [ ] **Step 3: Run product tests and verify failures**

Run:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -pl campus-product test
```

Expected: FAIL on the new authorization tests.

- [ ] **Step 4: Implement controller and service signatures**

Controller methods should read:

```java
@RequestHeader("X-User-Id") Long userId
```

Service methods should accept `authenticatedUserId` and use it for ownership checks.

- [ ] **Step 5: Run product tests**

Run:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -pl campus-product test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add campus-product/src/main/java/com/campustrade/product/controller/ProductController.java campus-product/src/main/java/com/campustrade/product/service/ProductService.java campus-product/src/test/java/com/campustrade/product/service/ProductServiceTest.java
git commit -m "feat: enforce product ownership authorization"
```

## Task 4: Order Participant Authorization

**Files:**
- Modify: `campus-order/src/main/java/com/campustrade/order/controller/OrderController.java`
- Modify: `campus-order/src/main/java/com/campustrade/order/service/OrderService.java`
- Modify: `campus-order/src/test/java/com/campustrade/order/service/OrderServiceTest.java`

- [ ] **Step 1: Write failing create identity test**

Add a test proving order creation uses authenticated user id as buyer id and rejects buying the authenticated user's own product.

- [ ] **Step 2: Write failing list authorization tests**

Add tests proving `/buyer/{buyerId}` and `/seller/{sellerId}` return `FORBIDDEN` when path id differs from authenticated user id.

- [ ] **Step 3: Write failing mutation authorization tests**

Add tests proving only buyer or seller can cancel or complete an order.

- [ ] **Step 4: Run order tests and verify failures**

Run:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -pl campus-order test
```

Expected: FAIL on the new authorization tests.

- [ ] **Step 5: Implement authenticated user checks**

Pass `authenticatedUserId` from controller to service. Return `ApiResponse.fail(ResultCode.FORBIDDEN, "...")` when the current user is not allowed.

- [ ] **Step 6: Run order tests**

Run:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -pl campus-order test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add campus-order/src/main/java/com/campustrade/order/controller/OrderController.java campus-order/src/main/java/com/campustrade/order/service/OrderService.java campus-order/src/test/java/com/campustrade/order/service/OrderServiceTest.java
git commit -m "feat: enforce order participant authorization"
```

## Task 5: Message Sender Authorization

**Files:**
- Modify: `campus-message/src/main/java/com/campustrade/message/controller/MessageController.java`
- Modify: `campus-message/src/main/java/com/campustrade/message/service/MessageService.java`
- Modify: `campus-message/src/test/java/com/campustrade/message/service/MessageServiceTest.java`

- [ ] **Step 1: Write failing post identity test**

Add a test proving message post uses authenticated user id as sender id.

- [ ] **Step 2: Write failing hide/delete ownership tests**

Add tests proving a user who is not the sender gets `FORBIDDEN` for hide and delete.

- [ ] **Step 3: Run message tests and verify failures**

Run:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -pl campus-message test
```

Expected: FAIL on the new authorization tests.

- [ ] **Step 4: Implement sender checks**

Pass `authenticatedUserId` from controller to service. Store that id on new messages and compare it with existing `senderId` for hide/delete.

- [ ] **Step 5: Run message tests**

Run:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -pl campus-message test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add campus-message/src/main/java/com/campustrade/message/controller/MessageController.java campus-message/src/main/java/com/campustrade/message/service/MessageService.java campus-message/src/test/java/com/campustrade/message/service/MessageServiceTest.java
git commit -m "feat: enforce message sender authorization"
```

## Task 6: Documentation and Full Verification

**Files:**
- Modify: `README.md`
- Modify: `docs/architecture.md`
- Modify: `docs/dev-log.md`
- Modify as needed: `docs/api/product-service.md`
- Modify as needed: `docs/api/order-service.md`
- Modify as needed: `docs/api/message-service.md`

- [ ] **Step 1: Update documentation**

Document that gateway authenticates and services authorize resource ownership through `X-User-Id`.

- [ ] **Step 2: Run full tests**

Run:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" test
```

Expected: reactor `BUILD SUCCESS`.

- [ ] **Step 3: Check git diff**

Run:

```bash
git diff --stat
git status --short
```

Expected: only auth hardening and docs files changed.

- [ ] **Step 4: Commit**

```bash
git add README.md docs/architecture.md docs/dev-log.md docs/api/product-service.md docs/api/order-service.md docs/api/message-service.md
git commit -m "docs: document auth hardening rules"
```

## Self-Review

- Spec coverage: Gateway whitelist, header cleanup, JWT fallback, product authorization, order authorization, message authorization, tests, and docs are all mapped to tasks.
- Placeholder scan: No task depends on an unspecified future implementation. The plan intentionally leaves exact test placement flexible only where existing test class shape should be preserved.
- Type consistency: All code references use existing Java service/controller patterns and `ApiResponse` / `ResultCode` conventions already present in the project.
