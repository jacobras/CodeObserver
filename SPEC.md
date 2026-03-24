# CodebaseObserver Specification

## Project structure

1. `server/` Ktor server that connects to the database and serves the web app.
2. `cli/` CLI tool to collect the metrics and send them to the server.
3. `web/` Compose web app to display the metrics in a dashboard.

## Server

- Ktor server with JSON API.
- DB: SQLite (embedded).
- Tables and endpoints are listed below, per feature.

## CLI

- Kotlin Multiplatform CLI (JVM only).
- Running without any command lists available commands.
- Commands are listed below, per feature.

## Web

- Compose Multiplatform WASM app.
- Web app is built and served by the same server host (same origin).
- Navigation:
    - Top nav bar with main screens:
        - `Dashboard`
        - `Settings`.
- Dashboard app switcher
    - Switcher at the top of the dashboard to choose between all projects.
    - Options sourced from `GET /projects`.
    - Selected `projectId` is required for all fetches and CRUD operations.
- Features are listed below.

## Features

### Projects

- Tables:
    - `projects`
        - `projectId` (TEXT)
        - `name` (TEXT)
- Endpoints:
    - Projects:
        - `GET /projects` -> list of distinct `projectId` values from `metrics`, sorted asc.
        - `DELETE /projects/{projectId}` -> deletes the project and all associated data.
- Web app:
    - Fetch from `GET /metrics?projectId=...`.
    - Settings screen allows editing projects.

### Code metrics

- Tables:
    - `metrics`
        - `createdAt` (LONG)
        - `projectId` (TEXT)
        - `gitHash` (TEXT)
        - `gitDate` (LONG) (epoch seconds)
        - `linesOfCode` (INTEGER)
        - `moduleCount` (INTEGER)
        - `moduleTreeHeight` (INTEGER)
- Endpoints:
    - Metrics:
        - `GET /metrics?projectId=...` -> list of `CodeMetricsDto` records.
        - `DELETE /metrics/{gitHash}` -> deletes matching record
    - Code metrics:
        - `POST /metrics/code` -> stores code metrics.
            - body `{ projectId, gitHash, gitDate, linesOfCode }`
    - Gradle metrics:
        - `POST /metrics/gradle` -> stores gradle metrics.
            - body `{ projectId, gitHash, gitDate, moduleCount, moduleTreeHeight, graph, moduleDetails }`
            - graph is Map<String, List<String>>
            - moduleDetails is optional (e.g. `moduleA[android],moduleB[kmp]`)
- CLI commands:
    - `measure`
        - Arguments:
            - `--path` (folder to scan, default `.`)
            - `--server` (server URL to upload results, optional)
            - `--project` (required project identifier)
        - Behavior:
            - Runs both `measure-code` and `measure-gradle` sequentially with the same arguments.
            - Print a startup message and summary from both commands.
    - `measure-code`
        - Arguments:
            - `--path` (folder to scan, default `.`)
            - `--server` (server URL to upload results, optional)
            - `--project` (required project identifier)
            - `--include` glob patterns to include files or folders (comma-separated, optional).
            - `--exclude` glob patterns to exclude files or folders (comma-separated, optional).
        - Behavior:
            - Count lines of code in regular files under the given `path` (recursive).
            - Exclude files and folders matching the glob patterns specified in `--exclude`.
            - Send `POST /metrics/code` to server with JSON payload including `projectId`.
            - If `--server` is provided:
                - Fetch all `importUsage` migrations for the project via `GET /migrations?projectId=...`.
                - For each migration, count the number of `import {rule}` statements across all scanned files.
                - Upload each count via `POST /migrationProgress` with `{ migrationId, gitHash, gitDate, count }`.
            - Print summary.
            - Show progress updates for every 1000 files.
                - If `--server` is provided, fetch the last known `linesOfCode` for the project via
                  `GET /metrics?projectId=...` before scanning, and use it to show an estimated progress percentage in
                  each update.
    - `measure-gradle`
        - Arguments:
            - `--path` (folder to scan, default `.`)
            - `--server` (server URL to upload results, optional)
            - `--project` (required project identifier)
        - Behavior:
            - Find `settings.gradle.kts` under the given `path`.
            - Count the number of Gradle modules in the project.
            - For each module, scan its `build.gradle.kts` for plugin IDs.
            - Send `POST /metrics/gradle` to server with JSON payload including `projectId`.
            - If `--server` is provided:
                - Fetch module identifiers for the project via `GET /moduleTypeIdentifiers?projectId=...`.
                - For each module, determine its type by matching scanned plugin IDs against module identifiers
                  (the identifier with the lowest `order` wins when multiple match).
                - Include `moduleDetails` in the request (e.g. `moduleA[android],moduleB[kmp]`).
                - Fetch all `moduleUsage` migrations for the project via `GET /migrations?projectId=...`.
                - For each migration, count the number of dependencies in the module graph that point to the migration's
                  `rule` module.
                - Upload each count via `POST /migrationProgress` with `{ migrationId, gitHash, gitDate, count }`.
            - Print summary.
