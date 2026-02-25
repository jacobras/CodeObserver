# CodebaseObserver Specification

## Project structure

1. `server/` Ktor server that connects to the database and serves the web app.
2. `cli/` CLI tool to collect the metrics and send them to the server.
3. `web/` Compose web app to display the metrics in a dashboard.

## Server

- Ktor server with JSON API.
- DB: SQLite (embedded).
- Table `metrics`:
    - `createdAt` (TEXT)
    - `projectId` (TEXT)
    - `gitHash` (TEXT)
    - `gitDate` (TEXT)
    - `linesOfCode` (INTEGER)
    - `moduleCount` (INTEGER)
    - `moduleTreeHeight` (INTEGER)
        - Endpoints:
            - `GET /metrics?projectId=...` -> list of records ordered by `gitDate` asc.
            - `POST /metrics/code` -> stores code metrics.
                - body `{ projectId, gitHash, gitDate, linesOfCode }`
            - `POST /metrics/gradle` -> stores gradle metrics.
                - body `{ projectId, gitHash, gitDate, moduleCount, moduleTreeHeight }`
            - `PUT /metrics/{gitHash}` -> updates matching record by `projectId` + `gitHash`.
                - body `{ projectId, gitDate, linesOfCode, moduleCount, moduleTreeHeight }`
            - `DELETE /metrics/{gitHash}` -> deletes matching record
- Table `artifactSizes`:
    - `projectId` (TEXT)
    - `name` (TEXT)
    - `semVer` (TEXT)
    - `size` (INTEGER)
        - Endpoints:
            - `GET /artifactSizes?projectId=...` -> list of records.
            - `POST /artifactSizes` -> stores artifact size.
                - body `{ projectId, name, semVer, size }`
- Projects:
    - `GET /projects` -> list of distinct `projectId` values from `metrics`, sorted asc.

## CLI

- Kotlin Multiplatform CLI (JVM only).
- Running without any command lists available commands.
- Commands:
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
            - Print summary.
    - `measure-gradle`
        - Arguments:
            - `--path` (folder to scan, default `.`)
            - `--server` (server URL to upload results, optional)
            - `--project` (required project identifier)
        - Behavior:
            - Find `settings.gradle.kts` under the given `path`.
            - Count the number of Gradle modules in the project.
            - Send `POST /metrics/gradle` to server with JSON payload including `projectId`.
            - Print summary.
    - `measure-artifact-size`
        - Arguments:
            - `--file` (path to the artifact file, required)
            - `--server` (server URL to upload results, optional)
            - `--project` (required project identifier)
        - Behavior:
            - Send `POST /artifactSizes` to server with JSON payload including `projectId` and `artifactName`.
            - If extension is `.aab`, exclude /base/lib/* that's not arm64-v8a from the size calculation.

## Web

- Compose Multiplatform WASM app.
- Fetch from `GET /metrics?projectId=...`.
- Web app is built and served by the same server host (same origin).
- Display:
    - Line chart of `linesOfCode` vs `gitDate` (using ComposeCharts).
    - Line chart of `moduleCount` vs `gitDate` (using ComposeCharts).
    - Line chart of `moduleTreeHeight` vs `gitDate` (using ComposeCharts).
    - Axis labels: horizontal time, vertical values (lines of code / module count).
    - Time selection: last 7 days/last 30 days/last 6 months/last 12 months.
- Navigation:
    - Top nav bar with main screens:
        - `Dashboard`
        - `Settings`.
- Dashboard app switcher:
    - Switcher at the top of the dashboard to choose between all projects.
    - Options sourced from `GET /projects`.
    - Selected `projectId` is required for all fetches and CRUD operations.
- Edit records:
    - Delete: remove row by `projectId` + `gitHash` -> `DELETE /metrics/{gitHash}`.
- Artifact size chart:
    - Version chart showing artifact sizes across versions.
    - X-axis: versions (sorted using semver).
    - Y-axis: artifact size in bytes.