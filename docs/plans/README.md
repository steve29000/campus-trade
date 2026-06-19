# Development Plans

This directory stores short implementation plans before each small development step.

Keep plans concise and practical. A good plan should let another developer understand exactly what will change before code is written.

## Plan File Naming

Use this format:

```text
YYYY-MM-DD-short-topic.md
```

Examples:

```text
2026-06-19-common-foundation.md
2026-06-20-user-service-skeleton.md
```

## Plan Template

```markdown
# <Feature or Step Name>

## Goal

Describe the one thing this step will accomplish.

## Scope

- Files or modules allowed to change.
- Public behavior expected after the step.

## Out of Scope

- Features intentionally left for later.

## Implementation Notes

- Main classes, configs, or docs to update.
- Important design choices.

## Verification

- Commands or checks to run.

## Learning Notes

- Concepts the student should understand after this step.

## Commit Message

`type: short description`
```
