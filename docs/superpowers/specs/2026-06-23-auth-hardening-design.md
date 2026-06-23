# CampusTrade AI Auth Hardening Design

## Goal

Improve the current JWT and resource authorization flow so the project is safer to demo and easier to explain as a portfolio microservice project.

This design covers one feature branch: `codex/auth-hardening`.

## Current Problems

The gateway authenticates JWT tokens and forwards `X-User-Id` to downstream services, but several services still trust user ids from request bodies or path variables. A logged-in user can therefore attempt operations for another user by changing fields such as `sellerId`, `buyerId`, or `senderId`.

The gateway whitelist also uses prefix matching for `/user/login` and `/user/register`, which can accidentally expose future endpoints with similar prefixes.

The user and gateway services load `jwt.secret` from Nacos through an optional config import. If Nacos config is not pushed locally, startup can fail because `JwtUtil` is not registered.

## Scope

In scope:

- Make gateway public-path matching exact.
- Strip any client-supplied `X-User-Id` before adding the verified JWT user id.
- Add local-development fallback JWT configuration for `campus-user` and `campus-gateway`.
- Make product publish and direct status update use the authenticated user from `X-User-Id`.
- Make order creation, listing, cancellation, and completion enforce the authenticated user.
- Make message creation, hiding, and deletion enforce the authenticated user.
- Add regression tests for the above behavior.
- Update project docs and development notes.

Out of scope:

- Distributed transactions between order and product services.
- Admin roles.
- Online payment.
- Real large model provider integration.
- Full frontend integration.

## Architecture

The gateway remains the authentication boundary. It validates JWT, checks Redis token revocation, removes any inbound `X-User-Id`, and forwards a trusted `X-User-Id` derived from the token.

Downstream services become authorization boundaries for their own resources. They read `X-User-Id` from controllers and pass it into service methods. Services compare the authenticated user id with the resource owner or participant before mutating data.

Request DTOs may keep legacy id fields for compatibility in tests or internal calls, but public controller paths should prefer the authenticated user id. For example, product publish should set `sellerId` from `X-User-Id` instead of trusting the request body.

## Service Rules

### Gateway

- `/user/login` and `/user/register` are public exact paths.
- Similar paths such as `/user/login-extra` require a token.
- Incoming `X-User-Id` is removed before forwarding.
- A valid token adds exactly one trusted `X-User-Id` header.

### User and Gateway JWT Config

- Local dev has a non-production fallback secret.
- Nacos `campus-shared.yaml` can override the local fallback.
- Docs must state that production deployments should provide a stronger secret through Nacos or environment variables.

### Product Service

- `POST /product` reads `X-User-Id` and uses it as `sellerId`.
- `PUT /product/{id}/status` allows only the product seller to update status.
- Internal service calls still need a path for order service to mark products sold or on sale. For this branch, the existing endpoint remains, but direct gateway access must be protected by owner checks. A later branch can split internal and public product status APIs.

### Order Service

- `POST /order` reads `X-User-Id` and uses it as `buyerId`.
- A buyer cannot create an order for their own product.
- Buyer and seller order lists only return data for the authenticated user.
- `cancel` is allowed for buyer or seller.
- `complete` is allowed for buyer or seller.

### Message Service

- `POST /message` reads `X-User-Id` and uses it as `senderId`.
- `hide` and `delete` are allowed only for the message sender.

## Testing Strategy

Tests should be added before implementation changes:

- Gateway test for whitelist exact matching.
- Gateway test that a forged inbound `X-User-Id` is replaced.
- Product controller/service test proving request `sellerId` is ignored in favor of authenticated user id.
- Product status update test rejecting a non-owner.
- Order create/list/cancel/complete tests for user authorization.
- Message create/hide/delete tests for user authorization.
- A lightweight context/config test or unit assertion for local JWT fallback.

Run the full Maven test suite before commit:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" test
```

## Documentation Updates

Update:

- `README.md`
- `docs/architecture.md`
- `docs/dev-log.md`
- API docs for product, order, and message if request semantics change.
- Development workflow or SOP notes if a reusable auth rule emerges.

## Risks

Some Feign calls currently reuse public endpoints. Tightening owner checks may break order-to-product status updates if the same endpoint is used for both public and internal flows. If that happens, introduce a small internal endpoint or internal header in the same branch, document it clearly, and keep it simple for the course project.

Cross-service consistency remains a known limitation after this branch. It should be handled in a separate order consistency branch.
