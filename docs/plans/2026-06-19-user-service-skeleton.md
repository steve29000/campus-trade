# User Service Skeleton

## Goal

Add a minimal, runnable user service API skeleton that demonstrates basic controller-service layering without implementing full authentication yet.

## Scope

- Add simple request and response DTOs under `campus-user`.
- Add an in-memory `UserService` suitable for early API demonstration.
- Add a `UserController` with basic register, login mock, and profile query endpoints.
- Reuse `campus-common` response and result code conventions.
- Update `docs/dev-log.md` and `docs/architecture.md` if needed.

## Out of Scope

- Do not add MySQL, Redis, JWT, MyBatis Plus, or real password encryption yet.
- Do not implement a complete permission system.
- Do not change product, order, gateway, AI, or message service behavior.
- Do not add frontend code.

## Implementation Notes

- Keep data in memory so the service stays easy to run during the course-project skeleton phase.
- Use clear DTO names:
  - `UserRegisterRequest`
  - `UserLoginRequest`
  - `UserProfileResponse`
  - `LoginResponse`
- Use simple validation inside service methods instead of adding validation dependencies.
- Return mock token text from login so later JWT work has an obvious replacement point.

## Verification

- Run:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" test
```

- Confirm the parent project and seven child modules report `SUCCESS`.
- Confirm `git status` only contains intended user-service and documentation files.

## Learning Notes

- How Spring Boot controllers expose HTTP APIs.
- Why DTOs keep request and response shapes clearer than exposing internal objects.
- Why an in-memory service is useful before adding databases.
- How to keep a skeleton feature small enough for one commit.

## Commit Message

`feat: add user service skeleton`
