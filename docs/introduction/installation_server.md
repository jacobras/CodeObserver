# Installation (server)

Server installation allows for CI integration. The server is provided as a Docker image.

### Docker Compose

Example `docker-compose.yml`:

```yaml
services:
    app:
        image: mrras/code-observer:0.5.0
        container_name: code-observer
        ports:
            - "8080:8080" # Update this to your desired port ('external:internal', where 'internal' needs to be 8080)
        volumes:
            - YourLocationHere:/data # Update this to where you'd like to store the data
        environment:
            - DB_PATH=/data/app.db
        restart: unless-stopped
```

Now start the server with `docker-compose up -d`.

## CLI tool

The CLI tool feeds data to the server.

### Prerequisites

1. Java 17+ needs to be available.
2. [GitHub CLI](https://cli.github.com) needs to be available.

These are both preinstalled on GitHub-hosted runners.

### Example workflow

```yaml
name: CodeObserver

on:
  push:
      branches: [main]
  workflow_dispatch:

jobs:
  analyse:
    runs-on: ubuntu-latest
    timeout-minutes: 10
    steps:
      - uses: actions/checkout@v6

      - name: CodeObserver
        uses: jacobras/CodeObserver@v0.5
        with:
          server-url: ${{ secrets.CODEOBSERVER_SERVER_URL }}
          project-id: your-project-id
```