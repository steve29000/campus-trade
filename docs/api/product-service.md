# campus-product API

This document describes the current `campus-product` first-stage API. It is intended for local course-project demos and early integration notes before Swagger/Knife4j is added.

## Current Phase Notes

- Product data is stored in memory inside the running `campus-product` process only.
- Published products are lost when the service restarts.
- This stage focuses on product publishing, browsing, detail lookup, and status updates before MyBatis Plus/MySQL persistence is added.
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

Creates an in-memory product listing. A new product starts with status `ON_SALE`.

### Request

```json
{
  "sellerId": 1,
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

Missing seller id:

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

## List Products

`GET /product`

Returns products from the current in-memory store.

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

Returns one product from the current in-memory store.

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

Updates a product status in the current in-memory store.

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

### Example Error Response

Unknown product id:

```json
{
  "code": 404,
  "message": "product not found",
  "data": null
}
```
