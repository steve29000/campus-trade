# Build Verification Setup

## Goal

Document a reliable Maven verification path so each future feature can be checked before commit.

## Scope

- Record the working Maven command for this local environment.
- Update project documentation with build verification notes.
- Keep the change documentation-only.

## Out of Scope

- Do not install new software.
- Do not add a Maven Wrapper yet.
- Do not change service code or Maven dependencies.

## Implementation Notes

- The shell does not currently expose `mvn` on `PATH`.
- IntelliJ IDEA includes a working Maven binary at:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn"
```

- This command successfully runs the multi-module test lifecycle:

```bash
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" test
```

## Verification

- Run the IntelliJ Maven command above.
- Confirm the parent project and seven child modules report `SUCCESS`.
- Confirm `git status` only contains intended documentation files before commit.

## Learning Notes

- Maven can exist inside an IDE even when `mvn` is not available in the terminal `PATH`.
- Maven downloads dependencies into `~/.m2/repository`.
- In restricted environments, Maven may need permission to write to the local dependency cache.

## Commit Message

`docs: document Maven verification workflow`
