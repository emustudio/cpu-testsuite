# emuStudio Repo Routing

## Current Repository
- `cpu-testsuite` owns the shared CPU instruction test framework, reusable test builders, fixtures, and verification helpers used by emuStudio CPU plugins.

## Sibling Repositories
- `/home/vbmacher/projects/emustudio/emuLib`: shared plugin API, runtime services, shared UI helpers, and reusable utilities.
- `/home/vbmacher/projects/emustudio/edigen`: decoder/disassembler generator from `.eds` specifications.
- `/home/vbmacher/projects/emustudio/emuStudio`: desktop application, bundled plugins, virtual computers, configs, and packaging.
- `/home/vbmacher/projects/emustudio/emustudio.github.io`: website, user documentation, developer documentation, and release-facing pages.
- `/home/vbmacher/projects/emustudio/edigen-gradle-plugin`: Gradle task and DSL integration for Edigen source generation.
- `/home/vbmacher/projects/emustudio/cpu-testsuite`: shared CPU instruction test framework and reusable verification helpers.

## When To Update Which Repository
- Shared CPU test builders, fixture setup, generated test coverage, or reusable verification helpers: update `cpu-testsuite`.
- CPU plugin tests that consume the shared framework: check and update affected plugins in `emuStudio`.
- Shared plugin API or runtime contract used by the test framework: update `emuLib`; then verify affected CPU tests.
- Generator changes that alter decoder/disassembler behavior covered by shared CPU tests: update `edigen`; then verify affected test definitions.
- User or developer documentation for CPU plugin testing: update `emustudio.github.io`.

## Tickets And Commits
- Every change must have an existing GitHub ticket.
- Every commit subject must start with the ticket prefix: `[#123] Short summary`.
- If one task touches multiple emuStudio repositories, use the same ticket prefix in each related commit.
