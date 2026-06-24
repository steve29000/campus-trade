# Order Feign Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `campus-order` create orders by validating buyer, seller, and product through OpenFeign calls to `campus-user` and `campus-product`.

**Architecture:** Keep the current in-memory services and `ApiResponse` envelope. Add small Feign client interfaces and client-side DTO records inside `campus-order`, then let `OrderService` build the order snapshot from the product service instead of trusting title and price from the request body.

**Tech Stack:** Java 17, Spring Boot, Spring Cloud OpenFeign, Nacos Discovery, JUnit 5, AssertJ, MockMvc.

---

## File Map

- Modify `campus-order/src/main/java/com/campustrade/order/dto/OrderCreateRequest.java`: keep only `buyerId`, `sellerId`, and `productId` as client input.
- Create `campus-order/src/main/java/com/campustrade/order/client/UserClient.java`: Feign client for `GET /user/{id}`.
- Create `campus-order/src/main/java/com/campustrade/order/client/ProductClient.java`: Feign client for `GET /product/{id}`.
- Create `campus-order/src/main/java/com/campustrade/order/client/dto/UserProfileClientResponse.java`: minimal user profile DTO.
- Create `campus-order/src/main/java/com/campustrade/order/client/dto/ProductClientResponse.java`: minimal product DTO used by order.
- Create `campus-order/src/main/java/com/campustrade/order/client/enums/ProductClientStatus.java`: client-side product status enum.
- Modify `campus-order/src/main/java/com/campustrade/order/service/OrderService.java`: inject clients, validate remote responses, create order from product snapshot.
- Modify `campus-order/src/test/java/com/campustrade/order/service/OrderServiceTest.java`: use fake clients and cover success/failure cases.
- Modify `campus-order/src/test/java/com/campustrade/order/controller/OrderControllerWebTest.java`: update create-order request shape.
- Modify `docs/api/order-service.md`, `docs/architecture.md`, `docs/dev-log.md`, and `README.md`: document current OpenFeign integration.

## Task 1: Add Feign Client Contracts

**Files:**
- Modify: `campus-order/src/main/java/com/campustrade/order/dto/OrderCreateRequest.java`
- Create: `campus-order/src/main/java/com/campustrade/order/client/UserClient.java`
- Create: `campus-order/src/main/java/com/campustrade/order/client/ProductClient.java`
- Create: `campus-order/src/main/java/com/campustrade/order/client/dto/UserProfileClientResponse.java`
- Create: `campus-order/src/main/java/com/campustrade/order/client/dto/ProductClientResponse.java`
- Create: `campus-order/src/main/java/com/campustrade/order/client/enums/ProductClientStatus.java`

- [x] Step 1: Update `OrderCreateRequest` to:

```java
public record OrderCreateRequest(
        Long buyerId,
        Long sellerId,
        Long productId
) {
}
```

- [x] Step 2: Add Feign clients returning `ApiResponse<T>`:

```java
@FeignClient(name = "campus-user")
public interface UserClient {
    @GetMapping("/user/{id}")
    ApiResponse<UserProfileClientResponse> findProfile(@PathVariable("id") Long id);
}

@FeignClient(name = "campus-product")
public interface ProductClient {
    @GetMapping("/product/{id}")
    ApiResponse<ProductClientResponse> findById(@PathVariable("id") Long id);
}
```

- [x] Step 3: Run `mvn -pl campus-order test` and expect compilation failures in existing tests and service code that still use title/price fields.

## Task 2: Refactor Order Service Around Remote Validation

**Files:**
- Modify: `campus-order/src/main/java/com/campustrade/order/service/OrderService.java`
- Modify: `campus-order/src/test/java/com/campustrade/order/service/OrderServiceTest.java`

- [x] Step 1: Write failing service tests for:
  - normal create copies product title and price from product client
  - missing buyer id
  - missing seller id
  - missing product id
  - buyer and seller are the same
  - buyer not found
  - seller not found
  - product not found
  - product status is not `ON_SALE`

- [x] Step 2: Implement constructor injection:

```java
public OrderService(UserClient userClient, ProductClient productClient) {
    this.userClient = userClient;
    this.productClient = productClient;
}
```

- [x] Step 3: In `create`, validate local ids first, then remote responses:
  - missing buyer id -> `buyer id is required`
  - missing seller id -> `seller id is required`
  - same buyer/seller -> `buyer and seller cannot be the same`
  - missing product id -> `product id is required`
  - buyer lookup failure -> `buyer not found`
  - seller lookup failure -> `seller not found`
  - product lookup failure -> `product not found`
  - non-`ON_SALE` product -> `product is not on sale`

- [x] Step 4: Create `OrderResponse` with product title and price from `ProductClientResponse`.

- [x] Step 5: Run `mvn -pl campus-order test`; expect order tests to pass.

## Task 3: Update Controller Tests And Runtime API Shape

**Files:**
- Modify: `campus-order/src/test/java/com/campustrade/order/controller/OrderControllerWebTest.java`
- Modify: `docs/api/order-service.md`

- [x] Step 1: Update MockMvc create-order request body to:

```json
{
  "buyerId": 2,
  "sellerId": 1,
  "productId": 10
}
```

- [x] Step 2: Keep response assertions on `productTitle`, `price`, and `status`, proving the service still returns an order snapshot.

- [x] Step 3: Update API docs to say order no longer accepts `productTitle` or `price` from clients.

- [x] Step 4: Run `mvn -pl campus-order test`; expect controller and service tests to pass.

## Task 4: Full Verification And Documentation

**Files:**
- Modify: `README.md`
- Modify: `docs/architecture.md`
- Modify: `docs/dev-log.md`

- [x] Step 1: Update README current-running section and completed phases to mention order OpenFeign integration.

- [x] Step 2: Update architecture order-service section from “future OpenFeign” to current behavior.

- [x] Step 3: Append a dev-log entry explaining the service-call flow and what was learned.

- [x] Step 4: Run full verification:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" test
```

Expected: reactor `BUILD SUCCESS`.

- [x] Step 5: If Nacos and services are running locally, run gateway smoke test:

```bash
curl -sS -X POST http://localhost:8080/user/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"buyer1","password":"123456","nickname":"Buyer"}'

curl -sS -X POST http://localhost:8080/user/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"seller1","password":"123456","nickname":"Seller"}'

curl -sS -X POST http://localhost:8080/product \
  -H 'Content-Type: application/json' \
  -d '{"sellerId":2,"title":"高数教材","description":"九成新","category":"图书","price":30}'

curl -sS -X POST http://localhost:8080/order \
  -H 'Content-Type: application/json' \
  -d '{"buyerId":1,"sellerId":2,"productId":1}'
```

Expected: order response includes title and price copied from product service.

- [ ] Step 6: Commit:

```bash
git add README.md docs/architecture.md docs/dev-log.md docs/api/order-service.md campus-order
git commit -m "feat: 订单服务接入 OpenFeign 校验"
```

## Self-Review

- Scope is one feature stage: order-service cross-service validation.
- No MySQL, Redis, JWT, payment, or real distributed transactions are included.
- The plan keeps service data in memory to preserve current course-project simplicity.
- Runtime success depends on Nacos plus running `campus-user`, `campus-product`, `campus-order`, and `campus-gateway`.
