# User Service Unit Tests

## Goal

Add focused unit tests for the current in-memory user service skeleton so its basic behavior is protected before adding more features.

## Scope

- Add test dependency only where needed.
- Add unit tests for `UserService`.
- Cover registration, duplicate username, login success, login failure, profile lookup, and password redaction in request DTO `toString()`.
- Update `docs/dev-log.md` with learning notes.

## Out of Scope

- Do not add database, Redis, JWT, MyBatis Plus, or real password encryption.
- Do not add controller integration tests yet.
- Do not start Nacos or any external service.
- Do not change product, order, gateway, AI, or message service behavior.

## Implementation Notes

- Prefer plain JUnit 5 tests that instantiate `UserService` directly.
- Keep tests readable for a course project.
- Add `spring-boot-starter-test` to `campus-user` with `test` scope if needed.
- Do not mock `UserService`; test the real service object.

## Verification

Run the full reactor:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" test
```

Run the user module directly when iterating:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -pl campus-user -am test
```

## Learning Notes

- Unit tests can protect service behavior before database integration exists.
- Tests should check outcomes, not internal maps.
- A focused test suite makes future refactors safer.

## Commit Message

`test: add user service unit tests`
