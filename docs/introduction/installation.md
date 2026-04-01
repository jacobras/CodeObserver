# Installation

## Server

The server is provided as a Docker image.

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

> [!INFO]
> Coming soon.