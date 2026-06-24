# Product Service Stage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first usable `campus-product` feature stage with in-memory product publishing, browsing, detail lookup, status updates, tests, and public API documentation.

**Architecture:** Keep this stage consistent with the existing `campus-user` and `campus-ai` skeletons: controller + DTO records + service with in-memory state. Do not add MyBatis Plus persistence yet; this stage proves service boundaries and API behavior before introducing database complexity.

**Tech Stack:** Java 17, Spring Boot, Maven multi-module, JUnit 5, shared `campus-common` `ApiResponse` envelope.

---

## Scope

### In Scope

- Implement `campus-product` DTOs, service, controller, and tests.
- Support product publish, list query, detail query, and status update.
- Use deterministic in-memory storage with `ConcurrentHashMap` and `AtomicLong`.
- Add `docs/api/product-service.md` with examples matching current behavior.
- Update `README.md`, `docs/dev-log.md`, and `docs/architecture.md`.
- Keep this branch focused on product service plus directly related documentation.

### Out of Scope

- No MySQL, MyBatis Plus mapper, Redis, or persistence migration in this stage.
- No real OpenFeign call to `campus-ai` yet.
- No image upload, favorites, cart, payment, or delivery workflow.
- No frontend code.
- No online payment; campus trades remain offline face-to-face.

## Product Model

Use a simple response model for the first stage:

```java
public record ProductResponse(
        Long id,
        Long sellerId,
        String title,
        String description,
        String category,
        BigDecimal price,
        ProductStatus status
) {
}
```

Supported statuses:

```java
public enum ProductStatus {
    ON_SALE,
    OFF_SALE,
    SOLD
}
```

## API Contract

### Publish Product

- Method: `POST /product`
- Request body:

```json
{
  "sellerId": 1,
  "title": "iPad Air",
  "description": "自用一年，功能正常",
  "category": "数码",
  "price": 2800
}
```

- Success response: `ApiResponse<ProductResponse>` with status `ON_SALE`.
- Validation:
  - missing `sellerId`: `BAD_REQUEST`, `seller id is required`
  - blank `title`, `description`, or `category`: `BAD_REQUEST`, `title, description and category are required`
  - missing or negative `price`: `BAD_REQUEST`, `price must be greater than or equal to 0`

### List Products

- Method: `GET /product`
- Query parameters: `keyword`, `category`, `status`
- Response: `ApiResponse<List<ProductResponse>>`
- Filtering:
  - `keyword` matches title or description, case-insensitive for English text.
  - `category` exact-match after trimming.
  - `status` must be one of `ON_SALE`, `OFF_SALE`, `SOLD`; invalid status returns `BAD_REQUEST`, `invalid product status`.

### Product Detail

- Method: `GET /product/{id}`
- Success response: `ApiResponse<ProductResponse>`
- Missing id in storage returns `NOT_FOUND`, `product not found`.

### Update Product Status

- Method: `PUT /product/{id}/status`
- Request body:

```json
{
  "status": "SOLD"
}
```

- Success response: updated `ProductResponse`.
- Validation:
  - invalid or blank status: `BAD_REQUEST`, `invalid product status`
  - missing product: `NOT_FOUND`, `product not found`

## File Plan

### Create

- `campus-product/src/main/java/com/campustrade/product/dto/ProductCreateRequest.java`
- `campus-product/src/main/java/com/campustrade/product/dto/ProductResponse.java`
- `campus-product/src/main/java/com/campustrade/product/dto/ProductStatusUpdateRequest.java`
- `campus-product/src/main/java/com/campustrade/product/enums/ProductStatus.java`
- `campus-product/src/main/java/com/campustrade/product/service/ProductService.java`
- `campus-product/src/main/java/com/campustrade/product/controller/ProductController.java`
- `campus-product/src/test/java/com/campustrade/product/service/ProductServiceTest.java`
- `campus-product/src/test/java/com/campustrade/product/controller/ProductControllerTest.java`
- `campus-product/src/test/java/com/campustrade/product/controller/ProductControllerWebTest.java`
- `docs/api/product-service.md`

### Modify

- `README.md`
- `docs/dev-log.md`
- `docs/architecture.md`

## Task 1: Product Domain and Service

**Files:**

- Create: `campus-product/src/main/java/com/campustrade/product/dto/ProductCreateRequest.java`
- Create: `campus-product/src/main/java/com/campustrade/product/dto/ProductResponse.java`
- Create: `campus-product/src/main/java/com/campustrade/product/dto/ProductStatusUpdateRequest.java`
- Create: `campus-product/src/main/java/com/campustrade/product/enums/ProductStatus.java`
- Create: `campus-product/src/main/java/com/campustrade/product/service/ProductService.java`
- Create: `campus-product/src/test/java/com/campustrade/product/service/ProductServiceTest.java`

- [x] Write service tests first for publish success, required field validation, negative price validation, detail not found, keyword/category/status listing, invalid status listing, status update success, and status update not found.
- [x] Implement DTO records and `ProductStatus` enum.
- [x] Implement `ProductService` with `AtomicLong`, `ConcurrentHashMap`, immutable response records, and small helper methods for validation/filtering.
- [x] Run:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -pl campus-product -am test
```

- [x] Expected: `campus-product` service tests pass.

## Task 2: Product Controller and Controller Tests

**Files:**

- Create: `campus-product/src/main/java/com/campustrade/product/controller/ProductController.java`
- Create: `campus-product/src/test/java/com/campustrade/product/controller/ProductControllerTest.java`
- Create: `campus-product/src/test/java/com/campustrade/product/controller/ProductControllerWebTest.java`
- Modify if needed: files from Task 1.

- [x] Write controller tests directly against `ProductController` for publish, list, detail, and status update delegation.
- [x] Add MockMvc route smoke tests for JSON body binding, query parameters, path variables, and response serialization.
- [x] Implement `ProductController` endpoints:
  - `POST /product`
  - `GET /product`
  - `GET /product/{id}`
  - `PUT /product/{id}/status`
- [x] Run:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -pl campus-product -am test
```

- [x] Expected: product service and controller tests pass.

## Task 3: Product API Documentation and Project Docs

**Files:**

- Create: `docs/api/product-service.md`
- Modify: `README.md`
- Modify: `docs/dev-log.md`
- Modify: `docs/architecture.md`

- [x] Document the four current product endpoints with request and response examples.
- [x] Clearly state that product data is in-memory only in this stage.
- [x] Update README interface docs section with a `campus-product API` link.
- [x] Update README current completed stages to include `campus-product`.
- [x] Update architecture product-service section to mention current in-memory stage and future MyBatis Plus persistence.
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
  - `campus-ai` tests pass.
  - `campus-product` tests pass.
  - Reactor ends with `BUILD SUCCESS`.
- [x] Commit:

```bash
git add campus-product README.md docs/api/product-service.md docs/dev-log.md docs/architecture.md docs/plans/2026-06-22-product-service-stage.md
git commit -m "feat: add product service first stage"
```

- [x] Push:

```bash
git push -u origin feature/product-service
```

## Self-Review

- Spec coverage: publish, list, detail, status update, tests, API docs, README, architecture, and dev-log are all represented in tasks.
- Placeholder scan: no task depends on unspecified TODO behavior.
- Type consistency: `ProductStatus`, `ProductCreateRequest`, `ProductStatusUpdateRequest`, and `ProductResponse` names are used consistently across the plan.
