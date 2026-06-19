# campus-user API

This document describes the current `campus-user` skeleton API. It is intended for local course-project demos and early integration notes before Swagger/Knife4j is added.

## Current Phase Notes

- User data is stored in memory inside the running `campus-user` process.
- Registered users are lost when the service restarts.
- Password storage and validation are mock-only in this phase: the current demo stores plain text passwords in memory and compares them directly.
- Login returns a mock token such as `mock-token-user-1`; it is not a JWT and should not be used as a real authentication token.
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

Checks the in-memory username and password, then returns a mock token and the user profile.

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
    "token": "mock-token-user-1",
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