- Web app:
    - Dashboard tab `Code trends`
        - Line chart of `linesOfCode` vs `gitDate`.
        - Line chart of `moduleCount` vs `gitDate`.
        - Line chart of `moduleTreeHeight` vs `gitDate`.
        - Axis labels: horizontal time, vertical values (lines of code / module count).
        - Time selection: last 7 days/last 30 days/last 6 months/last 12 months.
        - Edit records
            - Delete: remove row by `projectId` + `gitHash` -> `DELETE /metrics/{gitHash}`.

### Artifact sizes

- Tables:
    - `artifactSizes`
        - `projectId` (TEXT)
        - `name` (TEXT)
        - `semVer` (TEXT)
        - `size` (INTEGER)
- Endpoints:
    - Artifact sizes:
        - `GET /artifactSizes?projectId=...` -> list of records.
        - `POST /artifactSizes` -> stores artifact size.
            - body `{ projectId, name, semVer, size }`
- CLI commands:
    - `measure-artifact-size`
        - Arguments:
            - `--file` (path to the artifact file, required)
            - `--server` (server URL to upload results, optional)
            - `--project` (required project identifier)
        - Behavior:
            - Send `POST /artifactSizes` to server with JSON payload including `projectId` and `artifactName`.
            - If extension is `.aab`, exclude /base/lib/* that's not arm64-v8a from the size calculation.
- Web app:
    - Dashboard tab `Artifact sizes`
        - Artifact size chart
            - Version chart showing artifact sizes across versions.
            - X-axis: versions (sorted using semver).
            - Y-axis: artifact size in bytes.

### Build times

- Tables:
    - `buildTimes`
        - `projectId` (TEXT)
        - `buildName` (TEXT)
        - `gitHash` (TEXT)
        - `gitDate` (LONG) (epoch seconds)
        - `timeSeconds` (INTEGER)
- Endpoints:
    - Build times:
        - `GET /buildTimes?projectId=...` -> list of records.
        - `POST /buildTimes` -> stores a build time record.
            - body `{ projectId, buildName, gitHash, gitDate, timeSeconds }`
- CLI commands:
    - `report-build-time`
        - Arguments:
            - `--server` (server URL to upload results, required)
            - `--project` (required project identifier)
            - `--name` (build name, required)
            - `--time` (build time in seconds, required)
        - Behavior:
            - Send `POST /buildTimes` to server with JSON payload
              `{ projectId, buildName, gitHash, gitDate, timeSeconds }`.
            - Print summary.
- Web app:
    - Dashboard tab `Build times`
        - Line chart of build time vs `gitDate`.
        - X-axis: date.
        - Y-axis: time, formatted using `HumanReadable.duration(time)`.
        - Time selection.

### Migrations

- Tables:
    - `migrations`
        - `id` (INTEGER) (auto-incremented)
        - `createdAt` (LONG)
        - `name` (TEXT)
        - `description` (TEXT)
        - `projectId` (TEXT)
        - `type` (TEXT) (one of `moduleUsage`, `importUsage`)
        - `rule` (TEXT)
            - in case of `moduleUsage` rule is a module name, e.g. `util:deprecated`.
            - in case of `importUsage` rule is an import, e.g. `com.example.lib.Foo`.
                - or a wildcard, e.g. `com.example.lib.*` (matches any import under that package).
    - `migrationProgress`
        - `migrationId` (INTEGER) (points to `migrations` table)
        - `gitHash` (TEXT)
        - `gitDate` (LONG) (epoch seconds)
        - `count` (INT) (number of times the rule was matched)
- Endpoints:
    - Migrations:
        - `GET /migrations?projectId=...` -> list of all migrations.
        - `POST /migrations` -> stores a new migration.
            - body `{ projectId, name, description, type, rule }`
        - `PATCH /migrations/{id}` -> updates the migration name.
            - body `{ name, description }`
        - `DELETE /migrations/{id}` -> deletes the migration and all its progress records.
    - Migration progress:
        - `GET /migrationProgress?migrationId=...` -> list of migration progress records.
        - `POST /migrationProgress` -> stores migration progress.
            - body `{ migrationId, gitHash, gitDate, count }`
        - `DELETE /migrationProgress/{migrationId}/{gitHash}` -> deletes a single progress record.
- CLI commands:
    - Included in `measure-code` command.
- Web app:
    - Dashboard tab `Migrations`
        - Tab for each migration
            - Chart of migration progress.
            - Data table of migration progress.
        - Tab "Overview" that shows all migration configurations.
            - Data table with edit/delete functionality.
            - Form to add new migration.

### Module graph

- Tables:
    - `moduleGraph` (always holds one recorded per project)
        - `projectId` (TEXT)
        - `gitHash` (TEXT)
        - `gitDate` (LONG) (epoch seconds)
        - `graph` (TEXT) (serialized as `{ "moduleA": ["dep1", "dep2"] }`)
        - `moduleDetails` (TEXT) (serialized as `moduleA[android],moduleB[kmp],moduleC[java]`)
    - `moduleTypeIdentifiers`
        - `id` (INTEGER) (auto-incremented)
        - `projectId` (TEXT)
        - `typeName` (TEXT) (e.g. `android`, `kmp`, `java`)
        - `plugin` (TEXT) (e.g. `libs.plugins.androidApplication` or `kotlin("multiplatform")`)
        - `order` (INTEGER) (lower value wins when multiple types match a module)
        - `color` (TEXT) (hex color code, e.g. `#FF0000`)
    - `moduleGraphSettings`
        - `id` (INTEGER) (auto-incremented)
        - `createdAt` (LONG)
        - `projectId` (TEXT)
        - `type` (TEXT) (one of `deprecatedModule`, `forbiddenDependency`)
        - `data` (TEXT)
            - in case of `deprecatedModule` setting is a module name, e.g. `util:deprecated`.
            - in case of `forbiddenDependency` setting is a dependency, e.g. `* -> moduleB` or `moduleA -> *`.
- Endpoints:
    - Modules:
        - `GET /modules?projectId=...` -> list of all modules in a project, from the `moduleGraph` table.
    - Module graph:
        - `GET /moduleGraph?projectId=...&startModule=...&groupingThreshold=...` -> mermaid graph string.
            - Modules are colored according to their identifier color (from `moduleTypeIdentifiers`) when
              `moduleDetails` is available.
    - Module identifiers:
        - `GET /moduleTypeIdentifiers?projectId=...` -> list of module identifier records.
        - `POST /moduleTypeIdentifiers` -> creates a module identifier.
        - body `{ projectId, typeName, plugin, order, color }`
            - `PATCH /moduleTypeIdentifiers/{id}` -> updates a module identifier.
                - body `{ typeName, plugin, order, color }`
            - `DELETE /moduleTypeIdentifiers/{id}` -> deletes the module identifier.
- CLI commands:
    - Included in `measure-gradle` command.
- Web app:
    - Dashboard tab `Module graph`
        - Shows the module graph using the `DependencyGraph()` composable.
        - List to select a start module.
        - Tweakable grouping threshold and layer depth.
    - Dashboard tab `Module rules`
        - Shows a data table with all module graph settings.
        - Form to add/edit/delete module graph settings.
    - Dashboard tab `Module types`
        - Shows a data table with all module types.
        - Form to add/edit/delete module types (name, identifying plugin, order, color).