# CodebaseObserver Specification

## Project structure

1. `server/` Ktor server that connects to the database and serves the web app.
2. `cli/` CLI tool to collect the metrics and send them to the server.
3. `web/` Compose web app to display the metrics in a dashboard.

## Server

- Ktor server with JSON API.
- DB: SQLite (embedded).
- Table `counts`:
    - `createdAt` (TEXT)
    - `gitHash` (TEXT)
    - `gitDate` (TEXT)
    - `linesOfCode` (INTEGER)
    - Endpoints:
        - `POST /counts` body `{ gitHash, gitDate, linesOfCode }` -> stores record.
        - `GET /counts` -> list of records ordered by `gitDate` asc.
        - `PUT /counts/{gitHash}` body `{ gitDate, linesOfCode }` -> updates matching record.
        - `DELETE /counts/{gitHash}` -> deletes matching record.
- Table `gradle`:
    - `createdAt` (TEXT)
    - `gitHash` (TEXT)
    - `gitDate` (TEXT)
    - `moduleCount` (INTEGER)
    - `moduleTreeHeight` (INTEGER)
    - Endpoints:
        - `POST /gradle` body `{ gitHash, gitDate, moduleCount, moduleTreeHeight }` -> stores record.
        - `GET /gradle` -> list of records ordered by `gitDate` asc.
        - `PUT /gradle/{gitHash}` body `{ gitDate, moduleCount, moduleTreeHeight }` -> updates matching record.
        - `DELETE /gradle/{gitHash}` -> deletes matching record.

## CLI

- Kotlin Multiplatform CLI (JVM only).
- Running without any command lists available commands.
- Commands:
    - `measure`
        - Arguments:
            - `--path` (folder to scan, default `.`)
            - `--server` (server URL to upload results, optional)
        - Behavior:
            - Runs both `measure-code` and `measure-gradle` sequentially with the same arguments.
            - Print a startup message and summary from both commands.
    - `measure-code`
        - Arguments:
            - `--path` (folder to scan, default `.`)
            - `--server` (server URL to upload results, optional)
            - `--include` glob patterns to include files or folders (comma-separated, optional).
            - `--exclude` glob patterns to exclude files or folders (comma-separated, optional).
        - Behavior:
            - Count lines of code in regular files under the given `path` (recursive).
            - Exclude files and folders matching the glob patterns specified in `--exclude`.
            - Send `POST /counts` to server with JSON payload.
            - Print summary.
    - `measure-gradle`
        - Arguments:
            - `--path` (folder to scan, default `.`)
            - `--server` (server URL to upload results, optional)
        - Behavior:
            - Find `settings.gradle.kts` under the given `path`.
            - Count the number of Gradle modules in the project.
            - Send `POST /gradle` to server with JSON payload.
            - Print summary.

## Web

- Compose Multiplatform WASM app.
- Fetch from `GET /counts` and `GET /gradle`.
- Web app is built and served by the same server host (same origin).
- Display:
    - Line chart of `linesOfCode` vs `gitDate` (using ComposeCharts).
    - Line chart of `moduleCount` vs `gitDate` (using ComposeCharts).
    - Axis labels: horizontal time, vertical values (lines of code / module count).
    - Time selection: last 7 days/last 30 days/last 6 months/last 12 months.
- Navigation:
    - Top nav bar with main screens:
        - `Dashboard`
        - `Settings`.
- Add/update/delete counts:
    - Add: form for `gitHash`, `gitDate`, `linesOfCode` -> `POST /counts`.
    - Update: edit existing row by `gitHash` -> `PUT /counts/{gitHash}`.
    - Delete: remove row by `gitHash` -> `DELETE /counts/{gitHash}`.
- Add/update/delete gradle metrics:
    - Add: form for `gitHash`, `gitDate`, `moduleCount` -> `POST /gradle`.
    - Update: edit existing row by `gitHash` -> `PUT /gradle/{gitHash}`.
    - Delete: remove row by `gitHash` -> `DELETE /gradle/{gitHash}`.