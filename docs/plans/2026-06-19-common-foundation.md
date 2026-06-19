# Common Foundation

## Goal

Improve `campus-common` so later services can share consistent response and exception conventions.

## Scope

- Refine shared response classes in `campus-common`.
- Add a simple result code model.
- Keep the implementation suitable for a course project.
- Update `README.md`, `docs/dev-log.md`, or `docs/architecture.md` if the public architecture changes.

## Out of Scope

- Do not implement full user, product, order, or AI business logic.
- Do not add database dependencies.
- Do not add complex validation frameworks.
- Do not introduce real AI API integration.

## Implementation Notes

- Keep `ApiResponse` easy to read and easy to explain.
- Keep `BusinessException` lightweight.
- Add only shared code that multiple services will likely use.
- Avoid hiding business meaning behind too much abstraction.

## Verification

- Prefer `mvn test` after Maven is available.
- If Maven is still unavailable, compile simple Java-only classes with `javac --release 17` and document the limitation.
- Confirm `git status` contains only intended files before committing.

## Learning Notes

- Why microservices often use a common response format.
- Why business exceptions are clearer than generic runtime exceptions.
- How to keep a shared module useful without making it a dumping ground.

## Commit Message

`feat: add common response foundation`
