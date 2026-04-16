# Agent instructions

- See README.md for general information about the project and how to run it.

## Project structure

- `cli/` CLI tool to collect the metrics and send them to the server.
- `desktop/` Standalone desktop app that bundles the server + cli tool.
- `server/` Ktor server that connects to the database and serves the web app.
- `web/` Compose web app to display the metrics in a dashboard.

### Server

- Ktor server with JSON API.
- DB: SQLite (embedded).

### CLI

- Kotlin Multiplatform CLI (JVM only).

### Web

- Compose Multiplatform WASM app.
- Web app is built and served by the same server host (same origin).

## Code style

- No newline at the end of a file.

## Building

- CLI: `gradlew :cli:build`
- Server: `gradlew :server:build`
- Web: `gradlew :web:compileKotlinWasmJs`