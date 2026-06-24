# Order Service Stage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first usable `campus-order` feature stage with in-memory order creation, detail lookup, buyer/seller order lists, cancellation, completion, tests, and public API documentation.

**Architecture:** Keep this stage consistent with `campus-user`, `campus-ai`, and `campus-product`: controller + DTO records + service with in-memory state. Store a lightweight product snapshot in the order request instead of calling other services yet; real OpenFeign validation is a later integration stage.

**Tech Stack:** Java 17, Spring Boot, Maven multi-module, JUnit 5, MockMvc, shared `campus-common` `ApiResponse` envelope.

---

## Scope

### In Scope

- Implement `campus-order` DTOs, status enum, service, controller, and tests.
- Support order creation, detail lookup, buyer list, seller list, cancellation, and completion.
- Use deterministic in-memory storage with `ConcurrentHashMap` and `AtomicLong`.
- Add `docs/api/order-service.md` with examples matching current behavior.
- Update `README.md`, `docs/dev-log.md`, and `docs/architecture.md`.
- Keep this branch focused on order service plus directly related documentation.

### Out of Scope

- No online payment. Campus second-hand transactions remain offline face-to-face.
- No MySQL, MyBatis Plus mapper, Redis, or persistence migration in this stage.
- No real OpenFeign calls to `campus-user` or `campus-product` yet.
- No automatic product status update in `campus-product` yet.
- No frontend code.

## Order Model

```java
public record OrderResponse(
        Long id,
        Long buyerId,
        Long sellerId,
        Long productId,
        String productTitle,
        BigDecimal price,
        OrderStatus status
) {
}
```

Supported statuses:

```java
public enum OrderStatus {
    CREATED,
    CANCELLED,
    COMPLETED
}
```

## API Contract

### Create Order

- Method: `POST /order`
- Request body:

```json
{
  "buyerId": 2,
  "sellerId": 1,
  "productId": 10,
  "productTitle": "iPad Air",
  "price": 2800
}
```

- Success response: `ApiResponse<OrderResponse>` with status `CREATED`.
- Validation:
  - missing `buyerId`: `BAD_REQUEST`, `buyer id is required`
  - missing `sellerId`: `BAD_REQUEST`, `seller id is required`
  - buyer equals seller: `BAD_REQUEST`, `buyer and seller cannot be the same`
  - missing `productId`: `BAD_REQUEST`, `product id is required`
  - blank `productTitle`: `BAD_REQUEST`, `product title is required`
  - missing or negative `price`: `BAD_REQUEST`, `price must be greater than or equal to 0`

### Order Detail

- Method: `GET /order/{id}`
- Missing order: `NOT_FOUND`, `order not found`

### Buyer Orders

- Method: `GET /order/buyer/{buyerId}`
- Missing `buyerId`: `BAD_REQUEST`, `buyer id is required`
- Response: `ApiResponse<List<OrderResponse>>`, sorted by order id ascending.

### Seller Orders

- Method: `GET /order/seller/{sellerId}`
- Missing `sellerId`: `BAD_REQUEST`, `seller id is required`
- Response: `ApiResponse<List<OrderResponse>>`, sorted by order id ascending.

### Cancel Order

- Method: `PUT /order/{id}/cancel`
- Success status: `CANCELLED`
- Missing order: `NOT_FOUND`, `order not found`
- Already completed order: `BAD_REQUEST`, `completed order cannot be cancelled`

### Complete Order

- Method: `PUT /order/{id}/complete`
- Success status: `COMPLETED`
- Missing order: `NOT_FOUND`, `order not found`
- Cancelled order: `BAD_REQUEST`, `cancelled order cannot be completed`

## File Plan

### Create

- `campus-order/src/main/java/com/campustrade/order/dto/OrderCreateRequest.java`
- `campus-order/src/main/java/com/campustrade/order/dto/OrderResponse.java`
- `campus-order/src/main/java/com/campustrade/order/enums/OrderStatus.java`
- `campus-order/src/main/java/com/campustrade/order/service/OrderService.java`
- `campus-order/src/main/java/com/campustrade/order/controller/OrderController.java`
- `campus-order/src/test/java/com/campustrade/order/service/OrderServiceTest.java`
- `campus-order/src/test/java/com/campustrade/order/controller/OrderControllerTest.java`
- `campus-order/src/test/java/com/campustrade/order/controller/OrderControllerWebTest.java`
- `docs/api/order-service.md`

