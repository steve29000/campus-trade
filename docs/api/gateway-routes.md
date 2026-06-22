# campus-gateway Routes

This document describes the current `campus-gateway` route table. It is intended for local course-project demos and early integration notes before Swagger/Knife4j and gateway security policies are added.

## Current Phase Notes

- `campus-gateway` performs route forwarding and JWT authentication.
- Routes use Spring Cloud Gateway `Path` predicates and `lb://` target URIs so requests can be forwarded through Nacos service discovery.
- The gateway includes Spring Cloud LoadBalancer support for `lb://` routes.
- A global `JwtAuthFilter` whitelists `/user/login` and `/user/register`; all other requests must send `Authorization: Bearer <token>` or receive HTTP 401. On success the gateway adds an `X-User-Id` header for downstream services.
- Custom CORS rules, Sentinel rate limiting/fallbacks, and path rewriting are still planned later stages.

## Route Table

| Public Path | Service ID | Target URI |
| --- | --- | --- |
| `/user/**` | `campus-user` | `lb://campus-user` |
| `/product/**` | `campus-product` | `lb://campus-product` |
| `/order/**` | `campus-order` | `lb://campus-order` |
| `/ai/**` | `campus-ai` | `lb://campus-ai` |
| `/message/**` | `campus-message` | `lb://campus-message` |

## Forwarding Behavior

The current gateway keeps the incoming path unchanged when forwarding to the target service. For example, a client request to:

```text
GET /product/1
```

is routed to the `campus-product` service through:

```text
lb://campus-product
```

with the original `/product/1` path still present.

Because path rewriting is not part of this stage, downstream services should continue to expose endpoints with their service prefix, such as `/user`, `/product`, `/order`, and `/ai`.

## Later Gateway Stages

JWT authentication and user identity propagation (`X-User-Id`) are now in place. Later gateway work can add the remaining cross-cutting concerns:

- Project-specific CORS customization for frontend integration.
- Sentinel rate limiting, fallback, and resilience rules.
- Path rewriting if service-internal endpoint prefixes are changed.
- Token revocation / refresh (would introduce Redis).
