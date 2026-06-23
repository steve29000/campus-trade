# campus-user API

This document describes the current `campus-user` skeleton API. It is intended for local course-project demos and early integration notes before Swagger/Knife4j is added.

## Current Phase Notes

- User data is persisted in MySQL (`campus_user_db.user`) via MyBatis Plus.
- Password storage is still mock-only: plain text is stored and compared directly (hashing is a later stage).
- Login returns a real JWT (HMAC-signed, subject = user id, with a `username` claim). The gateway validates this token for protected routes; `/user/login` and `/user/register` are public.
- Responses use the shared `ApiResponse` envelope:

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

The `code` field is an application-level response body code. The current skeleton does not map these codes to HTTP status codes with `ResponseEntity`, `@ResponseStatus`, or global exception handling yet.

## Register User

`POST /user/register`

Creates an in-memory user and returns the public profile. `username` and `password` are required. If `nickname` is blank, the service uses the username as the nickname.

### Request

```json
{
  "username": "alice",
  "password": "secret",
  "nickname": "Alice"
}
```

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "alice",
    "nickname": "Alice"
  }
}
```

### Example Error Responses

Missing username or password:

```json
{
  "code": 400,
  "message": "username and password are required",
  "data": null
}
```

Duplicate username:

```json
{
  "code": 409,
  "message": "username already exists",
  "data": null
}
```

## Login

`POST /user/login`

Checks the stored username and password, then returns a signed JWT and the user profile.

### Request

```json
{
  "username": "alice",
  "password": "secret"
}
```

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9.<payload>.<signature>",
    "user": {
      "id": 1,
      "username": "alice",
      "nickname": "Alice"
    }
  }
}
```

### Example Error Responses

Missing username or password:

```json
{
  "code": 400,
  "message": "username and password are required",
  "data": null
}
```

Incorrect username or password:

```json
{
  "code": 401,
  "message": "username or password is incorrect",
  "data": null
}
```

## Logout

`POST /user/logout`

Revokes the caller's JWT by adding it to a Redis blocklist (TTL = the token's remaining
lifetime). After logout, the gateway rejects the same token with `401 token has been revoked`.
The request must carry the token (it is not a public route): `Authorization: Bearer <token>`.

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

## Get User Profile

`GET /user/{id}`

Returns the public profile for a user already registered during the current service process lifetime.

### Path Parameters

| Name | Type | Description |
| --- | --- | --- |
| `id` | number | In-memory user id returned by register or login. |

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "alice",
    "nickname": "Alice"
  }
}
```

### Example Error Response

Unknown user id:

```json
{
  "code": 404,
  "message": "user not found",
  "data": null
}
```
