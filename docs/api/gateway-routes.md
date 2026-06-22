# campus-gateway Routes

This document describes the current `campus-gateway` route table. It is intended for local course-project demos and early integration notes before Swagger/Knife4j and gateway security policies are added.

## Current Phase Notes

- `campus-gateway` currently performs route forwarding only.
- Routes use Spring Cloud Gateway `Path` predicates and `lb://` target URIs so requests can be forwarded through Nacos service discovery.
- The gateway includes Spring Cloud LoadBalancer support for `lb://` routes.
- The gateway does not currently apply JWT authentication, custom CORS rules, Sentinel rate limiting/fallbacks, or path rewriting.
- JWT auth, CORS customization, Sentinel integration, and path rewriting are planned later stages.

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

Later gateway work can add cross-cutting platform concerns after route forwarding is stable:

- JWT authentication and user identity propagation.
- Project-specific CORS customization for frontend integration.
- Sentinel rate limiting, fallback, and resilience rules.
- Path rewriting if service-internal endpoint prefixes are changed.
