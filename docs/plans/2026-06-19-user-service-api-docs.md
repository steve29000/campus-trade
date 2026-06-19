# User Service API Documentation

## Goal

Document the current user service skeleton endpoints so the project can be understood and demonstrated without reading controller code.

## Scope

- Add a concise API document for `campus-user`.
- Link the API document from `README.md`.
- Update `docs/dev-log.md` with learning notes.

## Out of Scope

- Do not change Java code.
- Do not add Swagger or Knife4j yet.
- Do not add controller integration tests.
- Do not change gateway routing or Nacos configuration.
- Do not document database/JWT behavior as implemented.

## Implementation Notes

- Document only the current skeleton behavior:
  - `POST /user/register`
  - `POST /user/login`
  - `GET /user/{id}`
- Include example request and response JSON.
- Clearly state that user data is in-memory and token is mock-only in this phase.
- Keep the wording portfolio-friendly and course-project appropriate.

## Verification

- Review Markdown content and links.
- Run the full reactor:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" test
```

## Learning Notes

- API docs let readers use the service without reading Java code first.
- Documentation should match current behavior, not planned future behavior.
- Lightweight docs can come before Swagger/Knife4j integration.

## Commit Message

`docs: add user service API examples`
