# CodebaseObserver Specification

## Project structure

1. `server/` Ktor server that connects to the database and serves the web app.
2. `cli/` CLI tool to collect the metrics and send them to the server.
3. `web/` Compose web app to display the metrics in a dashboard.

## Server

- Ktor server with JSON API.
- DB: SQLite (embedded). Table:
    - `createdAt` (TEXT)
    - `gitHash` (TEXT)
    - `gitDate` (TEXT)
    - `fileCount` (INTEGER)
    - Endpoints:
        - `POST /counts` body `{ gitHash, gitDate, fileCount }` -> stores record.
        - `GET /counts` -> list of records ordered by `gitDate` asc.
        - `PUT /counts/{gitHash}` body `{ gitDate, fileCount }` -> updates matching record.
        - `DELETE /counts/{gitHash}` -> deletes matching record.

## CLI

- Kotlin Multiplatform CLI (JVM only).
- Running without any command lists available commands.
- Commands:
    - `count-files`
        - Arguments:
            - `--path` (folder to scan, default `.`)
            - `--server` (server URL to upload results, optional)
            - `--extensions` optional file filter (comma-separated).
            - `--exclude` glob patterns to exclude files or folders (comma-separated, optional).
        - Behavior:
            - Count regular files under the given `path` (recursive).
            - Exclude files and folders matching the glob patterns specified in `--exclude`.
            - Send `POST /counts` to server with JSON payload.
            - Print summary.

## Web

- Compose Multiplatform WASM app.
- Fetch from `GET /counts`.
- Web app is built and served by the same server host (same origin).
- Display:
    - Line chart of `fileCount` vs `gitDate` (using ComposeCharts).
    - Axis labels: horizontal time, vertical file count.
    - Time selection: last 7 days/last 30 days/last 12 months.
- Navigation:
    - Top nav bar with `Dashboard` (current screen) and `Settings`.
- Add/update/delete counts:
    - Add: form for `gitHash`, `gitDate`, `fileCount` -> `POST /counts`.
    - Update: edit existing row by `gitHash` -> `PUT /counts/{gitHash}`.
    - Delete: remove row by `gitHash` -> `DELETE /counts/{gitHash}`.