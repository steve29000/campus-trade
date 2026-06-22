# campus-message API

This document describes the current `campus-message` API. It is intended for local course-project demos and early integration notes before Swagger/Knife4j is added.

## Current Phase Notes

- Message data is stored in memory inside the running `campus-message` process only.
- Created messages are lost when the service restarts.
- Posting a message calls `campus-user` and `campus-product` through OpenFeign to verify the sender and the product exist.
- The sender or product not existing is rejected; the remote service being unavailable returns `SYSTEM_ERROR`.
- Listing messages for a product returns only `VISIBLE` messages, sorted by id ascending.
- Hiding a message keeps it stored but excludes it from the product listing; deleting a message removes it.
- Supported message status values are `VISIBLE` and `HIDDEN`.
- Responses use the shared `ApiResponse` envelope:

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## Post Message

`POST /message`

Creates an in-memory message on a product. A new message starts with status `VISIBLE`.

### Request

```json
{
  "productId": 100,
  "senderId": 2,
  "content": "在吗，这个还在吗？"
}
```

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "productId": 100,
    "senderId": 2,
    "content": "在吗，这个还在吗？",
    "status": "VISIBLE"
  }
}
```

### Example Validation Failures

Missing product id:

```json
{
  "code": 400,
  "message": "product id is required",
  "data": null
}
```

Missing sender id:

```json
{
  "code": 400,
  "message": "sender id is required",
  "data": null
}
```

Blank content:

```json
{
  "code": 400,
  "message": "content is required",
  "data": null
}
```

Sender not found in `campus-user`:

```json
{
  "code": 404,
  "message": "sender not found",
  "data": null
}
```

Product not found in `campus-product`:

```json
{
  "code": 404,
  "message": "product not found",
  "data": null
}
```

## List Messages by Product

`GET /message/product/{productId}`

Returns the `VISIBLE` messages for a product, sorted by id ascending.

### Path Parameters

| Name | Type | Description |
| --- | --- | --- |
| `productId` | number | Product id the messages belong to. |

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "productId": 100,
      "senderId": 2,
      "content": "在吗，这个还在吗？",
      "status": "VISIBLE"
    }
  ]
}
```

## Hide Message

`PUT /message/{id}/hide`

Marks a message as `HIDDEN` so it no longer appears in the product listing. Hiding an already-hidden message is idempotent.

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "productId": 100,
    "senderId": 2,
    "content": "在吗，这个还在吗？",
    "status": "HIDDEN"
  }
}
```

### Example Error Response

```json
{
  "code": 404,
  "message": "message not found",
  "data": null
}
```

## Delete Message

`DELETE /message/{id}`

Removes a message from the in-memory store.

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### Example Error Response

```json
{
  "code": 404,
  "message": "message not found",
  "data": null
}
```
