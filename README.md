# CodeObserver

> [!WARNING]
> ⚠️ Under development, expect breaking changes.

CodeObserver is a CLI tool plus a dashboard to measure codebase metrics over time. Current supported metrics:

- 📦 **Artifacts**
    - Artifact size per version (e.g., release build size, debug build size).
- 🕰️ **Builds**
    - Build time (e.g., Gradle sync time, release build time, ...).
- 📜 **Code**
    - Lines of code (simple count, no comment/blank line exclusion).
- ☑️ **Code quality (from Detekt)**
    - Detekt trends (total findings and smells per 1,000 lines of code).
    - Latest Detekt HTML report.
- 🐘 **Gradle**
    - Number of Gradle modules.
    - Height of the Gradle dependency tree (the longest path from a module to its root consumer).
    - Module graph with type highlighting (e.g., Android module, KMP module, JVM module, ...).
- 🏗️ **Tech debt migrations**
    - Usage of deprecated modules and imports.

A demo dashboard is available online at <https://jacobras.github.io/CodeObserver/demo/>.

Everything's Kotlin. The Ktor server is bundled into a Docker image. The dashboard UI is built with Compose
Multiplatform.

A full technical and functional overview is available in the [specification document](SPEC.md).

## Documentation/Quickstart

All documentation is available to read online at <https://jacobras.github.io/CodeObserver/>.

### Installation

See [installation instructions](docs/introduction/installation.md).

### Contributing

See [contributing guidelines](docs/introduction/contributing.md).

## Assisted coding policy

Coding LLMs were and are allowed to use for bootstrapping features and reviewing code. Polishing was done manually.

The specification in `SPEC.md` needs to stay up to date with the latest changes to the codebase. This is also used to
review changes in PRs.

Every generated line of code still needs to be reviewed by a human developer.