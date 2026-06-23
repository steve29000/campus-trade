# campus-order API

This document describes the current `campus-order` API for local course-project demos. Knife4j is available at `/doc.html`; this Markdown file keeps the learning notes and example payloads readable in GitHub.

## Current Phase Notes

- Order data is persisted in MySQL through MyBatis Plus.
- This stage does not implement online payment. Campus second-hand trades remain offline face-to-face transactions.
- Creating an order calls `campus-user` and `campus-product` through OpenFeign.
- The client submits `productId`; `buyerId` comes from trusted `X-User-Id`, and `sellerId`, `productTitle`, and `price` are copied from the product service response.
- After an order is created, `campus-order` calls `campus-product` to update the product status to `SOLD`.
- When calling services through the gateway, send `Authorization: Bearer <token>`; when testing a service directly, provide `X-User-Id` manually.
- Supported order status values are `CREATED`, `CANCELLED`, and `COMPLETED`.
- Responses use the shared `ApiResponse` envelope:

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

The `code` field is an application-level response body code. The current skeleton does not map these codes to HTTP status codes with `ResponseEntity`, `@ResponseStatus`, or global exception handling yet.

## Create Order

`POST /order`

Creates an order after validating buyer, seller, and product through OpenFeign. A new order starts with status `CREATED`.
The product is marked as `SOLD` after successful order creation, so the same product cannot be ordered again in the simple demo flow.

### Request

```json
{
  "productId": 10
}
```

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "buyerId": 2,
    "sellerId": 1,
    "productId": 10,
    "productTitle": "iPad Air",
    "price": 2800,
    "status": "CREATED"
  }
}
```

### Example Validation Failures

Missing authenticated buyer id:

```json
{
  "code": 400,
  "message": "buyer id is required",
  "data": null
}
```

Buyer and seller are the same:

```json
{
  "code": 400,
  "message": "buyer and seller cannot be the same",
  "data": null
}
```

Missing product id:

```json
{
  "code": 400,
  "message": "product id is required",
  "data": null
}
```

Buyer does not exist:

```json
{
  "code": 404,
  "message": "buyer not found",
  "data": null
}
```

Seller does not exist:

```json
{
  "code": 404,
  "message": "seller not found",
  "data": null
}
```

Product does not exist:

```json
{
  "code": 404,
  "message": "product not found",
  "data": null
}
```

Product is not on sale:

```json
{
  "code": 400,
  "message": "product is not on sale",
  "data": null
}
```

Product status update fails:

```json
{
  "code": 500,
  "message": "product status update failed",
  "data": null
}
```

Remote service unavailable:

```json
{
  "code": 500,
  "message": "remote service unavailable",
  "data": null
}
```

## Order Detail

`GET /order/{id}`

Returns one order from the database. Only the buyer or seller can access the order.

### Path Parameters

| Name | Type | Description |
| --- | --- | --- |
| `id` | number | In-memory order id returned by create order. |

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "buyerId": 2,
    "sellerId": 1,
    "productId": 10,
    "productTitle": "iPad Air",
    "price": 2800,
    "status": "CREATED"
  }
}
```

### Example Error Response

Unknown order id:

```json
{
  "code": 404,
  "message": "order not found",
  "data": null
}
```

Authenticated user is not a participant:

```json
{
  "code": 403,
  "message": "only order participants can access this order",
  "data": null
}
```

## Buyer Orders

`GET /order/buyer/{buyerId}`

Returns orders for one buyer from the database, sorted by order id ascending. The path `buyerId` must match `X-User-Id`.

### Path Parameters

| Name | Type | Description |
| --- | --- | --- |
| `buyerId` | number | Buyer id stored on the order. |

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "buyerId": 2,
      "sellerId": 1,
      "productId": 10,
      "productTitle": "iPad Air",
      "price": 2800,
      "status": "CREATED"
    }
  ]
}
```

### Example Validation Failure

Missing buyer id:

```json
{
  "code": 400,
  "message": "buyer id is required",
  "data": null
}
```

Authenticated user is not the buyer:

```json
{
  "code": 403,
  "message": "only the buyer can list buyer orders",
  "data": null
}
```

## Seller Orders

`GET /order/seller/{sellerId}`

Returns orders for one seller from the database, sorted by order id ascending. The path `sellerId` must match `X-User-Id`.

### Path Parameters

| Name | Type | Description |
| --- | --- | --- |
| `sellerId` | number | Seller id stored on the order. |

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "buyerId": 2,
      "sellerId": 1,
      "productId": 10,
      "productTitle": "iPad Air",
      "price": 2800,
      "status": "CREATED"
    }
  ]
}
```

### Example Validation Failure

Missing seller id:

```json
{
  "code": 400,
  "message": "seller id is required",
  "data": null
}
```

Authenticated user is not the seller:

```json
{
  "code": 403,
  "message": "only the seller can list seller orders",
  "data": null
}
```

## Cancel Order

`PUT /order/{id}/cancel`

Cancels an order. Only the buyer or seller can cancel it. A completed order cannot be cancelled.

When a non-cancelled order is cancelled, `campus-order` calls `campus-product` to restore the product status from `SOLD` back to `ON_SALE`, so the product can be ordered again. Cancelling an already-cancelled order is idempotent and does not call the product service again. If the product status restore fails, the cancel returns `SYSTEM_ERROR` and the order stays `CREATED`.

### Path Parameters

| Name | Type | Description |
| --- | --- | --- |
| `id` | number | In-memory order id returned by create order. |

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "buyerId": 2,
    "sellerId": 1,
    "productId": 10,
    "productTitle": "iPad Air",
    "price": 2800,
    "status": "CANCELLED"
  }
}
```

### Example Error Responses

Unknown order id:

```json
{
  "code": 404,
  "message": "order not found",
  "data": null
}
```

Completed order:

```json
{
  "code": 400,
  "message": "completed order cannot be cancelled",
  "data": null
}
```

Authenticated user is not a participant:

```json
{
  "code": 403,
  "message": "only order participants can cancel this order",
  "data": null
}
```

## Complete Order

`PUT /order/{id}/complete`

Completes an order. Only the buyer or seller can complete it. A cancelled order cannot be completed.

### Path Parameters

| Name | Type | Description |
| --- | --- | --- |
| `id` | number | In-memory order id returned by create order. |

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "buyerId": 2,
    "sellerId": 1,
    "productId": 10,
    "productTitle": "iPad Air",
    "price": 2800,
    "status": "COMPLETED"
  }
}
```

### Example Error Responses

Unknown order id:

```json
{
  "code": 404,
  "message": "order not found",
  "data": null
}
```

Cancelled order:

```json
{
  "code": 400,
  "message": "cancelled order cannot be completed",
  "data": null
}
```

Authenticated user is not a participant:

```json
{
  "code": 403,
  "message": "only order participants can complete this order",
  "data": null
}
```
