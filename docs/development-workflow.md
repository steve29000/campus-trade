# Development Workflow

This project uses small, reviewable development steps. Each step should leave the repository in a runnable or explainable state.

## Working Rhythm

1. Write a short plan before changing code.
2. Implement one small feature or infrastructure improvement.
3. Update project documentation when the architecture or workflow changes.
4. Run the most relevant verification command.
5. Commit with a clear message.
6. Push the branch after the local state is clean.

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