### Modify

- `campus-order/pom.xml`
- `README.md`
- `docs/dev-log.md`
- `docs/architecture.md`

## Task 1: Order Domain and Service

**Files:**

- Create: `campus-order/src/main/java/com/campustrade/order/dto/OrderCreateRequest.java`
- Create: `campus-order/src/main/java/com/campustrade/order/dto/OrderResponse.java`
- Create: `campus-order/src/main/java/com/campustrade/order/enums/OrderStatus.java`
- Create: `campus-order/src/main/java/com/campustrade/order/service/OrderService.java`
- Create: `campus-order/src/test/java/com/campustrade/order/service/OrderServiceTest.java`
- Modify: `campus-order/pom.xml`

- [x] Add `spring-boot-starter-test` test dependency to `campus-order/pom.xml`.
- [x] Write service tests for create success, required field validation, same buyer/seller rejection, detail not found, buyer list, seller list, cancel success, cancel completed rejection, complete success, and complete cancelled rejection.
- [x] Implement DTO records and `OrderStatus` enum.
- [x] Implement `OrderService` with `AtomicLong`, `ConcurrentHashMap`, immutable response records, and validation helpers.
- [x] Run:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -pl campus-order -am test
```

- [x] Expected: `campus-order` service tests pass.

## Task 2: Order Controller and Route Tests

**Files:**

- Create: `campus-order/src/main/java/com/campustrade/order/controller/OrderController.java`
- Create: `campus-order/src/test/java/com/campustrade/order/controller/OrderControllerTest.java`
- Create: `campus-order/src/test/java/com/campustrade/order/controller/OrderControllerWebTest.java`
- Modify if needed: files from Task 1.

- [x] Write direct controller delegation tests for create, detail, buyer list, seller list, cancel, and complete.
- [x] Write MockMvc tests for JSON body binding, path variable binding, and response serialization.
- [x] Implement `OrderController` endpoints:
  - `POST /order`
  - `GET /order/{id}`
  - `GET /order/buyer/{buyerId}`
  - `GET /order/seller/{sellerId}`
  - `PUT /order/{id}/cancel`
  - `PUT /order/{id}/complete`
- [x] Use explicit annotation names such as `@PathVariable("id")` to avoid parameter-name reflection issues.
- [x] Run:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -pl campus-order -am test
```

- [x] Expected: order service, controller, and MockMvc tests pass.

## Task 3: Order API Documentation and Project Docs

**Files:**

- Create: `docs/api/order-service.md`
- Modify: `README.md`
- Modify: `docs/dev-log.md`
- Modify: `docs/architecture.md`

- [x] Document all six current order endpoints with request and response examples.
- [x] Clearly state that order data is in-memory only in this stage.
- [x] Clearly state that this stage does not implement online payment.
- [x] Update README interface docs section with a `campus-order API` link.
- [x] Update README current completed stages to include `campus-order`.
- [x] Update architecture order-service section to mention current in-memory stage and future OpenFeign/MyBatis Plus integration.
- [x] Update dev-log with what was implemented and learned.

## Task 4: Integration Review and Verification

**Files:**

- Review all files changed in this stage.

- [x] Verify no Codex/private `.project-memory` files are staged.
- [x] Run full reactor:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" test
```

- [x] Expected:
  - `campus-user` tests pass.
  - `campus-product` tests pass.
  - `campus-order` tests pass.
  - `campus-ai` tests pass.
  - Reactor ends with `BUILD SUCCESS`.
- [x] Commit:

```bash
git add campus-order README.md docs/api/order-service.md docs/dev-log.md docs/architecture.md docs/plans/2026-06-22-order-service-stage.md
git commit -m "feat: add order service first stage"
```

- [x] Push:

```bash
git push -u origin feature/order-service
```

## Self-Review

- Spec coverage: create, detail, buyer list, seller list, cancel, complete, tests, API docs, README, architecture, and dev-log are all represented in tasks.
- Placeholder scan: no task depends on unspecified TODO behavior.
- Type consistency: `OrderStatus`, `OrderCreateRequest`, and `OrderResponse` names are used consistently across the plan.
