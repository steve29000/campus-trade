# campus-order API

This document describes the current `campus-order` API. It is intended for local course-project demos and early integration notes before Swagger/Knife4j is added.

## Current Phase Notes

- Order data is stored in memory inside the running `campus-order` process only.
- Created orders are lost when the service restarts.
- This stage does not implement online payment. Campus second-hand trades remain offline face-to-face transactions.
- Creating an order calls `campus-user` and `campus-product` through OpenFeign.
- The client only submits `buyerId`, `sellerId`, and `productId`; `productTitle` and `price` are copied from the product service response.
- After an order is created, `campus-order` calls `campus-product` to update the product status to `SOLD`.
- For local manual testing, register the buyer and seller first, then publish the product with the same `sellerId` used by the order request.
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

Creates an in-memory order after validating buyer, seller, and product through OpenFeign. A new order starts with status `CREATED`.
The product is marked as `SOLD` after successful order creation, so the same product cannot be ordered again in the simple in-memory demo flow.

### Request

```json
{
  "buyerId": 2,
  "sellerId": 1,
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

Missing buyer id:

```json
{
  "code": 400,
  "message": "buyer id is required",
  "data": null
}
```

Missing seller id:

```json
{
  "code": 400,
  "message": "seller id is required",
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

Seller does not match product owner:

```json
{
  "code": 400,
  "message": "seller does not match product owner",
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

Returns one order from the current in-memory store.

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

## Buyer Orders

`GET /order/buyer/{buyerId}`

Returns orders for one buyer from the current in-memory store, sorted by order id ascending.

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

## Seller Orders

`GET /order/seller/{sellerId}`

Returns orders for one seller from the current in-memory store, sorted by order id ascending.

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

## Cancel Order

`PUT /order/{id}/cancel`

Cancels an order in the current in-memory store. A completed order cannot be cancelled.

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

## Complete Order

`PUT /order/{id}/complete`

Completes an order in the current in-memory store. A cancelled order cannot be completed.

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
