# campus-order API

This document describes the current `campus-order` first-stage API. It is intended for local course-project demos and early integration notes before Swagger/Knife4j is added.

## Current Phase Notes

- Order data is stored in memory inside the running `campus-order` process only.
- Created orders are lost when the service restarts.
- This stage does not implement online payment. Campus second-hand trades remain offline face-to-face transactions.
- This stage stores a lightweight product snapshot from the create-order request instead of calling `campus-product` or `campus-user` through OpenFeign.
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

Creates an in-memory order with a product snapshot. A new order starts with status `CREATED`.

### Request

```json
{
  "buyerId": 2,
  "sellerId": 1,
  "productId": 10,
  "productTitle": "iPad Air",
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

Missing product title:

```json
{
  "code": 400,
  "message": "product title is required",
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
