# Gateway Routes Stage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `campus-gateway` a tested and documented unified API entry point for the current user, product, order, AI, and optional message services.

**Architecture:** Keep gateway behavior configuration-first in `application.yml` using Spring Cloud Gateway discovery routes with `lb://` service IDs. Do not add JWT, Sentinel, CORS, or custom filters in this stage; this stage proves route definitions and startup wiring.

**Tech Stack:** Java 17, Spring Boot, Spring Cloud Gateway, Spring Cloud Alibaba Nacos Discovery, Maven multi-module, JUnit 5, Spring Boot test support.

---

## Scope

### In Scope

- Keep and verify gateway routes for:
  - `/user/**` -> `lb://campus-user`
  - `/product/**` -> `lb://campus-product`
  - `/order/**` -> `lb://campus-order`
  - `/ai/**` -> `lb://campus-ai`
  - `/message/**` -> `lb://campus-message`
- Add gateway test dependencies.
- Add tests that load the gateway route definitions from `application.yml`.
- Add a lightweight gateway Spring context smoke test with discovery disabled for local testing.
- Add `docs/api/gateway-routes.md`.
- Update `README.md`, `docs/dev-log.md`, and `docs/architecture.md`.

### Out of Scope

- No JWT authentication filter.
- No Sentinel rate limiting or fallback rules.
- No CORS customization.
- No real Nacos server requirement during tests.
- No path rewrite rules.
- No frontend code.

## Gateway Route Contract

| Public Path | Target Service | Target URI |
| --- | --- | --- |
| `/user/**` | `campus-user` | `lb://campus-user` |
| `/product/**` | `campus-product` | `lb://campus-product` |
| `/order/**` | `campus-order` | `lb://campus-order` |
| `/ai/**` | `campus-ai` | `lb://campus-ai` |
| `/message/**` | `campus-message` | `lb://campus-message` |

## File Plan

### Create

- `campus-gateway/src/test/java/com/campustrade/gateway/GatewayRoutesPropertiesTest.java`
- `campus-gateway/src/test/java/com/campustrade/gateway/CampusGatewayApplicationTest.java`
- `campus-gateway/src/test/java/com/campustrade/gateway/GatewayLoadBalancerSupportTest.java`
- `docs/api/gateway-routes.md`

### Modify

- `campus-gateway/pom.xml`
- `campus-gateway/src/main/resources/application.yml` if route config needs cleanup
- `README.md`
- `docs/dev-log.md`
- `docs/architecture.md`

## Task 1: Gateway Tests

**Files:**

- Modify: `campus-gateway/pom.xml`
- Create: `campus-gateway/src/test/java/com/campustrade/gateway/GatewayRoutesPropertiesTest.java`
- Create: `campus-gateway/src/test/java/com/campustrade/gateway/CampusGatewayApplicationTest.java`

- [x] Add `spring-boot-starter-test` test dependency to `campus-gateway/pom.xml`.
- [x] Write `GatewayRoutesPropertiesTest` using `@SpringBootTest(properties = {"spring.cloud.nacos.discovery.enabled=false", "spring.cloud.discovery.enabled=false"})`.
- [x] Autowire `GatewayProperties` and assert that route IDs, URIs, and `Path=` predicates match the route contract.
- [x] Write `CampusGatewayApplicationTest` as a context-load smoke test with Nacos discovery disabled.
- [x] Add Spring Cloud LoadBalancer dependency and assert `ReactiveLoadBalancerClientFilter` is available for `lb://` routes.
- [x] Run:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -pl campus-gateway -am test
```

- [x] Expected: gateway tests pass without requiring local Nacos.

## Task 2: Gateway Documentation

**Files:**

- Create: `docs/api/gateway-routes.md`
- Modify: `README.md`
- Modify: `docs/dev-log.md`
- Modify: `docs/architecture.md`

- [x] Document all current gateway routes with public path, service ID, and target URI.
- [x] State that gateway currently does route forwarding only.
- [x] State that JWT auth, CORS, Sentinel, and path rewriting are later stages.
- [x] Update README current running section and API docs list with a `campus-gateway routes` link.
- [x] Update README completed stages to include gateway route verification.
- [x] Update architecture `campus-gateway` section with current route table and later responsibilities.
- [x] Update dev-log with what was implemented and learned.

## Task 3: Integration Review and Verification

**Files:**

- Review all files changed in this stage.

- [x] Verify no Codex/private `.project-memory` files are staged.
- [x] Run full reactor:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" test
```

- [x] Expected:
  - `campus-gateway` tests pass.
  - `campus-user` tests pass.
  - `campus-product` tests pass.
  - `campus-order` tests pass.
  - `campus-ai` tests pass.
  - Reactor ends with `BUILD SUCCESS`.
- [x] Commit:

```bash
git add campus-gateway README.md docs/api/gateway-routes.md docs/dev-log.md docs/architecture.md docs/plans/2026-06-22-gateway-routes-stage.md
git commit -m "feat: verify gateway routes"
```

- [x] Push:

```bash
git push -u origin feature/gateway-routes
```

## Self-Review

- Spec coverage: route table, test dependency, route config tests, context test, API docs, README, architecture, and dev-log are represented.
- Placeholder scan: no task depends on unspecified behavior.
- Scope check: this is focused on gateway route verification only; auth and resilience remain later stages.
