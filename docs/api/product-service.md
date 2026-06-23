# campus-product API

This document describes the current `campus-product` API for local course-project demos. Knife4j is available at `/doc.html`; this Markdown file keeps the learning notes and example payloads readable in GitHub.

## Current Phase Notes

- Product data is persisted in MySQL through MyBatis Plus.
- Write APIs use the trusted `X-User-Id` header forwarded by `campus-gateway`.
- When calling services through the gateway, send `Authorization: Bearer <token>`; when testing a service directly, provide `X-User-Id` manually.
- Supported product status values are `ON_SALE`, `OFF_SALE`, and `SOLD`.
- Responses use the shared `ApiResponse` envelope:

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

The `code` field is an application-level response body code. The current skeleton does not map these codes to HTTP status codes with `ResponseEntity`, `@ResponseStatus`, or global exception handling yet.

## Publish Product

`POST /product`

Creates a product listing. A new product starts with status `ON_SALE`. The seller id is taken from `X-User-Id`; the request body `sellerId` is ignored if present.

Before the product is stored, `campus-product` calls `campus-ai` (`POST /ai/content/check`) with the title and description joined together. If the content fails the mock safety check, the publish is rejected with `FORBIDDEN` and the rejection reason from the AI service. If the AI service is unreachable or returns an error, the publish is rejected with `SYSTEM_ERROR`. Field validation runs first, so a request that fails validation never reaches the AI service.

### Request

```json
{
  "title": "iPad Air",
  "description": "自用一年，功能正常",
  "category": "数码",
  "price": 2800
}
```

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "sellerId": 1,
    "title": "iPad Air",
    "description": "自用一年，功能正常",
    "category": "数码",
    "price": 2800,
    "status": "ON_SALE"
  }
}
```

### Example Validation Failures

Missing authenticated user id:

```json
{
  "code": 400,
  "message": "seller id is required",
  "data": null
}
```

Missing title, description, or category:

```json
{
  "code": 400,
  "message": "title, description and category are required",
  "data": null
}
```

Missing or negative price:

```json
{
  "code": 400,
  "message": "price must be greater than or equal to 0",
  "data": null
}
```

### Example Content Safety Failure

Content that hits a prohibited keyword (returned by `campus-ai`):

```json
{
  "code": 403,
  "message": "content contains prohibited keyword: 枪",
  "data": null
}
```

## List Products

`GET /product`

Returns products from the database.

### Query Parameters

| Name | Type | Description |
| --- | --- | --- |
| `keyword` | string | Optional. Matches title or description, case-insensitive for English text. |
| `category` | string | Optional. Exact match after trimming. |
| `status` | string | Optional. Must be `ON_SALE`, `OFF_SALE`, or `SOLD`. |

### Example Request

```text
GET /product?keyword=iPad&category=数码&status=ON_SALE
```

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "sellerId": 1,
      "title": "iPad Air",
      "description": "自用一年，功能正常",
      "category": "数码",
      "price": 2800,
      "status": "ON_SALE"
    }
  ]
}
```

### Example Validation Failure

Invalid status:

```json
{
  "code": 400,
  "message": "invalid product status",
  "data": null
}
```

## Product Detail

`GET /product/{id}`

Returns one product from the database.

### Path Parameters

| Name | Type | Description |
| --- | --- | --- |
| `id` | number | In-memory product id returned by publish product. |

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "sellerId": 1,
    "title": "iPad Air",
    "description": "自用一年，功能正常",
    "category": "数码",
    "price": 2800,
    "status": "ON_SALE"
  }
}
```

### Example Error Response

Unknown product id:

```json
{
  "code": 404,
  "message": "product not found",
  "data": null
}
```

## Update Product Status

`PUT /product/{id}/status`

Updates a product status. Only the product seller, identified by `X-User-Id`, can update the status.

### Request

```json
{
  "status": "SOLD"
}
```

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "sellerId": 1,
    "title": "iPad Air",
    "description": "自用一年，功能正常",
    "category": "数码",
    "price": 2800,
    "status": "SOLD"
  }
}
```

### Example Validation Failure

Invalid or blank status:

```json
{
  "code": 400,
  "message": "invalid product status",
  "data": null
}
```

Non-seller status update:

```json
{
  "code": 403,
  "message": "only product seller can update status",
  "data": null
}
```

### Example Error Response

Unknown product id:

```json
{
  "code": 404,
  "message": "product not found",
  "data": null
}
```
