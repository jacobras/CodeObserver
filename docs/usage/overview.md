# Usage

## Prerequisites

First, add a project in the web UI.

The CLI tool requires Java 17+. The GitHub Action requires the `gh` CLI tool to be installed.

## Example use cases

CodeObserver can be used to track:

- 📦 Artifact sizes per version (e.g., release build size, debug build size).
- 🕰️ Build times (e.g., Gradle sync time, release build time, ...).
- 📊 [Code metrics](code_metrics.md): lines of code, Gradle modules, dependency tree height
- ☑️ [Code quality from Detekt](detekt_metrics.md): trends and the latest report
- 🏗️ Tech debt migrations: usage of deprecated modules and imports.

All use cases work with the server (Docker) and standalone installation options.