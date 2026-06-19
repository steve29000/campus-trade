# AI Mock Service

## Goal

Implement the first runnable `campus-ai` mock service with stable endpoints for product description optimization, category prediction, and content checking.

## Scope

- Add request and response DTOs under `campus-ai`.
- Add an `AiProvider` interface and `MockAiProvider` implementation.
- Add an `AiController` with three endpoints:
  - `POST /ai/description/optimize`
  - `POST /ai/category/predict`
  - `POST /ai/content/check`
- Add focused unit tests for mock provider behavior.
- Update `docs/architecture.md` and `docs/dev-log.md`.

## Out of Scope

- Do not call a real large model API.
- Do not add API keys, AI SDKs, billing, network calls, or external services.
- Do not modify product/order/user/gateway behavior.
- Do not implement persistence.
- Do not add frontend code.

## Implementation Notes

- Keep the provider interface small so a real provider can replace the mock later.
- Use deterministic mock rules:
  - Description optimization should return a more polished description based on title/description.
  - Category prediction should return simple categories such as `数码`, `图书`, `生活用品`, `运动户外`, or `其他`.
  - Content check should reject obvious prohibited keywords with a clear reason.
- Use `ApiResponse` and `ResultCode` from `campus-common`.
- Keep code easy to explain for a course project.

## Verification

Run the AI module tests while iterating:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -pl campus-ai -am test
```

Run the full reactor before commit:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" test
```

## Learning Notes

- A mock provider lets the service contract stabilize before real model integration.
- Deterministic mock outputs make tests and classroom demos reliable.
- Keeping provider and controller separate makes future replacement easier.

## Commit Message

`feat: add AI mock service`
