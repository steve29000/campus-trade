# Development Workflow

This project uses feature-stage development. Each feature stage should be large enough to be useful on its own, while still being split into reviewable commits.

## Working Rhythm

1. Write a feature-stage plan before changing code.
2. Split the stage into implementation, tests, API docs, and architecture/dev-log updates.
3. Implement related changes on the same feature branch with multiple focused commits.
4. Run the most relevant verification command after each meaningful commit.
5. Run full verification before considering the feature stage complete.
6. Push the branch after the local state is clean.

## Branch Strategy

Use one feature branch for a complete feature stage. Small follow-up changes should be committed on the same branch instead of creating a new branch each time.

Recommended branch examples:

- `feature/user-service`
- `feature/ai-service`
- `feature/product-service`
- `feature/gateway-routing`

Within one feature branch, prefer several small commits over one large commit.

## Feature Stage Size

A feature stage should usually include the complete first version of one service capability, not just one tiny edit. For example:

- `feature/ai-service`: mock provider, controller endpoints, unit tests, API examples, architecture/dev-log updates.
- `feature/user-service`: service skeleton, unit tests, API examples, later persistence/auth follow-ups if they still belong to the same user-service stage.
- `feature/product-service`: product DTOs, service/controller, AI integration point, tests, API docs, architecture/dev-log updates.

Tiny fixes can still be separate commits, but they should usually stay inside the current feature branch.

## Feature Step Template

Each feature step should answer:

- Goal: what this step adds or improves.
- Scope: what files or modules may change.
- Out of scope: what must not be implemented yet.
- Verification: what command or manual check proves the step works.
- Learning notes: what a student should understand after the step.
- Commit message: the suggested Git commit title.

## Recommended First Milestones

1. Project framework and documentation.
2. Common response and exception foundation.
3. Maven and Java build verification.
4. User service skeleton.
5. AI mock service.
6. Product service with AI integration point.
7. Gateway routing verification.
8. Order service skeleton.
9. Database schema documentation.
10. Final course report polishing.

## Commit Rules

- Keep commits small and focused.
- Avoid mixing unrelated modules in one commit.
- Update `docs/dev-log.md` after meaningful learning or architecture changes.
- Prefer readable commit messages, for example:
  - `chore: initialize project framework`
  - `feat: add common response foundation`
  - `docs: update architecture notes`

## Verification Notes

Use the strongest available verification for the current step:

- Maven project changes: `mvn test`
- Single module changes: `mvn -pl <module> test`
- Documentation-only changes: review file list and Markdown content
- GitHub publishing: confirm `git status` is clean and remote branch exists

If `mvn` is not available on `PATH`, this project's current macOS development environment can use IntelliJ IDEA's bundled Maven:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" test
```
